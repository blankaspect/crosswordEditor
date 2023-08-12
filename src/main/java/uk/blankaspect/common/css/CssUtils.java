/*====================================================================*\

CssUtils.java

Class: CSS utility methods.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.common.css;

import uk.blankaspect.common.net.PercentCodec;

//----------------------------------------------------------------------


// CLASS: CSS UTILITY METHODS


public class CssUtils
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	public static final		String	SEPARATOR	= createSeparatorComment('-', 72);

	private static final	String	DATA_URI_PREFIX	= "data:text/css,";

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	private CssUtils()
	{
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	public static String styleSheetToDataUri(
		String	text)
	{
		return DATA_URI_PREFIX + PercentCodec.encode(text, true);
	}

	//------------------------------------------------------------------

	public static String createSeparatorComment(
		char	ch,
		int		length)
	{
		// Validate arguments
		int repeatLength = length - CssConstants.COMMENT_PREFIX.length() - CssConstants.COMMENT_SUFFIX.length();
		if (repeatLength < 0)
			throw new IllegalArgumentException("Length out of bounds: " + length);

		// Create comment
		StringBuilder buffer = new StringBuilder(length + 1);
		buffer.append(CssConstants.COMMENT_PREFIX);
		buffer.append(Character.toString(ch).repeat(repeatLength));
		buffer.append(CssConstants.COMMENT_SUFFIX);
		buffer.append('\n');

		// Return comment
		return buffer.toString();
	}

	//------------------------------------------------------------------

	public static String createHeaderComment(
		int									length,
		Iterable<? extends CharSequence>	seqs)
	{
		// Validate arguments
		int repeatLength = length - CssConstants.COMMENT_PREFIX.length() - CssConstants.COMMENT_SUFFIX.length();
		if (repeatLength < 0)
			throw new IllegalArgumentException("Length out of bounds: " + length);

		// Create string of asterisks
		String asterisks = "*".repeat(repeatLength);

		// Create comment
		StringBuilder buffer = new StringBuilder(1024);
		buffer.append(CssConstants.COMMENT_PREFIX);
		buffer.append(asterisks);
		buffer.append("*\\\n");
		for (CharSequence seq : seqs)
		{
			buffer.append(seq);
			buffer.append('\n');
		}
		buffer.append("\\*");
		buffer.append(asterisks);
		buffer.append(CssConstants.COMMENT_SUFFIX);
		buffer.append('\n');

		// Return comment
		return buffer.toString();
	}

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------
