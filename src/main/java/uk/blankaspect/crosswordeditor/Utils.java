/*====================================================================*\

Utils.java

Class: utility methods.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.crosswordeditor;

//----------------------------------------------------------------------


// IMPORTS


import java.awt.Color;
import java.awt.Image;
import java.awt.Toolkit;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;

import java.awt.image.BufferedImage;

import java.io.File;
import java.io.IOException;

import uk.blankaspect.common.config.PropertiesPathname;

import uk.blankaspect.common.exception.AppException;

import uk.blankaspect.common.exception2.ExceptionUtils;
import uk.blankaspect.common.exception2.UnexpectedRuntimeException;

import uk.blankaspect.common.filesystem.PathnameUtils;

import uk.blankaspect.common.stack.StackUtils;

//----------------------------------------------------------------------


// CLASS: UTILITY METHODS


class Utils
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	private static final	String	FAILED_TO_GET_PATHNAME_STR			= "Failed to get the canonical pathname for ";
	private static final	String	FAILED_TO_CREATE_OPAQUE_IMAGE_STR	= "Failed to create an opaque image";

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

	public static int indexOf(
		Object		target,
		Object[]	values)
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
		return AppConfig.INSTANCE.isShowUnixPathnames() ? '/' : File.separatorChar;
	}

	//------------------------------------------------------------------

	public static String getPathname(
		File	file)
	{
		return getPathname(file, AppConfig.INSTANCE.isShowUnixPathnames());
	}

	//------------------------------------------------------------------

	public static String getPathname(
		File	file,
		boolean	unixStyle)
	{
		String pathname = null;
		if (file != null)
		{
			try
			{
				pathname = file.getCanonicalPath();
			}
			catch (Exception e)
			{
				ExceptionUtils.printStderrLocated(FAILED_TO_GET_PATHNAME_STR + file.getPath());
				System.err.println("- " + e);
				pathname = file.getAbsolutePath();
			}

			if (unixStyle)
				pathname = PathnameUtils.toUnixStyle(pathname, true);
		}
		return pathname;
	}

	//------------------------------------------------------------------

	public static String getPropertiesPathname()
	{
		String pathname = PropertiesPathname.getPathname();
		if (pathname != null)
			pathname += CrosswordEditorApp.NAME_KEY;
		return pathname;
	}

	//------------------------------------------------------------------

	public static boolean isSameFile(
		File	file1,
		File	file2)
	{
		try
		{
			if (file1 == null)
				return (file2 == null);
			return (file2 != null) && file1.getCanonicalPath().equals(file2.getCanonicalPath());
		}
		catch (IOException e)
		{
			return false;
		}
	}

	//------------------------------------------------------------------

	public static File appendSuffix(
		File	file,
		String	suffix)
	{
		String filename = file.getName();
		if (!filename.isEmpty() && (filename.indexOf('.') < 0))
			file = new File(file.getParentFile(), filename + suffix);
		return file;
	}

	//------------------------------------------------------------------

	public static String[] getOptionStrings(
		String...	optionStrs)
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
			return Toolkit.getDefaultToolkit().getSystemClipboard().isDataFlavorAvailable(DataFlavor.stringFlavor);
		}
		catch (IllegalStateException e)
		{
			System.err.println(StackUtils.toStackTraceString(StackUtils.stackFrame()) + " : " + e);
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
		if ((width <= 0) || (height <= 0))
			throw new AppException(ErrorId.CANNOT_GET_IMAGE_DIMENSIONS);

		// Create buffered image and return it
		BufferedImage outImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		if (!outImage.getGraphics().drawImage(inImage, 0, 0, Color.WHITE, null))
			new UnexpectedRuntimeException(FAILED_TO_CREATE_OPAQUE_IMAGE_STR).printStackTrace();
		return outImage;
	}

	//------------------------------------------------------------------

	public static void putClipboardText(
		String	text)
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

		CLIPBOARD_IS_UNAVAILABLE
		("The clipboard is currently unavailable."),

		FAILED_TO_GET_CLIPBOARD_DATA
		("Failed to get data from the clipboard."),

		NO_TEXT_ON_CLIPBOARD
		("There is no text on the clipboard."),

		NO_IMAGE_ON_CLIPBOARD
		("There is no image on the clipboard."),

		CANNOT_GET_IMAGE_DIMENSIONS
		("Cannot get the dimensions of the image.");

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
