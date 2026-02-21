/*====================================================================*\

IProcedure0.java

Interface: procedure with no parameters.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.common.function;

//----------------------------------------------------------------------


// INTERFACE: PROCEDURE WITH NO PARAMETERS


/**
 * This functional interface defines the method that must be implemented by a <i>procedure</i> (a function that has no
 * return value) with no parameters.  A procedure acts only through its side effects.
 */

@FunctionalInterface
public interface IProcedure0
{

////////////////////////////////////////////////////////////////////////
//  Methods
////////////////////////////////////////////////////////////////////////

	/**
	 * Invokes this procedure.
	 */

	void invoke();

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------
