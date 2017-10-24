/*====================================================================*\

CssConstants.java

CSS constants interface.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.crosswordeditor;

//----------------------------------------------------------------------


// CSS CONSTANTS INTERFACE


interface CssConstants
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	interface Property
	{
		String	BACKGROUND_COLOUR	= "background-color";
		String	BORDER				= "border";
		String	BORDER_COLLAPSE		= "border-collapse";
		String	COLOUR				= "color";
		String	DISPLAY				= "display";
		String	EMPTY_CELLS			= "empty-cells";
		String	FONT_FAMILY			= "font-family";
		String	FONT_SIZE			= "font-size";
		String	FONT_WEIGHT			= "font-weight";
		String	HEIGHT				= "height";
		String	LEFT				= "left";
		String	LINE_HEIGHT			= "line-height";
		String	MARGIN				= "margin";
		String	MARGIN_BOTTOM		= "margin-bottom";
		String	MARGIN_TOP			= "margin-top";
		String	PADDING				= "padding";
		String	PADDING_LEFT		= "padding-left";
		String	PADDING_RIGHT		= "padding-right";
		String	POSITION			= "position";
		String	TEXT_ALIGN			= "text-align";
		String	TEXT_DECORATION		= "text-decoration";
		String	TEXT_INDENT			= "text-indent";
		String	TOP					= "top";
		String	VERTICAL_ALIGN		= "vertical-align";
		String	VISIBILITY			= "visibility";
		String	WIDTH				= "width";
		String	Z_INDEX				= "z-index";
	}

	interface Selector
	{
		String	CHILD		= " > ";
		String	CLASS		= ".";
		String	DESCENDANT	= " ";
		String	FIRST_CHILD	= ":first-child";
		String	ID			= "#";
	}

}

//----------------------------------------------------------------------
