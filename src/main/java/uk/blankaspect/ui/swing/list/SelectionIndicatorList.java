/*====================================================================*\

SelectionIndicatorList.java

Selection indicator list class.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.ui.swing.list;

//----------------------------------------------------------------------


// IMPORTS


import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;

import java.awt.image.BufferedImage;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;

import uk.blankaspect.ui.swing.colour.Colours;

import uk.blankaspect.ui.swing.font.FontKey;
import uk.blankaspect.ui.swing.font.FontUtils;

import uk.blankaspect.ui.swing.misc.GuiUtils;

import uk.blankaspect.ui.swing.text.TextRendering;
import uk.blankaspect.ui.swing.text.TextUtils;

//----------------------------------------------------------------------


// SELECTION INDICATOR LIST CLASS


public class SelectionIndicatorList<E>
	extends JComponent
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	private static final	int	BORDER_TOP		= 1;
	private static final	int	BORDER_BOTTOM	= BORDER_TOP;
	private static final	int	BORDER_LEFT		= 1;
	private static final	int	BORDER_RIGHT	= BORDER_LEFT;

	private static final	int	MARKER_LEADING_MARGIN	= 5;
	private static final	int	MARKER_WIDTH			= 5;
	private static final	int	MARKER_HEIGHT			= MARKER_WIDTH;

	private static final	int	TEXT_TOP_MARGIN			= 1;
	private static final	int	TEXT_BOTTOM_MARGIN		= TEXT_TOP_MARGIN;
	private static final	int	TEXT_LEADING_MARGIN		= 5;
	private static final	int	TEXT_TRAILING_MARGIN	= TEXT_LEADING_MARGIN;

	private static final	Color	BORDER_COLOUR	= Color.DARK_GRAY;
	private static final	Color	MARKER_COLOUR	= Color.BLACK;

	private static final	BufferedImage	MARKER_IMAGE;

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	public SelectionIndicatorList(List<E> items,
								  E       currentItem)
	{
		this(items, currentItem, 0, 0);
	}

	//------------------------------------------------------------------

	/**
	 * @throws IllegalArgumentException
	 */

	public SelectionIndicatorList(List<E> items,
								  E       currentItem,
								  int     maxWidth,
								  int     maxHeight)
	{
		this(items, (currentItem == null) ? -1 : items.indexOf(currentItem), maxWidth, maxHeight);
	}

	//------------------------------------------------------------------

	public SelectionIndicatorList(List<E> items,
								  int     currentIndex)
	{
		this(items, currentIndex, 0, 0);
	}

	//------------------------------------------------------------------

	/**
	 * @throws IllegalArgumentException
	 */

	public SelectionIndicatorList(List<E> items,
								  int     currentIndex,
								  int     maxWidth,
								  int     maxHeight)
	{
		// Validate arguments
		if ((items == null) || (maxWidth < 0) || (maxHeight < 0))
			throw new IllegalArgumentException();

		// Substitute screen dimension for zero maximum dimension
		if ((maxWidth == 0) || (maxHeight == 0))
		{
			Rectangle screenRect = GuiUtils.getVirtualScreenBounds();
			if (maxWidth == 0)
				maxWidth = screenRect.width;
			if (maxHeight == 0)
				maxHeight = screenRect.height;
		}

		// Initialise instance variables
		this.items = new ArrayList<>();
		selectedIndex = -1;

		FontUtils.setAppFont(FontKey.MAIN, this);
		FontMetrics fontMetrics = getFontMetrics(getFont());
		rowHeight = TEXT_TOP_MARGIN + fontMetrics.getAscent() + fontMetrics.getDescent() +
																						TEXT_BOTTOM_MARGIN;
		int numRows = Math.min((maxHeight - BORDER_TOP - BORDER_BOTTOM) / rowHeight, items.size());
		this.currentIndex = ((currentIndex < 0) || (currentIndex >= numRows)) ? -1 : currentIndex;
		maxTextWidth = maxWidth - BORDER_LEFT - TEXT_LEADING_MARGIN - TEXT_TRAILING_MARGIN - BORDER_RIGHT;
		if (this.currentIndex >= 0)
			maxTextWidth -= MARKER_LEADING_MARGIN + MARKER_WIDTH;
		for (int i = 0; i < numRows; i++)
		{
			E item = items.get(i);
			this.items.add(item);
			String str = TextUtils.getLimitedWidthString(item.toString(), fontMetrics, maxTextWidth,
														 TextUtils.RemovalMode.END);
			int strWidth = fontMetrics.stringWidth(str);
			if (columnWidth < strWidth)
				columnWidth = strWidth;
		}
		columnWidth += TEXT_LEADING_MARGIN + TEXT_TRAILING_MARGIN;
		if (this.currentIndex >= 0)
			columnWidth += MARKER_LEADING_MARGIN + MARKER_WIDTH;

		// Set attributes
		setOpaque(true);
		setFocusable(false);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : overriding methods
////////////////////////////////////////////////////////////////////////

	@Override
	public Dimension getPreferredSize()
	{
		return new Dimension(BORDER_LEFT + columnWidth + BORDER_RIGHT,
							 BORDER_TOP + items.size() * rowHeight + BORDER_BOTTOM);
	}

	//------------------------------------------------------------------

	@Override
	protected void paintComponent(Graphics gr)
	{
		// Create copy of graphics context
		gr = gr.create();

		// Fill background
		Rectangle rect = gr.getClipBounds();
		gr.setColor(Colours.List.BACKGROUND.getColour());
		gr.fillRect(rect.x, rect.y, rect.width, rect.height);

		// Get start and end indices of rows
		int startIndex = Math.max(0, rect.y / rowHeight);
		int endIndex = Math.min(Math.max(0, (rect.y + rect.height + rowHeight - 1) / rowHeight),
								items.size() - 1);

		// Draw selection background
		if ((selectedIndex >= startIndex) && (selectedIndex <= endIndex))
		{
			gr.setColor(Colours.List.FOCUSED_SELECTION_BACKGROUND.getColour());
			gr.fillRect(rect.x, BORDER_TOP + selectedIndex * rowHeight, rect.width, rowHeight);
		}

		// Draw icon for current item
		int width = getWidth();
		if (currentIndex >= 0)
		{
			int x = BORDER_LEFT + MARKER_LEADING_MARGIN;
			int y = BORDER_TOP + currentIndex * rowHeight + TEXT_TOP_MARGIN + (rowHeight - MARKER_HEIGHT + 1) / 2;
			gr.drawImage(MARKER_IMAGE, x, y, null);
		}

		// Set rendering hints for text antialiasing and fractional metrics
		TextRendering.setHints((Graphics2D)gr);

		// Draw text
		FontMetrics fontMetrics = gr.getFontMetrics();
		int y = BORDER_TOP + startIndex * rowHeight + TEXT_TOP_MARGIN + fontMetrics.getAscent();
		for (int i = startIndex; i <= endIndex; i++)
		{
			// Get limited-width text
			String str = TextUtils.getLimitedWidthString(items.get(i).toString(), fontMetrics, maxTextWidth,
														 TextUtils.RemovalMode.END);

			// Get x coordinate
			int x = BORDER_LEFT + TEXT_LEADING_MARGIN;
			if (currentIndex >= 0)
				x += MARKER_LEADING_MARGIN + MARKER_WIDTH;

			// Draw text
			gr.setColor((i == selectedIndex) ? Colours.List.FOCUSED_SELECTION_FOREGROUND.getColour()
											 : Colours.List.FOREGROUND.getColour());
			gr.drawString(str, x, y);
			y += rowHeight;
		}

		// Draw border
		gr.setColor(BORDER_COLOUR);
		gr.drawRect(0, 0, width - 1, getHeight() - 1);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	public int getNumItems()
	{
		return items.size();
	}

	//------------------------------------------------------------------

	public int getSelectedIndex()
	{
		return selectedIndex;
	}

	//------------------------------------------------------------------

	public E getSelectedItem()
	{
		return ((selectedIndex < 0) ? null : items.get(selectedIndex));
	}

	//------------------------------------------------------------------

	public void setSelectedIndex(int index)
	{
		if (selectedIndex != index)
		{
			selectedIndex = index;
			repaint();
		}
	}

	//------------------------------------------------------------------

	public void setSelectedItem(E item)
	{
		setSelectedIndex(items.indexOf(item));
	}

	//------------------------------------------------------------------

	public int pointToIndex(Point point)
	{
		int index = -1;
		if ((point.x >= BORDER_LEFT) && (point.x < getWidth() - BORDER_RIGHT)
				&& (point.y >= BORDER_TOP) && (point.y < getHeight() - BORDER_BOTTOM))
		{
			index = (point.y - BORDER_TOP) / rowHeight;
			if (index >= items.size())
				index = -1;
		}
		return index;
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Static initialiser
////////////////////////////////////////////////////////////////////////

	static
	{
		MARKER_IMAGE = new BufferedImage(MARKER_WIDTH, MARKER_HEIGHT, BufferedImage.TYPE_INT_ARGB);
		Graphics2D gr = MARKER_IMAGE.createGraphics();
		gr.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		gr.setColor(MARKER_COLOUR);
		gr.fillOval(0, 0, MARKER_WIDTH, MARKER_HEIGHT);
	}

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private	int		columnWidth;
	private	int		rowHeight;
	private	int		maxTextWidth;
	private	List<E>	items;
	private	int		currentIndex;
	private	int		selectedIndex;

}

//----------------------------------------------------------------------
