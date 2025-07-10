/*====================================================================*\

TextSectionsDialog.java

Text sections dialog class.

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

import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import uk.blankaspect.common.exception.AppException;

import uk.blankaspect.common.exception2.UnexpectedRuntimeException;

import uk.blankaspect.ui.swing.action.KeyAction;

import uk.blankaspect.ui.swing.button.FButton;

import uk.blankaspect.ui.swing.label.FLabel;

import uk.blankaspect.ui.swing.misc.GuiUtils;

import uk.blankaspect.ui.swing.textfield.FTextField;

//----------------------------------------------------------------------


// TEXT SECTIONS DIALOG CLASS


class TextSectionsDialog
	extends JDialog
	implements ActionListener
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	private static final	int	TITLE_FIELD_NUM_COLUMNS	= 32;

	private static final	int	PROLOGUE_AREA_NUM_ROWS	= 6;
	private static final	int	EPILOGUE_AREA_NUM_ROWS	= 6;

	private static final	String	TEXT_SECTIONS_STR	= "Text sections";
	private static final	String	TITLE_STR			= "Title";
	private static final	String	PROLOGUE_STR		= "Prologue";
	private static final	String	EPILOGUE_STR		= "Epilogue";

	// Commands
	private interface Command
	{
		String	ACCEPT	= "accept";
		String	CLOSE	= "close";
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

		private Result(String       title,
					   List<String> prologueParagraphs,
					   List<String> epilogueParagraphs)
		{
			this.title = title;
			this.prologueParagraphs = prologueParagraphs;
			this.epilogueParagraphs = epilogueParagraphs;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		String			title;
		List<String>	prologueParagraphs;
		List<String>	epilogueParagraphs;

	}

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	private TextSectionsDialog(Window       owner,
							   String       title,
							   String       lineBreak,
							   List<String> prologueParagraphs,
							   List<String> epilogueParagraphs)
	{
		// Call superclass constructor
		super(owner, TEXT_SECTIONS_STR, ModalityType.APPLICATION_MODAL);

		// Set icons
		setIconImages(owner.getIconImages());

		// Initialise instance variables
		this.lineBreak = lineBreak;


		//----  Control panel

		GridBagLayout gridBag = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();

		JPanel controlPanel = new JPanel(gridBag);
		GuiUtils.setPaddedLineBorder(controlPanel);

		int gridY = 0;

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
		titleField = new FTextField(title, TITLE_FIELD_NUM_COLUMNS);

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
		prologuePanel = new TextPanel(PROLOGUE_AREA_NUM_ROWS, prologueParagraphs);

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
		epiloguePanel = new TextPanel(EPILOGUE_AREA_NUM_ROWS, epilogueParagraphs);

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

	public static Result showDialog(Component    parent,
									String       title,
									String       lineBreak,
									List<String> prologueParagraphs,
									List<String> epilogueParagraphs)
	{
		return new TextSectionsDialog(GuiUtils.getWindow(parent), title, lineBreak,
									  prologueParagraphs, epilogueParagraphs).getResult();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : ActionListener interface
////////////////////////////////////////////////////////////////////////

	public void actionPerformed(ActionEvent event)
	{
		String command = event.getActionCommand();

		if (command.equals(Command.ACCEPT))
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
		Result result = null;
		if (accepted)
		{
			try
			{
				result = new Result(titleField.getText(), prologuePanel.getParagraphs(lineBreak),
									epiloguePanel.getParagraphs(lineBreak));
			}
			catch (StyledText.ParseException e)
			{
				throw new UnexpectedRuntimeException(e);
			}
		}
		return result;
	}

	//------------------------------------------------------------------

	private void validateUserInput()
		throws AppException
	{
		prologuePanel.getParagraphs(lineBreak);
		epiloguePanel.getParagraphs(lineBreak);
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
			JOptionPane.showMessageDialog(this, e, TEXT_SECTIONS_STR, JOptionPane.ERROR_MESSAGE);
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

	private	boolean		accepted;
	private	String		lineBreak;
	private	FTextField	titleField;
	private	TextPanel	prologuePanel;
	private	TextPanel	epiloguePanel;

}

//----------------------------------------------------------------------
