/*====================================================================*\

StringSelectionDialog.java

String selection dialog class.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.crosswordeditor;

//----------------------------------------------------------------------


// IMPORTS


import java.awt.Component;
import java.awt.Dialog;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Window;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import uk.blankaspect.common.string.StringUtils;

import uk.blankaspect.ui.swing.action.KeyAction;

import uk.blankaspect.ui.swing.button.FButton;

import uk.blankaspect.ui.swing.combobox.FComboBox;

import uk.blankaspect.ui.swing.label.FLabel;

import uk.blankaspect.ui.swing.list.SingleSelectionList;

import uk.blankaspect.ui.swing.misc.GuiUtils;

import uk.blankaspect.ui.swing.textfield.FTextField;

//----------------------------------------------------------------------


// STRING SELECTION DIALOG CLASS


class StringSelectionDialog
	extends JDialog
	implements ActionListener, ChangeListener, DocumentListener, ListSelectionListener
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	private static final	int	SELECTION_LIST_NUM_COLUMNS	= 32;
	private static final	int	SELECTION_LIST_NUM_ROWS		= 12;

	private static final	int	ELEMENT_FIELD_NUM_COLUMNS	= 12;

	private static final	int	MODIFIERS_MASK	= ActionEvent.ALT_MASK | ActionEvent.META_MASK |
															ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK;

	private static final	String	ADD_STR				= "Add";
	private static final	String	EDIT_STR			= "Edit";
	private static final	String	DELETE_STR			= "Delete";
	private static final	String	DELETE_MESSAGE_STR	= "Do you want to delete the selected ";

	// Commands
	private interface Command
	{
		String	ADD		= "add";
		String	EDIT	= SingleSelectionList.Command.EDIT_ELEMENT;
		String	DELETE	= "delete";
		String	ACCEPT	= "accept";
		String	CLOSE	= "close";
	}

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	private StringSelectionDialog(Window       owner,
								  String       titleStr,
								  String       elementName,
								  StringList   strings,
								  List<String> candidates)
	{

		// Call superclass constructor
		super(owner, titleStr, Dialog.ModalityType.APPLICATION_MODAL);

		// Set icons
		setIconImages(owner.getIconImages());

		// Initialise instance variables
		this.elementName = elementName;


		//----  Selection list

		selectionList = new SingleSelectionList<>(SELECTION_LIST_NUM_COLUMNS, SELECTION_LIST_NUM_ROWS,
												  AppFont.MAIN.getFont(), strings);
		selectionList.setRowHeight(selectionList.getRowHeight() + 2);
		selectionList.addActionListener(this);
		selectionList.addListSelectionListener(this);

		// Scroll pane: selection list
		selectionListScrollPane = new JScrollPane(selectionList, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
												  JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		selectionListScrollPane.getVerticalScrollBar().getModel().addChangeListener(this);
		selectionListScrollPane.getVerticalScrollBar().setFocusable(false);

		selectionList.setViewport(selectionListScrollPane.getViewport());


		//----  Font name panel

		GridBagLayout gridBag = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();

		JPanel fontNamePanel = new JPanel(gridBag);
		GuiUtils.setPaddedLineBorder(fontNamePanel);

		int gridY = 0;

		// Label: element
		JLabel elementLabel = new FLabel(StringUtils.firstCharToUpperCase(elementName));

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(elementLabel, gbc);
		fontNamePanel.add(elementLabel);

		// Field or combo box: element
		JComponent elementComponent = null;
		if (candidates == null)
		{
			elementField = new FTextField(ELEMENT_FIELD_NUM_COLUMNS);
			elementField.addActionListener(this);
			elementComponent = elementField;
		}
		else
		{
			elementComboBox = new FComboBox<>(candidates);
			elementComboBox.setEditable(true);
			elementComboBox.setSelectedItem(null);
			elementComboBox.getEditor().addActionListener(this);
			elementComponent = elementComboBox;
		}
		getElementField().getDocument().addDocumentListener(this);

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(elementComponent, gbc);
		fontNamePanel.add(elementComponent);


		//----  List button panel

		JPanel listButtonPanel = new JPanel(new GridLayout(0, 1, 0, 8));
		listButtonPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

		// Button: add
		addButton = new FButton(ADD_STR);
		addButton.setMnemonic(KeyEvent.VK_A);
		addButton.setActionCommand(Command.ADD);
		addButton.addActionListener(this);
		listButtonPanel.add(addButton);

		// Button: edit
		editButton = new FButton(EDIT_STR);
		editButton.setMnemonic(KeyEvent.VK_E);
		editButton.setActionCommand(Command.EDIT);
		editButton.addActionListener(this);
		listButtonPanel.add(editButton);

		// Button: delete
		deleteButton = new FButton(DELETE_STR + AppConstants.ELLIPSIS_STR);
		deleteButton.setMnemonic(KeyEvent.VK_D);
		deleteButton.setActionCommand(Command.DELETE);
		deleteButton.addActionListener(this);
		listButtonPanel.add(deleteButton);


		//----  Close button panel

		JPanel closeButtonPanel = new JPanel(new GridLayout(1, 0, 8, 0));
		closeButtonPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

		// Button: OK
		JButton okButton = new FButton(AppConstants.OK_STR);
		okButton.setActionCommand(Command.ACCEPT);
		okButton.addActionListener(this);
		closeButtonPanel.add(okButton);

		// Button: cancel
		JButton cancelButton = new FButton(AppConstants.CANCEL_STR);
		cancelButton.setActionCommand(Command.CLOSE);
		cancelButton.addActionListener(this);
		closeButtonPanel.add(cancelButton);


		//----  Control panel

		JPanel controlPanel = new JPanel(gridBag);

		gridY = 0;

		gbc.gridx = 0;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.NORTH;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(0, 0, 0, 0);
		gridBag.setConstraints(fontNamePanel, gbc);
		controlPanel.add(fontNamePanel);

		gbc.gridx = 0;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.NORTH;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(4, 0, 0, 0);
		gridBag.setConstraints(listButtonPanel, gbc);
		controlPanel.add(listButtonPanel);

		gbc.gridx = 0;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 1.0;
		gbc.anchor = GridBagConstraints.SOUTH;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(16, 0, 4, 0);
		gridBag.setConstraints(closeButtonPanel, gbc);
		controlPanel.add(closeButtonPanel);


		//----  Main panel

		JPanel mainPanel = new JPanel(gridBag);
		mainPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.NORTH;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(0, 0, 0, 0);
		gridBag.setConstraints(selectionListScrollPane, gbc);
		mainPanel.add(selectionListScrollPane);

		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.NORTH;
		gbc.fill = GridBagConstraints.VERTICAL;
		gbc.insets = new Insets(0, 3, 0, 0);
		gridBag.setConstraints(controlPanel, gbc);
		mainPanel.add(controlPanel);

		// Add commands to action map
		KeyAction.create(mainPanel, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT,
						 KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), Command.CLOSE, this);

		// Update buttons
		updateButtons();


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

		// Show dialog
		setVisible(true);

	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	public static StringList showDialog(Component    parent,
										String       titleStr,
										String       elementName,
										StringList   strings,
										List<String> candidates)
	{
		return new StringSelectionDialog(GuiUtils.getWindow(parent), titleStr, elementName, strings, candidates)
																										.getStrings();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : ActionListener interface
////////////////////////////////////////////////////////////////////////

	public void actionPerformed(ActionEvent event)
	{
		String command = event.getActionCommand();

		if (command.equals(Command.ADD) || (event.getSource() == getElementField()))
			onAdd();

		else if (command.equals(Command.EDIT))
			onEdit();

		else if (command.equals(Command.DELETE))
		{
			if ((event.getModifiers() & MODIFIERS_MASK) == ActionEvent.SHIFT_MASK)
				onDelete();
			else
				onConfirmDelete();
		}

		else if (command.equals(SingleSelectionList.Command.DELETE_ELEMENT))
			onConfirmDelete();

		else if (command.equals(SingleSelectionList.Command.DELETE_EX_ELEMENT))
			onDelete();

		else if (command.equals(SingleSelectionList.Command.MOVE_ELEMENT_UP))
			onMoveUp();

		else if (command.equals(SingleSelectionList.Command.MOVE_ELEMENT_DOWN))
			onMoveDown();

		else if (command.equals(SingleSelectionList.Command.DRAG_ELEMENT))
			onMove();

		if (command.equals(Command.ACCEPT))
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
		if (!selectionListScrollPane.getVerticalScrollBar().getValueIsAdjusting() &&
			 !selectionList.isDragging())
			selectionList.snapViewPosition();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : DocumentListener interface
////////////////////////////////////////////////////////////////////////

	public void changedUpdate(DocumentEvent event)
	{
		// do nothing
	}

	//------------------------------------------------------------------

	public void insertUpdate(DocumentEvent event)
	{
		updateButtons();
	}

	//------------------------------------------------------------------

	public void removeUpdate(DocumentEvent event)
	{
		updateButtons();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : ListSelectionListener interface
////////////////////////////////////////////////////////////////////////

	public void valueChanged(ListSelectionEvent event)
	{
		if (!event.getValueIsAdjusting())
			updateButtons();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	private StringList getStrings()
	{
		return (accepted ? new StringList(selectionList.getElements()) : null);
	}

	//------------------------------------------------------------------

	private JTextField getElementField()
	{
		return ((elementField == null) ? (JTextField)elementComboBox.getEditor().getEditorComponent()
									   : elementField);
	}

	//------------------------------------------------------------------

	private void updateButtons()
	{
		addButton.setEnabled(getElementField().getDocument().getLength() > 0);
		editButton.setEnabled(selectionList.isSelection());
		deleteButton.setEnabled(selectionList.isSelection());
	}

	//------------------------------------------------------------------

	private void onAdd()
	{
		String name = getElementField().getText();
		int index = selectionList.findIndex(name);
		if (index < 0)
		{
			selectionList.addElement(name);
			updateButtons();
		}
		else
			selectionList.setSelectedIndex(index);
	}

	//------------------------------------------------------------------

	private void onEdit()
	{
		String text = selectionList.getSelectedElement();
		if (elementComboBox == null)
			elementField.setText(text);
		else
			elementComboBox.setSelectedValue(text);
		updateButtons();
	}

	//------------------------------------------------------------------

	private void onConfirmDelete()
	{
		String[] optionStrs = Utils.getOptionStrings(DELETE_STR);
		if (JOptionPane.showOptionDialog(this, DELETE_MESSAGE_STR + elementName + "?",
										 DELETE_STR + " " + elementName, JOptionPane.OK_CANCEL_OPTION,
										 JOptionPane.QUESTION_MESSAGE, null, optionStrs,
										 optionStrs[1]) == JOptionPane.OK_OPTION)
			onDelete();
	}

	//------------------------------------------------------------------

	private void onDelete()
	{
		selectionList.removeElement(selectionList.getSelectedIndex());
		updateButtons();
	}

	//------------------------------------------------------------------

	private void onMoveUp()
	{
		int index = selectionList.getSelectedIndex();
		selectionList.moveElement(index, index - 1);
	}

	//------------------------------------------------------------------

	private void onMoveDown()
	{
		int index = selectionList.getSelectedIndex();
		selectionList.moveElement(index, index + 1);
	}

	//------------------------------------------------------------------

	private void onMove()
	{
		int fromIndex = selectionList.getSelectedIndex();
		int toIndex = selectionList.getDragEndIndex();
		if (toIndex > fromIndex)
			--toIndex;
		selectionList.moveElement(fromIndex, toIndex);
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
		location = getLocation();
		setVisible(false);
		dispose();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class variables
////////////////////////////////////////////////////////////////////////

	private static	Point	location;

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private	boolean						accepted;
	private	String						elementName;
	private	SingleSelectionList<String>	selectionList;
	private	JScrollPane					selectionListScrollPane;
	private	JTextField					elementField;
	private	FComboBox<String>			elementComboBox;
	private	JButton						addButton;
	private	JButton						editButton;
	private	JButton						deleteButton;

}

//----------------------------------------------------------------------
