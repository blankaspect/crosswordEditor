/*====================================================================*\

ErrorListDialog.java

Error list dialog box class.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.crosswordeditor;

//----------------------------------------------------------------------


// IMPORTS


import java.awt.Color;
import java.awt.Component;
import java.awt.FontMetrics;
import java.awt.Window;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.TabSet;
import javax.swing.text.TabStop;

import uk.blankaspect.common.exception.AppException;

import uk.blankaspect.common.gui.AbstractNonEditableTextPaneDialog;
import uk.blankaspect.common.gui.GuiUtils;

import uk.blankaspect.common.misc.KeyAction;

//----------------------------------------------------------------------


// NON-EDITABLE TEXT PANE DIALOG BOX CLASS


class ErrorListDialog
	extends AbstractNonEditableTextPaneDialog
	implements ActionListener
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	private static final	int	NUM_COLUMNS	= 72;
	private static final	int	NUM_ROWS	= 24;

	private static final	String	COPY_STR			= "Copy";
	private static final	String	COPY_TOOLTIP_STR	= "Copy text to clipboard (Alt+C)";
	private static final	String	CLIPBOARD_ERROR_STR	= "Clipboard error";

	private static final	String	KEY	= ErrorListDialog.class.getCanonicalName();

	private static final	Map<Direction, String>	DIRECTION_STRS;

	// Commands
	private interface Command
	{
		String	COPY	= "copy";
		String	ACCEPT	= "accept";
		String	CLOSE	= "close";
	}

	private static final	KeyAction.KeyCommandPair[]	KEY_COMMANDS	=
	{
		new KeyAction.KeyCommandPair(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), Command.CLOSE)
	};

	private static final	Map<String, CommandAction>	COMMANDS;

////////////////////////////////////////////////////////////////////////
//  Enumerated types
////////////////////////////////////////////////////////////////////////


	// SPAN STYLE


	private enum SpanStyle
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		PLAIN
		(
			"plain",
			new Color(0, 0, 0)
		)
		{
			@Override
			protected void apply(Style style)
			{
				StyleConstants.setForeground(style, getColour());
			}
		},

		ERROR_KIND
		(
			"errorKind",
			new Color(192, 64, 0)
		)
		{
			@Override
			protected void apply(Style style)
			{
				StyleConstants.setForeground(style, getColour());
				StyleConstants.setItalic(style, true);
			}
		},

		DIRECTION
		(
			"direction",
			new Color(0, 0, 160)
		)
		{
			@Override
			protected void apply(Style style)
			{
				StyleConstants.setForeground(style, getColour());
			}
		};

		//--------------------------------------------------------------

		private static final	String	PREFIX	= "span.";

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private SpanStyle(String key,
						  Color  colour)
		{
			this.key = PREFIX + key;
			this.colour = colour;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Abstract methods
	////////////////////////////////////////////////////////////////////

		protected abstract void apply(Style style);

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		protected Color getColour()
		{
			return colour;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance fields
	////////////////////////////////////////////////////////////////////

		private	String	key;
		private	Color	colour;

	}

	//==================================================================


	// PARAGRAPH STYLE


	private enum ParagraphStyle
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		ID_LISTS
		(
			"idLists"
		)
		{
			@Override
			protected void apply(Style style)
			{
				// do nothing
			}
		},

		ERROR_KIND
		(
			"errorKind"
		)
		{
			@Override
			protected void apply(Style style)
			{
				StyleConstants.setSpaceAbove(style, (float)StyleConstants.getFontSize(style) * 0.5f);
			}
		};

		//--------------------------------------------------------------

		private static final	String	PREFIX	= "paragraph.";

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private ParagraphStyle(String key)
		{
			this.key = PREFIX + key;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Abstract methods
	////////////////////////////////////////////////////////////////////

		protected abstract void apply(Style style);

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance fields
	////////////////////////////////////////////////////////////////////

		private	String	key;

	}

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Member classes : non-inner classes
////////////////////////////////////////////////////////////////////////


	// IDENTIFIER LIST CLASS


	public static class IdList
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		public IdList(String              text,
					  List<Grid.Field.Id> ids)
		{
			this.text = text;
			this.ids = ids;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance fields
	////////////////////////////////////////////////////////////////////

		private	String				text;
		private	List<Grid.Field.Id>	ids;

	}

	//==================================================================


	// COMMAND ACTION CLASS


	private static class CommandAction
		extends AbstractAction
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CommandAction(String command,
							  String text,
							  int    mnemonicKey,
							  String tooltipStr)
		{
			// Call superclass constructor
			super(text);

			// Set action properties
			putValue(Action.ACTION_COMMAND_KEY, command);
			if (mnemonicKey != 0)
				putValue(Action.MNEMONIC_KEY, mnemonicKey);
			if (tooltipStr != null)
				putValue(Action.SHORT_DESCRIPTION, tooltipStr);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : ActionListener interface
	////////////////////////////////////////////////////////////////////

		public void actionPerformed(ActionEvent event)
		{
			listener.actionPerformed(event);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance fields
	////////////////////////////////////////////////////////////////////

		private	ActionListener	listener;

	}

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	private ErrorListDialog(Window owner,
							String titleStr,
							String closeStr)
	{
		// Call superclass constructor
		super(owner, titleStr, KEY, NUM_COLUMNS, NUM_ROWS, getCommands(closeStr), Command.CLOSE);

		// Add commands to action map
		KeyAction.create((JComponent)getContentPane(), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT,
						 this, KEY_COMMANDS);

		// Set action listener in commands
		for (String commandKey : COMMANDS.keySet())
			COMMANDS.get(commandKey).listener = this;

		// Add styles
		for (SpanStyle spanStyle : SpanStyle.values())
			spanStyle.apply(addStyle(spanStyle.key, getDefaultStyle()));
		for (ParagraphStyle paragraphStyle : ParagraphStyle.values())
			paragraphStyle.apply(addStyle(paragraphStyle.key, getDefaultStyle()));
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	public static boolean showDialog(Component    parent,
									 String       titleStr,
									 String       closeStr,
									 List<IdList> idLists)
	{
		// Create dialog
		ErrorListDialog dialog = new ErrorListDialog(GuiUtils.getWindow(parent), titleStr, closeStr);

		// Set text
		dialog.setText(idLists);
		dialog.setCaretToStart();

		// Show dialog
		dialog.setVisible(true);

		// Return result from dialog
		return dialog.accepted;
	}

	//------------------------------------------------------------------

	private static List<Action> getCommands(String closeStr)
	{
		List<Action> commands = new ArrayList<>();
		commands.add(COMMANDS.get(Command.COPY));
		commands.add(COMMANDS.get(Command.ACCEPT));
		CommandAction closeCommand = COMMANDS.get(Command.CLOSE);
		closeCommand.putValue(Action.NAME, closeStr);
		commands.add(closeCommand);
		return commands;
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : ActionListener interface
////////////////////////////////////////////////////////////////////////

	public void actionPerformed(ActionEvent event)
	{
		String command = event.getActionCommand();

		if (command.equals(Command.COPY))
			onCopy();

		else if (command.equals(Command.ACCEPT))
			onAccept();

		else if (command.equals(Command.CLOSE))
			onClose();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : overriding methods
////////////////////////////////////////////////////////////////////////

	@Override
	protected void updateComponents()
	{
		if (getTextLength() == 0)
		{
			COMMANDS.get(Command.COPY).setEnabled(false);
			getButton(Command.CLOSE).requestFocusInWindow();
		}
		else
			COMMANDS.get(Command.COPY).setEnabled(true);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	public boolean isCleared()
	{
		return cleared;
	}

	//------------------------------------------------------------------

	private void setText(List<IdList> idLists)
	{
		// Set tab stops
		StyledDocument document = getTextPane().getStyledDocument();
		FontMetrics fontMetrics = getTextPane().getFontMetrics(document.getFont(getDefaultStyle()));
		int maxWidth = 0;
		for (Direction direction : Direction.values())
		{
			int width = fontMetrics.stringWidth(DIRECTION_STRS.get(direction));
			if (maxWidth < width)
				maxWidth = width;
		}
		int indent1 = maxWidth + fontMetrics.stringWidth(":");
		int indent2 = indent1 + fontMetrics.stringWidth("  ");
		TabStop tab1 = new TabStop(indent1, TabStop.ALIGN_RIGHT, TabStop.LEAD_NONE);
		TabStop tab2 = new TabStop(indent2);
		StyleConstants.setTabSet(document.getStyle(ParagraphStyle.ID_LISTS.key),
								 new TabSet(new TabStop[]{ tab1, tab2 }));

		// Set text
		for (IdList idList : idLists)
		{
			// Append kind of error
			Paragraph paragraph = new Paragraph(ParagraphStyle.ERROR_KIND.key);
			paragraph.add(new Span(idList.text, SpanStyle.ERROR_KIND.key));
			append(paragraph);

			// Append field numbers
			for (Direction direction : Direction.values())
			{
				// Create a list of field numbers
				List<Integer> fieldNumbers = new ArrayList<>();
				for (Grid.Field.Id id : idList.ids)
				{
					if (id.direction == direction)
						fieldNumbers.add(id.number);
				}

				// Append direction and list of field numbers
				if (!fieldNumbers.isEmpty())
				{
					paragraph = new Paragraph(ParagraphStyle.ID_LISTS.key);
					paragraph.add(new Span("\t" + DIRECTION_STRS.get(direction) + ":\t",
										   SpanStyle.DIRECTION.key));
					StringBuilder buffer = new StringBuilder(128);
					for (int i = 0; i < fieldNumbers.size(); i++)
					{
						if (i > 0)
							buffer.append(", ");
						buffer.append(fieldNumbers.get(i));
					}
					paragraph.add(new Span(buffer.toString(), SpanStyle.PLAIN.key));
					append(paragraph);
				}
			}
		}
	}

	//------------------------------------------------------------------

	private void onCopy()
	{
		try
		{
			Utils.putClipboardText(getText().replace('\t', ' ').replaceAll("\n[^ ]", "\n$0"));
		}
		catch (AppException e)
		{
			JOptionPane.showMessageDialog(this, e, CLIPBOARD_ERROR_STR, JOptionPane.ERROR_MESSAGE);
		}
	}

	//------------------------------------------------------------------

	private void onAccept()
	{
		accepted = true;
		onClose();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Static initialiser
////////////////////////////////////////////////////////////////////////

	static
	{
		// Direction strings
		DIRECTION_STRS = new EnumMap<>(Direction.class);
		DIRECTION_STRS.put(Direction.NONE,   "No direction");
		DIRECTION_STRS.put(Direction.ACROSS, "Across");
		DIRECTION_STRS.put(Direction.DOWN,   "Down");

		// Commands
		COMMANDS = new HashMap<>();
		COMMANDS.put(Command.COPY,
					 new CommandAction(Command.COPY, COPY_STR, KeyEvent.VK_C, COPY_TOOLTIP_STR));
		COMMANDS.put(Command.ACCEPT,
					 new CommandAction(Command.ACCEPT, AppConstants.OK_STR, 0, null));
		COMMANDS.put(Command.CLOSE,
					 new CommandAction(Command.CLOSE, AppConstants.CANCEL_STR, 0, null));
	}

////////////////////////////////////////////////////////////////////////
//  Instance fields
////////////////////////////////////////////////////////////////////////

	private	boolean	accepted;
	private	boolean	cleared;

}

//----------------------------------------------------------------------
