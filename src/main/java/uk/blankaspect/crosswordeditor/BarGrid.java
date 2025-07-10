/*====================================================================*\

BarGrid.java

Class: bar grid.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.crosswordeditor;

//----------------------------------------------------------------------


// IMPORTS


import java.awt.Color;

import java.awt.image.BufferedImage;

import java.io.IOException;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import uk.blankaspect.common.css.CssMediaRule;
import uk.blankaspect.common.css.CssProperty;
import uk.blankaspect.common.css.CssRuleSet;
import uk.blankaspect.common.css.CssSelector;

import uk.blankaspect.common.exception.AppException;

import uk.blankaspect.common.misc.EditList;

import uk.blankaspect.common.tuple.StrKVPair;

import uk.blankaspect.common.xml.AttributeList;
import uk.blankaspect.common.xml.XmlWriter;

import uk.blankaspect.ui.swing.colour.ColourUtils;

//----------------------------------------------------------------------


// CLASS: BAR GRID


class BarGrid
	extends Grid
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	private static final	double	OFFSET_FACTOR	= 0.01;

	private static final	String	BORDER_WIDTH_PROPERTY	= "border-%s-width";

	private static final	String	GRID_DEF_CHARS	= "0123";

	private static final	String	PIXEL_SIZE_STR	= "%dpx";

	private static final	EnumSet<Edge>		SECONDARY_BARS		= EnumSet.of(Edge.BOTTOM, Edge.RIGHT);
	private static final	List<EnumSet<Edge>>	SECONDARY_BAR_SETS	= List.of
	(
		EnumSet.noneOf(Edge.class),
		EnumSet.of(Edge.BOTTOM),
		EnumSet.of(Edge.RIGHT),
		SECONDARY_BARS
	);

	private enum GridState
	{
		START,
		LEADING_LIGHT,
		DARK,
		TRAILING_LIGHT
	}

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	public BarGrid(
		int			numColumns,
		int			numRows,
		Symmetry	symmetry)
	{
		// Initialise instance variables
		this(numColumns, numRows);
		this.symmetry = symmetry;

		// Initialise fields
		initFields();
	}

	//------------------------------------------------------------------

	public BarGrid(
		int			numColumns,
		int			numRows,
		Symmetry	symmetry,
		String		definition)
		throws AppException
	{
		// Initialise instance variables
		this(numColumns, numRows);
		this.symmetry = symmetry;

		// Parse grid definition
		List<EnumSet<Edge>> barSets = new ArrayList<>();
		for (int i = 0; i < definition.length(); i++)
		{
			char ch = definition.charAt(i);
			int index = GRID_DEF_CHARS.indexOf(Character.toUpperCase(ch));
			if (index < 0)
				throw new AppException(ErrorId.ILLEGAL_CHARACTER_IN_GRID_DEFINITION, Character.toString(ch));
			barSets.add(SECONDARY_BAR_SETS.get(index));
		}
		int[] dimensions = symmetry.getPrincipalDimensions(numColumns, numRows);
		if (barSets.size() != dimensions[0] * dimensions[1])
			throw new AppException(ErrorId.MALFORMED_GRID_DEFINITION);

		// Initialise cells
		for (int i = 0; i < barSets.size(); i++)
			setCellBars(i / dimensions[0], i % dimensions[0], SECONDARY_BARS, barSets.get(i));

		// Initialise fields
		initFields();
	}

	//------------------------------------------------------------------

	public BarGrid(
		int				numColumns,
		int				numRows,
		BufferedImage	image,
		int				xOffset,
		int				yOffset,
		double			brightnessThreshold,
		int				barWidthThreshold)
		throws AppException
	{
		// Initialise instance variables
		this(numColumns, numRows);

		// Initialise local variables
		int imageWidth = image.getWidth();
		int imageHeight = image.getHeight();
		double cellWidth = (double)imageWidth / (double)numColumns;
		double cellHeight = (double)imageHeight / (double)numRows;
		double dx = (double)xOffset * OFFSET_FACTOR * cellWidth;
		double dy = (double)yOffset * OFFSET_FACTOR * cellHeight;

		// Detect vertical bars in image
		double y = 0.5 * cellHeight;
		for (int row = 0; row < numRows; row++)
		{
			int iy = (int)Math.round(y + dy);
			int prevImageX = -1;
			double x = 0.5 * cellWidth;
			for (int column = 0; column < numColumns; column++)
			{
				int imageX = (int)Math.round(x + dx);
				if ((prevImageX >= 0)
						&& isBar(prevImageX, imageX, iy, true, image, brightnessThreshold, barWidthThreshold))
					cells[row][column].addBar(Edge.LEFT);
				prevImageX = imageX;
				x += cellWidth;
			}
			y += cellHeight;
		}

		// Detect horizontal bars in image
		double x = 0.5 * cellWidth;
		for (int column = 0; column < numColumns; column++)
		{
			int ix = (int)Math.round(x + dx);
			int prevImageY = -1;
			y = 0.5 * cellHeight;
			for (int row = 0; row < numRows; row++)
			{
				int imageY = (int)Math.round(y + dy);
				if ((prevImageY >= 0)
						&& isBar(prevImageY, imageY, ix, false, image, brightnessThreshold, barWidthThreshold))
					cells[row][column].addBar(Edge.TOP);
				prevImageY = imageY;
				y += cellHeight;
			}
			x += cellWidth;
		}

		// Initialise remaining bars
		for (int row = 0; row < numRows; row++)
		{
			for (int column = 0; column < numColumns; column++)
			{
				Cell cell = cells[row][column];
				if ((row < numRows - 1) && cells[row + 1][column].hasBar(Edge.TOP))
					cell.addBar(Edge.BOTTOM);
				if ((column < numColumns - 1) && cells[row][column + 1].hasBar(Edge.LEFT))
					cell.addBar(Edge.RIGHT);
				if ((row > 0) && cells[row - 1][column].hasBar(Edge.BOTTOM))
					cell.addBar(Edge.TOP);
				if ((column > 0) && cells[row][column - 1].hasBar(Edge.RIGHT))
					cell.addBar(Edge.LEFT);
			}
		}

		// Initialise fields
		initFields();

		// Update symmetry
		updateSymmetry();
	}

	//------------------------------------------------------------------

	private BarGrid(
		BarGrid	grid)
	{
		// Initialise instance variables
		this(grid.numColumns, grid.numRows);
		symmetry = grid.symmetry;
		editList = new EditList<>(AppConfig.INSTANCE.getMaxEditListLength());

		// Initialise cells
		for (int row = 0; row < numRows; row++)
		{
			for (int column = 0; column < numColumns; column++)
				cells[row][column].bars = grid.cells[row][column].bars.clone();
		}

		// Initialise fields
		initFields();
	}

	//------------------------------------------------------------------

	private BarGrid(
		int	numColumns,
		int	numRows)
	{
		// Call superclass contructor
		super(numColumns, numRows);

		// Initialise instance variables
		cells = new Cell[numRows][numColumns];
		for (int row = 0; row < numRows; row++)
		{
			for (int column = 0; column < numColumns; column++)
				cells[row][column] = new Cell();
		}
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	private static Cell[][] copyCells(
		Cell[][]	cells)
	{
		Cell[][] outCells = new Cell[cells.length][];
		for (int i = 0; i < cells.length; i++)
		{
			int length = cells[i].length;
			outCells[i] = new Cell[length];
			for (int j = 0; j < length; j++)
				outCells[i][j] = cells[i][j].clone();
		}
		return outCells;
	}

	//------------------------------------------------------------------

	private static boolean isBar(
		int				startIndex,
		int				endIndex,
		int				fixedCoord,
		boolean			vertical,
		BufferedImage	image,
		double			brightnessThreshold,
		int				barWidthThreshold)
		throws AppException
	{
		int lineWidth = 0;
		GridState state = GridState.START;
		for (int i = startIndex; i <= endIndex; i++)
		{
			int rgb = image.getRGB(vertical ? i : fixedCoord, vertical ? fixedCoord : i);
			boolean dark = (ColourUtils.getBrightness(rgb) < brightnessThreshold);
			switch (state)
			{
				case START:
					if (dark)
						throw new AppException(ErrorId.MALFORMED_GRID_IMAGE);
					state = GridState.LEADING_LIGHT;
					break;

				case LEADING_LIGHT:
					if (dark)
					{
						++lineWidth;
						state = GridState.DARK;
					}
					break;

				case DARK:
					if (dark)
						++lineWidth;
					else
						state = GridState.TRAILING_LIGHT;
					break;

				case TRAILING_LIGHT:
					if (dark)
						throw new AppException(ErrorId.MALFORMED_GRID_IMAGE);
					break;
			}
		}
		if (state != GridState.TRAILING_LIGHT)
			throw new AppException(ErrorId.MALFORMED_GRID_IMAGE);

		return (lineWidth >= barWidthThreshold);
	}

	//------------------------------------------------------------------

	private static List<CssRuleSet> createCellRuleSets(
		int		cellSize,
		Color	gridColour,
		Color	barColour,
		Color	entryColour,
		double	fieldNumberFontSizeFactor,
		int		barWidth,
		int		cellOffsetTop,
		int		cellOffsetLeft,
		int		fieldNumOffsetTop,
		int		fieldNumOffsetLeft)
	{
		// Initialise local variables
		int halfBarWidth = barWidth / 2;

		// Generate the rule sets for the base cell style
		int fieldNumOffsetBase = (barWidth - 1) / 2;
		List<CssRuleSet> ruleSets =
				Cell.getStyleRuleSets(cellSize, cellOffsetTop, cellOffsetLeft, gridColour, entryColour,
									  fieldNumOffsetBase + fieldNumOffsetTop, fieldNumOffsetBase + fieldNumOffsetLeft,
									  fieldNumberFontSizeFactor);

		// Get rule set for barred cell
		CssRuleSet ruleSet = Cell.RULE_SET.clone();

		// Set property values in rule set
		int outerSize = cellSize + 1;
		ruleSet.replacePropertyValue(CssProperty.WIDTH, outerSize);
		ruleSet.replacePropertyValue(CssProperty.HEIGHT, outerSize);
		ruleSet.replacePropertyValue(CssProperty.TOP, cellOffsetTop - 1);
		ruleSet.replacePropertyValue(CssProperty.LEFT, cellOffsetLeft - 1);
		ruleSet.replacePropertyValue(CssProperty.BORDER, ColourUtils.colourToHexString(barColour));

		// Add rule set for barred cell
		ruleSets.add(1, ruleSet);

		// Generate the rule sets for the barred-cell styles
		for (int i = 1; i < (1 << Edge.values().length); i++)
		{
			// Get the set of edges for this rule set
			EnumSet<Edge> edges = EnumSet.noneOf(Edge.class);
			for (Edge edge : Edge.values())
			{
				if ((edge.getMask() & i) != 0)
					edges.add(edge);
			}

			// Create rule set
			ruleSet = CssRuleSet.empty(HtmlConstants.ElementName.DIV + CssSelector.CLASS + Cell.getClassName(edges));

			// Add property: width
			int numVerticalEdges = (int)edges.stream().filter(edge -> edge.isVertical()).count();
			if (numVerticalEdges > 0)
			{
				int width = outerSize - ((numVerticalEdges == 1) ? halfBarWidth : barWidth);
				ruleSet.addProperty(CssProperty.WIDTH, String.format(PIXEL_SIZE_STR, width));
			}

			// Add property: height
			int numHorizontalEdges = (int)edges.stream().filter(edge -> edge.isHorizontal()).count();
			if (numHorizontalEdges > 0)
			{
				int height = outerSize - ((numHorizontalEdges == 1) ? halfBarWidth : barWidth);
				ruleSet.addProperty(CssProperty.HEIGHT, String.format(PIXEL_SIZE_STR, height));
			}

			// Add property: top offset
			if (edges.contains(Edge.TOP))
				ruleSet.addProperty(CssProperty.TOP, String.format(PIXEL_SIZE_STR, cellOffsetTop - halfBarWidth - 1));

			// Add property: left offset
			if (edges.contains(Edge.LEFT))
				ruleSet.addProperty(CssProperty.LEFT, String.format(PIXEL_SIZE_STR, cellOffsetLeft - halfBarWidth - 1));

			// Add properties: border width
			for (Edge edge : edges)
				ruleSet.addProperty(String.format(BORDER_WIDTH_PROPERTY, edge.text),
									String.format(PIXEL_SIZE_STR, barWidth));

			// Add rule set to list
			ruleSets.add(ruleSet);
		}

		return ruleSets;
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : overriding methods
////////////////////////////////////////////////////////////////////////

	@Override
	public Separator getSeparator()
	{
		return Separator.BAR;
	}

	//------------------------------------------------------------------

	@Override
	public Cell getCell(
		int	row,
		int	column)
	{
		return cells[row][column];
	}

	//------------------------------------------------------------------

	@Override
	public List<IndexPair> getIsolatedCells()
	{
		List<IndexPair> isolatedCells = new ArrayList<>();
		for (int row = 0; row < numRows; row++)
		{
			for (int column = 0; column < numColumns; column++)
			{
				if (((column == 0) || cells[row][column].hasBar(Edge.LEFT))
						&& ((column == numColumns - 1) || cells[row][column + 1].hasBar(Edge.LEFT))
						&& ((row == 0) || cells[row][column].hasBar(Edge.TOP))
						&& ((row == numRows - 1) || cells[row + 1][column].hasBar(Edge.TOP)))
					isolatedCells.add(new IndexPair(row, column));
			}
		}
		return isolatedCells;
	}

	//------------------------------------------------------------------

	@Override
	public List<String> getGridDefinition()
	{
		List<String> strs = new ArrayList<>();
		int[] dimensions = symmetry.getPrincipalDimensions(numColumns, numRows);
		StringBuilder buffer = new StringBuilder(dimensions[0]);
		for (int row = 0; row < dimensions[1]; row++)
		{
			buffer.setLength(0);
			for (int column = 0; column < dimensions[0]; column++)
			{
				EnumSet<Edge> bars = cells[row][column].bars.clone();
				bars.retainAll(SECONDARY_BARS);
				buffer.append(GRID_DEF_CHARS.charAt(SECONDARY_BAR_SETS.indexOf(bars)));
			}
			strs.add(buffer.toString());
		}
		return strs;
	}

	//------------------------------------------------------------------

	@Override
	public List<CssRuleSet> getStyleRuleSets(
		int		cellSize,
		Color	gridColour,
		Color	entryColour,
		double	fieldNumberFontSizeFactor)
	{
		List<CssRuleSet> ruleSets = new ArrayList<>();
		ruleSets.addAll(RULE_SETS);
		AppConfig config = AppConfig.INSTANCE;
		ruleSets.addAll(createCellRuleSets(cellSize, gridColour, config.getHtmlBarColour(), entryColour,
										   fieldNumberFontSizeFactor, config.getHtmlBarGridBarWidth(),
										   config.getHtmlCellOffsetTop(), config.getHtmlCellOffsetLeft(),
										   config.getHtmlFieldNumOffsetTop(), config.getHtmlFieldNumOffsetLeft()));
		return ruleSets;
	}

	//------------------------------------------------------------------

	@Override
	public List<CssMediaRule> getStyleMediaRules()
	{
		return new ArrayList<>();
	}

	//------------------------------------------------------------------

	@Override
	public Grid createCopy()
	{
		return new BarGrid(this);
	}

	//------------------------------------------------------------------

	@Override
	public boolean canUndoEdit()
	{
		return editList.canUndo();
	}

	//------------------------------------------------------------------

	@Override
	public void undoEdit()
	{
		EditList.Element<BarGrid> edit = editList.removeUndo();
		if (edit != null)
			edit.undo(this);
	}

	//------------------------------------------------------------------

	@Override
	public boolean canRedoEdit()
	{
		return editList.canRedo();
	}

	//------------------------------------------------------------------

	@Override
	public void redoEdit()
	{
		EditList.Element<BarGrid> edit = editList.removeRedo();
		if (edit != null)
			edit.redo(this);
	}

	//------------------------------------------------------------------

	@Override
	public void setSymmetry(
		Symmetry	symmetry)
	{
		if (this.symmetry != symmetry)
		{
			// Save old state
			Symmetry oldSymmetry = this.symmetry;
			Cell[][] oldCells = copyCells(cells);

			// Set instance variable
			this.symmetry = symmetry;

			// Make list of secondary bars of cells of specified region
			int[] dimensions = symmetry.getPrincipalDimensions(numColumns, numRows);
			List<EnumSet<Edge>> barSets = new ArrayList<>();
			for (int row = 0; row < dimensions[1]; row++)
			{
				for (int column = 0; column < dimensions[0]; column++)
				{
					EnumSet<Edge> bars = cells[row][column].bars.clone();
					bars.retainAll(SECONDARY_BARS);
					barSets.add(bars);
				}
			}

			// Clear bars of all cells
			for (int row = 0; row < numRows; row++)
			{
				for (int column = 0; column < numColumns; column++)
					cells[row][column].bars.clear();
			}

			// Initialise bars
			for (int i = 0; i < barSets.size(); i++)
				setCellBars(i / dimensions[0], i % dimensions[0], SECONDARY_BARS, barSets.get(i));

			// Initialise fields
			initFields();

			// Add edit to list
			editList.add(new Edit(oldSymmetry, oldCells, symmetry, copyCells(cells)));
		}
	}

	//------------------------------------------------------------------

	@Override
	protected boolean isSymmetry(
		Symmetry	symmetry)
	{
		int[] dimensions = symmetry.getPrincipalDimensions(numColumns, numRows);
		for (int r1 = 0; r1 < dimensions[1]; r1++)
		{
			for (int c1 = 0; c1 < dimensions[0]; c1++)
			{
				EnumSet<Edge> bars = cells[r1][c1].bars;
				int c2 = numColumns - 1 - c1;
				int r2 = numRows - 1 - r1;
				switch (symmetry)
				{
					case NONE:
						// do nothing
						break;

					case ROTATION_HALF:
						if (!cells[r2][c2].bars.equals(Edge.rotateQuarter(bars, 2)))
							return false;
						break;

					case ROTATION_QUARTER:
						if (!cells[c1][r2].bars.equals(Edge.rotateQuarter(bars, 1))
								|| !cells[r2][c2].bars.equals(Edge.rotateQuarter(bars, 2))
								|| !cells[c2][r1].bars.equals(Edge.rotateQuarter(bars, 3)))
							return false;
						break;

					case REFLECTION_VERTICAL_AXIS:
						if (!cells[r1][c2].bars.equals(Edge.reflectVAxis(bars)))
							return false;
						break;

					case REFLECTION_HORIZONTAL_AXIS:
						if (!cells[r2][c1].bars.equals(Edge.reflectHAxis(bars)))
							return false;
						break;

					case REFLECTION_VERTICAL_HORIZONTAL_AXES:
						if (!cells[r1][c2].bars.equals(Edge.reflectVAxis(bars))
								|| !cells[r2][c1].bars.equals(Edge.reflectHAxis(bars))
								|| !cells[r2][c2].bars.equals(Edge.rotateQuarter(bars, 2)))
							return false;
						break;
				}
			}
		}
		return true;
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	public void setCellBars(
		int				row,
		int				column,
		EnumSet<Edge>	removeBars,
		EnumSet<Edge>	addBars)
	{
		// Set specified cell
		Edge.Sets barSets = new Edge.Sets(removeBars, addBars);
		int r1 = row;
		int c1 = column;
		setBars(r1, c1, barSets);

		// Set corresponding cells in other regions
		int c2 = numColumns - 1 - c1;
		int r2 = numRows - 1 - r1;
		switch (symmetry)
		{
			case NONE:
				// do nothing
				break;

			case ROTATION_HALF:
				setBars(r2, c2, Edge.rotateQuarter(barSets, 2));
				break;

			case ROTATION_QUARTER:
				setBars(c1, r2, Edge.rotateQuarter(barSets, 1));
				setBars(r2, c2, Edge.rotateQuarter(barSets, 2));
				setBars(c2, r1, Edge.rotateQuarter(barSets, 3));
				break;

			case REFLECTION_VERTICAL_AXIS:
				setBars(r1, c2, Edge.reflectVAxis(barSets));
				break;

			case REFLECTION_HORIZONTAL_AXIS:
				setBars(r2, c1, Edge.reflectHAxis(barSets));
				break;

			case REFLECTION_VERTICAL_HORIZONTAL_AXES:
				setBars(r1, c2, Edge.reflectVAxis(barSets));
				setBars(r2, c1, Edge.reflectHAxis(barSets));
				setBars(r2, c2, Edge.rotateQuarter(barSets, 2));
				break;
		}
	}

	//------------------------------------------------------------------

	public void toggleBar(
		int				row,
		int				column,
		BarGrid.Edge	edge)
	{
		Cell[][] oldCells = copyCells(cells);
		EnumSet<BarGrid.Edge> bars = EnumSet.noneOf(BarGrid.Edge.class);
		if (!getCell(row, column).hasBar(edge))
			bars.add(edge);
		setCellBars(row, column, EnumSet.of(edge), bars);
		initFields();
		editList.add(new Edit(null, oldCells, null, copyCells(cells)));
	}

	//------------------------------------------------------------------

	private void initFields()
	{
		fieldLists.clear();
		entries.init();
		int fieldNumber = 1;
		for (int row = 0; row < numRows; row++)
		{
			for (int column = 0; column < numColumns; column++)
			{
				Cell cell = cells[row][column];
				cell.resetFields();
				Field field = null;
				if ((column == 0) || cells[row][column].hasBar(Edge.LEFT))
				{
					int c = column + 1;
					while ((c < numColumns) && !cells[row][c].hasBar(Edge.LEFT))
						++c;
					int length = c - column;
					if (length > 1)
					{
						field = addField(row, column, Direction.ACROSS, length, fieldNumber);
						cell.setFieldOrigin(Direction.ACROSS, field);
						for (int i = 0; i < length; i++)
							entries.initValue(row, column + i);
					}
				}
				if ((row == 0) || cells[row][column].hasBar(Edge.TOP))
				{
					int r = row + 1;
					while ((r < numRows) && !cells[r][column].hasBar(Edge.TOP))
						++r;
					int length = r - row;
					if (length > 1)
					{
						field = addField(row, column, Direction.DOWN, length, fieldNumber);
						cell.setFieldOrigin(Direction.DOWN, field);
						for (int i = 0; i < length; i++)
							entries.initValue(row + i, column);
					}
				}
				if (field != null)
					++fieldNumber;
				for (Direction direction : Direction.DEFINED_DIRECTIONS)
				{
					if (cell.getField(direction) == null)
					{
						List<Field> fields = findFields(row, column, direction);
						if (!fields.isEmpty())
							cell.setField(direction, fields.get(0));
					}
				}
			}
		}
	}

	//------------------------------------------------------------------

	private void setBars(
		int			row,
		int			column,
		Edge.Sets	edgeSets)
	{
		// Remove bars from and add bars to cell
		Cell cell = cells[row][column];
		cell.bars.removeAll(edgeSets.remove);
		cell.bars.addAll(edgeSets.add);

		// Remove bars from surrounding cells
		for (Edge edge : edgeSets.remove)
		{
			switch (edge)
			{
				case TOP:
					if (row > 0)
						cells[row - 1][column].bars.remove(Edge.BOTTOM);
					break;

				case RIGHT:
					if (column < numColumns - 1)
						cells[row][column + 1].bars.remove(Edge.LEFT);
					break;

				case BOTTOM:
					if (row < numRows - 1)
						cells[row + 1][column].bars.remove(Edge.TOP);
					break;

				case LEFT:
					if (column > 0)
						cells[row][column - 1].bars.remove(Edge.RIGHT);
					break;
			}
		}

		// Add bars to surrounding cells
		for (Edge edge : edgeSets.add)
		{
			switch (edge)
			{
				case TOP:
					if (row > 0)
						cells[row - 1][column].bars.add(Edge.BOTTOM);
					break;

				case RIGHT:
					if (column < numColumns - 1)
						cells[row][column + 1].bars.add(Edge.LEFT);
					break;

				case BOTTOM:
					if (row < numRows - 1)
						cells[row + 1][column].bars.add(Edge.TOP);
					break;

				case LEFT:
					if (column > 0)
						cells[row][column - 1].bars.add(Edge.RIGHT);
					break;
			}
		}
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private	Cell[][]			cells;
	private	EditList<BarGrid>	editList;

////////////////////////////////////////////////////////////////////////
//  Enumerated types
////////////////////////////////////////////////////////////////////////


	// ENUMERATION: CELL EDGE


	enum Edge
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		TOP
		(
			'T',
			"top"
		),

		RIGHT
		(
			'R',
			"right"
		),

		BOTTOM
		(
			'B',
			"bottom"
		),

		LEFT
		(
			'L',
			"left"
		);

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	char	key;
		private	String	text;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private Edge(
			char	key,
			String	text)
		{
			this.key = key;
			this.text = text;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Class methods
	////////////////////////////////////////////////////////////////////

		private static EnumSet<Edge> reflectVAxis(
			Set<Edge>	edges)
		{
			EnumSet<Edge> outEdges = EnumSet.noneOf(Edge.class);
			for (Edge edge : edges)
				outEdges.add(edge.reflectVAxis());
			return outEdges;
		}

		//--------------------------------------------------------------

		private static Sets reflectVAxis(
			Sets	edgeSets)
		{
			return new Sets(reflectVAxis(edgeSets.remove), reflectVAxis(edgeSets.add));
		}

		//--------------------------------------------------------------

		private static EnumSet<Edge> reflectHAxis(
			Set<Edge>	edges)
		{
			EnumSet<Edge> outEdges = EnumSet.noneOf(Edge.class);
			for (Edge edge : edges)
				outEdges.add(edge.reflectHAxis());
			return outEdges;
		}

		//--------------------------------------------------------------

		private static Sets reflectHAxis(
			Sets	edgeSets)
		{
			return new Sets(reflectHAxis(edgeSets.remove), reflectHAxis(edgeSets.add));
		}

		//--------------------------------------------------------------

		private static EnumSet<Edge> rotateQuarter(
			Set<Edge>	edges,
			int			numQuarters)
		{
			EnumSet<Edge> outEdges = EnumSet.noneOf(Edge.class);
			for (Edge edge : edges)
				outEdges.add(edge.rotateQuarter(numQuarters));
			return outEdges;
		}

		//--------------------------------------------------------------

		private static Sets rotateQuarter(
			Sets	edgeSets,
			int		numQuarters)
		{
			return new Sets(rotateQuarter(edgeSets.remove, numQuarters), rotateQuarter(edgeSets.add, numQuarters));
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		private int getMask()
		{
			return 1 << ordinal();
		}

		//--------------------------------------------------------------

		private boolean isVertical()
		{
			return ((ordinal() % 2) != 0);
		}

		//--------------------------------------------------------------

		private boolean isHorizontal()
		{
			return ((ordinal() % 2) == 0);
		}

		//--------------------------------------------------------------

		private Edge reflectVAxis()
		{
			return isVertical() ? values()[(ordinal() + 2) % values().length] : this;
		}

		//--------------------------------------------------------------

		private Edge reflectHAxis()
		{
			return isHorizontal() ? values()[(ordinal() + 2) % values().length] : this;
		}

		//--------------------------------------------------------------

		private Edge rotateQuarter(
			int	numQuarters)
		{
			return values()[(ordinal() + numQuarters) % values().length];
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Member classes : non-inner classes
	////////////////////////////////////////////////////////////////////


		// CLASS: EDGE SETS


		private static class Sets
		{

		////////////////////////////////////////////////////////////////
		//  Instance variables
		////////////////////////////////////////////////////////////////

			private	Set<Edge>	remove;
			private	Set<Edge>	add;

		////////////////////////////////////////////////////////////////
		//  Constructors
		////////////////////////////////////////////////////////////////

			private Sets(
				Set<Edge>	remove,
				Set<Edge>	add)
			{
				this.remove = remove;
				this.add = add;
			}

			//----------------------------------------------------------

		}

		//==============================================================

	}

	//==================================================================


	// ENUMERATION: ERROR IDENTIFIERS


	private enum ErrorId
		implements AppException.IId
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		MALFORMED_GRID_IMAGE
		("The grid image is malformed."),

		ILLEGAL_CHARACTER_IN_GRID_DEFINITION
		("The grid definition contains an illegal character, \"%1\"."),

		MALFORMED_GRID_DEFINITION
		("The grid definition is malformed.");

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	String	message;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private ErrorId(
			String	message)
		{
			this.message = message;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : AppException.IId interface
	////////////////////////////////////////////////////////////////////

		@Override
		public String getMessage()
		{
			return message;
		}

		//--------------------------------------------------------------

	}

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Member classes : non-inner classes
////////////////////////////////////////////////////////////////////////


	// CLASS: CELL


	public static class Cell
		extends Grid.Cell
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		private static final	String	CLASS_PREFIX	= "bar-";

		private static final	String	STYLE_SELECTOR	=
				HtmlConstants.ElementName.DIV + CssSelector.CLASS + HtmlConstants.Class.BARS;
		private static final	CssRuleSet	RULE_SET	= CssRuleSet.of
		(
			STYLE_SELECTOR,
			StrKVPair.of(CssProperty.POSITION, "absolute"),
			StrKVPair.of(CssProperty.Z_INDEX,  "1"),
			StrKVPair.of(CssProperty.WIDTH,    "%dpx"),
			StrKVPair.of(CssProperty.HEIGHT,   "%dpx"),
			StrKVPair.of(CssProperty.TOP,      "%dpx"),
			StrKVPair.of(CssProperty.LEFT,     "%dpx"),
			StrKVPair.of(CssProperty.BORDER,   "0 solid %s")
		);

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	EnumSet<Edge>	bars;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private Cell()
		{
			bars = EnumSet.noneOf(Edge.class);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Class methods
	////////////////////////////////////////////////////////////////////

		private static String getClassName(
			Set<Edge>	edges)
		{
			StringBuilder buffer = new StringBuilder();
			if (!edges.isEmpty())
			{
				buffer.append(CLASS_PREFIX);
				for (Edge edge : edges)
					buffer.append(edge.key);
			}
			return buffer.toString();
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		public Cell clone()
		{
			Cell copy = (Cell)super.clone();
			copy.bars = bars.clone();
			return copy;
		}

		//--------------------------------------------------------------

		@Override
		protected void write(
			XmlWriter	writer,
			int			indent,
			int			cellSize,
			int			fieldNumber,
			char		entry)
			throws IOException
		{
			writer.writeElementStart(HtmlConstants.ElementName.DIV, indent, false);
			if (!bars.isEmpty())
			{
				AttributeList attributes = new AttributeList();
				attributes.add(HtmlConstants.AttrName.CLASS, HtmlConstants.Class.BARS + " " + getClassName(bars));
				writer.writeElementStart(HtmlConstants.ElementName.DIV, attributes, 0, false, false);
				writer.writeEndTag(HtmlConstants.ElementName.DIV);
			}
			writeContents(writer, fieldNumber, entry);
			writer.writeElementEnd(HtmlConstants.ElementName.DIV, 0);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		public boolean hasBar(
			Edge	edge)
		{
			return bars.contains(edge);
		}

		//--------------------------------------------------------------

		private void addBar(
			Edge	edge)
		{
			bars.add(edge);
		}

		//--------------------------------------------------------------

	}

	//==================================================================


	// CLASS: EDIT


	private static class Edit
		extends EditList.Element<BarGrid>
	{

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	Symmetry	oldSymmetry;
		private	Cell[][]	oldCells;
		private	Symmetry	newSymmetry;
		private	Cell[][]	newCells;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private Edit(
			Symmetry	oldSymmetry,
			Cell[][]	oldCells,
			Symmetry	newSymmetry,
			Cell[][]	newCells)
		{
			this.oldSymmetry = oldSymmetry;
			this.oldCells = oldCells;
			this.newSymmetry = newSymmetry;
			this.newCells = newCells;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		public String getText()
		{
			return null;
		}

		//--------------------------------------------------------------

		@Override
		public void undo(
			BarGrid	grid)
		{
			if (oldSymmetry != null)
				grid.symmetry = oldSymmetry;
			grid.cells = oldCells;
			grid.initFields();
		}

		//--------------------------------------------------------------

		@Override
		public void redo(
			BarGrid	grid)
		{
			if (newSymmetry != null)
				grid.symmetry = newSymmetry;
			grid.cells = newCells;
			grid.initFields();
		}

		//--------------------------------------------------------------

	}

	//==================================================================

}

//----------------------------------------------------------------------
