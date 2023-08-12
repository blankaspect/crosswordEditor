/*====================================================================*\

AbstractBinaryFile.java

Abstract binary file class.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.common.misc;

//----------------------------------------------------------------------


// IMPORTS


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import java.nio.channels.OverlappingFileLockException;

import java.util.ArrayList;
import java.util.List;

import uk.blankaspect.common.exception.AppException;
import uk.blankaspect.common.exception.FileException;
import uk.blankaspect.common.exception.TaskCancelledException;
import uk.blankaspect.common.exception.TempFileException;

import uk.blankaspect.common.filesystem.FilenameUtils;

//----------------------------------------------------------------------


// ABSTRACT BINARY FILE CLASS


public abstract class AbstractBinaryFile
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

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

		FAILED_TO_OPEN_FILE
		("Failed to open the file."),

		FAILED_TO_CLOSE_FILE
		("Failed to close the file."),

		FAILED_TO_LOCK_FILE
		("Failed to lock the file."),

		ERROR_READING_FILE
		("An error occurred when reading the file."),

		ERROR_WRITING_FILE
		("An error occurred when writing the file."),

		FILE_ACCESS_NOT_PERMITTED
		("Access to the file was not permitted."),

		WRITING_NOT_PERMITTED
		("Writing to the file was not permitted."),

		READ_BEYOND_END_OF_FILE
		("An attempt was made to read beyond the end of the file."),

		NOT_A_FILE
		("The pathname does not denote a normal file."),

		FAILED_TO_CREATE_TEMPORARY_FILE
		("Failed to create a temporary file."),

		FAILED_TO_DELETE_FILE
		("Failed to delete the existing file."),

		FAILED_TO_RENAME_FILE
		("Failed to rename the temporary file to the specified filename."),

		FILE_IS_TOO_LONG
		("The file is too long to be read by this program."),

		NOT_ENOUGH_MEMORY
		("There was not enough memory to read the file.");

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

	/**
	 * @throws IllegalArgumentException
	 *           if {@code file} is {@code null}.
	 */

	protected AbstractBinaryFile(File file)
	{
		if (file == null)
			throw new IllegalArgumentException();
		this.file = file;
		progressListeners = new ArrayList<>();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Abstract methods
////////////////////////////////////////////////////////////////////////

	public abstract void writeData(OutputStream outStream)
		throws AppException;

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	public byte[] read()
		throws AppException
	{
		// Test file length
		if (file.length() > Integer.MAX_VALUE)
			throw new FileException(ErrorId.FILE_IS_TOO_LONG, file);
		int fileLength = (int)file.length();

		// Allocate buffer
		byte[] buffer = null;
		try
		{
			buffer = new byte[fileLength];
		}
		catch (OutOfMemoryError e)
		{
			throw new FileException(ErrorId.NOT_ENOUGH_MEMORY, file, e);
		}

		// Read file into buffer
		read(buffer, 0, buffer.length);

		// Return buffer
		return buffer;
	}

	//------------------------------------------------------------------

	/**
	 * @throws IllegalArgumentException
	 */

	public void read(byte[] buffer)
		throws AppException
	{
		read(buffer, 0, buffer.length);
	}

	//------------------------------------------------------------------

	/**
	 * @throws IllegalArgumentException
	 * @throws IndexOutOfBoundsException
	 */

	public void read(byte[] buffer,
					 int    offset,
					 int    length)
		throws AppException
	{
		// Validate arguments
		if (buffer == null)
			throw new IllegalArgumentException();
		if ((offset < 0) || (offset > buffer.length))
			throw new IndexOutOfBoundsException();
		if ((length < 0) || (length > buffer.length - offset))
			throw new IllegalArgumentException();

		// Read file
		FileInputStream inStream = null;
		try
		{
			// Open input stream on file
			try
			{
				inStream = new FileInputStream(file);
			}
			catch (FileNotFoundException e)
			{
				throw new FileException(ErrorId.FAILED_TO_OPEN_FILE, file, e);
			}
			catch (SecurityException e)
			{
				throw new FileException(ErrorId.FILE_ACCESS_NOT_PERMITTED, file);
			}

			// Lock file
			try
			{
				if (inStream.getChannel().tryLock(0, Long.MAX_VALUE, true) == null)
					throw new FileException(ErrorId.FAILED_TO_LOCK_FILE, file);
			}
			catch (OverlappingFileLockException e)
			{
				// ignore
			}
			catch (IOException e)
			{
				throw new FileException(ErrorId.FAILED_TO_LOCK_FILE, file, e);
			}

			// Read file
			try
			{
				int startOffset = offset;
				int endOffset = offset + length;
				while (offset < endOffset)
				{
					// Test whether task has been cancelled by a monitor
					for (IProgressListener listener : progressListeners)
					{
						if (listener.isTaskCancelled())
							throw new TaskCancelledException();
					}

					// Read from file
					int readLength = inStream.read(buffer, offset, endOffset - offset);
					if (readLength < 0)
						throw new FileException(ErrorId.READ_BEYOND_END_OF_FILE, file);

					// Increment offset
					offset += readLength;

					// Notify monitor of progress
					for (IProgressListener listener : progressListeners)
						listener.setProgress((double)(offset - startOffset) / (double)length);
				}
			}
			catch (IOException e)
			{
				throw new FileException(ErrorId.ERROR_READING_FILE, file, e);
			}

			// Close input stream
			try
			{
				FileInputStream tempInStream = inStream;
				inStream = null;
				tempInStream.close();
			}
			catch (IOException e)
			{
				throw new FileException(ErrorId.FAILED_TO_CLOSE_FILE, file, e);
			}
		}
		catch (AppException e)
		{
			// Close input stream
			try
			{
				if (inStream != null)
					inStream.close();
			}
			catch (IOException e1)
			{
				// ignore
			}

			// Rethrow exception
			throw e;
		}
	}

	//------------------------------------------------------------------

	/**
	 * @throws IllegalStateException
	 * @throws AppException
	 */

	public void write(FileWritingMode writeMode)
		throws AppException
	{
		// Test for file
		if (!isFile())
			throw new IllegalStateException();

		// Test whether file exists
		boolean exists = false;
		try
		{
			exists = file.exists();
		}
		catch (SecurityException e)
		{
			throw new FileException(ErrorId.FILE_ACCESS_NOT_PERMITTED, file, e);
		}

		// Write file
		if (exists)
		{
			switch (writeMode)
			{
				case DIRECT:
					writeDirect();
					break;

				case USE_TEMP_FILE:
					writeUsingTempFile(false);
					break;

				case USE_TEMP_FILE_PRESERVE_ATTRS:
					writeUsingTempFile(true);
					break;
			}
		}
		else
			writeDirect();
	}

	//------------------------------------------------------------------

	public void addProgressListener(IProgressListener listener)
	{
		progressListeners.add(listener);
	}

	//------------------------------------------------------------------

	public void removeProgressListener(IProgressListener listener)
	{
		progressListeners.remove(listener);
	}

	//------------------------------------------------------------------

	public IProgressListener[] getProgressListeners()
	{
		return progressListeners.toArray(IProgressListener[]::new);
	}

	//------------------------------------------------------------------

	protected boolean isFile()
	{
		return (file != null);
	}

	//------------------------------------------------------------------

	private void writeDirect()
		throws AppException
	{
		FileOutputStream outStream = null;
		try
		{
			// Test for write access
			if (file.exists() && !file.canWrite())
				throw new FileException(ErrorId.WRITING_NOT_PERMITTED, file);

			// Open output stream on file
			try
			{
				outStream = new FileOutputStream(file);
			}
			catch (FileNotFoundException e)
			{
				throw new FileException(ErrorId.FAILED_TO_OPEN_FILE, file, e);
			}
			catch (SecurityException e)
			{
				throw new FileException(ErrorId.FILE_ACCESS_NOT_PERMITTED, file, e);
			}

			// Lock file
			try
			{
				if (outStream.getChannel().tryLock() == null)
					throw new FileException(ErrorId.FAILED_TO_LOCK_FILE, file);
			}
			catch (IOException e)
			{
				throw new FileException(ErrorId.FAILED_TO_LOCK_FILE, file, e);
			}

			// Write data to output stream
			writeData(outStream);

			// Close output stream
			try
			{
				FileOutputStream tempOutStream = outStream;
				outStream = null;
				tempOutStream.close();
			}
			catch (IOException e)
			{
				throw new FileException(ErrorId.FAILED_TO_CLOSE_FILE, file);
			}
		}
		catch (AppException e)
		{
			// Close output stream
			if (outStream != null)
			{
				try
				{
					outStream.close();
				}
				catch (IOException e1)
				{
					// ignore
				}
			}

			// Rethrow exception
			throw e;
		}
	}

	//------------------------------------------------------------------

	private void writeUsingTempFile(boolean preserveAttrs)
		throws AppException
	{
		FileOutputStream outStream = null;
		File tempFile = null;
		boolean oldFileDeleted = false;
		try
		{
			// Test for file
			if (!file.isFile())
				throw new FileException(ErrorId.NOT_A_FILE, file);

			// Test for write access
			if (!file.canWrite())
				throw new FileException(ErrorId.WRITING_NOT_PERMITTED, file);

			// Create temporary file
			try
			{
				tempFile = FilenameUtils.tempLocation(file);
				tempFile.createNewFile();
			}
			catch (Exception e)
			{
				throw new AppException(ErrorId.FAILED_TO_CREATE_TEMPORARY_FILE, e);
			}

			// Open output stream on temporary file
			try
			{
				outStream = new FileOutputStream(tempFile);
			}
			catch (FileNotFoundException e)
			{
				throw new FileException(ErrorId.FAILED_TO_OPEN_FILE, tempFile, e);
			}
			catch (SecurityException e)
			{
				throw new FileException(ErrorId.FILE_ACCESS_NOT_PERMITTED, tempFile, e);
			}

			// Lock file
			try
			{
				if (outStream.getChannel().tryLock() == null)
					throw new FileException(ErrorId.FAILED_TO_LOCK_FILE, tempFile);
			}
			catch (IOException e)
			{
				throw new FileException(ErrorId.FAILED_TO_LOCK_FILE, tempFile, e);
			}

			// Write data to output stream
			writeData(outStream);

			// Close output stream
			try
			{
				FileOutputStream tempOutStream = outStream;
				outStream = null;
				tempOutStream.close();
			}
			catch (IOException e)
			{
				throw new FileException(ErrorId.FAILED_TO_CLOSE_FILE, tempFile);
			}

			// Copy file attributes from current file to temporary file
			AppException fileAttributesException = null;
			if (preserveAttrs)
			{
				try
				{
					FileAttributeUtils.copyAttributes(file, tempFile);
				}
				catch (FileAttributeUtils.AttributesException e)
				{
					fileAttributesException = e;
				}
			}

			// Delete existing file
			try
			{
				if (file.exists() && !file.delete())
					throw new FileException(ErrorId.FAILED_TO_DELETE_FILE, file);
				oldFileDeleted = true;
			}
			catch (SecurityException e)
			{
				throw new FileException(ErrorId.FAILED_TO_DELETE_FILE, file);
			}

			// Rename temporary file
			try
			{
				if (!tempFile.renameTo(file))
					throw new TempFileException(ErrorId.FAILED_TO_RENAME_FILE, file, tempFile);
			}
			catch (SecurityException e)
			{
				throw new TempFileException(ErrorId.FAILED_TO_RENAME_FILE, file, tempFile);
			}

			// Throw any exception from copying file attributes
			if (fileAttributesException != null)
				throw fileAttributesException;
		}
		catch (AppException e)
		{
			// Close output stream
			if (outStream != null)
			{
				try
				{
					outStream.close();
				}
				catch (IOException e1)
				{
					// ignore
				}
			}

			// Delete temporary file
			try
			{
				if (!oldFileDeleted && (tempFile != null) && tempFile.exists())
					tempFile.delete();
			}
			catch (Exception e1)
			{
				// ignore
			}

			// Rethrow exception
			throw e;
		}
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	protected	File					file;
	protected	List<IProgressListener>	progressListeners;

}

//----------------------------------------------------------------------
