/*====================================================================*\

GridDialog.java

Grid dialog box class.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.crosswordeditor;

//----------------------------------------------------------------------


// IMPORTS


import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Window;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import uk.blankaspect.common.gui.FButton;
import uk.blankaspect.common.gui.FCheckBox;
import uk.blankaspect.common.gui.FComboBox;
import uk.blankaspect.common.gui.FLabel;
import uk.blankaspect.common.gui.FMenuItem;
import uk.blankaspect.common.gui.GuiUtils;
import uk.blankaspect.common.gui.TextRendering;

import uk.blankaspect.common.misc.KeyAction;

//----------------------------------------------------------------------


// GRID DIALOG BOX CLASS


class GridDialog
	extends JDialog
	implements ActionListener, ChangeListener, MouseListener
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	private static final	String	TITLE_STR				= "Grid";
	private static final	String	SYMMETRY_STR			= "Symmetry";
	private static final	String	FULLY_INTERSECTING_STR	= "Highlight fully intersecting fields";
	private static final	String	UNDO_STR				= "Undo";
	private static final	String	REDO_STR				= "Redo";

	// Commands
	private interface Command
	{
		String	SELECT_SYMMETRY						= "selectSymmetry";
		String	TOGGLE_HIGHLIGHT_FULLY_INTERSECTING	= "toggleHighlightFullyIntersecting";
		String	UNDO								= "undo";
		String	REDO								= "redo";
		String	SHOW_CONTEXT_MENU					= "showContextMenu";
		String	ACCEPT								= "accept";
		String	CLOSE								= "close";
	}

	private static final	Map<String, CommandAction>	COMMANDS;

	private static final	KeyAction.KeyCommandPair[]	KEY_COMMANDS	=
	{
		new KeyAction.KeyCommandPair
		(
			KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.CTRL_DOWN_MASK),
			Command.UNDO
		),
		new KeyAction.KeyCommandPair
		(
			KeyStroke.getKeyStroke(KeyEvent.VK_Y, KeyEvent.CTRL_DOWN_MASK),
			Command.REDO
		),
		new KeyAction.KeyCommandPair
		(
			KeyStroke.getKeyStroke(KeyEvent.VK_CONTEXT_MENU, 0),
			Command.SHOW_CONTEXT_MENU
		),
		new KeyAction.KeyCommandPair
		(
			KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
			Command.CLOSE
		)
	};

////////////////////////////////////////////////////////////////////////
//  Member classes : non-inner classes
////////////////////////////////////////////////////////////////////////


	// NUMBER FIELD CLASS


	private static class NumberField
		extends JComponent
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		private static final	int	VERTICAL_MARGIN		= 2;
		private static final	int	HORIZONTAL_MARGIN	= 6;

		private static final	Color	TEXT_COLOUR			= Color.BLACK;
		private static final	Color	BACKGROUND_COLOUR	= new Color(236, 244, 236);
		private static final	Color	BORDER_COLOUR		= new Color(192, 196, 192);

		private static final	String	PROTOTYPE_STR	= "000";

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private NumberField()
		{
			// Set font
			AppFont.TEXT_FIELD.apply(this);

			// Initialise instance fields
			numFields = -1;

			// Set preferred size
			FontMetrics fontMetrics = getFontMetrics(getFont());
			int width = 2 * HORIZONTAL_MARGIN + fontMetrics.stringWidth(PROTOTYPE_STR);
			int height = 2 * VERTICAL_MARGIN + fontMetrics.getAscent() + fontMetrics.getDescent();
			setPreferredSize(new Dimension(width, height));

			// Set attributes
			setEnabled(false);
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

			// Draw background
			gr.setColor(BACKGROUND_COLOUR);
			gr.fillRect(0, 0, width, height);

			// Draw text
			if (numFields >= 0)
			{
				// Set rendering hints for text antialiasing and fractional metrics
				TextRendering.setHints((Graphics2D)gr);

				// Draw text
				String text = Integer.toString(numFields);
				FontMetrics fontMetrics = gr.getFontMetrics();
				int x = width - HORIZONTAL_MARGIN - fontMetrics.stringWidth(text);
				gr.setColor(TEXT_COLOUR);
				gr.drawString(text, x, VERTICAL_MARGIN + fontMetrics.getAscent());
			}

			// Draw border
			gr.setColor(BORDER_COLOUR);
			gr.drawRect(0, 0, width - 1, height - 1);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		private void setNumFields(int numFields)
		{
			if (this.numFields != numFields)
			{
				this.numFields = numFields;
				repaint();
			}
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance fields
	////////////////////////////////////////////////////////////////////

		private	int	numFields;

	}

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Member classes : inner classes
////////////////////////////////////////////////////////////////////////


	// COMMAND ACTION CLASS


	private static class CommandAction
		extends AbstractAction
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CommandAction(String command,
							  String text)
		{
			// Call superclass constructor
			super(text);

			// Set action properties
			putValue(Action.ACTION_COMMAND_KEY, command);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : ActionListener interface
	////////////////////////////////////////////////////////////////////

		public void actionPerformed(ActionEvent event)
		{
			listener.actionPerformed(event);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance fields
	////////////////////////////////////////////////////////////////////

		private	ActionListener	listener;

	}

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	private GridDialog(Window owner,
					   Grid   grid)
	{

		// Call superclass constructor
		super(owner, TITLE_STR, Dialog.ModalityType.APPLICATION_MODAL);

		// Set icons
		setIconImages(owner.getIconImages());

		// Set action listener in commands
		for (String commandKey : COMMANDS.keySet())
			COMMANDS.get(commandKey).listener = this;


		//----  Control panel

		GridBagLayout gridBag = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();

		JPanel controlPanel = new JPanel(gridBag);
		GuiUtils.setPaddedLineBorder(controlPanel);

		int gridY = 0;

		// Label: symmetry
		JLabel symmetryLabel = new FLabel(SYMMETRY_STR);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(symmetryLabel, gbc);
		controlPanel.add(symmetryLabel);

		// Combo box: symmetry
		symmetryComboBox = new FComboBox<>();
		for (Grid.Symmetry symmetry : Grid.Symmetry.values())
		{
			if (symmetry.supportsDimensions(grid.getNumColumns(), grid.getNumRows()))
				symmetryComboBox.addItem(symmetry);
		}
		symmetryComboBox.setSelectedValue(grid.getSymmetry());
		symmetryComboBox.setActionCommand(Command.SELECT_SYMMETRY);
		symmetryComboBox.addActionListener(this);

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(symmetryComboBox, gbc);
		controlPanel.add(symmetryComboBox);

		// Check box: highlight fully intersecting fields
		highlightFullyIntersectingCheckBox = new FCheckBox(FULLY_INTERSECTING_STR);
		highlightFullyIntersectingCheckBox.setSelected(highlightFullyIntersecting);
		highlightFullyIntersectingCheckBox.setActionCommand(Command.TOGGLE_HIGHLIGHT_FULLY_INTERSECTING);
		highlightFullyIntersectingCheckBox.addActionListener(this);

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(highlightFullyIntersectingCheckBox, gbc);
		controlPanel.add(highlightFullyIntersectingCheckBox);


		//----  Grid panel

		JPanel gridOuterPanel = new JPanel(gridBag);
		GuiUtils.setPaddedLineBorder(gridOuterPanel);

		gridPanel = grid.getSeparator().createGridPanel(grid);
		gridPanel.addChangeListener(this);
		gridPanel.addMouseListener(this);

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.NORTH;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(0, 0, 0, 0);
		gridBag.setConstraints(gridPanel, gbc);
		gridOuterPanel.add(gridPanel);


		//----  Number of fields panel

		JPanel numFieldsPanel = new JPanel(gridBag);
		GuiUtils.setPaddedLineBorder(numFieldsPanel);

		numFieldsFields = new EnumMap<>(Direction.class);
		int gridX = 0;
		for (Direction direction : Direction.DEFINED_DIRECTIONS)
		{
			// Label: direction
			JLabel directionLabel = new FLabel(direction.toString());

			gbc.gridx = gridX++;
			gbc.gridy = 0;
			gbc.gridwidth = 1;
			gbc.gridheight = 1;
			gbc.weightx = 0.0;
			gbc.weighty = 0.0;
			gbc.anchor = GridBagConstraints.LINE_START;
			gbc.fill = GridBagConstraints.NONE;
			gbc.insets = new Insets(0, (gridX == 0) ? 0 : 16, 0, 0);
			gridBag.setConstraints(directionLabel, gbc);
			numFieldsPanel.add(directionLabel);

			// Field: number of fields
			NumberField field = new NumberField();
			numFieldsFields.put(direction, field);

			gbc.gridx = gridX++;
			gbc.gridy = 0;
			gbc.gridwidth = 1;
			gbc.gridheight = 1;
			gbc.weightx = 0.0;
			gbc.weighty = 0.0;
			gbc.anchor = GridBagConstraints.LINE_START;
			gbc.fill = GridBagConstraints.NONE;
			gbc.insets = new Insets(0, 6, 0, 0);
			gridBag.setConstraints(field, gbc);
			numFieldsPanel.add(field);
		}
		updateHighlighting();
		updateNumFields();


		//----  Button panel

		JPanel buttonPanel = new JPanel(new GridLayout(1, 0, 8, 0));
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(3, 12, 3, 12));

		// Button: OK
		JButton okButton = new FButton(AppConstants.OK_STR);
		okButton.setActionCommand(Command.ACCEPT);
		okButton.addActionListener(this);
		buttonPanel.add(okButton);

		// Button: cancel
		JButton cancelButton = new FButton(AppConstants.CANCEL_STR);
		cancelButton.setActionCommand(Command.CLOSE);
		cancelButton.addActionListener(this);
		buttonPanel.add(cancelButton);


		//----  Main panel

		JPanel mainPanel = new JPanel(gridBag);
		mainPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		mainPanel.addMouseListener(this);

		gridY = 0;

		gbc.gridx = 0;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.NORTH;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(0, 0, 0, 0);
		gridBag.setConstraints(controlPanel, gbc);
		mainPanel.add(controlPanel);

		gbc.gridx = 0;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.NORTH;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(3, 0, 0, 0);
		gridBag.setConstraints(gridOuterPanel, gbc);
		mainPanel.add(gridOuterPanel);

		gbc.gridx = 0;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.NORTH;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(3, 0, 0, 0);
		gridBag.setConstraints(numFieldsPanel, gbc);
		mainPanel.add(numFieldsPanel);

		gbc.gridx = 0;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.NORTH;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(3, 0, 0, 0);
		gridBag.setConstraints(buttonPanel, gbc);
		mainPanel.add(buttonPanel);

		// Add commands to action map
		KeyAction.create(mainPanel, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, this, KEY_COMMANDS);


		//----  Window

		// Set content pane
		setContentPane(mainPanel);

		// Dispose of window explicitly
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

		// Handle window closing
		addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent event)
			{
				onClose();
			}
		});

		// Prevent dialog from being resized
		setResizable(false);

		// Resize dialog to its preferred size
		pack();

		// Set location of dialog box
		if (location == null)
			location = GuiUtils.getComponentLocation(this, owner);
		setLocation(location);

		// Set default button
		getRootPane().setDefaultButton(okButton);

		// Show dialog
		setVisible(true);

	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	public static Grid showDialog(Component         parent,
								  CrosswordDocument document)
	{
		return new GridDialog(GuiUtils.getWindow(parent), document.getGrid()).getGrid();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : ActionListener interface
////////////////////////////////////////////////////////////////////////

	public void actionPerformed(ActionEvent event)
	{
		String command = event.getActionCommand();

		if (command.equals(Command.SELECT_SYMMETRY))
			onSelectSymmetry();

		else if (command.equals(Command.TOGGLE_HIGHLIGHT_FULLY_INTERSECTING))
			onToggleHighlightFullyIntersecting();

		else if (command.equals(Command.UNDO))
			onUndo();

		else if (command.equals(Command.REDO))
			onRedo();

		else if (command.equals(Command.SHOW_CONTEXT_MENU))
			onShowContextMenu();

		else if (command.equals(Command.ACCEPT))
			onAccept();

		else if (command.equals(Command.CLOSE))
			onClose();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : ChangeListener interface
////////////////////////////////////////////////////////////////////////

	public void stateChanged(ChangeEvent event)
	{
		symmetryComboBox.setSelectedValue(gridPanel.getGrid().getSymmetry());
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

	private Grid getGrid()
	{
		return (accepted ? gridPanel.getGrid() : null);
	}

	//------------------------------------------------------------------

	private void updateNumFields()
	{
		for (Direction direction : numFieldsFields.keySet())
			numFieldsFields.get(direction).setNumFields(gridPanel.getNumFields(direction));
	}

	//------------------------------------------------------------------

	private void updateHighlighting()
	{
		gridPanel.setHighlightFullyIntersecting(highlightFullyIntersectingCheckBox.isSelected());
	}

	//------------------------------------------------------------------

	private void showContextMenu(MouseEvent event)
	{
		if ((event == null) || event.isPopupTrigger())
		{
			// Create context menu
			if (contextMenu == null)
			{
				contextMenu = new JPopupMenu();

				contextMenu.add(new FMenuItem(COMMANDS.get(Command.UNDO)));
				contextMenu.add(new FMenuItem(COMMANDS.get(Command.REDO)));
			}

			// Update commands
			Grid grid = gridPanel.getGrid();
			COMMANDS.get(Command.UNDO).setEnabled(grid.canUndoEdit());
			COMMANDS.get(Command.REDO).setEnabled(grid.canRedoEdit());

			// Display menu
			if (event == null)
				contextMenu.show(getContentPane(), 0, 0);
			else
				contextMenu.show(event.getComponent(), event.getX(), event.getY());
		}
	}

	//------------------------------------------------------------------

	private void onSelectSymmetry()
	{
		gridPanel.setSymmetry(symmetryComboBox.getSelectedValue());
		updateNumFields();
	}

	//------------------------------------------------------------------

	private void onToggleHighlightFullyIntersecting()
	{
		updateHighlighting();
	}

	//------------------------------------------------------------------

	private void onUndo()
	{
		gridPanel.undoEdit();
	}

	//------------------------------------------------------------------

	private void onRedo()
	{
		gridPanel.redoEdit();
	}

	//------------------------------------------------------------------

	private void onShowContextMenu()
	{
		showContextMenu(null);
	}

	//------------------------------------------------------------------

	private void onAccept()
	{
		accepted = true;
		onClose();
	}

	//------------------------------------------------------------------

	private void onClose()
	{
		highlightFullyIntersecting = highlightFullyIntersectingCheckBox.isSelected();
		location = getLocation();
		setVisible(false);
		dispose();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class fields
////////////////////////////////////////////////////////////////////////

	private static	boolean	highlightFullyIntersecting;
	private static	Point	location;

////////////////////////////////////////////////////////////////////////
//  Static initialiser
////////////////////////////////////////////////////////////////////////

	static
	{
		COMMANDS = new HashMap<>();
		COMMANDS.put(Command.UNDO, new CommandAction(Command.UNDO, UNDO_STR));
		COMMANDS.put(Command.REDO, new CommandAction(Command.REDO, REDO_STR));
	}

////////////////////////////////////////////////////////////////////////
//  Instance fields
////////////////////////////////////////////////////////////////////////

	private	boolean						accepted;
	private	FComboBox<Grid.Symmetry>	symmetryComboBox;
	private	JCheckBox					highlightFullyIntersectingCheckBox;
	private	GridPanel					gridPanel;
	private	Map<Direction, NumberField>	numFieldsFields;
	private	JPopupMenu					contextMenu;

}

//----------------------------------------------------------------------
