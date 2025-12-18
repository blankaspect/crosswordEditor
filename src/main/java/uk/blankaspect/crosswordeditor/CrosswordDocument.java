/*====================================================================*\

CrosswordDocument.java

Crossword document class.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.crosswordeditor;

//----------------------------------------------------------------------


// IMPORTS


import java.awt.Color;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import java.beans.PropertyChangeListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.net.URL;
import java.net.URLConnection;

import java.nio.charset.StandardCharsets;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import java.util.function.Predicate;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import uk.blankaspect.common.collection.ArraySet;

import uk.blankaspect.common.css.CssMediaRule;
import uk.blankaspect.common.css.CssProperty;
import uk.blankaspect.common.css.CssRuleSet;
import uk.blankaspect.common.css.CssSelector;
import uk.blankaspect.common.css.CssUtils;

import uk.blankaspect.common.exception.AppException;
import uk.blankaspect.common.exception.FileException;
import uk.blankaspect.common.exception.TaskCancelledException;
import uk.blankaspect.common.exception.TempFileException;
import uk.blankaspect.common.exception.UrlException;

import uk.blankaspect.common.exception2.UnexpectedRuntimeException;

import uk.blankaspect.common.filesystem.FilenameUtils;

import uk.blankaspect.common.misc.EditList;
import uk.blankaspect.common.misc.FileWritingMode;
import uk.blankaspect.common.misc.TextFile;

import uk.blankaspect.common.regex.RegexUtils;
import uk.blankaspect.common.regex.Substitution;

import uk.blankaspect.common.string.StringUtils;

import uk.blankaspect.common.tuple.StrKVPair;

import uk.blankaspect.common.xml.AttributeList;
import uk.blankaspect.common.xml.XmlConstants;
import uk.blankaspect.common.xml.XmlFile;
import uk.blankaspect.common.xml.XmlParseException;
import uk.blankaspect.common.xml.XmlUtils;
import uk.blankaspect.common.xml.XmlWriter;

import uk.blankaspect.ui.swing.image.PngOutputFile;

//----------------------------------------------------------------------


// CROSSWORD DOCUMENT CLASS


class CrosswordDocument
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	public static final		int		MIN_MAX_EDIT_LIST_LENGTH		= 1;
	public static final		int		MAX_MAX_EDIT_LIST_LENGTH		= 9999;
	public static final		int		DEFAULT_MAX_EDIT_LIST_LENGTH	= 200;

	public static final		String	LINE_BREAK_REGEX	= "(?<%s%s)\\n";

	public static final		String	DEFAULT_FILENAME_SUFFIX	= ".xword";

	public static final		String	DEFAULT_MULTIPLE_FIELD_CLUE_ID_SEPARATOR	= ",";

	public static final		String	DEFAULT_CLUE_REFERENCE_KEYWORD	= "See";

	public static final		String	DEFAULT_TEXT_SECTION_LINE_BREAK	= "$$";

	public static final		String[]	DEFAULT_FONT_NAMES	= { "Arial", "Helvetica", "sans-serif" };

	enum TextSection
	{
		TITLE,
		PROLOGUE,
		EPILOGUE
	}

	private static final	int		INDENT_INCREMENT		= 2;
	private static final	int		MAX_TEXT_LINE_LENGTH	= 80;

	private static final	int		MIN_SUPPORTED_VERSION	= 0;
	private static final	int		MAX_SUPPORTED_VERSION	= 0;
	private static final	int		VERSION					= 0;

	private static final	String	NAMESPACE_NAME			= "http://ns.blankaspect.uk/crossword-1";
	private static final	String	NAMESPACE_NAME_REGEX	= "http://ns\\.[a-z.]+/crossword-1";

	private static final	String	XHTML_PUBLIC_ID	= "-//W3C//DTD XHTML 1.1//EN";
	private static final	String	XHTML_SYSTEM_ID	= "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd";

	private static final	String	STYLESHEET_PATHNAME_PREFIX	= "style/crossword-";
	private static final	String	STYLESHEET_PATHNAME_SUFFIX	= ".css";

	private static final	String	STYLE_PREFIX	= "/*<![CDATA[*/";
	private static final	String	STYLE_SUFFIX	= "/*]]>*/";

	private static final	String	XML_VERSION_STR			= "1.0";
	private static final	String	HTML_NAMESPACE_NAME		= "http://www.w3.org/1999/xhtml";
	private static final	String	LANG_STR				= "en";
	private static final	String	CONTENT_TYPE_STR		= "content-type";
	private static final	String	MIME_TYPE_STR			= "application/xhtml+xml;charset=UTF-8";
	private static final	String	STYLESHEET_STR			= "stylesheet";
	private static final	String	TEXT_CSS_STR			= "text/css";
	private static final	String	FIELD_ID_SEPARATOR		= ",&nbsp;";
	private static final	String	UNNAMED_STR				= "Unnamed";
	private static final	String	READING_STR				= "Reading";
	private static final	String	WRITING_STR				= "Writing";
	private static final	String	CONNECTING_TO_STR		= "Connecting to";
	private static final	String	READ_SOLUTION_STR		= "Read solution";
	private static final	String	UNDO_STR				= "Undo";
	private static final	String	REDO_STR				= "Redo";
	private static final	String	CLEAR_EDIT_LIST_STR		= "Do you want to clear all the undo/redo actions?";
	private static final	String	CLEAR_CLUES_STR			= "Clear clues";
	private static final	String	STYLESHEET_COMMENT_STR	= "Stylesheet for crossword : %s grid, cell size = %d";
	private static final	String	IMPORT_ENTRIES_STR		=
			"Do you want to import the grid entries from the clipboard?";
	private static final	String	CLEAR_ENTRIES_STR		= "Do you want to clear all the grid entries?";
	private static final	String	SOLUTION_AND_PROPS_STR	= "solution and properties";
	private static final	String	SHOW_SOLUTION1_STR		= "Do you want to show the solution?";
	private static final	String	SHOW_SOLUTION2_STR		=
			"Do you want to replace the current entries with the solution?";
	private static final	String	SHOW_STR				= "Show";
	private static final	String	SET_SOLUTION_STR		=
			"Do you want to set the solution to the current grid entries?";
	private static final	String	SET_STR					= "Set";
	private static final	String	IMPORT_SOLUTION_STR		= "Do you want to import the solution from the clipboard?";
	private static final	String	IMPORT_STR				= "Import";
	private static final	String	CLEAR_SOLUTION_STR		= "Do you want to clear the solution?";
	private static final	String	LOCATION_STR			= "Location: ";
	private static final	String	LOAD_SOLUTION1_STR		= "Load solution";
	private static final	String	LOAD_SOLUTION2_STR		= "Do you want to load the solution from this location?";
	private static final	String	LOAD_STR				= "Load";
	private static final	String	CLUE_LIST_ERRORS_STR	= "Errors in clue lists";
	private static final	String	NO_CLUE_STR				= "Fields for which there is no clue";
	private static final	String	MULTIPLE_CLUES_STR		= "Fields for which there are multiple clues";
	private static final	String	NO_FIELD_STR			= "Clue IDs for which there is no field";
	private static final	String	NO_REFERENCE_STR		= "Secondary clue IDs for which there is no reference";
	private static final	String	MULTIPLE_REFERENCES_STR	=
			"Secondary clue IDs for which there are multiple references";
	private static final	String	INCORRECT_LENGTH_STR	= "Clues whose length is incorrect";
	private static final	String	REF_NO_FIELD_STR		= "References to a non-existent field";
	private static final	String	REF_NO_CLUE_STR			= "References to a non-existent clue";
	private static final	String	REF_MULTIPLE_CLUES_STR	= "References to multiple clues";
	private static final	String	REF_NOT_TARGET_ID_STR	=
			"References that are not a secondary ID of the reference target";

	private static final	Pattern	WORD_PATTERN		= Pattern.compile("([^ \\t]+?)([ \\t]+|\\z)");
	private static final	Pattern	WORD_SPACE_PATTERN	= Pattern.compile("([^ \\t]+?[ \\t]*)([ \\t]|\\z)");

	private static final	CssRuleSet	DOCUMENT_RULE_SET	= CssRuleSet.of
	(
		HtmlConstants.ElementName.BODY,
		StrKVPair.of(CssProperty.COLOUR,            "#000000"),
		StrKVPair.of(CssProperty.BACKGROUND_COLOUR, "#FFFFFF"),
		StrKVPair.of(CssProperty.MARGIN,            "1.0em 0.5em"),
		StrKVPair.of(CssProperty.FONT_SIZE,         "%dpt")
	);

	private static final	List<CssRuleSet>	HEADER_RULE_SETS	= List.of
	(
		CssRuleSet.of
		(
			HtmlConstants.ElementName.H4,
			StrKVPair.of(CssProperty.MARGIN,      "0.5em 0"),
			StrKVPair.of(CssProperty.FONT_WEIGHT, "bold")
		),
		CssRuleSet.of
		(
			HtmlConstants.ElementName.H4 + CssSelector.ID + HtmlConstants.Id.TITLE,
			StrKVPair.of(CssProperty.MARGIN_BOTTOM, "1.0em")
		)
	);

	private static final	List<CssRuleSet>	TEXT_RULE_SETS	= List.of
	(
		CssRuleSet.of
		(
			HtmlConstants.ElementName.DIV + CssSelector.ID + HtmlConstants.Id.CLUES
												+ CssSelector.CHILD + HtmlConstants.ElementName.DIV,
			StrKVPair.of(CssProperty.DISPLAY, "table-cell"),
			StrKVPair.of(CssProperty.PADDING, "0 0.8em")
		),
		CssRuleSet.of
		(
			HtmlConstants.ElementName.DIV + CssSelector.ID + HtmlConstants.Id.CLUES
												+ CssSelector.CHILD + HtmlConstants.ElementName.DIV
												+ CssSelector.FIRST_CHILD,
			StrKVPair.of(CssProperty.PADDING_LEFT, "0")
		),
		CssRuleSet.of
		(
			HtmlConstants.ElementName.DIV + CssSelector.ID + HtmlConstants.Id.CLUES
												+ CssSelector.CHILD + HtmlConstants.ElementName.DIV
												+ CssSelector.CHILD + HtmlConstants.ElementName.DIV,
			StrKVPair.of(CssProperty.DISPLAY, "table")
		),
		CssRuleSet.of
		(
			HtmlConstants.ElementName.DIV + CssSelector.ID + HtmlConstants.Id.CLUES
												+ CssSelector.CHILD + HtmlConstants.ElementName.DIV
												+ CssSelector.CHILD + HtmlConstants.ElementName.DIV
												+ CssSelector.CHILD + HtmlConstants.ElementName.DIV,
			StrKVPair.of(CssProperty.DISPLAY, "table-row")
		),
		CssRuleSet.of
		(
			HtmlConstants.ElementName.DIV + CssSelector.ID + HtmlConstants.Id.CLUES
												+ CssSelector.CHILD + HtmlConstants.ElementName.DIV
												+ CssSelector.CHILD + HtmlConstants.ElementName.DIV
												+ CssSelector.CHILD + HtmlConstants.ElementName.DIV
												+ CssSelector.CHILD + HtmlConstants.ElementName.DIV,
			StrKVPair.of(CssProperty.DISPLAY,      "table-cell"),
			StrKVPair.of(CssProperty.PADDING_LEFT, "0.6em")
		),
		CssRuleSet.of
		(
			HtmlConstants.ElementName.DIV + CssSelector.ID + HtmlConstants.Id.CLUES
												+ CssSelector.CHILD + HtmlConstants.ElementName.DIV
												+ CssSelector.CHILD + HtmlConstants.ElementName.DIV
												+ CssSelector.CHILD + HtmlConstants.ElementName.DIV
												+ CssSelector.CHILD + HtmlConstants.ElementName.DIV
												+ CssSelector.FIRST_CHILD,
			StrKVPair.of(CssProperty.TEXT_ALIGN,   "right"),
			StrKVPair.of(CssProperty.PADDING_LEFT, "0"),
			StrKVPair.of(CssProperty.FONT_WEIGHT,  "bold")
		),
		CssRuleSet.of
		(
			HtmlConstants.ElementName.DIV + CssSelector.ID + HtmlConstants.Id.CLUES
												+ CssSelector.CHILD + HtmlConstants.ElementName.DIV
												+ CssSelector.CHILD + HtmlConstants.ElementName.DIV
												+ CssSelector.CHILD + HtmlConstants.ElementName.DIV
												+ CssSelector.CHILD + HtmlConstants.ElementName.DIV
												+ CssSelector.CLASS + HtmlConstants.Class.MULTI_FIELD_CLUE,
			StrKVPair.of(CssProperty.TEXT_INDENT, "-0.6em")
		),
		CssRuleSet.of
		(
			HtmlConstants.ElementName.DIV + CssSelector.ID + HtmlConstants.Id.CLUES
												+ CssSelector.DESCENDANT + HtmlConstants.ElementName.SPAN
												+ CssSelector.CLASS + HtmlConstants.Class.SECONDARY_IDS,
			StrKVPair.of(CssProperty.PADDING_RIGHT, "0.6em"),
			StrKVPair.of(CssProperty.FONT_WEIGHT,   "bold")
		),
		CssRuleSet.of
		(
			HtmlConstants.ElementName.DIV + CssSelector.ID + HtmlConstants.Id.PROLOGUE,
			StrKVPair.of(CssProperty.MARGIN_BOTTOM, "1.2em")
		),
		CssRuleSet.of
		(
			HtmlConstants.ElementName.DIV + CssSelector.ID + HtmlConstants.Id.EPILOGUE,
			StrKVPair.of(CssProperty.MARGIN_TOP, "1.5em")
		),
		CssRuleSet.of
		(
			HtmlConstants.ElementName.SPAN + CssSelector.CLASS + HtmlConstants.Class.STRIKE,
			StrKVPair.of(CssProperty.TEXT_DECORATION, "line-through")
		),
		CssRuleSet.of
		(
			HtmlConstants.ElementName.SPAN + CssSelector.CLASS + HtmlConstants.Class.UNDERLINE,
			StrKVPair.of(CssProperty.TEXT_DECORATION, "underline")
		)
	);

	private interface ElementName
	{
		String	ANSWER_LENGTH	= AppConstants.NS_PREFIX + "answerLength";
		String	CLUE			= AppConstants.NS_PREFIX + "clue";
		String	CLUE_REFERENCE	= AppConstants.NS_PREFIX + "clueReference";
		String	CLUES			= AppConstants.NS_PREFIX + "clues";
		String	CROSSWORD		= "crossword";
		String	EPILOGUE		= AppConstants.NS_PREFIX + "epilogue";
		String	INDICATIONS		= AppConstants.NS_PREFIX + "indications";
		String	LINE			= AppConstants.NS_PREFIX + "line";
		String	LINE_BREAK		= AppConstants.NS_PREFIX + "lineBreak";
		String	PROLOGUE		= AppConstants.NS_PREFIX + "prologue";
		String	SUBSTITUTION	= AppConstants.NS_PREFIX + "substitution";
	}

	private interface AttrName
	{
		String	DIRECTION	= AppConstants.NS_PREFIX + "direction";
		String	IDS			= AppConstants.NS_PREFIX + "ids";
		String	INDEX		= AppConstants.NS_PREFIX + "index";
		String	PATTERN		= AppConstants.NS_PREFIX + "pattern";
		String	TITLE		= AppConstants.NS_PREFIX + "title";
		String	VERSION		= AppConstants.NS_PREFIX + "version";
		String	XMLNS		= "xmlns";
	}

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private	File						file;
	private	File						exportHtmlFile;
	private	long						timestamp;
	private	int							unnamedIndex;
	private	boolean						executingCommand;
	private	String						clueReferenceKeyword;
	private	String						answerLengthPattern;
	private	List<Substitution>			answerLengthSubstitutions;
	private	String						lineBreak;
	private	List<Substitution>			clueSubstitutions;
	private	String						title;
	private	List<String>				prologueParagraphs;
	private	List<String>				epilogueParagraphs;
	private	String						filenameStem;
	private	File						documentDirectory;
	private	File						htmlDirectory;
	private	boolean						showFieldNumbers;
	private	boolean						showClues;
	private	Grid						grid;
	private	Map<Direction, List<Clue>>	clueLists;
	private	SolutionProperties			solutionProperties;
	private	EditList<CrosswordDocument>	editList;

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	public CrosswordDocument()
	{
		AppConfig config = AppConfig.INSTANCE;
		clueReferenceKeyword = config.getClueReferenceKeyword();
		answerLengthSubstitutions = new ArrayList<>();
		lineBreak = config.getTextSectionLineBreak();
		clueSubstitutions = new ArrayList<>();
		prologueParagraphs = new ArrayList<>();
		epilogueParagraphs = new ArrayList<>();
		showFieldNumbers = true;
		showClues = true;
		clueLists = new EnumMap<>(Direction.class);
		solutionProperties = new SolutionProperties();
		editList = new EditList<>(config.getMaxEditListLength());
	}

	//------------------------------------------------------------------

	public CrosswordDocument(int unnamedIndex)
	{
		this();
		this.unnamedIndex = unnamedIndex;
		editList.setChanged();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	private static MainWindow getWindow()
	{
		return CrosswordEditorApp.INSTANCE.getMainWindow();
	}

	//------------------------------------------------------------------

	private static void writeStyle(XmlWriter                       writer,
								   int                             indent,
								   EnumSet<CssMediaRule.MediaType> mediaTypes,
								   List<CssRuleSet>                ruleSets)
		throws IOException
	{
		List<String> mediaTypeStrs = new ArrayList<>();
		for (CssMediaRule.MediaType mediaType : mediaTypes)
			mediaTypeStrs.add(mediaType.getKey());

		AttributeList attributes = new AttributeList();
		attributes.add(HtmlConstants.AttrName.TYPE, TEXT_CSS_STR);
		attributes.add(HtmlConstants.AttrName.MEDIA, StringUtils.join(',', mediaTypeStrs));
		writer.writeElementStart(HtmlConstants.ElementName.STYLE, attributes, indent, true, false);

		indent += INDENT_INCREMENT;

		writer.writeSpaces(indent);
		writer.write(STYLE_PREFIX);
		writer.writeEol();

		for (CssRuleSet ruleSet : ruleSets)
		{
			for (String str : ruleSet.toStrings(indent))
			{
				writer.write(str);
				writer.writeEol();
			}
		}

		writer.writeSpaces(indent);
		writer.write(STYLE_SUFFIX);
		writer.writeEol();

		indent -= INDENT_INCREMENT;

		writer.writeElementEnd(HtmlConstants.ElementName.STYLE, indent);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	public File getFile()
	{
		return file;
	}

	//------------------------------------------------------------------

	public File getExportHtmlFile()
	{
		File outFile = exportHtmlFile;
		if (outFile == null)
		{
			if ((htmlDirectory != null) && (filenameStem != null))
				outFile = new File(htmlDirectory, filenameStem + AppConstants.HTML_FILENAME_EXTENSION);
		}
		return outFile;
	}

	//------------------------------------------------------------------

	public long getTimestamp()
	{
		return timestamp;
	}

	//------------------------------------------------------------------

	public boolean isExecutingCommand()
	{
		return executingCommand;
	}

	//------------------------------------------------------------------

	public boolean isChanged()
	{
		return editList.isChanged();
	}

	//------------------------------------------------------------------

	public String getTitle()
	{
		return title;
	}

	//------------------------------------------------------------------

	public String getClueReferenceKeyword()
	{
		return clueReferenceKeyword;
	}

	//------------------------------------------------------------------

	public List<String> getPrologue()
	{
		return prologueParagraphs;
	}

	//------------------------------------------------------------------

	public List<String> getEpilogue()
	{
		return epilogueParagraphs;
	}

	//------------------------------------------------------------------

	public boolean isShowFieldNumbers()
	{
		return showFieldNumbers;
	}

	//------------------------------------------------------------------

	public boolean isShowClues()
	{
		return showClues;
	}

	//------------------------------------------------------------------

	public Grid getGrid()
	{
		return grid;
	}

	//------------------------------------------------------------------

	public List<Clue> getClues(Direction direction)
	{
		return clueLists.containsKey(direction) ? Collections.unmodifiableList(clueLists.get(direction))
												: new ArrayList<>();
	}

	//------------------------------------------------------------------

	public void setTimestamp(long timestamp)
	{
		this.timestamp = timestamp;
	}

	//------------------------------------------------------------------

	public void setClueReferenceKeyword(String keyword)
	{
		clueReferenceKeyword = keyword;
	}

	//------------------------------------------------------------------

	public void setAnswerLengthPattern(String pattern)
	{
		answerLengthPattern = pattern;
	}

	//------------------------------------------------------------------

	public void setAnswerLengthSubstitutions(List<Substitution> substitutions)
	{
		answerLengthSubstitutions = substitutions;
	}

	//------------------------------------------------------------------

	public void setClueSubstitutions(List<Substitution> substitutions)
	{
		clueSubstitutions = substitutions;
	}

	//------------------------------------------------------------------

	public void setTitle(String title)
	{
		this.title = title;
	}

	//------------------------------------------------------------------

	public void setPrologue(List<String> paragraphs)
	{
		prologueParagraphs = (paragraphs == null) ? new ArrayList<>() : new ArrayList<>(paragraphs);
	}

	//------------------------------------------------------------------

	public void setEpilogue(List<String> paragraphs)
	{
		epilogueParagraphs = (paragraphs == null) ? new ArrayList<>() : new ArrayList<>(paragraphs);
	}

	//------------------------------------------------------------------

	public void setFilenameStem(String filename)
	{
		filenameStem = filename;
	}

	//------------------------------------------------------------------

	public void setDocumentDirectory(File directory)
	{
		documentDirectory = directory;
	}

	//------------------------------------------------------------------

	public void setHtmlDirectory(File directory)
	{
		htmlDirectory = directory;
	}

	//------------------------------------------------------------------

	public void setGrid(Grid grid)
	{
		this.grid = grid;
	}

	//------------------------------------------------------------------

	public void setClues(Direction  direction,
						 List<Clue> clues)
	{
		// Sort the clues
		clues.sort(Clue.COMPARATOR);

		// Set indices of clues with the same field ID
		int prevFieldNumber = 0;
		int index = 0;
		for (int i = 0; i < clues.size(); i++)
		{
			int fieldNumber = clues.get(i).getFieldId().number;
			if (fieldNumber == prevFieldNumber)
			{
				if (index == 0)
					clues.get(i - 1).setIndex(++index);
				clues.get(i).setIndex(++index);
			}
			else
				index = 0;
			prevFieldNumber = fieldNumber;
		}

		// Set the clues in the list
		clueLists.put(direction, clues);
	}

	//------------------------------------------------------------------

	public void setSolutionProperties(SolutionProperties properties)
	{
		solutionProperties = (properties == null) ? null : properties.clone();
	}

	//------------------------------------------------------------------

	public File getOutputFile()
	{
		File outFile = file;
		if (outFile == null)
		{
			if ((documentDirectory != null) && (filenameStem != null))
				outFile = new File(documentDirectory, filenameStem + AppConfig.INSTANCE.getFilenameSuffix());
		}
		return outFile;
	}

	//------------------------------------------------------------------

	public String getName(boolean fullPathname)
	{
		return (file == null)
					? UNNAMED_STR + unnamedIndex
					: fullPathname
							? Utils.getPathname(file)
							: file.getName();
	}

	//------------------------------------------------------------------

	public String getTitleString(boolean fullPathname)
	{
		String str = getName(fullPathname);
		if (isChanged())
			str += AppConstants.FILE_CHANGED_SUFFIX;
		return str;
	}

	//------------------------------------------------------------------

	public String getClueIdString(Direction direction,
								  Clue      clue)
	{
		boolean implicitDirection = AppConfig.INSTANCE.isImplicitFieldDirection();
		return StringUtils.join(", ", getClueIdStrings(direction, clue, implicitDirection));
	}

	//------------------------------------------------------------------

	public String getClueReferenceString(Direction direction,
										 Clue      clue)
	{
		Clue.Id refId = clue.getReferentId().clone();
		if ((refId.fieldId.direction == direction) ||
			 (grid.findFields(refId.fieldId.undefined()).size() == 1))
			refId.fieldId.direction = Direction.NONE;
		return ((clueReferenceKeyword == null) ? DEFAULT_CLUE_REFERENCE_KEYWORD : clueReferenceKeyword)
				+ " " + refId.toString();
	}

	//------------------------------------------------------------------

	public List<Clue.Id> getClueIds(Clue.Id clueId)
	{
		List<Clue.Id> clueIds = new ArraySet<>();
		Clue primaryClue = findPrimaryClue(clueId);
		if (primaryClue != null)
		{
			clueIds.add(primaryClue.getId());
			for (int i = 1; i < primaryClue.getNumFields(); i++)
			{
				for (Clue clue : findClues(primaryClue.getFieldId(i)))
					clueIds.add(clue.getId());
			}
		}
		return clueIds;
	}

	//------------------------------------------------------------------

	public Clue findClue(Clue.Id clueId)
	{
		if (clueLists.containsKey(clueId.fieldId.direction))
		{
			for (Clue clue : clueLists.get(clueId.fieldId.direction))
			{
				if (clue.getId().equals(clueId))
					return clue;
			}
		}
		return null;
	}

	//------------------------------------------------------------------

	public List<Clue> findClues(Grid.Field.Id fieldId)
	{
		List<Clue> clues = new ArrayList<>();
		if (clueLists.containsKey(fieldId.direction))
		{
			for (Clue clue : clueLists.get(fieldId.direction))
			{
				if (clue.getFieldId().equals(fieldId))
					clues.add(clue);
			}
		}

		return clues;
	}

	//------------------------------------------------------------------

	public Clue findPrimaryClue(Clue.Id clueId)
	{
		Clue clue = findClue(clueId);
		while ((clue != null) && clue.isReference())
			clue = findClue(clue.getReferentId());
		return clue;
	}

	//------------------------------------------------------------------

	public List<Clue> findPrimaryClues(Grid.Field.Id fieldId)
	{
		List<Clue> clues = new ArrayList<>();
		for (Clue clue : findClues(fieldId))
		{
			while ((clue != null) && clue.isReference())
				clue = findClue(clue.getReferentId());
			clues.add(clue);
		}
		return clues;
	}

	//------------------------------------------------------------------

	public void updateCommands()
	{
		Command.setAllEnabled(true);

		CrosswordView view = getView();
		boolean isSolution = grid.hasSolution();
		boolean isClues = !clueLists.isEmpty();
		boolean isEntries = !grid.isEntriesEmpty();

		Command.UNDO.setEnabled(editList.canUndo());
		EditList.Element<CrosswordDocument> edit = editList.getUndo();
		String text = (edit == null) ? null : edit.getText();
		text = StringUtils.isNullOrEmpty(text) ? UNDO_STR : UNDO_STR + " " + text;
		Command.UNDO.putValue(Action.NAME, text);

		Command.REDO.setEnabled(editList.canRedo());
		edit = editList.getRedo();
		text = (edit == null) ? null : edit.getText();
		text = StringUtils.isNullOrEmpty(text) ? REDO_STR : REDO_STR + " " + text;
		Command.REDO.putValue(Action.NAME, text);

		Command.CLEAR_EDIT_LIST.setEnabled(!editList.isEmpty());
		Command.EDIT_CLUE.setEnabled((view != null) && (view.getSelectedClueId() != null));
		Command.EDIT_GRID.setEnabled(!isClues);
		Command.COPY_CLUES_TO_CLIPBOARD.setEnabled(isClues);
		Command.COPY_ENTRIES_TO_CLIPBOARD.setEnabled(isEntries);
		Command.IMPORT_ENTRIES_FROM_CLIPBOARD.setEnabled(Utils.clipboardHasText());
		Command.CLEAR_ENTRIES.setEnabled(isEntries);
		Command.HIGHLIGHT_INCORRECT_ENTRIES.setEnabled(isEntries && isSolution);
		Command.SHOW_SOLUTION.setEnabled(isSolution);
		Command.SET_SOLUTION.setEnabled(grid.isEntriesComplete());
		Command.IMPORT_SOLUTION_FROM_CLIPBOARD.setEnabled(Utils.clipboardHasText());
		Command.LOAD_SOLUTION.setEnabled(solutionProperties.canLoad());
		Command.CLEAR_SOLUTION.setEnabled(isSolution);
		Command.COPY_SOLUTION_TO_CLIPBOARD.setEnabled(isSolution);
		Command.TOGGLE_FIELD_NUMBERS.setSelected(showFieldNumbers);
		Command.TOGGLE_CLUES.setSelected(showClues);
		Command.RESIZE_WINDOW_TO_VIEW.setEnabled(!getWindow().isMaximised());
	}

	//------------------------------------------------------------------

	public void executeCommand(Command command)
	{
		// Set command execution flag
		executingCommand = true;

		// Perform command
		EditList.Element<CrosswordDocument> edit = null;
		try
		{
			try
			{
				edit = switch (command)
				{
					case UNDO                            -> onUndo();
					case REDO                            -> onRedo();
					case CLEAR_EDIT_LIST                 -> onClearEditList();
					case EDIT_CLUE                       -> onEditClue();
					case EDIT_GRID                       -> onEditGrid();
					case EDIT_TEXT_SECTIONS              -> onEditTextSections();
					case EDIT_INDICATIONS                -> onEditIndications();
					case SET_ENTRY_CHARACTER             -> onSetEntryCharacter();
					case COPY_CLUES_TO_CLIPBOARD         -> onCopyCluesToClipboard();
					case IMPORT_CLUES_FROM_CLIPBOARD     -> onImportCluesFromClipboard();
					case CLEAR_CLUES                     -> onClearClues();
					case COPY_ENTRIES_TO_CLIPBOARD       -> onCopyEntriesToClipboard();
					case IMPORT_ENTRIES_FROM_CLIPBOARD   -> onImportEntriesFromClipboard();
					case CLEAR_ENTRIES                   -> onClearEntries();
					case COPY_FIELD_NUMBERS_TO_CLIPBOARD -> onCopyFieldNumbersToClipboard();
					case COPY_FIELD_IDS_TO_CLIPBOARD     -> onCopyFieldIdsToClipboard();
					case HIGHLIGHT_INCORRECT_ENTRIES     -> onHighlightIncorrectEntries();
					case SHOW_SOLUTION                   -> onShowSolution();
					case SET_SOLUTION                    -> onSetSolution();
					case IMPORT_SOLUTION_FROM_CLIPBOARD  -> onImportSolutionFromClipboard();
					case LOAD_SOLUTION                   -> onLoadSolution();
					case CLEAR_SOLUTION                  -> onClearSolution();
					case COPY_SOLUTION_TO_CLIPBOARD      -> onCopySolutionToClipboard();
					case EDIT_SOLUTION_PROPERTIES        -> onEditSolutionProperties();
					case TOGGLE_FIELD_NUMBERS            -> onToggleFieldNumbers();
					case TOGGLE_CLUES                    -> onToggleClues();
					case RESIZE_WINDOW_TO_VIEW           -> onResizeWindowToView();
				};
			}
			catch (OutOfMemoryError e)
			{
				throw new AppException(ErrorId.NOT_ENOUGH_MEMORY_TO_PERFORM_COMMAND);
			}
		}
		catch (AppException e)
		{
			CrosswordEditorApp.INSTANCE.showErrorMessage(CrosswordEditorApp.SHORT_NAME, e);
		}

		// Add edit to undo list
		if (edit != null)
			editList.add(edit);

		// Update title, menus and status in main window
		CrosswordEditorApp.INSTANCE.updateTabText(this);
		getWindow().updateAll();

		// Clear command execution flag
		executingCommand = false;
	}

	//------------------------------------------------------------------

	public void updateClueDirections()
	{
		for (Direction direction : clueLists.keySet())
		{
			for (Clue clue : clueLists.get(direction))
			{
				if (clue.getNumFields() > 0)
					clue.getFieldId().direction = direction;

				if (clue.isReference())
				{
					Grid.Field.Id refFieldId = clue.getReferentId().fieldId;
					if (refFieldId.direction == Direction.NONE)
					{
						List<Grid.Field> fields = grid.findFields(refFieldId);
						if (!fields.isEmpty())
							refFieldId.direction = (fields.size() == 1) ? fields.get(0).getDirection() : direction;
					}
				}
				else
				{
					for (int i = 0; i < clue.getNumFields(); i++)
					{
						Grid.Field.Id fieldId = clue.getFieldId(i);
						if (fieldId.direction == Direction.NONE)
						{
							List<Grid.Field> fields = grid.findFields(fieldId);
							if (!fields.isEmpty())
								fieldId.direction = (fields.size() == 1) ? fields.get(0).getDirection() : direction;
						}
					}
				}
			}
		}
	}

	//------------------------------------------------------------------

	public void validateClues()
	{
		if (!clueLists.isEmpty() && !validateClues(CLEAR_CLUES_STR))
			clueLists.clear();
	}

	//------------------------------------------------------------------

	public boolean validateClues(String closeStr)
	{
		// Initialise lists of clue IDs and reference IDs
		List<Grid.Field.Id> clueIds = new ArraySet<>();

		List<Grid.Field.Id> noClueIds = new ArraySet<>();
		List<Grid.Field.Id> multipleCluesIds = new ArraySet<>();
		List<Grid.Field.Id> noFieldIds = new ArraySet<>();
		List<Grid.Field.Id> invalidFieldIds = new ArraySet<>();
		List<Grid.Field.Id> noReferenceIds = new ArraySet<>();
		List<Grid.Field.Id> multipleReferencesIds = new ArraySet<>();
		List<Grid.Field.Id> incorrectLengthIds = new ArraySet<>();

		List<Grid.Field.Id> refNoFieldIds = new ArraySet<>();
		List<Grid.Field.Id> refNoClueIds = new ArraySet<>();
		List<Grid.Field.Id> refMultipleCluesIds = new ArraySet<>();
		List<Grid.Field.Id> refNotIdOfRefTargetIds = new ArraySet<>();

		// Validate clue IDs and reference IDs
		for (Direction direction : clueLists.keySet())
		{
			for (Clue clue : clueLists.get(direction))
			{
				// Validate a reference ID against fields and the reference target
				if (clue.isReference())
				{
					Grid.Field.Id fieldId = clue.getFieldId();
					List<Grid.Field> fields = grid.findFields(fieldId);
					if (fields.isEmpty())
						noFieldIds.add(fieldId);
					else
					{
						Grid.Field.Id refFieldId = clue.getReferentId().fieldId;
						fields = grid.findFields(refFieldId);
						if (fields.isEmpty())
							refNoFieldIds.add(refFieldId);
						else
						{
							List<Clue> clues = findClues(clue0 -> !clue0.isReference()
														 && refFieldId.matches(clue0.getFieldId()));
							if (clues.size() > 1)
							{
								boolean resolved = false;
								for (Clue referentClue : clues)
								{
									if (referentClue.isSecondaryId(fieldId))
									{
										clue.getReferentId().index = referentClue.getId().index;
										resolved = true;
										break;
									}
								}
								if (!resolved)
									refMultipleCluesIds.add(refFieldId);
							}
							else if (clues.isEmpty())
								refNoClueIds.add(refFieldId);
							else if (!clues.get(0).isSecondaryId(fieldId))
								refNotIdOfRefTargetIds.add(fieldId);
						}
					}
				}

				// Validate a clue ID against fields and references
				else
				{
					int fieldCount = 0;
					int length = 0;
					for (int i = 0; i < clue.getNumFields(); i++)
					{
						Grid.Field.Id fieldId = clue.getFieldId(i);
						if (fieldId.number <= 0)
							invalidFieldIds.add(fieldId);
						else
						{
							List<Grid.Field> fields = grid.findFields(fieldId);
							if (fields.isEmpty())
								noFieldIds.add(fieldId);
							else
							{
								++fieldCount;
								length += fields.get(0).getLength();
								if (i > 0)
								{
									List<Clue> clues = findClues(clue0 -> clue0.isReference()
																 && fieldId.matches(clue0.getFieldId()));
									if (clues.isEmpty())
										noReferenceIds.add(fieldId);
									else if (clues.size() > 1)
										multipleReferencesIds.add(fieldId);
								}
							}
							if (!clueIds.add(fieldId))
								multipleCluesIds.add(fieldId);
						}
					}

					// Check the sum of the lengths of fields against the length of the answer given in the
					// clue
					if ((fieldCount == clue.getNumFields()) && (clue.getAnswerLength() > 0)
						&& (length != clue.getAnswerLength()))
					{
						for (int i = 0; i < clue.getNumFields(); i++)
							incorrectLengthIds.add(clue.getFieldId(i));
					}
				}
			}
		}

		// Create a list of the field IDs for which there is no clue
		for (Grid.Field field : grid.getFields())
		{
			Grid.Field.Id fieldId = field.getId();
			if (!clueIds.contains(fieldId))
				noClueIds.add(fieldId);
		}

		// Sort the lists of erroneous clue IDs and reference IDs
		noClueIds.sort(null);
		multipleCluesIds.sort(null);
		noFieldIds.sort(null);
		noReferenceIds.sort(null);
		multipleReferencesIds.sort(null);
		refNoFieldIds.sort(null);
		refNoClueIds.sort(null);
		refMultipleCluesIds.sort(null);
		refNotIdOfRefTargetIds.sort(null);

		// Create a list of lists of erroneous clue IDs and reference IDs
		List<ErrorListDialog.IdList> idLists = new ArrayList<>();
		if (!noClueIds.isEmpty())
			idLists.add(new ErrorListDialog.IdList(NO_CLUE_STR, noClueIds));
		if (!multipleCluesIds.isEmpty())
			idLists.add(new ErrorListDialog.IdList(MULTIPLE_CLUES_STR, multipleCluesIds));
		if (!noFieldIds.isEmpty())
			idLists.add(new ErrorListDialog.IdList(NO_FIELD_STR, noFieldIds));
		if (!noReferenceIds.isEmpty())
			idLists.add(new ErrorListDialog.IdList(NO_REFERENCE_STR, noReferenceIds));
		if (!multipleReferencesIds.isEmpty())
			idLists.add(new ErrorListDialog.IdList(MULTIPLE_REFERENCES_STR, multipleReferencesIds));
		if (!incorrectLengthIds.isEmpty())
			idLists.add(new ErrorListDialog.IdList(INCORRECT_LENGTH_STR, incorrectLengthIds));

		if (!refNoFieldIds.isEmpty())
			idLists.add(new ErrorListDialog.IdList(REF_NO_FIELD_STR, refNoFieldIds));
		if (!refNoClueIds.isEmpty())
			idLists.add(new ErrorListDialog.IdList(REF_NO_CLUE_STR, refNoClueIds));
		if (!refMultipleCluesIds.isEmpty())
			idLists.add(new ErrorListDialog.IdList(REF_MULTIPLE_CLUES_STR, refMultipleCluesIds));
		if (!refNotIdOfRefTargetIds.isEmpty())
			idLists.add(new ErrorListDialog.IdList(REF_NOT_TARGET_ID_STR, refNotIdOfRefTargetIds));

		// If there are errors, display them in a dialog
		if (idLists.isEmpty())
			return true;
		boolean proceed =
				ErrorListDialog.showDialog(CrosswordEditorApp.INSTANCE.getMainWindow(), CLUE_LIST_ERRORS_STR, closeStr,
										   idLists);
		if (proceed)
		{
			// Remove clues that do not have a corresponding field or clues that refer to a non-existent
			// field
			for (Direction direction : clueLists.keySet())
			{
				List<Clue> clues = clueLists.get(direction);
				for (int i = 0; i < clues.size(); i++)
				{
					Clue clue = clues.get(i);
					if (clue.isReference() && (grid.getField(clue.getReferentId().fieldId) == null))
						clues.remove(i--);
					else
					{
						for (int j = 0; j < clue.getNumFields(); j++)
						{
							if (grid.getField(clue.getFieldId(j)) == null)
							{
								clues.remove(i--);
								break;
							}
						}
					}
				}
				if (clues.isEmpty())
					clueLists.remove(direction);
			}

			// Add missing references
			List<Clue> references = new ArrayList<>();
			for (Direction direction : clueLists.keySet())
			{
				for (Clue clue : clueLists.get(direction))
				{
					for (int i = 1; i < clue.getNumFields(); i++)
					{
						Grid.Field.Id fieldId = clue.getFieldId(i);
						if (findClues(fieldId).isEmpty())
							references.add(new Clue(fieldId, clue.getId()));
					}
				}
			}
			for (Clue clue : references)
				setClue(clue);
		}
		return proceed;
	}

	//------------------------------------------------------------------

	public void read(File file)
		throws AppException
	{
		// Test for file
		if (!file.exists())
			throw new FileException(ErrorId.FILE_DOES_NOT_EXIST, file);
		if (!file.isFile())
			throw new FileException(ErrorId.NOT_A_FILE, file);

		// Initialise information in progress view
		TaskProgressDialog progressView = (TaskProgressDialog)Task.getProgressView();
		progressView.setInfo(READING_STR, file);
		progressView.setProgress(0, 0.0);

		// Initialise instance variables
		AppConfig config = AppConfig.INSTANCE;
		this.file = file;
		timestamp = file.lastModified();
		clueReferenceKeyword = config.getClueReferenceKeyword();
		answerLengthPattern = null;
		answerLengthSubstitutions.clear();
		lineBreak = config.getTextSectionLineBreak();
		clueSubstitutions.clear();
		title = null;
		prologueParagraphs.clear();
		epilogueParagraphs.clear();
		grid = null;
		clueLists.clear();

		// Run garbage collector to maximise available memory
		System.gc();

		// Read and parse file
		try
		{
			parse(XmlFile.read(file), new XmlParseExceptionExtender(file), false);
		}
		catch (TaskCancelledException e)
		{
			throw e;
		}
		catch (XmlParseException e)
		{
			throw new XmlParseException(e, file);
		}
		catch (AppException e)
		{
			throw new FileException(e, file);
		}
	}

	//------------------------------------------------------------------

	public void write(File file)
		throws AppException
	{
		// Initialise progress view
		TaskProgressDialog progressView = (TaskProgressDialog)Task.getProgressView();
		progressView.setInfo(WRITING_STR, file);
		progressView.setProgress(0, -1.0);

		// Update instance variables
		this.file = file;

		// Write file
		File tempFile = null;
		XmlWriter writer = null;
		boolean oldFileDeleted = false;
		long timestamp = this.timestamp;
		this.timestamp = 0;
		try
		{
			// Create parent directory of output file
			File directory = file.getAbsoluteFile().getParentFile();
			if ((directory != null) && !directory.exists())
			{
				try
				{
					if (!directory.mkdirs())
						throw new FileException(ErrorId.FAILED_TO_CREATE_DIRECTORY, directory);
				}
				catch (SecurityException e)
				{
					throw new FileException(ErrorId.FAILED_TO_CREATE_DIRECTORY, directory, e);
				}
			}

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

			// Open XML writer on temporary file
			try
			{
				writer = new XmlWriter(tempFile, StandardCharsets.UTF_8);
			}
			catch (FileNotFoundException e)
			{
				throw new FileException(ErrorId.FAILED_TO_OPEN_FILE, tempFile, e);
			}
			catch (SecurityException e)
			{
				throw new FileException(ErrorId.FILE_ACCESS_NOT_PERMITTED, tempFile, e);
			}

			// Write file
			try
			{
				// Write XML declaration
				writer.writeXmlDeclaration(XML_VERSION_STR, XmlConstants.ENCODING_NAME_UTF8, XmlWriter.Standalone.NO);

				// Write document element, start tag
				int indent = 0;

				AttributeList attributes = new AttributeList();
				attributes.add(AttrName.XMLNS, NAMESPACE_NAME);
				attributes.add(AttrName.XMLNS + ":" + AppConstants.NS_PREFIX_BASE, NAMESPACE_NAME);
				attributes.add(AttrName.VERSION, VERSION);
				if (title != null)
					attributes.add(AttrName.TITLE, title, true);
				writer.writeElementStart(ElementName.CROSSWORD, attributes, indent, true, true);
				indent += INDENT_INCREMENT;

				// Write grid element
				grid.writeGrid(writer, indent);

				// Write grid entries
				if (!grid.isEntriesEmpty())
					grid.writeEntries(writer, indent);

				// Write solution
				if (solutionProperties.location == null)
				{
					if (grid.hasSolution())
						grid.writeSolution(writer, indent, solutionProperties.passphrase);
				}
				else
				{
					byte[] hashValue = solutionProperties.hashValue;
					if (grid.hasSolution())
						hashValue = grid.getEncodedSolution(solutionProperties.passphrase).getHashValue();
					if (hashValue != null)
						grid.writeSolution(writer, indent, solutionProperties.location, hashValue);
				}

				// Write indications
				boolean isClueReferences = !findClues(clue -> clue.isReference()).isEmpty();
				boolean isLineBreak = hasLineBreak(prologueParagraphs) || hasLineBreak(epilogueParagraphs);
				if (((clueReferenceKeyword != null) && isClueReferences)
						|| (answerLengthPattern != null) || ((lineBreak != null) && isLineBreak))
				{
					writer.writeElementStart(ElementName.INDICATIONS, indent, true);

					indent += INDENT_INCREMENT;
					if ((clueReferenceKeyword != null) && isClueReferences)
						writer.writeEscapedTextElement(ElementName.CLUE_REFERENCE, indent, clueReferenceKeyword);
					if (answerLengthPattern != null)
					{
						attributes.clear();
						attributes.add(AttrName.PATTERN, answerLengthPattern, true);
						if (answerLengthSubstitutions.isEmpty())
							writer.writeEmptyElement(ElementName.ANSWER_LENGTH, attributes, indent, false);
						else
						{
							writer.writeElementStart(ElementName.ANSWER_LENGTH, attributes, indent, true, false);

							indent += INDENT_INCREMENT;
							for (Substitution substitution : answerLengthSubstitutions)
							{
								writer.writeEscapedTextElement(ElementName.SUBSTITUTION, indent,
															   substitution.toString());
							}
							indent -= INDENT_INCREMENT;

							writer.writeElementEnd(ElementName.ANSWER_LENGTH, indent);
						}
					}
					if ((lineBreak != null) && isLineBreak)
						writer.writeEscapedTextElement(ElementName.LINE_BREAK, indent, lineBreak);
					indent -= INDENT_INCREMENT;

					writer.writeElementEnd(ElementName.INDICATIONS, indent);
				}

				// Write clues
				for (Direction direction : clueLists.keySet())
				{
					attributes.clear();
					attributes.add(AttrName.DIRECTION, direction.getKey());
					writer.writeElementStart(ElementName.CLUES, attributes, indent, true, false);

					indent += INDENT_INCREMENT;
					for (Clue clue : clueLists.get(direction))
					{
						String text = clue.isReference() ? getClueReferenceString(direction, clue)
														 : clue.getText().toString();
						String idStr = StringUtils.join(',', getClueIdStrings(direction, clue, false));
						attributes.clear();
						attributes.add(AttrName.IDS, idStr);
						writer.writeEscapedTextElement(ElementName.CLUE, attributes, indent, false, text);
					}
					indent -= INDENT_INCREMENT;

					writer.writeElementEnd(ElementName.CLUES, indent);
				}

				// Write prologue
				if (!prologueParagraphs.isEmpty())
				{
					for (int i = 0; i < prologueParagraphs.size(); i++)
					{
						String paragraph = prologueParagraphs.get(i);
						attributes.clear();
						attributes.add(AttrName.INDEX, i);
						writer.writeElementStart(ElementName.PROLOGUE, attributes, indent, true, false);
						indent += INDENT_INCREMENT;
						for (String line : splitText(paragraph, MAX_TEXT_LINE_LENGTH, true))
							writer.writeEscapedTextElement(ElementName.LINE, indent, line);
						indent -= INDENT_INCREMENT;
						writer.writeElementEnd(ElementName.PROLOGUE, indent);
					}
				}

				// Write epilogue
				if (!epilogueParagraphs.isEmpty())
				{
					for (int i = 0; i < epilogueParagraphs.size(); i++)
					{
						String paragraph = epilogueParagraphs.get(i);
						attributes.clear();
						attributes.add(AttrName.INDEX, i);
						writer.writeElementStart(ElementName.EPILOGUE, attributes, indent, true, false);
						indent += INDENT_INCREMENT;
						for (String line : splitText(paragraph, MAX_TEXT_LINE_LENGTH, true))
							writer.writeEscapedTextElement(ElementName.LINE, indent, line);
						indent -= INDENT_INCREMENT;
						writer.writeElementEnd(ElementName.EPILOGUE, indent);
					}
				}

				// Write document element, end tag
				indent -= INDENT_INCREMENT;
				writer.writeElementEnd(ElementName.CROSSWORD, indent);
			}
			catch (IOException e)
			{
				throw new FileException(ErrorId.ERROR_WRITING_FILE, tempFile, e);
			}

			// Close output stream
			try
			{
				writer.close();
				writer = null;
			}
			catch (IOException e)
			{
				throw new FileException(ErrorId.FAILED_TO_CLOSE_FILE, tempFile, e);
			}

			// Delete any existing file
			try
			{
				if (file.exists() && !file.delete())
					throw new FileException(ErrorId.FAILED_TO_DELETE_FILE, file);
				oldFileDeleted = true;
			}
			catch (SecurityException e)
			{
				throw new FileException(ErrorId.FAILED_TO_DELETE_FILE, file, e);
			}

			// Rename temporary file
			try
			{
				if (!tempFile.renameTo(file))
					throw new TempFileException(ErrorId.FAILED_TO_RENAME_FILE, file, tempFile);
			}
			catch (SecurityException e)
			{
				throw new TempFileException(ErrorId.FAILED_TO_RENAME_FILE, file, e, tempFile);
			}

			// Set timestamp
			timestamp = file.lastModified();

			// Reset list of edits
			if (AppConfig.INSTANCE.isClearEditListOnSave())
				editList.clear();
			else
				editList.reset();
		}
		catch (AppException e)
		{
			// Close output stream
			try
			{
				if (writer != null)
					writer.close();
			}
			catch (Exception e1)
			{
				// ignore
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
		finally
		{
			this.timestamp = timestamp;
		}
	}

	//------------------------------------------------------------------

	public void exportHtml(File            file,
						   StylesheetKind  stylesheetKind,
						   StyleProperties styleProperties,
						   boolean         writeStylesheet,
						   boolean         writeBlockImage,
						   boolean         writeEntries)
		throws AppException
	{
		// Initialise progress view
		TaskProgressDialog progressView = (TaskProgressDialog)Task.getProgressView();
		progressView.setProgress(0, -1.0);

		// Update instance variables
		exportHtmlFile = file;

		// Create parent directory of output file
		File directory = file.getAbsoluteFile().getParentFile();
		if ((directory != null) && !directory.exists())
		{
			try
			{
				if (!directory.mkdirs())
					throw new FileException(ErrorId.FAILED_TO_CREATE_DIRECTORY, directory);
			}
			catch (SecurityException e)
			{
				throw new FileException(ErrorId.FAILED_TO_CREATE_DIRECTORY, directory, e);
			}
		}

		// Write stylesheet file
		if (writeStylesheet)
		{
			// Create directory
			File stylesheetFile = new File(directory, getStylesheetPathname(styleProperties.cellSize));
			progressView.setInfo(WRITING_STR, stylesheetFile);
			File stylesheetDirectory = stylesheetFile.getAbsoluteFile().getParentFile();
			if (!stylesheetDirectory.exists() && !stylesheetDirectory.mkdirs())
				throw new FileException(ErrorId.FAILED_TO_CREATE_DIRECTORY, stylesheetDirectory);

			// Write CSS file
			TextFile.write(stylesheetFile, StandardCharsets.UTF_8, createStylesheet(styleProperties),
						   FileWritingMode.USE_TEMP_FILE);
		}

		// Write block image file
		if (writeBlockImage)
		{
			// Create directory
			File imageFile = new File(directory, BlockGrid.getBlockImagePathname(styleProperties.cellSize));
			progressView.setInfo(WRITING_STR, imageFile);
			File imageDirectory = imageFile.getAbsoluteFile().getParentFile();
			if (!imageDirectory.exists() && !imageDirectory.mkdirs())
				throw new FileException(ErrorId.FAILED_TO_CREATE_DIRECTORY, imageDirectory);

			// Create image and write PNG file
			AppConfig config = AppConfig.INSTANCE;
			PngOutputFile.write(imageFile, BlockGrid.createBlockImage(styleProperties.cellSize,
																	  config.getBlockImageNumLines(),
																	  (float)config.getBlockImageLineWidth(),
																	  config.getBlockImageColour()));
		}

		// Initialise progress view
		progressView.setInfo(WRITING_STR, file);

		// Write HTML file
		File tempFile = null;
		XmlWriter writer = null;
		boolean oldFileDeleted = false;
		try
		{
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

			// Open XML writer on temporary file
			try
			{
				writer = new XmlWriter(tempFile, StandardCharsets.UTF_8);
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
				if (writer.getFileOutStream().getChannel().tryLock() == null)
					throw new FileException(ErrorId.FAILED_TO_LOCK_FILE, tempFile);
			}
			catch (IOException e)
			{
				throw new FileException(ErrorId.FAILED_TO_LOCK_FILE, tempFile, e);
			}

			// Write file
			try
			{
				writer.writeXmlDeclaration(XML_VERSION_STR, XmlConstants.ENCODING_NAME_UTF8, XmlWriter.Standalone.NONE);
				writer.writeDocumentType(HtmlConstants.ElementName.HTML, XHTML_SYSTEM_ID, XHTML_PUBLIC_ID);
				writer.writeEol();
				writeHtml(writer, stylesheetKind, styleProperties, writeEntries);
			}
			catch (IOException e)
			{
				throw new FileException(ErrorId.ERROR_WRITING_FILE, tempFile, e);
			}

			// Close output stream
			try
			{
				writer.close();
				writer = null;
			}
			catch (IOException e)
			{
				throw new FileException(ErrorId.FAILED_TO_CLOSE_FILE, tempFile, e);
			}

			// Delete any existing file
			try
			{
				if (file.exists() && !file.delete())
					throw new FileException(ErrorId.FAILED_TO_DELETE_FILE, file);
				oldFileDeleted = true;
			}
			catch (SecurityException e)
			{
				throw new FileException(ErrorId.FAILED_TO_DELETE_FILE, file, e);
			}

			// Rename temporary file
			try
			{
				if (!tempFile.renameTo(file))
					throw new TempFileException(ErrorId.FAILED_TO_RENAME_FILE, file, tempFile);
			}
			catch (SecurityException e)
			{
				throw new TempFileException(ErrorId.FAILED_TO_RENAME_FILE, file, e, tempFile);
			}
		}
		catch (AppException e)
		{
			// Close output stream
			try
			{
				if (writer != null)
					writer.close();
			}
			catch (Exception e1)
			{
				// ignore
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

	public void loadSolution()
		throws AppException
	{
		// Initialise local variables
		URL url = solutionProperties.location;

		// Initialise progress view
		TaskProgressDialog progressView = (TaskProgressDialog)Task.getProgressView();
		progressView.setInfo(CONNECTING_TO_STR, url);
		progressView.setProgress(0, -1.0);

		// Create connection
		try
		{
			URLConnection connection = url.openConnection();
			connection.setUseCaches(false);
			connection.connect();
		}
		catch (IOException e)
		{
			throw new UrlException(ErrorId.FAILED_TO_CONNECT, url);
		}

		// Read remote document
		try
		{
			progressView.setInfo(READING_STR, url);
			solutionProperties.remoteDocument = new CrosswordDocument();
			solutionProperties.remoteDocument.parse(XmlFile.read(url), new XmlParseExceptionExtender(url), true);
		}
		catch (TaskCancelledException e)
		{
			throw e;
		}
		catch (XmlParseException e)
		{
			throw new XmlParseException(e, url);
		}
		catch (AppException e)
		{
			throw new UrlException(e, url);
		}
	}

	//------------------------------------------------------------------

	public CrosswordDocument createSolutionDocument(int index)
	{
		CrosswordDocument document = new CrosswordDocument(index);
		Grid gridCopy = grid.createCopy();
		gridCopy.setSolution(grid.getSolution());
		document.setGrid(gridCopy);
		document.setSolutionProperties(new SolutionProperties(null, solutionProperties.passphrase, null));
		return document;
	}

	//------------------------------------------------------------------

	public String getLineBreakRegex()
	{
		return String.format(LINE_BREAK_REGEX, "=", RegexUtils.escape(lineBreak));
	}

	//------------------------------------------------------------------

	private boolean hasLineBreak(List<String> paragraphs)
	{
		Pattern pattern = Pattern.compile(getLineBreakRegex());
		for (String paragraph : paragraphs)
		{
			if (pattern.matcher(paragraph).find())
				return true;
		}
		return false;
	}

	//------------------------------------------------------------------

	private List<String> splitText(String  text,
								   int     maxLineLength,
								   boolean keepTrailingSpace)
	{
		Pattern pattern = keepTrailingSpace ? WORD_SPACE_PATTERN : WORD_PATTERN;

		List<String> strs = new ArrayList<>();
		for (String str : text.split(getLineBreakRegex()))
		{
			Matcher matcher = pattern.matcher(str);
			int startIndex = 0;
			int endIndex1 = 0;
			int endIndex2 = 0;
			while (matcher.find())
			{
				String word = matcher.group(1);
				int index = matcher.start() + word.length();
				if ((index - startIndex > maxLineLength) && (startIndex < endIndex1))
				{
					strs.add(str.substring(startIndex, endIndex1));
					startIndex = endIndex2;
				}
				endIndex1 = index;
				endIndex2 = matcher.end();
			}
			if (startIndex < str.length())
				strs.add(str.substring(startIndex));
		}
		return strs;
	}

	//------------------------------------------------------------------

	private String getLineText(Element element)
		throws XmlParseException
	{
		try
		{
			StringBuilder buffer = new StringBuilder(1024);
			String line = null;
			for (Element el : XmlUtils.getChildElements(element, ElementName.LINE))
			{
				if (line != null)
					buffer.append(line.endsWith(lineBreak) ? '\n' : ' ');
				line = el.getTextContent();
				buffer.append(line);
			}
			String text = buffer.toString();
			new StyledText(text);
			return text;
		}
		catch (final StyledText.ParseException e)
		{
			String locationStr = XmlUtils.getElementPath(element);
			if (element.hasAttribute(AttrName.INDEX))
				locationStr += " #" + element.getAttribute(AttrName.INDEX);
			throw new XmlParseException(e::getMessageString, locationStr);
		}
	}

	//------------------------------------------------------------------

	private CrosswordView getView()
	{
		return CrosswordEditorApp.INSTANCE.getView(this);
	}

	//------------------------------------------------------------------

	private String getStylesheetPathname(int cellSize)
	{
		return STYLESHEET_PATHNAME_PREFIX + grid.getSeparator().getKey() + "-" + cellSize + STYLESHEET_PATHNAME_SUFFIX;
	}

	//------------------------------------------------------------------

	private void setClue(Clue clue)
	{
		Direction direction = clue.getFieldId().direction;
		List<Clue> clues = clueLists.get(direction);

		// Remove clue from list
		if (clue.isEmpty())
		{
			if (clues != null)
			{
				int index = Collections.binarySearch(clues, clue, Clue.COMPARATOR);
				if (index >= 0)
				{
					clues.remove(index);
					if (clues.isEmpty())
						clueLists.remove(direction);
				}
			}
		}

		// Add clue to list or replace existing clue
		else
		{
			if (clues == null)
			{
				clues = new ArrayList<>();
				clueLists.put(direction, clues);
			}
			int index = Collections.binarySearch(clues, clue, Clue.COMPARATOR);
			if (index < 0)
				clues.add(-index - 1, clue);
			else
				clues.set(index, clue);
		}
	}

	//------------------------------------------------------------------

	private void setClues(List<Clue> clues)
	{
		// Set clues
		for (Clue clue : clues)
			setClue(clue);

		// Update view
		for (Direction direction : Direction.DEFINED_DIRECTIONS)
			getView().updateClues(direction);
	}

	//------------------------------------------------------------------

	private List<Clue> findClues(Predicate<Clue> filter)
	{
		List<Clue> clues = new ArrayList<>();
		for (Direction direction : clueLists.keySet())
		{
			for (Clue clue : clueLists.get(direction))
			{
				if (filter.test(clue))
					clues.add(clue);
			}
		}
		return clues;
	}

	//------------------------------------------------------------------

	private List<String> getClueIdStrings(Direction direction,
										  Clue      clue,
										  boolean   implicitDirection)
	{
		List<String> strs = new ArrayList<>();
		for (int i = 0; i < clue.getNumFields(); i++)
		{
			Grid.Field.Id fieldId = clue.getFieldId(i).clone();
			if ((i == 0) || (grid.findFields(fieldId.undefined()).size() == 1) ||
				(implicitDirection && (fieldId.direction == direction)))
				fieldId.direction = Direction.NONE;
			strs.add(fieldId.toString());
		}
		return strs;
	}

	//------------------------------------------------------------------

	private void parse(Document                  document,
					   XmlParseExceptionExtender xmlParseExceptionExtender,
					   boolean                   solutionRequired)
		throws AppException
	{
		// Test document format
		Element rootElement = document.getDocumentElement();
		if (!rootElement.getNodeName().equals(ElementName.CROSSWORD))
			throw new AppException(ErrorId.UNEXPECTED_DOCUMENT_FORMAT);
		String elementPath = ElementName.CROSSWORD;

		// Attribute: namespace
		String attrName = AttrName.XMLNS;
		String attrKey = XmlUtils.appendAttributeName(elementPath, attrName);
		String attrValue = XmlUtils.getAttribute(rootElement, attrName);
		if (attrValue == null)
			throw new XmlParseException(ErrorId.NO_ATTRIBUTE, attrKey);
		if (!attrValue.matches(NAMESPACE_NAME_REGEX))
			throw new AppException(ErrorId.UNEXPECTED_DOCUMENT_FORMAT);

		// Attribute: namespace prefix
		attrName = AttrName.XMLNS + ":" + AppConstants.NS_PREFIX_BASE;
		attrKey = XmlUtils.appendAttributeName(elementPath, attrName);
		attrValue = XmlUtils.getAttribute(rootElement, attrName);
		if (attrValue == null)
			throw new XmlParseException(ErrorId.NO_ATTRIBUTE, attrKey);
		if (!attrValue.matches(NAMESPACE_NAME_REGEX))
			throw new AppException(ErrorId.UNEXPECTED_DOCUMENT_FORMAT);

		// Attribute: version
		attrName = AttrName.VERSION;
		attrKey = XmlUtils.appendAttributeName(elementPath, attrName);
		attrValue = XmlUtils.getAttribute(rootElement, attrName);
		if (attrValue == null)
			throw new XmlParseException(ErrorId.NO_ATTRIBUTE, attrKey);
		try
		{
			int version = Integer.parseInt(attrValue);
			if ((version < MIN_SUPPORTED_VERSION) || (version > MAX_SUPPORTED_VERSION))
				throw new AppException(ErrorId.UNSUPPORTED_DOCUMENT_VERSION, attrValue);
		}
		catch (NumberFormatException e)
		{
			throw new XmlParseException(ErrorId.INVALID_ATTRIBUTE, attrKey, attrValue);
		}

		// Attribute: title
		attrName = AttrName.TITLE;
		attrKey = XmlUtils.appendAttributeName(elementPath, attrName);
		attrValue = XmlUtils.getAttribute(rootElement, attrName);
		title = attrValue;

		// Get child elements
		List<Element> elements = XmlUtils.getChildElements(rootElement);

		// Process grid element
		for (Element element : elements)
		{
			if (Grid.isGridElement(element))
			{
				if (grid != null)
					throw new XmlParseException(ErrorId.MULTIPLE_GRID_ELEMENTS, XmlUtils.getElementPath(element));
				grid = Grid.create(element);
			}
		}
		if (grid == null)
			throw new FileException(ErrorId.NO_GRID_ELEMENT, file);

		// Process other elements
		for (Element element : elements)
		{
			String elementName = element.getTagName();

			// Grid entries
			if (Grid.isEntriesElement(element))
				grid.parseEntries(element);

			// Solution
			else if (Grid.isSolutionElement(element))
				parseSolution(element, xmlParseExceptionExtender, solutionRequired);

			// Indications
			else if (elementName.equals(ElementName.INDICATIONS))
				parseIndications(element);

			// Clues
			else if (elementName.equals(ElementName.CLUES))
				parseClues(element);

			// Prologue
			else if (elementName.equals(ElementName.PROLOGUE))
				prologueParagraphs.add(getLineText(element));

			// Epilogue
			else if (elementName.equals(ElementName.EPILOGUE))
				epilogueParagraphs.add(getLineText(element));
		}

		// Update clue directions
		updateClueDirections();
	}

	//------------------------------------------------------------------

	private void parseSolution(Element                   element,
							   XmlParseExceptionExtender xmlParseExceptionExtender,
							   boolean                   solutionRequired)
		throws TaskCancelledException
	{
		try
		{
			try
			{
				solutionProperties = grid.parseSolution(element, solutionRequired);
			}
			catch (TaskCancelledException e)
			{
				throw e;
			}
			catch (AppException e)
			{
				if (e instanceof XmlParseException)
					e = xmlParseExceptionExtender.extend((XmlParseException)e);
				if (solutionRequired)
				{
					CrosswordEditorApp.INSTANCE.showErrorMessage(READ_SOLUTION_STR, e);
					throw new TaskCancelledException();
				}
				else
				{
					String[] optionStrs = Utils.getOptionStrings(AppConstants.CONTINUE_STR);
					if (JOptionPane.showOptionDialog(getWindow(), e, READ_SOLUTION_STR, JOptionPane.OK_CANCEL_OPTION,
													 JOptionPane.ERROR_MESSAGE, null, optionStrs,
													 optionStrs[1]) != JOptionPane.OK_OPTION)
						throw new TaskCancelledException();
				}
			}
		}
		catch (TaskCancelledException e)
		{
			file = null;
			throw e;
		}
	}

	//------------------------------------------------------------------

	private void parseIndications(Element indicationsElement)
		throws XmlParseException
	{
		for (Element element : XmlUtils.getChildElements(indicationsElement))
		{
			String elementName = element.getTagName();

			// Clue reference
			if (elementName.equals(ElementName.CLUE_REFERENCE))
				clueReferenceKeyword = element.getTextContent();

			// Answer length
			else if (elementName.equals(ElementName.ANSWER_LENGTH))
			{
				// Get element path
				String elementPath = XmlUtils.getElementPath(element);

				// Attribute: pattern
				String attrName = AttrName.PATTERN;
				String attrKey = XmlUtils.appendAttributeName(elementPath, attrName);
				String attrValue = XmlUtils.getAttribute(element, attrName);
				if (attrValue == null)
					throw new XmlParseException(ErrorId.NO_ATTRIBUTE, attrKey);
				try
				{
					Pattern.compile(attrValue);
				}
				catch (PatternSyntaxException e)
				{
					throw new XmlParseException(ErrorId.MALFORMED_PATTERN, attrKey, attrValue,
												RegexUtils.getExceptionMessage(e));
				}
				answerLengthPattern = attrValue;

				// Substitution elements
				for (Element substitutionElement : XmlUtils.getChildElements(element))
				{
					if (substitutionElement.getTagName().equals(ElementName.SUBSTITUTION))
					{
						elementPath = XmlUtils.getElementPath(substitutionElement);
						String text = substitutionElement.getTextContent();
						try
						{
							answerLengthSubstitutions.add(new Substitution(text));
						}
						catch (PatternSyntaxException e)
						{
							throw new XmlParseException(ErrorId.MALFORMED_PATTERN, elementPath, text,
														RegexUtils.getExceptionMessage(e));
						}
						catch (IllegalArgumentException e)
						{
							throw new XmlParseException(ErrorId.MALFORMED_SUBSTITUTION, elementPath, text);
						}
					}
				}
			}

			// Line break
			else if (elementName.equals(ElementName.LINE_BREAK))
				lineBreak = element.getTextContent();
		}
	}

	//------------------------------------------------------------------

	private void parseClues(Element cluesElement)
		throws XmlParseException
	{
		// Get element path
		String elementPath = XmlUtils.getElementPath(cluesElement);

		// Attribute: direction
		String attrName = AttrName.DIRECTION;
		String attrKey = XmlUtils.appendAttributeName(elementPath, attrName);
		String attrValue = XmlUtils.getAttribute(cluesElement, attrName);
		if (attrValue == null)
			throw new XmlParseException(ErrorId.NO_ATTRIBUTE, attrKey);
		Direction direction = Direction.forKey(attrValue);
		if (direction == null)
			throw new XmlParseException(ErrorId.INVALID_ATTRIBUTE, attrKey, attrValue);

		// Test for existing list of clues for the direction
		if (clueLists.containsKey(direction))
			throw new XmlParseException(ErrorId.MULTIPLE_CLUES_ELEMENTS, attrKey, new String[] { direction.getKey() });

		// Parse clue elements
		List<Clue> clues = new ArrayList<>();
		Clue.AnswerLengthParser answerLengthParser = (answerLengthPattern == null)
															? null
															: new Clue.AnswerLengthParser(answerLengthPattern,
																						  answerLengthSubstitutions);
		elementPath = XmlUtils.concatenatePath(elementPath, ElementName.CLUE);
		for (Element element : XmlUtils.getChildElements(cluesElement, ElementName.CLUE))
		{
			// Attribute: IDs
			attrName = AttrName.IDS;
			attrKey = XmlUtils.appendAttributeName(elementPath, attrName);
			attrValue = XmlUtils.getAttribute(element, attrName);
			if (attrValue == null)
				throw new XmlParseException(ErrorId.NO_ATTRIBUTE, attrKey);
			List<Grid.Field.Id> ids = new ArrayList<>();
			try
			{
				for (String str : attrValue.split(" *, *"))
				{
					Grid.Field.Id fieldId = new Grid.Field.Id(str);
					Grid.Field.Id directedFieldId = fieldId.clone();
					if (ids.isEmpty())
						directedFieldId.direction = direction;
					if (grid.findFields(directedFieldId).isEmpty())
						throw new XmlParseException(ErrorId.INVALID_FIELD_ID, attrKey, fieldId.toString());
					ids.add(fieldId);
				}
			}
			catch (IllegalArgumentException e)
			{
				throw new XmlParseException(ErrorId.INVALID_ATTRIBUTE, attrKey, attrValue);
			}

			// Add clue to list
			try
			{
				clues.add(new Clue(ids, element.getTextContent(), clueReferenceKeyword, answerLengthParser));
			}
			catch (final StyledText.ParseException e)
			{
				String locationStr = elementPath + " (" + attrValue + direction.getSuffix() + ")";
				throw new XmlParseException(e::getMessageString, locationStr);
			}
		}
		setClues(direction, clues);
	}

	//------------------------------------------------------------------

	private List<CssRuleSet> getStyleRuleSets(StyleProperties styleProperties)
	{
		List<CssRuleSet> ruleSets = new ArrayList<>();

		CssRuleSet ruleSet = DOCUMENT_RULE_SET.clone();
		ruleSet.replacePropertyValue(CssProperty.FONT_SIZE, styleProperties.fontSize);
		if (!styleProperties.fontNames.isEmpty())
			ruleSet.addProperty(CssProperty.FONT_FAMILY, styleProperties.fontNames.toQuotedString());

		ruleSets.add(ruleSet);
		ruleSets.addAll(HEADER_RULE_SETS);
		ruleSets.addAll(grid.getStyleRuleSets(styleProperties.cellSize, styleProperties.gridColour,
											  styleProperties.entryColour, styleProperties.fieldNumberFontSizeFactor));
		ruleSets.addAll(TEXT_RULE_SETS);
		return ruleSets;
	}

	//------------------------------------------------------------------

	private String createStylesheet(StyleProperties styleProperties)
	{
		StringBuilder buffer = new StringBuilder(4096);
		String headerComment = String.format(STYLESHEET_COMMENT_STR, grid.getSeparator().getKey(),
											 styleProperties.cellSize);
		buffer.append(CssUtils.createHeaderComment(72, List.of("", headerComment, "")));
		buffer.append('\n');

		for (CssRuleSet ruleSet : getStyleRuleSets(styleProperties))
		{
			for (String str : ruleSet.toStrings())
			{
				buffer.append(str);
				buffer.append('\n');
			}
		}

		buffer.append('\n');
		buffer.append(CssUtils.SEPARATOR);

		return buffer.toString();
	}

	//------------------------------------------------------------------

	private void writeHtml(XmlWriter       writer,
						   StylesheetKind  stylesheetKind,
						   StyleProperties styleProperties,
						   boolean         writeEntries)
		throws AppException, IOException
	{
		// Write HTML start tag
		int indent = 0;

		AttributeList attributes = new AttributeList();
		attributes.add(HtmlConstants.AttrName.XMLNS, HTML_NAMESPACE_NAME);
		attributes.add(HtmlConstants.AttrName.XML_LANG, LANG_STR);
		writer.writeElementStart(HtmlConstants.ElementName.HTML, attributes, indent, true, false);

		// Write head start tag
		indent += INDENT_INCREMENT;
		writer.writeElementStart(HtmlConstants.ElementName.HEAD, indent, true);

		// Write meta tag
		indent += INDENT_INCREMENT;
		attributes.clear();
		attributes.add(HtmlConstants.AttrName.HTTP_EQUIV, CONTENT_TYPE_STR);
		attributes.add(HtmlConstants.AttrName.CONTENT, MIME_TYPE_STR);
		writer.writeEmptyElement(HtmlConstants.ElementName.META, attributes, indent, true);

		// Write title
		if (title != null)
			writer.writeEscapedTextElement(HtmlConstants.ElementName.TITLE, indent, title);

		// Write style element, rule sets for all media types
		attributes.clear();
		if (stylesheetKind == StylesheetKind.INTERNAL)
			writeStyle(writer, indent, EnumSet.of(CssMediaRule.MediaType.PRINT, CssMediaRule.MediaType.SCREEN),
					   getStyleRuleSets(styleProperties));

		// Write link element
		else
		{
			attributes.add(HtmlConstants.AttrName.HREF, getStylesheetPathname(styleProperties.cellSize));
			attributes.add(HtmlConstants.AttrName.REL, STYLESHEET_STR);
			attributes.add(HtmlConstants.AttrName.TYPE, TEXT_CSS_STR);
			writer.writeEmptyElement(HtmlConstants.ElementName.LINK, attributes, indent, true, false);
		}

		// Write style element, media-specific rule sets
		for (CssMediaRule mediaRule : grid.getStyleMediaRules())
			writeStyle(writer, indent, mediaRule.getMediaTypes(), mediaRule.getRuleSets());

		// Write head end tag
		indent -= INDENT_INCREMENT;
		writer.writeElementEnd(HtmlConstants.ElementName.HEAD, indent);

		// Write body start tag
		writer.writeEol();
		writer.writeElementStart(HtmlConstants.ElementName.BODY, indent, true);

		// Write title
		indent += INDENT_INCREMENT;
		if (title != null)
		{
			attributes.clear();
			attributes.add(HtmlConstants.AttrName.ID, HtmlConstants.Id.TITLE);
			writer.writeEscapedTextElement(HtmlConstants.ElementName.H4, attributes, indent, false, title);
		}

		// Write prologue
		String lineBreakEol = lineBreak + "\n";
		if (!prologueParagraphs.isEmpty())
		{
			// Write start tag, prologue division
			attributes.clear();
			attributes.add(HtmlConstants.AttrName.ID, HtmlConstants.Id.PROLOGUE);
			writer.writeElementStart(HtmlConstants.ElementName.DIV, attributes, indent, true, false);

			// Write prologue paragraphs
			indent += INDENT_INCREMENT;
			for (String text : prologueParagraphs)
			{
				writer.writeElementStart(HtmlConstants.ElementName.P, indent, true);
				indent += INDENT_INCREMENT;
				for (String line : splitText(new StyledText(text).toHtml(lineBreakEol),
											 MAX_TEXT_LINE_LENGTH - indent, false))
				{
					writer.writeSpaces(indent);
					writer.write(line.strip());
					writer.writeEol();
				}
				indent -= INDENT_INCREMENT;
				writer.writeElementEnd(HtmlConstants.ElementName.P, indent);
			}

			// Write end tag, prologue division
			indent -= INDENT_INCREMENT;
			writer.writeElementEnd(HtmlConstants.ElementName.DIV, indent);
		}

		// Write grid
		grid.writeHtml(writer, indent, styleProperties.cellSize, showFieldNumbers, writeEntries);

		// Write clues
		if (showClues)
		{
			// Write start tag, table division
			attributes.clear();
			attributes.add(HtmlConstants.AttrName.ID, HtmlConstants.Id.CLUES);
			writer.writeElementStart(HtmlConstants.ElementName.DIV, attributes, indent, true, false);

			// Write lists of clues
			indent += INDENT_INCREMENT;
			for (Direction direction : clueLists.keySet())
			{
				// Write start tag, cell division
				writer.writeElementStart(HtmlConstants.ElementName.DIV, indent, true);

				// Write H4 element
				indent += INDENT_INCREMENT;
				if (direction != Direction.NONE)
					writer.writeEscapedTextElement(HtmlConstants.ElementName.H4, indent, direction.toString());

				// Write start tag, list of clues
				writer.writeElementStart(HtmlConstants.ElementName.DIV, indent, true);

				// Write clues
				indent += INDENT_INCREMENT;
				for (Clue clue : clueLists.get(direction))
				{
					// Write start tag, clue
					writer.writeElementStart(HtmlConstants.ElementName.DIV, indent, true);

					// Write primary field ID
					indent += INDENT_INCREMENT;
					List<String> idStrs = getClueIdStrings(direction, clue,
														   AppConfig.INSTANCE.isImplicitFieldDirection());
					writer.writeEscapedTextElement(HtmlConstants.ElementName.DIV, indent, idStrs.get(0));

					// Write start tag and secondary field IDs
					if (idStrs.size() == 1)
						writer.writeElementStart(HtmlConstants.ElementName.DIV, indent, false);
					else
					{
						attributes.clear();
						attributes.add(HtmlConstants.AttrName.CLASS, HtmlConstants.Class.MULTI_FIELD_CLUE);
						writer.writeElementStart(HtmlConstants.ElementName.DIV, attributes, indent, false, false);
						attributes.clear();
						attributes.add(HtmlConstants.AttrName.CLASS, HtmlConstants.Class.SECONDARY_IDS);
						writer.writeElementStart(HtmlConstants.ElementName.SPAN, attributes, 0, false, false);
						idStrs.set(0, "");
						writer.write(StringUtils.join(FIELD_ID_SEPARATOR, idStrs));
						writer.writeEndTag(HtmlConstants.ElementName.SPAN);
					}

					// Write clue text
					if (clue.isReference())
						writer.writeEscaped(getClueReferenceString(direction, clue));
					else
						clue.getText().write(writer);

					// Write end tag, clue text
					writer.writeElementEnd(HtmlConstants.ElementName.DIV, 0);
					indent -= INDENT_INCREMENT;

					// Write end tag, clue
					writer.writeElementEnd(HtmlConstants.ElementName.DIV, indent);
				}
				indent -= INDENT_INCREMENT;

				// Write end tag, list of clues
				writer.writeElementEnd(HtmlConstants.ElementName.DIV, indent);

				// Write end tag, cell division
				indent -= INDENT_INCREMENT;
				writer.writeElementEnd(HtmlConstants.ElementName.DIV, indent);
			}
			indent -= INDENT_INCREMENT;

			// Write end tag, table division
			writer.writeElementEnd(HtmlConstants.ElementName.DIV, indent);
		}

		// Write epilogue
		if (!epilogueParagraphs.isEmpty())
		{
			// Write start tag, epilogue division
			attributes.clear();
			attributes.add(HtmlConstants.AttrName.ID, HtmlConstants.Id.EPILOGUE);
			writer.writeElementStart(HtmlConstants.ElementName.DIV, attributes, indent, true, false);

			// Write epilogue paragraphs
			indent += INDENT_INCREMENT;
			for (String text : epilogueParagraphs)
			{
				writer.writeElementStart(HtmlConstants.ElementName.P, indent, true);
				indent += INDENT_INCREMENT;
				for (String line : splitText(new StyledText(text).toHtml(lineBreakEol), MAX_TEXT_LINE_LENGTH - indent,
											 false))
				{
					writer.writeSpaces(indent);
					writer.write(line.strip());
					writer.writeEol();
				}
				indent -= INDENT_INCREMENT;
				writer.writeElementEnd(HtmlConstants.ElementName.P, indent);
			}

			// Write end tag, epilogue division
			indent -= INDENT_INCREMENT;
			writer.writeElementEnd(HtmlConstants.ElementName.DIV, indent);
		}

		// Write body end tag
		indent -= INDENT_INCREMENT;
		writer.writeElementEnd(HtmlConstants.ElementName.BODY, indent);

		// Write HTML end tag
		indent -= INDENT_INCREMENT;
		writer.writeElementEnd(HtmlConstants.ElementName.HTML, indent);
	}

	//------------------------------------------------------------------

	private EditList.Element<CrosswordDocument> onUndo()
	{
		EditList.Element<CrosswordDocument> edit = editList.removeUndo();
		if (edit != null)
			edit.undo(this);
		return null;
	}

	//------------------------------------------------------------------

	private EditList.Element<CrosswordDocument> onRedo()
	{
		EditList.Element<CrosswordDocument> edit = editList.removeRedo();
		if (edit != null)
			edit.redo(this);
		return null;
	}

	//------------------------------------------------------------------

	private EditList.Element<CrosswordDocument> onClearEditList()
	{
		String[] optionStrs = Utils.getOptionStrings(AppConstants.CLEAR_STR);
		if (JOptionPane.showOptionDialog(getWindow(), CLEAR_EDIT_LIST_STR, CrosswordEditorApp.SHORT_NAME,
										 JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, optionStrs,
										 optionStrs[1]) == JOptionPane.OK_OPTION)
		{
			editList.clear();
			System.gc();
		}
		return null;
	}

	//------------------------------------------------------------------

	private EditList.Element<CrosswordDocument> onEditClue()
	{
		EditList.Element<CrosswordDocument> edit = null;

		// Get selected clue
		Clue.Id clueId = getView().getSelectedClueId();
		Clue oldClue = findPrimaryClue(clueId);
		if (oldClue == null)
			oldClue = new Clue(clueId);

		// Edit clue
		Clue newClue = ClueDialog.showDialog(getWindow(), this, oldClue);

		// Update clue and references
		if ((newClue != null) && !newClue.equals(oldClue))
		{
			// Create lists of old and new clues
			List<Clue> oldClues = new ArrayList<>();
			oldClues.add(oldClue);
			List<Clue> newClues = new ArrayList<>();
			newClues.add(newClue);

			// Remove obsolete references
			Clue.Id newClueId = newClue.getId();
			for (int i = 1; i < oldClue.getNumFields(); i++)
			{
				Grid.Field.Id fieldId = oldClue.getFieldId(i);
				if (!newClue.isSecondaryId(fieldId))
				{
					for (Clue clue : findClues(fieldId))
					{
						if (newClueId.equals(clue.getReferentId()))
						{
							oldClues.add(clue);
							newClues.add(new Clue(fieldId));
						}
					}
				}
			}

			// Add new references
			for (int i = 1; i < newClue.getNumFields(); i++)
			{
				Grid.Field.Id fieldId = newClue.getFieldId(i);
				List<Clue> clues = findClues(fieldId);
				if (clues.isEmpty())
				{
					oldClues.add(new Clue(fieldId));
					newClues.add(new Clue(fieldId, newClueId));
				}
				else
				{
					for (Clue clue : clues)
					{
						if (clue.isReference())
						{
							oldClues.add(clue);
							newClues.add(new Clue(fieldId, newClueId));
						}
					}
				}
			}

			// Attach reference to its referent
			if (newClue.isReference())
			{
				// Resolve direction of reference ID
				Grid.Field.Id fieldId = newClue.getFieldId();
				Grid.Field.Id refFieldId = newClue.getReferentId().fieldId;
				if (refFieldId.direction == Direction.NONE)
				{
					List<Grid.Field> fields = grid.findFields(refFieldId);
					if (!fields.isEmpty())
						refFieldId.direction = (fields.size() == 1) ? fields.get(0).getDirection() : fieldId.direction;
				}

				// Find referent
				Clue clue = findClue(newClue.getReferentId());

				// Add secondary field ID to referent
				if ((clue != null) && !clue.isReference() && !clue.isSecondaryId(fieldId))
				{
					List<Grid.Field.Id> fieldIds = new ArrayList<>();
					for (int i = 0; i < clue.getNumFields(); i++)
						fieldIds.add(clue.getFieldId(i));
					fieldIds.add(fieldId);
					oldClues.add(clue);
					newClues.add(new Clue(fieldIds, clue.getText(), clue.getAnswerLength()));
				}
			}

			// Set clues and update view
			for (Clue clue : newClues)
			{
				setClue(clue);
				getView().updateClues(clue.getFieldId().direction);
			}

			// Create edit
			edit = new CluesEdit(oldClues, newClues);
		}
		return edit;
	}

	//------------------------------------------------------------------

	private EditList.Element<CrosswordDocument> onEditGrid()
	{
		EditList.Element<CrosswordDocument> edit = null;
		Grid grid = GridDialog.showDialog(getWindow(), this);
		if (grid != null)
		{
			edit = new GridEdit(grid.getSeparator(), grid.getNumColumns(), grid.getNumRows(), this.grid.getSymmetry(),
								StringUtils.join(null, this.grid.getGridDefinition()), grid.getSymmetry(),
								StringUtils.join(null, grid.getGridDefinition()));
			this.grid = grid;
			getView().updateGrid();
		}
		return edit;
	}

	//------------------------------------------------------------------

	private EditList.Element<CrosswordDocument> onEditTextSections()
	{
		TextSectionsEdit edit = null;
		TextSectionsDialog.Result result = TextSectionsDialog.showDialog(getWindow(), title, lineBreak,
																		 prologueParagraphs, epilogueParagraphs);
		if (result != null)
		{
			edit = new TextSectionsEdit(title, prologueParagraphs, epilogueParagraphs, result.title,
										result.prologueParagraphs, result.epilogueParagraphs);
			if (!Objects.equals(title, result.title))
			{
				title = result.title;
				edit.sections.add(TextSection.TITLE);
			}
			if (!prologueParagraphs.equals(result.prologueParagraphs))
			{
				prologueParagraphs = result.prologueParagraphs;
				edit.sections.add(TextSection.PROLOGUE);
			}
			if (!epilogueParagraphs.equals(result.epilogueParagraphs))
			{
				epilogueParagraphs = result.epilogueParagraphs;
				edit.sections.add(TextSection.EPILOGUE);
			}
			if (edit.sections.isEmpty())
				edit = null;
			else
				getView().updateTextSections(edit.sections);
		}
		return edit;
	}

	//------------------------------------------------------------------

	private EditList.Element<CrosswordDocument> onEditIndications()
	{
		IndicationsEdit edit = null;
		IndicationsDialog.Params params = new IndicationsDialog.Params(clueReferenceKeyword, answerLengthPattern,
																	   answerLengthSubstitutions, lineBreak);
		IndicationsDialog.Params result = IndicationsDialog.showDialog(getWindow(), params);
		if (result != null)
		{
			edit = new IndicationsEdit(clueReferenceKeyword, answerLengthPattern, answerLengthSubstitutions, lineBreak,
									   result.clueReferenceKeyword(), result.answerLengthPattern(),
									   result.answerLengthSubstitutions(), result.lineBreak());
			clueReferenceKeyword = result.clueReferenceKeyword();
			answerLengthPattern = result.answerLengthPattern();
			answerLengthSubstitutions = result.answerLengthSubstitutions();
			lineBreak = result.lineBreak();
		}
		return edit;
	}

	//------------------------------------------------------------------

	private EditList.Element<CrosswordDocument> onSetEntryCharacter()
	{
		Grid.EntryValue entryValue = (Grid.EntryValue)Command.SET_ENTRY_CHARACTER
																		.getValue(Command.Property.GRID_ENTRY_VALUE);
		Direction direction = (Direction)Command.SET_ENTRY_CHARACTER.getValue(Command.Property.DIRECTION);
		char oldValue = grid.getEntryValue(entryValue.row, entryValue.column);
		grid.setEntryValue(entryValue.row, entryValue.column, entryValue.value);
		return new GridEntryCharEdit(entryValue.row, entryValue.column, direction, oldValue, entryValue.value);
	}

	//------------------------------------------------------------------

	private EditList.Element<CrosswordDocument> onCopyCluesToClipboard()
		throws AppException
	{
		StringBuilder buffer = new StringBuilder(4096);
		int directionIndex = 0;
		for (Direction direction : clueLists.keySet())
		{
			if (directionIndex++ > 0)
				buffer.append('\n');
			for (Clue clue : clueLists.get(direction))
			{
				buffer.append(getClueIdString(direction, clue));
				buffer.append(' ');
				buffer.append(clue.isReference() ? getClueReferenceString(direction, clue) : clue.getText());
				buffer.append('\n');
			}
		}
		Utils.putClipboardText(buffer.toString());
		return null;
	}

	//------------------------------------------------------------------

	private EditList.Element<CrosswordDocument> onImportCluesFromClipboard()
		throws AppException
	{
		EditList.Element<CrosswordDocument> edit = null;
		Clue.AnswerLengthParser answerLengthParser =
				(answerLengthPattern == null)
						? null
						: new Clue.AnswerLengthParser(answerLengthPattern, answerLengthSubstitutions);
		ImportCluesDialog.Result result =
				ImportCluesDialog.showDialog(getWindow(), Direction.DEFINED_DIRECTIONS, clueReferenceKeyword,
											 answerLengthParser, clueSubstitutions);
		if (result != null)
		{
			// Create map of old clues
			Map<Direction, List<Clue>> oldClueLists = new EnumMap<>(Direction.class);
			for (Direction direction : clueLists.keySet())
				oldClueLists.put(direction, new ArrayList<>(clueLists.get(direction)));

			// Set clue substitutions
			clueSubstitutions = result.substitutions();

			// Set clues
			for (Direction direction : result.clues().keySet())
				setClues(direction, result.clues().get(direction));

			// Update the directions of clue IDs and reference IDs
			updateClueDirections();

			// Validate clues
			if (validateClues(AppConstants.CANCEL_STR))
			{
				// Create map of new clues
				Map<Direction, List<Clue>> newClueLists = new EnumMap<>(Direction.class);
				for (Direction direction : clueLists.keySet())
				{
					newClueLists.put(direction, new ArrayList<>(clueLists.get(direction)));
					getView().updateClues(direction);
				}
				edit = new ClueListsEdit(oldClueLists, newClueLists);
			}
			else
			{
				// Restore old clue lists
				clueLists.clear();
				for (Direction direction : oldClueLists.keySet())
					clueLists.put(direction, oldClueLists.get(direction));
			}
		}
		return edit;
	}

	//------------------------------------------------------------------

	private EditList.Element<CrosswordDocument> onClearClues()
	{
		EditList.Element<CrosswordDocument> edit = null;
		Boolean clearWithEmptyClue = ClearCluesDialog.showDialog(getWindow());
		if (clearWithEmptyClue != null)
		{
			// Create list of old clues
			List<Clue> oldClues = new ArrayList<>();
			for (Direction direction : clueLists.keySet())
			{
				for (Clue clue : clueLists.get(direction))
					oldClues.add(clue);
			}

			// Create list of new clues
			List<Clue> newClues = new ArrayList<>();
			if (clearWithEmptyClue)
			{
				for (Grid.Field field : grid.getFields())
					newClues.add(new Clue(List.of(field.getId()), new StyledText(), 0));
			}

			// Clear lists of clues
			clueLists.clear();

			// Set new clues
			setClues(newClues);

			// Create edit
			edit = new CluesEdit(oldClues, newClues);
		}
		return edit;
	}

	//------------------------------------------------------------------

	private EditList.Element<CrosswordDocument> onCopyEntriesToClipboard()
		throws AppException
	{
		Utils.putClipboardText(grid.getEntriesString("\n") + "\n");
		return null;
	}

	//------------------------------------------------------------------

	private EditList.Element<CrosswordDocument> onImportEntriesFromClipboard()
		throws AppException
	{
		EditList.Element<CrosswordDocument> edit = null;
		String[] optionStrs = Utils.getOptionStrings(IMPORT_STR);
		if (JOptionPane.showOptionDialog(getWindow(), IMPORT_ENTRIES_STR, CrosswordEditorApp.SHORT_NAME,
										 JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, optionStrs,
										 optionStrs[1]) == JOptionPane.OK_OPTION)
		{
			Grid.Entries oldEntries = grid.getEntries();
			grid.setEntries(List.of(Utils.getClipboardText().strip().toUpperCase().split("\\s+")));
			getView().redrawGrid();
			edit = new GridEntriesEdit(oldEntries, grid.getEntries());
		}
		return edit;
	}

	//------------------------------------------------------------------

	private EditList.Element<CrosswordDocument> onClearEntries()
	{
		EditList.Element<CrosswordDocument> edit = null;
		String[] optionStrs = Utils.getOptionStrings(AppConstants.CLEAR_STR);
		if (JOptionPane.showOptionDialog(getWindow(), CLEAR_ENTRIES_STR, CrosswordEditorApp.SHORT_NAME,
										 JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, optionStrs,
										 optionStrs[1]) == JOptionPane.OK_OPTION)
		{
			Grid.Entries oldEntries = grid.getEntries();
			Grid.Entries newEntries = oldEntries.clone();
			newEntries.clear();
			grid.setEntries(newEntries);
			getView().redrawGrid();
			edit = new GridEntriesEdit(oldEntries, newEntries);
		}
		return edit;
	}

	//------------------------------------------------------------------

	private EditList.Element<CrosswordDocument> onCopyFieldNumbersToClipboard()
		throws AppException
	{
		StringBuilder buffer = new StringBuilder(256);
		for (Direction direction : Direction.DEFINED_DIRECTIONS)
		{
			if (!buffer.isEmpty())
				buffer.append('\n');
			for (Grid.Field field : grid.getFields(direction))
			{
				buffer.append(field.getNumber());
				buffer.append('\n');
			}
		}
		Utils.putClipboardText(buffer.toString());
		return null;
	}

	//------------------------------------------------------------------

	private EditList.Element<CrosswordDocument> onCopyFieldIdsToClipboard()
		throws AppException
	{
		StringBuilder buffer = new StringBuilder(256);
		for (Direction direction : Direction.DEFINED_DIRECTIONS)
		{
			if (!buffer.isEmpty())
				buffer.append('\n');
			for (Grid.Field field : grid.getFields(direction))
			{
				buffer.append(field.getId());
				buffer.append('\n');
			}
		}
		Utils.putClipboardText(buffer.toString());
		return null;
	}

	//------------------------------------------------------------------

	private EditList.Element<CrosswordDocument> onHighlightIncorrectEntries()
	{
		grid.checkEntries();
		getView().redrawGrid();
		return null;
	}

	//------------------------------------------------------------------

	private EditList.Element<CrosswordDocument> onShowSolution()
	{
		EditList.Element<CrosswordDocument> edit = null;
		String messageStr = grid.isEntriesEmpty() ? SHOW_SOLUTION1_STR : SHOW_SOLUTION2_STR;
		String[] optionStrs = Utils.getOptionStrings(SHOW_STR);
		if (JOptionPane.showOptionDialog(getWindow(), messageStr, CrosswordEditorApp.SHORT_NAME,
										 JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, optionStrs,
										 optionStrs[1]) == JOptionPane.OK_OPTION)
		{
			Grid.Entries oldEntries = grid.getEntries();
			Grid.Entries newEntries = grid.getSolution();
			grid.setEntries(newEntries);
			getView().redrawGrid();
			edit = new GridEntriesEdit(oldEntries, newEntries);
		}
		return edit;
	}

	//------------------------------------------------------------------

	private EditList.Element<CrosswordDocument> onSetSolution()
	{
		EditList.Element<CrosswordDocument> edit = null;
		String[] optionStrs = Utils.getOptionStrings(SET_STR);
		if (JOptionPane.showOptionDialog(getWindow(), SET_SOLUTION_STR, CrosswordEditorApp.SHORT_NAME,
										 JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, optionStrs,
										 optionStrs[1]) == JOptionPane.OK_OPTION)
		{
			edit = onEditSolutionProperties();
			if (edit != null)
			{
				Grid.Entries oldSolution = grid.getSolution();
				grid.setSolution();
				CompoundEdit compoundEdit = new CompoundEdit(SOLUTION_AND_PROPS_STR);
				compoundEdit.addEdit(edit);
				compoundEdit.addEdit(new SolutionEdit(oldSolution, grid.getSolution()));
				edit = compoundEdit;
			}
		}
		return edit;
	}

	//------------------------------------------------------------------

	private EditList.Element<CrosswordDocument> onImportSolutionFromClipboard()
		throws AppException
	{
		EditList.Element<CrosswordDocument> edit = null;
		String[] optionStrs = Utils.getOptionStrings(IMPORT_STR);
		if (JOptionPane.showOptionDialog(getWindow(), IMPORT_SOLUTION_STR, CrosswordEditorApp.SHORT_NAME,
										 JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, optionStrs,
										 optionStrs[1]) == JOptionPane.OK_OPTION)
		{
			Grid.Entries oldSolution = grid.getSolution();
			grid.setSolution(List.of(Utils.getClipboardText().strip().toUpperCase().split("\\s+")));
			edit = new SolutionEdit(oldSolution, grid.getSolution());
		}
		return edit;
	}

	//------------------------------------------------------------------

	private EditList.Element<CrosswordDocument> onLoadSolution()
		throws AppException
	{
		EditList.Element<CrosswordDocument> edit = null;
		String[] optionStrs = Utils.getOptionStrings(LOAD_STR);
		String messageStr = LOCATION_STR + solutionProperties.location + "\n" + LOAD_SOLUTION2_STR;
		if (JOptionPane.showOptionDialog(getWindow(), messageStr, CrosswordEditorApp.SHORT_NAME,
										 JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, optionStrs,
										 optionStrs[1]) == JOptionPane.OK_OPTION)
		{
			try
			{
				// Read solution from remote location
				Grid.Entries oldSolution = grid.getSolution();
				TaskProgressDialog.showDialog(getWindow(), LOAD_SOLUTION1_STR, new Task.LoadSolution(this));

				// Validate hash
				if (!Arrays.equals(solutionProperties.hashValue,
								   solutionProperties.remoteDocument.solutionProperties.hashValue))
					throw new UrlException(ErrorId.REMOTE_SOLUTION_HAS_INCORRECT_HASH, solutionProperties.location);

				// Get solution of remote document
				Grid.Entries solution = solutionProperties.remoteDocument.grid.getSolution();
				if (solution == null)
					throw new UrlException(ErrorId.NO_SOLUTION_IN_REMOTE_DOCUMENT, solutionProperties.location);

				// Set solution of this document
				grid.setSolution(solution);

				// Create edit
				edit = new SolutionEdit(oldSolution, grid.getSolution());
			}
			catch (TaskCancelledException e)
			{
				// ignore
			}
		}
		return edit;
	}

	//------------------------------------------------------------------

	private EditList.Element<CrosswordDocument> onClearSolution()
	{
		EditList.Element<CrosswordDocument> edit = null;
		String[] optionStrs = Utils.getOptionStrings(AppConstants.CLEAR_STR);
		if (JOptionPane.showOptionDialog(getWindow(), CLEAR_SOLUTION_STR, CrosswordEditorApp.SHORT_NAME,
										 JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, optionStrs,
										 optionStrs[1]) == JOptionPane.OK_OPTION)
		{
			Grid.Entries oldSolution = grid.getSolution();
			grid.clearSolution();
			edit = new SolutionEdit(oldSolution, null);
		}
		return edit;
	}

	//------------------------------------------------------------------

	private EditList.Element<CrosswordDocument> onCopySolutionToClipboard()
		throws AppException
	{
		Utils.putClipboardText(grid.getSolutionString("\n") + "\n");
		return null;
	}

	//------------------------------------------------------------------

	private EditList.Element<CrosswordDocument> onEditSolutionProperties()
	{
		EditList.Element<CrosswordDocument> edit = null;
		SolutionProperties result = SolutionPropertiesDialog.showDialog(getWindow(), solutionProperties);
		if (result != null)
		{
			SolutionProperties oldProperties = solutionProperties;
			solutionProperties = result;
			edit = new SolutionPropertiesEdit(oldProperties, solutionProperties);
		}
		return edit;
	}

	//------------------------------------------------------------------

	private EditList.Element<CrosswordDocument> onToggleFieldNumbers()
	{
		showFieldNumbers = !showFieldNumbers;
		CrosswordView view = getView();
		if (view != null)
			view.redrawGrid();
		return null;
	}

	//------------------------------------------------------------------

	private EditList.Element<CrosswordDocument> onToggleClues()
	{
		showClues = !showClues;
		CrosswordView view = getView();
		if (view != null)
		{
			for (Direction direction : Direction.DEFINED_DIRECTIONS)
				getView().updateClues(direction);
		}
		return null;
	}

	//------------------------------------------------------------------

	private EditList.Element<CrosswordDocument> onResizeWindowToView()
	{
		CrosswordView view = getView();
		if (view != null)
			view.setPreferredViewportSize(view.getViewport().getView().getPreferredSize());
		return null;
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Enumerated types
////////////////////////////////////////////////////////////////////////


	// COMMANDS


	enum Command
		implements Action
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		// Commands

		UNDO
		(
			"undo",
			UNDO_STR,
			KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.CTRL_DOWN_MASK)
		),

		REDO
		(
			"redo",
			REDO_STR,
			KeyStroke.getKeyStroke(KeyEvent.VK_Y, KeyEvent.CTRL_DOWN_MASK)
		),

		CLEAR_EDIT_LIST
		(
			"clearEditList",
			"Clear edit history" + AppConstants.ELLIPSIS_STR
		),

		EDIT_CLUE
		(
			"editClue",
			"Edit clue" + AppConstants.ELLIPSIS_STR,
			KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0)
		),

		EDIT_GRID
		(
			"editGrid",
			"Edit grid" + AppConstants.ELLIPSIS_STR
		),

		EDIT_TEXT_SECTIONS
		(
			"editTextSections",
			"Edit text sections" + AppConstants.ELLIPSIS_STR,
			KeyStroke.getKeyStroke(KeyEvent.VK_T, KeyEvent.CTRL_DOWN_MASK)
		),

		EDIT_INDICATIONS
		(
			"editIndications",
			"Edit indications" + AppConstants.ELLIPSIS_STR
		),

		SET_ENTRY_CHARACTER
		(
			"setEntryCharacter"
		),

		COPY_CLUES_TO_CLIPBOARD
		(
			"copyCluesToClipboard",
			"Copy clues to clipboard"
		),

		IMPORT_CLUES_FROM_CLIPBOARD
		(
			"importCluesFromClipboard",
			"Import clues from clipboard" + AppConstants.ELLIPSIS_STR
		),

		CLEAR_CLUES
		(
			"clearClues",
			"Clear all clues" + AppConstants.ELLIPSIS_STR
		),

		COPY_ENTRIES_TO_CLIPBOARD
		(
			"copyEntriesToClipboard",
			"Copy grid entries to clipboard"
		),

		IMPORT_ENTRIES_FROM_CLIPBOARD
		(
			"importEntriesFromClipboard",
			"Import grid entries from clipboard" + AppConstants.ELLIPSIS_STR
		),

		CLEAR_ENTRIES
		(
			"clearEntries",
			"Clear all grid entries" + AppConstants.ELLIPSIS_STR
		),

		COPY_FIELD_NUMBERS_TO_CLIPBOARD
		(
			"copyFieldNumberToClipboard",
			"Copy field numbers to clipboard"
		),

		COPY_FIELD_IDS_TO_CLIPBOARD
		(
			"copyFieldIdsToClipboard",
			"Copy field IDs to clipboard"
		),

		HIGHLIGHT_INCORRECT_ENTRIES
		(
			"highlightIncorrectEntries",
			"Highlight incorrect grid entries",
			KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0)
		),

		SHOW_SOLUTION
		(
			"showSolution",
			"Show solution" + AppConstants.ELLIPSIS_STR
		),

		SET_SOLUTION
		(
			"setSolution",
			"Set solution to current entries" + AppConstants.ELLIPSIS_STR
		),

		IMPORT_SOLUTION_FROM_CLIPBOARD
		(
			"importSolutionFromClipboard",
			"Import solution from clipboard" + AppConstants.ELLIPSIS_STR
		),

		LOAD_SOLUTION
		(
			"loadSolution",
			"Load solution" + AppConstants.ELLIPSIS_STR
		),

		CLEAR_SOLUTION
		(
			"clearSolution",
			"Clear solution" + AppConstants.ELLIPSIS_STR
		),

		COPY_SOLUTION_TO_CLIPBOARD
		(
			"copySolutionToClipboard",
			"Copy solution to clipboard"
		),

		EDIT_SOLUTION_PROPERTIES
		(
			"editSolutionProperties",
			"Edit solution properties" + AppConstants.ELLIPSIS_STR
		),

		TOGGLE_FIELD_NUMBERS
		(
			"toggleFieldNumbers",
			"Show field numbers"
		),

		TOGGLE_CLUES
		(
			"toggleClues",
			"Show clues"
		),

		RESIZE_WINDOW_TO_VIEW
		(
			"resizeWindowToView",
			"Resize window to view",
			KeyStroke.getKeyStroke(KeyEvent.VK_F12, 0)
		);

		//--------------------------------------------------------------

		// Property keys
		interface Property
		{
			String	GRID_ENTRY_VALUE	= "gridEntryValue";
			String	DIRECTION			= "direction";
		}

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	uk.blankaspect.ui.swing.action.Command	command;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private Command(String key)
		{
			command = new uk.blankaspect.ui.swing.action.Command(this);
			putValue(Action.ACTION_COMMAND_KEY, key);
		}

		//--------------------------------------------------------------

		private Command(String key,
						String name)
		{
			this(key);
			putValue(Action.NAME, name);
		}

		//--------------------------------------------------------------

		private Command(String    key,
						String    name,
						KeyStroke acceleratorKey)
		{
			this(key, name);
			putValue(Action.ACCELERATOR_KEY, acceleratorKey);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Class methods
	////////////////////////////////////////////////////////////////////

		public static void setAllEnabled(boolean enabled)
		{
			for (Command command : values())
				command.setEnabled(enabled);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : Action interface
	////////////////////////////////////////////////////////////////////

		@Override
		public void addPropertyChangeListener(PropertyChangeListener listener)
		{
			command.addPropertyChangeListener(listener);
		}

		//--------------------------------------------------------------

		@Override
		public Object getValue(String key)
		{
			return command.getValue(key);
		}

		//--------------------------------------------------------------

		@Override
		public boolean isEnabled()
		{
			return command.isEnabled();
		}

		//--------------------------------------------------------------

		@Override
		public void putValue(String key,
							 Object value)
		{
			command.putValue(key, value);
		}

		//--------------------------------------------------------------

		@Override
		public void removePropertyChangeListener(PropertyChangeListener listener)
		{
			command.removePropertyChangeListener(listener);
		}

		//--------------------------------------------------------------

		@Override
		public void setEnabled(boolean enabled)
		{
			command.setEnabled(enabled);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : ActionListener interface
	////////////////////////////////////////////////////////////////////

		@Override
		public void actionPerformed(ActionEvent event)
		{
			CrosswordDocument document = CrosswordEditorApp.INSTANCE.getDocument();
			if (document != null)
				document.executeCommand(this);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		public void setSelected(boolean selected)
		{
			putValue(Action.SELECTED_KEY, selected);
		}

		//--------------------------------------------------------------

		public void execute()
		{
			actionPerformed(null);
		}

		//--------------------------------------------------------------

	}

	//==================================================================


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

		ERROR_WRITING_FILE
		("An error occurred when writing the file."),

		FILE_ACCESS_NOT_PERMITTED
		("Access to the file was not permitted."),

		FAILED_TO_CREATE_TEMPORARY_FILE
		("Failed to create a temporary file."),

		FAILED_TO_DELETE_FILE
		("Failed to delete the existing file."),

		FAILED_TO_RENAME_FILE
		("Failed to rename the temporary file to the specified filename."),

		FILE_DOES_NOT_EXIST
		("The file does not exist."),

		NOT_A_FILE
		("The pathname does not denote a normal file."),

		FAILED_TO_CREATE_DIRECTORY
		("Failed to create the directory."),

		UNEXPECTED_DOCUMENT_FORMAT
		("The document does not have the expected format."),

		NO_VERSION_NUMBER
		("The document does not have a version number."),

		INVALID_VERSION_NUMBER
		("The version number of the document is invalid."),

		UNSUPPORTED_DOCUMENT_VERSION
		("The version of the document (%1) is not supported by this version of " + CrosswordEditorApp.SHORT_NAME + "."),

		NO_ATTRIBUTE
		("The required attribute is missing."),

		INVALID_ATTRIBUTE
		("The attribute is invalid."),

		NO_GRID_ELEMENT
		("The document does not have a <grid> element."),

		MULTIPLE_GRID_ELEMENTS
		("The document has more than one <grid> element."),

		MULTIPLE_CLUES_ELEMENTS
		("The document has more than one <clues> element for the %1 direction."),

		INVALID_FIELD_ID
		("The ID does not refer to a field in the grid."),

		MALFORMED_PATTERN
		("The pattern is not a well-formed regular expression.\n(%1)"),

		MALFORMED_SUBSTITUTION
		("The substitution is malformed."),

		FAILED_TO_CONNECT
		("Failed to connect to the remote document."),

		REMOTE_SOLUTION_HAS_INCORRECT_HASH
		("The solution in the remote document is not a solution for this crossword."),

		NO_SOLUTION_IN_REMOTE_DOCUMENT
		("The remote document does not contain a solution."),

		NOT_ENOUGH_MEMORY_TO_PERFORM_COMMAND
		("There was not enough memory to perform the command.\n" +
			"Clearing the list of undo/redo actions may make more memory available.");

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	String	message;

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

		@Override
		public String getMessage()
		{
			return message;
		}

		//--------------------------------------------------------------

	}

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Member classes : non-inner classes
////////////////////////////////////////////////////////////////////////


	// SOLUTION PROPERTIES CLASS


	public static class SolutionProperties
		implements Cloneable
	{

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	URL					location;
		private	String				passphrase;
		private	byte[]				hashValue;
		private	CrosswordDocument	remoteDocument;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		public SolutionProperties()
		{
			passphrase = "";
		}

		//--------------------------------------------------------------

		public SolutionProperties(URL    location,
								  String passphrase,
								  byte[] hashValue)
		{
			this.location = location;
			this.passphrase = passphrase;
			this.hashValue = hashValue;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		public SolutionProperties clone()
		{
			try
			{
				return (SolutionProperties)super.clone();
			}
			catch (CloneNotSupportedException e)
			{
				throw new UnexpectedRuntimeException(e);
			}
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		public URL getLocation()
		{
			return location;
		}

		//--------------------------------------------------------------

		public String getPassphrase()
		{
			return passphrase;
		}

		//--------------------------------------------------------------

		public byte[] getHashValue()
		{
			return hashValue;
		}

		//--------------------------------------------------------------

		private boolean canLoad()
		{
			return (location != null) && (hashValue != null);
		}

		//--------------------------------------------------------------

	}

	//==================================================================


	// STYLE PROPERTIES CLASS


	public static class StyleProperties
	{

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		StringList	fontNames;
		int			fontSize;
		int			cellSize;
		Color		gridColour;
		Color		entryColour;
		double		fieldNumberFontSizeFactor;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		public StyleProperties(StringList fontNames,
							   int        fontSize,
							   int        cellSize,
							   Color      gridColour,
							   Color      entryColour,
							   double     fieldNumberFontSizeFactor)
		{
			this.fontNames = fontNames;
			this.fontSize = fontSize;
			this.cellSize = cellSize;
			this.gridColour = gridColour;
			this.entryColour = entryColour;
			this.fieldNumberFontSizeFactor = fieldNumberFontSizeFactor;
		}

		//--------------------------------------------------------------

	}

	//==================================================================


	// XML PARSE EXCEPTION EXTENDER CLASS


	private static class XmlParseExceptionExtender
	{

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		File	file;
		URL		url;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private XmlParseExceptionExtender(File file)
		{
			this.file = file;
		}

		//--------------------------------------------------------------

		private XmlParseExceptionExtender(URL url)
		{
			this.url = url;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		private XmlParseException extend(XmlParseException exception)
		{
			return (url == null) ? new XmlParseException(exception, file) : new XmlParseException(exception, url);
		}

		//--------------------------------------------------------------

	}

	//==================================================================


	// GRID EDIT CLASS


	private static class GridEdit
		extends EditList.Element<CrosswordDocument>
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		private static final	String	TEXT	= "grid";

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	Grid.Separator	separator;
		private	int				numColumns;
		private	int				numRows;
		private	Grid.Symmetry	oldSymmetry;
		private	String			oldDefinition;
		private	Grid.Symmetry	newSymmetry;
		private	String			newDefinition;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private GridEdit(Grid.Separator separator,
						 int            numColumns,
						 int            numRows,
						 Grid.Symmetry  oldSymmetry,
						 String         oldDefinition,
						 Grid.Symmetry  newSymmetry,
						 String         newDefinition)
		{
			this.separator = separator;
			this.numColumns = numColumns;
			this.numRows = numRows;
			this.oldSymmetry = oldSymmetry;
			this.oldDefinition = oldDefinition;
			this.newSymmetry = newSymmetry;
			this.newDefinition = newDefinition;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		public String getText()
		{
			return TEXT;
		}

		//--------------------------------------------------------------

		@Override
		public void undo(CrosswordDocument document)
		{
			setGrid(document, oldSymmetry, oldDefinition);
		}

		//--------------------------------------------------------------

		@Override
		public void redo(CrosswordDocument document)
		{
			setGrid(document, newSymmetry, newDefinition);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		private void setGrid(CrosswordDocument document,
							 Grid.Symmetry     symmetry,
							 String            definition)
		{
			try
			{
				document.setGrid(separator.createGrid(numColumns, numRows, symmetry, definition));
				document.getView().updateGrid();
			}
			catch (AppException e)
			{
				throw new UnexpectedRuntimeException(e);
			}
		}

		//--------------------------------------------------------------

	}

	//==================================================================


	// GRID ENTRY CHARACTER EDIT CLASS


	private static class GridEntryCharEdit
		extends EditList.Element<CrosswordDocument>
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		private static final	String	TEXT	= "grid entry";

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	int			row;
		private	int			column;
		private	Direction	direction;
		private	char		oldValue;
		private	char		newValue;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private GridEntryCharEdit(int       row,
								  int       column,
								  Direction direction,
								  char      oldValue,
								  char      newValue)
		{
			this.row = row;
			this.column = column;
			this.direction = direction;
			this.oldValue = oldValue;
			this.newValue = newValue;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		public String getText()
		{
			return TEXT;
		}

		//--------------------------------------------------------------

		@Override
		public void undo(CrosswordDocument document)
		{
			document.grid.setEntryValue(row, column, oldValue);
			document.getView().setGridCaretPosition(row, column, direction);
		}

		//--------------------------------------------------------------

		@Override
		public void redo(CrosswordDocument document)
		{
			document.grid.setEntryValue(row, column, newValue);
			document.getView().setGridCaretPosition(row, column, direction);
		}

		//--------------------------------------------------------------

	}

	//==================================================================


	// GRID ENTRIES EDIT CLASS


	private static class GridEntriesEdit
		extends EditList.Element<CrosswordDocument>
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		private static final	String	TEXT	= "grid entries";

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	Grid.Entries	oldEntries;
		private	Grid.Entries	newEntries;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private GridEntriesEdit(Grid.Entries oldEntries,
								Grid.Entries newEntries)
		{
			this.oldEntries = oldEntries.clone();
			this.newEntries = newEntries.clone();
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		public String getText()
		{
			return TEXT;
		}

		//--------------------------------------------------------------

		@Override
		public void undo(CrosswordDocument document)
		{
			document.grid.setEntries(oldEntries);
			document.getView().redrawGrid();
		}

		//--------------------------------------------------------------

		@Override
		public void redo(CrosswordDocument document)
		{
			document.grid.setEntries(newEntries);
			document.getView().redrawGrid();
		}

		//--------------------------------------------------------------

	}

	//==================================================================


	// SOLUTION EDIT CLASS


	private static class SolutionEdit
		extends EditList.Element<CrosswordDocument>
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		private static final	String	TEXT	= "solution";

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	Grid.Entries	oldSolution;
		private	Grid.Entries	newSolution;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private SolutionEdit(Grid.Entries oldSolution,
							 Grid.Entries newSolution)
		{
			if (oldSolution != null)
				this.oldSolution = oldSolution.clone();
			if (newSolution != null)
				this.newSolution = newSolution.clone();
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		public String getText()
		{
			return TEXT;
		}

		//--------------------------------------------------------------

		@Override
		public void undo(CrosswordDocument document)
		{
			document.grid.setSolution(oldSolution);
		}

		//--------------------------------------------------------------

		@Override
		public void redo(CrosswordDocument document)
		{
			document.grid.setSolution(newSolution);
		}

		//--------------------------------------------------------------

	}

	//==================================================================


	// SOLUTION PROPERTIES EDIT CLASS


	private static class SolutionPropertiesEdit
		extends EditList.Element<CrosswordDocument>
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		private static final	String	TEXT	= "solution properties";

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	SolutionProperties	oldProperties;
		private	SolutionProperties	newProperties;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private SolutionPropertiesEdit(SolutionProperties oldProperties,
									   SolutionProperties newProperties)
		{
			this.oldProperties = oldProperties;
			this.newProperties = newProperties;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		public String getText()
		{
			return TEXT;
		}

		//--------------------------------------------------------------

		@Override
		public void undo(CrosswordDocument document)
		{
			document.solutionProperties = oldProperties;
		}

		//--------------------------------------------------------------

		@Override
		public void redo(CrosswordDocument document)
		{
			document.solutionProperties = newProperties;
		}

		//--------------------------------------------------------------

	}

	//==================================================================


	// CLUES EDIT CLASS


	private static class CluesEdit
		extends EditList.Element<CrosswordDocument>
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		private static final	String	TEXT	= "clues";

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	List<Clue>	oldClues;
		private	List<Clue>	newClues;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CluesEdit(List<Clue> oldClues,
						  List<Clue> newClues)
		{
			this.oldClues = oldClues;
			this.newClues = newClues;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		public String getText()
		{
			return TEXT;
		}

		//--------------------------------------------------------------

		@Override
		public void undo(CrosswordDocument document)
		{
			document.setClues(oldClues);
		}

		//--------------------------------------------------------------

		@Override
		public void redo(CrosswordDocument document)
		{
			document.setClues(newClues);
		}

		//--------------------------------------------------------------

	}

	//==================================================================


	// CLUE LISTS EDIT CLASS


	private static class ClueListsEdit
		extends EditList.Element<CrosswordDocument>
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		private static final	String	TEXT	= "clue lists";

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	Map<Direction, List<Clue>>	oldClueLists;
		private	Map<Direction, List<Clue>>	newClueLists;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private ClueListsEdit(Map<Direction, List<Clue>> oldClueLists,
							  Map<Direction, List<Clue>> newClueLists)
		{
			this.oldClueLists = oldClueLists;
			this.newClueLists = newClueLists;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		public String getText()
		{
			return TEXT;
		}

		//--------------------------------------------------------------

		@Override
		public void undo(CrosswordDocument document)
		{
			document.clueLists.clear();
			for (Direction direction : oldClueLists.keySet())
				document.clueLists.put(direction, new ArrayList<>(oldClueLists.get(direction)));
			for (Direction direction : Direction.DEFINED_DIRECTIONS)
				document.getView().updateClues(direction);
		}

		//--------------------------------------------------------------

		@Override
		public void redo(CrosswordDocument document)
		{
			document.clueLists.clear();
			for (Direction direction : newClueLists.keySet())
				document.clueLists.put(direction, new ArrayList<>(newClueLists.get(direction)));
			for (Direction direction : Direction.DEFINED_DIRECTIONS)
				document.getView().updateClues(direction);
		}

		//--------------------------------------------------------------

	}

	//==================================================================


	// TEXT SECTIONS EDIT CLASS


	private static class TextSectionsEdit
		extends EditList.Element<CrosswordDocument>
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		private static final	String	TEXT	= "text sections";

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	EnumSet<TextSection>	sections;
		private	String					oldTitle;
		private	List<String>			oldPrologue;
		private	List<String>			oldEpilogue;
		private	String					newTitle;
		private	List<String>			newPrologue;
		private	List<String>			newEpilogue;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private TextSectionsEdit(String       oldTitle,
								 List<String> oldPrologue,
								 List<String> oldEpilogue,
								 String       newTitle,
								 List<String> newPrologue,
								 List<String> newEpilogue)
		{
			sections = EnumSet.noneOf(TextSection.class);
			this.oldTitle = oldTitle;
			this.oldPrologue = oldPrologue;
			this.oldEpilogue = oldEpilogue;
			this.newTitle = newTitle;
			this.newPrologue = newPrologue;
			this.newEpilogue = newEpilogue;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		public String getText()
		{
			return TEXT;
		}

		//--------------------------------------------------------------

		@Override
		public void undo(CrosswordDocument document)
		{
			setTextSections(document, oldTitle, oldPrologue, oldEpilogue);
		}

		//--------------------------------------------------------------

		@Override
		public void redo(CrosswordDocument document)
		{
			setTextSections(document, newTitle, newPrologue, newEpilogue);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		private void setTextSections(CrosswordDocument document,
									 String            title,
									 List<String>      prologue,
									 List<String>      epilogue)
		{
			document.setTitle(title);
			document.setPrologue(prologue);
			document.setEpilogue(epilogue);
			document.getView().updateTextSections(sections);
		}

		//--------------------------------------------------------------

	}

	//==================================================================


	// INDICATIONS EDIT CLASS


	private static class IndicationsEdit
		extends EditList.Element<CrosswordDocument>
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		private static final	String	TEXT	= "indications";

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	String				oldClueReferenceKeyword;
		private	String				oldAnswerLengthPattern;
		private	List<Substitution>	oldAnswerLengthSubstitutions;
		private	String				oldLineBreak;
		private	String				newClueReferenceKeyword;
		private	String				newAnswerLengthPattern;
		private	List<Substitution>	newAnswerLengthSubstitutions;
		private	String				newLineBreak;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private IndicationsEdit(String             oldClueReferenceKeyword,
								String             oldAnswerLengthPattern,
								List<Substitution> oldAnswerLengthSubstitutions,
								String             oldLineBreak,
								String             newClueReferenceKeyword,
								String             newAnswerLengthPattern,
								List<Substitution> newAnswerLengthSubstitutions,
								String             newLineBreak)
		{
			this.oldClueReferenceKeyword = oldClueReferenceKeyword;
			this.oldAnswerLengthPattern = oldAnswerLengthPattern;
			this.oldAnswerLengthSubstitutions = new ArrayList<>();
			for (Substitution substitution : oldAnswerLengthSubstitutions)
				this.oldAnswerLengthSubstitutions.add(substitution.clone());
			this.oldLineBreak = oldLineBreak;
			this.newClueReferenceKeyword = newClueReferenceKeyword;
			this.newAnswerLengthPattern = newAnswerLengthPattern;
			this.newAnswerLengthSubstitutions = new ArrayList<>();
			for (Substitution substitution : newAnswerLengthSubstitutions)
				this.newAnswerLengthSubstitutions.add(substitution.clone());
			this.newLineBreak = newLineBreak;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		public String getText()
		{
			return TEXT;
		}

		//--------------------------------------------------------------

		@Override
		public void undo(CrosswordDocument document)
		{
			setIndications(document, oldClueReferenceKeyword, oldAnswerLengthPattern,
						   oldAnswerLengthSubstitutions, oldLineBreak);
		}

		//--------------------------------------------------------------

		@Override
		public void redo(CrosswordDocument document)
		{
			setIndications(document, newClueReferenceKeyword, newAnswerLengthPattern,
						   newAnswerLengthSubstitutions, newLineBreak);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		private void setIndications(CrosswordDocument  document,
									String             clueReferenceKeyword,
									String             answerLengthPattern,
									List<Substitution> answerLengthSubstitutions,
									String             lineBreak)
		{
			document.clueReferenceKeyword = clueReferenceKeyword;
			document.answerLengthPattern = answerLengthPattern;
			document.answerLengthSubstitutions = answerLengthSubstitutions;
			document.lineBreak = lineBreak;
		}

		//--------------------------------------------------------------

	}

	//==================================================================


	// COMPOUND EDIT CLASS


	private static class CompoundEdit
		extends EditList.Element<CrosswordDocument>
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CompoundEdit(String text)
		{
			this.text = text;
			edits = new ArrayList<>();
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		public String getText()
		{
			return text;
		}

		//--------------------------------------------------------------

		@Override
		public void undo(CrosswordDocument document)
		{
			for (int i = edits.size() - 1; i >= 0; i--)
				edits.get(i).undo(document);
		}

		//--------------------------------------------------------------

		@Override
		public void redo(CrosswordDocument document)
		{
			for (EditList.Element<CrosswordDocument> edit : edits)
				edit.redo(document);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		public void addEdit(EditList.Element<CrosswordDocument> edit)
		{
			edits.add(edit);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	String										text;
		private	List<EditList.Element<CrosswordDocument>>	edits;

	}

	//==================================================================

}

//----------------------------------------------------------------------
