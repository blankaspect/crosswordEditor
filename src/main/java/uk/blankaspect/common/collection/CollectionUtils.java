/*====================================================================*\

CollectionUtils.java

Class: collection-related utility methods.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.common.collection;

//----------------------------------------------------------------------


// IMPORTS


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import uk.blankaspect.common.function.IFunction1;

//----------------------------------------------------------------------


// CLASS: COLLECTION-RELATED UTILITY METHODS


/**
 * This class provides utility methods that relate to {@linkplain Collection collections}.
 */

public class CollectionUtils
{

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	/**
	 * Prevents this class from being instantiated externally.
	 */

	private CollectionUtils()
	{
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	/**
	 * Returns {@code true} if the specified collection is {@code null} or empty.
	 *
	 * @param  collection
	 *           the collection of interest.
	 * @return {@code true} if {@code collection} is {@code null} or empty; {@code false} otherwise.
	 */

	public static boolean isNullOrEmpty(
		Collection<?>	collection)
	{
		return (collection == null) || collection.isEmpty();
	}

	//------------------------------------------------------------------

	/**
	 * Adds the specified number of elements to the specified collection.  The elements are provided by the specified
	 * function, which has a single parameter that is the index of the element.
	 *
	 * @param <T>
	 *          the type of the elements of {@code collection}.
	 * @param collection
	 *          the collection to which elements will be added.
	 * @param numElements
	 *          the number of elements that will be added to {@code collection}.
	 * @param source
	 *          the function that provides the elements that will be added to {@code collection}.  The function has a
	 *          single parameter that is the index of the element.
	 */

	public static <T> void add(
		Collection<T>			collection,
		int						numElements,
		IFunction1<T, Integer>	source)
	{
		for (int i = 0; i < numElements; i++)
			collection.add(source.invoke(i));
	}

	//------------------------------------------------------------------

	/**
	 * Adds the specified value to the specified collection if the collection does not already contain the value.
	 *
	 * @param  <T>
	 *           the type of the elements of {@code collection}.
	 * @param  collection
	 *           the collection to which {@code value} may be added.
	 * @param  value
	 *           the value that will be added to {@code collection} if it is not already present.
	 * @return {@code true} if {@code value} was added to {@code collection}.
	 */

	public static <T> boolean addIfAbsent(
		Collection<T>	collection,
		T				value)
	{
		if (!collection.contains(value))
			return collection.add(value);
		return false;
	}

	//------------------------------------------------------------------

	/**
	 * Converts the specified array of primitive {@code int}s to a list of {@link Integer}s by performing a boxing
	 * conversion on each element of the array.
	 *
	 * @param  values
	 *           the array of {@code int}s that will be converted to a list.
	 * @return a list of {@link Integer}s whose elements are the boxed equivalents of the elements of {@code values}.
	 */

	public static List<Integer> intsToList(
		int[]	values)
	{
		List<Integer> outValues = new ArrayList<>();
		for (int value : values)
			outValues.add(value);
		return outValues;
	}

	//------------------------------------------------------------------

	/**
	 * Converts the specified collection of {@link Integer}s to an array of primitive {@code int}s by performing an
	 * unboxing conversion on each element of the collection.
	 *
	 * @param  values
	 *           the collection of {@link Integer}s that will be converted to an array.
	 * @return an array of {@code int}s whose elements are the unboxed equivalents of the elements of {@code values}.
	 */

	public static int[] intsToArray(
		Collection<Integer>	values)
	{
		int[] outValues = new int[values.size()];
		int index = 0;
		for (Integer value : values)
			outValues[index++] = value;
		return outValues;
	}

	//------------------------------------------------------------------

	/**
	 * Converts the specified array of primitive {@code long}s to a list of {@link Long}s by performing a boxing
	 * conversion on each element of the array.
	 *
	 * @param  values
	 *           the array of {@code long}s that will be converted to a list.
	 * @return a list of {@link Long}s whose elements are the boxed equivalents of the elements of {@code values}.
	 */

	public static List<Long> longsToList(
		long[]	values)
	{
		List<Long> outValues = new ArrayList<>();
		for (long value : values)
			outValues.add(value);
		return outValues;
	}

	//------------------------------------------------------------------

	/**
	 * Converts the specified collection of {@link Long}s to an array of primitive {@code long}s by performing an
	 * unboxing conversion on each element of the collection.
	 *
	 * @param  values
	 *           the collection of {@link Long}s that will be converted to an array.
	 * @return an array of {@code long}s whose elements are the unboxed equivalents of the elements of {@code values}.
	 */

	public static long[] longsToArray(
		Collection<Long>	values)
	{
		long[] outValues = new long[values.size()];
		int index = 0;
		for (Long value : values)
			outValues[index++] = value;
		return outValues;
	}

	//------------------------------------------------------------------

	/**
	 * Converts the specified array of primitive {@code double}s to a list of {@link Double}s by performing a boxing
	 * conversion on each element of the array.
	 *
	 * @param  values
	 *           the array of {@code double}s that will be converted to a list.
	 * @return an array of {@code long}s whose elements are the unboxed equivalents of the elements of {@code values}.
	 */

	public static List<Double> doublesToList(
		double[]	values)
	{
		List<Double> outValues = new ArrayList<>();
		for (double value : values)
			outValues.add(value);
		return outValues;
	}

	//------------------------------------------------------------------

	/**
	 * Converts the specified collection of {@link Double}s to an array of primitive {@code double}s by performing an
	 * unboxing conversion on each element of the collection.
	 *
	 * @param  values
	 *           the collection of {@link Double}s that will be converted to an array.
	 * @return an array of {@code double}s whose elements are the unboxed equivalents of the elements of {@code values}.
	 */

	public static double[] doublesToArray(
		Collection<Double>	values)
	{
		double[] outValues = new double[values.size()];
		int index = 0;
		for (Double value : values)
			outValues[index++] = value;
		return outValues;
	}

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------
