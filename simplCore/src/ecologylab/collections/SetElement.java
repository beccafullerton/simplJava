/*
 * Copyright 1996-2002 by Andruid Kerne. All rights reserved. CONFIDENTIAL. Use is subject to
 * license terms.
 */
package ecologylab.collections;

import java.util.ArrayList;

import ecologylab.generic.Debug;

/**
 * A FloatWeightSet element.
 * 
 * Basic implementation of SetElement for cases when you can inherit from here
 **/
public class SetElement extends Debug implements AbstractSetElement
{

	private boolean									recycled	= false;

	protected ArrayList<WeightSet>	sets			= null; // new ArrayList<WeightSet>();

	public SetElement()
	{
	}

	/**
	 * This can be overridden to exclude elements from selection while keeping them in the set.
	 * 
	 * @return The default implementation always returns false, including all elements for selection.
	 */
	public boolean filteredOut()
	{
		return false;
	}

	/**
	 * Delete in the most expedient manner possible. This is final because you should override
	 * deleteHook() to provide custom behavior.
	 */
	@Override
	public final void delete()
	{
		if (!recycled && sets != null)
		{
			synchronized (sets)
			{
				for (WeightSet s : sets)
					if (s != null)
						s.removeFromSet(this);
				sets.clear();
			}
		}
		deleteHook();
	}

	@Override
	public void addSet(WeightSet s)
	{
		if (sets == null && !recycled)
			sets			= new ArrayList<WeightSet>();
		
		synchronized (sets)
		{
			sets.add(s);
		}
	}

	@Override
	public void removeSet(WeightSet s)
	{
		if (sets != null)
		{
			synchronized (sets)
			{
				sets.remove(s);
			}
		}
	}

	/**
	 * Delete the element from the set. This means changing the set's structure, and also changing
	 * slots in this to reflect its lack of membership.
	 * 
	 * @param recompute
	 *          -1 for absolutely no recomputation of the set's internal structures. 0 for recompute
	 *          upwards from el 1 for recompute all
	 * 
	 * @return true if the element was a member of a set, and thus, if the delete does something;
	 *         false if the element was not a member of a set, and thus, if the delete does *nothing*.
	 */
	// public final synchronized boolean delete(int recompute)
	// {
	// boolean inSet = isInSet();
	// if (inSet)//prevent double dip deletes
	// {
	// set.delete(this, recompute);
	// clear();
	// }
	// return inSet;
	// }

	/**
	 * Callback that happens at the end of a delete, and when an element gets pruned. This
	 * implementation is empty. Override to provide custom behaviors.
	 */
	@Override
	public void deleteHook()
	{

	}

	/**
	 * A callback method for when an element is inserted into a floatWeightSet.
	 * 
	 * This implementation is empty. Override to provide custom behaviors.
	 */
	@Override
	public void insertHook()
	{

	}

	@Override
	public String toString()
	{
		return super.toString();
	}

	/**
	 * Free resources associated w this element. Deletes from all constituent sets. Sets recycled to
	 * true.
	 */
	@Override
	public void recycle(boolean unconditional)
	{
		if (!recycled)
		{
			recycled = true;
			delete();
		}
	}

	@Override
	public boolean recycled()
	{
		return recycled;
	}

	@Override
	public boolean isInSet()
	{
		return (sets != null) && sets.size() > 0;
	}

}
