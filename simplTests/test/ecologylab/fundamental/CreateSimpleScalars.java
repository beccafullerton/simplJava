package ecologylab.fundamental;

import java.io.File;
import java.io.IOException;
import com.google.common.io.Files;
import com.google.common.base.Charsets;

public class CreateSimpleScalars {

	
	static String getRepr(String name, String type, String include)
	{
		StringBuilder sb = new StringBuilder();
		
		sb.append("package ecologylab.fundamental.simplescalar;\r\n");
		
		if(!include.isEmpty()){
			sb.append("import ");
			sb.append(include);
			sb.append(";\r\n");
		}
		
		sb.append("import ecologylab.serialization.annotations.simpl_scalar;\r\n");
		sb.append(String.format("public class Simple%s {\r\n", name));
		sb.append("\t@simpl_scalar\r\n");
		sb.append(String.format("\tprivate %s simple%s;\r\n", type, name.toLowerCase()));
		sb.append(String.format("\r\n\tpublic %s getSimple%s(){ \r\n", type, name));
		sb.append(String.format("\t\t return this.simple%s;\r\n", name.toLowerCase()));
		sb.append("\t}\r\n");
		sb.append(String.format("\r\n\tpublic void setSimple%s(%s value){\r\n", name, type));
		sb.append(String.format("\t\tthis.simple%s = value;\r\n", name.toLowerCase()));
		sb.append("\t}\r\n");
		sb.append("}\r\n");
		
		return sb.toString();
	}
	
	
	static File getFor(String name) throws IOException
	{
		File f = new File("C:\\Users\\tomwhite\\Documents\\test\\Simple" + name + ".java");
		f.createNewFile();
		return f;
	}
	
	static void Create(String name, String type, String include) throws IOException
	{
		File f = getFor(name);

		Files.write(getRepr(name, type, include), f, Charsets.UTF_8);
	}
	
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		
		Create("Integer", "Integer", "");
		Create("primint", "int" , "");
		
		Create("String", "String", "");
		
		Create("primdouble", "double", "");
		Create("Double", "Double", "");
		
		Create("primfloat", "float", "");
		Create("Float", "Float","");
		
		Create("primshort", "short", "");
		Create("Short", "Short","");
		
		Create("primlong", "long", "");
		Create("Long", "Long", "");
		
		Create("Boolean", "Boolean", "");
		Create("primboolean", "boolean", "");
		
		Create("Byte", "Byte", "");
		Create("primbyte","byte","");

		Create("Char", "Character", "");
		Create("primchar", "char", "");

		Create("Date", "Date", "java.util.Date");
		
		Create("ParsedURL", "ParsedURL", "ecologylab.net.ParsedURL");
		
		Create("Pattern", "Pattern", "java.util.regex.Pattern");
		
		Create("StringBuilder", "StringBuilder", "");

		Create("JavaURL", "URL", "java.net.URL");
		
		Create("UUID", "UUID", "java.util.UUID");
		
	}

}
