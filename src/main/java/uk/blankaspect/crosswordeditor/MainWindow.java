/*====================================================================*\

MainWindow.java

Main window class.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.crosswordeditor;

//----------------------------------------------------------------------


// IMPORTS


import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;

import java.awt.datatransfer.FlavorEvent;
import java.awt.datatransfer.FlavorListener;
import java.awt.datatransfer.UnsupportedFlavorException;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.io.File;
import java.io.IOException;

import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import uk.blankaspect.common.exception.AppException;

import uk.blankaspect.common.swing.menu.FCheckBoxMenuItem;
import uk.blankaspect.common.swing.menu.FMenu;
import uk.blankaspect.common.swing.menu.FMenuItem;

import uk.blankaspect.common.swing.misc.GuiUtils;

import uk.blankaspect.common.swing.tabbedpane.TabbedPane;

import uk.blankaspect.common.swing.transfer.DataImporter;

//----------------------------------------------------------------------


// MAIN WINDOW CLASS


class MainWindow
	extends JFrame
	implements ChangeListener, FlavorListener, MenuListener, MouseListener
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	public static final		int	DEFAULT_WIDTH	= 800;
	public static final		int	DEFAULT_HEIGHT	= 600;

////////////////////////////////////////////////////////////////////////
//  Enumerated types
////////////////////////////////////////////////////////////////////////


	// MENUS


	private enum Menu
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		FILE
		(
			"File",
			KeyEvent.VK_F
		)
		{
			protected void update()
			{
				updateAppCommands();
			}
		},

		EDIT
		(
			"Edit",
			KeyEvent.VK_E
		)
		{
			protected void update()
			{
				getMenu().setEnabled(App.INSTANCE.hasDocuments());
				updateDocumentCommands();
			}
		},

		CROSSWORD
		(
			"Crossword",
			KeyEvent.VK_C
		)
		{
			protected void update()
			{
				updateAppCommands();
			}
		},

		SOLUTION
		(
			"Solution",
			KeyEvent.VK_S
		)
		{
			protected void update()
			{
				getMenu().setEnabled(App.INSTANCE.hasDocuments());
				updateAppCommands();
				updateDocumentCommands();
			}
		},

		VIEW
		(
			"View",
			KeyEvent.VK_V
		)
		{
			protected void update()
			{
				getMenu().setEnabled(App.INSTANCE.hasDocuments());
				updateDocumentCommands();
			}
		},

		OPTIONS
		(
			"Options",
			KeyEvent.VK_O
		)
		{
			protected void update()
			{
				updateAppCommands();
			}
		};

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private Menu(String text,
					 int    keyCode)
		{
			menu = new FMenu(text, keyCode);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Class methods
	////////////////////////////////////////////////////////////////////

		private static void updateAppCommands()
		{
			App.INSTANCE.updateCommands();
		}

		//--------------------------------------------------------------

		private static void updateDocumentCommands()
		{
			CrosswordDocument document = App.INSTANCE.getDocument();
			if (document == null)
				CrosswordDocument.Command.setAllEnabled(false);
			else
				document.updateCommands();
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Abstract methods
	////////////////////////////////////////////////////////////////////

		protected abstract void update();

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		protected JMenu getMenu()
		{
			return menu;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	JMenu	menu;

	}

	//==================================================================


	// ERROR IDENTIFIERS


	private enum ErrorId
		implements AppException.IId
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		FILE_TRANSFER_NOT_SUPPORTED
		("File transfer is not supported."),

		ERROR_TRANSFERRING_DATA
		("An error occurred while transferring data.");

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private ErrorId(String message)
		{
			this.message = message;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : AppException.IId interface
	////////////////////////////////////////////////////////////////////

		public String getMessage()
		{
			return message;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	String	message;

	}

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Member classes : non-inner classes
////////////////////////////////////////////////////////////////////////


	// STATUS PANEL CLASS


	private static class StatusPanel
		extends AbstractStatusPanel
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		private static final	String	SOLUTION_STR	= "Solution";
		private static final	String	COMPLETE_STR	= "Complete";

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private StatusPanel()
		{
			// Call superclass constructor
			super(false);

			// Field: solution
			solutionField = new StatusField();
			add(solutionField);

			// Field: complete
			completeField = new StatusField();
			add(completeField);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		public void setSolution(boolean enabled)
		{
			solutionField.setText(enabled ? SOLUTION_STR : null);
		}

		//--------------------------------------------------------------

		public void setComplete(boolean enabled)
		{
			completeField.setText(enabled ? COMPLETE_STR : null);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	StatusField	solutionField;
		private	StatusField	completeField;

	}

	//==================================================================


	// CLOSE ACTION CLASS


	private static class CloseAction
		extends AbstractAction
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CloseAction()
		{
			putValue(Action.ACTION_COMMAND_KEY, "");
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : ActionListener interface
	////////////////////////////////////////////////////////////////////

		public void actionPerformed(ActionEvent event)
		{
			App.INSTANCE.closeDocument(Integer.parseInt(event.getActionCommand()));
		}

		//--------------------------------------------------------------

	}

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Member classes : inner classes
////////////////////////////////////////////////////////////////////////


	// MAIN PANEL CLASS


	private class MainPanel
		extends JPanel
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		private static final	int	VERTICAL_GAP	= 0;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private MainPanel()
		{
			// Lay out components explicitly
			setLayout(null);

			// Add components
			add(tabbedPanel);
			add(statusPanel);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		public Dimension getMinimumSize()
		{
			int width = tabbedPanel.getMinimumSize().width;
			int height = -VERTICAL_GAP;
			for (Component component : getComponents())
				height += component.getMinimumSize().height + VERTICAL_GAP;
			return new Dimension(width, height);
		}

		//--------------------------------------------------------------

		@Override
		public Dimension getPreferredSize()
		{
			int width = tabbedPanel.getPreferredSize().width;
			int height = -VERTICAL_GAP;
			for (Component component : getComponents())
				height += component.getPreferredSize().height + VERTICAL_GAP;
			return new Dimension(width, height);
		}

		//--------------------------------------------------------------

		@Override
		public void doLayout()
		{
			int width = getWidth();
			Dimension statusPanelSize = statusPanel.getPreferredSize();
			Dimension tabbedPanelSize = tabbedPanel.getFrameSize();

			int y = 0;
			tabbedPanel.setBounds(0, y, Math.max(tabbedPanelSize.width, width),
								  Math.max(tabbedPanelSize.height,
										   getHeight() - statusPanelSize.height - VERTICAL_GAP));

			y += tabbedPanel.getHeight() + VERTICAL_GAP;
			statusPanel.setBounds(0, y, Math.min(width, statusPanelSize.width), statusPanelSize.height);
		}

		//--------------------------------------------------------------

	}

	//==================================================================


	// FILE TRANSFER HANDLER CLASS


	private class FileTransferHandler
		extends TransferHandler
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		public FileTransferHandler()
		{
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		public boolean canImport(TransferHandler.TransferSupport support)
		{
			boolean supported = !support.isDrop() || ((support.getSourceDropActions() & COPY) == COPY);
			if (supported)
				supported = DataImporter.isFileList(support.getDataFlavors());
			if (support.isDrop() && supported)
				support.setDropAction(COPY);
			return supported;
		}

		//--------------------------------------------------------------

		@Override
		public boolean importData(TransferHandler.TransferSupport support)
		{
			if (canImport(support))
			{
				try
				{
					try
					{
						List<File> files = DataImporter.getFiles(support.getTransferable());
						if (!files.isEmpty())
						{
							toFront();
							AppCommand.IMPORT_FILES.putValue(AppCommand.Property.FILES, files);
							SwingUtilities.invokeLater(() -> AppCommand.IMPORT_FILES.execute());
							return true;
						}
					}
					catch (UnsupportedFlavorException e)
					{
						throw new AppException(ErrorId.FILE_TRANSFER_NOT_SUPPORTED);
					}
					catch (IOException e)
					{
						throw new AppException(ErrorId.ERROR_TRANSFERRING_DATA);
					}
				}
				catch (AppException e)
				{
					App.INSTANCE.showErrorMessage(App.SHORT_NAME, e);
				}
			}
			return false;
		}

		//--------------------------------------------------------------

	}

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	public MainWindow()
	{

		// Set icons
		setIconImages(Images.APP_ICON_IMAGES);


		//----  Menu bar

		JMenuBar menuBar = new JMenuBar();
		menuBar.setBorder(null);

		// File menu
		JMenu menu = Menu.FILE.menu;
		menu.addMenuListener(this);

		menu.add(new FMenuItem(AppCommand.CREATE_DOCUMENT, KeyEvent.VK_N));
		menu.add(new FMenuItem(AppCommand.OPEN_DOCUMENT, KeyEvent.VK_O));
		menu.add(new FMenuItem(AppCommand.REVERT_DOCUMENT, KeyEvent.VK_R));

		menu.addSeparator();

		menu.add(new FMenuItem(AppCommand.CLOSE_DOCUMENT, KeyEvent.VK_C));
		menu.add(new FMenuItem(AppCommand.CLOSE_ALL_DOCUMENTS, KeyEvent.VK_L));

		menu.addSeparator();

		menu.add(new FMenuItem(AppCommand.SAVE_DOCUMENT, KeyEvent.VK_S));
		menu.add(new FMenuItem(AppCommand.SAVE_DOCUMENT_AS, KeyEvent.VK_A));

		menu.addSeparator();

		menu.add(new FMenuItem(AppCommand.EXPORT_HTML_FILE, KeyEvent.VK_H));

		menu.addSeparator();

		menu.add(new FMenuItem(AppCommand.EXIT, KeyEvent.VK_X));

		menuBar.add(menu);

		// Edit menu
		menu = Menu.EDIT.menu;
		menu.addMenuListener(this);

		menu.add(new FMenuItem(CrosswordDocument.Command.UNDO, KeyEvent.VK_U));
		menu.add(new FMenuItem(CrosswordDocument.Command.REDO, KeyEvent.VK_R));
		menu.add(new FMenuItem(CrosswordDocument.Command.CLEAR_EDIT_LIST, KeyEvent.VK_L));

		menu.addSeparator();

		menu.add(new FMenuItem(CrosswordDocument.Command.EDIT_CLUE, KeyEvent.VK_C));
		menu.add(new FMenuItem(CrosswordDocument.Command.EDIT_GRID, KeyEvent.VK_G));
		menu.add(new FMenuItem(CrosswordDocument.Command.EDIT_TEXT_SECTIONS, KeyEvent.VK_T));
		menu.add(new FMenuItem(CrosswordDocument.Command.EDIT_INDICATIONS, KeyEvent.VK_I));

		menu.addSeparator();

		menu.add(new FMenuItem(CrosswordDocument.Command.COPY_CLUES_TO_CLIPBOARD, KeyEvent.VK_Y));
		menu.add(new FMenuItem(CrosswordDocument.Command.IMPORT_CLUES_FROM_CLIPBOARD, KeyEvent.VK_P));
		menu.add(new FMenuItem(CrosswordDocument.Command.CLEAR_CLUES, KeyEvent.VK_A));

		menu.addSeparator();

		menu.add(new FMenuItem(CrosswordDocument.Command.COPY_ENTRIES_TO_CLIPBOARD, KeyEvent.VK_N));
		menu.add(new FMenuItem(CrosswordDocument.Command.IMPORT_ENTRIES_FROM_CLIPBOARD, KeyEvent.VK_M));
		menu.add(new FMenuItem(CrosswordDocument.Command.CLEAR_ENTRIES, KeyEvent.VK_E));

		menu.addSeparator();

		menu.add(new FMenuItem(CrosswordDocument.Command.COPY_FIELD_NUMBERS_TO_CLIPBOARD,
							   KeyEvent.VK_F));
		menu.add(new FMenuItem(CrosswordDocument.Command.COPY_FIELD_IDS_TO_CLIPBOARD, KeyEvent.VK_D));

		menuBar.add(menu);

		// Crossword menu
		menu = Menu.CROSSWORD.menu;
		menu.addMenuListener(this);

		menu.add(new FMenuItem(AppCommand.CAPTURE_CROSSWORD, KeyEvent.VK_C));

		menuBar.add(menu);

		// Solution menu
		menu = Menu.SOLUTION.menu;
		menu.addMenuListener(this);

		menu.add(new FMenuItem(CrosswordDocument.Command.HIGHLIGHT_INCORRECT_ENTRIES, KeyEvent.VK_I));
		menu.add(new FMenuItem(CrosswordDocument.Command.SHOW_SOLUTION, KeyEvent.VK_S));

		menu.addSeparator();

		menu.add(new FMenuItem(CrosswordDocument.Command.SET_SOLUTION, KeyEvent.VK_E));
		menu.add(new FMenuItem(CrosswordDocument.Command.IMPORT_SOLUTION_FROM_CLIPBOARD,
							   KeyEvent.VK_I));
		menu.add(new FMenuItem(CrosswordDocument.Command.LOAD_SOLUTION, KeyEvent.VK_L));
		menu.add(new FMenuItem(CrosswordDocument.Command.CLEAR_SOLUTION, KeyEvent.VK_C));

		menu.addSeparator();

		menu.add(new FMenuItem(CrosswordDocument.Command.COPY_SOLUTION_TO_CLIPBOARD, KeyEvent.VK_Y));
		menu.add(new FMenuItem(AppCommand.CREATE_SOLUTION_DOCUMENT, KeyEvent.VK_D));

		menu.addSeparator();

		menu.add(new FMenuItem(CrosswordDocument.Command.EDIT_SOLUTION_PROPERTIES, KeyEvent.VK_P));

		menuBar.add(menu);

		// View menu
		menu = Menu.VIEW.menu;
		menu.addMenuListener(this);

		menu.add(new FCheckBoxMenuItem(CrosswordDocument.Command.TOGGLE_FIELD_NUMBERS, KeyEvent.VK_F));
		menu.add(new FCheckBoxMenuItem(CrosswordDocument.Command.TOGGLE_CLUES, KeyEvent.VK_C));

		menu.addSeparator();

		menu.add(new FMenuItem(CrosswordDocument.Command.RESIZE_WINDOW_TO_VIEW, KeyEvent.VK_R));

		menuBar.add(menu);

		// Options menu
		menu = Menu.OPTIONS.menu;
		menu.addMenuListener(this);

		menu.add(new FCheckBoxMenuItem(AppCommand.TOGGLE_SHOW_FULL_PATHNAMES, KeyEvent.VK_F));
		menu.add(new FMenuItem(AppCommand.MANAGE_FILE_ASSOCIATIONS, KeyEvent.VK_A));
		menu.add(new FMenuItem(AppCommand.EDIT_PREFERENCES, KeyEvent.VK_P));

		menuBar.add(menu);

		// Set menu bar
		setJMenuBar(menuBar);


		//----  Tabbed panel

		tabbedPanel = new TabbedPane();
		tabbedPanel.setIgnoreCase(true);
		tabbedPanel.addChangeListener(this);
		tabbedPanel.addMouseListener(this);


		//----  Status panel

		statusPanel = new StatusPanel();


		//----  Main panel

		MainPanel mainPanel = new MainPanel();


		//----  Window

		// Set content pane
		setContentPane(mainPanel);

		// Set transfer handler
		setTransferHandler(new FileTransferHandler());

		// Dispose of window explicitly
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

		// Handle window closing
		addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent event)
			{
				AppCommand.EXIT.execute();
			}
		});

		// Respond to changes to data flavours on system clipboard
		getToolkit().getSystemClipboard().addFlavorListener(this);

		// Resize window to its preferred size
		pack();

		// Set mininum size of window
		setMinimumSize(getPreferredSize());

		// Set location of window
		AppConfig config = AppConfig.INSTANCE;
		if (config.isMainWindowLocation())
			setLocation(GuiUtils.getLocationWithinScreen(this, config.getMainWindowLocation()));

		// Set size of window
		Dimension size = config.getMainWindowSize();
		if ((size != null) && (size.width > 0) && (size.height > 0))
			setSize(size);

		// Update title, menus and status
		updateAll();

		// Make window visible
		setVisible(true);

	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : ChangeListener interface
////////////////////////////////////////////////////////////////////////

	public void stateChanged(ChangeEvent event)
	{
		if (event.getSource() == tabbedPanel)
		{
			if (isVisible())
				updateAll();
		}
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : FlavorListener interface
////////////////////////////////////////////////////////////////////////

	public void flavorsChanged(FlavorEvent event)
	{
		CrosswordDocument.Command.IMPORT_SOLUTION_FROM_CLIPBOARD.
															setEnabled(App.INSTANCE.hasDocuments() &&
																	   Utils.clipboardHasText());
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : MenuListener interface
////////////////////////////////////////////////////////////////////////

	public void menuCanceled(MenuEvent event)
	{
		// do nothing
	}

	//------------------------------------------------------------------

	public void menuDeselected(MenuEvent event)
	{
		// do nothing
	}

	//------------------------------------------------------------------

	public void menuSelected(MenuEvent event)
	{
		Object eventSource = event.getSource();
		for (Menu menu : Menu.values())
		{
			if (eventSource == menu.menu)
				menu.update();
		}
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
		showContextMenu(event);
	}

	//------------------------------------------------------------------

	public void mouseReleased(MouseEvent event)
	{
		showContextMenu(event);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	public int getTabIndex()
	{
		return tabbedPanel.getSelectedIndex();
	}

	//------------------------------------------------------------------

	public void addView(String        title,
						String        tooltipText,
						CrosswordView view)
	{
		tabbedPanel.addComponent(title, new CloseAction(), view);
		int index = tabbedPanel.getNumTabs() - 1;
		tabbedPanel.setTooltipText(index, tooltipText);
		tabbedPanel.setSelectedIndex(index);
	}

	//------------------------------------------------------------------

	public void removeView(int index)
	{
		tabbedPanel.removeComponent(index);
	}

	//------------------------------------------------------------------

	public void setView(int           index,
						CrosswordView view)
	{
		tabbedPanel.setComponent(index, view);
	}

	//------------------------------------------------------------------

	public void selectView(int index)
	{
		tabbedPanel.setSelectedIndex(index);
	}

	//------------------------------------------------------------------

	public void setTabText(int    index,
						   String title,
						   String tooltipText)
	{
		tabbedPanel.setTitle(index, title);
		tabbedPanel.setTooltipText(index, tooltipText);
	}

	//------------------------------------------------------------------

	public boolean isMaximised()
	{
		return ((getExtendedState() & Frame.MAXIMIZED_BOTH) == Frame.MAXIMIZED_BOTH);
	}

	//------------------------------------------------------------------

	public void resize()
	{
		if (!isMaximised())
			pack();
	}

	//------------------------------------------------------------------

	public void updateAll()
	{
		updateTitle();
		updateMenus();
		updateStatus();
	}

	//------------------------------------------------------------------

	private void updateTitle()
	{
		CrosswordDocument document = App.INSTANCE.getDocument();
		boolean fullPathname = AppConfig.INSTANCE.isShowFullPathnames();
		setTitle((document == null) ? App.LONG_NAME + " " + App.INSTANCE.getVersionString()
									: App.SHORT_NAME + " - " + document.getTitleString(fullPathname));
	}

	//------------------------------------------------------------------

	private void updateMenus()
	{
		for (Menu menu : Menu.values())
			menu.update();
	}

	//------------------------------------------------------------------

	private void updateStatus()
	{
		CrosswordDocument document = App.INSTANCE.getDocument();
		statusPanel.setSolution((document != null) && document.getGrid().hasSolution());
		statusPanel.setComplete((document != null) && document.getGrid().isEntriesComplete());
	}

	//------------------------------------------------------------------

	private void showContextMenu(MouseEvent event)
	{
		if (event.isPopupTrigger())
		{
			// Create context menu
			if (contextMenu == null)
			{
				contextMenu = new JPopupMenu();

				contextMenu.add(new FMenuItem(AppCommand.CREATE_DOCUMENT));
				contextMenu.add(new FMenuItem(AppCommand.OPEN_DOCUMENT));

				contextMenu.addSeparator();

				contextMenu.add(new FMenuItem(AppCommand.CAPTURE_CROSSWORD));
			}

			// Update commands for menu items
			App.INSTANCE.updateCommands();

			// Display menu
			contextMenu.show(event.getComponent(), event.getX(), event.getY());
		}
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private	TabbedPane	tabbedPanel;
	private	StatusPanel	statusPanel;
	private	JPopupMenu	contextMenu;

}

//----------------------------------------------------------------------
