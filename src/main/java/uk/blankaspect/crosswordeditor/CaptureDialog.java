/*====================================================================*\

CaptureDialog.java

Crossword capture dialog box class.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.crosswordeditor;

//----------------------------------------------------------------------


// IMPORTS


import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Window;

import java.awt.datatransfer.FlavorEvent;
import java.awt.datatransfer.FlavorListener;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.awt.image.BufferedImage;

import java.io.File;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import uk.blankaspect.common.exception.AppException;
import uk.blankaspect.common.exception.FileException;

import uk.blankaspect.common.filesystem.PathnameUtils;

import uk.blankaspect.common.indexedsub.IndexedSub;

import uk.blankaspect.common.misc.FilenameSuffixFilter;
import uk.blankaspect.common.misc.MaxValueMap;

import uk.blankaspect.common.regex.RegexUtils;

import uk.blankaspect.common.string.StringUtils;

import uk.blankaspect.common.swing.action.KeyAction;

import uk.blankaspect.common.swing.border.TitledBorder;

import uk.blankaspect.common.swing.button.FButton;
import uk.blankaspect.common.swing.button.MenuButton;

import uk.blankaspect.common.swing.checkbox.FCheckBox;

import uk.blankaspect.common.swing.combobox.FComboBox;

import uk.blankaspect.common.swing.container.DimensionsSpinnerPanel;
import uk.blankaspect.common.swing.container.PathnamePanel;

import uk.blankaspect.common.swing.dialog.ParameterSetDialog;

import uk.blankaspect.common.swing.font.FontUtils;

import uk.blankaspect.common.swing.image.ClipboardImage;

import uk.blankaspect.common.swing.label.FixedWidthLabel;
import uk.blankaspect.common.swing.label.FLabel;

import uk.blankaspect.common.swing.misc.GuiUtils;
import uk.blankaspect.common.swing.misc.PropertyKeys;

import uk.blankaspect.common.swing.spinner.FIntegerSpinner;

import uk.blankaspect.common.swing.tabbedpane.FTabbedPane;

import uk.blankaspect.common.swing.textfield.FTextField;
import uk.blankaspect.common.swing.textfield.IntegerField;

//----------------------------------------------------------------------


// CROSSWORD CAPTURE DIALOG BOX CLASS


class CaptureDialog
	extends JDialog
	implements ActionListener, AnswerLengthPanel.LabelSource, DocumentListener, FlavorListener
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	public static final		int		MIN_GRID_IMAGE_VIEWPORT_WIDTH		= 160;
	public static final		int		MAX_GRID_IMAGE_VIEWPORT_WIDTH		= 1600;
	public static final		int		DEFAULT_GRID_IMAGE_VIEWPORT_WIDTH	= 800;

	public static final		int		MIN_GRID_IMAGE_VIEWPORT_HEIGHT		= 120;
	public static final		int		MAX_GRID_IMAGE_VIEWPORT_HEIGHT		= 1200;
	public static final		int		DEFAULT_GRID_IMAGE_VIEWPORT_HEIGHT	= 600;

	// Automatic grid-detection panel
	private static final	int		MIN_LINE_LENGTH_FIELD_LENGTH				= 3;
	private static final	int		MIN_LINE_SEPARATION_FIELD_LENGTH			= 2;
	private static final	int		MIN_LINE_ENDPOINT_TOLERANCE_FIELD_LENGTH	= 1;

	private static final	String	GRID_LINE_STR			= "Grid line";
	private static final	String	MIN_LENGTH_STR			= "Minimum length";
	private static final	String	MIN_SEPARATION_STR		= "Minimum separation";
	private static final	String	ENDPOINT_TOLERANCE_STR	= "Endpoint tolerance";

	// Grid panel
	private static final	int		GRID_SIZE_FIELD_LENGTH				= 2;
	private static final	int		X_OFFSET_FIELD_LENGTH				= 3;
	private static final	int		Y_OFFSET_FIELD_LENGTH				= 3;
	private static final	int		SAMPLE_SIZE_FIELD_LENGTH			= 1;
	private static final	int		BAR_WIDTH_THRESHOLD_FIELD_LENGTH	= 1;
	private static final	int		BRIGHTNESS_THRESHOLD_FIELD_LENGTH	= 2;

	private static final	double	BRIGHTNESS_THRESHOLD_FACTOR	= 0.01;

	private static final	String	GRID_SEPARATOR_STR			= "Grid separator";
	private static final	String	GRID_SIZE_STR				= "Grid size";
	private static final	String	COLUMNS_STR					= "columns";
	private static final	String	ROWS_STR					= "rows";
	private static final	String	AUTOMATIC_DETECTION_STR		= "Automatic grid detection";
	private static final	String	X_OFFSET_STR				= "X offset";
	private static final	String	Y_OFFSET_STR				= "Y offset";
	private static final	String	PERCENT_STR					= "%";
	private static final	String	SAMPLE_SIZE_STR				= "Sample size";
	private static final	String	BRIGHTNESS_THRESHOLD_STR	= "Brightness threshold";
	private static final	String	BAR_WIDTH_THRESHOLD_STR		= "Bar-width threshold";

	// Clue indications panel
	private static final	int		CLUE_REFERENCE_KEYWORD_NUM_COLUMNS	= 12;

	private static final	String	CLUE_REFERENCE_STR	= "Clue reference";

	// Clue substitutions panel
	private static final	int		CLUE_SUBSTITUTIONS_NUM_ROWS	= 8;

	// Text panel
	private static final	int		NUMBER_FIELD_LENGTH		= 6;
	private static final	int		TITLE_FIELD_NUM_COLUMNS	= 32;

	private static final	int		PROLOGUE_AREA_NUM_ROWS	= 4;
	private static final	int		EPILOGUE_AREA_NUM_ROWS	= 4;

	private static final	String	NUMBER_PLACEHOLDER	= "%n";

	private static final	String	NUMBER_STR		= "Number";
	private static final	String	TITLE_STR		= "Title";
	private static final	String	PROLOGUE_STR	= "Prologue";
	private static final	String	EPILOGUE_STR	= "Epilogue";

	// File panel
	private static final	int		FILENAME_FIELD_NUM_COLUMNS	= 24;

	private static final	String	FILENAME_STEM_STR		= "Filename stem";
	private static final	String	DOCUMENT_DIRECTORY_STR	= "Document directory";
	private static final	String	HTML_DIRECTORY_STR		= "HTML directory";

	// Button panel
	private static final	Insets	BUTTON_MARGINS	= new Insets(2, 8, 2, 8);

	private static final	String	MANAGE_PARAMETER_SET_STR	= "Manage parameter set";
	private static final	String	GET_GRID_IMAGE_STR			= "Get grid image";
	private static final	String	GET_CLUES_STR				= "Get clues";

	// General
	private static final	String	CAPTURE_STR				= "Capture crossword";
	private static final	String	SELECT_GRID_STR			= "Select grid in image";
	private static final	String	DOCUMENT_DIR_TITLE_STR	= "Document directory";
	private static final	String	HTML_DIR_TITLE_STR		= "HTML directory";
	private static final	String	SELECT_STR				= "Select";
	private static final	String	SELECT_FILE_STR			= "Select file";
	private static final	String	SELECT_DIRECTORY_STR	= "Select directory";
	private static final	String	PARAMETER_SET_STR		= "Parameter set";
	private static final	String	PARAMETER_SET_FILE_STR	= "Parameter-set file";
	private static final	String	CONFIRM_CLEAR_STR		= "Do you want to clear the crossword " +
																"number, prologue, epilogue,\ngrid image " +
																"and lists of clues?";
	private static final	String	GRID_FOUND_STR			= "A grid of %1 columns by %2 rows was found." +
																"\nDo you want to set the grid size to " +
																"these values?";
	private static final	String	NO_PARAM_SET_FILE_STR	= "No parameter-set file has been specified " +
																"in the user preferences.\nDo you want " +
																"to choose a file?";

	private static final	String	KEY	= CaptureDialog.class.getCanonicalName();

	// Commands
	private interface Command
	{
		String	SELECT_GRID_SEPARATOR		= "selectGridSeparator";
		String	CHOOSE_DOCUMENT_DIRECTORY	= "chooseDocumentDirectory";
		String	CHOOSE_HTML_DIRECTORY		= "chooseHtmlDirectory";
		String	MANAGE_PARAMETER_SET		= "manageParameterSet";
		String	CLEAR						= "clear";
		String	GET_GRID_IMAGE				= "getGridImage";
		String	GET_CLUES					= "getClues.";
		String	ACCEPT						= "accept";
		String	CLOSE						= "close";
	}

////////////////////////////////////////////////////////////////////////
//  Enumerated types
////////////////////////////////////////////////////////////////////////


	// TABS


	private enum Tab
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		AUTOMATIC_GRID_DETECTION
		(
			"Auto grid detection"
		)
		{
			@Override
			protected JPanel createPanel(CaptureDialog dialog)
			{
				return dialog.createPanelAutoGridDetection();
			}
		},

		GRID
		(
			"Grid"
		)
		{
			@Override
			protected JPanel createPanel(CaptureDialog dialog)
			{
				return dialog.createPanelGrid();
			}
		},

		CLUE_INDICATIONS
		(
			"Clue indications"
		)
		{
			@Override
			protected JPanel createPanel(CaptureDialog dialog)
			{
				return dialog.createPanelClueIndications();
			}
		},

		CLUE_SUBSTITUTIONS
		(
			"Clue substitutions"
		)
		{
			@Override
			protected JPanel createPanel(CaptureDialog dialog)
			{
				return dialog.createPanelClueSubstitutions();
			}
		},

		TEXT
		(
			"Text"
		)
		{
			@Override
			protected JPanel createPanel(CaptureDialog dialog)
			{
				return dialog.createPanelText();
			}
		},

		FILE
		(
			"File"
		)
		{
			@Override
			protected JPanel createPanel(CaptureDialog dialog)
			{
				return dialog.createPanelFile();
			}
		};

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private Tab(String text)
		{
			this.text = text;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Abstract methods
	////////////////////////////////////////////////////////////////////

		protected abstract JPanel createPanel(CaptureDialog dialog);

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	String	text;

	}

	//==================================================================


	// ERROR IDENTIFIERS


	private enum ErrorId
		implements AppException.IId
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		NOT_A_FILE
		("The pathname does not denote a normal file."),

		NOT_A_DIRECTORY
		("The pathname does not denote a directory."),

		MALFORMED_PATTERN
		("The pattern is not a well-formed regular expression.\n(%1)");

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


	// GRID LABEL CLASS


	private static class GridLabel
		extends FixedWidthLabel
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		private static final	String	KEY	= GridLabel.class.getCanonicalName();

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private GridLabel(String text)
		{
			super(text);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Class methods
	////////////////////////////////////////////////////////////////////

		private static void reset()
		{
			MaxValueMap.removeAll(KEY);
		}

		//--------------------------------------------------------------

		private static void update()
		{
			MaxValueMap.update(KEY);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		protected String getKey()
		{
			return KEY;
		}

		//--------------------------------------------------------------

	}

	//==================================================================


	// CLUE INDICATIONS LABEL CLASS


	private static class ClueIndicationsLabel
		extends FixedWidthLabel
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		private static final	String	KEY	= ClueIndicationsLabel.class.getCanonicalName();

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private ClueIndicationsLabel(String text)
		{
			super(text);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Class methods
	////////////////////////////////////////////////////////////////////

		private static void reset()
		{
			MaxValueMap.removeAll(KEY);
		}

		//--------------------------------------------------------------

		private static void update()
		{
			MaxValueMap.update(KEY);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		protected String getKey()
		{
			return KEY;
		}

		//--------------------------------------------------------------

	}

	//==================================================================


	// NUMBER FIELD CLASS


	private static class NumberField
		extends IntegerField.Unsigned
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private NumberField()
		{
			super(NUMBER_FIELD_LENGTH);
			AppFont.TEXT_FIELD.apply(this);
			GuiUtils.setTextComponentMargins(this);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		protected int getColumnWidth()
		{
			return (FontUtils.getCharWidth('0', getFontMetrics(getFont())) + 1);
		}

		//--------------------------------------------------------------

	}

	//==================================================================


	// STATUS PANEL CLASS


	private static class StatusPanel
		extends AbstractStatusPanel
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		private static final	String	GRID_STR	= "Grid";
		private static final	String	CLUES_STR	= "Clues - ";

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private StatusPanel()
		{
			// Call superclass constructor
			super(true);

			// Field: grid
			gridField = new StatusField();
			add(gridField);

			// Fields: clues
			cluesFields = new EnumMap<>(Direction.class);
			for (Direction direction : Direction.DEFINED_DIRECTIONS)
			{
				StatusField cluesField = new StatusField();
				cluesFields.put(direction, cluesField);
				add(cluesField);
			}
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		public void setGrid(boolean enabled)
		{
			gridField.setText(enabled ? GRID_STR : null);
		}

		//--------------------------------------------------------------

		public void setClues(Direction direction,
							 boolean   enabled)
		{
			cluesFields.get(direction).setText(enabled ? CLUES_STR + direction.toString() : null);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	StatusField					gridField;
		private	Map<Direction, StatusField>	cluesFields;

	}

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Member classes : inner classes
////////////////////////////////////////////////////////////////////////


	// MENU ITEM CLASS


	private class MenuItem
		extends JMenuItem
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private MenuItem(Direction direction)
		{
			super(direction.toString());
			AppFont.MAIN.apply(this);
			setMnemonic(direction.getKeyCode());
			setActionCommand(Command.GET_CLUES + direction.getKey());
			addActionListener(CaptureDialog.this);
		}

		//--------------------------------------------------------------

	}

	//==================================================================


	// BUTTON CLASS


	private class Button
		extends FButton
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private Button(String text,
					   String command,
					   int    mnemonicKey)
		{
			super(text);
			setMargin(BUTTON_MARGINS);
			setMnemonic(mnemonicKey);
			setActionCommand(command);
			addActionListener(CaptureDialog.this);
		}

		//--------------------------------------------------------------

	}

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	private CaptureDialog(Window owner,
						  int    documentIndex)
	{
		// Call superclass constructor
		super(owner, CAPTURE_STR, Dialog.ModalityType.APPLICATION_MODAL);

		// Set icons
		setIconImages(owner.getIconImages());

		// Initialise instance variables
		this.documentIndex = documentIndex;
		clueLists = new EnumMap<>(Direction.class);

		documentDirectoryChooser = new JFileChooser();
		documentDirectoryChooser.setDialogTitle(DOCUMENT_DIR_TITLE_STR);
		documentDirectoryChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		documentDirectoryChooser.setApproveButtonMnemonic(KeyEvent.VK_S);
		documentDirectoryChooser.setApproveButtonToolTipText(SELECT_DIRECTORY_STR);

		htmlDirectoryChooser = new JFileChooser();
		htmlDirectoryChooser.setDialogTitle(HTML_DIR_TITLE_STR);
		htmlDirectoryChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		htmlDirectoryChooser.setApproveButtonMnemonic(KeyEvent.VK_S);
		htmlDirectoryChooser.setApproveButtonToolTipText(SELECT_DIRECTORY_STR);

		parameterSetFileChooser = new JFileChooser();
		parameterSetFileChooser.setDialogTitle(PARAMETER_SET_FILE_STR);
		parameterSetFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		parameterSetFileChooser.setApproveButtonMnemonic(KeyEvent.VK_S);
		parameterSetFileChooser.setApproveButtonToolTipText(SELECT_FILE_STR);
		parameterSetFileChooser.setFileFilter(new FilenameSuffixFilter(AppConstants.XML_FILES_STR,
																	   AppConstants.XML_FILENAME_EXTENSION));


		//----  Tabbed panel

		tabbedPanel = new FTabbedPane();
		for (Tab tab : Tab.values())
			tabbedPanel.addTab(tab.text, tab.createPanel(this));
		tabbedPanel.setSelectedIndex(tabIndex);


		//----  Button panel: capture commands

		JPanel captureButtonPanel = new JPanel(new GridLayout(0, 2, 8, 4));

		// Button: manage parameter set
		JButton manageParameterSetButton = new Button(MANAGE_PARAMETER_SET_STR + AppConstants.ELLIPSIS_STR,
													  Command.MANAGE_PARAMETER_SET, KeyEvent.VK_P);
		captureButtonPanel.add(manageParameterSetButton);

		// Button: clear
		clearButton = new Button(AppConstants.CLEAR_STR + AppConstants.ELLIPSIS_STR, Command.CLEAR, KeyEvent.VK_L);
		captureButtonPanel.add(clearButton);

		// Button: get grid image
		getGridImageButton = new Button(GET_GRID_IMAGE_STR + AppConstants.ELLIPSIS_STR, Command.GET_GRID_IMAGE,
										KeyEvent.VK_G);
		captureButtonPanel.add(getGridImageButton);

		// Button: get clues
		getCluesButton = new MenuButton(GET_CLUES_STR);
		getCluesButton.setMargin(BUTTON_MARGINS);
		getCluesButton.setMnemonic(KeyEvent.VK_C);
		for (Direction direction : Direction.DEFINED_DIRECTIONS)
			getCluesButton.addMenuItem(new MenuItem(direction));
		captureButtonPanel.add(getCluesButton);


		//----  Button panel: close commands

		JPanel closeButtonPanel = new JPanel(new GridLayout(1, 0, 8, 0));

		// Button: OK
		okButton = new FButton(AppConstants.OK_STR);
		okButton.setActionCommand(Command.ACCEPT);
		okButton.addActionListener(this);
		closeButtonPanel.add(okButton);

		// Button: cancel
		JButton cancelButton = new FButton(AppConstants.CANCEL_STR);
		cancelButton.setActionCommand(Command.CLOSE);
		cancelButton.addActionListener(this);
		closeButtonPanel.add(cancelButton);


		//----  Button panel

		GridBagLayout gridBag = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();

		JPanel buttonPanel = new JPanel(gridBag);
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(3, 24, 3, 24));

		int gridX = 0;

		gbc.gridx = gridX++;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.5;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(0, 0, 0, 12);
		gridBag.setConstraints(captureButtonPanel, gbc);
		buttonPanel.add(captureButtonPanel);

		gbc.gridx = gridX++;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.5;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(0, 12, 0, 0);
		gridBag.setConstraints(closeButtonPanel, gbc);
		buttonPanel.add(closeButtonPanel);


		//----  Status panel

		statusPanel = new StatusPanel();


		//----  Main panel

		JPanel mainPanel = new JPanel(gridBag);
		mainPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		mainPanel.setTransferHandler(documentDirectoryField.getTransferHandler());

		int gridY = 0;

		gbc.gridx = 0;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.NORTH;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(0, 0, 0, 0);
		gridBag.setConstraints(tabbedPanel, gbc);
		mainPanel.add(tabbedPanel);

		gbc.gridx = 0;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.NORTH;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(3, 0, 0, 0);
		gridBag.setConstraints(buttonPanel, gbc);
		mainPanel.add(buttonPanel);

		gbc.gridx = 0;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(3, 0, 0, 0);
		gridBag.setConstraints(statusPanel, gbc);
		mainPanel.add(statusPanel);

		// Set parameters
		setParams(params);

		// Add command to action map
		KeyAction.create(mainPanel, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT,
						 KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.CTRL_DOWN_MASK), Command.ACCEPT, this);


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

		// Respond to changes to data flavours on system clipboard
		getToolkit().getSystemClipboard().addFlavorListener(this);

		// Prevent window from being resized
		setResizable(false);

		// Resize window to its preferred size
		pack();

		// Set location of window
		if (location == null)
			location = GuiUtils.getComponentLocation(this, owner);
		setLocation(location);

		// Make window visible
		setVisible(true);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	public static CrosswordDocument showDialog(Component parent,
											   int       documentIndex)

	{
		return new CaptureDialog(GuiUtils.getWindow(parent), documentIndex).getDocument();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : ActionListener interface
////////////////////////////////////////////////////////////////////////

	public void actionPerformed(ActionEvent event)
	{
		try
		{
			String command = event.getActionCommand();

			if (command.equals(Command.SELECT_GRID_SEPARATOR))
				onSelectGridSeparator();

			else if (command.equals(Command.CHOOSE_DOCUMENT_DIRECTORY))
				onChooseDocumentDirectory();

			else if (command.equals(Command.CHOOSE_HTML_DIRECTORY))
				onChooseHtmlDirectory();

			else if (command.equals(Command.MANAGE_PARAMETER_SET))
				onManageParameterSet();

			else if (command.equals(Command.CLEAR))
				onClear();

			else if (command.equals(Command.GET_GRID_IMAGE))
				onGetGridImage();

			else if (command.startsWith(Command.GET_CLUES))
				onGetClues(StringUtils.removePrefix(command, Command.GET_CLUES));

			else if (command.equals(Command.ACCEPT))
				onAccept();

			else if (command.equals(Command.CLOSE))
				onClose();
		}
		catch (AppException e)
		{
			JOptionPane.showMessageDialog(this, e, CAPTURE_STR, JOptionPane.ERROR_MESSAGE);
		}
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : AnswerLengthPanel.LabelSource interface
////////////////////////////////////////////////////////////////////////

	public JLabel createLabel(String text)
	{
		return new ClueIndicationsLabel(text);
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
		updateClearButton();
	}

	//------------------------------------------------------------------

	public void removeUpdate(DocumentEvent event)
	{
		updateClearButton();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : FlavorListener interface
////////////////////////////////////////////////////////////////////////

	public void flavorsChanged(FlavorEvent event)
	{
		updateClipboardButtons();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	private CrosswordDocument getDocument()
	{
		return (accepted ? document : null);
	}

	//------------------------------------------------------------------

	private Grid.Separator getGridSeparator()
	{
		return gridSeparatorComboBox.getSelectedValue();
	}

	//------------------------------------------------------------------

	private String getText(JTextField textField)
	{
		return textField.getText().replace(NUMBER_PLACEHOLDER, numberField.getText());
	}

	//------------------------------------------------------------------

	private File getDocumentDirectory()
	{
		return (documentDirectoryField.isEmpty()
						? null
						: new File(PathnameUtils.parsePathname(getText(documentDirectoryField))));
	}

	//------------------------------------------------------------------

	private File getHtmlDirectory()
	{
		return (htmlDirectoryField.isEmpty()
						? null
						: new File(PathnameUtils.parsePathname(getText(htmlDirectoryField))));
	}

	//------------------------------------------------------------------

	private void updateComponents()
	{
		((CardLayout)gridSeparatorParamPanel.getLayout()).
											show(gridSeparatorParamPanel, getGridSeparator().getKey());
		updateClearButton();
		updateClipboardButtons();
		updateAcceptButton();
	}

	//------------------------------------------------------------------

	private void updateClearButton()
	{
		clearButton.setEnabled(!numberField.isEmpty() || !prologuePanel.isEmpty() ||
								!epiloguePanel.isEmpty() || (gridImage != null) || !clueLists.isEmpty());
	}

	//------------------------------------------------------------------

	private void updateClipboardButtons()
	{
		getGridImageButton.setEnabled(ClipboardImage.clipboardHasImage());
		getCluesButton.setEnabled(Utils.clipboardHasText());
	}

	//------------------------------------------------------------------

	private void updateAcceptButton()
	{
		okButton.setEnabled(gridImage != null);
	}

	//------------------------------------------------------------------

	private CaptureParams getParams()
	{
		Grid.Separator gridSeparator = getGridSeparator();
		return new CaptureParams(gridSeparator,
								 gridSizePanel.getValue1(),
								 gridSizePanel.getValue2(),
								 autoGridDetectionCheckBox.isSelected(),
								 xOffsetSpinner.getIntValue(),
								 yOffsetSpinner.getIntValue(),
								 (gridSeparator == Grid.Separator.BLOCK)
															? sampleSizeSpinner.getIntValue()
															: CaptureParams.DEFAULT_SAMPLE_SIZE,
								 (gridSeparator == Grid.Separator.BLOCK)
															? blockBrightnessThresholdSpinner.getIntValue()
															: CaptureParams.DEFAULT_BRIGHTNESS_THRESHOLD,
								 (gridSeparator == Grid.Separator.BAR)
															? barWidthThresholdSpinner.getIntValue()
															: CaptureParams.DEFAULT_BAR_WIDTH_THRESHOLD,
								 (gridSeparator == Grid.Separator.BAR)
															? barBrightnessThresholdSpinner.getIntValue()
															: CaptureParams.DEFAULT_BRIGHTNESS_THRESHOLD,
								 gridLineBrightnessThresholdSpinner.getIntValue(),
								 gridLineMinLengthSpinner.getIntValue(),
								 gridLineMinSeparationSpinner.getIntValue(),
								 gridLineEndpointToleranceSpinner.getIntValue(),
								 clueReferenceKeywordField.getText(),
								 answerLengthPanel.getPattern(),
								 answerLengthPanel.getSubstitutions(),
								 clueSubstitutionsPanel.getSubstitutions(),
								 titleField.getText(),
								 filenameStemField.getText(),
								 documentDirectoryField.getPathname(),
								 htmlDirectoryField.getPathname());
	}

	//------------------------------------------------------------------

	private void setParams(CaptureParams params)
	{
		gridSeparatorComboBox.setSelectedValue(params.getGridSeparator());
		gridSizePanel.setValues(params.getNumColumns(), params.getNumRows());
		autoGridDetectionCheckBox.setSelected(params.isAutomaticGridDetection());
		xOffsetSpinner.setIntValue(params.getXOffset());
		yOffsetSpinner.setIntValue(params.getYOffset());
		sampleSizeSpinner.setIntValue(params.getSampleSize());
		blockBrightnessThresholdSpinner.setIntValue(params.getBlockBrightnessThreshold());
		barBrightnessThresholdSpinner.setIntValue(params.getBarBrightnessThreshold());
		barWidthThresholdSpinner.setIntValue(params.getBarWidthThreshold());
		gridLineBrightnessThresholdSpinner.setIntValue(params.getGridLineBrightnessThreshold());
		gridLineMinLengthSpinner.setIntValue(params.getGridLineMinLength());
		gridLineMinSeparationSpinner.setIntValue(params.getGridLineMinSeparation());
		gridLineEndpointToleranceSpinner.setIntValue(params.getGridLineEndpointTolerance());
		clueReferenceKeywordField.setText(params.getClueReferenceKeyword());
		answerLengthPanel.setPattern(params.getAnswerLengthPattern());
		answerLengthPanel.setSubstitutions(params.getAnswerLengthSubstitutions());
		clueSubstitutionsPanel.setSubstitutions(params.getClueSubstitutions());
		titleField.setText(params.getTitle());
		filenameStemField.setText(params.getFilename());
		documentDirectoryField.setText(params.getDocumentDirectory());
		htmlDirectoryField.setText(params.getHtmlDirectory());

		updateComponents();
	}

	//------------------------------------------------------------------

	private void validateClueParams()
		throws AppException
	{
		// Answer-length pattern
		try
		{
			if (answerLengthPanel.isPattern())
				Pattern.compile(answerLengthPanel.getPattern());
		}
		catch (PatternSyntaxException e)
		{
			GuiUtils.setFocus(answerLengthPanel.getPatternField());
			int index = e.getIndex();
			if (index >= 0)
				answerLengthPanel.getPatternField().setCaretPosition(index);
			throw new AppException(ErrorId.MALFORMED_PATTERN, RegexUtils.getExceptionMessage(e));
		}
	}

	//------------------------------------------------------------------

	private void validatePathnames()
		throws AppException
	{
		// Document directory
		try
		{
			if (!documentDirectoryField.isEmpty())
			{
				File directory = getDocumentDirectory();
				if (directory.exists() && !directory.isDirectory())
					throw new FileException(ErrorId.NOT_A_DIRECTORY, directory);
			}
		}
		catch (AppException e)
		{
			GuiUtils.setFocus(documentDirectoryField);
			throw e;
		}

		// HTML directory
		try
		{
			if (!htmlDirectoryField.isEmpty())
			{
				File directory = getHtmlDirectory();
				if (directory.exists() && !directory.isDirectory())
					throw new FileException(ErrorId.NOT_A_DIRECTORY, directory);
			}
		}
		catch (AppException e)
		{
			GuiUtils.setFocus(htmlDirectoryField);
			throw e;
		}
	}

	//------------------------------------------------------------------

	private JPanel createPanelAutoGridDetection()
	{

		//----  Control panel

		GridBagLayout gridBag = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();

		JPanel controlPanel = new JPanel(gridBag);
		TitledBorder.setPaddedBorder(controlPanel, GRID_LINE_STR);

		int gridY = 0;

		// Label: line brightness threshold
		JLabel lineBrightnessThresholdLabel = new FLabel(BRIGHTNESS_THRESHOLD_STR);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(lineBrightnessThresholdLabel, gbc);
		controlPanel.add(lineBrightnessThresholdLabel);

		// Spinner: line brightness threshold
		gridLineBrightnessThresholdSpinner = new FIntegerSpinner(CaptureParams.MIN_BRIGHTNESS_THRESHOLD,
																 CaptureParams.MIN_BRIGHTNESS_THRESHOLD,
																 CaptureParams.MAX_BRIGHTNESS_THRESHOLD,
																 BRIGHTNESS_THRESHOLD_FIELD_LENGTH);

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(gridLineBrightnessThresholdSpinner, gbc);
		controlPanel.add(gridLineBrightnessThresholdSpinner);

		// Label: minimum line length
		JLabel minLineLengthLabel = new FLabel(MIN_LENGTH_STR);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(minLineLengthLabel, gbc);
		controlPanel.add(minLineLengthLabel);

		// Spinner: minimum line length
		gridLineMinLengthSpinner = new FIntegerSpinner(CaptureParams.MIN_GRID_LINE_MIN_LENGTH,
													   CaptureParams.MIN_GRID_LINE_MIN_LENGTH,
													   CaptureParams.MAX_GRID_LINE_MIN_LENGTH,
													   MIN_LINE_LENGTH_FIELD_LENGTH);

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(gridLineMinLengthSpinner, gbc);
		controlPanel.add(gridLineMinLengthSpinner);

		// Label: minimum line separation
		JLabel minLineSeparationLabel = new FLabel(MIN_SEPARATION_STR);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(minLineSeparationLabel, gbc);
		controlPanel.add(minLineSeparationLabel);

		// Spinner: minimum line separation
		gridLineMinSeparationSpinner = new FIntegerSpinner(CaptureParams.MIN_GRID_LINE_MIN_SEPARATION,
														   CaptureParams.MIN_GRID_LINE_MIN_SEPARATION,
														   CaptureParams.MAX_GRID_LINE_MIN_SEPARATION,
														   MIN_LINE_SEPARATION_FIELD_LENGTH);

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(gridLineMinSeparationSpinner, gbc);
		controlPanel.add(gridLineMinSeparationSpinner);

		// Label: line endpoint tolerance
		JLabel lineEndpointToleranceLabel = new FLabel(ENDPOINT_TOLERANCE_STR);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(lineEndpointToleranceLabel, gbc);
		controlPanel.add(lineEndpointToleranceLabel);

		// Spinner: line endpoint tolerance
		gridLineEndpointToleranceSpinner =
										new FIntegerSpinner(CaptureParams.MIN_GRID_LINE_ENDPOINT_TOLERANCE,
															CaptureParams.MIN_GRID_LINE_ENDPOINT_TOLERANCE,
															CaptureParams.MAX_GRID_LINE_ENDPOINT_TOLERANCE,
															MIN_LINE_ENDPOINT_TOLERANCE_FIELD_LENGTH);

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(gridLineEndpointToleranceSpinner, gbc);
		controlPanel.add(gridLineEndpointToleranceSpinner);


		//----  Outer panel

		JPanel panel = new JPanel(gridBag);
		panel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

		// Set preferred focus owner
		panel.putClientProperty(PropertyKeys.PREFERRED_FOCUS_OWNER, gridLineBrightnessThresholdSpinner);

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.NORTH;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(0, 0, 0, 0);
		gridBag.setConstraints(controlPanel, gbc);
		panel.add(controlPanel);

		return panel;
	}

	//------------------------------------------------------------------

	private JPanel createPanelGrid()
	{

		//----  Control panel

		GridBagLayout gridBag = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();

		JPanel controlPanel = new JPanel(gridBag);
		GuiUtils.setPaddedLineBorder(controlPanel);

		// Reset fixed-width labels
		GridLabel.reset();


		//----  Control panel

		int gridY = 0;

		// Label: grid separator
		JLabel gridSeparatorLabel = new GridLabel(GRID_SEPARATOR_STR);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(gridSeparatorLabel, gbc);
		controlPanel.add(gridSeparatorLabel);

		// Combo box: grid separator
		gridSeparatorComboBox = new FComboBox<>(Grid.Separator.values());
		gridSeparatorComboBox.setActionCommand(Command.SELECT_GRID_SEPARATOR);
		gridSeparatorComboBox.addActionListener(this);

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(gridSeparatorComboBox, gbc);
		controlPanel.add(gridSeparatorComboBox);

		// Label: grid size
		JLabel gridSizeLabel = new GridLabel(GRID_SIZE_STR);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(gridSizeLabel, gbc);
		controlPanel.add(gridSizeLabel);

		// Panel: grid size
		gridSizePanel = new DimensionsSpinnerPanel(Grid.DEFAULT_NUM_COLUMNS, Grid.DEFAULT_NUM_ROWS,
												   Grid.MIN_NUM_COLUMNS, Grid.MAX_NUM_COLUMNS,
												   GRID_SIZE_FIELD_LENGTH,
												   new String[] { COLUMNS_STR, ROWS_STR }, true);

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 1.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(gridSizePanel, gbc);
		controlPanel.add(gridSizePanel);

		// Check box: automatic grid detection
		autoGridDetectionCheckBox = new FCheckBox(AUTOMATIC_DETECTION_STR);

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(autoGridDetectionCheckBox, gbc);
		controlPanel.add(autoGridDetectionCheckBox);

		// Label: x offset
		JLabel xOffsetLabel = new GridLabel(X_OFFSET_STR);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(xOffsetLabel, gbc);
		controlPanel.add(xOffsetLabel);

		// Panel: x offset
		JPanel xOffsetPanel = new JPanel(gridBag);

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(xOffsetPanel, gbc);
		controlPanel.add(xOffsetPanel);

		// Spinner: x offset
		xOffsetSpinner = new FIntegerSpinner(CaptureParams.MIN_X_OFFSET, CaptureParams.MIN_X_OFFSET,
											 CaptureParams.MAX_X_OFFSET, X_OFFSET_FIELD_LENGTH, true);

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(0, 0, 0, 0);
		gridBag.setConstraints(xOffsetSpinner, gbc);
		xOffsetPanel.add(xOffsetSpinner);

		// Label: percent
		JLabel xPercentLabel = new FLabel(PERCENT_STR);

		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(0, 4, 0, 0);
		gridBag.setConstraints(xPercentLabel, gbc);
		xOffsetPanel.add(xPercentLabel);

		// Label: y offset
		JLabel yOffsetLabel = new GridLabel(Y_OFFSET_STR);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(yOffsetLabel, gbc);
		controlPanel.add(yOffsetLabel);

		// Panel: y offset
		JPanel yOffsetPanel = new JPanel(gridBag);

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(yOffsetPanel, gbc);
		controlPanel.add(yOffsetPanel);

		// Spinner: y offset
		yOffsetSpinner = new FIntegerSpinner(CaptureParams.MIN_Y_OFFSET, CaptureParams.MIN_Y_OFFSET,
											 CaptureParams.MAX_Y_OFFSET, Y_OFFSET_FIELD_LENGTH, true);

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(0, 0, 0, 0);
		gridBag.setConstraints(yOffsetSpinner, gbc);
		yOffsetPanel.add(yOffsetSpinner);

		// Label: percent
		JLabel yPercentLabel = new FLabel(PERCENT_STR);

		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(0, 4, 0, 0);
		gridBag.setConstraints(yPercentLabel, gbc);
		yOffsetPanel.add(yPercentLabel);

		// Panel: grid-separator-specific parameters
		gridSeparatorParamPanel = new JPanel(new CardLayout());
		gridSeparatorParamPanel.add(createPanelBlock(), Grid.Separator.BLOCK.getKey());
		gridSeparatorParamPanel.add(createPanelBar(), Grid.Separator.BAR.getKey());

		gbc.gridx = 0;
		gbc.gridy = gridY++;
		gbc.gridwidth = 2;
		gbc.gridheight = 1;
		gbc.weightx = 1.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.FIRST_LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(0, 0, 0, 0);
		gridBag.setConstraints(gridSeparatorParamPanel, gbc);
		controlPanel.add(gridSeparatorParamPanel);

		// Update widths of labels
		GridLabel.update();


		//----  Outer panel

		JPanel panel = new JPanel(gridBag);
		panel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

		// Set preferred focus owner
		panel.putClientProperty(PropertyKeys.PREFERRED_FOCUS_OWNER, gridSeparatorComboBox);

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.NORTH;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(0, 0, 0, 0);
		gridBag.setConstraints(controlPanel, gbc);
		panel.add(controlPanel);

		return panel;
	}

	//------------------------------------------------------------------

	private JPanel createPanelBlock()
	{
		GridBagLayout gridBag = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();

		JPanel panel = new JPanel(gridBag);

		int gridY = 0;

		// Label: sample size
		JLabel sampleSizeLabel = new GridLabel(SAMPLE_SIZE_STR);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(sampleSizeLabel, gbc);
		panel.add(sampleSizeLabel);

		// Spinner: sample size
		sampleSizeSpinner = new FIntegerSpinner(CaptureParams.MIN_SAMPLE_SIZE,
												CaptureParams.MIN_SAMPLE_SIZE,
												CaptureParams.MAX_SAMPLE_SIZE,
												SAMPLE_SIZE_FIELD_LENGTH);

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(sampleSizeSpinner, gbc);
		panel.add(sampleSizeSpinner);

		// Label: block brightness threshold
		JLabel blockBrightnessThresholdLabel = new GridLabel(BRIGHTNESS_THRESHOLD_STR);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(blockBrightnessThresholdLabel, gbc);
		panel.add(blockBrightnessThresholdLabel);

		// Spinner: block brightness threshold
		blockBrightnessThresholdSpinner = new FIntegerSpinner(CaptureParams.MIN_BRIGHTNESS_THRESHOLD,
															  CaptureParams.MIN_BRIGHTNESS_THRESHOLD,
															  CaptureParams.MAX_BRIGHTNESS_THRESHOLD,
															  BRIGHTNESS_THRESHOLD_FIELD_LENGTH);

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(blockBrightnessThresholdSpinner, gbc);
		panel.add(blockBrightnessThresholdSpinner);

		return panel;
	}

	//------------------------------------------------------------------

	private JPanel createPanelBar()
	{
		GridBagLayout gridBag = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();

		JPanel panel = new JPanel(gridBag);

		int gridY = 0;

		// Label: bar width threshold
		JLabel barWidthThresholdLabel = new GridLabel(BAR_WIDTH_THRESHOLD_STR);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(barWidthThresholdLabel, gbc);
		panel.add(barWidthThresholdLabel);

		// Spinner: bar width threshold
		barWidthThresholdSpinner = new FIntegerSpinner(CaptureParams.MIN_BAR_WIDTH_THRESHOLD,
													   CaptureParams.MIN_BAR_WIDTH_THRESHOLD,
													   CaptureParams.MAX_BAR_WIDTH_THRESHOLD,
													   BAR_WIDTH_THRESHOLD_FIELD_LENGTH);

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(barWidthThresholdSpinner, gbc);
		panel.add(barWidthThresholdSpinner);

		// Label: bar brightness threshold
		JLabel barBrightnessThresholdLabel = new GridLabel(BRIGHTNESS_THRESHOLD_STR);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(barBrightnessThresholdLabel, gbc);
		panel.add(barBrightnessThresholdLabel);

		// Spinner: bar brightness threshold
		barBrightnessThresholdSpinner = new FIntegerSpinner(CaptureParams.MIN_BRIGHTNESS_THRESHOLD,
															CaptureParams.MIN_BRIGHTNESS_THRESHOLD,
															CaptureParams.MAX_BRIGHTNESS_THRESHOLD,
															BRIGHTNESS_THRESHOLD_FIELD_LENGTH);

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(barBrightnessThresholdSpinner, gbc);
		panel.add(barBrightnessThresholdSpinner);

		return panel;
	}

	//------------------------------------------------------------------

	private JPanel createPanelClueIndications()
	{
		// Reset fixed-width labels
		ClueIndicationsLabel.reset();


		//----  Control panel

		GridBagLayout gridBag = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();

		JPanel controlPanel = new JPanel(gridBag);
		GuiUtils.setPaddedLineBorder(controlPanel);

		int gridY = 0;

		// Label: clue reference
		JLabel clueReferenceLabel = new ClueIndicationsLabel(CLUE_REFERENCE_STR);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(clueReferenceLabel, gbc);
		controlPanel.add(clueReferenceLabel);

		// Field: clue-reference keyword
		clueReferenceKeywordField = new FTextField(CLUE_REFERENCE_KEYWORD_NUM_COLUMNS);

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 1.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(clueReferenceKeywordField, gbc);
		controlPanel.add(clueReferenceKeywordField);


		//----  Answer length panel

		answerLengthPanel = new AnswerLengthPanel(this);

		// Update widths of labels
		ClueIndicationsLabel.update();


		//----  Outer panel

		JPanel panel = new JPanel(gridBag);
		panel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

		// Set preferred focus owner
		panel.putClientProperty(PropertyKeys.PREFERRED_FOCUS_OWNER, clueReferenceKeywordField);

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
		panel.add(controlPanel);

		gbc.gridx = 0;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.NORTH;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(3, 0, 0, 0);
		gridBag.setConstraints(answerLengthPanel, gbc);
		panel.add(answerLengthPanel);

		return panel;
	}

	//------------------------------------------------------------------

	private JPanel createPanelClueSubstitutions()
	{

		//----  Clue substitutions panel

		clueSubstitutionsPanel = new SubstitutionSelectionPanel(CLUE_SUBSTITUTIONS_NUM_ROWS);
		GuiUtils.setPaddedLineBorder(clueSubstitutionsPanel, 8);


		//----  Outer panel

		GridBagLayout gridBag = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();

		JPanel panel = new JPanel(gridBag);
		panel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.NORTH;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(0, 0, 0, 0);
		gridBag.setConstraints(clueSubstitutionsPanel, gbc);
		panel.add(clueSubstitutionsPanel);

		return panel;
	}

	//------------------------------------------------------------------

	private JPanel createPanelText()
	{

		//----  Control panel

		GridBagLayout gridBag = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();

		JPanel controlPanel = new JPanel(gridBag);
		GuiUtils.setPaddedLineBorder(controlPanel);

		int gridY = 0;

		// Label: number
		JLabel numberLabel = new FLabel(NUMBER_STR);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(numberLabel, gbc);
		controlPanel.add(numberLabel);

		// Field: number
		numberField = new NumberField();
		numberField.getDocument().addDocumentListener(this);

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(numberField, gbc);
		controlPanel.add(numberField);

		// Label: title
		JLabel titleLabel = new FLabel(TITLE_STR);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(titleLabel, gbc);
		controlPanel.add(titleLabel);

		// Field: title
		titleField = new FTextField(TITLE_FIELD_NUM_COLUMNS);

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(titleField, gbc);
		controlPanel.add(titleField);

		// Label: prologue
		JLabel prologueLabel = new FLabel(PROLOGUE_STR);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.FIRST_LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(5, 3, 2, 3);
		gridBag.setConstraints(prologueLabel, gbc);
		controlPanel.add(prologueLabel);

		// Panel: prologue
		prologuePanel = new TextPanel(PROLOGUE_AREA_NUM_ROWS);
		prologuePanel.addDocumentListener(this);

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(prologuePanel, gbc);
		controlPanel.add(prologuePanel);

		// Label: epilogue
		JLabel epilogueLabel = new FLabel(EPILOGUE_STR);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.FIRST_LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(5, 3, 2, 3);
		gridBag.setConstraints(epilogueLabel, gbc);
		controlPanel.add(epilogueLabel);

		// Panel: epilogue
		epiloguePanel = new TextPanel(EPILOGUE_AREA_NUM_ROWS);
		epiloguePanel.addDocumentListener(this);

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(epiloguePanel, gbc);
		controlPanel.add(epiloguePanel);


		//----  Outer panel

		JPanel panel = new JPanel(gridBag);
		panel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

		// Set preferred focus owner
		panel.putClientProperty(PropertyKeys.PREFERRED_FOCUS_OWNER, numberField);

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.NORTH;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(0, 0, 0, 0);
		gridBag.setConstraints(controlPanel, gbc);
		panel.add(controlPanel);

		return panel;
	}

	//------------------------------------------------------------------

	private JPanel createPanelFile()
	{

		//----  Control panel

		GridBagLayout gridBag = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();

		JPanel controlPanel = new JPanel(gridBag);
		GuiUtils.setPaddedLineBorder(controlPanel);

		int gridY = 0;

		// Label: filename stem
		JLabel filenameStemLabel = new FLabel(FILENAME_STEM_STR);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(filenameStemLabel, gbc);
		controlPanel.add(filenameStemLabel);

		// Field: filename stem
		filenameStemField = new FTextField(FILENAME_FIELD_NUM_COLUMNS);

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(filenameStemField, gbc);
		controlPanel.add(filenameStemField);

		// Label: document directory
		JLabel documentDirectoryLabel = new FLabel(DOCUMENT_DIRECTORY_STR);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(documentDirectoryLabel, gbc);
		controlPanel.add(documentDirectoryLabel);

		// Panel: document directory
		documentDirectoryField = new FPathnameField();
		FPathnameField.addObserver(KEY, documentDirectoryField);
		JPanel documentDirectoryPanel = new PathnamePanel(documentDirectoryField,
														  Command.CHOOSE_DOCUMENT_DIRECTORY, this);

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(documentDirectoryPanel, gbc);
		controlPanel.add(documentDirectoryPanel);

		// Label: HTML directory
		JLabel htmlDirectoryLabel = new FLabel(HTML_DIRECTORY_STR);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(htmlDirectoryLabel, gbc);
		controlPanel.add(htmlDirectoryLabel);

		// Panel: HTML directory
		htmlDirectoryField = new FPathnameField();
		FPathnameField.addObserver(KEY, htmlDirectoryField);
		JPanel htmlDirectoryPanel = new PathnamePanel(htmlDirectoryField, Command.CHOOSE_HTML_DIRECTORY,
													  this);

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(htmlDirectoryPanel, gbc);
		controlPanel.add(htmlDirectoryPanel);


		//----  Outer panel

		JPanel panel = new JPanel(gridBag);
		panel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

		// Set preferred focus owner
		panel.putClientProperty(PropertyKeys.PREFERRED_FOCUS_OWNER, filenameStemField);

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.NORTH;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(0, 0, 0, 0);
		gridBag.setConstraints(controlPanel, gbc);
		panel.add(controlPanel);

		return panel;
	}

	//------------------------------------------------------------------

	private void onSelectGridSeparator()
	{
		updateComponents();
	}

	//------------------------------------------------------------------

	private void onChooseDocumentDirectory()
	{
		if (!documentDirectoryField.isEmpty())
			documentDirectoryChooser.setSelectedFile(documentDirectoryField.getCanonicalFile());
		documentDirectoryChooser.rescanCurrentDirectory();
		if (documentDirectoryChooser.showDialog(this, SELECT_STR) == JFileChooser.APPROVE_OPTION)
			documentDirectoryField.setFile(documentDirectoryChooser.getSelectedFile());
	}

	//------------------------------------------------------------------

	private void onChooseHtmlDirectory()
	{
		if (!htmlDirectoryField.isEmpty())
			htmlDirectoryChooser.setSelectedFile(htmlDirectoryField.getCanonicalFile());
		htmlDirectoryChooser.rescanCurrentDirectory();
		if (htmlDirectoryChooser.showDialog(this, SELECT_STR) == JFileChooser.APPROVE_OPTION)
			htmlDirectoryField.setFile(htmlDirectoryChooser.getSelectedFile());
	}

	//------------------------------------------------------------------

	private void onGetGridImage()
		throws AppException
	{
		// Get image from clipboard
		BufferedImage image = Utils.getClipboardImage();

		// Select grid
		Grid.Info gridInfo = null;
		if (autoGridDetectionCheckBox.isSelected())
		{
			try
			{
				double brightnessThreshold = (double)gridLineBrightnessThresholdSpinner.getIntValue()
																						* BRIGHTNESS_THRESHOLD_FACTOR;
				int minLineLength = gridLineMinLengthSpinner.getIntValue();
				int minLineSeparation = gridLineMinSeparationSpinner.getIntValue();
				int endpointTolerance = gridLineEndpointToleranceSpinner.getIntValue();
				gridInfo = Grid.findGrid(image, brightnessThreshold, minLineLength, minLineSeparation,
										 endpointTolerance);
				String messageStr = IndexedSub.sub(GRID_FOUND_STR, Integer.toString(gridInfo.numColumns),
												   Integer.toString(gridInfo.numRows));
				int result = JOptionPane.showConfirmDialog(this, messageStr, GET_GRID_IMAGE_STR,
														   JOptionPane.YES_NO_CANCEL_OPTION);
				if (result == JOptionPane.YES_OPTION)
					gridSizePanel.setValues(gridInfo.numColumns, gridInfo.numRows);
				else if (result != JOptionPane.NO_OPTION)
					return;
			}
			catch (AppException e)
			{
				JOptionPane.showMessageDialog(this, e, GET_GRID_IMAGE_STR, JOptionPane.WARNING_MESSAGE);
			}
		}

		// Select grid in image
		Dimension viewportSize = AppConfig.INSTANCE.getGridImageViewportSize();
		image = ImageRegionSelectionDialog.showDialog(this, SELECT_GRID_STR, viewportSize.width, viewportSize.height,
													  image, (gridInfo == null) ? null : gridInfo.getBounds());
		if (image != null)
		{
			gridImage = image;
			statusPanel.setGrid(true);
			updateComponents();
		}
	}

	//------------------------------------------------------------------

	private void onGetClues(String key)
		throws AppException
	{
		validateClueParams();
		String clueReferenceKeyword = clueReferenceKeywordField.isEmpty() ? null
																		  : clueReferenceKeywordField.getText();
		Clue.AnswerLengthParser answerLengthParser = answerLengthPanel.isPattern()
													? new Clue.AnswerLengthParser(answerLengthPanel.getPattern(),
																				  answerLengthPanel.getSubstitutions())
													: null;
		List<Clue> clues = Clue.getCluesFromClipboard(clueReferenceKeyword, answerLengthParser,
													  clueSubstitutionsPanel.getSubstitutions());
		if (!clues.isEmpty())
		{
			Direction direction = Direction.forKey(key);
			clueLists.put(direction, clues);
			statusPanel.setClues(direction, true);
			updateComponents();
		}
	}

	//------------------------------------------------------------------

	private void onClear()
		throws AppException
	{
		String[] optionStrs = Utils.getOptionStrings(AppConstants.CLEAR_STR);
		if (JOptionPane.showOptionDialog(this, CONFIRM_CLEAR_STR, AppConstants.CLEAR_STR, JOptionPane.OK_CANCEL_OPTION,
										 JOptionPane.QUESTION_MESSAGE, null, optionStrs, optionStrs[1])
																							== JOptionPane.OK_OPTION)
		{
			// Clear number field, prologue area and epilogue area
			numberField.setText(null);
			prologuePanel.clear();
			epiloguePanel.clear();

			// Clear grid image
			gridImage = null;
			statusPanel.setGrid(false);

			// Clear clue lists
			for (Direction direction : clueLists.keySet())
			{
				clueLists.remove(direction);
				statusPanel.setClues(direction, false);
			}

			// Update buttons
			updateComponents();
		}
	}

	//------------------------------------------------------------------

	private void onManageParameterSet()
		throws AppException
	{
		// Validate parameters
		validateClueParams();

		// If no parameter-set file is specified in the user preferences, prompt to choose one
		AppConfig config = AppConfig.INSTANCE;
		File file = config.getParameterSetFile();
		if ((file == null) &&
			(JOptionPane.showConfirmDialog(this, NO_PARAM_SET_FILE_STR, PARAMETER_SET_FILE_STR,
										   JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null)
																							== JOptionPane.OK_OPTION))
		{
			// Choose a parameter-set file
			parameterSetFileChooser.setSelectedFile(new File(""));
			parameterSetFileChooser.rescanCurrentDirectory();
			if (parameterSetFileChooser.showDialog(this, SELECT_STR) == JFileChooser.APPROVE_OPTION)
			{
				file = Utils.appendSuffix(parameterSetFileChooser.getSelectedFile(), AppConstants.XML_FILENAME_EXTENSION);
				if (file.exists() && !file.isFile())
					throw new FileException(ErrorId.NOT_A_FILE, file);
				config.setParameterSetPathname(Utils.getPathname(file));
			}
		}

		// Manage parameter sets
		if (file != null)
		{
			// Create a file if none exists
			if (!file.exists())
				CaptureParameterSetList.createFile(file);

			// Show the parameter-set dialog
			CaptureParams params = ParameterSetDialog.showDialog(this, CAPTURE_STR + " : " + PARAMETER_SET_STR,
																 getParams(), new CaptureParams(),
																 new CaptureParameterSetList(), file);

			// Update components with parameters
			if (params != null)
				setParams(params);

			// Set focus
			JComponent focusOwner = (JComponent)((JPanel)tabbedPanel.getSelectedComponent())
																.getClientProperty(PropertyKeys.PREFERRED_FOCUS_OWNER);
			if (focusOwner != null)
				focusOwner.requestFocusInWindow();
		}
	}

	//------------------------------------------------------------------

	private void onAccept()
		throws AppException
	{
		// Validate clue parameters
		validateClueParams();

		// Validate pathnames
		validatePathnames();

		// Set parameters
		params = getParams();

		// Create grid from image
		int numColumns = gridSizePanel.getValue1();
		int numRows = gridSizePanel.getValue2();
		int xOffset = xOffsetSpinner.getIntValue();
		int yOffset = yOffsetSpinner.getIntValue();
		int sampleSize = sampleSizeSpinner.getIntValue();
		int barWidthThreshold = barWidthThresholdSpinner.getIntValue();
		Grid grid = null;
		switch (getGridSeparator())
		{
			case BLOCK:
			{
				double brightnessThreshold = (double)blockBrightnessThresholdSpinner.getIntValue()
																						* BRIGHTNESS_THRESHOLD_FACTOR;
				grid = new BlockGrid(numColumns, numRows, gridImage, xOffset, yOffset, sampleSize,
									 brightnessThreshold);
				break;
			}

			case BAR:
			{
				double brightnessThreshold = (double)barBrightnessThresholdSpinner.getIntValue()
																						* BRIGHTNESS_THRESHOLD_FACTOR;
				grid = new BarGrid(numColumns, numRows, gridImage, xOffset, yOffset, brightnessThreshold,
								   barWidthThreshold);
				break;
			}
		}

		// Create document
		document = new CrosswordDocument(documentIndex);

		// Set grid in document
		document.setGrid(grid);

		// Set clue-processor variables in document
		if (!clueReferenceKeywordField.isEmpty())
			document.setClueReferenceKeyword(clueReferenceKeywordField.getText());
		if (answerLengthPanel.isPattern())
		{
			document.setAnswerLengthPattern(answerLengthPanel.getPattern());
			document.setAnswerLengthSubstitutions(answerLengthPanel.getSubstitutions());
		}
		document.setClueSubstitutions(clueSubstitutionsPanel.getSubstitutions());

		// Set clues in document
		for (Direction direction : clueLists.keySet())
			document.setClues(direction, clueLists.get(direction));

		// Update the directions of clue IDs and reference IDs
		document.updateClueDirections();

		// Validate clues
		if (document.validateClues(AppConstants.CANCEL_STR))
		{
			// Set title, prologue and epilogue in document
			if (!titleField.isEmpty())
				document.setTitle(getText(titleField));
			String lineBreak = AppConfig.INSTANCE.getTextSectionLineBreak();
			document.setPrologue(prologuePanel.getParagraphs(lineBreak));
			document.setEpilogue(epiloguePanel.getParagraphs(lineBreak));

			// Set filename stem and directories in document
			if (!filenameStemField.isEmpty())
				document.setFilenameStem(getText(filenameStemField));
			document.setDocumentDirectory(getDocumentDirectory());
			document.setHtmlDirectory(getHtmlDirectory());

			accepted = true;
			onClose();
		}
	}

	//------------------------------------------------------------------

	private void onClose()
	{
		FPathnameField.removeObservers(KEY);

		location = getLocation();
		tabIndex = tabbedPanel.getSelectedIndex();
		setVisible(false);
		dispose();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class variables
////////////////////////////////////////////////////////////////////////

	private static	CaptureParams	params		= new CaptureParams();
	private static	Point			location;
	private static	int				tabIndex	= Tab.TEXT.ordinal();

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private	boolean						accepted;
	private	int							documentIndex;
	private	CrosswordDocument			document;
	private	BufferedImage				gridImage;
	private	Map<Direction, List<Clue>>	clueLists;
	private	JTabbedPane					tabbedPanel;
	private	FIntegerSpinner				gridLineBrightnessThresholdSpinner;
	private	FIntegerSpinner				gridLineMinLengthSpinner;
	private	FIntegerSpinner				gridLineMinSeparationSpinner;
	private	FIntegerSpinner				gridLineEndpointToleranceSpinner;
	private	FComboBox<Grid.Separator>	gridSeparatorComboBox;
	private	DimensionsSpinnerPanel		gridSizePanel;
	private	JCheckBox					autoGridDetectionCheckBox;
	private	FIntegerSpinner				xOffsetSpinner;
	private	FIntegerSpinner				yOffsetSpinner;
	private	JPanel						gridSeparatorParamPanel;
	private	FIntegerSpinner				sampleSizeSpinner;
	private	FIntegerSpinner				blockBrightnessThresholdSpinner;
	private	FIntegerSpinner				barWidthThresholdSpinner;
	private	FIntegerSpinner				barBrightnessThresholdSpinner;
	private	FTextField					clueReferenceKeywordField;
	private	AnswerLengthPanel			answerLengthPanel;
	private	SubstitutionSelectionPanel	clueSubstitutionsPanel;
	private	NumberField					numberField;
	private	FTextField					titleField;
	private	TextPanel					prologuePanel;
	private	TextPanel					epiloguePanel;
	private	FTextField					filenameStemField;
	private	FPathnameField				documentDirectoryField;
	private	FPathnameField				htmlDirectoryField;
	private	JButton						clearButton;
	private	JButton						getGridImageButton;
	private	MenuButton					getCluesButton;
	private	JButton						okButton;
	private	StatusPanel					statusPanel;
	private	JFileChooser				documentDirectoryChooser;
	private	JFileChooser				htmlDirectoryChooser;
	private	JFileChooser				parameterSetFileChooser;

}

//----------------------------------------------------------------------
