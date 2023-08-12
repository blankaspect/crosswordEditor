/*====================================================================*\

TextOutputTaskDialog.java

Text-output task dialog class.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.crosswordeditor;

//----------------------------------------------------------------------


// IMPORTS


import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FontMetrics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Window;

import java.awt.datatransfer.StringSelection;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.io.File;

import java.util.Arrays;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import uk.blankaspect.common.exception.AppException;

import uk.blankaspect.common.misc.IProcessOutputWriter;

import uk.blankaspect.common.ui.progress.IProgressView;

import uk.blankaspect.ui.swing.action.KeyAction;

import uk.blankaspect.ui.swing.button.AlternativeTextButton;
import uk.blankaspect.ui.swing.button.FButton;

import uk.blankaspect.ui.swing.font.FontUtils;

import uk.blankaspect.ui.swing.misc.GuiUtils;

import uk.blankaspect.ui.swing.textarea.FTextArea;

//----------------------------------------------------------------------


// TEXT-OUTPUT TASK DIALOG CLASS


class TextOutputTaskDialog
	extends JDialog
	implements ActionListener, DocumentListener, IProgressView
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	private static final	int	TEXT_AREA_NUM_COLUMNS		= 80;
	private static final	int	TEXT_AREA_NUM_ROWS			= 8;
	private static final	int	TEXT_AREA_VERTICAL_MARGIN	= 2;
	private static final	int	TEXT_AREA_HORIZONTAL_MARGIN	= 4;

	private static final	String	COPY_STR	= "Copy";

	private static final	List<String>	CLOSE_STRS	= Arrays.asList
	(
		AppConstants.CANCEL_STR,
		AppConstants.CLOSE_STR
	);

	// Commands
	private interface Command
	{
		String	COPY	= "copy";
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

		CLIPBOARD_IS_UNAVAILABLE
		("The clipboard is currently unavailable.");

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
//  Member classes : inner classes
////////////////////////////////////////////////////////////////////////


	// TEXT WRITER CLASS


	public class TextWriter
		implements IProcessOutputWriter
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private TextWriter()
		{
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : FileAssociations.ScriptOutputWriter interface
	////////////////////////////////////////////////////////////////////

		public boolean isClosed()
		{
			return cancelled;
		}

		//--------------------------------------------------------------

		public void write(final String str)
		{
			textLength += str.length();
			SwingUtilities.invokeLater(new Runnable()
			{
				@Override
				public void run()
				{
					textArea.append(str);
				}
			});
		}

		//--------------------------------------------------------------

		public void close()
		{
			if (!cancelled)
			{
				if (textLength == 0)
					onClose();
				else
					closeButton.setAlternative(AppConstants.CLOSE_STR);
			}
		}

		//--------------------------------------------------------------

	}

	//==================================================================


	// WINDOW EVENT HANDLER CLASS


	private class WindowEventHandler
		extends WindowAdapter
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private WindowEventHandler()
		{
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		public void windowActivated(WindowEvent event)
		{
			if (!started)
			{
				started = true;
				Task.setProgressView((TextOutputTaskDialog)event.getWindow());
				Task.setException(null, true);
				Task.setCancelled(false);
				task.start();
			}
		}

		//--------------------------------------------------------------

		@Override
		public void windowClosing(WindowEvent event)
		{
			location = getLocation();
			if (stopped)
				dispose();
			else
			{
				cancelled = true;
				Task.setCancelled(true);
			}
		}

		//--------------------------------------------------------------

	}

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	private TextOutputTaskDialog(Window owner,
								 String titleStr,
								 Task   task)
		throws AppException
	{

		// Call superclass constructor
		super(owner, titleStr, Dialog.ModalityType.APPLICATION_MODAL);

		// Set icons
		setIconImages(owner.getIconImages());

		// Initialise instance variables
		this.task = task;


		//----  Text area scroll pane

		// Text area
		textArea = new FTextArea();
		textArea.setEditable(false);
		textArea.setCaretPosition(0);
		textArea.getDocument().addDocumentListener(this);

		// Scroll pane
		JScrollPane textAreaScrollPane = new JScrollPane(textArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
														 JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		FontMetrics fontMetrics = textArea.getFontMetrics(textArea.getFont());
		int width = TEXT_AREA_NUM_COLUMNS * FontUtils.getCharWidth('0', fontMetrics);
		int height = TEXT_AREA_NUM_ROWS * fontMetrics.getHeight();
		textAreaScrollPane.getViewport().setPreferredSize(new Dimension(width, height));
		GuiUtils.setViewportBorder(textAreaScrollPane, TEXT_AREA_VERTICAL_MARGIN,
								   TEXT_AREA_HORIZONTAL_MARGIN);


		//----  Button panel

		JPanel buttonPanel = new JPanel(new GridLayout(1, 0, 16, 0));
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));

		// Button: copy
		copyButton = new FButton(COPY_STR);
		copyButton.setActionCommand(Command.COPY);
		copyButton.addActionListener(this);
		buttonPanel.add(copyButton);

		// Button: close
		closeButton = new AlternativeTextButton<>(CLOSE_STRS);
		closeButton.setActionCommand(Command.CLOSE);
		closeButton.addActionListener(this);
		buttonPanel.add(closeButton);

		// Update components
		updateComponents();


		//----  Main panel

		GridBagLayout gridBag = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();

		JPanel mainPanel = new JPanel(gridBag);

		int gridY = 0;

		gbc.gridx = 0;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.NORTH;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(0, 0, 0, 0);
		gridBag.setConstraints(textAreaScrollPane, gbc);
		mainPanel.add(textAreaScrollPane);

		gbc.gridx = 0;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.NORTH;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(2, 0, 2, 0);
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
		addWindowListener(new WindowEventHandler());

		// Prevent dialog from being resized
		setResizable(false);

		// Resize dialog to its preferred size
		pack();

		// Set location of dialog box
		if (location == null)
			location = GuiUtils.getComponentLocation(this, owner);
		setLocation(location);

		// Set default button
		getRootPane().setDefaultButton(closeButton);

		// Show dialog
		setVisible(true);

		// Throw any exception from task thread
		Task.throwIfException();

	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	public static void showDialog(Component parent,
								  String    titleStr,
								  Task      task)
		throws AppException
	{
		new TextOutputTaskDialog(GuiUtils.getWindow(parent), titleStr, task);
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
		updateComponents();
	}

	//------------------------------------------------------------------

	public void removeUpdate(DocumentEvent event)
	{
		updateComponents();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : IProgressView interface
////////////////////////////////////////////////////////////////////////

	public void setInfo(String str)
	{
		// do nothing
	}

	//------------------------------------------------------------------

	public void setInfo(String str,
						File   file)
	{
		// do nothing
	}

	//------------------------------------------------------------------

	public void setProgress(int    index,
							double value)
	{
		// do nothing
	}

	//------------------------------------------------------------------

	public void waitForIdle()
	{
		EventQueue eventQueue = getToolkit().getSystemEventQueue();
		while (eventQueue.peekEvent() != null)
		{
			// do nothing
		}
	}

	//------------------------------------------------------------------

	public void close()
	{
		stopped = true;
		dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	public TextWriter getWriter()
	{
		if (textWriter == null)
			textWriter = new TextWriter();
		return textWriter;
	}

	//------------------------------------------------------------------

	private void updateComponents()
	{
		copyButton.setEnabled(!textArea.isEmpty());
	}

	//------------------------------------------------------------------

	private void onCopy()
	{
		try
		{
			try
			{
				StringSelection selection = new StringSelection(textArea.getText());
				getToolkit().getSystemClipboard().setContents(selection, selection);
			}
			catch (IllegalStateException e)
			{
				throw new AppException(ErrorId.CLIPBOARD_IS_UNAVAILABLE, e);
			}
		}
		catch (AppException e)
		{
			JOptionPane.showMessageDialog(this, e, getTitle(), JOptionPane.ERROR_MESSAGE);
		}
	}

	//------------------------------------------------------------------

	private void onClose()
	{
		closeButton.setEnabled(false);
		dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class variables
////////////////////////////////////////////////////////////////////////

	private static	Point	location;

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private volatile	boolean				stopped;

	private	Task							task;
	private	boolean							started;
	private	boolean							cancelled;
	private	int								textLength;
	private	TextWriter						textWriter;
	private	FTextArea						textArea;
	private	JButton							copyButton;
	private	AlternativeTextButton<String>	closeButton;

}

//----------------------------------------------------------------------
