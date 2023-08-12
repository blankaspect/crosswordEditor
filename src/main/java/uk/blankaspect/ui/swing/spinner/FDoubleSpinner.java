/*====================================================================*\

FDoubleSpinner.java

Double spinner class.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.ui.swing.spinner;

//----------------------------------------------------------------------


// IMPORTS


import java.text.NumberFormat;

import uk.blankaspect.ui.swing.font.FontKey;
import uk.blankaspect.ui.swing.font.FontUtils;

//----------------------------------------------------------------------


// DOUBLE SPINNER CLASS


public class FDoubleSpinner
	extends DoubleSpinner
{

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	public FDoubleSpinner(double       value,
						  double       minValue,
						  double       maxValue,
						  double       stepSize,
						  int          maxLength,
						  NumberFormat format)
	{
		this(value, minValue, maxValue, stepSize, maxLength, format, false);
	}

	//------------------------------------------------------------------

	public FDoubleSpinner(double       value,
						  double       minValue,
						  double       maxValue,
						  double       stepSize,
						  int          maxLength,
						  NumberFormat format,
						  boolean      signed)
	{
		super(value, minValue, maxValue, stepSize, maxLength, format, signed);
		FontUtils.setAppFont(FontKey.TEXT_FIELD, this);
	}

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------
