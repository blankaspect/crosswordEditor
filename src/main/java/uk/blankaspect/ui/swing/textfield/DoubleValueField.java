/*====================================================================*\

DoubleValueField.java

Double value field class.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.ui.swing.textfield;

//----------------------------------------------------------------------


// DOUBLE VALUE FIELD CLASS


public abstract class DoubleValueField
	extends ConstrainedTextField
{

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	protected DoubleValueField(int maxLength)
	{
		super(maxLength);
	}

	//------------------------------------------------------------------

	protected DoubleValueField(int    maxLength,
							   String text)
	{
		super(maxLength, text);
	}

	//------------------------------------------------------------------

	protected DoubleValueField(int maxLength,
							   int columns)
	{
		super(maxLength, columns);
	}

	//------------------------------------------------------------------

	protected DoubleValueField(int    maxLength,
							   int    columns,
							   String text)
	{
		super(maxLength, columns, text);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Abstract methods
////////////////////////////////////////////////////////////////////////

	public abstract double getValue();

	//------------------------------------------------------------------

	public abstract void setValue(double value);

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------
