/*====================================================================*\

TaskProgressDialog.java

Task progress dialog class.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.crosswordeditor;

//----------------------------------------------------------------------


// IMPORTS


import java.awt.Component;
import java.awt.Window;

import java.io.File;

import uk.blankaspect.common.exception.AppException;

import uk.blankaspect.ui.swing.misc.GuiUtils;

//----------------------------------------------------------------------


// TASK PROGRESS DIALOG CLASS


class TaskProgressDialog
	extends uk.blankaspect.ui.swing.dialog.TaskProgressDialog
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	private static final	int	DEFAULT_DELAY	= 0;

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	private TaskProgressDialog(Window owner,
							   String title,
							   Task   task,
							   int    delay)
		throws AppException
	{
		super(owner, title, task, delay, 1, -1, true);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	public static void showDialog(Component parent,
								  String    title,
								  Task      task)
		throws AppException
	{
		new TaskProgressDialog(GuiUtils.getWindow(parent), title, task, DEFAULT_DELAY);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : overriding methods
////////////////////////////////////////////////////////////////////////

	@Override
	protected String getPathname(File file)
	{
		return Utils.getPathname(file);
	}

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------
