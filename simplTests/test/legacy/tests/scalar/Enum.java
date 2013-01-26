package legacy.tests.scalar;

import static org.junit.Assert.*;
import legacy.tests.TestCase;
import legacy.tests.TestingUtils;
import legacy.tests.circle.Circle;
import legacy.tests.circle.Point;

import org.junit.Test;

import simpl.annotations.dbal.simpl_scalar;
import simpl.core.SimplTypesScope;
import simpl.exceptions.SIMPLTranslationException;
import simpl.formats.enums.Format;



public class Enum implements TestCase {

	@simpl_scalar
	NonsenseEnum p;
	public Enum()
	{
		
	}
	public Enum(NonsenseEnum pile) {
		p = pile;
	}

	@Override
	public void runTest() throws SIMPLTranslationException {
	
		Enum c = new Enum(NonsenseEnum.BUCKLE_MY_SHOES);
		SimplTypesScope t = SimplTypesScope.get("enumTest", Enum.class);		
		SimplTypesScope.enableGraphSerialization();

		TestingUtils.test(c, t, Format.XML);
		TestingUtils.test(c, t, Format.JSON);		
	}

}
