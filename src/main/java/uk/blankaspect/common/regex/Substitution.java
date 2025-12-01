/*====================================================================*\

Substitution.java

Class: regular-expression substitution.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.common.regex;

//----------------------------------------------------------------------


// IMPORTS


import java.util.ArrayList;
import java.util.List;

import java.util.regex.Pattern;

import uk.blankaspect.common.exception2.UnexpectedRuntimeException;

//----------------------------------------------------------------------


// CLASS: REGULAR-EXPRESSION SUBSTITUTION


public class Substitution
	implements Cloneable
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	private static final	char	SEPARATOR_LOWER_BOUND	= '\u2000';
	private static final	char	SEPARATOR_UPPER_BOUND	= '\u2B00';
	private static final	char	DEFAULT_SEPARATOR		= '\uFFFC';

	private static final	char	LITERAL_FLAG	= 'l';

	private static final	String	PREFERRED_SEPARATORS	= "/|!#%&$@?+-~*^=_<>[](){}.,;:`'\"\\";

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private	String	target;
	private	String	replacement;
	private	boolean	literal;

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	/**
	 * @throws IllegalArgumentException
	 * @throws PatternSyntaxException
	 */

	public Substitution(
		String	target,
		String	replacement)
	{
		this(target, replacement, false);
	}

	//------------------------------------------------------------------

	/**
	 * @throws IllegalArgumentException
	 * @throws PatternSyntaxException
	 */

	public Substitution(
		String	target,
		String	replacement,
		boolean	literal)
	{
		// Validate arguments
		if (target == null)
			throw new IllegalArgumentException();

		// Initialise instance variables
		this.target = target;
		this.replacement = (replacement == null) ? "" : replacement;
		this.literal = literal;

		// Validate regular-expression target
		if (!literal)
			getTargetPattern();
	}

	//------------------------------------------------------------------

	/**
	 * @throws IllegalArgumentException
	 * @throws PatternSyntaxException
	 */

	public Substitution(
		String	str)
	{
		// Validate arguments
		if ((str == null) || (str.length() < 3))
			throw new IllegalArgumentException();

		// Split string into target, replacement and flags
		List<String> strs = new ArrayList<>();
		char separator = str.charAt(0);
		int index = 1;
		while (index < str.length())
		{
			int startIndex = index;
			index = str.indexOf(separator, index);
			if (index < 0)
				index = str.length();
			strs.add(str.substring(startIndex, index));
			if (++index == str.length())
				strs.add("");
		}
		if (strs.size() != 3)
			throw new IllegalArgumentException();

		// Initialise instance variables
		index = 0;
		target = strs.get(index++);
		replacement = strs.get(index++);
		String flags = strs.get(index++);
		if (!flags.isEmpty())
		{
			if (!flags.equals(Character.toString(LITERAL_FLAG)))
				throw new IllegalArgumentException();
			literal = true;
		}

		// Validate regular-expression target
		if (!literal)
			getTargetPattern();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : overriding methods
////////////////////////////////////////////////////////////////////////

	@Override
	public boolean equals(
		Object	obj)
	{
		if (this == obj)
			return true;

		return (obj instanceof Substitution other) && target.equals(other.target)
				&& replacement.equals(other.replacement) && (literal == other.literal);
	}

	//------------------------------------------------------------------

	@Override
	public int hashCode()
	{
		int code = target.hashCode();
		code = 31 * code + replacement.hashCode();
		code = 31 * code + (literal ? 1 : 0);
		return code;
	}

	//------------------------------------------------------------------

	@Override
	public Substitution clone()
	{
		try
		{
			return (Substitution)super.clone();
		}
		catch (CloneNotSupportedException e)
		{
			throw new UnexpectedRuntimeException(e);
		}
	}

	//------------------------------------------------------------------

	@Override
	public String toString()
	{
		char separator = DEFAULT_SEPARATOR;
		for (int i = 0; i < PREFERRED_SEPARATORS.length(); i++)
		{
			char ch = PREFERRED_SEPARATORS.charAt(i);
			if ((target.indexOf(ch) < 0) && (replacement.indexOf(ch) < 0))
			{
				separator = ch;
				break;
			}
		}
		if (separator == DEFAULT_SEPARATOR)
		{
			for (char ch = SEPARATOR_LOWER_BOUND; ch < SEPARATOR_UPPER_BOUND; ch++)
			{
				if ((target.indexOf(ch) < 0) && (replacement.indexOf(ch) < 0))
				{
					separator = ch;
					break;
				}
			}
		}
		return separator + target + separator + replacement + separator + (literal ? LITERAL_FLAG : "");
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	public String getTarget()
	{
		return target;
	}

	//------------------------------------------------------------------

	public String getReplacement()
	{
		return replacement;
	}

	//------------------------------------------------------------------

	public boolean isLiteral()
	{
		return literal;
	}

	//------------------------------------------------------------------

	/**
	 * @throws PatternSyntaxException
	 */

	public Pattern getTargetPattern()
	{
		return Pattern.compile(target);
	}

	//------------------------------------------------------------------

	public String apply(
		String	str)
	{
		return literal ? str.replace(target, replacement) : str.replaceAll(target, replacement);
	}

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------
