package legacy.tests.composite;

import simpl.annotations.dbal.simpl_classes;
import simpl.annotations.dbal.simpl_composite;
import simpl.core.SimplTypesScope;
import simpl.exceptions.SIMPLTranslationException;
import simpl.formats.enums.Format;
import legacy.tests.TestCase;
import legacy.tests.TestingUtils;

public class Container implements TestCase
{

	// @simpl_wrap
	@simpl_composite
	@simpl_classes(
	{ WCBase.class, WCSubOne.class, WCSubTwo.class })
	WCBase	wc;

	public Container()
	{
		wc = new WCBase(0);
	}

	@Override
	public void runTest() throws SIMPLTranslationException
	{
		Container c = new Container();

		c.wc = new WCSubTwo(true);

		SimplTypesScope containerTest = SimplTypesScope.get("containerTranslationscope",
				Container.class, WCBase.class, WCSubOne.class, WCSubTwo.class);
		

		TestingUtils.test(c, containerTest, Format.XML);
		TestingUtils.test(c, containerTest, Format.JSON);

	}
}
