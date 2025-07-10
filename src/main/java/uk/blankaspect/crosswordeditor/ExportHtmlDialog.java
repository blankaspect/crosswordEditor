/*====================================================================*\

ExportHtmlDialog.java

Export HTML dialog class.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.crosswordeditor;

//----------------------------------------------------------------------


// IMPORTS


import java.awt.Component;
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

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import uk.blankaspect.ui.swing.action.KeyAction;

import uk.blankaspect.ui.swing.button.FButton;

import uk.blankaspect.ui.swing.checkbox.FCheckBox;

import uk.blankaspect.ui.swing.combobox.FComboBox;

import uk.blankaspect.ui.swing.label.FLabel;

import uk.blankaspect.ui.swing.misc.GuiUtils;

import uk.blankaspect.ui.swing.spinner.FIntegerSpinner;

//----------------------------------------------------------------------


// EXPORT HTML DIALOG CLASS


class ExportHtmlDialog
	extends JDialog
	implements ActionListener
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	private static final	int	CELL_SIZE_FIELD_LENGTH	= 2;

	private static final	String	TITLE_STR				= "Export HTML";
	private static final	String	STYLESHEET_STR			= "Stylesheet";
	private static final	String	CELL_SIZE_STR			= "Cell size";
	private static final	String	WRITE_STYLESHEET_STR	= "Write stylesheet file";
	private static final	String	WRITE_BLOCK_IMAGE_STR	= "Write block-image file";
	private static final	String	WRITE_ENTRIES_STR		= "Write grid entries";

	// Commands
	private interface Command
	{
		String	SELECT_STYLESHEET_KIND	= "selectStylesheetKind";
		String	ACCEPT					= "accept";
		String	CLOSE					= "close";
	}

////////////////////////////////////////////////////////////////////////
//  Member classes : non-inner classes
////////////////////////////////////////////////////////////////////////


	// RESULT CLASS


	public static class Result
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private Result(StylesheetKind stylesheetKind,
					   int            cellSize,
					   boolean        writeStylesheet,
					   boolean        writeBlockImage,
					   boolean        writeEntries)
		{
			this.stylesheetKind = stylesheetKind;
			this.cellSize = cellSize;
			this.writeStylesheet = writeStylesheet;
			this.writeBlockImage = writeBlockImage;
			this.writeEntries = writeEntries;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		StylesheetKind	stylesheetKind;
		int				cellSize;
		boolean			writeStylesheet;
		boolean			writeBlockImage;
		boolean			writeEntries;

	}

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	private ExportHtmlDialog(Window         owner,
							 Grid.Separator separator,
							 StylesheetKind stylesheetKind,
							 int            cellSize)
	{
		// Call superclass constructor
		super(owner, TITLE_STR, ModalityType.APPLICATION_MODAL);

		// Set icons
		setIconImages(owner.getIconImages());


		//----  Control panel

		GridBagLayout gridBag = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();

		JPanel controlPanel = new JPanel(gridBag);
		GuiUtils.setPaddedLineBorder(controlPanel);

		int gridY = 0;

		// Label: stylesheet kind
		JLabel styleKindLabel = new FLabel(STYLESHEET_STR);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(styleKindLabel, gbc);
		controlPanel.add(styleKindLabel);

		// Combo box: stylesheet kind
		stylesheetKindComboBox = new FComboBox<>(StylesheetKind.values());
		stylesheetKindComboBox.setSelectedValue(stylesheetKind);
		stylesheetKindComboBox.setActionCommand(Command.SELECT_STYLESHEET_KIND);
		stylesheetKindComboBox.addActionListener(this);

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(stylesheetKindComboBox, gbc);
		controlPanel.add(stylesheetKindComboBox);

		// Label: cell size
		JLabel cellSizeLabel = new FLabel(CELL_SIZE_STR);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(cellSizeLabel, gbc);
		controlPanel.add(cellSizeLabel);

		// Spinner: cell size
		cellSizeSpinner = new FIntegerSpinner(cellSize, Grid.MIN_HTML_CELL_SIZE, Grid.MAX_HTML_CELL_SIZE,
											  CELL_SIZE_FIELD_LENGTH);

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(cellSizeSpinner, gbc);
		controlPanel.add(cellSizeSpinner);

		// Check box: write stylesheet
		writeStylesheetCheckBox = new FCheckBox(WRITE_STYLESHEET_STR);

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(writeStylesheetCheckBox, gbc);
		controlPanel.add(writeStylesheetCheckBox);

		// Check box: write block image
		if (separator == Grid.Separator.BLOCK)
		{
			writeBlockImageCheckBox = new FCheckBox(WRITE_BLOCK_IMAGE_STR);

			gbc.gridx = 1;
			gbc.gridy = gridY++;
			gbc.gridwidth = 1;
			gbc.gridheight = 1;
			gbc.weightx = 0.0;
			gbc.weighty = 0.0;
			gbc.anchor = GridBagConstraints.LINE_START;
			gbc.fill = GridBagConstraints.NONE;
			gbc.insets = AppConstants.COMPONENT_INSETS;
			gridBag.setConstraints(writeBlockImageCheckBox, gbc);
			controlPanel.add(writeBlockImageCheckBox);
		}

		// Check box: write entries
		writeEntriesCheckBox = new FCheckBox(WRITE_ENTRIES_STR);

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(writeEntriesCheckBox, gbc);
		controlPanel.add(writeEntriesCheckBox);


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
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(3, 0, 0, 0);
		gridBag.setConstraints(buttonPanel, gbc);
		mainPanel.add(buttonPanel);

		// Add commands to action map
		KeyAction.create(mainPanel, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT,
						 KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), Command.CLOSE, this);

		// Update components
		updateComponents();


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

		// Set location of dialog
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

	public static Result showDialog(Component      parent,
									Grid.Separator separator,
									StylesheetKind stylesheetKind,
									int            cellSize)
	{
		return new ExportHtmlDialog(GuiUtils.getWindow(parent), separator, stylesheetKind, cellSize)
																							.getResult();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : ActionListener interface
////////////////////////////////////////////////////////////////////////

	public void actionPerformed(ActionEvent event)
	{
		String command = event.getActionCommand();

		if (command.equals(Command.SELECT_STYLESHEET_KIND))
			onSelectStylesheetKind();

		else if (command.equals(Command.ACCEPT))
			onAccept();

		else if (command.equals(Command.CLOSE))
			onClose();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	private Result getResult()
	{
		return (accepted
					? new Result(stylesheetKindComboBox.getSelectedValue(),
								 cellSizeSpinner.getIntValue(), writeStylesheetCheckBox.isSelected(),
								 (writeBlockImageCheckBox == null)
																? false
																: writeBlockImageCheckBox.isSelected(),
								 writeEntriesCheckBox.isSelected())
					: null);
	}

	//------------------------------------------------------------------

	private void updateComponents()
	{
		writeStylesheetCheckBox.
						setEnabled(stylesheetKindComboBox.getSelectedValue() == StylesheetKind.EXTERNAL);
	}

	//------------------------------------------------------------------

	private void onSelectStylesheetKind()
	{
		updateComponents();
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
	private	FComboBox<StylesheetKind>	stylesheetKindComboBox;
	private	FIntegerSpinner				cellSizeSpinner;
	private	JCheckBox					writeStylesheetCheckBox;
	private	JCheckBox					writeBlockImageCheckBox;
	private	JCheckBox					writeEntriesCheckBox;

}

//----------------------------------------------------------------------
