/*====================================================================*\

CrosswordView.java

Class: crossword view.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.crosswordeditor;

//----------------------------------------------------------------------


// IMPORTS


import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.Point;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Element;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

import uk.blankaspect.common.collection.CollectionUtils;

import uk.blankaspect.common.exception2.UnexpectedRuntimeException;

import uk.blankaspect.common.misc.IStringKeyed;

import uk.blankaspect.common.string.StringUtils;

import uk.blankaspect.ui.swing.action.KeyAction;

import uk.blankaspect.ui.swing.colour.Colours;

import uk.blankaspect.ui.swing.font.FontUtils;

import uk.blankaspect.ui.swing.menu.FMenuItem;

import uk.blankaspect.ui.swing.misc.GuiUtils;

//----------------------------------------------------------------------


// CLASS: CROSSWORD VIEW


class CrosswordView
	extends JScrollPane
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	public static final		int	MIN_SELECTED_CLUE_NUM_COLUMNS		= 16;
	public static final		int	MAX_SELECTED_CLUE_NUM_COLUMNS		= 1024;
	public static final		int	DEFAULT_SELECTED_CLUE_NUM_COLUMNS	= 50;

	private static final	int	MIN_WIDTH	= 128;
	private static final	int	MIN_HEIGHT	= 64;

	private static final	int	SCROLL_UNIT_INCREMENT_FACTOR	= 1;
	private static final	int	SCROLL_BLOCK_INCREMENT_FACTOR	= 10;

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private	CrosswordPane	crosswordPane;

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	public CrosswordView(
		CrosswordDocument	document)
	{
		// Call superclass constructor
		super(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

		// Set view and size of viewport
		crosswordPane = new CrosswordPane(document);
		setViewportView(crosswordPane);
		viewport.setFocusable(true);

		// Get font metrics
		FontMetrics fontMetrics = getFontMetrics(AppFont.CLUE.getFont());

		// Set vertical scrolling increments
		JScrollBar vScrollBar = getVerticalScrollBar();
		vScrollBar.setFocusable(false);
		int scrollUnit = fontMetrics.getHeight();
		vScrollBar.setUnitIncrement(SCROLL_UNIT_INCREMENT_FACTOR * scrollUnit);
		vScrollBar.setBlockIncrement(SCROLL_BLOCK_INCREMENT_FACTOR * scrollUnit);

		// Set horizontal scrolling increments
		JScrollBar hScrollBar = getHorizontalScrollBar();
		hScrollBar.setFocusable(false);
		scrollUnit = fontMetrics.charWidth('n');
		hScrollBar.setUnitIncrement(SCROLL_UNIT_INCREMENT_FACTOR * scrollUnit);
		hScrollBar.setBlockIncrement(SCROLL_BLOCK_INCREMENT_FACTOR * scrollUnit);

		// Set properties
		setBorder(null);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : overriding methods
////////////////////////////////////////////////////////////////////////

	@Override
	public boolean requestFocusInWindow()
	{
		return crosswordPane.gridPane.requestFocusInWindow();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	public Clue.Id getSelectedClueId()
	{
		return crosswordPane.selectedClueId;
	}

	//------------------------------------------------------------------

	public void redraw()
	{
		crosswordPane.repaint();
	}

	//------------------------------------------------------------------

	public void redrawGrid()
	{
		crosswordPane.gridPane.repaint();
	}

	//------------------------------------------------------------------

	public void setPreferredViewportSize(
		Dimension	preferredSize)
	{
		if (preferredSize == null)
			preferredSize = crosswordPane.getPreferredSize();
		preferredSize = new Dimension(Math.max(MIN_WIDTH, preferredSize.width),
									  Math.max(MIN_HEIGHT, preferredSize.height));
		if (!preferredSize.equals(viewport.getExtentSize()))
		{
			CrosswordEditorApp app = CrosswordEditorApp.INSTANCE;
			for (int i = 0; i < app.getNumDocuments(); i++)
				app.getView(i).setViewportSize(preferredSize);
			app.getMainWindow().resize();
		}
	}

	//------------------------------------------------------------------

	public Point getViewportLocation()
	{
		Point location = viewport.getLocation();
		SwingUtilities.convertPointToScreen(location, this);
		return location;
	}

	//------------------------------------------------------------------

	public void drawCaret(
		boolean	draw)
	{
		crosswordPane.gridPane.drawCaret(draw);
	}

	//------------------------------------------------------------------

	public void updateGrid()
	{
		crosswordPane.setSelection(null);
		crosswordPane.updateGrid();
		for (Direction direction : Direction.DEFINED_DIRECTIONS)
			crosswordPane.updateCluePane(direction);
		revalidate();
		crosswordPane.repaint();
	}

	//------------------------------------------------------------------

	public void updateClues(
		Direction	direction)
	{
		crosswordPane.updateDirectionLabel(direction);
		crosswordPane.updateCluePane(direction);
		crosswordPane.updateSelection();
		revalidate();
		crosswordPane.repaint();
	}

	//------------------------------------------------------------------

	public void updateTextSections(
		EnumSet<CrosswordDocument.TextSection>	textSections)
	{
		for (CrosswordDocument.TextSection textSection : textSections)
		{
			switch (textSection)
			{
				case TITLE:
					crosswordPane.updateTitle();
					break;

				case PROLOGUE:
					crosswordPane.updatePrologue();
					break;

				case EPILOGUE:
					crosswordPane.updateEpilogue();
					break;
			}
		}
		revalidate();
		crosswordPane.repaint();
	}

	//------------------------------------------------------------------

	public void setGridCaretPosition(
		int	row,
		int			column,
		Direction	direction)
	{
		crosswordPane.gridPane.setCaretPosition(row, column, direction);
	}

	//------------------------------------------------------------------

	private void setViewportSize(
		Dimension	size)
	{
		viewport.setPreferredSize(size);
		viewport.setExtentSize(size);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Enumerated types
////////////////////////////////////////////////////////////////////////


	// ENUMERATION: COLOURS


	enum Colour
		implements IStringKeyed
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		BACKGROUND
		(
			"background",
			"Background",
			Colours.BACKGROUND
		),

		SELECTED_FIELD_BACKGROUND
		(
			"selectedFieldBackground",
			"Background, selected field",
			Colours.SELECTION_BACKGROUND
		),

		FOCUSED_SELECTED_FIELD_BACKGROUND
		(
			"focusedSelectedFieldBackground",
			"Background, focused selected field",
			new Color(248, 224, 128)
		),

		FULLY_INTERSECTING_FIELD_BACKGROUND
		(
			"fullyIntersectingFieldBackground",
			"Background, fully intersecting field",
			new Color(200, 224, 248)
		),

		ISOLATED_CELL_BACKGROUND
		(
			"isolatedCellBackground",
			"Background, isolated cell",
			new Color(240, 192, 192)
		),

		SELECTED_CLUE_BACKGROUND
		(
			"selectedClueBackground",
			"Background, selected clue",
			new Color(248, 232, 160)
		),

		SELECTED_EMPTY_CLUE_BACKGROUND
		(
			"selectedEmptyClueBackground",
			"Background, selected empty clue",
			new Color(232, 232, 224)
		),

		TEXT
		(
			"text",
			"Text",
			Colours.FOREGROUND
		),

		FIELD_NUMBER_TEXT
		(
			"fieldNumberText",
			"Text, field number",
			Colours.FOREGROUND
		),

		GRID_ENTRY_TEXT
		(
			"gridEntryText",
			"Text, grid entry",
			Colours.FOREGROUND
		),

		CLUE_TEXT
		(
			"clueText",
			"Text, clue",
			Colours.FOREGROUND
		),

		EMPTY_CLUE_TEXT
		(
			"emptyClueText",
			"Text, empty clue",
			new Color(160, 160, 160)
		),

		GRID_LINE
		(
			"gridLine",
			"Grid line and separator",
			Color.DARK_GRAY
		),

		TITLE_SEPARATOR
		(
			"titleSeparator",
			"Title separator",
			new Color(160, 160, 160)
		),

		CARET
		(
			"caret",
			"Caret",
			Color.RED
		),

		EDITING_BOX
		(
			"editingBox",
			"Editing box",
			new Color(128, 128, 160)
		),

		FOCUSED_EDITING_BOX
		(
			"focusedEditingBox",
			"Editing box, focused",
			Color.RED
		);

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	String	key;
		private	String	text;
		private	Color	defaultColour;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private Colour(
			String	key,
			String	text,
			Color	defaultColour)
		{
			this.key = key;
			this.text = text;
			this.defaultColour = defaultColour;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Class methods
	////////////////////////////////////////////////////////////////////

		public static Colour forKey(
			String	key)
		{
			return Arrays.stream(values()).filter(value -> value.key.equals(key)).findFirst().orElse(null);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : IStringKeyed interface
	////////////////////////////////////////////////////////////////////

		@Override
		public String getKey()
		{
			return key;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		public String toString()
		{
			return text;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		public Color get()
		{
			return AppConfig.INSTANCE.getViewColour(this);
		}

		//--------------------------------------------------------------

		public Color getDefaultColour()
		{
			return defaultColour;
		}

		//--------------------------------------------------------------

	}

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Member classes : non-inner classes
////////////////////////////////////////////////////////////////////////


	// CLASS: CLUE ELEMENT MAP


	private static class ClueElementMap
	{

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	List<Entry>	entries;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private ClueElementMap()
		{
			entries = new ArrayList<>();
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		private void add(
			Clue.Id	clueId,
			Element	element,
			boolean	reference,
			boolean	hasText)
		{
			entries.add(new Entry(clueId, element, reference, hasText));
		}

		//--------------------------------------------------------------

		private Entry getEntry(
			Clue.Id	clueId)
		{
			for (Entry entry : entries)
			{
				if (entry.clueId.equals(clueId))
					return entry;
			}
			return null;
		}

		//--------------------------------------------------------------

		private Clue.Id getClueId(
			Element	element)
		{
			for (Entry entry : entries)
			{
				if (entry.element.equals(element))
					return entry.clueId;
			}
			return null;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Member classes : non-inner classes
	////////////////////////////////////////////////////////////////////


		// CLASS: CLUE ELEMENT MAP ENTRY


		private static class Entry
		{

		////////////////////////////////////////////////////////////////
		//  Instance variables
		////////////////////////////////////////////////////////////////

			private	Clue.Id	clueId;
			private	Element	element;
			private	boolean	reference;
			private	boolean	hasText;

		////////////////////////////////////////////////////////////////
		//  Constructors
		////////////////////////////////////////////////////////////////

			private Entry(
				Clue.Id	clueId,
				Element	element,
				boolean	reference,
				boolean	hasText)
			{
				this.clueId = clueId;
				this.element = element;
				this.reference = reference;
				this.hasText = hasText;
			}

			//----------------------------------------------------------

		////////////////////////////////////////////////////////////////
		//  Instance methods
		////////////////////////////////////////////////////////////////

			private boolean isEmpty()
			{
				return !(reference || hasText);
			}

			//----------------------------------------------------------

		}

		//==============================================================

	}

	//==================================================================


	// CLASS: HORIZONTAL LINE


	private static class HorizontalLine
		extends JComponent
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private HorizontalLine()
		{
			// Set properties
			setPreferredSize(new Dimension(1, 1));
			setOpaque(true);
			setFocusable(false);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		protected void paintComponent(
			Graphics	gr)
		{
			// Draw line
			gr.setColor(Colour.TITLE_SEPARATOR.get());
			gr.drawLine(0, 0, getWidth() - 1, 0);
		}

		//--------------------------------------------------------------

	}

	//==================================================================


	// CLASS: CLUE PANE


	private static class CluePane
		extends JTextPane
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		private static final	int		MAX_HEIGHT	= 1 << 16;  // 65536

		private static final	String	SEPARATOR			= "  ";
		private static final	String	SPAN_PREFIX			= "span.";
		private static final	String	PARAGRAPH_PREFIX	= "paragraph.";

		private static final	String	BOLD_KEY					= SPAN_PREFIX + StyledText.StyleAttr.BOLD.getKey();
		private static final	String	COLOURS_KEY					= "colours";
		private static final	String	EMPTY_COLOURS_KEY			= "emptyColours";
		private static final	String	SELECTED_COLOURS_KEY		= "selectedColours";
		private static final	String	SELECTED_EMPTY_COLOURS_KEY	= "selectedEmptyColours";

		private static final	String	NO_CLUE_STR	= "(no clue)";

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	ClueElementMap	clueElementMap;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CluePane(
			CrosswordDocument	document)
		{
			this(document, null, 0);
		}

		//--------------------------------------------------------------

		private CluePane(
			CrosswordDocument	document,
			List<Clue>			clues,
			int					width)
		{
			// Call superclass constructor
			super(new DefaultStyledDocument(new StyleContext()));

			// Initialise instance variables
			clueElementMap = new ClueElementMap();

			// Set font
			AppFont.CLUE.apply(this);

			// Set properties
			setBackground(null);
			setForeground(Colour.CLUE_TEXT.get());
			setBorder(null);
			setEditable(false);
			setFocusable(false);
			if (getCaret() instanceof DefaultCaret caret)
				caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
			if (width > 0)
				setSize(new Dimension(width, MAX_HEIGHT));
			setTransferHandler(null);

			// Set clues
			if (!CollectionUtils.isNullOrEmpty(clues))
				setClues(document, clues);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		private void setClues(
			CrosswordDocument	document,
			List<Clue>			clues)
		{
			// Calculate line indents
			FontMetrics fontMetrics = getFontMetrics(getFont().deriveFont(Font.BOLD));
			int[] widths = new int[clues.size()];
			int indent1 = 0;
			for (int i = 0; i < clues.size(); i++)
			{
				widths[i] = fontMetrics.stringWidth(Integer.toString(clues.get(i).getFieldId().number));
				if (indent1 < widths[i])
					indent1 = widths[i];
			}
			int indent2 = fontMetrics.stringWidth(SEPARATOR);

			// Add paragraph styles to styled document
			StyledDocument styledDoc = getStyledDocument();
			Style defStyle = styledDoc.getStyle(StyleContext.DEFAULT_STYLE);
			List<String> paragraphStyleKeys = new ArrayList<>();
			for (int w : widths)
			{
				String key = PARAGRAPH_PREFIX + Integer.toString(w);
				if (!paragraphStyleKeys.contains(key))
				{
					Style style = styledDoc.addStyle(key, defStyle);
					StyleConstants.setLeftIndent(style, (float)(indent1 + indent2));
					StyleConstants.setFirstLineIndent(style, (float)-(w + indent2));
					StyleConstants.setSpaceBelow(style, 1.0f);
					paragraphStyleKeys.add(key);
				}
			}

			// Add bold span style to styled document
			List<String> spanStyleKeys = new ArrayList<>();
			spanStyleKeys.add(BOLD_KEY);
			StyledText.StyleAttr.BOLD.apply(styledDoc.addStyle(BOLD_KEY, defStyle));

			// Add background and foreground colour styles to styled document
			Style style = styledDoc.addStyle(COLOURS_KEY, null);
			StyleConstants.setBackground(style, Colour.BACKGROUND.get());
			StyleConstants.setForeground(style, Colour.CLUE_TEXT.get());

			style = styledDoc.addStyle(EMPTY_COLOURS_KEY, null);
			StyleConstants.setBackground(style, Colour.BACKGROUND.get());
			StyleConstants.setForeground(style, Colour.EMPTY_CLUE_TEXT.get());

			style = styledDoc.addStyle(SELECTED_COLOURS_KEY, null);
			StyleConstants.setBackground(style, Colour.SELECTED_CLUE_BACKGROUND.get());
			StyleConstants.setForeground(style, Colour.CLUE_TEXT.get());

			style = styledDoc.addStyle(SELECTED_EMPTY_COLOURS_KEY, null);
			StyleConstants.setBackground(style, Colour.SELECTED_EMPTY_CLUE_BACKGROUND.get());
			StyleConstants.setForeground(style, Colour.EMPTY_CLUE_TEXT.get());

			// Append text
			for (int i = 0; i < clues.size(); i++)
			{
				try
				{
					// Append LF
					if (i > 0)
						styledDoc.insertString(styledDoc.getLength(), "\n", defStyle);

					// Add element to map
					Clue clue = clues.get(i);
					Clue.Id clueId = clue.getId();
					int startOffset = styledDoc.getLength();
					clueElementMap.add(clueId, styledDoc.getParagraphElement(startOffset), clue.isReference(),
									   clue.hasText());

					// Append clue IDs
					Direction direction = clueId.fieldId.direction;
					styledDoc.insertString(styledDoc.getLength(), document.getClueIdString(direction, clue) + SEPARATOR,
										   styledDoc.getStyle(BOLD_KEY));

					// If reference, append reference ...
					if (clue.isReference())
					{
						styledDoc.insertString(styledDoc.getLength(), document.getClueReferenceString(direction, clue),
											   defStyle);
					}

					// ... otherwise, append clue
					else if (clue.hasText())
					{
						StyledText text = clue.getText();
						for (int j = 0; j < text.getNumSpans(); j++)
						{
							// Add span style to styled document
							StyledText.Span span = text.getSpan(j);
							String key = span.getAttributeKey();
							if (key.isEmpty())
								style = defStyle;
							else
							{
								key = SPAN_PREFIX + key;
								if (!spanStyleKeys.contains(key))
								{
									span.setAttributes(styledDoc.addStyle(key, defStyle));
									spanStyleKeys.add(key);
								}
								style = styledDoc.getStyle(key);
							}

							// Append span text
							styledDoc.insertString(styledDoc.getLength(), span.getText(), style);
						}
					}
					else
						styledDoc.insertString(styledDoc.getLength(), NO_CLUE_STR, defStyle);

					// Set background and foreground colours
					int offset = styledDoc.getLength();
					String key = clue.isEmpty() ? EMPTY_COLOURS_KEY : COLOURS_KEY;
					styledDoc.setCharacterAttributes(startOffset, offset - startOffset, styledDoc.getStyle(key), false);

					// Set paragraph attributes
					key = PARAGRAPH_PREFIX + Integer.toString(widths[i]);
					styledDoc.setParagraphAttributes(startOffset, offset - startOffset, styledDoc.getStyle(key), true);
				}
				catch (BadLocationException e)
				{
					throw new UnexpectedRuntimeException(e);
				}
			}
		}

		//--------------------------------------------------------------

		private void setElementStyle(
			Element	element,
			String	styleKey)
		{
			StyledDocument styledDoc = getStyledDocument();
			Style style = styledDoc.getStyle(styleKey);
			int startOffset = element.getStartOffset();
			styledDoc.setCharacterAttributes(startOffset, element.getEndOffset() - startOffset, style, false);
		}

		//--------------------------------------------------------------

	}

	//==================================================================


	// CLASS: TEXT SECTION PANE


	private static class TextSectionPane
		extends JTextPane
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		private static final	int		MAX_HEIGHT	= 50000;

		private static final	String	SPAN_PREFIX		= "span.";
		private static final	String	PARAGRAPH_KEY	= "paragraph";

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	int	prefWidth;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private TextSectionPane()
		{
			// Set font
			AppFont.MAIN.apply(this);

			// Set properties
			setBackground(null);
			setForeground(Colour.CLUE_TEXT.get());
			setBorder(null);
			setEditable(false);
			setFocusable(false);
			if (getCaret() instanceof DefaultCaret caret)
				caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
			setTransferHandler(null);

			// Hide pane
			setVisible(false);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		private void setText(
			List<String>	paragraphs,
			boolean			spaceAbove)
		{
			// Create styled document
			StyledDocument styledDoc = new DefaultStyledDocument(new StyleContext());

			// Add paragraph style to document
			Style defStyle = styledDoc.getStyle(StyleContext.DEFAULT_STYLE);
			Style style = styledDoc.addStyle(PARAGRAPH_KEY, defStyle);
			float space = (float)StyleConstants.getFontSize(style) * 0.5f;
			if (spaceAbove)
				StyleConstants.setSpaceAbove(style, space);
			else
				StyleConstants.setSpaceBelow(style, space);

			// Append text
			String lineBreak = AppConfig.INSTANCE.getTextSectionLineBreak() + "\n";
			List<String> spanStyleKeys = new ArrayList<>();
			for (int i = 0; i < paragraphs.size(); i++)
			{
				try
				{
					// Append LF
					if (i > 0)
						styledDoc.insertString(styledDoc.getLength(), "\n", defStyle);

					// Add element to map
					int startOffset = styledDoc.getLength();
					StyledText text = new StyledText(paragraphs.get(i));
					int numLineBreaks = 0;
					for (int j = 0; j < text.getNumSpans(); j++)
					{
						// Add span style to styled document
						StyledText.Span span = text.getSpan(j);
						String key = span.getAttributeKey();
						if (key.isEmpty())
							style = defStyle;
						else
						{
							key = SPAN_PREFIX + key;
							if (!spanStyleKeys.contains(key))
							{
								span.setAttributes(styledDoc.addStyle(key, defStyle));
								spanStyleKeys.add(key);
							}
							style = styledDoc.getStyle(key);
						}

						// Append span text
						String spanText = span.getText();
						while (true)
						{
							// Test for line break
							int index = spanText.indexOf(lineBreak);
							if (index < 0)
								break;

							// Append text up to line break
							styledDoc.insertString(styledDoc.getLength(), spanText.substring(0, index) + "\n", style);

							// Set paragraph attributes
							Style paraStyle = (spaceAbove && (numLineBreaks == 0))
																	? styledDoc.getStyle(PARAGRAPH_KEY)
																	: defStyle;
							styledDoc.setParagraphAttributes(startOffset, styledDoc.getLength() - startOffset,
															 paraStyle, true);

							// Update variables
							startOffset = styledDoc.getLength();
							spanText = spanText.substring(index + lineBreak.length());
							++numLineBreaks;
						}

						// Append text after any line break
						styledDoc.insertString(styledDoc.getLength(), spanText, style);
					}

					// Set paragraph attributes
					Style paraStyle = (!spaceAbove || (numLineBreaks == 0)) ? styledDoc.getStyle(PARAGRAPH_KEY)
																			: defStyle;
					styledDoc.setParagraphAttributes(startOffset, styledDoc.getLength() - startOffset, paraStyle, true);
				}
				catch (StyledText.ParseException | BadLocationException e)
				{
					throw new UnexpectedRuntimeException(e);
				}
			}

			// Set document
			setStyledDocument(styledDoc);

			// Hide pane if there is no text
			setVisible(!paragraphs.isEmpty());

			// Apply preferred width
			setPrefWidth(-1);
		}

		//--------------------------------------------------------------

		private void setPrefWidth(
			int	width)
		{
			if (width >= 0)
				prefWidth = width;

			if (prefWidth > 0)
			{
				setPreferredSize(null);
				setSize(new Dimension(prefWidth, MAX_HEIGHT));
				setPreferredSize(new Dimension(prefWidth, getPreferredSize().height));
				revalidate();
			}
		}

		//--------------------------------------------------------------

	}

	//==================================================================


	// CLASS: CROSSWORD PANE


	private static class CrosswordPane
		extends JPanel
		implements ActionListener, KeyListener, MouseListener
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		private static final	int	CLUES_VERTICAL_MARGIN	= 2;
		private static final	int	HORIZONTAL_GAP			= 16;

		private static final	float	FONT_SIZE_FACTOR	= 1.125f;

		private static final	KeyStroke	FOCUS_FORWARD_KEY	= KeyStroke.getKeyStroke
		(
			KeyEvent.VK_TAB,
			KeyEvent.CTRL_DOWN_MASK
		);
		private static final	KeyStroke	FOCUS_BACKWARD_KEY	= KeyStroke.getKeyStroke
		(
			KeyEvent.VK_TAB,
			KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK
		);

		// Commands
		private interface Command
		{
			String	MOVE_CARET_UP			= "moveCaretUp";
			String	MOVE_CARET_DOWN			= "moveCaretDown";
			String	MOVE_CARET_LEFT			= "moveCaretLeft";
			String	MOVE_CARET_RIGHT		= "moveCaretRight";
			String	MOVE_CARET_TO_START		= "moveCaretToStart";
			String	MOVE_CARET_TO_END		= "moveCaretToEnd";
			String	SELECT_ACROSS_CLUE		= "selectAcrossClue";
			String	SELECT_DOWN_CLUE		= "selectDownClue";
			String	SELECT_PREVIOUS_CLUE	= "selectPreviousClue";
			String	SELECT_NEXT_CLUE		= "selectNextClue";
			String	SHOW_CONTEXT_MENU		= "showContextMenu";
		}

		private static final	KeyAction.KeyCommandPair[]	KEY_COMMANDS	=
		{
			KeyAction.command(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0),
							  Command.MOVE_CARET_UP),
			KeyAction.command(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0),
							  Command.MOVE_CARET_DOWN),
			KeyAction.command(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0),
							  Command.MOVE_CARET_LEFT),
			KeyAction.command(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0),
							  Command.MOVE_CARET_RIGHT),
			KeyAction.command(KeyStroke.getKeyStroke(KeyEvent.VK_HOME, 0),
							  Command.MOVE_CARET_TO_START),
			KeyAction.command(KeyStroke.getKeyStroke(KeyEvent.VK_END, 0),
							  Command.MOVE_CARET_TO_END),
			KeyAction.command(KeyStroke.getKeyStroke(KeyEvent.VK_UP, KeyEvent.CTRL_DOWN_MASK),
							  Command.SELECT_DOWN_CLUE),
			KeyAction.command(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, KeyEvent.CTRL_DOWN_MASK),
							  Command.SELECT_DOWN_CLUE),
			KeyAction.command(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, KeyEvent.CTRL_DOWN_MASK),
							  Command.SELECT_ACROSS_CLUE),
			KeyAction.command(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, KeyEvent.CTRL_DOWN_MASK),
							  Command.SELECT_ACROSS_CLUE),
			KeyAction.command(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, KeyEvent.SHIFT_DOWN_MASK),
							  Command.SELECT_PREVIOUS_CLUE),
			KeyAction.command(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0),
							  Command.SELECT_NEXT_CLUE),
			KeyAction.command(KeyStroke.getKeyStroke(KeyEvent.VK_CONTEXT_MENU, 0),
							  Command.SHOW_CONTEXT_MENU)
		};

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	CrosswordDocument			document;
		private	int							cluePaneWidth;
		private	Clue.Id						selectedClueId;
		private	JLabel						titleLabel;
		private	HorizontalLine				horizontalLine;
		private	GridPane					gridPane;
		private	CluePane					selectedCluePane;
		private	Map<Direction, JLabel>		directionLabels;
		private	Map<Direction, CluePane>	cluePanes;
		private	TextSectionPane				prologuePane;
		private	TextSectionPane				epiloguePane;
		private	JPopupMenu					contextMenu;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CrosswordPane(
			CrosswordDocument	document)
		{
			// Initialise instance variables
			this.document = document;

			// Set border
			GuiUtils.setPaddedLineBorder(this, 8);

			// Set properties
			setBackground(Colour.BACKGROUND.get());


			//----  Title label

			titleLabel = createTitleLabel();
			horizontalLine = new HorizontalLine();
			horizontalLine.setVisible(titleLabel.isVisible());


			//----  Upper pane

			GridBagLayout gridBag = new GridBagLayout();
			GridBagConstraints gbc = new GridBagConstraints();

			JPanel upperPane = new JPanel(gridBag);
			upperPane.setBackground(null);

			int gridX = 0;

			// Pane: grid
			gridPane = document.getGrid().getSeparator().createGridPane(document);
			gridPane.addKeyListener(this);
			gridPane.addMouseListener(this);

			gbc.gridx = gridX++;
			gbc.gridy = 0;
			gbc.gridwidth = 1;
			gbc.gridheight = 1;
			gbc.weightx = 0.0;
			gbc.weighty = 0.0;
			gbc.anchor = GridBagConstraints.FIRST_LINE_START;
			gbc.fill = GridBagConstraints.NONE;
			gbc.insets = new Insets(0, 0, 0, 0);
			gridBag.setConstraints(gridPane, gbc);
			upperPane.add(gridPane);

			// Pane: selected clue
			selectedCluePane = new CluePane(document);
			int width = FontUtils.getCharWidth('0', selectedCluePane.getFontMetrics(selectedCluePane.getFont()));
			width *= AppConfig.INSTANCE.getSelectedClueNumColumns();
			selectedCluePane.setPreferredSize(new Dimension(width, gridPane.getPreferredSize().height));
			selectedCluePane.addMouseListener(this);

			gbc.gridx = gridX++;
			gbc.gridy = 0;
			gbc.gridwidth = 1;
			gbc.gridheight = 1;
			gbc.weightx = 0.0;
			gbc.weighty = 0.0;
			gbc.anchor = GridBagConstraints.FIRST_LINE_END;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.insets = new Insets(0, HORIZONTAL_GAP, 0, 0);
			gridBag.setConstraints(selectedCluePane, gbc);
			upperPane.add(selectedCluePane);


			//----  Lower pane

			JPanel lowerPane = new JPanel(gridBag);
			lowerPane.setBackground(null);

			// Add clue pane
			int numCluePanes = Direction.DEFINED_DIRECTIONS.size();
			cluePaneWidth = (upperPane.getPreferredSize().width - (numCluePanes - 1) * HORIZONTAL_GAP) / numCluePanes;
			directionLabels = new EnumMap<>(Direction.class);
			cluePanes = new EnumMap<>(Direction.class);

			gridX = 0;
			for (Direction direction : Direction.DEFINED_DIRECTIONS)
			{
				int horizontalMargin = (gridX == 0) ? 0 : HORIZONTAL_GAP;

				// Label: direction
				JLabel directionLabel = new JLabel(direction.toString());
				directionLabels.put(direction, directionLabel);
				Font font = AppFont.MAIN.getFont();
				directionLabel.setFont(font.deriveFont(Font.BOLD, FONT_SIZE_FACTOR * font.getSize2D()));
				directionLabel.setVisible(document.isShowClues());

				gbc.gridx = gridX;
				gbc.gridy = 0;
				gbc.gridwidth = 1;
				gbc.gridheight = 1;
				gbc.weightx = 0.0;
				gbc.weighty = 0.0;
				gbc.anchor = GridBagConstraints.FIRST_LINE_START;
				gbc.fill = GridBagConstraints.NONE;
				gbc.insets = new Insets(0, horizontalMargin, 0, 0);
				gridBag.setConstraints(directionLabel, gbc);
				lowerPane.add(directionLabel);

				// Pane: clue
				CluePane cluePane = createCluePane(direction);
				cluePanes.put(direction, cluePane);

				gbc.gridx = gridX++;
				gbc.gridy = 1;
				gbc.gridwidth = 1;
				gbc.gridheight = 1;
				gbc.weightx = 0.0;
				gbc.weighty = 0.0;
				gbc.anchor = GridBagConstraints.FIRST_LINE_START;
				gbc.fill = GridBagConstraints.VERTICAL;
				gbc.insets = new Insets(CLUES_VERTICAL_MARGIN, horizontalMargin, 0, 0);
				gridBag.setConstraints(cluePane, gbc);
				lowerPane.add(cluePane);
			}


			//----  Prologue and epilogue panes

			prologuePane = new TextSectionPane();
			prologuePane.addMouseListener(this);

			epiloguePane = new TextSectionPane();
			epiloguePane.addMouseListener(this);

			upperPane.addComponentListener(new ComponentAdapter()
			{
				@Override
				public void componentResized(
					ComponentEvent	event)
				{
					int width = upperPane.getPreferredSize().width;
					prologuePane.setPrefWidth(width);
					epiloguePane.setPrefWidth(width);
				}
			});


			//----  This pane

			// Set layout manager
			setLayout(gridBag);

			// Initialise y index
			int gridY = 0;

			gbc.gridx = 0;
			gbc.gridy = gridY++;
			gbc.gridwidth = 1;
			gbc.gridheight = 1;
			gbc.weightx = 0.0;
			gbc.weighty = 0.0;
			gbc.anchor = GridBagConstraints.FIRST_LINE_START;
			gbc.fill = GridBagConstraints.NONE;
			gbc.insets = new Insets(0, 0, 0, 0);
			gridBag.setConstraints(titleLabel, gbc);
			add(titleLabel);

			gbc.gridx = 0;
			gbc.gridy = gridY++;
			gbc.gridwidth = 1;
			gbc.gridheight = 1;
			gbc.weightx = 0.0;
			gbc.weighty = 0.0;
			gbc.anchor = GridBagConstraints.FIRST_LINE_START;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.insets = new Insets(0, 0, 8, 0);
			gridBag.setConstraints(horizontalLine, gbc);
			add(horizontalLine);

			gbc.gridx = 0;
			gbc.gridy = gridY++;
			gbc.gridwidth = 1;
			gbc.gridheight = 1;
			gbc.weightx = 0.0;
			gbc.weighty = 0.0;
			gbc.anchor = GridBagConstraints.FIRST_LINE_START;
			gbc.fill = GridBagConstraints.NONE;
			gbc.insets = new Insets(0, 0, 8, 0);
			gridBag.setConstraints(prologuePane, gbc);
			add(prologuePane);

			gbc.gridx = 0;
			gbc.gridy = gridY++;
			gbc.gridwidth = 1;
			gbc.gridheight = 1;
			gbc.weightx = 0.0;
			gbc.weighty = 0.0;
			gbc.anchor = GridBagConstraints.FIRST_LINE_START;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.insets = new Insets(0, 0, 0, 0);
			gridBag.setConstraints(upperPane, gbc);
			add(upperPane);

			gbc.gridx = 0;
			gbc.gridy = gridY++;
			gbc.gridwidth = 1;
			gbc.gridheight = 1;
			gbc.weightx = 0.0;
			gbc.weighty = 0.0;
			gbc.anchor = GridBagConstraints.FIRST_LINE_START;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.insets = new Insets(8, 0, 0, 0);
			gridBag.setConstraints(lowerPane, gbc);
			add(lowerPane);

			gbc.gridx = 0;
			gbc.gridy = gridY++;
			gbc.gridwidth = 1;
			gbc.gridheight = 1;
			gbc.weightx = 0.0;
			gbc.weighty = 0.0;
			gbc.anchor = GridBagConstraints.FIRST_LINE_START;
			gbc.fill = GridBagConstraints.NONE;
			gbc.insets = new Insets(8, 0, 0, 0);
			gridBag.setConstraints(epiloguePane, gbc);
			add(epiloguePane);

			Box.Filler filler = GuiUtils.spacer();

			gbc.gridx = 0;
			gbc.gridy = gridY++;
			gbc.gridwidth = 1;
			gbc.gridheight = 1;
			gbc.weightx = 0.0;
			gbc.weighty = 1.0;
			gbc.anchor = GridBagConstraints.FIRST_LINE_START;
			gbc.fill = GridBagConstraints.NONE;
			gbc.insets = new Insets(0, 0, 0, 0);
			gridBag.setConstraints(filler, gbc);
			add(filler);

			// Remove Tab and Shift+Tab from focus traversal keys
			setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS,  Set.of(FOCUS_FORWARD_KEY));
			setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, Set.of(FOCUS_BACKWARD_KEY));

			// Add commands to action map
			KeyAction.create(this, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, this, KEY_COMMANDS);

			// Add listeners
			addMouseListener(this);

			// Set prologue and epilogue
			updatePrologue();
			updateEpilogue();
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : ActionListener interface
	////////////////////////////////////////////////////////////////////

		@Override
		public void actionPerformed(
			ActionEvent	event)
		{
			switch (event.getActionCommand())
			{
				case Command.MOVE_CARET_UP        -> onMoveCaretUp();
				case Command.MOVE_CARET_DOWN      -> onMoveCaretDown();
				case Command.MOVE_CARET_LEFT      -> onMoveCaretLeft();
				case Command.MOVE_CARET_RIGHT     -> onMoveCaretRight();
				case Command.MOVE_CARET_TO_START  -> onMoveCaretToStart();
				case Command.MOVE_CARET_TO_END    -> onMoveCaretToEnd();
				case Command.SELECT_ACROSS_CLUE   -> onSelectAcrossClue();
				case Command.SELECT_DOWN_CLUE     -> onSelectDownClue();
				case Command.SELECT_PREVIOUS_CLUE -> onSelectPreviousClue();
				case Command.SELECT_NEXT_CLUE     -> onSelectNextClue();
				case Command.SHOW_CONTEXT_MENU    -> onShowContextMenu();
			}
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : KeyListener interface
	////////////////////////////////////////////////////////////////////

		@Override
		public void keyPressed(
			KeyEvent	event)
		{
			switch (event.getKeyCode())
			{
				case KeyEvent.VK_DELETE     -> setEntryChar(Grid.Entries.UNDEFINED_VALUE, 0);
				case KeyEvent.VK_BACK_SPACE -> setEntryChar(Grid.Entries.UNDEFINED_VALUE, -1);
			}
		}

		//--------------------------------------------------------------

		@Override
		public void keyReleased(
			KeyEvent	event)
		{
			// do nothing
		}

		//--------------------------------------------------------------

		@Override
		public void keyTyped(
			KeyEvent	event)
		{
			char ch = Character.toUpperCase(event.getKeyChar());
			if (AppConfig.INSTANCE.getGridEntryCharacters().indexOf(ch) >= 0)
				setEntryChar(ch, 1);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : MouseListener interface
	////////////////////////////////////////////////////////////////////

		@Override
		public void mouseClicked(
			MouseEvent	event)
		{
			if (SwingUtilities.isLeftMouseButton(event) && (event.getClickCount() > 1))
			{
				Component component = event.getComponent();

				// Text-section panes
				if ((component == titleLabel) || (component == prologuePane) || (component == epiloguePane))
					document.executeCommand(CrosswordDocument.Command.EDIT_TEXT_SECTIONS);

				// Clue panes
				else if (selectedClueId != null)
				{
					for (Direction direction : cluePanes.keySet())
					{
						if (component == cluePanes.get(direction))
						{
							document.executeCommand(CrosswordDocument.Command.EDIT_CLUE);
							break;
						}
					}
				}
			}
		}

		//--------------------------------------------------------------

		@Override
		public void mouseEntered(
			MouseEvent	event)
		{
			// do nothing
		}

		//--------------------------------------------------------------

		@Override
		public void mouseExited(
			MouseEvent	event)
		{
			// do nothing
		}

		//--------------------------------------------------------------

		@Override
		public void mousePressed(
			MouseEvent	event)
		{
			if (SwingUtilities.isLeftMouseButton(event))
			{
				Component component = event.getComponent();

				// Grid pane
				if (component == gridPane)
				{
					Grid grid = document.getGrid();
					int row = event.getY() / gridPane.getCellSize();
					int column = event.getX() / gridPane.getCellSize();
					if ((row >= 0) && (row < grid.getNumRows()) &&
						 (column >= 0) && (column < grid.getNumColumns()))
					{
						List<Grid.Field> cellFields = document.getGrid().getCell(row, column).getFields();
						if (!cellFields.isEmpty())
						{
							// Create a list of clues for the fields of the cell, and a list of fields
							// corresponding to the clues
							List<Clue> clues = new ArrayList<>();
							List<Grid.Field> fields = new ArrayList<>();
							for (Grid.Field field : cellFields)
							{
								clues.addAll(document.findPrimaryClues(field.getId()));
								if (fields.size() == clues.size())
									clues.add(null);
								while (fields.size() < clues.size())
									fields.add(field);
							}

							// Get the index of the next field in the cycle
							Grid.Field selectedField = gridPane.getSelectedField();
							int index = 0;
							for (int i = 0; i < clues.size(); i++)
							{
								Clue clue = clues.get(i);
								if ((clue == null) ? fields.get(i).equals(selectedField)
												   : clue.getId().equals(selectedClueId))
								{
									if (++i < clues.size())
										index = i;
									break;
								}
							}

							// Select the field and clue
							Grid.Field field = fields.get(index);
							Clue clue = clues.get(index);
							if (clue == null)
							{
								gridPane.setSelection(new Clue.FieldList(field), null);
								setSelectedClue(new Clue.Id(field.getId(), 0));
							}
							else
							{
								gridPane.setSelection(new Clue.FieldList(grid.getFields(clue), field),
													   new Grid.IndexPair(row, column));
								setSelectedClue(clue.getId());
							}
						}
					}
				}

				// Clue panes
				else
				{
					for (Direction direction : cluePanes.keySet())
					{
						CluePane cluePane = cluePanes.get(direction);
						if (component == cluePane)
						{
							int pos = cluePane.viewToModel2D(event.getPoint());
							Element element = cluePane.getStyledDocument().getParagraphElement(pos);
							setSelection(cluePane.clueElementMap.getClueId(element));
							break;
						}
					}
				}
			}

			// Context menu
			showContextMenu(event);
		}

		//--------------------------------------------------------------

		@Override
		public void mouseReleased(
			MouseEvent	event)
		{
			showContextMenu(event);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		public void updateTitle()
		{
			Container parent = titleLabel.getParent();
			for (int i = 0; i < parent.getComponentCount(); i++)
			{
				if (parent.getComponent(i) == titleLabel)
				{
					GridBagConstraints constraints = ((GridBagLayout)parent.getLayout()).getConstraints(titleLabel);
					parent.remove(i);
					titleLabel = createTitleLabel();
					horizontalLine.setVisible(titleLabel.isVisible());
					parent.add(titleLabel, constraints, i);
					break;
				}
			}
		}

		//--------------------------------------------------------------

		public void updatePrologue()
		{
			prologuePane.setText(document.getPrologue(), false);
		}

		//--------------------------------------------------------------

		public void updateEpilogue()
		{
			epiloguePane.setText(document.getEpilogue(), true);
		}

		//--------------------------------------------------------------

		private CluePane createCluePane(
			Direction	direction)
		{
			// Create a list of clues that contains undefined clues for fields that do not have clues
			List<Clue> clues = new ArrayList<>(document.getClues(direction));
			List<Clue> undefinedClues = new ArrayList<>();
			List<Grid.Field> fields = document.getGrid().getFields(direction);
			for (Grid.Field field : fields)
			{
				Grid.Field.Id fieldId = field.getId();
				Clue clue = new Clue(fieldId);
				if (clues.stream().noneMatch(clue0 -> Clue.COMPARATOR.compare(clue, clue0) == 0))
					undefinedClues.add(clue);
			}
			clues.addAll(undefinedClues);
			clues.sort(Clue.COMPARATOR);

			// Create pane
			CluePane pane = new CluePane(document, clues, cluePaneWidth);
			int height = pane.getPreferredSize().height;
			pane = new CluePane(document, clues, 0);
			pane.setPreferredSize(new Dimension(cluePaneWidth, height));
			pane.setVisible(document.isShowClues());
			pane.addMouseListener(this);
			return pane;
		}

		//--------------------------------------------------------------

		private JLabel createTitleLabel()
		{
			String title = document.getTitle();
			boolean isTitle = !StringUtils.isNullOrEmpty(title);
			JLabel label = new JLabel(title);
			Font font = AppFont.MAIN.getFont();
			label.setFont(font.deriveFont(Font.BOLD, FONT_SIZE_FACTOR * font.getSize2D()));
			label.setForeground(Colour.TEXT.get());
			label.setVisible(isTitle);
			if (isTitle)
				label.addMouseListener(this);
			return label;
		}

		//--------------------------------------------------------------

		private void updateGrid()
		{
			gridPane.setGrid(document.getGrid());
		}

		//--------------------------------------------------------------

		private void updateDirectionLabel(
			Direction	direction)
		{
			JLabel directionLabel = directionLabels.get(direction);
			if (directionLabel != null)
				directionLabel.setVisible(document.isShowClues());
		}

		//--------------------------------------------------------------

		private void updateCluePane(
			Direction	direction)
		{
			CluePane cluePane = cluePanes.get(direction);
			if (cluePane != null)
			{
				Container parent = cluePane.getParent();
				for (int i = 0; i < parent.getComponentCount(); i++)
				{
					if (parent.getComponent(i) == cluePane)
					{
						GridBagConstraints constraints = ((GridBagLayout)parent.getLayout()).getConstraints(cluePane);
						parent.remove(i);
						cluePane = createCluePane(direction);
						parent.add(cluePane, constraints, i);
						cluePanes.put(direction, cluePane);
						break;
					}
				}
			}
		}

		//--------------------------------------------------------------

		private void updateSelectedClue(
			Clue.FieldList	selectedFields)
		{
			if (selectedFields != null)
			{
				Clue.Id clueId = null;
				if (!selectedFields.isEmpty())
				{
					Grid.Field.Id fieldId = selectedFields.getField().getId();
					for (Clue clue : document.findClues(fieldId))
					{
						Clue primaryClue = clue;
						if ((clue != null) && clue.isReference())
							primaryClue = document.findClue(clue.getReferentId());
						if ((primaryClue != null) && selectedFields.matches(document.getGrid().getFields(primaryClue)))
						{
							clueId = clue.getId();
							break;
						}
					}
				}
				setSelectedClue(clueId);
			}
		}

		//--------------------------------------------------------------

		private List<Clue.Id> getClueIds(
			Clue.Id	clueId)
		{
			List<Clue.Id> clueIds = document.getClueIds(clueId);
			if (clueIds.isEmpty())
				clueIds.add(clueId);
			return clueIds;
		}

		//--------------------------------------------------------------

		private void setSelectedClue(
			Clue.Id	clueId)
		{
			if ((clueId == null) ? (selectedClueId != null) : !clueId.equals(selectedClueId))
			{
				// Deselect current clue
				if (selectedClueId != null)
				{
					for (Clue.Id id : getClueIds(selectedClueId))
					{
						for (Direction direction : cluePanes.keySet())
						{
							CluePane cluePane = cluePanes.get(direction);
							ClueElementMap.Entry entry = cluePane.clueElementMap.getEntry(id);
							if (entry != null)
							{
								cluePane.setElementStyle(entry.element,
														  entry.isEmpty() ? CluePane.EMPTY_COLOURS_KEY
																		  : CluePane.COLOURS_KEY);
								break;
							}
						}
					}
					selectedCluePane.setText(null);
					selectedClueId = null;
				}

				// Select new clue
				if (clueId != null)
				{
					for (Clue.Id id : getClueIds(clueId))
					{
						for (Direction direction : cluePanes.keySet())
						{
							CluePane cluePane = cluePanes.get(direction);
							ClueElementMap.Entry entry = cluePane.clueElementMap.getEntry(id);
							if (entry != null)
							{
								cluePane.setElementStyle(entry.element,
														  entry.isEmpty()
																	? CluePane.SELECTED_EMPTY_COLOURS_KEY
																	: CluePane.SELECTED_COLOURS_KEY);
								break;
							}
						}
					}
					Clue clue = document.findPrimaryClue(clueId);
					if (clue != null)
						selectedCluePane.setClues(document, List.of(clue));
					selectedClueId = clueId;
				}

				// Update "edit clue" command
				CrosswordDocument.Command.EDIT_CLUE.setEnabled(selectedClueId != null);
			}
		}

		//--------------------------------------------------------------

		private void updateSelection()
		{
			Clue.Id clueId = selectedClueId;
			setSelection(null);
			if (clueId != null)
				setSelection(clueId);
		}

		//--------------------------------------------------------------

		private void setSelection(
			Clue.Id	clueId)
		{
			Clue.FieldList fields = new Clue.FieldList();
			if (clueId != null)
			{
				Grid grid = document.getGrid();
				Grid.Field field = grid.getField(clueId.fieldId);
				Clue clue = document.findPrimaryClue(clueId);
				fields = (clue == null) ? new Clue.FieldList(field)
										: new Clue.FieldList(grid.getFields(clue), field);
			}
			gridPane.requestFocusInWindow();
			gridPane.setSelection(fields);
			setSelectedClue(clueId);
		}

		//--------------------------------------------------------------

		private void setEntryChar(
			char	value,
			int		increment)
		{
			if (gridPane.setEntryChar(value, increment))
				updateSelectedClue(gridPane.incrementCaretPosition(increment));
		}

		//--------------------------------------------------------------

		private void incrementCaretColumn(
			int	increment)
		{
			updateSelectedClue(gridPane.incrementCaretColumn(increment));
		}

		//--------------------------------------------------------------

		private void incrementCaretRow(
			int	increment)
		{
			updateSelectedClue(gridPane.incrementCaretRow(increment));
		}

		//--------------------------------------------------------------

		private void showContextMenu(
			MouseEvent	event)
		{
			if ((event == null) || event.isPopupTrigger())
			{
				// Create context menu
				if (contextMenu == null)
				{
					contextMenu = new JPopupMenu();

					contextMenu.add(new FMenuItem(CrosswordDocument.Command.EDIT_CLUE));
					contextMenu.add(new FMenuItem(CrosswordDocument.Command.EDIT_GRID));
					contextMenu.add(new FMenuItem(CrosswordDocument.Command.EDIT_TEXT_SECTIONS));
					contextMenu.add(new FMenuItem(CrosswordDocument.Command.EDIT_INDICATIONS));

					contextMenu.addSeparator();

					contextMenu.add(new FMenuItem(CrosswordDocument.Command.COPY_CLUES_TO_CLIPBOARD));
					contextMenu.add(new FMenuItem(CrosswordDocument.Command.IMPORT_CLUES_FROM_CLIPBOARD));
					contextMenu.add(new FMenuItem(CrosswordDocument.Command.CLEAR_CLUES));

					contextMenu.addSeparator();

					contextMenu.add(new FMenuItem(CrosswordDocument.Command.COPY_ENTRIES_TO_CLIPBOARD));
					contextMenu.add(new FMenuItem(CrosswordDocument.Command.IMPORT_ENTRIES_FROM_CLIPBOARD));
					contextMenu.add(new FMenuItem(CrosswordDocument.Command.CLEAR_ENTRIES));

					contextMenu.addSeparator();

					contextMenu.add(new FMenuItem(CrosswordDocument.Command.COPY_FIELD_NUMBERS_TO_CLIPBOARD));
					contextMenu.add(new FMenuItem(CrosswordDocument.Command.COPY_FIELD_IDS_TO_CLIPBOARD));
				}

				// Update commands for menu items
				document.updateCommands();

				// Display menu
				if (event == null)
					contextMenu.show(this, 0, 0);
				else
					contextMenu.show(event.getComponent(), event.getX(), event.getY());
			}
		}

		//--------------------------------------------------------------

		private void onMoveCaretUp()
		{
			incrementCaretRow(-1);
		}

		//--------------------------------------------------------------

		private void onMoveCaretDown()
		{
			incrementCaretRow(1);
		}

		//--------------------------------------------------------------

		private void onMoveCaretLeft()
		{
			incrementCaretColumn(-1);
		}

		//--------------------------------------------------------------

		private void onMoveCaretRight()
		{
			incrementCaretColumn(1);
		}

		//--------------------------------------------------------------

		private void onMoveCaretToStart()
		{
			gridPane.incrementCaretPosition(-2);
		}

		//--------------------------------------------------------------

		private void onMoveCaretToEnd()
		{
			gridPane.incrementCaretPosition(2);
		}

		//--------------------------------------------------------------

		private void onSelectAcrossClue()
		{
			updateSelectedClue(gridPane.setSelection(Direction.ACROSS));
		}

		//--------------------------------------------------------------

		private void onSelectDownClue()
		{
			updateSelectedClue(gridPane.setSelection(Direction.DOWN));
		}

		//--------------------------------------------------------------

		private void onSelectPreviousClue()
		{
			List<ClueElementMap.Entry> entries = new ArrayList<>();
			for (Direction direction : cluePanes.keySet())
				entries.addAll(cluePanes.get(direction).clueElementMap.entries);
			if (!entries.isEmpty())
			{
				int index = entries.size() - 1;
				if (selectedClueId != null)
				{
					for (index = 0; index < entries.size(); index++)
					{
						if (entries.get(index).clueId.equals(selectedClueId))
						{
							if (--index < 0)
								index = entries.size() - 1;
							break;
						}
					}
				}
				setSelection(entries.get(index).clueId);
			}
		}

		//--------------------------------------------------------------

		private void onSelectNextClue()
		{
			List<ClueElementMap.Entry> entries = new ArrayList<>();
			for (Direction direction : cluePanes.keySet())
				entries.addAll(cluePanes.get(direction).clueElementMap.entries);
			if (!entries.isEmpty())
			{
				int index = 0;
				if (selectedClueId != null)
				{
					for (index = 0; index < entries.size(); index++)
					{
						if (entries.get(index).clueId.equals(selectedClueId))
						{
							if (++index >= entries.size())
								index = 0;
							break;
						}
					}
				}
				setSelection(entries.get(index).clueId);
			}
		}

		//--------------------------------------------------------------

		private void onShowContextMenu()
		{
			showContextMenu(null);
		}

		//--------------------------------------------------------------

	}

	//==================================================================

}

//----------------------------------------------------------------------
