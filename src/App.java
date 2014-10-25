/*====================================================================*\

App.java

Application class.

\*====================================================================*/


// IMPORTS


import java.awt.Point;

import java.io.File;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;

import uk.org.blankaspect.exception.AppException;
import uk.org.blankaspect.exception.ExceptionUtilities;

import uk.org.blankaspect.gui.GuiUtilities;
import uk.org.blankaspect.gui.QuestionDialog;
import uk.org.blankaspect.gui.TextRendering;

import uk.org.blankaspect.textfield.TextFieldUtilities;

import uk.org.blankaspect.util.CalendarTime;
import uk.org.blankaspect.util.FilenameSuffixFilter;
import uk.org.blankaspect.util.NoYes;
import uk.org.blankaspect.util.PropertyString;
import uk.org.blankaspect.util.ResourceProperties;
import uk.org.blankaspect.util.StringUtilities;

//----------------------------------------------------------------------


// APPLICATION CLASS


class App
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

    public static final     String  SHORT_NAME  = "CrosswordEditor";
    public static final     String  LONG_NAME   = "Crossword editor";
    public static final     String  NAME_KEY    = "crosswordEditor";

    public static final     int MAX_NUM_DOCUMENTS   = 64;

    private static final    int FILE_CHECK_TIMER_INTERVAL   = 500;

    private static final    String  DEBUG_PROPERTY_KEY      = "app.debug";
    private static final    String  VERSION_PROPERTY_KEY    = "version";
    private static final    String  BUILD_PROPERTY_KEY      = "build";
    private static final    String  RELEASE_PROPERTY_KEY    = "release";
    private static final    String  VIEW_KEY                = "view";
    private static final    String  DO_NOT_VIEW_KEY         = "doNotView";

    private static final    String  BUILD_PROPERTIES_PATHNAME   = "resources/build.properties";

    private static final    String  DEBUG_STR               = " Debug";
    private static final    String  CONFIG_ERROR_STR        = "Configuration error";
    private static final    String  LAF_ERROR1_STR          = "Look-and-feel: ";
    private static final    String  LAF_ERROR2_STR          = "\nThe look-and-feel is not installed.";
    private static final    String  OPEN_FILE_STR           = "Open file";
    private static final    String  REVERT_FILE_STR         = "Revert file";
    private static final    String  SAVE_FILE_STR           = "Save file";
    private static final    String  SAVE_FILE_AS_STR        = "Save file as";
    private static final    String  SAVE_CLOSE_FILE_STR     = "Save file before closing";
    private static final    String  EXPORT_AS_HTML_STR      = "Export document as HTML";
    private static final    String  MODIFIED_FILE_STR       = "Modified file";
    private static final    String  READ_FILE_STR           = "Read file";
    private static final    String  WRITE_FILE_STR          = "Write file";
    private static final    String  NEW_CROSSWORD_STR       = "New crossword";
    private static final    String  REVERT_STR              = "Revert";
    private static final    String  SAVE_STR                = "Save";
    private static final    String  DISCARD_STR             = "Discard";
    private static final    String  REVERT_MESSAGE_STR      = "\nDo you want discard the changes to the " +
                                                                "current document and reopen the " +
                                                                "original file?";
    private static final    String  MODIFIED_MESSAGE_STR    = "\nThe file has been modified externally.\n" +
                                                                "Do you want to open the modified file?";
    private static final    String  UNNAMED_FILE_STR        = "The unnamed file";
    private static final    String  CHANGED_MESSAGE1_STR    = "\nThe file";
    private static final    String  CHANGED_MESSAGE2_STR    = " has changed.\nDo you want to save the " +
                                                                "changed file?";
    private static final    String  VIEW_FILE_STR           = "View file";
    private static final    String  DO_NOT_VIEW_FILE_STR    = "Don't view file";
    private static final    String  VIEW_HTML_FILE_STR      = "Do you want to view the HTML file in an " +
                                                                "external browser?";
    private static final    String  DO_NOT_SHOW_AGAIN_STR   = "Do not show this dialog again";

    private static final    QuestionDialog.Option[] VIEW_FILE_OPTIONS   =
    {
        new QuestionDialog.Option( VIEW_KEY,                  VIEW_FILE_STR ),
        new QuestionDialog.Option( DO_NOT_VIEW_KEY,           DO_NOT_VIEW_FILE_STR ),
        new QuestionDialog.Option( QuestionDialog.CANCEL_KEY, AppConstants.CANCEL_STR )
    };

////////////////////////////////////////////////////////////////////////
//  Member classes : non-inner classes
////////////////////////////////////////////////////////////////////////


    // DOCUMENT-VIEW CLASS


    private static class DocumentView
    {

    ////////////////////////////////////////////////////////////////////
    //  Constructors
    ////////////////////////////////////////////////////////////////////

        private DocumentView( CrosswordDocument document )
        {
            this.document = document;
            view = new CrosswordView( document );
        }

        //--------------------------------------------------------------

    ////////////////////////////////////////////////////////////////////
    //  Instance variables
    ////////////////////////////////////////////////////////////////////

        private CrosswordDocument   document;
        private CrosswordView       view;

    }

    //==================================================================

////////////////////////////////////////////////////////////////////////
//  Member classes : inner classes
////////////////////////////////////////////////////////////////////////


    // INITIALISATION CLASS


    /**
     * The run() method of this class creates the main window and performs the remaining initialisation of
     * the application from the event-dispatching thread.
     */

    private class DoInitialisation
        implements Runnable
    {

    ////////////////////////////////////////////////////////////////////
    //  Constructors
    ////////////////////////////////////////////////////////////////////

        private DoInitialisation( String[] arguments )
        {
            this.arguments = arguments;
        }

        //--------------------------------------------------------------

    ////////////////////////////////////////////////////////////////////
    //  Instance methods : Runnable interface
    ////////////////////////////////////////////////////////////////////

        public void run( )
        {
            // Create main window
            mainWindow = new MainWindow( );

            // Start file-check timer
            fileCheckTimer = new Timer( FILE_CHECK_TIMER_INTERVAL, AppCommand.CHECK_MODIFIED_FILE );
            fileCheckTimer.setRepeats( false );
            fileCheckTimer.start( );

            // Open any files that were specified as command-line arguments
            if ( arguments.length > 0 )
            {
                // Create list of files from command-line arguments
                List<File> files = new ArrayList<>( );
                for ( String argument : arguments )
                    files.add( new File( PropertyString.parsePathname( argument ) ) );

                // Open files
                openFiles( files );

                // Update title, menus and status
                mainWindow.updateAll( );
            }
        }

        //--------------------------------------------------------------

    ////////////////////////////////////////////////////////////////////
    //  Instance variables
    ////////////////////////////////////////////////////////////////////

        private String[]    arguments;

    }

    //==================================================================

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

    private App( )
    {
    }

    //------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

    public static void main( String[] args )
    {
        getInstance( ).init( args );
    }

    //------------------------------------------------------------------

    public static App getInstance( )
    {
        if ( instance == null )
            instance = new App( );
        return instance;
    }

    //------------------------------------------------------------------

    public static boolean isDebug( )
    {
        return debug;
    }

    //------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

    public MainWindow getMainWindow( )
    {
        return mainWindow;
    }

    //------------------------------------------------------------------

    public int getNumDocuments( )
    {
        return documentsViews.size( );
    }

    //------------------------------------------------------------------

    public boolean hasDocuments( )
    {
        return !documentsViews.isEmpty( );
    }

    //------------------------------------------------------------------

    public boolean isDocumentsFull( )
    {
        return ( documentsViews.size( ) >= MAX_NUM_DOCUMENTS );
    }

    //------------------------------------------------------------------

    public CrosswordDocument getDocument( )
    {
        return ( (hasDocuments( ) && (mainWindow != null)) ? getDocument( mainWindow.getTabIndex( ) )
                                                           : null );
    }

    //------------------------------------------------------------------

    public CrosswordDocument getDocument( int index )
    {
        return ( hasDocuments( ) ? documentsViews.get( index ).document : null );
    }

    //------------------------------------------------------------------

    public CrosswordView getView( )
    {
        return ( (hasDocuments( ) && (mainWindow != null)) ? getView( mainWindow.getTabIndex( ) ) : null );
    }

    //------------------------------------------------------------------

    public CrosswordView getView( int index )
    {
        return ( hasDocuments( ) ? documentsViews.get( index ).view : null );
    }

    //------------------------------------------------------------------

    public CrosswordView getView( CrosswordDocument document )
    {
        for ( DocumentView documentView : documentsViews )
        {
            if ( documentView.document == document )
                return documentView.view;
        }
        return null;
    }

    //------------------------------------------------------------------

    public String getVersionString( )
    {
        StringBuilder buffer = new StringBuilder( 32 );
        String str = buildProperties.get( VERSION_PROPERTY_KEY );
        if ( str != null )
            buffer.append( str );

        str = buildProperties.get( RELEASE_PROPERTY_KEY );
        if ( str == null )
        {
            long time = System.currentTimeMillis( );
            if ( buffer.length( ) > 0 )
                buffer.append( ' ' );
            buffer.append( 'b' );
            buffer.append( CalendarTime.dateToString( time ) );
            buffer.append( '-' );
            buffer.append( CalendarTime.hoursMinsToString( time ) );
        }
        else
        {
            NoYes release = NoYes.forKey( str );
            if ( (release == null) || !release.toBoolean( ) )
            {
                str = buildProperties.get( BUILD_PROPERTY_KEY );
                if ( str != null )
                {
                    if ( buffer.length( ) > 0 )
                        buffer.append( ' ' );
                    buffer.append( str );
                }
            }
        }

        if ( debug )
            buffer.append( DEBUG_STR );

        return buffer.toString( );
    }

    //------------------------------------------------------------------

    public void showInfoMessage( String titleStr,
                                 Object message )
    {
        showMessageDialog( titleStr, message, JOptionPane.INFORMATION_MESSAGE );
    }

    //------------------------------------------------------------------

    public void showWarningMessage( String titleStr,
                                    Object message )
    {
        showMessageDialog( titleStr, message, JOptionPane.WARNING_MESSAGE );
    }

    //------------------------------------------------------------------

    public void showErrorMessage( String titleStr,
                                  Object message )
    {
        showMessageDialog( titleStr, message, JOptionPane.ERROR_MESSAGE );
    }

    //------------------------------------------------------------------

    public void showMessageDialog( String titleStr,
                                   Object message,
                                   int    messageKind )
    {
        JOptionPane.showMessageDialog( mainWindow, message, titleStr, messageKind );
    }

    //------------------------------------------------------------------

    public void updateTabText( CrosswordDocument document )
    {
        for ( int i = 0; i < getNumDocuments( ); ++i )
        {
            if ( getDocument( i ) == document )
            {
                mainWindow.setTabText( i, document.getTitleString( false ),
                                       document.getTitleString( true ) );
                break;
            }
        }
    }

    //------------------------------------------------------------------

    public void updateCommands( )
    {
        CrosswordDocument document = getDocument( );
        boolean isDocument = (document != null);
        boolean notFull = !isDocumentsFull( );

        AppCommand.CHECK_MODIFIED_FILE.setEnabled( true );
        AppCommand.IMPORT_FILES.setEnabled( true );
        AppCommand.CREATE_DOCUMENT.setEnabled( notFull );
        AppCommand.OPEN_DOCUMENT.setEnabled( notFull );
        AppCommand.REVERT_DOCUMENT.setEnabled( isDocument && document.isChanged( ) &&
                                               (document.getFile( ) != null) );
        AppCommand.CLOSE_DOCUMENT.setEnabled( isDocument );
        AppCommand.CLOSE_ALL_DOCUMENTS.setEnabled( isDocument );
        AppCommand.SAVE_DOCUMENT.setEnabled( isDocument && document.isChanged( ) );
        AppCommand.SAVE_DOCUMENT_AS.setEnabled( isDocument );
        AppCommand.EXPORT_HTML_FILE.setEnabled( isDocument );
        AppCommand.EXIT.setEnabled( true );
        AppCommand.CAPTURE_CROSSWORD.setEnabled( notFull );
        AppCommand.CREATE_SOLUTION_DOCUMENT.setEnabled( notFull && isDocument &&
                                                        document.getGrid( ).hasSolution( ) );
        AppCommand.TOGGLE_SHOW_FULL_PATHNAMES.setEnabled( true );
        AppCommand.TOGGLE_SHOW_FULL_PATHNAMES.
                                            setSelected( AppConfig.getInstance( ).isShowFullPathnames( ) );
        AppCommand.EDIT_PREFERENCES.setEnabled( true );
    }

    //------------------------------------------------------------------

    public void executeCommand( AppCommand command )
    {
        try
        {
            switch ( command )
            {
                case CHECK_MODIFIED_FILE:
                    onCheckModifiedFile( );
                    break;

                case IMPORT_FILES:
                    onImportFiles( );
                    break;

                case CREATE_DOCUMENT:
                    onCreateDocument( );
                    break;

                case OPEN_DOCUMENT:
                    onOpenDocument( );
                    break;

                case REVERT_DOCUMENT:
                    onRevertDocument( );
                    break;

                case CLOSE_DOCUMENT:
                    onCloseDocument( );
                    break;

                case CLOSE_ALL_DOCUMENTS:
                    onCloseAllDocuments( );
                    break;

                case SAVE_DOCUMENT:
                    onSaveDocument( );
                    break;

                case SAVE_DOCUMENT_AS:
                    onSaveDocumentAs( );
                    break;

                case EXPORT_HTML_FILE:
                    onExportHtmlFile( );
                    break;

                case EXIT:
                    onExit( );
                    break;

                case CAPTURE_CROSSWORD:
                    onCaptureCrossword( );
                    break;

                case CREATE_SOLUTION_DOCUMENT:
                    onCreateSolutionDocument( );
                    break;

                case EDIT_PREFERENCES:
                    onEditPreferences( );
                    break;

                case TOGGLE_SHOW_FULL_PATHNAMES:
                    onToggleShowFullPathnames( );
                    break;
            }
        }
        catch ( AppException e )
        {
            showErrorMessage( SHORT_NAME, e );
        }

        if ( command != AppCommand.CHECK_MODIFIED_FILE )
        {
            updateTabText( getDocument( ) );
            mainWindow.updateAll( );
        }
    }

    //------------------------------------------------------------------

    public void closeDocument( int index )
    {
        if ( confirmCloseDocument( index ) )
            removeDocument( index );
    }

    //------------------------------------------------------------------

    private void addDocument( CrosswordDocument document )
    {
        DocumentView documentView = new DocumentView( document );
        documentsViews.add( documentView );
        mainWindow.addView( document.getTitleString( false ), document.getTitleString( true ),
                            documentView.view );
    }

    //------------------------------------------------------------------

    private void removeDocument( int index )
    {
        documentsViews.remove( index );
        mainWindow.removeView( index );
    }

    //------------------------------------------------------------------

    private CrosswordDocument readDocument( File file )
        throws AppException
    {
        CrosswordDocument document = new CrosswordDocument( );
        TaskProgressDialog.showDialog( mainWindow, READ_FILE_STR, new Task.ReadDocument( document, file ) );
        return ( (document.getFile( ) == null) ? null : document );
    }

    //------------------------------------------------------------------

    private void writeDocument( CrosswordDocument document,
                                File              file )
        throws AppException
    {
        TaskProgressDialog.showDialog( mainWindow, WRITE_FILE_STR,
                                       new Task.WriteDocument( document, file ) );
    }

    //------------------------------------------------------------------

    private void openDocument( File file )
        throws AppException
    {
        // Test whether document is already open
        for ( int i = 0; i < documentsViews.size( ); ++i )
        {
            if ( Util.isSameFile( file, getDocument( i ).getFile( ) ) )
            {
                mainWindow.selectView( i );
                return;
            }
        }

        // Read document and add it to list
        CrosswordDocument document = readDocument( file );
        if ( document != null )
            addDocument( document );
    }

    //------------------------------------------------------------------

    private void revertDocument( File file )
        throws AppException
    {
        // Read document
        CrosswordDocument document = readDocument( file );

        // Replace document in list
        if ( document != null )
        {
            int index = mainWindow.getTabIndex( );
            documentsViews.set( index, new DocumentView( document ) );
            mainWindow.setTabText( index, document.getTitleString( false ),
                                   document.getTitleString( true ) );
            mainWindow.setView( index, getView( ) );
        }
    }

    //------------------------------------------------------------------

    private boolean confirmWriteFile( File   file,
                                      String titleStr )
    {
        String[] optionStrs = Util.getOptionStrings( AppConstants.REPLACE_STR );
        return ( !file.exists( ) ||
                 (JOptionPane.showOptionDialog( mainWindow,
                                                Util.getPathname( file ) + AppConstants.ALREADY_EXISTS_STR,
                                                titleStr, JOptionPane.OK_CANCEL_OPTION,
                                                JOptionPane.WARNING_MESSAGE, null, optionStrs,
                                                optionStrs[1] ) == JOptionPane.OK_OPTION) );
    }

    //------------------------------------------------------------------

    private boolean confirmCloseDocument( int index )
    {
        // Test whether document has changed
        CrosswordDocument document = getDocument( index );
        if ( !document.isChanged( ) )
            return true;

        // Restore window
        GuiUtilities.restoreFrame( mainWindow );

        // Display document
        mainWindow.selectView( index );

        // Display prompt to save changed document
        File file = document.getFile( );
        String messageStr =
                    ((file == null) ? UNNAMED_FILE_STR : Util.getPathname( file ) + CHANGED_MESSAGE1_STR) +
                                                                                    CHANGED_MESSAGE2_STR;
        String[] optionStrs = Util.getOptionStrings( SAVE_STR, DISCARD_STR );
        int result = JOptionPane.showOptionDialog( mainWindow, messageStr, SAVE_CLOSE_FILE_STR,
                                                   JOptionPane.YES_NO_CANCEL_OPTION,
                                                   JOptionPane.QUESTION_MESSAGE, null, optionStrs,
                                                   optionStrs[0] );

        // Discard changed document
        if ( result == JOptionPane.NO_OPTION )
            return true;

        // Save changed document
        if ( result == JOptionPane.YES_OPTION )
        {
            // Choose filename
            if ( file == null )
            {
                file = chooseSave( null );
                if ( file == null )
                    return false;
                if ( file.exists( ) )
                {
                    messageStr = Util.getPathname( file ) + AppConstants.ALREADY_EXISTS_STR;
                    result = JOptionPane.showConfirmDialog( mainWindow, messageStr, SAVE_CLOSE_FILE_STR,
                                                            JOptionPane.YES_NO_CANCEL_OPTION,
                                                            JOptionPane.WARNING_MESSAGE );
                    if ( result == JOptionPane.NO_OPTION )
                        return true;
                    if ( result != JOptionPane.YES_OPTION )
                        return false;
                }
            }

            // Write file
            try
            {
                writeDocument( document, file );
                return true;
            }
            catch ( AppException e )
            {
                showErrorMessage( SAVE_CLOSE_FILE_STR, e );
            }
        }

        return false;
    }

    //------------------------------------------------------------------

    private void init( String[] arguments )
    {
        // Set runtime debug flag
        debug = (System.getProperty( DEBUG_PROPERTY_KEY ) != null);

        // Read build properties
        buildProperties = new ResourceProperties( BUILD_PROPERTIES_PATHNAME, getClass( ) );

        // Initialise instance variables
        documentsViews = new ArrayList<>( );
        showViewHtmlFileMessage = true;

        // Read configuration
        AppConfig config = AppConfig.getInstance( );
        config.read( );

        // Set UNIX style for pathnames in file exceptions
        ExceptionUtilities.setUnixStyle( config.isShowUnixPathnames( ) );

        // Set text antialiasing
        TextRendering.setAntialiasing( config.getTextAntialiasing( ) );

        // Set look-and-feel
        String lookAndFeelName = config.getLookAndFeel( );
        for ( UIManager.LookAndFeelInfo lookAndFeelInfo : UIManager.getInstalledLookAndFeels( ) )
        {
            if ( lookAndFeelInfo.getName( ).equals( lookAndFeelName ) )
            {
                try
                {
                    UIManager.setLookAndFeel( lookAndFeelInfo.getClassName( ) );
                }
                catch ( Exception e )
                {
                    // ignore
                }
                lookAndFeelName = null;
                break;
            }
        }
        if ( lookAndFeelName != null )
            showWarningMessage( SHORT_NAME + " | " + CONFIG_ERROR_STR,
                                LAF_ERROR1_STR + lookAndFeelName + LAF_ERROR2_STR );

        // Select all text when a text field gains focus
        if ( config.isSelectTextOnFocusGained( ) )
            TextFieldUtilities.selectAllOnFocusGained( );

        // Initialise file choosers
        initFileChoosers( );

        // Perform remaining initialisation from event-dispatching thread
        SwingUtilities.invokeLater( new DoInitialisation( arguments ) );
    }

    //------------------------------------------------------------------

    private void initFileChoosers( )
    {
        AppConfig config = AppConfig.getInstance( );

        openFileChooser = new JFileChooser( config.getOpenCrosswordDirectory( ) );
        openFileChooser.setDialogTitle( OPEN_FILE_STR );
        openFileChooser.setFileSelectionMode( JFileChooser.FILES_ONLY );

        saveFileChooser = new JFileChooser( config.getSaveCrosswordDirectory( ) );
        saveFileChooser.setDialogTitle( SAVE_FILE_STR );
        saveFileChooser.setFileSelectionMode( JFileChooser.FILES_ONLY );

        exportHtmlFileChooser = new JFileChooser( config.getExportHtmlDirectory( ) );
        exportHtmlFileChooser.setDialogTitle( EXPORT_AS_HTML_STR );
        exportHtmlFileChooser.setFileSelectionMode( JFileChooser.FILES_ONLY );
        exportHtmlFileChooser.setFileFilter( new FilenameSuffixFilter( AppConstants.HTML_FILES_STR,
                                                                       AppConstants.HTML_FILE_SUFFIX ) );
    }

    //------------------------------------------------------------------

    private File chooseOpen( )
    {
        openFileChooser.
                setFileFilter( new FilenameSuffixFilter( AppConstants.CROSSWORD_FILES_STR,
                                                         AppConfig.getInstance( ).getFilenameSuffix( ) ) );
        openFileChooser.setSelectedFile( new File( new String( ) ) );
        openFileChooser.rescanCurrentDirectory( );
        return ( (openFileChooser.showOpenDialog( mainWindow ) == JFileChooser.APPROVE_OPTION)
                                                                        ? openFileChooser.getSelectedFile( )
                                                                        : null );
    }

    //------------------------------------------------------------------

    private File chooseSave( File file )
    {
        String filenameSuffix = AppConfig.getInstance( ).getFilenameSuffix( );
        saveFileChooser.setFileFilter( new FilenameSuffixFilter( AppConstants.CROSSWORD_FILES_STR,
                                                                 filenameSuffix ) );
        saveFileChooser.setSelectedFile( (file == null) ? new File( new String( ) )
                                                        : file.getAbsoluteFile( ) );
        saveFileChooser.rescanCurrentDirectory( );
        return ( (saveFileChooser.showSaveDialog( mainWindow ) == JFileChooser.APPROVE_OPTION)
                                ? Util.appendSuffix( saveFileChooser.getSelectedFile( ), filenameSuffix )
                                : null );
    }

    //------------------------------------------------------------------

    private File chooseExportHtml( File file )
    {
        exportHtmlFileChooser.setSelectedFile( (file == null) ? new File( new String( ) )
                                                              : file.getAbsoluteFile( ) );
        exportHtmlFileChooser.rescanCurrentDirectory( );
        return ( (exportHtmlFileChooser.showSaveDialog( mainWindow ) == JFileChooser.APPROVE_OPTION)
                                            ? Util.appendSuffix( exportHtmlFileChooser.getSelectedFile( ),
                                                                 AppConstants.HTML_FILE_SUFFIX )
                                            : null );
    }

    //------------------------------------------------------------------

    private void updateConfiguration( )
    {
        // Set location of main window
        AppConfig config = AppConfig.getInstance( );
        if ( config.isMainWindowLocation( ) )
        {
            Point location = GuiUtilities.getFrameLocation( mainWindow );
            if ( location != null )
                config.setMainWindowLocation( location );
        }
        config.setMainWindowSize( GuiUtilities.getFrameSize( mainWindow ) );

        // Set file locations
        config.setOpenCrosswordPathname( Util.getPathname( openFileChooser.getCurrentDirectory( ) ) );
        config.setSaveCrosswordPathname( Util.getPathname( saveFileChooser.getCurrentDirectory( ) ) );
        config.setExportHtmlPathname( Util.getPathname( exportHtmlFileChooser.getCurrentDirectory( ) ) );

        // Write configuration file
        config.write( );
    }

    //------------------------------------------------------------------

    private void openFiles( List<File> files )
    {
        for ( int i = 0; i < files.size( ); ++i )
        {
            if ( isDocumentsFull( ) )
                break;
            try
            {
                openDocument( files.get( i ) );
                if ( Task.isCancelled( ) )
                    break;
            }
            catch ( AppException e )
            {
                if ( i == files.size( ) - 1 )
                    showErrorMessage( OPEN_FILE_STR, e );
                else
                {
                    String[] optionStrs = Util.getOptionStrings( AppConstants.CONTINUE_STR );
                    if ( JOptionPane.showOptionDialog( mainWindow, e, OPEN_FILE_STR,
                                                       JOptionPane.OK_CANCEL_OPTION,
                                                       JOptionPane.ERROR_MESSAGE, null, optionStrs,
                                                       optionStrs[1] ) != JOptionPane.OK_OPTION )
                        break;
                }
            }
        }
    }

    //------------------------------------------------------------------

    private void onCheckModifiedFile( )
        throws AppException
    {
        CrosswordDocument document = getDocument( );
        if ( (document != null) && !document.isExecutingCommand( ) )
        {
            File file = document.getFile( );
            long timestamp = document.getTimestamp( );
            if ( (file != null) && (timestamp != 0) )
            {
                long currentTimestamp = file.lastModified( );
                if ( (currentTimestamp != 0) && (currentTimestamp != timestamp) )
                {
                    String messageStr = Util.getPathname( file ) + MODIFIED_MESSAGE_STR;
                    if ( JOptionPane.showConfirmDialog( mainWindow, messageStr, MODIFIED_FILE_STR,
                                                        JOptionPane.YES_NO_OPTION,
                                                        JOptionPane.QUESTION_MESSAGE ) ==
                                                                                    JOptionPane.YES_OPTION )
                    {
                        revertDocument( file );
                        mainWindow.updateAll( );
                    }
                    else
                        document.setTimestamp( currentTimestamp );
                }
            }
        }
        fileCheckTimer.start( );
    }

    //------------------------------------------------------------------

    private void onImportFiles( )
    {
        String filenameSuffix = AppConfig.getInstance( ).getFilenameSuffix( );
        List<File> crosswordFiles = new ArrayList<>( );
        for ( File file : (File[])AppCommand.IMPORT_FILES.getValue( AppCommand.Property.FILES ) )
        {
            if ( file.getName( ).endsWith( filenameSuffix ) )
                crosswordFiles.add( file );
        }
        openFiles( crosswordFiles );
    }

    //------------------------------------------------------------------

    private void onCreateDocument( )
    {
        if ( !isDocumentsFull( ) )
        {
            Grid grid = GridParamsDialog.showDialog( mainWindow, NEW_CROSSWORD_STR );
            if ( grid != null )
            {
                CrosswordDocument document = new CrosswordDocument( ++newFileIndex );
                document.setGrid( grid );
                addDocument( document );
            }
        }
    }

    //------------------------------------------------------------------

    private void onOpenDocument( )
        throws AppException
    {
        if ( !isDocumentsFull( ) )
        {
            File file = chooseOpen( );
            if ( file != null )
                openDocument( file );
        }
    }

    //------------------------------------------------------------------

    private void onRevertDocument( )
        throws AppException
    {
        CrosswordDocument document = getDocument( );
        if ( (document != null) && document.isChanged( ) )
        {
            File file = document.getFile( );
            if ( file != null )
            {
                String messageStr = Util.getPathname( file ) + REVERT_MESSAGE_STR;
                String[] optionStrs = Util.getOptionStrings( REVERT_STR );
                if ( JOptionPane.showOptionDialog( mainWindow, messageStr, REVERT_FILE_STR,
                                                   JOptionPane.OK_CANCEL_OPTION,
                                                   JOptionPane.QUESTION_MESSAGE, null, optionStrs,
                                                   optionStrs[1] ) == JOptionPane.OK_OPTION )
                    revertDocument( file );
            }
        }
    }

    //------------------------------------------------------------------

    private void onCloseDocument( )
    {
        if ( hasDocuments( ) )
            closeDocument( mainWindow.getTabIndex( ) );
    }

    //------------------------------------------------------------------

    private void onCloseAllDocuments( )
    {
        while ( hasDocuments( ) )
        {
            int index = getNumDocuments( ) - 1;
            if ( !confirmCloseDocument( index ) )
                break;
            removeDocument( index );
        }
    }

    //------------------------------------------------------------------

    private void onSaveDocument( )
        throws AppException
    {
        CrosswordDocument document = getDocument( );
        if ( (document != null) && document.isChanged( ) )
        {
            File file = document.getFile( );
            if ( file == null )
                onSaveDocumentAs( );
            else
                writeDocument( document, file );
        }
    }

    //------------------------------------------------------------------

    private void onSaveDocumentAs( )
        throws AppException
    {
        CrosswordDocument document = getDocument( );
        if ( document != null )
        {
            File file = document.getOutputFile( );
            if ( file != null )
            {
                File directory = file.getParentFile( );
                if ( (directory != null) && !directory.exists( ) )
                    directory.mkdirs( );
            }
            file = chooseSave( file );
            if ( (file != null) && confirmWriteFile( file, SAVE_FILE_AS_STR ) )
                writeDocument( document, file );
        }
    }

    //------------------------------------------------------------------

    private void onExportHtmlFile( )
        throws AppException
    {
        // Get current document
        CrosswordDocument document = getDocument( );

        // Get parameters
        if ( document != null )
        {
            // Specify export parameters
            Grid.Separator separator = document.getGrid( ).getSeparator( );
            AppConfig config = AppConfig.getInstance( );
            ExportHtmlDialog.Result result =
                                        ExportHtmlDialog.showDialog( mainWindow, separator,
                                                                     config.getHtmlStylesheetKind( ),
                                                                     config.getHtmlCellSize( separator ) );
            if ( result == null )
                return;

            // Update configuration properties
            config.setHtmlStylesheetKind( result.stylesheetKind );
            config.setHtmlCellSize( separator, result.cellSize );

            // Get pathname of HTML file from current document
            File file = document.getExportHtmlFile( );

            // Derive pathname of HTML file
            if ( file == null )
            {
                file = document.getFile( );
                if ( file != null )
                {
                    String filename = file.getName( );
                    filename = filename.endsWith( config.getFilenameSuffix( ) )
                                    ? StringUtilities.removeSuffix( filename, config.getFilenameSuffix( ) )
                                    : StringUtilities.removeFromLast( filename, '.' );
                    file = new File( file.getParentFile( ), filename + AppConstants.HTML_FILE_SUFFIX );
                }
            }

            // Select output file
            file = chooseExportHtml( file );

            // Confirm replacement of existing file
            if ( (file == null) || !confirmWriteFile( file, EXPORT_AS_HTML_STR ) )
                return;

            // Write HTML file
            CrosswordDocument.StyleProperties styleProperties =
                        new CrosswordDocument.StyleProperties( config.getHtmlFontNames( ),
                                                               config.getHtmlFontSize( ), result.cellSize,
                                                               config.getHtmlGridColour( ),
                                                               config.getHtmlFieldNumberFontSizeFactor( ) );
            Task task = new Task.ExportDocumentAsHtml( document, file, result.stylesheetKind,
                                                       styleProperties, result.writeStylesheet,
                                                       result.writeBlockImage );
            TaskProgressDialog.showDialog( mainWindow, EXPORT_AS_HTML_STR, task );

            // Show "View HTML file" message
            if ( showViewHtmlFileMessage )
            {
                QuestionDialog.Result viewResult =
                                        QuestionDialog.showDialog( mainWindow, VIEW_FILE_STR,
                                                                   new String[]{ VIEW_HTML_FILE_STR },
                                                                   VIEW_FILE_OPTIONS, 0, DO_NOT_VIEW_KEY,
                                                                   DO_NOT_SHOW_AGAIN_STR );
                if ( viewResult.checkBoxSelected )
                    showViewHtmlFileMessage = false;
                if ( viewResult.selectedKey.equals( QuestionDialog.CANCEL_KEY ) )
                    return;
                viewHtmlFile = viewResult.selectedKey.equals( VIEW_KEY );
            }

            // View output file
            if ( viewHtmlFile )
                Util.viewHtmlFile( file );
        }
    }

    //------------------------------------------------------------------

    private void onExit( )
    {
        if ( !exiting )
        {
            try
            {
                // Prevent re-entry to this method
                exiting = true;

                // Close all open documents
                while ( hasDocuments( ) )
                {
                    int index = getNumDocuments( ) - 1;
                    if ( !confirmCloseDocument( index ) )
                        return;
                    removeDocument( index );
                }

                // Update configuration
                updateConfiguration( );

                // Destroy main window
                mainWindow.setVisible( false );
                mainWindow.dispose( );

                // Exit application
                System.exit( 0 );
            }
            finally
            {
                exiting = false;
            }
        }
    }

    //------------------------------------------------------------------

    private void onCaptureCrossword( )
    {
        if ( !isDocumentsFull( ) )
        {
            CrosswordDocument document = CaptureDialog.showDialog( mainWindow, newFileIndex + 1 );
            if ( document != null )
            {
                ++newFileIndex;
                addDocument( document );
            }
        }
    }

    //------------------------------------------------------------------

    private void onCreateSolutionDocument( )
    {
        if ( !isDocumentsFull( ) )
            addDocument( getDocument( ).createSolutionDocument( ++newFileIndex ) );
    }

    //------------------------------------------------------------------

    private void onToggleShowFullPathnames( )
    {
        AppConfig.getInstance( ).setShowFullPathnames( !AppConfig.getInstance( ).isShowFullPathnames( ) );
    }

    //------------------------------------------------------------------

    private void onEditPreferences( )
    {
        if ( PreferencesDialog.showDialog( mainWindow ) )
            ExceptionUtilities.setUnixStyle( AppConfig.getInstance( ).isShowUnixPathnames( ) );
    }

    //------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class variables
////////////////////////////////////////////////////////////////////////

    private static  App     instance;
    private static  boolean debug;

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

    private ResourceProperties  buildProperties;
    private MainWindow          mainWindow;
    private Timer               fileCheckTimer;
    private List<DocumentView>  documentsViews;
    private JFileChooser        openFileChooser;
    private JFileChooser        saveFileChooser;
    private JFileChooser        exportHtmlFileChooser;
    private int                 newFileIndex;
    private boolean             showViewHtmlFileMessage;
    private boolean             viewHtmlFile;
    private boolean             exiting;

}

//----------------------------------------------------------------------
