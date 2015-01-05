/**
 * 
 */
package ecologylab.appframework.types.prefs;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import ecologylab.net.ParsedURL;
import ecologylab.serialization.ElementState;
import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.SimplTypesScope;
import ecologylab.serialization.annotations.simpl_collection;
import ecologylab.serialization.annotations.simpl_inherit;
import ecologylab.serialization.annotations.simpl_nowrap;
import ecologylab.serialization.annotations.simpl_scalar;
import ecologylab.serialization.annotations.simpl_scope;
import ecologylab.serialization.formatenums.Format;
/**
 * Groupings of MetaPrefs, by category. Categories
 * are also ordered in a separate ArrayList.
 * Contains functions related to adding MetaPrefs to the
 * grouping, getting MetaPrefs, getting the categories, etc.
 * 
 * @author Cae
 *
 */

@simpl_inherit
public class MetaPrefSet extends ElementState
{
	@simpl_scalar	String	title;
	@simpl_scalar	int		width;
	@simpl_scalar	int		height;
	
	@simpl_collection
	@simpl_nowrap
	@simpl_scope(MetaPrefsTranslationScope.NAME)
	ArrayList<MetaPref> metaPreferences;
	
    /**
     * HashMap of category Strings to ArrayList of MetaPrefs.
     */
    private HashMap<String, ArrayList<MetaPref>> categoryToMetaPrefs = new HashMap<String, ArrayList<MetaPref>>();
    /**
     * ArrayList of category Strings, in the order they are gotten from
     * the xml file. This is used to order the tabs in GUI creation.
     */
    private ArrayList<String> orderOfTabs = new ArrayList<String>();
    
	/**
	 * Perform custom processing on the newly created child node,
	 * just before it is added to this.
	 * <p/>
	 * This is part of depth-first traversal during translateFromXML().
	 * <p/>
	 * Add the entry to the category map.
	 * <p/>
	 * Also, create a Pref to match the MetaPref child, if there isn't one already.
	 * 
	 * @param child
	 */
	@Override
	protected void createChildHook(ElementState child)
	{
		MetaPref metaPref = (MetaPref) child;
		addEntryToCategoryMap(metaPref);
		metaPref.getAssociatedPref();	// create one if needed
	}
    
	/**
	 * Add entry to the Category-MetaPrefs map.
	 * @param metaPref
	 */
	void addEntryToCategoryMap(MetaPref metaPref)
	{
        ArrayList<MetaPref> metaPrefList = categoryToMetaPrefs.get(metaPref.getCategory());
        if (metaPrefList == null)
        {
            metaPrefList = new ArrayList<MetaPref>();
            categoryToMetaPrefs.put(metaPref.category, metaPrefList);
            orderOfTabs.add(metaPref.category);
        }
        metaPrefList.add(metaPref);		
	}
    
	/**
	 * 
	 */
	public MetaPrefSet() 
	{

	}

    /**
     * Return the ordered list of categories.
     * 
     * @param tabList   String Array that will hold category names.
     * 
     * @return tabList  String Array that will hold category names.
     */
    public String[] getOrderedTabNames(String[] tabList)
    {
        return orderOfTabs.toArray(tabList);
    }
    
    /**
     * Return the number of categories.
     * 
     * @return Number of categories
     */
    public int getNumberOfTabs()
    {
        return orderOfTabs.size();
    }
    
    /**
     * Get the category names in a Set of Strings.
     * 
     * @return String Set of categories.
     */
    public Set<String> getCategories()
    {
        return categoryToMetaPrefs.keySet();
    }
    
    /**
     * Get the MetaPref ArrayList for a category name.
     * 
     * @param cat   Name of category
     * 
     * @return ArrayList of MetaPrefs
     */
    public ArrayList<MetaPref> getMetaPrefListByCategory(String cat)
    {
        return categoryToMetaPrefs.get(cat);
    }

    /**
     * Register the MetaPref in the static global map, as well as adding it to the super ArrayListState.
     * @param metaPref
     * @return
     */
    public boolean add(MetaPref metaPref)
    {
        boolean result  = metaPreferences.add(metaPref);
        metaPref.register();
        return result;
    }
    
    /**
     * Read MetaPref declarations from a file or across the net.
     * 
     * @param file
     * @param translationScope
     * @return
     * @throws SIMPLTranslationException
     */
    public static MetaPrefSet load(File file, SimplTypesScope translationScope) 
    throws SIMPLTranslationException
    {
		 return load(new ParsedURL(file), translationScope);
    	
    }
	
    /**
     * Read MetaPref declarations from a file or across the net.
     * 
     * @param purl
     * @param translationScope
     * @return
     * @throws SIMPLTranslationException
     */
    public static MetaPrefSet load(ParsedURL purl, SimplTypesScope translationScope) 
    throws SIMPLTranslationException
    {
		 return (MetaPrefSet) translationScope.deserialize(purl, Format.XML);
    	
    }

	public int getHeight()
	{
		return height;
	}

	public String getTitle()
	{
		return title;
	}

	public int getWidth()
	{
		return width;
	}
}
