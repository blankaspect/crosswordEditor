/*====================================================================*\

QuestionDialog.java

Question dialog class.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.ui.swing.dialog;

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
import javax.swing.UIManager;

import uk.blankaspect.common.string.StringUtils;

import uk.blankaspect.ui.swing.action.KeyAction;

import uk.blankaspect.ui.swing.button.FButton;

import uk.blankaspect.ui.swing.checkbox.FCheckBox;

import uk.blankaspect.ui.swing.label.FLabel;

import uk.blankaspect.ui.swing.misc.GuiUtils;

//----------------------------------------------------------------------


// QUESTION DIALOG CLASS


public class QuestionDialog
	extends JDialog
	implements ActionListener
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	public static final		String	CANCEL_KEY	= "cancel";

	private static final	int		ICON_TEXT_GAP	= 12;

	private static final	String	QUESTION_ICON_KEY	= "OptionPane.questionIcon";

	// Commands
	private interface Command
	{
		String	ACCEPT	= "accept.";
		String	CANCEL	= "cancel";
	}

////////////////////////////////////////////////////////////////////////
//  Class variables
////////////////////////////////////////////////////////////////////////

	private static	Point	location;

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private	String		selectedKey;
	private	JCheckBox	checkBox;

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	private QuestionDialog(
		Window		owner,
		String		title,
		String[]	messageStrs,
		Option[]	options,
		int			numColumns,
		String		defaultOptionKey,
		String		checkBoxStr)
	{
		// Call superclass constructor
		super(owner, title, ModalityType.APPLICATION_MODAL);

		// Set icons
		if (owner != null)
			setIconImages(owner.getIconImages());

		// Initialise instance variables
		selectedKey = CANCEL_KEY;


		//----  Top panel

		GridBagLayout gridBag = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();

		JPanel topPanel = new JPanel(gridBag);

		// Icon
		JLabel questionIcon = new JLabel(UIManager.getIcon(QUESTION_ICON_KEY));

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(0, 0, 0, 0);
		gridBag.setConstraints(questionIcon, gbc);
		topPanel.add(questionIcon);

		// Message panel
		JPanel messagePanel = new JPanel(new GridLayout(0, 1, 0, 2));
		for (String messageStr : messageStrs)
			messagePanel.add(new FLabel(messageStr));

		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(0, ICON_TEXT_GAP, 0, 0);
		gridBag.setConstraints(messagePanel, gbc);
		topPanel.add(messagePanel);

		// Check box
		if (checkBoxStr != null)
		{
			checkBox = new FCheckBox(checkBoxStr);

			gbc.gridx = 1;
			gbc.gridy = 1;
			gbc.gridwidth = 1;
			gbc.gridheight = 1;
			gbc.weightx = 0.0;
			gbc.weighty = 0.0;
			gbc.anchor = GridBagConstraints.LINE_START;
			gbc.fill = GridBagConstraints.NONE;
			gbc.insets = new Insets(8, ICON_TEXT_GAP, 0, 0);
			gridBag.setConstraints(checkBox, gbc);
			topPanel.add(checkBox);
		}


		//----  Button panel

		JPanel buttonPanel = new JPanel(new GridLayout((numColumns > 0) ? 0 : 1, numColumns, 8, 6));

		JButton defaultButton = null;
		for (int i = 0; i < options.length; i++)
		{
			if (options[i] == null)
				buttonPanel.add(GuiUtils.spacer());
			else
			{
				JButton button = new FButton(options[i].text);
				String key = options[i].key;
				button.setActionCommand(CANCEL_KEY.equals(key) ? Command.CANCEL : Command.ACCEPT + key);
				if ((key != null) && (defaultButton == null) && key.equals(defaultOptionKey))
					defaultButton = button;
				if (options[i].mnemonic != 0)
					button.setMnemonic(options[i].mnemonic);
				button.addActionListener(this);
				buttonPanel.add(button);
			}
		}


		//----  Main panel

		JPanel mainPanel = new JPanel(gridBag);
		mainPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

		int gridY = 0;

		gbc.gridx = 0;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.FIRST_LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(0, 0, 0, 0);
		gridBag.setConstraints(topPanel, gbc);
		mainPanel.add(topPanel);

		gbc.gridx = 0;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.NORTH;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(10, 0, 0, 0);
		gridBag.setConstraints(buttonPanel, gbc);
		mainPanel.add(buttonPanel);

		// Add commands to action map
		KeyAction.create(mainPanel, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT,
						 KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), Command.CANCEL, this);


		//----  Window

		// Set content pane
		setContentPane(mainPanel);

		// Dispose of window explicitly
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

		// Handle window closing
		addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(
				WindowEvent	event)
			{
				close();
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

		// Set default button and focus
		if (defaultButton != null)
		{
			defaultButton.requestFocusInWindow();
			getRootPane().setDefaultButton(defaultButton);
		}

		// Show dialog
		setVisible(true);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	public static Result showDialog(
		Component	parent,
		String		title,
		String[]	messageStrs,
		Option[]	options,
		int			numColumns,
		String		defaultOptionKey,
		String		checkBoxStr)
	{
		return new QuestionDialog(GuiUtils.getWindow(parent), title, messageStrs, options, numColumns, defaultOptionKey,
								  checkBoxStr)
				.getResult();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : ActionListener interface
////////////////////////////////////////////////////////////////////////

	public void actionPerformed(
		ActionEvent	event)
	{
		String command = event.getActionCommand();

		if (command.startsWith(Command.ACCEPT))
			onAccept(StringUtils.removePrefix(command, Command.ACCEPT));

		else if (command.equals(Command.CANCEL))
			onCancel();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	private Result getResult()
	{
		return new Result(selectedKey, (checkBox != null) && checkBox.isSelected());
	}

	//------------------------------------------------------------------

	private void close()
	{
		location = getLocation();
		setVisible(false);
		dispose();
	}

	//------------------------------------------------------------------

	private void onAccept(
		String	key)
	{
		selectedKey = key;
		close();
	}

	//------------------------------------------------------------------

	private void onCancel()
	{
		close();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Member classes : non-inner classes
////////////////////////////////////////////////////////////////////////


	// OPTION CLASS


	public static class Option
	{

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	String	key;
		private	String	text;
		private	int		mnemonic;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		public Option(
			String	key,
			String	text)
		{
			this.key = key;
			this.text = text;
		}

		//--------------------------------------------------------------

		public Option(
			String	key,
			String	text,
			int		mnemonic)
		{
			this(key, text);
			this.mnemonic = mnemonic;
		}

		//--------------------------------------------------------------

	}

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Member records
////////////////////////////////////////////////////////////////////////


	// RECORD: RESULT


	public record Result(
		String	selectedKey,
		boolean	checkBoxSelected)
	{ }

	//==================================================================

}

//----------------------------------------------------------------------
