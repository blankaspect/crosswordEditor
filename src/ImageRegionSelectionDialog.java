/*====================================================================*\

ImageRegionSelectionDialog.java

Image region selection dialog class.

\*====================================================================*/


// IMPORTS


import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.Window;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.awt.image.BufferedImage;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import uk.org.blankaspect.gui.CrosshairCursor;
import uk.org.blankaspect.gui.FButton;
import uk.org.blankaspect.gui.GuiUtilities;

import uk.org.blankaspect.util.ImageUtilities;
import uk.org.blankaspect.util.KeyAction;

//----------------------------------------------------------------------


// IMAGE REGION SELECTION DIALOG CLASS


class ImageRegionSelectionDialog
    extends JDialog
    implements ActionListener
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

    private static final    int MAX_VIEWPORT_WIDTH  = 2048;
    private static final    int MAX_VIEWPORT_HEIGHT = 1536;

    private static final    int IMAGE_LAYER     = 0;
    private static final    int SELECTION_LAYER = IMAGE_LAYER + 1;

    private static final    int SELECTION_DASH_LENGTH   = 8;

    private static final    Color   SELECTION_BOX_COLOUR        = Color.BLACK;
    private static final    Color   SELECTION_BOX_XOR_COLOUR    = Color.WHITE;
    private static final    Color   SELECTION_BORDER_COLOUR     = new Color( 128, 192, 255 );
    private static final    Color   SELECTION_COLOUR            = new Color( 128, 192, 255, 80 );

    private static final    Stroke[]    SELECTION_DASHES;

    // Commands
    private interface Command
    {
        String  ACCEPT  = "accept";
        String  CLOSE   = "close";
    }

////////////////////////////////////////////////////////////////////////
//  Member classes : inner classes
////////////////////////////////////////////////////////////////////////


    // IMAGE PANEL CLASS


    private class ImagePanel
        extends JComponent
    {

    ////////////////////////////////////////////////////////////////////
    //  Constants
    ////////////////////////////////////////////////////////////////////

        private static final    int MIN_WIDTH   = 128;
        private static final    int MIN_HEIGHT  = 96;

    ////////////////////////////////////////////////////////////////////
    //  Constructors
    ////////////////////////////////////////////////////////////////////

        private ImagePanel( )
        {
            // Set component attributes
            setOpaque( true );
            setFocusable( false );
        }

        //--------------------------------------------------------------

    ////////////////////////////////////////////////////////////////////
    //  Instance methods : overriding methods
    ////////////////////////////////////////////////////////////////////

        @Override
        public Dimension getPreferredSize( )
        {
            return new Dimension( image.getWidth( ), image.getHeight( ) );
        }

        //--------------------------------------------------------------

        @Override
        protected void paintComponent( Graphics gr )
        {
            gr.drawImage( image, 0, 0, null );
        }

        //--------------------------------------------------------------

    }

    //==================================================================


    // SELECTION PANEL CLASS


    private class SelectionPanel
        extends JComponent
        implements ActionListener, MouseListener, MouseMotionListener
    {

    ////////////////////////////////////////////////////////////////////
    //  Constants
    ////////////////////////////////////////////////////////////////////

        private static final    int CURSOR_SIZE = 17;

        private static final    int RUBBER_BAND_BOX_INTERVAL    = 200;

    ////////////////////////////////////////////////////////////////////
    //  Constructors
    ////////////////////////////////////////////////////////////////////

        private SelectionPanel( Rectangle selection )
        {
            // Initialise instance variables
            if ( selection != null )
                this.selection = new Rectangle( selection );
            rubberBandBoxTimer = new Timer( RUBBER_BAND_BOX_INTERVAL, this );

            // Set component attributes
            setOpaque( false );
            setFocusable( false );
            setCursor( CrosshairCursor.getCursor( CURSOR_SIZE ) );

            // Add listeners
            addMouseListener( this );
            addMouseMotionListener( this );
        }

        //--------------------------------------------------------------

    ////////////////////////////////////////////////////////////////////
    //  Instance methods : ActionListener interface
    ////////////////////////////////////////////////////////////////////

        public void actionPerformed( ActionEvent event )
        {
            if ( --dashIndex < 0 )
                dashIndex = SELECTION_DASHES.length - 1;
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
            if ( SwingUtilities.isLeftMouseButton( event ) && (anchor == null) )
            {
                selection = null;
                anchor = position = event.getPoint( );
                rubberBandBoxTimer.start( );
                repaint( );
                updateComponents( );
            }
        }

        //--------------------------------------------------------------

        public void mouseReleased( MouseEvent event )
        {
            if ( SwingUtilities.isLeftMouseButton( event ) && (anchor != null) )
            {
                rubberBandBoxTimer.stop( );
                position = event.getPoint( );
                selection = getActiveSelection( );
                anchor = null;
                position = null;
                repaint( );
                updateComponents( );
            }
        }

        //--------------------------------------------------------------

    ////////////////////////////////////////////////////////////////////
    //  Instance methods : MouseMotionListener interface
    ////////////////////////////////////////////////////////////////////

        public void mouseDragged( MouseEvent event )
        {
            if ( SwingUtilities.isLeftMouseButton( event ) && (anchor != null) )
            {
                position = event.getPoint( );
                repaint( );
            }
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
        public Dimension getPreferredSize( )
        {
            return new Dimension( image.getWidth( ), image.getHeight( ) );
        }

        //--------------------------------------------------------------

        @Override
        protected void paintComponent( Graphics gr )
        {
            // Create copy of graphics context
            gr = gr.create( );

            // Draw selection
            if ( anchor != null )
            {
                // Get rectangle of rubber-band box
                Rectangle rect = getActiveSelection( );

                // Draw rubber-band box
                ((Graphics2D)gr).setStroke( SELECTION_DASHES[dashIndex] );
                gr.setColor( SELECTION_BOX_COLOUR );
                gr.setXORMode( SELECTION_BOX_XOR_COLOUR );
                gr.drawRect( rect.x, rect.y, rect.width - 1, rect.height - 1 );
            }
            else if ( selection != null )
            {
                // Fill selected region
                gr.setColor( SELECTION_COLOUR );
                gr.fillRect( selection.x, selection.y, selection.width, selection.height );

                // Draw border of selected region
                gr.setColor( SELECTION_BORDER_COLOUR );
                gr.drawRect( selection.x, selection.y, selection.width - 1, selection.height - 1 );
            }
        }

        //--------------------------------------------------------------

    ////////////////////////////////////////////////////////////////////
    //  Instance methods
    ////////////////////////////////////////////////////////////////////

        private boolean isSelection( )
        {
            return ( selection != null );
        }

        //--------------------------------------------------------------

        private BufferedImage getSelectedRegion( )
        {
            return ( (selection == null) ? null : ImageUtilities.getSubimage( image, selection ) );
        }

        //--------------------------------------------------------------

        private Rectangle getActiveSelection( )
        {
            int x1 = anchor.x;
            int x2 = position.x;
            if ( x1 > x2 )
            {
                int temp = x1;
                x1 = x2;
                x2 = temp;
            }

            int y1 = anchor.y;
            int y2 = position.y;
            if ( y1 > y2 )
            {
                int temp = y1;
                y1 = y2;
                y2 = temp;
            }

            return new Rectangle( x1, y1, x2 - x1 + 1, y2 - y1 + 1 );
        }

        //--------------------------------------------------------------

    ////////////////////////////////////////////////////////////////////
    //  Instance variables
    ////////////////////////////////////////////////////////////////////

        private Point       anchor;
        private Point       position;
        private Rectangle   selection;
        private int         dashIndex;
        private Timer       rubberBandBoxTimer;

    }

    //==================================================================


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

    /**
     * @throws IllegalArgumentException
     */

    private ImageRegionSelectionDialog( Window        owner,
                                        String        titleStr,
                                        int           viewportWidth,
                                        int           viewportHeight,
                                        BufferedImage image,
                                        Rectangle     selection )
    {

        // Call superclass constructor
        super( owner, titleStr, Dialog.ModalityType.APPLICATION_MODAL );

        // Validate arguments
        if ( (viewportWidth < ImagePanel.MIN_WIDTH) || (viewportWidth > MAX_VIEWPORT_WIDTH) ||
             (viewportHeight < ImagePanel.MIN_HEIGHT) || (viewportHeight > MAX_VIEWPORT_HEIGHT) )
            throw new IllegalArgumentException( );
        if ( (selection != null) &&
             ((selection.x < 0) || (selection.width < 0) ||
                                                    (selection.x + selection.width > image.getWidth( )) ||
              (selection.y < 0) || (selection.height < 0) ||
                                                    (selection.y + selection.height > image.getHeight( ))) )
            throw new IllegalArgumentException( );

        // Set icons
        setIconImages( owner.getIconImages( ) );

        // Initialise instance variables
        this.image = image;


        //----  Image panel and selection panel

        // Layered pane
        int imageWidth = image.getWidth( );
        int imageHeight = image.getHeight( );
        JLayeredPane layeredPane = new JLayeredPane( );
        layeredPane.setPreferredSize( new Dimension( imageWidth, imageHeight ) );

        // Image panel
        ImagePanel imagePanel = new ImagePanel( );
        imagePanel.setSize( imageWidth, imageHeight );
        layeredPane.add( imagePanel, Integer.valueOf( IMAGE_LAYER ) );

        // Selection panel (overlay)
        selectionPanel = new SelectionPanel( selection );
        selectionPanel.setSize( imageWidth, imageHeight );
        layeredPane.add( selectionPanel, Integer.valueOf( SELECTION_LAYER ) );

        // Scroll pane: image panel and selection panel
        JScrollPane imageScrollPane = new JScrollPane( layeredPane, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                                                       JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS );
        int width = Math.min( Math.max( ImagePanel.MIN_WIDTH, imageWidth ), viewportWidth );
        int height = Math.min( Math.max( ImagePanel.MIN_HEIGHT, imageHeight ), viewportHeight );
        int x = (selection == null) ? Math.max( 0, (imageWidth - width) / 2 )
                                    : selection.x - Math.max( 0, (width - selection.width) / 2 );
        int y = (selection == null) ? Math.max( 0, (imageHeight - height) / 2 )
                                    : selection.y - Math.max( 0, (height - selection.height) / 2 );
        imageScrollPane.getViewport( ).setPreferredSize( new Dimension( width, height ) );
        imageScrollPane.getViewport( ).setViewPosition( new Point( x, y ) );
        imageScrollPane.getVerticalScrollBar( ).setFocusable( false );
        imageScrollPane.getHorizontalScrollBar( ).setFocusable( false );


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

        // Update components
        updateComponents( );


        //----  Main panel

        GridBagLayout gridBag = new GridBagLayout( );
        GridBagConstraints gbc = new GridBagConstraints( );

        JPanel mainPanel = new JPanel( gridBag );
        mainPanel.setBorder( BorderFactory.createEmptyBorder( 2, 2, 2, 2 ) );

        int gridY = 0;

        gbc.gridx = 0;
        gbc.gridy = gridY++;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets( 0, 0, 0, 0 );
        gridBag.setConstraints( imageScrollPane, gbc );
        mainPanel.add( imageScrollPane );

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
        setLocation( GuiUtilities.getComponentLocation( this, owner ) );

        // Set default button
        getRootPane( ).setDefaultButton( okButton );

        // Show dialog
        setVisible( true );

    }

    //------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

    public static BufferedImage showDialog( Component     parent,
                                            String        titleStr,
                                            int           viewportWidth,
                                            int           viewportHeight,
                                            BufferedImage image,
                                            Rectangle     selection )
    {
        return new ImageRegionSelectionDialog( GuiUtilities.getWindow( parent ), titleStr, viewportWidth,
                                               viewportHeight, image, selection ).getSelectedRegion( );
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

    private BufferedImage getSelectedRegion( )
    {
        return ( accepted ? selectionPanel.getSelectedRegion( ) : null );
    }

    //------------------------------------------------------------------

    private void updateComponents( )
    {
        boolean selection = selectionPanel.isSelection( );
        okButton.setEnabled( selection );
        if ( selection )
            okButton.requestFocusInWindow( );
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
        setVisible( false );
        dispose( );
    }

    //------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Static initialiser
////////////////////////////////////////////////////////////////////////

    static
    {
        SELECTION_DASHES = new Stroke[SELECTION_DASH_LENGTH];
        float halfDashLength = (float)(SELECTION_DASH_LENGTH / 2);
        float[] dash = { halfDashLength, halfDashLength };
        for ( int i = 0; i < SELECTION_DASHES.length; ++i )
            SELECTION_DASHES[i] = new BasicStroke( 1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                                                   10.0f, dash, (float)i );
    }

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

    private boolean         accepted;
    private BufferedImage   image;
    private SelectionPanel  selectionPanel;
    private JButton         okButton;

}

//----------------------------------------------------------------------
