/*====================================================================*\

PropertyKeys.java

Interface: property keys.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.ui.swing.misc;

//----------------------------------------------------------------------


// INTERFACE: PROPERTY KEYS


public interface PropertyKeys
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	char	KEY_SEPARATOR_CHAR	= '.';
	String	KEY_SEPARATOR		= Character.toString(KEY_SEPARATOR_CHAR);

	String	KEY_PREFIX	= PropertyKeys.class.getCanonicalName();

	String	PREFERRED_FOCUS_OWNER	= KEY_PREFIX + KEY_SEPARATOR + "preferredFocusOwner";

}

//----------------------------------------------------------------------
