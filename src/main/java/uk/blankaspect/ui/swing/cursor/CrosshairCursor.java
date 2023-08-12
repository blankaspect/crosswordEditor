/*====================================================================*\

CrosshairCursor.java

Crosshair cursor class.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.ui.swing.cursor;

//----------------------------------------------------------------------


// IMPORTS


import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;

import java.awt.image.BufferedImage;

import java.util.HashMap;
import java.util.Map;

//----------------------------------------------------------------------


// CROSSHAIR CURSOR CLASS


public class CrosshairCursor
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	private static final	int	MIN_SIZE		= 7;
	private static final	int	MAX_SIZE		= 63;
	private static final	int	SIZE_INCREMENT	= 8;

	private static final	String	NAME	= "Crosshair cursor ";

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	private CrosshairCursor()
	{
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	/**
	 * @throws IllegalArgumentException
	 */

	public static Cursor getCursor(int size)
	{
		// Validate argument
		if ((size < MIN_SIZE) || (size > MAX_SIZE) || (size % 2 == 0))
			throw new IllegalArgumentException();

		// Get cursor from cache
		Cursor cursor = cursors.get(size);

		// If the cache doesn't contain a cursor of the required size, create a new cursor
		if (cursor == null)
		{
			// Get available cursor size, preferably greater than required size
			Toolkit toolkit = Toolkit.getDefaultToolkit();
			int preferredSize = size;
			int availableSize = -1;
			Dimension cursorSize = new Dimension();
			while ((availableSize < size) && (availableSize != cursorSize.width))
			{
				availableSize = cursorSize.width;
				cursorSize = toolkit.getBestCursorSize(preferredSize, preferredSize);
				preferredSize += SIZE_INCREMENT;
			}

			// Create image for cursor
			BufferedImage image = new BufferedImage(cursorSize.width, cursorSize.height,
													BufferedImage.TYPE_INT_ARGB);
			int cMid = size / 2;
			int c0 = 0;
			int c1 = Math.min(cMid - 2, cursorSize.width);
			int c2 = Math.min(cMid + 2, cursorSize.width);
			int c3 = Math.min(size - 1, cursorSize.width);
			for (int x = c0; x <= c1; x++)
				image.setRGB(x, cMid, 0xFF000000);
			for (int x = c2; x <= c3; x++)
				image.setRGB(x, cMid, 0xFF000000);
			for (int y = c0; y <= c1; y++)
				image.setRGB(cMid, y, 0xFF000000);
			for (int y = c2; y <= c3; y++)
				image.setRGB(cMid, y, 0xFF000000);

			// Create custom cursor
			cursor = toolkit.createCustomCursor(image, new Point(cMid, cMid), NAME + size);

			// Add cursor to cache
			cursors.put(size, cursor);
		}

		// Return cursor
		return cursor;
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class variables
////////////////////////////////////////////////////////////////////////

	private static	Map<Integer, Cursor>	cursors	= new HashMap<>();

}

//----------------------------------------------------------------------
