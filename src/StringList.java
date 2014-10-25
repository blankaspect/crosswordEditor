/*====================================================================*\

StringList.java

String list class.

\*====================================================================*/


// IMPORTS


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

//----------------------------------------------------------------------


// STRING LIST CLASS


class StringList
    extends ArrayList<String>
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

    private static final    char    SEPARATOR_CHAR  = ',';
    private static final    char    ESCAPE_CHAR     = '\\';

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

    public StringList( )
    {
    }

    //------------------------------------------------------------------

    public StringList( CharSequence seq )
    {
        parse( seq );
    }

    //------------------------------------------------------------------

    public StringList( String... strs )
    {
        Collections.addAll( this, strs );
    }

    //------------------------------------------------------------------

    public StringList( List<String> strs )
    {
        addAll( strs );
    }

    //------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : overriding methods
////////////////////////////////////////////////////////////////////////

    @Override
    public String toString( )
    {
        return toString( false );
    }

    //------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

    public String toString( boolean spaced )
    {
        StringBuilder buffer = new StringBuilder( 256 );
        for ( int i = 0; i < size( ); ++i )
        {
            // Append separator
            if ( i > 0 )
            {
                buffer.append( SEPARATOR_CHAR );
                if ( spaced )
                    buffer.append( ' ' );
            }

            // Append string
            String str = get( i );
            for ( int j = 0; j < str.length( ); ++j )
            {
                char ch = str.charAt( j );
                if ( (ch == ' ') || (ch == SEPARATOR_CHAR) || (ch == ESCAPE_CHAR) )
                    buffer.append( ESCAPE_CHAR );
                buffer.append( ch );
            }
        }
        return buffer.toString( );
    }

    //------------------------------------------------------------------

    public String toQuotedString( )
    {
        StringBuilder buffer = new StringBuilder( 256 );
        for ( int i = 0; i < size( ); ++i )
        {
            // Append separator
            if ( i > 0 )
            {
                buffer.append( SEPARATOR_CHAR );
                buffer.append( ' ' );
            }

            // Append string
            String str = get( i );
            if ( (str.indexOf( ' ' ) >= 0) || (str.indexOf( SEPARATOR_CHAR ) >= 0) )
            {
                buffer.append( '"' );
                buffer.append( str );
                buffer.append( '"' );
            }
            else
                buffer.append( str );
        }
        return buffer.toString( );
    }

    //------------------------------------------------------------------

    public void parse( CharSequence seq )
    {
        clear( );
        if ( seq.length( ) > 0 )
        {
            StringBuilder buffer = new StringBuilder( 128 );
            for ( int i = 0; i < seq.length( ); ++i )
            {
                char ch = seq.charAt( i );
                if ( ch == ESCAPE_CHAR )
                {
                    if ( i < seq.length( ) - 1 )
                        buffer.append( seq.charAt( ++i ) );
                }
                else if ( ch == SEPARATOR_CHAR )
                {
                    add( buffer.toString( ) );
                    buffer.setLength( 0 );
                }
                else if ( ch != ' ' )
                    buffer.append( ch );
            }
            add( buffer.toString( ) );
        }
    }

    //------------------------------------------------------------------

}

//----------------------------------------------------------------------
