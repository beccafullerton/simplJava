package cm.generic;

import java.io.*;
import java.util.*;

public class Files
extends Debug
{
   // to update, add entry here, and below in deriveLang()
   public static final int html=0;
   public static final int java=1;
   public static final int javascript=2;
   public static final int asp=3;
   public static final int css=4;
   public static final int exe=5;
   
   public File		outFile, inFile;
   public BufferedReader	fileReader;
   public BufferedWriter	fileWriter;
   String tempFileName;
   
   static String	readErrorString = "Error reading from file ";
   static String	writeErrorString = "Error writing to file ";
   
   public static final char	sep	= File.separatorChar;
   static public final String	indent	= "\t";   

   public Files()
   {
   }
   
   public Files(String inFileName)
   {
      setInFile(inFileName);
   }
   public void setInFile(String inFileName)
   {
      inFile	= newFile(inFileName);
   }
/**
 * More robust than just calling the File constructor, cause it
 * converts all slashes to the direction native for the platform.
 */
   public static File newFile(String inFileName)
   {
      return newFile(null, inFileName);
   }
   public static File newFile(File context, String inFileName)
   {
      // one of the replaces should do something;
      // and the other should do nothing.
//      Debug.println("newFile("+context+","+inFileName);
      String fixedFileName      =inFileName.replace('/',sep).replace('\\',sep);
      File one	= new File(fixedFileName);
      if (context == null)
	 return one;
      if (one.isAbsolute() || (context == null))
      {
	 // hack gnarly windows path shit
	 // (drives & backslashes are a plague on all programmers)
	 if (fixedFileName.charAt(1) != ':')
	 {
	    String contextName	= context.getPath();
	    if (contextName.charAt(1) == ':')
	    {
	       String fixedFixed= contextName.substring(0,2) + fixedFileName;
	       return new File(fixedFixed);
	    }
	 }
	 return one;
      }
      // else
//      Debug.println("newFile: (0)="+fixedFileName.charAt(0) + " (1)"+
//			 fixedFileName.charAt(0));
//      if ((fixedFileName.charAt(1) == ':') || (fixedFileName.charAt(0) == sep))
//	 return new File(fixedFileName);
//      else
	 return new File(context, fixedFileName);
   }
   public void close()
   {
      closeReader();
      closeWriter();
   }
   public boolean closeReader()
   {
      return closeReader(fileReader);
   }
   public static boolean closeReader(BufferedReader reader)
   {
      boolean ok	= true;
      if (reader != null)
	 try
	 { 
	    reader.close();
	 } catch (IOException ioe) { ok = false; }
      return ok;
   }
   public boolean closeWriter()
   {
      return closeWriter(fileWriter);
   }

   public static boolean closeWriter(BufferedWriter writer)
   {
      boolean ok	= true;
      try
      {
	 writer.close();
      } catch (IOException outE){ ok = false; }
      return ok;
   }
   public static BufferedReader openReader(String fileName)
   {
      return openReader(newFile(fileName));
   }
   
   public static BufferedReader openReader(File file)
   {
      boolean ok	= file.canRead();
      BufferedReader reader = null;
      if (!ok)
      {
	 Debug.println("Can't read file " + file.getAbsolutePath());
      }
      else
      {
	 try
	 {  
	    reader	= new BufferedReader(new FileReader(file));
	    println(3, "Input file: ");
	    println(3, indent + file + "");
	 }		
	 catch(IOException e)
	 {
	    Debug.println(readErrorString + file);
	 }
      }
      return reader;
   }
   
   public boolean openRead(File inputFile)
   {
      inFile		= inputFile;
      return openReader();
   }
   public boolean openRead(String inFileName)
   {
      setInFile(inFileName);
      return openReader();
   }
   
   public static String getTempFileName(String name)
   {
      Date	myZone	= new Date();
      return sep + "temp" + sep + name + myZone.getTime();
   }
   public boolean openReader()
   {
      fileReader	= openReader(inFile);
      return (fileReader != null);
   }
   public String getDir(String fullPath)
   {
      int index = fullPath.lastIndexOf(File.separator);
      if (index == -1)
	 return "";
      return fullPath.substring(0,index);
   }

   public boolean openWrite(String outFileName)
   {
      return openWrite(newFile(outFileName));
   }
   public boolean openWrite(File outDir, String outFileName)
   {
      return openWrite(newFile(outDir, outFileName));
   }
   public boolean openWrite(File oFile)
   {
      outFile			= oFile;
      BufferedWriter writer	= openWriter(oFile);
      boolean ok = (writer != null);
      if (ok)
	 fileWriter		= writer;
      return ok;
   }
   
   public static BufferedWriter openWriter(String oFileName)
   {
      return openWriter(newFile(oFileName));
   }
   public static BufferedWriter openWriter(File oFile)
   {
      return openWriter(oFile, 1);
   }
/**
 * Make any directories necesary for the file to be written.
 */   
   public static boolean makePath(File oFile)
   {
      String	oFileDirName	= oFile.getParent();
      File	oFileDir	= newFile(oFileDirName);
      boolean result		= !oFileDir.equals("");
      if (result)		   // if ok, continue
      {  
	 result			= oFileDir.exists();
	 if (!result)		   // if exists, no more work to do
	 {
	    println("\t");
	    println(oFileDir + " ");
	    result		= oFileDir.mkdirs(); 
	    if (!result)
	       Debug.println("[Making directories for you: failed.]");
	 }       
      }
      else
	 Debug.println("Can't write to " + oFile.getAbsolutePath());
      return result;
   }
   public static BufferedWriter openWriter(File oFile, int debugLevel)
   {
      BufferedWriter writer	= null;

      String	oFileFullPath	= oFile.getAbsolutePath();

      println("Files.openWriter(" + oFileFullPath);
      
//      if (makePath(oFile)) // maybe create directories along the path
	 try
	 {  
	    writer	= new BufferedWriter (new FileWriter(oFile));
	    println("\tOutput file: ");
	    println(indent + indent + oFileFullPath);
	 }		
	 catch(IOException e)
	 {
	    Debug.println(writeErrorString + oFileFullPath);
	 }
      return writer;
   }
   public static FileInputStream openInStream(File inFile)
   {
      FileInputStream	stream	= null;
      try
      {
	 stream	= new FileInputStream(inFile);
      } catch (Exception e)
      {
	 Debug.println("Can't open input stream from " + inFile + " " + e);
      }
      return stream;
   }
   
   public static BufferedOutputStream openOutStream(File oFile)
   {
      BufferedOutputStream outStream	= null;

      String	oFileFullPath	= oFile.getAbsolutePath();

      if (makePath(oFile)) // maybe create directories along the path
	 try
	 {  
	    outStream	=
	       new BufferedOutputStream(new FileOutputStream(oFile));
	    println(3, "Output file: ");
	    println(3, indent + oFileFullPath);
	 }		
	 catch(IOException e)
	 {
	    Debug.println(writeErrorString + oFileFullPath + e);
	 }
      return outStream;
   }

   public String readLine()
   {
      return readLine(fileReader);
   }
   public static String readLine(BufferedReader reader)
   {
      String aLine = " ";
      try
      {
	 aLine	= reader.readLine();
      }
      catch(IOException e)
      {
	 Debug.println(readErrorString + reader);
      }
      return aLine;
   }

   public boolean write(String toWrite)
   {
      return write(fileWriter, toWrite);
   }
   public static boolean write(BufferedWriter writer, String toWrite)
   {
      if ((toWrite == null) || (toWrite.equals("")))
	 return true;
      boolean	ok		= true;
      try
      {  
//	 Env.println("BufferedWriter " + writer + " toWrite=" +
//			    toWrite);
	 writer.write(toWrite);
      }
	 catch(IOException e) {ok = false; Debug.println(e + "");}
      if (!ok)
      	 Debug.println(writeErrorString + writer);
      return ok;
   }


   // write a line of output
   public boolean writeLine(String toWrite)
   {
//      Env.println("really writing a line");
      return writeLine(fileWriter, toWrite);
   }
   public static boolean writeLine(BufferedWriter writer, String toWrite)
   {
      if (toWrite == null)
	 return true;
      boolean	ok		= true;
      try
      {  
	 writer.write(toWrite);
	Debug.println("writeLine: " + toWrite);
	 writer.newLine();
      }
	 catch(IOException e) {ok = false;}
      if (!ok)
      	 Debug.println(writeErrorString + writer);
      return ok;
   }
   

   public void rename(String finalName)
   { 
      File newFile	= newFile(finalName); 
      outFile.renameTo(newFile); 
      // Env.println("All done!!!!!");
   }

   public boolean insertFile(String fileToInsert)
   {
      return insertFile(fileWriter, fileToInsert);
   }
   
   public static boolean insertFile(BufferedWriter writer,
				    String fileToInsert)
   {
      BufferedReader reader	= openReader(fileToInsert);
      boolean ok		= (reader != null);
      if (ok)
      {
	 String line;
	 for (line=readLine(reader); line!=null; line=readLine(reader))
	 {
//	    Env.println("into " + reader + " inserting: " + line);
	    writeLine(writer, line);
	 }
      }
      try 
      {
	 reader.close();
      } catch (IOException e) { ok = false; }
      return ok;
   }
   public static boolean isDir(File file)
   {
      if (file.isDirectory())
	 return true;
      // this is a bit of a hack -- it assumes files w suffixes
      String lastPart	= file.getName();
      return lastPart.indexOf(".") < 0;
   }
   // ????? i dunno where this was being used (CMDOC???)
   // write a line of output
//   public boolean writeLine(String toWrite)
//   {
//      return writeLine(toWrite, false);
//   }
   public static boolean copy(File from, File to)
   {
      if (!from.exists())
      {
	 println("I can't copy because<br>\n" + from.toString() +
		     " doesn't exist.");
	 println("");
	 return false;
      }
      println(2, "Copy:");
      println(2, indent + from);
      println(2, indent + to);
      
      File dir;
      if (isDir(to))
      {
	 dir	= to;
	 to	= newFile(to, from.getName());
      }
      else
	 dir	= newFile(to.getParent());
      
      if (!dir.exists())
	 dir.mkdirs();
      BufferedReader input	= null;
      BufferedWriter output	= null;
      boolean ok		= true;
      try
      {
	 if (to.exists())
	    to.delete();
	 input		= new BufferedReader(new FileReader(from));
	 output		= new BufferedWriter(new FileWriter(to));
//	 output			= new FileOutputStream(to);
      } catch (IOException e)
      {
	 Debug.println("Files.copy error opening streams: " +
		   e);
	 ok			= false;
      }
      if (ok)
      {
	 char	     buf[]	= new char[32768]; // 32 K
	 int count		= 0;
	 try
	 {
	    ok		= false;

	    while ((count = input.read(buf)) >= 0)
	    {
	       output.write(buf, 0, count);
	    }
	    ok		= true;
	 } catch (IOException e)
	 {
	    File errorFile	= ok ? to : from;
	    String operation	= ok ? "writing" : "reading";
	    Debug.println("I/O error during copying, while " + operation +
		      " " + errorFile.getAbsolutePath());
	    ok = false;
	 }
	 try
	 {
	    ok		= false;
	    input.close();
	    ok		= true;
	    output.close();
	 }  catch (IOException e)
	 {
	    File errorFile	= ok ? to : from;
	    Debug.println("I/O error during copying, while closing "
		      + errorFile.getAbsolutePath());
	    ok = false;
	 }
      }
      return ok;
   }
   public static String relativePath(File parent, File child)
   {
      String parentPath	= parent.getAbsolutePath();
      String childPath	= child.getAbsolutePath();
      return relativePath(parentPath, childPath);
   }
   public static String relativePath(File parent, String childPath)
   {
      String parentPath	= parent.getAbsolutePath();
      return relativePath(parentPath, childPath);
   }
   public static String relativePath(String parentPath, String childPath)
   {
      println(3, "Relative Path:");
      println(3, indent + parentPath);
      println(3, indent + childPath);
      if (parentPath.equals(childPath))
	 return "";
      if (childPath.indexOf(parentPath) > -1)
      {
	 println(3,indent+childPath.substring(parentPath.length() + 1));
	 return childPath.substring(parentPath.length() + 1);
      }
      else
	 return null;
   }
   public static String getBeforeExtension(File file)
   {
      return getBeforeExtension(file.getName());
   }
   public static String getBeforeExtension(String fName)
   {
      String before	= "";
      int dot	= fName.lastIndexOf(".");
      if (dot >= 0)
	 before	= fName.substring(0,dot);
      return before;
   }
   public static String getExtension(File file)
   {
      return getExtension(file.getName());
   }
   public static String getExtension(String fName)
   {
      String ext= "";
      int dot	= fName.lastIndexOf(".");
      if ((dot >= 0) && (dot < fName.length()-1))
	 ext	= fName.substring(dot+1);
      return ext;
   }
   public static int deriveLang(String fName)
   {
      int lang	= 0;
      String ext = getExtension(fName);
      println(4,"deriveLang got extension='"+ext+"'");
      if (ext.equalsIgnoreCase("html") || ext.equalsIgnoreCase("htm"))
	 lang	= html;
      else if (ext.equalsIgnoreCase("java"))
	 lang	= java;
      else if (ext.equalsIgnoreCase("js"))
	 lang	= javascript;
      else if (ext.equalsIgnoreCase("asp"))
	 lang	= asp;
      else if (ext.equalsIgnoreCase("css"))
	 lang	= css;
      else if (ext.equalsIgnoreCase("exe"))
	 lang	= exe;
      return lang;
   }
   public static String unix(File f)
   {
      return f.getAbsolutePath().replace(sep,'/');
   }
   public static String unix(String s)
   {
      return s.replace(sep,'/');
   }
   public static boolean contains(String fileName, String s)
   {
      return contains(fileName, s);
   }
   public static boolean contains(File f, String s)
   {
      BufferedReader reader = openReader(f);
      for (String line=Files.readLine(reader); line!=null;
	   line=Files.readLine(reader))
      {
	 if (line.indexOf(s) > -1)
	    return true;
      }
      return false;
   }
   public static void main(String[] s)
   {
      copy(newFile(s[0]), newFile(s[1]));
   }
}
