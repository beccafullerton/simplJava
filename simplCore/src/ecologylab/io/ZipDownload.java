/**
 * Simple class to download and write zip files to disk.
 */
package ecologylab.io;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import ecologylab.appframework.StatusReporter;
import ecologylab.concurrent.Downloadable;
import ecologylab.concurrent.DownloadableLogRecord;
import ecologylab.concurrent.Site;
import ecologylab.generic.Continuation;
import ecologylab.generic.Debug;
import ecologylab.net.NetTools;
import ecologylab.net.ParsedURL;

/**
 * Class implementing DownloadLoadable to allow for the downloading and writing to disk of zip
 * files. Additionally files can be extracted after downloaded.
 * 
 * @author Blake Dworaczyk
 */
public class ZipDownload extends Debug implements Downloadable, Continuation
{

	static DownloadProcessor<ZipDownload>	downloadProcessor;

	ParsedURL															zipSource;

	File																	zipTarget;

	StatusReporter												status;

	boolean																keepStatus					= false;

	boolean																downloadDone				= false;

	boolean																downloadStarted			= false;

	boolean																aborted							= false;

	boolean																extractWhenComplete	= false;

	int																		fileSize						= -1;

	InputStream														inputStream					= null;

	private static final int							BUFFER_SIZE					= 8192;

	public ZipDownload(ParsedURL zipSource, File zipTarget, StatusReporter status)
	{
		super();

		this.zipSource = zipSource;
		this.zipTarget = zipTarget;
		this.status = status;

		if (status != null)
			keepStatus = true;
	}

	public ZipDownload(ParsedURL zipSource, File zipTarget)
	{
		this(zipSource, zipTarget, null);
	}

	/**
	 * Initiate the download and writing of the zip file. This is called by outsiders.
	 * 
	 * @throws IOException
	 */
	public void downloadAndWrite(boolean extractWhenComplete) throws IOException
	{
		this.extractWhenComplete = extractWhenComplete;
		debug("downloadAndWrite() calling downloadMonitor");
		if (downloadProcessor == null)
			throw new RuntimeException("Can't download cause downloadProcessor = null.");

		downloadProcessor.download(this, this);
	}

	public static void stopDownloadProcessor()
	{
		if (downloadProcessor != null)
			downloadProcessor.stop();
	}

	public void downloadAndWrite() throws IOException
	{
		downloadAndWrite(false);
	}

	/**
	 * ONLY called by <code>DownloadProcessor</code>s to actually download the zip file! Not called by
	 * outsiders!
	 * 
	 * @throws IOException
	 */
	@Override
	public void performDownload() throws IOException
	{
//		debug("performDOwnload() top");
		if (downloadStarted)
			return;

		downloadStarted = true;

		// this gets the stream and sets the member field 'fileSize'
		// inputStream = getInputStream(zipSource);
		inputStream = zipSource.url().openStream();

		debug("performDownload() got InputStream");

		// actually read and write the zip
		// if zipTarget already exists, delete it
		if (zipTarget.exists())
		{
			boolean deleted = zipTarget.delete();
			debug("ZipTarget exists, so deleting = " + deleted);
		}

		OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(zipTarget));
		debug("performDownload() got outputStream from " + zipTarget);

		byte zipBytes[] = new byte[BUFFER_SIZE];

		// Read data from the source file and write it out to the zip file
		int count = 0;
		int lastTenth = 1;
		int incrementSize = fileSize / 10;
		// int i=0;
		while ((count = inputStream.read(zipBytes, 0, BUFFER_SIZE)) != -1)
		{
			if (status != null)
			{
				// Our status will be in 10% increments
				if (count >= incrementSize * (lastTenth))
					status.display("Downloading zip file " + zipTarget.getName(), 1, count / 10,
							incrementSize);

				// can't just increment because we maybe skip/hit 1/10ths due to
				// faster/slower transfer rates, traffic, etc.
				lastTenth = (int) Math.floor(((double) count / fileSize) * 10);
			}
			outputStream.write(zipBytes, 0, count);
		}

		outputStream.close();
		inputStream.close();

		if (extractWhenComplete)
			extractZipFile(zipTarget);

		synchronized (this)
		{
			downloadDone = true;
		}
	}

	public BufferedInputStream getInputStream(File zipTarget) throws Exception
	{
		URLConnection urlConnection;

		if (zipTarget.isDirectory())
		{

			throw new Exception("ZipDownload: write exception! Can't write to directory!");
		}

		if (zipSource.isFile())
		{
			inputStream = new FileInputStream(zipSource.file());

		}
		// In case the purl was constructed with a local file used as a URL
		// TODO find the source of these bad constructions! (only applicabale when cF is launched as an
		// application)
		else if (zipSource.toString().startsWith("file://"))
		{
			try
			{
				File fileInput = new File(zipSource.toString().substring(7, zipSource.toString().length()));
				inputStream = new FileInputStream(fileInput);
			}
			catch (Exception e)
			{
				System.out.println("ZipDownload: error reading local file");
				e.printStackTrace();
				throw new Exception("ZipDownload: IOException");
			}
		}
		else
		{
			urlConnection = zipSource.url().openConnection();
			if ((urlConnection == null) || aborted)
			{
				System.err.println("Cant open URLConnection for " + aborted + " " + zipSource);
				throw new Exception("ZipDownload: IOException");
			}
			try
			{
				urlConnection.connect();
				fileSize = urlConnection.getContentLength();
				inputStream = urlConnection.getInputStream();
			}
			catch (FileNotFoundException e)
			{
				System.err.println("FileNotFound!");
				throw new Exception("ZipDownload: IOException");
			}
		}
		if ((inputStream == null) || aborted)
		{
			System.err.println("Cant open InputStream for " + aborted + " " + zipSource);
			throw new Exception("ZipDownload: IOException");
		}

		return new BufferedInputStream(inputStream);
	}

	public boolean isDownloadDone()
	{
		return downloadDone;
	}

	@Override
	public void handleIoError(Throwable e)
	{
		aborted = true;
		NetTools.close(inputStream);
	}

	@Override
	public String toString()
	{
		return "ZipDownload() " + zipSource + " -> " + zipTarget;
	}

	@Override
	public void callback(Object o)
	{
		System.out.println("ZipDownload delivered: " + (o));
	}

	/**
	 * Convenience function to allow downloading and uncompressing of a zip file from a source to a
	 * target location with minimal effort.
	 * 
	 * @param sourceZip
	 *          The location of the zip file to download and uncompress.
	 * @param targetDir
	 *          The location where the zip file should be uncompressed. This directory structure will
	 *          be created if it doesn't exist.
	 * @param status
	 *          The StatusReporter object that provides a source of state change visiblity; can be
	 *          null.
	 * @param unCompress
	 *          true if the file is a zip that needs to be uncompressed after download.
	 * @return TODO
	 */
	public static ZipDownload downloadAndPerhapsUncompress(ParsedURL sourceZip, File targetDir,
			StatusReporter status, boolean unCompress)
	{
		// Create the target parent directory.
		if (!targetDir.exists())
			targetDir.mkdirs();

		println("downloading from zip URL: " + sourceZip + "\n\t to " + targetDir);
		try
		{
			// FIXME make this use a (fixed?!) version of ParsedURL.isFile() ...
			// if (sourceZip.toString().startsWith("file://"))
			if (sourceZip.isFile())
			{
				// copy zip file from assets source to cache
				File sourceZipFile = sourceZip.file();
				String fileName = sourceZipFile.getName();
				File destFile = Files.newFile(targetDir, fileName);
				File destFileDir = Files.newFile(targetDir,
						destFile.toString().substring(0, destFile.toString().length() - 4));

				println("Checking if dir exists: " + destFileDir.toString());

				// Delete the previous directory if it exists.
				if (destFileDir.exists())
					Files.deleteDirectory(destFileDir);

				StreamUtils.copyFile(sourceZipFile, destFile);
				extractZipFile(sourceZipFile, targetDir);

				return null;
			}
			else
			{
				String fileName = sourceZip.getName();
				ZipDownload zipDownload = new ZipDownload(sourceZip, Files.newFile(targetDir, fileName),
						status);
				zipDownload.downloadAndWrite(unCompress);
				return zipDownload;
			}
		}
		catch (IOException e)
		{
			System.err.println("Error, zip file not found on the server!");
			//	hiding stack trace -- it's annoying.
			//	e.printStackTrace();
			return null;
		}
	}

	/**
	 * Extracts a zip file into the directory where it resides
	 * 
	 * @param zipSourcePath
	 *          The path to the source zip file to extract.
	 */
	public static void extractZipFile(String zipSourcePath) throws IOException
	{
		extractZipFile(new File(zipSourcePath));
	}

	public static void extractZipFile(File zipSource) throws IOException
	{
		extractZipFile(zipSource, zipSource.getParentFile());
	}

	/**
	 * Extracts a zip file into the directory where it resides
	 * 
	 * @param zipSourcePath
	 *          The source zip file to extract
	 */
	public static void extractZipFile(String zipSourcePath, File unzipPath) throws IOException
	{
		extractZipFile(new File(zipSourcePath), unzipPath);
	}

	/**
	 * Extracts a zip file into the directory where it resides
	 * 
	 * @param zipSource
	 *          The source zip file to extract
	 */
	public static void extractZipFile(File zipSource, File unzipPath) throws IOException
	{
		ZipFile zipFile = new ZipFile(zipSource);
		System.out.println("Extracting zip file: " + zipSource);
		Enumeration entries = zipFile.entries();
		while (entries.hasMoreElements())
		{
			ZipEntry entry = (ZipEntry) entries.nextElement();
			String entryName = entry.getName();
			if (entry.isDirectory())
			{
				File entryDir = new File(unzipPath, entryName);
				if (!entryDir.exists())
					entryDir.mkdirs();

				continue;
			}
			// else (if !entry.ex)
			File outFile = new File(unzipPath, entryName);
			String dirPath = outFile.getParent();
			File dir = new File(dirPath);
			if (!dir.exists())
				dir.mkdirs();
			StreamUtils.copyInputStream(zipFile.getInputStream(entry), new BufferedOutputStream(
					new FileOutputStream(outFile)));
		}

		zipFile.close();

		System.out.println("Finished extracting Zip file to " + unzipPath);
	}

	/**
	 * Call to notify the object that its download is completed;
	 * 
	 */
	public synchronized void downloadDone()
	{
		notifyAll();
	}

	public void waitForDownload()
	{
		synchronized (this)
		{
			if (!downloadDone && !aborted)
			{
				try
				{
					wait();
				}
				catch (InterruptedException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	public static void setDownloadProcessor(DownloadProcessor downloadProcessor)
	{
		ZipDownload.downloadProcessor = downloadProcessor;
	}

	@Override
	public boolean isRecycled()
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Site getSite()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * 
	 * @return What to tell the user about what is being downloaded.
	 */
	@Override
	public String message()
	{
		return "zip archive " + zipSource.toString();
	}

	@Override
	public ParsedURL location()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void recycle()
	{
		if (inputStream != null)
			try
			{
				inputStream.close();
			}
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}

	/**
	 * Default empty implementation; will be ignored for this type.
	 */
	@Override
	public boolean isImage()
	{
		return false;
	}

	@Override
	public Site getDownloadSite()
	{
		return null;
	}

	@Override
	public ParsedURL getDownloadLocation()
	{
		return location();
	}

  @Override
  public boolean isCached()
  {
    // TODO Auto-generated method stub
    return false;
  }

	@Override
	public DownloadableLogRecord getLogRecord()
	{
		// TODO Auto-generated method stub
		return null;
	}
  
}
