/*====================================================================*\

AbstractBase64Encoder.java.java

Abstract Base64 encoder/decoder class.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.common.base64;

//----------------------------------------------------------------------


// IMPORTS


import java.io.ByteArrayOutputStream;

import java.util.ArrayList;
import java.util.List;

import uk.blankaspect.common.misc.SystemUtils;

import uk.blankaspect.common.string.StringUtils;

//----------------------------------------------------------------------


// ABSTRACT BASE64 ENCODER/DECODER CLASS


public abstract class AbstractBase64Encoder
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	private static final	String	ALPHANUMERIC_CHARS	=
													"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

	private static final	int	NUM_SUPPLEMENTARY_CHARS	= 64 - ALPHANUMERIC_CHARS.length();

	private static final	int	MIN_LINE_LENGTH	= 0;
	private static final	int	MAX_LINE_LENGTH	= Integer.MAX_VALUE;

////////////////////////////////////////////////////////////////////////
//  Member classes : non-inner classes
////////////////////////////////////////////////////////////////////////


	// ILLEGAL CHARACTER EXCEPTION CLASS


	public static class IllegalCharacterException
		extends Exception
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		public IllegalCharacterException()
		{
		}

		//--------------------------------------------------------------

	}

	//==================================================================


	// MALFORMED DATA EXCEPTION CLASS


	public static class MalformedDataException
		extends Exception
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		public MalformedDataException()
		{
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

	protected AbstractBase64Encoder(CharSequence supplementaryChars,
									char         padChar,
									int          lineLength,
									String       lineSeparator)
	{
		setCodeCharacters(supplementaryChars, padChar);
		setLineLength(lineLength);
		setLineSeparator(lineSeparator);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	private static byte[] intToBytes(int data)
	{
		byte[] bytes = new byte[3];
		for (int j = 2; j >= 0; j--)
		{
			bytes[j] = (byte)(data & 0xFF);
			data >>>= 8;
		}
		return bytes;
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	public int getLineLength()
	{
		return lineLength;
	}

	//------------------------------------------------------------------

	public String getLineSeparator()
	{
		return lineSeparator;
	}

	//------------------------------------------------------------------

	/**
	 * @throws IllegalArgumentException
	 */

	public void setLineLength(int lineLength)
	{
		if ((lineLength < MIN_LINE_LENGTH) || (lineLength > MAX_LINE_LENGTH))
			throw new IllegalArgumentException();
		this.lineLength = lineLength;
	}

	//------------------------------------------------------------------

	public void setLineSeparator(String lineSeparator)
	{
		this.lineSeparator = (lineSeparator == null) ? SystemUtils.lineSeparator() : lineSeparator;
	}

	//------------------------------------------------------------------

	/**
	 * @throws IllegalArgumentException
	 */

	public String encode(byte[] data)
	{
		return encode(data, 0, data.length);
	}

	//------------------------------------------------------------------

	/**
	 * @throws IllegalArgumentException
	 * @throws IndexOutOfBoundsException
	 */

	public String encode(byte[] data,
						 int    offset,
						 int    length)
	{
		List<String> lines = encodeLines(data, offset, length);
		return ((lineLength == 0) ? lines.isEmpty()
											? ""
											: lines.get(0)
								  : StringUtils.join(lineSeparator, true, lines));
	}

	//------------------------------------------------------------------

	/**
	 * @throws IllegalArgumentException
	 */

	public List<String> encodeLines(byte[] data)
	{
		return encodeLines(data, 0, data.length);
	}

	//------------------------------------------------------------------

	/**
	 * @throws IllegalArgumentException
	 * @throws IndexOutOfBoundsException
	 */

	public List<String> encodeLines(byte[] data,
									int    offset,
									int    length)
	{
		if (data == null)
			throw new IllegalArgumentException();
		if ((offset < 0) || (offset > data.length))
			throw new IndexOutOfBoundsException();
		if ((length < 0) || (length > data.length - offset))
			throw new IllegalArgumentException();

		List<String> lines = new ArrayList<>();
		StringBuilder outBuffer = new StringBuilder((lineLength == 0) ? (length + 2) / 3 * 4 : lineLength);
		int inBuffer = 0;
		int inBufferLength = 0;
		int numChars = 0;
		int endOffset = offset + length;
		while (offset < endOffset)
		{
			inBuffer <<= 8;
			inBuffer |= data[offset++] & 0xFF;
			if (++inBufferLength >= 3)
			{
				numChars = appendChars(outBuffer, intToChars(inBuffer), 4, numChars, lines);
				inBufferLength = 0;
			}
		}
		if (inBufferLength > 0)
		{
			inBuffer <<= (3 - inBufferLength) * 8;
			char[] chars = intToChars(inBuffer);
			int blockLength = inBufferLength + 1;
			if (padChar != '\0')
			{
				while (blockLength < chars.length)
					chars[blockLength++] = padChar;
			}
			appendChars(outBuffer, chars, blockLength, numChars, lines);
		}
		if (outBuffer.length() > 0)
			lines.add(outBuffer.toString());
		return lines;
	}

	//------------------------------------------------------------------

	/**
	 * @throws IllegalArgumentException
	 * @throws IllegalCharacterException
	 * @throws MalformedDataException
	 */

	public byte[] decode(CharSequence charSeq)
		throws IllegalCharacterException, MalformedDataException
	{
		return decode(charSeq, 0, charSeq.length());
	}

	//------------------------------------------------------------------

	/**
	 * @throws IllegalArgumentException
	 * @throws IndexOutOfBoundsException
	 * @throws IllegalCharacterException
	 * @throws MalformedDataException
	 */

	public byte[] decode(CharSequence charSeq,
						 int          offset,
						 int          length)
		throws IllegalCharacterException, MalformedDataException
	{
		if (charSeq == null)
			throw new IllegalArgumentException();
		if ((offset < 0) || (offset > charSeq.length()))
			throw new IndexOutOfBoundsException();
		if ((length < 0) || (length > charSeq.length() - offset))
			throw new IllegalArgumentException();

		boolean atEnd = false;
		int numInChars = 0;
		int inBuffer = 0;
		int inBufferLength = 0;
		int endOffset = offset + length;
		ByteArrayOutputStream outStream = new ByteArrayOutputStream(length);
		while (offset < endOffset)
		{
			char ch = charSeq.charAt(offset++);
			int value = codeChars.indexOf(ch);
			if (value < 0)
			{
				if ((padChar != '\0') && (ch == padChar))
				{
					atEnd = true;
					++numInChars;
				}
				else if (!Character.isWhitespace(ch))
					throw new IllegalCharacterException();
			}
			else
			{
				if (atEnd)
					throw new MalformedDataException();
				++numInChars;
				inBuffer <<= 6;
				inBuffer |= value;
				if (++inBufferLength >= 4)
				{
					outStream.write(intToBytes(inBuffer), 0, 3);
					inBufferLength = 0;
				}
			}
		}
		if ((padChar != '\0') && (numInChars % 4 != 0))
			throw new MalformedDataException();
		if (inBufferLength > 0)
		{
			inBuffer <<= (4 - inBufferLength) * 6;
			outStream.write(intToBytes(inBuffer), 0, inBufferLength - 1);
		}
		return outStream.toByteArray();
	}

	//------------------------------------------------------------------

	/**
	 * @throws IllegalArgumentException
	 */

	protected void setCodeCharacters(CharSequence supplementaryChars,
									 char         padChar)
	{
		if ((supplementaryChars == null) || (supplementaryChars.length() != NUM_SUPPLEMENTARY_CHARS))
			throw new IllegalArgumentException();

		codeChars = ALPHANUMERIC_CHARS + supplementaryChars;
		this.padChar = padChar;
	}

	//------------------------------------------------------------------

	private char[] intToChars(int data)
	{
		char[] chars = new char[4];
		for (int j = 3; j >= 0; j--)
		{
			chars[j] = codeChars.charAt(data & 0x3F);
			data >>>= 6;
		}
		return chars;
	}

	//------------------------------------------------------------------

	private int appendChars(StringBuilder buffer,
							char[]        chars,
							int           length,
							int           numChars,
							List<String>  lines)
	{
		if (lineLength == 0)
		{
			buffer.append(chars, 0, length);
			numChars += chars.length;
		}
		else
		{
			for (int i = 0; i < length; i++)
			{
				if ((numChars > 0) && (numChars % lineLength == 0))
				{
					lines.add(buffer.toString());
					buffer.setLength(0);
				}
				buffer.append(chars[i]);
				++numChars;
			}
		}
		return numChars;
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private	String	codeChars;
	private	char	padChar;
	private	int		lineLength;
	private	String	lineSeparator;

}

//----------------------------------------------------------------------
