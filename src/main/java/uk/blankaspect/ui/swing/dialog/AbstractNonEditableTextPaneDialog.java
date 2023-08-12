/*====================================================================*\

AbstractNonEditableTextPaneDialog.java

Abstract non-editable text pane dialog class.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.ui.swing.dialog;

//----------------------------------------------------------------------


// IMPORTS


import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Window;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.Style;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

import uk.blankaspect.common.exception.UnexpectedRuntimeException;

import uk.blankaspect.ui.swing.button.FButton;

import uk.blankaspect.ui.swing.font.FontKey;
import uk.blankaspect.ui.swing.font.FontUtils;

import uk.blankaspect.ui.swing.misc.GuiUtils;

//----------------------------------------------------------------------


// ABSTRACT NON-EDITABLE TEXT PANE DIALOG CLASS


public abstract class AbstractNonEditableTextPaneDialog
	extends JDialog
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	private static final	int	TEXT_PANE_VERTICAL_MARGIN	= 2;
	private static final	int	TEXT_PANE_HORIZONTAL_MARGIN	= 4;

	private static final	int	BUTTON_GAP	= 16;

////////////////////////////////////////////////////////////////////////
//  Member classes : non-inner classes
////////////////////////////////////////////////////////////////////////


	// SPAN CLASS


	public static class Span
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		public Span(String text)
		{
			this(text, null);
		}

		//--------------------------------------------------------------

		public Span(String text,
					String styleKey)
		{
			this.text = text;
			this.styleKey = styleKey;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	String	text;
		private	String	styleKey;

	}

	//==================================================================


	// PARAGRAPH CLASS


	public static class Paragraph
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		public Paragraph()
		{
			spans = new ArrayList<>();
		}

		//--------------------------------------------------------------

		public Paragraph(String styleKey)
		{
			this();
			this.styleKey = styleKey;
		}

		//--------------------------------------------------------------

		public Paragraph(Collection<Span> spans,
						 String           styleKey)
		{
			this();
			this.spans.addAll(spans);
			this.styleKey = styleKey;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		public void setStyle(String key)
		{
			styleKey = key;
		}

		//--------------------------------------------------------------

		public void add(Span span)
		{
			spans.add(span);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	List<Span>	spans;
		private	String		styleKey;

	}

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	protected AbstractNonEditableTextPaneDialog(Window       owner,
												String       titleStr,
												String       key,
												int          numColumns,
												int          numRows,
												List<Action> commands,
												String       defaultButtonKey)
	{

		// Call superclass constructor
		super(owner, titleStr, Dialog.ModalityType.APPLICATION_MODAL);

		// Set icons
		if (owner != null)
			setIconImages(owner.getIconImages());

		// Initialise instance variables
		this.key = key;


		//----  Text pane scroll pane

		// Text pane
		textPane = new JTextPane(new DefaultStyledDocument(new StyleContext()));
		String fontKey = FontKey.TEXT_AREA;
		if (!FontUtils.isAppFont(fontKey))
			fontKey = FontKey.TEXT_FIELD;
		FontUtils.setAppFont(fontKey, textPane);
		textPane.setBorder(null);
		textPane.setEditable(false);
		textPane.getStyledDocument().putProperty(DefaultEditorKit.EndOfLineStringProperty, "\n");

		// Set text pane attributes
		setTextPaneAttributes();

		// Wrap text pane in a panel to prevent text wrapping
		JPanel textPanePanel = new JPanel(new BorderLayout());
		textPanePanel.add(textPane);

		// Scroll pane: text pane
		JScrollPane textPaneScrollPane = new JScrollPane(textPanePanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
														 JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		FontMetrics fontMetrics = textPane.getFontMetrics(textPane.getFont());
		int width = numColumns * FontUtils.getCharWidth('0', fontMetrics);
		int height = numRows * fontMetrics.getHeight();
		textPaneScrollPane.getViewport().setPreferredSize(new Dimension(width, height));
		textPaneScrollPane.setViewportBorder(BorderFactory.createMatteBorder(TEXT_PANE_VERTICAL_MARGIN,
																			 TEXT_PANE_HORIZONTAL_MARGIN,
																			 TEXT_PANE_VERTICAL_MARGIN,
																			 TEXT_PANE_HORIZONTAL_MARGIN,
																			 textPane.getBackground()));


		//----  Button panel

		JPanel buttonPanel = new JPanel(new GridLayout(1, 0, BUTTON_GAP, 0));
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));

		// Buttons
		buttons = new HashMap<>();
		for (Action command : commands)
		{
			JButton button = new FButton(command);
			buttons.put((String)command.getValue(Action.ACTION_COMMAND_KEY), button);
			buttonPanel.add(button);
		}


		//----  Main panel

		GridBagLayout gridBag = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();

		JPanel mainPanel = new JPanel(gridBag);

		int gridY = 0;

		gbc.gridx = 0;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.NORTH;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(1, 0, 0, 0);
		gridBag.setConstraints(textPaneScrollPane, gbc);
		mainPanel.add(textPaneScrollPane);

		gbc.gridx = 0;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.NORTH;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(3, 0, 2, 0);
		gridBag.setConstraints(buttonPanel, gbc);
		mainPanel.add(buttonPanel);


		//----  Window

		// Set content pane
		setContentPane(mainPanel);

		// Update components
		updateComponents();

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
		Point location = locations.get(key);
		if (location == null)
			location = GuiUtils.getComponentLocation(this, owner);
		setLocation(location);

		// Set default button
		if (defaultButtonKey != null)
			getRootPane().setDefaultButton(getButton(defaultButtonKey));

	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	public Style getDefaultStyle()
	{
		return textPane.getStyledDocument().getStyle(StyleContext.DEFAULT_STYLE);
	}

	//------------------------------------------------------------------

	public Style addStyle(String key)
	{
		return addStyle(key, null);
	}

	//------------------------------------------------------------------

	public Style addStyle(String key,
						  Style  parentStyle)
	{
		return textPane.getStyledDocument().addStyle(key, parentStyle);
	}

	//------------------------------------------------------------------

	public void append(String text)
	{
		append(Collections.singletonList(new Span(text)));
	}

	//------------------------------------------------------------------

	public void append(String text,
					   String styleKey)
	{
		append(Collections.singletonList(new Span(text, styleKey)));
	}

	//------------------------------------------------------------------

	public void append(Span span)
	{
		append(Collections.singletonList(span));
	}

	//------------------------------------------------------------------

	public void append(Iterable<Span> spans)
	{
		try
		{
			StyledDocument document = textPane.getStyledDocument();
			for (Span span : spans)
				document.insertString(document.getLength(), span.text,
									  (span.styleKey == null) ? SimpleAttributeSet.EMPTY
															  : document.getStyle(span.styleKey));
		}
		catch (BadLocationException e)
		{
			throw new UnexpectedRuntimeException(e);
		}
	}

	//------------------------------------------------------------------

	public void append(Paragraph paragraph)
	{
		try
		{
			// Appends spans
			append(paragraph.spans);

			// Set paragraph attributes
			StyledDocument document = textPane.getStyledDocument();
			int offset = document.getLength();
			if (paragraph.styleKey != null)
			{
				int startOffset = document.getParagraphElement(offset).getStartOffset();
				document.setParagraphAttributes(startOffset, offset - startOffset,
												document.getStyle(paragraph.styleKey), true);
			}

			// Append LF
			document.insertString(offset, "\n", SimpleAttributeSet.EMPTY);
		}
		catch (BadLocationException e)
		{
			throw new UnexpectedRuntimeException(e);
		}
		finally
		{
			updateComponents();
		}
	}

	//------------------------------------------------------------------

	public void setCaretToStart()
	{
		textPane.setCaretPosition(0);
	}

	//------------------------------------------------------------------

	public void setCaretToEnd()
	{
		textPane.setCaretPosition(getTextLength());
	}

	//------------------------------------------------------------------

	protected int getTextLength()
	{
		return textPane.getDocument().getLength();
	}

	//------------------------------------------------------------------

	protected String getText()
	{
		return textPane.getText();
	}

	//------------------------------------------------------------------

	protected JTextPane getTextPane()
	{
		return textPane;
	}

	//------------------------------------------------------------------

	protected JButton getButton(String key)
	{
		return buttons.get(key);
	}

	//------------------------------------------------------------------

	protected void setTextPaneAttributes()
	{
		// do nothing
	}

	//------------------------------------------------------------------

	protected void updateComponents()
	{
		// do nothing
	}

	//------------------------------------------------------------------

	protected void onClose()
	{
		locations.put(key, getLocation());
		setVisible(false);
		dispose();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class variables
////////////////////////////////////////////////////////////////////////

	private static	Map<String, Point>	locations	= new Hashtable<>();

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private	String					key;
	private	JTextPane				textPane;
	private	Map<String, JButton>	buttons;

}

//----------------------------------------------------------------------
