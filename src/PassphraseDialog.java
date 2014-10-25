/*====================================================================*\

PassphraseDialog.java

Passphrase dialog box class.

\*====================================================================*/


// IMPORTS


import java.awt.Component;
import java.awt.Dialog;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Window;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import uk.org.blankaspect.gui.FButton;
import uk.org.blankaspect.gui.FLabel;
import uk.org.blankaspect.gui.GuiUtilities;
import uk.org.blankaspect.gui.PassphrasePanel;

import uk.org.blankaspect.util.KeyAction;

//----------------------------------------------------------------------


// PASSPHRASE DIALOG BOX CLASS


class PassphraseDialog
    extends JDialog
    implements ActionListener, DocumentListener, MouseListener
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

    private static final    int PASSPHRASE_MAX_LENGTH           = 1024;
    private static final    int PASSPHRASE_FIELD_NUM_COLUMNS    = 40;

    private static final    String  TITLE_STR       = "Passphrase";
    private static final    String  PASSPHRASE_STR  = "Passphrase:";
    private static final    String  SKIP_STR        = "Skip";

    // Commands
    private interface Command
    {
        String  SHOW_CONTEXT_MENU   = "showContextMenu";
        String  ACCEPT              = "accept";
        String  SKIP                = "skip";
        String  CLOSE               = "close";
    }

    private static final    KeyAction.KeyCommandPair[]  KEY_COMMANDS    =
    {
        new KeyAction.KeyCommandPair( KeyStroke.getKeyStroke( KeyEvent.VK_CONTEXT_MENU, 0 ),
                                      Command.SHOW_CONTEXT_MENU ),
        new KeyAction.KeyCommandPair( KeyStroke.getKeyStroke( KeyEvent.VK_ESCAPE, 0 ),
                                      Command.CLOSE )
    };

////////////////////////////////////////////////////////////////////////
//  Member classes : inner classes
////////////////////////////////////////////////////////////////////////


    // WINDOW EVENT HANDLER CLASS


    private class WindowEventHandler
        extends WindowAdapter
    {

    ////////////////////////////////////////////////////////////////////
    //  Constructors
    ////////////////////////////////////////////////////////////////////

        private WindowEventHandler( )
        {
        }

        //--------------------------------------------------------------

    ////////////////////////////////////////////////////////////////////
    //  Instance methods : overriding methods
    ////////////////////////////////////////////////////////////////////

        @Override
        public void windowClosing( WindowEvent event )
        {
            onClose( );
        }

        //--------------------------------------------------------------

    }

    //==================================================================

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

    private PassphraseDialog( Window  owner,
                              String  titleStr,
                              boolean canSkip )
    {

        // Call superclass constructor
        super( owner, (titleStr == null) ? TITLE_STR : TITLE_STR + " | " + titleStr,
               Dialog.ModalityType.APPLICATION_MODAL );

        // Set icons
        setIconImages( owner.getIconImages( ) );


        //----  Control panel

        GridBagLayout gridBag = new GridBagLayout( );
        GridBagConstraints gbc = new GridBagConstraints( );

        JPanel controlPanel = new JPanel( gridBag );
        GuiUtilities.setPaddedLineBorder( controlPanel );

        int gridY = 0;

        // Label: passphrase
        JLabel passphraseLabel = new FLabel( PASSPHRASE_STR );

        gbc.gridx = 0;
        gbc.gridy = gridY;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.LINE_END;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = AppConstants.COMPONENT_INSETS;
        gridBag.setConstraints( passphraseLabel, gbc );
        controlPanel.add( passphraseLabel );

        // Panel: passphrase
        passphrasePanel = new PassphrasePanel( PASSPHRASE_MAX_LENGTH, PASSPHRASE_FIELD_NUM_COLUMNS, true );
        passphrasePanel.getField( ).getDocument( ).addDocumentListener( this );
        passphrasePanel.getField( ).addMouseListener( this );

        gbc.gridx = 1;
        gbc.gridy = gridY++;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = AppConstants.COMPONENT_INSETS;
        gridBag.setConstraints( passphrasePanel, gbc );
        controlPanel.add( passphrasePanel );


        //----  Button panel

        JPanel buttonPanel = new JPanel( new GridLayout( 1, 0, 8, 0 ) );
        buttonPanel.setBorder( BorderFactory.createEmptyBorder( 3, 8, 3, 8 ) );

        // Button: OK
        okButton = new FButton( AppConstants.OK_STR );
        okButton.setActionCommand( Command.ACCEPT );
        okButton.addActionListener( this );
        buttonPanel.add( okButton );

        // Button: skip
        if ( canSkip )
        {
            JButton skipButton = new FButton( SKIP_STR );
            skipButton.setActionCommand( Command.SKIP );
            skipButton.addActionListener( this );
            buttonPanel.add( skipButton );
        }

        // Button: cancel
        JButton cancelButton = new FButton( AppConstants.CANCEL_STR );
        cancelButton.setActionCommand( Command.CLOSE );
        cancelButton.addActionListener( this );
        buttonPanel.add( cancelButton );

        // Update components
        updateComponents( );


        //----  Main panel

        JPanel mainPanel = new JPanel( gridBag );
        mainPanel.setBorder( BorderFactory.createEmptyBorder( 2, 2, 2, 2 ) );
        mainPanel.addMouseListener( this );

        gridY = 0;

        gbc.gridx = 0;
        gbc.gridy = gridY++;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets( 0, 0, 0, 0 );
        gridBag.setConstraints( controlPanel, gbc );
        mainPanel.add( controlPanel );

        gbc.gridx = 0;
        gbc.gridy = gridY++;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets( 3, 0, 0, 0 );
        gridBag.setConstraints( buttonPanel, gbc );
        mainPanel.add( buttonPanel );

        // Add commands to action map
        KeyAction.create( mainPanel, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, this, KEY_COMMANDS );


        //----  Window

        // Set content pane
        setContentPane( mainPanel );

        // Dispose of window explicitly
        setDefaultCloseOperation( DO_NOTHING_ON_CLOSE );

        // Handle window events
        addWindowListener( new WindowEventHandler( ) );

        // Prevent dialog from being resized
        setResizable( false );

        // Resize dialog to its preferred size
        pack( );

        // Set location of dialog box
        if ( location == null )
            location = GuiUtilities.getComponentLocation( this, owner );
        setLocation( location );

        // Set default button
        getRootPane( ).setDefaultButton( okButton );

        // Show dialog
        setVisible( true );

    }

    //------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

    public static String showDialog( Component parent,
                                     String    titleStr,
                                     boolean   canSkip )
    {
        return new PassphraseDialog( GuiUtilities.getWindow( parent ), titleStr, canSkip ).getPassphrase( );
    }

    //------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : ActionListener interface
////////////////////////////////////////////////////////////////////////

    public void actionPerformed( ActionEvent event )
    {
        String command = event.getActionCommand( );

        if ( command.equals( Command.SHOW_CONTEXT_MENU ) )
            onShowContextMenu( );

        else if ( command.equals( Command.ACCEPT ) )
            onAccept( );

        else if ( command.equals( Command.SKIP ) )
            onSkip( );

        else if ( command.equals( Command.CLOSE ) )
            onClose( );
    }

    //------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : DocumentListener interface
////////////////////////////////////////////////////////////////////////

    public void changedUpdate( DocumentEvent event )
    {
        // do nothing
    }

    //------------------------------------------------------------------

    public void insertUpdate( DocumentEvent event )
    {
        updateComponents( );
    }

    //------------------------------------------------------------------

    public void removeUpdate( DocumentEvent event )
    {
        updateComponents( );
    }

    //------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : MouseListener interface
////////////////////////////////////////////////////////////////////////

    public void mouseClicked( MouseEvent event )
    {
        // do nothing
    }

    //------------------------------------------------------------------

    public void mouseEntered( MouseEvent event )
    {
        // do nothing
    }

    //------------------------------------------------------------------

    public void mouseExited( MouseEvent event )
    {
        // do nothing
    }

    //------------------------------------------------------------------

    public void mousePressed( MouseEvent event )
    {
        showContextMenu( event );
    }

    //------------------------------------------------------------------

    public void mouseReleased( MouseEvent event )
    {
        showContextMenu( event );
    }

    //------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

    private String getPassphrase( )
    {
        return ( accepted ? passphrasePanel.getPassphrase( ) : null );
    }

    //------------------------------------------------------------------

    private void updateComponents( )
    {
        okButton.setEnabled( !passphrasePanel.getField( ).isEmpty( ) );
    }

    //------------------------------------------------------------------

    private void showContextMenu( MouseEvent event )
    {
        if ( event == null )
            passphrasePanel.getContextMenu( ).show( getContentPane( ), 0, 0 );
        else if ( event.isPopupTrigger( ) )
            passphrasePanel.getContextMenu( ).show( event.getComponent( ), event.getX( ), event.getY( ) );
    }

    //------------------------------------------------------------------

    private void onShowContextMenu( )
    {
        showContextMenu( null );
    }

    //------------------------------------------------------------------

    private void onAccept( )
    {
        accepted = true;
        onClose( );
    }

    //------------------------------------------------------------------

    private void onSkip( )
    {
        passphrasePanel.getField( ).setText( null );
        accepted = true;
        onClose( );
    }

    //------------------------------------------------------------------

    private void onClose( )
    {
        location = getLocation( );
        setVisible( false );
        dispose( );
    }

    //------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class variables
////////////////////////////////////////////////////////////////////////

    private static  Point   location;

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

    private boolean         accepted;
    private PassphrasePanel passphrasePanel;
    private JButton         okButton;

}

//----------------------------------------------------------------------
