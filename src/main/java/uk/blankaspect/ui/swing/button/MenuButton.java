/*====================================================================*\

MenuButton.java

Menu button class.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.ui.swing.button;

//----------------------------------------------------------------------


// IMPORTS


import java.awt.Point;
import java.awt.Rectangle;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingConstants;

import uk.blankaspect.ui.swing.font.FontKey;
import uk.blankaspect.ui.swing.font.FontUtils;

import uk.blankaspect.ui.swing.misc.GuiUtils;

//----------------------------------------------------------------------


// MENU BUTTON CLASS


public class MenuButton
	extends JButton
	implements ActionListener, ComponentListener
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	private static final	int	MIN_ICON_TEXT_GAP	= 16;

	private static final	ImageIcon	ARROWHEAD_RIGHT	= new ImageIcon(ImgData.ARROWHEAD_RIGHT);

	private interface Command
	{
		String	SHOW_MENU	= "showMenu";
	}

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private	int			textWidth;
	private	JPopupMenu	menu;

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	public MenuButton(String text)
	{
		// Call superclass constructor
		super(text, ARROWHEAD_RIGHT);

		// Set font
		FontUtils.setAppFont(FontKey.MAIN, this);

		// Initialise instance variables
		textWidth = getFontMetrics(getFont()).stringWidth(text);
		menu = new JPopupMenu();

		// Set properties
		setHorizontalTextPosition(SwingConstants.LEADING);
		setHorizontalAlignment(SwingConstants.TRAILING);
		setIconTextGap(MIN_ICON_TEXT_GAP);
		setActionCommand(Command.SHOW_MENU);

		// Add listeners
		addActionListener(this);
		addComponentListener(this);
	}

	//------------------------------------------------------------------

	public MenuButton(String          text,
					  List<JMenuItem> menuItems)
	{
		// Call primary constructor
		this(text);

		// Add menu items
		for (JMenuItem menuItem : menuItems)
			addMenuItem(menuItem);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : ActionListener interface
////////////////////////////////////////////////////////////////////////

	public void actionPerformed(ActionEvent event)
	{
		if (event.getActionCommand().equals(Command.SHOW_MENU))
			onShowMenu();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : ComponentListener interface
////////////////////////////////////////////////////////////////////////

	public void componentHidden(ComponentEvent event)
	{
		// do nothing
	}

	//------------------------------------------------------------------

	public void componentMoved(ComponentEvent event)
	{
		// do nothing
	}

	//------------------------------------------------------------------

	public void componentResized(ComponentEvent event)
	{
		// Update icon-text gap
		int gap = (getWidth() - textWidth) / 2 - getIcon().getIconWidth() -
																getBorder().getBorderInsets(this).right;
		setIconTextGap(Math.max(MIN_ICON_TEXT_GAP, gap));
	}

	//------------------------------------------------------------------

	public void componentShown(ComponentEvent event)
	{
		// do nothing
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	public JPopupMenu getMenu()
	{
		return menu;
	}

	//------------------------------------------------------------------

	public JMenuItem addMenuItem(JMenuItem menuItem)
	{
		return menu.add(menuItem);
	}

	//------------------------------------------------------------------

	public void addMenuSeparator()
	{
		menu.addSeparator();
	}

	//------------------------------------------------------------------

	public void clearMenuItems()
	{
		menu.removeAll();
	}

	//------------------------------------------------------------------

	private void onShowMenu()
	{
		// Get the absolute location of the button and the bounds of its screen
		Point baseLocation = getLocationOnScreen();
		Rectangle screenRect = GuiUtils.getComponentScreenBounds(this).bounds();

		// Make the menu visible to initialise its dimensions
		menu.setInvoker(this);
		menu.setVisible(true);

		// Get the preferred location of the menu
		int x1 = baseLocation.x + getWidth() - 1;
		int x2 = x1 + menu.getWidth();
		int screenX2 = screenRect.x + screenRect.width;
		if (x2 > screenX2)
		{
			x1 = baseLocation.x - (menu.getWidth() - 1);
			if (x1 < screenRect.x)
				x1 = screenX2 - menu.getWidth();
		}
		Point location = new Point(x1, baseLocation.y);

		// Adjust the location of the menu so that it is within the screen
		location = GuiUtils.getComponentLocation(menu, location);

		// Set the location of the menu
		menu.setLocation(location);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Image data
////////////////////////////////////////////////////////////////////////

	/**
	 * PNG image data.
	 */

	private interface ImgData
	{
		byte[]	ARROWHEAD_RIGHT	=
		{
			(byte)0x89, (byte)0x50, (byte)0x4E, (byte)0x47, (byte)0x0D, (byte)0x0A, (byte)0x1A, (byte)0x0A,
			(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x0D, (byte)0x49, (byte)0x48, (byte)0x44, (byte)0x52,
			(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x04, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x08,
			(byte)0x08, (byte)0x06, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0xDE, (byte)0x33, (byte)0x5E,
			(byte)0x05, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x27, (byte)0x49, (byte)0x44, (byte)0x41,
			(byte)0x54, (byte)0x78, (byte)0xDA, (byte)0x63, (byte)0x60, (byte)0x60, (byte)0x60, (byte)0x68,
			(byte)0xF8, (byte)0xFF, (byte)0xFF, (byte)0x3F, (byte)0x03, (byte)0x0C, (byte)0x83, (byte)0xC0,
			(byte)0x7F, (byte)0x64, (byte)0x41, (byte)0x98, (byte)0x00, (byte)0x5C, (byte)0x10, (byte)0x59,
			(byte)0x00, (byte)0x2C, (byte)0x88, (byte)0x57, (byte)0xA0, (byte)0x01, (byte)0xA7, (byte)0xA1,
			(byte)0x28, (byte)0xD6, (byte)0x02, (byte)0x00, (byte)0x6E, (byte)0xDB, (byte)0x33, (byte)0xD1,
			(byte)0x03, (byte)0x72, (byte)0x77, (byte)0x36, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
			(byte)0x49, (byte)0x45, (byte)0x4E, (byte)0x44, (byte)0xAE, (byte)0x42, (byte)0x60, (byte)0x82
		};
	}

	//==================================================================

}

//----------------------------------------------------------------------
