/*====================================================================*\

AnswerLengthPanel.java

Answer-length panel class.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.crosswordeditor;

//----------------------------------------------------------------------


// IMPORTS


import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import uk.blankaspect.common.regex.Substitution;

import uk.blankaspect.ui.swing.border.TitledBorder;

import uk.blankaspect.ui.swing.button.FButton;

import uk.blankaspect.ui.swing.textfield.FTextField;

//----------------------------------------------------------------------


// ANSWER-LENGTH PANEL CLASS


class AnswerLengthPanel
	extends JPanel
	implements ActionListener, DocumentListener
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	private static final	int	PATTERN_NUM_COLUMNS		= 16;
	private static final	int	SUBSTITUTIONS_NUM_ROWS	= 6;

	private static final	Insets	BUTTON_MARGINS	= new Insets(2, 6, 2, 6);

	private static final	String	ANSWER_LENGTH_STR	= "Answer length";
	private static final	String	PATTERN_STR			= "Pattern";
	private static final	String	DEFAULT_STR			= "Default";
	private static final	String	SUBSTITUTIONS_STR	= "Substitutions";

	// Commands
	private interface Command
	{
		String	SET_DEFAULT_PATTERN	= "setDefaultPattern";
	}

////////////////////////////////////////////////////////////////////////
//  Member interfaces
////////////////////////////////////////////////////////////////////////


	// LABEL SOURCE CLASS


	interface LabelSource
	{

	////////////////////////////////////////////////////////////////////
	//  Methods
	////////////////////////////////////////////////////////////////////

		public JLabel createLabel(String text);

		//--------------------------------------------------------------

	}

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	public AnswerLengthPanel(LabelSource labelSource)
	{
		this(labelSource, null, null);
	}

	//------------------------------------------------------------------

	public AnswerLengthPanel(LabelSource        labelSource,
							 String             pattern,
							 List<Substitution> substitutions)
	{
		// Set layout manager and border
		GridBagLayout gridBag = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();

		setLayout(gridBag);
		TitledBorder.setPaddedBorder(this, ANSWER_LENGTH_STR, 8);

		int gridY = 0;

		// Label: pattern
		JLabel patternLabel = labelSource.createLabel(PATTERN_STR);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(patternLabel, gbc);
		add(patternLabel);

		// Panel: pattern
		JPanel patternPanel = new JPanel(gridBag);

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 1.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(patternPanel, gbc);
		add(patternPanel);

		// Field: pattern
		patternField = new FTextField(pattern, PATTERN_NUM_COLUMNS);
		patternField.getDocument().addDocumentListener(this);

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(0, 0, 0, 0);
		gridBag.setConstraints(patternField, gbc);
		patternPanel.add(patternField);

		// Button: default
		JButton defaultButton = new FButton(DEFAULT_STR);
		defaultButton.setMargin(BUTTON_MARGINS);
		defaultButton.setActionCommand(Command.SET_DEFAULT_PATTERN);
		defaultButton.addActionListener(this);

		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(0, 6, 0, 0);
		gridBag.setConstraints(defaultButton, gbc);
		patternPanel.add(defaultButton);

		// Panel: substitutions
		JPanel substitutionsOuterPanel = new JPanel(gridBag);

		gbc.gridx = 0;
		gbc.gridy = gridY++;
		gbc.gridwidth = 2;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(3, 0, 0, 0);
		gridBag.setConstraints(substitutionsOuterPanel, gbc);
		add(substitutionsOuterPanel);

		// Label: substitutions
		JLabel substitutionsLabel = labelSource.createLabel(SUBSTITUTIONS_STR);

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.FIRST_LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(substitutionsLabel, gbc);
		substitutionsOuterPanel.add(substitutionsLabel);

		// Panel: substitution selection
		substitutionsPanel = new SubstitutionSelectionPanel(SUBSTITUTIONS_NUM_ROWS);
		if (substitutions != null)
			substitutionsPanel.setSubstitutions(substitutions);

		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.FIRST_LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(0, 0, 0, 0);
		gridBag.setConstraints(substitutionsPanel, gbc);
		substitutionsOuterPanel.add(substitutionsPanel);

		// Update components
		updateSubstitutionsPanel();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : ActionListener interface
////////////////////////////////////////////////////////////////////////

	public void actionPerformed(ActionEvent event)
	{
		if (event.getActionCommand().equals(Command.SET_DEFAULT_PATTERN))
			patternField.setText(Clue.DEFAULT_LENGTH_REGEX);
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
		updateSubstitutionsPanel();
	}

	//------------------------------------------------------------------

	public void removeUpdate(DocumentEvent event)
	{
		updateSubstitutionsPanel();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	public FTextField getPatternField()
	{
		return patternField;
	}

	//------------------------------------------------------------------

	public boolean isPattern()
	{
		return !patternField.isEmpty();
	}

	//------------------------------------------------------------------

	public String getPattern()
	{
		return patternField.getText();
	}

	//------------------------------------------------------------------

	public List<Substitution> getSubstitutions()
	{
		return substitutionsPanel.getSubstitutions();
	}

	//------------------------------------------------------------------

	public void setPattern(String pattern)
	{
		patternField.setText(pattern);
	}

	//------------------------------------------------------------------

	public void setSubstitutions(List<Substitution> substitutions)
	{
		substitutionsPanel.setSubstitutions(substitutions);
	}

	//------------------------------------------------------------------

	private void updateSubstitutionsPanel()
	{
		substitutionsPanel.setEnabled(!patternField.isEmpty());
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private	FTextField					patternField;
	private	SubstitutionSelectionPanel	substitutionsPanel;

}

//----------------------------------------------------------------------
