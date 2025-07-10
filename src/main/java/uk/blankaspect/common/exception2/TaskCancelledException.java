/*====================================================================*\

TaskCancelledException.java

Class: 'task cancelled' exception.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.common.exception2;

//----------------------------------------------------------------------


// CLASS: 'TASK CANCELLED' EXCEPTION


public class TaskCancelledException
	extends BaseException
{

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private	int		code;
	private	Thread	thread;

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	public TaskCancelledException()
	{
		// Call alternative constructor
		this(0);
	}

	//------------------------------------------------------------------

	public TaskCancelledException(
		int	code)
	{
		// Initialise instance variables
		this.code = code;
		thread = Thread.currentThread();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	public int getCode()
	{
		return code;
	}

	//------------------------------------------------------------------

	public Thread getThread()
	{
		return thread;
	}

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------
