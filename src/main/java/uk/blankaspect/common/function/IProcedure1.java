/*====================================================================*\

IProcedure1.java

Interface: procedure with one parameter.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.common.function;

//----------------------------------------------------------------------


// INTERFACE: PROCEDURE WITH ONE PARAMETER


/**
 * This functional interface defines the method that must be implemented by a <i>procedure</i> (a function that has no
 * return value) with one parameter.  A procedure acts only through its side effects.
 *
 * @param <T>
 *          the type of the parameter.
 */

@FunctionalInterface
public interface IProcedure1<T>
{

////////////////////////////////////////////////////////////////////////
//  Methods
////////////////////////////////////////////////////////////////////////

	/**
	 * Invokes this procedure with the specified argument.
	 *
	 * @param arg
	 *          the argument.
	 */

	void invoke(
		T	arg);

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------
