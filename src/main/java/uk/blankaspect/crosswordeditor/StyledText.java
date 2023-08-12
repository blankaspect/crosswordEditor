/*====================================================================*\

StyledText.java

Styled text class.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.crosswordeditor;

//----------------------------------------------------------------------


// IMPORTS


import java.io.CharArrayWriter;
import java.io.IOException;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.EnumSet;
import java.util.List;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.StyleConstants;

import uk.blankaspect.common.exception.AppException;

import uk.blankaspect.common.misc.IStringKeyed;

import uk.blankaspect.common.xml.Attribute;
import uk.blankaspect.common.xml.XhtmlUtils;
import uk.blankaspect.common.xml.XmlWriter;

//----------------------------------------------------------------------


// STYLED TEXT CLASS


class StyledText
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	public static final		String	STYLE_PREFIX	= "{@";
	public static final		String	STYLE_SUFFIX	= "}";

	private static final	String	STYLE_REGEX		= "(\\\\\\\\|(?<!\\\\)\\" + STYLE_PREFIX
															+ "([a-z]+).|(?<!\\\\)\\" + STYLE_SUFFIX + ")";

////////////////////////////////////////////////////////////////////////
//  Enumerated types
////////////////////////////////////////////////////////////////////////


	// STYLE ATTRIBUTES


	public enum StyleAttr
		implements IStringKeyed
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		BOLD
		(
			'b',
			"Bold",
			StyleConstants.Bold,
			HtmlConstants.ElementName.B
		),

		ITALIC
		(
			'i',
			"Italic",
			StyleConstants.Italic,
			HtmlConstants.ElementName.I
		),

		SUPERSCRIPT
		(
			'h',
			"Superscript",
			StyleConstants.Superscript,
			HtmlConstants.ElementName.SUP
		),

		SUBSCRIPT
		(
			'l',
			"Subscript",
			StyleConstants.Subscript,
			HtmlConstants.ElementName.SUB
		),

		UNDERLINE
		(
			'u',
			"Underline",
			StyleConstants.Underline,
			HtmlConstants.ElementName.SPAN,
			HtmlConstants.Class.UNDERLINE
		),

		STRIKETHROUGH
		(
			's',
			"Strikethrough",
			StyleConstants.StrikeThrough,
			HtmlConstants.ElementName.SPAN,
			HtmlConstants.Class.STRIKE
		);

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private StyleAttr(char   key,
						  String text,
						  Object attribute,
						  String elementName)
		{
			this.key = key;
			this.text = text;
			this.attribute = attribute;
			this.elementName = elementName;
		}

		//--------------------------------------------------------------

		private StyleAttr(char   key,
						  String text,
						  Object attribute,
						  String elementName,
						  String className)
		{
			this(key, text, attribute, elementName);
			classAttrList = Collections.singletonList(new Attribute(HtmlConstants.AttrName.CLASS, className));
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Class methods
	////////////////////////////////////////////////////////////////////

		public static StyleAttr forKey(char key)
		{
			for (StyleAttr value : values())
			{
				if (value.key == key)
					return value;
			}
			return null;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : IStringKeyed interface
	////////////////////////////////////////////////////////////////////

		@Override
		public String getKey()
		{
			return Character.toString(key);
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

		public char getKeyChar()
		{
			return key;
		}

		//--------------------------------------------------------------

		public void apply(MutableAttributeSet attributes)
		{
			attributes.addAttribute(attribute, Boolean.TRUE);
		}

		//--------------------------------------------------------------

		private void writeOn(XmlWriter writer)
			throws IOException
		{
			if (classAttrList == null)
				writer.writeElementStart(elementName, 0, false);
			else
				writer.writeElementStart(elementName, classAttrList, 0, false, false);
		}

		//--------------------------------------------------------------

		private void writeOff(XmlWriter writer)
			throws IOException
		{
			writer.writeEndTag(elementName);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	char			key;
		private	String			text;
		private	Object			attribute;
		private	String			elementName;
		private	List<Attribute>	classAttrList;

	}

	//==================================================================


	// ERROR IDENTIFIERS


	private enum ErrorId
		implements AppException.IId
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		NO_MATCHING_CLOSING_BRACE
		("The start tag does not have a matching '}'."),

		NO_MATCHING_START_TAG
		("The '}' does not have a matching start tag."),

		UNRECOGNISED_ATTRIBUTE
		("The style attribute '%1' is not recognised."),

		INCONSISTENT_ATTRIBUTES
		("A group of style attributes may not contain both '" + StyleAttr.SUPERSCRIPT.key + "' and '" +
			StyleAttr.SUBSCRIPT.key + "'.");

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

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	String	message;

	}

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Member classes : non-inner classes
////////////////////////////////////////////////////////////////////////


	// PARSE EXCEPTION CLASS


	public static class ParseException
		extends AppException
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		private static final	String	INDEX_STR		= "Index ";
		private static final	String	INDICATOR_STR	= "^";

		private static final	String	SEPARATOR	= ": ";

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private ParseException(AppException.IId id,
							   String           text,
							   int              index)
		{
			super(id);
			this.text = text;
			this.index = index;
		}

		//--------------------------------------------------------------

		private ParseException(AppException.IId id,
							   String           text,
							   int              index,
							   CharSequence...  replacements)
		{
			super(id, replacements);
			this.text = text;
			this.index = index;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		protected String getPrefix()
		{
			return (INDEX_STR + index + SEPARATOR);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		public String getText()
		{
			return text;
		}

		//--------------------------------------------------------------

		public int getIndex()
		{
			return index;
		}

		//--------------------------------------------------------------

		public String getIndicatorString()
		{
			return (index == 0) ? INDICATOR_STR : " ".repeat(index) + INDICATOR_STR;
		}

		//--------------------------------------------------------------

		public String getMessageString()
		{
			return toString().split(SEPARATOR)[1];
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	String	text;
		private	int		index;

	}

	//==================================================================


	// SPAN CLASS


	public static class Span
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private Span(String             text,
					 int                startIndex,
					 int                endIndex,
					 EnumSet<StyleAttr> attrs)
		{
			this.text = text.substring(startIndex, endIndex).replaceAll("\\\\(.)", "$1");
			this.attrs = (attrs == null) ? EnumSet.noneOf(StyleAttr.class) : attrs.clone();
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		public boolean equals(Object obj)
		{
			if (obj instanceof Span)
			{
				Span other = (Span)obj;
				return text.equals(other.text) && attrs.equals(other.attrs);
			}
			return false;
		}

		//--------------------------------------------------------------

		@Override
		public int hashCode()
		{
			return (text.hashCode() * 31 + attrs.hashCode());
		}

		//--------------------------------------------------------------

		@Override
		public String toString()
		{
			StringBuilder buffer = new StringBuilder(128);
			if (!attrs.isEmpty())
			{
				buffer.append(STYLE_PREFIX);
				for (StyleAttr attr : attrs)
					buffer.append(attr.key);
				buffer.append(' ');
			}
			if (text != null)
				buffer.append(text.replace("{", "\\{").replace("}", "\\}"));
			if (!attrs.isEmpty())
				buffer.append(STYLE_SUFFIX);
			return buffer.toString();
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		public String getText()
		{
			return text;
		}

		//--------------------------------------------------------------

		public String getAttributeKey()
		{
			StringBuilder buffer = new StringBuilder();
			for (StyleAttr attr : attrs)
				buffer.append(attr.key);
			return buffer.toString();
		}

		//--------------------------------------------------------------

		public MutableAttributeSet setAttributes(MutableAttributeSet attributes)
		{
			for (StyleAttr attr : attrs)
				attr.apply(attributes);
			return attributes;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	String				text;
		private	EnumSet<StyleAttr>	attrs;

	}

	//==================================================================


	// STYLE ATTRIBUTES CLASS


	private static class StyleAttrs
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private StyleAttrs(int                index,
						   EnumSet<StyleAttr> attrs)
		{
			this.index = index;
			this.attrs = attrs.clone();
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	int					index;
		private	EnumSet<StyleAttr>	attrs;

	}

	//==================================================================


	// STYLE ATTRIBUTE WEIGHT CLASS


	private static class AttrWeight
		implements Comparable<AttrWeight>
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private AttrWeight(StyleAttr attr)
		{
			this.attr = attr;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : Comparable interface
	////////////////////////////////////////////////////////////////////

		@Override
		public int compareTo(AttrWeight other)
		{
			return Integer.compare(other.weight, weight);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	StyleAttr	attr;
		private	int			weight;

	}

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	public StyledText()
	{
		spans = new ArrayList<>();
	}

	//------------------------------------------------------------------

	public StyledText(String text)
		throws ParseException
	{
		this();
		parse(text);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : overriding methods
////////////////////////////////////////////////////////////////////////

	@Override
	public boolean equals(Object obj)
	{
		return (obj instanceof StyledText) && spans.equals(((StyledText)obj).spans);
	}

	//------------------------------------------------------------------

	@Override
	public int hashCode()
	{
		return spans.hashCode();
	}

	//------------------------------------------------------------------

	@Override
	public String toString()
	{
		StringBuilder buffer = new StringBuilder(128);
		for (Span span : spans)
			buffer.append(span);
		return buffer.toString();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	public int getNumSpans()
	{
		return spans.size();
	}

	//------------------------------------------------------------------

	public Span getSpan(int index)
	{
		return spans.get(index);
	}

	//------------------------------------------------------------------

	public void parse(String text)
		throws ParseException
	{
		// Clear the list of spans
		spans.clear();

		// Create a regular-expression matcher
		Matcher matcher = Pattern.compile(STYLE_REGEX).matcher(text);

		// Create a list of spans from the text
		Deque<StyleAttrs> attrsStack = new ArrayDeque<>();
		EnumSet<StyleAttr> currentAttrs = EnumSet.noneOf(StyleAttr.class);
		int startIndex = 0;
		while (matcher.find())
		{
			// Set an index to the start of the matched text
			int index = matcher.start();

			// Process the matched text
			switch (matcher.group(1).charAt(0))
			{
				case '{':
				{
					// Add a new span to the list for the text before the current tag
					if (index > startIndex)
						spans.add(new Span(text, startIndex, index, currentAttrs));

					// Push the current attributes onto the stack
					attrsStack.addFirst(new StyleAttrs(index, currentAttrs));

					// Update the current attributes with the attributes from the current tag
					EnumSet<StyleAttr> attrs = EnumSet.noneOf(StyleAttr.class);
					String tag = matcher.group(2);
					for (int i = 0; i < tag.length(); i++)
					{
						StyleAttr attr = StyleAttr.forKey(tag.charAt(i));
						if (attr == null)
							throw new ParseException(ErrorId.UNRECOGNISED_ATTRIBUTE, text, matcher.start(2) + i,
													 Character.toString(tag.charAt(i)));
						attrs.add(attr);
						if (attrs.contains(StyleAttr.SUPERSCRIPT) &&
							 attrs.contains(StyleAttr.SUBSCRIPT))
							throw new ParseException(ErrorId.INCONSISTENT_ATTRIBUTES, text, matcher.start(2) + i);
					}
					if (attrs.contains(StyleAttr.SUPERSCRIPT) || attrs.contains(StyleAttr.SUBSCRIPT))
						currentAttrs.removeAll(EnumSet.of(StyleAttr.SUPERSCRIPT, StyleAttr.SUBSCRIPT));
					currentAttrs.addAll(attrs);

					// Update the start index
					startIndex = matcher.end();
					break;
				}

				case '}':
				{
					// Test for a matching start tag
					if (attrsStack.isEmpty())
						throw new ParseException(ErrorId.NO_MATCHING_START_TAG, text, index);

					// Add a new span to the list for the text in the current tag
					if (index > startIndex)
						spans.add(new Span(text, startIndex, index, currentAttrs));

					// Update the current attributes from the stack
					currentAttrs = attrsStack.removeFirst().attrs;

					// Update the start index
					startIndex = matcher.end();
					break;
				}
			}
		}

		// Test for an unmatched start tag
		if (!attrsStack.isEmpty())
			throw new ParseException(ErrorId.NO_MATCHING_CLOSING_BRACE, text, attrsStack.peekFirst().index);

		// Add a span for the remainder of the text
		int index = text.length();
		if (index > startIndex)
			spans.add(new Span(text, startIndex, index, null));
	}

	//------------------------------------------------------------------

	public String toHtml()
	{
		return toHtml(null);
	}

	//------------------------------------------------------------------

	public String toHtml(String lineBreak)
	{
		CharArrayWriter writer = new CharArrayWriter(1024);
		try
		{
			XmlWriter xmlWriter = new XmlWriter(writer);
			write(xmlWriter, lineBreak);
			xmlWriter.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return writer.toString();
	}

	//------------------------------------------------------------------

	public void write(XmlWriter writer)
		throws IOException
	{
		write(writer, null);
	}

	//------------------------------------------------------------------

	public void write(XmlWriter writer,
					  String    lineBreak)
		throws IOException
	{
		// Get ordered attributes
		AttrWeight[] orderedAttrs = getOrderedAttrs();

		// Write spans as HTML
		EnumSet<StyleAttr> currentAttrs = EnumSet.noneOf(StyleAttr.class);
		Deque<StyleAttr> attrStack = new ArrayDeque<>();
		for (Span span : spans)
		{
			// Get the intersection of the previous attributes and the attributes of the current span
			EnumSet<StyleAttr> commonAttrs = currentAttrs.clone();
			commonAttrs.retainAll(span.attrs);

			// Pop attributes off the stack until the attributes remaining on the stack are a subset of the intersection
			// of the previous attributes and the attributes of the current span.  Write the end tag of the HTML element
			// corresponding to each attribute that is popped from the stack.
			while (!attrStack.isEmpty() && !commonAttrs.containsAll(currentAttrs))
			{
				StyleAttr attr = attrStack.removeFirst();
				currentAttrs.remove(attr);
				attr.writeOff(writer);
			}

			// Iterate through the attributes in weighted order: push onto the stack each attribute of the current span
			// that isn't already on the stack, and write the start tag of the HTML element corresponding to the
			// attribute
			for (AttrWeight attrWeight : orderedAttrs)
			{
				StyleAttr attr = attrWeight.attr;
				if (span.attrs.contains(attr) && !currentAttrs.contains(attr))
				{
					attrStack.addFirst(attr);
					currentAttrs.add(attr);
					attr.writeOn(writer);
				}
			}

			// Replace characters in clue text with character entity references
			String text = XhtmlUtils.escape(span.text);

			// Replace line-break sequence
			if (lineBreak != null)
				text = text.replace(lineBreak, "<br/>");

			// Write the text of the span
			writer.write(text);
		}

		// Pop all attributes off the stack, and write the end tag of the HTML element corresponding to each one
		while (!attrStack.isEmpty())
			attrStack.removeFirst().writeOff(writer);
	}

	//------------------------------------------------------------------

	private AttrWeight[] getOrderedAttrs()
	{
		// Initialise the array of attribute-weight objects and the array of run lengths
		int numAttrs = StyleAttr.values().length;
		AttrWeight[] attrWeights = new AttrWeight[numAttrs];
		for (int i = 0; i < numAttrs; i++)
			attrWeights[i] = new AttrWeight(StyleAttr.values()[i]);
		int[] runLengths = new int[numAttrs];

		// Set the weight of each attribute.  The weight is the sum of the number the spans containing the attribute and
		// the square of each run length (ie, the number of contiguous spans containing the attribute).
		for (Span span : spans)
		{
			for (StyleAttr attr : StyleAttr.values())
			{
				int index = attr.ordinal();
				if (span.attrs.contains(attr))
					++runLengths[index];
				else
				{
					attrWeights[index].weight += runLengths[index] * (runLengths[index] + 1);
					runLengths[index] = 0;
				}
			}
		}

		// Update the attribute weights with any residual run lengths
		for (StyleAttr attr : StyleAttr.values())
		{
			int index = attr.ordinal();
			attrWeights[index].weight += runLengths[index] * (runLengths[index] + 1);
		}

		// Sort the attributes in decreasing order of weight
		Arrays.sort(attrWeights);

		// Return the ordered array
		return attrWeights;
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private	List<Span>	spans;

}

//----------------------------------------------------------------------
