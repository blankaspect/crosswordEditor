/*====================================================================*\

CrosswordEditorApp.java

Class: application.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.crosswordeditor;

//----------------------------------------------------------------------


// IMPORTS


import java.awt.Point;

import java.io.File;
import java.io.IOException;

import java.lang.invoke.MethodHandles;

import java.util.ArrayList;
import java.util.List;

import java.util.stream.Stream;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;

import uk.blankaspect.common.build.BuildUtils;

import uk.blankaspect.common.cls.ClassUtils;

import uk.blankaspect.common.config.PortNumber;

import uk.blankaspect.common.exception.AppException;
import uk.blankaspect.common.exception.ExceptionUtils;

import uk.blankaspect.common.exception2.LocationException;

import uk.blankaspect.common.filesystem.PathnameUtils;

import uk.blankaspect.common.logging.ErrorLogger;

import uk.blankaspect.common.misc.DataTxChannel;
import uk.blankaspect.common.misc.FilenameSuffixFilter;

import uk.blankaspect.common.platform.windows.FileAssociations;

import uk.blankaspect.common.resource.ResourceProperties;
import uk.blankaspect.common.resource.ResourceUtils;

import uk.blankaspect.common.string.StringUtils;

import uk.blankaspect.ui.swing.dialog.QuestionDialog;

import uk.blankaspect.ui.swing.misc.GuiUtils;

import uk.blankaspect.ui.swing.text.TextRendering;

import uk.blankaspect.ui.swing.textfield.TextFieldUtils;

//----------------------------------------------------------------------


// CLASS: APPLICATION


public class CrosswordEditorApp
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	public static final		CrosswordEditorApp	INSTANCE	= new CrosswordEditorApp();

	public static final		String	SHORT_NAME	= "CrosswordEditor";
	public static final		String	LONG_NAME	= "Crossword editor";
	public static final		String	NAME_KEY	= StringUtils.firstCharToLowerCase(SHORT_NAME);

	public static final		int		MAX_NUM_DOCUMENTS	= 64;

	private static final	int		FILE_CHECK_TIMER_INTERVAL	= 500;

	private static final	String	BUILD_PROPERTIES_FILENAME	= "build.properties";

	private static final	String	VIEW_KEY		= "view";
	private static final	String	DO_NOT_VIEW_KEY	= "doNotView";
	private static final	String	OS_NAME_KEY		= "os.name";

	private static final	String	RX_ID	= MethodHandles.lookup().lookupClass().getCanonicalName();

	private static final	String	ASSOC_FILE_KIND_KEY		= "BlankAspect." + SHORT_NAME + ".document";
	private static final	String	ASSOC_FILE_KIND_TEXT	= "CrosswordEditor document";
	private static final	String	ASSOC_FILE_OPEN_TEXT	= "&Open with CrosswordEditor";
	private static final	String	ASSOC_SCRIPT_DIR_PREFIX	= NAME_KEY + "_";
	private static final	String	ASSOC_SCRIPT_FILENAME	= NAME_KEY + "Associations";

	private static final	String	CONFIG_ERROR_STR		= "Configuration error";
	private static final	String	LAF_ERROR1_STR			= "Look-and-feel: ";
	private static final	String	LAF_ERROR2_STR			= "\nThe look-and-feel is not installed.";
	private static final	String	OPEN_FILE_STR			= "Open file";
	private static final	String	REVERT_FILE_STR			= "Revert file";
	private static final	String	SAVE_FILE_STR			= "Save file";
	private static final	String	SAVE_FILE_AS_STR		= "Save file as";
	private static final	String	SAVE_CLOSE_FILE_STR		= "Save file before closing";
	private static final	String	EXPORT_AS_HTML_STR		= "Export document as HTML";
	private static final	String	MODIFIED_FILE_STR		= "Modified file";
	private static final	String	READ_FILE_STR			= "Read file";
	private static final	String	WRITE_FILE_STR			= "Write file";
	private static final	String	NEW_CROSSWORD_STR		= "New crossword";
	private static final	String	REVERT_STR				= "Revert";
	private static final	String	SAVE_STR				= "Save";
	private static final	String	DISCARD_STR				= "Discard";
	private static final	String	REVERT_MESSAGE_STR		=
			"\nDo you want discard the changes to the current document and reopen the original file?";
	private static final	String	MODIFIED_MESSAGE_STR	=
			"\nThe file has been modified externally.\nDo you want to open the modified file?";
	private static final	String	UNNAMED_FILE_STR		= "The unnamed file";
	private static final	String	CHANGED_MESSAGE1_STR	= "\nThe file";
	private static final	String	CHANGED_MESSAGE2_STR	= " has changed.\nDo you want to save the changed file?";
	private static final	String	VIEW_FILE_STR			= "View file";
	private static final	String	DO_NOT_VIEW_FILE_STR	= "Don't view file";
	private static final	String	VIEW_HTML_FILE_STR		=
			"Do you want to view the HTML file in an external browser?";
	private static final	String	DO_NOT_SHOW_AGAIN_STR	= "Do not show this dialog again";
	private static final	String	WINDOWS_STR				= "Windows";
	private static final	String	FILE_ASSOCIATION_STR	= "File association";

	private static final	QuestionDialog.Option[]	VIEW_FILE_OPTIONS	=
	{
		new QuestionDialog.Option(VIEW_KEY,                  VIEW_FILE_STR),
		new QuestionDialog.Option(DO_NOT_VIEW_KEY,           DO_NOT_VIEW_FILE_STR),
		new QuestionDialog.Option(QuestionDialog.CANCEL_KEY, AppConstants.CANCEL_STR)
	};

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private	ResourceProperties	buildProperties;
	private	String				versionStr;
	private	MainWindow			mainWindow;
	private	List<DocumentView>	documentsViews;
	private	JFileChooser		openFileChooser;
	private	JFileChooser		saveFileChooser;
	private	JFileChooser		exportHtmlFileChooser;
	private	int					newFileIndex;
	private	boolean				showViewHtmlFileMessage;
	private	boolean				viewHtmlFile;
	private	boolean				exiting;
	private	boolean				executingCommand;
	private	List<File>			pendingFiles;

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	private CrosswordEditorApp()
	{
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	public static void main(
		String[]	args)
	{
		INSTANCE.init(args);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	public String getVersionString()
	{
		return versionStr;
	}

	//------------------------------------------------------------------

	public MainWindow getMainWindow()
	{
		return mainWindow;
	}

	//------------------------------------------------------------------

	public int getNumDocuments()
	{
		return documentsViews.size();
	}

	//------------------------------------------------------------------

	public boolean hasDocuments()
	{
		return !documentsViews.isEmpty();
	}

	//------------------------------------------------------------------

	public boolean isDocumentsFull()
	{
		return (documentsViews.size() >= MAX_NUM_DOCUMENTS);
	}

	//------------------------------------------------------------------

	public CrosswordDocument getDocument()
	{
		return (hasDocuments() && (mainWindow != null)) ? getDocument(mainWindow.getTabIndex()) : null;
	}

	//------------------------------------------------------------------

	public CrosswordDocument getDocument(
		int	index)
	{
		return hasDocuments() ? documentsViews.get(index).document : null;
	}

	//------------------------------------------------------------------

	public CrosswordView getView()
	{
		return (hasDocuments() && (mainWindow != null)) ? getView(mainWindow.getTabIndex()) : null;
	}

	//------------------------------------------------------------------

	public CrosswordView getView(
		int	index)
	{
		return (hasDocuments() ? documentsViews.get(index).view : null);
	}

	//------------------------------------------------------------------

	public CrosswordView getView(
		CrosswordDocument	document)
	{
		for (DocumentView documentView : documentsViews)
		{
			if (documentView.document == document)
				return documentView.view;
		}
		return null;
	}

	//------------------------------------------------------------------

	public void showInfoMessage(
		String	title,
		Object	message)
	{
		showMessageDialog(title, message, JOptionPane.INFORMATION_MESSAGE);
	}

	//------------------------------------------------------------------

	public void showWarningMessage(
		String	title,
		Object	message)
	{
		showMessageDialog(title, message, JOptionPane.WARNING_MESSAGE);
	}

	//------------------------------------------------------------------

	public void showErrorMessage(
		String	title,
		Object	message)
	{
		showMessageDialog(title, message, JOptionPane.ERROR_MESSAGE);
	}

	//------------------------------------------------------------------

	public void showMessageDialog(
		String	title,
		Object	message,
		int		messageKind)
	{
		JOptionPane.showMessageDialog(mainWindow, message, title, messageKind);
	}

	//------------------------------------------------------------------

	public void updateTabText(
		CrosswordDocument	document)
	{
		for (int i = 0; i < getNumDocuments(); i++)
		{
			if (getDocument(i) == document)
			{
				mainWindow.setTabText(i, document.getTitleString(false), document.getTitleString(true));
				break;
			}
		}
	}

	//------------------------------------------------------------------

	public void updateCommands()
	{
		CrosswordDocument document = getDocument();
		boolean isDocument = (document != null);
		boolean notFull = !isDocumentsFull();
		boolean isWindows = System.getProperty(OS_NAME_KEY, "").contains(WINDOWS_STR);

		AppCommand.CHECK_MODIFIED_FILE.setEnabled(true);
		AppCommand.IMPORT_FILES.setEnabled(true);
		AppCommand.CREATE_DOCUMENT.setEnabled(notFull);
		AppCommand.OPEN_DOCUMENT.setEnabled(notFull);
		AppCommand.REVERT_DOCUMENT.setEnabled(isDocument && document.isChanged() &&
											   (document.getFile() != null));
		AppCommand.CLOSE_DOCUMENT.setEnabled(isDocument);
		AppCommand.CLOSE_ALL_DOCUMENTS.setEnabled(isDocument);
		AppCommand.SAVE_DOCUMENT.setEnabled(isDocument && document.isChanged());
		AppCommand.SAVE_DOCUMENT_AS.setEnabled(isDocument);
		AppCommand.EXPORT_HTML_FILE.setEnabled(isDocument);
		AppCommand.EXIT.setEnabled(true);
		AppCommand.CAPTURE_CROSSWORD.setEnabled(notFull);
		AppCommand.CREATE_SOLUTION_DOCUMENT.setEnabled(notFull && isDocument &&
														document.getGrid().hasSolution());
		AppCommand.TOGGLE_SHOW_FULL_PATHNAMES.setEnabled(true);
		AppCommand.TOGGLE_SHOW_FULL_PATHNAMES.
											setSelected(AppConfig.INSTANCE.isShowFullPathnames());
		AppCommand.MANAGE_FILE_ASSOCIATION.setEnabled(isWindows);
		AppCommand.EDIT_PREFERENCES.setEnabled(true);
	}

	//------------------------------------------------------------------

	public void executeCommand(
		AppCommand	command)
	{
		if (!executingCommand)
		{
			// Prevent another command until current command is finished
			executingCommand = true;

			// Execute command
			try
			{
				switch (command)
				{
					case CHECK_MODIFIED_FILE:
						onCheckModifiedFile();
						break;

					case IMPORT_FILES:
						onImportFiles();
						break;

					case CREATE_DOCUMENT:
						onCreateDocument();
						break;

					case OPEN_DOCUMENT:
						onOpenDocument();
						break;

					case REVERT_DOCUMENT:
						onRevertDocument();
						break;

					case CLOSE_DOCUMENT:
						onCloseDocument();
						break;

					case CLOSE_ALL_DOCUMENTS:
						onCloseAllDocuments();
						break;

					case SAVE_DOCUMENT:
						onSaveDocument();
						break;

					case SAVE_DOCUMENT_AS:
						onSaveDocumentAs();
						break;

					case EXPORT_HTML_FILE:
						onExportHtmlFile();
						break;

					case EXIT:
						onExit();
						break;

					case CAPTURE_CROSSWORD:
						onCaptureCrossword();
						break;

					case CREATE_SOLUTION_DOCUMENT:
						onCreateSolutionDocument();
						break;

					case TOGGLE_SHOW_FULL_PATHNAMES:
						onToggleShowFullPathnames();
						break;

					case MANAGE_FILE_ASSOCIATION:
						onManageFileAssociation();
						break;

					case EDIT_PREFERENCES:
						onEditPreferences();
						break;
				}
			}
			catch (AppException e)
			{
				showErrorMessage(SHORT_NAME, e);
			}

			// Update main window
			if (command != AppCommand.CHECK_MODIFIED_FILE)
			{
				updateTabText(getDocument());
				mainWindow.updateAll();
			}

			// Open pending files
			if (!pendingFiles.isEmpty())
			{
				openFiles(pendingFiles);
				pendingFiles.clear();
				mainWindow.updateAll();
			}

			// Allow another command
			executingCommand = false;
		}
	}

	//------------------------------------------------------------------

	public void closeDocument(
		int	index)
	{
		if (confirmCloseDocument(index))
			removeDocument(index);
	}

	//------------------------------------------------------------------

	private void addDocument(
		CrosswordDocument	document)
	{
		DocumentView documentView = new DocumentView(document);
		documentsViews.add(documentView);
		mainWindow.addView(document.getTitleString(false), document.getTitleString(true), documentView.view);
	}

	//------------------------------------------------------------------

	private void removeDocument(
		int	index)
	{
		documentsViews.remove(index);
		mainWindow.removeView(index);
	}

	//------------------------------------------------------------------

	private CrosswordDocument readDocument(
		File	file)
		throws AppException
	{
		CrosswordDocument document = new CrosswordDocument();
		TaskProgressDialog.showDialog(mainWindow, READ_FILE_STR, new Task.ReadDocument(document, file));
		document.validateClues();
		return ((document.getFile() == null) ? null : document);
	}

	//------------------------------------------------------------------

	private void writeDocument(
		CrosswordDocument	document,
		File				file)
		throws AppException
	{
		TaskProgressDialog.showDialog(mainWindow, WRITE_FILE_STR, new Task.WriteDocument(document, file));
	}

	//------------------------------------------------------------------

	private void openDocument(
		File	file)
		throws AppException
	{
		// Test whether document is already open
		for (int i = 0; i < documentsViews.size(); i++)
		{
			if (Utils.isSameFile(file, getDocument(i).getFile()))
			{
				mainWindow.selectView(i);
				return;
			}
		}

		// Read document and add it to list
		CrosswordDocument document = readDocument(file);
		if (document != null)
			addDocument(document);
	}

	//------------------------------------------------------------------

	private void revertDocument(
		File	file)
		throws AppException
	{
		// Read document
		CrosswordDocument document = readDocument(file);

		// Replace document in list
		if (document != null)
		{
			int index = mainWindow.getTabIndex();
			documentsViews.set(index, new DocumentView(document));
			mainWindow.setTabText(index, document.getTitleString(false), document.getTitleString(true));
			mainWindow.setView(index, getView());
		}
	}

	//------------------------------------------------------------------

	private boolean confirmWriteFile(
		File	file,
		String	title)
	{
		String[] optionStrs = Utils.getOptionStrings(AppConstants.REPLACE_STR);
		return (!file.exists() ||
				(JOptionPane.showOptionDialog(mainWindow, Utils.getPathname(file) + AppConstants.ALREADY_EXISTS_STR,
											  title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, null,
											  optionStrs, optionStrs[1]) == JOptionPane.OK_OPTION));
	}

	//------------------------------------------------------------------

	private boolean confirmCloseDocument(
		int	index)
	{
		// Test whether document has changed
		CrosswordDocument document = getDocument(index);
		if (!document.isChanged())
			return true;

		// Restore window
		GuiUtils.restoreFrame(mainWindow);

		// Display document
		mainWindow.selectView(index);

		// Display prompt to save changed document
		File file = document.getFile();
		String messageStr =
					((file == null) ? UNNAMED_FILE_STR : Utils.getPathname(file) + CHANGED_MESSAGE1_STR) +
																					CHANGED_MESSAGE2_STR;
		String[] optionStrs = Utils.getOptionStrings(SAVE_STR, DISCARD_STR);
		int result = JOptionPane.showOptionDialog(mainWindow, messageStr, SAVE_CLOSE_FILE_STR,
												  JOptionPane.YES_NO_CANCEL_OPTION,
												  JOptionPane.QUESTION_MESSAGE, null, optionStrs,
												  optionStrs[0]);

		// Discard changed document
		if (result == JOptionPane.NO_OPTION)
			return true;

		// Save changed document
		if (result == JOptionPane.YES_OPTION)
		{
			// Choose filename
			if (file == null)
			{
				file = chooseSave(null);
				if (file == null)
					return false;
				if (file.exists())
				{
					messageStr = Utils.getPathname(file) + AppConstants.ALREADY_EXISTS_STR;
					result = JOptionPane.showConfirmDialog(mainWindow, messageStr, SAVE_CLOSE_FILE_STR,
														   JOptionPane.YES_NO_CANCEL_OPTION,
														   JOptionPane.WARNING_MESSAGE);
					if (result == JOptionPane.NO_OPTION)
						return true;
					if (result != JOptionPane.YES_OPTION)
						return false;
				}
			}

			// Write file
			try
			{
				writeDocument(document, file);
				return true;
			}
			catch (AppException e)
			{
				showErrorMessage(SAVE_CLOSE_FILE_STR, e);
			}
		}

		return false;
	}

	//------------------------------------------------------------------

	private void init(
		String[]	args)
	{
		// Log stack trace of uncaught exception
		if (ClassUtils.isFromJar(getClass()))
		{
			Thread.setDefaultUncaughtExceptionHandler((thread, exception) ->
			{
				try
				{
					ErrorLogger.INSTANCE.write(exception);
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			});
		}

		// Initialise instance variables
		documentsViews = new ArrayList<>();
		showViewHtmlFileMessage = true;
		pendingFiles = new ArrayList<>();

		// Read build properties and initialise version string
		try
		{
			buildProperties =
					new ResourceProperties(ResourceUtils.normalisedPathname(getClass(), BUILD_PROPERTIES_FILENAME));
			versionStr = BuildUtils.versionString(getClass(), buildProperties);
		}
		catch (LocationException e)
		{
			e.printStackTrace();
		}

		// Create list of files from command-line arguments
		List<File> files = Stream.of(args).map(arg -> new File(PathnameUtils.parsePathname(arg))).toList();

		// Read TX port number from file
		int txPort = PortNumber.getValue(NAME_KEY);

		// Seek another running instance of this application; if one is found, transmit list of command-line files to it
		if (txPort >= 0)
		{
			String txId = getClass().getSimpleName() + "." + DataTxChannel.getIdSuffix();
			DataTxChannel txChannel = new DataTxChannel(txId);
			if (txChannel.transmit(txPort, RX_ID, files.stream().map(file -> file.getAbsolutePath() + "\n").toList()))
				System.exit(0);
		}

		// Open a channel for receiving data from other instances of this application
		DataTxChannel rxChannel = new DataTxChannel(RX_ID);
		int rxPort = rxChannel.openReceiver();

		// Listen for lists of pathnames from other instances of this application
		if (rxPort >= 0)
		{
			// Listen for data on RX port
			rxChannel.listen(data ->
			{
				SwingUtilities.invokeLater(() ->
				{
					// Add pathnames to list of pending files
					List<String> pathnames = StringUtils.split(data, '\n');
					if (!pathnames.isEmpty())
						pendingFiles.addAll(pathnames.stream()
														.filter(pathname -> !pathname.isEmpty())
														.map(pathname -> new File(pathname))
														.toList());
				});
			});

			// On shutdown, invalidate port-number file
			Runtime.getRuntime().addShutdownHook(new Thread(() -> PortNumber.setValue(NAME_KEY, -1)));

			// Write port number to file
			PortNumber.setValue(NAME_KEY, rxPort);
		}

		// Read configuration
		AppConfig config = AppConfig.INSTANCE;
		config.read();

		// Set UNIX style for pathnames in file exceptions
		ExceptionUtils.setUnixStyle(config.isShowUnixPathnames());

		// Set text antialiasing
		TextRendering.setAntialiasing(config.getTextAntialiasing());

		// Set look-and-feel
		String lookAndFeelName = config.getLookAndFeel();
		for (UIManager.LookAndFeelInfo lookAndFeelInfo : UIManager.getInstalledLookAndFeels())
		{
			if (lookAndFeelInfo.getName().equals(lookAndFeelName))
			{
				try
				{
					UIManager.setLookAndFeel(lookAndFeelInfo.getClassName());
				}
				catch (Exception e)
				{
					// ignore
				}
				lookAndFeelName = null;
				break;
			}
		}
		if (lookAndFeelName != null)
			showWarningMessage(SHORT_NAME + " : " + CONFIG_ERROR_STR,
							   LAF_ERROR1_STR + lookAndFeelName + LAF_ERROR2_STR);

		// Select all text when a text field gains focus
		if (config.isSelectTextOnFocusGained())
			TextFieldUtils.selectAllOnFocusGained();

		// Initialise file choosers
		initFileChoosers();

		// Perform remaining initialisation on event-dispatching thread
		SwingUtilities.invokeLater(() ->
		{
			// Create main window
			mainWindow = new MainWindow();

			// Start file-check timer
			new Timer(FILE_CHECK_TIMER_INTERVAL, AppCommand.CHECK_MODIFIED_FILE).start();

			// Open any files that were specified as command-line arguments
			if (!files.isEmpty())
			{
				// Open files
				openFiles(files);

				// Update title, menus and status
				mainWindow.updateAll();
			}
		});
	}

	//------------------------------------------------------------------

	private void initFileChoosers()
	{
		AppConfig config = AppConfig.INSTANCE;

		openFileChooser = new JFileChooser(config.getOpenCrosswordDirectory());
		openFileChooser.setDialogTitle(OPEN_FILE_STR);
		openFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

		saveFileChooser = new JFileChooser(config.getSaveCrosswordDirectory());
		saveFileChooser.setDialogTitle(SAVE_FILE_STR);
		saveFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

		exportHtmlFileChooser = new JFileChooser(config.getExportHtmlDirectory());
		exportHtmlFileChooser.setDialogTitle(EXPORT_AS_HTML_STR);
		exportHtmlFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		exportHtmlFileChooser.setFileFilter(new FilenameSuffixFilter(AppConstants.HTML_FILES_STR,
																	 AppConstants.HTML_FILENAME_EXTENSION));
	}

	//------------------------------------------------------------------

	private File chooseOpen()
	{
		openFileChooser.setFileFilter(new FilenameSuffixFilter(AppConstants.CROSSWORD_FILES_STR,
															   AppConfig.INSTANCE.getFilenameSuffix()));
		openFileChooser.setSelectedFile(new File(""));
		openFileChooser.rescanCurrentDirectory();
		return ((openFileChooser.showOpenDialog(mainWindow) == JFileChooser.APPROVE_OPTION)
																		? openFileChooser.getSelectedFile()
																		: null);
	}

	//------------------------------------------------------------------

	private File chooseSave(
		File	file)
	{
		String filenameSuffix = AppConfig.INSTANCE.getFilenameSuffix();
		saveFileChooser.setFileFilter(new FilenameSuffixFilter(AppConstants.CROSSWORD_FILES_STR, filenameSuffix));
		saveFileChooser.setSelectedFile((file == null) ? new File("") : file.getAbsoluteFile());
		saveFileChooser.rescanCurrentDirectory();
		return ((saveFileChooser.showSaveDialog(mainWindow) == JFileChooser.APPROVE_OPTION)
								? Utils.appendSuffix(saveFileChooser.getSelectedFile(), filenameSuffix)
								: null);
	}

	//------------------------------------------------------------------

	private File chooseExportHtml(
		File	file)
	{
		exportHtmlFileChooser.setSelectedFile((file == null) ? new File("")
															 : file.getAbsoluteFile());
		exportHtmlFileChooser.rescanCurrentDirectory();
		return ((exportHtmlFileChooser.showSaveDialog(mainWindow) == JFileChooser.APPROVE_OPTION)
									? Utils.appendSuffix(exportHtmlFileChooser.getSelectedFile(),
														 AppConstants.HTML_FILENAME_EXTENSION)
									: null);
	}

	//------------------------------------------------------------------

	private void updateConfiguration()
	{
		// Set location of main window
		AppConfig config = AppConfig.INSTANCE;
		if (config.isMainWindowLocation())
		{
			Point location = GuiUtils.getFrameLocation(mainWindow);
			if (location != null)
				config.setMainWindowLocation(location);
		}
		config.setMainWindowSize(GuiUtils.getFrameSize(mainWindow));

		// Set file locations
		config.setOpenCrosswordPathname(Utils.getPathname(openFileChooser.getCurrentDirectory()));
		config.setSaveCrosswordPathname(Utils.getPathname(saveFileChooser.getCurrentDirectory()));
		config.setExportHtmlPathname(Utils.getPathname(exportHtmlFileChooser.getCurrentDirectory()));

		// Write configuration file
		config.write();
	}

	//------------------------------------------------------------------

	private void openFiles(
		List<File>	files)
	{
		for (int i = 0; i < files.size(); i++)
		{
			if (isDocumentsFull())
				break;
			try
			{
				openDocument(files.get(i));
				if (Task.isCancelled())
					break;
			}
			catch (AppException e)
			{
				if (i == files.size() - 1)
					showErrorMessage(OPEN_FILE_STR, e);
				else
				{
					String[] optionStrs = Utils.getOptionStrings(AppConstants.CONTINUE_STR);
					if (JOptionPane.showOptionDialog(mainWindow, e, OPEN_FILE_STR, JOptionPane.OK_CANCEL_OPTION,
													 JOptionPane.ERROR_MESSAGE, null, optionStrs, optionStrs[1])
																							!= JOptionPane.OK_OPTION)
						break;
				}
			}
		}
	}

	//------------------------------------------------------------------

	private void onCheckModifiedFile()
		throws AppException
	{
		CrosswordDocument document = getDocument();
		if ((document != null) && !document.isExecutingCommand())
		{
			File file = document.getFile();
			long timestamp = document.getTimestamp();
			if ((file != null) && (timestamp != 0))
			{
				long currentTimestamp = file.lastModified();
				if ((currentTimestamp != 0) && (currentTimestamp != timestamp))
				{
					String messageStr = Utils.getPathname(file) + MODIFIED_MESSAGE_STR;
					if (JOptionPane.showConfirmDialog(mainWindow, messageStr, MODIFIED_FILE_STR,
													  JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE)
																							== JOptionPane.YES_OPTION)
					{
						try
						{
							revertDocument(file);
							mainWindow.updateAll();
						}
						catch (AppException e)
						{
							document.setTimestamp(currentTimestamp);
							throw e;
						}
					}
					else
						document.setTimestamp(currentTimestamp);
				}
			}
		}
	}

	//------------------------------------------------------------------

	private void onImportFiles()
	{
		List<File> crosswordFiles = new ArrayList<>();
		String filenameSuffix = AppConfig.INSTANCE.getFilenameSuffix();
		@SuppressWarnings("unchecked")
		List<File> files = (List<File>)AppCommand.IMPORT_FILES.getValue(AppCommand.Property.FILES);
		for (File file : files)
		{
			if (file.getName().endsWith(filenameSuffix))
				crosswordFiles.add(file);
		}
		openFiles(crosswordFiles);
	}

	//------------------------------------------------------------------

	private void onCreateDocument()
	{
		if (!isDocumentsFull())
		{
			Grid grid = GridParamsDialog.showDialog(mainWindow, NEW_CROSSWORD_STR);
			if (grid != null)
			{
				CrosswordDocument document = new CrosswordDocument(++newFileIndex);
				document.setGrid(grid);
				addDocument(document);
			}
		}
	}

	//------------------------------------------------------------------

	private void onOpenDocument()
		throws AppException
	{
		if (!isDocumentsFull())
		{
			File file = chooseOpen();
			if (file != null)
				openDocument(file);
		}
	}

	//------------------------------------------------------------------

	private void onRevertDocument()
		throws AppException
	{
		CrosswordDocument document = getDocument();
		if ((document != null) && document.isChanged())
		{
			File file = document.getFile();
			if (file != null)
			{
				String messageStr = Utils.getPathname(file) + REVERT_MESSAGE_STR;
				String[] optionStrs = Utils.getOptionStrings(REVERT_STR);
				if (JOptionPane.showOptionDialog(mainWindow, messageStr, REVERT_FILE_STR, JOptionPane.OK_CANCEL_OPTION,
												 JOptionPane.QUESTION_MESSAGE, null, optionStrs, optionStrs[1])
																							== JOptionPane.OK_OPTION)
					revertDocument(file);
			}
		}
	}

	//------------------------------------------------------------------

	private void onCloseDocument()
	{
		if (hasDocuments())
			closeDocument(mainWindow.getTabIndex());
	}

	//------------------------------------------------------------------

	private void onCloseAllDocuments()
	{
		while (hasDocuments())
		{
			int index = getNumDocuments() - 1;
			if (!confirmCloseDocument(index))
				break;
			removeDocument(index);
		}
	}

	//------------------------------------------------------------------

	private void onSaveDocument()
		throws AppException
	{
		CrosswordDocument document = getDocument();
		if ((document != null) && document.isChanged())
		{
			File file = document.getFile();
			if (file == null)
				onSaveDocumentAs();
			else
				writeDocument(document, file);
		}
	}

	//------------------------------------------------------------------

	private void onSaveDocumentAs()
		throws AppException
	{
		CrosswordDocument document = getDocument();
		if (document != null)
		{
			File file = document.getOutputFile();
			if (file != null)
			{
				File directory = file.getParentFile();
				if ((directory != null) && !directory.exists())
					directory.mkdirs();
			}
			file = chooseSave(file);
			if ((file != null) && confirmWriteFile(file, SAVE_FILE_AS_STR))
				writeDocument(document, file);
		}
	}

	//------------------------------------------------------------------

	private void onExportHtmlFile()
		throws AppException
	{
		// Get current document
		CrosswordDocument document = getDocument();

		// Get parameters
		if (document != null)
		{
			// Specify export parameters
			Grid.Separator separator = document.getGrid().getSeparator();
			AppConfig config = AppConfig.INSTANCE;
			ExportHtmlDialog.Result result =
					ExportHtmlDialog.showDialog(mainWindow, separator, config.getHtmlStylesheetKind(),
												config.getHtmlCellSize(separator));
			if (result == null)
				return;

			// Update configuration properties
			config.setHtmlStylesheetKind(result.stylesheetKind);
			config.setHtmlCellSize(separator, result.cellSize);

			// Get pathname of HTML file from current document
			File file = document.getExportHtmlFile();

			// Derive pathname of HTML file
			if (file == null)
			{
				file = document.getFile();
				if (file != null)
				{
					String filename = file.getName();
					filename = filename.endsWith(config.getFilenameSuffix())
														? StringUtils.removeSuffix(filename, config.getFilenameSuffix())
														: StringUtils.getPrefixLast(filename, '.');
					file = new File(file.getParentFile(), filename + AppConstants.HTML_FILENAME_EXTENSION);
				}
			}

			// Select output file
			file = chooseExportHtml(file);

			// Confirm replacement of existing file
			if ((file == null) || !confirmWriteFile(file, EXPORT_AS_HTML_STR))
				return;

			// Write HTML file
			CrosswordDocument.StyleProperties styleProperties =
							new CrosswordDocument.StyleProperties(config.getHtmlFontNames(), config.getHtmlFontSize(),
																  result.cellSize, config.getHtmlGridColour(),
																  config.getHtmlEntryColour(),
																  config.getHtmlFieldNumFontSizeFactor());
			Task task = new Task.ExportDocumentAsHtml(document, file, result.stylesheetKind, styleProperties,
													  result.writeStylesheet, result.writeBlockImage,
													  result.writeEntries);
			TaskProgressDialog.showDialog(mainWindow, EXPORT_AS_HTML_STR, task);

			// Show "View HTML file" message
			if (showViewHtmlFileMessage)
			{
				QuestionDialog.Result viewResult = QuestionDialog.showDialog(mainWindow, VIEW_FILE_STR,
																			 new String[] { VIEW_HTML_FILE_STR },
																			 VIEW_FILE_OPTIONS, 0, DO_NOT_VIEW_KEY,
																			 DO_NOT_SHOW_AGAIN_STR);
				if (viewResult.checkBoxSelected())
					showViewHtmlFileMessage = false;
				if (QuestionDialog.CANCEL_KEY.equals(viewResult.selectedKey()))
					return;
				viewHtmlFile = VIEW_KEY.equals(viewResult.selectedKey());
			}

			// View output file
			if (viewHtmlFile)
			{
				String command = config.getHtmlViewerCommand();
				if (command != null)
					HtmlViewer.viewHtmlFile(command, file);
			}
		}
	}

	//------------------------------------------------------------------

	private void onExit()
	{
		if (!exiting)
		{
			try
			{
				// Prevent re-entry to this method
				exiting = true;

				// Close all open documents
				while (hasDocuments())
				{
					int index = getNumDocuments() - 1;
					if (!confirmCloseDocument(index))
						return;
					removeDocument(index);
				}

				// Update configuration
				updateConfiguration();

				// Destroy main window
				mainWindow.setVisible(false);
				mainWindow.dispose();

				// Exit application
				System.exit(0);
			}
			finally
			{
				exiting = false;
			}
		}
	}

	//------------------------------------------------------------------

	private void onCaptureCrossword()
	{
		if (!isDocumentsFull())
		{
			CrosswordDocument document = CaptureDialog.showDialog(mainWindow, newFileIndex + 1);
			if (document != null)
			{
				++newFileIndex;
				addDocument(document);
			}
		}
	}

	//------------------------------------------------------------------

	private void onCreateSolutionDocument()
	{
		if (!isDocumentsFull())
			addDocument(getDocument().createSolutionDocument(++newFileIndex));
	}

	//------------------------------------------------------------------

	private void onToggleShowFullPathnames()
	{
		AppConfig.INSTANCE.setShowFullPathnames(!AppConfig.INSTANCE.isShowFullPathnames());
	}

	//------------------------------------------------------------------

	private void onManageFileAssociation()
		throws AppException
	{
		FileAssociationDialog.Result result = FileAssociationDialog.showDialog(mainWindow);
		if (result != null)
		{
			FileAssociations fileAssoc = new FileAssociations();
			fileAssoc.addParams(ASSOC_FILE_KIND_KEY, ASSOC_FILE_KIND_TEXT, ASSOC_FILE_OPEN_TEXT,
								AppConfig.INSTANCE.getFilenameSuffix());
			TextOutputTaskDialog.showDialog(mainWindow, FILE_ASSOCIATION_STR,
											new Task.SetFileAssociation(fileAssoc, result.javaLauncherPathname,
																		result.jarPathname, result.iconPathname,
																		ASSOC_SCRIPT_DIR_PREFIX, ASSOC_SCRIPT_FILENAME,
																		result.removeEntries, result.scriptLifeCycle));
		}
	}

	//------------------------------------------------------------------

	private void onEditPreferences()
	{
		if (PreferencesDialog.showDialog(mainWindow))
		{
			ExceptionUtils.setUnixStyle(AppConfig.INSTANCE.isShowUnixPathnames());
			for (int i = 0; i < documentsViews.size(); i++)
			{
				DocumentView documentView = documentsViews.get(i);
				documentView.view = new CrosswordView(documentView.document);
				mainWindow.setView(i, documentView.view);
			}
		}
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Member classes : non-inner classes
////////////////////////////////////////////////////////////////////////


	// CLASS: DOCUMENT-VIEW


	private static class DocumentView
	{

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	CrosswordDocument	document;
		private	CrosswordView		view;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private DocumentView(
			CrosswordDocument	document)
		{
			this.document = document;
			view = new CrosswordView(document);
		}

		//--------------------------------------------------------------

	}

	//==================================================================

}

//----------------------------------------------------------------------
