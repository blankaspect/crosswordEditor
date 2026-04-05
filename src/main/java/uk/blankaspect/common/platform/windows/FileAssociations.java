/*====================================================================*\

FileAssociations.java

Class: Windows file associations.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.common.platform.windows;

//----------------------------------------------------------------------


// IMPORTS


import java.io.InputStreamReader;
import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import uk.blankaspect.common.exception2.BaseException;
import uk.blankaspect.common.exception2.FileException;
import uk.blankaspect.common.exception2.TaskCancelledException;
import uk.blankaspect.common.exception2.UnexpectedRuntimeException;

import uk.blankaspect.common.filesystem.FileSystemUtils;

import uk.blankaspect.common.misc.IProcessOutputWriter;
import uk.blankaspect.common.misc.SystemUtils;
import uk.blankaspect.common.misc.Task;

import uk.blankaspect.common.resource.ResourceUtils;

import uk.blankaspect.common.string.StringUtils;

//----------------------------------------------------------------------


// CLASS: WINDOWS FILE ASSOCIATIONS


public class FileAssociations
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	private static final	int		INDENT_INCREMENT	= 4;

	private static final	int		PROCESS_OUTPUT_BUFFER_LENGTH	= 512;

	private static final	int		NUM_FILE_DELETION_ATTEMPTS	= 3;

	private static final	String	COMMENT_PREFIX	= "#   ";

	private static final	String	EXTENSION_SEPARATOR	= ",";

	private static final	String	SCRIPT_FILENAME_EXTENSION	= ".ps1";

	private static final	String	SCRIPT_TEMPLATE_FILENAME	= "fileAssociation" + SCRIPT_FILENAME_EXTENSION;

	private static final	List<String>	SCRIPT_COMMAND_ARGS	= List.of
	(
		"powershell",
		"-ExecutionPolicy",
		"RemoteSigned",
		"-File"
	);
	private static final	String	SCRIPT_ARG_REMOVE	= "-remove";

	private static final	String	WRITING_STR		= "Writing ";
	private static final	String	EXECUTING_STR	= "Executing ";
	private static final	String	DELETING_STR	= "Deleting ";
	private static final	String	SUCCESS_STR		= "The operation was completed successfully.\n";
	private static final	String	ERROR_STR		= "! ERROR !\n";

	/** Error messages. */
	private interface ErrorMsg
	{
		String	FAILED_TO_CREATE_TEMPORARY_DIRECTORY =
				"Failed to create a temporary directory.";

		String	ERROR_WRITING_SCRIPT_FILE =
				"An error occurred when writing the script file.";

		String	FAILED_TO_EXECUTE_SCRIPT =
				"Failed to execute the script.";

		String	ERROR_EXECUTING_SCRIPT =
				"An error occurred when executing the script.";

		String	FAILED_TO_DELETE_SCRIPT_FILE =
				"Failed to delete the script file.";

		String	FAILED_TO_DELETE_TEMPORARY_DIRECTORY =
				"Failed to delete the temporary directory.";
	}

////////////////////////////////////////////////////////////////////////
//  Class variables
////////////////////////////////////////////////////////////////////////

	private static	List<Path>	locationsForDeletion	= new ArrayList<>();

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private	List<Map<FileKindParam, String>>	fileKindParams;

////////////////////////////////////////////////////////////////////////
//  Static initialiser
////////////////////////////////////////////////////////////////////////

	static
	{
		// Add shutdown hook to delete temporary locations that weren't deleted in the stop() method
		Runtime.getRuntime().addShutdownHook(new Thread(() ->
		{
			for (int i = locationsForDeletion.size() - 1; i >= 0; i--)
			{
				try
				{
					FileSystemUtils.deleteWithRetries(locationsForDeletion.get(i), NUM_FILE_DELETION_ATTEMPTS);
				}
				catch (Throwable e)
				{
					e.printStackTrace();
				}
			}
		}));
	}

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	public FileAssociations()
	{
		fileKindParams = new ArrayList<>();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	private static String escapeDoubleQuotes(
		String	str)
	{
		return str.replace("\"", "\"\"");
	}

	//------------------------------------------------------------------

	private static void executeProcess(
		List<String>			arguments,
		IProcessOutputWriter	outWriter)
		throws BaseException
	{
		// Start process
		Process process0 = null;
		try
		{
			process0 = new ProcessBuilder(arguments).redirectErrorStream(true).start();
		}
		catch (Exception e)
		{
			throw new BaseException(ErrorMsg.FAILED_TO_EXECUTE_SCRIPT, e);
		}
		Process process = process0;

		// Create thread to handle output from process
		Thread outputThread = new Thread(() ->
		{
			InputStreamReader reader = new InputStreamReader(process.getInputStream());
			char[] buffer = new char[PROCESS_OUTPUT_BUFFER_LENGTH];
			while (!Thread.currentThread().isInterrupted())
			{
				// Display output from process
				try
				{
					while (reader.ready())
					{
						int length = reader.read(buffer, 0, buffer.length);
						if ((length > 0) && (outWriter != null))
							outWriter.write(new String(buffer, 0, length));
					}
				}
				catch (IOException e)
				{
					// ignore
				}
			}
		});
		outputThread.start();

		// Wait for process to terminate
		try
		{
			while (true)
			{
				// Test whether task has been cancelled
				if (Task.isCancelled())
				{
					process.destroy();
					throw new TaskCancelledException();
				}

				// Allow process to execute
				try
				{
					Thread.sleep(100);
				}
				catch (InterruptedException e)
				{
					// ignore
				}

				// Test whether process has terminated
				try
				{
					int exitValue = process.exitValue();
					if (exitValue != 0)
						throw new BaseException(ErrorMsg.ERROR_EXECUTING_SCRIPT);
					break;
				}
				catch (IllegalThreadStateException e)
				{
					// ignore
				}
			}
		}
		finally
		{
			// Stop output thread
			outputThread.interrupt();

			// Wait for output thread to finish
			while (outputThread.isAlive())
			{
				try
				{
					Thread.sleep(50);
				}
				catch (InterruptedException e)
				{
					// ignore
				}
			}
		}
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	public void addParams(
		String		fileKindKey,
		String		fileKindText,
		String		fileOpenText,
		String...	extensions)
	{
		addParams(fileKindKey, fileKindText, fileOpenText, List.of(extensions));
	}

	//------------------------------------------------------------------

	public void addParams(
		String				fileKindKey,
		String				fileKindText,
		String				fileOpenText,
		Iterable<String>	extensions)
	{
		Map<FileKindParam, String> map = new EnumMap<>(FileKindParam.class);
		map.put(FileKindParam.FILE_KIND_KEY,  fileKindKey);
		map.put(FileKindParam.FILE_KIND_TEXT, fileKindText);
		map.put(FileKindParam.FILE_OPEN_TEXT, fileOpenText);
		map.put(FileKindParam.EXTENSIONS,     String.join(EXTENSION_SEPARATOR, extensions));
		fileKindParams.add(map);
	}

	//------------------------------------------------------------------

	public String createScript(
		String	appName,
		String	javaLauncherPathname,
		String	jarPathname,
		String	iconPathname)
	{
		// Read script template from resource
		String template = null;
		try
		{
			template = ResourceUtils.readText(getClass(), SCRIPT_TEMPLATE_FILENAME);
		}
		catch (IOException e)
		{
			throw new UnexpectedRuntimeException(e);
		}

		// Create text of filename extensions
		StringBuilder buffer = new StringBuilder(1024);
		for (Map<FileKindParam, String> params : fileKindParams)
		{
			for (String ext : params.get(FileKindParam.EXTENSIONS).split(EXTENSION_SEPARATOR))
				buffer.append(COMMENT_PREFIX).append(ext).append('\n');
		}
		String extensions = buffer.toString();

		// Create suffix for quantity of filename extensions
		String quantitySuffix = (fileKindParams.size() == 1) ? "" : "s";

		// Create script fragment for parameters
		buffer.setLength(0);
		Iterator<Map<FileKindParam, String>> it = fileKindParams.iterator();
		while (it.hasNext())
		{
			buffer.append(" ".repeat(INDENT_INCREMENT)).append("@{").append('\n');
			Map<FileKindParam, String> params = it.next();
			for (FileKindParam param : FileKindParam.values())
			{
				buffer.append(" ".repeat(2 * INDENT_INCREMENT));
				buffer.append(StringUtils.padAfter(param.key, FileKindParam.MAX_KEY_LENGTH));
				buffer.append(" = \"").append(escapeDoubleQuotes(params.get(param))).append("\"").append('\n');
			}
			buffer.append(" ".repeat(INDENT_INCREMENT)).append('}');
			if (it.hasNext())
				buffer.append(',');
			buffer.append('\n');
		}
		String paramsFragment = buffer.toString();

		// Replace placeholders in template and return result
		return String.format(template, appName, quantitySuffix, extensions, quantitySuffix,
							 javaLauncherPathname, jarPathname, iconPathname, paramsFragment);
	}

	//------------------------------------------------------------------

	public Path executeScript(
		String					appName,
		String					javaLauncherPathname,
		String					jarPathname,
		String					iconPathname,
		String					tempDirectoryPrefix,
		String					scriptFilename,
		boolean					removeEntries,
		ScriptLifeCycle			scriptLifeCycle,
		IProcessOutputWriter	outWriter)
		throws BaseException
	{
		Path tempDirectory = null;
		Path scriptFile = null;
		try
		{
			// Get pathname of system temporary directory
			Path sysTempDirectory = SystemUtils.tempDirectory();

			// Find available subdirectory of system temporary directory
			tempDirectory = FileSystemUtils.findAvailableLocationDateTime(sysTempDirectory, tempDirectoryPrefix, "", 0);

			// Create temporary directory
			try
			{
				Files.createDirectory(tempDirectory);
			}
			catch (Exception e)
			{
				throw new BaseException(ErrorMsg.FAILED_TO_CREATE_TEMPORARY_DIRECTORY, e);
			}

			// Get pathname of script file
			if (!scriptFilename.endsWith(SCRIPT_FILENAME_EXTENSION))
				scriptFilename += SCRIPT_FILENAME_EXTENSION;
			scriptFile = tempDirectory.resolve(scriptFilename);

			// Delete script file and temporary directory when the program terminates
			if (scriptLifeCycle == ScriptLifeCycle.WRITE_EXECUTE_DELETE)
			{
				locationsForDeletion.add(scriptFile);
				locationsForDeletion.add(tempDirectory);
			}

			// Write status message
			if (outWriter != null)
				outWriter.write(WRITING_STR + scriptFile.toString() + "\n");

			// Write script file to temporary directory
			try
			{
				Files.writeString(scriptFile, createScript(appName, javaLauncherPathname, jarPathname, iconPathname));
			}
			catch (IOException e)
			{
				throw new FileException(ErrorMsg.ERROR_WRITING_SCRIPT_FILE, scriptFile);
			}

			// Execute script
			if (scriptLifeCycle != ScriptLifeCycle.WRITE)
			{
				// Write status message
				if (outWriter != null)
					outWriter.write(EXECUTING_STR + scriptFilename + "\n");

				// Execute script
				List<String> arguments = new ArrayList<>();
				arguments.addAll(SCRIPT_COMMAND_ARGS);
				arguments.add(scriptFile.toString());
				if (removeEntries)
					arguments.add(SCRIPT_ARG_REMOVE);
				executeProcess(arguments, outWriter);
			}

			// Delete script file and temporary directory
			if (scriptLifeCycle == ScriptLifeCycle.WRITE_EXECUTE_DELETE)
			{
				// Write status message
				if (outWriter != null)
					outWriter.write(DELETING_STR + scriptFilename + "\n");

				// Delete script file
				try
				{
					FileSystemUtils.deleteWithRetries(scriptFile, NUM_FILE_DELETION_ATTEMPTS);
				}
				catch (IOException e)
				{
					throw new FileException(ErrorMsg.FAILED_TO_DELETE_SCRIPT_FILE, scriptFile);
				}

				// Delete temporary directory
				try
				{
					FileSystemUtils.deleteWithRetries(tempDirectory, NUM_FILE_DELETION_ATTEMPTS);
				}
				catch (IOException e)
				{
					throw new FileException(ErrorMsg.FAILED_TO_DELETE_TEMPORARY_DIRECTORY, tempDirectory);
				}
			}

			// Report success
			if (outWriter != null)
				outWriter.write(SUCCESS_STR);
		}
		catch (BaseException e)
		{
			// Delete script file and temporary directory
			if (scriptLifeCycle == ScriptLifeCycle.WRITE_EXECUTE_DELETE)
			{
				try
				{
					FileSystemUtils.deleteWithRetries(scriptFile, NUM_FILE_DELETION_ATTEMPTS);
					FileSystemUtils.deleteWithRetries(tempDirectory, NUM_FILE_DELETION_ATTEMPTS);
				}
				catch (IOException e0)
				{
					// ignore
				}
			}

			// Rethrow exception
			if (outWriter == null)
				throw e;

			// Write error message to output
			if (!(e instanceof TaskCancelledException))
			{
				outWriter.write(ERROR_STR);
				outWriter.write(e.toString());
			}
		}
		finally
		{
			// Close output writer and wait for it to close
			if (outWriter != null)
			{
				outWriter.close();
				while (outWriter.isOpen())
				{
					try
					{
						Thread.sleep(50);
					}
					catch (InterruptedException e)
					{
						// ignore
					}
				}
			}
		}

		// Return location of script file
		return (scriptLifeCycle == ScriptLifeCycle.WRITE_EXECUTE_DELETE) ? null : scriptFile;
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Enumerated types
////////////////////////////////////////////////////////////////////////


	// ENUMERATION: SCRIPT LIFE CYCLE


	public enum ScriptLifeCycle
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		WRITE
		(
			"Write"
		),

		WRITE_EXECUTE
		(
			"Write and execute"
		),

		WRITE_EXECUTE_DELETE
		(
			"Write, execute and delete"
		);

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	String	text;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private ScriptLifeCycle(
			String	text)
		{
			this.text = text;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		public String toString()
		{
			return text;
		}

		//--------------------------------------------------------------

	}


	//==================================================================


	// ENUMERATION: FILE-KIND PARAMETERS


	private enum FileKindParam
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		FILE_KIND_KEY
		(
			"fileKindKey"
		),

		FILE_KIND_TEXT
		(
			"fileKindText"
		),

		FILE_OPEN_TEXT
		(
			"fileOpenText"
		),

		EXTENSIONS
		(
			"extensions"
		);

		//--------------------------------------------------------------

		private static final	int	MAX_KEY_LENGTH;

	////////////////////////////////////////////////////////////////////
	//  Static initialiser
	////////////////////////////////////////////////////////////////////

		static
		{
			MAX_KEY_LENGTH = Arrays.stream(values()).mapToInt(value -> value.key.length()).max().orElse(0);
		}

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	String	key;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private FileKindParam(
			String	key)
		{
			this.key = key;
		}

		//--------------------------------------------------------------

	}

	//==================================================================

}

//----------------------------------------------------------------------
