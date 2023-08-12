/*====================================================================*\

CssSelector.java

Class: CSS selector.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.common.css;

//----------------------------------------------------------------------


// CLASS: CSS SELECTOR


public class CssSelector
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	public static final	String	CHILD			= " > ";
	public static final	String	CLASS			= ".";
	public static final	String	PSEUDO_CLASS	= ":";
	public static final	String	DESCENDANT		= " ";
	public static final	String	FIRST_CHILD		= PSEUDO_CLASS + "first-child";
	public static final	String	ID				= "#";

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	private CssSelector()
	{
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

////////////////////////////////////////////////////////////////////////
//  Member classes : non-inner classes
////////////////////////////////////////////////////////////////////////


	// CLASS: CSS SELECTOR BUILDER


	public static class Builder
	{

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	StringBuilder	buffer;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private Builder()
		{
			// Initialise instance variables
			buffer = new StringBuilder(256);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Class methods
	////////////////////////////////////////////////////////////////////

		public static Builder create()
		{
			return new Builder();
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		public Builder id(
			String	id)
		{
			// Add prefix and identifier
			buffer.append(ID);
			buffer.append(id);

			// Return this builder
			return this;
		}

		//--------------------------------------------------------------

		public Builder cls(
			String	name)
		{
			// Add prefix and name of class
			buffer.append(CLASS);
			buffer.append(name);

			// Return this builder
			return this;
		}

		//--------------------------------------------------------------

		public Builder pseudo(
			String...	names)
		{
			// Add prefix and name of each pseudo-class
			for (String name : names)
			{
				buffer.append(PSEUDO_CLASS);
				buffer.append(name);
			}

			// Return this builder
			return this;
		}

		//--------------------------------------------------------------

		public Builder child(
			String	name)
		{
			// Add combinator
			buffer.append(CHILD);

			// Add prefix and name of class; return this builder
			return cls(name);
		}

		//--------------------------------------------------------------

		public Builder childId(
			String	id)
		{
			// Add combinator
			buffer.append(CHILD);

			// Add prefix and identifier; return this builder
			return id(id);
		}

		//--------------------------------------------------------------

		public Builder desc(
			String	name)
		{
			// Add combinator
			buffer.append(DESCENDANT);

			// Add prefix and name of class; return this builder
			return cls(name);
		}

		//--------------------------------------------------------------

		public Builder descId(
			String	id)
		{
			// Add combinator
			buffer.append(DESCENDANT);

			// Add prefix and identifier; return this builder
			return id(id);
		}

		//--------------------------------------------------------------

		public Builder notId(
			String	id,
			int		count)
		{
			// Add prefix
			buffer.append(PSEUDO_CLASS);
			buffer.append(CssPseudoClass.NOT);
			buffer.append('(');
			for (int i = 0; i < count; i++)
			{
				buffer.append(ID);
				buffer.append(id);
			}
			buffer.append(')');

			// Return this builder
			return this;
		}

		//--------------------------------------------------------------

		public String build()
		{
			return buffer.toString();
		}

		//--------------------------------------------------------------

	}

	//==================================================================

}

//----------------------------------------------------------------------
