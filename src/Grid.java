/*====================================================================*\

Grid.java

Grid base class.

\*====================================================================*/


// IMPORTS


import java.awt.Color;
import java.awt.Rectangle;

import java.awt.image.BufferedImage;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import java.net.MalformedURLException;
import java.net.URL;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.SwingUtilities;

import org.w3c.dom.Element;

import uk.org.blankaspect.crypto.HmacSha256;
import uk.org.blankaspect.crypto.Salsa20;

import uk.org.blankaspect.exception.AppException;
import uk.org.blankaspect.exception.TaskCancelledException;
import uk.org.blankaspect.exception.UnexpectedRuntimeException;

import uk.org.blankaspect.html.CssMediaRule;
import uk.org.blankaspect.html.CssRuleSet;

import uk.org.blankaspect.util.Base64Encoder;
import uk.org.blankaspect.util.ColourUtilities;
import uk.org.blankaspect.util.NumberUtilities;
import uk.org.blankaspect.util.StringKeyed;
import uk.org.blankaspect.util.StringUtilities;

import uk.org.blankaspect.xml.Attribute;
import uk.org.blankaspect.xml.XmlParseException;
import uk.org.blankaspect.xml.XmlUtilities;
import uk.org.blankaspect.xml.XmlWriter;

//----------------------------------------------------------------------


// GRID BASE CLASS


abstract class Grid
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

    public static final     int MIN_NUM_COLUMNS     = 2;
    public static final     int MAX_NUM_COLUMNS     = 99;
    public static final     int DEFAULT_NUM_COLUMNS = 15;

    public static final     int MIN_NUM_ROWS        = MIN_NUM_COLUMNS;
    public static final     int MAX_NUM_ROWS        = MAX_NUM_COLUMNS;
    public static final     int DEFAULT_NUM_ROWS    = DEFAULT_NUM_COLUMNS;

    public static final     Separator   DEFAULT_SEPARATOR   = Separator.BLOCK;

    public static final     Symmetry    DEFAULT_SYMMETRY    = Symmetry.ROTATION_HALF;

    public static final     Color   DEFAULT_HTML_GRID_COLOUR = new Color( 160, 160, 160 );

    protected static final  List<CssRuleSet>    RULE_SETS   = Arrays.asList
    (
        new CssRuleSet
        (
            HtmlConstants.ElementName.DIV + HtmlConstants.CssSelector.ID + HtmlConstants.Id.GRID,
            new CssRuleSet.Decl( "display",         "table" ),
            new CssRuleSet.Decl( "border-collapse", "collapse" ),
            new CssRuleSet.Decl( "empty-cells",     "show" ),
            new CssRuleSet.Decl( "margin",          "1.0em 0" )
        ),
        new CssRuleSet
        (
            HtmlConstants.ElementName.DIV + HtmlConstants.CssSelector.ID + HtmlConstants.Id.GRID +
                                            HtmlConstants.CssSelector.CHILD + HtmlConstants.ElementName.DIV,
            new CssRuleSet.Decl( "display", "table-row" )
        )
    );

    private static final    int INDENT_INCREMENT    = 2;

    private static final    int MIN_NUM_LINES_PER_DIMENSION = 3;

    private static final    int SOLUTION_LINE_LENGTH    = 72;

    private static final    String  SOLUTION_ENCODING_NAME  = "UTF-8";

    private static final    String  RECTANGULAR_ORTHOGONAL_STR  = "rectangular-orthogonal";
    private static final    String  SOLUTION_STR                = "Solution";

    private final static    Symmetry[]  TEST_SYMMETRIES =
    {
        Symmetry.ROTATION_QUARTER,
        Symmetry.REFLECTION_VERTICAL_HORIZONTAL_AXES,
        Symmetry.ROTATION_HALF,
        Symmetry.REFLECTION_VERTICAL_AXIS,
        Symmetry.REFLECTION_HORIZONTAL_AXIS
    };

    private interface ElementName
    {
        String  ENTRIES     = AppConstants.NS_PREFIX + "entries";
        String  ENTRY       = AppConstants.NS_PREFIX + "entry";
        String  GRID        = AppConstants.NS_PREFIX + "grid";
        String  SOLUTION    = AppConstants.NS_PREFIX + "solution";
    }

    private interface AttrName
    {
        String  ENCRYPTION  = AppConstants.NS_PREFIX + "encryption";
        String  HASH        = AppConstants.NS_PREFIX + "hash";
        String  ID          = AppConstants.NS_PREFIX + "id";
        String  KIND        = AppConstants.NS_PREFIX + "kind";
        String  NONCE       = AppConstants.NS_PREFIX + "nonce";
        String  NUM_COLUMNS = AppConstants.NS_PREFIX + "numColumns";
        String  NUM_ROWS    = AppConstants.NS_PREFIX + "numRows";
        String  LOCATION    = AppConstants.NS_PREFIX + "location";
        String  SEPARATOR   = AppConstants.NS_PREFIX + "separator";
        String  SYMMETRY    = AppConstants.NS_PREFIX + "symmetry";
    }

////////////////////////////////////////////////////////////////////////
//  Enumerated types
////////////////////////////////////////////////////////////////////////


    // GRID SEPARATOR


    enum Separator
        implements StringKeyed
    {

    ////////////////////////////////////////////////////////////////////
    //  Constants
    ////////////////////////////////////////////////////////////////////

        BLOCK
        (
            "block"
        )
        {
            @Override
            public Grid createGrid( int      numColumns,
                                    int      numRows,
                                    Symmetry symmetry )
            {
                return new BlockGrid( numColumns, numRows, symmetry );
            }

            //----------------------------------------------------------

            @Override
            public Grid createGrid( int      numColumns,
                                    int      numRows,
                                    Symmetry symmetry,
                                    String   definition )
                throws AppException
            {
                return new BlockGrid( numColumns, numRows, symmetry, definition );
            }

            //----------------------------------------------------------

            @Override
            public GridPanel createGridPanel( CrosswordDocument document )
            {
                return new GridPanel.Block( document );
            }

            //----------------------------------------------------------

            @Override
            public GridPanel createGridPanel( Grid grid )
            {
                return ( (grid instanceof BlockGrid) ? new GridPanel.Block( grid.createCopy( ) ) : null );
            }

            //----------------------------------------------------------
        },

        BAR
        (
            "bar"
        )
        {
            @Override
            public Grid createGrid( int      numColumns,
                                    int      numRows,
                                    Symmetry symmetry )
            {
                return new BarGrid( numColumns, numRows, symmetry );
            }

            //----------------------------------------------------------

            @Override
            public Grid createGrid( int      numColumns,
                                    int      numRows,
                                    Symmetry symmetry,
                                    String   definition )
                throws AppException
            {
                return new BarGrid( numColumns, numRows, symmetry, definition );
            }

            //----------------------------------------------------------

            @Override
            public GridPanel createGridPanel( CrosswordDocument document )
            {
                return new GridPanel.Bar( document );
            }

            //----------------------------------------------------------

            @Override
            public GridPanel createGridPanel( Grid grid )
            {
                return ( (grid instanceof BarGrid) ? new GridPanel.Bar( grid.createCopy( ) ) : null );
            }

            //----------------------------------------------------------
        };

    ////////////////////////////////////////////////////////////////////
    //  Constructors
    ////////////////////////////////////////////////////////////////////

        private Separator( String key )
        {
            this.key = key;
        }

        //--------------------------------------------------------------

    ////////////////////////////////////////////////////////////////////
    //  Class methods
    ////////////////////////////////////////////////////////////////////

        public static Separator forKey( String key )
        {
            for ( Separator value : values( ) )
            {
                if ( value.key.equals( key ) )
                    return value;
            }
            return null;
        }

        //--------------------------------------------------------------

    ////////////////////////////////////////////////////////////////////
    //  Abstract methods
    ////////////////////////////////////////////////////////////////////

        public abstract Grid createGrid( int      numColumns,
                                         int      numRows,
                                         Symmetry symmetry );

        //--------------------------------------------------------------

        public abstract Grid createGrid( int      numColumns,
                                         int      numRows,
                                         Symmetry symmetry,
                                         String   definition )
            throws AppException;

        //--------------------------------------------------------------

        public abstract GridPanel createGridPanel( CrosswordDocument document );

        //--------------------------------------------------------------

        public abstract GridPanel createGridPanel( Grid grid );

        //--------------------------------------------------------------

    ////////////////////////////////////////////////////////////////////
    //  Instance methods : StringKeyed interface
    ////////////////////////////////////////////////////////////////////

        public String getKey( )
        {
            return key;
        }

        //--------------------------------------------------------------

    ////////////////////////////////////////////////////////////////////
    //  Instance methods : overriding methods
    ////////////////////////////////////////////////////////////////////

        @Override
        public String toString( )
        {
            return StringUtilities.firstCharToUpperCase( key );
        }

        //--------------------------------------------------------------

    ////////////////////////////////////////////////////////////////////
    //  Instance variables
    ////////////////////////////////////////////////////////////////////

        private String  key;

    }

    //==================================================================


    // SYMMETRY


    enum Symmetry
        implements StringKeyed
    {

    ////////////////////////////////////////////////////////////////////
    //  Constants
    ////////////////////////////////////////////////////////////////////

        NONE
        (
            "none",
            "None"
        )
        {
            @Override
            public int[] getPrincipalDimensions( int numColumns,
                                                 int numRows )
            {
                return new int[]{ numColumns, numRows };
            }
        },

        ROTATION_HALF
        (
            "rotate2",
            "Rotation by a half-turn"
        )
        {
            @Override
            public int[] getPrincipalDimensions( int numColumns,
                                                 int numRows )
            {
                return new int[]{ numColumns, (numRows + 1) / 2 };
            }
        },

        ROTATION_QUARTER
        (
            "rotate4",
            "Rotation by a quarter-turn"
        )
        {
            @Override
            public int[] getPrincipalDimensions( int numColumns,
                                                 int numRows )
            {
                return new int[]{ (numColumns + 1) / 2, (numRows + 1) / 2 };
            }

            //----------------------------------------------------------

            @Override
            public boolean supportsDimensions( int numColumns,
                                               int numRows )
            {
                return ( numColumns == numRows );
            }

            //----------------------------------------------------------
        },

        REFLECTION_VERTICAL_AXIS
        (
            "reflectVAxis",
            "Reflection in vertical axis"
        )
        {
            @Override
            public int[] getPrincipalDimensions( int numColumns,
                                                 int numRows )
            {
                return new int[]{ (numColumns + 1) / 2, numRows };
            }
        },

        REFLECTION_HORIZONTAL_AXIS
        (
            "reflectHAxis",
            "Reflection in horizontal axis"
        )
        {
            @Override
            public int[] getPrincipalDimensions( int numColumns,
                                                 int numRows )
            {
                return new int[]{ numColumns, (numRows + 1) / 2 };
            }
        },

        REFLECTION_VERTICAL_HORIZONTAL_AXES
        (
            "reflectVHAxes",
            "Reflection in V and H axes"
        )
        {
            @Override
            public int[] getPrincipalDimensions( int numColumns,
                                                 int numRows )
            {
                return new int[]{ (numColumns + 1) / 2, (numRows + 1) / 2 };
            }
        };

    ////////////////////////////////////////////////////////////////////
    //  Constructors
    ////////////////////////////////////////////////////////////////////

        private Symmetry( String key,
                          String text )
        {
            this.key = key;
            this.text = text;
        }

        //--------------------------------------------------------------

    ////////////////////////////////////////////////////////////////////
    //  Class methods
    ////////////////////////////////////////////////////////////////////

        public static Symmetry forKey( String key )
        {
            for ( Symmetry value : values( ) )
            {
                if ( value.key.equals( key ) )
                    return value;
            }
            return null;
        }

        //--------------------------------------------------------------

    ////////////////////////////////////////////////////////////////////
    //  Abstract methods
    ////////////////////////////////////////////////////////////////////

        public abstract int[] getPrincipalDimensions( int numColumns,
                                                      int numRows );

        //--------------------------------------------------------------

    ////////////////////////////////////////////////////////////////////
    //  Instance methods : StringKeyed interface
    ////////////////////////////////////////////////////////////////////

        public String getKey( )
        {
            return key;
        }

        //--------------------------------------------------------------

    ////////////////////////////////////////////////////////////////////
    //  Instance methods : overriding methods
    ////////////////////////////////////////////////////////////////////

        @Override
        public String toString( )
        {
            return text;
        }

        //--------------------------------------------------------------

    ////////////////////////////////////////////////////////////////////
    //  Instance methods
    ////////////////////////////////////////////////////////////////////

        public boolean supportsDimensions( int numColumns,
                                           int numRows )
        {
            return true;
        }

        //--------------------------------------------------------------

    ////////////////////////////////////////////////////////////////////
    //  Instance variables
    ////////////////////////////////////////////////////////////////////

        private String  key;
        private String  text;

    }

    //==================================================================


    // ENCRYPTION KIND


    enum EncryptionKind
        implements StringKeyed
    {

    ////////////////////////////////////////////////////////////////////
    //  Constants
    ////////////////////////////////////////////////////////////////////

        NONE    ( "none" ),
        SALSA20 ( "salsa20" );

    ////////////////////////////////////////////////////////////////////
    //  Constructors
    ////////////////////////////////////////////////////////////////////

        private EncryptionKind( String key )
        {
            this.key = key;
        }

        //--------------------------------------------------------------

    ////////////////////////////////////////////////////////////////////
    //  Class methods
    ////////////////////////////////////////////////////////////////////

        public static EncryptionKind forKey( String key )
        {
            for ( EncryptionKind value : values( ) )
            {
                if ( value.key.equals( key ) )
                    return value;
            }
            return null;
        }

        //--------------------------------------------------------------

    ////////////////////////////////////////////////////////////////////
    //  Instance methods : StringKeyed interface
    ////////////////////////////////////////////////////////////////////

        public String getKey( )
        {
            return key;
        }

        //--------------------------------------------------------------

    ////////////////////////////////////////////////////////////////////
    //  Instance methods : overriding methods
    ////////////////////////////////////////////////////////////////////

        @Override
        public String toString( )
        {
            return StringUtilities.firstCharToUpperCase( key );
        }

        //--------------------------------------------------------------

    ////////////////////////////////////////////////////////////////////
    //  Instance variables
    ////////////////////////////////////////////////////////////////////

        private String  key;

    }

    //==================================================================


    // ERROR IDENTIFIERS


    private enum ErrorId
        implements AppException.Id
    {

    ////////////////////////////////////////////////////////////////////
    //  Constants
    ////////////////////////////////////////////////////////////////////

        NO_ATTRIBUTE
        ( "The required attribute is missing." ),

        INVALID_ATTRIBUTE
        ( "The attribute is invalid." ),

        ATTRIBUTE_OUT_OF_BOUNDS
        ( "The attribute value is out of bounds." ),

        INCOMPATIBLE_SYMMETRY_AND_DIMENSIONS
        ( "The symmetry of the grid is not compatible with the dimensions of the grid." ),

        CLUES_NOT_DEFINED
        ( "%1 clues are not defined." ),

        ERRORS_IN_CLUE_LISTS
        ( "The lists of clues have the following errors:\n%1" ),

        TOO_FEW_LINES
        ( "There are fewer than " + MIN_NUM_LINES_PER_DIMENSION + " %1 lines of sufficient length in the " +
            "image." ),

        TOO_FEW_COINCIDENT_HORIZONTAL_AND_VERTICAL_LINES
        ( "The largest coincident sets of horizontal and vertical lines are too small to form a grid." ),

        INVALID_FIELD_ID
        ( "The ID does not correspond to a field in the grid." ),

        INCORRECT_NUMBER_OF_ENTRIES
        ( "The number of entries does not match the number of fields." ),

        INCORRECT_ENTRY_LENGTH
        ( "The length of the entry for %1 incorrect." ),

        ILLEGAL_CHARACTER_IN_ENTRY
        ( "The entry for %1 contains an illegal character: '%2'" ),

        CONFLICTING_ENTRY
        ( "The entry for %1 conflicts with an intersecting entry at index %2." ),

        ILLEGAL_CHARACTER_IN_SOLUTION_ENCODING
        ( "The Base64 encoding of the solution contains an illegal character." ),

        MALFORMED_SOLUTION_ENCODING
        ( "The Base64 encoding of the solution is malformed." ),

        INCORRECT_PASSPHRASE
        ( "The passphrase does not match the one that was used to encrypt the solution." ),

        SOLUTION_LENGTH_NOT_CONSISTENT_WITH_GRID
        ( "The length of the solution is not consistent with the grid." ),

        INCORRECT_NUMBER_OF_ANSWERS
        ( "The number of answers does not match the number of fields." ),

        INCORRECT_ANSWER_LENGTH
        ( "The length of the answer for %1 is incorrect." ),

        ILLEGAL_CHARACTER_IN_ANSWER
        ( "The answer for %1 contains an illegal character: '%2'" ),

        CONFLICTING_ANSWER
        ( "The answer for %1 conflicts with an intersecting answer at index %2." ),

        UNSUPPORTED_ENCRYPTION
        ( "The kind of encryption is not supported by this application." );

    ////////////////////////////////////////////////////////////////////
    //  Constructors
    ////////////////////////////////////////////////////////////////////

        private ErrorId( String message )
        {
            this.message = message;
        }

        //--------------------------------------------------------------

    ////////////////////////////////////////////////////////////////////
    //  Instance methods : AppException.Id interface
    ////////////////////////////////////////////////////////////////////

        public String getMessage( )
        {
            return message;
        }

        //--------------------------------------------------------------

    ////////////////////////////////////////////////////////////////////
    //  Instance variables
    ////////////////////////////////////////////////////////////////////

        private String  message;

    }

    //==================================================================

////////////////////////////////////////////////////////////////////////
//  Member classes : non-inner classes
////////////////////////////////////////////////////////////////////////


    // GRID INFORMATION CLASS


    public static class Info
    {

    ////////////////////////////////////////////////////////////////////
    //  Constructors
    ////////////////////////////////////////////////////////////////////

        private Info( int x,
                      int y,
                      int width,
                      int height,
                      int numColumns,
                      int numRows )
        {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.numColumns = numColumns;
            this.numRows = numRows;
        }

        //--------------------------------------------------------------

    ////////////////////////////////////////////////////////////////////
    //  Instance methods
    ////////////////////////////////////////////////////////////////////

        public Rectangle getBounds( )
        {
            return new Rectangle( x, y, width, height );
        }

        //--------------------------------------------------------------

    ////////////////////////////////////////////////////////////////////
    //  Instance variables
    ////////////////////////////////////////////////////////////////////

        int x;
        int y;
        int width;
        int height;
        int numColumns;
        int numRows;

    }

    //==================================================================


    // CELL INDEX PAIR CLASS


    public static class IndexPair
        implements Cloneable
    {

    ////////////////////////////////////////////////////////////////////
    //  Constructors
    ////////////////////////////////////////////////////////////////////

        public IndexPair( int row,
                          int column )
        {
            this.row = row;
            this.column = column;
        }

        //--------------------------------------------------------------

    ////////////////////////////////////////////////////////////////////
    //  Instance methods : overriding methods
    ////////////////////////////////////////////////////////////////////

        @Override
        public boolean equals( Object obj )
        {
            if ( obj instanceof IndexPair )
            {
                IndexPair other = (IndexPair)obj;
                return ( (row == other.row) && (column == other.column) );
            }
            return false;
        }

        //--------------------------------------------------------------

        @Override
        public int hashCode( )
        {
            return ( (row << 16) | column );
        }

        //--------------------------------------------------------------

        @Override
        public IndexPair clone( )
        {
            try
            {
                return (IndexPair)super.clone( );
            }
            catch ( CloneNotSupportedException e )
            {
                throw new UnexpectedRuntimeException( e );
            }
        }

        //--------------------------------------------------------------

        @Override
        public String toString( )
        {
            return ( row + ", " + column );
        }

        //--------------------------------------------------------------

    ////////////////////////////////////////////////////////////////////
    //  Instance methods
    ////////////////////////////////////////////////////////////////////

        public void set( int row,
                         int column )
        {
            this.row = row;
            this.column = column;
        }

        //--------------------------------------------------------------

    ////////////////////////////////////////////////////////////////////
    //  Instance variables
    ////////////////////////////////////////////////////////////////////

        int row;
        int column;

    }

    //==================================================================


    // GRID FIELD CLASS


    public static class Field
        implements Cloneable, Comparable<Field>
    {

    ////////////////////////////////////////////////////////////////////
    //  Member interfaces
    ////////////////////////////////////////////////////////////////////


        // FILTER INTERFACE


        interface Filter
        {

        ////////////////////////////////////////////////////////////////
        //  Methods
        ////////////////////////////////////////////////////////////////

            public boolean acceptField( Field field );

            //----------------------------------------------------------

        }

        //==============================================================

    ////////////////////////////////////////////////////////////////////
    //  Member classes : non-inner classes
    ////////////////////////////////////////////////////////////////////


        // FIELD IDENTIFIER CLASS


        public static class Id
            implements Cloneable, Comparable<Id>
        {

        ////////////////////////////////////////////////////////////////
        //  Constants
        ////////////////////////////////////////////////////////////////

            public static final     Pattern PATTERN;

            private static final    String  REGEX_FRAG1 = "(\\d+)(";
            private static final    String  REGEX_FRAG2 = ")?";

        ////////////////////////////////////////////////////////////////
        //  Constructors
        ////////////////////////////////////////////////////////////////

            public Id( int number )
            {
                this( number, Direction.NONE );
            }

            //----------------------------------------------------------

            public Id( int       number,
                       Direction direction )
            {
                this.number = number;
                this.direction = direction;
            }

            //----------------------------------------------------------

            /**
             * @throws IllegalArgumentException
             */

            public Id( String str )
            {
                Matcher matcher = PATTERN.matcher( str );
                if ( !matcher.matches( ) )
                    throw new IllegalArgumentException( );

                number = Integer.parseInt( matcher.group( 1 ) );
                direction = Direction.forSuffix( matcher.group( 2 ) );
            }

            //----------------------------------------------------------

            /**
             * @throws NumberFormatException
             */

            public Id( String numberStr,
                       String directionStr )
            {
                this( Integer.parseInt( numberStr ) );
                for ( Direction direction : Direction.DEFINED_DIRECTIONS )
                {
                    if ( directionStr == null )
                        break;
                    for ( String keyword : AppConfig.getInstance( ).getClueDirectionKeywords( direction ) )
                    {
                        keyword = StringUtilities.stripBefore( keyword );
                        if ( keyword.equals( directionStr ) )
                        {
                            this.direction = direction;
                            directionStr = null;
                            break;
                        }
                    }
                }
            }

            //----------------------------------------------------------

        ////////////////////////////////////////////////////////////////
        //  Instance methods : Comparable interface
        ////////////////////////////////////////////////////////////////

            public int compareTo( Id other )
            {
                int result = number - other.number;
                if ( result == 0 )
                    result = direction.ordinal( ) - other.direction.ordinal( );
                return result;
            }

            //----------------------------------------------------------

        ////////////////////////////////////////////////////////////////
        //  Instance methods : overriding methods
        ////////////////////////////////////////////////////////////////

            @Override
            public boolean equals( Object obj )
            {
                if ( obj instanceof Id )
                {
                    Id other = (Id)obj;
                    return ( (number == other.number) && (direction == other.direction) );
                }
                return false;
            }

            //----------------------------------------------------------

            @Override
            public int hashCode( )
            {
                return ( (number << 2) | direction.ordinal( ) );
            }

            //----------------------------------------------------------

            @Override
            public Id clone( )
            {
                try
                {
                    return (Id)super.clone( );
                }
                catch ( CloneNotSupportedException e )
                {
                    throw new UnexpectedRuntimeException( e );
                }
            }

            //----------------------------------------------------------

            @Override
            public String toString( )
            {
                return ( Integer.toString( number ) + direction.getSuffix( ) );
            }

            //----------------------------------------------------------

        ////////////////////////////////////////////////////////////////
        //  Instance methods
        ////////////////////////////////////////////////////////////////

            public boolean matches( Id other )
            {
                return ( (number == other.number) &&
                         ((direction == Direction.NONE) || (other.direction == Direction.NONE) ||
                          (direction == other.direction)) );
            }

            //----------------------------------------------------------

            public Id undefined( )
            {
                return new Id( number, Direction.NONE );
            }

            //----------------------------------------------------------

        ////////////////////////////////////////////////////////////////
        //  Static initialiser
        ////////////////////////////////////////////////////////////////

            static
            {
                List<String> suffixes = new ArrayList<>( );
                for ( Direction direction : Direction.values( ) )
                    suffixes.add( direction.getSuffix( ) );

                StringBuilder buffer = new StringBuilder( );
                buffer.append( REGEX_FRAG1 );
                buffer.append( StringUtilities.join( Clue.REGEX_ALTERNATION_CHAR, suffixes ) );
                buffer.append( REGEX_FRAG2 );
                PATTERN = Pattern.compile( buffer.toString( ) );
            }

        ////////////////////////////////////////////////////////////////
        //  Instance variables
        ////////////////////////////////////////////////////////////////

            int         number;
            Direction   direction;

        }

        //==============================================================

    ////////////////////////////////////////////////////////////////////
    //  Constructors
    ////////////////////////////////////////////////////////////////////

        protected Field( int       row,
                         int       column,
                         Direction direction,
                         int       length,
                         int       number )
        {
            this.row = row;
            this.column = column;
            this.direction = direction;
            this.length = length;
            this.number = number;
        }

        //--------------------------------------------------------------

    ////////////////////////////////////////////////////////////////////
    //  Instance methods : Comparable interface
    ////////////////////////////////////////////////////////////////////

        public int compareTo( Field other )
        {
            int result = direction.ordinal( ) - other.direction.ordinal( );
            if ( result == 0 )
                result = row - other.row;
            if ( result == 0 )
                result = column - other.column;
            return result;
        }

        //--------------------------------------------------------------

    ////////////////////////////////////////////////////////////////////
    //  Instance methods : overriding methods
    ////////////////////////////////////////////////////////////////////

        @Override
        public boolean equals( Object obj )
        {
            if ( obj instanceof Field )
            {
                Field other = (Field)obj;
                return ( (row == other.row) && (column == other.column) && (direction == other.direction) );
            }
            return false;
        }

        //--------------------------------------------------------------

        @Override
        public int hashCode( )
        {
            return ( (row << 12) | (column << 2) | direction.ordinal( ) );
        }

        //--------------------------------------------------------------

        @Override
        public Field clone( )
        {
            try
            {
                return (Field)super.clone( );
            }
            catch ( CloneNotSupportedException e )
            {
                throw new UnexpectedRuntimeException( e );
            }
        }

        //--------------------------------------------------------------

    ////////////////////////////////////////////////////////////////////
    //  Instance methods
    ////////////////////////////////////////////////////////////////////

        public int getRow( )
        {
            return row;
        }

        //--------------------------------------------------------------

        public int getColumn( )
        {
            return column;
        }

        //--------------------------------------------------------------

        public Direction getDirection( )
        {
            return direction;
        }

        //--------------------------------------------------------------

        public int getLength( )
        {
            return length;
        }

        //--------------------------------------------------------------

        public int getNumber( )
        {
            return number;
        }

        //--------------------------------------------------------------

        public Id getId( )
        {
            return new Id( number, direction );
        }

        //--------------------------------------------------------------

        public int getEndRow( )
        {
            switch ( direction )
            {
                case NONE:
                    // do nothing
                    break;

                case ACROSS:
                    return row;

                case DOWN:
                    return ( row + length - 1 );
            }
            return -1;
        }

        //--------------------------------------------------------------

        public int getEndColumn( )
        {
            switch ( direction )
            {
                case NONE:
                    // do nothing
                    break;

                case ACROSS:
                    return ( column + length - 1 );

                case DOWN:
                    return column;
            }
            return -1;
        }

        //--------------------------------------------------------------

        public IndexPair getStartIndices( )
        {
            return new IndexPair( row, column );
        }

        //--------------------------------------------------------------

        public IndexPair getEndIndices( )
        {
            switch ( direction )
            {
                case NONE:
                    // do nothing
                    break;

                case ACROSS:
                    return new IndexPair( row, column + length - 1 );

                case DOWN:
                    return new IndexPair( row  + length - 1, column );
            }
            return null;
        }

        //--------------------------------------------------------------

        public boolean containsCell( int row,
                                     int column )
        {
            switch ( direction )
            {
                case NONE:
                    // do nothing
                    break;

                case ACROSS:
                    return ( (row == this.row) &&
                             (column >= this.column) && (column < this.column + length) );

                case DOWN:
                    return ( (column == this.column) &&
                             (row >= this.row) && (row < this.row + length) );
            }
            return false;
        }

        //--------------------------------------------------------------

    ////////////////////////////////////////////////////////////////////
    //  Instance variables
    ////////////////////////////////////////////////////////////////////

        private int         row;
        private int         column;
        private Direction   direction;
        private int         length;
        private int         number;

    }

    //==================================================================


    // GRID ENTRY VALUE CLASS


    public static class EntryValue
    {

    ////////////////////////////////////////////////////////////////////
    //  Constructors
    ////////////////////////////////////////////////////////////////////

        public EntryValue( int  row,
                           int  column,
                           char value )
        {
            this.row = row;
            this.column = column;
            this.value = value;
        }

        //--------------------------------------------------------------

    ////////////////////////////////////////////////////////////////////
    //  Instance variables
    ////////////////////////////////////////////////////////////////////

        int     row;
        int     column;
        char    value;

    }

    //==================================================================


    // GRID ENTRY CLASS


    public static class Entry
    {

    ////////////////////////////////////////////////////////////////////
    //  Constructors
    ////////////////////////////////////////////////////////////////////

        private Entry( Field.Id fieldId,
                       String   text )
        {
            this.fieldId = fieldId.clone( );
            this.text = text;
        }

        //--------------------------------------------------------------

    ////////////////////////////////////////////////////////////////////
    //  Instance variables
    ////////////////////////////////////////////////////////////////////

        Field.Id    fieldId;
        String      text;

    }

    //==================================================================


    // GRID ENTRIES CLASS


    public static class Entries
        implements Cloneable
    {

    ////////////////////////////////////////////////////////////////////
    //  Constants
    ////////////////////////////////////////////////////////////////////

        public static final     char    UNDEFINED_VALUE = '?';
        private static final    char    NO_VALUE        = '\0';

    ////////////////////////////////////////////////////////////////////
    //  Constructors
    ////////////////////////////////////////////////////////////////////

        private Entries( int numColumns,
                         int numRows )
        {
            values = new char[numRows][];
            for ( int i = 0; i < values.length; ++i )
                values[i] = new char[numColumns];
        }

        //--------------------------------------------------------------

    ////////////////////////////////////////////////////////////////////
    //  Instance methods : overriding methods
    ////////////////////////////////////////////////////////////////////

        @Override
        public Entries clone( )
        {
            try
            {
                Entries copy = (Entries)super.clone( );
                copy.values = values.clone( );
                for ( int i = 0; i < values.length; ++i )
                    copy.values[i] = values[i].clone( );
                return copy;
            }
            catch ( CloneNotSupportedException e )
            {
                throw new UnexpectedRuntimeException( e );
            }
        }

        //--------------------------------------------------------------

    ////////////////////////////////////////////////////////////////////
    //  Instance methods
    ////////////////////////////////////////////////////////////////////

        public void clear( )
        {
            for ( int row = 0; row < values.length; ++row )
            {
                for ( int column = 0; column < values[row].length; ++column )
                {
                    if ( values[row][column] != NO_VALUE )
                        values[row][column] = UNDEFINED_VALUE;
                }
            }
            numValues = 0;
        }

        //--------------------------------------------------------------

        protected void init( )
        {
            for ( int i = 0; i < values.length; ++i )
                Arrays.fill( values[i], NO_VALUE );
            numCells = 0;
            numValues = 0;
        }

        //--------------------------------------------------------------

        protected void initValue( int row,
                                  int column )
        {
            if ( values[row][column] == NO_VALUE )
                ++numCells;
            values[row][column] = UNDEFINED_VALUE;
        }

        //--------------------------------------------------------------

        private void setValue( int  row,
                               int  column,
                               char value )
        {
            if ( values[row][column] != UNDEFINED_VALUE )
                --numValues;
            values[row][column] = value;
            if ( value != UNDEFINED_VALUE )
                ++numValues;
        }

        //--------------------------------------------------------------

        private boolean[][] compare( Entries other )
        {
            boolean[][] differences = new boolean[values.length][];
            for ( int row = 0; row < values.length; ++row )
            {
                differences[row] = new boolean[values[row].length];
                for ( int column = 0; column < values[row].length; ++column )
                {
                    if ( values[row][column] != NO_VALUE )
                        differences[row][column] = (values[row][column] != other.values[row][column]);
                }
            }
            return differences;
        }

        //--------------------------------------------------------------

    ////////////////////////////////////////////////////////////////////
    //  Instance variables
    ////////////////////////////////////////////////////////////////////

        private int         numCells;
        private int         numValues;
        private char[][]    values;

    }

    //==================================================================


    // ENCODED SOLUTION CLASS


    public static class EncodedSolution
    {

    ////////////////////////////////////////////////////////////////////
    //  Constructors
    ////////////////////////////////////////////////////////////////////

        private EncodedSolution( byte[] nonce,
                                 byte[] hashValue,
                                 byte[] data )
        {
            this.nonce = nonce;
            this.hashValue = hashValue;
            this.data = data;
        }

        //--------------------------------------------------------------

    ////////////////////////////////////////////////////////////////////
    //  Instance variables
    ////////////////////////////////////////////////////////////////////

        byte[]  nonce;
        byte[]  hashValue;
        byte[]  data;

    }

    //==================================================================


    // CELL BASE CLASS


    protected static abstract class Cell
        implements Cloneable
    {

    ////////////////////////////////////////////////////////////////////
    //  Constants
    ////////////////////////////////////////////////////////////////////

        public static final     String  STYLE_SELECTOR  =
                    HtmlConstants.ElementName.DIV + HtmlConstants.CssSelector.ID + HtmlConstants.Id.GRID +
                    HtmlConstants.CssSelector.CHILD + HtmlConstants.ElementName.DIV +
                    HtmlConstants.CssSelector.CHILD + HtmlConstants.ElementName.DIV;

        private static final    String  WIDTH_PROPERTY      = "width";
        private static final    String  HEIGHT_PROPERTY     = "height";
        private static final    String  FONT_SIZE_PROPERTY  = "font-size";
        private static final    String  BORDER_PROPERTY     = "border";

        private static final    CssRuleSet  RULE_SET    = new CssRuleSet
        (
            STYLE_SELECTOR,
            new CssRuleSet.Decl( "display",          "table-cell" ),
            new CssRuleSet.Decl( "vertical-align",   "top" ),
            new CssRuleSet.Decl( "text-align",       "left" ),
            new CssRuleSet.Decl( WIDTH_PROPERTY,     "%1px" ),
            new CssRuleSet.Decl( HEIGHT_PROPERTY,    "%1px" ),
            new CssRuleSet.Decl( "line-height",      "100%" ),
            new CssRuleSet.Decl( FONT_SIZE_PROPERTY, "%1%%" ),
            new CssRuleSet.Decl( BORDER_PROPERTY,    "1px solid %1" )
        );

    ////////////////////////////////////////////////////////////////////
    //  Constructors
    ////////////////////////////////////////////////////////////////////

        protected Cell( )
        {
            fields = new EnumMap<>( Direction.class );
            fieldOrigins = new EnumMap<>( Direction.class );
            for ( Direction direction : Direction.DEFINED_DIRECTIONS )
                fieldOrigins.put( direction, Boolean.FALSE );
        }

        //--------------------------------------------------------------

    ////////////////////////////////////////////////////////////////////
    //  Class methods
    ////////////////////////////////////////////////////////////////////

        public static CssRuleSet getStyleRuleSet( int     cellSize,
                                                  Color   gridColour,
                                                  double  fieldNumberFontSizeFactor )
        {
            CssRuleSet ruleSet = RULE_SET.clone( );

            String cellSizeStr = Integer.toString( cellSize );
            CssRuleSet.Decl decl = ruleSet.findDeclaration( WIDTH_PROPERTY );
            decl.value = StringUtilities.substitute( decl.value, cellSizeStr );
            decl = ruleSet.findDeclaration( HEIGHT_PROPERTY );
            decl.value = StringUtilities.substitute( decl.value, cellSizeStr );

            String fontSizeStr = AppConstants.FORMAT_1_1.format( fieldNumberFontSizeFactor * 100.0 );
            decl = ruleSet.findDeclaration( FONT_SIZE_PROPERTY );
            decl.value = StringUtilities.substitute( decl.value, fontSizeStr );

            String colourStr = ColourUtilities.colourToHexString( gridColour );
            decl = ruleSet.findDeclaration( BORDER_PROPERTY );
            decl.value = StringUtilities.substitute( decl.value, colourStr );

            return ruleSet;
        }

        //--------------------------------------------------------------

    ////////////////////////////////////////////////////////////////////
    //  Abstract methods
    ////////////////////////////////////////////////////////////////////

        protected abstract void write( XmlWriter writer,
                                       int       indent,
                                       int       cellSize )
            throws IOException;

        //--------------------------------------------------------------

    ////////////////////////////////////////////////////////////////////
    //  Instance methods : overriding methods
    ////////////////////////////////////////////////////////////////////

        @Override
        public Cell clone( )
        {
            try
            {
                Cell copy = (Cell)super.clone( );

                copy.fields = new EnumMap<>( Direction.class );
                for ( Direction direction : fields.keySet( ) )
                    copy.fields.put( direction, fields.get( direction ).clone( ) );

                copy.fieldOrigins = new EnumMap<>( Direction.class );
                for ( Direction direction : fieldOrigins.keySet( ) )
                    copy.fieldOrigins.put( direction, fieldOrigins.get( direction ).booleanValue( ) );

                return copy;
            }
            catch ( CloneNotSupportedException e )
            {
                throw new UnexpectedRuntimeException( e );
            }
        }

        //--------------------------------------------------------------

    ////////////////////////////////////////////////////////////////////
    //  Instance methods
    ////////////////////////////////////////////////////////////////////

        public Field getField( Direction direction )
        {
            return fields.get( direction );
        }

        //--------------------------------------------------------------

        public boolean isInField( )
        {
            return !fields.isEmpty( );
        }

        //--------------------------------------------------------------

        public boolean isFieldOrigin( )
        {
            for ( Direction direction : fieldOrigins.keySet( ) )
            {
                if ( fieldOrigins.get( direction ) )
                    return true;
            }
            return false;
        }

        //--------------------------------------------------------------

        public int getFieldNumber( )
        {
            for ( Direction direction : fieldOrigins.keySet( ) )
            {
                if ( fieldOrigins.get( direction ) )
                    return fields.get( direction ).number;
            }
            return 0;
        }

        //--------------------------------------------------------------

        protected List<Field> getFields( )
        {
            List<Field> fields = new ArrayList<>( );
            for ( Direction direction : this.fields.keySet( ) )
                fields.add( this.fields.get( direction ) );
            return fields;
        }

        //--------------------------------------------------------------

        protected void setField( Direction direction,
                                 Field     field )
        {
            fields.put( direction, field );
        }

        //--------------------------------------------------------------

        protected void setFieldOrigin( Direction direction,
                                       Field     field )
        {
            fields.put( direction, field );
            fieldOrigins.put( direction, Boolean.TRUE );
        }

        //--------------------------------------------------------------

        protected void resetFields( )
        {
            fields.clear( );
            fieldOrigins.clear( );
            for ( Direction direction : Direction.DEFINED_DIRECTIONS )
                fieldOrigins.put( direction, Boolean.FALSE );
        }

        //--------------------------------------------------------------

        protected void writeFieldNumber( XmlWriter writer )
            throws IOException
        {
            int number = getFieldNumber( );
            if ( number > 0 )
                writer.write( Integer.toString( number ) );
        }

        //--------------------------------------------------------------

    ////////////////////////////////////////////////////////////////////
    //  Instance variables
    ////////////////////////////////////////////////////////////////////

        private Map<Direction, Field>   fields;
        private Map<Direction, Boolean> fieldOrigins;

    }

    //==================================================================


    // LINE CLASS


    private static class Line
    {

    ////////////////////////////////////////////////////////////////////
    //  Enumerated types
    ////////////////////////////////////////////////////////////////////


        // LINE ORIENTATION


        private enum Orientation
        {

        ////////////////////////////////////////////////////////////////
        //  Constants
        ////////////////////////////////////////////////////////////////

            HORIZONTAL  ( "horizontal" ),
            VERTICAL    ( "vertical" );

        ////////////////////////////////////////////////////////////////
        //  Constructors
        ////////////////////////////////////////////////////////////////

            private Orientation( String text )
            {
                this.text = text;
            }

            //----------------------------------------------------------

        ////////////////////////////////////////////////////////////////
        //  Instance methods : overriding methods
        ////////////////////////////////////////////////////////////////

            @Override
            public String toString( )
            {
                return text;
            }

            //----------------------------------------------------------

        ////////////////////////////////////////////////////////////////
        //  Instance variables
        ////////////////////////////////////////////////////////////////

            private String  text;

        }

        //==============================================================

    ////////////////////////////////////////////////////////////////////
    //  Constructors
    ////////////////////////////////////////////////////////////////////

        private Line( Orientation orientation,
                      int         x,
                      int         y )
        {
            this.orientation = orientation;
            x1 = x2 = x;
            y1 = y2 = y;
        }

        //--------------------------------------------------------------

    ////////////////////////////////////////////////////////////////////
    //  Instance methods : overriding methods
    ////////////////////////////////////////////////////////////////////

        @Override
        public String toString( )
        {
            return ( orientation.name( ).charAt( 0 ) + ": " + x1 + ", " + y1 + ", " + getLength( ) );
        }

        //--------------------------------------------------------------

    ////////////////////////////////////////////////////////////////////
    //  Instance methods
    ////////////////////////////////////////////////////////////////////

        private int getLength( )
        {
            return ( ((orientation == Orientation.HORIZONTAL) ? x2 - x1 : y2 - y1) + 1 );
        }

        //--------------------------------------------------------------

    ////////////////////////////////////////////////////////////////////
    //  Instance variables
    ////////////////////////////////////////////////////////////////////

        private Orientation orientation;
        private int         x1;
        private int         y1;
        private int         x2;
        private int         y2;

    }

    //==================================================================


    // BOUNDS CLASS


    private static class Bounds
    {

    ////////////////////////////////////////////////////////////////////
    //  Constructors
    ////////////////////////////////////////////////////////////////////

        private Bounds( int x1,
                        int y1,
                        int x2,
                        int y2 )
        {
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
        }

        //--------------------------------------------------------------

    ////////////////////////////////////////////////////////////////////
    //  Instance variables
    ////////////////////////////////////////////////////////////////////

        private int x1;
        private int y1;
        private int x2;
        private int y2;

    }

    //==================================================================


    // PSEUDO-RANDOM NUMBER GENERATOR CLASS


    private static class Prng
    {

    ////////////////////////////////////////////////////////////////////
    //  Constants
    ////////////////////////////////////////////////////////////////////

        private static final    int NUM_ROUNDS  = 20;
        private static final    int BUFFER_SIZE = Salsa20.BLOCK_SIZE;

    ////////////////////////////////////////////////////////////////////
    //  Constructors
    ////////////////////////////////////////////////////////////////////

        private Prng( String passphrase,
                      byte[] nonce )
        {
            prng = new Salsa20( NUM_ROUNDS, Salsa20.stringToKey( passphrase ), nonce );
            buffer = new byte[BUFFER_SIZE];
            indexMask = BUFFER_SIZE - 1;
        }

        //--------------------------------------------------------------

    ////////////////////////////////////////////////////////////////////
    //  Class methods
    ////////////////////////////////////////////////////////////////////

        private static byte[] createNonce( )
        {
            byte[] nonce = new byte[Salsa20.NONCE_SIZE];
            NumberUtilities.longToBytesLE( nonceGenerator.nextLong( ), nonce );
            return nonce;
        }

        //--------------------------------------------------------------

    ////////////////////////////////////////////////////////////////////
    //  Instance methods
    ////////////////////////////////////////////////////////////////////

        private byte[] getKey( )
        {
            return prng.getKey( );
        }

        //--------------------------------------------------------------

        private void combine( byte[] data )
        {
            for ( int i = 0; i < data.length; ++i )
            {
                if ( index == 0 )
                    prng.getNextBlock( buffer, 0 );
                data[i] ^= buffer[index++];
                index &= indexMask;
            }
        }

        //--------------------------------------------------------------

    ////////////////////////////////////////////////////////////////////
    //  Class variables
    ////////////////////////////////////////////////////////////////////

        private static  Random  nonceGenerator  = new Random( );

    ////////////////////////////////////////////////////////////////////
    //  Instance variables
    ////////////////////////////////////////////////////////////////////

        private Salsa20 prng;
        private byte[]  buffer;
        private int     index;
        private int     indexMask;

    }

    //==================================================================

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

    protected Grid( int numColumns,
                    int numRows )
    {
        this.numColumns = numColumns;
        this.numRows = numRows;
        symmetry = Symmetry.NONE;
        fieldLists = new EnumMap<>( Direction.class );
        entries = new Entries( numColumns, numRows );
    }

    //------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

    public static boolean isGridElement( Element element )
    {
        return element.getTagName( ).equals( ElementName.GRID );
    }

    //------------------------------------------------------------------

    public static boolean isEntriesElement( Element element )
    {
        return element.getTagName( ).equals( ElementName.ENTRIES );
    }

    //------------------------------------------------------------------

    public static boolean isSolutionElement( Element element )
    {
        return element.getTagName( ).equals( ElementName.SOLUTION );
    }

    //------------------------------------------------------------------

    public static Grid create( Element element )
        throws XmlParseException
    {
        // Get element path
        String elementPath = XmlUtilities.getElementPath( element );

        // Attribute: kind
        String attrName = AttrName.KIND;
        String attrKey = XmlUtilities.appendAttributeName( elementPath, attrName );
        String attrValue = XmlUtilities.getAttribute( element, attrName );
        if ( attrValue == null )
            throw new XmlParseException( ErrorId.NO_ATTRIBUTE, attrKey );
        if ( !attrValue.equals( RECTANGULAR_ORTHOGONAL_STR ) )
            throw new XmlParseException( ErrorId.INVALID_ATTRIBUTE, attrKey, attrValue );

        // Attribute: separator
        attrName = AttrName.SEPARATOR;
        attrKey = XmlUtilities.appendAttributeName( elementPath, attrName );
        attrValue = XmlUtilities.getAttribute( element, attrName );
        if ( attrValue == null )
            throw new XmlParseException( ErrorId.NO_ATTRIBUTE, attrKey );
        Separator separator = Separator.forKey( attrValue );
        if ( separator == null )
            throw new XmlParseException( ErrorId.INVALID_ATTRIBUTE, attrKey, attrValue );

        // Attribute: number of columns
        attrName = AttrName.NUM_COLUMNS;
        attrKey = XmlUtilities.appendAttributeName( elementPath, attrName );
        attrValue = XmlUtilities.getAttribute( element, attrName );
        if ( attrValue == null )
            throw new XmlParseException( ErrorId.NO_ATTRIBUTE, attrKey );
        int numColumns = 0;
        try
        {
            numColumns = Integer.parseInt( attrValue );
            if ( (numColumns < MIN_NUM_COLUMNS) || (numColumns > MAX_NUM_COLUMNS) )
                throw new XmlParseException( ErrorId.ATTRIBUTE_OUT_OF_BOUNDS, attrKey, attrValue );
        }
        catch ( NumberFormatException e )
        {
            throw new XmlParseException( ErrorId.INVALID_ATTRIBUTE, attrKey, attrValue );
        }

        // Attribute: number of rows
        attrName = AttrName.NUM_ROWS;
        attrKey = XmlUtilities.appendAttributeName( elementPath, attrName );
        attrValue = XmlUtilities.getAttribute( element, attrName );
        if ( attrValue == null )
            throw new XmlParseException( ErrorId.NO_ATTRIBUTE, attrKey );
        int numRows = 0;
        try
        {
            numRows = Integer.parseInt( attrValue );
            if ( (numRows < MIN_NUM_ROWS) || (numRows > MAX_NUM_ROWS) )
                throw new XmlParseException( ErrorId.ATTRIBUTE_OUT_OF_BOUNDS, attrKey, attrValue );
        }
        catch ( NumberFormatException e )
        {
            throw new XmlParseException( ErrorId.INVALID_ATTRIBUTE, attrKey, attrValue );
        }

        // Attribute: symmetry
        attrName = AttrName.SYMMETRY;
        attrKey = XmlUtilities.appendAttributeName( elementPath, attrName );
        attrValue = XmlUtilities.getAttribute( element, attrName );
        if ( attrValue == null )
            throw new XmlParseException( ErrorId.NO_ATTRIBUTE, attrKey );
        Symmetry symmetry = Symmetry.forKey( attrValue );
        if ( symmetry == null )
            throw new XmlParseException( ErrorId.INVALID_ATTRIBUTE, attrKey, attrValue );

        // Check that symmetry is consistent with grid dimensions
        if ( !symmetry.supportsDimensions( numColumns, numRows ) )
            throw new XmlParseException( ErrorId.INCOMPATIBLE_SYMMETRY_AND_DIMENSIONS, attrKey, attrValue );

        // Get non-whitespace text content of element
        String text = element.getTextContent( );
        StringBuilder buffer = new StringBuilder( text.length( ) );
        for ( int i = 0; i < text.length( ); ++i )
        {
            char ch = text.charAt( i );
            if ( !Character.isWhitespace( ch ) )
                buffer.append( ch );
        }

        // Create grid
        Grid grid = null;
        try
        {
            grid = separator.createGrid( numColumns, numRows, symmetry, buffer.toString( ) );
        }
        catch ( AppException e )
        {
            throw new XmlParseException( e.getId( ), elementPath, e.getSubstitutionStrings( ) );
        }
        return grid;
    }

    //------------------------------------------------------------------

    public static Info findGrid( BufferedImage image,
                                 float         brightnessThreshold,
                                 int           minLineLength,
                                 int           minLineSeparation,
                                 int           endpointTolerance )
        throws AppException
    {
        // Find the set of horizontal lines that satisfy the brightness and length constraints
        List<Line> horizontalLines = new ArrayList<>( );
        for ( int y = 0; y < image.getHeight( ); ++y )
        {
            Line line = null;
            for ( int x = 0; x < image.getWidth( ); ++x )
            {
                if ( ColourUtilities.getBrightness( image.getRGB( x, y ) ) < brightnessThreshold )
                {
                    if ( line == null )
                        line = new Line( Line.Orientation.HORIZONTAL, x, y );
                    line.x2 = x;
                }
                else
                {
                    if ( line != null )
                    {
                        if ( line.getLength( ) >= minLineLength )
                            horizontalLines.add( line );
                        line = null;
                    }
                }
            }
            if ( (line != null) && (line.getLength( ) >= minLineLength) )
                horizontalLines.add( line );
        }
        if ( horizontalLines.size( ) < MIN_NUM_LINES_PER_DIMENSION )
            throw new AppException( ErrorId.TOO_FEW_LINES, Line.Orientation.HORIZONTAL.toString( ) );

        // Find the set of vertical lines that satisfy the brightness and length constraints
        List<Line> verticalLines = new ArrayList<>( );
        for ( int x = 0; x < image.getWidth( ); ++x )
        {
            Line line = null;
            for ( int y = 0; y < image.getHeight( ); ++y )
            {
                if ( ColourUtilities.getBrightness( image.getRGB( x, y ) ) < brightnessThreshold )
                {
                    if ( line == null )
                        line = new Line( Line.Orientation.VERTICAL, x, y );
                    line.y2 = y;
                }
                else
                {
                    if ( line != null )
                    {
                        if ( line.getLength( ) >= minLineLength )
                            verticalLines.add( line );
                        line = null;
                    }
                }
            }
            if ( (line != null) && (line.getLength( ) >= minLineLength) )
                verticalLines.add( line );
        }
        if ( verticalLines.size( ) < MIN_NUM_LINES_PER_DIMENSION )
            throw new AppException( ErrorId.TOO_FEW_LINES, Line.Orientation.VERTICAL.toString( ) );

        // Get the largest subset of horizontal and vertical lines within coincident bounding rectangles
        List<Integer> maxHorizontalIndices = new ArrayList<>( );
        List<Integer> maxVerticalIndices = new ArrayList<>( );
        int maxArea = 0;
        for ( int ih = 0; ih < horizontalLines.size( ); ++ih )
        {
            // Get the datum horizontal line
            Line line = horizontalLines.get( ih );

            // Get the bounds of the x coordinates of the horizontal lines of this subset
            int minX1 = line.x1 - endpointTolerance;
            int maxX1 = line.x1 + endpointTolerance;
            int minX2 = line.x2 - endpointTolerance;
            int maxX2 = line.x2 + endpointTolerance;

            // Get the subset of horizontal lines whose x coordinates are within bounds and are sufficiently
            // separated from their nearest neighbour.  Sequences of adjacent lines are reduced to the
            // line at the middle of the sequence.
            List<Integer> horizontalIndices = new ArrayList<>( );
            int prevY = Integer.MIN_VALUE;
            int startIndex = -1;
            for ( int i = 0; i < horizontalLines.size( ); ++i )
            {
                line = horizontalLines.get( i );
                if ( (line.x1 >= minX1) && (line.x1 <= maxX1) &&
                     (line.x2 >= minX2) && (line.x2 <= maxX2) )
                {
                    if ( (startIndex >= 0) && (line.y1 > prevY + 1) )
                    {
                        reduceIndices( horizontalIndices, startIndex );
                        startIndex = -1;
                    }
                    if ( (startIndex < 0) && (line.y1 > prevY + minLineSeparation) )
                        startIndex = horizontalIndices.size( );
                    if ( startIndex >= 0 )
                        horizontalIndices.add( i );
                    prevY = line.y1;
                }
            }
            if ( startIndex >= 0 )
                reduceIndices( horizontalIndices, startIndex );

            // If the subset of horizontal lines is smaller than the current largest subset, try the next
            // subset
            int numHLines = horizontalIndices.size( );
            int maxNumHLines = maxHorizontalIndices.size( );
            if ( (numHLines < MIN_NUM_LINES_PER_DIMENSION) || (numHLines < maxNumHLines) )
                continue;

            // Get the coordinates of the bounding rectangle of the subset of horizontal lines
            Bounds hBounds = getBoundsH( horizontalLines, horizontalIndices );

            // Get the coordinates of the relaxed bounding rectangle of the subset of horizontal lines
            int hx1 = hBounds.x1 - endpointTolerance;
            int hy1 = hBounds.y1 - endpointTolerance;
            int hx2 = hBounds.x2 + endpointTolerance;
            int hy2 = hBounds.y2 + endpointTolerance;

            // Get the largest subset of vertical lines that lie within the relaxed bounding rectangle of
            // the subset of horizontal lines and satisfy separation and endpoint tolerance
            for ( int iv = 0; iv < verticalLines.size( ); ++iv )
            {
                // Get the datum vertical line
                line = verticalLines.get( iv );

                // Test whether the datum lies within the relaxed bounding rectangle of the horizontal lines
                if ( (line.x1 < hx1) || (line.y1 < hy1) || (line.x2 > hx2) || (line.y2 > hy2) )
                    continue;

                // Get the bounds of the y coordinates of the vertical lines of this subset
                int minY1 = line.y1 - endpointTolerance;
                int maxY1 = line.y1 + endpointTolerance;
                int minY2 = line.y2 - endpointTolerance;
                int maxY2 = line.y2 + endpointTolerance;

                // Get the subset of vertical lines whose y coordinates are within bounds and are
                // sufficiently separated from their nearest neighbour.  Sequences of adjacent lines are
                // reduced to the line at the middle of the sequence.
                List<Integer> verticalIndices = new ArrayList<>( );
                int prevX = Integer.MIN_VALUE;
                startIndex = -1;
                for ( int i = 0; i < verticalLines.size( ); ++i )
                {
                    line = verticalLines.get( i );
                    if ( (line.y1 >= minY1) && (line.y1 <= maxY1) &&
                         (line.y2 >= minY2) && (line.y2 <= maxY2) )
                    {
                        if ( (startIndex >= 0) && (line.x1 > prevX + 1) )
                        {
                            reduceIndices( verticalIndices, startIndex );
                            startIndex = -1;
                        }
                        if ( (startIndex < 0) && (line.x1 > prevX + minLineSeparation) )
                            startIndex = verticalIndices.size( );
                        if ( startIndex >= 0 )
                            verticalIndices.add( i );
                        prevX = line.x1;
                    }
                }
                if ( startIndex >= 0 )
                    reduceIndices( verticalIndices, startIndex );

                // If the subset of vertical lines is smaller than the current largest subset, try the next
                // subset
                int numVLines = verticalIndices.size( );
                int maxNumVLines = maxVerticalIndices.size( );
                if ( (numVLines < MIN_NUM_LINES_PER_DIMENSION) || (numVLines < maxNumVLines) )
                    continue;

                // Get the coordinates of the bounding rectangle of the subset of vertical lines
                Bounds vBounds = getBoundsV( verticalLines, verticalIndices );

                // Get the relaxed y coordinates of subset of vertical lines
                int vy1 = vBounds.y1 - endpointTolerance;
                int vy2 = vBounds.y2 + endpointTolerance;

                // Remove horizontal lines that lie outside the relaxed y coordinates of the subset of
                // vertical lines
                for ( int i = 0; i < horizontalIndices.size( ); ++i )
                {
                    line = horizontalLines.get( horizontalIndices.get( i ) );
                    if ( (line.y1 < vy1) || (line.y2 > vy2) )
                        horizontalIndices.remove( i-- );
                }

                // If the subset of horizontal lines is now smaller than the current largest subset, try the
                // next subset
                numHLines = horizontalIndices.size( );
                if ( numHLines < maxNumHLines )
                    continue;

                // Calculate the area of the combined bounding rectangle
                Rectangle rect = getOuterRectangle( getBoundsH( horizontalLines, horizontalIndices ),
                                                    vBounds );
                int area = rect.width * rect.height;

                // Replace the current largest subsets of horizontal and vertical lines
                if ( (numHLines > maxNumHLines) || (numVLines > maxNumVLines) || (area > maxArea) )
                {
                    maxHorizontalIndices = horizontalIndices;
                    maxVerticalIndices = verticalIndices;
                    maxArea = area;
                }
            }
        }

        // Test for sufficient horizontal and vertical lines
        if ( (maxHorizontalIndices.size( ) < MIN_NUM_LINES_PER_DIMENSION) ||
             (maxVerticalIndices.size( ) < MIN_NUM_LINES_PER_DIMENSION) )
            throw new AppException( ErrorId.TOO_FEW_COINCIDENT_HORIZONTAL_AND_VERTICAL_LINES );

        // Get the combined bounding rectangle
        Rectangle rect = getOuterRectangle( getBoundsH( horizontalLines, maxHorizontalIndices ),
                                            getBoundsV( verticalLines, maxVerticalIndices ) );

        // Return grid information
        return new Info( rect.x, rect.y, rect.width, rect.height,
                         maxVerticalIndices.size( ) - 1, maxHorizontalIndices.size( ) - 1 );
    }

    //------------------------------------------------------------------

    private static void reduceIndices( List<Integer> indices,
                                       int           index )
    {
        indices.set( index, indices.get( (index + indices.size( ) - 1) / 2 ) );
        ++index;
        while ( indices.size( ) > index )
            indices.remove( indices.size( ) - 1 );
    }

    //------------------------------------------------------------------

    private static Bounds getBoundsH( List<Line>    lines,
                                      List<Integer> indices )
    {
        int x1 = Integer.MAX_VALUE;
        int x2 = Integer.MIN_VALUE;
        for ( Integer index : indices )
        {
            Line line = lines.get( index );
            if ( x1 > line.x1 )
                x1 = line.x1;
            if ( x2 < line.x2 )
                x2 = line.x2;
        }
        int y1 = lines.get( indices.get( 0 ) ).y1;
        int y2 = lines.get( indices.get( indices.size( ) - 1 ) ).y1;
        return new Bounds( x1, y1, x2, y2 );
    }

    //------------------------------------------------------------------

    private static Bounds getBoundsV( List<Line>    lines,
                                      List<Integer> indices )
    {
        int x1 = lines.get( indices.get( 0 ) ).x1;
        int x2 = lines.get( indices.get( indices.size( ) - 1 ) ).x1;
        int y1 = Integer.MAX_VALUE;
        int y2 = Integer.MIN_VALUE;
        for ( Integer index : indices )
        {
            Line line = lines.get( index );
            if ( y1 > line.y1 )
                y1 = line.y1;
            if ( y2 < line.y2 )
                y2 = line.y2;
        }
        return new Bounds( x1, y1, x2, y2 );
    }

    //------------------------------------------------------------------

    private static Rectangle getOuterRectangle( Bounds hBounds,
                                                Bounds vBounds )
    {
        int x = Math.min( hBounds.x1, vBounds.x1 );
        int y = Math.min( hBounds.y1, vBounds.y1 );
        int width = Math.max( hBounds.x2, vBounds.x2 ) - x + 1;
        int height = Math.max( hBounds.y2, vBounds.y2 ) - y + 1;
        return new Rectangle( x, y, width, height );
    }

    //------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Abstract methods
////////////////////////////////////////////////////////////////////////

    public abstract Separator getSeparator( );

    //------------------------------------------------------------------

    public abstract Cell getCell( int row,
                                  int column );

    //------------------------------------------------------------------

    public abstract List<IndexPair> getIsolatedCells( );

    //------------------------------------------------------------------

    public abstract List<String> getGridDefinition( );

    //------------------------------------------------------------------

    public abstract List<CssRuleSet> getStyleRuleSets( int    cellSize,
                                                       Color  gridColour,
                                                       double fieldNumberFontSizeFactor );

    //------------------------------------------------------------------

    public abstract List<CssMediaRule> getStyleMediaRules( );

    //------------------------------------------------------------------

    public abstract Grid createCopy( );

    //------------------------------------------------------------------

    public abstract boolean canUndoEdit( );

    //------------------------------------------------------------------

    public abstract void undoEdit( );

    //------------------------------------------------------------------

    public abstract boolean canRedoEdit( );

    //------------------------------------------------------------------

    public abstract void redoEdit( );

    //------------------------------------------------------------------

    public abstract void setSymmetry( Symmetry symmetry );

    //------------------------------------------------------------------

    protected abstract boolean isSymmetry( Symmetry symmetry );

    //------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

    public int getNumColumns( )
    {
        return numColumns;
    }

    //------------------------------------------------------------------

    public int getNumRows( )
    {
        return numRows;
    }

    //------------------------------------------------------------------

    public Symmetry getSymmetry( )
    {
        return symmetry;
    }

    //------------------------------------------------------------------

    public int getNumFields( Direction direction )
    {
        return ( fieldLists.containsKey( direction ) ? fieldLists.get( direction ).size( ) : 0 );
    }

    //------------------------------------------------------------------

    public Field getField( Field.Id fieldId )
    {
        if ( fieldLists.containsKey( fieldId.direction ) )
        {
            for ( Field field : fieldLists.get( fieldId.direction ) )
            {
                if ( field.getId( ).equals( fieldId ) )
                    return field;
            }
        }
        return null;
    }

    //------------------------------------------------------------------

    public List<Field> getFields( Clue clue )
    {
        List<Field> fields = new ArrayList<>( );
        for ( int i = 0; i < clue.getNumFields( ); ++i )
        {
            Field field = getField( clue.getFieldId( i ) );
            if ( field != null )
                fields.add( field );
        }
        return fields;
    }

    //------------------------------------------------------------------

    public List<Field> getFields( )
    {
        List<Field> fields = new ArrayList<>( );
        for ( Direction direction : fieldLists.keySet( ) )
            fields.addAll( fieldLists.get( direction ) );
        return fields;
    }

    //------------------------------------------------------------------

    public List<Field> getFields( Direction direction )
    {
        return ( fieldLists.containsKey( direction )
                                            ? Collections.unmodifiableList( fieldLists.get( direction ) )
                                            : new ArrayList<Field>( ) );
    }

    //------------------------------------------------------------------

    public boolean isEntryValue( int row,
                                 int column )
    {
        char ch = entries.values[row][column];
        return ( (ch != Entries.NO_VALUE) && (ch != Entries.UNDEFINED_VALUE) );
    }

    //------------------------------------------------------------------

    public char getEntryValue( int row,
                               int column )
    {
        return entries.values[row][column];
    }

    //------------------------------------------------------------------

    public Entries getEntries( )
    {
        return entries.clone( );
    }

    //------------------------------------------------------------------

    public String getEntriesString( String separator )
    {
        StringBuilder buffer = new StringBuilder( 1024 );
        for ( Direction direction : Direction.DEFINED_DIRECTIONS )
        {
            for ( Field field : fieldLists.get( direction ) )
            {
                if ( (separator != null) && (buffer.length( ) > 0) )
                    buffer.append( separator );

                switch ( direction )
                {
                    case NONE:
                        // do nothing
                        break;

                    case ACROSS:
                        for ( int i = 0; i < field.length; ++i )
                            buffer.append( entries.values[field.row][field.column + i] );
                        break;

                    case DOWN:
                        for ( int i = 0; i < field.length; ++i )
                            buffer.append( entries.values[field.row + i][field.column] );
                        break;
                }
            }
        }
        return buffer.toString( );
    }

    //------------------------------------------------------------------

    public void setEntryValue( int  row,
                               int  column,
                               char value )
    {
        entries.setValue( row, column, value );
        incorrectEntries = null;
    }

    //------------------------------------------------------------------

    public void setEntries( Entries entries )
    {
        this.entries = entries.clone( );
        incorrectEntries = null;
    }

    //------------------------------------------------------------------

    public void setEntries( List<String> entries )
        throws AppException
    {
        // Get list of fields
        List<Field> fields = getFields( );

        // Compare number of entries with number of fields
        if ( entries.size( ) != fields.size( ) )
            throw new AppException( ErrorId.INCORRECT_NUMBER_OF_ENTRIES );

        // Set entries
        this.entries.clear( );
        for ( int i = 0; i < fields.size( ); ++i )
            setEntry( fields.get( i ), entries.get( i ) );

        // Invalidate "incorrect entry" flags
        incorrectEntries = null;
    }

    //------------------------------------------------------------------

    public boolean isEntriesEmpty( )
    {
        return ( entries.numValues == 0 );
    }

    //------------------------------------------------------------------

    public boolean isEntriesComplete( )
    {
        return ( entries.numValues == entries.numCells );
    }

    //------------------------------------------------------------------

    public boolean isIncorrectEntries( )
    {
        return ( incorrectEntries != null );
    }

    //------------------------------------------------------------------

    public boolean isIncorrectEntry( int row,
                                     int column )
    {
        return incorrectEntries[row][column];
    }

    //------------------------------------------------------------------

    public boolean hasSolution( )
    {
        return ( solution != null );
    }

    //------------------------------------------------------------------

    public Entries getSolution( )
    {
        return ( (solution == null) ? null : solution.clone( ) );
    }

    //------------------------------------------------------------------

    public String getSolutionString( String separator )
    {
        StringBuilder buffer = new StringBuilder( 1024 );
        if ( solution != null )
        {
            for ( Direction direction : Direction.DEFINED_DIRECTIONS )
            {
                for ( Field field : fieldLists.get( direction ) )
                {
                    if ( (separator != null) && (buffer.length( ) > 0) )
                        buffer.append( separator );

                    switch ( direction )
                    {
                        case NONE:
                            // do nothing
                            break;

                        case ACROSS:
                            for ( int i = 0; i < field.length; ++i )
                                buffer.append( solution.values[field.row][field.column + i] );
                            break;

                        case DOWN:
                            for ( int i = 0; i < field.length; ++i )
                                buffer.append( solution.values[field.row + i][field.column] );
                            break;
                    }
                }
            }
        }
        return buffer.toString( );
    }

    //------------------------------------------------------------------

    public void setSolution( )
    {
        solution = entries.clone( );
    }

    //------------------------------------------------------------------

    public void setSolution( Entries solution )
    {
        this.solution = (solution == null) ? null : solution.clone( );
    }

    //------------------------------------------------------------------

    public void setSolution( List<String> answers )
        throws AppException
    {
        // Get list of fields
        List<Field> fields = getFields( );

        // Compare number of answers with number of fields
        if ( answers.size( ) != fields.size( ) )
            throw new AppException( ErrorId.INCORRECT_NUMBER_OF_ANSWERS );

        // Set solution
        solution = new Entries( numColumns, numRows );
        for ( int i = 0; i < fields.size( ); ++i )
        {
            // Compare length of answer with length of field
            String answer = answers.get( i );
            Field field = fields.get( i );
            String idStr = field.getId( ).toString( );
            if ( answer.length( ) != field.length )
                throw new AppException( ErrorId.INCORRECT_ANSWER_LENGTH, idStr );

            // Set answer
            for ( int j = 0; j < field.length; ++j )
            {
                char ch0 = answer.charAt( j );
                if ( !Character.isLetterOrDigit( ch0 ) )
                    throw new AppException( ErrorId.ILLEGAL_CHARACTER_IN_ANSWER, idStr,
                                            Character.toString( ch0 ) );

                switch ( field.direction )
                {
                    case NONE:
                        // do nothing
                        break;

                    case ACROSS:
                    {
                        char ch1 = solution.values[field.row][field.column + j];
                        if ( (ch1 != Entries.NO_VALUE) && (ch1 != ch0) )
                            throw new AppException( ErrorId.CONFLICTING_ANSWER,
                                                    new String[]{ idStr, Integer.toString( j ) } );
                        solution.setValue( field.row, field.column + j, ch0 );
                        break;
                    }

                    case DOWN:
                    {
                        char ch1 = solution.values[field.row + j][field.column];
                        if ( (ch1 != Entries.NO_VALUE) && (ch1 != ch0) )
                            throw new AppException( ErrorId.CONFLICTING_ANSWER,
                                                    new String[]{ idStr, Integer.toString( j ) } );
                        solution.setValue( field.row + j, field.column, ch0 );
                        break;
                    }
                }
            }
        }
    }

    //------------------------------------------------------------------

    public void clearSolution( )
    {
        solution = null;
    }

    //------------------------------------------------------------------

    public List<Field> findFields( Field.Filter filter )
    {
        List<Field> fields = new ArrayList<>( );
        for ( Direction direction : fieldLists.keySet( ) )
        {
            for ( Field field : fieldLists.get( direction ) )
            {
                if ( filter.acceptField( field ) )
                    fields.add( field );
            }
        }
        return fields;
    }

    //------------------------------------------------------------------

    public List<Field> findFields( final int       row,
                                   final int       column,
                                   final Direction direction )
    {
        Field.Filter filter = new Field.Filter( )
        {
            @Override
            public boolean acceptField( Field field )
            {
                return ( ((direction == Direction.NONE) || (direction == field.direction)) &&
                         field.containsCell( row, column ) );
            }
        };
        return findFields( filter );
    }

    //------------------------------------------------------------------

    public List<Field> findFields( final Field.Id id )
    {
        Field.Filter filter = new Field.Filter( )
        {
            @Override
            public boolean acceptField( Field field )
            {
                return ( ((id.direction == Direction.NONE) || (id.direction == field.direction)) &&
                         (id.number == field.number) );
            }
        };
        return findFields( filter );
    }

    //------------------------------------------------------------------

    public List<Field> getFullyIntersectingFields( )
    {
        List<Field> fullyIntersectingFields = new ArrayList<>( );
        for ( Direction direction : Direction.DEFINED_DIRECTIONS )
        {
            Direction crossDirection = (direction == Direction.ACROSS) ? Direction.DOWN : Direction.ACROSS;
            if ( fieldLists.containsKey( direction ) && fieldLists.containsKey( crossDirection ) )
            {
                List<Field> fields = fieldLists.get( direction );
                List<Field> crossFields = fieldLists.get( crossDirection );
                for ( Field field : fields )
                {
                    boolean intersects = false;
                    int row = field.row;
                    int column = field.column;
                    for ( int i = 0; i < field.length; ++i )
                    {
                        intersects = false;
                        for ( Field crossField : crossFields )
                        {
                            if ( crossField.containsCell( row, column ) )
                            {
                                intersects = true;
                                break;
                            }
                        }
                        if ( !intersects )
                            break;
                        switch ( direction )
                        {
                            case NONE:
                                // do nothing
                                break;

                            case ACROSS:
                                ++column;
                                break;

                            case DOWN:
                                ++row;
                                break;
                        }
                    }
                    if ( intersects )
                        fullyIntersectingFields.add( field );
                }
            }
        }
        return fullyIntersectingFields;
    }

    //------------------------------------------------------------------

    public void checkEntries( )
    {
        if ( solution != null )
            incorrectEntries = solution.compare( entries );
    }

    //------------------------------------------------------------------

    public void parseEntries( Element entriesElement )
        throws XmlParseException
    {
        XmlUtilities.ElementFilter filter = new XmlUtilities.ElementFilter( )
        {
            @Override
            public boolean acceptElement( Element element )
            {
                return ( element.getTagName( ).equals( ElementName.ENTRY ) );
            }
        };
        for ( Element element : XmlUtilities.getChildElements( entriesElement, filter ) )
        {
            // Get element path
            String elementPath = XmlUtilities.getElementPath( element );

            // Attribute: ID
            String attrName = AttrName.ID;
            String attrKey = XmlUtilities.appendAttributeName( elementPath, attrName );
            String attrValue = XmlUtilities.getAttribute( element, attrName );
            if ( attrValue == null )
                throw new XmlParseException( ErrorId.NO_ATTRIBUTE, attrKey );
            Field.Id fieldId = null;
            try
            {
                fieldId = new Field.Id( attrValue );
            }
            catch ( IllegalArgumentException e )
            {
                throw new XmlParseException( ErrorId.INVALID_ATTRIBUTE, attrKey, attrValue );
            }
            if ( !Direction.DEFINED_DIRECTIONS.contains( fieldId.direction ) )
                throw new XmlParseException( ErrorId.INVALID_ATTRIBUTE, attrKey, attrValue );

            // Get field for ID
            Field field = getField( fieldId );
            if ( field == null )
                throw new XmlParseException( ErrorId.INVALID_FIELD_ID, attrKey, fieldId.toString( ) );

            // Set entry
            try
            {
                setEntry( field, element.getTextContent( ) );
            }
            catch ( AppException e )
            {
                throw new XmlParseException( e.getId( ), elementPath, e.getSubstitutionStrings( ) );
            }
        }
    }

    //------------------------------------------------------------------

    public CrosswordDocument.SolutionProperties parseSolution( Element       element,
                                                               final boolean required )
        throws AppException
    {
        // Get element path
        String elementPath = XmlUtilities.getElementPath( element );

        // Attribute: hash
        String attrName = AttrName.HASH;
        String attrKey = XmlUtilities.appendAttributeName( elementPath, attrName );
        String attrValue = XmlUtilities.getAttribute( element, attrName );
        if ( attrValue == null )
            throw new XmlParseException( ErrorId.NO_ATTRIBUTE, attrKey );
        byte[] hashValue = null;
        try
        {
            hashValue = NumberUtilities.hexStringToBytes( attrValue );
            if ( hashValue.length != HmacSha256.HASH_VALUE_SIZE )
                throw new XmlParseException( ErrorId.INVALID_ATTRIBUTE, attrKey, attrValue );
        }
        catch ( NumberFormatException e )
        {
            throw new XmlParseException( ErrorId.INVALID_ATTRIBUTE, attrKey, attrValue );
        }

        // Attribute: location
        attrName = AttrName.LOCATION;
        attrKey = XmlUtilities.appendAttributeName( elementPath, attrName );
        attrValue = XmlUtilities.getAttribute( element, attrName );
        URL location = null;
        if ( attrValue != null )
        {
            try
            {
                location = new URL( attrValue );
            }
            catch ( MalformedURLException e )
            {
                throw new XmlParseException( ErrorId.INVALID_ATTRIBUTE, attrKey, attrValue );
            }
        }

        // Parse solution in document
        String passphrase = "";
        if ( location == null )
        {
            // Attribute: encryption
            attrName = AttrName.ENCRYPTION;
            attrKey = XmlUtilities.appendAttributeName( elementPath, attrName );
            attrValue = XmlUtilities.getAttribute( element, attrName );
            if ( attrValue == null )
                throw new XmlParseException( ErrorId.NO_ATTRIBUTE, attrKey );
            EncryptionKind encryptionKind = EncryptionKind.forKey( attrValue );
            if ( encryptionKind == null )
                throw new XmlParseException( ErrorId.UNSUPPORTED_ENCRYPTION, attrKey, attrValue );

            // Get passphrase
            if ( encryptionKind != EncryptionKind.NONE )
            {
                final String[] result = new String[1];
                try
                {
                    SwingUtilities.invokeAndWait( new Runnable( )
                    {
                        public void run( )
                        {
                            result[0] = PassphraseDialog.showDialog( App.getInstance( ).getMainWindow( ),
                                                                     SOLUTION_STR, !required );
                        }
                    } );
                }
                catch ( Exception e )
                {
                    e.printStackTrace( );
                }
                passphrase = result[0];
                if ( passphrase == null )
                    throw new TaskCancelledException( );
                if ( passphrase.isEmpty( ) )
                    return new CrosswordDocument.SolutionProperties( );
            }

            // Attribute: nonce
            attrName = AttrName.NONCE;
            attrKey = XmlUtilities.appendAttributeName( elementPath, attrName );
            attrValue = XmlUtilities.getAttribute( element, attrName );
            if ( attrValue == null )
                throw new XmlParseException( ErrorId.NO_ATTRIBUTE, attrKey );
            if ( attrValue.length( ) != 2 * Salsa20.NONCE_SIZE )
                throw new XmlParseException( ErrorId.INVALID_ATTRIBUTE, attrKey, attrValue );
            byte[] nonce = null;
            try
            {
                nonce = NumberUtilities.hexStringToBytes( attrValue );
            }
            catch ( NumberFormatException e )
            {
                throw new XmlParseException( ErrorId.INVALID_ATTRIBUTE, attrKey, attrValue );
            }

            // Decode Base64 text
            byte[] data = null;
            try
            {
                data = new Base64Encoder( ).decode( element.getTextContent( ) );
            }
            catch ( Base64Encoder.IllegalCharacterException e )
            {
                throw new XmlParseException( ErrorId.ILLEGAL_CHARACTER_IN_SOLUTION_ENCODING, elementPath );
            }
            catch ( Base64Encoder.MalformedDataException e )
            {
                throw new XmlParseException( ErrorId.MALFORMED_SOLUTION_ENCODING, elementPath );
            }

            // Decrypt solution
            Prng prng = new Prng( passphrase, nonce );
            prng.combine( data );

            // Verify data
            HmacSha256 hash = new HmacSha256( prng.getKey( ) );
            hash.update( data );
            if ( !Arrays.equals( hash.getValue( ), hashValue ) )
                throw new AppException( ErrorId.INCORRECT_PASSPHRASE );

            // Convert solution to string
            String str = null;
            try
            {
                str = new String( data, SOLUTION_ENCODING_NAME );
            }
            catch ( UnsupportedEncodingException e )
            {
                throw new UnexpectedRuntimeException( );
            }

            // Extract answers from solution string
            List<String> answers = new ArrayList<>( );
            int index = 0;
            for ( Field field : getFields( ) )
            {
                int endIndex = index + field.length;
                if ( endIndex <= str.length( ) )
                    answers.add( str.substring( index, endIndex ) );
                index = endIndex;
            }
            if ( index != str.length( ) )
                throw new XmlParseException( ErrorId.SOLUTION_LENGTH_NOT_CONSISTENT_WITH_GRID,
                                             elementPath );

            // Set solution
            try
            {
                setSolution( answers );
            }
            catch ( AppException e )
            {
                throw new XmlParseException( e.getId( ), elementPath, e.getSubstitutionStrings( ) );
            }
        }
        return new CrosswordDocument.SolutionProperties( location, passphrase, hashValue );
    }

    //------------------------------------------------------------------

    public void writeGrid( XmlWriter writer,
                           int       indent )
        throws IOException
    {
        // Write start tag, grid
        List<Attribute> attributes = new ArrayList<>( );
        attributes.add( new Attribute( AttrName.KIND, RECTANGULAR_ORTHOGONAL_STR ) );
        attributes.add( new Attribute( AttrName.SEPARATOR, getSeparator( ).key ) );
        attributes.add( new Attribute( AttrName.NUM_COLUMNS, numColumns ) );
        attributes.add( new Attribute( AttrName.NUM_ROWS, numRows ) );
        attributes.add( new Attribute( AttrName.SYMMETRY, symmetry.key ) );
        writer.writeElementStart( ElementName.GRID, attributes, indent, true, true );

        // Write grid definition
        indent += INDENT_INCREMENT;
        for ( String str : getGridDefinition( ) )
        {
            writer.writeSpaces( indent );
            writer.write( str );
            writer.writeEol( );
        }

        // Write end tag, grid
        indent -= INDENT_INCREMENT;
        writer.writeElementEnd( ElementName.GRID, indent );
    }

    //------------------------------------------------------------------

    public void writeEntries( XmlWriter writer,
                              int       indent )
        throws IOException
    {
        // Write start tag, entries
        writer.writeElementStart( ElementName.ENTRIES, indent, true );

        // Write entries
        indent += INDENT_INCREMENT;
        List<Attribute> attributes = new ArrayList<>( );
        for ( Direction direction : Direction.DEFINED_DIRECTIONS )
        {
            for ( Entry entry : getEntries( direction ) )
            {
                attributes.clear( );
                attributes.add( new Attribute( AttrName.ID, entry.fieldId ) );
                writer.writeEscapedTextElement( ElementName.ENTRY, attributes, indent, false, entry.text );
            }
        }
        indent -= INDENT_INCREMENT;

        // Write end tag, entries
        writer.writeElementEnd( ElementName.ENTRIES, indent );
    }

    //------------------------------------------------------------------

    public EncodedSolution getEncodedSolution( String passphrase )
    {
        // Convert solution string to bytes
        byte[] data = null;
        try
        {
            data = getSolutionString( null ).getBytes( SOLUTION_ENCODING_NAME );
        }
        catch ( UnsupportedEncodingException e )
        {
            throw new UnexpectedRuntimeException( );
        }

        // Initialise PRNG
        byte[] nonce = Prng.createNonce( );
        Prng prng = new Prng( passphrase, nonce );

        // Generate a hash of the solution data
        HmacSha256 hash = new HmacSha256( prng.getKey( ) );
        hash.update( data );

        // Encrypt the solution data
        prng.combine( data );

        // Return encoded and encrypted solution
        return new EncodedSolution( nonce, hash.getValue( ), data );
    }

    //------------------------------------------------------------------

    public void writeSolution( XmlWriter writer,
                               int       indent,
                               String    passphrase )
        throws IOException
    {
        // Encode and encrypt solution
        EncodedSolution encodedSolution = getEncodedSolution( passphrase );

        // Write start tag, solution
        List<Attribute> attributes = new ArrayList<>( );
        NumberUtilities.setHexLower( );
        EncryptionKind encryptionKind = passphrase.isEmpty( ) ? EncryptionKind.NONE
                                                              : EncryptionKind.SALSA20;
        attributes.add( new Attribute( AttrName.ENCRYPTION, encryptionKind.key ) );
        attributes.add( new Attribute( AttrName.NONCE,
                                       NumberUtilities.bytesToHexString( encodedSolution.nonce ) ) );
        attributes.add( new Attribute( AttrName.HASH,
                                       NumberUtilities.bytesToHexString( encodedSolution.hashValue ) ) );
        NumberUtilities.setHexUpper( );
        writer.writeElementStart( ElementName.SOLUTION, attributes, indent, true, true );

        // Write solution, encoded as Base64
        indent += INDENT_INCREMENT;
        for ( String line : new Base64Encoder( SOLUTION_LINE_LENGTH ).encodeLines( encodedSolution.data ) )
        {
            writer.writeSpaces( indent );
            writer.write( line );
            writer.writeEol( );
        }
        indent -= INDENT_INCREMENT;

        // Write end tag, solution
        writer.writeElementEnd( ElementName.SOLUTION, indent );
    }

    //------------------------------------------------------------------

    public void writeSolution( XmlWriter writer,
                               int       indent,
                               URL       location,
                               byte[]    hashValue )
        throws IOException
    {
        List<Attribute> attributes = new ArrayList<>( );
        NumberUtilities.setHexLower( );
        attributes.add( new Attribute( AttrName.LOCATION, location, true ) );
        attributes.add( new Attribute( AttrName.HASH, NumberUtilities.bytesToHexString( hashValue ) ) );
        NumberUtilities.setHexUpper( );
        writer.writeEmptyElement( ElementName.SOLUTION, attributes, indent, true );
    }

    //------------------------------------------------------------------

    public void writeHtml( XmlWriter writer,
                           int       indent,
                           int       cellSize )
        throws IOException
    {
        // Write start tag, table division
        List<Attribute> attributes = new ArrayList<>( );
        attributes.add( new Attribute( HtmlConstants.AttrName.ID, HtmlConstants.Id.GRID ) );
        writer.writeElementStart( HtmlConstants.ElementName.DIV, attributes, indent, true, false );

        // Write grid
        indent += INDENT_INCREMENT;
        for ( int row = 0; row < numRows; ++row )
        {
            writer.writeElementStart( HtmlConstants.ElementName.DIV, indent, true );

            indent += INDENT_INCREMENT;
            for ( int column = 0; column < numColumns; ++column )
                getCell( row, column ).write( writer, indent, cellSize );
            indent -= INDENT_INCREMENT;

            writer.writeElementEnd( HtmlConstants.ElementName.DIV, indent );
        }

        // Write end tag, table division
        indent -= INDENT_INCREMENT;
        writer.writeElementEnd( HtmlConstants.ElementName.DIV, indent );
    }

    //------------------------------------------------------------------

    protected void updateSymmetry( )
    {
        for ( Symmetry symmetry : TEST_SYMMETRIES )
        {
            if ( symmetry.supportsDimensions( numColumns, numRows ) && isSymmetry( symmetry ) )
            {
                this.symmetry = symmetry;
                break;
            }
        }
    }

    //------------------------------------------------------------------

    protected Field addField( int       row,
                              int       column,
                              Direction direction,
                              int       length,
                              int       fieldNumber )
    {
        Field field = new Field( row, column, direction, length, fieldNumber );
        List<Field> fields = fieldLists.get( direction );
        if ( fields == null )
        {
            fields = new ArrayList<>( );
            fieldLists.put( direction, fields );
        }
        fields.add( field );
        return field;
    }

    //------------------------------------------------------------------

    private List<Entry> getEntries( Direction direction )
    {
        List<Entry> outEntries = new ArrayList<>( );
        for ( Field field : fieldLists.get( direction ) )
        {
            char[] chars = null;
            int numUndefined = 0;
            switch ( direction )
            {
                case NONE:
                    // do nothing
                    break;

                case ACROSS:
                    chars = new char[field.length];
                    for ( int i = 0; i < chars.length; ++i )
                    {
                        char ch = entries.values[field.row][field.column + i];
                        if ( ch == Entries.UNDEFINED_VALUE )
                            ++numUndefined;
                        chars[i] = ch;
                    }
                    break;

                case DOWN:
                    chars = new char[field.length];
                    for ( int i = 0; i < chars.length; ++i )
                    {
                        char ch = entries.values[field.row + i][field.column];
                        if ( ch == Entries.UNDEFINED_VALUE )
                            ++numUndefined;
                        chars[i] = ch;
                    }
                    break;
            }
            if ( (chars != null) && (numUndefined < chars.length) )
                outEntries.add( new Entry( field.getId( ), new String( chars ) ) );
        }
        return outEntries;
    }

    //------------------------------------------------------------------

    private void setEntry( Field  field,
                           String entry )
        throws AppException
    {
        // Compare length of entry with length of field
        String idStr = field.getId( ).toString( );
        if ( entry.length( ) != field.length )
            throw new AppException( ErrorId.INCORRECT_ENTRY_LENGTH, idStr );

        // Set entry
        for ( int i = 0; i < field.length; ++i )
        {
            char ch0 = entry.charAt( i );
            if ( ch0 != Entries.UNDEFINED_VALUE )
            {
                if ( !Character.isLetterOrDigit( ch0 ) )
                    throw new AppException( ErrorId.ILLEGAL_CHARACTER_IN_ENTRY, idStr,
                                            Character.toString( ch0 ) );

                switch ( field.direction )
                {
                    case NONE:
                        // do nothing
                        break;

                    case ACROSS:
                    {
                        char ch1 = entries.values[field.row][field.column + i];
                        if ( (ch1 != Entries.UNDEFINED_VALUE) && (ch1 != ch0) )
                            throw new AppException( ErrorId.CONFLICTING_ENTRY,
                                                    new String[]{ idStr, Integer.toString( i ) } );
                        entries.setValue( field.row, field.column + i, ch0 );
                        break;
                    }

                    case DOWN:
                    {
                        char ch1 = entries.values[field.row + i][field.column];
                        if ( (ch1 != Entries.UNDEFINED_VALUE) && (ch1 != ch0) )
                            throw new AppException( ErrorId.CONFLICTING_ENTRY,
                                                    new String[]{ idStr, Integer.toString( i ) } );
                        entries.setValue( field.row + i, field.column, ch0 );
                        break;
                    }
                }
            }
        }
    }

    //------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

    protected   int                         numRows;
    protected   int                         numColumns;
    protected   Symmetry                    symmetry;
    protected   Map<Direction, List<Field>> fieldLists;
    protected   Entries                     entries;
    protected   Entries                     solution;
    protected   boolean[][]                 incorrectEntries;

}

//----------------------------------------------------------------------
