/*====================================================================*\

GridPanel.java

Grid panel base class.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.crosswordeditor;

//----------------------------------------------------------------------


// IMPORTS


import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import uk.blankaspect.common.gui.GuiUtils;
import uk.blankaspect.common.gui.TextRendering;

import uk.blankaspect.common.misc.ArraySet;
import uk.blankaspect.common.misc.KeyAction;

//----------------------------------------------------------------------


// GRID PANEL BASE CLASS


abstract class GridPanel
	extends JComponent
	implements ActionListener, FocusListener, MouseListener
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	// Commands
	private interface Command
	{
		String	MOVE_EDIT_POSITION_UP_UNIT		= "moveEditPositionUpUnit";
		String	MOVE_EDIT_POSITION_DOWN_UNIT	= "moveEditPositionDownUnit";
		String	MOVE_EDIT_POSITION_LEFT_UNIT	= "moveEditPositionLeftUnit";
		String	MOVE_EDIT_POSITION_RIGHT_UNIT	= "moveEditPositionRightUnit";
		String	MOVE_EDIT_POSITION_UP_MAX		= "moveEditPositionUpMax";
		String	MOVE_EDIT_POSITION_DOWN_MAX		= "moveEditPositionDownMax";
		String	MOVE_EDIT_POSITION_LEFT_MAX		= "moveEditPositionLeftMax";
		String	MOVE_EDIT_POSITION_RIGHT_MAX	= "moveEditPositionRightMax";
	}

	private static final	KeyAction.KeyCommandPair[]	EDIT_POSITION_KEY_COMMANDS	=
	{
		new KeyAction.KeyCommandPair
		(
			KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0),
			Command.MOVE_EDIT_POSITION_UP_UNIT
		),
		new KeyAction.KeyCommandPair
		(
			KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0),
			Command.MOVE_EDIT_POSITION_DOWN_UNIT
		),
		new KeyAction.KeyCommandPair
		(
			KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0),
			Command.MOVE_EDIT_POSITION_LEFT_UNIT
		),
		new KeyAction.KeyCommandPair
		(
			KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0),
			Command.MOVE_EDIT_POSITION_RIGHT_UNIT
		),
		new KeyAction.KeyCommandPair
		(
			KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, 0),
			Command.MOVE_EDIT_POSITION_UP_MAX
		),
		new KeyAction.KeyCommandPair
		(
			KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, 0),
			Command.MOVE_EDIT_POSITION_DOWN_MAX
		),
		new KeyAction.KeyCommandPair
		(
			KeyStroke.getKeyStroke(KeyEvent.VK_HOME, 0),
			Command.MOVE_EDIT_POSITION_LEFT_MAX
		),
		new KeyAction.KeyCommandPair
		(
			KeyStroke.getKeyStroke(KeyEvent.VK_END, 0),
			Command.MOVE_EDIT_POSITION_RIGHT_MAX
		)
	};

////////////////////////////////////////////////////////////////////////
//  Member classes : non-inner classes
////////////////////////////////////////////////////////////////////////


	// BLOCK GRID PANEL CLASS


	public static class Block
		extends GridPanel
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		// Commands
		private interface Command
		{
			String	TOGGLE_BLOCK	= "toggleBlock";
		}

		private static final	KeyAction.KeyCommandPair[]	KEY_COMMANDS	=
		{
			new KeyAction.KeyCommandPair(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0),
										 Command.TOGGLE_BLOCK)
		};

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		public Block(Grid grid)
		{
			// Call superclass constructor
			super(grid);

			// Add commands to action map
			KeyAction.create(this, JComponent.WHEN_FOCUSED, this, KEY_COMMANDS);
		}

		//--------------------------------------------------------------

		public Block(CrosswordDocument document)
		{
			// Call superclass constructor
			super(document);

			// Initialise instance fields
			this.grid = (BlockGrid)document.getGrid();
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		public void actionPerformed(ActionEvent event)
		{
			// Call superclass method
			super.actionPerformed(event);

			// Execute command
			String command = event.getActionCommand();

			if (command.equals(Command.TOGGLE_BLOCK))
				onToggleBlock();
		}

		//--------------------------------------------------------------

		@Override
		public Grid getGrid()
		{
			return grid;
		}

		//--------------------------------------------------------------

		@Override
		protected void setGrid(Grid grid)
		{
			if (grid instanceof BlockGrid)
				this.grid = (BlockGrid)grid;
		}

		//--------------------------------------------------------------

		@Override
		protected int[] getFieldNumberOffsets(int row,
											  int column)
		{
			return new int[]{ 1, 0 };
		}

		//--------------------------------------------------------------

		@Override
		protected void drawSeparators(Graphics gr)
		{
			for (int row = 0; row < numRows; row++)
			{
				for (int column = 0; column < numColumns; column++)
				{
					if (grid.getCell(row, column).isBlocked())
						gr.fillRect(column * cellSize + 1, row * cellSize + 1,
									cellSize - 1, cellSize - 1);
				}
			}
		}

		//--------------------------------------------------------------

		@Override
		protected void drawSeparator(Graphics gr,
									 int      row,
									 int      column)
		{
			if (grid.getCell(row, column).isBlocked())
				gr.fillRect(column * cellSize + 1, row * cellSize + 1, cellSize - 1, cellSize - 1);
		}

		//--------------------------------------------------------------

		@Override
		protected boolean toggleSeparator(int x,
										  int y)
		{
			return toggleBlock(y / cellSize, x / cellSize);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		private boolean toggleBlock(int row,
									int column)
		{
			grid.toggleBlock(row, column);
			updateHighlightedCells();
			repaint();
			fireStateChanged();
			return true;
		}

		//--------------------------------------------------------------

		private void onToggleBlock()
		{
			toggleBlock(editPosition.row, editPosition.column);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance fields
	////////////////////////////////////////////////////////////////////

		private	BlockGrid	grid;

	}

	//==================================================================


	// BAR GRID PANEL CLASS


	public static class Bar
		extends GridPanel
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		public static final		int	MIN_BAR_WIDTH		= 2;
		public static final		int	MAX_BAR_WIDTH		= 20;
		public static final		int	DEFAULT_BAR_WIDTH	= 4;

		private static final	int	BAR_ZONE_HALF_WIDTH	= 4;

		private static final	Map<BarGrid.Edge, Integer>	EDGE_SELECTORS;

		// Commands
		private interface Command
		{
			String	TOGGLE_BAR_TOP		= "toggleBarTop";
			String	TOGGLE_BAR_RIGHT	= "toggleBarRight";
			String	TOGGLE_BAR_BOTTOM	= "toggleBarBottom";
			String	TOGGLE_BAR_LEFT		= "toggleBarLeft";
		}

		private static final	KeyAction.KeyCommandPair[]	KEY_COMMANDS	=
		{
			new KeyAction.KeyCommandPair
			(
				KeyStroke.getKeyStroke(KeyEvent.VK_UP, KeyEvent.ALT_DOWN_MASK),
				Command.TOGGLE_BAR_TOP
			),
			new KeyAction.KeyCommandPair
			(
				KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, KeyEvent.ALT_DOWN_MASK),
				Command.TOGGLE_BAR_RIGHT
			),
			new KeyAction.KeyCommandPair
			(
				KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, KeyEvent.ALT_DOWN_MASK),
				Command.TOGGLE_BAR_BOTTOM
			),
			new KeyAction.KeyCommandPair
			(
				KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, KeyEvent.ALT_DOWN_MASK),
				Command.TOGGLE_BAR_LEFT
			),
		};

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		public Bar(Grid grid)
		{
			// Call superclass constructor
			super(grid);

			// Add commands to action map
			KeyAction.create(this, JComponent.WHEN_FOCUSED, this, KEY_COMMANDS);
		}

		//--------------------------------------------------------------

		public Bar(CrosswordDocument document)
		{
			// Call superclass constructor
			super(document);

			// Initialise instance fields
			this.grid = (BarGrid)document.getGrid();
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Class methods
	////////////////////////////////////////////////////////////////////

		private static BarGrid.Edge getEdgeForSelector(int selector)
		{
			for (BarGrid.Edge edge : BarGrid.Edge.values())
			{
				if (EDGE_SELECTORS.get(edge) == selector)
					return edge;
			}
			return null;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		public void actionPerformed(ActionEvent event)
		{
			// Call superclass method
			super.actionPerformed(event);

			// Execute command
			String command = event.getActionCommand();

			if (command.equals(Command.TOGGLE_BAR_TOP))
				onToggleBarTop();

			else if (command.equals(Command.TOGGLE_BAR_RIGHT))
				onToggleBarRight();

			else if (command.equals(Command.TOGGLE_BAR_BOTTOM))
				onToggleBarBottom();

			else if (command.equals(Command.TOGGLE_BAR_LEFT))
				onToggleBarLeft();
		}

		//--------------------------------------------------------------

		@Override
		public Grid getGrid()
		{
			return grid;
		}

		//--------------------------------------------------------------

		@Override
		protected void setGrid(Grid grid)
		{
			if (grid instanceof BarGrid)
				this.grid = (BarGrid)grid;
		}

		//--------------------------------------------------------------

		@Override
		protected int[] getFieldNumberOffsets(int row,
											  int column)
		{
			int barWidthIn = (AppConfig.INSTANCE.getBarGridBarWidth() - 1) / 2;
			return new int[]{ cellHasLeftBar(row, column) ? barWidthIn + 1 : 1,
							  cellHasTopBar(row, column) ? barWidthIn : 0 };
		}

		//--------------------------------------------------------------

		@Override
		protected void drawSeparators(Graphics gr)
		{
			int barWidth = AppConfig.INSTANCE.getBarGridBarWidth();
			int barWidthIn = (barWidth - 1) / 2;
			int barWidthOut = barWidth / 2;
			for (int row = 0; row < numRows; row++)
			{
				for (int column = 0; column < numColumns; column++)
				{
					// Draw top bar
					if (cellHasTopBar(row, column))
					{
						int x = column * cellSize + 1;
						int y = row * cellSize - barWidthOut;
						int width = cellSize - 1;
						boolean left0 = (row > 0) && cellHasLeftBar(row - 1, column);
						boolean left1 = cellHasLeftBar(row, column);
						if ((column > 0) && (left0 || left1) &&
							 !(cellHasTopBar(row, column - 1) || (left0 && left1)))
						{
							x -= barWidthOut + 1;
							width += barWidthOut + 1;
						}
						if (column < numColumns - 1)
						{
							left0 = (row > 0) && cellHasLeftBar(row - 1, column + 1);
							left1 = cellHasLeftBar(row, column + 1);
							if ((left0 || left1) &&
								 !(cellHasTopBar(row, column + 1) || (left0 && left1)))
								width += barWidthIn + 1;
						}
						gr.fillRect(x, y, width, barWidth);
					}

					// Draw left bar
					if (cellHasLeftBar(row, column))
					{
						int x = column * cellSize - barWidthOut;
						int y = row * cellSize + 1;
						int height = cellSize - 1;
						boolean top0 = (column > 0) && cellHasTopBar(row, column - 1);
						boolean top1 = cellHasTopBar(row, column);
						if ((row > 0) && (top0 || top1) &&
							 !(cellHasLeftBar(row - 1, column) || (top0 && top1)))
						{
							y -= barWidthOut + 1;
							height += barWidthOut + 1;
						}
						if (row < numRows - 1)
						{
							top0 = (column > 0) && cellHasTopBar(row + 1, column - 1);
							top1 = cellHasTopBar(row + 1, column);
							if ((top0 || top1) &&
								 !(cellHasLeftBar(row + 1, column) || (top0 && top1)))
								height += barWidthIn + 1;
						}
						gr.fillRect(x, y, barWidth, height);
					}
				}
			}
		}

		//--------------------------------------------------------------

		@Override
		protected void drawSeparator(Graphics gr,
									 int      row,
									 int      column)
		{
			int barWidth = AppConfig.INSTANCE.getBarGridBarWidth();
			int barWidthIn = (barWidth - 1) / 2;
			int barWidthOut = barWidth / 2;
			BarGrid.Cell cell = grid.getCell(row, column);
			if (cell.hasBar(BarGrid.Edge.TOP))
				gr.fillRect(column * cellSize + 1, row * cellSize + 1, cellSize - 1, barWidthIn);
			if (cell.hasBar(BarGrid.Edge.LEFT))
				gr.fillRect(column * cellSize + 1, row * cellSize + 1, barWidthIn, cellSize - 1);
			if (cell.hasBar(BarGrid.Edge.BOTTOM))
				gr.fillRect(column * cellSize + 1, (row + 1) * cellSize - barWidthOut, cellSize - 1,
							barWidthOut);
			if (cell.hasBar(BarGrid.Edge.RIGHT))
				gr.fillRect((column + 1) * cellSize - barWidthOut, row * cellSize + 1, barWidthOut,
							cellSize - 1);
		}

		//--------------------------------------------------------------

		@Override
		protected boolean toggleSeparator(int x,
										  int y)
		{
			int a = BAR_ZONE_HALF_WIDTH + 1;
			int b = cellSize - BAR_ZONE_HALF_WIDTH;
			int dx = x % cellSize;
			int selectorX = (dx < a) ? 0
									 : (dx < b) ? 1 : 2;
			int dy = y % cellSize;
			int selectorY = (dy < a) ? 0
									 : (dy < b) ? 1 : 2;
			BarGrid.Edge edge = getEdgeForSelector((selectorX << 4) | selectorY);
			return ((edge != null) && toggleBar(y / cellSize, x / cellSize, edge));
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		private boolean cellHasTopBar(int row,
									  int column)
		{
			return grid.getCell(row, column).hasBar(BarGrid.Edge.TOP);
		}

		//--------------------------------------------------------------

		private boolean cellHasLeftBar(int row,
									   int column)
		{
			return grid.getCell(row, column).hasBar(BarGrid.Edge.LEFT);
		}

		//--------------------------------------------------------------

		private boolean toggleBar(int          row,
								  int          column,
								  BarGrid.Edge edge)
		{
			switch (edge)
			{
				case LEFT:
					if (column == 0)
						edge = null;
					break;

				case TOP:
					if (row == 0)
						edge = null;
					break;

				case BOTTOM:
					if (row < numRows - 1)
					{
						edge = BarGrid.Edge.TOP;
						++row;
					}
					else
						edge = null;
					break;

				case RIGHT:
					if (column < numColumns - 1)
					{
						edge = BarGrid.Edge.LEFT;
						++column;
					}
					else
						edge = null;
					break;
			}
			if (edge == null)
				return false;

			grid.toggleBar(row, column, edge);
			updateHighlightedCells();
			repaint();
			fireStateChanged();
			return true;
		}

		//--------------------------------------------------------------

		private void onToggleBarTop()
		{
			toggleBar(editPosition.row, editPosition.column, BarGrid.Edge.TOP);
		}

		//--------------------------------------------------------------

		private void onToggleBarRight()
		{
			toggleBar(editPosition.row, editPosition.column, BarGrid.Edge.RIGHT);
		}

		//--------------------------------------------------------------

		private void onToggleBarBottom()
		{
			toggleBar(editPosition.row, editPosition.column, BarGrid.Edge.BOTTOM);
		}

		//--------------------------------------------------------------

		private void onToggleBarLeft()
		{
			toggleBar(editPosition.row, editPosition.column, BarGrid.Edge.LEFT);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Static initialiser
	////////////////////////////////////////////////////////////////////

		static
		{
			EDGE_SELECTORS = new EnumMap<>(BarGrid.Edge.class);
			EDGE_SELECTORS.put(BarGrid.Edge.TOP,    0x10);
			EDGE_SELECTORS.put(BarGrid.Edge.RIGHT,  0x21);
			EDGE_SELECTORS.put(BarGrid.Edge.BOTTOM, 0x12);
			EDGE_SELECTORS.put(BarGrid.Edge.LEFT,   0x01);
		}

	////////////////////////////////////////////////////////////////////
	//  Instance fields
	////////////////////////////////////////////////////////////////////

		private	BarGrid	grid;

	}

	//==================================================================


	// CARET CLASS


	private static class Caret
		implements ActionListener
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		private static final	int	BLINK_INTERVAL	= 400;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private Caret()
		{
			timer = new Timer(BLINK_INTERVAL, this);
			timer.start();
			drawn = true;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : ActionListener interface
	////////////////////////////////////////////////////////////////////

		public void actionPerformed(ActionEvent event)
		{
			drawn = !drawn;
			show();
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		private void setVisible(boolean visible)
		{
			if (this.visible != visible)
			{
				this.visible = visible;
				show();
			}
		}

		//--------------------------------------------------------------

		private void show()
		{
			CrosswordView view = App.INSTANCE.getView();
			if (view != null)
				view.drawCaret(visible && drawn);
		}

		//--------------------------------------------------------------

		private void reset()
		{
			timer.restart();
			drawn = true;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance fields
	////////////////////////////////////////////////////////////////////

		private	boolean	visible;
		private	boolean	drawn;
		private	Timer	timer;

	}

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	protected GridPanel(Grid grid)
	{
		// Initialise panel for editing
		this(grid.getNumColumns(), grid.getNumRows());
		editPosition = new Grid.IndexPair(0, 0);
		changeListeners = new ArrayList<>();
		setGrid(grid);

		// Update highlighted cells
		updateHighlightedCells();

		// Add commands to action map
		KeyAction.create(this, JComponent.WHEN_FOCUSED, this, EDIT_POSITION_KEY_COMMANDS);
	}

	//------------------------------------------------------------------

	protected GridPanel(CrosswordDocument document)
	{
		this(document.getGrid().getNumColumns(), document.getGrid().getNumRows());
		this.document = document;
	}

	//------------------------------------------------------------------

	private GridPanel(int numColumns,
					  int numRows)
	{
		// Initialise instance fields
		this.numColumns = numColumns;
		this.numRows = numRows;
		cellSize = AppConfig.INSTANCE.getGridCellSize(Grid.Separator.BLOCK);
		selectedFields = new Clue.FieldList();
		isolatedCells = new ArrayList<>();
		fullyIntersectingFieldCells = new ArraySet<>();

		// Set component attributes
		AppFont.GRID_ENTRY.apply(this);
		setOpaque(true);
		setFocusable(true);

		// Add listeners
		addFocusListener(this);
		addMouseListener(this);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Abstract methods
////////////////////////////////////////////////////////////////////////

	public abstract Grid getGrid();

	//------------------------------------------------------------------

	protected abstract void setGrid(Grid grid);

	//------------------------------------------------------------------

	protected abstract int[] getFieldNumberOffsets(int row,
												   int column);

	//------------------------------------------------------------------

	protected abstract void drawSeparators(Graphics gr);

	//------------------------------------------------------------------

	protected abstract void drawSeparator(Graphics gr,
										  int      row,
										  int      column);

	//------------------------------------------------------------------

	protected abstract boolean toggleSeparator(int x,
											   int y);

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : ActionListener interface
////////////////////////////////////////////////////////////////////////

	public void actionPerformed(ActionEvent event)
	{
		String command = event.getActionCommand();

		if (command.equals(Command.MOVE_EDIT_POSITION_UP_UNIT))
			onMoveEditPositionUpUnit();

		else if (command.equals(Command.MOVE_EDIT_POSITION_DOWN_UNIT))
			onMoveEditPositionDownUnit();

		else if (command.equals(Command.MOVE_EDIT_POSITION_LEFT_UNIT))
			onMoveEditPositionLeftUnit();

		else if (command.equals(Command.MOVE_EDIT_POSITION_RIGHT_UNIT))
			onMoveEditPositionRightUnit();

		else if (command.equals(Command.MOVE_EDIT_POSITION_UP_MAX))
			onMoveEditPositionUpMax();

		else if (command.equals(Command.MOVE_EDIT_POSITION_DOWN_MAX))
			onMoveEditPositionDownMax();

		else if (command.equals(Command.MOVE_EDIT_POSITION_LEFT_MAX))
			onMoveEditPositionLeftMax();

		else if (command.equals(Command.MOVE_EDIT_POSITION_RIGHT_MAX))
			onMoveEditPositionRightMax();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : FocusListener interface
////////////////////////////////////////////////////////////////////////

	public void focusGained(FocusEvent event)
	{
		caret.setVisible(true);
		repaint();
	}

	//------------------------------------------------------------------

	public void focusLost(FocusEvent event)
	{
		caret.setVisible(false);
		repaint();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : MouseListener interface
////////////////////////////////////////////////////////////////////////

	public void mouseClicked(MouseEvent event)
	{
		// do nothing
	}

	//------------------------------------------------------------------

	public void mouseEntered(MouseEvent event)
	{
		// do nothing
	}

	//------------------------------------------------------------------

	public void mouseExited(MouseEvent event)
	{
		// do nothing
	}

	//------------------------------------------------------------------

	public void mousePressed(MouseEvent event)
	{
		if (SwingUtilities.isLeftMouseButton(event))
		{
			requestFocusInWindow();
			if (isEditing())
			{
				int x = event.getX();
				int y = event.getY();
				int row = y / cellSize;
				int column = x / cellSize;
				if ((row >= 0) && (row < numRows) && (column >= 0) && (column < numColumns))
				{
					boolean repaint = false;
					if ((row != editPosition.row) || (column != editPosition.column))
					{
						editPosition.set(row, column);
						repaint = true;
					}
					if (toggleSeparator(x, y))
						repaint = false;
					if (repaint)
						repaint();
				}
			}
		}
	}

	//------------------------------------------------------------------

	public void mouseReleased(MouseEvent event)
	{
		// do nothing
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : overriding methods
////////////////////////////////////////////////////////////////////////

	@Override
	public Dimension getPreferredSize()
	{
		return new Dimension(numColumns * cellSize + 1, numRows * cellSize + 1);
	}

	//------------------------------------------------------------------

	@Override
	protected void paintComponent(Graphics gr)
	{
		// Create copy of graphics context
		gr = gr.create();

		// Set rendering hints for text antialiasing and fractional metrics
		TextRendering.setHints((Graphics2D)gr);

		// Get clip bounds
		Rectangle rect = gr.getClipBounds();

		// Get row and column indices and grid
		int row = rect.y / cellSize;
		int column = rect.x / cellSize;
		Grid grid = getGrid();

		// Draw interior of a single cell
		AppConfig config = AppConfig.INSTANCE;
		if (!isEditing()
			&& ((rect.x + rect.width - 1) / cellSize == column) && ((rect.y + rect.height - 1) / cellSize == row))
		{
			// Fill background
			Color fillColour = null;
			if (grid.isIncorrectEntries() && grid.isIncorrectEntry(row, column))
				fillColour = config.getViewColour(CrosswordView.Colour.ISOLATED_CELL_BACKGROUND);
			else
			{
				for (Grid.Field field : selectedFields.fields)
				{
					if (field.containsCell(row, column))
					{
						fillColour = config.getViewColour((isFocusOwner() && selectedFields.enabled)
													? CrosswordView.Colour.FOCUSED_SELECTED_FIELD_BACKGROUND
													: CrosswordView.Colour.SELECTED_FIELD_BACKGROUND);
						break;
					}
				}
				if (fillColour == null)
					fillColour = config.getViewColour(CrosswordView.Colour.BACKGROUND);
			}
			gr.setColor(fillColour);
			gr.fillRect(rect.x, rect.y, rect.width, rect.height);

			// Draw separator
			gr.setColor(config.getViewColour(CrosswordView.Colour.GRID_LINE));
			drawSeparator(gr, row, column);

			// Draw field number
			int x = column * cellSize;
			int y = row * cellSize;
			if (document.isShowFieldNumbers())
			{
				int fieldNumber = grid.getCell(row, column).getFieldNumber();
				if (fieldNumber > 0)
				{
					gr.setColor(config.getViewColour(CrosswordView.Colour.FIELD_NUMBER_TEXT));
					gr.setFont(AppFont.FIELD_NUMBER.getFont());
					int[] offsets = getFieldNumberOffsets(row, column);
					gr.drawString(Integer.toString(fieldNumber), x + offsets[0],
								  y + gr.getFontMetrics().getAscent() + offsets[1]);
				}
			}

			// Draw entry character
			++x;
			++y;
			if (grid.isEntryValue(row, column))
			{
				gr.setColor(config.getViewColour(CrosswordView.Colour.GRID_ENTRY_TEXT));
				gr.setFont(AppFont.GRID_ENTRY.getFont());
				FontMetrics fontMetrics = gr.getFontMetrics();
				char ch = grid.getEntryValue(row, column);
				gr.drawString(Character.toString(ch), x + (cellSize - fontMetrics.charWidth(ch)) / 2,
							  y + GuiUtils.getBaselineOffset(cellSize, fontMetrics));
			}

			// Draw caret
			if (caretDrawnPosition != null)
			{
				gr.setColor(config.getViewColour(CrosswordView.Colour.CARET));
				gr.drawRect(x, y, cellSize - 2, cellSize - 2);
			}
		}

		// Draw entire component
		else
		{
			// Fill background
			gr.setColor(config.getViewColour(CrosswordView.Colour.BACKGROUND));
			gr.fillRect(rect.x, rect.y, rect.width, rect.height);

			// Fill background of highlighted cells
			if (isEditing())
			{
				gr.setColor(config.getViewColour(CrosswordView.Colour.ISOLATED_CELL_BACKGROUND));
				for (Grid.IndexPair indices : isolatedCells)
					gr.fillRect(indices.column * cellSize + 1, indices.row * cellSize + 1, cellSize - 1, cellSize - 1);
				gr.setColor(config.getViewColour(CrosswordView.Colour.FULLY_INTERSECTING_FIELD_BACKGROUND));
				for (Grid.IndexPair indices : fullyIntersectingFieldCells)
					gr.fillRect(indices.column * cellSize + 1, indices.row * cellSize + 1, cellSize - 1, cellSize - 1);
			}

			// Fill background of selected fields
			gr.setColor(config.getViewColour((isFocusOwner() && selectedFields.enabled)
													? CrosswordView.Colour.FOCUSED_SELECTED_FIELD_BACKGROUND
													: CrosswordView.Colour.SELECTED_FIELD_BACKGROUND));
			for (Grid.Field field : selectedFields.fields)
			{
				int x = field.getColumn() * cellSize;
				int y = field.getRow() * cellSize;
				int width = 0;
				int height = 0;
				switch (field.getDirection())
				{
					case NONE:
						// do nothing
						break;

					case ACROSS:
						width = field.getLength() * cellSize;
						height = cellSize;
						break;

					case DOWN:
						width = cellSize;
						height = field.getLength() * cellSize;
						break;
				}
				gr.fillRect(x, y, width, height);
			}

			// Fill background of cells with incorrect entries
			if (grid.isIncorrectEntries())
			{
				gr.setColor(config.getViewColour(CrosswordView.Colour.ISOLATED_CELL_BACKGROUND));
				for (row = 0; row < numRows; row++)
				{
					for (column = 0; column < numColumns; column++)
					{
						if (grid.isIncorrectEntry(row, column))
							gr.fillRect(column * cellSize + 1, row * cellSize + 1, cellSize - 1, cellSize - 1);
					}
				}
			}

			// Draw vertical grid lines
			gr.setColor(config.getViewColour(CrosswordView.Colour.GRID_LINE));
			int x = 0;
			int y1 = 0;
			int y2 = getHeight() - 1;
			for (column = 0; column <= numColumns; column++)
			{
				gr.drawLine(x, y1, x, y2);
				x += cellSize;
			}

			// Draw horizontal grid lines
			int y = 0;
			int x1 = 0;
			int x2 = getWidth() - 1;
			for (row = 0; row <= numRows; row++)
			{
				gr.drawLine(x1, y, x2, y);
				y += cellSize;
			}

			// Draw separators
			drawSeparators(gr);

			// Draw editing box
			if (isEditing())
			{
				x = editPosition.column * cellSize;
				y = editPosition.row * cellSize;
				gr.setColor(config.getViewColour(isFocusOwner() ? CrosswordView.Colour.FOCUSED_EDITING_BOX
																: CrosswordView.Colour.EDITING_BOX));
				gr.drawRect(x, y, cellSize, cellSize);
				gr.drawRect(x + 1, y + 1, cellSize - 2, cellSize - 2);
			}

			// Draw cell contents
			else
			{
				// Draw field numbers
				if (document.isShowFieldNumbers())
				{
					gr.setColor(config.getViewColour(CrosswordView.Colour.FIELD_NUMBER_TEXT));
					gr.setFont(AppFont.FIELD_NUMBER.getFont());
					FontMetrics fontMetrics = gr.getFontMetrics();
					int textY = fontMetrics.getAscent();
					for (row = 0; row < numRows; row++)
					{
						for (column = 0; column < numColumns; column++)
						{
							int fieldNumber = grid.getCell(row, column).getFieldNumber();
							if (fieldNumber > 0)
							{
								int[] offsets = getFieldNumberOffsets(row, column);
								gr.drawString(Integer.toString(fieldNumber), column * cellSize + offsets[0],
											  row * cellSize + textY + offsets[1]);
							}
						}
					}
				}

				// Draw entries
				gr.setColor(config.getViewColour(CrosswordView.Colour.GRID_ENTRY_TEXT));
				gr.setFont(AppFont.GRID_ENTRY.getFont());
				FontMetrics fontMetrics = gr.getFontMetrics();
				for (row = 0; row < numRows; row++)
				{
					for (column = 0; column < numColumns; column++)
					{
						if (grid.isEntryValue(row, column))
						{
							char ch = grid.getEntryValue(row, column);
							int charWidth = fontMetrics.charWidth(ch);
							x = column * cellSize + 1 + (cellSize - charWidth) / 2;
							y = row * cellSize + 1 + GuiUtils.getBaselineOffset(cellSize, fontMetrics);
							gr.drawString(Character.toString(ch), x, y);
						}
					}
				}

				// Draw caret
				if (caretDrawnPosition != null)
				{
					row = caretDrawnPosition.row;
					column = caretDrawnPosition.column;
					x = column * cellSize + 1;
					y = row * cellSize + 1;
					gr.setColor(config.getViewColour(CrosswordView.Colour.CARET));
					gr.drawRect(x, y, cellSize - 2, cellSize - 2);
				}
			}
		}
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	public int getCellSize()
	{
		return cellSize;
	}

	//------------------------------------------------------------------

	public int getNumFields(Direction direction)
	{
		return getGrid().getNumFields(direction);
	}

	//------------------------------------------------------------------

	public Grid.Field getSelectedField()
	{
		return selectedFields.getField();
	}

	//------------------------------------------------------------------

	public boolean setEntryChar(char value,
								int  increment)
	{
		if (caretPosition == null)
			return false;

		CrosswordDocument.Command command = CrosswordDocument.Command.SET_ENTRY_CHARACTER;
		command.putValue(CrosswordDocument.Command.Property.GRID_ENTRY_VALUE,
						 new Grid.EntryValue(caretPosition.row, caretPosition.column, value));
		command.putValue(CrosswordDocument.Command.Property.DIRECTION, getSelectedFieldDirection());
		command.execute();
		repaint(caretPosition.column * cellSize + 1, caretPosition.row * cellSize + 1,
				cellSize - 1, cellSize - 1);
		return true;
	}

	//------------------------------------------------------------------

	public void setSymmetry(Grid.Symmetry symmetry)
	{
		Grid grid = getGrid();
		if (grid.getSymmetry() != symmetry)
		{
			grid.setSymmetry(symmetry);
			updateHighlightedCells();
			repaint();
		}
	}

	//------------------------------------------------------------------

	public void setHighlightFullyIntersecting(boolean highlight)
	{
		if (highlightFullyIntersectingFields != highlight)
		{
			highlightFullyIntersectingFields = highlight;
			updateHighlightedCells();
			repaint();
		}
	}

	//------------------------------------------------------------------

	public void addChangeListener(ChangeListener listener)
	{
		changeListeners.add(listener);
	}

	//------------------------------------------------------------------

	public void setCaretPosition(int       row,
								 int       column,
								 Direction direction)
	{
		setSelection(getClueFields(row, column, direction), new Grid.IndexPair(row, column));
	}

	//------------------------------------------------------------------

	public Clue.FieldList setSelection(Direction direction)
	{
		Clue.FieldList fields = null;
		if (caretPosition != null)
		{
			fields = getClueFields(caretPosition.row, caretPosition.column, direction);
			if (fields != null)
				setSelection(fields, caretPosition);
		}
		return fields;
	}

	//------------------------------------------------------------------

	public void setSelection(Clue.FieldList fields)
	{
		setSelection(fields,
					 (fields.enabled && (fields.getField() != null)) ? fields.getField().getStartIndices() : null);
	}

	//------------------------------------------------------------------

	public void setSelection(Clue.FieldList fields,
							 Grid.IndexPair position)
	{
		if (!fields.equals(selectedFields) ||
			((position == null) ? (caretPosition != null) : !position.equals(caretPosition)))
		{
			selectedFields = fields.clone();
			caretPosition = (position == null) ? null : position.clone();
			caretDrawnPosition = null;
			repaint();
			caret.reset();
			drawCaret(true);
		}
	}

	//------------------------------------------------------------------

	public void drawCaret(boolean draw)
	{
		// Draw caret
		if (draw)
		{
			if (caretPosition != null)
			{
				caretDrawnPosition = caretPosition;
				repaint(caretDrawnPosition.column * cellSize + 1, caretDrawnPosition.row * cellSize + 1, cellSize - 1,
						cellSize - 1);
			}
		}

		// Erase caret
		else
		{
			if (caretDrawnPosition != null)
			{
				int x = caretDrawnPosition.column * cellSize + 1;
				int y = caretDrawnPosition.row * cellSize + 1;
				caretDrawnPosition = null;
				repaint(x, y, cellSize - 1, cellSize - 1);
			}
		}
	}

	//------------------------------------------------------------------

	public Clue.FieldList incrementCaretPosition(int increment)
	{
		switch (getSelectedFieldDirection())
		{
			case NONE:
				// do nothing
				break;

			case ACROSS:
				return incrementCaretColumn(increment);

			case DOWN:
				return incrementCaretRow(increment);
		}
		return null;
	}

	//------------------------------------------------------------------

	public Clue.FieldList incrementCaretColumn(int increment)
	{
		Clue.FieldList fields = null;
		if ((caretPosition == null) && selectedFields.enabled)
			caretPosition = selectedFields.getField().getStartIndices();
		if (caretPosition != null)
		{
			Grid grid = getGrid();
			Grid.IndexPair position = caretPosition.clone();
			int absIncrement = Math.abs(increment);
			boolean navigateOverSeparators = AppConfig.INSTANCE.isNavigateOverGridSeparators();
			fields = getClueFields(position.row, position.column, Direction.ACROSS);
			if (fields == null)
			{
				if (absIncrement == 1)
				{
					if (navigateOverSeparators)
						fields = incrementCaretColumn(position, increment);
					else
					{
						List<Grid.Field> flds = grid.findFields(position.row, position.column, Direction.ACROSS);
						if (!flds.isEmpty())
						{
							position.column += increment;
							if ((position.column >= 0) && (position.column < numColumns))
							{
								Grid.Field.Id id = flds.get(0).getId();
								flds = grid.findFields(position.row, position.column, Direction.ACROSS);
								if (!flds.isEmpty() && flds.get(0).getId().equals(id))
									fields = getClueFields(position.row, position.column, Direction.DOWN);
							}
						}
					}
				}
			}
			else
			{
				Grid.Field field = fields.getField();
				if (absIncrement > 1)
					position.column = (increment < 0) ? field.getColumn() : field.getEndColumn();
				else
				{
					int column = position.column + increment;
					if ((column >= field.getColumn()) && (column <= field.getEndColumn()))
						position.column = column;
					else
					{
						int index = fields.index;
						if (index >= 0)
						{
							if (column < field.getColumn())
							{
								if (index > 0)
								{
									--fields.index;
									position = fields.getField().getEndIndices();
								}
							}
							else
							{
								if (index < fields.getNumFields() - 1)
								{
									++fields.index;
									position = fields.getField().getStartIndices();
								}
							}
						}
						if (navigateOverSeparators && (fields.index == index))
							fields = incrementCaretColumn(position, increment);
					}
				}
			}
			if (fields != null)
				setSelection(fields, position);
		}
		return fields;
	}

	//------------------------------------------------------------------

	public Clue.FieldList incrementCaretRow(int increment)
	{
		Clue.FieldList fields = null;
		if ((caretPosition == null) && selectedFields.enabled)
			caretPosition = selectedFields.getField().getStartIndices();
		if (caretPosition != null)
		{
			Grid grid = getGrid();
			Grid.IndexPair position = caretPosition.clone();
			int absIncrement = Math.abs(increment);
			boolean navigateOverSeparators = AppConfig.INSTANCE.isNavigateOverGridSeparators();
			fields = getClueFields(position.row, position.column, Direction.DOWN);
			if (fields == null)
			{
				if (absIncrement == 1)
				{
					if (navigateOverSeparators)
						fields = incrementCaretRow(position, increment);
					else
					{
						List<Grid.Field> flds = grid.findFields(position.row, position.column,
																Direction.DOWN);
						if (!flds.isEmpty())
						{
							position.row += increment;
							if ((position.row >= 0) && (position.row < numRows))
							{
								Grid.Field.Id id = flds.get(0).getId();
								flds = grid.findFields(position.row, position.column, Direction.DOWN);
								if (!flds.isEmpty() && flds.get(0).getId().equals(id))
									fields = getClueFields(position.row, position.column,
														   Direction.ACROSS);
							}
						}
					}
				}
			}
			else
			{
				Grid.Field field = fields.getField();
				if (absIncrement > 1)
					position.row = (increment < 0) ? field.getRow() : field.getEndRow();
				else
				{
					int row = position.row + increment;
					if ((row >= field.getRow()) && (row <= field.getEndRow()))
						position.row = row;
					else
					{
						int index = fields.index;
						if (index >= 0)
						{
							if (row < field.getRow())
							{
								if (index > 0)
								{
									--fields.index;
									position = fields.getField().getEndIndices();
								}
							}
							else
							{
								if (index < fields.getNumFields() - 1)
								{
									++fields.index;
									position = fields.getField().getStartIndices();
								}
							}
						}
						if (navigateOverSeparators && (fields.index == index))
							fields = incrementCaretRow(position, increment);
					}
				}
			}
			if (fields != null)
				setSelection(fields, position);
		}
		return fields;
	}

	//------------------------------------------------------------------

	public void undoEdit()
	{
		getGrid().undoEdit();
		updateHighlightedCells();
		repaint();
		fireStateChanged();
	}

	//------------------------------------------------------------------

	public void redoEdit()
	{
		getGrid().redoEdit();
		updateHighlightedCells();
		repaint();
		fireStateChanged();
	}

	//------------------------------------------------------------------

	protected void updateHighlightedCells()
	{
		if (isEditing())
		{
			isolatedCells = getGrid().getIsolatedCells();
			fullyIntersectingFieldCells.clear();
			if (highlightFullyIntersectingFields)
			{
				for (Grid.Field field : getGrid().getFullyIntersectingFields())
				{
					switch (field.getDirection())
					{
						case NONE:
							// do nothing
							break;

						case ACROSS:
						{
							int row = field.getRow();
							int column = field.getColumn();
							for (int i = 0; i < field.getLength(); i++)
								fullyIntersectingFieldCells.add(new Grid.IndexPair(row, column++));
							break;
						}

						case DOWN:
						{
							int row = field.getRow();
							int column = field.getColumn();
							for (int i = 0; i < field.getLength(); i++)
								fullyIntersectingFieldCells.add(new Grid.IndexPair(row++, column));
							break;
						}
					}
				}
			}
		}
	}

	//------------------------------------------------------------------

	protected void fireStateChanged()
	{
		for (int i = changeListeners.size() - 1; i >= 0; i--)
		{
			if (changeEvent == null)
				changeEvent = new ChangeEvent(this);
			changeListeners.get(i).stateChanged(changeEvent);
		}
	}

	//------------------------------------------------------------------

	private boolean isEditing()
	{
		return (editPosition != null);
	}

	//------------------------------------------------------------------

	private Clue.FieldList getClueFields(int       row,
										 int       column,
										 Direction direction)
	{
		List<Grid.Field> fields = null;
		Grid grid = getGrid();
		Grid.Field field = grid.getCell(row, column).getField(direction);
		if (field != null)
		{
			for (Clue clue : document.findPrimaryClues(field.getId()))
			{
				List<Grid.Field> clueFields = grid.getFields(clue);
				if (fields == null)
					fields = clueFields;
				if (selectedFields.matches(clueFields))
				{
					fields = clueFields;
					break;
				}
			}
		}
		return ((fields == null) ? null : new Clue.FieldList(fields, field));
	}

	//------------------------------------------------------------------

	private Direction getSelectedFieldDirection()
	{
		return (selectedFields.isEmpty() ? Direction.NONE : selectedFields.getField().getDirection());
	}

	//------------------------------------------------------------------

	private Clue.FieldList incrementCaretColumn(Grid.IndexPair position,
												int            increment)
	{
		Clue.FieldList fields = null;
		while (true)
		{
			position.column += increment;
			if ((position.column < 0) || (position.column >= numColumns))
				break;
			Grid grid = getGrid();
			if (grid.getCell(position.row, position.column).isInField())
			{
				List<Grid.Field> flds = grid.findFields(position.row, position.column, Direction.ACROSS);
				flds.addAll(grid.findFields(position.row, position.column, Direction.DOWN));
				for (Grid.Field field : flds)
				{
					fields = getClueFields(position.row, position.column, field.getDirection());
					if (fields != null)
						break;
				}
				break;
			}
		}
		return fields;
	}

	//------------------------------------------------------------------

	private Clue.FieldList incrementCaretRow(Grid.IndexPair position,
											 int            increment)
	{
		Clue.FieldList fields = null;
		while (true)
		{
			position.row += increment;
			if ((position.row < 0) || (position.row >= numRows))
				break;
			Grid grid = getGrid();
			if (grid.getCell(position.row, position.column).isInField())
			{
				List<Grid.Field> flds = grid.findFields(position.row, position.column, Direction.DOWN);
				flds.addAll(grid.findFields(position.row, position.column, Direction.ACROSS));
				for (Grid.Field field : flds)
				{
					fields = getClueFields(position.row, position.column,
										   field.getDirection());
					if (fields != null)
						break;
				}
				break;
			}
		}
		return fields;
	}

	//------------------------------------------------------------------

	private void incrementEditColumn(int increment)
	{
		int column = Math.min(Math.max(0, editPosition.column + increment), numColumns - 1);
		if (column != editPosition.column)
		{
			editPosition.column = column;
			repaint();
		}
	}

	//------------------------------------------------------------------

	private void incrementEditRow(int increment)
	{
		int row = Math.min(Math.max(0, editPosition.row + increment), numRows - 1);
		if (row != editPosition.row)
		{
			editPosition.row = row;
			repaint();
		}
	}

	//------------------------------------------------------------------

	private void onMoveEditPositionUpUnit()
	{
		incrementEditRow(-1);
	}

	//------------------------------------------------------------------

	private void onMoveEditPositionDownUnit()
	{
		incrementEditRow(1);
	}

	//------------------------------------------------------------------

	private void onMoveEditPositionLeftUnit()
	{
		incrementEditColumn(-1);
	}

	//------------------------------------------------------------------

	private void onMoveEditPositionRightUnit()
	{
		incrementEditColumn(1);
	}

	//------------------------------------------------------------------

	private void onMoveEditPositionUpMax()
	{
		incrementEditRow(-numRows);
	}

	//------------------------------------------------------------------

	private void onMoveEditPositionDownMax()
	{
		incrementEditRow(numRows);
	}

	//------------------------------------------------------------------

	private void onMoveEditPositionLeftMax()
	{
		incrementEditColumn(-numColumns);
	}

	//------------------------------------------------------------------

	private void onMoveEditPositionRightMax()
	{
		incrementEditColumn(numColumns);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class fields
////////////////////////////////////////////////////////////////////////

	private static	Caret	caret	= new Caret();

////////////////////////////////////////////////////////////////////////
//  Instance fields
////////////////////////////////////////////////////////////////////////

	protected	CrosswordDocument		document;
	protected	int						numColumns;
	protected	int						numRows;
	protected	int						cellSize;
	protected	List<Grid.IndexPair>	isolatedCells;
	protected	boolean					highlightFullyIntersectingFields;
	protected	List<Grid.IndexPair>	fullyIntersectingFieldCells;
	protected	Grid.IndexPair			editPosition;
	private		Grid.IndexPair			caretPosition;
	private		Grid.IndexPair			caretDrawnPosition;
	private		Clue.FieldList			selectedFields;
	private		List<ChangeListener>	changeListeners;
	private		ChangeEvent				changeEvent;

}

//----------------------------------------------------------------------
