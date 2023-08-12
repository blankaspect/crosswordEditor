/*====================================================================*\

InformationField.java

Information field class.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.ui.swing.textfield;

//----------------------------------------------------------------------


// IMPORTS


import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import java.util.Objects;

import javax.swing.JComponent;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.SwingUtilities;

import uk.blankaspect.ui.swing.font.FontKey;
import uk.blankaspect.ui.swing.font.FontUtils;

import uk.blankaspect.ui.swing.misc.GuiUtils;

import uk.blankaspect.ui.swing.modifiers.InputModifiers;

import uk.blankaspect.ui.swing.text.TextRendering;
import uk.blankaspect.ui.swing.text.TextUtils;

//----------------------------------------------------------------------


// INFORMATION FIELD CLASS


public class InformationField
	extends JComponent
	implements MouseListener
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	public enum Alignment
	{
		LEFT,
		RIGHT,
		CENTRE
	}

	private static final	int	VERTICAL_MARGIN		= 3;
	private static final	int	HORIZONTAL_MARGIN	= 5;

	private static final	Color	BACKGROUND_COLOUR	= new Color(244, 240, 216);
	private static final	Color	TEXT_COLOUR			= Color.BLACK;
	private static final	Color	BORDER_COLOUR		= new Color(212, 208, 192);

////////////////////////////////////////////////////////////////////////
//  Member classes : inner classes
////////////////////////////////////////////////////////////////////////


	// POP-UP COMPONENT CLASS


	private class PopUpComponent
		extends JComponent
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private PopUpComponent()
		{
			// Set font
			setFont(InformationField.this.getFont());

			// Set preferred size
			int width = 2 * HORIZONTAL_MARGIN + getFontMetrics(getFont()).stringWidth(text);
			setPreferredSize(new Dimension(width, InformationField.this.getHeight()));

			// Set component attributes
			setOpaque(true);
			setFocusable(false);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		protected void paintComponent(Graphics gr)
		{
			// Create copy of graphics context
			gr = gr.create();

			// Get dimensions
			int width = getWidth();
			int height = getHeight();

			// Fill interior
			gr.setColor(BACKGROUND_COLOUR);
			gr.fillRect(0, 0, width, height);

			// Set rendering hints for text antialiasing and fractional metrics
			TextRendering.setHints((Graphics2D)gr);

			// Draw text
			gr.setColor(TEXT_COLOUR);
			gr.drawString(text, HORIZONTAL_MARGIN, VERTICAL_MARGIN + gr.getFontMetrics().getAscent());

			// Draw border
			gr.setColor(BORDER_COLOUR);
			gr.drawRect(0, 0, width - 1, height - 1);
		}

		//--------------------------------------------------------------

	}

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	public InformationField(int numColumns)
	{
		this("0".repeat(numColumns));
	}

	//------------------------------------------------------------------

	public InformationField(int    numColumns,
							String text)
	{
		this(numColumns);
		setText(text);
	}

	//------------------------------------------------------------------

	public InformationField(String prototypeText)
	{
		// Initialise instance variables
		horizontalAlignment = Alignment.LEFT;

		// Set font
		FontUtils.setAppFont(FontKey.MAIN, this);

		// Set preferred size
		FontMetrics fontMetrics = getFontMetrics(getFont());
		int width = 2 * HORIZONTAL_MARGIN + fontMetrics.stringWidth(prototypeText);
		int height = 2 * VERTICAL_MARGIN + fontMetrics.getAscent() + fontMetrics.getDescent();
		setPreferredSize(new Dimension(width, height));

		// Set component attributes
		setOpaque(true);
		setFocusable(false);
		setBackground(BACKGROUND_COLOUR);
		setForeground(TEXT_COLOUR);

		// Add listeners
		addMouseListener(this);
	}

	//------------------------------------------------------------------

	public InformationField(String prototypeText,
							String text)
	{
		this(prototypeText);
		setText(text);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : MouseListener interface
////////////////////////////////////////////////////////////////////////

	public void mouseClicked(MouseEvent event)
	{
		// do nothing
	}

	//------------------------------------------------------------------

	public void mouseEntered(MouseEvent event)
	{
		// do nothing
	}

	//------------------------------------------------------------------

	public void mouseExited(MouseEvent event)
	{
		// do nothing
	}

	//------------------------------------------------------------------

	public void mousePressed(MouseEvent event)
	{
		if (SwingUtilities.isLeftMouseButton(event) && InputModifiers.forEvent(event).isControl())
			showPopUp(event);
	}

	//------------------------------------------------------------------

	public void mouseReleased(MouseEvent event)
	{
		if (SwingUtilities.isLeftMouseButton(event))
			hidePopUp();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : overriding methods
////////////////////////////////////////////////////////////////////////

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

			// Get x coordinate
			FontMetrics fontMetrics = gr.getFontMetrics();
			int x = 0;
			switch (horizontalAlignment)
			{
				case LEFT:
					x = HORIZONTAL_MARGIN;
					break;

				case RIGHT:
					x = width - HORIZONTAL_MARGIN - fontMetrics.stringWidth(text);
					break;

				case CENTRE:
					x = (width - fontMetrics.stringWidth(text)) / 2;
					break;
			}

			// Draw text
			int maxWidth = width - 2 * HORIZONTAL_MARGIN;
			String str = TextUtils.getLimitedWidthString(text, fontMetrics, maxWidth, TextUtils.RemovalMode.END);
			gr.setColor(getForeground());
			gr.drawString(str, x, VERTICAL_MARGIN + fontMetrics.getAscent());
		}

		// Draw border
		gr.setColor(getBorderColour());
		gr.drawRect(0, 0, width - 1, height - 1);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	public String getText()
	{
		return ((text == null) ? "" : text);
	}

	//------------------------------------------------------------------

	public void setHorizontalAlignment(Alignment alignment)
	{
		if (horizontalAlignment != alignment)
		{
			horizontalAlignment = alignment;
			repaint();
		}
	}

	//------------------------------------------------------------------

	public void setText(String text)
	{
		if (!Objects.equals(text, this.text))
		{
			this.text = text.isEmpty() ? null : text;
			repaint();
		}
	}

	//------------------------------------------------------------------

	protected Color getBorderColour()
	{
		return BORDER_COLOUR;
	}

	//------------------------------------------------------------------

	private void showPopUp(MouseEvent event)
	{
		if ((text != null) && (getFontMetrics(getFont()).stringWidth(text) > getWidth() - 2 * HORIZONTAL_MARGIN))
		{
			PopUpComponent popUpComponent = new PopUpComponent();
			int popUpWidth = popUpComponent.getPreferredSize().width;
			Point location = getLocationOnScreen();
			Rectangle screen = GuiUtils.getVirtualScreenBounds(this);
			int x = Math.min(location.x, screen.x + screen.width - popUpWidth);
			popUp = PopupFactory.getSharedInstance().getPopup(this, popUpComponent, x, location.y);
			popUp.show();
		}
	}

	//------------------------------------------------------------------

	private void hidePopUp()
	{
		if (popUp != null)
		{
			popUp.hide();
			popUp = null;
		}
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private	Alignment	horizontalAlignment;
	private	String		text;
	private	Popup		popUp;

}

//----------------------------------------------------------------------
