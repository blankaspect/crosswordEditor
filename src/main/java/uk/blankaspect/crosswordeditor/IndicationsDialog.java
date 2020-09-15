/*====================================================================*\

IndicationsDialog.java

Indications dialog box class.

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

import java.util.ArrayList;
import java.util.List;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import uk.blankaspect.common.exception.AppException;

import uk.blankaspect.common.misc.MaxValueMap;

import uk.blankaspect.common.regex.RegexUtils;
import uk.blankaspect.common.regex.Substitution;

import uk.blankaspect.common.swing.action.KeyAction;

import uk.blankaspect.common.swing.button.FButton;

import uk.blankaspect.common.swing.label.FixedWidthLabel;

import uk.blankaspect.common.swing.misc.GuiUtils;

import uk.blankaspect.common.swing.textfield.FTextField;

//----------------------------------------------------------------------


// INDICATIONS DIALOG BOX CLASS


class IndicationsDialog
	extends JDialog
	implements ActionListener, AnswerLengthPanel.LabelSource
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	private static final	int	CLUE_REFERENCE_KEYWORD_NUM_COLUMNS	= 12;
	private static final	int	LINE_BREAK_FIELD_NUM_COLUMNS		= 4;

	private static final	String	TITLE_STR			= "Indications";
	private static final	String	CLUE_REFERENCE_STR	= "Clue reference";
	private static final	String	LINE_BREAK_STR		= "Line break";

	// Commands
	private interface Command
	{
		String	ACCEPT	= "accept";
		String	CLOSE	= "close";
	}

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


	// PARAMETERS CLASS


	public static class Params
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		public Params(String             clueReferenceKeyword,
					  String             answerLengthPattern,
					  List<Substitution> answerLengthSubstitutions,
					  String             lineBreak)
		{
			this.clueReferenceKeyword = clueReferenceKeyword;
			this.answerLengthPattern = answerLengthPattern;
			this.answerLengthSubstitutions = answerLengthSubstitutions;
			this.lineBreak = lineBreak;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		String				clueReferenceKeyword;
		String				answerLengthPattern;
		List<Substitution>	answerLengthSubstitutions;
		String				lineBreak;

	}

	//==================================================================


	// INDICATIONS LABEL CLASS


	private static class Label
		extends FixedWidthLabel
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		private static final	String	KEY	= Label.class.getCanonicalName();

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private Label(String text)
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

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	private IndicationsDialog(Window owner,
							  Params params)
	{

		// Call superclass constructor
		super(owner, TITLE_STR, Dialog.ModalityType.APPLICATION_MODAL);

		// Set icons
		setIconImages(owner.getIconImages());

		// Reset fixed-width labels
		Label.reset();


		//----  Control panel

		GridBagLayout gridBag = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();

		JPanel controlPanel = new JPanel(gridBag);
		GuiUtils.setPaddedLineBorder(controlPanel);

		int gridY = 0;

		// Label: clue reference
		JLabel clueReferenceLabel = new Label(CLUE_REFERENCE_STR);

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
		clueReferenceKeywordField.setText(params.clueReferenceKeyword);

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

		// Label: line break
		JLabel lineBreakLabel = new Label(LINE_BREAK_STR);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(lineBreakLabel, gbc);
		controlPanel.add(lineBreakLabel);

		// Field: line break
		lineBreakField = new FTextField(params.lineBreak, LINE_BREAK_FIELD_NUM_COLUMNS);

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 1.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(lineBreakField, gbc);
		controlPanel.add(lineBreakField);


		//----  Answer length panel

		answerLengthPanel = new AnswerLengthPanel(this, params.answerLengthPattern,
												  params.answerLengthSubstitutions);

		// Update widths of labels
		Label.update();


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
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(3, 0, 0, 0);
		gridBag.setConstraints(answerLengthPanel, gbc);
		mainPanel.add(answerLengthPanel);

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

	public static Params showDialog(Component parent,
									Params    params)
	{
		return new IndicationsDialog(GuiUtils.getWindow(parent), params).getResult();
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
//  Instance methods : AnswerLengthPanel.LabelSource interface
////////////////////////////////////////////////////////////////////////

	public JLabel createLabel(String text)
	{
		return new Label(text);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	private Params getResult()
	{
		Params result = null;
		if (accepted)
		{
			String clueReferenceKeyword = clueReferenceKeywordField.isEmpty()
																	? null
																	: clueReferenceKeywordField.getText();
			String answerLengthPattern = null;
			List<Substitution> answerLengthSubstitutions = new ArrayList<>();
			if (answerLengthPanel.isPattern())
			{
				answerLengthPattern = answerLengthPanel.getPattern();
				answerLengthSubstitutions = answerLengthPanel.getSubstitutions();
			}
			String lineBreak = lineBreakField.isEmpty() ? null : lineBreakField.getText();
			result = new Params(clueReferenceKeyword, answerLengthPattern, answerLengthSubstitutions,
								lineBreak);
		}
		return result;
	}

	//------------------------------------------------------------------

	private void validateUserInput()
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

	private	boolean				accepted;
	private	FTextField			clueReferenceKeywordField;
	private	FTextField			lineBreakField;
	private	AnswerLengthPanel	answerLengthPanel;

}

//----------------------------------------------------------------------
