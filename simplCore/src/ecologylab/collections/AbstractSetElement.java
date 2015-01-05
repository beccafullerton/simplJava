package ecologylab.collections;

public interface AbstractSetElement
{
	public void delete();
	
	public void addSet(WeightSet s);
	public void removeSet(WeightSet s);
	public boolean isInSet();
	
	public void deleteHook();
	public void insertHook();
	
	public void recycle(boolean allContents);
	public boolean recycled();
}
