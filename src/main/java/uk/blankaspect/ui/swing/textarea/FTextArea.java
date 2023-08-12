/*====================================================================*\

FTextArea.java

Text area class.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.ui.swing.textarea;

//----------------------------------------------------------------------


// IMPORTS


import javax.swing.JTextArea;

import javax.swing.text.Document;

import uk.blankaspect.ui.swing.font.FontKey;
import uk.blankaspect.ui.swing.font.FontUtils;

//----------------------------------------------------------------------


// TEXT AREA CLASS


public class FTextArea
	extends JTextArea
{

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	public FTextArea()
	{
		_init();
	}

	//------------------------------------------------------------------

	public FTextArea(String text)
	{
		super(text);
		_init();
	}

	//------------------------------------------------------------------

	public FTextArea(int numRows,
					 int numColumns)
	{
		super(numRows, numColumns);
		_init();
	}

	//------------------------------------------------------------------

	public FTextArea(String text,
					 int    numRows,
					 int    numColumns)
	{
		super(text, numRows, numColumns);
		_init();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	public boolean isEmpty()
	{
		Document document = getDocument();
		return ((document == null) ? true : (document.getLength() == 0));
	}

	//------------------------------------------------------------------

	private void _init()
	{
		String fontKey = FontKey.TEXT_AREA;
		if (!FontUtils.isAppFont(fontKey))
			fontKey = FontKey.TEXT_FIELD;
		FontUtils.setAppFont(fontKey, this);
		setBorder(null);
	}

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------
