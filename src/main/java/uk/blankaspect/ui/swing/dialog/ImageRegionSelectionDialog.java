/*====================================================================*\

ImageRegionSelectionDialog.java

Class: image-region selection dialog.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.ui.swing.dialog;

//----------------------------------------------------------------------


// IMPORTS


import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.Window;

import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.awt.image.BufferedImage;

import java.util.ArrayList;
import java.util.List;

import java.util.stream.Stream;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import uk.blankaspect.common.function.IFunction1;

import uk.blankaspect.ui.swing.action.KeyAction;

import uk.blankaspect.ui.swing.colour.Colours;

import uk.blankaspect.ui.swing.font.FontKey;
import uk.blankaspect.ui.swing.font.FontUtils;

import uk.blankaspect.ui.swing.misc.GuiConstants;
import uk.blankaspect.ui.swing.misc.GuiUtils;

//----------------------------------------------------------------------


// CLASS: IMAGE-REGION SELECTION DIALOG


public class ImageRegionSelectionDialog
	extends JDialog
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	private static final	Color	BORDER_COLOUR	= new Color(224, 144, 64);

	/** The padding around a button. */
	private static final	Insets	BUTTON_PADDING	= new Insets(1, 8, 1, 8);

	/** Miscellaneous strings. */
	private static final	String	SELECT_ALL_STR	= "Select all";

	// Commands
	private interface Command
	{
		String	CANCEL	= "cancel";
	}

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private	Selection		result;
	private	int				viewWidth;
	private	int				viewHeight;
	private	double			scaleFactor;
	private	boolean			allSelected;
	private	Anchor			anchor;
	private	ResizeHandle	resizeHandle;
	private	Rectangle		selectedRegion;
	private	SelectionPane	selectionPane;
	private	JButton			selectAllButton;
	private	JButton			okButton;
	private	JButton			cancelButton;

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	/**
	 * @throws IllegalArgumentException
	 */

	private ImageRegionSelectionDialog(
		Window			owner,
		Component		referenceComponent,
		BufferedImage	image,
		Rectangle		selection)
	{
		// Call superclass constructor
		super(owner, ModalityType.APPLICATION_MODAL);

		// Validate arguments
		if (image == null)
			throw new IllegalArgumentException("Null image");
		int imageWidth = image.getWidth();
		int imageHeight = image.getHeight();
		if (selection != null)
		{
			if ((selection.x < 0) || (selection.x > imageWidth))
				throw new IllegalArgumentException("Selection x coordinate out of bounds");
			if ((selection.y < 0) || (selection.y > imageHeight))
				throw new IllegalArgumentException("Selection y coordinate out of bounds");
			if ((selection.width < 0) || (selection.x + selection.width > imageWidth))
				throw new IllegalArgumentException("Selection width out of bounds");
			if ((selection.height < 0) || (selection.y + selection.height > imageHeight))
				throw new IllegalArgumentException("Selection height out of bounds");
		}

		// Set properties
		setUndecorated(true);
		if (owner != null)
			setIconImages(owner.getIconImages());

		// Create placeholder component for image
		Dimension imageSize = new Dimension(imageWidth, imageHeight);
		Box.Filler imagePlaceholder = new Box.Filler(new Dimension(1, 1), imageSize, imageSize);

		// Create factory for command buttons
		IFunction1<JButton, String> buttonFactory = text ->
		{
			JButton button = new JButton(text);
			FontUtils.setAppFont(FontKey.MAIN, button);
			button.setMargin(BUTTON_PADDING);
			return button;
		};

		// Button: select all
		selectAllButton = buttonFactory.invoke(SELECT_ALL_STR);
		selectAllButton.addActionListener(event ->
		{
			anchor = null;
			selectedRegion = new Rectangle(0, 0, viewWidth, viewHeight);
			allSelected = true;
			selectionPane.repaint();
			updateButtons();
		});

		// Create right button pane
		JPanel rightButtonPane = new JPanel(new GridLayout(1, 0, 8, 0));

		// Button: OK
		okButton = buttonFactory.invoke(GuiConstants.OK_STR);
		okButton.addActionListener(event ->
		{
			if (selectedRegion != null)
			{
				if (allSelected)
					result = new Selection(null, true);
				else
				{
					int width = selectedRegion.width;
					int height = selectedRegion.height;
					if ((width > 0) && (height > 0))
					{
						Rectangle region = (scaleFactor == 0.0)
													? selectedRegion
													: scale(selectedRegion, scaleFactor, imageWidth, imageHeight);
						result = new Selection(region, false);
					}
				}
				if (result != null)
					onClose();
			}
		});
		rightButtonPane.add(okButton);

		// Button: cancel
		cancelButton = buttonFactory.invoke(GuiConstants.CANCEL_STR);
		cancelButton.addActionListener(event -> onClose());
		rightButtonPane.add(cancelButton);

		// Create button pane
		Box buttonPane = Box.createHorizontalBox();
		buttonPane.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createMatteBorder(1, 0, 0, 0, Colours.LINE_BORDER),
				BorderFactory.createEmptyBorder(2, 8, 2, 8)));
		buttonPane.add(GuiUtils.hBoxFiller());
		buttonPane.add(selectAllButton);
		buttonPane.add(Box.createHorizontalStrut(24));
		buttonPane.add(rightButtonPane);

		// Update buttons
		updateButtons();

		// Create main pane
		Box mainPane = Box.createVerticalBox();
		mainPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOUR));
		mainPane.add(imagePlaceholder);
		mainPane.add(buttonPane);

		// Add commands to action map
		KeyAction.create(mainPane, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT,
						 KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), Command.CANCEL, event ->
		{
			// If no selection, close dialog ...
			if (selectedRegion == null)
				onClose();

			// ... otherwise, clear selection
			else
			{
				allSelected = false;
				selectedRegion = null;
				selectionPane.repaint();
				updateButtons();
			}
		});

		// Set content pane
		setContentPane(mainPane);

		// Dispose of window explicitly
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

		// When dialog is shown, scale image and replace image placeholder with image pane and selection pane; handle
		// window closing
		addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowOpened(
				WindowEvent	event)
			{
				// Get size of image view
				Dimension viewSize = imagePlaceholder.getSize();
				viewWidth = viewSize.width;
				viewHeight = viewSize.height;

				// Initialise coordinates of image view
				int viewX = 0;
				int viewY = 0;

				// Initialise scaled image and scaled selection
				BufferedImage scaledImage = image;
				Rectangle scaledSelection = selection;

				// If view is smaller than image, scale image to fit in view
				if ((imageHeight > 0) && (viewHeight > 0) && ((viewWidth < imageWidth) || (viewHeight < imageHeight)))
				{
					// Calculate aspect ratio of view
					double viewAspectRatio = (double)viewWidth / (double)viewHeight;

					// Calculate aspect ratio of image
					double imageAspectRatio = (double)imageWidth / (double)imageHeight;

					// Calculate changed dimension of view
					if (viewAspectRatio < imageAspectRatio)
					{
						viewHeight = (int)Math.floor((double)viewWidth / imageAspectRatio);
						viewY = (viewSize.height - viewHeight) / 2;
					}
					else
					{
						viewWidth = (int)Math.floor((double)viewHeight * imageAspectRatio);
						viewX = (viewSize.width - viewWidth) / 2;
					}

					// Calculate view-to-image scale factor
					scaleFactor = (imageWidth < imageHeight) ? (double)imageHeight / (double)viewHeight
															 : (double)imageWidth / (double)viewWidth;

					// Create scaled version of image
					Image scaledImage0 = image.getScaledInstance(viewWidth, viewHeight, Image.SCALE_SMOOTH);

					// Convert scaled image to buffered image
					scaledImage = new BufferedImage(viewWidth, viewHeight, BufferedImage.TYPE_INT_RGB);
					Graphics2D gr2d = scaledImage.createGraphics();
					gr2d.setRenderingHint(RenderingHints.KEY_RENDERING,
										  RenderingHints.VALUE_RENDER_QUALITY);
					gr2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
										  RenderingHints.VALUE_INTERPOLATION_BILINEAR);
					gr2d.drawImage(scaledImage0, 0, 0, null);

					// Scale selection
					if ((selection != null) && (scaleFactor > 0.0))
						scaledSelection = scale(selection, 1.0 / scaleFactor, viewWidth, viewHeight);
				}

				// Create container for image pane and selection pane
				JLayeredPane contentPane = new JLayeredPane();
				int layerIndex = 0;

				// Create image pane
				ImagePane imagePane = new ImagePane(scaledImage);
				imagePane.setBounds(viewX, viewY, viewWidth, viewHeight);
				contentPane.add(imagePane, Integer.valueOf(layerIndex++));

				// Create selection pane
				selectionPane = new SelectionPane(scaledSelection);
				selectionPane.setBounds(viewX, viewY, viewWidth, viewHeight);
				contentPane.add(selectionPane, Integer.valueOf(layerIndex++));

				// Fix size of button pane
				Dimension buttonPaneSize = buttonPane.getSize();
				buttonPane.setMinimumSize(buttonPaneSize);
				buttonPane.setMaximumSize(buttonPaneSize);

				// Replace image placeholder with container for image pane and selection pane
				mainPane.remove(imagePlaceholder);
				mainPane.add(contentPane, 0);

				// Update layout of main pane
				mainPane.revalidate();

				// Update buttons
				updateButtons();
			}

			@Override
			public void windowClosing(
				WindowEvent	event)
			{
				onClose();
			}
		});

		// Prevent dialog from being resized
		setResizable(false);

		// Resize dialog to its preferred size
		pack();

		// Get screen bounds for reference component
		Rectangle screenBounds = GuiUtils.getComponentScreenBounds(referenceComponent).bounds();

		// Calculate dimensions and coordinates of dialog
		int width = Math.min(getWidth(), screenBounds.width);
		int height = Math.min(getHeight(), screenBounds.height);
		int x = (screenBounds.width - width) / 2;
		int y = (screenBounds.height - height) / 2;

		// Set location and size of dialog
		setBounds(x, y, width, height);

		// Set default button
		getRootPane().setDefaultButton(okButton);

		// Show dialog
		setVisible(true);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	public static Selection show(
		Component		parent,
		Component		referenceComponent,
		BufferedImage	image,
		Rectangle		selection)
	{
		return new ImageRegionSelectionDialog(GuiUtils.getWindow(parent), referenceComponent, image, selection).result;
	}

	//------------------------------------------------------------------

	private static Rectangle scale(
		Rectangle	rect,
		double		scaleFactor,
		int			maxWidth,
		int			maxHeight)
	{
		int x = Math.min((int)Math.round(scaleFactor * (double)rect.x), maxWidth);
		int y = Math.min((int)Math.round(scaleFactor * (double)rect.y), maxHeight);
		int width = Math.min((int)Math.round(scaleFactor * (double)rect.width), maxWidth);
		int height = Math.min((int)Math.round(scaleFactor * (double)rect.height), maxHeight);
		return new Rectangle(x, y, width, height);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	/**
	 * Returns the selected region of the image.
	 *
	 * @return the selected region of the image, or {@code null} if this dialog was cancelled.
	 */

	public Selection getResult()
	{
		return result;
	}

	//------------------------------------------------------------------

	/**
	 * Returns the specified x coordinate clamped to the closed interval [0, {@link #viewWidth}].  In other words, this
	 * method implements the following function:
	 *
	 * <pre>
	 *        ( 0          if <i>x</i> &lt; 0,
	 * <i>f</i>(<i>x</i>) = ( <i>x</i>          if 0 &lt;= <i>x</i> &lt;= viewWidth,
	 *        ( viewWidth  if <i>x</i> &gt; viewWidth,
	 * </pre>
	 * @param  x
	 *           the x coordinate that will be clamped.
	 * @return {@code x} clamped to the interval [0, {@link #viewWidth}].
	 */

	private int clampX(
		int	x)
	{
		return Math.min(Math.max(0, x), viewWidth);
	}

	//------------------------------------------------------------------

	/**
	 * Returns the specified y coordinate clamped to the closed interval [0, {@link #viewHeight}].  In other words, this
	 * method implements the following function:
	 *
	 * <pre>
	 *        ( 0           if <i>y</i> &lt; 0,
	 * <i>f</i>(<i>y</i>) = ( <i>y</i>           if 0 &lt;= <i>y</i> &lt;= viewHeight,
	 *        ( viewHeight  if <i>y</i> &gt; viewHeight,
	 * </pre>
	 * @param  y
	 *           the y coordinate that will be clamped.
	 * @return {@code y} clamped to the interval [0, {@link #viewHeight}].
	 */

	private int clampY(
		int	y)
	{
		return Math.min(Math.max(0, y), viewHeight);
	}

	//------------------------------------------------------------------

	private void updateButtons()
	{
		boolean notSelecting = (selectionPane == null) || (anchor == null);
		selectAllButton.setEnabled(notSelecting);
		okButton.setEnabled(notSelecting && (selectedRegion != null)
								&& (selectedRegion.width > 0) && (selectedRegion.height > 0));
		cancelButton.setEnabled(notSelecting);

		if (okButton.isEnabled())
			okButton.requestFocusInWindow();
		SwingUtilities.invokeLater(() ->
		{
			if (!okButton.isFocusOwner())
				cancelButton.requestFocusInWindow();
		});
	}

	//------------------------------------------------------------------

	private void onClose()
	{
		setVisible(false);
		dispose();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Enumerated types
////////////////////////////////////////////////////////////////////////


	// ENUMERATION: KINDS OF RESIZING HANDLE


	/**
	 * This is an enumeration of the kinds of resizing handle.  A kind of handle is named for its position relative to
	 * the selection rectangle that contains it.
	 */

	private enum ResizeHandle
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		/**
		 * The handle lies along the top side of the selection rectangle.
		 */
		TOP
		(
			Cursor.N_RESIZE_CURSOR
		)
		{
			@Override
			protected Rectangle bounds(
				Rectangle	container)
			{
				Rectangle bounds = null;
				int width = container.width;
				if ((width >= MIN_CONTAINER_SIZE_LARGE) && (container.height >= MIN_CONTAINER_SIZE_SMALL))
				{
					int dx = DEPTH + GAP;
					bounds = new Rectangle(container.x + dx, container.y, width - 2 * dx, DEPTH);
				}
				return bounds;
			}

			//----------------------------------------------------------

			@Override
			protected void draw(
				Graphics	gr,
				Rectangle	container)
			{
				Rectangle bounds = bounds(container);
				if (bounds != null)
				{
					// Get outer coordinates
					int x1 = bounds.x;
					int x2 = x1 + bounds.width - 1;
					int y1 = bounds.y + 1;
					int y2 = y1 + bounds.height - 2;

					// Draw fill
					gr.setColor(BACKGROUND_COLOUR);
					gr.fillRect(x1 + 1, y1, x2 - x1 - 1, y2 - y1);

					// Draw stroke
					gr.setColor(BORDER_COLOUR);
					Polyline.init()
							.vertex(x1, y1)
							.vertex(x1, y2)
							.vertex(x2, y2)
							.vertex(x2, y1)
							.draw(gr);
				}
			}

			//----------------------------------------------------------
		},

		/**
		 * The handle lies along the bottom side of the selection rectangle.
		 */
		BOTTOM
		(
			Cursor.S_RESIZE_CURSOR
		)
		{
			@Override
			protected Rectangle bounds(
				Rectangle	container)
			{
				Rectangle bounds = null;
				int width = container.width;
				int height = container.height;
				if ((width >= MIN_CONTAINER_SIZE_LARGE) && (height >= MIN_CONTAINER_SIZE_SMALL))
				{
					int dx = DEPTH + GAP;
					bounds = new Rectangle(container.x + dx, container.y + height - DEPTH, width - 2 * dx, DEPTH);
				}
				return bounds;
			}

			//----------------------------------------------------------

			@Override
			protected void draw(
				Graphics	gr,
				Rectangle	container)
			{
				Rectangle bounds = bounds(container);
				if (bounds != null)
				{
					// Get outer coordinates
					int x1 = bounds.x;
					int x2 = x1 + bounds.width - 1;
					int y1 = bounds.y;
					int y2 = y1 + bounds.height - 2;

					// Draw fill
					gr.setColor(BACKGROUND_COLOUR);
					gr.fillRect(x1 + 1, y1 + 1, x2 - x1 - 1, y2 - y1);

					// Draw stroke
					gr.setColor(BORDER_COLOUR);
					Polyline.init()
							.vertex(x1, y2)
							.vertex(x1, y1)
							.vertex(x2, y1)
							.vertex(x2, y2)
							.draw(gr);
				}
			}

			//----------------------------------------------------------
		},

		/**
		 * The handle lies along the left side of the selection rectangle.
		 */
		LEFT
		(
			Cursor.W_RESIZE_CURSOR
		)
		{
			@Override
			protected Rectangle bounds(
				Rectangle	container)
			{
				Rectangle bounds = null;
				int height = container.height;
				if ((container.width >= MIN_CONTAINER_SIZE_SMALL) && (height >= MIN_CONTAINER_SIZE_LARGE))
				{
					int dy = DEPTH + GAP;
					bounds = new Rectangle(container.x, container.y + dy, DEPTH, height - 2 * dy);
				}
				return bounds;
			}

			//----------------------------------------------------------

			@Override
			protected void draw(
				Graphics	gr,
				Rectangle	container)
			{
				Rectangle bounds = bounds(container);
				if (bounds != null)
				{
					// Get outer coordinates
					int x1 = bounds.x + 1;
					int x2 = x1 + bounds.width - 2;
					int y1 = bounds.y;
					int y2 = y1 + bounds.height - 1;

					// Draw fill
					gr.setColor(BACKGROUND_COLOUR);
					gr.fillRect(x1, y1 + 1, x2 - x1, y2 - y1 - 1);

					// Draw stroke
					gr.setColor(BORDER_COLOUR);
					Polyline.init()
							.vertex(x1, y1)
							.vertex(x2, y1)
							.vertex(x2, y2)
							.vertex(x1, y2)
							.draw(gr);
				}
			}

			//----------------------------------------------------------
		},

		/**
		 * The handle lies along the right side of the selection rectangle.
		 */
		RIGHT
		(
			Cursor.E_RESIZE_CURSOR
		)
		{
			@Override
			protected Rectangle bounds(
				Rectangle	container)
			{
				Rectangle bounds = null;
				int width = container.width;
				int height = container.height;
				if ((width >= MIN_CONTAINER_SIZE_SMALL) && (height >= MIN_CONTAINER_SIZE_LARGE))
				{
					int dy = DEPTH + GAP;
					bounds = new Rectangle(container.x + width - DEPTH, container.y + dy, DEPTH, height - 2 * dy);
				}
				return bounds;
			}

			//----------------------------------------------------------

			@Override
			protected void draw(
				Graphics	gr,
				Rectangle	container)
			{
				Rectangle bounds = bounds(container);
				if (bounds != null)
				{
					// Get outer coordinates
					int x1 = bounds.x;
					int x2 = x1 + bounds.width - 2;
					int y1 = bounds.y;
					int y2 = y1 + bounds.height - 1;

					// Draw fill
					gr.setColor(BACKGROUND_COLOUR);
					gr.fillRect(x1 + 1, y1 + 1, x2 - x1, y2 - y1 - 1);

					// Draw stroke
					gr.setColor(BORDER_COLOUR);
					Polyline.init()
							.vertex(x2, y1)
							.vertex(x1, y1)
							.vertex(x1, y2)
							.vertex(x2, y2)
							.draw(gr);
				}
			}

			//----------------------------------------------------------
		},

		/**
		 * The handle is in the top left corner of the selection rectangle.
		 */
		TOP_LEFT
		(
			Cursor.NW_RESIZE_CURSOR
		)
		{
			@Override
			protected Rectangle bounds(
				Rectangle	container)
			{
				Rectangle bounds = null;
				if ((container.width >= MIN_CONTAINER_SIZE_SMALL) && (container.height >= MIN_CONTAINER_SIZE_SMALL))
					bounds = new Rectangle(container.x, container.y, DEPTH, DEPTH);
				return bounds;
			}

			//----------------------------------------------------------

			@Override
			protected void draw(
				Graphics	gr,
				Rectangle	container)
			{
				Rectangle bounds = bounds(container);
				if (bounds != null)
				{
					// Get outer coordinates
					int x1 = bounds.x + 1;
					int x2 = x1 + bounds.width - 2;
					int y1 = bounds.y + 1;
					int y2 = y1 + bounds.height - 2;

					// Draw fill
					gr.setColor(BACKGROUND_COLOUR);
					gr.fillRect(x1, y1, DEPTH - 2, DEPTH - 2);

					// Draw stroke
					gr.setColor(BORDER_COLOUR);
					Polyline.init()
							.vertex(x2, y1)
							.vertex(x2, y2)
							.vertex(x1, y2)
							.draw(gr);
				}
			}

			//----------------------------------------------------------
		},

		/**
		 * The handle is in the top right corner of the selection rectangle.
		 */
		TOP_RIGHT
		(
			Cursor.NE_RESIZE_CURSOR
		)
		{
			@Override
			protected Rectangle bounds(
				Rectangle	container)
			{
				Rectangle bounds = null;
				int width = container.width;
				if ((width >= MIN_CONTAINER_SIZE_SMALL) && (container.height >= MIN_CONTAINER_SIZE_SMALL))
					bounds = new Rectangle(container.x + width - DEPTH, container.y, DEPTH, DEPTH);
				return bounds;
			}

			//----------------------------------------------------------

			@Override
			protected void draw(
				Graphics	gr,
				Rectangle	container)
			{
				Rectangle bounds = bounds(container);
				if (bounds != null)
				{
					// Get outer coordinates
					int x1 = bounds.x;
					int x2 = x1 + bounds.width - 2;
					int y1 = bounds.y + 1;
					int y2 = y1 + bounds.height - 2;

					// Draw fill
					gr.setColor(BACKGROUND_COLOUR);
					gr.fillRect(x1 + 1, y1, DEPTH - 2, DEPTH - 2);

					// Draw stroke
					gr.setColor(BORDER_COLOUR);
					Polyline.init()
							.vertex(x1, y1)
							.vertex(x1, y2)
							.vertex(x2, y2)
							.draw(gr);
				}
			}

			//----------------------------------------------------------
		},

		/**
		 * The handle is in the bottom left corner of the selection rectangle.
		 */
		BOTTOM_LEFT
		(
			Cursor.SW_RESIZE_CURSOR
		)
		{
			@Override
			protected Rectangle bounds(
				Rectangle	container)
			{
				Rectangle bounds = null;
				int height = container.height;
				if ((container.width >= MIN_CONTAINER_SIZE_SMALL) && (height >= MIN_CONTAINER_SIZE_SMALL))
					bounds = new Rectangle(container.x, container.y + height - DEPTH, DEPTH, DEPTH);
				return bounds;
			}

			//----------------------------------------------------------

			@Override
			protected void draw(
				Graphics	gr,
				Rectangle	container)
			{
				Rectangle bounds = bounds(container);
				if (bounds != null)
				{
					// Get outer coordinates
					int x1 = bounds.x + 1;
					int x2 = x1 + bounds.width - 2;
					int y1 = bounds.y;
					int y2 = y1 + bounds.height - 2;

					// Draw fill
					gr.setColor(BACKGROUND_COLOUR);
					gr.fillRect(x1, y1 + 1, DEPTH - 2, DEPTH - 2);

					// Draw stroke
					gr.setColor(BORDER_COLOUR);
					Polyline.init()
							.vertex(x1, y1)
							.vertex(x2, y1)
							.vertex(x2, y2)
							.draw(gr);
				}
			}

			//----------------------------------------------------------
		},

		/**
		 * The handle is in the bottom right corner of the selection rectangle.
		 */
		BOTTOM_RIGHT
		(
			Cursor.SE_RESIZE_CURSOR
		)
		{
			@Override
			protected Rectangle bounds(
				Rectangle	container)
			{
				Rectangle bounds = null;
				int width = container.width;
				int height = container.height;
				if ((width >= MIN_CONTAINER_SIZE_SMALL) && (height >= MIN_CONTAINER_SIZE_SMALL))
					bounds = new Rectangle(container.x + width - DEPTH, container.y + height - DEPTH, DEPTH, DEPTH);
				return bounds;
			}

			//----------------------------------------------------------

			@Override
			protected void draw(
				Graphics	gr,
				Rectangle	container)
			{
				Rectangle bounds = bounds(container);
				if (bounds != null)
				{
					// Get outer coordinates
					int x1 = bounds.x;
					int x2 = x1 + bounds.width - 2;
					int y1 = bounds.y;
					int y2 = y1 + bounds.height - 2;

					// Draw fill
					gr.setColor(BACKGROUND_COLOUR);
					gr.fillRect(x1 + 1, y1 + 1, DEPTH - 2, DEPTH - 2);

					// Draw stroke
					gr.setColor(BORDER_COLOUR);
					Polyline.init()
							.vertex(x2, y1)
							.vertex(x1, y1)
							.vertex(x1, y2)
							.draw(gr);
				}
			}

			//----------------------------------------------------------
		};

		/** The distance that a handle extends into a selection rectangle. */
		private static final	int		DEPTH		= 12;

		/** The gap between a side handle and an adjacent corner handle. */
		private static final	int		GAP	= 3;

		/** The minimum length of a side handle. */
		private static final	int		MIN_LENGTH	= 16;

		/** The minimum size of a side of a selection rectangle that can accommodate two corner handles. */
		private static final	int		MIN_CONTAINER_SIZE_SMALL	= 2 * DEPTH;

		/** The minimum size of a side of a selection rectangle that can accommodate a side handle and two corner
			handles. */
		private static final	int		MIN_CONTAINER_SIZE_LARGE	= 2 * (DEPTH + GAP) + MIN_LENGTH;

		/** The background colour of a resizing handle. */
		private static final	Color	BACKGROUND_COLOUR	= new Color(232, 176, 80, 64);

		/** The border colour of a resizing handle. */
		private static final	Color	BORDER_COLOUR		= new Color(232, 176, 80);

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		/** The identifier of the mouse cursor that is displayed over this kind of resizing handle. */
		private	int	cursorId;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		/**
		 * Creates a new instance of an enumeration constant for a kind of resizing handle of a selection rectangle.
		 *
		 * @param cursorId
		 *          the identifier of the mouse cursor that will be displayed over the kind of handle.
		 */

		private ResizeHandle(
			int	cursorId)
		{
			// Initialise instance variables
			this.cursorId = cursorId;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : abstract methods
	////////////////////////////////////////////////////////////////////

		/**
		 * Returns the bounds of this kind of resizing handle.
		 *
		 * @param  container
		 *           the selection rectangle that contains the resizing handle.
		 * @return the bounds of this kind of resizing handle.
		 */

		protected abstract Rectangle bounds(
			Rectangle	container);

		//--------------------------------------------------------------

		/**
		 * Draws this kind of resizing handle on the specified graphics context.
		 *
		 * @param gr
		 *          the graphics context on which the resizing handle is to be drawn.
		 * @param container
		 *          the selection rectangle that contains the resizing handle.
		 */

		protected abstract void draw(
			Graphics	gr,
			Rectangle	container);

		//--------------------------------------------------------------

	}

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Member records
////////////////////////////////////////////////////////////////////////


	// RECORD: SELECTION


	/**
	 * This record encapsulates information about a selected region of an image.
	 *
	 * @param region
	 *          the selected region, ignored if {@code allSelected} is {@code true}.
	 * @param allSelected
	 *          if {@code true}, the entire image is selected, and {@code region} should be ignored.
	 */

	public record Selection(
		Rectangle	region,
		boolean		allSelected)
	{ }

	//==================================================================


	// RECORD: POLYLINE


	private record Polyline(
		List<Point>	vertices)
	{

	////////////////////////////////////////////////////////////////////
	//  Class methods
	////////////////////////////////////////////////////////////////////

		private static Polyline init()
		{
			return new Polyline(new ArrayList<>());
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		private Polyline vertex(
			int	x,
			int	y)
		{
			vertices.add(new Point(x, y));
			return this;
		}

		//--------------------------------------------------------------

		private void draw(
			Graphics	gr)
		{
			int numVertices = vertices.size();
			int[] xCoords = new int[numVertices];
			int[] yCoords = new int[numVertices];
			for (int i = 0; i < numVertices; i++)
			{
				Point vertex = vertices.get(i);
				xCoords[i] = vertex.x;
				yCoords[i] = vertex.y;
			}
			gr.drawPolyline(xCoords, yCoords, numVertices);
		}

		//--------------------------------------------------------------

	}

	//==================================================================


	// RECORD: ANCHOR


	/**
	 * This record encapsulates the anchor of a selection rectangle, which may be a point or a line segment.
	 *
	 * @param kind
	 *          the kind of anchor.
	 * @param x1
	 *          the first x coordinate of the anchor.
	 * @param y1
	 *          the first y coordinate of the anchor.
	 * @param x2
	 *          the second x coordinate of the anchor.
	 * @param y2
	 *          the second y coordinate of the anchor.
	 * @param dx
	 *          the x displacement of the moving point from the location of the causal event.
	 * @param dy
	 *          the y displacement of the moving point from the location of the causal event.
	 */

	private record Anchor(
		Kind	kind,
		int		x1,
		int		y1,
		int		x2,
		int		y2,
		int		dx,
		int		dy)
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		/**
		 * The kind of anchor.
		 */

		private enum Kind
		{
			/**
			 * The anchor is a point.
			 */
			POINT,

			/**
			 * The anchor is a horizontal line segment.
			 */
			H_LINE,

			/**
			 * The anchor is a vertical line segment.
			 */
			V_LINE
		}

	////////////////////////////////////////////////////////////////////
	//  Class methods
	////////////////////////////////////////////////////////////////////

		/**
		 * Creates and returns a new instance of an anchor that is a point.
		 *
		 * @param  x1
		 *           the first x coordinate of the anchor point.
		 * @param  y1
		 *           the first y coordinate of the anchor point.
		 * @param  dx
		 *           the x displacement of the moving point from the location of the causal event.
		 * @param  dy
		 *           the y displacement of the moving point from the location of the causal event.
		 * @return a new instance of an anchor that is a point.
		 */

		private static Anchor point(
			int	x1,
			int	y1,
			int	dx,
			int	dy)
		{
			return new Anchor(Kind.POINT, x1, y1, 0, 0, dx, dy);
		}

		//--------------------------------------------------------------

		/**
		 * Creates and returns a new instance of an anchor that is a horizontal line segment.
		 *
		 * @param  x1
		 *           the first x coordinate of the anchor line segment.
		 * @param  x2
		 *           the second x coordinate of the anchor line segment.
		 * @param  y
		 *           the y coordinate of the anchor line segment.
		 * @param  dy
		 *           the y displacement of the moving point from the location of the causal event.
		 * @return a new instance of an anchor that is a horizontal line segment.
		 */

		private static Anchor hLine(
			int	x1,
			int	x2,
			int	y,
			int	dy)
		{
			return new Anchor(Kind.H_LINE, x1, y, x2, 0, 0, dy);
		}

		//--------------------------------------------------------------

		/**
		 * Creates and returns a new instance of an anchor that is a vertical line segment.
		 *
		 * @param  x
		 *           the x coordinate of the anchor line segment.
		 * @param  y1
		 *           the first y coordinate of the anchor line segment.
		 * @param  y2
		 *           the second y coordinate of the anchor line segment.
		 * @param  dx
		 *           the x displacement of the moving point from the location of the causal event.
		 * @return a new instance of an anchor that is a vertical line segment.
		 */

		private static Anchor vLine(
			int	x,
			int	y1,
			int	y2,
			int	dx)
		{
			return new Anchor(Kind.V_LINE, x, y1, 0, y2, dx, 0);
		}

		//--------------------------------------------------------------

	}

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Member classes : non-inner classes
////////////////////////////////////////////////////////////////////////


	// CLASS: IMAGE PANE


	private static class ImagePane
		extends JComponent
	{

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	BufferedImage	image;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private ImagePane(
			BufferedImage	image)
		{
			// Initialise instance variables
			this.image = image;

			// Set properties
			setOpaque(true);
			setFocusable(false);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		public Dimension getPreferredSize()
		{
			return new Dimension(image.getWidth(), image.getHeight());
		}

		//--------------------------------------------------------------

		@Override
		protected void paintComponent(
			Graphics	gr)
		{
			gr.drawImage(image, 0, 0, null);
		}

		//--------------------------------------------------------------

	}

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Member classes : inner classes
////////////////////////////////////////////////////////////////////////


	// CLASS: SELECTION PANE


	private class SelectionPane
		extends JComponent
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		/** The fill colour of a selection rectangle. */
		private static final	Color	SELECTION_FILL_COLOUR	= new Color(128, 192, 255, 72);

		/** The stroke colour of a selection rectangle. */
		private static final	Color	SELECTION_STROKE_COLOUR	= new Color(128, 192, 255);

		/** The fill colour of an active selection rectangle. */
		private static final	Color	SELECTION_ACTIVE_FILL_COLOUR	= new Color(248, 192, 112, 64);

		/** The stroke colour of an active selection rectangle. */
		private static final	Color	SELECTION_ACTIVE_STROKE_COLOUR	= new Color(224, 144, 64);

		/** The half-length of the dash of the stroke of an active selection rectangle. */
		private static final	int		SELECTION_STROKE_DASH_HALF_LENGTH	= 3;

		/** The length of the dash of the stroke of an active selection rectangle. */
		private static final	int		SELECTION_STROKE_DASH_LENGTH		= 2 * SELECTION_STROKE_DASH_HALF_LENGTH;

		/** The dashes of the stroke of an active selection rectangle. */
		private static final	Stroke[]	SELECTION_DASHES;

		/** The interval (in milliseconds) between updates of the dashes of the stroke of an active selection
			rectangle. */
		private static final	int		SELECTION_DASH_UPDATE_INTERVAL	= 100;

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	Point	dragPosition;
		private	int		dashIndex;
		private	Timer	dashTimer;

	////////////////////////////////////////////////////////////////////
	//  Static initialiser
	////////////////////////////////////////////////////////////////////

		static
		{
			SELECTION_DASHES = new Stroke[SELECTION_STROKE_DASH_LENGTH];
			float[] dash = { (float)SELECTION_STROKE_DASH_HALF_LENGTH, (float)SELECTION_STROKE_DASH_HALF_LENGTH };
			for (int i = 0; i < SELECTION_DASHES.length; i++)
			{
				SELECTION_DASHES[i] =
						new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash, (float)i);
			}
		}

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private SelectionPane(
			Rectangle	selection)
		{
			// Initialise instance variables
			if (selection != null)
				selectedRegion = new Rectangle(selection);
			dashTimer = new Timer(SELECTION_DASH_UPDATE_INTERVAL, event ->
			{
				if (--dashIndex < 0)
					dashIndex = SELECTION_DASHES.length - 1;
				repaint();
			});

			// Set properties
			setOpaque(false);
			setFocusable(false);
			setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));

			// Add handlers for mouse events
			addMouseListener(new MouseAdapter()
			{
				@Override
				public void mouseExited(
					MouseEvent	event)
				{
					if ((anchor == null) && (resizeHandle != null))
					{
						resizeHandle = null;
						repaint();
					}
				}

				@Override
				public void mousePressed(
					MouseEvent	event)
				{
					if (SwingUtilities.isLeftMouseButton(event) && (anchor == null))
					{
						dragPosition = event.getPoint();
						allSelected = false;
						updateResizeHandle(event);
						if (resizeHandle == null)
						{
							selectedRegion = null;
							anchor = Anchor.point(clampX(dragPosition.x), clampY(dragPosition.y), 0, 0);
						}
						else
						{
							int eventX = dragPosition.x;
							int eventY = dragPosition.y;
							int x1 = selectedRegion.x;
							int x2 = x1 + selectedRegion.width - 1;
							int y1 = selectedRegion.y;
							int y2 = y1 + selectedRegion.height - 1;
							anchor = switch (resizeHandle)
							{
								case TOP          -> Anchor.hLine(x1, x2, y2, y1 - eventY);
								case BOTTOM       -> Anchor.hLine(x1, x2, y1, y2 - eventY);
								case LEFT         -> Anchor.vLine(x2, y1, y2, x1 - eventX);
								case RIGHT        -> Anchor.vLine(x1, y1, y2, x2 - eventX);
								case TOP_LEFT     -> Anchor.point(x2, y2, x1 - eventX, y1 - eventY);
								case TOP_RIGHT    -> Anchor.point(x1, y2, x2 - eventX, y1 - eventY);
								case BOTTOM_LEFT  -> Anchor.point(x2, y1, x1 - eventX, y2 - eventY);
								case BOTTOM_RIGHT -> Anchor.point(x1, y1, x2 - eventX, y2 - eventY);
							};
						}
						dashTimer.start();
						repaint();
						updateButtons();
					}
				}

				@Override
				public void mouseReleased(
					MouseEvent	event)
				{
					if (SwingUtilities.isLeftMouseButton(event) && (anchor != null))
					{
						dashTimer.stop();
						selectedRegion = getActiveSelection(event.getX(), event.getY());
						anchor = null;
						dragPosition = null;
						updateResizeHandle(event);
						repaint();
						updateButtons();
					}
				}
			});
			addMouseMotionListener(new MouseMotionAdapter()
			{
				@Override
				public void mouseMoved(
					MouseEvent	event)
				{
					if (anchor == null)
						updateResizeHandle(event);
				}

				@Override
				public void mouseDragged(
					MouseEvent	event)
				{
					if (SwingUtilities.isLeftMouseButton(event) && (anchor != null))
					{
						dragPosition = event.getPoint();
						repaint();
					}
				}
			});
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		public Dimension getPreferredSize()
		{
			return new Dimension(viewWidth, viewHeight);
		}

		//--------------------------------------------------------------

		@Override
		protected void paintComponent(
			Graphics	gr)
		{
			// Create copy of graphics context
			Graphics2D gr2d = GuiUtils.copyGraphicsContext(gr);

			// Draw active selection
			if (anchor != null)
			{
				// Get rectangle of selection box
				Rectangle rect = getActiveSelection(dragPosition.x, dragPosition.y);

				// Fill active selection rectangle
				gr2d.setColor(SELECTION_ACTIVE_FILL_COLOUR);
				gr2d.fillRect(rect.x, rect.y, rect.width, rect.height);

				// Draw border of active selection rectangle
				gr2d.setStroke(SELECTION_DASHES[dashIndex]);
				gr2d.setColor(SELECTION_ACTIVE_STROKE_COLOUR);
				gr2d.drawRect(rect.x, rect.y, rect.width - 1, rect.height - 1);
			}

			// Draw selection
			else if (selectedRegion != null)
			{
				// Fill selection rectangle
				gr2d.setColor(SELECTION_FILL_COLOUR);
				gr2d.fillRect(selectedRegion.x, selectedRegion.y, selectedRegion.width, selectedRegion.height);

				// Draw border of selection rectangle
				gr2d.setColor(SELECTION_STROKE_COLOUR);
				gr2d.drawRect(selectedRegion.x, selectedRegion.y, selectedRegion.width - 1, selectedRegion.height - 1);

				// Draw resizing handle
				if (resizeHandle != null)
					resizeHandle.draw(gr2d, selectedRegion);
			}
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		private Rectangle getActiveSelection(
			int	x,
			int	y)
		{
			// Get x coordinates of selected region
			int x1 = anchor.x1;
			int x2 = switch (anchor.kind)
			{
				case POINT, V_LINE -> clampX(x + anchor.dx);
				case H_LINE        -> anchor.x2;
			};

			// Sort x coordinates
			if (x1 > x2)
			{
				int temp = x1;
				x1 = x2;
				x2 = temp;
			}

			// Get y coordinates of selected region
			int y1 = anchor.y1;
			int y2 = switch (anchor.kind)
			{
				case POINT, H_LINE -> clampY(y + anchor.dy);
				case V_LINE        -> anchor.y2;
			};

			// Sort y coordinates
			if (y1 > y2)
			{
				int temp = y1;
				y1 = y2;
				y2 = temp;
			}

			// Return result
			return new Rectangle(x1, y1, x2 - x1 + 1, y2 - y1 + 1);
		}

		//--------------------------------------------------------------

		private void updateResizeHandle(
			MouseEvent	event)
		{
			ResizeHandle handle = null;
			if (selectedRegion != null)
			{
				Point point = event.getPoint();
				handle = Stream.of(ResizeHandle.values())
						.filter(h ->
						{
							Rectangle bounds = h.bounds(selectedRegion);
							return (bounds != null) && bounds.contains(point);
						})
						.findFirst()
						.orElse(null);
			}

			if (resizeHandle != handle)
			{
				resizeHandle = handle;
				repaint();
				setCursor(Cursor.getPredefinedCursor((handle == null) ? Cursor.CROSSHAIR_CURSOR : handle.cursorId));
			}
		}

		//--------------------------------------------------------------

	}

	//==================================================================

}

//----------------------------------------------------------------------
