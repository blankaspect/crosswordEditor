/*====================================================================*\

Workarounds01.java

Class: workarounds for bugs that have been observed in Swing/AWT.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.ui.swing.workaround;

//----------------------------------------------------------------------


// IMPORTS


import java.awt.Graphics;

//----------------------------------------------------------------------


// CLASS: WORKAROUNDS FOR BUGS THAT HAVE BEEN OBSERVED IN SWING/AWT


public class Workarounds01
{

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	/**
	 * Prevents this class from being instantiated externally.
	 */

	private Workarounds01()
	{
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	/**
	 * Draws a rectangle with the specified dimensions and stroke width at the specified coordinates.  The rectangle is
	 * drawn by filling its four sides with rectangles whose appropriate dimension (width for the left and right sides,
	 * height for the top and bottom sides) corresponds to the specified stroke width.
	 *
	 * This method provides a workaround for a bug whereby AWT/Swing doesn't scale the stroke width for a high-DPI
	 * display with a scale factor of 2.  It may be used to replace {@link Graphics#drawRect(int, int, int, int)} while
	 * the bug persists.
	 *
	 * @param gr
	 *          the graphics context in which the rectangle will be drawn.
	 * @param x
	 *          the <i>x</i> coordinate of the top left corner of the rectangle.
	 * @param y
	 *          the <i>y</i> coordinate of the top left corner of the rectangle.
	 * @param width
	 *          the width of the rectangle.
	 * @param height
	 *          the height of the rectangle.
	 * @param strokeWidth
	 *          the stroke width.
	 * @see   Graphics#drawRect(int, int, int, int)
	 */

	public static void drawRect(
		Graphics	gr,
		int			x,
		int			y,
		int			width,
		int			height,
		int			strokeWidth)
	{
		gr.fillRect(x, y, width, strokeWidth);
		gr.fillRect(x, y, strokeWidth, height);
		gr.fillRect(x + width - strokeWidth, y, strokeWidth, height);
		gr.fillRect(x, y + height - strokeWidth, width, strokeWidth);
	}

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------
