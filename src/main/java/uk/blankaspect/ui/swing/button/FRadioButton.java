/*====================================================================*\

FRadioButton.java

Radio button class.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.ui.swing.button;

//----------------------------------------------------------------------


// IMPORTS


import javax.swing.BorderFactory;
import javax.swing.JRadioButton;

import uk.blankaspect.ui.swing.font.FontKey;
import uk.blankaspect.ui.swing.font.FontUtils;

//----------------------------------------------------------------------


// RADIO BUTTON CLASS


public class FRadioButton
	extends JRadioButton
{

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	public FRadioButton(String text)
	{
		this(text, false, false);
	}

	//------------------------------------------------------------------

	public FRadioButton(String  text,
						boolean noVerticalBorder)
	{
		this(text, noVerticalBorder, false);
	}

	//------------------------------------------------------------------

	public FRadioButton(String  text,
						boolean noVerticalBorder,
						boolean selected)
	{
		super(text, selected);
		FontUtils.setAppFont(FontKey.MAIN, this);
		setBorder(noVerticalBorder ? null : BorderFactory.createEmptyBorder(2, 0, 2, 0));
	}

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------
