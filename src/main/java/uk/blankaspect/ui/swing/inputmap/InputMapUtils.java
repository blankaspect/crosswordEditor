/*====================================================================*\

InputMapUtils.java

Input map utility methods class.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.ui.swing.inputmap;

//----------------------------------------------------------------------


// IMPORTS


import java.awt.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

//----------------------------------------------------------------------


// INPUT MAP UTILITY METHODS CLASS


public class InputMapUtils
{

////////////////////////////////////////////////////////////////////////
//  Member classes : non-inner classes
////////////////////////////////////////////////////////////////////////


	// REMOVED KEY CLASS


	private static class RemovedKey
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private RemovedKey(InputMap  inputMap,
						   KeyStroke key,
						   Object    actionMapKey)
		{
			this.inputMap = inputMap;
			this.key = key;
			this.actionMapKey = actionMapKey;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		private void restore()
		{
			inputMap.put(key, actionMapKey);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	InputMap	inputMap;
		private	KeyStroke	key;
		private	Object		actionMapKey;

	}

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	private InputMapUtils()
	{
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	public static int removeFromInputMap(JComponent   component,
										 int          condition,
										 KeyStroke... keys)
	{
		return removeFromInputMap(component, condition, Arrays.asList(keys));
	}

	//------------------------------------------------------------------

	public static int removeFromInputMap(JComponent      component,
										 int             condition,
										 List<KeyStroke> keys)
	{
		// Remove keys from the hierarchy of input maps
		List<RemovedKey> removedKeys = new ArrayList<>();
		for (KeyStroke key : keys)
		{
			InputMap inputMap = component.getInputMap(condition);
			while (inputMap != null)
			{
				KeyStroke[] mapKeys = inputMap.keys();
				if ((mapKeys != null) && Arrays.asList(mapKeys).contains(key))
				{
					removedKeys.add(new RemovedKey(inputMap, key, inputMap.get(key)));
					inputMap.remove(key);
					break;
				}
				inputMap = inputMap.getParent();
			}
		}

		// If any keys have been removed, add the list of keys to the map of removed keys and return the
		// key of the map entry
		int key = 0;
		if (!removedKeys.isEmpty())
		{
			while ((key == 0) || removedKeyMap.containsKey(key))
				key = prng.nextInt();
			removedKeyMap.put(key, removedKeys);
		}
		return key;
	}

	//------------------------------------------------------------------

	public static void restoreInputMap(int key)
	{
		List<RemovedKey> removedKeys = removedKeyMap.get(key);
		if (removedKeys != null)
		{
			for (RemovedKey removedKey : removedKeys)
				removedKey.restore();
			removedKeyMap.remove(key);
		}
	}

	//------------------------------------------------------------------

	public static void removeFromInputMap(JComponent   component,
										  boolean      recursive,
										  int          condition,
										  KeyStroke... keyStrokes)
	{
		removeFromInputMap(component, true, condition, Arrays.asList(keyStrokes));
	}

	//------------------------------------------------------------------

	public static void removeFromInputMap(JComponent      component,
										  boolean         recursive,
										  int             condition,
										  List<KeyStroke> keyStrokes)
	{
		// Remove key strokes from input map of component
		InputMap inputMap = component.getInputMap(condition);
		while (inputMap != null)
		{
			for (KeyStroke keyStroke : keyStrokes)
				inputMap.remove(keyStroke);
			inputMap = inputMap.getParent();
		}

		// Remove key strokes from input maps of component's children
		if (recursive)
		{
			for (int i = 0; i < component.getComponentCount(); i++)
			{
				Component childComponent = component.getComponent(i);
				if (childComponent instanceof JComponent)
					removeFromInputMap((JComponent)childComponent, true, condition, keyStrokes);
			}
		}
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class variables
////////////////////////////////////////////////////////////////////////

	private static	Random							prng			= new Random();
	private static	Map<Integer, List<RemovedKey>>	removedKeyMap	= new HashMap<>();

}

//----------------------------------------------------------------------
