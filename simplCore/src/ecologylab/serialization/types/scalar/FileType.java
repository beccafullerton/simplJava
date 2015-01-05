/*
 * Created on Dec 22, 2006
 */
package ecologylab.serialization.types.scalar;

import java.io.File;
import java.io.IOException;

import ecologylab.generic.StringTools;
import ecologylab.io.Files;
import ecologylab.serialization.ScalarUnmarshallingContext;
import ecologylab.serialization.TranslationContext;
import ecologylab.serialization.annotations.simpl_inherit;
import ecologylab.serialization.types.CrossLanguageTypeConstants;

/**
 * @author Zachary O. Toups (toupsz@cs.tamu.edu)
 */
@simpl_inherit
public class FileType extends ReferenceType<File> implements CrossLanguageTypeConstants
{

	/**
     */
	public FileType()
	{
		super(File.class, JAVA_FILE, DOTNET_FILE, OBJC_FILE, null);
	}

	@Override
	public File getInstance(String value, String[] formatStrings,
			ScalarUnmarshallingContext scalarUnmarshallingContext)
	{
		File fileContext = (scalarUnmarshallingContext == null) ? null : scalarUnmarshallingContext
				.fileContext();
		File file = (fileContext == null) ? new File(value) : new File(fileContext, value);
		try
		{
			return file.getCanonicalFile();
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return file;
		}
	}

	@Override
	public String marshall(File instance, TranslationContext serializationContext)
	{
		File contextualizedInstance = instance;
		if (serializationContext != null)
		{
			File fileContext = serializationContext.fileContext();
			if (fileContext != null)
			{
				String pathRelativeTo = StringTools.getPathRelativeTo(instance.getAbsolutePath(),
						fileContext.getAbsolutePath(), Files.sep);
				if (pathRelativeTo != null)
					return pathRelativeTo;
			}
		}

		return super.marshall(contextualizedInstance, serializationContext);
	}
}
