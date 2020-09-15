/*====================================================================*\

Clue.java

Clue class.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.crosswordeditor;

//----------------------------------------------------------------------


// IMPORTS


import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import uk.blankaspect.common.exception.AppException;
import uk.blankaspect.common.exception.UnexpectedRuntimeException;

import uk.blankaspect.common.regex.RegexUtils;
import uk.blankaspect.common.regex.Substitution;

import uk.blankaspect.common.string.StringUtils;

//----------------------------------------------------------------------


// CLUE CLASS


class Clue
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	public static final		char	REGEX_ALTERNATION_CHAR	= '|';

	public static final		String	DEFAULT_LENGTH_REGEX	= "\\((\\d[^\\(]*?)\\)$";

	public static final		Comparator<Clue>	ID_COMPARATOR;

	private static final	String	SPACE_REGEX				= " +";
	private static final	String	WHITESPACE_REGEX		= "[\\p{Zs}\\t]+";
	private static final	String	LENGTH_SEPARATOR_REGEX	= "[^\\d]+";
	private static final	String	CLUE_INDEX_REGEX		= "(?: *" + RegexUtils.escape(Id.INDEX_PREFIX) + "(\\d+))?";

	private static final	String	FIELD_ID_REGEX_FRAG1	= "(\\d+)(?:(";
	private static final	String	FIELD_ID_REGEX_FRAG2	= ")|(?: *)(";
	private static final	String	FIELD_ID_REGEX_FRAG3	= "))?";

	private static final	String	SEC_FIELD_ID_REGEX_FRAG1	= " *, *";
	private static final	String	SEC_FIELD_ID_REGEX_FRAG2	= "(?= |,|$)";

	private static final	Pattern	NUMBER_PATTERN	= Pattern.compile("^(\\d+)");

	private enum ParseState
	{
		START_OF_CLUE,
		PRIMARY_FIELD_ID,
		SECONDARY_FIELD_IDS,
		REFERENCE,
		TEXT,
		ANSWER_LENGTH,
		END_OF_CLUE,
		STOP
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

		FIELD_NUMBER_EXPECTED
		("A field number was expected at the start of the line."),

		INVALID_FIELD_NUMBER
		("The field number %1 is invalid."),

		CLUE_TEXT_EXPECTED
		("The text of a clue was expected."),

		ANSWER_LENGTH_EXPECTED
		("The length of the answer was expected at the end of the line.");

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


	// CLUE IDENTIFIER CLASS


	public static class Id
		implements Cloneable
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		private static final	String	INDEX_PREFIX	= "#";

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		/**
		 * @throws IllegalArgumentException
		 */

		public Id(Grid.Field.Id fieldId,
				  int           index)
		{
			if (fieldId == null)
				throw new IllegalArgumentException();

			this.fieldId = fieldId.clone();
			this.index = index;
		}

		//--------------------------------------------------------------

		private Id(String numberStr,
				   String directionStr,
				   String indexStr)
		{
			fieldId = new Grid.Field.Id(numberStr, directionStr);
			index = (indexStr == null) ? 0 : Integer.parseInt(indexStr);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		public boolean equals(Object obj)
		{
			if (obj instanceof Id)
			{
				Id other = (Id)obj;
				return (fieldId.equals(other.fieldId) && (index == other.index));
			}
			return false;
		}

		//--------------------------------------------------------------

		@Override
		public int hashCode()
		{
			return (fieldId.hashCode() * 31 + index);
		}

		//--------------------------------------------------------------

		@Override
		public Id clone()
		{
			try
			{
				Id copy = (Id)super.clone();
				copy.fieldId = fieldId.clone();
				return copy;
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
			return ((index == 0) ? fieldId.toString() : fieldId.toString() + INDEX_PREFIX + index);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		Grid.Field.Id	fieldId;
		int				index;

	}

	//==================================================================


	// FIELD LIST CLASS


	public static class FieldList
		implements Cloneable
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		public FieldList()
		{
			fields = new ArrayList<>();
			index = -1;
		}

		//--------------------------------------------------------------

		public FieldList(Grid.Field field)
		{
			this();
			fields.add(field);
			index = 0;
		}

		//--------------------------------------------------------------

		public FieldList(Collection<Grid.Field> fields,
						 int                    index)
		{
			this.fields = new ArrayList<>(fields);
			this.index = index;
			enabled = true;
		}

		//--------------------------------------------------------------

		public FieldList(List<Grid.Field> fields,
						 Grid.Field       field)
		{
			this(fields, fields.indexOf(field));
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		public boolean equals(Object obj)
		{
			if (obj instanceof FieldList)
			{
				FieldList other = (FieldList)obj;
				return (fields.equals(other.fields) && (index == other.index) && (enabled == other.enabled));
			}
			return false;
		}

		//--------------------------------------------------------------

		@Override
		public int hashCode()
		{
			return (fields.hashCode() * 31 + index);
		}

		//--------------------------------------------------------------

		@Override
		public FieldList clone()
		{
			try
			{
				FieldList copy = (FieldList)super.clone();
				copy.fields = new ArrayList<>();
				for (Grid.Field field : fields)
					copy.fields.add(field.clone());
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

		public int getNumFields()
		{
			return fields.size();
		}

		//--------------------------------------------------------------

		public Grid.Field getField()
		{
			return ((index < 0) ? null : fields.get(index));
		}

		//--------------------------------------------------------------

		public Grid.Field getField(int index)
		{
			return fields.get(index);
		}

		//--------------------------------------------------------------

		public boolean isEmpty()
		{
			return fields.isEmpty();
		}

		//--------------------------------------------------------------

		public boolean matches(List<Grid.Field> fields)
		{
			return this.fields.equals(fields);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		List<Grid.Field>	fields;
		int					index;
		boolean				enabled;

	}

	//==================================================================


	// ANSWER-LENGTH PARSER CLASS


	public static class AnswerLengthParser
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		public AnswerLengthParser(String             regex,
								  List<Substitution> substitutions)
		{
			this.pattern = Pattern.compile(regex);
			this.substitutions = substitutions;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		private int parseLength(String text)
		{
			for (Substitution substitution : substitutions)
				text = substitution.apply(text);

			int length = 0;
			for (String str : text.split(LENGTH_SEPARATOR_REGEX))
			{
				str = str.trim();
				if (!str.isEmpty())
					length += Integer.parseInt(str);
			}
			return length;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	Pattern				pattern;
		private	List<Substitution>	substitutions;

	}

	//==================================================================


	// CLUE FIELD ID COMPARATOR CLASS


	public static class FieldIdComparator
		implements Comparator<Clue>
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		public static final	FieldIdComparator	INSTANCE	= new FieldIdComparator();

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private FieldIdComparator()
		{
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : Comparator interface
	////////////////////////////////////////////////////////////////////

		@Override
		public int compare(Clue clue1,
						   Clue clue2)
		{
			Grid.Field.Id id1 = clue1.fieldIds.get(0);
			Grid.Field.Id id2 = clue2.fieldIds.get(0);
			int result = Integer.compare(id1.direction.ordinal(), id2.direction.ordinal());
			if (result == 0)
				result = Integer.compare(id1.number, id2.number);
			return result;
		}

		//--------------------------------------------------------------

	}

	//==================================================================


	// PARSE EXCEPTION CLASS


	private static class ParseException
		extends AppException
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		private static final	String	LINE_STR	= "Line ";

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private ParseException(AppException.IId id,
							   int              lineNum,
							   String           text)
		{
			super(id);
			this.lineNum = lineNum;
			this.text = text;
		}

		//--------------------------------------------------------------

		private ParseException(AppException.IId id,
							   int              lineNum,
							   String           text,
							   CharSequence...  replacements)
		{
			super(id, replacements);
			this.lineNum = lineNum;
			this.text = text;
		}

		//--------------------------------------------------------------

		private ParseException(StyledText.ParseException exception,
							   int                       lineNum,
							   String                    text)
		{
			super(exception);
			this.lineNum = lineNum;
			this.text = text;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		protected String getPrefix()
		{
			StringBuilder buffer = new StringBuilder(128);
			buffer.append(LINE_STR);
			buffer.append(lineNum);
			if (text != null)
			{
				buffer.append(": ");
				buffer.append(text);
			}
			buffer.append('\n');
			return buffer.toString();
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	int		lineNum;
		private	String	text;

	}

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	public Clue(Grid.Field.Id fieldId)
	{
		fieldIds = new ArrayList<>();
		fieldIds.add(fieldId);
	}

	//------------------------------------------------------------------

	public Clue(Clue.Id clueId)
	{
		fieldIds = new ArrayList<>();
		fieldIds.add(clueId.fieldId);
		index = clueId.index;
	}

	//------------------------------------------------------------------

	public Clue(Grid.Field.Id fieldId,
				Id            referentId)
	{
		this(fieldId);
		this.referentId = referentId.clone();
	}

	//------------------------------------------------------------------

	public Clue(List<Grid.Field.Id> fieldIds,
				String              text,
				String              referenceKeyword,
				int                 answerLength)
		throws StyledText.ParseException
	{
		// Initialise field IDs, reference and text
		this(fieldIds, text, referenceKeyword);

		// Initialise answer length
		this.answerLength = answerLength;
	}

	//------------------------------------------------------------------

	public Clue(List<Grid.Field.Id> fieldIds,
				StyledText          text,
				int                 answerLength)
	{
		this.fieldIds = new ArrayList<>(fieldIds);
		this.text = text;
		this.answerLength = answerLength;
	}

	//------------------------------------------------------------------

	public Clue(List<Grid.Field.Id> fieldIds,
				String              text,
				String              referenceKeyword,
				AnswerLengthParser  answerLengthParser)
		throws StyledText.ParseException
	{
		// Initialise field IDs, reference and text
		this(fieldIds, text, referenceKeyword);

		// Initialise answer length
		if (answerLengthParser != null)
		{
			Matcher matcher = answerLengthParser.pattern.matcher(text);
			if (matcher.find())
				answerLength = getAnswerLength(matcher, answerLengthParser);
		}
	}

	//------------------------------------------------------------------

	private Clue(String numberStr)
	{
		this(new Grid.Field.Id(numberStr, null));
	}

	//------------------------------------------------------------------

	private Clue(List<Grid.Field.Id> fieldIds,
				 String              text,
				 String              referenceKeyword)
		throws StyledText.ParseException
	{
		// Set field IDs
		this.fieldIds = new ArrayList<>(fieldIds);

		// Replace sequences of tabs and non-standard spaces with single space characters
		text = text.replace(WHITESPACE_REGEX, " ").trim();

		// Set referent ID
		if (referenceKeyword != null)
		{
			String regex = RegexUtils.escape(referenceKeyword) + SPACE_REGEX + Grid.Field.Id.PATTERN.pattern()
																									+ CLUE_INDEX_REGEX;
			Matcher matcher = Pattern.compile(regex).matcher(text);
			if (matcher.matches())
				referentId = new Id(matcher.group(1), matcher.group(2), matcher.group(3));
		}

		// Set text
		if (referentId == null)
			this.text = new StyledText(text);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	public static List<Clue> getCluesFromClipboard(String             referenceKeyword,
												   AnswerLengthParser answerLengthParser,
												   List<Substitution> substitutions)
		throws AppException
	{
		return parseClues(Utils.getClipboardText(), referenceKeyword, answerLengthParser, substitutions);
	}

	//------------------------------------------------------------------

	private static int getAnswerLength(Matcher            matcher,
									   AnswerLengthParser lengthParser)
	{
		return lengthParser.parseLength(matcher.group(1));
	}

	//------------------------------------------------------------------

	private static List<Clue> parseClues(String             text,
										 String             referenceKeyword,
										 AnswerLengthParser answerLengthParser,
										 List<Substitution> substitutions)
		throws AppException
	{
		// Initialise lists of direction keywords
		List<String> directionKeywords = new ArrayList<>();
		for (Direction direction : Direction.DEFINED_DIRECTIONS)
		{
			List<String> keywords = AppConfig.INSTANCE.getClueDirectionKeywords(direction);
			if (keywords.isEmpty())
				keywords.add(direction.getSuffix());
			directionKeywords.addAll(keywords);
		}
		directionKeywords.sort(Direction.KEYWORD_COMPARATOR);

		// Escape direction keywords for use in regular expression, and divide keywords into two lists:
		// those with leading spaces and those without
		List<String> spaceKeywords = new ArrayList<>();
		List<String> noSpaceKeywords = new ArrayList<>();
		for (String keyword : directionKeywords)
		{
			keyword = RegexUtils.escape(keyword);
			int length = keyword.length();
			keyword = StringUtils.stripBefore(keyword);
			if (keyword.length() == length)
				noSpaceKeywords.add(keyword);
			else
				spaceKeywords.add(keyword);
		}

		// Initialise the regular expression for a field ID
		String fieldIdRegex = FIELD_ID_REGEX_FRAG1 + StringUtils.join(REGEX_ALTERNATION_CHAR, noSpaceKeywords)
										+ FIELD_ID_REGEX_FRAG2 + StringUtils.join(REGEX_ALTERNATION_CHAR, spaceKeywords)
										+ FIELD_ID_REGEX_FRAG3;

		// Create regular-expression patterns
		Pattern secFieldIdPattern = Pattern.compile(SEC_FIELD_ID_REGEX_FRAG1 + fieldIdRegex + SEC_FIELD_ID_REGEX_FRAG2);
		Pattern referencePattern = (referenceKeyword == null)
											? null
											: Pattern.compile(WHITESPACE_REGEX + RegexUtils.escape(referenceKeyword)
																	+ SPACE_REGEX + fieldIdRegex + CLUE_INDEX_REGEX);

		// Split the input text into lines and add an empty line
		List<String> lines = StringUtils.split(text, '\n');
		lines.add("");

		// Process the lines of input text
		List<Clue> clues = new ArrayList<>();
		Clue clue = null;
		StringBuilder buffer = new StringBuilder(256);
		String line = null;
		Matcher matcher = null;
		int clueFirstLineNum = 0;
		int lastNonEmptyLineNum = 0;
		int lineIndex = 0;
		int offset = 0;
		ParseState state = ParseState.START_OF_CLUE;
		while (state != ParseState.STOP)
		{
			switch (state)
			{
				case START_OF_CLUE:
				{
					// If there is more input text, get the next line ...
					if (lineIndex < lines.size())
					{
						line = lines.get(lineIndex++).replaceAll(WHITESPACE_REGEX, " ").trim();
						if (!line.isEmpty())
						{
							// Set the numbers of the last non-empty line and the first line of the clue for
							// use in error messages
							lastNonEmptyLineNum = lineIndex;
							clueFirstLineNum = lineIndex;

							// Put the line of input text in the buffer
							buffer.setLength(0);
							buffer.append(line);

							// Parse the primary field ID
							state = ParseState.PRIMARY_FIELD_ID;
						}
					}

					// ... otherwise, stop
					else
						state = ParseState.STOP;
					break;
				}

				case PRIMARY_FIELD_ID:
				{
					// Test for a field number at the start of the line
					matcher = NUMBER_PATTERN.matcher(buffer);
					if (!matcher.find())
						throw new ParseException(ErrorId.FIELD_NUMBER_EXPECTED, lineIndex, line);
					offset = matcher.end();

					// If end of line, get some more input text ...
					if (offset == buffer.length())
					{
						// Get the next line of input text
						if (lineIndex < lines.size())
							line = lines.get(lineIndex++).replaceAll(WHITESPACE_REGEX, " ").trim();
						if (StringUtils.isNullOrEmpty(line))
							throw new ParseException(ErrorId.CLUE_TEXT_EXPECTED, lastNonEmptyLineNum,
													 lines.get(lastNonEmptyLineNum - 1));

						// Set the number of the last non-empty line for use in error messages
						lastNonEmptyLineNum = lineIndex;

						// Append the line to the contents of the buffer
						buffer.append(' ');
						buffer.append(line);
					}

					// ... otherwise, initialise a clue and proceed
					else
					{
						// Initialise a clue with the primary field ID
						clue = new Clue(matcher.group(1));
						for (Grid.Field.Id id : clue.fieldIds)
						{
							if (id.number <= 0)
								throw new ParseException(ErrorId.INVALID_FIELD_NUMBER, lineIndex, line,
														 Integer.toString(id.number));
						}

						// Test for a reference
						state = ((referencePattern != null) &&
								 matcher.usePattern(referencePattern).region(offset, buffer.length()).matches())
												? ParseState.REFERENCE
												: ParseState.SECONDARY_FIELD_IDS;
					}
					break;
				}

				case SECONDARY_FIELD_IDS:
				{
					// If more secondary field IDs are expected for the current clue, get the next line
					// of input text ...
					if (line == null)
					{
						// Get the next line of input text
						if (lineIndex < lines.size())
							line = lines.get(lineIndex++).replaceAll(WHITESPACE_REGEX, " ").trim();
						if (StringUtils.isNullOrEmpty(line))
							throw new ParseException(ErrorId.CLUE_TEXT_EXPECTED, lastNonEmptyLineNum,
													 lines.get(lastNonEmptyLineNum - 1));

						// Set the number of the last non-empty line for use in error messages
						lastNonEmptyLineNum = lineIndex;

						// Append the line to the contents of the buffer
						buffer.append(line);

						// Update matcher
						matcher = secFieldIdPattern.matcher(buffer);
					}

					// ... otherwise, update the matcher
					else
						matcher.usePattern(secFieldIdPattern);

					// Find secondary field IDs
					while (matcher.region(offset, buffer.length()).lookingAt())
					{
						offset = matcher.end();
						String directionStr = matcher.group(2);
						if (directionStr == null)
							directionStr = matcher.group(3);
						clue.fieldIds.add(new Grid.Field.Id(matcher.group(1), directionStr));
					}

					// Replace the contents of buffer with the remainder of the line
					String str = buffer.substring(offset).trim();
					buffer.setLength(0);
					buffer.append(str);
					offset = 0;

					// If more secondary field IDs are expected, get another line of input text next time around ...
					if (str.equals(","))
						line = null;

					// .. otherwise, process the text of the clue
					else
					{
						if (str.isEmpty())
							line = null;
						state = ParseState.TEXT;
					}
					break;
				}

				case REFERENCE:
				{
					// Set the referent ID of the clue
					String directionStr = matcher.group(2);
					if (directionStr == null)
						directionStr = matcher.group(3);
					clue.referentId = new Id(matcher.group(1), directionStr, matcher.group(4));

					// Add the clue to the list
					clues.add(clue);

					// Move on to the next clue
					state = ParseState.START_OF_CLUE;
					break;
				}

				case TEXT:
				{
					// If more text is expected for the current clue, get the next line of input text ...
					if (line == null)
					{
						// Get the next line of input text
						if (lineIndex < lines.size())
							line = lines.get(lineIndex++).replaceAll(WHITESPACE_REGEX, " ").trim();
						if (StringUtils.isNullOrEmpty(line))
						{
							AppException.IId id = (answerLengthParser == null) ? ErrorId.CLUE_TEXT_EXPECTED
																			   : ErrorId.ANSWER_LENGTH_EXPECTED;
							throw new ParseException(id, lastNonEmptyLineNum, lines.get(lastNonEmptyLineNum - 1));
						}

						// Set the number of the last non-empty line for use in error messages
						lastNonEmptyLineNum = lineIndex;

						// Append line to contents of buffer
						buffer.append(line);

						// Update matcher
						matcher = answerLengthParser.pattern.matcher(buffer);
					}

					// ... otherwise, test for the end of the clue
					else
					{
						// If there is no answer-length parser, assume the end of the clue ...
						if (answerLengthParser == null)
							state = ParseState.END_OF_CLUE;

						// ... otherwise, test for an answer-length expression
						else
						{
							if (matcher.usePattern(answerLengthParser.pattern).find(offset))
								state = ParseState.ANSWER_LENGTH;
							else
							{
								if (!line.endsWith("-"))
									buffer.append(' ');
								line = null;
							}
						}
					}
					break;
				}

				case ANSWER_LENGTH:
				{
					// Set the answer length of the clue
					clue.answerLength = getAnswerLength(matcher, answerLengthParser);

					// Finish processing the clue
					state = ParseState.END_OF_CLUE;
					break;
				}

				case END_OF_CLUE:
				{
					// Apply substitutions to the clue text
					String clueText = buffer.toString();
					for (Substitution substitution : substitutions)
						clueText = substitution.apply(clueText);

					// Set the text of the clue
					try
					{
						clue.text = new StyledText(clueText);
					}
					catch (StyledText.ParseException e)
					{
						throw new ParseException(e, clueFirstLineNum, buffer.toString());
					}

					// Add the clue to the list
					clues.add(clue);

					// Move on to the next clue
					state = ParseState.START_OF_CLUE;
					break;
				}

				case STOP:
					// do nothing
					break;
			}
		}

		return clues;
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : overriding methods
////////////////////////////////////////////////////////////////////////

	@Override
	public boolean equals(Object obj)
	{
		if (obj instanceof Clue)
		{
			Clue other = (Clue)obj;
			return (fieldIds.equals(other.fieldIds) && (index == other.index) &&
					((referentId == null) ? (other.referentId == null) : referentId.equals(other.referentId)) &&
					(answerLength == other.answerLength) &&
					((text == null) ? (other.text == null) : text.equals(other.text)));
		}
		return false;
	}

	//------------------------------------------------------------------

	@Override
	public int hashCode()
	{
		int code = fieldIds.hashCode();
		code = code * 31 + ((referentId == null) ? 0 : referentId.hashCode());
		code = code * 31 + answerLength;
		code = code * 31 + ((text == null) ? 0 : text.hashCode());
		return code;
	}

	//------------------------------------------------------------------

	@Override
	public String toString()
	{
		StringBuilder buffer = new StringBuilder(128);
		for (int i = 0; i < fieldIds.size(); i++)
		{
			if (i > 0)
				buffer.append(", ");
			buffer.append(fieldIds.get(i));
		}
		buffer.append(' ');
		if (referentId == null)
			buffer.append(text);
		else
		{
			buffer.append(AppConfig.INSTANCE.getClueReferenceKeyword());
			buffer.append(' ');
			buffer.append(referentId);
		}
		return buffer.toString();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	public int getNumFields()
	{
		return fieldIds.size();
	}

	//------------------------------------------------------------------

	public Grid.Field.Id getFieldId()
	{
		return fieldIds.get(0);
	}

	//------------------------------------------------------------------

	public Grid.Field.Id getFieldId(int index)
	{
		return fieldIds.get(index);
	}

	//------------------------------------------------------------------

	public boolean isReference()
	{
		return (referentId != null);
	}

	//------------------------------------------------------------------

	public boolean hasText()
	{
		return (text != null);
	}

	//------------------------------------------------------------------

	public boolean isEmpty()
	{
		return ((referentId == null) && (text == null));
	}

	//------------------------------------------------------------------

	public Id getId()
	{
		return new Id(fieldIds.get(0), index);
	}

	//------------------------------------------------------------------

	public Id getReferentId()
	{
		return referentId;
	}

	//------------------------------------------------------------------

	public int getAnswerLength()
	{
		return answerLength;
	}

	//------------------------------------------------------------------

	public StyledText getText()
	{
		return text;
	}

	//------------------------------------------------------------------

	public void setIndex(int index)
	{
		this.index = index;
	}

	//------------------------------------------------------------------

	public boolean isSecondaryId(Grid.Field.Id fieldId)
	{
		for (int i = 1; i < fieldIds.size(); i++)
		{
			if (fieldIds.get(i).equals(fieldId))
				return true;
		}
		return false;
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Static initialiser
////////////////////////////////////////////////////////////////////////

	static
	{
		ID_COMPARATOR = (clue1, clue2) ->
		{
			Grid.Field.Id id1 = clue1.fieldIds.get(0);
			Grid.Field.Id id2 = clue2.fieldIds.get(0);
			int result = Integer.compare(id1.direction.ordinal(), id2.direction.ordinal());
			if (result == 0)
				result = Integer.compare(id1.number, id2.number);
			if (result == 0)
				result = Integer.compare(clue1.index, clue2.index);
			return result;
		};
	}

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private	List<Grid.Field.Id>	fieldIds;
	private	int					index;
	private	Id					referentId;
	private	int					answerLength;
	private	StyledText			text;

}

//----------------------------------------------------------------------
