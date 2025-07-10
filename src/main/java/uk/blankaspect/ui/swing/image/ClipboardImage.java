/*====================================================================*\

ClipboardImage.java

Class: clipboard image.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.ui.swing.image;

//----------------------------------------------------------------------


// IMPORTS


import java.awt.Image;
import java.awt.Toolkit;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;

import uk.blankaspect.common.stack.StackUtils;

//----------------------------------------------------------------------


// CLASS: CLIPBOARD IMAGE


public class ClipboardImage
	implements ClipboardOwner, Transferable
{

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private	Image	image;

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	public ClipboardImage(
		Image	image)
	{
		this.image = image;
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	public static boolean clipboardHasImage()
	{
		try
		{
			return Toolkit.getDefaultToolkit().getSystemClipboard().isDataFlavorAvailable(DataFlavor.imageFlavor);
		}
		catch (IllegalStateException e)
		{
			System.err.println(StackUtils.toStackTraceString(StackUtils.stackFrame()) + " : " + e);
		}
		return false;
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : ClipboardOwner interface
////////////////////////////////////////////////////////////////////////

	@Override
	public void lostOwnership(
		Clipboard		clipboard,
		Transferable	contents)
	{
		// do nothing
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : Transferable interface
////////////////////////////////////////////////////////////////////////

	@Override
	public Object getTransferData(
		DataFlavor	flavour)
		throws UnsupportedFlavorException
	{
		if (!flavour.equals(DataFlavor.imageFlavor))
			throw new UnsupportedFlavorException(DataFlavor.imageFlavor);
		return image;
	}

	//------------------------------------------------------------------

	@Override
	public DataFlavor[] getTransferDataFlavors()
	{
		return new DataFlavor[] { DataFlavor.imageFlavor };
	}

	//------------------------------------------------------------------

	@Override
	public boolean isDataFlavorSupported(
		DataFlavor	flavour)
	{
		return flavour.equals(DataFlavor.imageFlavor);
	}

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------
