/*
 * Copyright 1996-2002 by Andruid Kerne. All rights reserved.
 * CONFIDENTIAL. Use is subject to license terms.
 */
package ecologylab.generic;

import java.awt.Rectangle;

/**
 * Representation of a bounding box, based on 2 points.
 */
public class Bounds
{
   public int xMin, yMin, xMax, yMax;

   public Bounds(int xMinArg, int yMinArg, int xMaxArg, int yMaxArg)
   {
      xMin	= xMinArg;
      yMin	= yMinArg;
      xMax	= xMaxArg;
      yMax	= yMaxArg;
   }
   public Bounds(Bounds oldBounds)
   {
      xMin	= oldBounds.xMin;
      yMin	= oldBounds.yMin;
      xMax	= oldBounds.xMax;
      yMax	= oldBounds.yMax;
   }
   public Bounds()
	{
		// TODO Auto-generated constructor stub
	}
	@Override
	public boolean equals(Object other)
   {
      Bounds fb = (Bounds)other;
      
      return ((fb != null) && (fb.xMin == this.xMin) && (fb.xMax == this.xMax)
	      && (fb.yMin == this.yMin) && (fb.yMax == this.yMax));
   }
    @Override
	public String toString()
   {
      return "Bounds[" + xMin+","+yMin+"; "+xMax+","+yMax +"]";
   }
    
    public boolean intersects(Rectangle intersectingRect)
    {
    	Rectangle boundsRect = new Rectangle(xMin, yMin, xMax - xMin, yMax - yMin);
    	
    	return boundsRect.intersects(intersectingRect);
    }
}
