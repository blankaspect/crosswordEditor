/*====================================================================*\

PngOutputFile.java

PNG image output file class.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.ui.swing.image;

//----------------------------------------------------------------------


// IMPORTS


import java.awt.image.RenderedImage;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import javax.imageio.ImageIO;

import uk.blankaspect.common.exception.AppException;
import uk.blankaspect.common.exception.FileException;
import uk.blankaspect.common.exception.TaskCancelledException;

import uk.blankaspect.common.misc.AbstractBinaryFile;
import uk.blankaspect.common.misc.FileWritingMode;
import uk.blankaspect.common.misc.IProgressListener;

//----------------------------------------------------------------------


// PNG IMAGE OUTPUT FILE CLASS


public class PngOutputFile
	extends AbstractBinaryFile
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	private static final	String	PNG_STR	= "png";

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

		ERROR_WRITING_FILE
		("An error occurred when writing the file."),

		PNG_OUTPUT_NOT_SUPPORTED
		("This implementation of Java does not support the writing of PNG files.");

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
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	String	message;

	}

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	public PngOutputFile(File          file,
						 RenderedImage image)
	{
		super(file);
		this.image = image;
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	public static boolean canWrite()
	{
		for (String name : ImageIO.getWriterFormatNames())
		{
			if (PNG_STR.equals(name))
				return true;
		}
		return false;
	}

	//------------------------------------------------------------------

	public static void write(File          file,
							 RenderedImage image)
		throws AppException
	{
		new PngOutputFile(file, image).write(FileWritingMode.DIRECT);
	}

	//------------------------------------------------------------------

	public static void write(File            file,
							 RenderedImage   image,
							 FileWritingMode writeMode)
		throws AppException
	{
		new PngOutputFile(file, image).write(writeMode);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : overriding methods
////////////////////////////////////////////////////////////////////////

	@Override
	public void writeData(OutputStream outStream)
		throws AppException
	{
		// Test whether task has been cancelled by a monitor
		for (IProgressListener listener : progressListeners)
		{
			if (listener.isTaskCancelled())
				throw new TaskCancelledException();
		}

		// Write data to output stream
		try
		{
			if (!ImageIO.write(image, PNG_STR, outStream))
				throw new AppException(ErrorId.PNG_OUTPUT_NOT_SUPPORTED);
		}
		catch (IOException e)
		{
			throw new FileException(ErrorId.ERROR_WRITING_FILE, file, e);
		}

		// Notify monitor of progress
		for (IProgressListener listener : progressListeners)
			listener.setProgress(1.0);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private	RenderedImage	image;

}

//----------------------------------------------------------------------
