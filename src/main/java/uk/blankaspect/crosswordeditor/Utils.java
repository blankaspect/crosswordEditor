/*====================================================================*\

Utils.java

Utility methods class.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.crosswordeditor;

//----------------------------------------------------------------------


// IMPORTS


import java.awt.Image;
import java.awt.Toolkit;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;

import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.awt.image.PixelGrabber;

import java.io.File;
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;

import uk.blankaspect.common.exception.AppException;
import uk.blankaspect.common.exception.UnexpectedRuntimeException;

import uk.blankaspect.common.misc.PropertiesPathname;
import uk.blankaspect.common.misc.PropertyString;
import uk.blankaspect.common.misc.SystemUtils;

//----------------------------------------------------------------------


// UTILITY METHODS CLASS


class Utils
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	private static final	String	USER_HOME_PREFIX			= "~";
	private static final	String	FAILED_TO_GET_PATHNAME_STR	= "Failed to get the canonical pathname " +
																	"for the file or directory.";

////////////////////////////////////////////////////////////////////////
//  Enumerated types
////////////////////////////////////////////////////////////////////////


	// ERROR IDENTIFIERS


	private enum ErrorId
		implements AppException.IId
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		CLIPBOARD_IS_UNAVAILABLE
		("The clipboard is currently unavailable."),

		FAILED_TO_GET_CLIPBOARD_DATA
		("Failed to get data from the clipboard."),

		NO_TEXT_ON_CLIPBOARD
		("There is no text on the clipboard."),

		NO_IMAGE_ON_CLIPBOARD
		("There is no image on the clipboard."),

		FAILED_TO_GET_IMAGE_FROM_CLIPBOARD
		("Failed to get the image from the clipboard."),

		MALFORMED_VIEWER_COMMAND
		("The HTML viewer command is malformed."),

		FAILED_TO_EXECUTE_VIEWER_COMMAND
		("Failed to execute the HTML viewer command.");

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private ErrorId(String message)
		{
			this.message = message;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : AppException.IId interface
	////////////////////////////////////////////////////////////////////

		public String getMessage()
		{
			return message;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance fields
	////////////////////////////////////////////////////////////////////

		private	String	message;

	}

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	private Utils()
	{
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	public static int indexOf(Object   target,
							  Object[] values)
	{
		for (int i = 0; i < values.length; i++)
		{
			if (values[i].equals(target))
				return i;
		}
		return -1;
	}

	//------------------------------------------------------------------

	public static char getFileSeparatorChar()
	{
		return (AppConfig.INSTANCE.isShowUnixPathnames() ? '/' : File.separatorChar);
	}

	//------------------------------------------------------------------

	public static String getPathname(File file)
	{
		return getPathname(file, AppConfig.INSTANCE.isShowUnixPathnames());
	}

	//------------------------------------------------------------------

	public static String getPathname(File    file,
									 boolean unixStyle)
	{
		String pathname = null;
		if (file != null)
		{
			try
			{
				try
				{
					pathname = file.getCanonicalPath();
				}
				catch (Exception e)
				{
					System.err.println(file.getPath());
					System.err.println(FAILED_TO_GET_PATHNAME_STR);
					System.err.println("(" + e + ")");
					pathname = file.getAbsolutePath();
				}
			}
			catch (SecurityException e)
			{
				System.err.println(e);
				pathname = file.getPath();
			}

			if (unixStyle)
			{
				try
				{
					String userHome = SystemUtils.getUserHomePathname();
					if ((userHome != null) && pathname.startsWith(userHome))
						pathname = USER_HOME_PREFIX + pathname.substring(userHome.length());
				}
				catch (SecurityException e)
				{
					// ignore
				}
				pathname = pathname.replace(File.separatorChar, '/');
			}
		}
		return pathname;
	}

	//------------------------------------------------------------------

	public static String getPropertiesPathname()
	{
		String pathname = PropertiesPathname.getPathname();
		if (pathname != null)
			pathname += App.NAME_KEY;
		return pathname;
	}

	//------------------------------------------------------------------

	public static boolean isSameFile(File file1,
									 File file2)
	{
		try
		{
			if (file1 == null)
				return (file2 == null);
			return ((file2 != null) && file1.getCanonicalPath().equals(file2.getCanonicalPath()));
		}
		catch (IOException e)
		{
			return false;
		}
	}

	//------------------------------------------------------------------

	public static File appendSuffix(File   file,
									String suffix)
	{
		String filename = file.getName();
		if (!filename.isEmpty() && (filename.indexOf('.') < 0))
			file = new File(file.getParentFile(), filename + suffix);
		return file;
	}

	//------------------------------------------------------------------

	public static String[] getOptionStrings(String... optionStrs)
	{
		String[] strs = new String[optionStrs.length + 1];
		System.arraycopy(optionStrs, 0, strs, 0, optionStrs.length);
		strs[optionStrs.length] = AppConstants.CANCEL_STR;
		return strs;
	}

	//------------------------------------------------------------------

	public static boolean clipboardHasText()
	{
		try
		{
			return Toolkit.getDefaultToolkit().getSystemClipboard().
														isDataFlavorAvailable(DataFlavor.stringFlavor);
		}
		catch (IllegalStateException e)
		{
			System.out.println(Thread.currentThread().getStackTrace()[1] + " : " + e);
		}
		return false;
	}

	//------------------------------------------------------------------

	public static String getClipboardText()
		throws AppException
	{
		try
		{
			Transferable contents = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
			if (contents == null)
				throw new AppException(ErrorId.NO_TEXT_ON_CLIPBOARD);
			return (String)contents.getTransferData(DataFlavor.stringFlavor);
		}
		catch (IllegalStateException e)
		{
			throw new AppException(ErrorId.CLIPBOARD_IS_UNAVAILABLE, e);
		}
		catch (UnsupportedFlavorException e)
		{
			throw new AppException(ErrorId.NO_TEXT_ON_CLIPBOARD);
		}
		catch (IOException e)
		{
			throw new AppException(ErrorId.FAILED_TO_GET_CLIPBOARD_DATA, e);
		}
	}

	//------------------------------------------------------------------

	public static BufferedImage getClipboardImage()
		throws AppException
	{
		// Get image from clipboard
		Image inImage = null;
		try
		{
			Transferable contents = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
			if (contents == null)
				throw new AppException(ErrorId.NO_IMAGE_ON_CLIPBOARD);
			inImage = (Image)contents.getTransferData(DataFlavor.imageFlavor);
		}
		catch (IllegalStateException e)
		{
			throw new AppException(ErrorId.CLIPBOARD_IS_UNAVAILABLE, e);
		}
		catch (UnsupportedFlavorException e)
		{
			throw new AppException(ErrorId.NO_IMAGE_ON_CLIPBOARD);
		}
		catch (IOException e)
		{
			throw new AppException(ErrorId.FAILED_TO_GET_CLIPBOARD_DATA, e);
		}

		// Test for image
		int width = inImage.getWidth(null);
		int height = inImage.getHeight(null);
		if ((width < 0) || (height < 0))
			throw new AppException(ErrorId.FAILED_TO_GET_IMAGE_FROM_CLIPBOARD);

		// Get RGB data of image
		int[] rgbBuffer = new int[width * height];
		PixelGrabber grabber = new PixelGrabber(inImage, 0, 0, width, height, rgbBuffer, 0, width);
		try
		{
			grabber.grabPixels();
		}
		catch (InterruptedException e)
		{
			throw new UnexpectedRuntimeException();
		}
		if ((grabber.getStatus() & ImageObserver.ABORT) != 0)
			throw new AppException(ErrorId.FAILED_TO_GET_IMAGE_FROM_CLIPBOARD);

		// Create buffered image and return it
		BufferedImage outImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		outImage.setRGB(0, 0, width, height, rgbBuffer, 0, width);
		return outImage;
	}

	//------------------------------------------------------------------

	public static void putClipboardText(String text)
		throws AppException
	{
		try
		{
			StringSelection selection = new StringSelection(text);
			Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, selection);
		}
		catch (IllegalStateException e)
		{
			throw new AppException(ErrorId.CLIPBOARD_IS_UNAVAILABLE, e);
		}
	}

	//------------------------------------------------------------------

	public static List<String> parseCommand(String str,
											String pathname)
	{
		final	char	ESCAPE_CHAR					= '%';
		final	char	PATHNAME_PLACEHOLDER_CHAR	= 'f';

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
						else
							buffer.append(ch);
					}
					break;

				case ' ':
					if (buffer.length() > 0)
					{
						arguments.add(PropertyString.parsePathname(buffer.toString()));
						buffer.setLength(0);
					}
					break;

				default:
					buffer.append(ch);
					break;
			}
		}
		if (buffer.length() > 0)
			arguments.add(PropertyString.parsePathname(buffer.toString()));

		return arguments;
	}

	//------------------------------------------------------------------

	public static void viewHtmlFile(File file)
		throws AppException
	{
		// Get command for invoking HTML viewer
		String command = AppConfig.INSTANCE.getHtmlViewerCommand();

		// Execute command
		if (command != null)
		{
			// Parse command to create list of arguments
			List<String> arguments = null;
			try
			{
				arguments = parseCommand(command, file.getPath());
			}
			catch (IllegalArgumentException e)
			{
				throw new AppException(ErrorId.MALFORMED_VIEWER_COMMAND);
			}

			// Invoke viewer
			try
			{
				new ProcessBuilder(arguments).start();
			}
			catch (IOException e)
			{
				throw new AppException(ErrorId.FAILED_TO_EXECUTE_VIEWER_COMMAND, e);
			}
		}
	}

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------
