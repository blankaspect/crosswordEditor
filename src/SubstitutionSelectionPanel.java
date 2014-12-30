/*====================================================================*\

SubstitutionSelectionPanel.java

Substitution selection panel class.

\*====================================================================*/


// IMPORTS


import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
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

import java.util.List;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import uk.org.blankaspect.exception.AppException;

import uk.org.blankaspect.gui.FButton;
import uk.org.blankaspect.gui.FCheckBox;
import uk.org.blankaspect.gui.FLabel;
import uk.org.blankaspect.gui.FTextField;
import uk.org.blankaspect.gui.GuiUtilities;
import uk.org.blankaspect.gui.SingleSelectionList;
import uk.org.blankaspect.gui.TextRendering;

import uk.org.blankaspect.regex.RegexUtilities;
import uk.org.blankaspect.regex.Substitution;

import uk.org.blankaspect.util.KeyAction;

//----------------------------------------------------------------------


// SUBSTITUTION SELECTION PANEL CLASS


class SubstitutionSelectionPanel
    extends JPanel
    implements ActionListener, ChangeListener, ListSelectionListener
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

    private static final    int MAX_NUM_SUBSTITUTIONS   = 32;

    private static final    int MODIFIERS_MASK  = ActionEvent.ALT_MASK | ActionEvent.META_MASK |
                                                            ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK;

    private static final    String  ADD_STR                         = "Add";
    private static final    String  EDIT_STR                        = "Edit";
    private static final    String  DELETE_STR                      = "Delete";
    private static final    String  ADD_SUBSTITUTION_STR            = "Add substitution";
    private static final    String  EDIT_SUBSTITUTION_STR           = "Edit substitution";
    private static final    String  DELETE_SUBSTITUTION_STR         = "Delete substitution";
    private static final    String  DELETE_SUBSTITUTION_MESSAGE_STR = "Do you want to delete the " +
                                                                        "selected substitution?";

    // Commands
    private interface Command
    {
        String  ADD     = "add";
        String  EDIT    = SingleSelectionList.Command.EDIT_ELEMENT;
        String  DELETE  = "delete";
    }

////////////////////////////////////////////////////////////////////////
//  Member classes : non-inner classes
////////////////////////////////////////////////////////////////////////


    // SUBSTITUTION SELECTION LIST CLASS


    private static class SubstitutionList
        extends SingleSelectionList<Substitution>
    {

    ////////////////////////////////////////////////////////////////////
    //  Constants
    ////////////////////////////////////////////////////////////////////

        private static final    int TARGET_FIELD_NUM_COLUMNS        = 32;
        private static final    int REPLACEMENT_FIELD_NUM_COLUMNS   = 24;

        private static final    int SEPARATOR_WIDTH = 1;

        private static final    String  REGEX_STR   = "RE";

        private static final    Color   RE_TEXT_COLOUR      = new Color( 176, 80, 0 );
        private static final    Color   SEPARATOR_COLOUR    = new Color( 192, 200, 192 );

    ////////////////////////////////////////////////////////////////////
    //  Constructors
    ////////////////////////////////////////////////////////////////////

        private SubstitutionList( int numRows )
        {
            super( TARGET_FIELD_NUM_COLUMNS + REPLACEMENT_FIELD_NUM_COLUMNS, numRows,
                   AppFont.MAIN.getFont( ) );
            regexStrWidth = getFontMetrics( getFont( ) ).stringWidth( REGEX_STR );
            setExtraWidth( 4 * getHorizontalMargin( ) + 2 * SEPARATOR_WIDTH + regexStrWidth );
            setRowHeight( getRowHeight( ) + 1 );
        }

        //--------------------------------------------------------------

    ////////////////////////////////////////////////////////////////////
    //  Instance methods : overriding methods
    ////////////////////////////////////////////////////////////////////

        @Override
        protected void drawElement( Graphics gr,
                                    int      index )
        {
            // Create copy of graphics context
            gr = gr.create( );

            // Set rendering hints for text antialiasing and fractional metrics
            TextRendering.setHints( (Graphics2D)gr );

            // Get substitution
            Substitution substitution = getElement( index );

            // Draw regex indicator
            FontMetrics fontMetrics = gr.getFontMetrics( );
            int rowHeight = getRowHeight( );
            int x = getHorizontalMargin( );
            int y = index * rowHeight;
            int textY = y + DEFAULT_VERTICAL_MARGIN + fontMetrics.getAscent( );
            if ( !substitution.isLiteral( ) )
            {
                gr.setColor( RE_TEXT_COLOUR );
                gr.drawString( REGEX_STR, x, textY );
            }

            // Draw first separator
            x += regexStrWidth + getHorizontalMargin( );
            gr.setColor( SEPARATOR_COLOUR );
            gr.drawLine( x, y, x, y + rowHeight - 1 );

            // Get target text and truncate it if it is too wide
            int replacementFieldWidth = REPLACEMENT_FIELD_NUM_COLUMNS * getColumnWidth( );
            int targetFieldWidth = getMaxTextWidth( ) - replacementFieldWidth;
            String text = truncateText( substitution.getTarget( ), fontMetrics, targetFieldWidth );

            // Draw target text
            x += SEPARATOR_WIDTH + getHorizontalMargin( );
            Color textColour = getForegroundColour( index );
            gr.setColor( textColour );
            gr.drawString( text, x, textY );

            // Draw second separator
            x += targetFieldWidth + getHorizontalMargin( );
            gr.setColor( SEPARATOR_COLOUR );
            gr.drawLine( x, y, x, y + rowHeight - 1 );

            // Get replacement text and truncate it if it is too wide
            text = truncateText( substitution.getReplacement( ), fontMetrics, replacementFieldWidth );

            // Draw replacement text
            x += SEPARATOR_WIDTH + getHorizontalMargin( );
            gr.setColor( textColour );
            gr.drawString( text, x, textY );

            // Draw bottom border
            y += rowHeight - 1;
            gr.setColor( SEPARATOR_COLOUR );
            gr.drawLine( 0, y, getWidth( ) - 1, y );
        }

        //--------------------------------------------------------------

    ////////////////////////////////////////////////////////////////////
    //  Instance variables
    ////////////////////////////////////////////////////////////////////

        private int regexStrWidth;

    }

    //==================================================================


    // SUBSTITUTION DIALOG BOX CLASS


    private static class SubstitutionDialog
        extends JDialog
        implements ActionListener, DocumentListener
    {

    ////////////////////////////////////////////////////////////////////
    //  Constants
    ////////////////////////////////////////////////////////////////////

        private static final    int TARGET_FIELD_NUM_COLUMNS        = 40;
        private static final    int REPLACEMENT_FIELD_NUM_COLUMNS   = 40;

        private static final    String  REGEX_STR       = "Regular expression";
        private static final    String  TARGET_STR      = "Target:";
        private static final    String  REPLACEMENT_STR = "Replacement:";

        // Commands
        private interface Command
        {
            String  ACCEPT  = "accept";
            String  CLOSE   = "close";
        }

    ////////////////////////////////////////////////////////////////////
    //  Enumerated types
    ////////////////////////////////////////////////////////////////////


        // ERROR IDENTIFIERS


        private enum ErrorId
            implements AppException.Id
        {

        ////////////////////////////////////////////////////////////////
        //  Constants
        ////////////////////////////////////////////////////////////////

            MALFORMED_TARGET
            ( "The target is not a well-formed regular expression.\n(%1)" );

        ////////////////////////////////////////////////////////////////
        //  Constructors
        ////////////////////////////////////////////////////////////////

            private ErrorId( String message )
            {
                this.message = message;
            }

            //----------------------------------------------------------

        ////////////////////////////////////////////////////////////////
        //  Instance methods : AppException.Id interface
        ////////////////////////////////////////////////////////////////

            public String getMessage( )
            {
                return message;
            }

            //----------------------------------------------------------

        ////////////////////////////////////////////////////////////////
        //  Instance variables
        ////////////////////////////////////////////////////////////////

            private String  message;

        }

        //==============================================================

    ////////////////////////////////////////////////////////////////////
    //  Constructors
    ////////////////////////////////////////////////////////////////////

        private SubstitutionDialog( Window       owner,
                                    String       titleStr,
                                    Substitution substitution )
        {

            // Call superclass constructor
            super( owner, titleStr, Dialog.ModalityType.APPLICATION_MODAL );

            // Set icons
            setIconImages( owner.getIconImages( ) );


            //----  Control panel

            GridBagLayout gridBag = new GridBagLayout( );
            GridBagConstraints gbc = new GridBagConstraints( );

            JPanel controlPanel = new JPanel( gridBag );
            GuiUtilities.setPaddedLineBorder( controlPanel );

            int gridY = 0;

            // Label: target
            JLabel targetLabel = new FLabel( TARGET_STR );

            gbc.gridx = 0;
            gbc.gridy = gridY;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.weightx = 0.0;
            gbc.weighty = 0.0;
            gbc.anchor = GridBagConstraints.LINE_END;
            gbc.fill = GridBagConstraints.NONE;
            gbc.insets = AppConstants.COMPONENT_INSETS;
            gridBag.setConstraints( targetLabel, gbc );
            controlPanel.add( targetLabel );

            // Field: target
            targetField = new FTextField( (substitution == null) ? null : substitution.getTarget( ),
                                          TARGET_FIELD_NUM_COLUMNS );
            targetField.getDocument( ).addDocumentListener( this );

            gbc.gridx = 1;
            gbc.gridy = gridY++;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.weightx = 0.0;
            gbc.weighty = 0.0;
            gbc.anchor = GridBagConstraints.LINE_START;
            gbc.fill = GridBagConstraints.NONE;
            gbc.insets = AppConstants.COMPONENT_INSETS;
            gridBag.setConstraints( targetField, gbc );
            controlPanel.add( targetField );

            // Label: replacement
            JLabel replacementLabel = new FLabel( REPLACEMENT_STR );

            gbc.gridx = 0;
            gbc.gridy = gridY;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.weightx = 0.0;
            gbc.weighty = 0.0;
            gbc.anchor = GridBagConstraints.LINE_END;
            gbc.fill = GridBagConstraints.NONE;
            gbc.insets = AppConstants.COMPONENT_INSETS;
            gridBag.setConstraints( replacementLabel, gbc );
            controlPanel.add( replacementLabel );

            // Field: replacement
            replacementField = new FTextField( (substitution == null) ? null
                                                                      : substitution.getReplacement( ),
                                               REPLACEMENT_FIELD_NUM_COLUMNS );

            gbc.gridx = 1;
            gbc.gridy = gridY++;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.weightx = 0.0;
            gbc.weighty = 0.0;
            gbc.anchor = GridBagConstraints.LINE_START;
            gbc.fill = GridBagConstraints.NONE;
            gbc.insets = AppConstants.COMPONENT_INSETS;
            gridBag.setConstraints( replacementField, gbc );
            controlPanel.add( replacementField );

            // Check box: regular expression
            regularExpressionCheckBox = new FCheckBox( REGEX_STR );
            regularExpressionCheckBox.setSelected( (substitution != null) && !substitution.isLiteral( ) );

            gbc.gridx = 1;
            gbc.gridy = gridY++;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.weightx = 0.0;
            gbc.weighty = 0.0;
            gbc.anchor = GridBagConstraints.LINE_START;
            gbc.fill = GridBagConstraints.NONE;
            gbc.insets = AppConstants.COMPONENT_INSETS;
            gridBag.setConstraints( regularExpressionCheckBox, gbc );
            controlPanel.add( regularExpressionCheckBox );


            //----  Button panel

            JPanel buttonPanel = new JPanel( new GridLayout( 1, 0, 8, 0 ) );
            buttonPanel.setBorder( BorderFactory.createEmptyBorder( 3, 8, 3, 8 ) );

            // Button: OK
            okButton = new FButton( AppConstants.OK_STR );
            okButton.setActionCommand( Command.ACCEPT );
            okButton.addActionListener( this );
            buttonPanel.add( okButton );

            // Button: cancel
            JButton cancelButton = new FButton( AppConstants.CANCEL_STR );
            cancelButton.setActionCommand( Command.CLOSE );
            cancelButton.addActionListener( this );
            buttonPanel.add( cancelButton );


            //----  Main panel

            JPanel mainPanel = new JPanel( gridBag );
            mainPanel.setBorder( BorderFactory.createEmptyBorder( 2, 2, 2, 2 ) );

            gridY = 0;

            gbc.gridx = 0;
            gbc.gridy = gridY++;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.weightx = 0.0;
            gbc.weighty = 0.0;
            gbc.anchor = GridBagConstraints.NORTH;
            gbc.fill = GridBagConstraints.NONE;
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
            KeyAction.create( mainPanel, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT,
                              KeyStroke.getKeyStroke( KeyEvent.VK_ESCAPE, 0 ), Command.CLOSE, this );

            // Update components
            updateAcceptButton( );


            //----  Window

            // Set content pane
            setContentPane( mainPanel );

            // Dispose of window explicitly
            setDefaultCloseOperation( DO_NOTHING_ON_CLOSE );

            // Handle window closing
            addWindowListener( new WindowAdapter( )
            {
                @Override
                public void windowClosing( WindowEvent event )
                {
                    onClose( );
                }
            } );

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

        //--------------------------------------------------------------

    ////////////////////////////////////////////////////////////////////
    //  Class methods
    ////////////////////////////////////////////////////////////////////

        private static Substitution showDialog( Component    parent,
                                                String       titleStr,
                                                Substitution substitution )
        {
            return new SubstitutionDialog( GuiUtilities.getWindow( parent ), titleStr, substitution ).
                                                                                        getSubstitution( );
        }

        //--------------------------------------------------------------

    ////////////////////////////////////////////////////////////////////
    //  Instance methods : DocumentListener interface
    ////////////////////////////////////////////////////////////////////

        public void changedUpdate( DocumentEvent event )
        {
            // do nothing
        }

        //--------------------------------------------------------------

        public void insertUpdate( DocumentEvent event )
        {
            updateAcceptButton( );
        }

        //--------------------------------------------------------------

        public void removeUpdate( DocumentEvent event )
        {
            updateAcceptButton( );
        }

        //--------------------------------------------------------------

    ////////////////////////////////////////////////////////////////////
    //  Instance methods : ActionListener interface
    ////////////////////////////////////////////////////////////////////

        public void actionPerformed( ActionEvent event )
        {
            String command = event.getActionCommand( );

            if ( command.equals( Command.ACCEPT ) )
                onAccept( );

            else if ( command.equals( Command.CLOSE ) )
                onClose( );
        }

        //--------------------------------------------------------------

    ////////////////////////////////////////////////////////////////////
    //  Instance methods
    ////////////////////////////////////////////////////////////////////

        private Substitution getSubstitution( )
        {
            return ( accepted ? new Substitution( targetField.getText( ), replacementField.getText( ),
                                                  !regularExpressionCheckBox.isSelected( ) )
                              : null );
        }

        //--------------------------------------------------------------

        private void updateAcceptButton( )
        {
            okButton.setEnabled( !targetField.isEmpty( ) );
        }

        //--------------------------------------------------------------

        private void validateUserInput( )
            throws AppException
        {
            // Target
            try
            {
                if ( regularExpressionCheckBox.isSelected( ) )
                    Pattern.compile( targetField.getText( ) );
            }
            catch ( PatternSyntaxException e )
            {
                GuiUtilities.setFocus( targetField );
                int index = e.getIndex( );
                if ( index >= 0 )
                    targetField.setCaretPosition( index );
                throw new AppException( ErrorId.MALFORMED_TARGET, RegexUtilities.getExceptionMessage( e ) );
            }
        }

        //--------------------------------------------------------------

        private void onAccept( )
        {
            try
            {
                validateUserInput( );
                accepted = true;
                onClose( );
            }
            catch ( AppException e )
            {
                JOptionPane.showMessageDialog( this, e, App.SHORT_NAME, JOptionPane.ERROR_MESSAGE );
            }
        }

        //--------------------------------------------------------------

        private void onClose( )
        {
            location = getLocation( );
            setVisible( false );
            dispose( );
        }

        //--------------------------------------------------------------

    ////////////////////////////////////////////////////////////////////
    //  Class variables
    ////////////////////////////////////////////////////////////////////

        private static  Point   location;

    ////////////////////////////////////////////////////////////////////
    //  Instance variables
    ////////////////////////////////////////////////////////////////////

        private boolean     accepted;
        private FTextField  targetField;
        private FTextField  replacementField;
        private JCheckBox   regularExpressionCheckBox;
        private JButton     okButton;

    }

    //==================================================================

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

    public SubstitutionSelectionPanel( int numRows )
    {

        //----  List scroll pane

        // Selection list
        substitutionList = new SubstitutionList( numRows );
        substitutionList.addActionListener( this );
        substitutionList.addListSelectionListener( this );

        // Scroll pane: selection list
        substitutionListScrollPane = new JScrollPane( substitutionList,
                                                      JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                                                      JScrollPane.HORIZONTAL_SCROLLBAR_NEVER );
        substitutionListScrollPane.getVerticalScrollBar( ).setFocusable( false );
        substitutionListScrollPane.getVerticalScrollBar( ).getModel( ).addChangeListener( this );

        substitutionList.setViewport( substitutionListScrollPane.getViewport( ) );


        //----  List button panel

        JPanel listButtonPanel = new JPanel( new GridLayout( 0, 1, 0, 8 ) );

        // Button: add
        addButton = new FButton( ADD_STR + AppConstants.ELLIPSIS_STR );
        addButton.setMnemonic( KeyEvent.VK_A );
        addButton.setActionCommand( Command.ADD );
        addButton.addActionListener( this );
        listButtonPanel.add( addButton );

        // Button: edit
        editButton = new FButton( EDIT_STR + AppConstants.ELLIPSIS_STR );
        editButton.setMnemonic( KeyEvent.VK_E );
        editButton.setActionCommand( Command.EDIT );
        editButton.addActionListener( this );
        listButtonPanel.add( editButton );

        // Button: delete
        deleteButton = new FButton( DELETE_STR + AppConstants.ELLIPSIS_STR );
        deleteButton.setMnemonic( KeyEvent.VK_D );
        deleteButton.setActionCommand( Command.DELETE );
        deleteButton.addActionListener( this );
        listButtonPanel.add( deleteButton );

        // Update buttons
        updateButtons( );


        //----  This panel

        GridBagLayout gridBag = new GridBagLayout( );
        GridBagConstraints gbc = new GridBagConstraints( );

        setLayout( gridBag );

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 2;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets( 0, 0, 0, 8 );
        gridBag.setConstraints( substitutionListScrollPane, gbc );
        add( substitutionListScrollPane );

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets( 0, 0, 0, 0 );
        gridBag.setConstraints( listButtonPanel, gbc );
        add( listButtonPanel );

    }

    //------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : ActionListener interface
////////////////////////////////////////////////////////////////////////

    public void actionPerformed( ActionEvent event )
    {
        String command = event.getActionCommand( );

        if ( command.equals( Command.ADD ) )
            onAdd( );

        else if ( command.equals( Command.EDIT ) )
            onEdit( );

        else if ( command.equals( Command.DELETE ) )
        {
            if ( (event.getModifiers( ) & MODIFIERS_MASK) == ActionEvent.SHIFT_MASK )
                onDelete( );
            else
                onConfirmDelete( );
        }

        else if ( command.equals( SingleSelectionList.Command.DELETE_ELEMENT ) )
            onConfirmDelete( );

        else if ( command.equals( SingleSelectionList.Command.DELETE_EX_ELEMENT ) )
            onDelete( );

        else if ( command.equals( SingleSelectionList.Command.MOVE_ELEMENT_UP ) )
            onMoveUp( );

        else if ( command.equals( SingleSelectionList.Command.MOVE_ELEMENT_DOWN ) )
            onMoveDown( );

        else if ( command.equals( SingleSelectionList.Command.DRAG_ELEMENT ) )
            onMove( );
    }

    //------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : ChangeListener interface
////////////////////////////////////////////////////////////////////////

    public void stateChanged( ChangeEvent event )
    {
        if ( !substitutionListScrollPane.getVerticalScrollBar( ).getValueIsAdjusting( ) &&
             !substitutionList.isDragging( ) )
            substitutionList.snapViewPosition( );
    }

    //------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : ListSelectionListener interface
////////////////////////////////////////////////////////////////////////

    public void valueChanged( ListSelectionEvent event )
    {
        if ( !event.getValueIsAdjusting( ) )
            updateButtons( );
    }

    //------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : overriding methods
////////////////////////////////////////////////////////////////////////

    @Override
    public void setEnabled( boolean enabled )
    {
        super.setEnabled( enabled );
        substitutionList.setEnabled( enabled );
        updateButtons( );
    }

    //------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

    public List<Substitution> getSubstitutions( )
    {
        return substitutionList.getElements( );
    }

    //------------------------------------------------------------------

    public void setSubstitutions( List<Substitution> substitutions )
    {
        substitutionList.setElements( substitutions );
    }

    //------------------------------------------------------------------

    private void updateButtons( )
    {
        addButton.setEnabled( isEnabled( ) && substitutionList.getNumElements( ) < MAX_NUM_SUBSTITUTIONS );
        editButton.setEnabled( isEnabled( ) && substitutionList.isSelection( ) );
        deleteButton.setEnabled( isEnabled( ) && substitutionList.isSelection( ) );
    }

    //------------------------------------------------------------------

    private void onAdd( )
    {
        Substitution substitution = SubstitutionDialog.showDialog( this, ADD_SUBSTITUTION_STR, null );
        if ( substitution != null )
        {
            substitutionList.addElement( substitution );
            updateButtons( );
        }
    }

    //------------------------------------------------------------------

    private void onEdit( )
    {
        int index = substitutionList.getSelectedIndex( );
        Substitution substitution = SubstitutionDialog.showDialog( this, EDIT_SUBSTITUTION_STR,
                                                                   substitutionList.getElement( index ) );
        if ( substitution != null )
            substitutionList.setElement( index, substitution );
    }

    //------------------------------------------------------------------

    private void onConfirmDelete( )
    {
        String[] optionStrs = Util.getOptionStrings( DELETE_STR );
        if ( JOptionPane.showOptionDialog( this, DELETE_SUBSTITUTION_MESSAGE_STR, DELETE_SUBSTITUTION_STR,
                                           JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
                                           null, optionStrs, optionStrs[1] ) == JOptionPane.OK_OPTION )
            onDelete( );
    }

    //------------------------------------------------------------------

    private void onDelete( )
    {
        substitutionList.removeElement( substitutionList.getSelectedIndex( ) );
        updateButtons( );
    }

    //------------------------------------------------------------------

    private void onMoveUp( )
    {
        int index = substitutionList.getSelectedIndex( );
        substitutionList.moveElement( index, index - 1 );
    }

    //------------------------------------------------------------------

    private void onMoveDown( )
    {
        int index = substitutionList.getSelectedIndex( );
        substitutionList.moveElement( index, index + 1 );
    }

    //------------------------------------------------------------------

    private void onMove( )
    {
        int fromIndex = substitutionList.getSelectedIndex( );
        int toIndex = substitutionList.getDragEndIndex( );
        if ( toIndex > fromIndex )
            --toIndex;
        substitutionList.moveElement( fromIndex, toIndex );
    }

    //------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

    private SubstitutionList    substitutionList;
    private JScrollPane         substitutionListScrollPane;
    private JButton             addButton;
    private JButton             editButton;
    private JButton             deleteButton;

}

//----------------------------------------------------------------------
