/*====================================================================*\

ParameterSet.java

Class: parameter set.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.common.property;

//----------------------------------------------------------------------


// IMPORTS


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import uk.blankaspect.common.exception.AppException;

import uk.blankaspect.common.tuple.IStrKVPair;
import uk.blankaspect.common.tuple.StrKVPair;

import uk.blankaspect.common.xml.XmlParseException;
import uk.blankaspect.common.xml.XmlUtils;

//----------------------------------------------------------------------


// CLASS: PARAMETER SET


public abstract class ParameterSet
	implements Comparable<ParameterSet>, Property.ISource, Property.ITarget
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	protected static final	String	DEFAULT_SET_ELEMENT_NAME	= ElementName.PARAMETER_SET;

	private static final	String	PARAMETER_SET_STR	= "Parameter set";

	private interface ElementName
	{
		String	PARAMETER_SET	= "parameterSet";
		String	PROPERTY		= "property";
	}

	private interface AttrName
	{
		String	KEY		= "key";
		String	NAME	= "name";
		String	VALUE	= "value";
	}

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	protected	String				name;
	protected	Map<String, String>	properties;

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	public ParameterSet()
	{
		properties = new HashMap<>();
	}

	//------------------------------------------------------------------

	public ParameterSet(
		Element	element)
		throws XmlParseException
	{
		// Initialise instance variables
		this();

		// Parse name
		String elementPath = XmlUtils.getElementPath(element);
		String attrName = AttrName.NAME;
		String attrKey = XmlUtils.appendAttributeName(elementPath, attrName);
		String attrValue = XmlUtils.getAttribute(element, attrName);
		if (attrValue == null)
			throw new XmlParseException(ErrorId.NO_ATTRIBUTE, attrKey);
		name = attrValue;

		// Initialise properties
		NodeList nodes = element.getElementsByTagName(ElementName.PROPERTY);
		for (int i = 0; i < nodes.getLength(); i++)
		{
			Element propertyElement = (Element)nodes.item(i);
			elementPath = XmlUtils.getElementPath(propertyElement);

			// Attribute: key
			attrName = AttrName.KEY;
			attrKey = XmlUtils.appendAttributeName(elementPath, attrName);
			attrValue = XmlUtils.getAttribute(propertyElement, attrName);
			if (attrValue == null)
				throw new XmlParseException(ErrorId.NO_ATTRIBUTE, attrKey);
			String key = attrValue;

			// Attribute: value
			attrName = AttrName.VALUE;
			attrKey = XmlUtils.appendAttributeName(elementPath, attrName);
			attrValue = XmlUtils.getAttribute(propertyElement, attrName);
			if (attrValue == null)
				throw new XmlParseException(ErrorId.NO_ATTRIBUTE, attrKey);
			String value = attrValue;

			properties.put(key, value);
		}
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Abstract methods
////////////////////////////////////////////////////////////////////////

	public abstract ParameterSet create()
		throws AppException;

	//------------------------------------------------------------------

	protected abstract void getProperties(
		Property.ISource...	propertySources);

	//------------------------------------------------------------------

	protected abstract void putProperties(
		Property.ITarget	propertyTarget);

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : Comparable interface
////////////////////////////////////////////////////////////////////////

	@Override
	public int compareTo(
		ParameterSet	paramSet)
	{
		return ((name == null) ? (paramSet.name == null) ? 0 : -1
							   : (paramSet.name == null) ? 1 : name.compareTo(paramSet.name));
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : Property.ISource interface
////////////////////////////////////////////////////////////////////////

	@Override
	public String getSourceName()
	{
		return PARAMETER_SET_STR;
	}

	//------------------------------------------------------------------

	@Override
	public String getProperty(
		String	key)
	{
		if (key == null)
			throw new IllegalArgumentException();

		return properties.get(key);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : Property.ITarget interface
////////////////////////////////////////////////////////////////////////

	@Override
	public boolean putProperty(
		String	key,
		String	value)
	{
		if (key == null)
			throw new IllegalArgumentException();

		boolean valueSet = false;
		if (value != null)
		{
			properties.put(key, value);
			valueSet = true;
		}
		return valueSet;
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	public String getName()
	{
		return name;
	}

	//------------------------------------------------------------------

	public void setName(
		String	name)
	{
		this.name = name;
	}

	//------------------------------------------------------------------

	public List<StrKVPair> toList()
	{
		List<StrKVPair> entries = new ArrayList<>();
		for (String key : properties.keySet())
			entries.add(StrKVPair.of(key, properties.get(key)));
		entries.sort(IStrKVPair.KEY_COMPARATOR);
		return entries;
	}

	//------------------------------------------------------------------

	public Element createElement()
		throws AppException
	{
		return createElement(XmlUtils.createDocument());
	}

	//------------------------------------------------------------------

	public Element createElement(
		Document	document)
	{
		properties.clear();
		putProperties(this);

		Element setElement = document.createElement(getSetElementName());
		setElement.setAttribute(AttrName.NAME, name);
		for (StrKVPair param : toList())
		{
			Element element = document.createElement(ElementName.PROPERTY);
			element.setAttribute(AttrName.KEY, param.key());
			element.setAttribute(AttrName.VALUE, param.value());
			setElement.appendChild(element);
		}
		return setElement;
	}

	//------------------------------------------------------------------

	protected String getSetElementName()
	{
		return DEFAULT_SET_ELEMENT_NAME;
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Enumerated types
////////////////////////////////////////////////////////////////////////


	// ENUMERATION: ERROR IDENTIFIERS


	private enum ErrorId
		implements AppException.IId
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		NO_ATTRIBUTE
		("The required attribute is missing.");

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	String	message;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private ErrorId(
			String	message)
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

}

//----------------------------------------------------------------------
