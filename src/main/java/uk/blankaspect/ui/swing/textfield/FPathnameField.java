/*====================================================================*\

FPathnameField.java

Pathname field class.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.ui.swing.textfield;

//----------------------------------------------------------------------


// IMPORTS


import java.io.File;

import uk.blankaspect.ui.swing.font.FontKey;
import uk.blankaspect.ui.swing.font.FontUtils;

import uk.blankaspect.ui.swing.misc.GuiUtils;

//----------------------------------------------------------------------


// PATHNAME FIELD CLASS


public class FPathnameField
	extends PathnameField
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	private static final	int	NUM_COLUMNS	= 40;

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	public FPathnameField()
	{
		super(NUM_COLUMNS);
		_init();
	}

	//------------------------------------------------------------------

	public FPathnameField(String pathname)
	{
		super(pathname, NUM_COLUMNS);
		_init();
	}

	//------------------------------------------------------------------

	public FPathnameField(File file)
	{
		super(file, NUM_COLUMNS);
		_init();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	private void _init()
	{
		FontUtils.setAppFont(FontKey.TEXT_FIELD, this);
		GuiUtils.setTextComponentMargins(this);
	}

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------
