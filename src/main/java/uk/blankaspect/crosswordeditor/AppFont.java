/*====================================================================*\

AppFont.java

Enumeration: application font.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.crosswordeditor;

//----------------------------------------------------------------------


// IMPORTS


import java.awt.Component;
import java.awt.Font;

import uk.blankaspect.common.misc.IStringKeyed;

import uk.blankaspect.ui.swing.font.FontEx;
import uk.blankaspect.ui.swing.font.FontStyle;
import uk.blankaspect.ui.swing.font.FontUtils;

//----------------------------------------------------------------------


// ENUMERATION: APPLICATION FONT


public enum AppFont
	implements IStringKeyed
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	MAIN
	(
		"main",
		"Main"
	),

	TEXT_FIELD
	(
		"textField",
		"Text field"
	),

	COMBO_BOX
	(
		"comboBox",
		"Combo box"
	),

	CLUE
	(
		"clue",
		"Clue"
	),

	FIELD_NUMBER
	(
		"fieldNumber",
		"Field number",
		null,
		null,
		9
	),

	GRID_ENTRY
	(
		"gridEntry",
		"Grid entry"
	);

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private	String	key;
	private	String	text;
	private	FontEx	fontEx;

////////////////////////////////////////////////////////////////////////
//  Static initialiser
////////////////////////////////////////////////////////////////////////

	static
	{
		FontUtils.setAppFontClass(AppFont.class);
	}

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	private AppFont(
		String	key,
		String	text)
	{
		this(key, text, null, null, 0);
	}

	//------------------------------------------------------------------

	private AppFont(
		String		key,
		String		text,
		String		name,
		FontStyle	style,
		int			size)
	{
		this.key = key;
		this.text = text;
		fontEx = new FontEx(name, style, size);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	public static int getNumFonts()
	{
		return values().length;
	}

	//------------------------------------------------------------------

	public static String[] getKeys()
	{
		String[] keys = new String[values().length];
		for (int i = 0; i < keys.length; i++)
			keys[i] = values()[i].key;
		return keys;
	}

	//------------------------------------------------------------------

	public static void setFontExs(
		FontEx...	fontExs)
	{
		for (AppFont appFont : values())
		{
			FontEx fontEx = fontExs[appFont.ordinal()];
			if (fontEx != null)
				appFont.setFontEx(fontEx);
		}
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : IStringKeyed interface
////////////////////////////////////////////////////////////////////////

	@Override
	public String getKey()
	{
		return key;
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : overriding methods
////////////////////////////////////////////////////////////////////////

	@Override
	public String toString()
	{
		return text;
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	public FontEx getFontEx()
	{
		return fontEx;
	}

	//------------------------------------------------------------------

	public Font getFont()
	{
		return fontEx.toFont();
	}

	//------------------------------------------------------------------

	public void setFontEx(
		FontEx	fontEx)
	{
		this.fontEx = fontEx;
	}

	//------------------------------------------------------------------

	public void apply(
		Component	component)
	{
		fontEx.applyFont(component);
	}

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------
