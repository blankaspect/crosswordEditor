/*====================================================================*\

IFunction1.java

Interface: function with one parameter.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.common.function;

//----------------------------------------------------------------------


// INTERFACE: FUNCTION WITH ONE PARAMETER


/**
 * This functional interface defines the method that must be implemented by a function with one parameter.
 * <p>
 * <b>Note</b>:<br>
 * The return value is the <i>first</i> type parameter.
 * </p>
 *
 * @param <R>
 *          the type of the return value.
 * @param <T>
 *          the type of the parameter.
 */

@FunctionalInterface
public interface IFunction1<R, T>
{

////////////////////////////////////////////////////////////////////////
//  Static methods
////////////////////////////////////////////////////////////////////////

	/**
	 * Returns a function that always returns its argument.
	 *
	 * @param  <T>
	 *           the type of the parameter.
	 * @return a function that always returns its argument.
	 */

	static <T> IFunction1<T, T> identity()
	{
		return arg -> arg;
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Methods
////////////////////////////////////////////////////////////////////////

	/**
	 * Invokes this function with the specified argument.
	 *
	 * @param  arg
	 *           the argument.
	 * @return the result of applying this function to {@code arg}.
	 */

	R invoke(
		T	arg);

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------
