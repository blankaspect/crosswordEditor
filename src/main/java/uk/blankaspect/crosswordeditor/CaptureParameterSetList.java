/*====================================================================*\

CaptureParameterSetList.java

Class: parameter-set list for crossword capture.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.crosswordeditor;

//----------------------------------------------------------------------


// IMPORTS


import java.io.File;

import org.w3c.dom.Element;

import uk.blankaspect.common.exception.AppException;

import uk.blankaspect.common.property.ParameterSetList;

import uk.blankaspect.common.xml.XmlParseException;

//----------------------------------------------------------------------


// CLASS: PARAMETER-SET LIST FOR CROSSWORD CAPTURE


class CaptureParameterSetList
	extends ParameterSetList<CaptureParams>
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	private static final	int		VERSION					= 0;
	private static final	int		MIN_SUPPORTED_VERSION	= 0;
	private static final	int		MAX_SUPPORTED_VERSION	= 0;

	private static final	String	APPLICATION_KEY	= CrosswordEditorApp.NAME_KEY;

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	public CaptureParameterSetList()
	{
		super(APPLICATION_KEY);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	public static void createFile(
		File	file)
		throws AppException
	{
		new CaptureParameterSetList().write(file, false);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : overriding methods
////////////////////////////////////////////////////////////////////////

	@Override
	public int getVersion()
	{
		return VERSION;
	}

	//------------------------------------------------------------------

	@Override
	protected boolean isSupportedVersion(
		int	version)
	{
		return (version >= MIN_SUPPORTED_VERSION) && (version <= MAX_SUPPORTED_VERSION);
	}

	//------------------------------------------------------------------

	@Override
	protected CaptureParams createElement(
		Element	element)
		throws XmlParseException
	{
		return new CaptureParams(element);
	}

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------
