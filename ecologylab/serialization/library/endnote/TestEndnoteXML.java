/*
 * Created on Dec 12, 2006
 */
package ecologylab.serialization.library.endnote;

import java.io.File;

import ecologylab.serialization.ClassDescriptor;
import ecologylab.serialization.Format;
import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.StringFormat;

public class TestEndnoteXML
{

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		try
		{
			XmlState fromFile = (XmlState) EndnoteNameSpace.get().deserialize(
					new File("/Users/toupsz/Desktop/RSBib.xml"), Format.XML);

			ClassDescriptor.serialize(fromFile, System.out, StringFormat.XML);
		}
		catch (SIMPLTranslationException e)
		{
			e.printStackTrace();
		}
	}

}
