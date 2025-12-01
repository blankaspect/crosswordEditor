/*====================================================================*\

BlockGrid.java

Class: block grid.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.crosswordeditor;

//----------------------------------------------------------------------


// IMPORTS


import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import java.awt.geom.Line2D;

import java.awt.image.BufferedImage;

import java.io.IOException;

import java.util.ArrayList;
import java.util.List;

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


// CLASS: BLOCK GRID


class BlockGrid
	extends Grid
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	public static final		int		MIN_BLOCK_IMAGE_NUM_LINES		= 0;
	public static final		int		MAX_BLOCK_IMAGE_NUM_LINES		= 64;
	public static final		int		DEFAULT_BLOCK_IMAGE_NUM_LINES	= 4;

	public static final		double	MIN_BLOCK_IMAGE_LINE_WIDTH		= 0.01;
	public static final		double	MAX_BLOCK_IMAGE_LINE_WIDTH		= 16.0;
	public static final		double	DEFAULT_BLOCK_IMAGE_LINE_WIDTH	= 1.5;

	public static final		Color	DEFAULT_BLOCK_IMAGE_COLOUR	= new Color(96, 96, 96);

	private static final	double	OFFSET_FACTOR	= 0.01;

	private static final	String	GRID_DEF_CHARS	= "01";

	private static final	String	BLOCK_IMAGE_PATHNAME	= "images/block-%d.png";

	private static final	String	IMAGE_SELECTOR	= HtmlConstants.ElementName.DIV + CssSelector.CLASS
														+ HtmlConstants.Class.BLOCK + CssSelector.CHILD
														+ HtmlConstants.ElementName.IMG;

	private static final	CssRuleSet	IMAGE_RULE_SET	= CssRuleSet.of
	(
		IMAGE_SELECTOR,
		StrKVPair.of(CssProperty.VERTICAL_ALIGN, "top")
	);

	private static final	List<CssRuleSet>	BLOCK_RULE_SETS	= List.of
	(
		CssRuleSet.of
		(
			HtmlConstants.ElementName.DIV + CssSelector.CLASS + HtmlConstants.Class.BLOCK,
			StrKVPair.of(CssProperty.BACKGROUND_COLOUR, "%s")
		),
		CssRuleSet.of
		(
			IMAGE_SELECTOR,
			StrKVPair.of(CssProperty.VISIBILITY, "hidden")
		)
	);

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private	Cell[][]			cells;
	private	EditList<BlockGrid>	editList;

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	public BlockGrid(
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

	public BlockGrid(
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
		List<Boolean> cellBlocked = new ArrayList<>();
		for (int i = 0; i < definition.length(); i++)
		{
			char ch = definition.charAt(i);
			int index = GRID_DEF_CHARS.indexOf(ch);
			if (index < 0)
				throw new AppException(ErrorId.ILLEGAL_CHARACTER_IN_GRID_DEFINITION, Character.toString(ch));
			cellBlocked.add(index != 0);
		}
		int[] dimensions = symmetry.getPrincipalDimensions(numColumns, numRows);
		if (cellBlocked.size() != dimensions[0] * dimensions[1])
			throw new AppException(ErrorId.MALFORMED_GRID_DEFINITION);

		// Initialise cells
		for (int i = 0; i < cellBlocked.size(); i++)
		{
			if (cellBlocked.get(i))
				setCellBlocked(i / dimensions[0], i % dimensions[0], true);
		}

		// Initialise fields
		initFields();
	}

	//------------------------------------------------------------------

	public BlockGrid(
		int				numColumns,
		int				numRows,
		BufferedImage	image,
		int				xOffset,
		int				yOffset,
		int				sampleSize,
		double			brightnessThreshold)
	{
		// Initialise instance variables
		this(numColumns, numRows);

		// Extract grid from image
		int imageWidth = image.getWidth();
		int imageHeight = image.getHeight();
		double cellWidth = (double)imageWidth / (double)numColumns;
		double cellHeight = (double)imageHeight / (double)numRows;
		double dx = (double)xOffset * OFFSET_FACTOR * cellWidth;
		double dy = (double)yOffset * OFFSET_FACTOR * cellHeight;

		double y = 0.5 * cellHeight;
		for (int row = 0; row < numRows; row++)
		{
			int imageY = (int)Math.round(y + dy);

			double x = 0.5 * cellWidth;
			for (int column = 0; column < numColumns; column++)
			{
				int imageX = (int)Math.round(x + dx);

				double brightnessSum = 0.0;
				int numSamples = 0;
				int endY = imageY + sampleSize;
				for (int iy = imageY - (sampleSize - 1); iy < endY; iy++)
				{
					int endX = imageX + sampleSize;
					for (int ix = imageX - (sampleSize - 1); ix < endX; ix++)
					{
						if ((ix >= 0) && (ix < imageWidth) && (iy >= 0) && (iy < imageHeight))
						{
							brightnessSum += ColourUtils.getBrightness(image.getRGB(ix, iy));
							++numSamples;
						}
					}
				}
				if ((numSamples == 0) || (brightnessSum / (double)numSamples < brightnessThreshold))
					cells[row][column].blocked = true;
				x += cellWidth;
			}
			y += cellHeight;
		}

		// Initialise fields
		initFields();

		// Update symmetry
		updateSymmetry();
	}

	//------------------------------------------------------------------

	private BlockGrid(
		BlockGrid	grid)
	{
		// Initialise instance variables
		this(grid.numColumns, grid.numRows);
		symmetry = grid.symmetry;
		editList = new EditList<>(AppConfig.INSTANCE.getMaxEditListLength());

		// Initialise cells
		for (int row = 0; row < numRows; row++)
		{
			for (int column = 0; column < numColumns; column++)
				cells[row][column].blocked = grid.cells[row][column].blocked;
		}

		// Initialise fields
		initFields();
	}

	//------------------------------------------------------------------

	private BlockGrid(
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

	public static String getBlockImagePathname(
		int	size)
	{
		return String.format(BLOCK_IMAGE_PATHNAME, size);
	}

	//------------------------------------------------------------------

	public static BufferedImage createBlockImage(
		int		size,
		int		numLines,
		float	lineWidth,
		Color	colour)
	{
		BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
		Graphics2D gr = image.createGraphics();

		gr.setColor(colour);
		if (numLines == 0)
			gr.fillRect(0, 0, size, size);
		else
		{
			gr.setRenderingHint(RenderingHints.KEY_ANTIALIASING,   RenderingHints.VALUE_ANTIALIAS_ON);
			gr.setRenderingHint(RenderingHints.KEY_RENDERING,      RenderingHints.VALUE_RENDER_QUALITY);
			gr.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
			gr.setStroke(new BasicStroke(lineWidth));
			double interval = (double)size / (double)numLines;
			double offset = 0.5 * interval;
			Line2D.Double line = new Line2D.Double();
			for (int i = 0; i < numLines; i++)
			{
				line.setLine(offset, 0.0, 0.0, offset);
				gr.draw(line);
				line.setLine((double)size, offset, offset, (double)size);
				gr.draw(line);
				offset += interval;
			}
		}

		return image;
	}

	//------------------------------------------------------------------

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

////////////////////////////////////////////////////////////////////////
//  Instance methods : overriding methods
////////////////////////////////////////////////////////////////////////

	@Override
	public Separator getSeparator()
	{
		return Separator.BLOCK;
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
				if (!cells[row][column].blocked
						&& ((column == 0) || cells[row][column - 1].blocked)
						&& ((column == numColumns - 1) || cells[row][column + 1].blocked)
						&& ((row == 0) || cells[row - 1][column].blocked)
						&& ((row == numRows - 1) || cells[row + 1][column].blocked))
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
				buffer.append(GRID_DEF_CHARS.charAt(cells[row][column].blocked ? 1 : 0));
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
		ruleSets.addAll(Cell.getStyleRuleSets(cellSize, config.getHtmlCellOffsetTop(), config.getHtmlCellOffsetLeft(),
											  gridColour, entryColour, config.getHtmlFieldNumOffsetTop(),
											  config.getHtmlFieldNumOffsetLeft(), fieldNumberFontSizeFactor));
		ruleSets.add(IMAGE_RULE_SET);
		return ruleSets;
	}

	//------------------------------------------------------------------

	@Override
	public List<CssMediaRule> getStyleMediaRules()
	{
		List<CssMediaRule> mediaRules = new ArrayList<>();
		AppConfig config = AppConfig.INSTANCE;
		if (config.isBlockImagePrintOnly())
		{
			CssMediaRule mediaRule = new CssMediaRule(CssMediaRule.MediaType.SCREEN);
			for (CssRuleSet ruleSet : BLOCK_RULE_SETS)
			{
				ruleSet = ruleSet.clone();
				ruleSet.replacePropertyValue(CssProperty.BACKGROUND_COLOUR,
											 ColourUtils.colourToHexString(config.getHtmlGridColour()));
				mediaRule.addRuleSet(ruleSet);
			}
			mediaRules.add(mediaRule);
		}
		return mediaRules;
	}

	//------------------------------------------------------------------

	@Override
	public Grid createCopy()
	{
		return new BlockGrid(this);
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
		EditList.Element<BlockGrid> edit = editList.removeUndo();
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
		EditList.Element<BlockGrid> edit = editList.removeRedo();
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

			// Make list of blocked state of cells of specified region
			int[] dimensions = symmetry.getPrincipalDimensions(numColumns, numRows);
			List<Boolean> blocked = new ArrayList<>();
			for (int row = 0; row < dimensions[1]; row++)
			{
				for (int column = 0; column < dimensions[0]; column++)
					blocked.add(cells[row][column].blocked);
			}

			// Clear blocked state of all cells
			for (int row = 0; row < numRows; row++)
			{
				for (int column = 0; column < numColumns; column++)
					cells[row][column].blocked = false;
			}

			// Initialise blocked state
			for (int i = 0; i < blocked.size(); i++)
			{
				if (blocked.get(i))
					setCellBlocked(i / dimensions[0], i % dimensions[0], true);
			}

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
				boolean blocked = cells[r1][c1].blocked;
				int c2 = numColumns - 1 - c1;
				int r2 = numRows - 1 - r1;
				switch (symmetry)
				{
					case NONE:
						// do nothing
						break;

					case ROTATION_HALF:
						if (cells[r2][c2].blocked != blocked)
							return false;
						break;

					case ROTATION_QUARTER:
						if ((cells[c1][r2].blocked != blocked) || (cells[r2][c2].blocked != blocked)
								|| (cells[c2][r1].blocked != blocked))
							return false;
						break;

					case REFLECTION_VERTICAL_AXIS:
						if (cells[r1][c2].blocked != blocked)
							return false;
						break;

					case REFLECTION_HORIZONTAL_AXIS:
						if (cells[r2][c1].blocked != blocked)
							return false;
						break;

					case REFLECTION_VERTICAL_HORIZONTAL_AXES:
						if ((cells[r1][c2].blocked != blocked) || (cells[r2][c1].blocked != blocked)
								|| (cells[r2][c2].blocked != blocked))
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

	public void setCellBlocked(
		int		row,
		int		column,
		boolean	blocked)
	{
		// Set specified cell
		int r1 = row;
		int c1 = column;
		cells[r1][c1].blocked = blocked;

		// Set corresponding cells in other regions
		int c2 = numColumns - 1 - c1;
		int r2 = numRows - 1 - r1;
		switch (symmetry)
		{
			case NONE:
				// do nothing
				break;

			case ROTATION_HALF:
				cells[r2][c2].blocked = blocked;
				break;

			case ROTATION_QUARTER:
				cells[c1][r2].blocked = blocked;
				cells[r2][c2].blocked = blocked;
				cells[c2][r1].blocked = blocked;
				break;

			case REFLECTION_VERTICAL_AXIS:
				cells[r1][c2].blocked = blocked;
				break;

			case REFLECTION_HORIZONTAL_AXIS:
				cells[r2][c1].blocked = blocked;
				break;

			case REFLECTION_VERTICAL_HORIZONTAL_AXES:
				cells[r1][c2].blocked = blocked;
				cells[r2][c1].blocked = blocked;
				cells[r2][c2].blocked = blocked;
				break;
		}
	}

	//------------------------------------------------------------------

	public void toggleBlock(
		int	row,
		int	column)
	{
		Cell[][] oldCells = copyCells(cells);
		setCellBlocked(row, column, !getCell(row, column).isBlocked());
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
				if (!cell.blocked)
				{
					Field field = null;
					if ((column == 0) || cells[row][column - 1].blocked)
					{
						int c = column + 1;
						while ((c < numColumns) && !cells[row][c].blocked)
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
					if ((row == 0) || cells[row - 1][column].blocked)
					{
						int r = row + 1;
						while ((r < numRows) && !cells[r][column].blocked)
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
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Enumerated types
////////////////////////////////////////////////////////////////////////


	// ENUMERATION: ERROR IDENTIFIERS


	private enum ErrorId
		implements AppException.IId
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		ILLEGAL_CHARACTER_IN_GRID_DEFINITION
		("The grid definition contains an illegal character: \"%1\"."),

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
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	boolean	blocked;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private Cell()
		{
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		public Cell clone()
		{
			return (Cell)super.clone();
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
			if (blocked)
			{
				AttributeList attributes = new AttributeList();
				attributes.add(HtmlConstants.AttrName.CLASS, HtmlConstants.Class.BLOCK);
				writer.writeElementStart(HtmlConstants.ElementName.DIV, attributes, indent, false, false);

				attributes.clear();
				attributes.add(HtmlConstants.AttrName.ALT, "");
				attributes.add(HtmlConstants.AttrName.SRC, getBlockImagePathname(cellSize));
				writer.writeEmptyElement(HtmlConstants.ElementName.IMG, attributes, 0, false, false);

				writer.writeElementEnd(HtmlConstants.ElementName.DIV, 0);
			}
			else
			{
				writer.writeElementStart(HtmlConstants.ElementName.DIV, indent, false);
				writeContents(writer, fieldNumber, entry);
				writer.writeElementEnd(HtmlConstants.ElementName.DIV, 0);
			}
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		public boolean isBlocked()
		{
			return blocked;
		}

		//--------------------------------------------------------------

	}

	//==================================================================


	// CLASS: EDIT


	private static class Edit
		extends EditList.Element<BlockGrid>
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
			BlockGrid	grid)
		{
			if (oldSymmetry != null)
				grid.symmetry = oldSymmetry;
			grid.cells = oldCells;
			grid.initFields();
		}

		//--------------------------------------------------------------

		@Override
		public void redo(
			BlockGrid	grid)
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
