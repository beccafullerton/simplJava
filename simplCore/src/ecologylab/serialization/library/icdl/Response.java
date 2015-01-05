/**
 * 
 */
package ecologylab.serialization.library.icdl;

import java.util.ArrayList;

import ecologylab.serialization.ElementState;
import ecologylab.serialization.annotations.Hint;
import ecologylab.serialization.annotations.simpl_collection;
import ecologylab.serialization.annotations.simpl_hints;
import ecologylab.serialization.annotations.simpl_inherit;
import ecologylab.serialization.annotations.simpl_nowrap;
import ecologylab.serialization.annotations.simpl_scalar;
/**
 * The root element in a reply to ICDL BookXMLResults.
 * 
 * http://www.childrenslibrary.org/icdl/BookXMLResults?ids=133,265&viewids=&prefcollids=474&langid=&text=&lang=English&match=all&sort=title&pnum=1&pgct=8&ptype=simple
 * can be reduced to
 * http://www.childrenslibrary.org/icdl/BookXMLResults?ids=133,265&prefcollids=474&lang=English&sort=title&ptype=simple
 * @author andruid
 */
@simpl_inherit
public class Response extends ElementState
{
	@simpl_scalar @simpl_hints(Hint.XML_LEAF)	int		pnum;
	@simpl_scalar @simpl_hints(Hint.XML_LEAF)	int		total;
	
	@simpl_collection("Book")
	@simpl_nowrap
	ArrayList<Book> books;
	
	/**
	 * 
	 */
	public Response()
	{
		super();

	}
	
	public ArrayList<Book> getBooks()
	{
		if (books != null)
			return books;
		return books = new ArrayList<Book>();
	}

}
