/*====================================================================*\

Task.java

Task class.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.crosswordeditor;

//----------------------------------------------------------------------


// IMPORTS


import java.io.File;

import uk.blankaspect.common.exception.AppException;
import uk.blankaspect.common.exception.TaskCancelledException;

import uk.blankaspect.common.platform.windows.FileAssociations;

//----------------------------------------------------------------------


// TASK CLASS


abstract class Task
	extends uk.blankaspect.common.misc.Task
{

////////////////////////////////////////////////////////////////////////
//  Member classes : non-inner classes
////////////////////////////////////////////////////////////////////////


	// 'READ DOCUMENT' TASK CLASS


	public static class ReadDocument
		extends Task
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		public ReadDocument(CrosswordDocument document,
							File              file)
		{
			this.document = document;
			this.file = file;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : Runnable interface
	////////////////////////////////////////////////////////////////////

		public void run()
		{
			// Perform task
			try
			{
				document.read(file);
			}
			catch (TaskCancelledException e)
			{
				// ignore
			}
			catch (AppException e)
			{
				setException(e, false);
			}

			// Remove thread
			removeThread();
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	CrosswordDocument	document;
		private	File				file;

	}

	//==================================================================


	// 'WRITE DOCUMENT' TASK CLASS


	public static class WriteDocument
		extends Task
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		public WriteDocument(CrosswordDocument document,
							 File              file)
		{
			this.document = document;
			this.file = file;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : Runnable interface
	////////////////////////////////////////////////////////////////////

		public void run()
		{
			// Perform task
			try
			{
				document.write(file);
			}
			catch (AppException e)
			{
				setException(e, false);
			}

			// Remove thread
			removeThread();
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	CrosswordDocument	document;
		private	File				file;

	}

	//==================================================================


	// 'EXPORT HTML' TASK CLASS


	public static class ExportDocumentAsHtml
		extends Task
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		public ExportDocumentAsHtml(CrosswordDocument                 document,
									File                              file,
									StylesheetKind                    stylesheetKind,
									CrosswordDocument.StyleProperties styleProperties,
									boolean                           writeStylesheet,
									boolean                           writeBlockImage,
									boolean                           writeEntries)
		{
			this.document = document;
			this.file = file;
			this.stylesheetKind = stylesheetKind;
			this.styleProperties = styleProperties;
			this.writeStylesheet = writeStylesheet;
			this.writeBlockImage = writeBlockImage;
			this.writeEntries = writeEntries;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : Runnable interface
	////////////////////////////////////////////////////////////////////

		public void run()
		{
			// Perform task
			try
			{
				document.exportHtml(file, stylesheetKind, styleProperties, writeStylesheet, writeBlockImage,
									writeEntries);
			}
			catch (AppException e)
			{
				setException(e, false);
			}

			// Remove thread
			removeThread();
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	CrosswordDocument					document;
		private	File								file;
		private	StylesheetKind						stylesheetKind;
		private	CrosswordDocument.StyleProperties	styleProperties;
		private	boolean								writeStylesheet;
		private	boolean								writeBlockImage;
		private	boolean								writeEntries;

	}

	//==================================================================


	// 'LOAD SOLUTION' TASK CLASS


	public static class LoadSolution
		extends Task
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		public LoadSolution(CrosswordDocument document)
		{
			this.document = document;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : Runnable interface
	////////////////////////////////////////////////////////////////////

		public void run()
		{
			// Perform task
			try
			{
				document.loadSolution();
			}
			catch (AppException e)
			{
				setException(e, false);
			}

			// Remove thread
			removeThread();
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	CrosswordDocument	document;

	}

	//==================================================================


	// 'SET FILE ASSOCIATION' TASK CLASS


	public static class SetFileAssociation
		extends Task
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		public SetFileAssociation(FileAssociations                 fileAssociations,
								  String                           javaLauncherPathname,
								  String                           jarPathname,
								  String                           iconPathname,
								  String                           tempDirectoryPrefix,
								  String                           scriptFilename,
								  boolean                          removeEntries,
								  FileAssociations.ScriptLifeCycle scriptLifeCycle)
		{
			this.fileAssociations = fileAssociations;
			this.javaLauncherPathname = javaLauncherPathname;
			this.jarPathname = jarPathname;
			this.iconPathname = iconPathname;
			this.tempDirectoryPrefix = tempDirectoryPrefix;
			this.scriptFilename = scriptFilename;
			this.removeEntries = removeEntries;
			this.scriptLifeCycle = scriptLifeCycle;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : Runnable interface
	////////////////////////////////////////////////////////////////////

		public void run()
		{
			// Perform task
			try
			{
				fileAssociations.executeScript(App.SHORT_NAME, javaLauncherPathname, jarPathname,
											   iconPathname, tempDirectoryPrefix, scriptFilename,
											   removeEntries, scriptLifeCycle,
											   ((TextOutputTaskDialog)getProgressView()).getWriter());
			}
			catch (TaskCancelledException e)
			{
				// ignore
			}
			catch (AppException e)
			{
				setException(e, false);
			}

			// Remove thread
			removeThread();
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	FileAssociations					fileAssociations;
		private	String								javaLauncherPathname;
		private	String								jarPathname;
		private	String								iconPathname;
		private	String								tempDirectoryPrefix;
		private	String								scriptFilename;
		private	boolean								removeEntries;
		private	FileAssociations.ScriptLifeCycle	scriptLifeCycle;

	}

	//==================================================================


	// 'WRITE CONFIGURATION' TASK CLASS


	public static class WriteConfig
		extends Task
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		public WriteConfig(File file)
		{
			this.file = file;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : Runnable interface
	////////////////////////////////////////////////////////////////////

		public void run()
		{
			// Perform task
			try
			{
				AppConfig.INSTANCE.write(file);
			}
			catch (AppException e)
			{
				setException(e, false);
			}

			// Remove thread
			removeThread();
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	File	file;

	}

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	private Task()
	{
	}

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------
