/**
 * 
 */
package ecologylab.standalone;

import simpl.core.SimplTypesScope;
import simpl.exceptions.SIMPLTranslationException;
import ecologylab.appframework.SingletonApplicationEnvironment;
import ecologylab.appframework.types.prefs.PrefsTranslationsProvider;
import ecologylab.appframework.types.prefs.gui.PrefsEditor;

/**
 * Standalone app to open a prefs editing dialog.
 * 
 * @author Cae
 * @author andruid
 */
public class PrefsEditorAp extends SingletonApplicationEnvironment 
{
	
	public PrefsEditorAp(String[] args) throws SIMPLTranslationException 
	{
		super("ecologyLabFundamental", PrefsTranslationsProvider.get(), (SimplTypesScope) null, args, 0);

		this.createPrefsEditor(true, true);
	}
	/**
	 * @param args
	 * @throws SIMPLTranslationException 
	 */
	public static void main(String[] args) throws SIMPLTranslationException 
	{
		new PrefsEditorAp(args);
	}

}
