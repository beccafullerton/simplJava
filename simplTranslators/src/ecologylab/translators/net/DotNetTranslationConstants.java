package ecologylab.translators.net;

import ecologylab.serialization.SimplTypesScope;

/**
 * General class file declares various constants used by the translators for generating the right
 * output files.
 * 
 * @author Nabeel Shahzad
 */
public interface DotNetTranslationConstants
{

	/*
	 * File constants
	 */
	public static final String	PACKAGE_NAME_SEPARATOR		= "\\.";

	public static final String	FILE_PATH_SEPARATOR				= "/";

	public static final String	FILE_EXTENSION						= ".cs";

	public static final String	XML_FILE_EXTENSION				= ".xml";

	public static final String	TRANSATIONSCOPE_FOLDER		= "tscope";

	/*
	 * Keywords
	 */
	public static final String	PUBLIC										= "public";

	public static final String	STATIC										= "static";

	public static final String	PRIVATE										= "private";

	public static final String	GET												= "get";

	public static final String	SET												= "set";

	public static final String	CLASS											= "class";

	public static final String	RETURN										= "return";

	public static final String	VALUE											= "value";

	public static final String	INHERITANCE_OBJECT				= "ElementState";

	public static final String	INHERITANCE_OPERATOR			= ":";

	public static final String	NAMESPACE									= "namespace";

	public static final String	USING											= "using";

	public static final String	SYSTEM										= "System";

	public static final String	COLLECTIONS								= "Collections";

	public static final String	GENERIC										= "Generic";

	public static final String	ECOLOGYLAB_NAMESPACE			= "ecologylab.attributes";

	public static final String	SERIALIZATION_NAMESPACE		= "ecologylab.serialization";

	public static final String	KEY												= "key";

	public static final String	TYPE_OF										= "typeof";

	public static final String	DEFAULT_IMPLEMENTATION		= "throw new NotImplementedException();";

	/*
	 * Formatting constants
	 */

	public static final String	OPENING_CURLY_BRACE				= "{";

	public static final String	CLOSING_CURLY_BRACE				= "}";

	public static final String	OPENING_SQUARE_BRACE			= "[";

	public static final String	CLOSING_SQUARE_BRACE			= "]";

	public static final String	OPENING_BRACE							= "(";

	public static final String	CLOSING_BRACE							= ")";

	public static final String	SINGLE_LINE_BREAK					= "\n";

	public static final String	DOUBLE_LINE_BREAK					= "\n\n";

	public static final String	TAB												= "\t";

	public static final String	DOUBLE_TAB								= "\t\t";

	public static final String	SPACE											= " ";

	public static final String	DOT												= ".";

	public static final String	QUOTE											= "\"";

	public static final String	COMMA											= ",";

	public static final String	END_LINE									= ";";

	public static final String	ASSIGN										= "=";

	public static final String	OPEN_COMMENTS							= "/// <summary>";

	public static final String	XML_COMMENTS							= "/// ";

	public static final String	CLOSE_COMMENTS						= "/// </summary>";

	public static final String	SINGLE_LINE_COMMENT				= "//";
	
	public static final String	OPEN_BLOCK_COMMENTS				= "/*";
	
	public static final String	CLOSE_BLOCK_COMMENTS			= "*/";

	/*
	 * Scalar types
	 */
	public static final String	DOTNET_INTEGER						= "Int32";

	public static final String	DOTNET_FLOAT							= "Single";

	public static final String	DOTNET_DOUBLE							= "Double";

	public static final String	DOTNET_BYTE								= "Char";

	public static final String	DOTNET_CHAR								= "Byte";

	public static final String	DOTNET_BOOLEAN						= "Boolean";

	public static final String	DOTNET_LONG								= "Int64";

	public static final String	DOTNET_SHORT							= "Int16";

	public static final String	DOTNET_STRING							= "String";

	/*
	 * Reference types
	 */
	public static final String	DOTNET_DATE								= "DateTime";

	public static final String	DOTNET_STRING_BUILDER			= "StringBuilder";

	public static final String	DOTNET_URL								= "Uri";

	public static final String	DOTNET_PARSED_URL					= "ParsedURL";

	public static final String	DOTNET_SCALAR_TYPE				= "ScalarType";

	public static final String	DOTNET_CLASS							= "Type";

	public static final String	DOTNET_FIELD							= "FieldInfo";

	public static final String	DOTNET_OBJECT							= "Object";

	public static final String	DOTNET_TRANSLATION_SCOPE	= SimplTypesScope.class.getSimpleName();

	/*
	 * Collection types
	 */
	public static final String	DOTNET_ARRAYLIST					= "List";

	public static final String	DOTNET_HASHMAP						= "Dictionary";

	public static final String	DOTNET_HASHMAPARRAYLIST		= "DictionaryList";

	public static final String	DOTNET_SCOPE							= "Scope";

	/*
	 * Other constants
	 */
	public static final String	FGET											= "Get";
	
	public static final String	NEW_OP										= "new";

}
