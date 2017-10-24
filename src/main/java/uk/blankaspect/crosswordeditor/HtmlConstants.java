/*====================================================================*\

HtmlConstants.java

HTML constants interface.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.crosswordeditor;

//----------------------------------------------------------------------


// HTML CONSTANTS INTERFACE


interface HtmlConstants
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	interface ElementName
	{
		String	B		= "b";
		String	BODY	= "body";
		String	DIV		= "div";
		String	H4		= "h4";
		String	HEAD	= "head";
		String	HTML	= "html";
		String	I		= "i";
		String	IMG		= "img";
		String	LINK	= "link";
		String	META	= "meta";
		String	P		= "p";
		String	SPAN	= "span";
		String	STYLE	= "style";
		String	SUB		= "sub";
		String	SUP		= "sup";
		String	TITLE	= "title";
	}

	interface AttrName
	{
		String	ALT			= "alt";
		String	CLASS		= "class";
		String	CONTENT		= "content";
		String	HREF		= "href";
		String	HTTP_EQUIV	= "http-equiv";
		String	ID			= "id";
		String	MEDIA		= "media";
		String	REL			= "rel";
		String	SRC			= "src";
		String	TYPE		= "type";
		String	XML_LANG	= "xml:lang";
		String	XMLNS		= "xmlns";
	}

	interface Class
	{
		String	BARS				= "bars";
		String	BLOCK				= "block";
		String	ENTRY				= "entry";
		String	FIELD_NUMBER		= "fieldNumber";
		String	MULTI_FIELD_CLUE	= "multiFieldClue";
		String	SECONDARY_IDS		= "secondaryIds";
		String	STRIKE				= "strike";
		String	UNDERLINE			= "underline";
	}

	interface Id
	{
		String	CLUES		= "clues";
		String	EPILOGUE	= "epilogue";
		String	GRID		= "grid";
		String	PROLOGUE	= "prologue";
		String	TITLE		= "title";
	}

}

//----------------------------------------------------------------------
