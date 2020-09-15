/*====================================================================*\

AbstractStatusPanel.java

Abstract status panel class.

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

import javax.swing.JComponent;
import javax.swing.JPanel;

import uk.blankaspect.common.string.StringUtils;

import uk.blankaspect.common.swing.colour.Colours;

import uk.blankaspect.common.swing.text.TextRendering;
import uk.blankaspect.common.swing.text.TextUtils;

//----------------------------------------------------------------------


// ABSTRACT STATUS PANEL CLASS


abstract class AbstractStatusPanel
	extends JPanel
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	public static final		Color	DEFAULT_TEXT_COLOUR	= new Color(160, 64, 0);

	private static final	int	BORDER_WIDTH	= 1;
	private static final	int	VERTICAL_MARGIN	= 1;

////////////////////////////////////////////////////////////////////////
//  Member classes : non-inner classes
////////////////////////////////////////////////////////////////////////


	// STATUS FIELD CLASS


	protected static class StatusField
		extends JComponent
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		private static final	int	VERTICAL_MARGIN		= 1;
		private static final	int	HORIZONTAL_MARGIN	= 6;
		private static final	int	SEPARATOR_WIDTH		= 1;

		private static final	Color	LINE_COLOUR	= Color.GRAY;

		private static final	String	PROTOTYPE_STR	= StringUtils.createCharString(' ', 4);

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		protected StatusField()
		{
			// Set font
			AppFont.MAIN.apply(this);

			// Initialise instance variables
			FontMetrics fontMetrics = getFontMetrics(getFont());
			preferredWidth = 2 * HORIZONTAL_MARGIN + SEPARATOR_WIDTH +
																fontMetrics.stringWidth(PROTOTYPE_STR);
			preferredHeight = 2 * VERTICAL_MARGIN + fontMetrics.getAscent() + fontMetrics.getDescent();

			// Set component attributes
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
		protected void paintComponent(Graphics gr)
		{
			// Create copy of graphics context
			gr = gr.create();

			// Get dimensions
			int width = getWidth();
			int height = getHeight();

			// Draw background
			gr.setColor(getBackground());
			gr.fillRect(0, 0, width, height);

			// Draw text
			if (text != null)
			{
				// Set rendering hints for text antialiasing and fractional metrics
				TextRendering.setHints((Graphics2D)gr);

				// Draw text
				FontMetrics fontMetrics = gr.getFontMetrics();
				int maxWidth = width - 2 * HORIZONTAL_MARGIN - SEPARATOR_WIDTH;
				String str = TextUtils.getLimitedWidthString(text, fontMetrics, maxWidth, TextUtils.RemovalMode.END);
				gr.setColor(AppConfig.INSTANCE.getStatusTextColour());
				gr.drawString(str, HORIZONTAL_MARGIN, VERTICAL_MARGIN + fontMetrics.getAscent());
			}

			// Draw separator
			int x = width - SEPARATOR_WIDTH;
			gr.setColor(LINE_COLOUR);
			gr.drawLine(x, 0, x, height - 1);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		protected void setText(String text)
		{
			if (!StringUtils.equal(text, this.text))
			{
				this.text = text;
				int textWidth = getFontMetrics(getFont()).
													stringWidth((text == null) ? PROTOTYPE_STR : text);
				preferredWidth = 2 * HORIZONTAL_MARGIN + SEPARATOR_WIDTH + textWidth;
				revalidate();
				repaint();
			}
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	int		preferredWidth;
		private	int		preferredHeight;
		private	String	text;

	}

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	protected AbstractStatusPanel(boolean topBorder)
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
	protected void paintComponent(Graphics gr)
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
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private	boolean	topBorder;

}

//----------------------------------------------------------------------
