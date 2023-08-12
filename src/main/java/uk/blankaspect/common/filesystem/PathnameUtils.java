/*====================================================================*\

PathnameUtils.java

Class: pathname-related utility methods.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.common.filesystem;

//----------------------------------------------------------------------


// IMPORTS


import java.io.File;

import java.nio.file.Path;

import java.util.Arrays;

import uk.blankaspect.common.function.IFunction2;

import uk.blankaspect.common.misc.SystemUtils;

import uk.blankaspect.common.property.PropertyString;

//----------------------------------------------------------------------


// CLASS: PATHNAME-RELATED UTILITY METHODS


/**
 * This class contains utility methods that relate to pathnames.
 */

public class PathnameUtils
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	public static final	String	USER_HOME_PREFIX	= "~";

////////////////////////////////////////////////////////////////////////
//  Class variables
////////////////////////////////////////////////////////////////////////

	private static	boolean	ignoreFilenameCase	= (File.separatorChar == '\\');

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	/**
	 * Prevents this class from being instantiated externally.
	 */

	private PathnameUtils()
	{
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	public static boolean isIgnoreFilenameCase()
	{
		return ignoreFilenameCase;
	}

	//------------------------------------------------------------------

	public static void setIgnoreFilenameCase(
		boolean	ignore)
	{
		ignoreFilenameCase = ignore;
	}

	//------------------------------------------------------------------

	public static String parsePathname(
		String	str)
	{
		if (str == null)
			return null;

		if (str.startsWith(USER_HOME_PREFIX))
		{
			int prefixLength = USER_HOME_PREFIX.length();
			if ((str.length() == prefixLength) || (str.charAt(prefixLength) == File.separatorChar)
					|| (str.charAt(prefixLength) == '/'))
			{
				String pathname = SystemUtils.getUserHomePathname();
				if (pathname != null)
					str = pathname + str.substring(prefixLength);
			}
		}
		return PropertyString.parse(str);
	}

	//------------------------------------------------------------------

	public static String toUnixStyle(
		String	pathname,
		boolean	abbreviateUserHome)
	{
		// If pathname starts with user's home directory, replace it with '~'
		if (abbreviateUserHome)
		{
			String userHome = SystemUtils.getUserHomePathname();
			if ((userHome != null) && pathname.startsWith(userHome))
				pathname = PathnameUtils.USER_HOME_PREFIX + pathname.substring(userHome.length());
		}

		// Replace non-Unix file separators with Unix separators
		return pathname.replace(File.separatorChar, '/');
	}

	//------------------------------------------------------------------

	public static String toSystemStyle(
		String	pathname)
	{
		if (File.separatorChar != '/')
		{
			// Replace Unix file separators with local separators
			pathname = pathname.replace('/', File.separatorChar);

			// If pathname starts with '~', replace it with user's home directory
			if (pathname.startsWith(USER_HOME_PREFIX))
			{
				String userHome = SystemUtils.getUserHomePathname();
				if (userHome != null)
					pathname = userHome + pathname.substring(USER_HOME_PREFIX.length());
			}
		}

		return pathname;
	}

	//------------------------------------------------------------------

	/**
	 * Returns {@code true} if the suffix of the specified pathname matches any of the specified suffixes (filename
	 * extensions, for example).  Letter case is ignored if {@link #ignoreFilenameCase} is {@code true}.
	 *
	 * @param  pathname
	 *           the pathname whose suffix will be compared to {@code suffixes}.
	 * @param  suffixes
	 *           the strings that will be compared to the suffix of {@code pathname}.
	 * @return {@code true} if the suffix of {@code pathname} matches any of {@code suffixes}.
	 */

	public static boolean suffixMatches(
		CharSequence		pathname,
		Iterable<String>	suffixes)
	{
		IFunction2<Boolean, String, String> matcher = (target, suffix) ->
				ignoreFilenameCase ? suffix.equalsIgnoreCase(target) : suffix.equals(target);

		for (String suffix : suffixes)
		{
			int pathnameLength = pathname.length();
			int suffixLength = suffix.length();
			if ((pathnameLength >= suffixLength)
				&& matcher.invoke(pathname.subSequence(pathnameLength - suffixLength, pathnameLength).toString(), suffix))
				return true;
		}
		return false;
	}

	//------------------------------------------------------------------

	/**
	 * Returns {@code true} if the suffix of the specified pathname matches any of the specified suffixes (filename
	 * extensions, for example).  Letter case is ignored if {@link #ignoreFilenameCase} is {@code true}.
	 *
	 * @param  pathname
	 *           the pathname whose suffix will be compared to {@code suffixes}.
	 * @param  suffixes
	 *           the strings that will be compared to the suffix of {@code pathname}.
	 * @return {@code true} if the suffix of {@code pathname} matches any of {@code suffixes}.
	 */

	public static boolean suffixMatches(
		CharSequence	pathname,
		String...		suffixes)
	{
		return suffixMatches(pathname, Arrays.asList(suffixes));
	}

	//------------------------------------------------------------------

	/**
	 * Returns {@code true} if the suffix of the specified file-system location matches any of the specified suffixes
	 * (filename extensions, for example).  Letter case is ignored if {@link #ignoreFilenameCase} is {@code true}.
	 *
	 * @param  location
	 *           the file-system location whose suffix will be compared to {@code suffixes}.
	 * @param  suffixes
	 *           the strings that will be compared to the suffix of {@code location}.
	 * @return {@code true} if the suffix of {@code location} matches any of {@code suffixes}.
	 */

	public static boolean suffixMatches(
		Path				location,
		Iterable<String>	suffixes)
	{
		return suffixMatches(location.toString(), suffixes);
	}

	//------------------------------------------------------------------

	/**
	 * Returns {@code true} if the suffix of the specified file-system location matches any of the specified suffixes
	 * (filename extensions, for example).  Letter case is ignored if {@link #ignoreFilenameCase} is {@code true}.
	 *
	 * @param  location
	 *           the file-system location whose suffix will be compared to {@code suffixes}.
	 * @param  suffixes
	 *           the strings that will be compared to the suffix of {@code location}.
	 * @return {@code true} if the suffix of {@code location} matches any of {@code suffixes}.
	 */

	public static boolean suffixMatches(
		Path		location,
		String...	suffixes)
	{
		return suffixMatches(location.toString(), suffixes);
	}

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------
