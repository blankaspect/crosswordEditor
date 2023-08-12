/*====================================================================*\

PercentCodec.java

Class: percent encoder and decoder.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.common.net;

//----------------------------------------------------------------------


// IMPORTS


import java.nio.ByteBuffer;

import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;

//----------------------------------------------------------------------


// CLASS: PERCENT ENCODER AND DECODER


/**
 * This class provides methods for encoding and decoding a string with <i>percent-encoding</i>, as specified by
 * <a href="https://www.rfc-editor.org/rfc/rfc3986.html#section-2.1">IETF RFC 3986</a>.
 */

public class PercentCodec
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	private static final	int		MIN_HIGH_SURROGATE	= 0xD800;

	private static final	int		MIN_LOW_SURROGATE	= 0xDC00;
	private static final	int		MAX_LOW_SURROGATE	= 0xDFFF;

	private static final	int		PLANE1_MIN_VALUE	= 0x10000;

	private static final	int		MAX_VALUE	= 0x10FFFF;

	private static final	int		REPLACEMENT_CHAR	= 0xFFFD;

	private static final	int		MIN_VALUE_2SEQ	= 1 << 7;
	private static final	int		MIN_VALUE_3SEQ	= 1 << 11;
	private static final	int		MIN_VALUE_4SEQ	= 1 << 16;

	private static final	int		ESCAPE_SEQUENCE_LENGTH	= 3;

	private static final	char	ESCAPE_PREFIX	= '%';

	private static final	String	ESCAPED_SPACE	= ESCAPE_PREFIX + "20";

	private static final	String	EXTRA_UNRESERVED_CHARS	= "-._~";

	private static final	String	IN_HEX_DIGITS	= "0123456789ABCDEF";
	private static final	char[]	OUT_HEX_DIGITS	= IN_HEX_DIGITS.toCharArray();

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	private PercentCodec()
	{
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	public static String encode(
		CharSequence	text,
		boolean			reduceWhitespace)
	{
		// Allocate buffer for encoded text
		int length = text.length();
		StringBuilder buffer = new StringBuilder(length * 2);

		// Encode text
		boolean whitespace = false;
		int index = 0;
		while (index < length)
		{
			// Get next character from sequence
			char ch = text.charAt(index++);

			// Case: unreserved character
			if (((ch >= '0') && (ch <= '9')) || ((ch >= 'A') && (ch <= 'Z')) || ((ch >= 'a') && (ch <= 'z'))
					|| (EXTRA_UNRESERVED_CHARS.indexOf(ch) >= 0))
			{
				// Append any pending whitespace
				if (whitespace)
				{
					buffer.append(ESCAPED_SPACE);
					whitespace = false;
				}

				// Append character
				buffer.append(ch);
			}

			// Case: whitespace
			else if (reduceWhitespace && Character.isWhitespace(ch))
				whitespace = true;

			// Case: not an allowed character
			else
			{
				// Append any pending whitespace
				if (whitespace)
				{
					buffer.append(ESCAPED_SPACE);
					whitespace = false;
				}

				// If character is high surrogate, get low surrogate from sequence, combine surrogates into code point,
				// convert code point to UTF-8 and append escape sequence ...
				if ((ch >= MIN_HIGH_SURROGATE) && (ch < MIN_LOW_SURROGATE))
				{
					// Get low surrogate from sequence and combine surrogates into code point
					int code = ch;
					if (index < length)
					{
						int code0 = text.charAt(index);
						if ((code0 >= MIN_LOW_SURROGATE) && (code0 <= MAX_LOW_SURROGATE))
						{
							++index;
							code = PLANE1_MIN_VALUE + ((code - MIN_HIGH_SURROGATE) << 10 | (code0 - MIN_LOW_SURROGATE));
						}
						else
							code = REPLACEMENT_CHAR;
					}
					else
						code = REPLACEMENT_CHAR;

					// Convert code point to UTF-8 and append escape sequence
					for (byte b : codePointToUtf8(code))
					{
						buffer.append(ESCAPE_PREFIX);
						buffer.append(OUT_HEX_DIGITS[(b >> 4) & 0x0F]);
						buffer.append(OUT_HEX_DIGITS[b & 0x0F]);
					}
				}

				// ... otherwise, convert character to UTF-8 and append escape sequence
				else
				{
					for (byte b : charToUtf8(ch))
					{
						buffer.append(ESCAPE_PREFIX);
						buffer.append(OUT_HEX_DIGITS[(b >> 4) & 0x0F]);
						buffer.append(OUT_HEX_DIGITS[b & 0x0F]);
					}
				}
			}
		}

		// Return encoded text
		return buffer.toString();
	}

	//------------------------------------------------------------------

	public static String decode(
		CharSequence	seq)
	{
		// Allocate buffer for decoded text
		int length = seq.length();
		ByteBuffer buffer = ByteBuffer.allocate(length);

		int index = 0;
		while (index < length)
		{
			// Get next character from sequence
			char ch = seq.charAt(index);

			if (ch >= MIN_VALUE_2SEQ)
				throw new IllegalArgumentException("Illegal character at index " + index);

			if (ch == ESCAPE_PREFIX)
			{
				if (length - index < ESCAPE_SEQUENCE_LENGTH)
					throw new IllegalArgumentException("Malformed escape sequence at index " + index);

				int value = IN_HEX_DIGITS.indexOf(Character.toUpperCase(seq.charAt(index + 1)));
				if (value >= 0)
				{
					int i = IN_HEX_DIGITS.indexOf(Character.toUpperCase(seq.charAt(index + 2)));
					value = (i < 0) ? -1 : (value << 4) + i;
				}
				if (value < 0)
					throw new IllegalArgumentException("Illegal escape sequence at index " + index);

				buffer.put((byte)value);
				index += ESCAPE_SEQUENCE_LENGTH;
			}
			else
			{
				buffer.put((byte)ch);
				++index;
			}
		}

		try
		{
			buffer.flip();
			return StandardCharsets.UTF_8.newDecoder()
											.onMalformedInput(CodingErrorAction.REPORT)
											.onUnmappableCharacter(CodingErrorAction.REPORT)
											.decode(buffer)
											.toString();
		}
		catch (CharacterCodingException e)
		{
			throw new IllegalArgumentException("Input contains malformed or unmappable UTF-8 sequence");
		}
	}

	//------------------------------------------------------------------

	public static byte[] charToUtf8(
		char	ch)
	{
		// Test for unpaired surrogate
		if ((ch >= MIN_HIGH_SURROGATE) && (ch <= MAX_LOW_SURROGATE))
			throw new IllegalArgumentException("Unpaired surrogate");

		// Case: 1-byte sequence
		if (ch < MIN_VALUE_2SEQ)
			return new byte[] { (byte)ch };

		// Case: 2-byte sequence
		if (ch < MIN_VALUE_3SEQ)
		{
			return new byte[]
			{
				(byte)(0xC0 | (ch >> 6)),
				(byte)(0x80 | (ch & 0x3F))
			};
		}

		// Case: 3-byte sequence
		return new byte[]
		{
			(byte)(0xE0 | (ch >> 12)),
			(byte)(0x80 | (ch >> 6 & 0x3F)),
			(byte)(0x80 | (ch      & 0x3F))
		};
	}

	//------------------------------------------------------------------

	public static byte[] codePointToUtf8(
		int	ch)
	{
		// Test for unpaired surrogate
		if ((ch >= MIN_HIGH_SURROGATE) && (ch <= MAX_LOW_SURROGATE))
			throw new IllegalArgumentException("Unpaired surrogate");

		// Replace code that is out of bounds
		if ((ch < 0) || (ch > MAX_VALUE))
			ch = REPLACEMENT_CHAR;

		// Case: 1-byte sequence
		if (ch < MIN_VALUE_2SEQ)
			return new byte[] { (byte)ch };

		// Case: 2-byte sequence
		if (ch < MIN_VALUE_3SEQ)
		{
			return new byte[]
			{
				(byte)(0xC0 | (ch >> 6)),
				(byte)(0x80 | (ch & 0x3F))
			};
		}

		// Case: 3-byte sequence
		if (ch < MIN_VALUE_4SEQ)
		{
			return new byte[]
			{
				(byte)(0xE0 | (ch >> 12)),
				(byte)(0x80 | (ch >> 6 & 0x3F)),
				(byte)(0x80 | (ch      & 0x3F))
			};
		}

		// Case: 4-byte sequence
		return new byte[]
		{
			(byte)(0xF0 | (ch >> 18)),
			(byte)(0x80 | (ch >> 12 & 0x3F)),
			(byte)(0x80 | (ch >> 6  & 0x3F)),
			(byte)(0x80 | (ch       & 0x3F))
		};
	}

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------
