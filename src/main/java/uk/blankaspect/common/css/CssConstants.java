/*====================================================================*\

CssConstants.java

Interface: CSS constants.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.common.css;

//----------------------------------------------------------------------


// INTERFACE: CSS CONSTANTS


public interface CssConstants
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	int		INDENT_INCREMENT	= 2;

	char	BLOCK_START_CHAR	= '{';
	String	BLOCK_START_STR		= Character.toString(BLOCK_START_CHAR);
	char	BLOCK_END_CHAR		= '}';
	String	BLOCK_END_STR		= Character.toString(BLOCK_END_CHAR);

	char	PROPERTY_SEPARATOR_CHAR				= ';';
	char	PROPERTY_NAME_VALUE_SEPARATOR_CHAR	= ':';

	String	COMMENT_PREFIX	= "/*";
	String	COMMENT_SUFFIX	= "*/";

}

//----------------------------------------------------------------------
