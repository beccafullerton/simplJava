package ecologylab.io;

import java.io.IOException;

import ecologylab.concurrent.Downloadable;
import ecologylab.generic.Continuation;


/**
 * Interface to a module that performs downloads, perhaps concurrently.
 * A wrapper for DownloadMonitor, for example.
 *
 * @author andruid
 */
public interface DownloadProcessor<T extends Downloadable>
{
	public void stop();
	
/**
 * Download the Downloadable, perhaps concurrently.
 * If concurrently, call the Continuation.callback(T) method when done.
 * 
 * @param thatDownloadable
 * @param dispatchTarget
 * @throws IOException 
 */
	public void download(T thatDownloadable, Continuation<T> continuation);
	
	public void requestStop();
}
