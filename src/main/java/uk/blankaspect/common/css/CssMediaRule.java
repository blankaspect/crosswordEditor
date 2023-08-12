/*====================================================================*\

CssMediaRule.java

Class: CSS media rule.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.common.css;

//----------------------------------------------------------------------


// IMPORTS


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import uk.blankaspect.common.exception.UnexpectedRuntimeException;

import uk.blankaspect.common.misc.IStringKeyed;

import uk.blankaspect.common.string.StringUtils;

//----------------------------------------------------------------------


// CLASS: CSS MEDIA RULE


public class CssMediaRule
	implements Cloneable
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	private static final	String	MEDIA_KEYWORD	= "@media";

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private	EnumSet<MediaType>	mediaTypes;
	private	List<CssRuleSet>	ruleSets;

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	public CssMediaRule(
		Collection<MediaType>	mediaTypes)
	{
		// Initialise instance variables
		this.mediaTypes = EnumSet.copyOf(mediaTypes);
		ruleSets = new ArrayList<>();
	}

	//------------------------------------------------------------------

	public CssMediaRule(
		MediaType...	mediaTypes)
	{
		// Call alternative constructor
		this(Arrays.asList(mediaTypes));
	}

	//------------------------------------------------------------------

	public CssMediaRule(
		Collection<MediaType>	mediaTypes,
		Collection<CssRuleSet>	ruleSets)
	{
		// Call alternative constructor
		this(mediaTypes);

		// Initialise instance variables
		this.ruleSets.addAll(ruleSets);
	}

	//------------------------------------------------------------------

	public CssMediaRule(
		Collection<MediaType>	mediaTypes,
		CssRuleSet...			ruleSets)
	{
		// Call alternative constructor
		this(mediaTypes, Arrays.asList(ruleSets));
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : overriding methods
////////////////////////////////////////////////////////////////////////

	@Override
	public CssMediaRule clone()
	{
		try
		{
			CssMediaRule copy = (CssMediaRule)super.clone();
			copy.mediaTypes = mediaTypes.clone();
			copy.ruleSets = new ArrayList<>();
			for (CssRuleSet ruleSet : ruleSets)
				copy.ruleSets.add(ruleSet.clone());
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

	public EnumSet<MediaType> getMediaTypes()
	{
		return mediaTypes.clone();
	}

	//------------------------------------------------------------------

	public List<CssRuleSet> getRuleSets()
	{
		return Collections.unmodifiableList(ruleSets);
	}

	//------------------------------------------------------------------

	public void setRuleSets(
		List<CssRuleSet>	ruleSets)
	{
		this.ruleSets.clear();
		this.ruleSets.addAll(ruleSets);
	}

	//------------------------------------------------------------------

	public void addRuleSet(
		CssRuleSet	ruleSet)
	{
		ruleSets.add(ruleSet);
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
		String indentStr = " ".repeat(indent);
		List<String> mediaTypeStrs = new ArrayList<>();
		for (MediaType mediaType : mediaTypes)
			mediaTypeStrs.add(mediaType.getKey());
		List<String> strs = new ArrayList<>();
		strs.add(indentStr + MEDIA_KEYWORD + " " + String.join(", ", mediaTypeStrs));
		strs.add(indentStr + CssConstants.BLOCK_START_STR);
		for (CssRuleSet ruleSet : ruleSets)
			strs.addAll(ruleSet.toStrings(indent + CssConstants.INDENT_INCREMENT));
		strs.add(indentStr + CssConstants.BLOCK_END_STR);
		return strs;
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Enumerated types
////////////////////////////////////////////////////////////////////////


	// ENUMERATION: MEDIA TYPES


	public enum MediaType
		implements IStringKeyed
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		PRINT
		(
			"print"
		),

		SCREEN
		(
			"screen"
		);

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	String	key;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private MediaType(
			String	key)
		{
			// Initialise instance variables
			this.key = key;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : IStringKeyed interface
	////////////////////////////////////////////////////////////////////

		@Override
		public String getKey()
		{
			return key;
		}

		//--------------------------------------------------------------

	}

	//==================================================================

}

//----------------------------------------------------------------------
