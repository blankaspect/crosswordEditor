/*====================================================================*\

ImportCluesDialog.java

Import clues dialog class.

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

import java.awt.datatransfer.FlavorEvent;
import java.awt.datatransfer.FlavorListener;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import uk.blankaspect.common.exception.AppException;

import uk.blankaspect.common.regex.Substitution;

import uk.blankaspect.common.string.StringUtils;

import uk.blankaspect.ui.swing.action.KeyAction;

import uk.blankaspect.ui.swing.border.TitledBorder;

import uk.blankaspect.ui.swing.button.FButton;
import uk.blankaspect.ui.swing.button.MenuButton;

import uk.blankaspect.ui.swing.misc.GuiUtils;

import uk.blankaspect.ui.swing.text.TextUtils;

import uk.blankaspect.ui.swing.textfield.InformationField;

//----------------------------------------------------------------------


// IMPORT CLUES DIALOG CLASS


class ImportCluesDialog
	extends JDialog
	implements ActionListener, FlavorListener
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	private static final	int	CLUE_SUBSTITUTIONS_NUM_ROWS	= 8;

	private static final	Insets	BUTTON_MARGINS	= new Insets(2, 8, 2, 8);

	private static final	String	TITLE_STR			= "Import clues";
	private static final	String	SUBSTITUTIONS_STR	= "Substitutions";
	private static final	String	GET_CLUES_STR		= "Get clues";

	// Commands
	private interface Command
	{
		String	GET_CLUES	= "getClues.";
		String	ACCEPT		= "accept";
		String	CLOSE		= "close";
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

		public Result(Map<Direction, List<Clue>> clues,
					  List<Substitution>         substitutions)
		{
			this.clues = clues;
			this.substitutions = substitutions;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		Map<Direction, List<Clue>>	clues;
		List<Substitution>			substitutions;

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
			addActionListener(ImportCluesDialog.this);
		}

		//--------------------------------------------------------------

	}

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	private ImportCluesDialog(Window                  owner,
							  EnumSet<Direction>      directions,
							  String                  clueReferenceKeyword,
							  Clue.AnswerLengthParser answerLengthParser,
							  List<Substitution>      clueSubstitutions)
	{

		// Call superclass constructor
		super(owner, TITLE_STR, Dialog.ModalityType.APPLICATION_MODAL);

		// Set icons
		setIconImages(owner.getIconImages());

		// Initialise instance variables
		this.clueReferenceKeyword = clueReferenceKeyword;
		this.answerLengthParser = answerLengthParser;
		clueLists = new EnumMap<>(Direction.class);


		//----  Clue substitutions panel

		clueSubstitutionsPanel = new SubstitutionSelectionPanel(CLUE_SUBSTITUTIONS_NUM_ROWS);
		TitledBorder.setPaddedBorder(clueSubstitutionsPanel, SUBSTITUTIONS_STR, 8);
		clueSubstitutionsPanel.setSubstitutions(clueSubstitutions);


		//----  Information panel

		GridBagLayout gridBag = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();

		JPanel infoPanel = new JPanel(gridBag);
		GuiUtils.setPaddedLineBorder(infoPanel);

		int gridX = 0;

		List<String> strs = new ArrayList<>();
		for (Direction direction : directions)
			strs.add(direction.toString());
		String str = TextUtils.getWidestString(getFontMetrics(AppFont.MAIN.getFont()), strs).str;
		informationFields = new EnumMap<>(Direction.class);
		for (Direction direction : directions)
		{
			InformationField field = new InformationField(str);
			field.setHorizontalAlignment(InformationField.Alignment.CENTRE);
			informationFields.put(direction, field);

			gbc.gridx = gridX++;
			gbc.gridy = 0;
			gbc.gridwidth = 1;
			gbc.gridheight = 1;
			gbc.weightx = 0.0;
			gbc.weighty = 0.0;
			gbc.anchor = GridBagConstraints.LINE_START;
			gbc.fill = GridBagConstraints.NONE;
			gbc.insets = new Insets(0, 4, 0, 4);
			gridBag.setConstraints(field, gbc);
			infoPanel.add(field);
		}


		//----  Button panel: import commands

		JPanel importButtonPanel = new JPanel(new GridLayout(1, 0, 8, 0));

		// Button: get clues
		getCluesButton = new MenuButton(GET_CLUES_STR);
		getCluesButton.setMargin(BUTTON_MARGINS);
		getCluesButton.setMnemonic(KeyEvent.VK_C);
		for (Direction direction : directions)
			getCluesButton.addMenuItem(new MenuItem(direction));
		importButtonPanel.add(getCluesButton);


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

		JPanel buttonPanel = new JPanel(gridBag);
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(3, 24, 3, 24));

		gridX = 0;

		gbc.gridx = gridX++;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.5;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(0, 0, 0, 12);
		gridBag.setConstraints(importButtonPanel, gbc);
		buttonPanel.add(importButtonPanel);

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


		//----  Main panel

		JPanel mainPanel = new JPanel(gridBag);
		mainPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

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
		gridBag.setConstraints(clueSubstitutionsPanel, gbc);
		mainPanel.add(clueSubstitutionsPanel);

		gbc.gridx = 0;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.NORTH;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(3, 0, 0, 0);
		gridBag.setConstraints(infoPanel, gbc);
		mainPanel.add(infoPanel);

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

		// Respond to changes to data flavours on system clipboard
		getToolkit().getSystemClipboard().addFlavorListener(this);

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

	public static Result showDialog(Component               parent,
									EnumSet<Direction>      directions,
									String                  clueReferenceKeyword,
									Clue.AnswerLengthParser answerLengthParser,
									List<Substitution>      clueSubstitutions)
	{
		return new ImportCluesDialog(GuiUtils.getWindow(parent), directions, clueReferenceKeyword,
									 answerLengthParser, clueSubstitutions).getResult();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : ActionListener interface
////////////////////////////////////////////////////////////////////////

	public void actionPerformed(ActionEvent event)
	{
		String command = event.getActionCommand();

		if (command.startsWith(Command.GET_CLUES))
			onGetClues(StringUtils.removePrefix(command, Command.GET_CLUES));

		else if (command.equals(Command.ACCEPT))
			onAccept();

		else if (command.equals(Command.CLOSE))
			onClose();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : FlavorListener interface
////////////////////////////////////////////////////////////////////////

	public void flavorsChanged(FlavorEvent event)
	{
		updateComponents();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	private Result getResult()
	{
		return (accepted ? new Result(clueLists, clueSubstitutionsPanel.getSubstitutions()) : null);
	}

	//------------------------------------------------------------------

	private void updateComponents()
	{
		getCluesButton.setEnabled(Utils.clipboardHasText());
		okButton.setEnabled(!clueLists.isEmpty());
	}

	//------------------------------------------------------------------

	private void onGetClues(String key)
	{
		try
		{
			List<Clue> clues = Clue.getCluesFromClipboard(clueReferenceKeyword, answerLengthParser,
														  clueSubstitutionsPanel.getSubstitutions());
			if (!clues.isEmpty())
			{
				Direction direction = Direction.forKey(key);
				clueLists.put(direction, clues);
				informationFields.get(direction).setText(direction.toString());
				updateComponents();
			}
		}
		catch (AppException e)
		{
			JOptionPane.showMessageDialog(this, e, TITLE_STR, JOptionPane.ERROR_MESSAGE);
		}
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

	private	boolean								accepted;
	private	String								clueReferenceKeyword;
	private	Clue.AnswerLengthParser				answerLengthParser;
	private	Map<Direction, List<Clue>>			clueLists;
	private	SubstitutionSelectionPanel			clueSubstitutionsPanel;
	private	Map<Direction, InformationField>	informationFields;
	private	MenuButton							getCluesButton;
	private	JButton								okButton;

}

//----------------------------------------------------------------------
