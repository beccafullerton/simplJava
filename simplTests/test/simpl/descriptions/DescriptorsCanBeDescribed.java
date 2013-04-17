package simpl.descriptions;

import org.junit.Before;
import org.junit.Test;

import simpl.core.SimplTypesScope;
import simpl.core.SimplTypesScopeFactory;
import simpl.descriptions.ClassDescriptor;
import simpl.descriptions.FieldDescriptor;


public class DescriptorsCanBeDescribed{


	@Before
	public void ResetSTS()
	{
		SimplTypesScope.ResetAllTypesScopes();
	}

	@Test
	public void FieldDescriptorCanBeDescribed()
	{
		ClassDescriptor cd = ClassDescriptors.getClassDescriptor(FieldDescriptor.class);
	}

	@Test
	public void ClassDescriptorCanBeDescribed()
	{
		ClassDescriptor cd = ClassDescriptors.getClassDescriptor(ClassDescriptor.class);
	}

	
	@Test
	public void YinsCase()
	{
		SimplTypesScope tscope = (SimplTypesScope)SimplTypesScopeFactory.name("test-de/serialize descriptors in json")
                .translations(FieldDescriptor.class,
                ClassDescriptor.class,
                SimplTypesScope.class).create();

	}
	//TODO: Better validation on these.
	//They're sparse because they're just trying to catch silly exceptions.



}