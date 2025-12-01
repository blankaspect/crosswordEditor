/*====================================================================*\

AlternativeTextButton.java

Class: alternative-text button.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.ui.swing.button;

//----------------------------------------------------------------------


// IMPORTS


import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Insets;

import java.util.EnumSet;

//----------------------------------------------------------------------


// CLASS: ALTERNATIVE-TEXT BUTTON


public class AlternativeTextButton<T>
	extends FButton
{

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private	T	alternative;
	private	int	width;

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	public AlternativeTextButton(
		Iterable<? extends T>	alternatives)
	{
		this(alternatives, null, -1, -1);
	}

	//------------------------------------------------------------------

	public AlternativeTextButton(
		Iterable<? extends T>	alternatives,
		T						alternative)
	{
		this(alternatives, alternative, -1, -1);
	}

	//------------------------------------------------------------------

	public AlternativeTextButton(
		Iterable<? extends T>	alternatives,
		int						verticalMargin,
		int						horizontalMargin)
	{
		this(alternatives, null, verticalMargin, horizontalMargin);
	}

	//------------------------------------------------------------------

	public AlternativeTextButton(
		Iterable<? extends T>	alternatives,
		T						alternative,
		int						verticalMargin,
		int						horizontalMargin)
	{
		// Call superclass constructor
		super((alternative == null) ? alternatives.iterator().next().toString() : alternative.toString());

		// Initialise instance variables
		this.alternative = alternative;

		// Set margins
		Insets margins = getMargin();
		if (verticalMargin >= 0)
		{
			margins.top = verticalMargin;
			margins.bottom = verticalMargin;
		}
		if (horizontalMargin >= 0)
		{
			margins.left = horizontalMargin;
			margins.right = horizontalMargin;
		}
		setMargin(margins);

		// Calculate width from alternatives
		FontMetrics fontMetrics = getFontMetrics(getFont());
		for (T alt : alternatives)
		{
			int strWidth = fontMetrics.stringWidth(alt.toString()) + 1;
			if (width < strWidth)
				width = strWidth;
		}
		width += getInsets().left + getInsets().right;
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	public static <E extends Enum<E>> AlternativeTextButton<E> create(
		Class<E>	enumClass)
	{
		return create(enumClass, null, -1, -1);
	}

	//------------------------------------------------------------------

	public static <E extends Enum<E>> AlternativeTextButton<E> create(
		Class<E>	enumClass,
		E			alternative)
	{
		return create(enumClass, alternative, -1, -1);
	}

	//------------------------------------------------------------------

	public static <E extends Enum<E>> AlternativeTextButton<E> create(
		Class<E>	enumClass,
		int			verticalMargin,
		int			horizontalMargin)
	{
		return create(enumClass, null, verticalMargin, horizontalMargin);
	}

	//------------------------------------------------------------------

	public static <E extends Enum<E>> AlternativeTextButton<E> create(
		Class<E>	enumClass,
		E			alternative,
		int			verticalMargin,
		int			horizontalMargin)
	{
		return new AlternativeTextButton<>(EnumSet.allOf(enumClass), alternative, verticalMargin, horizontalMargin);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : overriding methods
////////////////////////////////////////////////////////////////////////

	@Override
	public Dimension getPreferredSize()
	{
		return new Dimension(width, super.getPreferredSize().height);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	public T getAlternative()
	{
		return alternative;
	}

	//------------------------------------------------------------------

	public void setAlternative(
		T	alternative)
	{
		if ((alternative == null) ? (this.alternative != null) : !alternative.equals(this.alternative))
		{
			this.alternative = alternative;
			setText((alternative == null) ? null : alternative.toString());
		}
	}

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------
