/*====================================================================*\

Base64Encoder.java

Base64 encoder/decoder class.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.common.base64;

//----------------------------------------------------------------------


// BASE64 ENCODER/DECODER CLASS


public class Base64Encoder
	extends AbstractBase64Encoder
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	private static final	String	SUPPLEMENTARY_CHARS	= "+/";
	private static final	char	PAD_CHAR			= '=';

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	public Base64Encoder()
	{
		this(0, null);
	}

	//------------------------------------------------------------------

	/**
	 * @throws IllegalArgumentException
	 */

	public Base64Encoder(int lineLength)
	{
		this(lineLength, null);
	}

	//------------------------------------------------------------------

	/**
	 * @throws IllegalArgumentException
	 */

	public Base64Encoder(int    lineLength,
						 String lineSeparator)
	{
		super(SUPPLEMENTARY_CHARS, PAD_CHAR, lineLength, lineSeparator);
	}

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------
