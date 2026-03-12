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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.KeyStroke;

import uk.blankaspect.common.function.IProcedure0;

import uk.blankaspect.ui.swing.button.FButton;

import uk.blankaspect.ui.swing.misc.GuiUtils;

import uk.blankaspect.ui.swing.workaround.LinuxWorkarounds;

//----------------------------------------------------------------------


// CLASS: GRID-PREVIEW DIALOG


class GridPreviewDialog
	extends JDialog
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	/** The command that is associated with the <i>close</i> button of the dialog. */
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

		// Create procedure to close dialog
		IProcedure0 closeDialog = () ->
		{
			location = getLocation();
			setVisible(false);
			dispose();
		};

		// Button: close
		JButton closeButton = new FButton(AppConstants.CLOSE_STR);
		closeButton.addActionListener(event -> closeDialog.invoke());

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

		// Close dialog if Escape key is pressed
		mainPane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
				.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), CLOSE_COMMAND);
		mainPane.getActionMap().put(CLOSE_COMMAND, new AbstractAction()
		{
			@Override
			public void actionPerformed(
				ActionEvent	event)
			{
				closeDialog.invoke();
			}
		});

		// Set content pane
		setContentPane(mainPane);

		// Dispose of window explicitly
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

		// Handle window events
		addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowOpened(
				WindowEvent	event)
			{
				// WORKAROUND for a bug that has been observed on Linux/GNOME whereby a window is displaced downwards
				// when its location is set.  The error in the y coordinate is the height of the title bar of the
				// window.  The workaround is to set the location of the window again with an adjustment for the error.
				LinuxWorkarounds.fixWindowYCoord(event.getWindow(), location);
			}

			@Override
			public void windowClosing(
				WindowEvent	event)
			{
				closeDialog.invoke();
			}
		});

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
