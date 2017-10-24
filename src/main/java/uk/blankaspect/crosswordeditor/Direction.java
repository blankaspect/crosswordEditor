/*====================================================================*\

Direction.java

Direction enumeration.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.crosswordeditor;

//----------------------------------------------------------------------


// IMPORTS


import java.awt.event.KeyEvent;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import uk.blankaspect.common.misc.IStringKeyed;
import uk.blankaspect.common.misc.StringUtils;

//----------------------------------------------------------------------


// DIRECTION ENUMERATION


enum Direction
	implements IStringKeyed
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	NONE
	(
		"none",
		"",
		KeyEvent.VK_N
	),

	ACROSS
	(
		"across",
		"a",
		KeyEvent.VK_A
	),

	DOWN
	(
		"down",
		"d",
		KeyEvent.VK_D
	);

	//------------------------------------------------------------------

	public static final		EnumSet<Direction>	DEFINED_DIRECTIONS	= EnumSet.of(ACROSS, DOWN);

	public static final		KeywordComparator	KEYWORD_COMPARATOR	= new KeywordComparator();

	private static final	Map<Direction, List<String>>	KEYWORD_LISTS;

////////////////////////////////////////////////////////////////////////
//  Member classes : non-inner classes
////////////////////////////////////////////////////////////////////////


	// KEYWORD COMPARATOR CLASS


	private static class KeywordComparator
		implements Comparator<String>
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private KeywordComparator()
		{
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : Comparator interface
	////////////////////////////////////////////////////////////////////

		@Override
		public int compare(String keyword1,
						   String keyword2)
		{
			int result = Integer.compare(keyword2.length(), keyword1.length());
			if (result == 0)
				result = keyword1.compareTo(keyword2);
			return result;
		}

		//--------------------------------------------------------------

	}

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	private Direction(String key,
					  String suffix,
					  int    keyCode)
	{
		this.key = key;
		this.suffix = suffix;
		this.keyCode = keyCode;
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	public static Direction forKey(String key)
	{
		for (Direction value : values())
		{
			if (value.key.equals(key))
				return value;
		}
		return null;
	}

	//------------------------------------------------------------------

	public static Direction forSuffix(String suffix)
	{
		for (Direction value : values())
		{
			if (value.suffix.equals(suffix))
				return value;
		}
		return null;
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : IStringKeyed interface
////////////////////////////////////////////////////////////////////////

	public String getKey()
	{
		return key;
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : overriding methods
////////////////////////////////////////////////////////////////////////

	@Override
	public String toString()
	{
		return StringUtils.firstCharToUpperCase(key);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	public String getSuffix()
	{
		return suffix;
	}

	//------------------------------------------------------------------

	public int getKeyCode()
	{
		return keyCode;
	}

	//------------------------------------------------------------------

	public List<String> getKeywords()
	{
		return KEYWORD_LISTS.get(this);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Static initialiser
////////////////////////////////////////////////////////////////////////

	static
	{
		Map<Direction, List<String>> lists = new EnumMap<>(Direction.class);
		lists.put(Direction.ACROSS,
				  Arrays.asList("Across", " Across", " across", "Ac", " ac", "A", " a"));
		lists.put(Direction.DOWN,
				  Arrays.asList("Down",   " Down",   " down",   "Dn", " dn", "D", " d"));
		KEYWORD_LISTS = Collections.unmodifiableMap(lists);
	}

////////////////////////////////////////////////////////////////////////
//  Instance fields
////////////////////////////////////////////////////////////////////////

	private	String	key;
	private	String	suffix;
	private	int		keyCode;

}

//----------------------------------------------------------------------
