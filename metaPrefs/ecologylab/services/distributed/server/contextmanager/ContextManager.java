package ecologylab.services.nio;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.concurrent.LinkedBlockingQueue;

import ecologylab.appframework.ObjectRegistry;
import ecologylab.generic.Debug;
import ecologylab.services.ServerConstants;
import ecologylab.services.ServicesServer;
import ecologylab.services.exceptions.BadClientException;
import ecologylab.services.messages.RequestMessage;
import ecologylab.services.messages.ResponseMessage;
import ecologylab.xml.ElementState;
import ecologylab.xml.TranslationSpace;
import ecologylab.xml.XmlTranslationException;

/**
 * Stores information about the connection context for the client on the server.
 * Should be extended for more specific implementations. Handles accumulating
 * incoming messages and translating them into RequestMessage objects, as well
 * as the ability to perform the messages' services and send their responses.
 * 
 * Generally, this class can be driven by one or more threads, depending on the
 * desired functionality.
 * 
 * @author Zach Toups
 */
public class ContextManager extends Debug implements ServerConstants
{
    /**
     * Stores incoming character data until it can be parsed into an XML message
     * and turned into a Java object.
     */
    private StringBuilder                         incomingMessageBuffer  = new StringBuilder(
                                                                                 MAX_PACKET_SIZE);

    protected boolean                             messageWaiting         = false;

    private RequestMessage                        request;

    protected LinkedBlockingQueue<RequestMessage> requestQueue           = new LinkedBlockingQueue<RequestMessage>();

    private Object                                token                  = null;

    private CharBuffer                            outgoingChars          = CharBuffer
                                                                                 .allocate(MAX_PACKET_SIZE);

    private final static CharsetEncoder           encoder                = Charset
                                                                                 .forName(
                                                                                         CHARACTER_ENCODING)
                                                                                 .newEncoder();

    protected long                                initialTimeStamp       = System
                                                                                 .currentTimeMillis();

    protected boolean                             receivedAValidMsg;

    protected ObjectRegistry                      registry;

    private int                                   badTransmissionCount;

    NIOServerBackend                              server;

    protected SocketChannel                       socket;

    private int                                   endOfFirstHeader       = -1;

    /**
     * Counts how many characters still need to be extracted from the
     * incomingMessageBuffer before they can be turned into a message (based
     * upon the HTTP header). A value of -1 means that there is not yet a
     * complete header, so no length has been determined (yet).
     */
    private int                                   contentLengthRemaining = -1;

    /**
     * Stores the first XML message from the incomingMessageBuffer, or parts of
     * it (if it is being read over several invocations).
     */
    StringBuilder                                 firstMessageBuffer     = new StringBuilder();

    /**
     * Used to translate incoming message XML strings into RequestMessages.
     */
    private TranslationSpace                      translationSpace;

    public ContextManager(Object token, /* SelectionKey key, */
    NIOServerBackend server, SocketChannel socket,
            TranslationSpace translationSpace, ObjectRegistry registry)
    {
        this.token = token;

        this.socket = socket;
        this.server = server;
        // this.key = key;

        // channel = (SocketChannel) key.channel();
        //
        this.registry = registry;
        this.translationSpace = translationSpace;

        System.out.println("my server is: " + this.server);
    }

    /**
     * @return the next message in the requestQueue.
     */
    protected RequestMessage getNextMessage()
    {
        synchronized (requestQueue)
        {
            int queueSize = requestQueue.size();

            if (queueSize == 1)
            {
                messageWaiting = false;
            }

            // return null if none left, or the next Request otherwise
            return requestQueue.poll();
        }
    }

    /**
     * Calls performService on the given RequestMessage using the local
     * ObjectRegistry. Can be overridden by subclasses to provide more
     * specialized functionality.
     * 
     * @param requestMessage
     * @return
     */
    protected ResponseMessage performService(RequestMessage requestMessage)
    {
        requestMessage.setSender(this.socket.socket().getInetAddress());

        return requestMessage.performService(registry);
    }

    /**
     * Calls performService(requestMessage), then converts the resulting
     * ResponseMessage into a String, adds the HTTP-like headers, and passes the
     * final String to the server backend for sending to the client.
     * 
     * @param request
     */
    private void processRequest(RequestMessage request)
    {
        ResponseMessage response = null;

        if (request == null)
        {
            debug("No request.");
        }
        else
        {
            // perform the service being requested
            response = performService(request);

            if (response != null)
            { // if the response is null, then we do
                // nothing else
                try
                {
                    response.setUid(request.getUid());

                    String responseString = this
                            .translateResponseMessageToString(request, response);

                    outgoingChars.clear();
                    outgoingChars.put(responseString);
                    outgoingChars.flip();

                    ByteBuffer outgoingBuffer = encoder.encode(outgoingChars);

                    server.send(this.socket, outgoingBuffer);
                }
                catch (XmlTranslationException e)
                {
                    e.printStackTrace();
                }
                catch (CharacterCodingException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Translates response into an XML string and adds an HTTP-like header, then
     * returns the result.
     * 
     * This method may be overridden to provide more specific functionality; for
     * example, for servers that do not use HTTP-like headers or that use
     * customized messages instead of XML.
     * 
     * @param requestMessage -
     *            the current request.
     * @param responseMessage -
     *            the ResponseMessage generated by processing requestMessage.
     * @return a String that constitutes a complete response message in XML with
     *         HTTP-like headers.
     */
    protected String translateResponseMessageToString(
            RequestMessage requestMessage, ResponseMessage responseMessage)
            throws XmlTranslationException
    {
        String outgoingResp = responseMessage.translateToXML(false);

        return "content-length:" + outgoingResp.length() + "\r\n\r\n"
                + outgoingResp;
    }

    public void processNextMessageAndSendResponse()
    {
        this.processRequest(this.getNextMessage());
    }

    /**
     * Calls processNextMessageAndSendResponse() on each queued message.
     * 
     * Can be overridden for more specific functionality.
     * 
     * @throws BadClientException
     */
    public void processAllMessagesAndSendResponses() throws BadClientException
    {
        timeoutBeforeValidMsg();
        while (isMessageWaiting())
        {
            this.processNextMessageAndSendResponse();
            timeoutBeforeValidMsg();
        }
    }

    /**
     * Checks the time since the last valid message. If the time has been too
     * long (MAX_TIME_BEFORE_VALID_MSG), throws a BadClientException, which the
     * server will deal with.
     * 
     * If the time has not been too long, updates the time stamp.
     * 
     * @throws BadClientException
     */
    void timeoutBeforeValidMsg() throws BadClientException
    {
        long now = System.currentTimeMillis();
        long elapsedTime = now - this.initialTimeStamp;
        if (elapsedTime >= MAX_TIME_BEFORE_VALID_MSG)
        {
            throw new BadClientException(this.socket.socket().getInetAddress()
                    .getHostAddress(),
                    "Too long before valid response: elapsedTime="
                            + elapsedTime + ".");
        }
        else
        {
            this.initialTimeStamp = now;
        }
    }

    /**
     * @return Returns the token.
     */
    public Object getToken()
    {
        return token;
    }

    /**
     * @return Returns the messageWaiting.
     */
    public boolean isMessageWaiting()
    {
        return messageWaiting;
    }

    /**
     * Hook method to provide specific functionality.
     * 
     * @param messageString
     * @return
     * @throws XmlTranslationException
     * @throws UnsupportedEncodingException
     */
    protected RequestMessage translateXMLStringToRequestMessage(
            String messageString) throws XmlTranslationException,
            UnsupportedEncodingException
    {
        return (RequestMessage) ElementState.translateFromXMLString(
                messageString, translationSpace);
    }

    /**
     * Takes an incoming message in the form of an XML String and converts it
     * into a RequestMessage. Then places the RequestMessage on the
     * requestQueue.
     * 
     * @param incomingMessage
     * @throws BadClientException
     */
    private void processString(String incomingMessage)
            throws BadClientException
    {
        if (show(5))
        {
            debug("processing: " + incomingMessage);
            debug("translationSpace: " + translationSpace.toString());
        }

        request = null;
        try
        {
            request = this.translateXMLStringToRequestMessage(incomingMessage);
        }
        catch (XmlTranslationException e)
        {
            // drop down to request == null, below
        }
        catch (UnsupportedEncodingException e)
        {
            // drop down to request == null, below
        }

        if (request == null)
        {
            debug("ERROR: " + incomingMessage);
            if (++badTransmissionCount >= MAXIMUM_TRANSMISSION_ERRORS)
            {
                throw new BadClientException(this.socket.socket()
                        .getInetAddress().getHostAddress(),
                        "Too many Bad Transmissions: " + badTransmissionCount);
            }
            // else
            error("ERROR: translation failed: badTransmissionCount="
                    + badTransmissionCount);
        }
        else
        {
            receivedAValidMsg = true;
            badTransmissionCount = 0;

            synchronized (requestQueue)
            {
                this.enqueueRequest(request);
            }
        }
    }

    /**
     * Adds the given request to this's request queue.
     * 
     * enqueueRequest(RequestMessage) is a hook method for ContextManagers that
     * need to implement other functionality, such as prioritizing messages.
     * 
     * @param request
     */
    protected void enqueueRequest(RequestMessage request)
    {
        if (requestQueue.offer(request))
        {
            messageWaiting = true;
        }
    }

    /**
     * Converts the given bytes into chars, then extracts any messages from the
     * chars and enqueues them.
     * 
     * @param message
     */
    public void enqueueStringMessage(CharBuffer message)
            throws CharacterCodingException, BadClientException
    {
        incomingMessageBuffer.append(message);

        // look for HTTP header
        while (incomingMessageBuffer.length() > 0)
        {
            // debug("START: buffer size: " + incomingMessageBuffer.length());
            // debug("buffer contents: " + incomingMessageBuffer.toString());

            if (endOfFirstHeader == -1)
                endOfFirstHeader = incomingMessageBuffer.indexOf("\r\n\r\n");

            if (endOfFirstHeader == -1)
            { /*
                 * no header yet; if it's too large, bad client; if it's not too
                 * large yet, just exit, it'll get checked again when more data
                 * comes in the pipe
                 */
                if (incomingMessageBuffer.length() > ServerConstants.MAX_HTTP_HEADER_LENGTH)
                {
                    // clear the buffer
                    incomingMessageBuffer.delete(0, incomingMessageBuffer
                            .length());
                    throw new BadClientException(this.socket.socket()
                            .getInetAddress().getHostAddress(),
                            "Maximum HTTP header length exceeded.");
                }

                break;
            }
            /*
             * we have the end of the first header; either just now or from a
             * previous invokation of this method. If we have it, but don't have
             * the remaining content length, then we first need to extract that
             * from the header.
             */
            if (contentLengthRemaining == -1)
            {
                try
                {
                    contentLengthRemaining = ServicesServer
                            .parseHeader(incomingMessageBuffer.substring(0,
                                    endOfFirstHeader));

                    // if we got here, endOfFirstHeader is not -1, so we
                    // need to
                    // add
                    // 4 to it to ensure we're just after all the header
                    // (including the four termination markers)
                    endOfFirstHeader += 4;

                    // done with the header; kill it
                    incomingMessageBuffer.delete(0, endOfFirstHeader);
                }
                catch (IllegalStateException e)
                {
                    throw new BadClientException(this.socket.socket()
                            .getInetAddress().getHostAddress(),
                            "Malformed header.");
                }
            }

            // if we still don't have the remaining length, then there was a
            // problem
            if (contentLengthRemaining == -1)
                break;

            // make sure contentLength isn't too big
            if (contentLengthRemaining > ServerConstants.MAX_PACKET_SIZE)
            {
                throw new BadClientException(this.socket.socket()
                        .getInetAddress().getHostAddress(),
                        "Specified content length too large: "
                                + contentLengthRemaining);
            }

            try
            {

                // see if the incoming buffer has enough characters to
                // include the specified content length
                if (incomingMessageBuffer.length() >= contentLengthRemaining)
                {
                    // debug("buffer size >= contentLengthRemaining:
                    // "+incomingMessageBuffer.length()+" >=
                    // "+contentLengthRemaining);
                    firstMessageBuffer.append(incomingMessageBuffer.substring(
                            0, contentLengthRemaining));

                    incomingMessageBuffer.delete(0, contentLengthRemaining);

                    // reset to do a new read on the next invocation
                    contentLengthRemaining = -1;
                    endOfFirstHeader = -1;
                }
                else
                {
                    // debug("buffer size < contentLengthRemaining:
                    // "+incomingMessageBuffer.length()+" <
                    // "+contentLengthRemaining);

                    firstMessageBuffer.append(incomingMessageBuffer);

                    // indicate that we need to get more from the buffer in
                    // the next invocation
                    contentLengthRemaining -= incomingMessageBuffer.length();
                    endOfFirstHeader = 0;

                    incomingMessageBuffer.delete(0, incomingMessageBuffer
                            .length());
                }

                // debug("first message contents:
                // "+firstMessageBuffer.toString());
                // debug("buffer contents:
                // "+incomingMessageBuffer.toString());
            }
            catch (NullPointerException e)
            {
                e.printStackTrace();
            }

            if ((firstMessageBuffer != null)
                    && (firstMessageBuffer.length() > 0)
                    && (contentLengthRemaining == -1))
            { // if we've read a complete message, then
                // contentLengthRemaining
                // will be reset to -1
                processString(firstMessageBuffer.toString());
                firstMessageBuffer.delete(0, firstMessageBuffer.length());
            }
            else
            {
                // debug("first message buffer null? "
                // + (firstMessageBuffer == null));
                // debug("first message buffer empty? "
                // + (firstMessageBuffer.length() == 0));
                // debug("content length remaining? " +
                // contentLengthRemaining);
                // debug("end of first header index? " + endOfFirstHeader);
                debug("first message contents: "
                        + (firstMessageBuffer.toString()));
                debug("buffer contents: " + (incomingMessageBuffer.toString()));
            }
        }
    }

    /**
     * Hook method for having shutdown behavior.
     * 
     * This method is called whenever the client terminates their connection or
     * when the server is shutting down.
     */
    public void shutdown()
    {

    }
}
