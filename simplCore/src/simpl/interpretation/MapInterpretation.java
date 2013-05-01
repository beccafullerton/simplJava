package simpl.interpretation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import simpl.descriptions.FieldDescriptor;
import simpl.exceptions.SIMPLTranslationException;
import simpl.tools.ReflectionTools;
import simpl.types.MapType;

public class MapInterpretation implements SimplInterpretation{

	
	
	public MapInterpretation()
	{
		this.entryInterpretations = new ArrayList<MapEntryInterpretation>();
		this.mapType = new MapType(HashMap.class);
	}
	
	public MapInterpretation(Class<?> mapType)
	{
		this.entryInterpretations = new ArrayList<MapEntryInterpretation>();
		this.mapType = new MapType(mapType);
	}

	List<MapEntryInterpretation> entryInterpretations;
	
	String fieldName;
	MapType mapType;
	
	public void setFieldName(String fieldName)
	{
		this.fieldName = fieldName;
	}
	
	public String getFieldName()
	{
		return this.fieldName;
	}
	
	public MapType getMapType()
	{
		return this.mapType;
	}
	
	public void addEntryInterpretation(SimplInterpretation key, SimplInterpretation value)
	{
		entryInterpretations.add(new MapEntryInterpretation(key,value));
	}
	
	@Override
	public void resolve(Object context, Set<String> refSet,
			UnderstandingContext understandingContext)
			throws SIMPLTranslationException {
		Object value = getValue(context, refSet, understandingContext);

		try 
		{
			ReflectionTools.setFieldValue(value, context.getClass().getField(this.fieldName), context);
		}
		catch (Exception e)
		{
			throw new SIMPLTranslationException(e);
		}
	}

	@Override
	public Object getValue(Object context, Set<String> refSet,
			UnderstandingContext understandingContext)
			throws SIMPLTranslationException 
	{		
		Map m = (Map)mapType.createInstance();
		
		for(MapEntryInterpretation entry : entryInterpretations)
		{
			MapEntryEvaluation result = (MapEntryEvaluation)entry.getValue(context, refSet, understandingContext);
			m.put(result.key, result.value);
		}
	
		return m;
	}

	@Override
	public SimplInterpretation interpret(Object context, FieldDescriptor field,
			InterpretationContext interpretationContext)
			throws SIMPLTranslationException {
		
		MapInterpretation mi = (MapInterpretation)this.interpretObject(ReflectionTools.getFieldValue(field.getField(), context), interpretationContext);
		mi.setFieldName(field.getName());
		return mi;
	}

	@Override
	public SimplInterpretation interpretObject(Object theObject,
			InterpretationContext interpretationContext) throws SIMPLTranslationException {
		// TODO Auto-generated method stub

		MapInterpretation mi = new MapInterpretation(theObject.getClass());

		Map m = (Map)theObject;
		
		for(Object ent : m.entrySet())
		{
			Map.Entry entry = (Map.Entry)ent;
			
			Object key = entry.getKey();
			Object value = entry.getValue();
			
			SimplInterpretation simplInterpKey = interpretationContext.interpretObject(key);
			SimplInterpretation simplInterpVal = interpretationContext.interpretObject(value);
			
			mi.addEntryInterpretation(simplInterpKey, simplInterpVal);
		}
		
		return mi;
	}
}
