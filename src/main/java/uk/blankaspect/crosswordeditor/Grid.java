/*====================================================================*\

Grid.java

Grid base class.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.crosswordeditor;

//----------------------------------------------------------------------


// IMPORTS


import java.awt.Color;
import java.awt.Rectangle;

import java.awt.image.BufferedImage;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import java.net.MalformedURLException;
import java.net.URL;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.SwingUtilities;

import org.w3c.dom.Element;

import uk.blankaspect.common.crypto.HmacSha256;
import uk.blankaspect.common.crypto.Salsa20;

import uk.blankaspect.common.exception.AppException;
import uk.blankaspect.common.exception.TaskCancelledException;
import uk.blankaspect.common.exception.UnexpectedRuntimeException;

import uk.blankaspect.common.html.CssMediaRule;
import uk.blankaspect.common.html.CssRuleSet;

import uk.blankaspect.common.misc.Base64Encoder;
import uk.blankaspect.common.misc.ColourUtils;
import uk.blankaspect.common.misc.IStringKeyed;
import uk.blankaspect.common.misc.NumberUtils;
import uk.blankaspect.common.misc.StringUtils;

import uk.blankaspect.common.xml.Attribute;
import uk.blankaspect.common.xml.XmlParseException;
import uk.blankaspect.common.xml.XmlUtils;
import uk.blankaspect.common.xml.XmlWriter;

//----------------------------------------------------------------------


// GRID BASE CLASS


abstract class Grid
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	public static final		int	MIN_NUM_COLUMNS		= 2;
	public static final		int	MAX_NUM_COLUMNS		= 99;
	public static final		int	DEFAULT_NUM_COLUMNS	= 15;

	public static final		int	MIN_NUM_ROWS		= MIN_NUM_COLUMNS;
	public static final		int	MAX_NUM_ROWS		= MAX_NUM_COLUMNS;
	public static final		int	DEFAULT_NUM_ROWS	= DEFAULT_NUM_COLUMNS;

	public static final		int	MIN_HTML_CELL_OFFSET		= -9;
	public static final		int	MAX_HTML_CELL_OFFSET		= 9;
	public static final		int	DEFAULT_HTML_CELL_OFFSET	= 0;

	public static final		Separator	DEFAULT_SEPARATOR	= Separator.BLOCK;

	public static final		Symmetry	DEFAULT_SYMMETRY	= Symmetry.ROTATION_HALF;

	public static final		Color	DEFAULT_HTML_GRID_COLOUR	= new Color(160, 160, 160);
	public static final		Color	DEFAULT_HTML_ENTRY_COLOUR	= new Color(96, 96, 96);

	protected static final	String	PIXEL_SIZE_STR	= "%1px";

	protected static final	List<CssRuleSet>	RULE_SETS	= Arrays.asList
	(
		new CssRuleSet
		(
			HtmlConstants.ElementName.DIV + CssConstants.Selector.ID + HtmlConstants.Id.GRID,
			new CssRuleSet.Decl(CssConstants.Property.DISPLAY,         "table"),
			new CssRuleSet.Decl(CssConstants.Property.BORDER_COLLAPSE, "collapse"),
			new CssRuleSet.Decl(CssConstants.Property.EMPTY_CELLS,     "show"),
			new CssRuleSet.Decl(CssConstants.Property.MARGIN,          "1.0em 0")
		),
		new CssRuleSet
		(
			HtmlConstants.ElementName.DIV + CssConstants.Selector.ID + HtmlConstants.Id.GRID
														+ CssConstants.Selector.CHILD + HtmlConstants.ElementName.DIV,
			new CssRuleSet.Decl(CssConstants.Property.DISPLAY, "table-row")
		)
	);

	private static final	int	INDENT_INCREMENT	= 2;

	private static final	int	MIN_NUM_LINES_PER_DIMENSION	= 3;

	private static final	int	SOLUTION_LINE_LENGTH	= 72;

	private static final	String	SOLUTION_ENCODING_NAME	= "UTF-8";

	private static final	String	RECTANGULAR_ORTHOGONAL_STR	= "rectangular-orthogonal";
	private static final	String	SOLUTION_STR				= "Solution";

	private final static	Symmetry[]	TEST_SYMMETRIES	=
	{
		Symmetry.ROTATION_QUARTER,
		Symmetry.REFLECTION_VERTICAL_HORIZONTAL_AXES,
		Symmetry.ROTATION_HALF,
		Symmetry.REFLECTION_VERTICAL_AXIS,
		Symmetry.REFLECTION_HORIZONTAL_AXIS
	};

	private interface ElementName
	{
		String	ENTRIES		= AppConstants.NS_PREFIX + "entries";
		String	ENTRY		= AppConstants.NS_PREFIX + "entry";
		String	GRID		= AppConstants.NS_PREFIX + "grid";
		String	SOLUTION	= AppConstants.NS_PREFIX + "solution";
	}

	private interface AttrName
	{
		String	ENCRYPTION	= AppConstants.NS_PREFIX + "encryption";
		String	HASH		= AppConstants.NS_PREFIX + "hash";
		String	ID			= AppConstants.NS_PREFIX + "id";
		String	KIND		= AppConstants.NS_PREFIX + "kind";
		String	NONCE		= AppConstants.NS_PREFIX + "nonce";
		String	NUM_COLUMNS	= AppConstants.NS_PREFIX + "numColumns";
		String	NUM_ROWS	= AppConstants.NS_PREFIX + "numRows";
		String	LOCATION	= AppConstants.NS_PREFIX + "location";
		String	SEPARATOR	= AppConstants.NS_PREFIX + "separator";
		String	SYMMETRY	= AppConstants.NS_PREFIX + "symmetry";
	}

////////////////////////////////////////////////////////////////////////
//  Enumerated types
////////////////////////////////////////////////////////////////////////


	// GRID SEPARATOR


	enum Separator
		implements IStringKeyed
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		BLOCK
		(
			"block"
		)
		{
			@Override
			public Grid createGrid(int      numColumns,
								   int      numRows,
								   Symmetry symmetry)
			{
				return new BlockGrid(numColumns, numRows, symmetry);
			}

			//----------------------------------------------------------

			@Override
			public Grid createGrid(int      numColumns,
								   int      numRows,
								   Symmetry symmetry,
								   String   definition)
				throws AppException
			{
				return new BlockGrid(numColumns, numRows, symmetry, definition);
			}

			//----------------------------------------------------------

			@Override
			public GridPanel createGridPanel(CrosswordDocument document)
			{
				return new GridPanel.Block(document);
			}

			//----------------------------------------------------------

			@Override
			public GridPanel createGridPanel(Grid grid)
			{
				return ((grid instanceof BlockGrid) ? new GridPanel.Block(grid.createCopy()) : null);
			}

			//----------------------------------------------------------
		},

		BAR
		(
			"bar"
		)
		{
			@Override
			public Grid createGrid(int      numColumns,
								   int      numRows,
								   Symmetry symmetry)
			{
				return new BarGrid(numColumns, numRows, symmetry);
			}

			//----------------------------------------------------------

			@Override
			public Grid createGrid(int      numColumns,
								   int      numRows,
								   Symmetry symmetry,
								   String   definition)
				throws AppException
			{
				return new BarGrid(numColumns, numRows, symmetry, definition);
			}

			//----------------------------------------------------------

			@Override
			public GridPanel createGridPanel(CrosswordDocument document)
			{
				return new GridPanel.Bar(document);
			}

			//----------------------------------------------------------

			@Override
			public GridPanel createGridPanel(Grid grid)
			{
				return ((grid instanceof BarGrid) ? new GridPanel.Bar(grid.createCopy()) : null);
			}

			//----------------------------------------------------------
		};

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private Separator(String key)
		{
			this.key = key;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Class methods
	////////////////////////////////////////////////////////////////////

		public static Separator forKey(String key)
		{
			for (Separator value : values())
			{
				if (value.key.equals(key))
					return value;
			}
			return null;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Abstract methods
	////////////////////////////////////////////////////////////////////

		public abstract Grid createGrid(int      numColumns,
										int      numRows,
										Symmetry symmetry);

		//--------------------------------------------------------------

		public abstract Grid createGrid(int      numColumns,
										int      numRows,
										Symmetry symmetry,
										String   definition)
			throws AppException;

		//--------------------------------------------------------------

		public abstract GridPanel createGridPanel(CrosswordDocument document);

		//--------------------------------------------------------------

		public abstract GridPanel createGridPanel(Grid grid);

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : IStringKeyed interface
	////////////////////////////////////////////////////////////////////

		public String getKey()
		{
			return key;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		public String toString()
		{
			return StringUtils.firstCharToUpperCase(key);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance fields
	////////////////////////////////////////////////////////////////////

		private	String	key;

	}

	//==================================================================


	// SYMMETRY


	enum Symmetry
		implements IStringKeyed
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		NONE
		(
			"none",
			"None"
		)
		{
			@Override
			public int[] getPrincipalDimensions(int numColumns,
												int numRows)
			{
				return new int[]{ numColumns, numRows };
			}
		},

		ROTATION_HALF
		(
			"rotate2",
			"Rotation by a half-turn"
		)
		{
			@Override
			public int[] getPrincipalDimensions(int numColumns,
												int numRows)
			{
				return new int[]{ numColumns, (numRows + 1) / 2 };
			}
		},

		ROTATION_QUARTER
		(
			"rotate4",
			"Rotation by a quarter-turn"
		)
		{
			@Override
			public int[] getPrincipalDimensions(int numColumns,
												int numRows)
			{
				return new int[]{ (numColumns + 1) / 2, (numRows + 1) / 2 };
			}

			//----------------------------------------------------------

			@Override
			public boolean supportsDimensions(int numColumns,
											  int numRows)
			{
				return (numColumns == numRows);
			}

			//----------------------------------------------------------
		},

		REFLECTION_VERTICAL_AXIS
		(
			"reflectVAxis",
			"Reflection in vertical axis"
		)
		{
			@Override
			public int[] getPrincipalDimensions(int numColumns,
												int numRows)
			{
				return new int[]{ (numColumns + 1) / 2, numRows };
			}
		},

		REFLECTION_HORIZONTAL_AXIS
		(
			"reflectHAxis",
			"Reflection in horizontal axis"
		)
		{
			@Override
			public int[] getPrincipalDimensions(int numColumns,
												int numRows)
			{
				return new int[]{ numColumns, (numRows + 1) / 2 };
			}
		},

		REFLECTION_VERTICAL_HORIZONTAL_AXES
		(
			"reflectVHAxes",
			"Reflection in V and H axes"
		)
		{
			@Override
			public int[] getPrincipalDimensions(int numColumns,
												int numRows)
			{
				return new int[]{ (numColumns + 1) / 2, (numRows + 1) / 2 };
			}
		};

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private Symmetry(String key,
						 String text)
		{
			this.key = key;
			this.text = text;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Class methods
	////////////////////////////////////////////////////////////////////

		public static Symmetry forKey(String key)
		{
			for (Symmetry value : values())
			{
				if (value.key.equals(key))
					return value;
			}
			return null;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Abstract methods
	////////////////////////////////////////////////////////////////////

		public abstract int[] getPrincipalDimensions(int numColumns,
													 int numRows);

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : IStringKeyed interface
	////////////////////////////////////////////////////////////////////

		public String getKey()
		{
			return key;
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

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		public boolean supportsDimensions(int numColumns,
										  int numRows)
		{
			return true;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance fields
	////////////////////////////////////////////////////////////////////

		private	String	key;
		private	String	text;

	}

	//==================================================================


	// ENCRYPTION KIND


	enum EncryptionKind
		implements IStringKeyed
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		NONE    ("none"),
		SALSA20 ("salsa20");

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private EncryptionKind(String key)
		{
			this.key = key;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Class methods
	////////////////////////////////////////////////////////////////////

		public static EncryptionKind forKey(String key)
		{
			for (EncryptionKind value : values())
			{
				if (value.key.equals(key))
					return value;
			}
			return null;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : IStringKeyed interface
	////////////////////////////////////////////////////////////////////

		public String getKey()
		{
			return key;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		public String toString()
		{
			return StringUtils.firstCharToUpperCase(key);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance fields
	////////////////////////////////////////////////////////////////////

		private	String	key;

	}

	//==================================================================


	// ERROR IDENTIFIERS


	private enum ErrorId
		implements AppException.IId
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		NO_ATTRIBUTE
		("The required attribute is missing."),

		INVALID_ATTRIBUTE
		("The attribute is invalid."),

		ATTRIBUTE_OUT_OF_BOUNDS
		("The attribute value is out of bounds."),

		INCOMPATIBLE_SYMMETRY_AND_DIMENSIONS
		("The symmetry of the grid is not compatible with the dimensions of the grid."),

		CLUES_NOT_DEFINED
		("%1 clues are not defined."),

		ERRORS_IN_CLUE_LISTS
		("The lists of clues have the following errors:\n%1"),

		TOO_FEW_LINES
		("There are fewer than " + MIN_NUM_LINES_PER_DIMENSION + " %1 lines of sufficient length in the image."),

		TOO_FEW_COINCIDENT_HORIZONTAL_AND_VERTICAL_LINES
		("The largest coincident sets of horizontal and vertical lines are too small to form a grid."),

		INVALID_FIELD_ID
		("The ID does not correspond to a field in the grid."),

		INCORRECT_NUMBER_OF_ENTRIES
		("The number of entries does not match the number of fields."),

		INCORRECT_ENTRY_LENGTH
		("The length of the entry for %1 incorrect."),

		ILLEGAL_CHARACTER_IN_ENTRY
		("The entry for %1 contains an illegal character: '%2'"),

		CONFLICTING_ENTRY
		("The entry for %1 conflicts with an intersecting entry at index %2."),

		ILLEGAL_CHARACTER_IN_SOLUTION_ENCODING
		("The Base64 encoding of the solution contains an illegal character."),

		MALFORMED_SOLUTION_ENCODING
		("The Base64 encoding of the solution is malformed."),

		INCORRECT_PASSPHRASE
		("The passphrase does not match the one that was used to encrypt the solution."),

		SOLUTION_LENGTH_NOT_CONSISTENT_WITH_GRID
		("The length of the solution is not consistent with the grid."),

		INCORRECT_NUMBER_OF_ANSWERS
		("The number of answers does not match the number of fields."),

		INCORRECT_ANSWER_LENGTH
		("The length of the answer for %1 is incorrect."),

		ILLEGAL_CHARACTER_IN_ANSWER
		("The answer for %1 contains an illegal character: '%2'"),

		CONFLICTING_ANSWER
		("The answer for %1 conflicts with an intersecting answer at index %2."),

		UNSUPPORTED_ENCRYPTION
		("The kind of encryption is not supported by this application.");

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
//  Member classes : non-inner classes
////////////////////////////////////////////////////////////////////////


	// GRID INFORMATION CLASS


	public static class Info
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private Info(int x,
					 int y,
					 int width,
					 int height,
					 int numColumns,
					 int numRows)
		{
			this.x = x;
			this.y = y;
			this.width = width;
			this.height = height;
			this.numColumns = numColumns;
			this.numRows = numRows;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		public Rectangle getBounds()
		{
			return new Rectangle(x, y, width, height);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance fields
	////////////////////////////////////////////////////////////////////

		int	x;
		int	y;
		int	width;
		int	height;
		int	numColumns;
		int	numRows;

	}

	//==================================================================


	// CELL INDEX PAIR CLASS


	public static class IndexPair
		implements Cloneable
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		public IndexPair(int row,
						 int column)
		{
			this.row = row;
			this.column = column;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		public boolean equals(Object obj)
		{
			if (obj instanceof IndexPair)
			{
				IndexPair other = (IndexPair)obj;
				return ((row == other.row) && (column == other.column));
			}
			return false;
		}

		//--------------------------------------------------------------

		@Override
		public int hashCode()
		{
			return ((row << 16) | column);
		}

		//--------------------------------------------------------------

		@Override
		public IndexPair clone()
		{
			try
			{
				return (IndexPair)super.clone();
			}
			catch (CloneNotSupportedException e)
			{
				throw new UnexpectedRuntimeException(e);
			}
		}

		//--------------------------------------------------------------

		@Override
		public String toString()
		{
			return (row + ", " + column);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		public void set(int row,
						int column)
		{
			this.row = row;
			this.column = column;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance fields
	////////////////////////////////////////////////////////////////////

		int	row;
		int	column;

	}

	//==================================================================


	// GRID FIELD CLASS


	public static class Field
		implements Cloneable, Comparable<Field>
	{

	////////////////////////////////////////////////////////////////////
	//  Member interfaces
	////////////////////////////////////////////////////////////////////


		// FILTER INTERFACE


		@FunctionalInterface
		interface IFilter
		{

		////////////////////////////////////////////////////////////////
		//  Methods
		////////////////////////////////////////////////////////////////

			public boolean acceptField(Field field);

			//----------------------------------------------------------

		}

		//==============================================================

	////////////////////////////////////////////////////////////////////
	//  Member classes : non-inner classes
	////////////////////////////////////////////////////////////////////


		// FIELD IDENTIFIER CLASS


		public static class Id
			implements Cloneable, Comparable<Id>
		{

		////////////////////////////////////////////////////////////////
		//  Constants
		////////////////////////////////////////////////////////////////

			public static final		Pattern	PATTERN;

			private static final	String	REGEX_FRAG1	= "(\\d+)(";
			private static final	String	REGEX_FRAG2	= ")?";

		////////////////////////////////////////////////////////////////
		//  Constructors
		////////////////////////////////////////////////////////////////

			public Id(int number)
			{
				this(number, Direction.NONE);
			}

			//----------------------------------------------------------

			public Id(int       number,
					  Direction direction)
			{
				this.number = number;
				this.direction = direction;
			}

			//----------------------------------------------------------

			/**
			 * @throws IllegalArgumentException
			 */

			public Id(String str)
			{
				Matcher matcher = PATTERN.matcher(str);
				if (!matcher.matches())
					throw new IllegalArgumentException();

				number = Integer.parseInt(matcher.group(1));
				direction = Direction.forSuffix(matcher.group(2));
			}

			//----------------------------------------------------------

			/**
			 * @throws NumberFormatException
			 */

			public Id(String numberStr,
					  String directionStr)
			{
				this(Integer.parseInt(numberStr));
				for (Direction direction : Direction.DEFINED_DIRECTIONS)
				{
					if (directionStr == null)
						break;
					for (String keyword : AppConfig.INSTANCE.getClueDirectionKeywords(direction))
					{
						keyword = StringUtils.stripBefore(keyword);
						if (keyword.equals(directionStr))
						{
							this.direction = direction;
							directionStr = null;
							break;
						}
					}
				}
			}

			//----------------------------------------------------------

		////////////////////////////////////////////////////////////////
		//  Instance methods : Comparable interface
		////////////////////////////////////////////////////////////////

			@Override
			public int compareTo(Id other)
			{
				int result = Integer.compare(number, other.number);
				if (result == 0)
					result = Integer.compare(direction.ordinal(), other.direction.ordinal());
				return result;
			}

			//----------------------------------------------------------

		////////////////////////////////////////////////////////////////
		//  Instance methods : overriding methods
		////////////////////////////////////////////////////////////////

			@Override
			public boolean equals(Object obj)
			{
				if (obj instanceof Id)
				{
					Id other = (Id)obj;
					return ((number == other.number) && (direction == other.direction));
				}
				return false;
			}

			//----------------------------------------------------------

			@Override
			public int hashCode()
			{
				return ((number << 2) | direction.ordinal());
			}

			//----------------------------------------------------------

			@Override
			public Id clone()
			{
				try
				{
					return (Id)super.clone();
				}
				catch (CloneNotSupportedException e)
				{
					throw new UnexpectedRuntimeException(e);
				}
			}

			//----------------------------------------------------------

			@Override
			public String toString()
			{
				return (Integer.toString(number) + direction.getSuffix());
			}

			//----------------------------------------------------------

		////////////////////////////////////////////////////////////////
		//  Instance methods
		////////////////////////////////////////////////////////////////

			public boolean matches(Id other)
			{
				return ((number == other.number) &&
						 ((direction == Direction.NONE) || (other.direction == Direction.NONE) ||
						  (direction == other.direction)));
			}

			//----------------------------------------------------------

			public Id undefined()
			{
				return new Id(number, Direction.NONE);
			}

			//----------------------------------------------------------

		////////////////////////////////////////////////////////////////
		//  Static initialiser
		////////////////////////////////////////////////////////////////

			static
			{
				List<String> suffixes = new ArrayList<>();
				for (Direction direction : Direction.values())
					suffixes.add(direction.getSuffix());

				StringBuilder buffer = new StringBuilder();
				buffer.append(REGEX_FRAG1);
				buffer.append(StringUtils.join(Clue.REGEX_ALTERNATION_CHAR, suffixes));
				buffer.append(REGEX_FRAG2);
				PATTERN = Pattern.compile(buffer.toString());
			}

		////////////////////////////////////////////////////////////////
		//  Instance fields
		////////////////////////////////////////////////////////////////

			int			number;
			Direction	direction;

		}

		//==============================================================

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		protected Field(int       row,
						int       column,
						Direction direction,
						int       length,
						int       number)
		{
			this.row = row;
			this.column = column;
			this.direction = direction;
			this.length = length;
			this.number = number;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : Comparable interface
	////////////////////////////////////////////////////////////////////

		@Override
		public int compareTo(Field other)
		{
			int result = Integer.compare(direction.ordinal(), other.direction.ordinal());
			if (result == 0)
				result = Integer.compare(row, other.row);
			if (result == 0)
				result = Integer.compare(column, other.column);
			return result;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		public boolean equals(Object obj)
		{
			if (obj instanceof Field)
			{
				Field other = (Field)obj;
				return ((row == other.row) && (column == other.column) && (direction == other.direction));
			}
			return false;
		}

		//--------------------------------------------------------------

		@Override
		public int hashCode()
		{
			return ((row << 12) | (column << 2) | direction.ordinal());
		}

		//--------------------------------------------------------------

		@Override
		public Field clone()
		{
			try
			{
				return (Field)super.clone();
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

		public int getRow()
		{
			return row;
		}

		//--------------------------------------------------------------

		public int getColumn()
		{
			return column;
		}

		//--------------------------------------------------------------

		public Direction getDirection()
		{
			return direction;
		}

		//--------------------------------------------------------------

		public int getLength()
		{
			return length;
		}

		//--------------------------------------------------------------

		public int getNumber()
		{
			return number;
		}

		//--------------------------------------------------------------

		public Id getId()
		{
			return new Id(number, direction);
		}

		//--------------------------------------------------------------

		public int getEndRow()
		{
			switch (direction)
			{
				case NONE:
					// do nothing
					break;

				case ACROSS:
					return row;

				case DOWN:
					return (row + length - 1);
			}
			return -1;
		}

		//--------------------------------------------------------------

		public int getEndColumn()
		{
			switch (direction)
			{
				case NONE:
					// do nothing
					break;

				case ACROSS:
					return (column + length - 1);

				case DOWN:
					return column;
			}
			return -1;
		}

		//--------------------------------------------------------------

		public IndexPair getStartIndices()
		{
			return new IndexPair(row, column);
		}

		//--------------------------------------------------------------

		public IndexPair getEndIndices()
		{
			switch (direction)
			{
				case NONE:
					// do nothing
					break;

				case ACROSS:
					return new IndexPair(row, column + length - 1);

				case DOWN:
					return new IndexPair(row  + length - 1, column);
			}
			return null;
		}

		//--------------------------------------------------------------

		public boolean containsCell(int row,
									int column)
		{
			switch (direction)
			{
				case NONE:
					// do nothing
					break;

				case ACROSS:
					return ((row == this.row) &&
							 (column >= this.column) && (column < this.column + length));

				case DOWN:
					return ((column == this.column) &&
							 (row >= this.row) && (row < this.row + length));
			}
			return false;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance fields
	////////////////////////////////////////////////////////////////////

		private	int			row;
		private	int			column;
		private	Direction	direction;
		private	int			length;
		private	int			number;

	}

	//==================================================================


	// GRID ENTRY VALUE CLASS


	public static class EntryValue
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		public EntryValue(int  row,
						  int  column,
						  char value)
		{
			this.row = row;
			this.column = column;
			this.value = value;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance fields
	////////////////////////////////////////////////////////////////////

		int		row;
		int		column;
		char	value;

	}

	//==================================================================


	// GRID ENTRY CLASS


	public static class Entry
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private Entry(Field.Id fieldId,
					  String   text)
		{
			this.fieldId = fieldId.clone();
			this.text = text;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance fields
	////////////////////////////////////////////////////////////////////

		Field.Id	fieldId;
		String		text;

	}

	//==================================================================


	// GRID ENTRIES CLASS


	public static class Entries
		implements Cloneable
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		public static final		char	UNDEFINED_VALUE	= '?';
		private static final	char	NO_VALUE		= '\0';

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private Entries(int numColumns,
						int numRows)
		{
			values = new char[numRows][];
			for (int i = 0; i < values.length; i++)
				values[i] = new char[numColumns];
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		public Entries clone()
		{
			try
			{
				Entries copy = (Entries)super.clone();
				copy.values = values.clone();
				for (int i = 0; i < values.length; i++)
					copy.values[i] = values[i].clone();
				return copy;
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

		public void clear()
		{
			for (int row = 0; row < values.length; row++)
			{
				for (int column = 0; column < values[row].length; column++)
				{
					if (values[row][column] != NO_VALUE)
						values[row][column] = UNDEFINED_VALUE;
				}
			}
			numValues = 0;
		}

		//--------------------------------------------------------------

		protected void init()
		{
			for (int i = 0; i < values.length; i++)
				Arrays.fill(values[i], NO_VALUE);
			numCells = 0;
			numValues = 0;
		}

		//--------------------------------------------------------------

		protected void initValue(int row,
								 int column)
		{
			if (values[row][column] == NO_VALUE)
				++numCells;
			values[row][column] = UNDEFINED_VALUE;
		}

		//--------------------------------------------------------------

		private void setValue(int  row,
							  int  column,
							  char value)
		{
			if (values[row][column] != UNDEFINED_VALUE)
				--numValues;
			values[row][column] = value;
			if (value != UNDEFINED_VALUE)
				++numValues;
		}

		//--------------------------------------------------------------

		private boolean[][] compare(Entries other)
		{
			boolean[][] differences = new boolean[values.length][];
			for (int row = 0; row < values.length; row++)
			{
				differences[row] = new boolean[values[row].length];
				for (int column = 0; column < values[row].length; column++)
				{
					if (values[row][column] != NO_VALUE)
						differences[row][column] = (values[row][column] != other.values[row][column]);
				}
			}
			return differences;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance fields
	////////////////////////////////////////////////////////////////////

		private	int			numCells;
		private	int			numValues;
		private	char[][]	values;

	}

	//==================================================================


	// ENCODED SOLUTION CLASS


	public static class EncodedSolution
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private EncodedSolution(byte[] nonce,
								byte[] hashValue,
								byte[] data)
		{
			this.nonce = nonce;
			this.hashValue = hashValue;
			this.data = data;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		public byte[] getNonce()
		{
			return nonce;
		}

		//--------------------------------------------------------------

		public byte[] getHashValue()
		{
			return hashValue;
		}

		//--------------------------------------------------------------

		public byte[] getData()
		{
			return data;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance fields
	////////////////////////////////////////////////////////////////////

		private	byte[]	nonce;
		private	byte[]	hashValue;
		private	byte[]	data;

	}

	//==================================================================


	// CELL BASE CLASS


	protected static abstract class Cell
		implements Cloneable
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		private static final	double	ENTRY_TOP_OFFSET_FRACTION	= 0.1;
		private static final	int		ENTRY_LEFT_OFFSET			= 1;

		private static final	String	STYLE_SELECTOR1	= HtmlConstants.ElementName.DIV + CssConstants.Selector.ID
															+ HtmlConstants.Id.GRID + CssConstants.Selector.CHILD
															+ HtmlConstants.ElementName.DIV
															+ CssConstants.Selector.CHILD
															+ HtmlConstants.ElementName.DIV;
		private static final	String	STYLE_SELECTOR2	= HtmlConstants.ElementName.DIV + CssConstants.Selector.CLASS
															+ HtmlConstants.Class.FIELD_NUMBER;
		private static final	String	STYLE_SELECTOR3	= HtmlConstants.ElementName.DIV + CssConstants.Selector.CLASS
															+ HtmlConstants.Class.ENTRY;

		private static final	CssRuleSet	CONTAINER_RULE_SET	= new CssRuleSet
		(
			STYLE_SELECTOR1,
			new CssRuleSet.Decl(CssConstants.Property.DISPLAY,        "table-cell"),
			new CssRuleSet.Decl(CssConstants.Property.POSITION,       "relative"),
			new CssRuleSet.Decl(CssConstants.Property.VERTICAL_ALIGN, "top"),
			new CssRuleSet.Decl(CssConstants.Property.WIDTH,          ""),
			new CssRuleSet.Decl(CssConstants.Property.HEIGHT,         ""),
			new CssRuleSet.Decl(CssConstants.Property.BORDER,         "1px solid %1")
		);
		private static final	CssRuleSet	FIELD_NUMBER_RULE_SET	= new CssRuleSet
		(
			STYLE_SELECTOR2,
			new CssRuleSet.Decl(CssConstants.Property.POSITION,    "absolute"),
			new CssRuleSet.Decl(CssConstants.Property.Z_INDEX,     "2"),
			new CssRuleSet.Decl(CssConstants.Property.WIDTH,       "100%"),
			new CssRuleSet.Decl(CssConstants.Property.HEIGHT,      "100%"),
			new CssRuleSet.Decl(CssConstants.Property.TOP,         ""),
			new CssRuleSet.Decl(CssConstants.Property.LEFT,        ""),
			new CssRuleSet.Decl(CssConstants.Property.TEXT_ALIGN,  "left"),
			new CssRuleSet.Decl(CssConstants.Property.LINE_HEIGHT, "100%"),
			new CssRuleSet.Decl(CssConstants.Property.FONT_SIZE,   "%1%%")
		);
		private static final	CssRuleSet	ENTRY_RULE_SET	= new CssRuleSet
		(
			STYLE_SELECTOR3,
			new CssRuleSet.Decl(CssConstants.Property.POSITION,    "absolute"),
			new CssRuleSet.Decl(CssConstants.Property.Z_INDEX,     "3"),
			new CssRuleSet.Decl(CssConstants.Property.WIDTH,       "100%"),
			new CssRuleSet.Decl(CssConstants.Property.HEIGHT,      "100%"),
			new CssRuleSet.Decl(CssConstants.Property.TOP,         "%1%%"),
			new CssRuleSet.Decl(CssConstants.Property.LEFT,        ""),
			new CssRuleSet.Decl(CssConstants.Property.TEXT_ALIGN,  "center"),
			new CssRuleSet.Decl(CssConstants.Property.LINE_HEIGHT, "125%"),
			new CssRuleSet.Decl(CssConstants.Property.FONT_SIZE,   "125%"),
			new CssRuleSet.Decl(CssConstants.Property.COLOUR,      "%1")
		);

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		protected Cell()
		{
			fields = new EnumMap<>(Direction.class);
			fieldOrigins = new EnumMap<>(Direction.class);
			for (Direction direction : Direction.DEFINED_DIRECTIONS)
				fieldOrigins.put(direction, Boolean.FALSE);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Class methods
	////////////////////////////////////////////////////////////////////

		public static List<CssRuleSet> getStyleRuleSets(int    cellSize,
														int    cellOffset,
														Color  gridColour,
														Color  entryColour,
														int    fieldNumOffset,
														double fieldNumFontSizeFactor)
		{
			List<CssRuleSet> ruleSets = new ArrayList<>();

			// Add rule set for cell container
			CssRuleSet ruleSet = CONTAINER_RULE_SET.clone();

			String cellSizeStr = Integer.toString(cellSize);
			CssRuleSet.Decl decl = ruleSet.findDeclaration(CssConstants.Property.WIDTH);
			decl.value = StringUtils.substitute(PIXEL_SIZE_STR, cellSizeStr);
			decl = ruleSet.findDeclaration(CssConstants.Property.HEIGHT);
			decl.value = StringUtils.substitute(PIXEL_SIZE_STR, cellSizeStr);

			String colourStr = ColourUtils.colourToHexString(gridColour);
			decl = ruleSet.findDeclaration(CssConstants.Property.BORDER);
			decl.value = StringUtils.substitute(decl.value, colourStr);

			ruleSets.add(ruleSet);

			// Add rule set for field number
			ruleSet = FIELD_NUMBER_RULE_SET.clone();

			String offsetStr = Integer.toString(cellOffset + fieldNumOffset);
			decl = ruleSet.findDeclaration(CssConstants.Property.TOP);
			decl.value = StringUtils.substitute(PIXEL_SIZE_STR, offsetStr);
			decl = ruleSet.findDeclaration(CssConstants.Property.LEFT);
			decl.value = StringUtils.substitute(PIXEL_SIZE_STR, offsetStr);

			String fontSizeStr = AppConstants.FORMAT_1_1.format(fieldNumFontSizeFactor * 100.0);
			decl = ruleSet.findDeclaration(CssConstants.Property.FONT_SIZE);
			decl.value = StringUtils.substitute(decl.value, fontSizeStr);

			ruleSets.add(ruleSet);

			// Add rule set for entry
			ruleSet = ENTRY_RULE_SET.clone();

			decl = ruleSet.findDeclaration(CssConstants.Property.TOP);
			double offset = (double)cellOffset / (double)cellSize + ENTRY_TOP_OFFSET_FRACTION;
			decl.value = StringUtils.substitute(decl.value, AppConstants.FORMAT_1_3.format(offset * 100.0));
			decl = ruleSet.findDeclaration(CssConstants.Property.LEFT);
			decl.value = StringUtils.substitute(PIXEL_SIZE_STR, Integer.toString(cellOffset + ENTRY_LEFT_OFFSET));

			colourStr = ColourUtils.colourToHexString(entryColour);
			decl = ruleSet.findDeclaration(CssConstants.Property.COLOUR);
			decl.value = StringUtils.substitute(decl.value, colourStr);

			ruleSets.add(ruleSet);

			return ruleSets;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Abstract methods
	////////////////////////////////////////////////////////////////////

		protected abstract void write(XmlWriter writer,
									  int       indent,
									  int       cellSize,
									  int       fieldNumber,
									  char      entry)
			throws IOException;

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		public Cell clone()
		{
			try
			{
				Cell copy = (Cell)super.clone();

				copy.fields = new EnumMap<>(Direction.class);
				for (Direction direction : fields.keySet())
					copy.fields.put(direction, fields.get(direction).clone());

				copy.fieldOrigins = new EnumMap<>(Direction.class);
				for (Direction direction : fieldOrigins.keySet())
					copy.fieldOrigins.put(direction, fieldOrigins.get(direction).booleanValue());

				return copy;
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

		public Field getField(Direction direction)
		{
			return fields.get(direction);
		}

		//--------------------------------------------------------------

		public boolean isInField()
		{
			return !fields.isEmpty();
		}

		//--------------------------------------------------------------

		public boolean isFieldOrigin()
		{
			for (Direction direction : fieldOrigins.keySet())
			{
				if (fieldOrigins.get(direction))
					return true;
			}
			return false;
		}

		//--------------------------------------------------------------

		public int getFieldNumber()
		{
			for (Direction direction : fieldOrigins.keySet())
			{
				if (fieldOrigins.get(direction))
					return fields.get(direction).number;
			}
			return 0;
		}

		//--------------------------------------------------------------

		protected List<Field> getFields()
		{
			List<Field> fields = new ArrayList<>();
			for (Direction direction : this.fields.keySet())
				fields.add(this.fields.get(direction));
			return fields;
		}

		//--------------------------------------------------------------

		protected void setField(Direction direction,
								Field     field)
		{
			fields.put(direction, field);
		}

		//--------------------------------------------------------------

		protected void setFieldOrigin(Direction direction,
									  Field     field)
		{
			fields.put(direction, field);
			fieldOrigins.put(direction, Boolean.TRUE);
		}

		//--------------------------------------------------------------

		protected void resetFields()
		{
			fields.clear();
			fieldOrigins.clear();
			for (Direction direction : Direction.DEFINED_DIRECTIONS)
				fieldOrigins.put(direction, Boolean.FALSE);
		}

		//--------------------------------------------------------------

		protected void writeContents(XmlWriter writer,
									 int       fieldNumber,
									 char      entry)
			throws IOException
		{
			List<Attribute> attributes = new ArrayList<>();
			if (fieldNumber > 0)
			{
				attributes.add(new Attribute(HtmlConstants.AttrName.CLASS, HtmlConstants.Class.FIELD_NUMBER));
				writer.writeElementStart(HtmlConstants.ElementName.DIV, attributes, 0, false, false);
				writer.write(Integer.toString(fieldNumber));
				writer.writeEndTag(HtmlConstants.ElementName.DIV);
			}

			if (entry != Entries.UNDEFINED_VALUE)
			{
				attributes.clear();
				attributes.add(new Attribute(HtmlConstants.AttrName.CLASS, HtmlConstants.Class.ENTRY));
				writer.writeElementStart(HtmlConstants.ElementName.DIV, attributes, 0, false, false);
				writer.write(entry);
				writer.writeEndTag(HtmlConstants.ElementName.DIV);
			}
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance fields
	////////////////////////////////////////////////////////////////////

		private	Map<Direction, Field>	fields;
		private	Map<Direction, Boolean>	fieldOrigins;

	}

	//==================================================================


	// LINE CLASS


	private static class Line
		implements Cloneable
	{

	////////////////////////////////////////////////////////////////////
	//  Enumerated types
	////////////////////////////////////////////////////////////////////


		// LINE ORIENTATION


		private enum Orientation
		{

		////////////////////////////////////////////////////////////////
		//  Constants
		////////////////////////////////////////////////////////////////

			HORIZONTAL  ("horizontal"),
			VERTICAL    ("vertical");

		////////////////////////////////////////////////////////////////
		//  Constructors
		////////////////////////////////////////////////////////////////

			private Orientation(String text)
			{
				this.text = text;
			}

			//----------------------------------------------------------

		////////////////////////////////////////////////////////////////
		//  Instance methods : overriding methods
		////////////////////////////////////////////////////////////////

			@Override
			public String toString()
			{
				return text;
			}

			//----------------------------------------------------------

		////////////////////////////////////////////////////////////////
		//  Instance fields
		////////////////////////////////////////////////////////////////

			private	String	text;

		}

		//==============================================================

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private Line(Orientation orientation,
					 int         x,
					 int         y)
		{
			this.orientation = orientation;
			x1 = x2 = x;
			y1 = y2 = y;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		public Line clone()
		{
			try
			{
				return (Line)super.clone();
			}
			catch (CloneNotSupportedException e)
			{
				throw new UnexpectedRuntimeException(e);
			}
		}

		//--------------------------------------------------------------

		@Override
		public String toString()
		{
			return (orientation.name().charAt(0) + ": " + x1 + ", " + y1 + ", " + getLength());
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		private int getLength()
		{
			return (((orientation == Orientation.HORIZONTAL) ? x2 - x1 : y2 - y1) + 1);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance fields
	////////////////////////////////////////////////////////////////////

		private	Orientation	orientation;
		private	int			x1;
		private	int			y1;
		private	int			x2;
		private	int			y2;

	}

	//==================================================================


	// BOUNDS CLASS


	private static class Bounds
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private Bounds(int x1,
					   int y1,
					   int x2,
					   int y2)
		{
			this.x1 = x1;
			this.y1 = y1;
			this.x2 = x2;
			this.y2 = y2;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance fields
	////////////////////////////////////////////////////////////////////

		private	int	x1;
		private	int	y1;
		private	int	x2;
		private	int	y2;

	}

	//==================================================================


	// PSEUDO-RANDOM NUMBER GENERATOR CLASS


	private static class Prng
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		private static final	int	NUM_ROUNDS	= 20;
		private static final	int	BUFFER_SIZE	= Salsa20.BLOCK_SIZE;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private Prng(String passphrase,
					 byte[] nonce)
		{
			prng = new Salsa20(NUM_ROUNDS, Salsa20.stringToKey(passphrase), nonce);
			buffer = new byte[BUFFER_SIZE];
			indexMask = BUFFER_SIZE - 1;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Class methods
	////////////////////////////////////////////////////////////////////

		private static byte[] createNonce()
		{
			byte[] nonce = new byte[Salsa20.NONCE_SIZE];
			NumberUtils.longToBytesLE(nonceGenerator.nextLong(), nonce);
			return nonce;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		private byte[] getKey()
		{
			return prng.getKey();
		}

		//--------------------------------------------------------------

		private void combine(byte[] data)
		{
			for (int i = 0; i < data.length; i++)
			{
				if (index == 0)
					prng.getNextBlock(buffer, 0);
				data[i] ^= buffer[index++];
				index &= indexMask;
			}
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Class fields
	////////////////////////////////////////////////////////////////////

		private static	Random	nonceGenerator	= new Random();

	////////////////////////////////////////////////////////////////////
	//  Instance fields
	////////////////////////////////////////////////////////////////////

		private	Salsa20	prng;
		private	byte[]	buffer;
		private	int		index;
		private	int		indexMask;

	}

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	protected Grid(int numColumns,
				   int numRows)
	{
		this.numColumns = numColumns;
		this.numRows = numRows;
		symmetry = Symmetry.NONE;
		fieldLists = new EnumMap<>(Direction.class);
		entries = new Entries(numColumns, numRows);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	public static boolean isGridElement(Element element)
	{
		return element.getTagName().equals(ElementName.GRID);
	}

	//------------------------------------------------------------------

	public static boolean isEntriesElement(Element element)
	{
		return element.getTagName().equals(ElementName.ENTRIES);
	}

	//------------------------------------------------------------------

	public static boolean isSolutionElement(Element element)
	{
		return element.getTagName().equals(ElementName.SOLUTION);
	}

	//------------------------------------------------------------------

	public static Grid create(Element element)
		throws XmlParseException
	{
		// Get element path
		String elementPath = XmlUtils.getElementPath(element);

		// Attribute: kind
		String attrName = AttrName.KIND;
		String attrKey = XmlUtils.appendAttributeName(elementPath, attrName);
		String attrValue = XmlUtils.getAttribute(element, attrName);
		if (attrValue == null)
			throw new XmlParseException(ErrorId.NO_ATTRIBUTE, attrKey);
		if (!attrValue.equals(RECTANGULAR_ORTHOGONAL_STR))
			throw new XmlParseException(ErrorId.INVALID_ATTRIBUTE, attrKey, attrValue);

		// Attribute: separator
		attrName = AttrName.SEPARATOR;
		attrKey = XmlUtils.appendAttributeName(elementPath, attrName);
		attrValue = XmlUtils.getAttribute(element, attrName);
		if (attrValue == null)
			throw new XmlParseException(ErrorId.NO_ATTRIBUTE, attrKey);
		Separator separator = Separator.forKey(attrValue);
		if (separator == null)
			throw new XmlParseException(ErrorId.INVALID_ATTRIBUTE, attrKey, attrValue);

		// Attribute: number of columns
		attrName = AttrName.NUM_COLUMNS;
		attrKey = XmlUtils.appendAttributeName(elementPath, attrName);
		attrValue = XmlUtils.getAttribute(element, attrName);
		if (attrValue == null)
			throw new XmlParseException(ErrorId.NO_ATTRIBUTE, attrKey);
		int numColumns = 0;
		try
		{
			numColumns = Integer.parseInt(attrValue);
			if ((numColumns < MIN_NUM_COLUMNS) || (numColumns > MAX_NUM_COLUMNS))
				throw new XmlParseException(ErrorId.ATTRIBUTE_OUT_OF_BOUNDS, attrKey, attrValue);
		}
		catch (NumberFormatException e)
		{
			throw new XmlParseException(ErrorId.INVALID_ATTRIBUTE, attrKey, attrValue);
		}

		// Attribute: number of rows
		attrName = AttrName.NUM_ROWS;
		attrKey = XmlUtils.appendAttributeName(elementPath, attrName);
		attrValue = XmlUtils.getAttribute(element, attrName);
		if (attrValue == null)
			throw new XmlParseException(ErrorId.NO_ATTRIBUTE, attrKey);
		int numRows = 0;
		try
		{
			numRows = Integer.parseInt(attrValue);
			if ((numRows < MIN_NUM_ROWS) || (numRows > MAX_NUM_ROWS))
				throw new XmlParseException(ErrorId.ATTRIBUTE_OUT_OF_BOUNDS, attrKey, attrValue);
		}
		catch (NumberFormatException e)
		{
			throw new XmlParseException(ErrorId.INVALID_ATTRIBUTE, attrKey, attrValue);
		}

		// Attribute: symmetry
		attrName = AttrName.SYMMETRY;
		attrKey = XmlUtils.appendAttributeName(elementPath, attrName);
		attrValue = XmlUtils.getAttribute(element, attrName);
		if (attrValue == null)
			throw new XmlParseException(ErrorId.NO_ATTRIBUTE, attrKey);
		Symmetry symmetry = Symmetry.forKey(attrValue);
		if (symmetry == null)
			throw new XmlParseException(ErrorId.INVALID_ATTRIBUTE, attrKey, attrValue);

		// Check that symmetry is consistent with grid dimensions
		if (!symmetry.supportsDimensions(numColumns, numRows))
			throw new XmlParseException(ErrorId.INCOMPATIBLE_SYMMETRY_AND_DIMENSIONS, attrKey, attrValue);

		// Get non-whitespace text content of element
		String text = element.getTextContent();
		StringBuilder buffer = new StringBuilder(text.length());
		for (int i = 0; i < text.length(); i++)
		{
			char ch = text.charAt(i);
			if (!Character.isWhitespace(ch))
				buffer.append(ch);
		}

		// Create grid
		Grid grid = null;
		try
		{
			grid = separator.createGrid(numColumns, numRows, symmetry, buffer.toString());
		}
		catch (AppException e)
		{
			throw new XmlParseException(e.getId(), elementPath, e.getSubstitutionStrings());
		}
		return grid;
	}

	//------------------------------------------------------------------

	public static Info findGrid(BufferedImage image,
								double        brightnessThreshold,
								int           minLineLength,
								int           minLineSeparation,
								int           endpointTolerance)
		throws AppException
	{
		// Find the set of horizontal lines that satisfy the brightness and length constraints
		List<Line> linesH = new ArrayList<>();
		for (int y = 0; y < image.getHeight(); y++)
		{
			Line line = null;
			for (int x = 0; x < image.getWidth(); x++)
			{
				if (ColourUtils.getBrightness(image.getRGB(x, y)) < brightnessThreshold)
				{
					if (line == null)
						line = new Line(Line.Orientation.HORIZONTAL, x, y);
					line.x2 = x;
				}
				else if (line != null)
				{
					if (line.getLength() >= minLineLength)
						linesH.add(line);
					line = null;
				}
			}
			if ((line != null) && (line.getLength() >= minLineLength))
				linesH.add(line);
		}
		if (linesH.size() < MIN_NUM_LINES_PER_DIMENSION)
			throw new AppException(ErrorId.TOO_FEW_LINES, Line.Orientation.HORIZONTAL.toString());

		// Find the set of vertical lines that satisfy the brightness and length constraints
		List<Line> linesV = new ArrayList<>();
		for (int x = 0; x < image.getWidth(); x++)
		{
			Line line = null;
			for (int y = 0; y < image.getHeight(); y++)
			{
				if (ColourUtils.getBrightness(image.getRGB(x, y)) < brightnessThreshold)
				{
					if (line == null)
						line = new Line(Line.Orientation.VERTICAL, x, y);
					line.y2 = y;
				}
				else if (line != null)
				{
					if (line.getLength() >= minLineLength)
						linesV.add(line);
					line = null;
				}
			}
			if ((line != null) && (line.getLength() >= minLineLength))
				linesV.add(line);
		}
		if (linesV.size() < MIN_NUM_LINES_PER_DIMENSION)
			throw new AppException(ErrorId.TOO_FEW_LINES, Line.Orientation.VERTICAL.toString());

		// Get the largest subset of horizontal and vertical lines within coincident bounding rectangles
		List<Line> maxCombinedLinesH = new ArrayList<>();
		List<Line> maxCombinedLinesV = new ArrayList<>();
		int maxArea = 0;
		for (int ih = 0; ih < linesH.size(); ih++)
		{
			// Get the datum horizontal line
			Line line = linesH.get(ih);

			// Get the bounds of the x coordinates of the horizontal lines of this subset
			int minX1 = line.x1 - endpointTolerance;
			int maxX1 = line.x1 + endpointTolerance;
			int minX2 = line.x2 - endpointTolerance;
			int maxX2 = line.x2 + endpointTolerance;

			// Get the subset of horizontal lines whose x coordinates are within bounds and are sufficiently separated
			// from their nearest neighbour.  Sequences of adjacent lines are combined into a single line.
			List<Line> combinedLinesH = new ArrayList<>();
			Line combinedLine = null;
			for (int i = 0; i < linesH.size(); i++)
			{
				line = linesH.get(i);
				if ((line.x1 >= minX1) && (line.x1 <= maxX1) && (line.x2 >= minX2) && (line.x2 <= maxX2))
				{
					if ((combinedLine == null) || (line.y1 > combinedLine.y2 + 1))
					{
						if ((combinedLine != null) && (line.y1 <= combinedLine.y2 + minLineSeparation))
							break;
						combinedLine = line.clone();
						combinedLinesH.add(combinedLine);
					}
					else
						++combinedLine.y2;
				}
			}

			// If the set of combined horizontal lines is smaller than the current largest subset, try the next subset
			if (combinedLinesH.size() < Math.max(MIN_NUM_LINES_PER_DIMENSION, maxCombinedLinesH.size()))
				continue;

			// Get the coordinates of the bounding rectangle of the combined horizontal lines
			Bounds hBounds = getBoundsH(combinedLinesH);

			// Get the coordinates of the relaxed bounding rectangle of the combined horizontal lines
			int hx1 = hBounds.x1 - endpointTolerance;
			int hy1 = hBounds.y1 - endpointTolerance;
			int hx2 = hBounds.x2 + endpointTolerance;
			int hy2 = hBounds.y2 + endpointTolerance;

			// Get the largest subset of vertical lines that lie within the relaxed bounding rectangle of the subset of
			// horizontal lines and satisfy separation and endpoint tolerance
			for (int iv = 0; iv < linesV.size(); iv++)
			{
				// Get the datum vertical line
				line = linesV.get(iv);

				// Test whether the datum lies within the relaxed bounding rectangle of the horizontal lines
				if ((line.x1 < hx1) || (line.y1 < hy1) || (line.x2 > hx2) || (line.y2 > hy2))
					continue;

				// Get the bounds of the y coordinates of the vertical lines of this subset
				int minY1 = line.y1 - endpointTolerance;
				int maxY1 = line.y1 + endpointTolerance;
				int minY2 = line.y2 - endpointTolerance;
				int maxY2 = line.y2 + endpointTolerance;

				// Get the subset of vertical lines whose y coordinates are within bounds and are sufficiently separated
				// from their nearest neighbour.  Sequences of adjacent lines are combined into a single line.
				List<Line> combinedLinesV = new ArrayList<>();
				combinedLine = null;
				for (int i = 0; i < linesV.size(); i++)
				{
					line = linesV.get(i);
					if ((line.y1 >= minY1) && (line.y1 <= maxY1) && (line.y2 >= minY2) && (line.y2 <= maxY2))
					{
						if ((combinedLine == null) || (line.x1 > combinedLine.x2 + 1))
						{
							if ((combinedLine != null) && (line.x1 <= combinedLine.x2 + minLineSeparation))
								break;
							combinedLine = line.clone();
							combinedLinesV.add(combinedLine);
						}
						else
							++combinedLine.x2;
					}
				}

				// If the set of combined vertical lines is smaller than the current largest subset, try the next subset
				if (combinedLinesV.size() < Math.max(MIN_NUM_LINES_PER_DIMENSION, maxCombinedLinesV.size()))
					continue;

				// Get the coordinates of the bounding rectangle of the combined vertical lines
				Bounds vBounds = getBoundsV(combinedLinesV);

				// Get the relaxed y coordinates of the combined vertical lines
				int vy1 = vBounds.y1 - endpointTolerance;
				int vy2 = vBounds.y2 + endpointTolerance;

				// Remove horizontal lines that lie outside the relaxed y coordinates of the combined vertical lines
				for (int i = 0; i < combinedLinesH.size(); i++)
				{
					combinedLine = combinedLinesH.get(i);
					if ((combinedLine.y1 < vy1) || (combinedLine.y2 > vy2))
						combinedLinesH.remove(i--);
				}

				// If the set of combined horizontal lines is now smaller than the current largest subset, try the next
				// subset
				if (combinedLinesH.size() < Math.max(MIN_NUM_LINES_PER_DIMENSION, maxCombinedLinesH.size()))
					continue;

				// Calculate the area of the combined bounding rectangle
				Rectangle rect = getCombinedBounds(getBoundsH(combinedLinesH), vBounds);
				int area = rect.width * rect.height;

				// Update the current largest subsets of horizontal and vertical lines
				if ((combinedLinesH.size() > maxCombinedLinesH.size())
					|| (combinedLinesV.size() > maxCombinedLinesV.size()) || (area > maxArea))
				{
					maxCombinedLinesH = combinedLinesH;
					maxCombinedLinesV = combinedLinesV;
					maxArea = area;
				}
			}
		}

		// Test for sufficient horizontal and vertical lines
		if ((maxCombinedLinesH.size() < MIN_NUM_LINES_PER_DIMENSION)
			|| (maxCombinedLinesV.size() < MIN_NUM_LINES_PER_DIMENSION))
			throw new AppException(ErrorId.TOO_FEW_COINCIDENT_HORIZONTAL_AND_VERTICAL_LINES);

		// Get the combined bounding rectangle
		Rectangle rect = getCombinedBounds(getBoundsH(maxCombinedLinesH), getBoundsV(maxCombinedLinesV));

		// Return grid information
		return new Info(rect.x, rect.y, rect.width, rect.height, maxCombinedLinesV.size() - 1,
						maxCombinedLinesH.size() - 1);
	}

	//------------------------------------------------------------------

	private static Bounds getBoundsH(List<Line> lines)
	{
		// Get x coordinates
		int x1 = Integer.MAX_VALUE;
		int x2 = Integer.MIN_VALUE;
		for (Line line : lines)
		{
			if (x1 > line.x1)
				x1 = line.x1;
			if (x2 < line.x2)
				x2 = line.x2;
		}

		// Get y coordinates
		int y1 = 0;
		int y2 = 0;
		if (!lines.isEmpty())
		{
			y1 = lines.get(0).y1;
			y2 = lines.get(lines.size() - 1).y2;
		}

		// Return bounds
		return new Bounds(x1, y1, x2, y2);
	}

	//------------------------------------------------------------------

	private static Bounds getBoundsV(List<Line> lines)
	{
		// Get x coordinates
		int x1 = 0;
		int x2 = 0;
		if (!lines.isEmpty())
		{
			x1 = lines.get(0).x1;
			x2 = lines.get(lines.size() - 1).x2;
		}

		// Get y coordinates
		int y1 = Integer.MAX_VALUE;
		int y2 = Integer.MIN_VALUE;
		for (Line line : lines)
		{
			if (y1 > line.y1)
				y1 = line.y1;
			if (y2 < line.y2)
				y2 = line.y2;
		}

		// Return bounds
		return new Bounds(x1, y1, x2, y2);
	}

	//------------------------------------------------------------------

	private static Rectangle getCombinedBounds(Bounds hBounds,
											   Bounds vBounds)
	{
		int x = Math.min(hBounds.x1, vBounds.x1);
		int y = Math.min(hBounds.y1, vBounds.y1);
		int width = Math.max(hBounds.x2, vBounds.x2) - x + 1;
		int height = Math.max(hBounds.y2, vBounds.y2) - y + 1;
		return new Rectangle(x, y, width, height);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Abstract methods
////////////////////////////////////////////////////////////////////////

	public abstract Separator getSeparator();

	//------------------------------------------------------------------

	public abstract Cell getCell(int row,
								 int column);

	//------------------------------------------------------------------

	public abstract List<IndexPair> getIsolatedCells();

	//------------------------------------------------------------------

	public abstract List<String> getGridDefinition();

	//------------------------------------------------------------------

	public abstract List<CssRuleSet> getStyleRuleSets(int    cellSize,
													  Color  gridColour,
													  Color  entryColour,
													  double fieldNumberFontSizeFactor);

	//------------------------------------------------------------------

	public abstract List<CssMediaRule> getStyleMediaRules();

	//------------------------------------------------------------------

	public abstract Grid createCopy();

	//------------------------------------------------------------------

	public abstract boolean canUndoEdit();

	//------------------------------------------------------------------

	public abstract void undoEdit();

	//------------------------------------------------------------------

	public abstract boolean canRedoEdit();

	//------------------------------------------------------------------

	public abstract void redoEdit();

	//------------------------------------------------------------------

	public abstract void setSymmetry(Symmetry symmetry);

	//------------------------------------------------------------------

	protected abstract boolean isSymmetry(Symmetry symmetry);

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	public int getNumColumns()
	{
		return numColumns;
	}

	//------------------------------------------------------------------

	public int getNumRows()
	{
		return numRows;
	}

	//------------------------------------------------------------------

	public Symmetry getSymmetry()
	{
		return symmetry;
	}

	//------------------------------------------------------------------

	public int getNumFields(Direction direction)
	{
		return (fieldLists.containsKey(direction) ? fieldLists.get(direction).size() : 0);
	}

	//------------------------------------------------------------------

	public Field getField(Field.Id fieldId)
	{
		if (fieldLists.containsKey(fieldId.direction))
		{
			for (Field field : fieldLists.get(fieldId.direction))
			{
				if (field.getId().equals(fieldId))
					return field;
			}
		}
		return null;
	}

	//------------------------------------------------------------------

	public List<Field> getFields(Clue clue)
	{
		List<Field> fields = new ArrayList<>();
		for (int i = 0; i < clue.getNumFields(); i++)
		{
			Field field = getField(clue.getFieldId(i));
			if (field != null)
				fields.add(field);
		}
		return fields;
	}

	//------------------------------------------------------------------

	public List<Field> getFields()
	{
		List<Field> fields = new ArrayList<>();
		for (Direction direction : fieldLists.keySet())
			fields.addAll(fieldLists.get(direction));
		return fields;
	}

	//------------------------------------------------------------------

	public List<Field> getFields(Direction direction)
	{
		return (fieldLists.containsKey(direction)
											? Collections.unmodifiableList(fieldLists.get(direction))
											: new ArrayList<Field>());
	}

	//------------------------------------------------------------------

	public boolean isEntryValue(int row,
								int column)
	{
		char ch = entries.values[row][column];
		return ((ch != Entries.NO_VALUE) && (ch != Entries.UNDEFINED_VALUE));
	}

	//------------------------------------------------------------------

	public char getEntryValue(int row,
							  int column)
	{
		return entries.values[row][column];
	}

	//------------------------------------------------------------------

	public Entries getEntries()
	{
		return entries.clone();
	}

	//------------------------------------------------------------------

	public String getEntriesString(String separator)
	{
		StringBuilder buffer = new StringBuilder(1024);
		for (Direction direction : Direction.DEFINED_DIRECTIONS)
		{
			for (Field field : fieldLists.get(direction))
			{
				if ((separator != null) && (buffer.length() > 0))
					buffer.append(separator);

				switch (direction)
				{
					case NONE:
						// do nothing
						break;

					case ACROSS:
						for (int i = 0; i < field.length; i++)
							buffer.append(entries.values[field.row][field.column + i]);
						break;

					case DOWN:
						for (int i = 0; i < field.length; i++)
							buffer.append(entries.values[field.row + i][field.column]);
						break;
				}
			}
		}
		return buffer.toString();
	}

	//------------------------------------------------------------------

	public void setEntryValue(int  row,
							  int  column,
							  char value)
	{
		entries.setValue(row, column, value);
		incorrectEntries = null;
	}

	//------------------------------------------------------------------

	public void setEntries(Entries entries)
	{
		this.entries = entries.clone();
		incorrectEntries = null;
	}

	//------------------------------------------------------------------

	public void setEntries(List<String> entries)
		throws AppException
	{
		// Get list of fields
		List<Field> fields = getFields();

		// Compare number of entries with number of fields
		if (entries.size() != fields.size())
			throw new AppException(ErrorId.INCORRECT_NUMBER_OF_ENTRIES);

		// Set entries
		this.entries.clear();
		for (int i = 0; i < fields.size(); i++)
			setEntry(fields.get(i), entries.get(i));

		// Invalidate "incorrect entry" flags
		incorrectEntries = null;
	}

	//------------------------------------------------------------------

	public boolean isEntriesEmpty()
	{
		return (entries.numValues == 0);
	}

	//------------------------------------------------------------------

	public boolean isEntriesComplete()
	{
		return (entries.numValues == entries.numCells);
	}

	//------------------------------------------------------------------

	public boolean isIncorrectEntries()
	{
		return (incorrectEntries != null);
	}

	//------------------------------------------------------------------

	public boolean isIncorrectEntry(int row,
									int column)
	{
		return incorrectEntries[row][column];
	}

	//------------------------------------------------------------------

	public boolean hasSolution()
	{
		return (solution != null);
	}

	//------------------------------------------------------------------

	public Entries getSolution()
	{
		return ((solution == null) ? null : solution.clone());
	}

	//------------------------------------------------------------------

	public String getSolutionString(String separator)
	{
		StringBuilder buffer = new StringBuilder(1024);
		if (solution != null)
		{
			for (Direction direction : Direction.DEFINED_DIRECTIONS)
			{
				for (Field field : fieldLists.get(direction))
				{
					if ((separator != null) && (buffer.length() > 0))
						buffer.append(separator);

					switch (direction)
					{
						case NONE:
							// do nothing
							break;

						case ACROSS:
							for (int i = 0; i < field.length; i++)
								buffer.append(solution.values[field.row][field.column + i]);
							break;

						case DOWN:
							for (int i = 0; i < field.length; i++)
								buffer.append(solution.values[field.row + i][field.column]);
							break;
					}
				}
			}
		}
		return buffer.toString();
	}

	//------------------------------------------------------------------

	public void setSolution()
	{
		solution = entries.clone();
	}

	//------------------------------------------------------------------

	public void setSolution(Entries solution)
	{
		this.solution = (solution == null) ? null : solution.clone();
	}

	//------------------------------------------------------------------

	public void setSolution(List<String> answers)
		throws AppException
	{
		// Get list of fields
		List<Field> fields = getFields();

		// Compare number of answers with number of fields
		if (answers.size() != fields.size())
			throw new AppException(ErrorId.INCORRECT_NUMBER_OF_ANSWERS);

		// Set solution
		solution = new Entries(numColumns, numRows);
		for (int i = 0; i < fields.size(); i++)
		{
			// Compare length of answer with length of field
			String answer = answers.get(i);
			Field field = fields.get(i);
			String idStr = field.getId().toString();
			if (answer.length() != field.length)
				throw new AppException(ErrorId.INCORRECT_ANSWER_LENGTH, idStr);

			// Set answer
			for (int j = 0; j < field.length; j++)
			{
				char ch0 = answer.charAt(j);
				if (!Character.isLetterOrDigit(ch0))
					throw new AppException(ErrorId.ILLEGAL_CHARACTER_IN_ANSWER, idStr,
										   Character.toString(ch0));

				switch (field.direction)
				{
					case NONE:
						// do nothing
						break;

					case ACROSS:
					{
						char ch1 = solution.values[field.row][field.column + j];
						if ((ch1 != Entries.NO_VALUE) && (ch1 != ch0))
							throw new AppException(ErrorId.CONFLICTING_ANSWER,
												   new String[]{ idStr, Integer.toString(j) });
						solution.setValue(field.row, field.column + j, ch0);
						break;
					}

					case DOWN:
					{
						char ch1 = solution.values[field.row + j][field.column];
						if ((ch1 != Entries.NO_VALUE) && (ch1 != ch0))
							throw new AppException(ErrorId.CONFLICTING_ANSWER,
												   new String[]{ idStr, Integer.toString(j) });
						solution.setValue(field.row + j, field.column, ch0);
						break;
					}
				}
			}
		}
	}

	//------------------------------------------------------------------

	public void clearSolution()
	{
		solution = null;
	}

	//------------------------------------------------------------------

	public List<Field> findFields(Field.IFilter filter)
	{
		List<Field> fields = new ArrayList<>();
		for (Direction direction : fieldLists.keySet())
		{
			for (Field field : fieldLists.get(direction))
			{
				if (filter.acceptField(field))
					fields.add(field);
			}
		}
		return fields;
	}

	//------------------------------------------------------------------

	public List<Field> findFields(int       row,
								  int       column,
								  Direction direction)
	{
		return findFields(field -> ((direction == Direction.NONE) || (direction == field.direction))
								   && field.containsCell(row, column));
	}

	//------------------------------------------------------------------

	public List<Field> findFields(Field.Id id)
	{
		return findFields(field -> ((id.direction == Direction.NONE) || (id.direction == field.direction))
								   && (id.number == field.number));
	}

	//------------------------------------------------------------------

	public List<Field> getFullyIntersectingFields()
	{
		List<Field> fullyIntersectingFields = new ArrayList<>();
		for (Direction direction : Direction.DEFINED_DIRECTIONS)
		{
			Direction crossDirection = (direction == Direction.ACROSS) ? Direction.DOWN : Direction.ACROSS;
			if (fieldLists.containsKey(direction) && fieldLists.containsKey(crossDirection))
			{
				List<Field> fields = fieldLists.get(direction);
				List<Field> crossFields = fieldLists.get(crossDirection);
				for (Field field : fields)
				{
					boolean intersects = false;
					int row = field.row;
					int column = field.column;
					for (int i = 0; i < field.length; i++)
					{
						intersects = false;
						for (Field crossField : crossFields)
						{
							if (crossField.containsCell(row, column))
							{
								intersects = true;
								break;
							}
						}
						if (!intersects)
							break;
						switch (direction)
						{
							case NONE:
								// do nothing
								break;

							case ACROSS:
								++column;
								break;

							case DOWN:
								++row;
								break;
						}
					}
					if (intersects)
						fullyIntersectingFields.add(field);
				}
			}
		}
		return fullyIntersectingFields;
	}

	//------------------------------------------------------------------

	public void checkEntries()
	{
		if (solution != null)
			incorrectEntries = solution.compare(entries);
	}

	//------------------------------------------------------------------

	public void parseEntries(Element entriesElement)
		throws XmlParseException
	{
		for (Element element : XmlUtils.getChildElements(entriesElement, ElementName.ENTRY))
		{
			// Get element path
			String elementPath = XmlUtils.getElementPath(element);

			// Attribute: ID
			String attrName = AttrName.ID;
			String attrKey = XmlUtils.appendAttributeName(elementPath, attrName);
			String attrValue = XmlUtils.getAttribute(element, attrName);
			if (attrValue == null)
				throw new XmlParseException(ErrorId.NO_ATTRIBUTE, attrKey);
			Field.Id fieldId = null;
			try
			{
				fieldId = new Field.Id(attrValue);
			}
			catch (IllegalArgumentException e)
			{
				throw new XmlParseException(ErrorId.INVALID_ATTRIBUTE, attrKey, attrValue);
			}
			if (!Direction.DEFINED_DIRECTIONS.contains(fieldId.direction))
				throw new XmlParseException(ErrorId.INVALID_ATTRIBUTE, attrKey, attrValue);

			// Get field for ID
			Field field = getField(fieldId);
			if (field == null)
				throw new XmlParseException(ErrorId.INVALID_FIELD_ID, attrKey, fieldId.toString());

			// Set entry
			try
			{
				setEntry(field, element.getTextContent());
			}
			catch (AppException e)
			{
				throw new XmlParseException(e.getId(), elementPath, e.getSubstitutionStrings());
			}
		}
	}

	//------------------------------------------------------------------

	public CrosswordDocument.SolutionProperties parseSolution(Element       element,
															  final boolean required)
		throws AppException
	{
		// Get element path
		String elementPath = XmlUtils.getElementPath(element);

		// Attribute: hash
		String attrName = AttrName.HASH;
		String attrKey = XmlUtils.appendAttributeName(elementPath, attrName);
		String attrValue = XmlUtils.getAttribute(element, attrName);
		if (attrValue == null)
			throw new XmlParseException(ErrorId.NO_ATTRIBUTE, attrKey);
		byte[] hashValue = null;
		try
		{
			hashValue = NumberUtils.hexStringToBytes(attrValue);
			if (hashValue.length != HmacSha256.HASH_VALUE_SIZE)
				throw new XmlParseException(ErrorId.INVALID_ATTRIBUTE, attrKey, attrValue);
		}
		catch (NumberFormatException e)
		{
			throw new XmlParseException(ErrorId.INVALID_ATTRIBUTE, attrKey, attrValue);
		}

		// Attribute: location
		attrName = AttrName.LOCATION;
		attrKey = XmlUtils.appendAttributeName(elementPath, attrName);
		attrValue = XmlUtils.getAttribute(element, attrName);
		URL location = null;
		if (attrValue != null)
		{
			try
			{
				location = new URL(attrValue);
			}
			catch (MalformedURLException e)
			{
				throw new XmlParseException(ErrorId.INVALID_ATTRIBUTE, attrKey, attrValue);
			}
		}

		// Parse solution in document
		String passphrase = "";
		if (location == null)
		{
			// Attribute: encryption
			attrName = AttrName.ENCRYPTION;
			attrKey = XmlUtils.appendAttributeName(elementPath, attrName);
			attrValue = XmlUtils.getAttribute(element, attrName);
			if (attrValue == null)
				throw new XmlParseException(ErrorId.NO_ATTRIBUTE, attrKey);
			EncryptionKind encryptionKind = EncryptionKind.forKey(attrValue);
			if (encryptionKind == null)
				throw new XmlParseException(ErrorId.UNSUPPORTED_ENCRYPTION, attrKey, attrValue);

			// Get passphrase
			if (encryptionKind != EncryptionKind.NONE)
			{
				String[] result = new String[1];
				try
				{
					SwingUtilities.invokeAndWait(() -> result[0] =
														PassphraseDialog.showDialog(App.INSTANCE.getMainWindow(),
																					SOLUTION_STR, !required));
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
				passphrase = result[0];
				if (passphrase == null)
					throw new TaskCancelledException();
				if (passphrase.isEmpty())
					return new CrosswordDocument.SolutionProperties();
			}

			// Attribute: nonce
			attrName = AttrName.NONCE;
			attrKey = XmlUtils.appendAttributeName(elementPath, attrName);
			attrValue = XmlUtils.getAttribute(element, attrName);
			if (attrValue == null)
				throw new XmlParseException(ErrorId.NO_ATTRIBUTE, attrKey);
			if (attrValue.length() != 2 * Salsa20.NONCE_SIZE)
				throw new XmlParseException(ErrorId.INVALID_ATTRIBUTE, attrKey, attrValue);
			byte[] nonce = null;
			try
			{
				nonce = NumberUtils.hexStringToBytes(attrValue);
			}
			catch (NumberFormatException e)
			{
				throw new XmlParseException(ErrorId.INVALID_ATTRIBUTE, attrKey, attrValue);
			}

			// Decode Base64 text
			byte[] data = null;
			try
			{
				data = new Base64Encoder().decode(element.getTextContent());
			}
			catch (Base64Encoder.IllegalCharacterException e)
			{
				throw new XmlParseException(ErrorId.ILLEGAL_CHARACTER_IN_SOLUTION_ENCODING, elementPath);
			}
			catch (Base64Encoder.MalformedDataException e)
			{
				throw new XmlParseException(ErrorId.MALFORMED_SOLUTION_ENCODING, elementPath);
			}

			// Decrypt solution
			Prng prng = new Prng(passphrase, nonce);
			prng.combine(data);

			// Verify data
			HmacSha256 hash = new HmacSha256(prng.getKey());
			hash.update(data);
			if (!Arrays.equals(hash.getValue(), hashValue))
				throw new AppException(ErrorId.INCORRECT_PASSPHRASE);

			// Convert solution to string
			String str = null;
			try
			{
				str = new String(data, SOLUTION_ENCODING_NAME);
			}
			catch (UnsupportedEncodingException e)
			{
				throw new UnexpectedRuntimeException();
			}

			// Extract answers from solution string
			List<String> answers = new ArrayList<>();
			int index = 0;
			for (Field field : getFields())
			{
				int endIndex = index + field.length;
				if (endIndex <= str.length())
					answers.add(str.substring(index, endIndex));
				index = endIndex;
			}
			if (index != str.length())
				throw new XmlParseException(ErrorId.SOLUTION_LENGTH_NOT_CONSISTENT_WITH_GRID,
											elementPath);

			// Set solution
			try
			{
				setSolution(answers);
			}
			catch (AppException e)
			{
				throw new XmlParseException(e.getId(), elementPath, e.getSubstitutionStrings());
			}
		}
		return new CrosswordDocument.SolutionProperties(location, passphrase, hashValue);
	}

	//------------------------------------------------------------------

	public void writeGrid(XmlWriter writer,
						  int       indent)
		throws IOException
	{
		// Write start tag, grid
		List<Attribute> attributes = new ArrayList<>();
		attributes.add(new Attribute(AttrName.KIND, RECTANGULAR_ORTHOGONAL_STR));
		attributes.add(new Attribute(AttrName.SEPARATOR, getSeparator().key));
		attributes.add(new Attribute(AttrName.NUM_COLUMNS, numColumns));
		attributes.add(new Attribute(AttrName.NUM_ROWS, numRows));
		attributes.add(new Attribute(AttrName.SYMMETRY, symmetry.key));
		writer.writeElementStart(ElementName.GRID, attributes, indent, true, true);

		// Write grid definition
		indent += INDENT_INCREMENT;
		for (String str : getGridDefinition())
		{
			writer.writeSpaces(indent);
			writer.write(str);
			writer.writeEol();
		}

		// Write end tag, grid
		indent -= INDENT_INCREMENT;
		writer.writeElementEnd(ElementName.GRID, indent);
	}

	//------------------------------------------------------------------

	public void writeEntries(XmlWriter writer,
							 int       indent)
		throws IOException
	{
		// Write start tag, entries
		writer.writeElementStart(ElementName.ENTRIES, indent, true);

		// Write entries
		indent += INDENT_INCREMENT;
		List<Attribute> attributes = new ArrayList<>();
		for (Direction direction : Direction.DEFINED_DIRECTIONS)
		{
			for (Entry entry : getEntries(direction))
			{
				attributes.clear();
				attributes.add(new Attribute(AttrName.ID, entry.fieldId));
				writer.writeEscapedTextElement(ElementName.ENTRY, attributes, indent, false, entry.text);
			}
		}
		indent -= INDENT_INCREMENT;

		// Write end tag, entries
		writer.writeElementEnd(ElementName.ENTRIES, indent);
	}

	//------------------------------------------------------------------

	public EncodedSolution getEncodedSolution(String passphrase)
	{
		// Convert solution string to bytes
		byte[] data = null;
		try
		{
			data = getSolutionString(null).getBytes(SOLUTION_ENCODING_NAME);
		}
		catch (UnsupportedEncodingException e)
		{
			throw new UnexpectedRuntimeException();
		}

		// Initialise PRNG
		byte[] nonce = Prng.createNonce();
		Prng prng = new Prng(passphrase, nonce);

		// Generate a hash of the solution data
		HmacSha256 hash = new HmacSha256(prng.getKey());
		hash.update(data);

		// Encrypt the solution data
		prng.combine(data);

		// Return encoded and encrypted solution
		return new EncodedSolution(nonce, hash.getValue(), data);
	}

	//------------------------------------------------------------------

	public void writeSolution(XmlWriter writer,
							  int       indent,
							  String    passphrase)
		throws IOException
	{
		// Encode and encrypt solution
		EncodedSolution encodedSolution = getEncodedSolution(passphrase);

		// Write start tag, solution
		List<Attribute> attributes = new ArrayList<>();
		NumberUtils.setHexLower();
		EncryptionKind encryptionKind = passphrase.isEmpty() ? EncryptionKind.NONE
															 : EncryptionKind.SALSA20;
		attributes.add(new Attribute(AttrName.ENCRYPTION, encryptionKind.key));
		attributes.add(new Attribute(AttrName.NONCE,
									 NumberUtils.bytesToHexString(encodedSolution.nonce)));
		attributes.add(new Attribute(AttrName.HASH,
									 NumberUtils.bytesToHexString(encodedSolution.hashValue)));
		NumberUtils.setHexUpper();
		writer.writeElementStart(ElementName.SOLUTION, attributes, indent, true, true);

		// Write solution, encoded as Base64
		indent += INDENT_INCREMENT;
		for (String line : new Base64Encoder(SOLUTION_LINE_LENGTH).encodeLines(encodedSolution.data))
		{
			writer.writeSpaces(indent);
			writer.write(line);
			writer.writeEol();
		}
		indent -= INDENT_INCREMENT;

		// Write end tag, solution
		writer.writeElementEnd(ElementName.SOLUTION, indent);
	}

	//------------------------------------------------------------------

	public void writeSolution(XmlWriter writer,
							  int       indent,
							  URL       location,
							  byte[]    hashValue)
		throws IOException
	{
		List<Attribute> attributes = new ArrayList<>();
		NumberUtils.setHexLower();
		attributes.add(new Attribute(AttrName.LOCATION, location, true));
		attributes.add(new Attribute(AttrName.HASH, NumberUtils.bytesToHexString(hashValue)));
		NumberUtils.setHexUpper();
		writer.writeEmptyElement(ElementName.SOLUTION, attributes, indent, true);
	}

	//------------------------------------------------------------------

	public void writeHtml(XmlWriter writer,
						  int       indent,
						  int       cellSize,
						  boolean   writeFieldNumbers,
						  boolean   writeEntries)
		throws IOException
	{
		// Write start tag, table division
		List<Attribute> attributes = new ArrayList<>();
		attributes.add(new Attribute(HtmlConstants.AttrName.ID, HtmlConstants.Id.GRID));
		writer.writeElementStart(HtmlConstants.ElementName.DIV, attributes, indent, true, false);

		// Write grid
		indent += INDENT_INCREMENT;
		for (int row = 0; row < numRows; row++)
		{
			writer.writeElementStart(HtmlConstants.ElementName.DIV, indent, true);

			indent += INDENT_INCREMENT;
			for (int column = 0; column < numColumns; column++)
			{
				Cell cell = getCell(row, column);
				cell.write(writer, indent, cellSize, writeFieldNumbers ? cell.getFieldNumber() : 0,
						   writeEntries ? getEntryValue(row, column) : Entries.UNDEFINED_VALUE);
			}
			indent -= INDENT_INCREMENT;

			writer.writeElementEnd(HtmlConstants.ElementName.DIV, indent);
		}

		// Write end tag, table division
		indent -= INDENT_INCREMENT;
		writer.writeElementEnd(HtmlConstants.ElementName.DIV, indent);
	}

	//------------------------------------------------------------------

	protected void updateSymmetry()
	{
		for (Symmetry symmetry : TEST_SYMMETRIES)
		{
			if (symmetry.supportsDimensions(numColumns, numRows) && isSymmetry(symmetry))
			{
				this.symmetry = symmetry;
				break;
			}
		}
	}

	//------------------------------------------------------------------

	protected Field addField(int       row,
							 int       column,
							 Direction direction,
							 int       length,
							 int       fieldNumber)
	{
		Field field = new Field(row, column, direction, length, fieldNumber);
		List<Field> fields = fieldLists.get(direction);
		if (fields == null)
		{
			fields = new ArrayList<>();
			fieldLists.put(direction, fields);
		}
		fields.add(field);
		return field;
	}

	//------------------------------------------------------------------

	private List<Entry> getEntries(Direction direction)
	{
		List<Entry> outEntries = new ArrayList<>();
		for (Field field : fieldLists.get(direction))
		{
			char[] chars = null;
			int numUndefined = 0;
			switch (direction)
			{
				case NONE:
					// do nothing
					break;

				case ACROSS:
					chars = new char[field.length];
					for (int i = 0; i < chars.length; i++)
					{
						char ch = entries.values[field.row][field.column + i];
						if (ch == Entries.UNDEFINED_VALUE)
							++numUndefined;
						chars[i] = ch;
					}
					break;

				case DOWN:
					chars = new char[field.length];
					for (int i = 0; i < chars.length; i++)
					{
						char ch = entries.values[field.row + i][field.column];
						if (ch == Entries.UNDEFINED_VALUE)
							++numUndefined;
						chars[i] = ch;
					}
					break;
			}
			if ((chars != null) && (numUndefined < chars.length))
				outEntries.add(new Entry(field.getId(), new String(chars)));
		}
		return outEntries;
	}

	//------------------------------------------------------------------

	private void setEntry(Field  field,
						  String entry)
		throws AppException
	{
		// Compare length of entry with length of field
		String idStr = field.getId().toString();
		if (entry.length() != field.length)
			throw new AppException(ErrorId.INCORRECT_ENTRY_LENGTH, idStr);

		// Set entry
		for (int i = 0; i < field.length; i++)
		{
			char ch0 = entry.charAt(i);
			if (ch0 != Entries.UNDEFINED_VALUE)
			{
				if (!Character.isLetterOrDigit(ch0))
					throw new AppException(ErrorId.ILLEGAL_CHARACTER_IN_ENTRY, idStr,
										   Character.toString(ch0));

				switch (field.direction)
				{
					case NONE:
						// do nothing
						break;

					case ACROSS:
					{
						char ch1 = entries.values[field.row][field.column + i];
						if ((ch1 != Entries.UNDEFINED_VALUE) && (ch1 != ch0))
							throw new AppException(ErrorId.CONFLICTING_ENTRY,
												   new String[]{ idStr, Integer.toString(i) });
						entries.setValue(field.row, field.column + i, ch0);
						break;
					}

					case DOWN:
					{
						char ch1 = entries.values[field.row + i][field.column];
						if ((ch1 != Entries.UNDEFINED_VALUE) && (ch1 != ch0))
							throw new AppException(ErrorId.CONFLICTING_ENTRY,
												   new String[]{ idStr, Integer.toString(i) });
						entries.setValue(field.row + i, field.column, ch0);
						break;
					}
				}
			}
		}
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance fields
////////////////////////////////////////////////////////////////////////

	protected	int							numRows;
	protected	int							numColumns;
	protected	Symmetry					symmetry;
	protected	Map<Direction, List<Field>>	fieldLists;
	protected	Entries						entries;
	protected	Entries						solution;
	protected	boolean[][]					incorrectEntries;

}

//----------------------------------------------------------------------
