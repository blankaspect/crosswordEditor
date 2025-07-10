/*====================================================================*\

HtmlViewer.java

Class: HTML viewer methods.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.crosswordeditor;

//----------------------------------------------------------------------


// IMPORTS


import java.io.File;

import java.nio.file.Path;

import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;

import uk.blankaspect.common.exception.AppException;

import uk.blankaspect.common.filesystem.PathnameUtils;

import uk.blankaspect.common.thread.DaemonFactory;

//----------------------------------------------------------------------


// CLASS: HTML VIEWER METHODS


class HtmlViewer
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	private static final	String	THREAD_NAME_PREFIX	= "viewer-";

////////////////////////////////////////////////////////////////////////
//  Class variables
////////////////////////////////////////////////////////////////////////

	private static	int	threadIndex;

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	private HtmlViewer()
	{
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	public static void viewHtmlFile(
		String	command,
		File	file)
		throws AppException
	{
		// Parse command to create list of arguments
		List<String> args = null;
		try
		{
			args = parseCommand(command, file.getPath());
		}
		catch (IllegalArgumentException e)
		{
			throw new AppException(ErrorId.MALFORMED_VIEWER_COMMAND);
		}
		List<String> arguments = args;

		// Execute viewer command
		DaemonFactory.create(THREAD_NAME_PREFIX + ++threadIndex, () ->
		{
			try
			{
				// Start process
				new ProcessBuilder(arguments).inheritIO().start();
			}
			catch (Exception e)
			{
				SwingUtilities.invokeLater(() ->
						CrosswordEditorApp.INSTANCE
								.showErrorMessage(CrosswordEditorApp.SHORT_NAME,
												  new AppException(ErrorId.FAILED_TO_EXECUTE_VIEWER_COMMAND, e)));
			}
		})
		.start();
	}

	//------------------------------------------------------------------

	private static List<String> parseCommand(
		String	str,
		String	pathname)
	{
		final	char	ESCAPE_CHAR					= '%';
		final	char	PATHNAME_PLACEHOLDER_CHAR	= 'f';
		final	char	URI_PLACEHOLDER_CHAR		= 'u';

		List<String> arguments = new ArrayList<>();
		StringBuilder buffer = new StringBuilder();
		int index = 0;
		while (index < str.length())
		{
			char ch = str.charAt(index++);
			switch (ch)
			{
				case ESCAPE_CHAR:
					if (index < str.length())
					{
						ch = str.charAt(index++);
						if (ch == PATHNAME_PLACEHOLDER_CHAR)
							buffer.append(pathname);
						else if (ch == URI_PLACEHOLDER_CHAR)
							buffer.append(Path.of(pathname).toUri());
						else
							buffer.append(ch);
					}
					break;

				case ' ':
					if (!buffer.isEmpty())
					{
						arguments.add(PathnameUtils.parsePathname(buffer.toString()));
						buffer.setLength(0);
					}
					break;

				default:
					buffer.append(ch);
					break;
			}
		}
		if (!buffer.isEmpty())
			arguments.add(PathnameUtils.parsePathname(buffer.toString()));

		return arguments;
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Enumerated types
////////////////////////////////////////////////////////////////////////


	// ENUMERATION: ERROR IDENTIFIERS


	private enum ErrorId
		implements AppException.IId
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		MALFORMED_VIEWER_COMMAND
		("The HTML viewer command is malformed."),

		FAILED_TO_EXECUTE_VIEWER_COMMAND
		("Failed to execute the HTML viewer command.");

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	String	message;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private ErrorId(
			String	message)
		{
			this.message = message;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : AppException.IId interface
	////////////////////////////////////////////////////////////////////

		@Override
		public String getMessage()
		{
			return message;
		}

		//--------------------------------------------------------------

	}

	//==================================================================

}

//----------------------------------------------------------------------
