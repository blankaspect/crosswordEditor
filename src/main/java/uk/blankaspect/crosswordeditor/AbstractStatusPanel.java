/*====================================================================*\

AbstractStatusPanel.java

Class: abstract status panel.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.crosswordeditor;

//----------------------------------------------------------------------


// IMPORTS


import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;

import java.util.Objects;

import javax.swing.JComponent;
import javax.swing.JPanel;

import uk.blankaspect.ui.swing.colour.Colours;

import uk.blankaspect.ui.swing.misc.GuiUtils;

import uk.blankaspect.ui.swing.text.TextRendering;
import uk.blankaspect.ui.swing.text.TextUtils;

//----------------------------------------------------------------------


// CLASS: ABSTRACT STATUS PANEL


abstract class AbstractStatusPanel
	extends JPanel
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	public static final		Color	DEFAULT_TEXT_COLOUR	= new Color(160, 64, 0);

	private static final	int		BORDER_WIDTH	= 1;
	private static final	int		VERTICAL_MARGIN	= 1;

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private	boolean	topBorder;

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	protected AbstractStatusPanel(
		boolean	topBorder)
	{
		// Initialise instance variables
		this.topBorder = topBorder;

		// Lay out components explicitly
		setLayout(null);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : overriding methods
////////////////////////////////////////////////////////////////////////

	@Override
	public Dimension getMinimumSize()
	{
		return getPreferredSize();
	}

	//------------------------------------------------------------------

	@Override
	public Dimension getPreferredSize()
	{
		int width = 0;
		int height = 0;
		for (Component component : getComponents())
		{
			Dimension size = component.getPreferredSize();
			width += size.width;
			if (height < size.height)
				height = size.height;
		}
		if (width == 0)
			++width;
		height += 2 * VERTICAL_MARGIN;
		if (topBorder)
			height += BORDER_WIDTH;
		return new Dimension(width, height);
	}

	//------------------------------------------------------------------

	@Override
	public void doLayout()
	{
		// Get maximum height of components
		int maxHeight = 0;
		for (Component component : getComponents())
		{
			int height = component.getPreferredSize().height;
			if (maxHeight < height)
				maxHeight = height;
		}

		// Set location and size of components
		int x = 0;
		int y = VERTICAL_MARGIN;
		if (topBorder)
			y += BORDER_WIDTH;
		for (Component component : getComponents())
		{
			Dimension size = component.getPreferredSize();
			component.setBounds(x, y, size.width, maxHeight);
			x += size.width;
		}
	}

	//------------------------------------------------------------------

	@Override
	protected void paintComponent(
		Graphics	gr)
	{
		// Call superclass method
		super.paintComponent(gr);

		// Draw border on top edge
		if (topBorder)
		{
			gr.setColor(Colours.LINE_BORDER);
			gr.drawLine(0, 0, getWidth() - 1, 0);
		}
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Member classes : non-inner classes
////////////////////////////////////////////////////////////////////////


	// CLASS: STATUS FIELD


	protected static class StatusField
		extends JComponent
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		private static final	int		VERTICAL_MARGIN		= 1;
		private static final	int		HORIZONTAL_MARGIN	= 6;
		private static final	int		SEPARATOR_WIDTH		= 1;

		private static final	Color	LINE_COLOUR	= Color.GRAY;

		private static final	String	PROTOTYPE_TEXT	= " ".repeat(4);

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	int		preferredWidth;
		private	int		preferredHeight;
		private	String	text;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		protected StatusField()
		{
			// Set font
			AppFont.MAIN.apply(this);

			// Initialise instance variables
			FontMetrics fontMetrics = getFontMetrics(getFont());
			preferredWidth = 2 * HORIZONTAL_MARGIN + SEPARATOR_WIDTH + fontMetrics.stringWidth(PROTOTYPE_TEXT);
			preferredHeight = 2 * VERTICAL_MARGIN + fontMetrics.getAscent() + fontMetrics.getDescent();

			// Set properties
			setOpaque(true);
			setFocusable(false);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		public Dimension getPreferredSize()
		{
			return new Dimension(preferredWidth, preferredHeight);
		}

		//--------------------------------------------------------------

		@Override
		protected void paintComponent(
			Graphics	gr)
		{
			// Create copy of graphics context
			Graphics2D gr2d = GuiUtils.copyGraphicsContext(gr);

			// Get dimensions
			int width = getWidth();
			int height = getHeight();

			// Draw background
			gr2d.setColor(getBackground());
			gr2d.fillRect(0, 0, width, height);

			// Draw text
			if (text != null)
			{
				// Set rendering hints for text antialiasing and fractional metrics
				TextRendering.setHints(gr2d);

				// Draw text
				FontMetrics fontMetrics = gr2d.getFontMetrics();
				int maxWidth = width - 2 * HORIZONTAL_MARGIN - SEPARATOR_WIDTH;
				String str = TextUtils.getLimitedWidthString(text, fontMetrics, maxWidth, TextUtils.RemovalMode.END);
				gr2d.setColor(AppConfig.INSTANCE.getStatusTextColour());
				gr2d.drawString(str, HORIZONTAL_MARGIN, VERTICAL_MARGIN + fontMetrics.getAscent());
			}

			// Draw separator
			int x = width - SEPARATOR_WIDTH;
			gr2d.setColor(LINE_COLOUR);
			gr2d.drawLine(x, 0, x, height - 1);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		protected void setText(
			String	text)
		{
			if (!Objects.equals(text, this.text))
			{
				this.text = text;
				int textWidth = getFontMetrics(getFont()).stringWidth((text == null) ? PROTOTYPE_TEXT : text);
				preferredWidth = 2 * HORIZONTAL_MARGIN + SEPARATOR_WIDTH + textWidth;
				revalidate();
				repaint();
			}
		}

		//--------------------------------------------------------------

	}

	//==================================================================

}

//----------------------------------------------------------------------
