/*====================================================================*\

AppConfig.java

Application configuration class.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.crosswordeditor;

//----------------------------------------------------------------------


// IMPORTS


import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import java.lang.reflect.Field;

import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.swing.JFileChooser;
import javax.swing.UIManager;

import uk.blankaspect.common.cls.ClassUtils;

import uk.blankaspect.common.exception.AppException;
import uk.blankaspect.common.exception.FileException;

import uk.blankaspect.common.filesystem.PathnameUtils;

import uk.blankaspect.common.misc.FilenameSuffixFilter;

import uk.blankaspect.common.property.Property;
import uk.blankaspect.common.property.PropertySet;

import uk.blankaspect.common.range.IntegerRange;

import uk.blankaspect.common.ui.progress.IProgressView;

import uk.blankaspect.ui.swing.colour.ColourProperty;
import uk.blankaspect.ui.swing.colour.ColourUtils;

import uk.blankaspect.ui.swing.font.FontEx;

import uk.blankaspect.ui.swing.text.TextRendering;

//----------------------------------------------------------------------


// APPLICATION CONFIGURATION CLASS


class AppConfig
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	public static final		AppConfig	INSTANCE;

	private static final	int	VERSION					= 0;
	private static final	int	MIN_SUPPORTED_VERSION	= 0;
	private static final	int	MAX_SUPPORTED_VERSION	= 0;

	private static final	String	CONFIG_ERROR_STR	= "Configuration error";
	private static final	String	CONFIG_DIR_KEY		= Property.APP_PREFIX + "configDir";
	private static final	String	PROPERTIES_FILENAME	= App.NAME_KEY + "-properties" + AppConstants.XML_FILENAME_EXTENSION;
	private static final	String	FILENAME_STEM		= App.NAME_KEY + "-config";
	private static final	String	CONFIG_FILENAME		= FILENAME_STEM + AppConstants.XML_FILENAME_EXTENSION;
	private static final	String	CONFIG_OLD_FILENAME	= FILENAME_STEM + "-old" + AppConstants.XML_FILENAME_EXTENSION;

	private static final	String	SAVE_CONFIGURATION_FILE_STR	= "Save configuration file";
	private static final	String	WRITING_STR					= "Writing";

	private interface Key
	{
		String	ALLOW_MULTIPLE_FIELD_USE		= "allowMultipleFieldUse";
		String	APPEARANCE						= "appearance";
		String	BAR_COLOUR						= "barColour";
		String	BAR_GRID						= "barGrid";
		String	BAR_WIDTH						= "barWidth";
		String	BLOCK_IMAGE						= "blockImage";
		String	CELL_OFFSET_LEFT				= "cellOffsetLeft";
		String	CELL_OFFSET_TOP					= "cellOffsetTop";
		String	CELL_SIZE						= "cellSize";
		String	CLEAR_EDIT_LIST_ON_SAVE			= "clearEditListOnSave";
		String	CLUE							= "clue";
		String	COLOUR							= "colour";
		String	CONFIGURATION					= App.NAME_KEY + "Configuration";
		String	DIRECTION_KEYWORDS				= "directionKeywords";
		String	ENTRY_CHARACTERS				= "entryCharacters";
		String	ENTRY_COLOUR					= "entryColour";
		String	EXPORT_HTML_DIRECTORY			= "exportHtmlDirectory";
		String	FIELD_NUMBER_FONT_SIZE_FACTOR	= "fieldNumberFontSizeFactor";
		String	FIELD_NUMBER_OFFSET_LEFT		= "fieldNumberOffsetLeft";
		String	FIELD_NUMBER_OFFSET_TOP			= "fieldNumberOffsetTop";
		String	FILE							= "file";
		String	FILENAME_SUFFIX					= "filenameSuffix";
		String	FONT							= "font";
		String	FONT_NAMES						= "fontNames";
		String	FONT_SIZE						= "fontSize";
		String	GENERAL							= "general";
		String	GRID							= "grid";
		String	GRID_COLOUR						= "gridColour";
		String	HTML							= "html";
		String	IMPLICIT_FIELD_DIRECTION		= "implicitFieldDirection";
		String	IMAGE_VIEWPORT_SIZE				= "imageViewportSize";
		String	LINE_BREAK						= "lineBreak";
		String	LINE_WIDTH						= "lineWidth";
		String	LOOK_AND_FEEL					= "lookAndFeel";
		String	MAIN_WINDOW_LOCATION			= "mainWindowLocation";
		String	MAIN_WINDOW_SIZE				= "mainWindowSize";
		String	MAX_EDIT_LIST_LENGTH			= "maxEditListLength";
		String	NAVIGATE_OVER_SEPARATORS		= "navigateOverSeparators";
		String	NUM_LINES						= "numLines";
		String	OPEN_CROSSWORD_DIRECTORY		= "openCrosswordDirectory";
		String	PARAMETER_SET_FILE				= "parameterSetFile";
		String	PATH							= "path";
		String	PRINT_ONLY						= "printOnly";
		String	REFERENCE_KEYWORD				= "referenceKeyword";
		String	SAVE_CROSSWORD_DIRECTORY		= "saveCrosswordDirectory";
		String	SELECT_TEXT_ON_FOCUS_GAINED		= "selectTextOnFocusGained";
		String	SELECTED_CLUE_NUM_COLUMNS		= "selectedClueNumColumns";
		String	SHOW_FULL_PATHNAMES				= "showFullPathnames";
		String	SHOW_UNIX_PATHNAMES				= "showUnixPathnames";
		String	STATUS_TEXT_COLOUR				= "statusTextColour";
		String	STYLESHEET_KIND					= "stylesheetKind";
		String	TEXT_ANTIALIASING				= "textAntialiasing";
		String	TEXT_SECTION					= "textSection";
		String	VIEW							= "view";
		String	VIEWER_COMMAND					= "viewerCommand";
	}

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

		ERROR_READING_PROPERTIES_FILE
		("An error occurred when reading the properties file."),

		NO_CONFIGURATION_FILE
		("No configuration file was found at the specified location."),

		NO_VERSION_NUMBER
		("The configuration file does not have a version number."),

		INVALID_VERSION_NUMBER
		("The version number of the configuration file is invalid."),

		UNSUPPORTED_CONFIGURATION_FILE
		("The version of the configuration file (%1) is not supported by this version of " +
			App.SHORT_NAME + "."),

		FAILED_TO_CREATE_DIRECTORY
		("Failed to create the directory for the configuration file."),

		MALFORMED_PATTERN
		("The pattern is not a well-formed regular expression.\n(%1)");

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
//  Member classes : non-inner classes
////////////////////////////////////////////////////////////////////////


	// CONFIGURATION FILE CLASS


	private static class ConfigFile
		extends PropertySet
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		private static final	String	CONFIG_FILE1_STR	= "configuration file";
		private static final	String	CONFIG_FILE2_STR	= "Configuration file";

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private ConfigFile()
		{
		}

		//--------------------------------------------------------------

		private ConfigFile(String versionStr)
			throws AppException
		{
			super(Key.CONFIGURATION, null, versionStr);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		public String getSourceName()
		{
			return CONFIG_FILE2_STR;
		}

		//--------------------------------------------------------------

		@Override
		protected String getFileKindString()
		{
			return CONFIG_FILE1_STR;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		public void read(File file)
			throws AppException
		{
			// Read file
			read(file, Key.CONFIGURATION);

			// Test version number
			String versionStr = getVersionString();
			if (versionStr == null)
				throw new FileException(ErrorId.NO_VERSION_NUMBER, file);
			try
			{
				int version = Integer.parseInt(versionStr);
				if ((version < MIN_SUPPORTED_VERSION) || (version > MAX_SUPPORTED_VERSION))
					throw new FileException(ErrorId.UNSUPPORTED_CONFIGURATION_FILE, file, versionStr);
			}
			catch (NumberFormatException e)
			{
				throw new FileException(ErrorId.INVALID_VERSION_NUMBER, file);
			}
		}

		//--------------------------------------------------------------

	}

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Member classes : inner classes
////////////////////////////////////////////////////////////////////////


	// PROPERTY CLASS: SHOW UNIX PATHNAMES


	private class CPShowUnixPathnames
		extends Property.BooleanProperty
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CPShowUnixPathnames()
		{
			super(concatenateKeys(Key.GENERAL, Key.SHOW_UNIX_PATHNAMES));
			value = false;
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public boolean isShowUnixPathnames()
	{
		return cpShowUnixPathnames.getValue();
	}

	//------------------------------------------------------------------

	public void setShowUnixPathnames(boolean value)
	{
		cpShowUnixPathnames.setValue(value);
	}

	//------------------------------------------------------------------

	public void addShowUnixPathnamesObserver(Property.IObserver observer)
	{
		cpShowUnixPathnames.addObserver(observer);
	}

	//------------------------------------------------------------------

	public void removeShowUnixPathnamesObserver(Property.IObserver observer)
	{
		cpShowUnixPathnames.removeObserver(observer);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance variables : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	CPShowUnixPathnames	cpShowUnixPathnames	= new CPShowUnixPathnames();

	//==================================================================


	// PROPERTY CLASS: SELECT TEXT ON FOCUS GAINED


	private class CPSelectTextOnFocusGained
		extends Property.BooleanProperty
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CPSelectTextOnFocusGained()
		{
			super(concatenateKeys(Key.GENERAL, Key.SELECT_TEXT_ON_FOCUS_GAINED));
			value = true;
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public boolean isSelectTextOnFocusGained()
	{
		return cpSelectTextOnFocusGained.getValue();
	}

	//------------------------------------------------------------------

	public void setSelectTextOnFocusGained(boolean value)
	{
		cpSelectTextOnFocusGained.setValue(value);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance variables : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	CPSelectTextOnFocusGained	cpSelectTextOnFocusGained	= new CPSelectTextOnFocusGained();

	//==================================================================


	// PROPERTY CLASS: SHOW FULL PATHNAMES


	private class CPShowFullPathnames
		extends Property.BooleanProperty
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CPShowFullPathnames()
		{
			super(concatenateKeys(Key.GENERAL, Key.SHOW_FULL_PATHNAMES));
			value = false;
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public boolean isShowFullPathnames()
	{
		return cpShowFullPathnames.getValue();
	}

	//------------------------------------------------------------------

	public void setShowFullPathnames(boolean value)
	{
		cpShowFullPathnames.setValue(value);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance variables : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	CPShowFullPathnames	cpShowFullPathnames	= new CPShowFullPathnames();

	//==================================================================


	// PROPERTY CLASS: MAIN WINDOW LOCATION


	private class CPMainWindowLocation
		extends Property.SimpleProperty<Point>
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CPMainWindowLocation()
		{
			super(concatenateKeys(Key.GENERAL, Key.MAIN_WINDOW_LOCATION));
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		public void parse(Input input)
			throws AppException
		{
			if (input.getValue().isEmpty())
				value = null;
			else
			{
				int[] outValues = input.parseIntegers(2, null);
				value = new Point(outValues[0], outValues[1]);
			}
		}

		//--------------------------------------------------------------

		@Override
		public String toString()
		{
			return (value == null) ? "" : value.x + ", " + value.y;
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public boolean isMainWindowLocation()
	{
		return (getMainWindowLocation() != null);
	}

	//------------------------------------------------------------------

	public Point getMainWindowLocation()
	{
		return cpMainWindowLocation.getValue();
	}

	//------------------------------------------------------------------

	public void setMainWindowLocation(Point value)
	{
		cpMainWindowLocation.setValue(value);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance variables : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	CPMainWindowLocation	cpMainWindowLocation	= new CPMainWindowLocation();

	//==================================================================


	// PROPERTY CLASS: MAIN WINDOW SIZE


	private class CPMainWindowSize
		extends Property.SimpleProperty<Dimension>
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CPMainWindowSize()
		{
			super(concatenateKeys(Key.GENERAL, Key.MAIN_WINDOW_SIZE));
			value = new Dimension(MainWindow.DEFAULT_WIDTH, MainWindow.DEFAULT_HEIGHT);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		public void parse(Input input)
			throws AppException
		{
			if (!input.getValue().isEmpty())
			{
				int[] outValues = input.parseIntegers(2, null);
				value = new Dimension(outValues[0], outValues[1]);
			}
		}

		//--------------------------------------------------------------

		@Override
		public String toString()
		{
			return (value.width + ", " + value.height);
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public Dimension getMainWindowSize()
	{
		return cpMainWindowSize.getValue();
	}

	//------------------------------------------------------------------

	public void setMainWindowSize(Dimension value)
	{
		cpMainWindowSize.setValue(value);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance variables : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	CPMainWindowSize	cpMainWindowSize	= new CPMainWindowSize();

	//==================================================================


	// PROPERTY CLASS: MAXIMUM EDIT LIST LENGTH


	private class CPMaxEditListLength
		extends Property.IntegerProperty
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CPMaxEditListLength()
		{
			super(concatenateKeys(Key.GENERAL, Key.MAX_EDIT_LIST_LENGTH),
				  CrosswordDocument.MIN_MAX_EDIT_LIST_LENGTH, CrosswordDocument.MAX_MAX_EDIT_LIST_LENGTH);
			value = CrosswordDocument.DEFAULT_MAX_EDIT_LIST_LENGTH;
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public int getMaxEditListLength()
	{
		return cpMaxEditListLength.getValue();
	}

	//------------------------------------------------------------------

	public void setMaxEditListLength(int value)
	{
		cpMaxEditListLength.setValue(value);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance variables : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	CPMaxEditListLength	cpMaxEditListLength	= new CPMaxEditListLength();

	//==================================================================


	// PROPERTY CLASS: CLEAR EDIT LIST ON SAVE


	private class CPClearEditListOnSave
		extends Property.BooleanProperty
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CPClearEditListOnSave()
		{
			super(concatenateKeys(Key.GENERAL, Key.CLEAR_EDIT_LIST_ON_SAVE));
			value = false;
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public boolean isClearEditListOnSave()
	{
		return cpClearEditListOnSave.getValue();
	}

	//------------------------------------------------------------------

	public void setClearEditListOnSave(boolean value)
	{
		cpClearEditListOnSave.setValue(value);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance variables : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	CPClearEditListOnSave	cpClearEditListOnSave	= new CPClearEditListOnSave();

	//==================================================================


	// PROPERTY CLASS: FILENAME SUFFIX


	private class CPFilenameSuffix
		extends Property.StringProperty
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CPFilenameSuffix()
		{
			super(concatenateKeys(Key.FILE, Key.FILENAME_SUFFIX));
			value = CrosswordDocument.DEFAULT_FILENAME_SUFFIX;
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public String getFilenameSuffix()
	{
		return cpFilenameSuffix.getValue();
	}

	//------------------------------------------------------------------

	public void setFilenameSuffix(String value)
	{
		cpFilenameSuffix.setValue(value);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance variables : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	CPFilenameSuffix	cpFilenameSuffix	= new CPFilenameSuffix();

	//==================================================================


	// PROPERTY CLASS: LOOK-AND-FEEL


	private class CPLookAndFeel
		extends Property.StringProperty
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CPLookAndFeel()
		{
			super(concatenateKeys(Key.APPEARANCE, Key.LOOK_AND_FEEL));
			value = "";
			for (UIManager.LookAndFeelInfo lookAndFeelInfo : UIManager.getInstalledLookAndFeels())
			{
				if (lookAndFeelInfo.getClassName().
											equals(UIManager.getCrossPlatformLookAndFeelClassName()))
				{
					value = lookAndFeelInfo.getName();
					break;
				}
			}
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public String getLookAndFeel()
	{
		return cpLookAndFeel.getValue();
	}

	//------------------------------------------------------------------

	public void setLookAndFeel(String value)
	{
		cpLookAndFeel.setValue(value);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance variables : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	CPLookAndFeel	cpLookAndFeel	= new CPLookAndFeel();

	//==================================================================


	// PROPERTY CLASS: TEXT ANTIALIASING


	private class CPTextAntialiasing
		extends Property.EnumProperty<TextRendering.Antialiasing>
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CPTextAntialiasing()
		{
			super(concatenateKeys(Key.APPEARANCE, Key.TEXT_ANTIALIASING),
				  TextRendering.Antialiasing.class);
			value = TextRendering.Antialiasing.DEFAULT;
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public TextRendering.Antialiasing getTextAntialiasing()
	{
		return cpTextAntialiasing.getValue();
	}

	//------------------------------------------------------------------

	public void setTextAntialiasing(TextRendering.Antialiasing value)
	{
		cpTextAntialiasing.setValue(value);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance variables : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	CPTextAntialiasing	cpTextAntialiasing	= new CPTextAntialiasing();

	//==================================================================


	// PROPERTY CLASS: STATUS TEXT COLOUR


	private class CPStatusTextColour
		extends ColourProperty
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CPStatusTextColour()
		{
			super(concatenateKeys(Key.APPEARANCE, Key.STATUS_TEXT_COLOUR));
			value = AbstractStatusPanel.DEFAULT_TEXT_COLOUR;
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public Color getStatusTextColour()
	{
		return cpStatusTextColour.getValue();
	}

	//------------------------------------------------------------------

	public void setStatusTextColour(Color value)
	{
		cpStatusTextColour.setValue(value);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance variables : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	CPStatusTextColour	cpStatusTextColour	= new CPStatusTextColour();

	//==================================================================


	// PROPERTY CLASS: PATHNAME OF DIRECTORY OF OPEN CROSSWORD FILE CHOOSER


	private class CPOpenCrosswordPathname
		extends Property.StringProperty
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CPOpenCrosswordPathname()
		{
			super(concatenateKeys(Key.PATH, Key.OPEN_CROSSWORD_DIRECTORY));
			value = PathnameUtils.USER_HOME_PREFIX;
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public String getOpenCrosswordPathname()
	{
		return cpOpenCrosswordPathname.getValue();
	}

	//------------------------------------------------------------------

	public File getOpenCrosswordDirectory()
	{
		return new File(PathnameUtils.parsePathname(getOpenCrosswordPathname()));
	}

	//------------------------------------------------------------------

	public void setOpenCrosswordPathname(String value)
	{
		cpOpenCrosswordPathname.setValue(value);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance variables : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	CPOpenCrosswordPathname	cpOpenCrosswordPathname	= new CPOpenCrosswordPathname();

	//==================================================================


	// PROPERTY CLASS: PATHNAME OF DIRECTORY OF SAVE CROSSWORD FILE CHOOSER


	private class CPSaveCrosswordPathname
		extends Property.StringProperty
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CPSaveCrosswordPathname()
		{
			super(concatenateKeys(Key.PATH, Key.SAVE_CROSSWORD_DIRECTORY));
			value = PathnameUtils.USER_HOME_PREFIX;
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public String getSaveCrosswordPathname()
	{
		return cpSaveCrosswordPathname.getValue();
	}

	//------------------------------------------------------------------

	public File getSaveCrosswordDirectory()
	{
		return new File(PathnameUtils.parsePathname(getSaveCrosswordPathname()));
	}

	//------------------------------------------------------------------

	public void setSaveCrosswordPathname(String value)
	{
		cpSaveCrosswordPathname.setValue(value);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance variables : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	CPSaveCrosswordPathname	cpSaveCrosswordPathname	= new CPSaveCrosswordPathname();

	//==================================================================


	// PROPERTY CLASS: PATHNAME OF DIRECTORY OF EXPORT HTML FILE CHOOSER


	private class CPExportHtmlPathname
		extends Property.StringProperty
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CPExportHtmlPathname()
		{
			super(concatenateKeys(Key.PATH, Key.EXPORT_HTML_DIRECTORY));
			value = PathnameUtils.USER_HOME_PREFIX;
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public String getExportHtmlPathname()
	{
		return cpExportHtmlPathname.getValue();
	}

	//------------------------------------------------------------------

	public File getExportHtmlDirectory()
	{
		return new File(PathnameUtils.parsePathname(getExportHtmlPathname()));
	}

	//------------------------------------------------------------------

	public void setExportHtmlPathname(String value)
	{
		cpExportHtmlPathname.setValue(value);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance variables : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	CPExportHtmlPathname	cpExportHtmlPathname	= new CPExportHtmlPathname();

	//==================================================================


	// PROPERTY CLASS: PATHNAME OF PARAMETER-SET FILE


	private class CPParameterSetPathname
		extends Property.StringProperty
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CPParameterSetPathname()
		{
			super(concatenateKeys(Key.PATH, Key.PARAMETER_SET_FILE));
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public String getParameterSetPathname()
	{
		return cpParameterSetPathname.getValue();
	}

	//------------------------------------------------------------------

	public File getParameterSetFile()
	{
		String pathname = getParameterSetPathname();
		return ((pathname == null) ? null : new File(PathnameUtils.parsePathname(pathname)));
	}

	//------------------------------------------------------------------

	public void setParameterSetPathname(String value)
	{
		cpParameterSetPathname.setValue(value);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance variables : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	CPParameterSetPathname	cpParameterSetPathname	= new CPParameterSetPathname();

	//==================================================================


	// PROPERTY CLASS: GRID ENTRY CHARACTERS


	private class CPGridEntryCharacters
		extends Property.StringProperty
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		private static final	String	DEFAULT_VALUE	= "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CPGridEntryCharacters()
		{
			super(concatenateKeys(Key.GRID, Key.ENTRY_CHARACTERS));
			value = DEFAULT_VALUE;
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public String getGridEntryCharacters()
	{
		return cpGridEntryCharacters.getValue();
	}

	//------------------------------------------------------------------

	public void setGridEntryCharacters(String value)
	{
		cpGridEntryCharacters.setValue(value);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance variables : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	CPGridEntryCharacters	cpGridEntryCharacters	= new CPGridEntryCharacters();

	//==================================================================


	// PROPERTY CLASS: NAVIGATE OVER GRID SEPARATORS


	private class CPNavigateOverGridSeparators
		extends Property.BooleanProperty
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CPNavigateOverGridSeparators()
		{
			super(concatenateKeys(Key.GRID, Key.NAVIGATE_OVER_SEPARATORS));
			value = true;
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public boolean isNavigateOverGridSeparators()
	{
		return cpNavigateOverGridSeparators.getValue();
	}

	//------------------------------------------------------------------

	public void setNavigateOverGridSeparators(boolean value)
	{
		cpNavigateOverGridSeparators.setValue(value);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance variables : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	CPNavigateOverGridSeparators	cpNavigateOverGridSeparators	=
																		new CPNavigateOverGridSeparators();

	//==================================================================


	// PROPERTY CLASS: GRID CELL SIZE


	private class CPGridCellSize
		extends Property.PropertyMap<Grid.Separator, Integer>
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CPGridCellSize()
		{
			super(concatenateKeys(Key.GRID, Key.CELL_SIZE), Grid.Separator.class);
			for (Grid.Separator key : Grid.Separator.values())
				values.put(key, Grid.DEFAULT_CELL_SIZE);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		public void parse(Input          input,
						  Grid.Separator key)
			throws AppException
		{
			IntegerRange range = new IntegerRange(Grid.MIN_CELL_SIZE, Grid.MAX_CELL_SIZE);
			values.put(key, input.parseInteger(range));
		}

		//--------------------------------------------------------------

		@Override
		public String toString(Grid.Separator key)
		{
			return Integer.toString(getValue(key));
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public int getGridCellSize(Grid.Separator key)
	{
		return cpGridCellSize.getValue(key);
	}

	//------------------------------------------------------------------

	public void setGridCellSize(Grid.Separator key,
								int            value)
	{
		cpGridCellSize.setValue(key, value);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance variables : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	CPGridCellSize	cpGridCellSize	= new CPGridCellSize();

	//==================================================================


	// PROPERTY CLASS: BAR WIDTH IN BAR GRID


	private class CPBarGridBarWidth
		extends Property.IntegerProperty
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CPBarGridBarWidth()
		{
			super(concatenateKeys(Key.GRID, Grid.Separator.BAR.getKey(), Key.BAR_WIDTH),
				  GridPanel.Bar.MIN_BAR_WIDTH, GridPanel.Bar.MAX_BAR_WIDTH);
			value = GridPanel.Bar.DEFAULT_BAR_WIDTH;
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public int getBarGridBarWidth()
	{
		return cpBarGridBarWidth.getValue();
	}

	//------------------------------------------------------------------

	public void setBarGridBarWidth(int value)
	{
		cpBarGridBarWidth.setValue(value);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance variables : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	CPBarGridBarWidth	cpBarGridBarWidth	= new CPBarGridBarWidth();

	//==================================================================


	// PROPERTY CLASS: GRID IMAGE VIEWPORT SIZE


	private class CPGridImageViewportSize
		extends Property.SimpleProperty<Dimension>
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CPGridImageViewportSize()
		{
			super(concatenateKeys(Key.GRID, Key.IMAGE_VIEWPORT_SIZE));
			value = new Dimension(CaptureDialog.DEFAULT_GRID_IMAGE_VIEWPORT_WIDTH,
								  CaptureDialog.DEFAULT_GRID_IMAGE_VIEWPORT_HEIGHT);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		public void parse(Input input)
			throws AppException
		{
			if (!input.getValue().isEmpty())
			{
				int[] outValues = input.parseIntegers(2, null);
				value = new Dimension(outValues[0], outValues[1]);
			}
		}

		//--------------------------------------------------------------

		@Override
		public String toString()
		{
			return (value.width + ", " + value.height);
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public Dimension getGridImageViewportSize()
	{
		return cpGridImageViewportSize.getValue();
	}

	//------------------------------------------------------------------

	public void setGridImageViewportSize(Dimension value)
	{
		cpGridImageViewportSize.setValue(value);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance variables : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	CPGridImageViewportSize	cpGridImageViewportSize	= new CPGridImageViewportSize();

	//==================================================================


	// PROPERTY CLASS: CLUE DIRECTION KEYWORDS


	private class CPClueDirectionKeywords
		extends Property.PropertyMap<Direction, StringList>
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CPClueDirectionKeywords()
		{
			super(concatenateKeys(Key.CLUE, Key.DIRECTION_KEYWORDS), Direction.class);
			for (Direction key : getMapKeys())
				values.put(key, new StringList(key.getKeywords()));
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		public void parse(Input     input,
						  Direction key)
		{
			String value = input.getValue();
			values.put(key, new StringList(value));
		}

		//--------------------------------------------------------------

		@Override
		public String toString(Direction key)
		{
			return getValue(key).toString(true);
		}

		//--------------------------------------------------------------

		@Override
		protected Iterable<Direction> getMapKeys()
		{
			return Direction.DEFINED_DIRECTIONS;
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public StringList getClueDirectionKeywords(Direction key)
	{
		return cpClueDirectionKeywords.getValue(key);
	}

	//------------------------------------------------------------------

	public void setClueDirectionKeywords(Direction  key,
										 StringList value)
	{
		cpClueDirectionKeywords.setValue(key, value);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance variables : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	CPClueDirectionKeywords	cpClueDirectionKeywords	= new CPClueDirectionKeywords();

	//==================================================================


	// PROPERTY CLASS: CLUE-REFERENCE KEYWORD


	private class CPClueReferenceKeyword
		extends Property.StringProperty
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CPClueReferenceKeyword()
		{
			super(concatenateKeys(Key.CLUE, Key.REFERENCE_KEYWORD));
			value = CrosswordDocument.DEFAULT_CLUE_REFERENCE_KEYWORD;
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public String getClueReferenceKeyword()
	{
		return cpClueReferenceKeyword.getValue();
	}

	//------------------------------------------------------------------

	public void setClueReferenceKeyword(String value)
	{
		cpClueReferenceKeyword.setValue(value);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance variables : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	CPClueReferenceKeyword	cpClueReferenceKeyword	= new CPClueReferenceKeyword();

	//==================================================================


	// PROPERTY CLASS: IMPLICIT DIRECTION OF FIELD


	private class CPImplicitFieldDirection
		extends Property.BooleanProperty
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CPImplicitFieldDirection()
		{
			super(concatenateKeys(Key.CLUE, Key.IMPLICIT_FIELD_DIRECTION));
			value = false;
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public boolean isImplicitFieldDirection()
	{
		return cpImplicitFieldDirection.getValue();
	}

	//------------------------------------------------------------------

	public void setImplicitFieldDirection(boolean value)
	{
		cpImplicitFieldDirection.setValue(value);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance variables : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	CPImplicitFieldDirection	cpImplicitFieldDirection	= new CPImplicitFieldDirection();

	//==================================================================


	// PROPERTY CLASS: ALLOW FIELDS TO BE USED IN MULTIPLE CLUES


	private class CPAllowMultipleFieldUse
		extends Property.BooleanProperty
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CPAllowMultipleFieldUse()
		{
			super(concatenateKeys(Key.CLUE, Key.ALLOW_MULTIPLE_FIELD_USE));
			value = false;
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public boolean isAllowMultipleFieldUse()
	{
		return cpAllowMultipleFieldUse.getValue();
	}

	//------------------------------------------------------------------

	public void setAllowMultipleFieldUse(boolean value)
	{
		cpAllowMultipleFieldUse.setValue(value);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance variables : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	CPAllowMultipleFieldUse	cpAllowMultipleFieldUse	= new CPAllowMultipleFieldUse();

	//==================================================================


	// PROPERTY CLASS: TEXT-SECTION LINE-BREAK SEQUENCE


	private class CPTextSectionLineBreak
		extends Property.StringProperty
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CPTextSectionLineBreak()
		{
			super(concatenateKeys(Key.TEXT_SECTION, Key.LINE_BREAK));
			value = CrosswordDocument.DEFAULT_TEXT_SECTION_LINE_BREAK;
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public String getTextSectionLineBreak()
	{
		return cpTextSectionLineBreak.getValue();
	}

	//------------------------------------------------------------------

	public void setTextSectionLineBreak(String value)
	{
		cpTextSectionLineBreak.setValue(value);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance variables : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	CPTextSectionLineBreak	cpTextSectionLineBreak	= new CPTextSectionLineBreak();

	//==================================================================


	// PROPERTY CLASS: SELECTED CLUE COLUMNS


	private class CPSelectedClueNumColumns
		extends Property.IntegerProperty
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CPSelectedClueNumColumns()
		{
			super(concatenateKeys(Key.VIEW, Key.SELECTED_CLUE_NUM_COLUMNS),
				  CrosswordView.MIN_SELECTED_CLUE_NUM_COLUMNS,
				  CrosswordView.MAX_SELECTED_CLUE_NUM_COLUMNS);
			value = CrosswordView.DEFAULT_SELECTED_CLUE_NUM_COLUMNS;
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public int getSelectedClueNumColumns()
	{
		return cpSelectedClueNumColumns.getValue();
	}

	//------------------------------------------------------------------

	public void setSelectedClueNumColumns(int value)
	{
		cpSelectedClueNumColumns.setValue(value);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance variables : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	CPSelectedClueNumColumns	cpSelectedClueNumColumns	= new CPSelectedClueNumColumns();

	//==================================================================


	// PROPERTY CLASS: VIEW COLOUR


	private class CPViewColour
		extends Property.PropertyMap<CrosswordView.Colour, Color>
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CPViewColour()
		{
			super(concatenateKeys(Key.VIEW, Key.COLOUR), CrosswordView.Colour.class);
			for (CrosswordView.Colour key : CrosswordView.Colour.values())
				values.put(key, key.getDefaultColour());
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		public void parse(Input                input,
						  CrosswordView.Colour key)
		{
			try
			{
				values.put(key, ColourProperty.parseColour(input));
			}
			catch (AppException e)
			{
				showWarningMessage(e);
			}
		}

		//--------------------------------------------------------------

		@Override
		public String toString(CrosswordView.Colour key)
		{
			return ColourUtils.colourToRgbString(getValue(key));
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public Color getViewColour(CrosswordView.Colour key)
	{
		return cpViewColour.getValue(key);
	}

	//------------------------------------------------------------------

	public void setViewColour(CrosswordView.Colour key,
							  Color                value)
	{
		cpViewColour.setValue(key, value);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance variables : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	CPViewColour	cpViewColour	= new CPViewColour();

	//==================================================================


	// PROPERTY CLASS: HTML STYLESHEET KIND


	private class CPHtmlStylesheetKind
		extends Property.EnumProperty<StylesheetKind>
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CPHtmlStylesheetKind()
		{
			super(concatenateKeys(Key.HTML, Key.STYLESHEET_KIND), StylesheetKind.class);
			value = StylesheetKind.EXTERNAL;
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public StylesheetKind getHtmlStylesheetKind()
	{
		return cpHtmlStylesheetKind.getValue();
	}

	//------------------------------------------------------------------

	public void setHtmlStylesheetKind(StylesheetKind value)
	{
		cpHtmlStylesheetKind.setValue(value);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance variables : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	CPHtmlStylesheetKind	cpHtmlStylesheetKind	= new CPHtmlStylesheetKind();

	//==================================================================


	// PROPERTY CLASS: HTML VIEWER COMMAND


	private class CPHtmlViewerCommand
		extends Property.StringProperty
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CPHtmlViewerCommand()
		{
			super(concatenateKeys(Key.HTML, Key.VIEWER_COMMAND));
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public String getHtmlViewerCommand()
	{
		return cpHtmlViewerCommand.getValue();
	}

	//------------------------------------------------------------------

	public void setHtmlViewerCommand(String value)
	{
		cpHtmlViewerCommand.setValue(value);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance variables : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	CPHtmlViewerCommand	cpHtmlViewerCommand	= new CPHtmlViewerCommand();

	//==================================================================


	// PROPERTY CLASS: HTML FONT NAMES


	private class CPHtmlFontNames
		extends Property.SimpleProperty<StringList>
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CPHtmlFontNames()
		{
			super(concatenateKeys(Key.HTML, Key.FONT_NAMES));
			value = new StringList(CrosswordDocument.DEFAULT_FONT_NAMES);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		public void parse(Input input)
			throws AppException
		{
			value = new StringList(input.getValue());
		}

		//--------------------------------------------------------------

		@Override
		public String toString()
		{
			return value.toString(true);
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public StringList getHtmlFontNames()
	{
		return cpHtmlFontNames.getValue();
	}

	//------------------------------------------------------------------

	public void setHtmlFontNames(StringList value)
	{
		cpHtmlFontNames.setValue(value);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance variables : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	CPHtmlFontNames	cpHtmlFontNames	= new CPHtmlFontNames();

	//==================================================================


	// PROPERTY CLASS: HTML FONT SIZE


	private class CPHtmlFontSize
		extends Property.IntegerProperty
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CPHtmlFontSize()
		{
			super(concatenateKeys(Key.HTML, Key.FONT_SIZE), Grid.MIN_HTML_FONT_SIZE, Grid.MAX_HTML_FONT_SIZE);
			value = Grid.DEFAULT_HTML_FONT_SIZE;
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public int getHtmlFontSize()
	{
		return cpHtmlFontSize.getValue();
	}

	//------------------------------------------------------------------

	public void setHtmlFontSize(int value)
	{
		cpHtmlFontSize.setValue(value);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance variables : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	CPHtmlFontSize	cpHtmlFontSize	= new CPHtmlFontSize();

	//==================================================================


	// PROPERTY CLASS: HTML FIELD-NUMBER FONT-SIZE FACTOR


	private class CPHtmlFieldNumFontSizeFactor
		extends Property.DoubleProperty
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CPHtmlFieldNumFontSizeFactor()
		{
			super(concatenateKeys(Key.HTML, Key.FIELD_NUMBER_FONT_SIZE_FACTOR),
				  Grid.MIN_HTML_FIELD_NUM_FONT_SIZE_FACTOR, Grid.MAX_HTML_FIELD_NUM_FONT_SIZE_FACTOR);
			value = Grid.DEFAULT_HTML_FIELD_NUM_FONT_SIZE_FACTOR;
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public double getHtmlFieldNumFontSizeFactor()
	{
		return cpHtmlFieldNumFontSizeFactor.getValue();
	}

	//------------------------------------------------------------------

	public void setHtmlFieldNumFontSizeFactor(double value)
	{
		cpHtmlFieldNumFontSizeFactor.setValue(value);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance variables : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	CPHtmlFieldNumFontSizeFactor	cpHtmlFieldNumFontSizeFactor	= new CPHtmlFieldNumFontSizeFactor();

	//==================================================================


	// PROPERTY CLASS: HTML FIELD-NUMBER OFFSET, TOP


	private class CPHtmlFieldNumOffsetTop
		extends Property.IntegerProperty
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CPHtmlFieldNumOffsetTop()
		{
			super(concatenateKeys(Key.HTML, Key.FIELD_NUMBER_OFFSET_TOP),
				  Grid.MIN_HTML_FIELD_NUM_OFFSET, Grid.MAX_HTML_FIELD_NUM_OFFSET);
			value = Grid.DEFAULT_HTML_FIELD_NUM_OFFSET;
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public int getHtmlFieldNumOffsetTop()
	{
		return cpHtmlFieldNumOffsetTop.getValue();
	}

	//------------------------------------------------------------------

	public void setHtmlFieldNumOffsetTop(int value)
	{
		cpHtmlFieldNumOffsetTop.setValue(value);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance variables : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	CPHtmlFieldNumOffsetTop	cpHtmlFieldNumOffsetTop	= new CPHtmlFieldNumOffsetTop();

	//==================================================================


	// PROPERTY CLASS: HTML FIELD-NUMBER OFFSET, TOP


	private class CPHtmlFieldNumOffsetLeft
		extends Property.IntegerProperty
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CPHtmlFieldNumOffsetLeft()
		{
			super(concatenateKeys(Key.HTML, Key.FIELD_NUMBER_OFFSET_LEFT),
				  Grid.MIN_HTML_FIELD_NUM_OFFSET, Grid.MAX_HTML_FIELD_NUM_OFFSET);
			value = Grid.DEFAULT_HTML_FIELD_NUM_OFFSET;
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public int getHtmlFieldNumOffsetLeft()
	{
		return cpHtmlFieldNumOffsetLeft.getValue();
	}

	//------------------------------------------------------------------

	public void setHtmlFieldNumOffsetLeft(int value)
	{
		cpHtmlFieldNumOffsetLeft.setValue(value);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance variables : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	CPHtmlFieldNumOffsetLeft	cpHtmlFieldNumOffsetLeft	= new CPHtmlFieldNumOffsetLeft();

	//==================================================================


	// PROPERTY CLASS: HTML CELL OFFSET, TOP


	private class CPHtmlCellOffsetTop
		extends Property.IntegerProperty
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CPHtmlCellOffsetTop()
		{
			super(concatenateKeys(Key.HTML, Key.CELL_OFFSET_TOP),
				  Grid.MIN_HTML_CELL_OFFSET, Grid.MAX_HTML_CELL_OFFSET);
			value = Grid.DEFAULT_HTML_CELL_OFFSET;
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public int getHtmlCellOffsetTop()
	{
		return cpHtmlCellOffsetTop.getValue();
	}

	//------------------------------------------------------------------

	public void setHtmlCellOffsetTop(int value)
	{
		cpHtmlCellOffsetTop.setValue(value);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance variables : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	CPHtmlCellOffsetTop	cpHtmlCellOffsetTop	= new CPHtmlCellOffsetTop();

	//==================================================================


	// PROPERTY CLASS: HTML CELL OFFSET, LEFT


	private class CPHtmlCellOffsetLeft
		extends Property.IntegerProperty
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CPHtmlCellOffsetLeft()
		{
			super(concatenateKeys(Key.HTML, Key.CELL_OFFSET_LEFT),
				  Grid.MIN_HTML_CELL_OFFSET, Grid.MAX_HTML_CELL_OFFSET);
			value = Grid.DEFAULT_HTML_CELL_OFFSET;
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public int getHtmlCellOffsetLeft()
	{
		return cpHtmlCellOffsetLeft.getValue();
	}

	//------------------------------------------------------------------

	public void setHtmlCellOffsetLeft(int value)
	{
		cpHtmlCellOffsetLeft.setValue(value);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance variables : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	CPHtmlCellOffsetLeft	cpHtmlCellOffsetLeft	= new CPHtmlCellOffsetLeft();

	//==================================================================


	// PROPERTY CLASS: HTML GRID COLOUR


	private class CPHtmlGridColour
		extends ColourProperty
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CPHtmlGridColour()
		{
			super(concatenateKeys(Key.HTML, Key.GRID_COLOUR));
			value = Grid.DEFAULT_HTML_GRID_COLOUR;
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public Color getHtmlGridColour()
	{
		return cpHtmlGridColour.getValue();
	}

	//------------------------------------------------------------------

	public void setHtmlGridColour(Color value)
	{
		cpHtmlGridColour.setValue(value);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance variables : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	CPHtmlGridColour	cpHtmlGridColour	= new CPHtmlGridColour();

	//==================================================================


	// PROPERTY CLASS: HTML GRID-ENTRY COLOUR


	private class CPHtmlEntryColour
		extends ColourProperty
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CPHtmlEntryColour()
		{
			super(concatenateKeys(Key.HTML, Key.ENTRY_COLOUR));
			value = Grid.DEFAULT_HTML_ENTRY_COLOUR;
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public Color getHtmlEntryColour()
	{
		return cpHtmlEntryColour.getValue();
	}

	//------------------------------------------------------------------

	public void setHtmlEntryColour(Color value)
	{
		cpHtmlEntryColour.setValue(value);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance variables : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	CPHtmlEntryColour	cpHtmlEntryColour	= new CPHtmlEntryColour();

	//==================================================================


	// PROPERTY CLASS: HTML CELL SIZE


	private class CPHtmlCellSize
		extends Property.PropertyMap<Grid.Separator, Integer>
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CPHtmlCellSize()
		{
			super(concatenateKeys(Key.HTML, Key.CELL_SIZE), Grid.Separator.class);
			for (Grid.Separator key : Grid.Separator.values())
				values.put(key, Grid.DEFAULT_HTML_CELL_SIZES.get(key));
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		public void parse(Input          input,
						  Grid.Separator key)
			throws AppException
		{
			IntegerRange range = new IntegerRange(Grid.MIN_HTML_CELL_SIZE, Grid.MAX_HTML_CELL_SIZE);
			values.put(key, input.parseInteger(range));
		}

		//--------------------------------------------------------------

		@Override
		public String toString(Grid.Separator key)
		{
			return Integer.toString(getValue(key));
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public int getHtmlCellSize(Grid.Separator key)
	{
		return cpHtmlCellSize.getValue(key);
	}

	//------------------------------------------------------------------

	public void setHtmlCellSize(Grid.Separator key,
								int            value)
	{
		cpHtmlCellSize.setValue(key, value);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance variables : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	CPHtmlCellSize	cpHtmlCellSize	= new CPHtmlCellSize();

	//==================================================================


	// PROPERTY CLASS: HTML BLOCK IMAGE NUMBER OF LINES


	private class CPBlockImageNumLines
		extends Property.IntegerProperty
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CPBlockImageNumLines()
		{
			super(concatenateKeys(Key.HTML, Key.BLOCK_IMAGE, Key.NUM_LINES),
				  BlockGrid.MIN_BLOCK_IMAGE_NUM_LINES, BlockGrid.MAX_BLOCK_IMAGE_NUM_LINES);
			value = BlockGrid.DEFAULT_BLOCK_IMAGE_NUM_LINES;
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public int getBlockImageNumLines()
	{
		return cpBlockImageNumLines.getValue();
	}

	//------------------------------------------------------------------

	public void setBlockImageNumLines(int value)
	{
		cpBlockImageNumLines.setValue(value);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance variables : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	CPBlockImageNumLines	cpBlockImageNumLines	= new CPBlockImageNumLines();

	//==================================================================


	// PROPERTY CLASS: HTML BLOCK IMAGE LINE WIDTH


	private class CPBlockImageLineWidth
		extends Property.DoubleProperty
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CPBlockImageLineWidth()
		{
			super(concatenateKeys(Key.HTML, Key.BLOCK_IMAGE, Key.LINE_WIDTH),
				  BlockGrid.MIN_BLOCK_IMAGE_LINE_WIDTH, BlockGrid.MAX_BLOCK_IMAGE_LINE_WIDTH);
			value = BlockGrid.DEFAULT_BLOCK_IMAGE_LINE_WIDTH;
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public double getBlockImageLineWidth()
	{
		return cpBlockImageLineWidth.getValue();
	}

	//------------------------------------------------------------------

	public void setBlockImageLineWidth(double value)
	{
		cpBlockImageLineWidth.setValue(value);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance variables : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	CPBlockImageLineWidth	cpBlockImageLineWidth	= new CPBlockImageLineWidth();

	//==================================================================


	// PROPERTY CLASS: HTML BLOCK IMAGE COLOUR


	private class CPBlockImageColour
		extends ColourProperty
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CPBlockImageColour()
		{
			super(concatenateKeys(Key.HTML, Key.BLOCK_IMAGE, Key.COLOUR));
			value = BlockGrid.DEFAULT_BLOCK_IMAGE_COLOUR;
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public Color getBlockImageColour()
	{
		return cpBlockImageColour.getValue();
	}

	//------------------------------------------------------------------

	public void setBlockImageColour(Color value)
	{
		cpBlockImageColour.setValue(value);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance variables : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	CPBlockImageColour	cpBlockImageColour	= new CPBlockImageColour();

	//==================================================================


	// PROPERTY CLASS: HTML BLOCK IMAGE PRINT ONLY


	private class CPBlockImagePrintOnly
		extends Property.BooleanProperty
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CPBlockImagePrintOnly()
		{
			super(concatenateKeys(Key.HTML, Key.BLOCK_IMAGE, Key.PRINT_ONLY));
			value = false;
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public boolean isBlockImagePrintOnly()
	{
		return cpBlockImagePrintOnly.getValue();
	}

	//------------------------------------------------------------------

	public void setBlockImagePrintOnly(boolean value)
	{
		cpBlockImagePrintOnly.setValue(value);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance variables : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	CPBlockImagePrintOnly	cpBlockImagePrintOnly	= new CPBlockImagePrintOnly();

	//==================================================================


	// PROPERTY CLASS: HTML BAR WIDTH IN BAR GRID


	private class CPHtmlBarGridBarWidth
		extends Property.IntegerProperty
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CPHtmlBarGridBarWidth()
		{
			super(concatenateKeys(Key.HTML, Key.BAR_GRID, Key.BAR_WIDTH),
				  GridPanel.Bar.MIN_BAR_WIDTH, GridPanel.Bar.MAX_BAR_WIDTH);
			value = GridPanel.Bar.DEFAULT_BAR_WIDTH;
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public int getHtmlBarGridBarWidth()
	{
		return cpHtmlBarGridBarWidth.getValue();
	}

	//------------------------------------------------------------------

	public void setHtmlBarGridBarWidth(int value)
	{
		cpHtmlBarGridBarWidth.setValue(value);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance variables : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	CPHtmlBarGridBarWidth	cpHtmlBarGridBarWidth	= new CPHtmlBarGridBarWidth();

	//==================================================================


	// PROPERTY CLASS: HTML BAR COLOUR


	private class CPHtmlBarColour
		extends ColourProperty
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CPHtmlBarColour()
		{
			super(concatenateKeys(Key.HTML, Key.BAR_GRID, Key.BAR_COLOUR));
			value = BarGrid.DEFAULT_HTML_GRID_COLOUR;
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public Color getHtmlBarColour()
	{
		return cpHtmlBarColour.getValue();
	}

	//------------------------------------------------------------------

	public void setHtmlBarColour(Color value)
	{
		cpHtmlBarColour.setValue(value);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance variables : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	CPHtmlBarColour	cpHtmlBarColour	= new CPHtmlBarColour();

	//==================================================================


	// PROPERTY CLASS: FONTS


	private class CPFonts
		extends Property.PropertyMap<AppFont, FontEx>
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		private static final	int	DEFAULT_FIELD_NUMBER_FONT_SIZE	= 9;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CPFonts()
		{
			super(Key.FONT, AppFont.class);
			for (AppFont font : AppFont.values())
				values.put(font, new FontEx());
			values.get(AppFont.FIELD_NUMBER).setSize(DEFAULT_FIELD_NUMBER_FONT_SIZE);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		public void parse(Input   input,
						  AppFont appFont)
		{
			try
			{
				FontEx font = new FontEx(input.getValue());
				appFont.setFontEx(font);
				values.put(appFont, font);
			}
			catch (IllegalArgumentException e)
			{
				showWarningMessage(new IllegalValueException(input));
			}
			catch (uk.blankaspect.common.exception.ValueOutOfBoundsException e)
			{
				showWarningMessage(new ValueOutOfBoundsException(input));
			}
		}

		//--------------------------------------------------------------

		@Override
		public String toString(AppFont appFont)
		{
			return getValue(appFont).toString();
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public FontEx getFont(int index)
	{
		return cpFonts.getValue(AppFont.values()[index]);
	}

	//------------------------------------------------------------------

	public void setFont(int    index,
						FontEx font)
	{
		cpFonts.setValue(AppFont.values()[index], font);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance variables : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	CPFonts	cpFonts	= new CPFonts();

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	private AppConfig()
	{
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	public static void showWarningMessage(AppException exception)
	{
		App.INSTANCE.showWarningMessage(App.SHORT_NAME + " : " + CONFIG_ERROR_STR, exception);
	}

	//------------------------------------------------------------------

	public static void showErrorMessage(AppException exception)
	{
		App.INSTANCE.showErrorMessage(App.SHORT_NAME + " : " + CONFIG_ERROR_STR, exception);
	}

	//------------------------------------------------------------------

	private static File getFile()
		throws AppException
	{
		File file = null;

		// Get location of container of class file of application
		Path containerLocation = null;
		try
		{
			containerLocation = ClassUtils.getClassFileContainer(AppConfig.class);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		// Get pathname of configuration directory from properties file
		String pathname = null;
		Path propertiesFile = (containerLocation == null) ? Path.of(PROPERTIES_FILENAME)
														  : containerLocation.resolveSibling(PROPERTIES_FILENAME);
		if (Files.isRegularFile(propertiesFile, LinkOption.NOFOLLOW_LINKS))
		{
			try
			{
				Properties properties = new Properties();
				properties.loadFromXML(new FileInputStream(propertiesFile.toFile()));
				pathname = properties.getProperty(CONFIG_DIR_KEY);
			}
			catch (IOException e)
			{
				throw new FileException(ErrorId.ERROR_READING_PROPERTIES_FILE, propertiesFile.toFile());
			}
		}

		// Get pathname of configuration directory from system property or set system property to pathname
		try
		{
			if (pathname == null)
				pathname = System.getProperty(CONFIG_DIR_KEY);
			else
				System.setProperty(CONFIG_DIR_KEY, pathname);
		}
		catch (SecurityException e)
		{
			// ignore
		}

		// Look for configuration file in default locations
		if (pathname == null)
		{
			// Look for configuration file in local directory
			file = new File(CONFIG_FILENAME);

			// Look for configuration file in default configuration directory
			if (!file.isFile())
			{
				file = null;
				pathname = Utils.getPropertiesPathname();
				if (pathname != null)
				{
					file = new File(pathname, CONFIG_FILENAME);
					if (!file.isFile())
						file = null;
				}
			}
		}

		// Get location of configuration file from pathname of configuration directory
		else if (!pathname.isEmpty())
		{
			file = new File(PathnameUtils.parsePathname(pathname), CONFIG_FILENAME);
			if (!file.isFile())
				throw new FileException(ErrorId.NO_CONFIGURATION_FILE, file);
		}

		return file;
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	public File chooseFile(Component parent)
	{
		if (fileChooser == null)
		{
			fileChooser = new JFileChooser();
			fileChooser.setDialogTitle(SAVE_CONFIGURATION_FILE_STR);
			fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			fileChooser.setFileFilter(new FilenameSuffixFilter(AppConstants.XML_FILES_STR,
															   AppConstants.XML_FILENAME_EXTENSION));
			selectedFile = file;
		}

		fileChooser.setSelectedFile((selectedFile == null) ? new File(CONFIG_FILENAME).getAbsoluteFile()
														   : selectedFile.getAbsoluteFile());
		fileChooser.rescanCurrentDirectory();
		if (fileChooser.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION)
		{
			selectedFile = Utils.appendSuffix(fileChooser.getSelectedFile(),
											  AppConstants.XML_FILENAME_EXTENSION);
			return selectedFile;
		}
		return null;
	}

	//------------------------------------------------------------------

	public void read()
	{
		// Read configuration file
		fileRead = false;
		ConfigFile configFile = null;
		try
		{
			file = getFile();
			if (file != null)
			{
				configFile = new ConfigFile();
				configFile.read(file);
				fileRead = true;
			}
		}
		catch (AppException e)
		{
			showErrorMessage(e);
		}

		// Get properties
		if (fileRead)
			getProperties(configFile, Property.getSystemSource());
		else
			getProperties(Property.getSystemSource());

		// Reset changed status of properties
		resetChanged();
	}

	//------------------------------------------------------------------

	public void write()
	{
		if (isChanged())
		{
			try
			{
				if (file == null)
				{
					if (System.getProperty(CONFIG_DIR_KEY) == null)
					{
						String pathname = Utils.getPropertiesPathname();
						if (pathname != null)
						{
							File directory = new File(pathname);
							if (!directory.exists() && !directory.mkdirs())
								throw new FileException(ErrorId.FAILED_TO_CREATE_DIRECTORY, directory);
							file = new File(directory, CONFIG_FILENAME);
						}
					}
				}
				else
				{
					if (!fileRead)
						file.renameTo(new File(file.getParentFile(), CONFIG_OLD_FILENAME));
				}
				if (file != null)
				{
					write(file);
					resetChanged();
				}
			}
			catch (AppException e)
			{
				showErrorMessage(e);
			}
		}
	}

	//------------------------------------------------------------------

	public void write(File file)
		throws AppException
	{
		// Initialise progress view
		IProgressView progressView = Task.getProgressView();
		if (progressView != null)
		{
			progressView.setInfo(WRITING_STR, file);
			progressView.setProgress(0, -1.0);
		}

		// Create new DOM document
		ConfigFile configFile = new ConfigFile(Integer.toString(VERSION));

		// Set configuration properties in document
		putProperties(configFile);

		// Write file
		configFile.write(file);
	}

	//------------------------------------------------------------------

	private void getProperties(Property.ISource... propertySources)
	{
		for (Property property : getProperties())
		{
			try
			{
				property.get(propertySources);
			}
			catch (AppException e)
			{
				showWarningMessage(e);
			}
		}
	}

	//------------------------------------------------------------------

	private void putProperties(Property.ITarget propertyTarget)
	{
		for (Property property : getProperties())
			property.put(propertyTarget);
	}

	//------------------------------------------------------------------

	private boolean isChanged()
	{
		for (Property property : getProperties())
		{
			if (property.isChanged())
				return true;
		}
		return false;
	}

	//------------------------------------------------------------------

	private void resetChanged()
	{
		for (Property property : getProperties())
			property.setChanged(false);
	}

	//------------------------------------------------------------------

	private List<Property> getProperties()
	{
		if (properties == null)
		{
			properties = new ArrayList<>();
			for (Field field : getClass().getDeclaredFields())
			{
				try
				{
					if (field.getName().startsWith(Property.FIELD_PREFIX))
						properties.add((Property)field.get(this));
				}
				catch (IllegalAccessException e)
				{
					e.printStackTrace();
				}
			}
		}
		return properties;
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Static initialiser
////////////////////////////////////////////////////////////////////////

	static
	{
		INSTANCE = new AppConfig();
	}

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private	File			file;
	private	boolean			fileRead;
	private	File			selectedFile;
	private	JFileChooser	fileChooser;
	private	List<Property>	properties;

}

//----------------------------------------------------------------------
