/*====================================================================*\

ClipboardImage.java

Clipboard image class.

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

//----------------------------------------------------------------------


// CLIPBOARD IMAGE CLASS


public class ClipboardImage
	implements ClipboardOwner, Transferable
{

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	public ClipboardImage(Image image)
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
			return Toolkit.getDefaultToolkit().getSystemClipboard().
														isDataFlavorAvailable(DataFlavor.imageFlavor);
		}
		catch (IllegalStateException e)
		{
			System.out.println(Thread.currentThread().getStackTrace()[1] + " : " + e);
		}
		return false;
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : ClipboardOwner interface
////////////////////////////////////////////////////////////////////////

	public void lostOwnership(Clipboard    clipboard,
							  Transferable contents)
	{
		// do nothing
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : Transferable interface
////////////////////////////////////////////////////////////////////////

	public Object getTransferData(DataFlavor flavour)
		throws UnsupportedFlavorException
	{
		if (!flavour.equals(DataFlavor.imageFlavor))
			throw new UnsupportedFlavorException(DataFlavor.imageFlavor);
		return image;
	}

	//------------------------------------------------------------------

	public DataFlavor[] getTransferDataFlavors()
	{
		return new DataFlavor[] { DataFlavor.imageFlavor };
	}

	//------------------------------------------------------------------

	public boolean isDataFlavorSupported(DataFlavor flavour)
	{
		return flavour.equals(DataFlavor.imageFlavor);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private	Image	image;

}

//----------------------------------------------------------------------
