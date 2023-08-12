/*====================================================================*\

Apostrophe.java

Enumeration: apostrophe.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.common.xml;

//----------------------------------------------------------------------


// IMPORTS


import java.util.stream.Stream;

import uk.blankaspect.common.misc.IStringKeyed;

//----------------------------------------------------------------------


// ENUMERATION: APOSTROPHE


public enum Apostrophe
	implements IStringKeyed
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	CHARACTER
	(
		"character",
		"'",
		null
	),

	XML_ENTITY
	(
		"xmlEntity",
		XmlConstants.Entity.APOS,
		XmlConstants.EntityName.APOS
	),

	NUMERIC_ENTITY
	(
		"numericEntity",
		"&#39;",
		"#39"
	);

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private	String	key;
	private	String	text;
	private	String	entityName;

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	private Apostrophe(String key,
					   String text,
					   String entityName)
	{
		this.key = key;
		this.text = text;
		this.entityName = entityName;
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	public static Apostrophe forKey(String key)
	{
		return Stream.of(values())
						.filter(value -> value.key.equals(key))
						.findFirst()
						.orElse(null);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : IStringKeyed interface
////////////////////////////////////////////////////////////////////////

	@Override
	public String getKey()
	{
		return key;
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : overriding methods
////////////////////////////////////////////////////////////////////////

	@Override
	public String toString()
	{
		return text;
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	public String getEntityName()
	{
		return entityName;
	}

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------
