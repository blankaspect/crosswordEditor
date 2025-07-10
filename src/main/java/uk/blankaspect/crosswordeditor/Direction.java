/*====================================================================*\

Direction.java

Enumeration: direction.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.crosswordeditor;

//----------------------------------------------------------------------


// IMPORTS


import java.awt.event.KeyEvent;

import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import java.util.stream.Stream;

import uk.blankaspect.common.misc.IStringKeyed;

import uk.blankaspect.common.string.StringUtils;

//----------------------------------------------------------------------


// ENUMERATION: DIRECTION


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

	public static final		Comparator<String>	KEYWORD_COMPARATOR;

	private static final	Map<Direction, List<String>>	KEYWORD_LISTS;

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private	String	key;
	private	String	suffix;
	private	int		keyCode;

////////////////////////////////////////////////////////////////////////
//  Static initialiser
////////////////////////////////////////////////////////////////////////

	static
	{
		KEYWORD_COMPARATOR = Comparator.<String>comparingInt(keyword -> keyword.length())
								.thenComparing(Comparator.naturalOrder());

		Map<Direction, List<String>> lists = new EnumMap<>(Direction.class);
		lists.put(Direction.ACROSS, List.of("Across", " Across", " across", "Ac", " ac", "A", " a"));
		lists.put(Direction.DOWN,   List.of("Down",   " Down",   " down",   "Dn", " dn", "D", " d"));
		KEYWORD_LISTS = Collections.unmodifiableMap(lists);
	}

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
		return Stream.of(values())
				.filter(value -> value.key.equals(key))
				.findFirst()
				.orElse(null);
	}

	//------------------------------------------------------------------

	public static Direction forSuffix(String suffix)
	{
		return Stream.of(values())
				.filter(value -> value.suffix.equals(suffix))
				.findFirst()
				.orElse(null);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : IStringKeyed interface
////////////////////////////////////////////////////////////////////////

	@Override
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

}

//----------------------------------------------------------------------
