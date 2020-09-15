/*====================================================================*\

ClueDialog.java

Clue dialog box class.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.crosswordeditor;

//----------------------------------------------------------------------


// IMPORTS


import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.Window;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import uk.blankaspect.common.exception.AppException;
import uk.blankaspect.common.exception.UnexpectedRuntimeException;

import uk.blankaspect.common.number.NumberUtils;

import uk.blankaspect.common.string.StringUtils;

import uk.blankaspect.common.swing.action.KeyAction;

import uk.blankaspect.common.swing.button.FButton;

import uk.blankaspect.common.swing.misc.GuiUtils;

import uk.blankaspect.common.swing.text.TextRendering;

//----------------------------------------------------------------------


// CLUE DIALOG BOX CLASS


class ClueDialog
	extends JDialog
	implements ActionListener
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	private static final	int	TEXT_AREA_NUM_ROWS	= 4;

	private static final	String	TITLE_STR	= "Clue - ";

	// Commands
	private interface Command
	{
		String	ACCEPT	= "accept";
		String	CLOSE	= "close";
	}

////////////////////////////////////////////////////////////////////////
//  Member classes : non-inner classes
////////////////////////////////////////////////////////////////////////


	// FIELD CLASS


	public static class Field
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		public static final	String	DEFINED_PREFIX	= "* ";

		public static final	Grid.Field.Id	NO_ID	= new Grid.Field.Id(0);

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		public Field(Grid.Field.Id id)
		{
			this.id = id;
		}

		//--------------------------------------------------------------

		private Field(Grid.Field.Id id,
					  int           length,
					  boolean       defined)
		{
			this.id = id;
			this.length = length;
			this.defined = defined;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		public boolean equals(Object obj)
		{
			return ((obj instanceof Field) && id.equals(((Field)obj).id));
		}

		//--------------------------------------------------------------

		@Override
		public int hashCode()
		{
			return id.hashCode();
		}

		//--------------------------------------------------------------

		@Override
		public String toString()
		{
			String str = id.toString();
			return (defined ? DEFINED_PREFIX + str : str);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		Grid.Field.Id	id;
		int				length;
		boolean			defined;

	}

	//==================================================================


	// FIELD ID PANEL CLASS


	private static class IdPanel
		extends JComponent
		implements ActionListener, FocusListener, MouseListener
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		private static final	int	VERTICAL_MARGIN		= 2;
		private static final	int	HORIZONTAL_MARGIN	= 5;
		private static final	int	SEPARATOR_WIDTH		= 1;

		private static final	int	NUM_VIEWABLE_IDS	= 8;

		private static final	Color	BACKGROUND_COLOUR			= new Color(248, 240, 216);
		private static final	Color	SELECTING_BACKGROUND_COLOUR	= new Color(224, 232, 224);
		private static final	Color	ID_TEXT_COLOUR				= Color.BLACK;
		private static final	Color	LENGTH_TEXT_COLOUR			= new Color(192, 64, 0);
		private static final	Color	BORDER_COLOUR				= new Color(192, 184, 160);
		private static final	Color	FOCUSED_BORDER_COLOUR		= Color.BLACK;
		private static final	Color	SELECTING_BORDER_COLOUR		= new Color(64, 80, 64);

		// Commands
		private interface Command
		{
			String	SELECT_PREVIOUS_ID	= "selectPreviousId";
			String	SELECT_NEXT_ID		= "selectNextId";
			String	SELECT_FIRST_ID		= "selectFirstId";
			String	SELECT_LAST_ID		= "selectLastId";
			String	EDIT_ID				= "editId";
		}

		private static final	KeyAction.KeyCommandPair[]	KEY_COMMANDS	=
		{
			new KeyAction.KeyCommandPair
			(
				KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0),
				Command.SELECT_PREVIOUS_ID
			),
			new KeyAction.KeyCommandPair
			(
				KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0),
				Command.SELECT_NEXT_ID
			),
			new KeyAction.KeyCommandPair
			(
				KeyStroke.getKeyStroke(KeyEvent.VK_HOME, 0),
				Command.SELECT_FIRST_ID
			),
			new KeyAction.KeyCommandPair
			(
				KeyStroke.getKeyStroke(KeyEvent.VK_END, 0),
				Command.SELECT_LAST_ID
			),
			new KeyAction.KeyCommandPair
			(
				KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0),
				Command.EDIT_ID
			)
		};

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private IdPanel(ClueDialog                  dialog,
						List<Field>                 clueFields,
						 Map<Direction, List<Field>> availableFields,
						 int                         maxNumDigits)
		{
			// Initialise instance variables
			this.dialog = dialog;
			this.clueFields = clueFields;
			this.availableFields = availableFields;
			selectedIndex = 1;

			// Set font
			AppFont.MAIN.apply(this);

			// Set preferred dimensions
			FontMetrics fontMetrics = getFontMetrics(getFont());
			int textWidth = 0;
			for (Direction direction : Direction.DEFINED_DIRECTIONS)
			{
				int width = fontMetrics.stringWidth(direction.getSuffix());
				if (textWidth < width)
					textWidth = width;
			}
			if (maxNumDigits < 2)
				maxNumDigits = 2;
			textWidth += fontMetrics.stringWidth(StringUtils.createCharString('0', maxNumDigits));
			textWidth += fontMetrics.stringWidth(Field.DEFINED_PREFIX);
			cellWidth = 2 * HORIZONTAL_MARGIN + textWidth + SEPARATOR_WIDTH;
			preferredWidth = NUM_VIEWABLE_IDS * cellWidth + 1;
			preferredHeight = 2 * VERTICAL_MARGIN + fontMetrics.getAscent() + fontMetrics.getDescent() +
																				fontMetrics.getHeight();

			// Set attributes
			setOpaque(true);
			setFocusable(true);

			// Add listeners
			addFocusListener(this);
			addMouseListener(this);

			// Add commands to action map
			KeyAction.create(this, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, this, KEY_COMMANDS);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : ActionListener interface
	////////////////////////////////////////////////////////////////////

		public void actionPerformed(ActionEvent event)
		{
			String command = event.getActionCommand();

			if (command.equals(Command.SELECT_PREVIOUS_ID))
				onSelectPreviousId();

			else if (command.equals(Command.SELECT_NEXT_ID))
				onSelectNextId();

			else if (command.equals(Command.SELECT_FIRST_ID))
				onSelectFirstId();

			else if (command.equals(Command.SELECT_LAST_ID))
				onSelectLastId();

			else if (command.equals(Command.EDIT_ID))
				onEditId();
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : FocusListener interface
	////////////////////////////////////////////////////////////////////

		public void focusGained(FocusEvent event)
		{
			repaint();
		}

		//--------------------------------------------------------------

		public void focusLost(FocusEvent event)
		{
			repaint();
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : MouseListener interface
	////////////////////////////////////////////////////////////////////

		public void mouseClicked(MouseEvent event)
		{
			if (SwingUtilities.isLeftMouseButton(event) && (event.getClickCount() > 1))
			{
				if (event.getX() / cellWidth > 0)
					onEditId();
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
			requestFocusInWindow();

			if (SwingUtilities.isLeftMouseButton(event))
			{
				int index = event.getX() / cellWidth;
				if (index > 0)
				{
					int maxIndex = getMaxCellIndex();
					if (index > maxIndex)
						index = maxIndex;
					if (selectedIndex != index)
					{
						selectedIndex = index;
						repaint();
					}
				}
			}
		}

		//--------------------------------------------------------------

		public void mouseReleased(MouseEvent event)
		{
			// do nothing
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		public Dimension getPreferredSize()
		{
			return new Dimension(preferredWidth, preferredHeight);
		}

		//--------------------------------------------------------------

		@Override
		protected void paintComponent(Graphics gr)
		{
			// Create copy of graphics context
			gr = gr.create();

			// Get dimensions
			int width = getWidth();
			int height = getHeight();

			// Fill background
			Rectangle rect = gr.getClipBounds();
			gr.setColor(getBackground());
			gr.fillRect(rect.x, rect.y, rect.width, rect.height);

			// Fill background of cells
			gr.setColor(selectingField ? SELECTING_BACKGROUND_COLOUR : BACKGROUND_COLOUR);
			gr.fillRect(1, 1, clueFields.size() * cellWidth - 1, height - 2);

			// Set rendering hints for text antialiasing and fractional metrics
			TextRendering.setHints((Graphics2D)gr);

			// Draw IDs
			FontMetrics fontMetrics = gr.getFontMetrics();
			int x = 0;
			int index = 0;
			while (index < clueFields.size())
			{
				Field field = clueFields.get(index);

				// Draw ID
				String str = field.toString();
				int strWidth = fontMetrics.stringWidth(str);
				int y = VERTICAL_MARGIN + fontMetrics.getAscent();
				gr.setColor(ID_TEXT_COLOUR);
				gr.drawString(str, x + cellWidth - HORIZONTAL_MARGIN - strWidth, y);

				// Draw field length
				str = Integer.toString(field.length);
				strWidth = fontMetrics.stringWidth(str);
				y += fontMetrics.getHeight();
				gr.setColor(LENGTH_TEXT_COLOUR);
				gr.drawString(str, x + cellWidth - HORIZONTAL_MARGIN - strWidth, y);

				// Increment index and x coordinate
				++index;
				x += cellWidth;
				if (x >= width)
					break;

				// Draw separator
				gr.setColor(BORDER_COLOUR);
				gr.drawLine(x, 0, x, height - 1);
			}

			// Draw selection indicator
			x = selectedIndex * cellWidth + 1;
			int boxWidth = ((selectedIndex == clueFields.size()) ? width - x : cellWidth) - 2;
			if (isFocusOwner())
			{
				Graphics2D gr2d = (Graphics2D)gr;
				Stroke oldStroke = gr2d.getStroke();
				gr2d.setStroke(GuiUtils.getBasicDash());
				gr2d.setColor(FOCUSED_BORDER_COLOUR);
				gr2d.drawRect(x, 1, boxWidth, height - 3);
				gr2d.setStroke(oldStroke);
			}
			else if (selectingField)
			{
				gr.setColor(SELECTING_BORDER_COLOUR);
				gr.drawRect(x, 1, boxWidth, height - 3);
			}

			// Draw border
			gr.setColor(BORDER_COLOUR);
			gr.drawRect(0, 0, width - 1, height - 1);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		private int getMaxCellIndex()
		{
			return Math.min(getWidth() / cellWidth - 1, clueFields.size());
		}

		//--------------------------------------------------------------

		private List<Grid.Field.Id> getIds()
		{
			List<Grid.Field.Id> ids = new ArrayList<>();
			for (Field field : clueFields)
				ids.add(field.id);
			return ids;
		}

		//--------------------------------------------------------------

		private int getLength()
		{
			int length = 0;
			for (Field field : clueFields)
				length += field.length;
			return length;
		}

		//--------------------------------------------------------------

		private void onSelectPreviousId()
		{
			if (selectedIndex > 1)
			{
				--selectedIndex;
				repaint();
			}
		}

		//--------------------------------------------------------------

		private void onSelectNextId()
		{
			if (selectedIndex < getMaxCellIndex())
			{
				++selectedIndex;
				repaint();
			}
		}

		//--------------------------------------------------------------

		private void onSelectFirstId()
		{
			if (selectedIndex > 1)
			{
				selectedIndex = 1;
				repaint();
			}
		}

		//--------------------------------------------------------------

		private void onSelectLastId()
		{
			int maxIndex = getMaxCellIndex();
			if (selectedIndex < maxIndex)
			{
				selectedIndex = maxIndex;
				repaint();
			}
		}

		//--------------------------------------------------------------

		private void onEditId()
		{
			// Create a copy of the map of available fields with current clue fields removed
			Map<Direction, List<Field>> fieldMap = new EnumMap<>(Direction.class);
			for (Direction direction : availableFields.keySet())
			{
				List<Field> fields = new ArrayList<>(availableFields.get(direction));
				for (int i = 0; i < clueFields.size(); i++)
				{
					if (i != selectedIndex)
					{
						Field field = clueFields.get(i);
						if (field.id.direction == direction)
							fields.remove(field);
					}
				}
				fieldMap.put(direction, fields);
			}

			// Redraw panel
			selectingField = true;
			repaint();

			// Select field
			dialog.setButtonsEnabled(false);
			Grid.Field.Id fieldId = (selectedIndex < clueFields.size())
																	? clueFields.get(selectedIndex).id
																	: Field.NO_ID;
			Grid.Field.Id newFieldId = FieldSelectionDialog.showDialog(this, selectedIndex * cellWidth,
																	   fieldMap, fieldId);
			dialog.setButtonsEnabled(true);

			// Update list of clue fields
			if ((newFieldId != null) && !newFieldId.equals(fieldId))
			{
				if (newFieldId.number == 0)
					clueFields.remove(selectedIndex);
				else
				{
					List<Field> fields = availableFields.get(newFieldId.direction);
					Field field = fields.get(fields.indexOf(new Field(newFieldId)));
					if (selectedIndex < clueFields.size())
						clueFields.set(selectedIndex, field);
					else
						clueFields.add(field);
				}
			}

			// Redraw panel
			selectingField = false;
			repaint();
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	ClueDialog					dialog;
		private	List<Field>					clueFields;
		private	Map<Direction, List<Field>>	availableFields;
		private	int							selectedIndex;
		private	boolean						selectingField;
		private	int							cellWidth;
		private	int							preferredWidth;
		private	int							preferredHeight;

	}

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	private ClueDialog(Window            owner,
					   CrosswordDocument document,
					   Clue              clue)
	{

		// Call superclass constructor
		super(owner, TITLE_STR + clue.getFieldId(), Dialog.ModalityType.APPLICATION_MODAL);

		// Set icons
		setIconImages(owner.getIconImages());

		// Initialise instance variables
		this.clueReferenceKeyword = document.getClueReferenceKeyword();
		clueIndex = clue.getId().index;


		//----  Field ID panel

		List<Field> clueFields = new ArrayList<>();
		for (int i = 0; i < clue.getNumFields(); i++)
		{
			Grid.Field.Id fieldId = clue.getFieldId(i);
			int length = document.getGrid().getField(fieldId).getLength();
			List<Clue> clues = document.findClues(fieldId);
			boolean defined = false;
			if (i == 0)
				defined = (clues.size() > 1) ||
								((clues.size() == 1) && !clues.get(0).getFieldId().equals(fieldId));
			else
			{
				for (Clue c : clues)
				{
					if (!c.isReference())
					{
						defined = true;
						break;
					}
				}
			}
			clueFields.add(new Field(fieldId, length, defined));
		}

		boolean allowMultipleFieldUse = AppConfig.INSTANCE.isAllowMultipleFieldUse();
		Grid.Field.Id clueFieldId = clue.getFieldId();
		Map<Direction, List<Field>> availableFields = new EnumMap<>(Direction.class);
		for (Direction direction : Direction.DEFINED_DIRECTIONS)
		{
			List<Field> fields = new ArrayList<>();
			availableFields.put(direction, fields);

			List<Clue> clues = document.getClues(direction);
			for (Grid.Field field : document.getGrid().getFields(direction))
			{
				Grid.Field.Id fieldId = field.getId();
				int length = field.getLength();
				int index = Collections.binarySearch(clues, new Clue(fieldId), Clue.FieldIdComparator.INSTANCE);
				if (index < 0)
					fields.add(new Field(fieldId, length, false));
				else
				{
					Clue.Id refId = clues.get(index).getReferentId();
					if (refId == null)
					{
						if (allowMultipleFieldUse && !fieldId.equals(clueFieldId))
							fields.add(new Field(fieldId, length, true));
					}
					else
					{
						if (refId.fieldId.equals(clueFieldId))
							fields.add(new Field(fieldId, length, false));
					}
				}
			}
		}

		int maxNumDigits = 0;
		for (Grid.Field field : document.getGrid().getFields())
		{
			int numDigits = NumberUtils.getNumDecDigitsInt(field.getNumber());
			if (maxNumDigits < numDigits)
				maxNumDigits = numDigits;
		}

		idPanel = new IdPanel(this, clueFields, availableFields, maxNumDigits);


		//----  Text panel

		textPanel = new TextPanel(TEXT_AREA_NUM_ROWS, clue.hasText() ? clue.getText().toString() : null, true);


		//----  Button panel

		buttonPanel = new JPanel(new GridLayout(1, 0, 8, 0));
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(3, 12, 3, 12));

		// Button: OK
		JButton okButton = new FButton(AppConstants.OK_STR);
		okButton.setActionCommand(Command.ACCEPT);
		okButton.addActionListener(this);
		buttonPanel.add(okButton);

		// Button: cancel
		JButton cancelButton = new FButton(AppConstants.CANCEL_STR);
		cancelButton.setActionCommand(Command.CLOSE);
		cancelButton.addActionListener(this);
		buttonPanel.add(cancelButton);


		//----  Main panel

		GridBagLayout gridBag = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();

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
		gridBag.setConstraints(idPanel, gbc);
		mainPanel.add(idPanel);

		gbc.gridx = 0;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.NORTH;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(3, 0, 0, 0);
		gridBag.setConstraints(textPanel, gbc);
		mainPanel.add(textPanel);

		gbc.gridx = 0;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.NORTH;
		gbc.fill = GridBagConstraints.NONE;
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

		// Set focus
		textPanel.requestFocusInWindow();

		// Show dialog
		setVisible(true);

	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	public static Clue showDialog(Component         parent,
								  CrosswordDocument document,
								  Clue              clue)
	{
		return new ClueDialog(GuiUtils.getWindow(parent), document, clue).getClue();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : ActionListener interface
////////////////////////////////////////////////////////////////////////

	public void actionPerformed(ActionEvent event)
	{
		String command = event.getActionCommand();

		if (command.equals(Command.ACCEPT))
			onAccept();

		else if (command.equals(Command.CLOSE))
			onClose();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	private Clue getClue()
	{
		Clue clue = null;
		if (accepted)
		{
			try
			{
				clue = textPanel.isEmpty()
								? new Clue(idPanel.clueFields.get(0).id)
								: new Clue(idPanel.getIds(), textPanel.getText(), clueReferenceKeyword,
										   idPanel.getLength());
				clue.setIndex(clueIndex);
			}
			catch (StyledText.ParseException e)
			{
				throw new UnexpectedRuntimeException();
			}
		}
		return clue;
	}

	//------------------------------------------------------------------

	private void setButtonsEnabled(boolean enabled)
	{
		for (Component component : buttonPanel.getComponents())
			component.setEnabled(enabled);
	}

	//------------------------------------------------------------------

	private void validateUserInput()
		throws AppException
	{
		textPanel.getText();
	}

	//------------------------------------------------------------------

	private void onAccept()
	{
		try
		{
			validateUserInput();
			accepted = true;
			onClose();
		}
		catch (AppException e)
		{
			JOptionPane.showMessageDialog(this, e, getTitle(), JOptionPane.ERROR_MESSAGE);
		}
	}

	//------------------------------------------------------------------

	private void onClose()
	{
		location = getLocation();
		setVisible(false);
		dispose();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class variables
////////////////////////////////////////////////////////////////////////

	private static	Point	location;

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private	boolean		accepted;
	private	String		clueReferenceKeyword;
	private	int			clueIndex;
	private	IdPanel		idPanel;
	private	TextPanel	textPanel;
	private	JPanel		buttonPanel;

}

//----------------------------------------------------------------------
