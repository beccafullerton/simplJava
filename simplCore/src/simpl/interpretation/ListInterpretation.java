package simpl.interpretation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import simpl.descriptions.ClassDescriptor;
import simpl.descriptions.ClassDescriptors;
import simpl.descriptions.FieldCategorizer;
import simpl.descriptions.FieldDescriptor;
import simpl.exceptions.SIMPLTranslationException;
import simpl.tools.ReflectionTools;
import simpl.types.ListType;

public class ListInterpretation implements SimplInterpretation {

	String fieldName; 
	
	ListType ourListType; 
	
	public ListType getListType()
	{
		return this.ourListType;
	}
	
	public String getFieldName()
	{
		return this.fieldName;
	}
	
	public void setFieldName(String s)
	{
		this.fieldName = s;
	}
	
	List<SimplInterpretation> interps; 
	
	public List<SimplInterpretation> getInterpretations()
	{
		return this.interps;
	}
	
	public ListInterpretation()
	{
		this.interps = new LinkedList<SimplInterpretation>();
		this.ourListType = new ListType();
	}
	
	public ListInterpretation(Class<?> listType)
	{
		this.interps = new LinkedList<SimplInterpretation>();
		this.ourListType = new ListType(listType);
	}
	
	public void addItemInterpretation(SimplInterpretation si)
	{
		this.interps.add(si);
	}
	
	public int size()
	{
		return this.interps.size();
	}
	
	@Override
	public void resolve(Object context, Set<String> refSet,
			UnderstandingContext understandingContext)
			throws SIMPLTranslationException {

		Collection items = (Collection)getValue(context, refSet, understandingContext);

		for(Object item : items)
		{
			try{
				this.ourListType.addTo(context, context.getClass().getField(this.fieldName), item);
			}
			catch(Exception e)
			{
				throw new SIMPLTranslationException(e);
			}
		}
	}

	@Override
	public Object getValue(Object context, Set<String> refSet,
			UnderstandingContext understandingContext)
			throws SIMPLTranslationException {
		
		Collection items = this.ourListType.createInstance(); // default. 
		
		for(int i = 0; i < this.interps.size(); i++)
		{
			items.add(interps.get(i).getValue(context, refSet, understandingContext));
		}
		
		return items;
	}

	@Override
	public SimplInterpretation interpret(Object context, FieldDescriptor field,
			InterpretationContext interpretationContext)
			throws SIMPLTranslationException {
		
		ListInterpretation li = (ListInterpretation) interpretObject(ReflectionTools.getFieldValue(field.getField(), context), interpretationContext);
		
		li.setFieldName(field.getName());
		
		return li;		
	}

	@Override
	public SimplInterpretation interpretObject(Object theObject,
			InterpretationContext interpretationContext) throws SIMPLTranslationException {
	
		ListInterpretation li = new ListInterpretation(theObject.getClass());
		
		Collection theCollection  = (Collection) theObject;
				
		for(Object o: theCollection)
		{
			SimplInterpretation si = interpretationContext.interpretObject(o);
			li.addItemInterpretation(si);
		}
		
		return li;
	}
	
}