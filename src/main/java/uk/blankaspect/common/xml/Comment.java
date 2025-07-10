/*====================================================================*\

Comment.java

Comment class.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.common.xml;

//----------------------------------------------------------------------


// IMPORTS


import java.io.IOException;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import uk.blankaspect.common.exception.AppException;

//----------------------------------------------------------------------


// COMMENT CLASS


public class Comment
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	private interface ElementName
	{
		String	COMMENT	= "comment";
	}

	private interface AttrName
	{
		String	INDENT	= "indent";
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

		NO_ATTRIBUTE
		("The required attribute is missing."),

		INVALID_ATTRIBUTE
		("The attribute is invalid."),

		ATTRIBUTE_OUT_OF_BOUNDS
		("The attribute value is out of bounds.");

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

	public Comment()
	{
		text = "";
	}

	//------------------------------------------------------------------

	public Comment(String text)
	{
		setText(text);
	}

	//------------------------------------------------------------------

	public Comment(Element element)
		throws XmlParseException
	{
		String elementPath = XmlUtils.getElementPath(element);

		// Attribute: indent
		String attrName = AttrName.INDENT;
		String attrKey = XmlUtils.appendAttributeName(elementPath, attrName);
		String attrValue = XmlUtils.getAttribute(element, attrName);
		int indent = 0;
		if (attrValue != null)
		{
			try
			{
				indent = Integer.parseInt(attrValue);
				if (indent < 0)
					throw new XmlParseException(ErrorId.ATTRIBUTE_OUT_OF_BOUNDS, attrKey, attrValue);
			}
			catch (NumberFormatException e)
			{
				throw new XmlParseException(ErrorId.INVALID_ATTRIBUTE, attrKey, attrValue);
			}
		}

		// Merge text nodes
		StringBuilder buffer = new StringBuilder();
		NodeList nodes = element.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++)
		{
			if (nodes.item(i).getNodeType() == Node.TEXT_NODE)
				buffer.append(nodes.item(i).getNodeValue());
		}
		String str = buffer.toString();

		// Remove indent from each line of comment
		buffer.setLength(0);
		int index = 0;
		while (index < str.length())
		{
			int startIndex = index;
			index = str.indexOf('\n', index);
			if (index < 0)
				index = str.length();

			int indentEndIndex = Math.min(startIndex + indent, index);
			while (startIndex < indentEndIndex)
			{
				if (str.charAt(startIndex) != ' ')
					break;
				++startIndex;
			}

			if (index > startIndex)
				buffer.append(str.substring(startIndex, index));
			buffer.append('\n');
			++index;
		}

		text = buffer.toString().strip();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	public static String getElementName()
	{
		return ElementName.COMMENT;
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	public String getText()
	{
		return text;
	}

	//------------------------------------------------------------------

	public void setText(String text)
	{
		this.text = text.strip();
	}

	//------------------------------------------------------------------

	public boolean isEmpty()
	{
		return text.isEmpty();
	}

	//------------------------------------------------------------------

	public void write(XmlWriter writer,
					  int       elementIndent,
					  int       textIndent)
		throws IOException
	{
		AttributeList attributes = new AttributeList();
		attributes.add(AttrName.INDENT, textIndent);
		writer.writeElementStart(ElementName.COMMENT, attributes, elementIndent, true, true);

		int index = 0;
		while (index < text.length())
		{
			int startIndex = index;
			index = text.indexOf('\n', startIndex);
			if (index < 0)
				index = text.length();
			if (startIndex < index)
			{
				writer.writeSpaces(textIndent);
				writer.writeEscaped(text.substring(startIndex, index));
			}
			writer.writeEol();
			++index;
		}

		writer.writeElementEnd(ElementName.COMMENT, elementIndent);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private	String	text;

}

//----------------------------------------------------------------------
