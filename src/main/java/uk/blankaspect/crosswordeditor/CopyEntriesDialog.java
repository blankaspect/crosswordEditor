/*====================================================================*\

CopyEntriesDialog.java

Class: 'copy entries' dialog.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.crosswordeditor;

//----------------------------------------------------------------------


// IMPORTS


import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Window;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.util.EnumMap;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.KeyStroke;

import uk.blankaspect.common.function.IProcedure0;

import uk.blankaspect.ui.swing.button.FButton;
import uk.blankaspect.ui.swing.button.FRadioButton;

import uk.blankaspect.ui.swing.colour.Colours;

import uk.blankaspect.ui.swing.misc.GuiUtils;

import uk.blankaspect.ui.swing.workaround.LinuxWorkarounds;

//----------------------------------------------------------------------


// CLASS: 'COPY ENTRIES' DIALOG


class CopyEntriesDialog
	extends JDialog
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	/** The internal margins of a command button. */
	private static final	Insets	BUTTON_MARGINS	= new Insets(3, 8, 3, 8);

	/** The command that is associated with the <i>cancel</i> button of the dialog. */
	private static final	String	CANCEL_COMMAND	= "cancel";

	/** Miscellaneous strings. */
	private static final	String	COPY_STR	= "Copy";

////////////////////////////////////////////////////////////////////////
//  Class variables
////////////////////////////////////////////////////////////////////////

	private static	Point		location;
	private static	DownOrder	downOrder	= DownOrder.FIELD_NUMBER;

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private	DownOrder	result;

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	private CopyEntriesDialog(
		Window	owner,
		String	title,
		Grid	grid)
	{
		// Call superclass constructor
		super(owner, title, ModalityType.APPLICATION_MODAL);

		// Set icons
		setIconImages(owner.getIconImages());

		// Create order pane
		Box orderPane = Box.createVerticalBox();
		orderPane.setAlignmentX(0.5f);
		orderPane.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

		// Create radio buttons for 'down' orders
		ButtonGroup buttonGroup = new ButtonGroup();
		Map<DownOrder, JRadioButton> orderButtons = new EnumMap<>(DownOrder.class);
		for (DownOrder order : DownOrder.values())
		{
			// Add spacing between buttons
			if (order.ordinal() > 0)
				orderPane.add(Box.createVerticalStrut(2));

			// Create radio button
			JRadioButton button = new FRadioButton(order.text);
			button.setSelected(order == downOrder);
			orderButtons.put(order, button);
			buttonGroup.add(button);
			orderPane.add(button);
		}

		// Create inner button pane
		JPanel innerButtonPane = new JPanel(new GridLayout(1, 0, 8, 0));

		// Create procedure to close dialog
		IProcedure0 closeDialog = () ->
		{
			location = getLocation();
			setVisible(false);
			dispose();
		};

		// Button: copy
		JButton copyButton = new FButton(COPY_STR);
		copyButton.setMargin(BUTTON_MARGINS);
		copyButton.addActionListener(event ->
		{
			// Save state
			downOrder = orderButtons.keySet().stream().filter(order -> orderButtons.get(order).isSelected())
					.findFirst().orElse(null);

			// Set result
			result = downOrder;

			// Close dialog
			closeDialog.invoke();
		});
		innerButtonPane.add(copyButton);

		// Button: cancel
		JButton cancelButton = new FButton(AppConstants.CANCEL_STR);
		cancelButton.setMargin(BUTTON_MARGINS);
		cancelButton.addActionListener(event -> closeDialog.invoke());
		innerButtonPane.add(cancelButton);

		// Create button pane
		JPanel buttonPane = new JPanel(new BorderLayout());
		buttonPane.add(innerButtonPane, BorderLayout.LINE_END);
		buttonPane.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createMatteBorder(1, 0, 0, 0, Colours.LINE_BORDER),
				BorderFactory.createEmptyBorder(4, 12, 4, 12)));

		// Create main pane
		Box mainPane = Box.createVerticalBox();
		mainPane.add(orderPane);
		mainPane.add(buttonPane);

		// Close dialog if Escape key is pressed
		mainPane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
				.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), CANCEL_COMMAND);
		mainPane.getActionMap().put(CANCEL_COMMAND, new AbstractAction()
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
		getRootPane().setDefaultButton(copyButton);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	public static DownOrder show(
		Window	owner,
		String	title,
		Grid	grid)
	{
		CopyEntriesDialog dialog = new CopyEntriesDialog(owner, title, grid);
		dialog.setVisible(true);
		return dialog.result;
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Enumerated types
////////////////////////////////////////////////////////////////////////


	// ENUMERATION: ORDER OF 'DOWN' ENTRIES


	public enum DownOrder
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		FIELD_NUMBER
		(
			"Order 'down' entries by field number"
		),

		COLUMN
		(
			"Order 'down' entries by column"
		);

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	String	text;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private DownOrder(
			String	text)
		{
			// Initialise instance variables
			this.text = text;
		}

		//--------------------------------------------------------------

	}

	//==================================================================

}

//----------------------------------------------------------------------
