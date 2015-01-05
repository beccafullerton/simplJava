package ecologylab.net;

import java.io.File;
import java.io.IOException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.StringTokenizer;

import ecologylab.collections.CollectionTools;
import ecologylab.generic.Debug;
import ecologylab.generic.IntSlot;
import ecologylab.generic.StringTools;
import ecologylab.io.Files;
import ecologylab.platformspecifics.FundamentalPlatformSpecifics;
import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.SimplTypesScope;
import ecologylab.serialization.formatenums.Format;

/**
 * Extends the URL with many features for the convenience and power of network programmers. New
 * class for manipulating and displaying URLs.
 * 
 * Uses lazy evaluation to minimize storage allocation.
 * 
 * @author andruid
 * @author eunyee
 * @author madhur
 */
public class ParsedURL extends Debug implements MimeType
{
	private static final String	NOT_IN_THE_FORMAT_OF_A_WEB_ADDRESS	= " is not in the format of a web address";

	private static final String	DEFAULT_USER_AGENT									= "Mozilla/5.0 (Windows; U; Windows NT 6.0; en-US; rv:1.9.1.2) Gecko/20090729 Firefox/3.5.2 (.NET CLR 3.5.30729)";

	/**
	 * this is the no hash url, that is, the one with # and anything after it stripped out.
	 */
	protected URL								url																	= null;

	/**
	 * If this is built from an entity of the local file system, store a reference to the object for
	 * that here.
	 */
	File												file;

	/**
	 * Directory that the document referred to by the URL resides in.
	 */
	protected URL								directory														= null;

	private ParsedURL						directoryPURL;

	/**
	 * String representation of the URL.
	 */
	protected String						string															= null;

	/**
	 * Shorter version of the string, for printing in tight spaces.
	 */
	String											shortString;

	/* lower case of the url string */
	protected String						lc																	= null;

	/* suffix string of the url */
	protected String						suffix															= null;

	/* domain value string of the ulr */
	protected String						domain															= null;
	
	protected boolean						includePrefix												= true;
	
	private String              fragment;
	
	public static CookieManager cookieManager = new CookieManager();
	
	static
	{
		//cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
		CookieHandler.setDefault(cookieManager);
	}
	
	public ParsedURL(URL url)
	{
		String hash = url.getRef();
		
		if ("file".equals(url.getProtocol()))
		{
			String urlString = url.toString();
			if (urlString.startsWith("file://")) // this should be the case...
			{
				this.file = new File(urlString.substring(7));
			}
			else // if not, try our hardest to make a good file
			{
				this.file = new File(url.getHost()+url.getPath());
			}
			this.url 	= url;
		}
		else
		{
		  this.url = url;
		  this.fragment = hash;
		}
	}

	/**
	 * 
	 * @return true if this refers to a file, and that file exists.  Also true if this does not refer to a file.
	 */
	public boolean isNotFileOrExists()
	{
		return (file == null) || file.exists();
	}

	/**
	 * Create a ParsedURL from a file. If the file is a directory, append "/" to the path, so that
	 * relative URLs will be formed properly later.
	 * 
	 * @param file
	 */
	public ParsedURL(File file)
	{
		try
		{
			String urlString = "file://" + file.getAbsolutePath();
			urlString = urlString.replace('\\', '/');
			if (file.isDirectory())
				urlString += "/";
			this.url = new URL(urlString);
		}
		catch (MalformedURLException e)
		{
			e.printStackTrace();
		}
		this.file = file;
	}

	/*
	 * Constructor with a url string parameter. get absolute URL with getAbsolute() method.
	 */
	/*
	 * public ParsedURL(String urlString) { // The second parameter of getAbolute method is error
	 * description. this.url = getAbsolute(urlString, "").url(); }
	 */
	// /////////////////////////////////////////////////////////////////////
	/**
	 * Create a PURL from an absolute address. (Do it the quick and dirty way, providing less error
	 * handling.) NB: Only call this method if you are *sure* a MalformedURlException would never be
	 * produced.
	 * 
	 */
	public static ParsedURL getAbsolute(String webAddr)
	{
		return getAbsolute(webAddr, "getAbsolute(String) ");
	}
	
	public static ParsedURL get(URI uri)
	{
		return getAbsolute(uri.toString());
	}
	
	/**
	 * Create a PURL from an absolute address.
	 * 
	 * @param webAddr
	 *          url string
	 * @param errorDescriptor
	 *          which will be printed out in the trace file if there is something happen converting
	 *          from the url string to URL.
	 * @return ParsedURL from url string parameter named webAddr, or null if the param is malformed.
	 */
	public static ParsedURL getAbsolute(String webAddr, String errorDescriptor)
	{
		if (webAddr == null || webAddr.length() <= 7)
		{
			println("ERROR: ParsedURL.getAbsolute() webAddr is null or too short: [" + webAddr + "]");
			//Thread.dumpStack(); //We don't really need such a hostile message.
		}
		else
		{
			try
			{
				URL url = new URL(webAddr);
				if (isUndetectedMalformedURL(url))
					return null;
				return new ParsedURL(url);
			}
			catch (MalformedURLException e)
			{
				if (!"".equals(errorDescriptor))
					errorDescriptor = "\n" + errorDescriptor;
				Debug.error(webAddr, NOT_IN_THE_FORMAT_OF_A_WEB_ADDRESS + "." + errorDescriptor);
			}
		}
		return null;
	}

	/**
	 * Determines a URL is malformed since Java fails to detect this.
	 * 
	 * @param url
	 * @return
	 */
	private static boolean isUndetectedMalformedURL(URL url)
	{
		// originally checked against "file:", but on OS X, we just get "file"; this is probably true
		// everywhere else too, but I will leave "file:" for the time being. -Zach
		boolean isFileProtocol = "file".equals(url.getProtocol()) || "file:".equals(url.getProtocol());
		String host = url.getHost().trim();

		return ((!isFileProtocol && ("".equals(host) || "/".equals(host)))
		        || (isFileProtocol && ("".equals(url.getPath().trim())
		                               || "localhost".equalsIgnoreCase(host))));
	}

	/**
	 * Form a ParsedURL, based on a relative path, using this as the base.
	 * 
	 * @param relativeURLPath
	 *          Path relative to this.
	 * @param errorDescriptor
	 * 
	 * @return New ParsedURL based on this and the relative path.
	 */
	public final ParsedURL getRelative(String relativeURLPath, String errorDescriptor)
	{
		if (isFile())
		{
			File newFile = Files.newFile(file, relativeURLPath);
			// remove ..'s from path
			if (newFile.getAbsolutePath().contains(".."))
			{
				try
				{
					File canonicalFile = newFile.getCanonicalFile();
					return new ParsedURL(canonicalFile);
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
			return new ParsedURL(newFile);
		}
		else
			return getRelative(url, relativeURLPath, errorDescriptor);
	}

	/**
	 * Form a ParsedURL, based on a relative path, using this as the base.
	 * 
	 * @param relativeURLPath
	 *          Path relative to this.
	 * 
	 * @return New ParsedURL based on this and the relative path.
	 */
	public final ParsedURL getRelative(String relativeURLPath)
	{
		return getRelative(relativeURLPath, "");
	}

	/**
	 * Form a new ParsedURL, relative from a supplied base URL. Checks to see if the relativePath
	 * starts w a protocol spec. If so, calls getAbsolute(). Otherwise, forms a relative URL using the
	 * URL base.
	 * 
	 * @param relativeURLPath
	 * @param errorDescriptor
	 * @return New ParsedURL
	 */
	public static ParsedURL getRelative(URL base, String relativeURLPath, String errorDescriptor)
	{
		if (relativeURLPath == null)
			return null;

		ParsedURL result = null;
		if (!relativeURLPath.startsWith("http://") && !relativeURLPath.startsWith("ftp://"))
		{
			try
			{
				URL resultURL = new URL(base, relativeURLPath);
				result = new ParsedURL(resultURL);
			}
			catch (MalformedURLException e)
			{
				if (!"".equals(errorDescriptor))
					errorDescriptor = "\n" + errorDescriptor;
				Debug.error(relativeURLPath, NOT_IN_THE_FORMAT_OF_A_WEB_ADDRESS + "[" + base + "]."
						+ errorDescriptor);
			}
		}
		else
		{
			return getAbsolute(relativeURLPath, errorDescriptor);
		}

		return result;
	}

	/**
	 * Use this as the source of stuff to translate from XML
	 * 
	 * @param translationScope
	 *          Translations that specify package + class names for translating.
	 * @return ElementState object derived from XML at the InputStream of this.
	 * @throws SIMPLTranslationException
	 */
	public Object translateFromXML(SimplTypesScope translationScope)
			throws SIMPLTranslationException
	{
		return translationScope.deserialize(this, Format.XML);
	}

	public static URL getURL(URL base, String path, String error)
	{
		// ??? might want to allow this default behaviour ???
		if (path == null)
			return null;
		try
		{
			// System.err.println("\nGENERIC - base, path, error = \n" + base + "\n" + path);
			URL newURL = new URL(base, path);
			// System.err.println("\nNEW URL = " + newURL);
			return newURL;
		}
		catch (MalformedURLException e)
		{
			if (error != null)
				throw new Error(e + "\n" + error + " " + base + " -> " + path);
			return null;
		}
	}

	/**
	 * Uses lazy evaluation to minimize storage allocation.
	 * 
	 * @return The URL as a String.
	 */
	@Override
	public String toString()
	{
		String result = string;
		if (result == null)
		{
			if (isFile() && includePrefix)
				result = "file://" + file.toString().replace('\\', '/');
			else if (isFile() && !includePrefix)
				result = file.toString().replace('\\', '/');
			else if (url == null)
				result = "weirdly null";
			else
				result = StringTools.pageString(url);
			string = result;
		}
		return result;
	}

	/**
	 * Uses lazy evaluation to minimize storage allocation.
	 * 
	 * @return Lower case rendition of the URL String.
	 */
	public String lc()
	{
		String result = lc;
		if (result == null)
		{
			result = toString().toLowerCase();
			lc = result;
		}
		return result;
	}

	/**
	 * Uses lazy evaluation to minimize storage allocation.
	 * 
	 * @return The suffix of the filename, in lower case.
	 */
	public String suffix()
	{
		String result = suffix;
		if (result == null)
		{
			String path = url.getPath();
			if (path != null)
			{
				result = suffix(path.toLowerCase());
			}
			// TODO make sure that there isnt code somewhere testing suffix for null!
			if (result == null)
				result = "";
			suffix = result;
		}
		return result;
	}

	/**
	 * Form a ParsedURL based on this, if this is a directory. Otherwise, form the ParsedURL from the
	 * parent of this. Process files carefully to propagate their file-ness.
	 * 
	 * @return
	 */
	public ParsedURL directoryPURL()
	{
		ParsedURL result = directoryPURL;
		if (result == null)
		{
			if (isFile())
			{
				if (file.isDirectory())
					result = this;
				else
				{
					File parent = file.getParentFile();
					result = new ParsedURL(parent);
				}
			}
			else
			{
				result = new ParsedURL(directory());
			}
			this.directoryPURL = result;
		}
		return result;
	}

	/**
	 * Get the URL for the directory associated with this. Requires looking for slash at the end,
	 * looking for a suffix or arguments. As a result, we sometimes add a slash at the end, sometimes
	 * peel off the filename. Result is cached a la lazy evaluation.
	 * 
	 * @return Directory URL
	 */
	public URL directory()
	{
		URL result = this.directory;
		if (result == null)
		{
			if (StringTools.endsWithSlash(toString()))
				result = this.url;
			if (result == null)
			{
				String suffix = suffix();
				try
				{
					String path = url.getPath();
					String args = url.getQuery();
					String protocol = url.getProtocol();
					String host = url.getHost();
					int port = url.getPort();
					if (suffix.length() == 0)
					{ // this is a directory that is unterminated by slash; we need to fix that

						if (path.length() == 0)
							result = new URL(protocol, host, port, "/");
						else
						{
							if ((args == null) || (args.length() == 0))
								result = new URL(protocol, host, port, path + '/');
							else
							// this is a tricky executable with no suffix
							{
								// result = null;
								// drop down into the next block, and peel off that suffix-less executable name
							}
						}
					}
					// else
					if (result == null)
					{ // you have a suffix, so we need to trim off the filename
						int lastSlashIndex = path.lastIndexOf('/');
						if (lastSlashIndex == -1)
							// suffix, but not within any subdirectory
							result = new URL(protocol, host, port, "/");
						else
						{
							String pathThroughLastSlash = path.substring(0, lastSlashIndex + 1);
							result = new URL(protocol, host, port, pathThroughLastSlash);
						}
					}
				}
				catch (MalformedURLException e)
				{
					debug("Unexpected ERROR forming directory.");
					e.printStackTrace();
				}
			}
			this.directory = result;
		}
		return result;
	}

	/**
	 * Uses lazy evaluation to minimize storage allocation.
	 * 
	 * @return The domain of the URL.
	 */
	public String domain()
	{
		String result = domain;
		if (result == null && (url != null))
		{
			result = StringTools.domain(url);
			domain = result;
		}
		return result;
	}

	public boolean isNull()
	{
		return url == null && file == null;
	}

	/**
	 * @return The suffix of the filename, in whatever case is found in the input string.
	 */
	public static String suffix(String lc)
	{
		int afterDot = lc.lastIndexOf('.') + 1;
		int lastSlash = lc.lastIndexOf('/');
		String result = ((afterDot == 0) || (afterDot < lastSlash)) ? "" : lc.substring(afterDot);
		return result;
	}

	public String filename()
	{
		String lowerCase = noAnchorNoQueryPageString();
		int lastDot = lowerCase.lastIndexOf('.');
		int lastSlash = lowerCase.lastIndexOf('/');
		String result = (lastDot == 0 || (lastDot < lastSlash)) ? "" : lowerCase.substring(lastSlash,
				lastDot);
		return result;
	}

	/**
	 * Uses lazy evaluation to minimize storage allocation.
	 * 
	 * @return the URL.
	 */
	public final URL url()
	{
		return url;
	}

	public final URL hashUrl()
	{
    return url();
	}

	/*
	 * return noAnchor no query page string
	 */
	public String noAnchorNoQueryPageString()
	{
		return StringTools.noAnchorNoQueryPageString(url);
	}

	/*
	 * return no anchor no page string.
	 */
	public String noAnchorPageString()
	{
		return StringTools.noAnchorPageString(url);
	}

	/**
	 * @return true if the suffix of this is equal to that of the argument.
	 */
	public final boolean hasSuffix(String s)
	{
		return lc().endsWith(s);
		// return suffix().equals(s);
	}

	final static String										unsupportedMimeStrings[]		=
																																		{ "ai", "bmp", "eps", "ps",
			"psd", "svg", "tif", "vrml", "doc", "xls", "pps", "ppt", "adp", "rtf", "vbs", "vsd", "wht",
			"aif", "aiff", "aifc", "au", "mp3", "wav", "ra", "ram", "wm", "wma", "wmf", "wmp", "wms",
			"wmv", "wmx", "wmz", "avi", "mov", "mpa", "mpeg", "mpg", "ppj", "swf", "spl", "qdb", "cab",
			"chm", "gzip", "hqx", "jar", "lzh", "tar", "zip", "wpd", "xsl", };

	final static HashMap									unsupportedMimes						= CollectionTools
																																				.buildHashMapFromStrings(unsupportedMimeStrings);

	static final String[]									unsupportedProtocolStrings	=
																																		{ "mailto", "vbscript", "news",
			"rtsp", "https",																							};

	static final HashMap									unsupportedProtocols				= CollectionTools
																																				.buildHashMapFromStrings(unsupportedProtocolStrings);

	static final String[]									supportedProtocolStrings		=
																																		{ "http", "ftp", "file", };

	static final HashMap									supportedProtocols					= CollectionTools
																																				.buildHashMapFromStrings(supportedProtocolStrings);

	static final String[]									imgSuffixStrings;

	static final String[] SOME_IMG_SUFFIXES = { "jpg", "jpeg", "pjpg", "pjpeg", "gif", "png", };
	/*
	 * { "jpg", "jpeg", "pjpg", "pjpeg", "gif", "png", };
	 */
	static final HashMap									imgSuffixMap;																																													// case

	static final String[]									jpegMimeStrings							=
																																		{ "jpg", "JPG", "jpeg", "JPEG",
			"pjpg", "pjpeg",																							};

	static final String[]									gifMimeStrings							=
																																		{ "gif", "GIF", };

	static final String[]									pngMimeStrings							=
																																		{ "png", "PNG", };

	static final HashMap									jpegSuffixMap								= CollectionTools
																																				.buildHashMapFromStrings(jpegMimeStrings);

	static final String[]									htmlSuffixStrings							=
																																		{ "html", "htm", "stm", "php",
			"jhtml", "jsp", "asp", "txt", "shtml", "pl", "plx", "exe"		};

	static final String[]									noAlphaSuffixStrings					=
	{ 
		"bmp", "BMP", "wbmp", "WBMP", 
		"jpg", "JPG", "jpeg", "JPEG",
		"pjpg", "PJPG", "pjpeg", "PJPEG",
	};

	static final HashMap									noAlphaSuffixMap						= CollectionTools
																																				.buildHashMapFromStrings(noAlphaSuffixStrings);

	static final HashMap									htmlSuffixMap								= CollectionTools
																																				.buildHashMapFromStrings(htmlSuffixStrings);

	static final String[]									pdfMimeStrings							=
																																		{ "pdf" };

	static final HashMap									pdfSuffixMap								= CollectionTools
																																				.buildHashMapFromStrings(pdfMimeStrings);

	static final String[]									rssMimeStrings							=
																																		{ "rss", "xml" };

	static final HashMap									rssSuffixMap								= CollectionTools
																																				.buildHashMapFromStrings(rssMimeStrings);

	static final HashMap<String, IntSlot>	suffixesToMap								= new HashMap<String, IntSlot>();
	static
	{
		String[] platformSpecificImgFormats = null;
		try
		{
			platformSpecificImgFormats = FundamentalPlatformSpecifics.get().getReaderFormatNames(); 
		} catch (Throwable e)
		{
		}
		imgSuffixStrings	= (platformSpecificImgFormats == null) ? SOME_IMG_SUFFIXES : platformSpecificImgFormats;
		imgSuffixMap		= CollectionTools.buildHashMapFromLCStrings(imgSuffixStrings);

		for (int i = 0; i < pdfMimeStrings.length; i++)
			CollectionTools.stringIntMapEntry(suffixesToMap, pdfMimeStrings[i], PDF);
		for (int i = 0; i < htmlSuffixStrings.length; i++)
			CollectionTools.stringIntMapEntry(suffixesToMap, htmlSuffixStrings[i], HTML);
		for (int i = 0; i < rssMimeStrings.length; i++)
			CollectionTools.stringIntMapEntry(suffixesToMap, rssMimeStrings[i], RSS);
		for (int i = 0; i < jpegMimeStrings.length; i++)
			CollectionTools.stringIntMapEntry(suffixesToMap, jpegMimeStrings[i], JPG);
		for (int i = 0; i < gifMimeStrings.length; i++)
			CollectionTools.stringIntMapEntry(suffixesToMap, gifMimeStrings[i], GIF);
		for (int i = 0; i < pngMimeStrings.length; i++)
			CollectionTools.stringIntMapEntry(suffixesToMap, pngMimeStrings[i], PNG);
	}

	/**
	 * Called while processing (parsing) HTML. Used to create new <code>ParsedURL</code>s from
	 * urlStrings in response to such as the <code>a</code> element's <code>href</code> attribute, the
	 * <code>img</code> element's <code>src</code> attribute, etc.
	 * <p>
	 * Does processing of some fancy stuff, like, in the case of <code>javascript:</code> URLs, it
	 * mines them for embedded absolute URLs, if possible, and uses only those embedded URLs.
	 * 
	 * @param addressString
	 *          This may be specify a relative or absolute url.
	 * 
	 * @return The resulting ParsedURL. It may be null. It will never have protocol
	 *         <code>javascript:</code>.
	 */
	public ParsedURL createFromHTML(String addressString)
	{
		return createFromHTML(addressString, false);
	}

	/**
	 * Called while processing (parsing) HTML. Used to create new <code>ParsedURL</code>s from
	 * urlStrings in response to such as the <code>a</code> element's <code>href</code> attribute, the
	 * <code>img</code> element's <code>src</code> attribute, etc.
	 * <p>
	 * Does processing of some fancy stuff, like, in the case of <code>javascript:</code> URLs, it
	 * mines them for embedded absolute URLs, if possible, and uses only those embedded URLs.
	 * 
	 * @param addressString
	 *          This may be specify a relative or absolute url.
	 * 
	 * @param fromSearchPage
	 *          If false, then add <code>/</code> to the end of the URL if it seems to be a directory.
	 * 
	 * @return The resulting ParsedURL. It may be null. It will never have protocol
	 *         <code>javascript:</code>.
	 */
	public ParsedURL createFromHTML(String addressString, boolean fromSearchPage)
	{
		return createFromHTML(this, addressString, fromSearchPage);
	}

	protected static ParsedURL get(URL url, String addressString)
	{
		try
		{
			return new ParsedURL(new URL(url, addressString));
		}
		catch (MalformedURLException e)
		{
			println("ParsedURL.get() cant from url from: " +
			/* url +"\n\taddressString = "+ */addressString);
			// e.printStackTrace();
		}
		return null;
	}

	/**
	 * Called while processing (parsing) HTML. Used to create new <code>ParsedURL</code>s from
	 * urlStrings in response to such as the <code>a</code> element's <code>href</code> attribute, the
	 * <code>img</code> element's <code>src</code> attribute, etc.
	 * <p>
	 * Does processing of some fancy stuff, like, in the case of <code>javascript:</code> URLs, it
	 * mines them for embedded absolute URLs, if possible, and uses only those embedded URLs.
	 * 
	 * @param addressString
	 *          This may be specify a relative or absolute url.
	 * 
	 * @param fromSearchPage
	 *          If false, then add <code>/</code> to the end of the URL if it seems to be a directory.
	 * 
	 * @return The resulting ParsedURL. It may be null. It will never have protocol
	 *         <code>javascript:</code>.
	 */
	public static ParsedURL createFromHTML(ParsedURL contextPURL, String addressString,
			boolean fromSearchPage)
	{
		if ((addressString == null) || (addressString.length() == 0))
			return null;
		if (addressString.startsWith("#") || addressString.startsWith("mailto"))
		{
			// return get(contextPURL.url(), addressString);
			return null;
		}

		String lc = addressString.toLowerCase();
		boolean javascript = lc.startsWith("javascript:");

		// mine urls from javascript quoted strings
		if (javascript)
		{
			// !!! Could do an even better job here of mining quoted
			// !!! javascript strings.
			// println("Container.newURL("+s);
			int http = lc.lastIndexOf("http://");
			// TODO learn to mine PDFs as well as html!!
			int html = lc.lastIndexOf(".html");
			int pdf = lc.lastIndexOf(".pdf");
			// println("Container.newURL() checking javascript url:="+s+
			// " http="+http+" html="+html);
			if (http > -1)
			{ // seek absolute web addrs
				if ((html > -1) && (http < html))
				{
					int end = html + 5;
					addressString = addressString.substring(http, end);
					// println("Container.newURL fixed javascript:= " + s);
					lc = lc.substring(http, end);
					javascript = false;
				}
				else if ((pdf > -1) && (http < pdf))
				{
					int end = pdf + 4;
					addressString = addressString.substring(http, end);
					// println("Container.newURL fixed javascript:= " + s);
					lc = lc.substring(http, end);
					javascript = false;
				}
			}
			else
			{
				// seek relative addresses

				// need to find the bounds of a quoted string, if there is one
			}
			// !!! What we should really do here is find quoted strings
			// (usually with single quote, but perhaps double as well)
			// (use regular expressions?? - are they fast enough?)
			// and look at each one to see if either protocol is supported
			// or suffix is htmlMime or imgMime.
		}
		if (javascript)
			return null;

		char argDelim = '?';
		// url string always keep hash string.
		String hashString = StringTools.EMPTY_STRING;
		if (fromSearchPage)
		{
			// handle embedded http://
			int lastHttp = addressString.lastIndexOf("http://");
			// usually ? but could be &
			if (lastHttp > 0)
			{
				// this is search engine crap
				addressString = addressString.substring(lastHttp);
				// debugA("now addressString="+addressString);
				// handle any embedded args (for google mess)
				argDelim = '&';
			}
		}
		else
		{
			// TODO do we really need to do any of this???????????????????????
			// 1) peel off hash
			int hashPos = addressString.indexOf('#');
			// String hashString= StringTools.EMPTY_STRING;

			if (hashPos > -1)
			{
				hashString = addressString.substring(hashPos);
				addressString = addressString.substring(0, hashPos);
			}
			// 2) peel off args
			int argPos = addressString.indexOf(argDelim);
			String argString = StringTools.EMPTY_STRING;
			if (argPos > -1)
			{
				argString = addressString.substring(argPos);
				addressString = addressString.substring(0, argPos);
			}
			// This seems uneccessary, crawling any wikimedia based site will break by adding an extra
			// slash.
			// else
			// {
			// // 3) if what's left is a directory (w/o a mime type),add slash
			// int endingSlash = addressString.lastIndexOf('/');
			// int lastChar = addressString.length() - 1;
			// if (endingSlash == -1)
			// endingSlash++;
			// if ((lastChar > 0) &&
			// (lastChar != endingSlash) &&
			// (addressString.substring(endingSlash).indexOf('.') == -1))
			// addressString += '/';
			// }
			// 4) put back what we peeled off
			addressString = addressString + argString + hashString;
		}
		int protocolEnd = addressString.indexOf(":");
		if (protocolEnd != -1)
		{
			// this is an absolute URL; check for supported protocol
			String protocol = addressString.substring(0, protocolEnd);
			if (protocolIsUnsupported(protocol))
				return null;
		}
		ParsedURL parsedUrl;
		if (contextPURL == null || addressString.startsWith("http://"))
		{
			parsedUrl = getAbsolute(addressString, "in createFromHTML()");
		}
		else
		{
			ParsedURL directoryPURL = contextPURL.directoryPURL();
			parsedUrl = directoryPURL.getRelative(addressString);
		}

		return parsedUrl;
	}

	/**
	 * 
	 * @return A String version of the URL path, in which all punctuation characters have been changed
	 *         into spaces.
	 */
	public String removePunctuation()
	{
		return StringTools.removePunctuation(toString());
	}

	/**
	 * @return true if they have same domains. false if they have different domains.
	 */
	public boolean sameDomain(ParsedURL other)
	{
		return (other != null) && domain().equals(other.domain());
	}

	/**
	 * @return true if they have same hosts. false if they have different hosts.
	 */
	public boolean sameHost(ParsedURL other)
	{
		return (other != null) && url.getHost().equals(other.url().getHost());
	}

	/**
	 * Use unsupportedMimes and protocolIsSupported to determine if this is content fit for
	 * processing.
	 * 
	 * @return true if this seems to be a web addr we can crawl to. (currently that means html).
	 **/
	public boolean crawlable()
	{
		return protocolIsSupported() && !unsupportedMimes.containsKey(suffix());
	}

	/**
	 * Check whether the protocol is supported or not. Currently, only http and ftp are.
	 */
	public boolean protocolIsSupported()
	{
		return (url != null) && protocolIsSupported(url.getProtocol());
	}

	/**
	 * Check whether the protocol is supported or not. Currently, only http and ftp are.
	 */
	public static boolean protocolIsSupported(String protocol)
	{
		return supportedProtocols.containsKey(protocol);
	}

	/**
	 * Check whether the protocol is supported or not. Currently, only http and ftp are.
	 */
	public boolean protocolIsUnsupported()
	{
		return (url != null) && protocolIsUnsupported(url.getProtocol());
	}

	/**
	 * Check whether the protocol is supported or not. Currently, only http and ftp are.
	 */
	public static boolean protocolIsUnsupported(String protocol)
	{
		return unsupportedProtocols.containsKey(protocol);
	}

	/**
	 * @return true if this is an image file.
	 */
	public boolean isImg()
	{
		return isImageSuffix(suffix());
	}

	/**
	 * 
	 * @param thatSuffix
	 * @return true if the suffix passed in is one for an image type that we can handle.
	 */
	public static boolean isImageSuffix(String thatSuffix)
	{
		return imgSuffixMap.containsKey(thatSuffix);
	}

	/**
	 * @return true if this is a JPEG image file.
	 */
	public boolean isJpeg()
	{
		return jpegSuffixMap.containsKey(suffix());
	}

	/**
	 * @return true if we can tell the image file wont have alpha, just from its suffix. This is
	 *         currently the case for jpeg and bmp.
	 */
	public boolean isNoAlpha()
	{
		return noAlphaSuffixMap.containsKey(suffix());
	}

	/**
	 * Test type of document this refers to.
	 * 
	 * @return true if this refers to an HTML file
	 */
	public boolean isHTML()
	{
		return htmlSuffixMap.containsKey(suffix());
	}

	/**
	 * Test type of document this refers to.
	 * 
	 * @return true if this refers to a PDF file
	 */
	public boolean isPDF()
	{
		return pdfSuffixMap.containsKey(suffix());
	}

	/**
	 * Test type of document this refers to.
	 * 
	 * @return true if this refers to an RSS feed
	 */
	public boolean isRSS()
	{
		return rssSuffixMap.containsKey(suffix());
	}

	int	mimeIndex	= -1;

	/**
	 * Get MimeType index by seeing suffix().
	 * 
	 * @param parsedURL
	 */
	public int mimeIndex()
	{
		if (mimeIndex == -1)
		{
			String suffix = suffix();
			IntSlot mimeSlot = suffixesToMap.get(suffix);
			mimeIndex = (mimeSlot != null) ? mimeSlot.value : UNKNOWN_MIME;
			return mimeIndex;
		}
		else
			return mimeIndex;
	}

	public static int mimeIndex(String location)
	{
		int afterLastDot = location.lastIndexOf('.') + 1;
		int result = UNKNOWN_MIME;
		if ((afterLastDot > 0) && (location.length() > afterLastDot))
		{
			String suffix = location.substring(afterLastDot);
			IntSlot mimeSlot = suffixesToMap.get(suffix);
			if (mimeSlot != null)
				result = mimeSlot.value;
		}
		return result;
	}

	/**
	 * Get Media MimeType indexes. Media MimeTypes are currently text and all kinds of images such as
	 * JPG, GIF, and PNG.
	 * 
	 * @param parsedURL
	 */
	public int mediaMimeIndex()
	{
		return (mimeIndex() >= MimeType.UNKNOWN_MIME) ? MimeType.UNKNOWN_MIME : mimeIndex();
	}

	/*
	 * Check the suffix whether it is in the unsupportedMimes or not. If it is in the
	 * unsupportedMimes, return true, and if it is not, return false.
	 */
	public boolean isUnsupported()
	{
		return unsupportedMimes.containsKey(suffix());
	}

	/*
	 * return the inverse of isUnsupported(). Then, if the suffix is in the unsupportedMimes, return
	 * false, and if it is not, return true.
	 */
	public boolean supportedMime()
	{
		return !isUnsupported();
	}

	/**
	 * @return The directory of this, with protocol and host.
	 */
	public String directoryString()
	{
		String path = pathDirectoryString();

		int portNum = url.getPort();
		String port = (portNum == -1) ? "" : ":" + portNum;
		String host = url.getHost();
		String protocol = url.getProtocol();

		int stringLength = protocol.length() + 3 + host.length() + port.length() + path.length();

		StringBuffer buffy = new StringBuffer(stringLength);
		buffy.append(protocol).append("://").append(host).append(port).append(path);

		return buffy.toString(); // dont copy; wont reuse buffy
	}

	/**
	 * 
	 * @return The directory of this, without protocol and host.
	 */
	public String pathDirectoryString()
	{
		String path = url.getPath();

		int lastSlash = path.lastIndexOf("/");
		int lastDot = path.lastIndexOf(".");
		if (lastDot > lastSlash)
			path = path.substring(0, lastSlash);

		return path;
	}
	
	public String path()
	{
		return (url == null) ? null : url.getFile();
	}
	
	public String pathNoQuery()
	{
	  return url.getPath();
	}

	/**
	 * Return true if the other object is either a ParsedURL or a URL that refers to the same location
	 * as this. Note: this is our own implementation. It is *much* faster and slightly less careful
	 * than JavaSoft's. Checks port, host, file, protocol, and query. Ignores ref = hash.
	 */
	@Override
	public boolean equals(Object other)
	{
		if (other == null)
			return false;

		boolean otherIsPURL = other instanceof ParsedURL;
		boolean otherIsFile = other instanceof File;
		if (otherIsPURL || otherIsFile)
		{
			File otherFile = otherIsFile ? (File) other : ((ParsedURL) other).file;
			if (file != null)
			{
				return file.equals(otherFile);
			}
			if (otherFile != null)
				return false; // other has file but this does not
		}
		else if (!(other instanceof URL))
			return false; // not a PURL or an URL

		URL url = this.url;
		URL otherURL = otherIsPURL ? ((ParsedURL) other).url : (URL) other;

		if (url == null && otherURL == null)
			return true;

		if (url == null || otherURL == null)
			return false;

		// compare port
		if (url.getPort() != otherURL.getPort())
			return false;

		// compare host
		if (!url.getHost().equals(otherURL.getHost()))
			return false;

		// compare file
		if (!url.getFile().equals(otherURL.getFile()))
			return false;

		// compare protocol
		if (!url.getProtocol().equals(otherURL.getProtocol()))
			return false;

		// compare arguments
		return bothNullOrEqual(url.getQuery(), otherURL.getQuery());
	}

	private static boolean bothNullOrEqual(String a, String b)
	{
		return ((a == b) || // both are null or the same string
		((a != null) && a.equals(b))); // now safe to use a.equals()
	}

	/**
	 * Hash this by its URL.
	 */
	@Override
	public int hashCode()
	{
		if (url == null && file == null)
			debug("help!");
		return (url != null) ? url.hashCode() : (file != null) ? file.hashCode() : -1;
	}

	/**
	 * A shorter string for displaing in the modeline for debugging, and in popup messages.
	 */
	public String shortString()
	{
		String shortString = this.shortString;
		if (shortString == null)
		{
			URL url = this.url;
			if (url == null)
				shortString = "null";
			else
			{
				String file = url.getFile();
				shortString = url.getHost() + "/.../" + file.substring(file.lastIndexOf('/') + 1);
			}
			this.shortString = shortString;
		}
		return shortString;
	}

	/**
	 * True if this ParsedURL represents an entity on the local file system.
	 * 
	 * @return true if this is a local File object.
	 */
	public boolean isFile()
	{
		return file != null;
	}

	/**
	 * @return The file system object associated with this, if this is an entity on the local file
	 *         system, or null, otherwise.
	 */
	public File file()
	{
		return file;
	}

	/**
	 * Form a new ParsedURL from this, and the args passed in. A question mark is appended to the
	 * String form of this, and then args are appended.
	 * 
	 * @param args
	 * @return ParsedURL with args after ?
	 */
	public ParsedURL withArgs(String args)
	{
		try
		{
			URL url = new URL(toString() + "?" + args);
			return new ParsedURL(url);
		}
		catch (MalformedURLException e)
		{
			return null;
		}

	}

	/**
	 * Returns the name of the file or directory denoted by this abstract pathname. This is just the
	 * last name in the pathname's name sequence. If the pathname's name sequence is empty, then the
	 * empty string is returned.
	 * <p/>
	 * Analagous to File.getName().
	 * 
	 * @return Name of this, without directory, host, or protocol.
	 */
	public String getName()
	{
		URL url = this.url;
		String path = url.getPath();
		int lastSlash = path.lastIndexOf('/');
		if (lastSlash > -1)
		{
			path = path.substring(lastSlash + 1);
		}
		return path;
	}

	/**
	 * Basic ConnectionHelper. Does *nothing special* when encountering directories, re-directs, ...
	 */
	private static final ConnectionAdapter	connectionAdapter	= new ConnectionAdapter();

	// Set the URLConnection timeout a little smaller than our DownloadMonitor timeout.
	public static final int									CONNECT_TIMEOUT		= 15000;

	public static final int									READ_TIMEOUT			= 25000;

	/**
	 * Create a connection, using the standard timeouts of 23 seconds, and the super-basic
	 * ConnectionAdapter, which does *nothing special* when encountering directories, re-directs, ...
	 * 
	 * @param connectionHelper
	 * @return
	 */
	public PURLConnection connect()
	{
		return connect(connectionAdapter);
	}

	public PURLConnection connect(String userAgentName)
	{
		return connect(connectionAdapter, userAgentName);
	}

	/**
	 * Create a connection, using the standard timeouts of 23 seconds.
	 * 
	 * @param connectionHelper
	 * @return
	 */
	public PURLConnection connect(ConnectionHelper connectionHelper)
	{
		return connect(connectionHelper, DEFAULT_USER_AGENT, CONNECT_TIMEOUT, READ_TIMEOUT);
	}

	public PURLConnection connect(ConnectionHelper connectionHelper, String userAgentString)
	{

		return (userAgentString != null) ? connect(connectionHelper, userAgentString, CONNECT_TIMEOUT,
				READ_TIMEOUT) : connect(connectionHelper);
	}

	/**
	 * Create a connection.
	 * 
	 * @param connectionHelper
	 * @param userAgent
	 *          TODO
	 * @param connectionTimeout
	 * @param readTimeout
	 * @return
	 */
	public PURLConnection connect(ConnectionHelper connectionHelper, String userAgent,
			int connectionTimeout, int readTimeout)
	{
		PURLConnection result	= new PURLConnection(this);
		result.connect(connectionHelper,  userAgent, connectionTimeout,  readTimeout);
		return result;
	}

	/**
	 * Free some memory resources. They can be re-allocated through subsequent lazy evaluation. The
	 * object is still fully functional after this call.
	 */
	public void resetCaches()
	{
		this.directory = null;

		this.string = null;
		this.shortString = null;
		this.lc = null;
		this.suffix = null;
		this.domain = null;

		if (directoryPURL != null)
		{
			this.directoryPURL.recycle();
			this.directoryPURL = null;
		}
	}

	/**
	 * Free <b>all</b> all resources associated with this, rendering it no longer usable.
	 */
	public void recycle()
	{
		resetCaches();
		url = null;
		file = null;
	}

	public String host()
	{
		return (url == null) ? null : url.getHost();
	}
	
	/**
	 * 
	 * @return	A lightweight object corresponding to this, either a URL or a File
	 */
	public Object shadow()
	{
		return (url != null) ? url : file;
	}
	
	public ParsedURL filterArgs(String...argsToKeep)
	{
		if (url != null)
		{
			String query	= url.getQuery();
			StringTokenizer tokenizer	= new StringTokenizer(query, "&");
			if (!tokenizer.hasMoreElements())
				return this;
			StringBuilder resultQuery	= new StringBuilder(noAnchorNoQueryPageString());	// initialize w base URL
			boolean first							= true;
			while (tokenizer.hasMoreElements())
			{
				String token	= tokenizer.nextToken();
				for (String argToKeep: argsToKeep)
				{
					if (token.startsWith(argToKeep))
					{
						if (first)
						{
							first		= false;
							resultQuery.append('?');
						}
						else
							resultQuery.append('&');
						resultQuery.append(token);
					}
				}
			}
			return getAbsolute(resultQuery.toString());
		}
		return this;
	}
	public ParsedURL ignoreArgs(HashMap<String, String> argsToIgnore)
	{
		if (url != null)
		{
			String query	= url.getQuery();
			if (query !=null)
			{
				StringTokenizer tokenizer	= new StringTokenizer(query, "&");
				if (!tokenizer.hasMoreElements())
					return this;
				StringBuilder resultQuery	= new StringBuilder(noAnchorNoQueryPageString());	// initialize w base URL
				boolean first							= true;
				while (tokenizer.hasMoreElements())
				{
					String token	= tokenizer.nextToken();
					int argEnd		= token.indexOf('=');
					String arg		= argEnd == -1 ? token : token.substring(0, argEnd);
					if (!argsToIgnore.containsKey(arg))
					{
						if (first)
						{
							first		= false;
							resultQuery.append('?');
						}
						else
							resultQuery.append('&');
						resultQuery.append(token);
					}
				}
				return getAbsolute(resultQuery.toString());
			}
		}
		return this;
	}
	public String query()
	{
		return url.getQuery();
	}
	static public void main(String[] args)
	{
		try
		{
			URL u	= new URL("http://acm.org/citation.cfm?id=33344");
			System.out.println("query: " + u.getQuery() + "\n" + URLEncoder.encode("?"));
		}
		catch (MalformedURLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Extract arguments from the "query" portion of the URL (the part after ?).
	 * @param keepEmptyParams TODO
	 * 
	 * @return	HashMap of String name / value pairs.
	 */
	public HashMap<String, String> extractParams(boolean keepEmptyParams)
	{
		return StringTools.doubleSplit(url, keepEmptyParams);
	}
	
	/**
	 * Form a new ParsedURL using the base of this, while forming the query from a map of name / value pairs.
	 * 
	 * @param newParamMap	Map of name / value pairs.
	 * 
	 * @return	A new ParsedURL based on this one and the input argument map, or this, if that map is the same as in this.
	 */
	public ParsedURL updateParams(HashMap<String, String> newParamMap)
	{
		HashMap<String, String> oldParamMap	= extractParams(true);
		
		String newArgString	= StringTools.unDoubleSplit(newParamMap);
		String noArgsNoQuery= StringTools.noAnchorPageString(url, false);
		ParsedURL result		= this;
		if (newArgString != null && newArgString.length() > 0)
		{
			//TODO -- check to see if args are the same or different.
			result						= getAbsolute(noArgsNoQuery + '?' + newArgString);
		}
		else if (oldParamMap != null && oldParamMap.size() != 0)
		{
			result						= getAbsolute(noArgsNoQuery);
		}
		return result;
	}
	
	public ParsedURL changeHost(String newHost)
	{
		ParsedURL result	= null;
		if (newHost != null && newHost.length() > 0)
		{
			int port		= url.getPort();
			try
			{
				URL newURL	= (port > 0) ? new URL(url.getProtocol(), newHost, port, url.getFile()) :
					new URL(url.getProtocol(), newHost, url.getFile());
				
				result	= new ParsedURL(newURL);
			}
			catch (MalformedURLException e)
			{
				e.printStackTrace();
			}
		}
		return result;
	}

	public void setIncludePrefix(boolean includePrefix)
	{
		this.includePrefix = includePrefix;
	}

  public String fragment()
  {
    return fragment == null ? url.getRef() : fragment;
  }

}
