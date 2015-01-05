/**
 * 
 */
package ecologylab.appframework.types.prefs;

import ecologylab.serialization.TranslationsClassProvider;

/**
 * Provides the list of Class'es used for translating the PrefSet class. This class is used to
 * provide the base list of Class'es that will be provided to an ApplicationEnvironment.
 * 
 * @author Zachary O. Toups (zach@ecologylab.net)
 */
public class PrefSetBaseClassProvider extends TranslationsClassProvider
{
	public static final PrefSetBaseClassProvider	STATIC_INSTANCE	= new PrefSetBaseClassProvider();

	protected PrefSetBaseClassProvider()
	{
	}

	/**
	 * @see ecologylab.serialization.TranslationsClassProvider#specificSuppliedClasses()
	 */
	@Override
	protected Class<? extends Pref<?>>[] specificSuppliedClasses()
	{
		Class[] prefSetClasses =
		{ MetaPref.class, MetaPrefSet.class, MetaPrefBoolean.class, MetaPrefFloat.class,
				MetaPrefInt.class, MetaPrefString.class,

				Pref.class, PrefSet.class, PrefBoolean.class, PrefDouble.class, PrefFloat.class,
				PrefInt.class, PrefLong.class, PrefString.class, PrefElementState.class, PrefFile.class,
				PrefOp.class,

		};

		return prefSetClasses;
	}
}
