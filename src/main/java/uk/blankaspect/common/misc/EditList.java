/*====================================================================*\

EditList.java

Class: edit list.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.common.misc;

//----------------------------------------------------------------------


// IMPORTS


import java.util.LinkedList;

//----------------------------------------------------------------------


// CLASS: EDIT LIST


public class EditList
{

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	protected	int					maxLength;
	protected	int					currentIndex;
	protected	int					unchangedIndex;
	protected	LinkedList<IEdit>	edits;

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	public EditList(
		int	maxLength)
	{
		// Initialise instance variables
		this.maxLength = maxLength;
		edits = new LinkedList<>();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	public IEdit getUndo()
	{
		return canUndo() ? edits.get(currentIndex - 1) : null;
	}

	//------------------------------------------------------------------

	public IEdit getRedo()
	{
		return canRedo() ? edits.get(currentIndex) : null;
	}

	//------------------------------------------------------------------

	public IEdit removeUndo()
	{
		return canUndo() ? edits.get(--currentIndex) : null;
	}

	//------------------------------------------------------------------

	public IEdit removeRedo()
	{
		return canRedo() ? edits.get(currentIndex++) : null;
	}

	//------------------------------------------------------------------

	public boolean canUndo()
	{
		return (currentIndex > 0);
	}

	//------------------------------------------------------------------

	public boolean canRedo()
	{
		return (currentIndex < edits.size());
	}

	//------------------------------------------------------------------

	public boolean isEmpty()
	{
		return edits.isEmpty();
	}

	//------------------------------------------------------------------

	public boolean isChanged()
	{
		return (currentIndex != unchangedIndex);
	}

	//------------------------------------------------------------------

	public void setChanged()
	{
		unchangedIndex = -1;
	}

	//------------------------------------------------------------------

	public void add(
		IEdit	edit)
	{
		// Test whether to accept edit
		if (!acceptEdit(edit))
			return;

		// Remove redos
		while (edits.size() > currentIndex)
			edits.removeLast();

		// Preserve changed status if unchanged state cannot be recovered
		if (unchangedIndex > currentIndex)
			unchangedIndex = -1;

		// Process edit
		edit = processEdit(edit);

		// Remove oldest edits while list is full
		while (edits.size() >= maxLength)
		{
			edits.removeFirst();
			if (--unchangedIndex < 0)
				unchangedIndex = -1;
			if (--currentIndex < 0)
				currentIndex = 0;
		}

		// Add new edit
		edits.add(edit);
		++currentIndex;
	}

	//------------------------------------------------------------------

	public void clear()
	{
		edits.clear();
		unchangedIndex = currentIndex = 0;
	}

	//------------------------------------------------------------------

	public void reset()
	{
		while (edits.size() > currentIndex)
			edits.removeLast();

		unchangedIndex = currentIndex;
	}

	//------------------------------------------------------------------

	protected boolean acceptEdit(
		IEdit	edit)
	{
		return true;
	}

	//------------------------------------------------------------------

	protected IEdit processEdit(
		IEdit	edit)
	{
		return edit;
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Member interfaces
////////////////////////////////////////////////////////////////////////


	// INTERFACE: EDIT


	public interface IEdit
	{

	////////////////////////////////////////////////////////////////////
	//  Abstract methods
	////////////////////////////////////////////////////////////////////

		void undo();

		//--------------------------------------------------------------

		void redo();

		//--------------------------------------------------------------

		default String text()
		{
			return null;
		}

		//--------------------------------------------------------------

	}

	//==================================================================

}

//----------------------------------------------------------------------
