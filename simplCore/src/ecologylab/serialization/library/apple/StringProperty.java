/**
 * 
 */
package ecologylab.serialization.library.apple;

import ecologylab.serialization.annotations.Hint;
import ecologylab.serialization.annotations.simpl_hints;
import ecologylab.serialization.annotations.simpl_inherit;
import ecologylab.serialization.annotations.simpl_scalar;
import ecologylab.serialization.annotations.simpl_tag;

/**
 * @author Zachary O. Toups (zach@ecologylab.net)
 */
@simpl_tag("string")
@simpl_inherit
public class StringProperty extends Property
{
	@simpl_hints(
	{ Hint.XML_TEXT })
	@simpl_scalar
	String	contents;

	/**
	 * 
	 */
	public StringProperty()
	{
	}

	public StringProperty(String string)
	{
		this.contents = string;
	}

	public String getContents()
	{
		return contents;
	}

	@Override
	public String toString()
	{
		return "StringProperty: " + contents;
	}
}
