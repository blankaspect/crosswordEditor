/*====================================================================*\

PassphrasePane.java

Class: passphrase pane.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.ui.swing.container;

//----------------------------------------------------------------------


// IMPORTS


import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import java.util.Arrays;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.TransferHandler;

import uk.blankaspect.ui.swing.button.AlternativeTextButton;

import uk.blankaspect.ui.swing.font.FontKey;
import uk.blankaspect.ui.swing.font.FontUtils;

import uk.blankaspect.ui.swing.menu.FMenuItem;

import uk.blankaspect.ui.swing.misc.GuiUtils;

import uk.blankaspect.ui.swing.textfield.PasswordField;

//----------------------------------------------------------------------


// CLASS: PASSPHRASE PANE


public class PassphrasePane
	extends JPanel
	implements ActionListener
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	private static final	int		BUTTON_VERTICAL_MARGIN		= 2;
	private static final	int		BUTTON_HORIZONTAL_MARGIN	= 4;

	private static final	String	PASTE_STR						= "Paste";
	private static final	String	SHOW_STR						= "Show";
	private static final	String	HIDE_STR						= "Hide";
	private static final	String	PASSPHRASE_STR					= "Passphrase";
	private static final	String	FAILED_TO_CLEAR_CLIPBOARD_STR	= "Failed to clear the system clipboard.";

	private static final	List<String>	SHOW_HIDE_STRS	= List.of(SHOW_STR, HIDE_STR);

	// Commands
	private interface Command
	{
		String	PASTE		= "paste";
		String	SHOW_HIDE	= "showHide";
	}

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private	boolean							warnIfClipboardNotCleared;
	private	ActionMap						actionMap;
	private	PasswordField					field;
	private	AlternativeTextButton<String>	button;

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	public PassphrasePane(
		int	numColumns)
	{
		this(0, numColumns, false);
	}

	//------------------------------------------------------------------

	public PassphrasePane(
		int	maxLength,
		int	numColumns)
	{
		this(maxLength, numColumns, false);
	}

	//------------------------------------------------------------------

	public PassphrasePane(
		int		numColumns,
		boolean	warnIfClipboardNotCleared)
	{
		this(0, numColumns, warnIfClipboardNotCleared);
	}

	//------------------------------------------------------------------

	public PassphrasePane(
		int		maxLength,
		int		numColumns,
		boolean	warnIfClipboardNotCleared)
	{
		// Initialise instance variables
		this.warnIfClipboardNotCleared = warnIfClipboardNotCleared;

		// Initialise actions
		actionMap = new ActionMap();
		addAction(Command.PASTE, PASTE_STR);
		addAction(Command.SHOW_HIDE, SHOW_STR);

		// Set layout
		GridBagLayout gridBag = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();
		setLayout(gridBag);

		// Field
		field = new PasswordField(0, numColumns);
		FontUtils.setAppFont(FontKey.TEXT_FIELD, field);
		GuiUtils.setTextComponentMargins(field);
		field.setEchoChar(' ');

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(0, 0, 0, 0);
		gridBag.setConstraints(field, gbc);
		add(field);

		// Button: show/hide
		button = new AlternativeTextButton<>(SHOW_HIDE_STRS, BUTTON_VERTICAL_MARGIN, BUTTON_HORIZONTAL_MARGIN);
		button.setMnemonic(KeyEvent.VK_H);
		button.setActionCommand(Command.SHOW_HIDE);
		button.addActionListener(this);

		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(0, 6, 0, 0);
		gridBag.setConstraints(button, gbc);
		add(button);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	public static String escapePassphrase(
		String	passphrase)
	{
		StringBuilder buffer = new StringBuilder(128);
		for (int i = 0; i < passphrase.length(); i++)
		{
			char ch = passphrase.charAt(i);
			switch (ch)
			{
				case '"':
					buffer.append('\\').append(ch);
					break;

				default:
					buffer.append(ch);
					break;
			}
		}
		return buffer.toString();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : ActionListener interface
////////////////////////////////////////////////////////////////////////

	@Override
	public void actionPerformed(
		ActionEvent	event)
	{
		if (isEnabled())
		{
			switch (event.getActionCommand())
			{
				case Command.PASTE     -> onPaste();
				case Command.SHOW_HIDE -> onShowHide();
			}
		}
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : overriding methods
////////////////////////////////////////////////////////////////////////

	@Override
	public void setEnabled(
		boolean	enabled)
	{
		super.setEnabled(enabled);
		field.setEnabled(enabled);
		button.setEnabled(enabled);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	public PasswordField getField()
	{
		return field;
	}

	//------------------------------------------------------------------

	public String getPassphrase()
	{
		char[] chars = field.getPassword();
		String passphrase = new String(chars);
		Arrays.fill(chars, '\0');
		return passphrase;
	}

	//------------------------------------------------------------------

	public JPopupMenu getContextMenu()
	{
		// Create context menu
		JPopupMenu menu = new JPopupMenu();
		menu.add(new FMenuItem(getAction(Command.PASTE)));
		menu.addSeparator();
		menu.add(new FMenuItem(getAction(Command.SHOW_HIDE)));

		// Update actions for menu items
		updateActions();

		// Return menu
		return menu;
	}

	//------------------------------------------------------------------

	private CommandAction getAction(
		String	key)
	{
		return (CommandAction)actionMap.get(key);
	}

	//------------------------------------------------------------------

	private void addAction(
		String	key,
		String	text)
	{
		actionMap.put(key, new CommandAction(key, text));
	}

	//------------------------------------------------------------------

	private void updateActions()
	{
		boolean enabled = false;
		try
		{
			if (getToolkit().getSystemClipboard().isDataFlavorAvailable(DataFlavor.stringFlavor))
				enabled = true;
		}
		catch (Exception e)
		{
			// ignore
		}
		getAction(Command.PASTE).setEnabled(enabled);
		getAction(Command.SHOW_HIDE).putValue(Action.NAME, field.echoCharIsSet() ? SHOW_STR : HIDE_STR);
	}

	//------------------------------------------------------------------

	private void onPaste()
	{
		// Paste data from transfer handler into field
		Action action = TransferHandler.getPasteAction();
		action.actionPerformed(new ActionEvent(field, ActionEvent.ACTION_PERFORMED,
											   action.getValue(Action.NAME).toString()));

		// Clear system clipboard
		try
		{
			StringSelection selection = new StringSelection("");
			Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, selection);
		}
		catch (IllegalStateException e)
		{
			if (warnIfClipboardNotCleared)
			{
				JOptionPane.showMessageDialog(null, FAILED_TO_CLEAR_CLIPBOARD_STR, PASSPHRASE_STR,
											  JOptionPane.WARNING_MESSAGE);
			}
		}
	}

	//------------------------------------------------------------------

	private void onShowHide()
	{
		if (field.echoCharIsSet())
		{
			field.setEchoChar('\0');
			button.setAlternative(HIDE_STR);
		}
		else
		{
			field.setEchoChar(' ');
			button.setAlternative(SHOW_STR);
		}
		GuiUtils.setFocus(field);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Member classes : inner classes
////////////////////////////////////////////////////////////////////////


	// CLASS: COMMAND ACTION


	protected class CommandAction
		extends AbstractAction
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		protected CommandAction(
			String	command,
			String	text)
		{
			super(text);
			putValue(Action.ACTION_COMMAND_KEY, command);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : ActionListener interface
	////////////////////////////////////////////////////////////////////

		@Override
		public void actionPerformed(
			ActionEvent	event)
		{
			PassphrasePane.this.actionPerformed(event);
		}

		//--------------------------------------------------------------

	}

	//==================================================================

}

//----------------------------------------------------------------------
