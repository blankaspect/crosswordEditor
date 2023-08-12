/*====================================================================*\

PortNumber.java

Class: methods for accessing a port-number file.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.common.config;

//----------------------------------------------------------------------


// IMPORTS


import java.io.File;
import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;

import uk.blankaspect.common.exception2.ExceptionUtils;

//----------------------------------------------------------------------


// CLASS: METHODS FOR ACCESSING A PORT-NUMBER FILE


public class PortNumber
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	private static final	String	PORT_FILENAME_PREFIX	= "port-";

	private interface ErrorMsg
	{
		String	INVALID_PORT_NUMBER	= "The port number is invalid.";
		String	NOT_A_FILE			= "The location does not denote a regular file";
		String	ERROR_READING_FILE	= "An error occurred when reading the file";
		String	ERROR_WRITING_FILE	= "An error occurred when writing the file";
	}

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	private PortNumber()
	{
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	public static int getValue(
		String	key)
	{
		String pathname = PropertiesPathname.getPathname();
		return (pathname == null) ? -1 : getValue(Path.of(pathname), key);
	}

	//------------------------------------------------------------------

	public static int getValue(
		Path	directory,
		String	key)
	{
		// Get content of file
		Path location = getFileLocation(directory, key);
		String str = readFile(location);

		// Parse value
		int value = -1;
		if (str != null)
		{
			try
			{
				value = Integer.parseInt(str);
			}
			catch (IllegalArgumentException e)
			{
				ExceptionUtils.printStderrLocated(location);
				System.err.println(ErrorMsg.INVALID_PORT_NUMBER);
			}
		}

		return value;
	}

	//------------------------------------------------------------------

	public static void setValue(
		String	key,
		int		value)
	{
		String pathname = PropertiesPathname.getPathname();
		if (pathname != null)
			setValue(Path.of(pathname), key, value);
	}

	//------------------------------------------------------------------

	public static void setValue(
		Path	directory,
		String	key,
		int		value)
	{
		// Convert value to string and write it to file
		writeFile(getFileLocation(directory, key), Integer.toString(value));
	}

	//------------------------------------------------------------------

	private static String readFile(
		Path	location)
	{
		// Initialise file content
		String content = null;

		// If file exists, read it
		if (Files.exists(location, LinkOption.NOFOLLOW_LINKS))
		{
			try
			{
				// Test for regular file
				if (!Files.isRegularFile(location, LinkOption.NOFOLLOW_LINKS))
					throw new Exception(ErrorMsg.NOT_A_FILE);

				// Read file
				try
				{
					content = Files.readString(location);
				}
				catch (IOException e)
				{
					throw new Exception(ErrorMsg.ERROR_READING_FILE, e);
				}
			}
			catch (Exception e)
			{
				ExceptionUtils.printStderrLocated(location);
				String message = e.getMessage();
				if (message != null)
					System.err.println(e.getMessage());
			}
		}

		// Return file content
		return content;
	}

	//------------------------------------------------------------------

	private static void writeFile(
		Path	location,
		String	content)
	{
		try
		{
			try
			{
				Files.writeString(location, content);
			}
			catch (IOException e)
			{
				throw new Exception(ErrorMsg.ERROR_WRITING_FILE, e);
			}
		}
		catch (Exception e)
		{
			ExceptionUtils.printStderrLocated(location);
			String message = e.getMessage();
			if (message != null)
				System.err.println(e.getMessage());
		}
	}

	//------------------------------------------------------------------

	private static Path getFileLocation(
		Path	directory,
		String	key)
	{
		return Path.of(directory.toString().replace('/', File.separatorChar), PORT_FILENAME_PREFIX + key);
	}

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------
