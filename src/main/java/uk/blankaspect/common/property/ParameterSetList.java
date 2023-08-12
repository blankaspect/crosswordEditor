/*====================================================================*\

ParameterSetList.java

Parameter-set list class.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.common.property;

//----------------------------------------------------------------------


// IMPORTS


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.nio.charset.StandardCharsets;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import uk.blankaspect.common.exception.AppException;
import uk.blankaspect.common.exception.FileException;
import uk.blankaspect.common.exception.TempFileException;

import uk.blankaspect.common.filesystem.FilenameUtils;

import uk.blankaspect.common.list.IListModel;

import uk.blankaspect.common.xml.AttributeList;
import uk.blankaspect.common.xml.Comment;
import uk.blankaspect.common.xml.XmlConstants;
import uk.blankaspect.common.xml.XmlParseException;
import uk.blankaspect.common.xml.XmlUtils;
import uk.blankaspect.common.xml.XmlValidationException;
import uk.blankaspect.common.xml.XmlWriter;

//----------------------------------------------------------------------


// PARAMETER-SET LIST CLASS


public abstract class ParameterSetList<E extends ParameterSet>
	implements IListModel<E>
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	private static final	String	NAMESPACE_NAME			= "http://ns.blankaspect.uk/"
																+ ElementName.PARAMETER_SET_LIST + "-1";
	private static final	String	NAMESPACE_NAME_REGEX	= "http://ns\\.[a-z.]+/"
																+ ElementName.PARAMETER_SET_LIST + "-1";

	private static final	String	DTD_SUFFIX	= ".dtd";

	private static final	String	XML_VERSION_STR		= "1.0";

	private interface ElementName
	{
		String	PARAMETER_SET_LIST	= "parameterSetList";
	}

	private interface AttrName
	{
		String	APPLICATION	= "application";
		String	VERSION		= "version";
		String	XMLNS		= "xmlns";
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

		FILE_DOES_NOT_EXIST
		("The file does not exist."),

		FAILED_TO_OPEN_FILE
		("Failed to open the file."),

		FAILED_TO_CLOSE_FILE
		("Failed to close the file."),

		ERROR_READING_FILE
		("An error occurred when reading the file."),

		ERROR_WRITING_FILE
		("An error occurred when writing the file."),

		FILE_ACCESS_NOT_PERMITTED
		("Access to the file was not permitted."),

		FAILED_TO_CREATE_DIRECTORY
		("Failed to create the directory."),

		FAILED_TO_CREATE_TEMPORARY_FILE
		("Failed to create a temporary file."),

		FAILED_TO_DELETE_FILE
		("Failed to delete the existing file."),

		FAILED_TO_RENAME_FILE
		("Failed to rename the temporary file."),

		NOT_ENOUGH_MEMORY
		("There was not enough memory to read the file."),

		INVALID_DOCUMENT
		("The file is not a valid document."),

		UNEXPECTED_DOCUMENT_FORMAT
		("The document does not have the expected format."),

		UNEXPECTED_NAMESPACE_NAME
		("The document does not have the expected namespace name."),

		UNEXPECTED_APPLICATION_KEY
		("The document does not have the expected application key."),

		UNSUPPORTED_DOCUMENT_VERSION
		("The version of the document (%1) is not supported."),

		NO_ATTRIBUTE
		("The required attribute is missing."),

		INVALID_ATTRIBUTE
		("The attribute is invalid."),

		MULTIPLE_ELEMENTS
		("The file contains more than one element with this name.");

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

	protected ParameterSetList(String applicationKey)
	{
		this.applicationKey = applicationKey;
		comment = new Comment();
		elements = new ArrayList<>();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Abstract methods
////////////////////////////////////////////////////////////////////////

	public abstract int getVersion();

	//------------------------------------------------------------------

	protected abstract boolean isSupportedVersion(int version);

	//------------------------------------------------------------------

	protected abstract E createElement(Element element)
		throws XmlParseException;

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : IListModel interface
////////////////////////////////////////////////////////////////////////

	@Override
	public int getNumElements()
	{
		return elements.size();
	}

	//------------------------------------------------------------------

	@Override
	public E getElement(int index)
	{
		return elements.get(index);
	}

	//------------------------------------------------------------------

	@Override
	public String getElementText(int index)
	{
		return elements.get(index).getName();
	}

	//------------------------------------------------------------------

	@Override
	public void setElement(int index,
						   E   element)
	{
		elements.set(index, element);
	}

	//------------------------------------------------------------------

	@Override
	public void addElement(int index,
						   E   element)
	{
		if (index < elements.size())
			elements.add(index, element);
		else
			elements.add(element);
	}

	//------------------------------------------------------------------

	@Override
	public E removeElement(int index)
	{
		return elements.remove(index);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	public String getApplicationKey()
	{
		return applicationKey;
	}

	//------------------------------------------------------------------

	public String getComment()
	{
		return comment.getText();
	}

	//------------------------------------------------------------------

	public boolean isEmpty()
	{
		return elements.isEmpty();
	}

	//------------------------------------------------------------------

	public List<E> getElements()
	{
		return elements;
	}

	//------------------------------------------------------------------

	public List<String> getElementNames()
	{
		List<String> names = new ArrayList<>();
		for (E element : elements)
			names.add(element.getName());
		return names;
	}

	//------------------------------------------------------------------

	public void setComment(String text)
	{
		comment.setText(text);
	}

	//------------------------------------------------------------------

	public void setElements(List<E> elements)
	{
		this.elements.clear();
		this.elements.addAll(elements);
	}

	//------------------------------------------------------------------

	public int find(String name)
	{
		for (int i = 0; i < elements.size(); i++)
		{
			if (elements.get(i).getName().equals(name))
				return i;
		}
		return -1;
	}

	//------------------------------------------------------------------

	public void read(File file,
					 File dtdDirectory)
		throws AppException
	{
		FileInputStream inStream = null;
		try
		{
			// Test for file
			try
			{
				if (!file.isFile())
					throw new FileException(ErrorId.FILE_DOES_NOT_EXIST, file);
			}
			catch (SecurityException e)
			{
				throw new FileException(ErrorId.FILE_ACCESS_NOT_PERMITTED, file);
			}

			// Open input stream on file
			try
			{
				inStream = new FileInputStream(file);
			}
			catch (FileNotFoundException e)
			{
				throw new FileException(ErrorId.FAILED_TO_OPEN_FILE, file);
			}
			catch (SecurityException e)
			{
				throw new FileException(ErrorId.FILE_ACCESS_NOT_PERMITTED, file);
			}

			// Read and parse file
			try
			{
				parse(file, inStream, dtdDirectory);
			}
			catch (OutOfMemoryError e)
			{
				throw new FileException(ErrorId.NOT_ENOUGH_MEMORY, file);
			}

			// Close input stream
			try
			{
				inStream.close();
				inStream = null;
			}
			catch (IOException e)
			{
				throw new FileException(ErrorId.FAILED_TO_CLOSE_FILE, file);
			}
		}
		catch (AppException e)
		{
			// Close input stream
			try
			{
				if (inStream != null)
					inStream.close();
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

	public void write(File    file,
					  boolean writeDtd)
		throws AppException
	{
		File tempFile = null;
		XmlWriter writer = null;
		boolean oldFileDeleted = false;
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
				writer.writeEol();

				// Write DTD
				if (writeDtd)
				{
					writer.writeDocumentType(ElementName.PARAMETER_SET_LIST,
											 ElementName.PARAMETER_SET_LIST + DTD_SUFFIX, null);
					writer.writeEol();
				}

				// Write root element start tag
				AttributeList attributes = new AttributeList();
				attributes.add(AttrName.XMLNS, NAMESPACE_NAME);
				attributes.add(AttrName.VERSION, getVersion());
				attributes.add(AttrName.APPLICATION, applicationKey);
				writer.writeElementStart(ElementName.PARAMETER_SET_LIST, attributes, 0, true, true);

				// Write comment element
				if (!comment.isEmpty())
					comment.write(writer, XmlWriter.INDENT_INCREMENT, 2 * XmlWriter.INDENT_INCREMENT);

				// Write parameter sets
				Document document = XmlUtils.createDocument();
				for (int i = 0; i < elements.size(); i++)
				{
					Element element = elements.get(i).createElement(document);
					if ((i > 0) || !comment.isEmpty())
						writer.writeEol();
					writer.writeElement(element, XmlWriter.INDENT_INCREMENT, true);
				}

				// Write root element end tag
				writer.writeElementEnd(ElementName.PARAMETER_SET_LIST, 0);
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

			// Delete any existing file
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

	private void parse(File            file,
					   FileInputStream inStream,
					   File            dtdDirectory)
		throws AppException
	{
		// Create DOM document from file
		Document document = XmlUtils.createDocument(inStream, (dtdDirectory == null) ? null : dtdDirectory.toURI(),
													dtdDirectory != null);
		if (!XmlUtils.getErrorHandler().isEmpty())
			throw new XmlValidationException(ErrorId.INVALID_DOCUMENT, file,
											 XmlUtils.getErrorHandler().getErrorStrings());

		// Test document format
		Element element = document.getDocumentElement();
		if (!element.getNodeName().equals(ElementName.PARAMETER_SET_LIST))
			throw new FileException(ErrorId.UNEXPECTED_DOCUMENT_FORMAT, file);
		String elementPath = ElementName.PARAMETER_SET_LIST;

		// Attribute: namespace
		String attrName = AttrName.XMLNS;
		String attrKey = XmlUtils.appendAttributeName(elementPath, attrName);
		String attrValue = XmlUtils.getAttribute(element, attrName);
		if (attrValue == null)
			throw new XmlParseException(ErrorId.NO_ATTRIBUTE, file, attrKey);
		if (!attrValue.matches(NAMESPACE_NAME_REGEX))
			throw new FileException(ErrorId.UNEXPECTED_NAMESPACE_NAME, file);

		// Attribute: version
		attrName = AttrName.VERSION;
		attrKey = XmlUtils.appendAttributeName(elementPath, attrName);
		attrValue = XmlUtils.getAttribute(element, attrName);
		if (attrValue == null)
			throw new XmlParseException(ErrorId.NO_ATTRIBUTE, file, attrKey);
		try
		{
			int version = Integer.parseInt(attrValue);
			if (version < 0)
				throw new NumberFormatException();
			if (!isSupportedVersion(version))
				throw new FileException(ErrorId.UNSUPPORTED_DOCUMENT_VERSION, file, attrValue);
		}
		catch (NumberFormatException e)
		{
			throw new XmlParseException(ErrorId.INVALID_ATTRIBUTE, file, attrKey, attrValue);
		}

		// Attribute: application
		attrName = AttrName.APPLICATION;
		attrKey = XmlUtils.appendAttributeName(elementPath, attrName);
		attrValue = XmlUtils.getAttribute(element, attrName);
		if (attrValue == null)
			throw new XmlParseException(ErrorId.NO_ATTRIBUTE, file, attrKey);
		if (!attrValue.equals(applicationKey))
			throw new XmlParseException(ErrorId.UNEXPECTED_APPLICATION_KEY, file, attrKey, attrValue);

		// Parse comment and parameter set elements
		try
		{
			elements.clear();
			boolean hasComment = false;
			NodeList childNodes = element.getChildNodes();
			for (int i = 0; i < childNodes.getLength(); i++)
			{
				Node node = childNodes.item(i);
				if (node.getNodeType() == Node.ELEMENT_NODE)
				{
					element = (Element)node;
					elementPath = XmlUtils.getElementPath(element);
					String elementName = node.getNodeName();

					if (elementName.equals(Comment.getElementName()))
					{
						if (hasComment)
							throw new XmlParseException(ErrorId.MULTIPLE_ELEMENTS, elementPath);
						hasComment = true;
						comment = new Comment(element);
					}

					else if (elementName.equals(ParameterSet.DEFAULT_SET_ELEMENT_NAME))
						elements.add(createElement((Element)node));
				}
			}
		}
		catch (XmlParseException e)
		{
			throw new XmlParseException(e, file);
		}
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private	String	applicationKey;
	private	Comment	comment;
	private	List<E>	elements;

}

//----------------------------------------------------------------------
