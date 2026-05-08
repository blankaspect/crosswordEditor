/*====================================================================*\

ICancellable.java

Interface: task or operation that can be cancelled.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.common.task;

//----------------------------------------------------------------------


// INTERFACE: TASK OR OPERATION THAT CAN BE CANCELLED


@FunctionalInterface
public interface ICancellable
{

////////////////////////////////////////////////////////////////////////
//  Methods
////////////////////////////////////////////////////////////////////////

	/**
	 * Returns {@code true} if the associated task or operation has been cancelled.
	 *
	 * @return {@code true} if the associated task or operation has been cancelled.
	 */

	boolean isCancelled();

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------
