package ecologylab.oodss.logging.translationScope;

import simpl.core.TranslationsClassProvider;
import ecologylab.logging.MixedInitiativeOp;

/**
 * Provide base log operation classes for translating a polymorphic list of Ops.
 * 
 * @author Zachary O. Toups (zach@ecologylab.net)
 */
public class MixedInitiativeOpClassesProvider extends TranslationsClassProvider
{
	public static final MixedInitiativeOpClassesProvider	STATIC_INSTANCE	= new MixedInitiativeOpClassesProvider();

	protected MixedInitiativeOpClassesProvider()
	{

	}

	/**
	 * @see simpl.core.TranslationsClassProvider#specificSuppliedClasses()
	 */
	@Override
	protected Class[] specificSuppliedClasses()
	{
		Class mixedInitiativeOpClasses[] =
		{ MixedInitiativeOp.class };

		return mixedInitiativeOpClasses;
	}
}
