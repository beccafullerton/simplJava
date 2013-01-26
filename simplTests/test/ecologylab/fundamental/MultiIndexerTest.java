package ecologylab.fundamental;

import static org.junit.Assert.*;

import org.junit.Test;

import simpl.descriptions.ClassDescriptor;
import simpl.descriptions.indexers.ClassDescriptorIndexer;

import ecologylab.generic.MultiIndexer;

public class MultiIndexerTest {

	@Test
	public void testInsert() {

		final class myClass{
		}
		
		MultiIndexer<ClassDescriptor<?>> sut = new ClassDescriptorIndexer();
		
		ClassDescriptor<?> relevant = ClassDescriptor.getClassDescriptor(myClass.class);
		
		sut.Insert(relevant);
		
		ClassDescriptor<?> result = sut.by("simplname").get(relevant.getClassSimpleName());
			
		assertEquals(relevant, result);
		assertTrue(1==sut.size());
		
		
	}
	
	@Test
	public void testRemove()
	{
		final class myClass{}
		
		MultiIndexer<ClassDescriptor<?>> sut = new ClassDescriptorIndexer();
		
		ClassDescriptor<?> relevant = ClassDescriptor.getClassDescriptor(myClass.class);
		
		sut.Insert(relevant);
		
		assertTrue(1==sut.size());
		
		sut.Remove(relevant);
		
		assertTrue(0==sut.size());
		
		assertNull(sut.by("simplname").get(relevant.getClassSimpleName()));
	}

}
