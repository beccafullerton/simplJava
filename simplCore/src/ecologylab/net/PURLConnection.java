package ecologylab.net;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;

import ecologylab.collections.CollectionTools;
import ecologylab.generic.Debug;

/**
 * Combines URLConnection with InputStream, providing convenience.
 *
 * @author andruid
 */
public class PURLConnection extends Debug
{
	protected	ParsedURL				purl;
	protected InputStream			inputStream;
	protected HttpURLConnection urlConnection;
	protected String					mimeType;

	/**
	 * If true, a timeout occurred during connect().
	 */
	boolean	timeout	= false;

	boolean	good		= false;

	 /** Fill out the instance of this resulting from a succcessful connect().
	 * @param purl TODO
	 * @param urlConnection
	 * @param inputStream
	 */
	public PURLConnection(ParsedURL purl)
	{
		this.purl						= purl;
	}
	public PURLConnection(ParsedURL purl, HttpURLConnection urlConnection, InputStream inputStream)
	{
		this.purl						= purl;
		this.inputStream		= inputStream;
		this.urlConnection	= urlConnection;
		this.good						= true;
	}

	public void connect(ConnectionHelper connectionHelper, String userAgent,
			int connectionTimeout, int readTimeout)
	{
		// get an InputStream, and set the mimeType, if not bad
		if (purl.isFile())
		{
			File file = purl.file();
			if (file.isDirectory())
				connectionHelper.handleFileDirectory(file);
			else
			{
				String suffix = purl.suffix();
				if (suffix != null)
				{
					if (connectionHelper.parseFilesWithSuffix(suffix))
					{
						try
						{
							fileConnect();
						}
						catch (FileNotFoundException e)
						{
							error("Can't open because FileNotFoundException");
						}
					}
				}
			}
		}
		else
		{
			networkConnectAndCatch(connectionHelper, userAgent, connectionTimeout, readTimeout);
		}
	}
	public void fileConnect() throws FileNotFoundException
	{
		inputStream = new FileInputStream(purl.file());
		good				= true;
	}
	
	public void streamConnect(InputStream inputStream)
	{
		this.inputStream = inputStream;
		good = true;
	}
	
	public void networkConnectAndCatch(ConnectionHelper connectionHelper, String userAgent)
	{
		networkConnectAndCatch(connectionHelper, userAgent, ParsedURL.CONNECT_TIMEOUT, ParsedURL.READ_TIMEOUT);
	}
	/**
	 * @param connectionHelper
	 * @param userAgent
	 * @param connectionTimeout
	 * @param readTimeout
	 */
	public void networkConnectAndCatch(ConnectionHelper connectionHelper, String userAgent,
			int connectionTimeout, int readTimeout)
	{
		try
		{
			networkConnect(connectionHelper, userAgent, connectionTimeout, readTimeout);
		}
		catch (SocketTimeoutException e)
		{
			timeout = true;
			cleanup(e);
		}
		catch (FileNotFoundException e)
		{
			cleanup(e);
		}
		catch (IOException e)
		{
			cleanup(e);
		}
		catch (Exception e) // catch all exceptions, including security
		{
			cleanup(e);
		}
	}
	public void networkConnect(ConnectionHelperJustRemote connectionHelper, String userAgent) 
	throws IOException
	{
		networkConnect(connectionHelper, userAgent, ParsedURL.CONNECT_TIMEOUT, ParsedURL.READ_TIMEOUT);
	}
	/**
	 * @param connectionHelper
	 * @param userAgent
	 * @param connectionTimeout
	 * @param readTimeout
	 * @throws IOException
	 * @throws Exception
	 */
	public void networkConnect(ConnectionHelperJustRemote connectionHelper, String userAgent,
			int connectionTimeout, int readTimeout) 
	throws IOException
	{
		URL url = purl.url();
		urlConnection 							= (HttpURLConnection) url.openConnection();

		// hack so google thinks we're a normal browser
		// (otherwise, it wont serve us)
		// connection.setRequestProperty("user-agent", GOOGLE_BOT_USER_AGENT_0);
		urlConnection.setRequestProperty("user-agent", userAgent);

		// Set the connection and read timeout.
		urlConnection.setConnectTimeout(connectionTimeout);
		urlConnection.setReadTimeout(readTimeout);

		/*
		 * //TODO include more structure instead of this total hack! if
		 * ("nytimes.com".equals(this.domain())) { String auth = new
		 * sun.misc.BASE64Encoder().encode("fred66:fred66".getBytes());
		 * connection.setRequestProperty("Authorization", auth); }
		 */
		urlConnection.getContentLength();
		String mimeType = urlConnection.getContentType();

		// no one uses the encoding header: connection.getContentEncoding();
		String unsupportedCharset = NetTools.isCharsetSupported(mimeType);
		if (unsupportedCharset != null)
		{
			String message = "Cant process charset " + unsupportedCharset + " in "
					+ this;
			connectionHelper.displayStatus(message);
			error(message);
		}
		else
		{
			// notice if url changed between request and retrieved connection
			// if so, this is a server-side redirect
			URL connectionURL = urlConnection.getURL();

			if (!url.equals(connectionURL)) // follow redirects!
			{
				// avoid doubly stuffed urls
				//TODO -- does this test belong here?????
				String connectionFile = connectionURL.getFile();
				String file 					= url.getFile();

				if ((file.indexOf("http://") == -1) && (connectionFile.indexOf("http://") == -1))
//					if ((path.indexOf("http://") != -1) || (connectionPath.indexOf("http://") != -1))
				{
					if (connectionHelper.processRedirect(connectionURL))
						inputStream = urlConnection.getInputStream();
					this.good			= true;
				}
				else
				{
					println("WEIRD: skipping double stuffed url: " + connectionURL);
				}
			}
			else
			{
				// no redirect, eveything is kewl
				inputStream 	= urlConnection.getInputStream();
				this.good			= true;
			}
		}
	}

	private void cleanup(Exception e)
	{
		error("connect() " + e);
		close();
	}


	public void recycle()
	{
		close();
//		purl.recycle();
//		purl							= null;
	}
	public void reconnect()
	{
		if (purl != null && purl.isFile() && inputStream ==null)
		{
			try
			{
				inputStream			= new FileInputStream(purl.file());
			}
			catch (FileNotFoundException e)
			{
				e.printStackTrace();
			}
		}
	}
	/**
	 * Close the InputStream, and disconnect the URLConnection.
	 */
	public void close()
	{
		// parsing done. now free resources asap to avert leaking and memory fragmentation
		// (this is a known problem w java.net.HttpURLConnection)
		InputStream inputStream		= this.inputStream;
		if (inputStream != null)
		{
			NetTools.close(inputStream);
			this.inputStream		= null;
		}
		if (urlConnection != null)
		{
			urlConnection.disconnect();
			this.urlConnection		= null;
		}
		mimeType					= null;
	}

	/**
	 * @return Returns the inputStream.
	 */
	public InputStream inputStream()
	{
		return inputStream;
	}

	/**
	 * @return Returns the urlConnection.
	 */
	public URLConnection urlConnection()
	{
		return urlConnection;
	}

	/**
	 * Find the mime type returned by the web server to the URLConnection, in its header.
	 * Thus, if there is no URLConnection (as for local file system), this always returns null.
	 * 
	 * @return	the mime type or null
	 */
	public String mimeType()
	{
		String result				= this.mimeType;
		if ((result == null) && (urlConnection != null))
		{
			result					= urlConnection.getContentType();
			if (result != null)
			{
				// create the appropriate DocumentType object
				// lookout for mime types with charset appened
				int semicolonIndex	= result.indexOf(';');
				if (semicolonIndex > 0)
					result			= result.substring(0, semicolonIndex);
				this.mimeType		= result;
			}
		}
		return result;
	}
	
	@Override
	public String toString()
	{
		String u = urlConnection != null ? urlConnection.toString() : "null";
		String p = purl != null ? purl.toString() : "null";
		String f = purl != null ? (purl.file() != null ? "path: "+ purl.file().toString() + " exists: " + purl.file().exists(): "null file") : "null purl"; 
		StringBuilder sb = new StringBuilder();
		
		sb.append("urlConnection: ");
		sb.append(u);
		sb.append("\t purl: ");
		sb.append(p);
		sb.append("\t file: ");
		sb.append(f);
		
		return sb.toString();
		
	}
	
	public ParsedURL getPurl()
	{
		return purl;
	}
	
	static final String[]									noAlphaMimeStrings					=
	{ 
		"image/jpeg", "image/bmp", 
	};

	static final HashMap									noAlphaMimeMap						= CollectionTools
																																				.buildHashMapFromStrings(noAlphaMimeStrings);

	public boolean isNoAlpha()
	{
		return mimeType != null && noAlphaMimeMap.containsKey(mimeType);
	}
	
	public boolean getTimeout()
	{
		return timeout;
	}

	public boolean isGood()
	{
		return good;
	}
	
	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}
}
