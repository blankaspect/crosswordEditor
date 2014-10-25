/*====================================================================*\

HtmlConstants.java

HTML constants interface.

\*====================================================================*/


// HTML CONSTANTS INTERFACE


interface HtmlConstants
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

    interface ElementName
    {
        String  BODY    = "body";
        String  DIV     = "div";
        String  H4      = "h4";
        String  HEAD    = "head";
        String  HTML    = "html";
        String  IMG     = "img";
        String  LINK    = "link";
        String  META    = "meta";
        String  P       = "p";
        String  SPAN    = "span";
        String  STYLE   = "style";
        String  TITLE   = "title";
    }

    interface AttrName
    {
        String  ALT         = "alt";
        String  CLASS       = "class";
        String  CONTENT     = "content";
        String  HREF        = "href";
        String  HTTP_EQUIV  = "http-equiv";
        String  ID          = "id";
        String  MEDIA       = "media";
        String  REL         = "rel";
        String  SRC         = "src";
        String  TYPE        = "type";
        String  XML_LANG    = "xml:lang";
        String  XMLNS       = "xmlns";
    }

    interface Class
    {
        String  BLOCK               = "block";
        String  MULTI_FIELD_CLUE    = "multiFieldClue";
        String  SECONDARY_IDS       = "secondaryIds";
        String  STRIKE              = "strike";
        String  UNDERLINE           = "underline";
    }

    interface Id
    {
        String  CLUES       = "clues";
        String  EPILOGUE    = "epilogue";
        String  GRID        = "grid";
        String  PROLOGUE    = "prologue";
        String  TITLE       = "title";
    }

    interface CssSelector
    {
        String  CHILD       = " > ";
        String  CLASS       = ".";
        String  DESCENDANT  = " ";
        String  FIRST_CHILD = ":first-child";
        String  ID          = "#";
    }

}

//----------------------------------------------------------------------
