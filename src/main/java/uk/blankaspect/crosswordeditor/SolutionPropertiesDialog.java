/*====================================================================*\

SolutionPropertiesDialog.java

Solution properties dialog box class.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.crosswordeditor;

//----------------------------------------------------------------------


// IMPORTS


import java.awt.Color;
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
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import uk.blankaspect.common.exception.AppException;

import uk.blankaspect.common.swing.action.KeyAction;

import uk.blankaspect.common.swing.button.FButton;

import uk.blankaspect.common.swing.checkbox.FCheckBox;

import uk.blankaspect.common.swing.container.PassphrasePanel;

import uk.blankaspect.common.swing.label.FLabel;

import uk.blankaspect.common.swing.misc.GuiUtils;

import uk.blankaspect.common.swing.textfield.FTextField;
import uk.blankaspect.common.swing.textfield.PasswordField;

//----------------------------------------------------------------------


// SOLUTION PROPERTIES DIALOG BOX CLASS


class SolutionPropertiesDialog
	extends JDialog
	implements ActionListener, DocumentListener, MouseListener
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	private static final	int	PASSPHRASE_MAX_LENGTH	= 1024;

	private static final	int	LOCATION_FIELD_NUM_COLUMNS		= 40;
	private static final	int	PASSPHRASE_FIELD_NUM_COLUMNS	= 40;

	private static final	String	TITLE_STR						= "Solution properties";
	private static final	String	SAVE_SOLUTION_WITH_DOCUMENT_STR	= "Save solution with document";
	private static final	String	LOCATION_STR					= "Location";
	private static final	String	PASSPHRASE_STR					= "Passphrase";

	private static final	Color	PASSPHRASE_FIELD_EMPTY_BACKGROUND	= new Color(244, 240, 216);

	// Commands
	private interface Command
	{
		String	TOGGLE_SAVE_SOLUTION_WITH_DOCUMENT	= "toggleSaveSolutionWithDocument";
		String	SHOW_CONTEXT_MENU					= "showContextMenu";
		String	ACCEPT								= "accept";
		String	CLOSE								= "close";
	}

	private static final	KeyAction.KeyCommandPair[]	KEY_COMMANDS	=
	{
		new KeyAction.KeyCommandPair(KeyStroke.getKeyStroke(KeyEvent.VK_CONTEXT_MENU, 0),
									 Command.SHOW_CONTEXT_MENU),
		new KeyAction.KeyCommandPair(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
									 Command.CLOSE)
	};

////////////////////////////////////////////////////////////////////////
//  Enumerated types
////////////////////////////////////////////////////////////////////////


	// ERROR IDENTIFIERS


	private enum ErrorId
		implements AppException.IId
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		MALFORMED_URL
		("The URL is malformed.");

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
//  Constructors
////////////////////////////////////////////////////////////////////////

	private SolutionPropertiesDialog(Window                               owner,
									 CrosswordDocument.SolutionProperties properties)
	{
		// Call superclass constructor
		super(owner, TITLE_STR, Dialog.ModalityType.APPLICATION_MODAL);

		// Set icons
		setIconImages(owner.getIconImages());

		// Initialise instance variables
		hashValue = properties.getHashValue();


		//----  Control panel

		GridBagLayout gridBag = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();

		JPanel controlPanel = new JPanel(gridBag);
		GuiUtils.setPaddedLineBorder(controlPanel);

		int gridY = 0;

		// Check box: save solution with document
		saveSolutionWithDocumentCheckBox = new FCheckBox(SAVE_SOLUTION_WITH_DOCUMENT_STR);
		saveSolutionWithDocumentCheckBox.setSelected(properties.getLocation() == null);
		saveSolutionWithDocumentCheckBox.setActionCommand(Command.TOGGLE_SAVE_SOLUTION_WITH_DOCUMENT);
		saveSolutionWithDocumentCheckBox.addActionListener(this);

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(saveSolutionWithDocumentCheckBox, gbc);
		controlPanel.add(saveSolutionWithDocumentCheckBox);

		// Label: location
		JLabel locationLabel = new FLabel(LOCATION_STR);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(locationLabel, gbc);
		controlPanel.add(locationLabel);

		// Field: location
		locationField = new FTextField(LOCATION_FIELD_NUM_COLUMNS);
		if (properties.getLocation() != null)
			locationField.setText(properties.getLocation().toString());

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(locationField, gbc);
		controlPanel.add(locationField);

		// Label: passphrase
		JLabel passphraseLabel = new FLabel(PASSPHRASE_STR);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(passphraseLabel, gbc);
		controlPanel.add(passphraseLabel);

		// Panel: passphrase
		passphrasePanel = new PassphrasePanel(PASSPHRASE_MAX_LENGTH, PASSPHRASE_FIELD_NUM_COLUMNS, true);
		PasswordField field = passphrasePanel.getField();
		passphraseFieldBackgroundColour = field.getBackground();
		field.setText(properties.getPassphrase());
		field.addMouseListener(this);
		field.getDocument().addDocumentListener(this);

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(passphrasePanel, gbc);
		controlPanel.add(passphrasePanel);


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
		KeyAction.create(mainPanel, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, this, KEY_COMMANDS);

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

	public static CrosswordDocument.SolutionProperties showDialog(Component                            parent,
																  CrosswordDocument.SolutionProperties properties)
	{
		return new SolutionPropertiesDialog(GuiUtils.getWindow(parent), properties).getResult();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : ActionListener interface
////////////////////////////////////////////////////////////////////////

	public void actionPerformed(ActionEvent event)
	{
		String command = event.getActionCommand();

		if (command.equals(Command.TOGGLE_SAVE_SOLUTION_WITH_DOCUMENT))
			onToggleSaveSolutionWithDocument();

		else if (command.equals(Command.SHOW_CONTEXT_MENU))
			onShowContextMenu();

		else if (command.equals(Command.ACCEPT))
			onAccept();

		else if (command.equals(Command.CLOSE))
			onClose();
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
		updatePassphraseField();
	}

	//------------------------------------------------------------------

	public void removeUpdate(DocumentEvent event)
	{
		updatePassphraseField();
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

	private CrosswordDocument.SolutionProperties getResult()
	{
		CrosswordDocument.SolutionProperties result = null;
		if (accepted)
		{
			try
			{
				result = new CrosswordDocument.SolutionProperties(getUrl(), passphrasePanel.getPassphrase(), hashValue);
			}
			catch (MalformedURLException e)
			{
				// not expected
			}
		}
		return result;
	}

	//------------------------------------------------------------------

	private URL getUrl()
		throws MalformedURLException
	{
		return ((locationField.isEnabled() && !locationField.isEmpty()) ? new URL(locationField.getText()) : null);
	}

	//------------------------------------------------------------------

	private void updateLocationField()
	{
		locationField.setEnabled(!saveSolutionWithDocumentCheckBox.isSelected());
	}

	//------------------------------------------------------------------

	private void updatePassphraseField()
	{
		PasswordField field = passphrasePanel.getField();
		field.setBackground(field.isEmpty() ? PASSPHRASE_FIELD_EMPTY_BACKGROUND : passphraseFieldBackgroundColour);
	}

	//------------------------------------------------------------------

	private void updateComponents()
	{
		updateLocationField();
		updatePassphraseField();
	}

	//------------------------------------------------------------------

	private void showContextMenu(MouseEvent event)
	{
		if (passphrasePanel.isEnabled())
		{
			if (event == null)
				passphrasePanel.getContextMenu().show(getContentPane(), 0, 0);
			else if (event.isPopupTrigger())
				passphrasePanel.getContextMenu().show(event.getComponent(), event.getX(), event.getY());
		}
	}

	//------------------------------------------------------------------

	private void validateUserInput()
		throws AppException
	{
		try
		{
			getUrl();
		}
		catch (MalformedURLException e)
		{
			GuiUtils.setFocus(locationField);
			throw new AppException(ErrorId.MALFORMED_URL);
		}
	}

	//------------------------------------------------------------------

	private void onShowContextMenu()
	{
		showContextMenu(null);
	}

	//------------------------------------------------------------------

	private void onToggleSaveSolutionWithDocument()
	{
		updateLocationField();
	}

	//------------------------------------------------------------------

	private void onAccept()
	{
		try
		{
			validateUserInput();
			accepted = true;
			onClose();
		}
		catch (AppException e)
		{
			JOptionPane.showMessageDialog(this, e, TITLE_STR, JOptionPane.ERROR_MESSAGE);
		}
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

	private	boolean			accepted;
	private	byte[]			hashValue;
	private	JCheckBox		saveSolutionWithDocumentCheckBox;
	private	FTextField		locationField;
	private	PassphrasePanel	passphrasePanel;
	private	Color			passphraseFieldBackgroundColour;

}

//----------------------------------------------------------------------
