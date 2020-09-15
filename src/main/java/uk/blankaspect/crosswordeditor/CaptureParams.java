/*====================================================================*\

CaptureParams.java

Crossword capture parameters class.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.crosswordeditor;

//----------------------------------------------------------------------


// IMPORTS


import java.lang.reflect.Field;

import java.util.ArrayList;
import java.util.List;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.w3c.dom.Element;

import uk.blankaspect.common.exception.AppException;

import uk.blankaspect.common.property.ParameterSet;
import uk.blankaspect.common.property.Property;

import uk.blankaspect.common.regex.RegexUtils;
import uk.blankaspect.common.regex.Substitution;

import uk.blankaspect.common.xml.XmlParseException;

//----------------------------------------------------------------------


// CROSSWORD CAPTURE PARAMETERS CLASS


class CaptureParams
	extends ParameterSet
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	public static final		int	MIN_X_OFFSET		= -50;
	public static final		int	MAX_X_OFFSET		= 50;
	public static final		int	DEFAULT_X_OFFSET	= 10;

	public static final		int	MIN_Y_OFFSET		= -50;
	public static final		int	MAX_Y_OFFSET		= 50;
	public static final		int	DEFAULT_Y_OFFSET	= 10;

	public static final		int	MIN_SAMPLE_SIZE		= 1;
	public static final		int	MAX_SAMPLE_SIZE		= 5;
	public static final		int	DEFAULT_SAMPLE_SIZE	= 1;

	public static final		int	MIN_BRIGHTNESS_THRESHOLD		= 1;
	public static final		int	MAX_BRIGHTNESS_THRESHOLD		= 99;
	public static final		int	DEFAULT_BRIGHTNESS_THRESHOLD	= 50;

	public static final		int	MIN_BAR_WIDTH_THRESHOLD		= 1;
	public static final		int	MAX_BAR_WIDTH_THRESHOLD		= 9;
	public static final		int	DEFAULT_BAR_WIDTH_THRESHOLD	= 3;

	public static final		int	DEFAULT_GRID_LINE_BRIGHTNESS_THRESHOLD	= 50;

	public static final		int	MIN_GRID_LINE_MIN_LENGTH		= 1;
	public static final		int	MAX_GRID_LINE_MIN_LENGTH		= 999;
	public static final		int	DEFAULT_GRID_LINE_MIN_LENGTH	= 200;

	public static final		int	MIN_GRID_LINE_MIN_SEPARATION		= 1;
	public static final		int	MAX_GRID_LINE_MIN_SEPARATION		= 99;
	public static final		int	DEFAULT_GRID_LINE_MIN_SEPARATION	= 16;

	public static final		int	MIN_GRID_LINE_ENDPOINT_TOLERANCE		= 0;
	public static final		int	MAX_GRID_LINE_ENDPOINT_TOLERANCE		= 6;
	public static final		int	DEFAULT_GRID_LINE_ENDPOINT_TOLERANCE	= 1;

	private static final	int	MAX_NUM_CLUE_SUBSTITUTIONS	= 200;

	private static final	int	MAX_NUM_ANSWER_LENGTH_SUBSTITUTIONS	= 8;

	private interface Key
	{
		String	ANSWER_LENGTH_PATTERN		= "answerLengthPattern";
		String	ANSWER_LENGTH_SUBSTITUTION	= "answerLengthSubstitution";
		String	AUTOMATIC_GRID_DETECTION	= "automaticGridDetection";
		String	BAR_BRIGHTNESS_THRESHOLD	= "barBrightnessThreshold";
		String	BAR_WIDTH_THRESHOLD			= "barWidthThreshold";
		String	BLOCK_BRIGHTNESS_THRESHOLD	= "blockBrightnessThreshold";
		String	BRIGHTNESS_THRESHOLD		= "brightnessThreshold";
		String	CLUE_REFERENCE_KEYWORD		= "clueReferenceKeyword";
		String	CLUE_SUBSTITUTION			= "clueSubstitution";
		String	DOCUMENT_DIRECTORY			= "documentDirectory";
		String	ENDPOINT_TOLERANCE			= "endpointTolerance";
		String	FILENAME					= "filename";
		String	GRID_LINE					= "gridLine";
		String	GRID_SEPARATOR				= "gridSeparator";
		String	HTML_DIRECTORY				= "htmlDirectory";
		String	MIN_LENGTH					= "minLength";
		String	MIN_SEPARATION				= "minSeparation";
		String	NUM_COLUMNS					= "numColumns";
		String	NUM_ROWS					= "numRows";
		String	SAMPLE_SIZE					= "sampleSize";
		String	TITLE						= "title";
		String	X_OFFSET					= "xOffset";
		String	Y_OFFSET					= "yOffset";
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
//  Member classes : inner classes
////////////////////////////////////////////////////////////////////////


	// PROPERTY CLASS: GRID SEPARATOR


	private class CPGridSeparator
		extends Property.EnumProperty<Grid.Separator>
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CPGridSeparator()
		{
			super(Key.GRID_SEPARATOR, Grid.Separator.class);
			value = Grid.DEFAULT_SEPARATOR;
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public Grid.Separator getGridSeparator()
	{
		return cpGridSeparator.getValue();
	}

	//------------------------------------------------------------------

	public void setGridSeparator(Grid.Separator value)
	{
		cpGridSeparator.setValue(value);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance variables : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	CPGridSeparator	cpGridSeparator	= new CPGridSeparator();

	//==================================================================


	// PROPERTY CLASS: NUMBER OF COLUMNS


	private class CPNumColumns
		extends Property.IntegerProperty
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CPNumColumns()
		{
			super(Key.NUM_COLUMNS, Grid.MIN_NUM_COLUMNS, Grid.MAX_NUM_COLUMNS);
			value = Grid.DEFAULT_NUM_COLUMNS;
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public int getNumColumns()
	{
		return cpNumColumns.getValue();
	}

	//------------------------------------------------------------------

	public void setNumColumns(int value)
	{
		cpNumColumns.setValue(value);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance variables : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	CPNumColumns	cpNumColumns	= new CPNumColumns();

	//==================================================================


	// PROPERTY CLASS: NUMBER OF ROWS


	private class CPNumRows
		extends Property.IntegerProperty
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CPNumRows()
		{
			super(Key.NUM_ROWS, Grid.MIN_NUM_ROWS, Grid.MAX_NUM_ROWS);
			value = Grid.DEFAULT_NUM_ROWS;
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public int getNumRows()
	{
		return cpNumRows.getValue();
	}

	//------------------------------------------------------------------

	public void setNumRows(int value)
	{
		cpNumRows.setValue(value);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance variables : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	CPNumRows	cpNumRows	= new CPNumRows();

	//==================================================================


	// PROPERTY CLASS: AUTOMATIC GRID DETECTION


	private class CPAutomaticGridDetection
		extends Property.BooleanProperty
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CPAutomaticGridDetection()
		{
			super(Key.AUTOMATIC_GRID_DETECTION);
			value = Boolean.TRUE;
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public boolean isAutomaticGridDetection()
	{
		return cpAutomaticGridDetection.getValue();
	}

	//------------------------------------------------------------------

	public void setAutomaticGridDetection(boolean value)
	{
		cpAutomaticGridDetection.setValue(value);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance variables : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	CPAutomaticGridDetection	cpAutomaticGridDetection	= new CPAutomaticGridDetection();

	//==================================================================


	// PROPERTY CLASS: X OFFSET


	private class CPXOffset
		extends Property.IntegerProperty
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CPXOffset()
		{
			super(Key.X_OFFSET, MIN_X_OFFSET, MAX_X_OFFSET);
			value = DEFAULT_X_OFFSET;
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public int getXOffset()
	{
		return cpXOffset.getValue();
	}

	//------------------------------------------------------------------

	public void setXOffset(int value)
	{
		cpXOffset.setValue(value);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance variables : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	CPXOffset	cpXOffset	= new CPXOffset();

	//==================================================================


	// PROPERTY CLASS: Y OFFSET


	private class CPYOffset
		extends Property.IntegerProperty
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CPYOffset()
		{
			super(Key.Y_OFFSET, MIN_Y_OFFSET, MAX_Y_OFFSET);
			value = DEFAULT_Y_OFFSET;
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public int getYOffset()
	{
		return cpYOffset.getValue();
	}

	//------------------------------------------------------------------

	public void setYOffset(int value)
	{
		cpYOffset.setValue(value);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance variables : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	CPYOffset	cpYOffset	= new CPYOffset();

	//==================================================================


	// PROPERTY CLASS: SAMPLE SIZE


	private class CPSampleSize
		extends Property.IntegerProperty
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CPSampleSize()
		{
			super(Key.SAMPLE_SIZE, MIN_SAMPLE_SIZE, MAX_SAMPLE_SIZE);
			value = DEFAULT_SAMPLE_SIZE;
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public int getSampleSize()
	{
		return cpSampleSize.getValue();
	}

	//------------------------------------------------------------------

	public void setSampleSize(int value)
	{
		cpSampleSize.setValue(value);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance variables : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	CPSampleSize	cpSampleSize	= new CPSampleSize();

	//==================================================================


	// PROPERTY CLASS: BLOCK BRIGHTNESS THRESHOLD


	private class CPBlockBrightnessThreshold
		extends Property.IntegerProperty
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CPBlockBrightnessThreshold()
		{
			super(Key.BLOCK_BRIGHTNESS_THRESHOLD, MIN_BRIGHTNESS_THRESHOLD, MAX_BRIGHTNESS_THRESHOLD);
			value = DEFAULT_BRIGHTNESS_THRESHOLD;
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public int getBlockBrightnessThreshold()
	{
		return cpBlockBrightnessThreshold.getValue();
	}

	//------------------------------------------------------------------

	public void setBlockBrightnessThreshold(int value)
	{
		cpBlockBrightnessThreshold.setValue(value);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance variables : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	CPBlockBrightnessThreshold	cpBlockBrightnessThreshold	= new CPBlockBrightnessThreshold();

	//==================================================================


	// PROPERTY CLASS: BAR WIDTH THRESHOLD


	private class CPBarWidthThreshold
		extends Property.IntegerProperty
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CPBarWidthThreshold()
		{
			super(Key.BAR_WIDTH_THRESHOLD, MIN_BAR_WIDTH_THRESHOLD, MAX_BAR_WIDTH_THRESHOLD);
			value = DEFAULT_BAR_WIDTH_THRESHOLD;
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public int getBarWidthThreshold()
	{
		return cpBarWidthThreshold.getValue();
	}

	//------------------------------------------------------------------

	public void setBarWidthThreshold(int value)
	{
		cpBarWidthThreshold.setValue(value);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance variables : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	CPBarWidthThreshold	cpBarWidthThreshold	= new CPBarWidthThreshold();

	//==================================================================


	// PROPERTY CLASS: BAR BRIGHTNESS THRESHOLD


	private class CPBarBrightnessThreshold
		extends Property.IntegerProperty
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CPBarBrightnessThreshold()
		{
			super(Key.BAR_BRIGHTNESS_THRESHOLD, MIN_BRIGHTNESS_THRESHOLD, MAX_BRIGHTNESS_THRESHOLD);
			value = DEFAULT_BRIGHTNESS_THRESHOLD;
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public int getBarBrightnessThreshold()
	{
		return cpBarBrightnessThreshold.getValue();
	}

	//------------------------------------------------------------------

	public void setBarBrightnessThreshold(int value)
	{
		cpBarBrightnessThreshold.setValue(value);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance variables : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	CPBarBrightnessThreshold	cpBarBrightnessThreshold	= new CPBarBrightnessThreshold();

	//==================================================================


	// PROPERTY CLASS: GRID LINE BRIGHTNESS THRESHOLD


	private class CPGridLineBrightnessThreshold
		extends Property.IntegerProperty
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CPGridLineBrightnessThreshold()
		{
			super(Property.concatenateKeys(Key.GRID_LINE, Key.BRIGHTNESS_THRESHOLD),
				  MIN_BRIGHTNESS_THRESHOLD, MAX_BRIGHTNESS_THRESHOLD);
			value = DEFAULT_GRID_LINE_BRIGHTNESS_THRESHOLD;
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public int getGridLineBrightnessThreshold()
	{
		return cpGridLineBrightnessThreshold.getValue();
	}

	//------------------------------------------------------------------

	public void setGridLineBrightnessThreshold(int value)
	{
		cpGridLineBrightnessThreshold.setValue(value);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance variables : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	CPGridLineBrightnessThreshold	cpGridLineBrightnessThreshold	=
																	new CPGridLineBrightnessThreshold();

	//==================================================================


	// PROPERTY CLASS: GRID LINE MINIMUM LENGTH


	private class CPGridLineMinLength
		extends Property.IntegerProperty
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CPGridLineMinLength()
		{
			super(Property.concatenateKeys(Key.GRID_LINE, Key.MIN_LENGTH),
				  MIN_GRID_LINE_MIN_LENGTH, MAX_GRID_LINE_MIN_LENGTH);
			value = DEFAULT_GRID_LINE_MIN_LENGTH;
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public int getGridLineMinLength()
	{
		return cpGridLineMinLength.getValue();
	}

	//------------------------------------------------------------------

	public void setGridLineMinLength(int value)
	{
		cpGridLineMinLength.setValue(value);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance variables : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	CPGridLineMinLength	cpGridLineMinLength	=   new CPGridLineMinLength();

	//==================================================================


	// PROPERTY CLASS: GRID LINE MINIMUM SEPARATION


	private class CPGridLineMinSeparation
		extends Property.IntegerProperty
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CPGridLineMinSeparation()
		{
			super(Property.concatenateKeys(Key.GRID_LINE, Key.MIN_SEPARATION),
				  MIN_GRID_LINE_MIN_SEPARATION, MAX_GRID_LINE_MIN_SEPARATION);
			value = DEFAULT_GRID_LINE_MIN_SEPARATION;
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public int getGridLineMinSeparation()
	{
		return cpGridLineMinSeparation.getValue();
	}

	//------------------------------------------------------------------

	public void setGridLineMinSeparation(int value)
	{
		cpGridLineMinSeparation.setValue(value);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance variables : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	CPGridLineMinSeparation	cpGridLineMinSeparation	=   new CPGridLineMinSeparation();

	//==================================================================


	// PROPERTY CLASS: GRID LINE ENDPOINT TOLERANCE


	private class CPGridLineEndpointTolerance
		extends Property.IntegerProperty
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CPGridLineEndpointTolerance()
		{
			super(Property.concatenateKeys(Key.GRID_LINE, Key.ENDPOINT_TOLERANCE),
				  MIN_GRID_LINE_ENDPOINT_TOLERANCE, MAX_GRID_LINE_ENDPOINT_TOLERANCE);
			value = DEFAULT_GRID_LINE_ENDPOINT_TOLERANCE;
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public int getGridLineEndpointTolerance()
	{
		return cpGridLineEndpointTolerance.getValue();
	}

	//------------------------------------------------------------------

	public void setGridLineEndpointTolerance(int value)
	{
		cpGridLineEndpointTolerance.setValue(value);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance variables : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	CPGridLineEndpointTolerance	cpGridLineEndpointTolerance	=   new CPGridLineEndpointTolerance();

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
			super(concatenateKeys(Key.CLUE_REFERENCE_KEYWORD));
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


	// PROPERTY CLASS: ANSWER-LENGTH PATTERN


	private class CPAnswerLengthPattern
		extends Property.SimpleProperty<String>
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CPAnswerLengthPattern()
		{
			super(Key.ANSWER_LENGTH_PATTERN);
			value = Clue.DEFAULT_LENGTH_REGEX;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		public void parse(Input input)
			throws AppException
		{
			try
			{
				value = input.getValue();
				Pattern.compile(value);
			}
			catch (PatternSyntaxException e)
			{
				InputException exception = new InputException(CaptureParams.ErrorId.MALFORMED_PATTERN,
															  input);
				exception.setReplacements(RegexUtils.getExceptionMessage(e));
				throw exception;
			}
		}

		//--------------------------------------------------------------

		@Override
		public String toString()
		{
			return (value.isEmpty() ? null : value.toString());
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public String getAnswerLengthPattern()
	{
		return cpAnswerLengthPattern.getValue();
	}

	//------------------------------------------------------------------

	public void setAnswerLengthPattern(String value)
	{
		cpAnswerLengthPattern.setValue(value);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance variables : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	CPAnswerLengthPattern	cpAnswerLengthPattern	= new CPAnswerLengthPattern();

	//==================================================================


	// PROPERTY CLASS: ANSWER-LENGTH SUBSTITUTION


	private class CPAnswerLengthSubstitution
		extends Property.PropertyList<Substitution>
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CPAnswerLengthSubstitution()
		{
			super(Key.ANSWER_LENGTH_SUBSTITUTION, MAX_NUM_ANSWER_LENGTH_SUBSTITUTIONS);
			fill(null);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		protected void parse(Input input,
							 int   index)
			throws AppException
		{
			try
			{
				values.set(index, new Substitution(input.getValue()));
			}
			catch (PatternSyntaxException e)
			{
				InputException exception = new InputException(CaptureParams.ErrorId.MALFORMED_PATTERN,
															  input);
				exception.setReplacements(RegexUtils.getExceptionMessage(e));
				throw exception;
			}
			catch (IllegalArgumentException e)
			{
				throw new IllegalValueException(input);
			}
		}

		//--------------------------------------------------------------

		@Override
		protected String toString(int index)
		{
			Substitution value = values.get(index);
			return ((value == null) ? null : value.toString());
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public List<Substitution> getAnswerLengthSubstitutions()
	{
		return cpAnswerLengthSubstitution.getNonNullValues();
	}

	//------------------------------------------------------------------

	public void setAnswerLengthSubstitutions(List<Substitution> values)
	{
		cpAnswerLengthSubstitution.setValues(values);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance variables : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	CPAnswerLengthSubstitution	cpAnswerLengthSubstitution	= new CPAnswerLengthSubstitution();

	//==================================================================


	// PROPERTY CLASS: CLUE SUBSTITUTION


	private class CPClueSubstitution
		extends Property.PropertyList<Substitution>
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CPClueSubstitution()
		{
			super(Key.CLUE_SUBSTITUTION, MAX_NUM_CLUE_SUBSTITUTIONS);
			fill(null);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		protected void parse(Input input,
							 int   index)
			throws AppException
		{
			try
			{
				values.set(index, new Substitution(input.getValue()));
			}
			catch (PatternSyntaxException e)
			{
				InputException exception = new InputException(CaptureParams.ErrorId.MALFORMED_PATTERN,
															  input);
				exception.setReplacements(RegexUtils.getExceptionMessage(e));
				throw exception;
			}
			catch (IllegalArgumentException e)
			{
				throw new IllegalValueException(input);
			}
		}

		//--------------------------------------------------------------

		@Override
		protected String toString(int index)
		{
			Substitution value = values.get(index);
			return ((value == null) ? null : value.toString());
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public List<Substitution> getClueSubstitutions()
	{
		return cpClueSubstitution.getNonNullValues();
	}

	//------------------------------------------------------------------

	public void setClueSubstitutions(List<Substitution> values)
	{
		cpClueSubstitution.setValues(values);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance variables : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	CPClueSubstitution	cpClueSubstitution	= new CPClueSubstitution();

	//==================================================================


	// PROPERTY CLASS: TITLE


	private class CPTitle
		extends Property.StringProperty
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CPTitle()
		{
			super(Key.TITLE);
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public String getTitle()
	{
		return cpTitle.getValue();
	}

	//------------------------------------------------------------------

	public void setTitle(String value)
	{
		cpTitle.setValue(value);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance variables : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	CPTitle	cpTitle	= new CPTitle();

	//==================================================================


	// PROPERTY CLASS: FILENAME


	private class CPFilename
		extends Property.StringProperty
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CPFilename()
		{
			super(Key.FILENAME);
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public String getFilename()
	{
		return cpFilename.getValue();
	}

	//------------------------------------------------------------------

	public void setFilename(String value)
	{
		cpFilename.setValue(value);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance variables : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	CPFilename	cpFilename	= new CPFilename();

	//==================================================================


	// PROPERTY CLASS: DOCUMENT DIRECTORY


	private class CPDocumentDirectory
		extends Property.StringProperty
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CPDocumentDirectory()
		{
			super(Key.DOCUMENT_DIRECTORY);
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public String getDocumentDirectory()
	{
		return cpDocumentDirectory.getValue();
	}

	//------------------------------------------------------------------

	public void setDocumentDirectory(String value)
	{
		cpDocumentDirectory.setValue(value);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance variables : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	CPDocumentDirectory	cpDocumentDirectory	= new CPDocumentDirectory();

	//==================================================================


	// PROPERTY CLASS: HTML DIRECTORY


	private class CPHtmlDirectory
		extends Property.StringProperty
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CPHtmlDirectory()
		{
			super(Key.HTML_DIRECTORY);
		}

		//--------------------------------------------------------------

	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance methods : associated methods in enclosing class
//--////////////////////////////////////////////////////////////////////

	public String getHtmlDirectory()
	{
		return cpHtmlDirectory.getValue();
	}

	//------------------------------------------------------------------

	public void setHtmlDirectory(String value)
	{
		cpHtmlDirectory.setValue(value);
	}

	//------------------------------------------------------------------

//--////////////////////////////////////////////////////////////////////
//--//  Instance variables : associated variables in enclosing class
//--////////////////////////////////////////////////////////////////////

	private	CPHtmlDirectory	cpHtmlDirectory	= new CPHtmlDirectory();

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	public CaptureParams()
	{
	}

	//------------------------------------------------------------------

	public CaptureParams(Grid.Separator     gridSeparator,
						 int                numColumns,
						 int                numRows,
						 boolean            autoGridDetection,
						 int                xOffset,
						 int                yOffset,
						 int                sampleSize,
						 int                blockBrightnessThreshold,
						 int                barWidthThreshold,
						 int                barBrightnessThreshold,
						 int                gridLineBrightnessThreshold,
						 int                gridLineMinLength,
						 int                gridLineMinSeparation,
						 int                gridLineEndpointTolerance,
						 String             clueReferenceKeyword,
						 String             answerLengthPattern,
						 List<Substitution> answerLengthSubstitutions,
						 List<Substitution> clueSubstitutions,
						 String             title,
						 String             filename,
						 String             documentDirectory,
						 String             htmlDirectory)
	{
		setGridSeparator(gridSeparator);
		setNumColumns(numColumns);
		setNumRows(numRows);
		setAutomaticGridDetection(autoGridDetection);
		setXOffset(xOffset);
		setYOffset(yOffset);
		setSampleSize(sampleSize);
		setBlockBrightnessThreshold(blockBrightnessThreshold);
		setBarWidthThreshold(barWidthThreshold);
		setBarBrightnessThreshold(barBrightnessThreshold);
		setGridLineBrightnessThreshold(gridLineBrightnessThreshold);
		setGridLineMinLength(gridLineMinLength);
		setGridLineMinSeparation(gridLineMinSeparation);
		setGridLineEndpointTolerance(gridLineEndpointTolerance);
		setClueReferenceKeyword(clueReferenceKeyword);
		setAnswerLengthPattern(answerLengthPattern);
		setAnswerLengthSubstitutions(answerLengthSubstitutions);
		setClueSubstitutions(clueSubstitutions);
		setTitle(title);
		setFilename(filename);
		setDocumentDirectory(documentDirectory);
		setHtmlDirectory(htmlDirectory);

		resetChanged();
	}

	//------------------------------------------------------------------

	public CaptureParams(Element element)
		throws XmlParseException
	{
		super(element);
		getProperties(this);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : overriding methods
////////////////////////////////////////////////////////////////////////

	@Override
	public CaptureParams create()
		throws AppException
	{
		return new CaptureParams(createElement());
	}

	//------------------------------------------------------------------

	@Override
	protected void getProperties(Property.ISource... propertySources)
	{
		for (Property property : getProperties())
		{
			try
			{
				property.get(propertySources);
			}
			catch (AppException e)
			{
				App.INSTANCE.showWarningMessage(App.SHORT_NAME, e);
			}
		}
	}

	//------------------------------------------------------------------

	@Override
	protected void putProperties(Property.ITarget propertyTarget)
	{
		for (Property property : getProperties())
			property.put(propertyTarget);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

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
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private	List<Property>	properties;

}

//----------------------------------------------------------------------
