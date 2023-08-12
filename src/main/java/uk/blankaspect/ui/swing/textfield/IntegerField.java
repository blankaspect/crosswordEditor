/*====================================================================*\

IntegerField.java

Integer text field class.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.ui.swing.textfield;

//----------------------------------------------------------------------


// INTEGER TEXT FIELD CLASS


public abstract class IntegerField
	extends IntegerValueField
{

////////////////////////////////////////////////////////////////////////
//  Member classes : non-inner classes
////////////////////////////////////////////////////////////////////////


	// UNSIGNED INTEGER FIELD CLASS


	public static class Unsigned
		extends IntegerField
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		private static final	String	VALID_CHARS	= "0123456789";

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		public Unsigned(int maxLength)
		{
			super(maxLength);
		}

		//--------------------------------------------------------------

		public Unsigned(int maxLength,
						int value)
		{
			super(maxLength, value);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		/**
		 * @throws NumberFormatException
		 */

		@Override
		public int getValue()
		{
			long value = Long.parseLong(getText());
			if (value > 0xFFFFFFFFL)
				throw new NumberFormatException();
			return (int)value;
		}

		//--------------------------------------------------------------

		@Override
		public void setValue(int value)
		{
			setText(Long.toString(value & 0xFFFFFFFFL));
		}

		//--------------------------------------------------------------

		@Override
		protected boolean acceptCharacter(char ch,
										  int  index)
		{
			return (VALID_CHARS.indexOf(ch) >= 0);
		}

		//--------------------------------------------------------------

	}

	//==================================================================


	// SIGNED INTEGER FIELD CLASS


	public static class Signed
		extends IntegerField
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		private static final	String	VALID_CHARS	= "-0123456789";

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		public Signed(int maxLength)
		{
			super(maxLength);
		}

		//--------------------------------------------------------------

		public Signed(int maxLength,
					  int value)
		{
			super(maxLength, value);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		protected boolean acceptCharacter(char ch,
										  int  index)
		{
			return (VALID_CHARS.indexOf(ch) >= 0);
		}

		//--------------------------------------------------------------

	}

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	private IntegerField(int maxLength)
	{
		super(maxLength);
	}

	//------------------------------------------------------------------

	private IntegerField(int maxLength,
						 int value)
	{
		super(maxLength);
		setValue(value);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : overriding methods
////////////////////////////////////////////////////////////////////////

	/**
	 * @throws NumberFormatException
	 */

	@Override
	public int getValue()
	{
		return Integer.parseInt(getText());
	}

	//------------------------------------------------------------------

	@Override
	public void setValue(int value)
	{
		setText(Integer.toString(value));
	}

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------
