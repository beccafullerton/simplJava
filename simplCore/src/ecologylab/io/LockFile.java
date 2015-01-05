package ecologylab.io;

import java.io.File;
import java.io.IOException;

import ecologylab.appframework.PropertiesAndDirectories;
import ecologylab.generic.Debug;

/**
 * Simple utility to write and detect lock files.
 * 
 * @author blake
 */
public class LockFile 
extends Debug
{
	String 	programName;
	File	lockFile = null;
	
	/**
	 * Takes a program name to check/generate lockfile state.
	 * 
	 * @param uniqueProgramName The unique name of the program concerned with locking.
	 */
	public LockFile(String uniqueProgramName)
	{
		programName = uniqueProgramName;
	}
	
	/**
	 * Lock the program (create a lockfile). Return a status.
	 * @return whether or not the lockfile could be sucessfully written.
	 */
	public boolean lock()
	{
		if (!isLocked())
		{
			try
			{
				lockFile.createNewFile();
			}
			catch (IOException e)
			{
				System.out.println("LockfileUtility: failed to create lockfile: " + programName);
				return false;
			}
			return true;
		}
		else
		{
			return false;
		}
		
	}
	
	/**
	 * Unlock the program (delete the lockfile). Return a status.
	 * @return whether or not the lockfile could be sucessfully deleted.
	 */
	public boolean unlock()
	{
		if (lockFile == null)
			lockFile = getLockFile();
		
		lockFile.delete();
		//lockFile = null;
		return true;
	}
	
	/**
	 * Determine weather or not the program is locked right now. This is NEVER cached.
	 * 
	 * @return whether or not this program is locked.
	 */
	public boolean isLocked()
	{
		if (lockFile == null)
			lockFile = getLockFile();
		
		return (lockFile != null && lockFile.exists());
	}
	static final int THIRTY_SECONDS	= 1000 * 30;
	/**
	 * Return true if the lock file exists, and is older than 90 seconds ago.
	 * @return
	 */
	public boolean isOld()
	{
		if (!isLocked())
			return false;
		long lastMod	= lockFile.lastModified();
		long now		= System.currentTimeMillis();
		long deltaT		= now - lastMod;
		debug("Checking lock file; its age is " + deltaT);
		return isLocked() && (deltaT >= THIRTY_SECONDS);
	}
	/**
	 * Gets rid of the file handler (for garbage collection)
	 *
	 */
	public void close()
	{
		if (lockFile != null)
			lockFile = null;
	}
	
	/**
	 * Gets a File reference to the actual file lock.
	 * 
	 * @return The file lock.
	 */
	public File getFileLock()
	{
		return lockFile;
	}
	
	private File getLockFile()
	{
		String lockName = "." + programName + ".lock";
		return new File(PropertiesAndDirectories.tempDir(), 
							lockName);
	}
	
}
