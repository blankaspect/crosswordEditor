/*====================================================================*\

AppCommand.java

Application command enumeration.

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

import uk.blankaspect.common.misc.Command;

//----------------------------------------------------------------------


// APPLICATION COMMAND ENUMERATION


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
		"New document" + AppConstants.ELLIPSIS_STR,
		KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_DOWN_MASK)
	),

	OPEN_DOCUMENT
	(
		"openDocument",
		"Open document" + AppConstants.ELLIPSIS_STR,
		KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_DOWN_MASK)
	),

	REVERT_DOCUMENT
	(
		"revertDocument",
		"Revert document"
	),

	CLOSE_DOCUMENT
	(
		"closeDocument",
		"Close document",
		KeyStroke.getKeyStroke(KeyEvent.VK_W, KeyEvent.CTRL_DOWN_MASK)
	),

	CLOSE_ALL_DOCUMENTS
	(
		"closeAllDocuments",
		"Close all document"
	),

	SAVE_DOCUMENT
	(
		"saveDocument",
		"Save document",
		KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK)
	),

	SAVE_DOCUMENT_AS
	(
		"saveDocumentAs",
		"Save document as" + AppConstants.ELLIPSIS_STR
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

	MANAGE_FILE_ASSOCIATIONS
	(
		"manageFileAssociations",
		"Manage file associations" + AppConstants.ELLIPSIS_STR
	),

	EDIT_PREFERENCES
	(
		"editPreferences",
		"Preferences" + AppConstants.ELLIPSIS_STR
	);

	//------------------------------------------------------------------

	// Property keys
	interface Property
	{
		String	FILES	= "files";
	}

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

	public void addPropertyChangeListener(PropertyChangeListener listener)
	{
		command.addPropertyChangeListener(listener);
	}

	//------------------------------------------------------------------

	public Object getValue(String key)
	{
		return command.getValue(key);
	}

	//------------------------------------------------------------------

	public boolean isEnabled()
	{
		return command.isEnabled();
	}

	//------------------------------------------------------------------

	public void putValue(String key,
						 Object value)
	{
		command.putValue(key, value);
	}

	//------------------------------------------------------------------

	public void removePropertyChangeListener(PropertyChangeListener listener)
	{
		command.removePropertyChangeListener(listener);
	}

	//------------------------------------------------------------------

	public void setEnabled(boolean enabled)
	{
		command.setEnabled(enabled);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : ActionListener interface
////////////////////////////////////////////////////////////////////////

	public void actionPerformed(ActionEvent event)
	{
		App.INSTANCE.executeCommand(this);
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

////////////////////////////////////////////////////////////////////////
//  Instance fields
////////////////////////////////////////////////////////////////////////

	private	Command	command;

}

//----------------------------------------------------------------------
