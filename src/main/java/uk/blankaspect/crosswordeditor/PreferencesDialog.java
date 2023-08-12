/*====================================================================*\

PreferencesDialog.java

Preferences dialog class.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.crosswordeditor;

//----------------------------------------------------------------------


// IMPORTS


import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.awt.image.BufferedImage;

import java.io.File;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import uk.blankaspect.common.exception.AppException;
import uk.blankaspect.common.exception.FileException;

import uk.blankaspect.common.misc.FilenameSuffixFilter;
import uk.blankaspect.common.misc.MaxValueMap;

import uk.blankaspect.common.string.StringUtils;

import uk.blankaspect.ui.swing.action.KeyAction;

import uk.blankaspect.ui.swing.border.TitledBorder;

import uk.blankaspect.ui.swing.button.FButton;

import uk.blankaspect.ui.swing.combobox.BooleanComboBox;
import uk.blankaspect.ui.swing.combobox.FComboBox;

import uk.blankaspect.ui.swing.container.DimensionsSpinnerPanel;
import uk.blankaspect.ui.swing.container.PathnamePanel;

import uk.blankaspect.ui.swing.font.FontEx;
import uk.blankaspect.ui.swing.font.FontStyle;
import uk.blankaspect.ui.swing.font.FontUtils;

import uk.blankaspect.ui.swing.icon.ColourSampleIcon;

import uk.blankaspect.ui.swing.label.FixedWidthLabel;
import uk.blankaspect.ui.swing.label.FLabel;

import uk.blankaspect.ui.swing.list.SelectionList;

import uk.blankaspect.ui.swing.misc.GuiUtils;

import uk.blankaspect.ui.swing.spinner.FDoubleSpinner;
import uk.blankaspect.ui.swing.spinner.FIntegerSpinner;
import uk.blankaspect.ui.swing.spinner.IntegerSpinner;

import uk.blankaspect.ui.swing.tabbedpane.FTabbedPane;

import uk.blankaspect.ui.swing.text.TextRendering;

import uk.blankaspect.ui.swing.textfield.ConstrainedTextField;
import uk.blankaspect.ui.swing.textfield.FTextField;
import uk.blankaspect.ui.swing.textfield.InformationField;
import uk.blankaspect.ui.swing.textfield.IntegerValueField;

//----------------------------------------------------------------------


// PREFERENCES DIALOG CLASS


class PreferencesDialog
	extends JDialog
	implements ActionListener, ChangeListener, ListSelectionListener
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	private static final	String	KEY	= PreferencesDialog.class.getCanonicalName();

	// Main panel
	private static final	String	TITLE_STR				= "Preferences";
	private static final	String	SAVE_CONFIGURATION_STR	= "Save configuration";
	private static final	String	SAVE_CONFIG_FILE_STR	= "Save configuration file";
	private static final	String	WRITE_CONFIG_FILE_STR	= "Write configuration file";

	// General panel
	private static final	int		MAX_EDIT_LIST_LENGTH_FIELD_LENGTH	= 4;

	private static final	String	SHOW_UNIX_PATHNAMES_STR			= "Display UNIX-style pathnames";
	private static final	String	SELECT_TEXT_ON_FOCUS_GAINED_STR	= "Select text when focus is gained";
	private static final	String	SAVE_MAIN_WINDOW_LOCATION_STR	= "Save location of main window";
	private static final	String	MAX_EDIT_HISTORY_SIZE_STR		= "Maximum size of edit history";
	private static final	String	CLEAR_EDIT_HISTORY_ON_SAVE_STR	= "Clear edit history on save";

	// Appearance panel
	private static final	String	LOOK_AND_FEEL_STR		= "Look-and-feel";
	private static final	String	NO_LOOK_AND_FEELS_STR	= "<no look-and-feels>";
	private static final	String	TEXT_ANTIALIASING_STR	= "Text antialiasing";
	private static final	String	STATUS_TEXT_COLOUR_STR	= "Status text colour";

	// View panel
	private static final	int		SELECTED_CLUE_NUM_COLUMNS_FIELD_LENGTH	= 3;
	private static final	int		VIEW_COLOURS_LIST_NUM_ROWS				= 6;

	private static final	String	SELECTED_CLUE_NUM_COLUMNS_STR	= "Width of selected clue";
	private static final	String	COLUMNS_STR						= "columns";
	private static final	String	COLOURS_STR						= "Colours";
	private static final	String	VIEW_COLOUR_STR					= "View colour : ";

	// Grid panel
	private static final	int		GRID_IMAGE_VIEWPORT_FIELD_LENGTH	= 4;
	private static final	int		GRID_CELL_SIZE_FIELD_LENGTH			= 2;
	private static final	int		BAR_WIDTH_FIELD_LENGTH				= 2;

	private static final	String	GRID_ENTRY_CHARS_STR			= "Grid-entry characters";
	private static final	String	NAVIGATE_OVER_SEPARATORS_STR	= "Navigate over separators";
	private static final	String	IMAGE_VIEWPORT_SIZE_STR			= "Capture-image viewport size";
	private static final	String	WIDTH_STR						= "width";
	private static final	String	HEIGHT_STR						= "height";
	private static final	String	CELL_SIZE_STR					= "Cell size";
	private static final	String	BAR_WIDTH_STR					= "Bar width";

	// Clues panel
	private static final	int		DIRECTION_KEYWORDS_FIELD_NUM_COLUMNS		= 48;
	private static final	int		CLUE_REFERENCE_KEYWORD_FIELD_NUM_COLUMNS	= 12;

	private static final	Insets	EDIT_BUTTON_MARGINS		= new Insets(2, 4, 2, 4);

	private static final	String	DIRECTION_KEYWORDS_STR			= "Direction keywords";
	private static final	String	EDIT_DIRECTION_KEYWORDS_STR		= "Edit direction keywords : ";
	private static final	String	KEYWORD_STR						= "keyword";
	private static final	String	CLUE_REFERENCE_KEYWORD_STR		= "Clue-reference keyword";
	private static final	String	IMPLICIT_FIELD_DIRECTION_STR	= "Implicit direction of secondary fields";
	private static final	String	ALLOW_MULTIPLE_FIELD_USE_STR	= "Allow multiple clues for a field";

	// Text sections panel
	private static final	int		LINE_BREAK_FIELD_NUM_COLUMNS	= 4;

	private static final	String	LINE_BREAK_STR	= "Line break";

	// HTML panel
	private static final	int		HTML_VIEWER_COMMAND_FIELD_NUM_COLUMNS			= 40;
	private static final	int		HTML_FONT_NAMES_FIELD_NUM_COLUMNS				= 56;
	private static final	int		HTML_FONT_SIZE_FIELD_LENGTH						= 3;
	private static final	int		HTML_FIELD_NUM_FONT_SIZE_FACTOR_FIELD_LENGTH	= 5;
	private static final	int		HTML_FIELD_NUM_OFFSET_FIELD_LENGTH				= 2;
	private static final	int		HTML_CELL_OFFSET_FIELD_LENGTH					= 2;
	private static final	int		HTML_CELL_SIZE_FIELD_LENGTH						= 2;
	private static final	int		BLOCK_IMAGE_NUM_LINES_FIELD_LENGTH				= 2;
	private static final	int		BLOCK_IMAGE_LINE_WIDTH_FIELD_LENGTH				= 4;

	private static final	double	DELTA_FIELD_NUM_FONT_SIZE_FACTOR	= 0.001;
	private static final	double	DELTA_BLOCK_IMAGE_LINE_WIDTH		= 0.1;

	private static final	String	VIEWER_COMMAND_STR				= "Viewer command";
	private static final	String	FONT_NAMES_STR					= "Font names";
	private static final	String	EDIT_STR						= "Edit";
	private static final	String	EDIT_FONT_NAMES_STR				= "Edit font names";
	private static final	String	FONT_NAME_STR					= "font name";
	private static final	String	FONT_SIZE_STR					= "Font size";
	private static final	String	FIELD_NUM_FONT_SIZE_FACTOR_STR	= "Field-number font-size factor";
	private static final	String	TOP_STR							= "    Top";
	private static final	String	LEFT_STR						= "Left";
	private static final	String	FIELD_NUM_OFFSET_STR			= "Field-number offset:" + TOP_STR;
	private static final	String	CELL_OFFSET_STR					= "Cell offset:" + TOP_STR;
	private static final	String	GRID_COLOUR_STR					= "Grid colour";
	private static final	String	ENTRY_COLOUR_STR				= "Grid-entry colour";
	private static final	String	NUM_LINES_STR					= "Number of lines";
	private static final	String	LINE_WIDTH_STR					= "Line width";
	private static final	String	COLOUR_STR						= "Colour";
	private static final	String	PRINT_ONLY_STR					= "Print only";
	private static final	String	BAR_COLOUR_STR					= "Bar colour";
	private static final	String	HTML_GRID_COLOUR_STR			= "HTML grid colour";
	private static final	String	HTML_ENTRY_COLOUR_STR			= "HTML entry colour";
	private static final	String	HTML_BLOCK_IMAGE_COLOUR_STR		= "HTML block-image colour";
	private static final	String	HTML_BAR_COLOUR_STR				= "HTML bar colour";

	private static final	String[]	GENERIC_FONT_NAMES	= { "serif", "sans-serif", "monospace" };

	// Files panel
	private static final	String	FILENAME_SUFFIX_STR		= "Filename suffix";
	private static final	String	PARAMETER_SET_FILE_STR	= "Parameter-set file";
	private static final	String	SELECT_STR				= "Select";
	private static final	String	SELECT_FILE_STR			= "Select file";

	// Fonts panel
	private static final	String	PT_STR	= "pt";

	// Commands
	private interface Command
	{
		String	EDIT_DIRECTION_KEYWORDS		= "editDirectionKeywords.";
		String	EDIT_HTML_FONT_NAMES		= "editHtmlFontNames";
		String	CHOOSE_STATUS_TEXT_COLOUR	= "chooseStatusTextColour";
		String	CHOOSE_VIEW_COLOUR			= "chooseViewColour";
		String	CHOOSE_HTML_GRID_COLOUR		= "chooseHtmlGridColour";
		String	CHOOSE_HTML_ENTRY_COLOUR	= "chooseHtmlEntryColour";
		String	CHOOSE_BLOCK_IMAGE_COLOUR	= "chooseBlockImageColour";
		String	CHOOSE_BAR_COLOUR			= "chooseBarColour";
		String	CHOOSE_PARAMETER_SET_FILE	= "chooseParameterSetFile";
		String	SAVE_CONFIGURATION			= "saveConfiguration";
		String	ACCEPT						= "accept";
		String	CLOSE						= "close";
	}

////////////////////////////////////////////////////////////////////////
//  Enumerated types
////////////////////////////////////////////////////////////////////////


	// TABS


	private enum Tab
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		GENERAL
		(
			"General"
		)
		{
			@Override
			protected JPanel createPanel(PreferencesDialog dialog)
			{
				return dialog.createPanelGeneral();
			}

			//----------------------------------------------------------

			@Override
			protected void validatePreferences(PreferencesDialog dialog)
				throws AppException
			{
				dialog.validatePreferencesGeneral();
			}

			//----------------------------------------------------------

			@Override
			protected void setPreferences(PreferencesDialog dialog)
			{
				dialog.setPreferencesGeneral();
			}

			//----------------------------------------------------------
		},

		APPEARANCE
		(
			"Appearance"
		)
		{
			@Override
			protected JPanel createPanel(PreferencesDialog dialog)
			{
				return dialog.createPanelAppearance();
			}

			//----------------------------------------------------------

			@Override
			protected void validatePreferences(PreferencesDialog dialog)
				throws AppException
			{
				dialog.validatePreferencesAppearance();
			}

			//----------------------------------------------------------

			@Override
			protected void setPreferences(PreferencesDialog dialog)
			{
				dialog.setPreferencesAppearance();
			}

			//----------------------------------------------------------
		},

		VIEW
		(
			"View"
		)
		{
			@Override
			protected JPanel createPanel(PreferencesDialog dialog)
			{
				return dialog.createPanelView();
			}

			//----------------------------------------------------------

			@Override
			protected void validatePreferences(PreferencesDialog dialog)
				throws AppException
			{
				dialog.validatePreferencesView();
			}

			//----------------------------------------------------------

			@Override
			protected void setPreferences(PreferencesDialog dialog)
			{
				dialog.setPreferencesView();
			}

			//----------------------------------------------------------
		},

		GRID
		(
			"Grid"
		)
		{
			@Override
			protected JPanel createPanel(PreferencesDialog dialog)
			{
				return dialog.createPanelGrid();
			}

			//----------------------------------------------------------

			@Override
			protected void validatePreferences(PreferencesDialog dialog)
				throws AppException
			{
				dialog.validatePreferencesGrid();
			}

			//----------------------------------------------------------

			@Override
			protected void setPreferences(PreferencesDialog dialog)
			{
				dialog.setPreferencesGrid();
			}

			//----------------------------------------------------------
		},

		CLUES
		(
			"Clues"
		)
		{
			@Override
			protected JPanel createPanel(PreferencesDialog dialog)
			{
				return dialog.createPanelClues();
			}

			//----------------------------------------------------------

			@Override
			protected void validatePreferences(PreferencesDialog dialog)
				throws AppException
			{
				dialog.validatePreferencesClues();
			}

			//----------------------------------------------------------

			@Override
			protected void setPreferences(PreferencesDialog dialog)
			{
				dialog.setPreferencesClues();
			}

			//----------------------------------------------------------
		},

		TEXT_SECTIONS
		(
			"Text sections"
		)
		{
			@Override
			protected JPanel createPanel(PreferencesDialog dialog)
			{
				return dialog.createPanelTextSections();
			}

			//----------------------------------------------------------

			@Override
			protected void validatePreferences(PreferencesDialog dialog)
				throws AppException
			{
				dialog.validatePreferencesTextSections();
			}

			//----------------------------------------------------------

			@Override
			protected void setPreferences(PreferencesDialog dialog)
			{
				dialog.setPreferencesTextSections();
			}

			//----------------------------------------------------------
		},

		HTML
		(
			"HTML"
		)
		{
			@Override
			protected JPanel createPanel(PreferencesDialog dialog)
			{
				return dialog.createPanelHtml();
			}

			//----------------------------------------------------------

			@Override
			protected void validatePreferences(PreferencesDialog dialog)
				throws AppException
			{
				dialog.validatePreferencesHtml();
			}

			//----------------------------------------------------------

			@Override
			protected void setPreferences(PreferencesDialog dialog)
			{
				dialog.setPreferencesHtml();
			}

			//----------------------------------------------------------
		},

		FILES
		(
			"Files"
		)
		{
			@Override
			protected JPanel createPanel(PreferencesDialog dialog)
			{
				return dialog.createPanelFiles();
			}

			//----------------------------------------------------------

			@Override
			protected void validatePreferences(PreferencesDialog dialog)
				throws AppException
			{
				dialog.validatePreferencesFiles();
			}

			//----------------------------------------------------------

			@Override
			protected void setPreferences(PreferencesDialog dialog)
			{
				dialog.setPreferencesFiles();
			}

			//----------------------------------------------------------
		},

		FONTS
		(
			"Fonts"
		)
		{
			@Override
			protected JPanel createPanel(PreferencesDialog dialog)
			{
				return dialog.createPanelFonts();
			}

			//----------------------------------------------------------

			@Override
			protected void validatePreferences(PreferencesDialog dialog)
				throws AppException
			{
				dialog.validatePreferencesFonts();
			}

			//----------------------------------------------------------

			@Override
			protected void setPreferences(PreferencesDialog dialog)
			{
				dialog.setPreferencesFonts();
			}

			//----------------------------------------------------------
		};

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private Tab(String text)
		{
			this.text = text;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Abstract methods
	////////////////////////////////////////////////////////////////////

		protected abstract JPanel createPanel(PreferencesDialog dialog);

		//--------------------------------------------------------------

		protected abstract void validatePreferences(PreferencesDialog dialog)
			throws AppException;

		//--------------------------------------------------------------

		protected abstract void setPreferences(PreferencesDialog dialog);

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	String	text;

	}

	//==================================================================


	// ERROR IDENTIFIERS


	private enum ErrorId
		implements AppException.IId
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		NO_LINE_BREAK_SEQUENCE
		("No line-break sequence was specified."),

		NO_FILENAME_SUFFIX
		("No filename suffix was specified."),

		NOT_A_FILE
		("The pathname does not denote a normal file."),

		FILE_ACCESS_NOT_PERMITTED
		("Access to the file was not permitted.");

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private ErrorId(String message)
		{
			this.message = message;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : AppException.IId interface
	////////////////////////////////////////////////////////////////////

		public String getMessage()
		{
			return message;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	String	message;

	}

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Member classes : non-inner classes
////////////////////////////////////////////////////////////////////////


	// GRID PANEL LABEL CLASS


	private static class GridPanelLabel
		extends FixedWidthLabel
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		private static final	String	KEY	= GridPanelLabel.class.getCanonicalName();

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private GridPanelLabel(String text)
		{
			super(text);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Class methods
	////////////////////////////////////////////////////////////////////

		private static void reset()
		{
			MaxValueMap.removeAll(KEY);
		}

		//--------------------------------------------------------------

		private static void update()
		{
			MaxValueMap.update(KEY);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		protected String getKey()
		{
			return KEY;
		}

		//--------------------------------------------------------------

	}

	//==================================================================


	// CLUES PANEL LABEL CLASS


	private static class CluesPanelLabel
		extends FixedWidthLabel
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		private static final	String	KEY	= CluesPanelLabel.class.getCanonicalName();

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CluesPanelLabel(String text)
		{
			super(text);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Class methods
	////////////////////////////////////////////////////////////////////

		private static void reset()
		{
			MaxValueMap.removeAll(KEY);
		}

		//--------------------------------------------------------------

		private static void update()
		{
			MaxValueMap.update(KEY);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		protected String getKey()
		{
			return KEY;
		}

		//--------------------------------------------------------------

	}

	//==================================================================


	// HTML PANEL LABEL CLASS


	private static class HtmlPanelLabel
		extends FixedWidthLabel
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		private static final	String	KEY	= HtmlPanelLabel.class.getCanonicalName();

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private HtmlPanelLabel(String text)
		{
			super(text);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Class methods
	////////////////////////////////////////////////////////////////////

		private static void reset()
		{
			MaxValueMap.removeAll(KEY);
		}

		//--------------------------------------------------------------

		private static void update()
		{
			MaxValueMap.update(KEY);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		protected String getKey()
		{
			return KEY;
		}

		//--------------------------------------------------------------

	}

	//==================================================================


	// COLOUR BUTTON CLASS


	private static class ColourButton
		extends JButton
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		private static final	int		ICON_WIDTH	= 40;
		private static final	int		ICON_HEIGHT	= 16;
		private static final	Insets	MARGINS		= new Insets(2, 2, 2, 2);

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private ColourButton(Color colour)
		{
			super(new ColourSampleIcon(ICON_WIDTH, ICON_HEIGHT));
			setMargin(MARGINS);
			setForeground(colour);
		}

		//--------------------------------------------------------------

	}

	//==================================================================


	// GRID-ENTRY CHARACTERS FIELD CLASS


	private static class EntryCharsField
		extends ConstrainedTextField
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		private static final	int	NUM_COLUMNS	= 40;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private EntryCharsField(String text)
		{
			super(0, NUM_COLUMNS, text);
			AppFont.TEXT_FIELD.apply(this);
			GuiUtils.setTextComponentMargins(this);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		protected int getColumnWidth()
		{
			return FontUtils.getCharWidth('0', getFontMetrics(getFont()));
		}

		//--------------------------------------------------------------

		@Override
		protected String translateInsertString(String str,
											   int    offset)
		{
			return str.toUpperCase();
		}

		//--------------------------------------------------------------

		@Override
		protected boolean acceptCharacter(char ch,
										  int  index)
		{
			return (getText().indexOf(Character.toUpperCase(ch)) < 0) && !Character.isISOControl(ch);
		}

		//--------------------------------------------------------------

	}

	//==================================================================


	// FILENAME-SUFFIX FIELD CLASS


	private static class FilenameSuffixField
		extends ConstrainedTextField
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		private static final	int		LENGTH			= 32;
		private static final	int		NUM_COLUMNS		= 8;
		private static final	String	VALID_SYMBOLS	= "-._";

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private FilenameSuffixField(String text)
		{
			super(LENGTH, NUM_COLUMNS, text);
			AppFont.TEXT_FIELD.apply(this);
			GuiUtils.setTextComponentMargins(this);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		protected boolean acceptCharacter(char ch,
										  int  index)
		{
			return (Character.isAlphabetic(ch) || Character.isDigit(ch) ||
					 (VALID_SYMBOLS.indexOf(ch) >= 0));
		}

		//--------------------------------------------------------------

	}

	//==================================================================


	// STRING-LIST FIELD CLASS


	private static class StringListField
		extends InformationField
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private StringListField(int        numColumns,
								StringList strings)
		{
			super(numColumns);
			setStrings(strings);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		public void setStrings(StringList strings)
		{
			this.strings = strings;
			setText(strings.toQuotedString());
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	StringList	strings;

	}

	//==================================================================


	// IMAGE PANEL CLASS


	private static class ImagePanel
		extends JComponent
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		private static final	int	WIDTH	= Grid.MAX_HTML_CELL_SIZE + 2;
		private static final	int	HEIGHT	= WIDTH;

		private static final	Color	IMAGE_BACKGROUND_COLOUR	= Color.WHITE;
		private static final	Color	IMAGE_BORDER_COLOUR		= new Color(208, 208, 208);

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private ImagePanel()
		{
			// Set component attributes
			setPreferredSize(new Dimension(WIDTH, HEIGHT));
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
			// Fill background
			Rectangle rect = gr.getClipBounds();
			gr.setColor(getBackground());
			gr.fillRect(rect.x, rect.y, rect.width, rect.height);

			// Draw image
			if (image != null)
			{
				int width = image.getWidth();
				int height = image.getHeight();

				// Fill image background
				gr.setColor(IMAGE_BACKGROUND_COLOUR);
				gr.fillRect(1, 1, width, height);

				// Draw image
				gr.drawImage(image, 1, 1, null);

				// Draw image border
				gr.setColor(IMAGE_BORDER_COLOUR);
				gr.drawRect(0, 0, width + 1, height + 1);
			}
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		private void updateImage(int    size,
								 int    numLines,
								 double lineWidth,
								 Color  colour)
		{
			image = BlockGrid.createBlockImage(size, numLines, (float)lineWidth, colour);
			repaint();
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	BufferedImage	image;

	}

	//==================================================================


	// FONT PANEL CLASS


	private static class FontPanel
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		private static final	int	MIN_SIZE	= 0;
		private static final	int	MAX_SIZE	= 99;

		private static final	int	SIZE_FIELD_LENGTH	= 2;

		private static final	String	DEFAULT_FONT_STR	= "<default font>";

	////////////////////////////////////////////////////////////////////
	//  Member classes : non-inner classes
	////////////////////////////////////////////////////////////////////


		// SIZE SPINNER CLASS


		private static class SizeSpinner
			extends IntegerSpinner
		{

		////////////////////////////////////////////////////////////////
		//  Constructors
		////////////////////////////////////////////////////////////////

			private SizeSpinner(int value)
			{
				super(value, MIN_SIZE, MAX_SIZE, SIZE_FIELD_LENGTH);
				AppFont.TEXT_FIELD.apply(this);
			}

			//----------------------------------------------------------

		////////////////////////////////////////////////////////////////
		//  Instance methods : overriding methods
		////////////////////////////////////////////////////////////////

			/**
			 * @throws NumberFormatException
			 */

			@Override
			protected int getEditorValue()
			{
				IntegerValueField field = (IntegerValueField)getEditor();
				return (field.isEmpty() ? 0 : field.getValue());
			}

			//----------------------------------------------------------

			@Override
			protected void setEditorValue(int value)
			{
				IntegerValueField field = (IntegerValueField)getEditor();
				if (value == 0)
					field.setText(null);
				else
					field.setValue(value);
			}

			//----------------------------------------------------------

		}

		//==============================================================

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private FontPanel(FontEx   font,
						  String[] fontNames)
		{
			nameComboBox = new FComboBox<>();
			nameComboBox.addItem(DEFAULT_FONT_STR);
			for (String fontName : fontNames)
				nameComboBox.addItem(fontName);
			nameComboBox.setSelectedIndex(Utils.indexOf(font.getName(), fontNames) + 1);

			styleComboBox = new FComboBox<>(FontStyle.values());
			styleComboBox.setSelectedValue(font.getStyle());

			sizeSpinner = new SizeSpinner(font.getSize());
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		public FontEx getFont()
		{
			String name = (nameComboBox.getSelectedIndex() <= 0) ? null : nameComboBox.getSelectedValue();
			return new FontEx(name, styleComboBox.getSelectedValue(), sizeSpinner.getIntValue());
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	FComboBox<String>		nameComboBox;
		private	FComboBox<FontStyle>	styleComboBox;
		private	SizeSpinner				sizeSpinner;

	}

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	private PreferencesDialog(Window owner)
	{

		// Call superclass constructor
		super(owner, TITLE_STR, Dialog.ModalityType.APPLICATION_MODAL);

		// Set icons
		setIconImages(owner.getIconImages());


		//----  Tabbed panel

		tabbedPanel = new FTabbedPane();
		for (Tab tab : Tab.values())
			tabbedPanel.addTab(tab.text, tab.createPanel(this));
		tabbedPanel.setSelectedIndex(tabIndex);


		//----  Button panel: save configuration

		JPanel saveButtonPanel = new JPanel(new GridLayout(1, 0, 8, 0));

		// Button: save configuration
		JButton saveButton = new FButton(SAVE_CONFIGURATION_STR + AppConstants.ELLIPSIS_STR);
		saveButton.setActionCommand(Command.SAVE_CONFIGURATION);
		saveButton.addActionListener(this);
		saveButtonPanel.add(saveButton);


		//----  Button panel: OK, cancel

		JPanel okCancelButtonPanel = new JPanel(new GridLayout(1, 0, 8, 0));

		// Button: OK
		JButton okButton = new FButton(AppConstants.OK_STR);
		okButton.setActionCommand(Command.ACCEPT);
		okButton.addActionListener(this);
		okCancelButtonPanel.add(okButton);

		// Button: cancel
		JButton cancelButton = new FButton(AppConstants.CANCEL_STR);
		cancelButton.setActionCommand(Command.CLOSE);
		cancelButton.addActionListener(this);
		okCancelButtonPanel.add(cancelButton);


		//----  Button panel

		GridBagLayout gridBag = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();

		JPanel buttonPanel = new JPanel(gridBag);
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(3, 24, 3, 24));

		int gridX = 0;

		gbc.gridx = gridX++;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.5;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(0, 0, 0, 12);
		gridBag.setConstraints(saveButtonPanel, gbc);
		buttonPanel.add(saveButtonPanel);

		gbc.gridx = gridX++;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.5;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(0, 12, 0, 0);
		gridBag.setConstraints(okCancelButtonPanel, gbc);
		buttonPanel.add(okCancelButtonPanel);


		//----  Main panel

		JPanel mainPanel = new JPanel(gridBag);
		mainPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

		int gridY = 0;

		gbc.gridx = 0;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.NORTH;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(0, 0, 0, 0);
		gridBag.setConstraints(tabbedPanel, gbc);
		mainPanel.add(tabbedPanel);

		gbc.gridx = 0;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.NORTH;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(3, 0, 0, 0);
		gridBag.setConstraints(buttonPanel, gbc);
		mainPanel.add(buttonPanel);

		// Add commands to action map
		KeyAction.create(mainPanel, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT,
						 KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), Command.CLOSE, this);


		//----  Window

		// Set content pane
		setContentPane(mainPanel);

		// Dispose of window explicitly
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

		// Handle window closing
		addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent event)
			{
				onClose();
			}
		});

		// Prevent dialog from being resized
		setResizable(false);

		// Resize dialog to its preferred size
		pack();

		// Set location of dialog box
		if (location == null)
			location = GuiUtils.getComponentLocation(this, owner);
		setLocation(location);

		// Set default button
		getRootPane().setDefaultButton(okButton);

		// Show dialog
		setVisible(true);

	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	public static boolean showDialog(Component parent)
	{
		return new PreferencesDialog(GuiUtils.getWindow(parent)).accepted;
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : ActionListener interface
////////////////////////////////////////////////////////////////////////

	public void actionPerformed(ActionEvent event)
	{
		String command = event.getActionCommand();

		if (command.startsWith(Command.EDIT_DIRECTION_KEYWORDS))
			onEditDirectionKeywords(StringUtils.removePrefix(command,
															 Command.EDIT_DIRECTION_KEYWORDS));

		else if (command.equals(Command.EDIT_HTML_FONT_NAMES))
			onEditHtmlFontNames();

		else if (command.equals(Command.CHOOSE_STATUS_TEXT_COLOUR))
			onChooseStatusTextColour();

		else if (command.equals(Command.CHOOSE_VIEW_COLOUR))
			onChooseViewColour();

		else if (command.equals(Command.CHOOSE_HTML_GRID_COLOUR))
			onChooseHtmlGridColour();

		else if (command.equals(Command.CHOOSE_HTML_ENTRY_COLOUR))
			onChooseHtmlEntryColour();

		else if (command.equals(Command.CHOOSE_BLOCK_IMAGE_COLOUR))
			onChooseBlockImageColour();

		else if (command.equals(Command.CHOOSE_BAR_COLOUR))
			onChooseBarColour();

		else if (command.equals(Command.CHOOSE_PARAMETER_SET_FILE))
			onChooseParameterSetFile();

		else if (command.equals(Command.SAVE_CONFIGURATION))
			onSaveConfiguration();

		else if (command.equals(Command.ACCEPT))
			onAccept();

		else if (command.equals(Command.CLOSE))
			onClose();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : ChangeListener interface
////////////////////////////////////////////////////////////////////////

	public void stateChanged(ChangeEvent event)
	{
		updateBlockImage();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : ListSelectionListener interface
////////////////////////////////////////////////////////////////////////

	public void valueChanged(ListSelectionEvent event)
	{
		if (!event.getValueIsAdjusting())
			viewColourButton.setForeground(viewColours.get(viewColoursList.getSelectedValue()));
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	private void validatePreferences()
		throws AppException
	{
		for (Tab tab : Tab.values())
			tab.validatePreferences(this);
	}

	//------------------------------------------------------------------

	private void setPreferences()
	{
		for (Tab tab : Tab.values())
			tab.setPreferences(this);
	}

	//------------------------------------------------------------------

	private void onEditDirectionKeywords(String key)
	{
		Direction direction = Direction.forKey(key);
		StringList keywords = clueDirectionKeywordsFields.get(direction).strings;
		keywords = StringSelectionDialog.showDialog(this, EDIT_DIRECTION_KEYWORDS_STR + direction,
													KEYWORD_STR, keywords, null);
		if (keywords != null)
			clueDirectionKeywordsFields.get(direction).setStrings(keywords);
	}

	//------------------------------------------------------------------

	private void onEditHtmlFontNames()
	{
		List<String> names = new ArrayList<>();
		Collections.addAll(names, GENERIC_FONT_NAMES);
		Collections.addAll(names, GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames());
		StringList fontNames = StringSelectionDialog.showDialog(this, EDIT_FONT_NAMES_STR, FONT_NAME_STR,
																htmlFontNamesField.strings, names);
		if (fontNames != null)
			htmlFontNamesField.setStrings(fontNames);
	}

	//------------------------------------------------------------------

	private void onChooseStatusTextColour()
	{
		Color colour = JColorChooser.showDialog(this, STATUS_TEXT_COLOUR_STR, statusTextColourButton.getForeground());
		if (colour != null)
			statusTextColourButton.setForeground(colour);
	}

	//------------------------------------------------------------------

	private void onChooseViewColour()
	{
		CrosswordView.Colour colourId = viewColoursList.getSelectedValue();
		Color colour = JColorChooser.showDialog(this, VIEW_COLOUR_STR + colourId, viewColourButton.getForeground());
		if (colour != null)
		{
			viewColourButton.setForeground(colour);
			viewColours.put(colourId, colour);
		}
	}

	//------------------------------------------------------------------

	private void onChooseHtmlGridColour()
	{
		Color colour = JColorChooser.showDialog(this, HTML_GRID_COLOUR_STR, htmlGridColourButton.getForeground());
		if (colour != null)
			htmlGridColourButton.setForeground(colour);
	}

	//------------------------------------------------------------------

	private void onChooseHtmlEntryColour()
	{
		Color colour = JColorChooser.showDialog(this, HTML_ENTRY_COLOUR_STR, htmlEntryColourButton.getForeground());
		if (colour != null)
			htmlEntryColourButton.setForeground(colour);
	}

	//------------------------------------------------------------------

	private void onChooseBlockImageColour()
	{
		Color colour = JColorChooser.showDialog(this, HTML_BLOCK_IMAGE_COLOUR_STR,
												blockImageColourButton.getForeground());
		if (colour != null)
		{
			blockImageColourButton.setForeground(colour);
			updateBlockImage();
		}
	}

	//------------------------------------------------------------------

	private void onChooseBarColour()
	{
		Color colour = JColorChooser.showDialog(this, HTML_BAR_COLOUR_STR, htmlBarColourButton.getForeground());
		if (colour != null)
			htmlBarColourButton.setForeground(colour);
	}

	//------------------------------------------------------------------

	private void onChooseParameterSetFile()
	{
		if (parameterSetFileChooser == null)
		{
			parameterSetFileChooser = new JFileChooser();
			parameterSetFileChooser.setDialogTitle(PARAMETER_SET_FILE_STR);
			parameterSetFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			parameterSetFileChooser.setApproveButtonMnemonic(KeyEvent.VK_S);
			parameterSetFileChooser.setApproveButtonToolTipText(SELECT_FILE_STR);
			parameterSetFileChooser.setFileFilter(new FilenameSuffixFilter(AppConstants.XML_FILES_STR,
																		   AppConstants.XML_FILENAME_EXTENSION));
		}
		parameterSetFileChooser.setSelectedFile(parameterSetPathnameField.getCanonicalFile());
		parameterSetFileChooser.rescanCurrentDirectory();
		if (parameterSetFileChooser.showDialog(this, SELECT_STR) == JFileChooser.APPROVE_OPTION)
			parameterSetPathnameField.setFile(parameterSetFileChooser.getSelectedFile());
	}

	//------------------------------------------------------------------

	private void onSaveConfiguration()
	{
		try
		{
			validatePreferences();

			File file = AppConfig.INSTANCE.chooseFile(this);
			if (file != null)
			{
				String[] optionStrs = Utils.getOptionStrings(AppConstants.REPLACE_STR);
				if (!file.exists() ||
					 (JOptionPane.showOptionDialog(this, Utils.getPathname(file) +
																			AppConstants.ALREADY_EXISTS_STR,
												   SAVE_CONFIG_FILE_STR, JOptionPane.OK_CANCEL_OPTION,
												   JOptionPane.WARNING_MESSAGE, null, optionStrs,
												   optionStrs[1]) == JOptionPane.OK_OPTION))
				{
					setPreferences();
					accepted = true;
					TaskProgressDialog.showDialog(this, WRITE_CONFIG_FILE_STR,
												  new Task.WriteConfig(file));
				}
			}
		}
		catch (AppException e)
		{
			JOptionPane.showMessageDialog(this, e, App.SHORT_NAME, JOptionPane.ERROR_MESSAGE);
		}
		if (accepted)
			onClose();
	}

	//------------------------------------------------------------------

	private void onAccept()
	{
		try
		{
			validatePreferences();
			setPreferences();
			accepted = true;
			onClose();
		}
		catch (AppException e)
		{
			JOptionPane.showMessageDialog(this, e, App.SHORT_NAME, JOptionPane.ERROR_MESSAGE);
		}
	}

	//------------------------------------------------------------------

	private void onClose()
	{
		FPathnameField.removeObservers(KEY);

		location = getLocation();
		tabIndex = tabbedPanel.getSelectedIndex();
		setVisible(false);
		dispose();
	}

	//------------------------------------------------------------------

	private void updateBlockImage()
	{
		blockImagePanel.updateImage(htmlCellSizeSpinners.get(Grid.Separator.BLOCK).getIntValue(),
									blockImageNumLinesSpinner.getIntValue(),
									blockImageLineWidthSpinner.getDoubleValue(),
									blockImageColourButton.getForeground());
	}

	//------------------------------------------------------------------

	private JPanel createPanelGeneral()
	{
		//----  Control panel

		GridBagLayout gridBag = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();

		JPanel controlPanel = new JPanel(gridBag);
		GuiUtils.setPaddedLineBorder(controlPanel);

		int gridY = 0;

		AppConfig config = AppConfig.INSTANCE;

		// Label: show UNIX pathnames
		JLabel showUnixPathnamesLabel = new FLabel(SHOW_UNIX_PATHNAMES_STR);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(showUnixPathnamesLabel, gbc);
		controlPanel.add(showUnixPathnamesLabel);

		// Combo box: show UNIX pathnames
		showUnixPathnamesComboBox = new BooleanComboBox(config.isShowUnixPathnames());

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(showUnixPathnamesComboBox, gbc);
		controlPanel.add(showUnixPathnamesComboBox);

		// Label: select text on focus gained
		JLabel selectTextOnFocusGainedLabel = new FLabel(SELECT_TEXT_ON_FOCUS_GAINED_STR);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(selectTextOnFocusGainedLabel, gbc);
		controlPanel.add(selectTextOnFocusGainedLabel);

		// Combo box: select text on focus gained
		selectTextOnFocusGainedComboBox = new BooleanComboBox(config.isSelectTextOnFocusGained());

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(selectTextOnFocusGainedComboBox, gbc);
		controlPanel.add(selectTextOnFocusGainedComboBox);

		// Label: save main window location
		JLabel saveMainWindowLocationLabel = new FLabel(SAVE_MAIN_WINDOW_LOCATION_STR);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(saveMainWindowLocationLabel, gbc);
		controlPanel.add(saveMainWindowLocationLabel);

		// Combo box: save main window location
		saveMainWindowLocationComboBox = new BooleanComboBox(config.isMainWindowLocation());

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(saveMainWindowLocationComboBox, gbc);
		controlPanel.add(saveMainWindowLocationComboBox);

		// Label: maximum edit list length
		JLabel maxEditListLengthLabel = new FLabel(MAX_EDIT_HISTORY_SIZE_STR);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(maxEditListLengthLabel, gbc);
		controlPanel.add(maxEditListLengthLabel);

		// Spinner: maximum edit list length
		maxEditListLengthSpinner = new FIntegerSpinner(config.getMaxEditListLength(),
													   CrosswordDocument.MIN_MAX_EDIT_LIST_LENGTH,
													   CrosswordDocument.MAX_MAX_EDIT_LIST_LENGTH,
													   MAX_EDIT_LIST_LENGTH_FIELD_LENGTH);

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(maxEditListLengthSpinner, gbc);
		controlPanel.add(maxEditListLengthSpinner);

		// Label: clear edit list on save
		JLabel clearEditListOnSaveLabel = new FLabel(CLEAR_EDIT_HISTORY_ON_SAVE_STR);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(clearEditListOnSaveLabel, gbc);
		controlPanel.add(clearEditListOnSaveLabel);

		// Combo box: clear edit list on save
		clearEditListOnSaveComboBox = new BooleanComboBox(config.isClearEditListOnSave());

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(clearEditListOnSaveComboBox, gbc);
		controlPanel.add(clearEditListOnSaveComboBox);


		//----  Outer panel

		JPanel outerPanel = new JPanel(gridBag);
		outerPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.NORTH;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(0, 0, 0, 0);
		gridBag.setConstraints(controlPanel, gbc);
		outerPanel.add(controlPanel);

		return outerPanel;
	}

	//------------------------------------------------------------------

	private JPanel createPanelAppearance()
	{
		//----  Control panel

		GridBagLayout gridBag = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();

		JPanel controlPanel = new JPanel(gridBag);
		GuiUtils.setPaddedLineBorder(controlPanel);

		int gridY = 0;

		AppConfig config = AppConfig.INSTANCE;

		// Label: look-and-feel
		JLabel lookAndFeelLabel = new FLabel(LOOK_AND_FEEL_STR);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(lookAndFeelLabel, gbc);
		controlPanel.add(lookAndFeelLabel);

		// Combo box: look-and-feel
		lookAndFeelComboBox = new FComboBox<>();

		UIManager.LookAndFeelInfo[] lookAndFeelInfos = UIManager.getInstalledLookAndFeels();
		if (lookAndFeelInfos.length == 0)
		{
			lookAndFeelComboBox.addItem(NO_LOOK_AND_FEELS_STR);
			lookAndFeelComboBox.setSelectedIndex(0);
			lookAndFeelComboBox.setEnabled(false);
		}
		else
		{
			String[] lookAndFeelNames = new String[lookAndFeelInfos.length];
			for (int i = 0; i < lookAndFeelInfos.length; i++)
			{
				lookAndFeelNames[i] = lookAndFeelInfos[i].getName();
				lookAndFeelComboBox.addItem(lookAndFeelNames[i]);
			}
			lookAndFeelComboBox.setSelectedValue(config.getLookAndFeel());
		}

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(lookAndFeelComboBox, gbc);
		controlPanel.add(lookAndFeelComboBox);

		// Label: text antialiasing
		JLabel textAntialiasingLabel = new FLabel(TEXT_ANTIALIASING_STR);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(textAntialiasingLabel, gbc);
		controlPanel.add(textAntialiasingLabel);

		// Combo box: text antialiasing
		textAntialiasingComboBox = new FComboBox<>(TextRendering.Antialiasing.values());
		textAntialiasingComboBox.setSelectedValue(config.getTextAntialiasing());

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 1.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(textAntialiasingComboBox, gbc);
		controlPanel.add(textAntialiasingComboBox);

		// Label: status text colour
		JLabel statusTextColourLabel = new FLabel(STATUS_TEXT_COLOUR_STR);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(statusTextColourLabel, gbc);
		controlPanel.add(statusTextColourLabel);

		// Button: status text colour
		statusTextColourButton = new ColourButton(config.getStatusTextColour());
		statusTextColourButton.setActionCommand(Command.CHOOSE_STATUS_TEXT_COLOUR);
		statusTextColourButton.addActionListener(this);

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(statusTextColourButton, gbc);
		controlPanel.add(statusTextColourButton);


		//----  Outer panel

		JPanel outerPanel = new JPanel(gridBag);
		outerPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.NORTH;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(0, 0, 0, 0);
		gridBag.setConstraints(controlPanel, gbc);
		outerPanel.add(controlPanel);

		return outerPanel;
	}

	//------------------------------------------------------------------

	private JPanel createPanelView()
	{
		//----  Control panel

		GridBagLayout gridBag = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();

		JPanel controlPanel = new JPanel(gridBag);
		GuiUtils.setPaddedLineBorder(controlPanel);

		int gridY = 0;

		AppConfig config = AppConfig.INSTANCE;

		// Label: number of columns for selected clue
		JLabel selClueNumColumnsLabel = new FLabel(SELECTED_CLUE_NUM_COLUMNS_STR);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(selClueNumColumnsLabel, gbc);
		controlPanel.add(selClueNumColumnsLabel);

		// Panel: number of columns
		JPanel numColumnsPanel = new JPanel(gridBag);

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(numColumnsPanel, gbc);
		controlPanel.add(numColumnsPanel);

		// Spinner: number of columns for selected clue
		selClueNumColumnsSpinner = new FIntegerSpinner(config.getSelectedClueNumColumns(),
													   CrosswordView.MIN_SELECTED_CLUE_NUM_COLUMNS,
													   CrosswordView.MAX_SELECTED_CLUE_NUM_COLUMNS,
													   SELECTED_CLUE_NUM_COLUMNS_FIELD_LENGTH);

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(0, 0, 0, 0);
		gridBag.setConstraints(selClueNumColumnsSpinner, gbc);
		numColumnsPanel.add(selClueNumColumnsSpinner);

		// Label: columns
		JLabel columnsLabel = new FLabel(COLUMNS_STR);

		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(0, 4, 0, 0);
		gridBag.setConstraints(columnsLabel, gbc);
		numColumnsPanel.add(columnsLabel);


		//----  Colours panel

		JPanel coloursPanel = new JPanel(gridBag);
		TitledBorder.setPaddedBorder(coloursPanel, COLOURS_STR, 6, 8);

		// Initialise colours
		viewColours = new EnumMap<>(CrosswordView.Colour.class);
		for (CrosswordView.Colour colour : CrosswordView.Colour.values())
			viewColours.put(colour, colour.get());

		// List: colours
		viewColoursList = new SelectionList<CrosswordView.Colour>(CrosswordView.Colour.values(), 0,
																  VIEW_COLOURS_LIST_NUM_ROWS);
		viewColoursList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		viewColoursList.setSelectedIndex(0);
		viewColoursList.addListSelectionListener(this);

		// Scroll pane: colours list
		JScrollPane coloursListScrollPane = new JScrollPane(viewColoursList,
															JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
															JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		coloursListScrollPane.getVerticalScrollBar().setFocusable(false);
		coloursListScrollPane.getHorizontalScrollBar().setFocusable(false);

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.NORTH;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(0, 0, 0, 0);
		gridBag.setConstraints(coloursListScrollPane, gbc);
		coloursPanel.add(coloursListScrollPane);

		// Button: colour
		viewColourButton = new ColourButton(CrosswordView.Colour.values()[0].get());
		viewColourButton.setActionCommand(Command.CHOOSE_VIEW_COLOUR);
		viewColourButton.addActionListener(this);

		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.NORTH;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(0, 8, 0, 0);
		gridBag.setConstraints(viewColourButton, gbc);
		coloursPanel.add(viewColourButton);


		//----  Outer panel

		JPanel outerPanel = new JPanel(gridBag);
		outerPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

		gridY = 0;

		gbc.gridx = 0;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.NORTH;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(0, 0, 0, 0);
		gridBag.setConstraints(controlPanel, gbc);
		outerPanel.add(controlPanel);

		gbc.gridx = 0;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.NORTH;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(3, 0, 0, 0);
		gridBag.setConstraints(coloursPanel, gbc);
		outerPanel.add(coloursPanel);

		return outerPanel;
	}

	//------------------------------------------------------------------

	private JPanel createPanelGrid()
	{
		// Reset fixed-width labels
		GridPanelLabel.reset();


		//----  Control panel

		GridBagLayout gridBag = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();

		JPanel controlPanel = new JPanel(gridBag);
		GuiUtils.setPaddedLineBorder(controlPanel);

		AppConfig config = AppConfig.INSTANCE;

		int gridY = 0;

		// Label: grid-entry characters
		JLabel gridEntryCharsLabel = new GridPanelLabel(GRID_ENTRY_CHARS_STR);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(gridEntryCharsLabel, gbc);
		controlPanel.add(gridEntryCharsLabel);

		// Field: grid-entry characters
		gridEntryCharsField = new EntryCharsField(config.getGridEntryCharacters());

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 1.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(gridEntryCharsField, gbc);
		controlPanel.add(gridEntryCharsField);

		// Label: navigate over separators
		JLabel navigateOverSeparatorsLabel = new GridPanelLabel(NAVIGATE_OVER_SEPARATORS_STR);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(navigateOverSeparatorsLabel, gbc);
		controlPanel.add(navigateOverSeparatorsLabel);

		// Combo box: navigate over separators
		navigateOverSeparatorsComboBox = new BooleanComboBox(config.isNavigateOverGridSeparators());

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(navigateOverSeparatorsComboBox, gbc);
		controlPanel.add(navigateOverSeparatorsComboBox);

		// Label: image viewport size
		JLabel imageViewportSizeLabel = new GridPanelLabel(IMAGE_VIEWPORT_SIZE_STR);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(imageViewportSizeLabel, gbc);
		controlPanel.add(imageViewportSizeLabel);

		// Panel: image viewport size
		gridImageViewportSizePanel =
								new DimensionsSpinnerPanel(config.getGridImageViewportSize().width,
														   CaptureDialog.MIN_GRID_IMAGE_VIEWPORT_WIDTH,
														   CaptureDialog.MAX_GRID_IMAGE_VIEWPORT_WIDTH,
														   GRID_IMAGE_VIEWPORT_FIELD_LENGTH,
														   config.getGridImageViewportSize().height,
														   CaptureDialog.MIN_GRID_IMAGE_VIEWPORT_HEIGHT,
														   CaptureDialog.MAX_GRID_IMAGE_VIEWPORT_HEIGHT,
														   GRID_IMAGE_VIEWPORT_FIELD_LENGTH,
														   new String[] { WIDTH_STR, HEIGHT_STR });

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(gridImageViewportSizePanel, gbc);
		controlPanel.add(gridImageViewportSizePanel);


		//----  Separator-specific panels

		Map<Grid.Separator, JPanel> separatorPanels = new EnumMap<>(Grid.Separator.class);
		gridCellSizeSpinners = new EnumMap<>(Grid.Separator.class);
		for (Grid.Separator separator : Grid.Separator.values())
		{
			// Separator panel
			JPanel separatorPanel = new JPanel(gridBag);
			TitledBorder.setPaddedBorder(separatorPanel, separator.toString());
			separatorPanels.put(separator, separatorPanel);

			gridY = 0;

			// Label: cell size
			JLabel label = new GridPanelLabel(CELL_SIZE_STR);

			gbc.gridx = 0;
			gbc.gridy = gridY;
			gbc.gridwidth = 1;
			gbc.gridheight = 1;
			gbc.weightx = 0.0;
			gbc.weighty = 0.0;
			gbc.anchor = GridBagConstraints.LINE_END;
			gbc.fill = GridBagConstraints.NONE;
			gbc.insets = AppConstants.COMPONENT_INSETS;
			gridBag.setConstraints(label, gbc);
			separatorPanel.add(label);

			// Spinner: cell width
			FIntegerSpinner spinner = new FIntegerSpinner(config.getGridCellSize(separator), Grid.MIN_CELL_SIZE,
														  Grid.MAX_CELL_SIZE, GRID_CELL_SIZE_FIELD_LENGTH);
			gridCellSizeSpinners.put(separator, spinner);

			gbc.gridx = 1;
			gbc.gridy = gridY++;
			gbc.gridwidth = 1;
			gbc.gridheight = 1;
			gbc.weightx = 1.0;
			gbc.weighty = 0.0;
			gbc.anchor = GridBagConstraints.LINE_START;
			gbc.fill = GridBagConstraints.NONE;
			gbc.insets = AppConstants.COMPONENT_INSETS;
			gridBag.setConstraints(spinner, gbc);
			separatorPanel.add(spinner);

			// Add components specific to separator
			switch (separator)
			{
				case BLOCK:
					// do nothing
					break;

				case BAR:
					addGridPanelBar(separatorPanel, gridBag, gbc, gridY);
					break;
			}
		}

		// Update widths of labels
		GridPanelLabel.update();


		//----  Outer panel

		JPanel outerPanel = new JPanel(gridBag);
		outerPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

		gridY = 0;

		gbc.gridx = 0;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.NORTH;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(0, 0, 0, 0);
		gridBag.setConstraints(controlPanel, gbc);
		outerPanel.add(controlPanel);

		for (Grid.Separator separator : separatorPanels.keySet())
		{
			JPanel separatorPanel = separatorPanels.get(separator);

			gbc.gridx = 0;
			gbc.gridy = gridY++;
			gbc.gridwidth = 1;
			gbc.gridheight = 1;
			gbc.weightx = 0.0;
			gbc.weighty = 0.0;
			gbc.anchor = GridBagConstraints.NORTH;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.insets = new Insets(3, 0, 0, 0);
			gridBag.setConstraints(separatorPanel, gbc);
			outerPanel.add(separatorPanel);
		}

		return outerPanel;
	}

	//------------------------------------------------------------------

	private void addGridPanelBar(JPanel             panel,
								 GridBagLayout      gridBag,
								 GridBagConstraints gbc,
								 int                gridY)
	{
		AppConfig config = AppConfig.INSTANCE;

		// Label: bar width
		JLabel barWidthLabel = new GridPanelLabel(BAR_WIDTH_STR);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(barWidthLabel, gbc);
		panel.add(barWidthLabel);

		// Spinner: bar width
		gridBarWidthSpinner = new FIntegerSpinner(config.getBarGridBarWidth(),
												  GridPanel.Bar.MIN_BAR_WIDTH, GridPanel.Bar.MAX_BAR_WIDTH,
												  BAR_WIDTH_FIELD_LENGTH);

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(gridBarWidthSpinner, gbc);
		panel.add(gridBarWidthSpinner);
	}

	//------------------------------------------------------------------

	private JPanel createPanelClues()
	{
		// Reset fixed-width labels
		CluesPanelLabel.reset();


		//----  Direction keywords panel

		GridBagLayout gridBag = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();

		JPanel directionKeywordsPanel = new JPanel(gridBag);
		TitledBorder.setPaddedBorder(directionKeywordsPanel, DIRECTION_KEYWORDS_STR);

		int gridY = 0;

		AppConfig config = AppConfig.INSTANCE;

		clueDirectionKeywordsFields = new EnumMap<>(Direction.class);
		for (Direction direction : Direction.DEFINED_DIRECTIONS)
		{
			// Label: direction
			JLabel label = new CluesPanelLabel(direction.toString());

			gbc.gridx = 0;
			gbc.gridy = gridY;
			gbc.gridwidth = 1;
			gbc.gridheight = 1;
			gbc.weightx = 0.0;
			gbc.weighty = 0.0;
			gbc.anchor = GridBagConstraints.LINE_END;
			gbc.fill = GridBagConstraints.NONE;
			gbc.insets = AppConstants.COMPONENT_INSETS;
			gridBag.setConstraints(label, gbc);
			directionKeywordsPanel.add(label);

			// Panel: keywords
			JPanel panel = new JPanel(gridBag);

			gbc.gridx = 1;
			gbc.gridy = gridY++;
			gbc.gridwidth = 1;
			gbc.gridheight = 1;
			gbc.weightx = 1.0;
			gbc.weighty = 0.0;
			gbc.anchor = GridBagConstraints.LINE_START;
			gbc.fill = GridBagConstraints.NONE;
			gbc.insets = AppConstants.COMPONENT_INSETS;
			gridBag.setConstraints(panel, gbc);
			directionKeywordsPanel.add(panel);

			// Field: direction keywords
			StringListField field = new StringListField(DIRECTION_KEYWORDS_FIELD_NUM_COLUMNS,
														config.getClueDirectionKeywords(direction));
			clueDirectionKeywordsFields.put(direction, field);

			gbc.gridx = 0;
			gbc.gridy = 0;
			gbc.gridwidth = 1;
			gbc.gridheight = 1;
			gbc.weightx = 1.0;
			gbc.weighty = 0.0;
			gbc.anchor = GridBagConstraints.LINE_START;
			gbc.fill = GridBagConstraints.NONE;
			gbc.insets = new Insets(0, 0, 0, 0);
			gridBag.setConstraints(field, gbc);
			panel.add(field);

			// Button: edit
			JButton button = new FButton(EDIT_STR + AppConstants.ELLIPSIS_STR);
			button.setMargin(EDIT_BUTTON_MARGINS);
			button.setActionCommand(Command.EDIT_DIRECTION_KEYWORDS + direction.getKey());
			button.addActionListener(this);

			gbc.gridx = 1;
			gbc.gridy = 0;
			gbc.gridwidth = 1;
			gbc.gridheight = 1;
			gbc.weightx = 1.0;
			gbc.weighty = 0.0;
			gbc.anchor = GridBagConstraints.LINE_START;
			gbc.fill = GridBagConstraints.NONE;
			gbc.insets = new Insets(0, 6, 0, 0);
			gridBag.setConstraints(button, gbc);
			panel.add(button);
		}


		//----  Control panel

		JPanel controlPanel = new JPanel(gridBag);
		GuiUtils.setPaddedLineBorder(controlPanel);

		gridY = 0;

		// Label: clue-reference keyword
		JLabel clueReferenceKeywordLabel = new CluesPanelLabel(CLUE_REFERENCE_KEYWORD_STR);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(clueReferenceKeywordLabel, gbc);
		controlPanel.add(clueReferenceKeywordLabel);

		// Field: clue-reference keyword
		clueReferenceKeywordField = new FTextField(config.getClueReferenceKeyword(),
												   CLUE_REFERENCE_KEYWORD_FIELD_NUM_COLUMNS);

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 1.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(clueReferenceKeywordField, gbc);
		controlPanel.add(clueReferenceKeywordField);

		// Label: implicit field direction
		JLabel implicitFieldDirectionLabel = new CluesPanelLabel(IMPLICIT_FIELD_DIRECTION_STR);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(implicitFieldDirectionLabel, gbc);
		controlPanel.add(implicitFieldDirectionLabel);

		// Combo box: implicit field direction
		implicitFieldDirectionComboBox = new BooleanComboBox(config.isImplicitFieldDirection());

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(implicitFieldDirectionComboBox, gbc);
		controlPanel.add(implicitFieldDirectionComboBox);

		// Label: allow multiple field use
		JLabel allowMultipleFieldUseLabel = new CluesPanelLabel(ALLOW_MULTIPLE_FIELD_USE_STR);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(allowMultipleFieldUseLabel, gbc);
		controlPanel.add(allowMultipleFieldUseLabel);

		// Combo box: allow multiple field use
		allowMultipleFieldUseComboBox = new BooleanComboBox(config.isAllowMultipleFieldUse());

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(allowMultipleFieldUseComboBox, gbc);
		controlPanel.add(allowMultipleFieldUseComboBox);

		// Update widths of labels
		CluesPanelLabel.update();


		//----  Outer panel

		JPanel outerPanel = new JPanel(gridBag);
		outerPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

		gridY = 0;

		gbc.gridx = 0;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.NORTH;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(0, 0, 0, 0);
		gridBag.setConstraints(directionKeywordsPanel, gbc);
		outerPanel.add(directionKeywordsPanel);

		gbc.gridx = 0;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.NORTH;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(3, 0, 0, 0);
		gridBag.setConstraints(controlPanel, gbc);
		outerPanel.add(controlPanel);

		return outerPanel;
	}

	//------------------------------------------------------------------

	private JPanel createPanelTextSections()
	{
		//----  Control panel

		GridBagLayout gridBag = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();

		JPanel controlPanel = new JPanel(gridBag);
		GuiUtils.setPaddedLineBorder(controlPanel);

		int gridY = 0;

		AppConfig config = AppConfig.INSTANCE;

		// Label: line break
		JLabel lineBreakSequenceLabel = new FLabel(LINE_BREAK_STR);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(lineBreakSequenceLabel, gbc);
		controlPanel.add(lineBreakSequenceLabel);

		// Field: line break
		lineBreakField = new FTextField(config.getTextSectionLineBreak(), LINE_BREAK_FIELD_NUM_COLUMNS);

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 1.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(lineBreakField, gbc);
		controlPanel.add(lineBreakField);


		//----  Outer panel

		JPanel outerPanel = new JPanel(gridBag);
		outerPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.NORTH;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(0, 0, 0, 0);
		gridBag.setConstraints(controlPanel, gbc);
		outerPanel.add(controlPanel);

		return outerPanel;
	}

	//------------------------------------------------------------------

	private JPanel createPanelHtml()
	{
		// Reset fixed-width labels
		HtmlPanelLabel.reset();


		//----  Control panel

		GridBagLayout gridBag = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();

		JPanel controlPanel = new JPanel(gridBag);
		GuiUtils.setPaddedLineBorder(controlPanel);

		int gridY = 0;

		AppConfig config = AppConfig.INSTANCE;

		// Label: viewer command
		JLabel viewerCommandLabel = new HtmlPanelLabel(VIEWER_COMMAND_STR);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(viewerCommandLabel, gbc);
		controlPanel.add(viewerCommandLabel);

		// Field: viewer command
		htmlViewerCommandField = new FTextField(config.getHtmlViewerCommand(), HTML_VIEWER_COMMAND_FIELD_NUM_COLUMNS);

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 1.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(htmlViewerCommandField, gbc);
		controlPanel.add(htmlViewerCommandField);

		// Label: font names
		JLabel fontNamesLabel = new HtmlPanelLabel(FONT_NAMES_STR);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(fontNamesLabel, gbc);
		controlPanel.add(fontNamesLabel);

		// Panel: font names
		JPanel fontNamesPanel = new JPanel(gridBag);

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(fontNamesPanel, gbc);
		controlPanel.add(fontNamesPanel);

		// Field: font names
		htmlFontNamesField = new StringListField(HTML_FONT_NAMES_FIELD_NUM_COLUMNS, config.getHtmlFontNames());

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(0, 0, 0, 0);
		gridBag.setConstraints(htmlFontNamesField, gbc);
		fontNamesPanel.add(htmlFontNamesField);

		// Button: edit
		JButton editButton = new FButton(EDIT_STR + AppConstants.ELLIPSIS_STR);
		editButton.setMargin(EDIT_BUTTON_MARGINS);
		editButton.setActionCommand(Command.EDIT_HTML_FONT_NAMES);
		editButton.addActionListener(this);

		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(0, 8, 0, 0);
		gridBag.setConstraints(editButton, gbc);
		fontNamesPanel.add(editButton);

		// Label: font size
		JLabel fontSizeLabel = new HtmlPanelLabel(FONT_SIZE_STR);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(fontSizeLabel, gbc);
		controlPanel.add(fontSizeLabel);

		// Panel: font size
		JPanel fontSizePanel = new JPanel(gridBag);

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(fontSizePanel, gbc);
		controlPanel.add(fontSizePanel);

		// Spinner: font size
		htmlFontSizeSpinner = new FIntegerSpinner(config.getHtmlFontSize(), Grid.MIN_HTML_FONT_SIZE,
												  Grid.MAX_HTML_FONT_SIZE, HTML_FONT_SIZE_FIELD_LENGTH);

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(0, 0, 0, 0);
		gridBag.setConstraints(htmlFontSizeSpinner, gbc);
		fontSizePanel.add(htmlFontSizeSpinner);

		// Label: "pt"
		JLabel ptLabel = new FLabel(PT_STR);

		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(0, 4, 0, 0);
		gridBag.setConstraints(ptLabel, gbc);
		fontSizePanel.add(ptLabel);

		// Label: field number font-size factor
		JLabel fieldNumberFontSizeFactorLabel = new HtmlPanelLabel(FIELD_NUM_FONT_SIZE_FACTOR_STR);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(fieldNumberFontSizeFactorLabel, gbc);
		controlPanel.add(fieldNumberFontSizeFactorLabel);

		// Spinner: field number font-size factor
		htmlFieldNumFontSizeFactorSpinner = new FDoubleSpinner(config.getHtmlFieldNumFontSizeFactor(),
															   Grid.MIN_HTML_FIELD_NUM_FONT_SIZE_FACTOR,
															   Grid.MAX_HTML_FIELD_NUM_FONT_SIZE_FACTOR,
															   DELTA_FIELD_NUM_FONT_SIZE_FACTOR,
															   HTML_FIELD_NUM_FONT_SIZE_FACTOR_FIELD_LENGTH,
															   AppConstants.FORMAT_1_3);

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(htmlFieldNumFontSizeFactorSpinner, gbc);
		controlPanel.add(htmlFieldNumFontSizeFactorSpinner);

		// Label: field number offset
		JLabel fieldNumOffsetLabel = new FLabel(FIELD_NUM_OFFSET_STR);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(fieldNumOffsetLabel, gbc);
		controlPanel.add(fieldNumOffsetLabel);

		// Panel: field number offset
		JPanel fieldNumOffsetPanel = new JPanel(gridBag);

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(fieldNumOffsetPanel, gbc);
		controlPanel.add(fieldNumOffsetPanel);

		// Spinner: field number offset, top
		htmlFieldNumOffsetTopSpinner = new FIntegerSpinner(config.getHtmlFieldNumOffsetTop(),
														   Grid.MIN_HTML_FIELD_NUM_OFFSET,
														   Grid.MAX_HTML_FIELD_NUM_OFFSET,
														   HTML_FIELD_NUM_OFFSET_FIELD_LENGTH, true);

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(0, 0, 0, 0);
		gridBag.setConstraints(htmlFieldNumOffsetTopSpinner, gbc);
		fieldNumOffsetPanel.add(htmlFieldNumOffsetTopSpinner);

		// Label: "left"
		JLabel fieldNumOffsetLeftLabel = new FLabel(LEFT_STR);

		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(0, 10, 0, 0);
		gridBag.setConstraints(fieldNumOffsetLeftLabel, gbc);
		fieldNumOffsetPanel.add(fieldNumOffsetLeftLabel);

		// Spinner: field number offset, left
		htmlFieldNumOffsetLeftSpinner = new FIntegerSpinner(config.getHtmlFieldNumOffsetLeft(),
															Grid.MIN_HTML_FIELD_NUM_OFFSET,
															Grid.MAX_HTML_FIELD_NUM_OFFSET,
															HTML_FIELD_NUM_OFFSET_FIELD_LENGTH, true);

		gbc.gridx = 2;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(0, 6, 0, 0);
		gridBag.setConstraints(htmlFieldNumOffsetLeftSpinner, gbc);
		fieldNumOffsetPanel.add(htmlFieldNumOffsetLeftSpinner);

		// Label: cell offset
		JLabel cellOffsetLabel = new FLabel(CELL_OFFSET_STR);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(cellOffsetLabel, gbc);
		controlPanel.add(cellOffsetLabel);

		// Panel: cell offset
		JPanel cellOffsetPanel = new JPanel(gridBag);

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(cellOffsetPanel, gbc);
		controlPanel.add(cellOffsetPanel);

		// Spinner: cell offset, top
		htmlCellOffsetTopSpinner = new FIntegerSpinner(config.getHtmlCellOffsetTop(), Grid.MIN_HTML_CELL_OFFSET,
													   Grid.MAX_HTML_CELL_OFFSET, HTML_CELL_OFFSET_FIELD_LENGTH, true);

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(0, 0, 0, 0);
		gridBag.setConstraints(htmlCellOffsetTopSpinner, gbc);
		cellOffsetPanel.add(htmlCellOffsetTopSpinner);

		// Label: "left"
		JLabel cellOffsetLeftLabel = new FLabel(LEFT_STR);

		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(0, 10, 0, 0);
		gridBag.setConstraints(cellOffsetLeftLabel, gbc);
		cellOffsetPanel.add(cellOffsetLeftLabel);

		// Spinner: cell offset, left
		htmlCellOffsetLeftSpinner = new FIntegerSpinner(config.getHtmlCellOffsetLeft(), Grid.MIN_HTML_CELL_OFFSET,
														Grid.MAX_HTML_CELL_OFFSET, HTML_CELL_OFFSET_FIELD_LENGTH, true);

		gbc.gridx = 2;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(0, 6, 0, 0);
		gridBag.setConstraints(htmlCellOffsetLeftSpinner, gbc);
		cellOffsetPanel.add(htmlCellOffsetLeftSpinner);

		// Label: grid colour
		JLabel gridColourLabel = new HtmlPanelLabel(GRID_COLOUR_STR);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(gridColourLabel, gbc);
		controlPanel.add(gridColourLabel);

		// Button: grid colour
		htmlGridColourButton = new ColourButton(config.getHtmlGridColour());
		htmlGridColourButton.setActionCommand(Command.CHOOSE_HTML_GRID_COLOUR);
		htmlGridColourButton.addActionListener(this);

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(htmlGridColourButton, gbc);
		controlPanel.add(htmlGridColourButton);

		// Label: entry colour
		JLabel entryColourLabel = new HtmlPanelLabel(ENTRY_COLOUR_STR);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(entryColourLabel, gbc);
		controlPanel.add(entryColourLabel);

		// Button: entry colour
		htmlEntryColourButton = new ColourButton(config.getHtmlEntryColour());
		htmlEntryColourButton.setActionCommand(Command.CHOOSE_HTML_ENTRY_COLOUR);
		htmlEntryColourButton.addActionListener(this);

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(htmlEntryColourButton, gbc);
		controlPanel.add(htmlEntryColourButton);


		//----  Block panel

		htmlCellSizeSpinners = new EnumMap<>(Grid.Separator.class);

		Grid.Separator separator = Grid.Separator.BLOCK;
		JPanel blockPanel = new JPanel(gridBag);
		TitledBorder.setPaddedBorder(blockPanel, separator.toString());

		// Panel: block control
		JPanel blockControlPanel = new JPanel(gridBag);

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.FIRST_LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(0, 0, 0, 0);
		gridBag.setConstraints(blockControlPanel, gbc);
		blockPanel.add(blockControlPanel);

		gridY = 0;

		// Label: cell size
		JLabel blockCellSizeLabel = new HtmlPanelLabel(CELL_SIZE_STR);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(blockCellSizeLabel, gbc);
		blockControlPanel.add(blockCellSizeLabel);

		// Spinner: cell size
		FIntegerSpinner blockCellSizeSpinner = new FIntegerSpinner(config.getHtmlCellSize(separator),
																   Grid.MIN_HTML_CELL_SIZE, Grid.MAX_HTML_CELL_SIZE,
																   HTML_CELL_SIZE_FIELD_LENGTH);
		blockCellSizeSpinner.addChangeListener(this);
		htmlCellSizeSpinners.put(separator, blockCellSizeSpinner);

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(blockCellSizeSpinner, gbc);
		blockControlPanel.add(blockCellSizeSpinner);

		// Label: number of lines
		JLabel numLinesLabel = new HtmlPanelLabel(NUM_LINES_STR);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(numLinesLabel, gbc);
		blockControlPanel.add(numLinesLabel);

		// Spinner: number of lines
		blockImageNumLinesSpinner = new FIntegerSpinner(config.getBlockImageNumLines(),
														BlockGrid.MIN_BLOCK_IMAGE_NUM_LINES,
														BlockGrid.MAX_BLOCK_IMAGE_NUM_LINES,
														BLOCK_IMAGE_NUM_LINES_FIELD_LENGTH);
		blockImageNumLinesSpinner.addChangeListener(this);

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 1.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(blockImageNumLinesSpinner, gbc);
		blockControlPanel.add(blockImageNumLinesSpinner);

		// Label: line width
		JLabel lineWidthLabel = new HtmlPanelLabel(LINE_WIDTH_STR);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(lineWidthLabel, gbc);
		blockControlPanel.add(lineWidthLabel);

		// Spinner: line width
		blockImageLineWidthSpinner = new FDoubleSpinner(config.getBlockImageLineWidth(),
														BlockGrid.MIN_BLOCK_IMAGE_LINE_WIDTH,
														BlockGrid.MAX_BLOCK_IMAGE_LINE_WIDTH,
														DELTA_BLOCK_IMAGE_LINE_WIDTH,
														BLOCK_IMAGE_LINE_WIDTH_FIELD_LENGTH,
														AppConstants.FORMAT_1_1F);
		blockImageLineWidthSpinner.addChangeListener(this);

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(blockImageLineWidthSpinner, gbc);
		blockControlPanel.add(blockImageLineWidthSpinner);

		// Label: block-image colour
		JLabel colourLabel = new HtmlPanelLabel(COLOUR_STR);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(colourLabel, gbc);
		blockControlPanel.add(colourLabel);

		// Button: block-image colour
		blockImageColourButton = new ColourButton(config.getBlockImageColour());
		blockImageColourButton.setActionCommand(Command.CHOOSE_BLOCK_IMAGE_COLOUR);
		blockImageColourButton.addActionListener(this);

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(blockImageColourButton, gbc);
		blockControlPanel.add(blockImageColourButton);

		// Label: print only
		JLabel printOnlyLabel = new HtmlPanelLabel(PRINT_ONLY_STR);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(printOnlyLabel, gbc);
		blockControlPanel.add(printOnlyLabel);

		// Combo box: print only
		blockImagePrintOnlyComboBox = new BooleanComboBox(config.isBlockImagePrintOnly());

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(blockImagePrintOnlyComboBox, gbc);
		blockControlPanel.add(blockImagePrintOnlyComboBox);

		// Panel: block image, outer
		JPanel blockImageOuterPanel = new JPanel(gridBag);

		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.FIRST_LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(0, 0, 0, 0);
		gridBag.setConstraints(blockImageOuterPanel, gbc);
		blockPanel.add(blockImageOuterPanel);

		// Panel: block image
		blockImagePanel = new ImagePanel();

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.FIRST_LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(2, 24, 0, 24);
		gridBag.setConstraints(blockImagePanel, gbc);
		blockImageOuterPanel.add(blockImagePanel);

		// Filler
		Box.Filler filler = GuiUtils.createFiller();

		gbc.gridx = 2;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 1.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(0, 0, 0, 0);
		gridBag.setConstraints(filler, gbc);
		blockPanel.add(filler);

		// Update block image
		updateBlockImage();


		//----  Bar panel

		separator = Grid.Separator.BAR;
		JPanel barPanel = new JPanel(gridBag);
		TitledBorder.setPaddedBorder(barPanel, separator.toString());

		gridY = 0;

		// Label: cell size
		JLabel barCellSizeLabel = new FLabel(CELL_SIZE_STR);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(barCellSizeLabel, gbc);
		barPanel.add(barCellSizeLabel);

		// Spinner: cell size
		FIntegerSpinner barCellSizeSpinner = new FIntegerSpinner(config.getHtmlCellSize(separator),
																 Grid.MIN_HTML_CELL_SIZE, Grid.MAX_HTML_CELL_SIZE,
																 HTML_CELL_SIZE_FIELD_LENGTH);
		htmlCellSizeSpinners.put(separator, barCellSizeSpinner);

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(barCellSizeSpinner, gbc);
		barPanel.add(barCellSizeSpinner);

		// Label: bar width
		JLabel barWidthLabel = new FLabel(BAR_WIDTH_STR);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(barWidthLabel, gbc);
		barPanel.add(barWidthLabel);

		// Spinner: bar width
		htmlBarWidthSpinner = new FIntegerSpinner(config.getHtmlBarGridBarWidth(), GridPanel.Bar.MIN_BAR_WIDTH,
												  GridPanel.Bar.MAX_BAR_WIDTH, BAR_WIDTH_FIELD_LENGTH);

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(htmlBarWidthSpinner, gbc);
		barPanel.add(htmlBarWidthSpinner);

		// Label: bar colour
		JLabel barColourLabel = new FLabel(BAR_COLOUR_STR);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(barColourLabel, gbc);
		barPanel.add(barColourLabel);

		// Button: bar colour
		htmlBarColourButton = new ColourButton(config.getHtmlBarColour());
		htmlBarColourButton.setActionCommand(Command.CHOOSE_BAR_COLOUR);
		htmlBarColourButton.addActionListener(this);

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(htmlBarColourButton, gbc);
		barPanel.add(htmlBarColourButton);

		// Update widths of labels
		HtmlPanelLabel.update();


		//----  Outer panel

		JPanel outerPanel = new JPanel(gridBag);
		outerPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

		gridY = 0;

		gbc.gridx = 0;
		gbc.gridy = gridY++;
		gbc.gridwidth = 2;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.NORTH;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(0, 0, 0, 0);
		gridBag.setConstraints(controlPanel, gbc);
		outerPanel.add(controlPanel);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.NORTH;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(3, 0, 0, 0);
		gridBag.setConstraints(blockPanel, gbc);
		outerPanel.add(blockPanel);

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.NORTH;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(3, 3, 0, 0);
		gridBag.setConstraints(barPanel, gbc);
		outerPanel.add(barPanel);

		return outerPanel;
	}

	//------------------------------------------------------------------

	private JPanel createPanelFiles()
	{
		//----  Control panel

		GridBagLayout gridBag = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();

		JPanel controlPanel = new JPanel(gridBag);
		GuiUtils.setPaddedLineBorder(controlPanel);

		int gridY = 0;

		AppConfig config = AppConfig.INSTANCE;

		// Label: filename suffix
		JLabel filenameSuffixLabel = new FLabel(FILENAME_SUFFIX_STR);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(filenameSuffixLabel, gbc);
		controlPanel.add(filenameSuffixLabel);

		// Field: filename suffix
		filenameSuffixField = new FilenameSuffixField(config.getFilenameSuffix());

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(filenameSuffixField, gbc);
		controlPanel.add(filenameSuffixField);

		// Label: parameter-set file
		JLabel parameterSetFileLabel = new FLabel(PARAMETER_SET_FILE_STR);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(parameterSetFileLabel, gbc);
		controlPanel.add(parameterSetFileLabel);

		// Panel: parameter-set pathname
		parameterSetPathnameField = new FPathnameField(config.getParameterSetFile());
		FPathnameField.addObserver(KEY, parameterSetPathnameField);
		JPanel parameterSetPathnamePanel = new PathnamePanel(parameterSetPathnameField,
															 Command.CHOOSE_PARAMETER_SET_FILE, this);

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(parameterSetPathnamePanel, gbc);
		controlPanel.add(parameterSetPathnamePanel);


		//----  Outer panel

		JPanel outerPanel = new JPanel(gridBag);
		outerPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.NORTH;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(0, 0, 0, 0);
		gridBag.setConstraints(controlPanel, gbc);
		outerPanel.add(controlPanel);

		return outerPanel;

	}

	//------------------------------------------------------------------

	private JPanel createPanelFonts()
	{

		//----  Control panel

		GridBagLayout gridBag = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();

		JPanel controlPanel = new JPanel(gridBag);
		GuiUtils.setPaddedLineBorder(controlPanel);

		String[] fontNames = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
		fontPanels = new FontPanel[AppFont.getNumFonts()];
		for (int i = 0; i < fontPanels.length; i++)
		{
			FontEx fontEx = AppConfig.INSTANCE.getFont(i);
			fontPanels[i] = new FontPanel(fontEx, fontNames);

			int gridX = 0;

			// Label: font
			JLabel fontLabel = new FLabel(AppFont.values()[i].toString());

			gbc.gridx = gridX++;
			gbc.gridy = i;
			gbc.gridwidth = 1;
			gbc.gridheight = 1;
			gbc.weightx = 0.0;
			gbc.weighty = 0.0;
			gbc.anchor = GridBagConstraints.LINE_END;
			gbc.fill = GridBagConstraints.NONE;
			gbc.insets = AppConstants.COMPONENT_INSETS;
			gridBag.setConstraints(fontLabel, gbc);
			controlPanel.add(fontLabel);

			// Combo box: font name
			gbc.gridx = gridX++;
			gbc.gridy = i;
			gbc.gridwidth = 1;
			gbc.gridheight = 1;
			gbc.weightx = 0.0;
			gbc.weighty = 0.0;
			gbc.anchor = GridBagConstraints.LINE_START;
			gbc.fill = GridBagConstraints.NONE;
			gbc.insets = AppConstants.COMPONENT_INSETS;
			gridBag.setConstraints(fontPanels[i].nameComboBox, gbc);
			controlPanel.add(fontPanels[i].nameComboBox);

			// Combo box: font style
			gbc.gridx = gridX++;
			gbc.gridy = i;
			gbc.gridwidth = 1;
			gbc.gridheight = 1;
			gbc.weightx = 0.0;
			gbc.weighty = 0.0;
			gbc.anchor = GridBagConstraints.LINE_START;
			gbc.fill = GridBagConstraints.NONE;
			gbc.insets = AppConstants.COMPONENT_INSETS;
			gridBag.setConstraints(fontPanels[i].styleComboBox, gbc);
			controlPanel.add(fontPanels[i].styleComboBox);

			// Panel: font size
			JPanel sizePanel = new JPanel(gridBag);

			gbc.gridx = gridX++;
			gbc.gridy = i;
			gbc.gridwidth = 1;
			gbc.gridheight = 1;
			gbc.weightx = 0.0;
			gbc.weighty = 0.0;
			gbc.anchor = GridBagConstraints.LINE_START;
			gbc.fill = GridBagConstraints.NONE;
			gbc.insets = AppConstants.COMPONENT_INSETS;
			gridBag.setConstraints(sizePanel, gbc);
			controlPanel.add(sizePanel);

			// Spinner: font size
			gbc.gridx = 0;
			gbc.gridy = 0;
			gbc.gridwidth = 1;
			gbc.gridheight = 1;
			gbc.weightx = 0.0;
			gbc.weighty = 0.0;
			gbc.anchor = GridBagConstraints.LINE_START;
			gbc.fill = GridBagConstraints.NONE;
			gbc.insets = new Insets(0, 0, 0, 0);
			gridBag.setConstraints(fontPanels[i].sizeSpinner, gbc);
			sizePanel.add(fontPanels[i].sizeSpinner);

			// Label: "pt"
			JLabel ptLabel = new FLabel(PT_STR);

			gbc.gridx = 1;
			gbc.gridy = 0;
			gbc.gridwidth = 1;
			gbc.gridheight = 1;
			gbc.weightx = 0.0;
			gbc.weighty = 0.0;
			gbc.anchor = GridBagConstraints.LINE_START;
			gbc.fill = GridBagConstraints.NONE;
			gbc.insets = new Insets(0, 4, 0, 0);
			gridBag.setConstraints(ptLabel, gbc);
			sizePanel.add(ptLabel);
		}


		//----  Outer panel

		JPanel outerPanel = new JPanel(gridBag);
		outerPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.NORTH;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(0, 0, 0, 0);
		gridBag.setConstraints(controlPanel, gbc);
		outerPanel.add(controlPanel);

		return outerPanel;
	}

	//------------------------------------------------------------------

	private void setFocus(Tab        tab,
						  JComponent component)
	{
		tabbedPanel.setSelectedIndex(tab.ordinal());
		GuiUtils.setFocus(component);
	}

	//------------------------------------------------------------------

	private void validatePreferencesGeneral()
		throws AppException
	{
		// Filename suffix
		if (filenameSuffixField.isEmpty())
		{
			setFocus(Tab.GENERAL, filenameSuffixField);
			throw new AppException(ErrorId.NO_FILENAME_SUFFIX);
		}
	}

	//------------------------------------------------------------------

	private void validatePreferencesAppearance()
	{
		// do nothing
	}

	//------------------------------------------------------------------

	private void validatePreferencesView()
	{
		// do nothing
	}

	//------------------------------------------------------------------

	private void validatePreferencesGrid()
	{
		// do nothing
	}

	//------------------------------------------------------------------

	private void validatePreferencesClues()
	{
		// do nothing
	}

	//------------------------------------------------------------------

	private void validatePreferencesTextSections()
		throws AppException
	{
		// Line break
		if (lineBreakField.isEmpty())
		{
			setFocus(Tab.TEXT_SECTIONS, lineBreakField);
			throw new AppException(ErrorId.NO_LINE_BREAK_SEQUENCE);
		}
	}

	//------------------------------------------------------------------

	private void validatePreferencesHtml()
	{
		// do nothing
	}

	//------------------------------------------------------------------

	private void validatePreferencesFiles()
		throws AppException
	{
		// Default search parameters file
		try
		{
			if (!parameterSetPathnameField.isEmpty())
			{
				File file = parameterSetPathnameField.getFile();
				try
				{
					if (!file.exists() && !file.isFile())
						throw new FileException(ErrorId.NOT_A_FILE, file);
				}
				catch (SecurityException e)
				{
					throw new FileException(ErrorId.FILE_ACCESS_NOT_PERMITTED, file, e);
				}
			}
		}
		catch (AppException e)
		{
			setFocus(Tab.FILES, parameterSetPathnameField);
			throw e;
		}
	}

	//------------------------------------------------------------------

	private void validatePreferencesFonts()
	{
		// do nothing
	}

	//------------------------------------------------------------------

	private void setPreferencesGeneral()
	{
		AppConfig config = AppConfig.INSTANCE;
		config.setShowUnixPathnames(showUnixPathnamesComboBox.getSelectedValue());
		config.setSelectTextOnFocusGained(selectTextOnFocusGainedComboBox.getSelectedValue());
		if (saveMainWindowLocationComboBox.getSelectedValue() != config.isMainWindowLocation())
			config.setMainWindowLocation(saveMainWindowLocationComboBox.getSelectedValue() ? new Point() : null);
		config.setMaxEditListLength(maxEditListLengthSpinner.getIntValue());
		config.setClearEditListOnSave(clearEditListOnSaveComboBox.getSelectedValue());
		config.setFilenameSuffix(filenameSuffixField.getText());
	}

	//------------------------------------------------------------------

	private void setPreferencesAppearance()
	{
		AppConfig config = AppConfig.INSTANCE;
		if (lookAndFeelComboBox.isEnabled() && (lookAndFeelComboBox.getSelectedIndex() >= 0))
			config.setLookAndFeel(lookAndFeelComboBox.getSelectedValue());
		config.setTextAntialiasing(textAntialiasingComboBox.getSelectedValue());
		config.setStatusTextColour(statusTextColourButton.getForeground());
	}

	//------------------------------------------------------------------

	private void setPreferencesView()
	{
		AppConfig config = AppConfig.INSTANCE;
		config.setSelectedClueNumColumns(selClueNumColumnsSpinner.getIntValue());
		for (CrosswordView.Colour key : viewColours.keySet())
			config.setViewColour(key, viewColours.get(key));
	}

	//------------------------------------------------------------------

	private void setPreferencesGrid()
	{
		AppConfig config = AppConfig.INSTANCE;
		config.setGridEntryCharacters(gridEntryCharsField.getText());
		config.setNavigateOverGridSeparators(navigateOverSeparatorsComboBox.getSelectedValue());
		config.setGridImageViewportSize(gridImageViewportSizePanel.getDimensions());
		for (Grid.Separator separator : gridCellSizeSpinners.keySet())
			config.setGridCellSize(separator, gridCellSizeSpinners.get(separator).getIntValue());
		config.setBarGridBarWidth(gridBarWidthSpinner.getIntValue());
	}

	//------------------------------------------------------------------

	private void setPreferencesClues()
	{
		AppConfig config = AppConfig.INSTANCE;
		for (Direction direction : clueDirectionKeywordsFields.keySet())
		{
			StringList names = clueDirectionKeywordsFields.get(direction).strings;
			config.setClueDirectionKeywords(direction,
											names.isEmpty() ? new StringList(direction.getKeywords()) : names);
		}
		config.setClueReferenceKeyword(clueReferenceKeywordField.isEmpty() ? null
																		   : clueReferenceKeywordField.getText());
		config.setImplicitFieldDirection(implicitFieldDirectionComboBox.getSelectedValue());
		config.setAllowMultipleFieldUse(allowMultipleFieldUseComboBox.getSelectedValue());
	}

	//------------------------------------------------------------------

	private void setPreferencesTextSections()
	{
		AppConfig config = AppConfig.INSTANCE;
		config.setTextSectionLineBreak(lineBreakField.getText());
	}

	//------------------------------------------------------------------

	private void setPreferencesHtml()
	{
		AppConfig config = AppConfig.INSTANCE;
		config.setHtmlViewerCommand(htmlViewerCommandField.isEmpty() ? null : htmlViewerCommandField.getText());
		config.setHtmlFontNames(htmlFontNamesField.strings);
		config.setHtmlFontSize(htmlFontSizeSpinner.getIntValue());
		config.setHtmlFieldNumFontSizeFactor(htmlFieldNumFontSizeFactorSpinner.getDoubleValue());
		config.setHtmlFieldNumOffsetTop(htmlFieldNumOffsetTopSpinner.getIntValue());
		config.setHtmlFieldNumOffsetLeft(htmlFieldNumOffsetLeftSpinner.getIntValue());
		config.setHtmlCellOffsetTop(htmlCellOffsetTopSpinner.getIntValue());
		config.setHtmlCellOffsetLeft(htmlCellOffsetLeftSpinner.getIntValue());
		config.setHtmlGridColour(htmlGridColourButton.getForeground());
		config.setHtmlEntryColour(htmlEntryColourButton.getForeground());
		for (Grid.Separator separator : htmlCellSizeSpinners.keySet())
			config.setHtmlCellSize(separator, htmlCellSizeSpinners.get(separator).getIntValue());
		config.setBlockImageNumLines(blockImageNumLinesSpinner.getIntValue());
		config.setBlockImageLineWidth(blockImageLineWidthSpinner.getDoubleValue());
		config.setBlockImageColour(blockImageColourButton.getForeground());
		config.setBlockImagePrintOnly(blockImagePrintOnlyComboBox.getSelectedValue());
		config.setHtmlBarGridBarWidth(htmlBarWidthSpinner.getIntValue());
		config.setHtmlBarColour(htmlBarColourButton.getForeground());
	}

	//------------------------------------------------------------------

	private void setPreferencesFiles()
	{
		AppConfig config = AppConfig.INSTANCE;
		config.setParameterSetPathname(parameterSetPathnameField.isEmpty() ? null
																		   : parameterSetPathnameField.getText());
	}

	//------------------------------------------------------------------

	private void setPreferencesFonts()
	{
		for (int i = 0; i < fontPanels.length; i++)
		{
			if (fontPanels[i].nameComboBox.getSelectedIndex() >= 0)
				AppConfig.INSTANCE.setFont(i, fontPanels[i].getFont());
		}
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class variables
////////////////////////////////////////////////////////////////////////

	private static	Point	location;
	private static	int		tabIndex;

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	// Main panel
	private	boolean									accepted;
	private	JTabbedPane								tabbedPanel;

	// General panel
	private	BooleanComboBox							showUnixPathnamesComboBox;
	private	BooleanComboBox							selectTextOnFocusGainedComboBox;
	private	BooleanComboBox							saveMainWindowLocationComboBox;
	private	FIntegerSpinner							maxEditListLengthSpinner;
	private	BooleanComboBox							clearEditListOnSaveComboBox;

	// Appearance panel
	private	FComboBox<String>						lookAndFeelComboBox;
	private	FComboBox<TextRendering.Antialiasing>	textAntialiasingComboBox;
	private	ColourButton							statusTextColourButton;

	// View panel
	private	FIntegerSpinner							selClueNumColumnsSpinner;
	private	Map<CrosswordView.Colour, Color>		viewColours;
	private	SelectionList<CrosswordView.Colour>		viewColoursList;
	private	ColourButton							viewColourButton;

	// Grid panel
	private	EntryCharsField							gridEntryCharsField;
	private	BooleanComboBox							navigateOverSeparatorsComboBox;
	private	DimensionsSpinnerPanel					gridImageViewportSizePanel;
	private	Map<Grid.Separator, FIntegerSpinner>	gridCellSizeSpinners;
	private	FIntegerSpinner							gridBarWidthSpinner;

	// Clues panel
	private	Map<Direction, StringListField>			clueDirectionKeywordsFields;
	private	FTextField								clueReferenceKeywordField;
	private	BooleanComboBox							implicitFieldDirectionComboBox;
	private	BooleanComboBox							allowMultipleFieldUseComboBox;

	// Text sections panel
	private	FTextField								lineBreakField;

	// HTML panel
	private	FTextField								htmlViewerCommandField;
	private	StringListField							htmlFontNamesField;
	private	FIntegerSpinner							htmlFontSizeSpinner;
	private	FDoubleSpinner							htmlFieldNumFontSizeFactorSpinner;
	private	FIntegerSpinner							htmlFieldNumOffsetTopSpinner;
	private	FIntegerSpinner							htmlFieldNumOffsetLeftSpinner;
	private	FIntegerSpinner							htmlCellOffsetTopSpinner;
	private	FIntegerSpinner							htmlCellOffsetLeftSpinner;
	private	ColourButton							htmlGridColourButton;
	private	ColourButton							htmlEntryColourButton;
	private	Map<Grid.Separator, FIntegerSpinner>	htmlCellSizeSpinners;
	private	FIntegerSpinner							blockImageNumLinesSpinner;
	private	FDoubleSpinner							blockImageLineWidthSpinner;
	private	ColourButton							blockImageColourButton;
	private	BooleanComboBox							blockImagePrintOnlyComboBox;
	private	ImagePanel								blockImagePanel;
	private	FIntegerSpinner							htmlBarWidthSpinner;
	private	ColourButton							htmlBarColourButton;

	// Files panel
	private	FilenameSuffixField						filenameSuffixField;
	private	FPathnameField							parameterSetPathnameField;
	private	JFileChooser							parameterSetFileChooser;

	// Fonts panel
	private	FontPanel[]								fontPanels;

}

//----------------------------------------------------------------------
