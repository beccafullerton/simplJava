package ecologylab.oodss.distributed.server;

import ecologylab.generic.StartAndStoppable;
import ecologylab.oodss.distributed.server.clientsessionmanager.WebSocketClientSessionManager;

public interface WebSocketServerProcessor extends StartAndStoppable
{
	/**
	 * Performs any internal actions that should be taken whenever a client is disconnected.
	 * 
	 * @param sessionId
	 *          the identifier for the client's connection (key.attachment(), normally)
	 * @param forcePermanent
	 *          TODO
	 * @return true if the client is permanently disconnecting
	 */
	public boolean invalidate(String sessionId, boolean forcePermanent);

	/**
	 * Attempts to switch the ContextManager for a SocketChannel. oldId indicates the session id that
	 * was used for the connection previously (in order to find the correct ContextManager) and
	 * newContextManager is the recently-created (and now, no longer necessary) ContextManager for the
	 * connection.
	 * 
	 * @param oldId
	 * @param newContextManager
	 * @return true if the restore was successful, false if it was not.
	 */
	public boolean restoreContextManagerFromSessionId(String oldId,
			WebSocketClientSessionManager newContextManager);
}
