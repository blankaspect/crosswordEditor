/*====================================================================*\

FieldSelectionDialog.java

Field selection dialog box class.

\*====================================================================*/


// IMPORTS


import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowEvent;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import uk.org.blankaspect.gui.Colours;
import uk.org.blankaspect.gui.GuiUtilities;
import uk.org.blankaspect.gui.TextRendering;

import uk.org.blankaspect.util.KeyAction;
import uk.org.blankaspect.util.NumberUtilities;
import uk.org.blankaspect.util.StringUtilities;

//----------------------------------------------------------------------


// FIELD SELECTION DIALOG BOX CLASS


class FieldSelectionDialog
    extends JDialog
    implements ActionListener
{

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

    private static final    Color   TEXT_COLOUR1                = Colours.FOREGROUND;
    private static final    Color   TEXT_COLOUR2                = new Color( 192, 96, 0 );
    private static final    Color   BACKGROUND_COLOUR           = new Color( 248, 240, 216 );
    private static final    Color   SELECTION_BACKGROUND_COLOUR = Colours.FOCUSED_SELECTION_BACKGROUND;
    private static final    Color   BORDER_COLOUR               = new Color( 192, 184, 160);
    private static final    Color   FOCUSED_BORDER_COLOUR       = Color.BLACK;

    private static final    ImageIcon   CROSS_ICON  = new ImageIcon( ImageData.CROSS );

    // Commands
    private interface Command
    {
        String  SELECT_UP_UNIT      = "selectUpUnit";
        String  SELECT_DOWN_UNIT    = "selectDownUnit";
        String  SELECT_UP_MAX       = "selectUpMax";
        String  SELECT_DOWN_MAX     = "selectDownMax";
        String  SELECT_LEFT_UNIT    = "selectLeftUnit";
        String  SELECT_RIGHT_UNIT   = "selectRightUnit";
        String  SELECT_LEFT_MAX     = "selectLeftMax";
        String  SELECT_RIGHT_MAX    = "selectRightMax";
        String  ACCEPT              = "accept";
        String  CLOSE               = "close";
    }

    private static final    KeyAction.KeyCommandPair[]  KEY_COMMANDS    =
    {
        new KeyAction.KeyCommandPair
        (
            KeyStroke.getKeyStroke( KeyEvent.VK_ENTER, 0 ),
            Command.ACCEPT
        ),
        new KeyAction.KeyCommandPair
        (
            KeyStroke.getKeyStroke( KeyEvent.VK_ESCAPE, 0 ),
            Command.CLOSE
        )
    };

    private static final    KeyAction.KeyCommandPair[]  SELECTION_KEY_COMMANDS  =
    {
        new KeyAction.KeyCommandPair
        (
            KeyStroke.getKeyStroke( KeyEvent.VK_UP, 0 ),
            Command.SELECT_UP_UNIT
        ),
        new KeyAction.KeyCommandPair
        (
            KeyStroke.getKeyStroke( KeyEvent.VK_DOWN, 0 ),
            Command.SELECT_DOWN_UNIT
        ),
        new KeyAction.KeyCommandPair
        (
            KeyStroke.getKeyStroke( KeyEvent.VK_PAGE_UP, 0 ),
            Command.SELECT_UP_MAX
        ),
        new KeyAction.KeyCommandPair
        (
            KeyStroke.getKeyStroke( KeyEvent.VK_PAGE_DOWN, 0 ),
            Command.SELECT_DOWN_MAX
        ),
        new KeyAction.KeyCommandPair
        (
            KeyStroke.getKeyStroke( KeyEvent.VK_HOME, KeyEvent.CTRL_DOWN_MASK ),
            Command.SELECT_UP_MAX
        ),
        new KeyAction.KeyCommandPair
        (
            KeyStroke.getKeyStroke( KeyEvent.VK_END, KeyEvent.CTRL_DOWN_MASK ),
            Command.SELECT_DOWN_MAX
        ),
        new KeyAction.KeyCommandPair
        (
            KeyStroke.getKeyStroke( KeyEvent.VK_LEFT, 0 ),
            Command.SELECT_LEFT_UNIT
        ),
        new KeyAction.KeyCommandPair
        (
            KeyStroke.getKeyStroke( KeyEvent.VK_RIGHT, 0 ),
            Command.SELECT_RIGHT_UNIT
        ),
        new KeyAction.KeyCommandPair
        (
            KeyStroke.getKeyStroke( KeyEvent.VK_LEFT, KeyEvent.CTRL_DOWN_MASK ),
            Command.SELECT_LEFT_MAX
        ),
        new KeyAction.KeyCommandPair
        (
            KeyStroke.getKeyStroke( KeyEvent.VK_RIGHT, KeyEvent.CTRL_DOWN_MASK ),
            Command.SELECT_RIGHT_MAX
        ),
        new KeyAction.KeyCommandPair
        (
            KeyStroke.getKeyStroke( KeyEvent.VK_HOME, 0 ),
            Command.SELECT_LEFT_MAX
        ),
        new KeyAction.KeyCommandPair
        (
            KeyStroke.getKeyStroke( KeyEvent.VK_END, 0 ),
            Command.SELECT_RIGHT_MAX
        )
    };

    // Image data
    private interface ImageData
    {
        // cross-black-9x9.png
        byte[]  CROSS   =
        {
            (byte)0x89, (byte)0x50, (byte)0x4E, (byte)0x47, (byte)0x0D, (byte)0x0A, (byte)0x1A, (byte)0x0A,
            (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x0D, (byte)0x49, (byte)0x48, (byte)0x44, (byte)0x52,
            (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x09, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x09,
            (byte)0x08, (byte)0x06, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0xE0, (byte)0x91, (byte)0x06,
            (byte)0x10, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x3D, (byte)0x49, (byte)0x44, (byte)0x41,
            (byte)0x54, (byte)0x78, (byte)0xDA, (byte)0x63, (byte)0x60, (byte)0x60, (byte)0x60, (byte)0x38,
            (byte)0x0B, (byte)0xC4, (byte)0xD1, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0x67, (byte)0x40,
            (byte)0xC7, (byte)0x20, (byte)0x71, (byte)0xA8, (byte)0x3C, (byte)0x98, (byte)0xF1, (byte)0x13,
            (byte)0x5D, (byte)0x21, (byte)0xB2, (byte)0x38, (byte)0x86, (byte)0x00, (byte)0x56, (byte)0x3E,
            (byte)0x16, (byte)0x9D, (byte)0xD3, (byte)0xD0, (byte)0x4D, (byte)0x46, (byte)0x77, (byte)0x03,
            (byte)0x48, (byte)0x01, (byte)0x88, (byte)0x31, (byte)0x0D, (byte)0x45, (byte)0x9C, (byte)0x68,
            (byte)0x93, (byte)0x08, (byte)0xBA, (byte)0x89, (byte)0x28, (byte)0xDF, (byte)0x11, (byte)0x13,
            (byte)0x4E, (byte)0x00, (byte)0x71, (byte)0x0D, (byte)0x86, (byte)0x54, (byte)0x49, (byte)0xE8,
            (byte)0xBD, (byte)0x9A, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x49, (byte)0x45,
            (byte)0x4E, (byte)0x44, (byte)0xAE, (byte)0x42, (byte)0x60, (byte)0x82
        };
    }

////////////////////////////////////////////////////////////////////////
//  Member classes : inner classes
////////////////////////////////////////////////////////////////////////


    // FIELD SELECTION PANEL CLASS


    private class FieldSelectionPanel
        extends JComponent
        implements ActionListener, FocusListener, MouseListener, MouseMotionListener
    {

    ////////////////////////////////////////////////////////////////////
    //  Constants
    ////////////////////////////////////////////////////////////////////

        private static final    int GRID_LINE_WIDTH     = 1;
        private static final    int HORIZONTAL_MARGIN   = 2;
        private static final    int VERTICAL_MARGIN     = 1;

        private static final    int COLUMNS_PER_ROW = 12;

        private static final    String  NONE_STR    = "None";

    ////////////////////////////////////////////////////////////////////
    //  Constructors
    ////////////////////////////////////////////////////////////////////

        private FieldSelectionPanel( Grid.Field.Id selectedId )
        {
            // Initialise instance variables
            this.selectedId = selectedId;

            // Initialise cell dimensions
            AppFont.MAIN.apply( this );
            FontMetrics fontMetrics = getFontMetrics( getFont( ) );
            int textWidth = 0;
            for ( Direction direction : Direction.DEFINED_DIRECTIONS )
            {
                int width = fontMetrics.stringWidth( direction.getSuffix( ) );
                if ( textWidth < width )
                    textWidth = width;
            }
            int maxNumDigits = 0;
            for ( Direction direction : fieldMap.keySet( ) )
            {
                for ( ClueDialog.Field field : fieldMap.get( direction ) )
                {
                    int numDigits = NumberUtilities.getNumDigitsDec( field.id.number );
                    if ( maxNumDigits < numDigits )
                        maxNumDigits = numDigits;
                }
            }
            textWidth += fontMetrics.stringWidth( StringUtilities.createCharString( '0', maxNumDigits ) );
            textWidth += fontMetrics.stringWidth( ClueDialog.Field.DEFINED_PREFIX );
            columnWidth = 2 * HORIZONTAL_MARGIN + textWidth;
            columnWidth = GRID_LINE_WIDTH + Math.max( CROSS_ICON.getIconWidth( ), columnWidth );
            rowHeight = 2 * VERTICAL_MARGIN + fontMetrics.getAscent( ) + fontMetrics.getDescent( );
            rowHeight = GRID_LINE_WIDTH + Math.max( CROSS_ICON.getIconHeight( ), rowHeight );

            // Initialise numbers of columns and rows
            int maxNumIds = 0;
            int totalNumRows = 1;
            numRows = new EnumMap<>( Direction.class );
            numRows.put( Direction.NONE, 1 );
            for ( Direction direction : fieldMap.keySet( ) )
            {
                int numIds = fieldMap.get( direction ).size( );
                int rows = NumberUtilities.roundUpQuotientInt( numIds, COLUMNS_PER_ROW );
                numRows.put( direction, rows );
                totalNumRows += rows;

                if ( maxNumIds < numIds )
                    maxNumIds = numIds;
            }
            int noneWidth = GRID_LINE_WIDTH + 4 * HORIZONTAL_MARGIN + fontMetrics.stringWidth( NONE_STR );
            numColumnsNone = NumberUtilities.roundUpQuotientInt( noneWidth, columnWidth );
            numColumns = Math.min( Math.max( numColumnsNone + 1, maxNumIds ), COLUMNS_PER_ROW );

            // Set preferred size of panel
            setPreferredSize( new Dimension( numColumns * columnWidth + GRID_LINE_WIDTH,
                                             totalNumRows * rowHeight + GRID_LINE_WIDTH ) );

            // Set component attributes
            setOpaque( true );
            setFocusable( true );

            // Add commands to action map
            KeyAction.create( this, JComponent.WHEN_FOCUSED, this, SELECTION_KEY_COMMANDS );

            // Add listeners
            addFocusListener( this );
            addMouseListener( this );
            addMouseMotionListener( this );
        }

        //--------------------------------------------------------------

    ////////////////////////////////////////////////////////////////////
    //  Instance methods : ActionListener interface
    ////////////////////////////////////////////////////////////////////

        public void actionPerformed( ActionEvent event )
        {
            String command = event.getActionCommand( );

            if ( command.equals( Command.SELECT_UP_UNIT ) )
                onSelectUpUnit( );

            else if ( command.equals( Command.SELECT_DOWN_UNIT ) )
                onSelectDownUnit( );

            else if ( command.equals( Command.SELECT_UP_MAX ) )
                onSelectUpMax( );

            else if ( command.equals( Command.SELECT_DOWN_MAX ) )
                onSelectDownMax( );

            else if ( command.equals( Command.SELECT_LEFT_UNIT ) )
                onSelectLeftUnit( );

            else if ( command.equals( Command.SELECT_RIGHT_UNIT ) )
                onSelectRightUnit( );

            else if ( command.equals( Command.SELECT_LEFT_MAX ) )
                onSelectLeftMax( );

            else if ( command.equals( Command.SELECT_RIGHT_MAX ) )
                onSelectRightMax( );
        }

        //--------------------------------------------------------------

    ////////////////////////////////////////////////////////////////////
    //  Instance methods : FocusListener interface
    ////////////////////////////////////////////////////////////////////

        public void focusGained( FocusEvent event )
        {
            repaint( );
        }

        //--------------------------------------------------------------

        public void focusLost( FocusEvent event )
        {
            repaint( );
        }

        //--------------------------------------------------------------

    ////////////////////////////////////////////////////////////////////
    //  Instance methods : MouseListener interface
    ////////////////////////////////////////////////////////////////////

        public void mouseClicked( MouseEvent event )
        {
            // do nothing
        }

        //--------------------------------------------------------------

        public void mouseEntered( MouseEvent event )
        {
            // do nothing
        }

        //--------------------------------------------------------------

        public void mouseExited( MouseEvent event )
        {
            // do nothing
        }

        //--------------------------------------------------------------

        public void mousePressed( MouseEvent event )
        {
            if ( SwingUtilities.isLeftMouseButton( event ) )
                setSelectedId( getId( event ) );
        }

        //--------------------------------------------------------------

        public void mouseReleased( MouseEvent event )
        {
            if ( SwingUtilities.isLeftMouseButton( event ) )
            {
                setSelectedId( getId( event ) );
                onAccept( );
            }
        }

        //--------------------------------------------------------------

    ////////////////////////////////////////////////////////////////////
    //  Instance methods : MouseMotionListener interface
    ////////////////////////////////////////////////////////////////////

        public void mouseDragged( MouseEvent event )
        {
            if ( SwingUtilities.isLeftMouseButton( event ) )
                setSelectedId( getId( event ) );
        }

        //--------------------------------------------------------------

        public void mouseMoved( MouseEvent event )
        {
            // do nothing
        }

        //--------------------------------------------------------------

    ////////////////////////////////////////////////////////////////////
    //  Instance methods : overriding methods
    ////////////////////////////////////////////////////////////////////

        @Override
        protected void paintComponent( Graphics gr )
        {
            // Create copy of graphics context
            gr = gr.create( );

            // Get dimensions
            int width = getWidth( );
            int height = getHeight( );

            // Fill background
            Rectangle rect = gr.getClipBounds( );
            gr.setColor( BACKGROUND_COLOUR );
            gr.fillRect( rect.x, rect.y, rect.width, rect.height );

            // Fill background of selected cell
            int noneWidth = numColumnsNone * columnWidth;
            Rectangle cellRect = null;
            if ( selectedId != null )
            {
                int row = getRow( selectedId );
                int column = getColumn( selectedId );
                gr.setColor( SELECTION_BACKGROUND_COLOUR );
                cellRect = new Rectangle( column * columnWidth + GRID_LINE_WIDTH,
                                          row * rowHeight + GRID_LINE_WIDTH,
                                          ((row == 0) ? noneWidth : columnWidth) - GRID_LINE_WIDTH,
                                          rowHeight - GRID_LINE_WIDTH );
                gr.fillRect( cellRect.x, cellRect.y, cellRect.width, cellRect.height );
            }

            // Set rendering hints for text antialiasing and fractional metrics
            TextRendering.setHints( (Graphics2D)gr );

            // Draw strings and boxes
            FontMetrics fontMetrics = gr.getFontMetrics( );
            int ascent = fontMetrics.getAscent( );
            int y = 0;
            gr.setColor( TEXT_COLOUR1 );
            gr.drawString( NONE_STR, (noneWidth - fontMetrics.stringWidth( NONE_STR )) / 2,
                           y + GRID_LINE_WIDTH + VERTICAL_MARGIN + ascent );

            gr.setColor( BORDER_COLOUR );
            int x = noneWidth;
            gr.drawLine( x, y + 1, x, y + rowHeight - 1 );

            gr.drawImage( CROSS_ICON.getImage( ), x + (width - noneWidth - CROSS_ICON.getIconWidth( )) / 2,
                          y + GRID_LINE_WIDTH + (rowHeight - CROSS_ICON.getIconHeight( )) / 2, null );

            y += rowHeight;

            for ( Direction direction : fieldMap.keySet( ) )
            {
                List<ClueDialog.Field> fields = fieldMap.get( direction );
                for ( int row = 0; row < numRows.get( direction ); ++row )
                {
                    gr.setColor( BORDER_COLOUR );
                    gr.drawLine( 0, y, width - 1, y );

                    x = 0;
                    for ( int column = 0; column < numColumns; ++column )
                    {
                        int index = row * numColumns + column;
                        if ( index < fields.size( ) )
                        {
                            ClueDialog.Field field = fields.get( index );

                            String str = Integer.toString( field.id.number );
                            String str1 = field.defined ? ClueDialog.Field.DEFINED_PREFIX + str : str;
                            int strWidth1 = fontMetrics.stringWidth( str1 );
                            String str2 = direction.getSuffix( );
                            int strWidth2 = fontMetrics.stringWidth( str2 );
                            int textX = x + columnWidth - HORIZONTAL_MARGIN - strWidth1 - strWidth2;
                            int textY = y + GRID_LINE_WIDTH + VERTICAL_MARGIN + ascent;

                            gr.setColor( TEXT_COLOUR1 );
                            gr.drawString( str1, textX, textY );
                            textX += strWidth1;

                            gr.setColor( TEXT_COLOUR2 );
                            gr.drawString( str2, textX, textY );

                            gr.setColor( BORDER_COLOUR );
                            int x2 = x + columnWidth + GRID_LINE_WIDTH - 1;
                            int y2 = y + rowHeight + GRID_LINE_WIDTH - 1;
                            gr.drawLine( x, y2, x2, y2 );
                            gr.drawLine( x2, y, x2, y2 );
                        }
                        x += columnWidth;
                    }
                    y += rowHeight;
                }
            }

            // Draw selection border
            if ( isFocusOwner( ) && (cellRect != null) )
            {
                Graphics2D gr2d = (Graphics2D)gr;
                Stroke oldStroke = gr2d.getStroke( );
                gr2d.setStroke( GuiUtilities.getBasicDash( ) );
                gr2d.setColor( FOCUSED_BORDER_COLOUR );
                gr2d.drawRect( cellRect.x, cellRect.y, cellRect.width - 1, cellRect.height - 1 );
                gr2d.setStroke( oldStroke );
            }

            // Draw border
            gr.setColor( BORDER_COLOUR );
            gr.drawRect( 0, 0, width - 1, height - 1 );
        }

        //--------------------------------------------------------------

    ////////////////////////////////////////////////////////////////////
    //  Instance methods
    ////////////////////////////////////////////////////////////////////

        private Grid.Field.Id getId( int row,
                                     int column )
        {
            if ( row == 0 )
                return ( (column < numColumnsNone) ? ClueDialog.Field.NO_ID : null );
            int rowOffset = 0;
            for ( Direction direction : numRows.keySet( ) )
            {
                int rows = numRows.get( direction );
                if ( row < rowOffset + rows )
                {
                    int index = (row - rowOffset) * numColumns + column;
                    List<ClueDialog.Field> fields = fieldMap.get( direction );
                    return ( (index < fields.size( )) ? fields.get( index ).id : null );
                }
                rowOffset += rows;
            }
            return null;
        }

        //--------------------------------------------------------------

        private Grid.Field.Id getId( MouseEvent event )
        {
            int column = event.getX( ) / columnWidth;
            int row = event.getY( ) / rowHeight;
            return ( ((column >= 0) && (column < numColumns) && (row >= 0) && (row < getRowOffset( null )))
                                                                                    ? getId( row, column )
                                                                                    : null );
        }

        //--------------------------------------------------------------

        private int getRowOffset( Direction direction )
        {
            int offset = 0;
            for ( Direction d : numRows.keySet( ) )
            {
                if ( d == direction )
                    return offset;
                offset += numRows.get( d );
            }
            return offset;
        }

        //--------------------------------------------------------------

        private int getRow( Grid.Field.Id id )
        {
            if ( id == null )
                return -1;
            if ( id.equals( ClueDialog.Field.NO_ID ) )
                return 0;
            int index = fieldMap.get( id.direction ).indexOf( new ClueDialog.Field( id ) );
            return ( getRowOffset( id.direction ) + index / numColumns );
        }

        //--------------------------------------------------------------

        private int getColumn( Grid.Field.Id id )
        {
            if ( id == null )
                return -1;
            if ( id.equals( ClueDialog.Field.NO_ID ) )
                return 0;
            return ( fieldMap.get( id.direction ).indexOf( new ClueDialog.Field( id ) ) % numColumns );
        }

        //--------------------------------------------------------------

        private void incrementSelectionColumn( int increment )
        {
            Grid.Field.Id id = (selectedId == null) ? ClueDialog.Field.NO_ID : selectedId;
            int row = getRow( id );
            int column = Math.min( Math.max( 0, getColumn( id ) + increment ), numColumns - 1 );
            id = getId( row, column );
            if ( id == null )
            {
                if ( row == 0 )
                    id = ClueDialog.Field.NO_ID;
                else
                {
                    List<ClueDialog.Field> fields = fieldMap.get( getId( row, 0 ).direction );
                    id = fields.get( fields.size( ) - 1 ).id;
                }
            }
            setSelectedId( id );
        }

        //--------------------------------------------------------------

        private void incrementSelectionRow( int increment )
        {
            Grid.Field.Id id = (selectedId == null) ? ClueDialog.Field.NO_ID : selectedId;
            int row = Math.min( Math.max( 0, getRow( id ) + increment ), getRowOffset( null ) - 1 );
            id = getId( row, getColumn( id ) );
            if ( id == null )
            {
                if ( row == 0 )
                    id = ClueDialog.Field.NO_ID;
                else
                {
                    List<ClueDialog.Field> fields = fieldMap.get( getId( row, 0 ).direction );
                    id = fields.get( fields.size( ) - 1 ).id;
                }
            }
            setSelectedId( id );
        }

        //--------------------------------------------------------------

        private void setSelectedId( Grid.Field.Id id )
        {
            if ( (id == null) ? (selectedId != null) : !id.equals( selectedId ) )
            {
                selectedId = id;
                repaint( );
            }
        }

        //--------------------------------------------------------------

        private void onSelectUpUnit( )
        {
            incrementSelectionRow( -1 );
        }

        //--------------------------------------------------------------

        private void onSelectDownUnit( )
        {
            incrementSelectionRow( 1 );
        }

        //--------------------------------------------------------------

        private void onSelectUpMax( )
        {
            incrementSelectionRow( -getRowOffset( null ) );
        }

        //--------------------------------------------------------------

        private void onSelectDownMax( )
        {
            incrementSelectionRow( getRowOffset( null ) );
        }

        //--------------------------------------------------------------

        private void onSelectLeftUnit( )
        {
            incrementSelectionColumn( -1 );
        }

        //--------------------------------------------------------------

        private void onSelectRightUnit( )
        {
            incrementSelectionColumn( 1 );
        }

        //--------------------------------------------------------------

        private void onSelectLeftMax( )
        {
            incrementSelectionColumn( -numColumns );
        }

        //--------------------------------------------------------------

        private void onSelectRightMax( )
        {
            incrementSelectionColumn( numColumns );
        }

        //--------------------------------------------------------------

    ////////////////////////////////////////////////////////////////////
    //  Instance variables
    ////////////////////////////////////////////////////////////////////

        private int                     numColumns;
        private int                     numColumnsNone;
        private Map<Direction, Integer> numRows;
        private int                     columnWidth;
        private int                     rowHeight;
        private Grid.Field.Id           selectedId;

    }

    //==================================================================

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

    private FieldSelectionDialog( Component                              parent,
                                  int                                    xOffset,
                                  Map<Direction, List<ClueDialog.Field>> fieldMap,
                                  Grid.Field.Id                          selectedId )
    {

        // Call superclass constructor
        super( GuiUtilities.getWindow( parent ), Dialog.ModalityType.APPLICATION_MODAL );

        // Initialise instance variables
        this.fieldMap = fieldMap;


        //----  Field selection panel

        selectionPanel = new FieldSelectionPanel( selectedId );

        // Add commands to action map
        KeyAction.create( selectionPanel, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, this,
                          KEY_COMMANDS );


        //----  Window

        // Set content pane
        setContentPane( selectionPanel );

        // Omit frame from dialog box
        setUndecorated( true );

        // Dispose of window when it is closed
        setDefaultCloseOperation( DISPOSE_ON_CLOSE );

        // Prevent dialog from being resized
        setResizable( false );

        // Resize dialog to its preferred size
        pack( );

        // Set location of dialog box
        Point location = parent.getLocationOnScreen( );
        int x1 = location.x;
        int x2 = x1 + parent.getWidth( );
        location.x = Math.max( x1, Math.min( x1 + xOffset, x2 - getWidth( ) ) );
        location.y += parent.getHeight( ) - 1;
        setLocation( GuiUtilities.getComponentLocation( this, location ) );

        // Show dialog
        setVisible( true );

    }

    //------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

    public static Grid.Field.Id showDialog( Component                              parent,
                                            int                                    xOffset,
                                            Map<Direction, List<ClueDialog.Field>> fieldMap,
                                            Grid.Field.Id                          selectedId )
    {
        return new FieldSelectionDialog( parent, xOffset, fieldMap, selectedId ).getId( );
    }

    //------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : ActionListener interface
////////////////////////////////////////////////////////////////////////

    public void actionPerformed( ActionEvent event )
    {
        String command = event.getActionCommand( );

        if ( command.equals( Command.ACCEPT ) )
            onAccept( );

        else if ( command.equals( Command.CLOSE ) )
            onClose( );
    }

    //------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

    private Grid.Field.Id getId( )
    {
        return ( accepted ? selectionPanel.selectedId : null );
    }

    //------------------------------------------------------------------

    private void onAccept( )
    {
        accepted = true;
        onClose( );
    }

    //------------------------------------------------------------------

    private void onClose( )
    {
        dispatchEvent( new WindowEvent( this, Event.WINDOW_DESTROY ) );
    }

    //------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

    private boolean                                 accepted;
    private Map<Direction, List<ClueDialog.Field>>  fieldMap;
    private FieldSelectionPanel                     selectionPanel;

}

//----------------------------------------------------------------------
