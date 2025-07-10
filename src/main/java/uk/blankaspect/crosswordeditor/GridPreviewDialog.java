/*====================================================================*\

GridPreviewDialog.java

Class: grid-preview dialog.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.crosswordeditor;

//----------------------------------------------------------------------


// IMPORTS


import java.awt.Component;
import java.awt.Point;
import java.awt.Window;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.KeyStroke;

import uk.blankaspect.ui.swing.button.FButton;

import uk.blankaspect.ui.swing.misc.GuiUtils;

//----------------------------------------------------------------------


// CLASS: GRID-PREVIEW DIALOG


class GridPreviewDialog
	extends JDialog
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	/** The command that is associated with the <i>close</i> button of a dialog. */
	private static final	String	CLOSE_COMMAND	= "close";

	/** Miscellaneous strings. */
	private static final	String	GRID_PREVIEW_STR	= "Grid preview";

////////////////////////////////////////////////////////////////////////
//  Class variables
////////////////////////////////////////////////////////////////////////

	private static	Point	location;

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	private GridPreviewDialog(
		Window	owner,
		Grid	grid)
	{
		// Call superclass constructor
		super(owner, GRID_PREVIEW_STR, ModalityType.APPLICATION_MODAL);

		// Set icons
		setIconImages(owner.getIconImages());

		// Create grid pane
		GridPane gridPane = grid.getSeparator().createGridPane(grid, false);

		// Button: close
		JButton closeButton = new FButton(AppConstants.CLOSE_STR);
		closeButton.addActionListener(event ->
		{
			location = getLocation();
			setVisible(false);
			dispose();
		});

		// Create button pane
		Box buttonPane = Box.createHorizontalBox();
		buttonPane.add(Box.createHorizontalGlue());
		buttonPane.add(closeButton);

		// Create main pane
		Box mainPane = Box.createVerticalBox();
		mainPane.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		mainPane.add(gridPane);
		mainPane.add(Box.createVerticalStrut(4));
		mainPane.add(buttonPane);

		// If Escape key is pressed, fire 'close' button
		mainPane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
					.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), CLOSE_COMMAND);
		mainPane.getActionMap().put(CLOSE_COMMAND, new AbstractAction()
		{
			@Override
			public void actionPerformed(
				ActionEvent	event)
			{
				closeButton.doClick();
			}
		});

		// Set content pane
		setContentPane(mainPane);

		// Dispose of window when it is closed
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		// Prevent dialog from being resized
		setResizable(false);

		// Resize dialog to its preferred size
		pack();

		// Set location of dialog
		if (location == null)
			location = GuiUtils.getComponentLocation(this, owner);
		setLocation(location);

		// Set default button
		getRootPane().setDefaultButton(closeButton);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	public static void showDialog(
		Component	parent,
		Grid		grid)
	{
		new GridPreviewDialog(GuiUtils.getWindow(parent), grid).setVisible(true);
	}

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------
