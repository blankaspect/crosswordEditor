/*====================================================================*\

TaskProgressDialog.java

Task progress dialog box class.

\*====================================================================*/


// IMPORTS


import java.awt.Component;
import java.awt.Window;

import java.io.File;

import uk.org.blankaspect.exception.AppException;

import uk.org.blankaspect.gui.GuiUtilities;

//----------------------------------------------------------------------


// TASK PROGRESS DIALOG BOX CLASS


class TaskProgressDialog
    extends uk.org.blankaspect.gui.TaskProgressDialog
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

    private static final    int DEFAULT_DELAY   = 0;

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

    private TaskProgressDialog( Window owner,
                                String titleStr,
                                Task   task,
                                int    delay )
        throws AppException
    {
        super( owner, titleStr, task, delay, 1, -1, true );
    }

    //------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

    public static void showDialog( Component parent,
                                   String    titleStr,
                                   Task      task )
        throws AppException
    {
        new TaskProgressDialog( GuiUtilities.getWindow( parent ), titleStr, task, DEFAULT_DELAY );
    }

    //------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : overriding methods
////////////////////////////////////////////////////////////////////////

    @Override
    protected String getPathname( File file )
    {
        return Util.getPathname( file );
    }

    //------------------------------------------------------------------

    @Override
    protected char getFileSeparatorChar( )
    {
        return Util.getFileSeparatorChar( );
    }

    //------------------------------------------------------------------

}

//----------------------------------------------------------------------
