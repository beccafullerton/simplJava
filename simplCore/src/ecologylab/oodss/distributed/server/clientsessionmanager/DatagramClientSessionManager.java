/**
 * 
 */
package ecologylab.oodss.distributed.server.clientsessionmanager;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;

import ecologylab.collections.Scope;
import ecologylab.oodss.distributed.server.NIODatagramServer;
import ecologylab.oodss.messages.RequestMessage;
import ecologylab.oodss.messages.ResponseMessage;
import ecologylab.oodss.messages.UpdateMessage;

/**
 * @author Zachary O. Toups (zach@ecologylab.net)
 * 
 */
public class DatagramClientSessionManager<S extends Scope, PARENT extends Scope> extends BaseSessionManager<S, PARENT>
{
	InetSocketAddress	address;
	
	NIODatagramServer server;
	
	SelectionKey mostRecentKey;
	
	/**
	 * @param sessionId
	 * @param socket
	 * @param baseScope
	 */
	public DatagramClientSessionManager(String sessionId, NIODatagramServer server,
			SelectionKey socket, PARENT baseScope, InetSocketAddress	address)
	{
		super(sessionId, server, socket, baseScope);
		
		this.address = address;
		this.server = server;
	}

	/**
	 * Calls RequestMessage.performService(Scope) and returns the result.
	 * 
	 * @param request
	 *          - the request message to process.
	 */
	@Override
	public ResponseMessage processRequest(RequestMessage request, InetAddress address)
	{
		return super.processRequest(request, address);
	}

	@Override
	public InetSocketAddress getAddress()
	{
		return address;
	}

	@Override
  public void sendUpdateToClient(UpdateMessage update) 
	{
		server.sendMessage(update, mostRecentKey, (long) -1, getAddress());
  }

	public void updateKey(SelectionKey key) 
	{
		mostRecentKey = key;
  }
}
