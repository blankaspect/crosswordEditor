/*====================================================================*\

ImageUtils.java

Image utility methods class.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.ui.swing.image;

//----------------------------------------------------------------------


// IMPORTS


import java.awt.Color;
import java.awt.Rectangle;

import java.awt.image.BufferedImage;

//----------------------------------------------------------------------


// IMAGE UTILITY METHODS CLASS


public class ImageUtils
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	private static final	int	RGB_MASK	= 0x00FFFFFF;
	private static final	int	ALPHA_MASK	= 0xFF000000;

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	/**
	 * Prevents this class from being instantiated externally.
	 */

	private ImageUtils()
	{
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	public static BufferedImage stringArrayToImage(
		String[]	strings,
		Color[]		colours)
	{
		int[] rgbaValues = new int[colours.length];
		for (int i = 0; i < rgbaValues.length; i++)
			rgbaValues[i] = colours[i].getRGB();
		return stringArrayToImage(strings, rgbaValues);
	}

	//------------------------------------------------------------------

	public static BufferedImage stringArrayToImage(
		String[]	strings,
		int[]		rgbaValues)
	{
		int width = strings[0].length();
		int height = strings.length;
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		for (int y = 0; y < height; y++)
		{
			String str = strings[y];
			for (int x = 0; x < width; x++)
				image.setRGB(x, y, rgbaValues[str.charAt(x) - '0']);
		}
		return image;
	}

	//------------------------------------------------------------------

	/**
	 * @throws IllegalArgumentException
	 */

	public static BufferedImage getSubimage(
		BufferedImage	image,
		int				x,
		int				y,
		int				width,
		int				height)
	{
		// Validate arguments
		if ((x < 0) || (width < 0) || (x + width > image.getWidth()) ||
			 (y < 0) || (height < 0) || (y + height > image.getHeight()))
			throw new IllegalArgumentException();

		// Create a new image from a region of the input image
		BufferedImage subimage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		int[] rgbBuffer = new int[width * height];
		image.getRGB(x, y, width, height, rgbBuffer, 0, width);
		subimage.setRGB(0, 0, width, height, rgbBuffer, 0, width);
		return subimage;
	}

	//------------------------------------------------------------------

	/**
	 * @throws IllegalArgumentException
	 */

	public static BufferedImage getSubimage(
		BufferedImage	image,
		Rectangle		rect)
	{
		return getSubimage(image, rect.x, rect.y, rect.width, rect.height);
	}

	//------------------------------------------------------------------

	public static BufferedImage imageDataToImage(
		int		dataWidth,
		int		dataHeight,
		int		imageWidth,
		int		imageHeight,
		int[]	data,
		Color	colour)
	{
		// Set colour in image data
		int[] outImageData = new int[data.length];
		int rgb = colour.getRGB() & RGB_MASK;
		for (int i = 0; i < outImageData.length; i++)
		{
			int value = data[i];
			if ((value & RGB_MASK) == 0)
			{
				value &= ALPHA_MASK;
				value |= rgb;
			}
			outImageData[i] = value;
		}

		// Create image from tiled image data
		BufferedImage image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB);
		for (int y = 0; y < imageHeight; y += dataHeight)
		{
			for (int x = 0; x < imageWidth; x += dataWidth)
				image.setRGB(x, y, Math.min(imageWidth - x, dataWidth),
							 Math.min(imageHeight - y, dataHeight), outImageData, 0, dataWidth);
		}
		return image;
	}

	//------------------------------------------------------------------

	public static BufferedImage imageDataToImage(
		int		dataWidth,
		int		dataHeight,
		int[]	imageData,
		Color	colour)
	{
		return imageDataToImage(dataWidth, dataHeight, dataWidth, dataHeight, imageData, colour);
	}

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------
