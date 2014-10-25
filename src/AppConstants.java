/*====================================================================*\

AppConstants.java

Application constants interface.

\*====================================================================*/


// IMPORTS


import java.awt.Insets;

import java.text.DecimalFormat;

//----------------------------------------------------------------------


// APPLICATION CONSTANTS INTERFACE


interface AppConstants
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

    // Component constants
    Insets  COMPONENT_INSETS    = new Insets( 2, 3, 2, 3 );

    // Decimal formats
    DecimalFormat   FORMAT_1_1  = new DecimalFormat( "0.#" );
    DecimalFormat   FORMAT_1_1F = new DecimalFormat( "0.0" );
    DecimalFormat   FORMAT_1_3  = new DecimalFormat( "0.0##" );

    // Strings
    String  ELLIPSIS_STR        = "...";
    String  FILE_CHANGED_SUFFIX = " *";
    String  OK_STR              = "OK";
    String  CANCEL_STR          = "Cancel";
    String  CLOSE_STR           = "Close";
    String  CONTINUE_STR        = "Continue";
    String  REPLACE_STR         = "Replace";
    String  CLEAR_STR           = "Clear";
    String  ALREADY_EXISTS_STR  = "\nThe file already exists.\nDo you want to replace it?";

    // Temporary-file prefix
    String  TEMP_FILE_PREFIX    = "_$_";

    // Namespace prefix
    String  NS_PREFIX_BASE  = "xw";
    String  NS_PREFIX       = NS_PREFIX_BASE + ":";

    // Filename suffixes
    String  HTML_FILE_SUFFIX    = ".html";
    String  XML_FILE_SUFFIX     = ".xml";

    // File-filter descriptions
    String  CROSSWORD_FILES_STR = "Crossword files";
    String  HTML_FILES_STR      = "HTML files";
    String  XML_FILES_STR       = "XML files";

}

//----------------------------------------------------------------------
