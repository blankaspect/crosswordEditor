/*====================================================================*\

ClearCluesDialog.java

Class: "clear clues" dialog.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.crosswordeditor;

//----------------------------------------------------------------------


// IMPORTS


import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
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
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.KeyStroke;
import javax.swing.UIManager;

import uk.blankaspect.ui.swing.action.KeyAction;

import uk.blankaspect.ui.swing.button.FButton;

import uk.blankaspect.ui.swing.label.FLabel;

import uk.blankaspect.ui.swing.misc.GuiUtils;

import uk.blankaspect.ui.swing.workaround.LinuxWorkarounds;

//----------------------------------------------------------------------


// CLASS: "CLEAR CLUES" DIALOG


class ClearCluesDialog
	extends JDialog
	implements ActionListener
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	private static final	Color	RADIO_BUTTON_BORDER_COLOUR	= new Color(200, 208, 216);

	private static final	String	TITLE_STR		= "Clear all clues";
	private static final	String	QUESTION_STR	= "How should the clues be cleared?";
	private static final	String	NO_CLUE1_STR	= "No clue";
	private static final	String	NO_CLUE2_STR	= "The corresponding grid entry cannot be edited.";
	private static final	String	EMPTY_CLUE1_STR	= "Empty clue";
	private static final	String	EMPTY_CLUE2_STR	= "The corresponding grid entry can be edited.";
	private static final	String	CLEAR_STR		= "Clear";

	private static final	String	QUESTION_ICON_KEY	= "OptionPane.questionIcon";

	// Commands
	private interface Command
	{
		String	ACCEPT	= "accept";
		String	CLOSE	= "close";
	}

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	private ClearCluesDialog(Window owner)
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

		// Label: icon
		JLabel iconLabel = new JLabel(UIManager.getIcon(QUESTION_ICON_KEY));

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(4, 4, 6, 8);
		gridBag.setConstraints(iconLabel, gbc);
		controlPanel.add(iconLabel);

		// Label: question
		JLabel questionLabel = new FLabel(QUESTION_STR);

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(4, 4, 6, 0);
		gridBag.setConstraints(questionLabel, gbc);
		controlPanel.add(questionLabel);

		// Create button group
		ButtonGroup group = new ButtonGroup();

		// Panel: no clue
		JPanel noCluePanel = new JPanel(gridBag);
		GuiUtils.setPaddedLineBorder(noCluePanel, 0, 0, RADIO_BUTTON_BORDER_COLOUR);

		gbc.gridx = 0;
		gbc.gridy = gridY++;
		gbc.gridwidth = 2;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(noCluePanel, gbc);
		controlPanel.add(noCluePanel);

		// Radio button: no clue
		noClueRadioButton = new JRadioButton(NO_CLUE1_STR);
		noClueRadioButton.setFont(AppFont.MAIN.getFont().deriveFont(Font.BOLD));
		group.add(noClueRadioButton);

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(2, 3, 0, 6);
		gridBag.setConstraints(noClueRadioButton, gbc);
		noCluePanel.add(noClueRadioButton);

		// Label: no clue
		JLabel noClueLabel = new FLabel(NO_CLUE2_STR);
		int dx = noClueRadioButton.getPreferredSize().width
							- noClueRadioButton.getFontMetrics(noClueRadioButton.getFont()).stringWidth(NO_CLUE1_STR);

		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 1.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(0, dx, 6, 6);
		gridBag.setConstraints(noClueLabel, gbc);
		noCluePanel.add(noClueLabel);

		// Panel: empty clue
		JPanel emptyCluePanel = new JPanel(gridBag);
		GuiUtils.setPaddedLineBorder(emptyCluePanel, 0, 0, RADIO_BUTTON_BORDER_COLOUR);

		gbc.gridx = 0;
		gbc.gridy = gridY++;
		gbc.gridwidth = 2;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(emptyCluePanel, gbc);
		controlPanel.add(emptyCluePanel);

		// Radio button: empty clue
		emptyClueRadioButton = new JRadioButton(EMPTY_CLUE1_STR);
		emptyClueRadioButton.setFont(AppFont.MAIN.getFont().deriveFont(Font.BOLD));
		group.add(emptyClueRadioButton);

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(2, 3, 0, 6);
		gridBag.setConstraints(emptyClueRadioButton, gbc);
		emptyCluePanel.add(emptyClueRadioButton);

		// Label: empty clue
		JLabel emptyClueLabel = new FLabel(EMPTY_CLUE2_STR);

		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 1.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(0, dx, 6, 6);
		gridBag.setConstraints(emptyClueLabel, gbc);
		emptyCluePanel.add(emptyClueLabel);

		// Select radio button
		if (clearWithEmptyClue)
			emptyClueRadioButton.setSelected(true);
		else
			noClueRadioButton.setSelected(true);


		//----  Button panel

		JPanel buttonPanel = new JPanel(new GridLayout(1, 0, 8, 0));
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(3, 12, 3, 12));

		// Button: clear
		JButton clearButton = new FButton(CLEAR_STR);
		clearButton.setActionCommand(Command.ACCEPT);
		clearButton.addActionListener(this);
		buttonPanel.add(clearButton);

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

		// Handle window events
		addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowOpened(
				WindowEvent	event)
			{
				// WORKAROUND for a bug that has been observed on Linux/GNOME whereby a window is displaced downwards
				// when its location is set.  The error in the y coordinate is the height of the title bar of the
				// window.  The workaround is to set the location of the window again with an adjustment for the error.
				LinuxWorkarounds.fixWindowYCoord(event.getWindow(), location);
			}

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

		// Show dialog
		setVisible(true);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	public static Boolean showDialog(Component parent)
	{
		return new ClearCluesDialog(GuiUtils.getWindow(parent)).getResult();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : ActionListener interface
////////////////////////////////////////////////////////////////////////

	@Override
	public void actionPerformed(ActionEvent event)
	{
		switch (event.getActionCommand())
		{
			case Command.ACCEPT -> onAccept();
			case Command.CLOSE  -> onClose();
		}
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	private Boolean getResult()
	{
		Boolean result = null;
		if (accepted)
		{
			clearWithEmptyClue = emptyClueRadioButton.isSelected();
			result = clearWithEmptyClue;
		}
		return result;
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

	private static	boolean	clearWithEmptyClue;
	private static	Point	location;

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private	boolean			accepted;
	private	JRadioButton	noClueRadioButton;
	private	JRadioButton	emptyClueRadioButton;

}

//----------------------------------------------------------------------
