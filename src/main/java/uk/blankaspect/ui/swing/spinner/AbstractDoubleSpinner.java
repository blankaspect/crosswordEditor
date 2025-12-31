/*====================================================================*\

AbstractDoubleSpinner.java

Abstract double spinner class.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.ui.swing.spinner;

//----------------------------------------------------------------------


// IMPORTS


import javax.swing.BorderFactory;
import javax.swing.SpinnerNumberModel;

import uk.blankaspect.ui.swing.textfield.DoubleValueField;

//----------------------------------------------------------------------


// ABSTRACT DOUBLE SPINNER CLASS


public abstract class AbstractDoubleSpinner
	extends AbstractSpinner
{

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	public AbstractDoubleSpinner(double value,
								 double minValue,
								 double maxValue,
								 double stepSize)
	{
		this(value, minValue, maxValue, stepSize, false);
	}

	//------------------------------------------------------------------

	public AbstractDoubleSpinner(double  value,
								 double  minValue,
								 double  maxValue,
								 double  stepSize,
								 boolean alwaysUpdate)
	{
		super(new SpinnerNumberModel(value, minValue, maxValue, stepSize), alwaysUpdate);
	}

	//------------------------------------------------------------------

	public AbstractDoubleSpinner(double           value,
								 double           minValue,
								 double           maxValue,
								 double           stepSize,
								 DoubleValueField editor)
	{
		this(value, minValue, maxValue, stepSize, editor, false);
	}

	//------------------------------------------------------------------

	public AbstractDoubleSpinner(double           value,
								 double           minValue,
								 double           maxValue,
								 double           stepSize,
								 DoubleValueField editor,
								 boolean          alwaysUpdate)
	{
		this(value, minValue, maxValue, stepSize, alwaysUpdate);
		initEditor(editor);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : overriding methods
////////////////////////////////////////////////////////////////////////

	@Override
	public boolean requestFocusInWindow()
	{
		return (editor == null) ? super.requestFocusInWindow() : editor.requestFocusInWindow();
	}

	//------------------------------------------------------------------

	@Override
	public void updateValue()
	{
		try
		{
			double value = getEditorValue();
			double boundedValue = clampValue(value);
			setDoubleValue(boundedValue);
			if (alwaysUpdate || (value != boundedValue) || isEditorInvalid())
				setEditorValue(boundedValue);
		}
		catch (NumberFormatException e)
		{
			// ignore
		}
	}

	//------------------------------------------------------------------

	@Override
	public void updateEditorValue()
	{
		setEditorValue(getDoubleValue());
	}

	//------------------------------------------------------------------

	@Override
	protected void incrementValue(int increment)
	{
		double stepSize = (Double)((SpinnerNumberModel)getModel()).getStepSize();
		setDoubleValue(clampValue(getDoubleValue() + (double)increment * stepSize));
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	public double getDoubleValue()
	{
		return (Double)getValue();
	}

	//------------------------------------------------------------------

	public void setDoubleValue(double value)
	{
		setValue(value);
	}

	//------------------------------------------------------------------

	public void setMinimum(double minValue)
	{
		((SpinnerNumberModel)getModel()).setMinimum(minValue);
		setDoubleValue(clampValue(getDoubleValue()));
	}

	//------------------------------------------------------------------

	public void setMaximum(double maxValue)
	{
		((SpinnerNumberModel)getModel()).setMaximum(maxValue);
		setDoubleValue(clampValue(getDoubleValue()));
	}

	//------------------------------------------------------------------

	public void setStepSize(double stepSize)
	{
		((SpinnerNumberModel)getModel()).setStepSize(stepSize);
	}

	//------------------------------------------------------------------

	protected void initEditor(DoubleValueField editor)
	{
		// Set editor
		this.editor = editor;
		editor.setBorder(BorderFactory.createEmptyBorder(SpinnerConstants.VERTICAL_MARGIN,
														 SpinnerConstants.HORIZONTAL_MARGIN,
														 SpinnerConstants.VERTICAL_MARGIN,
														 SpinnerConstants.HORIZONTAL_MARGIN));
		setEditor(editor);
		updateEditorValue();

		// Add actions to editor
		addEditorActions();

		// Add listeners
		editor.setActionCommand(Command.COMMIT_EDIT);
		editor.addActionListener(this);
		editor.addFocusListener(this);
	}

	//------------------------------------------------------------------

	/**
	 * @throws NumberFormatException
	 */

	protected double getEditorValue()
	{
		return editor.getValue();
	}

	//------------------------------------------------------------------

	protected void setEditorValue(double value)
	{
		editor.setValue(value);
	}

	//------------------------------------------------------------------

	protected boolean isEditorInvalid()
	{
		return false;
	}

	//------------------------------------------------------------------

	protected double clampValue(double value)
	{
		SpinnerNumberModel model = (SpinnerNumberModel)getModel();
		double minValue = (Double)model.getMinimum();
		double maxValue = (Double)model.getMaximum();
		return Math.min(Math.max(minValue, value), maxValue);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	protected	DoubleValueField	editor;

}

//----------------------------------------------------------------------
