/*====================================================================*\

CssRuleSet.java

Class: CSS rule set.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.common.css;

//----------------------------------------------------------------------


// IMPORTS


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import uk.blankaspect.common.exception.UnexpectedRuntimeException;

import uk.blankaspect.common.string.StringUtils;

import uk.blankaspect.common.tuple.IStrKVPair;

//----------------------------------------------------------------------


// CLASS: CSS RULE SET


public class CssRuleSet
	implements Cloneable
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	private static final	String	INDENT_STR	= " ".repeat(CssConstants.INDENT_INCREMENT);

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private	List<String>		selectors;
	private	Map<String, String>	properties;

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	private CssRuleSet()
	{
		// Initialise instance variables
		selectors = new ArrayList<>();
		properties = new LinkedHashMap<>();
	}

	//------------------------------------------------------------------

	private CssRuleSet(
		Collection<String>	selectors,
		Map<String, String>	properties)
	{
		// Initialise instance variables
		this.selectors = new ArrayList<>(selectors);
		this.properties = new LinkedHashMap<>();

		// Add properties to map
		if (!properties.isEmpty())
			this.properties.putAll(properties);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	public static Builder builder()
	{
		return new Builder();
	}

	//------------------------------------------------------------------

	public static CssRuleSet empty(
		String...	selectors)
	{
		// Validate argument
		if (selectors == null)
			throw new IllegalArgumentException("Null selectors");
		if (selectors.length == 0)
			throw new IllegalArgumentException("No selectors");

		// Create and return rule set
		return new CssRuleSet(Arrays.asList(selectors), Collections.emptyMap());
	}

	//------------------------------------------------------------------

	public static CssRuleSet empty(
		Collection<String>	selectors)
	{
		// Validate argument
		if (selectors == null)
			throw new IllegalArgumentException("Null selectors");
		if (selectors.isEmpty())
			throw new IllegalArgumentException("No selectors");

		// Create and return rule set
		return new CssRuleSet(selectors, Collections.emptyMap());
	}

	//------------------------------------------------------------------

	public static CssRuleSet of(
		String				selector,
		Map<String, String>	properties)
	{
		// Validate arguments
		if (selector == null)
			throw new IllegalArgumentException("Null selector");
		if (properties == null)
			throw new IllegalArgumentException("Null properties");
		if (properties.containsKey(null))
			throw new IllegalArgumentException("Null property name");
		if (properties.containsValue(null))
			throw new IllegalArgumentException("Null property value");

		// Create and return rule set
		return new CssRuleSet(List.of(selector), properties);
	}

	//------------------------------------------------------------------

	public static CssRuleSet of(
		Collection<String>	selectors,
		Map<String, String>	properties)
	{
		// Validate arguments
		if (selectors == null)
			throw new IllegalArgumentException("Null selectors");
		if (selectors.isEmpty())
			throw new IllegalArgumentException("No selectors");
		if (properties == null)
			throw new IllegalArgumentException("Null properties");
		if (properties.containsKey(null))
			throw new IllegalArgumentException("Null property name");
		if (properties.containsValue(null))
			throw new IllegalArgumentException("Null property value");

		// Create and return rule set
		return new CssRuleSet(selectors, properties);
	}

	//------------------------------------------------------------------

	public static CssRuleSet of(
		Map<String, String>	properties,
		String...			selectors)
	{
		// Validate arguments
		if (properties == null)
			throw new IllegalArgumentException("Null properties");
		if (properties.containsKey(null))
			throw new IllegalArgumentException("Null property name");
		if (properties.containsValue(null))
			throw new IllegalArgumentException("Null property value");
		if (selectors == null)
			throw new IllegalArgumentException("Null selectors");
		if (selectors.length == 0)
			throw new IllegalArgumentException("No selectors");

		// Create and return rule set
		return new CssRuleSet(Arrays.asList(selectors), properties);
	}

	//------------------------------------------------------------------

	public static CssRuleSet of(
		String								selector,
		Collection<? extends IStrKVPair>	properties)
	{
		// Validate arguments
		if (selector == null)
			throw new IllegalArgumentException("Null selector");

		// Create and return rule set
		return of(List.of(selector), properties);
	}

	//------------------------------------------------------------------

	public static CssRuleSet of(
		Collection<String>					selectors,
		Collection<? extends IStrKVPair>	properties)
	{
		// Validate arguments
		if (selectors == null)
			throw new IllegalArgumentException("Null selectors");
		if (selectors.isEmpty())
			throw new IllegalArgumentException("No selectors");
		if (properties == null)
			throw new IllegalArgumentException("Null properties");

		// Create rule set
		CssRuleSet ruleSet = new CssRuleSet(selectors, Collections.emptyMap());

		// Add properties to map
		for (IStrKVPair property : properties)
		{
			String key = property.key();
			if (key == null)
				throw new IllegalArgumentException("Null key: " + key);
			String value = property.value();
			if (value == null)
				throw new IllegalArgumentException("Null value for '" + key + "'");
			ruleSet.properties.put(key, value);
		}

		// Return rule set
		return ruleSet;
	}

	//------------------------------------------------------------------

	public static CssRuleSet of(
		Collection<? extends IStrKVPair>	properties,
		String...							selectors)
	{
		// Validate arguments
		if (selectors == null)
			throw new IllegalArgumentException("Null selectors");
		if (selectors.length == 0)
			throw new IllegalArgumentException("No selectors");

		// Create and return rule set
		return of(Arrays.asList(selectors), properties);
	}

	//------------------------------------------------------------------

	public static CssRuleSet of(
		String			selector,
		IStrKVPair...	properties)
	{
		// Validate arguments
		if (selector == null)
			throw new IllegalArgumentException("Null selector");
		if (properties == null)
			throw new IllegalArgumentException("Null properties");

		// Create and return rule set
		return of(List.of(selector), Arrays.asList(properties));
	}

	//------------------------------------------------------------------

	public static CssRuleSet of(
		Collection<String>	selectors,
		IStrKVPair...		properties)
	{
		// Validate arguments
		if (selectors == null)
			throw new IllegalArgumentException("Null selectors");
		if (selectors.isEmpty())
			throw new IllegalArgumentException("No selectors");
		if (properties == null)
			throw new IllegalArgumentException("Null properties");

		// Create and return rule set
		return of(selectors, Arrays.asList(properties));
	}

	//------------------------------------------------------------------

	public static String propertyToString(
		String	name,
		String	value)
	{
		return name + CssConstants.PROPERTY_NAME_VALUE_SEPARATOR_CHAR + " " + value + CssConstants.PROPERTY_SEPARATOR_CHAR;
	}

	//------------------------------------------------------------------

	public static void merge(
		List<CssRuleSet>	ruleSets)
	{
		for (int i = 0; i < ruleSets.size(); i++)
		{
			CssRuleSet ruleSet0 = ruleSets.get(i);
			for (int j = i + 1; j < ruleSets.size(); j++)
			{
				CssRuleSet ruleSet = ruleSets.get(j);
				if (ruleSet0.selectors.equals(ruleSet.selectors))
				{
					ruleSet0.properties.putAll(ruleSet.properties);
					ruleSets.remove(j--);
				}
			}
		}
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : overriding methods
////////////////////////////////////////////////////////////////////////

	@Override
	public boolean equals(
		Object	obj)
	{
		if (this == obj)
			return true;

		return (obj instanceof CssRuleSet other) && selectors.equals(other.selectors)
					&& properties.equals(other.properties);
	}

	//------------------------------------------------------------------

	@Override
	public int hashCode()
	{
		return selectors.hashCode() * 31 + properties.hashCode();
	}

	//------------------------------------------------------------------

	@Override
	public CssRuleSet clone()
	{
		try
		{
			// Create copy of superclass
			CssRuleSet copy = (CssRuleSet)super.clone();

			// Copy selectors
			copy.selectors = new ArrayList<>(selectors);

			// Copy properties
			copy.properties = new LinkedHashMap<>(properties);

			// Return copy
			return copy;
		}
		catch (CloneNotSupportedException e)
		{
			throw new UnexpectedRuntimeException(e);
		}
	}

	//------------------------------------------------------------------

	@Override
	public String toString()
	{
		return StringUtils.join('\n', true, toStrings(0));
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	public List<String> selectors()
	{
		return Collections.unmodifiableList(selectors);
	}

	//------------------------------------------------------------------

	public List<String> propertyNames()
	{
		return new ArrayList<>(properties.keySet());
	}

	//------------------------------------------------------------------

	public Map<String, String> properties()
	{
		return Collections.unmodifiableMap(properties);
	}

	//------------------------------------------------------------------

	public boolean hasProperty(
		String	name)
	{
		return properties.containsKey(name);
	}

	//------------------------------------------------------------------

	public String propertyValue(
		String	name)
	{
		return properties.get(name);
	}

	//------------------------------------------------------------------

	public void clearProperties()
	{
		properties.clear();
	}

	//------------------------------------------------------------------

	public void addProperty(
		String	name,
		String	value)
	{
		// Validate arguments
		if (name == null)
			throw new IllegalArgumentException("Null name");
		if (value == null)
			throw new IllegalArgumentException("Null value");

		// Add property to map
		properties.put(name, value);
	}

	//------------------------------------------------------------------

	public void replacePropertyValue(
		String		name,
		Object...	replacements)
	{
		// Validate arguments
		if (name == null)
			throw new IllegalArgumentException("Null name");
		if (replacements == null)
			throw new IllegalArgumentException("Null replacements");

		// Get property value
		String value = properties.get(name);

		// If map contains target property, replace its value and update map
		if (value != null)
			properties.put(name, String.format(value, replacements));
	}

	//------------------------------------------------------------------

	public List<String> toStrings()
	{
		return toStrings(0);
	}

	//------------------------------------------------------------------

	public List<String> toStrings(
		int	indent)
	{
		// Initialise list of strings
		List<String> strs = new ArrayList<>();

		// Add selectors
		String indentStr = " ".repeat(indent);
		Iterator<String> it = selectors.iterator();
		while (it.hasNext())
			strs.add(indentStr + it.next() + (it.hasNext() ? "," : " " + CssConstants.BLOCK_START_STR));

		// Add properties
		for (Map.Entry<String, String> entry : properties.entrySet())
			strs.add(indentStr + INDENT_STR + propertyToString(entry.getKey(), entry.getValue()));

		// Add block end
		strs.add(indentStr + CssConstants.BLOCK_END_STR);

		// Return list of strings
		return strs;
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Member classes : non-inner classes
////////////////////////////////////////////////////////////////////////


	// CLASS: CSS RULE-SET BUILDER


	public static class Builder
	{

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	CssRuleSet	ruleSet;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private Builder()
		{
			// Initialise instance variables
			ruleSet = new CssRuleSet();
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		public Builder selector(
			String	selector)
		{
			// Validate argument
			if (selector == null)
				throw new IllegalArgumentException("Null selector");

			// Add selector to list
			ruleSet.selectors.add(selector);

			// Return this builder
			return this;
		}

		//--------------------------------------------------------------

		public Builder property(
			String	name,
			String	value)
		{
			// Validate arguments
			if (name == null)
				throw new IllegalArgumentException("Null property name");
			if (value == null)
				throw new IllegalArgumentException("Null property value");

			// Add property to map
			ruleSet.properties.put(name, value);

			// Return this builder
			return this;
		}

		//--------------------------------------------------------------

		public CssRuleSet build()
		{
			return ruleSet;
		}

		//--------------------------------------------------------------

	}

	//==================================================================

}

//----------------------------------------------------------------------
