/*====================================================================*\

TabbedPane.java

Class: tabbed pane.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.ui.swing.tabbedpane;

//----------------------------------------------------------------------


// IMPORTS


import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AWTEventListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.JWindow;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import uk.blankaspect.ui.swing.action.KeyAction;

import uk.blankaspect.ui.swing.button.ArrowButton;

import uk.blankaspect.ui.swing.colour.Colours;

import uk.blankaspect.ui.swing.font.FontKey;
import uk.blankaspect.ui.swing.font.FontUtils;

import uk.blankaspect.ui.swing.inputmap.InputMapUtils;

import uk.blankaspect.ui.swing.list.SelectionIndicatorList;

import uk.blankaspect.ui.swing.menu.FMenuItem;

import uk.blankaspect.ui.swing.misc.GuiConstants;
import uk.blankaspect.ui.swing.misc.GuiUtils;

import uk.blankaspect.ui.swing.text.TextRendering;
import uk.blankaspect.ui.swing.text.TextUtils;

//----------------------------------------------------------------------


// CLASS: TABBED PANE


public class TabbedPane
	extends JComponent
	implements ActionListener, ComponentListener, PropertyChangeListener
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	public enum Border
	{
		TOP,
		BOTTOM,
		LEFT,
		RIGHT
	}

	private static final	int		SCROLL_INTERVAL	= 400;

	private static final	int		TOP_MARGIN						= 1;
	private static final	int		HEADER_BUTTON_TOP_MARGIN		= 1;
	private static final	int		HEADER_BUTTON_BOTTOM_MARGIN		= 1;
	private static final	int		SCROLL_BUTTON_LEADING_MARGIN	= 2;
	private static final	int		SCROLL_BUTTON_TRAILING_MARGIN	= 2;
	private static final	int		LIST_BUTTON_TRAILING_MARGIN		= 2;

	private static final	int		ARROW_SIZE	= 5;

	private static final	int		SCROLL_BUTTON_WIDTH		= 15;
	private static final	int		LIST_BUTTON_WIDTH		= 17;
	private static final	int		HEADER_BUTTON_HEIGHT	= 15;

	private static final	int		MIN_HEADER_WIDTH	= SCROLL_BUTTON_LEADING_MARGIN
															+ 2 * SCROLL_BUTTON_WIDTH + SCROLL_BUTTON_TRAILING_MARGIN
															+ LIST_BUTTON_WIDTH + LIST_BUTTON_TRAILING_MARGIN;

	private static final	Color	TAB_BACKGROUND_COLOUR				= new Color(216, 216, 216);
	private static final	Color	TAB_BORDER_COLOUR					= Colours.LINE_BORDER;
	private static final	Color	SELECTED_TAB_BACKGROUND_COLOUR		= new Color(244, 208, 128);
	private static final	Color	SELECTED_TAB_BORDER_COLOUR			= new Color(224, 144, 88);
	private static final	Color	SELECTED_TAB_BOTTOM_BORDER_COLOUR	= new Color(220, 186, 108);
	private static final	Color	TEXT_COLOUR							= Colours.FOREGROUND;
	private static final	Color	BORDER_COLOUR						= Colours.LINE_BORDER;
	private static final	Color	BUTTON_BORDER_COLOUR				= new Color(128, 128, 160);
	private static final	Color	BUTTON_BACKGROUND_COLOUR			= new Color(248, 224, 144);

	private static final	String	FOCUS_OWNER_PROPERTY_KEY	= "focusOwner";

	private enum ButtonState
	{
		NOT_OVER,
		OVER,
		PRESSED
	}

	private enum ScrollDirection
	{
		BACKWARD,
		FORWARD
	}

	// Icons
	private static final	ImageIcon	CROSS_ICON			= new ImageIcon(ImgData.CROSS);
	private static final	ImageIcon	ACTIVE_CROSS_ICON	= new ImageIcon(ImgData.ACTIVE_CROSS);
	private static final	ImageIcon	CORNER_L_ICON		= new ImageIcon(ImgData.CORNER_L);
	private static final	ImageIcon	CORNER_R_ICON		= new ImageIcon(ImgData.CORNER_R);
	private static final	ImageIcon	CORNER_LS_ICON		= new ImageIcon(ImgData.CORNER_LS);
	private static final	ImageIcon	CORNER_RS_ICON		= new ImageIcon(ImgData.CORNER_RS);

	private static final	int	CLOSE_BUTTON_ICON_WIDTH		= CROSS_ICON.getIconWidth();
	private static final	int	CLOSE_BUTTON_ICON_HEIGHT	= CROSS_ICON.getIconHeight();
	private static final	int	CLOSE_BUTTON_WIDTH			= CLOSE_BUTTON_ICON_WIDTH + 6;
	private static final	int	CLOSE_BUTTON_HEIGHT			= CLOSE_BUTTON_ICON_HEIGHT + 6;

	// Commands
	private interface Command
	{
		String	CLOSE				= "close";
		String	SCROLL				= "scroll";
		String	SELECT_PREVIOUS_TAB	= "selectPreviousTab";
		String	SELECT_NEXT_TAB		= "selectNextTab";
	}

	private static final	KeyStroke	KEY_PREVIOUS_TAB_A	= KeyStroke.getKeyStroke
	(
		KeyEvent.VK_PAGE_UP,
		KeyEvent.CTRL_DOWN_MASK
	);
	private static final	KeyStroke	KEY_NEXT_TAB_A		= KeyStroke.getKeyStroke
	(
		KeyEvent.VK_PAGE_DOWN,
		KeyEvent.CTRL_DOWN_MASK
	);

	private static final	KeyStroke	KEY_PREVIOUS_TAB_B	= KeyStroke.getKeyStroke
	(
		KeyEvent.VK_TAB,
		KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK
	);
	private static final	KeyStroke	KEY_NEXT_TAB_B		= KeyStroke.getKeyStroke
	(
		KeyEvent.VK_TAB,
		KeyEvent.CTRL_DOWN_MASK
	);

	private static final	KeyAction.KeyCommandPair[]	KEY_COMMANDS_A	=
	{
		new KeyAction.KeyCommandPair(KEY_PREVIOUS_TAB_A, Command.SELECT_PREVIOUS_TAB),
		new KeyAction.KeyCommandPair(KEY_NEXT_TAB_A, Command.SELECT_NEXT_TAB)
	};

	private static final	KeyAction.KeyCommandPair[]	KEY_COMMANDS_B	=
	{
		new KeyAction.KeyCommandPair(KEY_PREVIOUS_TAB_B, Command.SELECT_PREVIOUS_TAB),
		new KeyAction.KeyCommandPair(KEY_NEXT_TAB_B, Command.SELECT_NEXT_TAB)
	};

////////////////////////////////////////////////////////////////////////
//  Member classes : non-inner classes
////////////////////////////////////////////////////////////////////////


	// ELEMENT CLASS


	private static class Element
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private Element(Tab       tab,
						Component component)
		{
			this.tab = tab;
			this.component = component;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		public String toString()
		{
			return tab.getTitle();
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	Tab			tab;
		private	Component	component;
		private	Component	focusOwner;

	}

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Member classes : inner classes
////////////////////////////////////////////////////////////////////////


	// SCROLL BUTTON CLASS


	private class ScrollButton
		extends ArrowButton
		implements MouseListener
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private ScrollButton(ScrollDirection scrollDirection)
		{
			super(SCROLL_BUTTON_WIDTH, HEADER_BUTTON_HEIGHT, ARROW_SIZE);
			this.scrollDirection = scrollDirection;
			setActive(Active.PRESSED);
			setFocusable(false);
			setDirection();
			addMouseListener(this);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : MouseListener interface
	////////////////////////////////////////////////////////////////////

		@Override
		public void mouseClicked(MouseEvent event)
		{
			// do nothing
		}

		//--------------------------------------------------------------

		@Override
		public void mouseEntered(MouseEvent event)
		{
			// do nothing
		}

		//--------------------------------------------------------------

		@Override
		public void mouseExited(MouseEvent event)
		{
			// do nothing
		}

		//--------------------------------------------------------------

		@Override
		public void mousePressed(MouseEvent event)
		{
			if (isEnabled())
				startScrolling(scrollDirection);
		}

		//--------------------------------------------------------------

		@Override
		public void mouseReleased(MouseEvent event)
		{
			if (isEnabled())
				stopScrolling();
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		public void setDirection()
		{
			setDirection((scrollDirection == ScrollDirection.BACKWARD) ? Direction.LEFT : Direction.RIGHT);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	ScrollDirection	scrollDirection;

	}

	//==================================================================


	// LIST BUTTON CLASS


	private class ListButton
		extends ArrowButton
		implements MouseListener, MouseMotionListener
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		private static final	int	CLICK_INTERVAL	= 500;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private ListButton()
		{
			super(LIST_BUTTON_WIDTH, HEADER_BUTTON_HEIGHT, ARROW_SIZE, ArrowButton.Direction.DOWN);
			setActive(Active.PRESSED);
			setFocusable(false);
			addMouseListener(this);
			addMouseMotionListener(this);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : MouseListener interface
	////////////////////////////////////////////////////////////////////

		@Override
		public void mouseClicked(MouseEvent event)
		{
			// do nothing
		}

		//--------------------------------------------------------------

		@Override
		public void mouseEntered(MouseEvent event)
		{
			// do nothing
		}

		//--------------------------------------------------------------

		@Override
		public void mouseExited(MouseEvent event)
		{
			// do nothing
		}

		//--------------------------------------------------------------

		@Override
		public void mousePressed(MouseEvent event)
		{
			if (mouseSelectionListWindow == null)
			{
				pressTime = event.getWhen();
				createMouseSelectionList();
			}
		}

		//--------------------------------------------------------------

		@Override
		public void mouseReleased(MouseEvent event)
		{
			if ((mouseSelectionListWindow != null) && (event.getWhen() > pressTime + CLICK_INTERVAL))
			{
				mouseSelectionListWindow.updateSelection(event);
				mouseSelectionListWindow.doSelection();
			}
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : MouseMotionListener interface
	////////////////////////////////////////////////////////////////////

		@Override
		public void mouseDragged(MouseEvent event)
		{
			if (mouseSelectionListWindow != null)
				mouseSelectionListWindow.updateSelection(event);
		}

		//--------------------------------------------------------------

		@Override
		public void mouseMoved(MouseEvent event)
		{
			// do nothing
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	long	pressTime;

	}

	//==================================================================


	// TAB CLASS


	private class Tab
		extends JComponent
		implements ActionListener, MouseListener, MouseMotionListener
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		private static final	int	TOP_MARGIN				= 2;
		private static final	int	BOTTOM_MARGIN			= 3;
		private static final	int	TEXT_LEADING_MARGIN		= 6;
		private static final	int	TEXT_TRAILING_MARGIN	= 6;
		private static final	int	BUTTON_TRAILING_MARGIN	= 1;

		private static final	int	CORNER_WIDTH	= 4;
		private static final	int	CORNER_HEIGHT	= CORNER_WIDTH;

		private static final	int	MIN_WIDTH	= TEXT_LEADING_MARGIN;

	////////////////////////////////////////////////////////////////////
	//  Member classes : inner classes
	////////////////////////////////////////////////////////////////////


		// TAB ACTION CLASS


		protected class TabAction
			extends AbstractAction
		{

		////////////////////////////////////////////////////////////////
		//  Constructors
		////////////////////////////////////////////////////////////////

			protected TabAction(String command,
								String text)
			{
				super(text);
				putValue(Action.ACTION_COMMAND_KEY, command);
			}

			//----------------------------------------------------------

		////////////////////////////////////////////////////////////////
		//  Instance methods : ActionListener interface
		////////////////////////////////////////////////////////////////

			public void actionPerformed(ActionEvent event)
			{
				Tab.this.actionPerformed(event);
			}

			//----------------------------------------------------------

		}

		//==============================================================

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private Tab(String title,
					Action closeAction)
		{
			// Initialise instance variables
			this.title = title;
			this.closeAction = closeAction;
			buttonState = ButtonState.NOT_OVER;
			FontUtils.setAppFont(FontKey.MAIN, this);
			FontMetrics fontMetrics = getFontMetrics(getFont());
			preferredWidth = TEXT_LEADING_MARGIN + fontMetrics.stringWidth(title)
												+ TEXT_TRAILING_MARGIN + CLOSE_BUTTON_WIDTH + BUTTON_TRAILING_MARGIN;
			height = TOP_MARGIN + Math.max(fontMetrics.getAscent() + fontMetrics.getDescent(), CLOSE_BUTTON_HEIGHT)
																										+ BOTTOM_MARGIN;

			// Set properties
			setOpaque(true);
			setFocusable(false);

			// Add listeners
			addMouseListener(this);
			addMouseMotionListener(this);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : ActionListener interface
	////////////////////////////////////////////////////////////////////

		public void actionPerformed(ActionEvent event)
		{
			close(event.getModifiers());
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : MouseListener interface
	////////////////////////////////////////////////////////////////////

		@Override
		public void mouseClicked(MouseEvent event)
		{
			// do nothing
		}

		//--------------------------------------------------------------

		@Override
		public void mouseEntered(MouseEvent event)
		{
			updateCloseButtonState(event, false);
		}

		//--------------------------------------------------------------

		@Override
		public void mouseExited(MouseEvent event)
		{
			updateCloseButtonState(event, false);
		}

		//--------------------------------------------------------------

		@Override
		public void mousePressed(MouseEvent event)
		{
			updateCloseButtonState(event, true);
			if (buttonState == ButtonState.NOT_OVER)
				selectTab(getIndex());

			showContextMenu(event);
		}

		//--------------------------------------------------------------

		@Override
		public void mouseReleased(MouseEvent event)
		{
			boolean pressed = (buttonState == ButtonState.PRESSED);
			updateCloseButtonState(event, false);
			if (SwingUtilities.isLeftMouseButton(event) && (buttonState == ButtonState.OVER) && pressed)
				close(event.getModifiersEx());

			showContextMenu(event);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : MouseMotionListener interface
	////////////////////////////////////////////////////////////////////

		@Override
		public void mouseDragged(MouseEvent event)
		{
			updateCloseButtonState(event, true);
		}

		//--------------------------------------------------------------

		@Override
		public void mouseMoved(MouseEvent event)
		{
			updateCloseButtonState(event, false);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		public Dimension getPreferredSize()
		{
			return new Dimension(preferredWidth, height);
		}

		//--------------------------------------------------------------

		@Override
		protected void paintComponent(Graphics gr)
		{
			// Create copy of graphics context
			gr = gr.create();

			// Fill background
			Rectangle rect = gr.getClipBounds();
			gr.setColor(selected ? SELECTED_TAB_BACKGROUND_COLOUR : TAB_BACKGROUND_COLOUR);
			gr.fillRect(rect.x, rect.y, rect.width, rect.height);

			// Draw corners
			int width = getWidth();
			int height = getHeight();
			boolean fullWidth = (width == preferredWidth);
			gr.setColor(getBackground());
			if (fullWidth)
			{
				gr.fillRect(0, 0, CORNER_WIDTH, CORNER_HEIGHT);
				gr.drawImage(selected ? CORNER_LS_ICON.getImage() : CORNER_L_ICON.getImage(), 0, 0, null);
				gr.fillRect(width - CORNER_WIDTH, 0, CORNER_WIDTH, CORNER_HEIGHT);
				gr.drawImage(selected ? CORNER_RS_ICON.getImage()
									  : CORNER_R_ICON.getImage(), width - CORNER_WIDTH, 0, null);
			}
			else
			{
				gr.fillRect(0, 0, CORNER_WIDTH, CORNER_HEIGHT);
				gr.drawImage(selected ? CORNER_LS_ICON.getImage() : CORNER_L_ICON.getImage(), 0, 0,
							 null);
			}

			// Set rendering hints for text antialiasing and fractional metrics
			TextRendering.setHints((Graphics2D)gr);

			// Get text
			FontMetrics fontMetrics = gr.getFontMetrics();
			String str = title;
			if (!fullWidth)
			{
				int maxTextWidth = width - TEXT_LEADING_MARGIN - TEXT_TRAILING_MARGIN;
				str = (maxTextWidth < fontMetrics.stringWidth(GuiConstants.ELLIPSIS_STR))
													? null
													: TextUtils.getLimitedWidthString(str, fontMetrics, maxTextWidth,
																					  TextUtils.RemovalMode.END);
			}

			// Draw text
			if (str != null)
			{
				gr.setColor(TEXT_COLOUR);
				gr.drawString(str, TEXT_LEADING_MARGIN, FontUtils.getBaselineOffset(height, fontMetrics));
			}

			// Draw close button
			if (fullWidth)
			{
				int x = width - CLOSE_BUTTON_WIDTH - BUTTON_TRAILING_MARGIN;
				int y = (height - CLOSE_BUTTON_HEIGHT + 1) / 2;

				if (buttonState != ButtonState.NOT_OVER)
				{
					gr.setColor(BUTTON_BORDER_COLOUR);
					if (buttonState == ButtonState.PRESSED)
						gr.fillRect(x, y, CLOSE_BUTTON_WIDTH, CLOSE_BUTTON_HEIGHT);
					else
					{
						gr.drawRect(x, y, CLOSE_BUTTON_WIDTH - 1, CLOSE_BUTTON_HEIGHT - 1);
						gr.setColor(BUTTON_BACKGROUND_COLOUR);
						gr.fillRect(x + 1, y + 1, CLOSE_BUTTON_WIDTH - 2, CLOSE_BUTTON_HEIGHT - 2);
					}
				}

				gr.drawImage((buttonState == ButtonState.PRESSED) ? ACTIVE_CROSS_ICON.getImage()
																  : CROSS_ICON.getImage(),
							 x + (CLOSE_BUTTON_WIDTH - CLOSE_BUTTON_ICON_WIDTH) / 2,
							 y + (CLOSE_BUTTON_HEIGHT - CLOSE_BUTTON_ICON_HEIGHT) / 2, null);
			}

			// Draw border
			gr.setColor(selected ? SELECTED_TAB_BORDER_COLOUR : TAB_BORDER_COLOUR);
			if (fullWidth)
			{
				int x1 = 0;
				int x2 = width - 1;
				int y1 = CORNER_HEIGHT;
				int y2 = height - 1;
				gr.drawLine(x1, y1, x1, y2);
				gr.drawLine(x2, y1, x2, y2);

				x1 = CORNER_WIDTH;
				x2 = width - CORNER_WIDTH - 1;
				y1 = 0;
				gr.drawLine(x1, y1, x2, y1);
			}
			else
			{
				int x1 = 0;
				int y1 = CORNER_HEIGHT;
				int y2 = height - 1;
				gr.drawLine(x1, y1, x1, y2);

				x1 = CORNER_WIDTH;
				int x2 = width - 1;
				y1 = 0;
				gr.drawLine(x1, y1, x2, y1);
			}

			int x1 = 0;
			int x2 = width - 1;
			int y = height - 1;
			if (selected)
			{
				gr.setColor(SELECTED_TAB_BOTTOM_BORDER_COLOUR);
				++x1;
				if (fullWidth)
					--x2;
			}
			else
				gr.setColor(SELECTED_TAB_BORDER_COLOUR);
			gr.drawLine(x1, y, x2, y);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		private String getTitle()
		{
			return title;
		}

		//--------------------------------------------------------------

		private void setSelected(boolean selected)
		{
			if (this.selected != selected)
			{
				this.selected = selected;
				repaint();
			}
		}

		//--------------------------------------------------------------

		private void setTitle(String title)
		{
			if (!this.title.equals(title))
			{
				this.title = title;
				FontMetrics fontMetrics = getFontMetrics(getFont());
				int width = TEXT_LEADING_MARGIN + fontMetrics.stringWidth(title)
												+ TEXT_TRAILING_MARGIN + CLOSE_BUTTON_WIDTH + BUTTON_TRAILING_MARGIN;
				if (preferredWidth != width)
				{
					preferredWidth = width;
					updateTabs();
				}
				else
					repaint();
			}
		}

		//--------------------------------------------------------------

		private boolean isWithinButton(MouseEvent event)
		{
			int width = getWidth();
			if (width < preferredWidth)
				return false;

			int x = event.getX();
			int y = event.getY();
			int x2 = width - BUTTON_TRAILING_MARGIN;
			int x1 = x2 - CLOSE_BUTTON_WIDTH;
			int y1 = (getHeight() - CLOSE_BUTTON_HEIGHT) / 2;
			int y2 = y1 + CLOSE_BUTTON_HEIGHT;
			return ((x >= x1) && (x < x2) && (y >= y1) && (y < y2));
		}

		//--------------------------------------------------------------

		private void updateCloseButtonState(MouseEvent event,
											boolean    pressed)
		{
			ButtonState state = isWithinButton(event)
											? (pressed && SwingUtilities.isLeftMouseButton(event)) ? ButtonState.PRESSED
																								   : ButtonState.OVER
											: ButtonState.NOT_OVER;
			if (buttonState != state)
			{
				buttonState = state;
				repaint();
			}
		}

		//--------------------------------------------------------------

		private int getIndex()
		{
			for (int i = 0; i < elements.size(); i++)
			{
				if (elements.get(i).tab == this)
					return i;
			}
			return -1;
		}

		//--------------------------------------------------------------

		private void showContextMenu(MouseEvent event)
		{
			if (event.isPopupTrigger())
			{
				// Create context menu
				if (contextMenu == null)
					contextMenu = new JPopupMenu();
				else
					contextMenu.removeAll();

				contextMenu.add(new FMenuItem(new TabAction(Command.CLOSE, GuiConstants.CLOSE_STR)));

				// Display menu
				contextMenu.show(event.getComponent(), event.getX(), event.getY());
			}
		}

		//--------------------------------------------------------------

		private void close(int modifiers)
		{
			if ((closeAction != null) && !closing)
			{
				closing = true;
				String command = closeAction.getValue(Action.ACTION_COMMAND_KEY).toString()
																						+ Integer.toString(getIndex());
				closeAction.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, command, modifiers));
				closing = false;
			}
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	String		title;
		private	Action		closeAction;
		private	boolean		closing;
		private	int			preferredWidth;
		private	int			height;
		private	boolean		selected;
		private	ButtonState	buttonState;

	}

	//==================================================================


	// KEY-SELECTION LIST WINDOW CLASS


	private class KeySelectionListWindow
		extends JWindow
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private KeySelectionListWindow(Window  owner,
									   boolean decrement)
		{
			// Call superclass constructor
			super(owner);

			// Make window non-focusable
			setFocusableWindowState(false);

			// Create list
			list = new SelectionIndicatorList<>(recentElements, -1);
			list.setSelectedIndex(decrement ? recentElements.size() - 1 : 1);

			// Set list as content pane
			setContentPane(list);

			// Resize window to its preferred size
			pack();

			// Set location of window
			setLocation(GuiUtils.getComponentLocation(list, owner));

			// Show window
			setVisible(true);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		private void decrementSelection()
		{
			int numItems = list.getNumItems();
			int index = list.getSelectedIndex();
			if (index < 0)
				index = 0;
			index = (index == 0) ? numItems - 1 : index - 1;
			list.setSelectedIndex(index);
		}

		//--------------------------------------------------------------

		private void incrementSelection()
		{
			int numItems = list.getNumItems();
			int index = list.getSelectedIndex();
			if (index < 0)
				index = 0;
			index = (index == numItems - 1) ? 0 : index + 1;
			list.setSelectedIndex(index);
		}

		//--------------------------------------------------------------

		private void doSelection()
		{
			destroyKeySelectionList();
			selectTab(list.getSelectedItem());
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	SelectionIndicatorList<Element>	list;

	}

	//==================================================================


	// MOUSE-SELECTION LIST WINDOW CLASS


	private class MouseSelectionListWindow
		extends JWindow
		implements AWTEventListener, MouseListener, MouseMotionListener
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private MouseSelectionListWindow(Window owner)
		{
			// Call superclass constructor
			super(owner);

			// Make window non-focusable
			setFocusableWindowState(false);

			// Create list
			List<Element> orderedElements = new ArrayList<>(elements);
			orderedElements.sort(Comparator.<Element, String>comparing(element -> element.toString(),
																	   ignoreCase ? String.CASE_INSENSITIVE_ORDER
																				  : Comparator.naturalOrder()));
			list = new SelectionIndicatorList<>(orderedElements,
												(selectedIndex < 0) ? null : elements.get(selectedIndex));

			// Add listeners
			getToolkit().addAWTEventListener(this, AWTEvent.MOUSE_EVENT_MASK);
			list.addMouseListener(this);
			list.addMouseMotionListener(this);

			// Set list as content pane
			setContentPane(list);

			// Resize window to its preferred size
			pack();

			// Set location of window
			Point location = new Point(listButton.getWidth() - list.getPreferredSize().width, listButton.getHeight());
			SwingUtilities.convertPointToScreen(location, listButton);
			setLocation(GuiUtils.getComponentLocation(list, location));

			// Show window
			setVisible(true);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : AWTEventListener interface
	////////////////////////////////////////////////////////////////////

		public void eventDispatched(AWTEvent event)
		{
			if (event.getID() == MouseEvent.MOUSE_PRESSED)
			{
				MouseEvent mouseEvent = (MouseEvent)event;
				if (mouseEvent.getComponent() != list)
					destroyMouseSelectionList();
				if (mouseEvent.getComponent() == listButton)
					mouseEvent.consume();
			}
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : MouseListener interface
	////////////////////////////////////////////////////////////////////

		@Override
		public void mouseClicked(MouseEvent event)
		{
			// do nothing
		}

		//--------------------------------------------------------------

		@Override
		public void mouseEntered(MouseEvent event)
		{
			updateSelection(event);
		}

		//--------------------------------------------------------------

		@Override
		public void mouseExited(MouseEvent event)
		{
			updateSelection(event);
		}

		//--------------------------------------------------------------

		@Override
		public void mousePressed(MouseEvent event)
		{
			updateSelection(event);
			doSelection();
		}

		//--------------------------------------------------------------

		@Override
		public void mouseReleased(MouseEvent event)
		{
			updateSelection(event);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : MouseMotionListener interface
	////////////////////////////////////////////////////////////////////

		@Override
		public void mouseDragged(MouseEvent event)
		{
			updateSelection(event);
		}

		//--------------------------------------------------------------

		@Override
		public void mouseMoved(MouseEvent event)
		{
			updateSelection(event);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		private void updateSelection(MouseEvent event)
		{
			Point point = SwingUtilities.convertPoint(event.getComponent(), event.getPoint(), list);
			list.setSelectedIndex(list.pointToIndex(point));
		}

		//--------------------------------------------------------------

		private void doSelection()
		{
			destroyMouseSelectionList();
			selectTab(list.getSelectedItem());
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	SelectionIndicatorList<Element>	list;

	}

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private	ScrollButton				previousTabButton;
	private	ScrollButton				nextTabButton;
	private	ListButton					listButton;
	private	List<Element>				elements;
	private	List<Element>				recentElements;
	private	Set<Border>					borders;
	private	int							startIndex;
	private	int							selectedIndex;
	private	int							numViewableTabs;
	private	int							headerHeight;
	private	boolean						ignoreCase;
	private	boolean						directTabTraversal;
	private	Timer						scrollTimer;
	private	ScrollDirection				scrollDirection;
	private	KeySelectionListWindow		keySelectionListWindow;
	private	MouseSelectionListWindow	mouseSelectionListWindow;
	private	List<ChangeListener>		changeListeners;
	private	ChangeEvent					changeEvent;
	private	JPopupMenu					contextMenu;

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	public TabbedPane()
	{
		this(EnumSet.allOf(Border.class), true);
	}

	//------------------------------------------------------------------

	public TabbedPane(Set<Border> borderComponents)
	{
		this(borderComponents, true);
	}

	//------------------------------------------------------------------

	public TabbedPane(boolean directTabTraversal)
	{
		this(EnumSet.allOf(Border.class), directTabTraversal);
	}

	//------------------------------------------------------------------

	public TabbedPane(Set<Border> borders,
					  boolean     directTabTraversal)
	{
		// Initialise instance variables
		elements = new ArrayList<>();
		recentElements = new ArrayList<>();
		this.borders = EnumSet.copyOf(borders);
		selectedIndex = -1;
		scrollTimer = new Timer(SCROLL_INTERVAL, this);
		scrollTimer.setActionCommand(Command.SCROLL);
		changeListeners = new ArrayList<>();
		headerHeight = Math.max(HEADER_BUTTON_TOP_MARGIN + HEADER_BUTTON_HEIGHT + HEADER_BUTTON_BOTTOM_MARGIN,
								createTab("", null).getPreferredSize().height);
		this.directTabTraversal = directTabTraversal;

		// Set properties
		setLayout(null);
		setOpaque(true);
		setFocusable(false);

		// Create header buttons
		previousTabButton = new ScrollButton(ScrollDirection.BACKWARD);
		nextTabButton = new ScrollButton(ScrollDirection.FORWARD);
		listButton = new ListButton();
		updateButtons();

		// Remove Ctrl+Tab and Ctrl+Shift+Tab from focus traversal keys
		if (!directTabTraversal)
		{
			setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS,
								  Collections.singleton(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0)));
			setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS,
								  Collections.singleton(KeyStroke.getKeyStroke(KeyEvent.VK_TAB,
																			   KeyEvent.SHIFT_DOWN_MASK)));
		}

		// Track changes in size
		addComponentListener(this);

		// Track focus owner
		KeyboardFocusManager.getCurrentKeyboardFocusManager().addPropertyChangeListener(FOCUS_OWNER_PROPERTY_KEY, this);

		// Add commands to action map
		KeyAction.create(this, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, this,
						 directTabTraversal ? KEY_COMMANDS_A : KEY_COMMANDS_B);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : ActionListener interface
////////////////////////////////////////////////////////////////////////

	@Override
	public void actionPerformed(ActionEvent event)
	{
		String command = event.getActionCommand();

		if (command.equals(Command.SCROLL))
			onScroll();

		else if (command.equals(Command.SELECT_PREVIOUS_TAB))
			onSelectPreviousTab();

		else if (command.equals(Command.SELECT_NEXT_TAB))
			onSelectNextTab();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : ComponentListener interface
////////////////////////////////////////////////////////////////////////

	@Override
	public void componentHidden(ComponentEvent event)
	{
		// do nothing
	}

	//------------------------------------------------------------------

	@Override
	public void componentMoved(ComponentEvent event)
	{
		// do nothing
	}

	//------------------------------------------------------------------

	@Override
	public void componentResized(ComponentEvent event)
	{
		if (!elements.isEmpty())
		{
			updateStartIndex(true);
			updateButtons();
		}
	}

	//------------------------------------------------------------------

	@Override
	public void componentShown(ComponentEvent event)
	{
		// do nothing
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : PropertyChangeListener interface
////////////////////////////////////////////////////////////////////////

	/**
	 * Tracks changes to the focused component.  If the new focus owner is a descendant of this panel, the
	 * focus-owner field of its ancestor in the list of elements is updated accordingly.  If the new focus
	 * owner is not a descendant of this panel, any key-selection list or mouse-selection list that is
	 * currently display is destroyed.
	 *
	 * @param event  the {@code PropertyChangeEvent} that notifies the change of focus owner.
	 */

	@Override
	public void propertyChange(PropertyChangeEvent event)
	{
		Component focusOwner = (Component)event.getNewValue();
		if (isAncestorOf(focusOwner))
		{
			for (Element element : elements)
			{
				if (SwingUtilities.isDescendingFrom(focusOwner, element.component))
				{
					element.focusOwner = focusOwner;
					break;
				}
			}
		}
		else
		{
			destroyKeySelectionList();
			destroyMouseSelectionList();
		}
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : overriding methods
////////////////////////////////////////////////////////////////////////

	@Override
	public Dimension getMinimumSize()
	{
		if (isMinimumSizeSet())
			return super.getMinimumSize();

		int maxWidth = MIN_HEADER_WIDTH;
		int maxHeight = 0;
		for (Element element : elements)
		{
			if (element.component.isVisible())
			{
				Dimension elementSize = element.component.getMinimumSize();
				if (maxWidth < elementSize.width)
					maxWidth = elementSize.width;
				if (maxHeight < elementSize.height)
					maxHeight = elementSize.height;
			}
		}
		return new Dimension(getBorderSize(Border.LEFT) + maxWidth + getBorderSize(Border.RIGHT),
							 getBorderSize(Border.TOP) + TOP_MARGIN + headerHeight + maxHeight
																						+ getBorderSize(Border.BOTTOM));
	}

	//------------------------------------------------------------------

	@Override
	public Dimension getPreferredSize()
	{
		if (isPreferredSizeSet())
			return super.getPreferredSize();

		int maxWidth = MIN_HEADER_WIDTH;
		int maxHeight = 0;
		for (Element element : elements)
		{
			if (element.component.isVisible())
			{
				Dimension elementSize = element.component.getPreferredSize();
				if (maxWidth < elementSize.width)
					maxWidth = elementSize.width;
				if (maxHeight < elementSize.height)
					maxHeight = elementSize.height;
			}
		}
		return new Dimension(getBorderSize(Border.LEFT) + maxWidth + getBorderSize(Border.RIGHT),
							 getBorderSize(Border.TOP) + TOP_MARGIN + headerHeight + maxHeight
																						+ getBorderSize(Border.BOTTOM));
	}

	//------------------------------------------------------------------

	@Override
	public void doLayout()
	{
		Component focusOwner = null;

		if (elements.isEmpty())
			focusOwner = getWindow();
		else
		{
			// Tabs
			int width = getWidth();
			int maxTotalTabWidth = getMaxTotalTabWidth();
			int x = getBorderSize(Border.LEFT);
			for (int i = 0; i < getComponentCount(); i++)
			{
				Component component = getComponent(i);
				if (component instanceof Tab)
				{
					int tabHeight = component.getPreferredSize().height;
					int tabWidth = Math.min(component.getPreferredSize().width, maxTotalTabWidth - x);
					component.setBounds(x, getBorderSize(Border.TOP) + TOP_MARGIN + headerHeight - tabHeight, tabWidth,
										tabHeight);
					x += tabWidth;
				}
			}

			// Buttons
			x = width - getBorderSize(Border.RIGHT) - 2 * SCROLL_BUTTON_WIDTH
									- SCROLL_BUTTON_TRAILING_MARGIN - LIST_BUTTON_WIDTH - LIST_BUTTON_TRAILING_MARGIN;
			int y = getBorderSize(Border.TOP) + (TOP_MARGIN + headerHeight - HEADER_BUTTON_HEIGHT) / 2;
			int buttonWidth = SCROLL_BUTTON_WIDTH;
			previousTabButton.setBounds(x, y, buttonWidth, HEADER_BUTTON_HEIGHT);

			x += SCROLL_BUTTON_WIDTH;
			nextTabButton.setBounds(x, y, buttonWidth, HEADER_BUTTON_HEIGHT);

			x += SCROLL_BUTTON_WIDTH + SCROLL_BUTTON_TRAILING_MARGIN;
			buttonWidth = LIST_BUTTON_WIDTH;
			listButton.setBounds(x, y, buttonWidth, HEADER_BUTTON_HEIGHT);

			// Main component
			x = getBorderSize(Border.LEFT);
			y = getBorderSize(Border.TOP) + TOP_MARGIN + headerHeight;
			for (int i = 0; i < elements.size(); i++)
			{
				Element element = elements.get(i);
				if (element.component.isVisible())
				{
					Dimension minSize = element.component.getMinimumSize();
					Dimension maxSize = element.component.getMaximumSize();
					int compWidth = Math.min(Math.max(minSize.width, width - x - getBorderSize(Border.RIGHT)),
											 maxSize.width);
					int compHeight = Math.min(Math.max(minSize.height, getHeight() - y - getBorderSize(Border.BOTTOM)),
											  maxSize.height);
					element.component.setBounds(x, y, compWidth, compHeight);
					if (i == selectedIndex)
						focusOwner = element.focusOwner;
				}
			}
		}

		// Request focus
		if (focusOwner != null)
			focusOwner.requestFocusInWindow();
	}

	//------------------------------------------------------------------

	@Override
	protected void paintComponent(Graphics gr)
	{
		// Fill background
		Rectangle rect = gr.getClipBounds();
		gr.setColor(getBackground());
		gr.fillRect(rect.x, rect.y, rect.width, rect.height);

		// Draw border
		gr.setColor(BORDER_COLOUR);
		int x1 = 0;
		int x2 = getWidth() - 1;
		int y1 = 0;
		int y2 = getHeight() - 1;
		for (Border border : borders)
		{
			switch (border)
			{
				case TOP:
					gr.drawLine(x1, y1, x2, y1);
					break;

				case BOTTOM:
					gr.drawLine(x1, y2, x2, y2);
					break;

				case LEFT:
					gr.drawLine(x1, y1, x1, y2);
					break;

				case RIGHT:
					gr.drawLine(x2, y1, x2, y2);
					break;
			}
		}

		// Draw header bottom border
		if (!elements.isEmpty())
		{
			gr.setColor(SELECTED_TAB_BORDER_COLOUR);
			int y = getBorderSize(Border.TOP) + TOP_MARGIN + headerHeight - 1;
			gr.drawLine(getBorderSize(Border.LEFT), y, getWidth() - getBorderSize(Border.RIGHT) - 1, y);
		}
	}

	//------------------------------------------------------------------

	@Override
	protected boolean processKeyBinding(KeyStroke keyStroke,
										KeyEvent  event,
										int       condition,
										boolean   pressed)
	{
		if ((keySelectionListWindow != null) && (event.getKeyCode() == KeyEvent.VK_CONTROL) && !pressed)
			keySelectionListWindow.doSelection();

		return super.processKeyBinding(keyStroke, event, condition, pressed);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	public int getNumTabs()
	{
		return elements.size();
	}

	//------------------------------------------------------------------

	public int getSelectedIndex()
	{
		return selectedIndex;
	}

	//------------------------------------------------------------------

	public Component getSelectedComponent()
	{
		return ((selectedIndex < 0) ? null : elements.get(selectedIndex).component);
	}

	//------------------------------------------------------------------

	public Dimension getFrameSize()
	{
		return new Dimension(getBorderSize(Border.LEFT) + getBorderSize(Border.RIGHT),
							 getBorderSize(Border.TOP) + TOP_MARGIN + headerHeight + getBorderSize(Border.BOTTOM));
	}

	//------------------------------------------------------------------

	public void setIgnoreCase(boolean ignoreCase)
	{
		this.ignoreCase = ignoreCase;
	}

	//------------------------------------------------------------------

	public void setTitle(int    index,
						 String title)
	{
		elements.get(index).tab.setTitle(title);
	}

	//------------------------------------------------------------------

	public void setTooltipText(int    index,
							   String text)
	{
		elements.get(index).tab.setToolTipText(text);
	}

	//------------------------------------------------------------------

	public void setComponent(int       index,
							 Component component)
	{
		// Set component
		elements.get(index).component = component;

		// Update components
		if (index == selectedIndex)
			updateComponents();

		// Remove tab traversal keys from input map of component
		removeTabTraversalKeys(component);
	}

	//------------------------------------------------------------------

	public void addComponent(String    title,
							 Action    closeAction,
							 Component component)
	{
		// Destroy any selection list
		destroyKeySelectionList();
		destroyMouseSelectionList();

		// Remove tab traversal keys from input map of component
		removeTabTraversalKeys(component);

		// Add new component to lists
		elements.add(new Element(createTab(title, closeAction), component));
		int index = elements.size() - 1;
		recentElements.add(0, elements.get(index));

		// Select new component
		setSelectedIndex(index);

		// Request focus for new component
		component.requestFocusInWindow();
	}

	//------------------------------------------------------------------

	/**
	 * @throws IndexOutOfBoundsException
	 */

	public void removeComponent(int index)
	{
		// Validate index
		if ((index < 0) || (index >= elements.size()))
			throw new IndexOutOfBoundsException();

		// Destroy any selection list
		destroyKeySelectionList();
		destroyMouseSelectionList();

		// Remove component from lists
		recentElements.remove(elements.get(index));
		elements.remove(index);

		// Adjust selected index
		if (selectedIndex < elements.size())
		{
			int i = selectedIndex;
			selectedIndex = -1;
			setSelectedIndex(i);
		}
		else
		{
			if (selectedIndex > 0)
				setSelectedIndex(selectedIndex - 1);
			else
			{
				selectedIndex = -1;
				fireStateChanged();
			}
		}

		// Adjust index of first tab
		int maxStartIndex = getMaximumStartIndex();
		if (startIndex > maxStartIndex)
			startIndex = maxStartIndex;

		// Update buttons and components
		updateButtons();
		updateComponents();
	}

	//------------------------------------------------------------------

	/**
	 * @throws IndexOutOfBoundsException
	 */

	public void setSelectedIndex(int index)
	{
		// Validate index
		if ((index < 0) || (index >= elements.size()))
			throw new IndexOutOfBoundsException();

		// Set new selected index
		if (selectedIndex != index)
		{
			// Set index
			selectedIndex = index;

			// Update selected status of tabs
			for (int i = 0; i < elements.size(); i++)
				elements.get(i).tab.setSelected(i == selectedIndex);

			// Update list of recent elements
			int recentIndex = recentElements.indexOf(elements.get(selectedIndex));
			if (recentIndex > 0)
				recentElements.add(0, recentElements.remove(recentIndex));

			// Update components of panel
			updateComponents();

			// Update start index of tabs
			updateStartIndex(false);

			// Notify listeners of change to selected index
			fireStateChanged();
		}
	}

	//------------------------------------------------------------------

	public void setBorders(Set<Border> borders)
	{
		// Enable border components
		this.borders = (borders == null) ? EnumSet.noneOf(Border.class) : EnumSet.copyOf(borders);

		// Revalidate and repaint panel
		revalidate();
		repaint();
	}

	//------------------------------------------------------------------

	public void addChangeListener(ChangeListener listener)
	{
		changeListeners.add(listener);
	}

	//------------------------------------------------------------------

	protected void fireStateChanged()
	{
		for (int i = changeListeners.size() - 1; i >= 0; i--)
		{
			if (changeEvent == null)
				changeEvent = new ChangeEvent(this);
			changeListeners.get(i).stateChanged(changeEvent);
		}
	}

	//------------------------------------------------------------------

	private Window getWindow()
	{
		return SwingUtilities.getWindowAncestor(this);
	}

	//------------------------------------------------------------------

	private int getMaxTotalTabWidth()
	{
		return Math.max(0, getWidth() - (getBorderSize(Border.LEFT) + MIN_HEADER_WIDTH + getBorderSize(Border.RIGHT)));
	}

	//------------------------------------------------------------------

	private int getMaximumStartIndex()
	{
		int maxTotalTabWidth = getMaxTotalTabWidth();
		int tabWidthSum = 0;
		int index = elements.size();
		while (--index >= 0)
		{
			Tab tab = elements.get(index).tab;
			if (tabWidthSum + Math.min(tab.preferredWidth, maxTotalTabWidth) > maxTotalTabWidth)
				break;
			tabWidthSum += tab.preferredWidth;
		}
		++index;
		return Math.max(0, Math.min(index, elements.size() - 1));
	}

	//------------------------------------------------------------------

	private int getBorderSize(Border border)
	{
		return (borders.contains(border) ? 1 : 0);
	}

	//------------------------------------------------------------------

	/**
	 * Adjusts the index of the first tab in the tab header so that the selected tab is displayed at its
	 * preferred width.
	 *
	 * @param force  if {@code true}, force the index of the first tab to be recalculated; otherwise, adjust
	 *               the index only if the selected tab is not displayed in the tab header.
	 */

	private void updateStartIndex(boolean force)
	{
		// Ensure that selected index is not beyond tabs
		int oldStartIndex = startIndex;
		if (force || (selectedIndex >= startIndex + numViewableTabs - 1))
		{
			startIndex = selectedIndex;
			int tabWidthSum = 0;
			int maxTotalTabWidth = getMaxTotalTabWidth();
			while (startIndex >= 0)
			{
				Tab tab = elements.get(startIndex).tab;
				if (tabWidthSum + Math.min(tab.preferredWidth, maxTotalTabWidth) > maxTotalTabWidth)
					break;
				tabWidthSum += tab.preferredWidth;
				--startIndex;
			}
			++startIndex;
		}

		// Ensure that start index is not beyond selected index
		if (startIndex > selectedIndex)
			startIndex = selectedIndex;

		// Update tabs and buttons if start index has changed
		if (startIndex != oldStartIndex)
		{
			updateTabs();
			updateButtons();
		}
	}

	//------------------------------------------------------------------

	private void updateTabs()
	{
		// Remove all tabs
		for (int i = 0; i < getComponentCount(); i++)
		{
			if (getComponent(i) instanceof Tab)
			{
				remove(i--);
				--numViewableTabs;
			}
		}

		// Add tabs
		addTabs();

		// Revalidate panel and repaint header
		revalidate();
		repaint(0, getBorderSize(Border.TOP) + TOP_MARGIN, getWidth(), headerHeight);
	}

	//------------------------------------------------------------------

	private void updateButtons()
	{
		previousTabButton.setEnabled(startIndex > 0);
		nextTabButton.setEnabled(startIndex < getMaximumStartIndex());
	}

	//------------------------------------------------------------------

	private void updateComponents()
	{
		// Remove all child components
		removeAll();
		numViewableTabs = 0;

		// Set direction of scroll buttons
		previousTabButton.setDirection();
		nextTabButton.setDirection();

		// Add child components
		if (!elements.isEmpty())
		{
			// Add tabs
			addTabs();

			// Add buttons
			add(previousTabButton);
			add(nextTabButton);
			add(listButton);

			// Add main component
			add(elements.get(selectedIndex).component);
		}

		// Revalidate and repaint panel
		revalidate();
		repaint();
	}

	//------------------------------------------------------------------

	private void removeTabTraversalKeys(Component component)
	{
		if (directTabTraversal && (component instanceof JComponent))
			InputMapUtils.removeFromInputMap((JComponent)component, true, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT,
											 new KeyStroke[] { KEY_PREVIOUS_TAB_A, KEY_NEXT_TAB_A });
	}

	//------------------------------------------------------------------

	private void addTabs()
	{
		int maxTotalTabWidth = getMaxTotalTabWidth();
		int tabWidthSum = 0;
		for (int i = startIndex; i < elements.size(); i++)
		{
			Tab tab = elements.get(i).tab;
			if (tabWidthSum + Tab.MIN_WIDTH > maxTotalTabWidth)
				break;
			add(tab, i - startIndex);
			++numViewableTabs;
			tabWidthSum += tab.preferredWidth;
		}
	}

	//------------------------------------------------------------------

	private void selectTab(int index)
	{
		if (index == selectedIndex)
			updateStartIndex(false);
		else
			setSelectedIndex(index);
	}

	//------------------------------------------------------------------

	private void selectTab(Element element)
	{
		if (element != null)
		{
			int index = elements.indexOf(element);
			if (index >= 0)
				selectTab(index);
		}
	}

	//------------------------------------------------------------------

	private Tab createTab(String title,
						  Action closeAction)
	{
		return new Tab(title, closeAction);
	}

	//------------------------------------------------------------------

	private void createKeySelectionList(boolean decrement)
	{
		if (elements.size() > 1)
			keySelectionListWindow = new KeySelectionListWindow(getWindow(), decrement);
	}

	//------------------------------------------------------------------

	private void destroyKeySelectionList()
	{
		if (keySelectionListWindow != null)
		{
			keySelectionListWindow.setVisible(false);
			keySelectionListWindow.dispose();
			keySelectionListWindow = null;
		}
	}

	//------------------------------------------------------------------

	private void createMouseSelectionList()
	{
		mouseSelectionListWindow = new MouseSelectionListWindow(getWindow());
	}

	//------------------------------------------------------------------

	private void destroyMouseSelectionList()
	{
		if (mouseSelectionListWindow != null)
		{
			mouseSelectionListWindow.getToolkit().removeAWTEventListener(mouseSelectionListWindow);
			mouseSelectionListWindow.setVisible(false);
			mouseSelectionListWindow.dispose();
			mouseSelectionListWindow = null;
		}
	}

	//------------------------------------------------------------------

	private void startScrolling(ScrollDirection direction)
	{
		scrollDirection = direction;
		scrollTimer.start();
		onScroll();
	}

	//------------------------------------------------------------------

	private void stopScrolling()
	{
		scrollTimer.stop();
		scrollDirection = null;
	}

	//------------------------------------------------------------------

	private void onScroll()
	{
		switch (scrollDirection)
		{
			case BACKWARD:
			{
				if (startIndex > 0)
				{
					if (--startIndex == 0)
						stopScrolling();
					updateTabs();
					updateButtons();
				}
				break;
			}

			case FORWARD:
			{
				int maxStartIndex = getMaximumStartIndex();
				if (startIndex < maxStartIndex)
				{
					if (++startIndex == maxStartIndex)
						stopScrolling();
					updateTabs();
					updateButtons();
				}
				break;
			}
		}
	}

	//------------------------------------------------------------------

	private void onSelectPreviousTab()
	{
		if (directTabTraversal)
		{
			if (selectedIndex > 0)
				selectTab(selectedIndex - 1);
		}
		else
		{
			if (keySelectionListWindow == null)
				createKeySelectionList(true);
			else
				keySelectionListWindow.decrementSelection();
		}
	}

	//------------------------------------------------------------------

	private void onSelectNextTab()
	{
		if (directTabTraversal)
		{
			if (selectedIndex < elements.size() - 1)
				selectTab(selectedIndex + 1);
		}
		else
		{
			if (keySelectionListWindow == null)
				createKeySelectionList(false);
			else
				keySelectionListWindow.incrementSelection();
		}
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
		// File: cross-black-8x8
		byte[]	CROSS	=
		{
			(byte)0x89, (byte)0x50, (byte)0x4E, (byte)0x47, (byte)0x0D, (byte)0x0A, (byte)0x1A, (byte)0x0A,
			(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x0D, (byte)0x49, (byte)0x48, (byte)0x44, (byte)0x52,
			(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x08, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x08,
			(byte)0x08, (byte)0x06, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0xC4, (byte)0x0F, (byte)0xBE,
			(byte)0x8B, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x5A, (byte)0x49, (byte)0x44, (byte)0x41,
			(byte)0x54, (byte)0x78, (byte)0xDA, (byte)0x6D, (byte)0x8F, (byte)0x51, (byte)0x0E, (byte)0xC0,
			(byte)0x20, (byte)0x08, (byte)0x43, (byte)0xB9, (byte)0xAA, (byte)0x7C, (byte)0xC9, (byte)0x5D,
			(byte)0xE7, (byte)0x8D, (byte)0x74, (byte)0xAC, (byte)0x10, (byte)0xB6, (byte)0xD5, (byte)0x65,
			(byte)0x26, (byte)0x2F, (byte)0x31, (byte)0xB4, (byte)0x16, (byte)0x2B, (byte)0x22, (byte)0x32,
			(byte)0x40, (byte)0x77, (byte)0x77, (byte)0x61, (byte)0x70, (byte)0x1A, (byte)0x38, (byte)0xE2,
			(byte)0xD2, (byte)0xC1, (byte)0x02, (byte)0x46, (byte)0xA2, (byte)0x82, (byte)0x99, (byte)0xA6,
			(byte)0x1A, (byte)0xD8, (byte)0x6D, (byte)0x22, (byte)0x51, (byte)0x53, (byte)0xA3, (byte)0x57,
			(byte)0x21, (byte)0x9E, (byte)0x65, (byte)0xD4, (byte)0x67, (byte)0xFE, (byte)0x89, (byte)0x5D,
			(byte)0x65, (byte)0xB2, (byte)0xCD, (byte)0xC0, (byte)0xB1, (byte)0xBC, (byte)0xAE, (byte)0xB4,
			(byte)0xFC, (byte)0xED, (byte)0xDC, (byte)0x62, (byte)0x5F, (byte)0x53, (byte)0x14, (byte)0xC8,
			(byte)0x2A, (byte)0xED, (byte)0xA7, (byte)0x66, (byte)0x88, (byte)0xE3, (byte)0x02, (byte)0xA3,
			(byte)0xF7, (byte)0x60, (byte)0x41, (byte)0x94, (byte)0xFD, (byte)0xBB, (byte)0x43, (byte)0x00,
			(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x49, (byte)0x45, (byte)0x4E, (byte)0x44, (byte)0xAE,
			(byte)0x42, (byte)0x60, (byte)0x82
		};

		// File: cross-yellow-8x8
		byte[]	ACTIVE_CROSS	=
		{
			(byte)0x89, (byte)0x50, (byte)0x4E, (byte)0x47, (byte)0x0D, (byte)0x0A, (byte)0x1A, (byte)0x0A,
			(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x0D, (byte)0x49, (byte)0x48, (byte)0x44, (byte)0x52,
			(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x08, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x08,
			(byte)0x08, (byte)0x06, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0xC4, (byte)0x0F, (byte)0xBE,
			(byte)0x8B, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x43, (byte)0x49, (byte)0x44, (byte)0x41,
			(byte)0x54, (byte)0x78, (byte)0xDA, (byte)0x63, (byte)0xF8, (byte)0xFF, (byte)0xA2, (byte)0xE1,
			(byte)0x03, (byte)0x10, (byte)0xF7, (byte)0xFE, (byte)0xFF, (byte)0xFF, (byte)0x9F, (byte)0x01,
			(byte)0x05, (byte)0xBF, (byte)0x68, (byte)0xE8, (byte)0x07, (byte)0xC9, (byte)0xC1, (byte)0x18,
			(byte)0xFF, (byte)0xC1, (byte)0x34, (byte)0xAA, (byte)0x24, (byte)0x48, (byte)0xAC, (byte)0x17,
			(byte)0x5D, (byte)0xA0, (byte)0x1F, (byte)0x5D, (byte)0x03, (byte)0x03, (byte)0x16, (byte)0x5D,
			(byte)0x28, (byte)0xA6, (byte)0x11, (byte)0xA9, (byte)0x00, (byte)0xAF, (byte)0x15, (byte)0x20,
			(byte)0x1F, (byte)0xE0, (byte)0x76, (byte)0x64, (byte)0x3F, (byte)0x03, (byte)0xD4, (byte)0x9B,
			(byte)0xFD, (byte)0x58, (byte)0xBC, (byte)0x09, (byte)0xD2, (byte)0xF8, (byte)0x01, (byte)0x00,
			(byte)0xC8, (byte)0xA5, (byte)0xC4, (byte)0xE5, (byte)0x5C, (byte)0x4E, (byte)0x36, (byte)0xDA,
			(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x49, (byte)0x45, (byte)0x4E, (byte)0x44,
			(byte)0xAE, (byte)0x42, (byte)0x60, (byte)0x82
		};

		// File: tabbedPanel-corner-l
		byte[]	CORNER_L	=
		{
			(byte)0x89, (byte)0x50, (byte)0x4E, (byte)0x47, (byte)0x0D, (byte)0x0A, (byte)0x1A, (byte)0x0A,
			(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x0D, (byte)0x49, (byte)0x48, (byte)0x44, (byte)0x52,
			(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x04, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x04,
			(byte)0x08, (byte)0x06, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0xA9, (byte)0xF1, (byte)0x9E,
			(byte)0x7E, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x38, (byte)0x49, (byte)0x44, (byte)0x41,
			(byte)0x54, (byte)0x78, (byte)0xDA, (byte)0x63, (byte)0xD8, (byte)0xB1, (byte)0x63, (byte)0x07,
			(byte)0xC3, (byte)0xCC, (byte)0x99, (byte)0x33, (byte)0x59, (byte)0x17, (byte)0x2F, (byte)0x5E,
			(byte)0x5C, (byte)0xBF, (byte)0x68, (byte)0xD1, (byte)0xA2, (byte)0xFB, (byte)0x30, (byte)0xCE,
			(byte)0x85, (byte)0x35, (byte)0x6B, (byte)0x56, (byte)0x7F, (byte)0x3D, (byte)0x76, (byte)0x6C,
			(byte)0xFF, (byte)0x7F, (byte)0x06, (byte)0x90, (byte)0x0C, (byte)0x88, (byte)0x73, (byte)0xE1,
			(byte)0xC2, (byte)0x89, (byte)0xFF, (byte)0x20, (byte)0xCC, (byte)0x00, (byte)0x52, (byte)0x06,
			(byte)0x92, (byte)0x81, (byte)0x09, (byte)0x00, (byte)0x00, (byte)0x75, (byte)0xEB, (byte)0x2C,
			(byte)0xFC, (byte)0x16, (byte)0x4B, (byte)0x0E, (byte)0xB8, (byte)0x00, (byte)0x00, (byte)0x00,
			(byte)0x00, (byte)0x49, (byte)0x45, (byte)0x4E, (byte)0x44, (byte)0xAE, (byte)0x42, (byte)0x60,
			(byte)0x82
		};

		// File: tabbedPanel-corner-r
		byte[]	CORNER_R	=
		{
			(byte)0x89, (byte)0x50, (byte)0x4E, (byte)0x47, (byte)0x0D, (byte)0x0A, (byte)0x1A, (byte)0x0A,
			(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x0D, (byte)0x49, (byte)0x48, (byte)0x44, (byte)0x52,
			(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x04, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x04,
			(byte)0x08, (byte)0x06, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0xA9, (byte)0xF1, (byte)0x9E,
			(byte)0x7E, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x39, (byte)0x49, (byte)0x44, (byte)0x41,
			(byte)0x54, (byte)0x78, (byte)0xDA, (byte)0x63, (byte)0x58, (byte)0xB4, (byte)0x68, (byte)0xD1,
			(byte)0xFD, (byte)0xC5, (byte)0x8B, (byte)0x17, (byte)0xD7, (byte)0xCF, (byte)0x9C, (byte)0x39,
			(byte)0x93, (byte)0x75, (byte)0xC7, (byte)0x8E, (byte)0x1D, (byte)0x0C, (byte)0x0C, (byte)0xC7,
			(byte)0x8E, (byte)0xED, (byte)0xFF, (byte)0xBF, (byte)0x66, (byte)0xCD, (byte)0xEA, (byte)0xAF,
			(byte)0x40, (byte)0xC1, (byte)0x0B, (byte)0x20, (byte)0x41, (byte)0x86, (byte)0x0B, (byte)0x17,
			(byte)0x4E, (byte)0xFC, (byte)0x07, (byte)0x61, (byte)0xA8, (byte)0x60, (byte)0x3D, (byte)0x5C,
			(byte)0x00, (byte)0xA4, (byte)0x12, (byte)0xA4, (byte)0x1D, (byte)0x00, (byte)0xA8, (byte)0x17,
			(byte)0x2C, (byte)0xFC, (byte)0x17, (byte)0x34, (byte)0x42, (byte)0xAB, (byte)0x00, (byte)0x00,
			(byte)0x00, (byte)0x00, (byte)0x49, (byte)0x45, (byte)0x4E, (byte)0x44, (byte)0xAE, (byte)0x42,
			(byte)0x60, (byte)0x82
		};

		// File: tabbedPanel-corner-ls
		byte[]	CORNER_LS	=
		{
			(byte)0x89, (byte)0x50, (byte)0x4E, (byte)0x47, (byte)0x0D, (byte)0x0A, (byte)0x1A, (byte)0x0A,
			(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x0D, (byte)0x49, (byte)0x48, (byte)0x44, (byte)0x52,
			(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x04, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x04,
			(byte)0x08, (byte)0x06, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0xA9, (byte)0xF1, (byte)0x9E,
			(byte)0x7E, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x3A, (byte)0x49, (byte)0x44, (byte)0x41,
			(byte)0x54, (byte)0x78, (byte)0xDA, (byte)0x63, (byte)0xD8, (byte)0xB1, (byte)0x63, (byte)0x07,
			(byte)0xC3, (byte)0x99, (byte)0x99, (byte)0x69, (byte)0xAC, (byte)0x0F, (byte)0x27, (byte)0x47,
			(byte)0xD5, (byte)0x3F, (byte)0x9C, (byte)0x12, (byte)0x75, (byte)0x1F, (byte)0xCC, (byte)0x79,
			(byte)0x34, (byte)0x25, (byte)0xFA, (byte)0xC2, (byte)0xD3, (byte)0x05, (byte)0x49, (byte)0x5F,
			(byte)0x3F, (byte)0x1C, (byte)0x2A, (byte)0xFF, (byte)0xCF, (byte)0x00, (byte)0x92, (byte)0x01,
			(byte)0x71, (byte)0xBE, (byte)0x5C, (byte)0x68, (byte)0xF8, (byte)0x0F, (byte)0xC2, (byte)0x0C,
			(byte)0x20, (byte)0x65, (byte)0x20, (byte)0x19, (byte)0x98, (byte)0x00, (byte)0x00, (byte)0x53,
			(byte)0x85, (byte)0x2B, (byte)0x74, (byte)0xEF, (byte)0x4F, (byte)0x8D, (byte)0x60, (byte)0x00,
			(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x49, (byte)0x45, (byte)0x4E, (byte)0x44, (byte)0xAE,
			(byte)0x42, (byte)0x60, (byte)0x82
		};

		// File: tabbedPanel-corner-rs
		byte[]	CORNER_RS	=
		{
			(byte)0x89, (byte)0x50, (byte)0x4E, (byte)0x47, (byte)0x0D, (byte)0x0A, (byte)0x1A, (byte)0x0A,
			(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x0D, (byte)0x49, (byte)0x48, (byte)0x44, (byte)0x52,
			(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x04, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x04,
			(byte)0x08, (byte)0x06, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0xA9, (byte)0xF1, (byte)0x9E,
			(byte)0x7E, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x3B, (byte)0x49, (byte)0x44, (byte)0x41,
			(byte)0x54, (byte)0x78, (byte)0xDA, (byte)0x63, (byte)0x78, (byte)0x38, (byte)0x25, (byte)0xEA,
			(byte)0xFE, (byte)0xC3, (byte)0xC9, (byte)0x51, (byte)0xF5, (byte)0x67, (byte)0x66, (byte)0xA6,
			(byte)0xB1, (byte)0xEE, (byte)0xD8, (byte)0xB1, (byte)0x83, (byte)0x81, (byte)0xE1, (byte)0xC3,
			(byte)0xA1, (byte)0xF2, (byte)0xFF, (byte)0x4F, (byte)0x17, (byte)0x24, (byte)0x7D, (byte)0x7D,
			(byte)0x34, (byte)0x25, (byte)0xFA, (byte)0x02, (byte)0x48, (byte)0x90, (byte)0xE1, (byte)0xCB,
			(byte)0x85, (byte)0x86, (byte)0xFF, (byte)0x20, (byte)0x0C, (byte)0x12, (byte)0x04, (byte)0xA9,
			(byte)0x84, (byte)0x0B, (byte)0x80, (byte)0x54, (byte)0x82, (byte)0xB4, (byte)0x03, (byte)0x00,
			(byte)0x81, (byte)0x59, (byte)0x2B, (byte)0x74, (byte)0x92, (byte)0xB8, (byte)0x1F, (byte)0x12,
			(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x49, (byte)0x45, (byte)0x4E, (byte)0x44,
			(byte)0xAE, (byte)0x42, (byte)0x60, (byte)0x82
		};
	}

	//==================================================================

}

//----------------------------------------------------------------------
