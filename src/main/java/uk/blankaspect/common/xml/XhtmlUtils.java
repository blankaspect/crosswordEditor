/*====================================================================*\

XhtmlUtils.java

XHTML utilities class.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.common.xml;

//----------------------------------------------------------------------


// IMPORTS


import java.util.Arrays;

import uk.blankaspect.common.exception.UnexpectedRuntimeException;

import uk.blankaspect.common.number.NumberUtils;

//----------------------------------------------------------------------


// XHTML UTILITIES CLASS


public class XhtmlUtils
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	// XHTML character entities : xhtml-lat1
	private static final	StringMapEntry[]	CHAR_ENTITIES_LATIN1	=
	{
		new StringMapEntry(0x00A0, "nbsp"),
		new StringMapEntry(0x00A1, "iexcl"),
		new StringMapEntry(0x00A2, "cent"),
		new StringMapEntry(0x00A3, "pound"),
		new StringMapEntry(0x00A4, "curren"),
		new StringMapEntry(0x00A5, "yen"),
		new StringMapEntry(0x00A6, "brvbar"),
		new StringMapEntry(0x00A7, "sect"),
		new StringMapEntry(0x00A8, "uml"),
		new StringMapEntry(0x00A9, "copy"),
		new StringMapEntry(0x00AA, "ordf"),
		new StringMapEntry(0x00AB, "laquo"),
		new StringMapEntry(0x00AC, "not"),
		new StringMapEntry(0x00AD, "shy"),
		new StringMapEntry(0x00AE, "reg"),
		new StringMapEntry(0x00AF, "macr"),
		new StringMapEntry(0x00B0, "deg"),
		new StringMapEntry(0x00B1, "plusmn"),
		new StringMapEntry(0x00B2, "sup2"),
		new StringMapEntry(0x00B3, "sup3"),
		new StringMapEntry(0x00B4, "acute"),
		new StringMapEntry(0x00B5, "micro"),
		new StringMapEntry(0x00B6, "para"),
		new StringMapEntry(0x00B7, "middot"),
		new StringMapEntry(0x00B8, "cedil"),
		new StringMapEntry(0x00B9, "sup1"),
		new StringMapEntry(0x00BA, "ordm"),
		new StringMapEntry(0x00BB, "raquo"),
		new StringMapEntry(0x00BC, "frac14"),
		new StringMapEntry(0x00BD, "frac12"),
		new StringMapEntry(0x00BE, "frac34"),
		new StringMapEntry(0x00BF, "iquest"),
		new StringMapEntry(0x00C0, "Agrave"),
		new StringMapEntry(0x00C1, "Aacute"),
		new StringMapEntry(0x00C2, "Acirc"),
		new StringMapEntry(0x00C3, "Atilde"),
		new StringMapEntry(0x00C4, "Auml"),
		new StringMapEntry(0x00C5, "Aring"),
		new StringMapEntry(0x00C6, "AElig"),
		new StringMapEntry(0x00C7, "Ccedil"),
		new StringMapEntry(0x00C8, "Egrave"),
		new StringMapEntry(0x00C9, "Eacute"),
		new StringMapEntry(0x00CA, "Ecirc"),
		new StringMapEntry(0x00CB, "Euml"),
		new StringMapEntry(0x00CC, "Igrave"),
		new StringMapEntry(0x00CD, "Iacute"),
		new StringMapEntry(0x00CE, "Icirc"),
		new StringMapEntry(0x00CF, "Iuml"),
		new StringMapEntry(0x00D0, "ETH"),
		new StringMapEntry(0x00D1, "Ntilde"),
		new StringMapEntry(0x00D2, "Ograve"),
		new StringMapEntry(0x00D3, "Oacute"),
		new StringMapEntry(0x00D4, "Ocirc"),
		new StringMapEntry(0x00D5, "Otilde"),
		new StringMapEntry(0x00D6, "Ouml"),
		new StringMapEntry(0x00D7, "times"),
		new StringMapEntry(0x00D8, "Oslash"),
		new StringMapEntry(0x00D9, "Ugrave"),
		new StringMapEntry(0x00DA, "Uacute"),
		new StringMapEntry(0x00DB, "Ucirc"),
		new StringMapEntry(0x00DC, "Uuml"),
		new StringMapEntry(0x00DD, "Yacute"),
		new StringMapEntry(0x00DE, "THORN"),
		new StringMapEntry(0x00DF, "szlig"),
		new StringMapEntry(0x00E0, "agrave"),
		new StringMapEntry(0x00E1, "aacute"),
		new StringMapEntry(0x00E2, "acirc"),
		new StringMapEntry(0x00E3, "atilde"),
		new StringMapEntry(0x00E4, "auml"),
		new StringMapEntry(0x00E5, "aring"),
		new StringMapEntry(0x00E6, "aelig"),
		new StringMapEntry(0x00E7, "ccedil"),
		new StringMapEntry(0x00E8, "egrave"),
		new StringMapEntry(0x00E9, "eacute"),
		new StringMapEntry(0x00EA, "ecirc"),
		new StringMapEntry(0x00EB, "euml"),
		new StringMapEntry(0x00EC, "igrave"),
		new StringMapEntry(0x00ED, "iacute"),
		new StringMapEntry(0x00EE, "icirc"),
		new StringMapEntry(0x00EF, "iuml"),
		new StringMapEntry(0x00F0, "eth"),
		new StringMapEntry(0x00F1, "ntilde"),
		new StringMapEntry(0x00F2, "ograve"),
		new StringMapEntry(0x00F3, "oacute"),
		new StringMapEntry(0x00F4, "ocirc"),
		new StringMapEntry(0x00F5, "otilde"),
		new StringMapEntry(0x00F6, "ouml"),
		new StringMapEntry(0x00F7, "divide"),
		new StringMapEntry(0x00F8, "oslash"),
		new StringMapEntry(0x00F9, "ugrave"),
		new StringMapEntry(0x00FA, "uacute"),
		new StringMapEntry(0x00FB, "ucirc"),
		new StringMapEntry(0x00FC, "uuml"),
		new StringMapEntry(0x00FD, "yacute"),
		new StringMapEntry(0x00FE, "thorn"),
		new StringMapEntry(0x00FF, "yuml")
	};

	private static final	char	LATIN1_FIRST_CHAR	= '\u00A0';
	private static final	char	LATIN1_LAST_CHAR	= '\u00FF';

	// XHTML character entities : xhtml-symbol
	private static final	StringMapEntry[]	CHAR_ENTITIES_SYMBOL	=
	{
		new StringMapEntry(0x0192, "fnof"),
		new StringMapEntry(0x0391, "Alpha"),
		new StringMapEntry(0x0392, "Beta"),
		new StringMapEntry(0x0393, "Gamma"),
		new StringMapEntry(0x0394, "Delta"),
		new StringMapEntry(0x0395, "Epsilon"),
		new StringMapEntry(0x0396, "Zeta"),
		new StringMapEntry(0x0397, "Eta"),
		new StringMapEntry(0x0398, "Theta"),
		new StringMapEntry(0x0399, "Iota"),
		new StringMapEntry(0x039A, "Kappa"),
		new StringMapEntry(0x039B, "Lambda"),
		new StringMapEntry(0x039C, "Mu"),
		new StringMapEntry(0x039D, "Nu"),
		new StringMapEntry(0x039E, "Xi"),
		new StringMapEntry(0x039F, "Omicron"),
		new StringMapEntry(0x03A0, "Pi"),
		new StringMapEntry(0x03A1, "Rho"),
		new StringMapEntry(0x03A3, "Sigma"),
		new StringMapEntry(0x03A4, "Tau"),
		new StringMapEntry(0x03A5, "Upsilon"),
		new StringMapEntry(0x03A6, "Phi"),
		new StringMapEntry(0x03A7, "Chi"),
		new StringMapEntry(0x03A8, "Psi"),
		new StringMapEntry(0x03A9, "Omega"),
		new StringMapEntry(0x03B1, "alpha"),
		new StringMapEntry(0x03B2, "beta"),
		new StringMapEntry(0x03B3, "gamma"),
		new StringMapEntry(0x03B4, "delta"),
		new StringMapEntry(0x03B5, "epsilon"),
		new StringMapEntry(0x03B6, "zeta"),
		new StringMapEntry(0x03B7, "eta"),
		new StringMapEntry(0x03B8, "theta"),
		new StringMapEntry(0x03B9, "iota"),
		new StringMapEntry(0x03BA, "kappa"),
		new StringMapEntry(0x03BB, "lambda"),
		new StringMapEntry(0x03BC, "mu"),
		new StringMapEntry(0x03BD, "nu"),
		new StringMapEntry(0x03BE, "xi"),
		new StringMapEntry(0x03BF, "omicron"),
		new StringMapEntry(0x03C0, "pi"),
		new StringMapEntry(0x03C1, "rho"),
		new StringMapEntry(0x03C2, "sigmaf"),
		new StringMapEntry(0x03C3, "sigma"),
		new StringMapEntry(0x03C4, "tau"),
		new StringMapEntry(0x03C5, "upsilon"),
		new StringMapEntry(0x03C6, "phi"),
		new StringMapEntry(0x03C7, "chi"),
		new StringMapEntry(0x03C8, "psi"),
		new StringMapEntry(0x03C9, "omega"),
		new StringMapEntry(0x03D1, "thetasym"),
		new StringMapEntry(0x03D2, "upsih"),
		new StringMapEntry(0x03D6, "piv"),
		new StringMapEntry(0x2022, "bull"),
		new StringMapEntry(0x2026, "hellip"),
		new StringMapEntry(0x2032, "prime"),
		new StringMapEntry(0x2033, "Prime"),
		new StringMapEntry(0x203E, "oline"),
		new StringMapEntry(0x2044, "frasl"),
		new StringMapEntry(0x2111, "image"),
		new StringMapEntry(0x2118, "weierp"),
		new StringMapEntry(0x211C, "real"),
		new StringMapEntry(0x2122, "trade"),
		new StringMapEntry(0x2135, "alefsym"),
		new StringMapEntry(0x2190, "larr"),
		new StringMapEntry(0x2191, "uarr"),
		new StringMapEntry(0x2192, "rarr"),
		new StringMapEntry(0x2193, "darr"),
		new StringMapEntry(0x2194, "harr"),
		new StringMapEntry(0x21B5, "crarr"),
		new StringMapEntry(0x21D0, "lArr"),
		new StringMapEntry(0x21D1, "uArr"),
		new StringMapEntry(0x21D2, "rArr"),
		new StringMapEntry(0x21D3, "dArr"),
		new StringMapEntry(0x21D4, "hArr"),
		new StringMapEntry(0x2200, "forall"),
		new StringMapEntry(0x2202, "part"),
		new StringMapEntry(0x2203, "exist"),
		new StringMapEntry(0x2205, "empty"),
		new StringMapEntry(0x2207, "nabla"),
		new StringMapEntry(0x2208, "isin"),
		new StringMapEntry(0x2209, "notin"),
		new StringMapEntry(0x220B, "ni"),
		new StringMapEntry(0x220F, "prod"),
		new StringMapEntry(0x2211, "sum"),
		new StringMapEntry(0x2212, "minus"),
		new StringMapEntry(0x2217, "lowast"),
		new StringMapEntry(0x221A, "radic"),
		new StringMapEntry(0x221D, "prop"),
		new StringMapEntry(0x221E, "infin"),
		new StringMapEntry(0x2220, "ang"),
		new StringMapEntry(0x2227, "and"),
		new StringMapEntry(0x2228, "or"),
		new StringMapEntry(0x2229, "cap"),
		new StringMapEntry(0x222A, "cup"),
		new StringMapEntry(0x222B, "int"),
		new StringMapEntry(0x2234, "there4"),
		new StringMapEntry(0x223C, "sim"),
		new StringMapEntry(0x2245, "cong"),
		new StringMapEntry(0x2248, "asymp"),
		new StringMapEntry(0x2260, "ne"),
		new StringMapEntry(0x2261, "equiv"),
		new StringMapEntry(0x2264, "le"),
		new StringMapEntry(0x2265, "ge"),
		new StringMapEntry(0x2282, "sub"),
		new StringMapEntry(0x2283, "sup"),
		new StringMapEntry(0x2284, "nsub"),
		new StringMapEntry(0x2286, "sube"),
		new StringMapEntry(0x2287, "supe"),
		new StringMapEntry(0x2295, "oplus"),
		new StringMapEntry(0x2297, "otimes"),
		new StringMapEntry(0x22A5, "perp"),
		new StringMapEntry(0x22C5, "sdot"),
		new StringMapEntry(0x2308, "lceil"),
		new StringMapEntry(0x2309, "rceil"),
		new StringMapEntry(0x230A, "lfloor"),
		new StringMapEntry(0x230B, "rfloor"),
		new StringMapEntry(0x2329, "lang"),
		new StringMapEntry(0x232A, "rang"),
		new StringMapEntry(0x25CA, "loz"),
		new StringMapEntry(0x2660, "spades"),
		new StringMapEntry(0x2663, "clubs"),
		new StringMapEntry(0x2665, "hearts"),
		new StringMapEntry(0x2666, "diams")
	};

	// XHTML character entities : xhtml-special
	private static final	StringMapEntry[]	CHAR_ENTITIES_SPECIAL	=
	{
/*
		new StringMapEntry(0x0022, "quot"),
		new StringMapEntry(0x0026, "amp"),
		new StringMapEntry(0x0027, "apos"),
		new StringMapEntry(0x003C, "lt"),
		new StringMapEntry(0x003E, "gt"),
*/
		new StringMapEntry(0x0152, "OElig"),
		new StringMapEntry(0x0153, "oelig"),
		new StringMapEntry(0x0160, "Scaron"),
		new StringMapEntry(0x0161, "scaron"),
		new StringMapEntry(0x0178, "Yuml"),
		new StringMapEntry(0x02C6, "circ"),
		new StringMapEntry(0x02DC, "tilde"),
		new StringMapEntry(0x2002, "ensp"),
		new StringMapEntry(0x2003, "emsp"),
		new StringMapEntry(0x2009, "thinsp"),
		new StringMapEntry(0x200C, "zwnj"),
		new StringMapEntry(0x200D, "zwj"),
		new StringMapEntry(0x200E, "lrm"),
		new StringMapEntry(0x200F, "rlm"),
		new StringMapEntry(0x2013, "ndash"),
		new StringMapEntry(0x2014, "mdash"),
		new StringMapEntry(0x2018, "lsquo"),
		new StringMapEntry(0x2019, "rsquo"),
		new StringMapEntry(0x201A, "sbquo"),
		new StringMapEntry(0x201C, "ldquo"),
		new StringMapEntry(0x201D, "rdquo"),
		new StringMapEntry(0x201E, "bdquo"),
		new StringMapEntry(0x2020, "dagger"),
		new StringMapEntry(0x2021, "Dagger"),
		new StringMapEntry(0x2030, "permil"),
		new StringMapEntry(0x2039, "lsaquo"),
		new StringMapEntry(0x203A, "rsaquo"),
		new StringMapEntry(0x20AC, "euro")
	};

	private static final	String	ENTITY_VALUE_PREFIX	= "#x";

////////////////////////////////////////////////////////////////////////
//  Member classes : non-inner classes
////////////////////////////////////////////////////////////////////////


	// STRING MAP ENTRY CLASS


	private static class StringMapEntry
		implements Comparable<StringMapEntry>
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private StringMapEntry(int    key,
							   String value)
		{
			this.key = key;
			this.value = value;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : Comparable interface
	////////////////////////////////////////////////////////////////////

		@Override
		public int compareTo(StringMapEntry other)
		{
			return Integer.compare(key, other.key);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		int		key;
		String	value;

	}

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	private XhtmlUtils()
	{
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	public static String escape(CharSequence charSeq)
	{
		return escape(charSeq, Apostrophe.XML_ENTITY);
	}

	//------------------------------------------------------------------

	public static String escape(CharSequence charSeq,
								Apostrophe   apostrophe)
	{
		StringBuilder buffer = new StringBuilder();
		for (int i = 0; i < charSeq.length(); i++)
		{
			String name = null;
			char ch = charSeq.charAt(i);
			if (ch < '\u007F')
			{
				switch (ch)
				{
					case '<':
						name = XmlConstants.EntityName.LT;
						break;

					case '>':
						name = XmlConstants.EntityName.GT;
						break;

					case '\'':
						name = apostrophe.getEntityName();
						break;

					case '"':
						name = XmlConstants.EntityName.QUOT;
						break;

					case '&':
						name = XmlConstants.EntityName.AMP;
						break;
				}
			}
			else
			{
				if ((ch >= LATIN1_FIRST_CHAR) && (ch <= LATIN1_LAST_CHAR))
					name = CHAR_ENTITIES_LATIN1[ch - LATIN1_FIRST_CHAR].value;
				else
				{
					StringMapEntry target = new StringMapEntry(ch, null);
					int index = Arrays.binarySearch(CHAR_ENTITIES_SYMBOL, target);
					if (index >= 0)
						name = CHAR_ENTITIES_SYMBOL[index].value;
					else
					{
						index = Arrays.binarySearch(CHAR_ENTITIES_SPECIAL, target);
						if (index >= 0)
							name = CHAR_ENTITIES_SPECIAL[index].value;
						else
							name = ENTITY_VALUE_PREFIX + NumberUtils.uIntToHexString(ch, 4, '0');
					}
				}
			}
			if (name == null)
				buffer.append(ch);
			else
			{
				buffer.append(XmlConstants.ENTITY_PREFIX);
				buffer.append(name);
				buffer.append(XmlConstants.ENTITY_SUFFIX);
			}
		}
		return buffer.toString();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Static initialiser
////////////////////////////////////////////////////////////////////////

	static
	{
		if (CHAR_ENTITIES_LATIN1.length != LATIN1_LAST_CHAR - LATIN1_FIRST_CHAR + 1)
			throw new UnexpectedRuntimeException();
	}

}

//----------------------------------------------------------------------
