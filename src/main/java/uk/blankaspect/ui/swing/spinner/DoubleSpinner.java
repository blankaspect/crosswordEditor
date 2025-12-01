/*====================================================================*\

DoubleSpinner.java

Double spinner class.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.ui.swing.spinner;

//----------------------------------------------------------------------


// IMPORTS


import java.awt.Font;

import java.text.NumberFormat;

import uk.blankaspect.ui.swing.font.FontUtils;

import uk.blankaspect.ui.swing.textfield.DoubleValueField;

//----------------------------------------------------------------------


// DOUBLE SPINNER CLASS


public class DoubleSpinner
	extends AbstractDoubleSpinner
{

////////////////////////////////////////////////////////////////////////
//  Member classes : non-inner classes
////////////////////////////////////////////////////////////////////////


	// EDITOR CLASS


	private static class Editor
		extends DoubleValueField
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		private static final	String	VALID_CHARS	= "-.0123456789";

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private Editor(int          maxLength,
					   NumberFormat format,
					   boolean      signed)
		{
			super(maxLength);
			this.format = format;
			this.signed = signed;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		/**
		 * @throws NumberFormatException
		 */

		@Override
		public double getValue()
		{
			try
			{
				return Double.parseDouble(getText());
			}
			catch (NumberFormatException e)
			{
				setInvalid(true);
				throw e;
			}
		}

		//--------------------------------------------------------------

		/**
		 * @throws IllegalArgumentException
		 */

		@Override
		public void setValue(double value)
		{
			if (!signed && (value < 0.0))
				throw new IllegalArgumentException();
			setText(format.format(value));
		}

		//--------------------------------------------------------------

		@Override
		protected boolean acceptCharacter(char ch,
										  int  index)
		{
			int i = VALID_CHARS.indexOf(ch);
			return (i > 0) || (signed && (i == 0));
		}

		//--------------------------------------------------------------

		@Override
		protected int getColumnWidth()
		{
			return FontUtils.getCharWidth('0', getFontMetrics(getFont()));
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		public void setSigned(boolean signed)
		{
			this.signed = signed;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	NumberFormat	format;
		private	boolean			signed;

	}

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	public DoubleSpinner(double       value,
						 double       minValue,
						 double       maxValue,
						 double       stepSize,
						 int          maxLength,
						 NumberFormat format)
	{
		this(value, minValue, maxValue, stepSize, maxLength, format, false);
	}

	//------------------------------------------------------------------

	public DoubleSpinner(double       value,
						 double       minValue,
						 double       maxValue,
						 double       stepSize,
						 int          maxLength,
						 NumberFormat format,
						 boolean      signed)
	{
		super(value, minValue, maxValue, stepSize, new Editor(maxLength, format, signed));
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : overriding methods
////////////////////////////////////////////////////////////////////////

	@Override
	public void setFont(Font font)
	{
		super.setFont(font);
		if (editor != null)
			editor.setFont(font);
	}

	//------------------------------------------------------------------

	@Override
	protected boolean isEditorInvalid()
	{
		return ((Editor)editor).isInvalid();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	public void setSigned(boolean signed)
	{
		((Editor)editor).setSigned(signed);
	}

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------
