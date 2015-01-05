package ecologylab.serialization.serializers.binaryformats;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.TranslationContext;
import ecologylab.serialization.XMLTools;
import ecologylab.serialization.serializers.FormatSerializer;

public abstract class BinarySerializer extends FormatSerializer
{

	@Override
	public void serialize(Object object, OutputStream outputStream,
			TranslationContext translationContext) throws SIMPLTranslationException
	{
		serialize(object, new DataOutputStream(outputStream), translationContext);
	}

	@Override
	public void serialize(Object object, File outputFile, TranslationContext translationContext)
			throws SIMPLTranslationException
	{
		try
		{
			XMLTools.createParentDirs(outputFile);

			if (outputFile.getParentFile() != null)
				translationContext.setBaseDirFile(outputFile.getParentFile());

			FileOutputStream fStream = new FileOutputStream(outputFile);
			serialize(object, fStream, translationContext);

			fStream.close();
		}
		catch (IOException e)
		{
			throw new SIMPLTranslationException("IO Exception: ", e);
		}

	}

	/**
	 * 
	 * @param object
	 * @param dataOutputStream
	 * @param translationContext
	 * @throws SIMPLTranslationException
	 */
	public abstract void serialize(Object object, DataOutputStream dataOutputStream,
			TranslationContext translationContext) throws SIMPLTranslationException;

}
