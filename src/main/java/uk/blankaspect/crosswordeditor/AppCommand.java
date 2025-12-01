/*====================================================================*\

AppCommand.java

Enumeration: application command.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.crosswordeditor;

//----------------------------------------------------------------------


// IMPORTS


import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import java.beans.PropertyChangeListener;

import javax.swing.Action;
import javax.swing.KeyStroke;

import uk.blankaspect.ui.swing.action.Command;

//----------------------------------------------------------------------


// ENUMERATION: APPLICATION COMMAND


enum AppCommand
	implements Action
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	// Commands

	CHECK_MODIFIED_FILE
	(
		"checkModifiedFile"
	),

	IMPORT_FILES
	(
		"importFiles"
	),

	CREATE_DOCUMENT
	(
		"createDocument",
		"New " + AppConstants.ELLIPSIS_STR,
		KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_DOWN_MASK)
	),

	OPEN_DOCUMENT
	(
		"openDocument",
		"Open " + AppConstants.ELLIPSIS_STR,
		KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_DOWN_MASK)
	),

	REVERT_DOCUMENT
	(
		"revertDocument",
		"Revert"
	),

	CLOSE_DOCUMENT
	(
		"closeDocument",
		"Close",
		KeyStroke.getKeyStroke(KeyEvent.VK_W, KeyEvent.CTRL_DOWN_MASK)
	),

	CLOSE_ALL_DOCUMENTS
	(
		"closeAllDocuments",
		"Close all"
	),

	SAVE_DOCUMENT
	(
		"saveDocument",
		"Save",
		KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK)
	),

	SAVE_DOCUMENT_AS
	(
		"saveDocumentAs",
		"Save as" + AppConstants.ELLIPSIS_STR
	),

	EXPORT_HTML_FILE
	(
		"exportHtmlFile",
		"Export HTML file" + AppConstants.ELLIPSIS_STR,
		KeyStroke.getKeyStroke(KeyEvent.VK_E, KeyEvent.CTRL_DOWN_MASK)
	),

	EXIT
	(
		"exit",
		"Exit"
	),

	CAPTURE_CROSSWORD
	(
		"captureCrossword",
		"Capture crossword" + AppConstants.ELLIPSIS_STR,
		KeyStroke.getKeyStroke(KeyEvent.VK_F9, 0)
	),

	CREATE_SOLUTION_DOCUMENT
	(
		"createSolutionDocument",
		"Create document containing solution"
	),

	TOGGLE_SHOW_FULL_PATHNAMES
	(
		"toggleShowFullPathnames",
		"Show full pathnames"
	),

	MANAGE_FILE_ASSOCIATION
	(
		"manageFileAssociation",
		"Manage file association" + AppConstants.ELLIPSIS_STR
	),

	EDIT_PREFERENCES
	(
		"editPreferences",
		"Preferences"
	);

	//------------------------------------------------------------------

	// Property keys
	interface Property
	{
		String	FILES	= "files";
	}

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private	Command	command;

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	private AppCommand(String key)
	{
		command = new Command(this);
		putValue(Action.ACTION_COMMAND_KEY, key);
	}

	//------------------------------------------------------------------

	private AppCommand(String key,
					   String name)
	{
		this(key);
		putValue(Action.NAME, name);
	}

	//------------------------------------------------------------------

	private AppCommand(String    key,
					   String    name,
					   KeyStroke acceleratorKey)
	{
		this(key, name);
		putValue(Action.ACCELERATOR_KEY, acceleratorKey);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : Action interface
////////////////////////////////////////////////////////////////////////

	@Override
	public void addPropertyChangeListener(PropertyChangeListener listener)
	{
		command.addPropertyChangeListener(listener);
	}

	//------------------------------------------------------------------

	@Override
	public Object getValue(String key)
	{
		return command.getValue(key);
	}

	//------------------------------------------------------------------

	@Override
	public boolean isEnabled()
	{
		return command.isEnabled();
	}

	//------------------------------------------------------------------

	@Override
	public void putValue(String key,
						 Object value)
	{
		command.putValue(key, value);
	}

	//------------------------------------------------------------------

	@Override
	public void removePropertyChangeListener(PropertyChangeListener listener)
	{
		command.removePropertyChangeListener(listener);
	}

	//------------------------------------------------------------------

	@Override
	public void setEnabled(boolean enabled)
	{
		command.setEnabled(enabled);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : ActionListener interface
////////////////////////////////////////////////////////////////////////

	@Override
	public void actionPerformed(ActionEvent event)
	{
		CrosswordEditorApp.INSTANCE.executeCommand(this);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	public void setSelected(boolean selected)
	{
		putValue(Action.SELECTED_KEY, selected);
	}

	//------------------------------------------------------------------

	public void execute()
	{
		actionPerformed(null);
	}

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------
