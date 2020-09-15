/*====================================================================*\

CrosswordView.java

Crossword view class.

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
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

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

import uk.blankaspect.common.exception.UnexpectedRuntimeException;

import uk.blankaspect.common.misc.IStringKeyed;

import uk.blankaspect.common.string.StringUtils;

import uk.blankaspect.common.swing.action.KeyAction;

import uk.blankaspect.common.swing.colour.Colours;

import uk.blankaspect.common.swing.font.FontUtils;

import uk.blankaspect.common.swing.menu.FMenuItem;

import uk.blankaspect.common.swing.misc.GuiUtils;

//----------------------------------------------------------------------


// CROSSWORD VIEW CLASS


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
//  Enumerated types
////////////////////////////////////////////////////////////////////////


	// COLOURS


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
			new Color(248, 236, 192)
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
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private Colour(String key,
					   String text,
					   Color  defaultColour)
		{
			this.key = key;
			this.text = text;
			this.defaultColour = defaultColour;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Class methods
	////////////////////////////////////////////////////////////////////

		public static Colour forKey(String key)
		{
			for (Colour value : values())
			{
				if (value.key.equals(key))
					return value;
			}
			return null;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : IStringKeyed interface
	////////////////////////////////////////////////////////////////////

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

		public Color getDefaultColour()
		{
			return defaultColour;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	String	key;
		private	String	text;
		private	Color	defaultColour;

	}

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Member classes : non-inner classes
////////////////////////////////////////////////////////////////////////


	// CLUE ELEMENT MAP CLASS


	private static class ClueElementMap
	{

	////////////////////////////////////////////////////////////////////
	//  Member classes : non-inner classes
	////////////////////////////////////////////////////////////////////


		// CLUE ELEMENT MAP ENTRY CLASS


		private static class Entry
		{

		////////////////////////////////////////////////////////////////
		//  Constructors
		////////////////////////////////////////////////////////////////

			private Entry(Clue.Id clueId,
						  Element element,
						  boolean reference,
						  boolean hasText)
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
				return (!(reference || hasText));
			}

			//----------------------------------------------------------

		////////////////////////////////////////////////////////////////
		//  Instance variables
		////////////////////////////////////////////////////////////////

			private	Clue.Id	clueId;
			private	Element	element;
			private	boolean	reference;
			private	boolean	hasText;

		}

		//==============================================================

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

		private void add(Clue.Id clueId,
						 Element element,
						 boolean reference,
						 boolean hasText)
		{
			entries.add(new Entry(clueId, element, reference, hasText));
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		private Entry getEntry(Clue.Id clueId)
		{
			for (Entry entry : entries)
			{
				if (entry.clueId.equals(clueId))
					return entry;
			}
			return null;
		}

		//--------------------------------------------------------------

		private Clue.Id getClueId(Element element)
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
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	List<Entry>	entries;

	}

	//==================================================================


	// HORIZONTAL LINE CLASS


	private static class HorizontalLine
		extends JComponent
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private HorizontalLine()
		{
			// Set component attributes
			setPreferredSize(new Dimension(1, 1));
			setOpaque(true);
			setFocusable(false);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		protected void paintComponent(Graphics gr)
		{
			// Draw line
			gr.setColor(getColour(Colour.TITLE_SEPARATOR));
			gr.drawLine(0, 0, getWidth() - 1, 0);
		}

		//--------------------------------------------------------------

	}

	//==================================================================


	// CLUE PANEL CLASS


	private static class CluePanel
		extends JTextPane
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		private static final	int	MAX_HEIGHT	= 1 << 16;  // 65536

		private static final	String	SEPARATOR			= "  ";
		private static final	String	SPAN_PREFIX			= "span.";
		private static final	String	PARAGRAPH_PREFIX	= "paragraph.";

		private static final	String	BOLD_KEY					= SPAN_PREFIX +
																		StyledText.StyleAttr.BOLD.getKey();
		private static final	String	COLOURS_KEY					= "colours";
		private static final	String	EMPTY_COLOURS_KEY			= "emptyColours";
		private static final	String	SELECTED_COLOURS_KEY		= "selectedColours";
		private static final	String	SELECTED_EMPTY_COLOURS_KEY	= "selectedEmptyColours";

		private static final	String	NO_CLUE_STR	= "(no clue)";

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CluePanel(CrosswordDocument document)
		{
			this(document, null, 0);
		}

		//--------------------------------------------------------------

		private CluePanel(CrosswordDocument document,
						  List<Clue>        clues,
						  int               width)
		{
			// Call superclass constructor
			super(new DefaultStyledDocument(new StyleContext()));

			// Initialise instance variables
			clueElementMap = new ClueElementMap();

			// Set font
			AppFont.CLUE.apply(this);

			// Set component attributes
			setBackground(Colours.BACKGROUND);
			setForeground(Colours.FOREGROUND);
			setBorder(null);
			setEditable(false);
			setFocusable(false);
			((DefaultCaret)getCaret()).setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
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

		private void setClues(CrosswordDocument document,
							  List<Clue>        clues)
		{
			// Calculate line indents
			FontMetrics fontMetrics = getFontMetrics(getFont().deriveFont(Font.BOLD));
			int[] widths = new int[clues.size()];
			int indent1 = 0;
			for (int i = 0; i < clues.size(); i++)
			{
				widths[i] = fontMetrics.stringWidth(Integer.toString(clues.get(i).
																				getFieldId().number));
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
			StyleConstants.setBackground(style, getColour(Colour.BACKGROUND));
			StyleConstants.setForeground(style, getColour(Colour.CLUE_TEXT));

			style = styledDoc.addStyle(EMPTY_COLOURS_KEY, null);
			StyleConstants.setBackground(style, getColour(Colour.BACKGROUND));
			StyleConstants.setForeground(style, getColour(Colour.EMPTY_CLUE_TEXT));

			style = styledDoc.addStyle(SELECTED_COLOURS_KEY, null);
			StyleConstants.setBackground(style, getColour(Colour.SELECTED_CLUE_BACKGROUND));
			StyleConstants.setForeground(style, getColour(Colour.CLUE_TEXT));

			style = styledDoc.addStyle(SELECTED_EMPTY_COLOURS_KEY, null);
			StyleConstants.setBackground(style, getColour(Colour.SELECTED_EMPTY_CLUE_BACKGROUND));
			StyleConstants.setForeground(style, getColour(Colour.EMPTY_CLUE_TEXT));

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
					clueElementMap.add(clueId, styledDoc.getParagraphElement(startOffset),
									   clue.isReference(), clue.hasText());

					// Append clue IDs
					Direction direction = clueId.fieldId.direction;
					styledDoc.insertString(styledDoc.getLength(),
										   document.getClueIdString(direction, clue) + SEPARATOR,
										   styledDoc.getStyle(BOLD_KEY));

					// If reference, append reference ...
					if (clue.isReference())
						styledDoc.insertString(styledDoc.getLength(),
											   document.getClueReferenceString(direction, clue),
											   defStyle);

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
					styledDoc.setCharacterAttributes(startOffset, offset - startOffset,
													 styledDoc.getStyle(key), false);

					// Set paragraph attributes
					key = PARAGRAPH_PREFIX + Integer.toString(widths[i]);
					styledDoc.setParagraphAttributes(startOffset, offset - startOffset,
													 styledDoc.getStyle(key), true);
				}
				catch (BadLocationException e)
				{
					throw new UnexpectedRuntimeException();
				}
			}
		}

		//--------------------------------------------------------------

		private void setElementStyle(Element element,
									 String  styleKey)
		{
			StyledDocument styledDoc = getStyledDocument();
			Style style = styledDoc.getStyle(styleKey);
			int startOffset = element.getStartOffset();
			styledDoc.setCharacterAttributes(startOffset, element.getEndOffset() - startOffset, style,
											 false);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	ClueElementMap	clueElementMap;

	}

	//==================================================================


	// TEXT SECTION PANEL CLASS


	private static class TextSectionPanel
		extends JTextPane
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		private static final	int	MAX_HEIGHT	= 1 << 16;  // 65536

		private static final	String	SPAN_PREFIX		= "span.";
		private static final	String	PARAGRAPH_KEY	= "paragraph";

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private TextSectionPanel(List<String> paragraphs,
								 int          width,
								 boolean      spaceAbove)
		{
			// Call superclass constructor
			super(new DefaultStyledDocument(new StyleContext()));

			// Set font
			AppFont.MAIN.apply(this);

			// Set component attributes
			setBackground(getColour(Colour.BACKGROUND));
			setForeground(getColour(Colour.TEXT));
			setBorder(null);
			setEditable(false);
			setFocusable(false);
			((DefaultCaret)getCaret()).setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
			if (width > 0)
				setSize(new Dimension(width, MAX_HEIGHT));
			setTransferHandler(null);

			// Add paragraph style to styled document
			StyledDocument styledDoc = getStyledDocument();
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
							styledDoc.insertString(styledDoc.getLength(),
												   spanText.substring(0, index) + "\n", style);

							// Set paragraph attributes
							Style paraStyle = (spaceAbove && (numLineBreaks == 0))
																	? styledDoc.getStyle(PARAGRAPH_KEY)
																	: defStyle;
							styledDoc.setParagraphAttributes(startOffset,
															 styledDoc.getLength() - startOffset,
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
					Style paraStyle = (!spaceAbove || (numLineBreaks == 0))
																	? styledDoc.getStyle(PARAGRAPH_KEY)
																	: defStyle;
					styledDoc.setParagraphAttributes(startOffset, styledDoc.getLength() - startOffset,
													 paraStyle, true);
				}
				catch (StyledText.ParseException e)
				{
					throw new UnexpectedRuntimeException();
				}
				catch (BadLocationException e)
				{
					throw new UnexpectedRuntimeException();
				}
			}

			// Set visibility of panel
			setVisible(!paragraphs.isEmpty());
		}

		//--------------------------------------------------------------

	}

	//==================================================================


	// CROSSWORD PANEL CLASS


	private static class CrosswordPanel
		extends JPanel
		implements ActionListener, KeyListener, MouseListener
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		private static final	int	CLUES_VERTICAL_MARGIN	= 2;
		private static final	int	HORIZONTAL_GAP			= 16;

		private static final	float	FONT_FACTOR	= 1.125f;

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
			new KeyAction.KeyCommandPair
			(
				KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0),
				Command.MOVE_CARET_UP
			),
			new KeyAction.KeyCommandPair
			(
				KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0),
				Command.MOVE_CARET_DOWN
			),
			new KeyAction.KeyCommandPair
			(
				KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0),
				Command.MOVE_CARET_LEFT
			),
			new KeyAction.KeyCommandPair
			(
				KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0),
				Command.MOVE_CARET_RIGHT
			),
			new KeyAction.KeyCommandPair
			(
				KeyStroke.getKeyStroke(KeyEvent.VK_HOME, 0),
				Command.MOVE_CARET_TO_START
			),
			new KeyAction.KeyCommandPair
			(
				KeyStroke.getKeyStroke(KeyEvent.VK_END, 0),
				Command.MOVE_CARET_TO_END
			),
			new KeyAction.KeyCommandPair
			(
				KeyStroke.getKeyStroke(KeyEvent.VK_UP, KeyEvent.CTRL_DOWN_MASK),
				Command.SELECT_DOWN_CLUE
			),
			new KeyAction.KeyCommandPair
			(
				KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, KeyEvent.CTRL_DOWN_MASK),
				Command.SELECT_DOWN_CLUE
			),
			new KeyAction.KeyCommandPair
			(
				KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, KeyEvent.CTRL_DOWN_MASK),
				Command.SELECT_ACROSS_CLUE
			),
			new KeyAction.KeyCommandPair
			(
				KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, KeyEvent.CTRL_DOWN_MASK),
				Command.SELECT_ACROSS_CLUE
			),
			new KeyAction.KeyCommandPair
			(
				KeyStroke.getKeyStroke(KeyEvent.VK_TAB, KeyEvent.SHIFT_DOWN_MASK),
				Command.SELECT_PREVIOUS_CLUE
			),
			new KeyAction.KeyCommandPair
			(
				KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0),
				Command.SELECT_NEXT_CLUE
			),
			new KeyAction.KeyCommandPair
			(
				KeyStroke.getKeyStroke(KeyEvent.VK_CONTEXT_MENU, 0),
				Command.SHOW_CONTEXT_MENU
			)
		};

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CrosswordPanel(CrosswordDocument document)
		{
			// Initialise instance variables
			this.document = document;

			// Set border
			GuiUtils.setPaddedLineBorder(this, 8);

			// Set component attributes
			setBackground(Colours.BACKGROUND);


			//----  Title label

			titleLabel = createTitleLabel();
			horizontalLine = new HorizontalLine();
			horizontalLine.setVisible(titleLabel.isVisible());


			//----  Upper panel

			GridBagLayout gridBag = new GridBagLayout();
			GridBagConstraints gbc = new GridBagConstraints();

			JPanel upperPanel = new JPanel(gridBag);
			upperPanel.setBackground(Colours.BACKGROUND);

			int gridX = 0;

			// Panel: grid
			gridPanel = document.getGrid().getSeparator().createGridPanel(document);
			gridPanel.addKeyListener(this);
			gridPanel.addMouseListener(this);

			gbc.gridx = gridX++;
			gbc.gridy = 0;
			gbc.gridwidth = 1;
			gbc.gridheight = 1;
			gbc.weightx = 0.0;
			gbc.weighty = 0.0;
			gbc.anchor = GridBagConstraints.FIRST_LINE_START;
			gbc.fill = GridBagConstraints.NONE;
			gbc.insets = new Insets(0, 0, 0, 0);
			gridBag.setConstraints(gridPanel, gbc);
			upperPanel.add(gridPanel);

			// Panel: selected clue
			selectedCluePanel = new CluePanel(document);
			int width = FontUtils.getCharWidth('0', selectedCluePanel.getFontMetrics(selectedCluePanel.getFont()));
			width *= AppConfig.INSTANCE.getSelectedClueNumColumns();
			selectedCluePanel.setPreferredSize(new Dimension(width, gridPanel.getPreferredSize().height));
			selectedCluePanel.addMouseListener(this);

			gbc.gridx = gridX++;
			gbc.gridy = 0;
			gbc.gridwidth = 1;
			gbc.gridheight = 1;
			gbc.weightx = 0.0;
			gbc.weighty = 0.0;
			gbc.anchor = GridBagConstraints.FIRST_LINE_END;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.insets = new Insets(0, HORIZONTAL_GAP, 0, 0);
			gridBag.setConstraints(selectedCluePanel, gbc);
			upperPanel.add(selectedCluePanel);

			contentWidth = upperPanel.getPreferredSize().width;


			//----  Lower panel

			JPanel lowerPanel = new JPanel(gridBag);
			lowerPanel.setBackground(Colours.BACKGROUND);

			// Add clue panels
			int numCluePanels = Direction.DEFINED_DIRECTIONS.size();
			cluePanelWidth = (contentWidth - (numCluePanels - 1) * HORIZONTAL_GAP) / numCluePanels;
			directionLabels = new EnumMap<>(Direction.class);
			cluePanels = new EnumMap<>(Direction.class);

			gridX = 0;
			for (Direction direction : Direction.DEFINED_DIRECTIONS)
			{
				int horizontalMargin = (gridX == 0) ? 0 : HORIZONTAL_GAP;

				// Label: direction
				JLabel directionLabel = new JLabel(direction.toString());
				directionLabels.put(direction, directionLabel);
				Font font = AppFont.MAIN.getFont();
				directionLabel.setFont(font.deriveFont(Font.BOLD, FONT_FACTOR * font.getSize2D()));
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
				lowerPanel.add(directionLabel);

				// Panel: clues
				CluePanel cluePanel = createCluePanel(direction);
				cluePanels.put(direction, cluePanel);

				gbc.gridx = gridX++;
				gbc.gridy = 1;
				gbc.gridwidth = 1;
				gbc.gridheight = 1;
				gbc.weightx = 0.0;
				gbc.weighty = 0.0;
				gbc.anchor = GridBagConstraints.FIRST_LINE_START;
				gbc.fill = GridBagConstraints.VERTICAL;
				gbc.insets = new Insets(CLUES_VERTICAL_MARGIN, horizontalMargin, 0, 0);
				gridBag.setConstraints(cluePanel, gbc);
				lowerPanel.add(cluePanel);
			}


			//----  Prologue and epilogue panels

			prologuePanel = createTextSectionPanel(document.getPrologue(), false);
			epiloguePanel = createTextSectionPanel(document.getEpilogue(), true);


			//----  This panel

			// Set layout manager
			setLayout(gridBag);

			// Add child panels
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
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.insets = new Insets(0, 0, 8, 0);
			gridBag.setConstraints(prologuePanel, gbc);
			add(prologuePanel);

			gbc.gridx = 0;
			gbc.gridy = gridY++;
			gbc.gridwidth = 1;
			gbc.gridheight = 1;
			gbc.weightx = 0.0;
			gbc.weighty = 0.0;
			gbc.anchor = GridBagConstraints.FIRST_LINE_START;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.insets = new Insets(0, 0, 0, 0);
			gridBag.setConstraints(upperPanel, gbc);
			add(upperPanel);

			gbc.gridx = 0;
			gbc.gridy = gridY++;
			gbc.gridwidth = 1;
			gbc.gridheight = 1;
			gbc.weightx = 0.0;
			gbc.weighty = 0.0;
			gbc.anchor = GridBagConstraints.FIRST_LINE_START;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.insets = new Insets(8, 0, 0, 0);
			gridBag.setConstraints(lowerPanel, gbc);
			add(lowerPanel);

			gbc.gridx = 0;
			gbc.gridy = gridY++;
			gbc.gridwidth = 1;
			gbc.gridheight = 1;
			gbc.weightx = 0.0;
			gbc.weighty = 0.0;
			gbc.anchor = GridBagConstraints.FIRST_LINE_START;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.insets = new Insets(8, 0, 0, 0);
			gridBag.setConstraints(epiloguePanel, gbc);
			add(epiloguePanel);

			Box.Filler filler = GuiUtils.createFiller();

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
			setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS,
								  Collections.singleton(FOCUS_FORWARD_KEY));
			setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS,
								  Collections.singleton(FOCUS_BACKWARD_KEY));

			// Add commands to action map
			KeyAction.create(this, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, this, KEY_COMMANDS);

			// Add listeners
			addMouseListener(this);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : ActionListener interface
	////////////////////////////////////////////////////////////////////

		public void actionPerformed(ActionEvent event)
		{
			String command = event.getActionCommand();

			if (command.equals(Command.MOVE_CARET_UP))
				onMoveCaretUp();

			else if (command.equals(Command.MOVE_CARET_DOWN))
				onMoveCaretDown();

			else if (command.equals(Command.MOVE_CARET_LEFT))
				onMoveCaretLeft();

			else if (command.equals(Command.MOVE_CARET_RIGHT))
				onMoveCaretRight();

			else if (command.equals(Command.MOVE_CARET_TO_START))
				onMoveCaretToStart();

			else if (command.equals(Command.MOVE_CARET_TO_END))
				onMoveCaretToEnd();

			else if (command.equals(Command.SELECT_ACROSS_CLUE))
				onSelectAcrossClue();

			else if (command.equals(Command.SELECT_DOWN_CLUE))
				onSelectDownClue();

			else if (command.equals(Command.SELECT_PREVIOUS_CLUE))
				onSelectPreviousClue();

			else if (command.equals(Command.SELECT_NEXT_CLUE))
				onSelectNextClue();

			else if (command.equals(Command.SHOW_CONTEXT_MENU))
				onShowContextMenu();
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : KeyListener interface
	////////////////////////////////////////////////////////////////////

		public void keyPressed(KeyEvent event)
		{
			switch (event.getKeyCode())
			{
				case KeyEvent.VK_DELETE:
					setEntryChar(Grid.Entries.UNDEFINED_VALUE, 0);
					break;

				case KeyEvent.VK_BACK_SPACE:
					setEntryChar(Grid.Entries.UNDEFINED_VALUE, -1);
					break;
			}
		}

		//--------------------------------------------------------------

		public void keyReleased(KeyEvent event)
		{
			// do nothing
		}

		//--------------------------------------------------------------

		public void keyTyped(KeyEvent event)
		{
			char ch = Character.toUpperCase(event.getKeyChar());
			if (AppConfig.INSTANCE.getGridEntryCharacters().indexOf(ch) >= 0)
				setEntryChar(ch, 1);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : MouseListener interface
	////////////////////////////////////////////////////////////////////

		public void mouseClicked(MouseEvent event)
		{
			if (SwingUtilities.isLeftMouseButton(event) && (event.getClickCount() > 1))
			{
				Component component = event.getComponent();

				// Text-section panels
				if ((component == titleLabel) || (component == prologuePanel) || (component == epiloguePanel))
					document.executeCommand(CrosswordDocument.Command.EDIT_TEXT_SECTIONS);

				// Clue panels
				else if (selectedClueId != null)
				{
					for (Direction direction : cluePanels.keySet())
					{
						if (component == cluePanels.get(direction))
						{
							document.executeCommand(CrosswordDocument.Command.EDIT_CLUE);
							break;
						}
					}
				}
			}
		}

		//--------------------------------------------------------------

		public void mouseEntered(MouseEvent event)
		{
			// do nothing
		}

		//--------------------------------------------------------------

		public void mouseExited(MouseEvent event)
		{
			// do nothing
		}

		//--------------------------------------------------------------

		public void mousePressed(MouseEvent event)
		{
			if (SwingUtilities.isLeftMouseButton(event))
			{
				Component component = event.getComponent();

				// Grid panel
				if (component == gridPanel)
				{
					Grid grid = document.getGrid();
					int row = event.getY() / gridPanel.getCellSize();
					int column = event.getX() / gridPanel.getCellSize();
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
							Grid.Field selectedField = gridPanel.getSelectedField();
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
								gridPanel.setSelection(new Clue.FieldList(field), null);
								setSelectedClue(new Clue.Id(field.getId(), 0));
							}
							else
							{
								gridPanel.setSelection(new Clue.FieldList(grid.getFields(clue), field),
													   new Grid.IndexPair(row, column));
								setSelectedClue(clue.getId());
							}
						}
					}
				}

				// Clue panels
				else
				{
					for (Direction direction : cluePanels.keySet())
					{
						CluePanel cluePanel = cluePanels.get(direction);
						if (component == cluePanel)
						{
							int pos = cluePanel.viewToModel2D(event.getPoint());
							Element element = cluePanel.getStyledDocument().getParagraphElement(pos);
							setSelection(cluePanel.clueElementMap.getClueId(element));
							break;
						}
					}
				}
			}

			// Context menu
			showContextMenu(event);
		}

		//--------------------------------------------------------------

		public void mouseReleased(MouseEvent event)
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
			Container parent = prologuePanel.getParent();
			for (int i = 0; i < parent.getComponentCount(); i++)
			{
				if (parent.getComponent(i) == prologuePanel)
				{
					GridBagConstraints constraints = ((GridBagLayout)parent.getLayout()).getConstraints(prologuePanel);
					parent.remove(i);
					prologuePanel = createTextSectionPanel(document.getPrologue(), false);
					parent.add(prologuePanel, constraints, i);
					break;
				}
			}
		}

		//--------------------------------------------------------------

		public void updateEpilogue()
		{
			Container parent = epiloguePanel.getParent();
			for (int i = 0; i < parent.getComponentCount(); i++)
			{
				if (parent.getComponent(i) == epiloguePanel)
				{
					GridBagConstraints constraints = ((GridBagLayout)parent.getLayout()).getConstraints(epiloguePanel);
					parent.remove(i);
					epiloguePanel = createTextSectionPanel(document.getEpilogue(), true);
					parent.add(epiloguePanel, constraints, i);
					break;
				}
			}
		}

		//--------------------------------------------------------------

		private CluePanel createCluePanel(Direction direction)
		{
			// Create a list of clues that contains undefined clues for fields that do not have clues
			List<Clue> clues = new ArrayList<>(document.getClues(direction));
			List<Clue> undefinedClues = new ArrayList<>();
			List<Grid.Field> fields = document.getGrid().getFields(direction);
			for (Grid.Field field : fields)
			{
				Grid.Field.Id fieldId = field.getId();
				Clue clue = new Clue(fieldId);
				if (clues.stream().noneMatch(clue0 -> Clue.FieldIdComparator.INSTANCE.compare(clue, clue0) == 0))
					undefinedClues.add(clue);
			}
			clues.addAll(undefinedClues);
			clues.sort(Clue.ID_COMPARATOR);

			// Create panel
			CluePanel panel = new CluePanel(document, clues, cluePanelWidth);
			int height = panel.getPreferredSize().height;
			panel = new CluePanel(document, clues, 0);
			panel.setPreferredSize(new Dimension(cluePanelWidth, height));
			panel.setVisible(document.isShowClues());
			panel.addMouseListener(this);
			return panel;
		}

		//--------------------------------------------------------------

		private JLabel createTitleLabel()
		{
			String title = document.getTitle();
			boolean isTitle = !StringUtils.isNullOrEmpty(title);
			JLabel label = new JLabel(title);
			Font font = AppFont.MAIN.getFont();
			label.setFont(font.deriveFont(Font.BOLD, FONT_FACTOR * font.getSize2D()));
			label.setForeground(getColour(Colour.TEXT));
			label.setVisible(isTitle);
			if (isTitle)
				label.addMouseListener(this);
			return label;
		}

		//--------------------------------------------------------------

		private TextSectionPanel createTextSectionPanel(List<String> paragraphs,
														boolean      spaceAbove)
		{
			TextSectionPanel panel = new TextSectionPanel(paragraphs, contentWidth, spaceAbove);
			int height = panel.getPreferredSize().height;
			panel = new TextSectionPanel(paragraphs, 0, spaceAbove);
			panel.setPreferredSize(new Dimension(contentWidth, height));
			panel.addMouseListener(this);
			return panel;
		}

		//--------------------------------------------------------------

		private void updateGrid()
		{
			gridPanel.setGrid(document.getGrid());
		}

		//--------------------------------------------------------------

		private void updateDirectionLabel(Direction direction)
		{
			JLabel directionLabel = directionLabels.get(direction);
			if (directionLabel != null)
				directionLabel.setVisible(document.isShowClues());
		}

		//--------------------------------------------------------------

		private void updateCluePanel(Direction direction)
		{
			CluePanel cluePanel = cluePanels.get(direction);
			if (cluePanel != null)
			{
				Container parent = cluePanel.getParent();
				for (int i = 0; i < parent.getComponentCount(); i++)
				{
					if (parent.getComponent(i) == cluePanel)
					{
						GridBagConstraints constraints = ((GridBagLayout)parent.getLayout()).getConstraints(cluePanel);
						parent.remove(i);
						cluePanel = createCluePanel(direction);
						parent.add(cluePanel, constraints, i);
						cluePanels.put(direction, cluePanel);
						break;
					}
				}
			}
		}

		//--------------------------------------------------------------

		private void updateSelectedClue(Clue.FieldList selectedFields)
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

		private List<Clue.Id> getClueIds(Clue.Id clueId)
		{
			List<Clue.Id> clueIds = document.getClueIds(clueId);
			if (clueIds.isEmpty())
				clueIds.add(clueId);
			return clueIds;
		}

		//--------------------------------------------------------------

		private void setSelectedClue(Clue.Id clueId)
		{
			if ((clueId == null) ? (selectedClueId != null) : !clueId.equals(selectedClueId))
			{
				// Deselect current clue
				if (selectedClueId != null)
				{
					for (Clue.Id id : getClueIds(selectedClueId))
					{
						for (Direction direction : cluePanels.keySet())
						{
							CluePanel cluePanel = cluePanels.get(direction);
							ClueElementMap.Entry entry = cluePanel.clueElementMap.getEntry(id);
							if (entry != null)
							{
								cluePanel.setElementStyle(entry.element,
														  entry.isEmpty() ? CluePanel.EMPTY_COLOURS_KEY
																		  : CluePanel.COLOURS_KEY);
								break;
							}
						}
					}
					selectedCluePanel.setText(null);
					selectedClueId = null;
				}

				// Select new clue
				if (clueId != null)
				{
					for (Clue.Id id : getClueIds(clueId))
					{
						for (Direction direction : cluePanels.keySet())
						{
							CluePanel cluePanel = cluePanels.get(direction);
							ClueElementMap.Entry entry = cluePanel.clueElementMap.getEntry(id);
							if (entry != null)
							{
								cluePanel.setElementStyle(entry.element,
														  entry.isEmpty()
																	? CluePanel.SELECTED_EMPTY_COLOURS_KEY
																	: CluePanel.SELECTED_COLOURS_KEY);
								break;
							}
						}
					}
					Clue clue = document.findPrimaryClue(clueId);
					if (clue != null)
						selectedCluePanel.setClues(document, Collections.singletonList(clue));
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

		private void setSelection(Clue.Id clueId)
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
			gridPanel.requestFocusInWindow();
			gridPanel.setSelection(fields);
			setSelectedClue(clueId);
		}

		//--------------------------------------------------------------

		private void setEntryChar(char value,
								  int  increment)
		{
			if (gridPanel.setEntryChar(value, increment))
				updateSelectedClue(gridPanel.incrementCaretPosition(increment));
		}

		//--------------------------------------------------------------

		private void incrementCaretColumn(int increment)
		{
			updateSelectedClue(gridPanel.incrementCaretColumn(increment));
		}

		//--------------------------------------------------------------

		private void incrementCaretRow(int increment)
		{
			updateSelectedClue(gridPanel.incrementCaretRow(increment));
		}

		//--------------------------------------------------------------

		private void showContextMenu(MouseEvent event)
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
			gridPanel.incrementCaretPosition(-2);
		}

		//--------------------------------------------------------------

		private void onMoveCaretToEnd()
		{
			gridPanel.incrementCaretPosition(2);
		}

		//--------------------------------------------------------------

		private void onSelectAcrossClue()
		{
			updateSelectedClue(gridPanel.setSelection(Direction.ACROSS));
		}

		//--------------------------------------------------------------

		private void onSelectDownClue()
		{
			updateSelectedClue(gridPanel.setSelection(Direction.DOWN));
		}

		//--------------------------------------------------------------

		private void onSelectPreviousClue()
		{
			List<ClueElementMap.Entry> entries = new ArrayList<>();
			for (Direction direction : cluePanels.keySet())
				entries.addAll(cluePanels.get(direction).clueElementMap.entries);
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
			for (Direction direction : cluePanels.keySet())
				entries.addAll(cluePanels.get(direction).clueElementMap.entries);
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

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	CrosswordDocument			document;
		private	int							contentWidth;
		private	int							cluePanelWidth;
		private	Clue.Id						selectedClueId;
		private	JLabel						titleLabel;
		private	HorizontalLine				horizontalLine;
		private	GridPanel					gridPanel;
		private	CluePanel					selectedCluePanel;
		private	Map<Direction, JLabel>		directionLabels;
		private	Map<Direction, CluePanel>	cluePanels;
		private	TextSectionPanel			prologuePanel;
		private	TextSectionPanel			epiloguePanel;
		private	JPopupMenu					contextMenu;

	}

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	public CrosswordView(CrosswordDocument document)
	{
		// Call superclass constructor
		super(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

		// Set view and size of viewport
		crosswordPanel = new CrosswordPanel(document);
		setViewportView(crosswordPanel);
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

		// Set component attributes
		setBorder(null);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	public static Color getColour(Colour key)
	{
		return AppConfig.INSTANCE.getViewColour(key);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : overriding methods
////////////////////////////////////////////////////////////////////////

	@Override
	public boolean requestFocusInWindow()
	{
		return crosswordPanel.gridPanel.requestFocusInWindow();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	public Clue.Id getSelectedClueId()
	{
		return crosswordPanel.selectedClueId;
	}

	//------------------------------------------------------------------

	public void redraw()
	{
		crosswordPanel.repaint();
	}

	//------------------------------------------------------------------

	public void redrawGrid()
	{
		crosswordPanel.gridPanel.repaint();
	}

	//------------------------------------------------------------------

	public void setPreferredViewportSize(Dimension preferredSize)
	{
		if (preferredSize == null)
			preferredSize = crosswordPanel.getPreferredSize();
		preferredSize = new Dimension(Math.max(MIN_WIDTH, preferredSize.width),
									  Math.max(MIN_HEIGHT, preferredSize.height));
		if (!preferredSize.equals(viewport.getExtentSize()))
		{
			App app = App.INSTANCE;
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

	public void drawCaret(boolean draw)
	{
		crosswordPanel.gridPanel.drawCaret(draw);
	}

	//------------------------------------------------------------------

	public void updateGrid()
	{
		crosswordPanel.setSelection(null);
		crosswordPanel.updateGrid();
		for (Direction direction : Direction.DEFINED_DIRECTIONS)
			crosswordPanel.updateCluePanel(direction);
		revalidate();
		crosswordPanel.repaint();
	}

	//------------------------------------------------------------------

	public void updateClues(Direction direction)
	{
		crosswordPanel.updateDirectionLabel(direction);
		crosswordPanel.updateCluePanel(direction);
		crosswordPanel.updateSelection();
		revalidate();
		crosswordPanel.repaint();
	}

	//------------------------------------------------------------------

	public void updateTextSections(EnumSet<CrosswordDocument.TextSection> textSections)
	{
		for (CrosswordDocument.TextSection textSection : textSections)
		{
			switch (textSection)
			{
				case TITLE:
					crosswordPanel.updateTitle();
					break;

				case PROLOGUE:
					crosswordPanel.updatePrologue();
					break;

				case EPILOGUE:
					crosswordPanel.updateEpilogue();
					break;
			}
		}
		revalidate();
		crosswordPanel.repaint();
	}

	//------------------------------------------------------------------

	public void setGridCaretPosition(int       row,
									 int       column,
									 Direction direction)
	{
		crosswordPanel.gridPanel.setCaretPosition(row, column, direction);
	}

	//------------------------------------------------------------------

	private void setViewportSize(Dimension size)
	{
		viewport.setPreferredSize(size);
		viewport.setExtentSize(size);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private	CrosswordPanel	crosswordPanel;

}

//----------------------------------------------------------------------
