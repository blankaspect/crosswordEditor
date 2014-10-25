/*====================================================================*\

TextPanel.java

Text panel class.

\*====================================================================*/


// IMPORTS


import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;

import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentListener;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoableEdit;
import javax.swing.undo.UndoManager;

import uk.org.blankaspect.exception.UnexpectedRuntimeException;

import uk.org.blankaspect.gui.FMenuItem;
import uk.org.blankaspect.gui.FTextArea;
import uk.org.blankaspect.gui.GuiUtilities;

import uk.org.blankaspect.regex.RegexUtilities;

import uk.org.blankaspect.util.InputMapUtilities;
import uk.org.blankaspect.util.KeyAction;
import uk.org.blankaspect.util.ListUtilities;
import uk.org.blankaspect.util.StringUtilities;

//----------------------------------------------------------------------


// TEXT PANEL CLASS


class TextPanel
    extends JPanel
    implements ActionListener, CaretListener, MouseListener, PropertyChangeListener
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

    private static final    int NUM_COLUMNS         = 64;
    private static final    int VERTICAL_MARGIN     = 2;
    private static final    int HORIZONTAL_MARGIN   = 4;

    private static final    String  ANCESTOR_PROPERTY_NAME  = "ancestor";

    private static final    String  UNDO_STR    = "Undo";
    private static final    String  REDO_STR    = "Redo";
    private static final    String  CUT_STR     = "Cut";
    private static final    String  COPY_STR    = "Copy";
    private static final    String  PASTE_STR   = "Paste";

    private static final    Pattern PARAGRAPH_SEPARATOR_PATTERN = Pattern.compile( "\\n[\\s*\\n]+" );

    private static final    KeyStroke   ENTER_KEY   = KeyStroke.getKeyStroke( KeyEvent.VK_ENTER, 0 );

    private static final    Set<KeyStroke>  FOCUS_FORWARD_KEYS;
    private static final    Set<KeyStroke>  FOCUS_BACKWARD_KEYS;

    // Commands
    private interface Command
    {
        String  APPLY_STYLE         = "applyStyle.";
        String  UNDO                = "undo";
        String  REDO                = "redo";
        String  CUT                 = "cut";
        String  COPY                = "copy";
        String  PASTE               = "paste";
        String  SHOW_CONTEXT_MENU   = "showContextMenu";
    }

    private static final    KeyAction.KeyCommandPair[]  KEY_COMMANDS    =
    {
        new KeyAction.KeyCommandPair( KeyStroke.getKeyStroke( KeyEvent.VK_Z, KeyEvent.CTRL_DOWN_MASK ),
                                      Command.UNDO ),
        new KeyAction.KeyCommandPair( KeyStroke.getKeyStroke( KeyEvent.VK_Y, KeyEvent.CTRL_DOWN_MASK ),
                                      Command.REDO ),
        new KeyAction.KeyCommandPair( KeyStroke.getKeyStroke( KeyEvent.VK_CONTEXT_MENU, 0 ),
                                      Command.SHOW_CONTEXT_MENU )
    };

    private static final    Map<String, CommandAction>  COMMANDS;

    // Image data for style-button icons
    private interface ImageData
    {
        byte[]  BOLD    =
        {
            (byte)0x89, (byte)0x50, (byte)0x4E, (byte)0x47, (byte)0x0D, (byte)0x0A, (byte)0x1A, (byte)0x0A,
            (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x0D, (byte)0x49, (byte)0x48, (byte)0x44, (byte)0x52,
            (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x0C, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x0D,
            (byte)0x08, (byte)0x06, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x9D, (byte)0x29, (byte)0x8F,
            (byte)0x42, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0xA6, (byte)0x49, (byte)0x44, (byte)0x41,
            (byte)0x54, (byte)0x78, (byte)0xDA, (byte)0x63, (byte)0xF8, (byte)0xFF, (byte)0xFF, (byte)0x3F,
            (byte)0x03, (byte)0x10, (byte)0x48, (byte)0x00, (byte)0x71, (byte)0x1C, (byte)0x10, (byte)0x4F,
            (byte)0x03, (byte)0xE2, (byte)0x28, (byte)0x90, (byte)0x18, (byte)0x2E, (byte)0xCC, (byte)0x00,
            (byte)0xD5, (byte)0x10, (byte)0x0E, (byte)0xC4, (byte)0xFF, (byte)0xA1, (byte)0x38, (byte)0x8B,
            (byte)0x18, (byte)0x0D, (byte)0xEE, (byte)0x48, (byte)0x1A, (byte)0x22, (byte)0x89, (byte)0xD1,
            (byte)0x60, (byte)0x86, (byte)0xA4, (byte)0xC1, (byte)0x93, (byte)0x18, (byte)0x0D, (byte)0xDA,
            (byte)0x48, (byte)0x1A, (byte)0x5C, (byte)0xA1, (byte)0x62, (byte)0xE2, (byte)0x40, (byte)0xCC,
            (byte)0x8B, (byte)0x4B, (byte)0x83, (byte)0x16, (byte)0x92, (byte)0x86, (byte)0x4E, (byte)0x20,
            (byte)0xBE, (byte)0x82, (byte)0xC4, (byte)0x3F, (byte)0x0A, (byte)0x92, (byte)0xC7, (byte)0xA7,
            (byte)0x61, (byte)0x27, (byte)0x10, (byte)0x1B, (byte)0x02, (byte)0x71, (byte)0x20, (byte)0x10,
            (byte)0x3F, (byte)0x81, (byte)0x8A, (byte)0xDD, (byte)0x01, (byte)0x62, (byte)0x2E, (byte)0x5C,
            (byte)0x1A, (byte)0x5C, (byte)0xE1, (byte)0xA6, (byte)0x31, (byte)0x30, (byte)0xB4, (byte)0x22,
            (byte)0x89, (byte)0x3B, (byte)0x10, (byte)0xA3, (byte)0xC1, (byte)0x07, (byte)0x49, (byte)0x3C,
            (byte)0x82, (byte)0x18, (byte)0x0D, (byte)0x51, (byte)0x48, (byte)0xE2, (byte)0xBA, (byte)0xC8,
            (byte)0x1A, (byte)0x74, (byte)0x09, (byte)0x38, (byte)0xE9, (byte)0x11, (byte)0x10, (byte)0xB3,
            (byte)0x20, (byte)0x6B, (byte)0xB0, (byte)0x40, (byte)0xD2, (byte)0x50, (byte)0x0F, (byte)0xC4,
            (byte)0xFC, (byte)0x40, (byte)0xEC, (byte)0x01, (byte)0xC4, (byte)0x8F, (byte)0x81, (byte)0xF8,
            (byte)0x27, (byte)0x10, (byte)0x9B, (byte)0xA3, (byte)0x87, (byte)0x92, (byte)0x13, (byte)0x10,
            (byte)0x1F, (byte)0x04, (byte)0xE2, (byte)0x45, (byte)0x40, (byte)0x7C, (byte)0x1A, (byte)0x6A,
            (byte)0xE2, (byte)0x5D, (byte)0x20, (byte)0x5E, (byte)0x05, (byte)0xC4, (byte)0x3A, (byte)0xC8,
            (byte)0xF1, (byte)0x00, (byte)0x00, (byte)0x2F, (byte)0xE1, (byte)0xFB, (byte)0xDA, (byte)0x48,
            (byte)0xA5, (byte)0x30, (byte)0xF5, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x49,
            (byte)0x45, (byte)0x4E, (byte)0x44, (byte)0xAE, (byte)0x42, (byte)0x60, (byte)0x82
        };

        byte[]  ITALIC  =
        {
            (byte)0x89, (byte)0x50, (byte)0x4E, (byte)0x47, (byte)0x0D, (byte)0x0A, (byte)0x1A, (byte)0x0A,
            (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x0D, (byte)0x49, (byte)0x48, (byte)0x44, (byte)0x52,
            (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x0C, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x0D,
            (byte)0x08, (byte)0x06, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x9D, (byte)0x29, (byte)0x8F,
            (byte)0x42, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x78, (byte)0x49, (byte)0x44, (byte)0x41,
            (byte)0x54, (byte)0x78, (byte)0xDA, (byte)0x63, (byte)0xF8, (byte)0xFF, (byte)0xFF, (byte)0x3F,
            (byte)0x03, (byte)0x36, (byte)0x0C, (byte)0x04, (byte)0x4C, (byte)0x40, (byte)0x2C, (byte)0x82,
            (byte)0x21, (byte)0x8E, (byte)0x47, (byte)0x43, (byte)0x3D, (byte)0x10, (byte)0xFF, (byte)0x00,
            (byte)0x62, (byte)0x6E, (byte)0x62, (byte)0x35, (byte)0x68, (byte)0x02, (byte)0x71, (byte)0x0E,
            (byte)0xD1, (byte)0x36, (byte)0xE0, (byte)0x34, (byte)0x08, (byte)0x8B, (byte)0xC9, (byte)0xFA,
            (byte)0x40, (byte)0x9C, (byte)0x09, (byte)0xC4, (byte)0x53, (byte)0x80, (byte)0x58, (byte)0x9B,
            (byte)0x18, (byte)0x0D, (byte)0x7C, (byte)0x40, (byte)0x5C, (byte)0x05, (byte)0x75, (byte)0x3F,
            (byte)0x17, (byte)0x51, (byte)0x4E, (byte)0x02, (byte)0x82, (byte)0xD9, (byte)0x40, (byte)0xBC,
            (byte)0x87, (byte)0x28, (byte)0x27, (byte)0x41, (byte)0x35, (byte)0x3C, (byte)0x04, (byte)0xE2,
            (byte)0x52, (byte)0x62, (byte)0xFD, (byte)0xA0, (byte)0x05, (byte)0xC4, (byte)0x20, (byte)0x86,
            (byte)0x2E, (byte)0xB1, (byte)0x1A, (byte)0x8A, (byte)0x81, (byte)0xF8, (byte)0x19, (byte)0x29,
            (byte)0xA1, (byte)0xB4, (byte)0x1B, (byte)0x88, (byte)0x17, (byte)0x00, (byte)0xB1, (byte)0x11,
            (byte)0x10, (byte)0xCB, (byte)0x10, (byte)0xA3, (byte)0xE1, (byte)0x0D, (byte)0x10, (byte)0x1F,
            (byte)0x04, (byte)0xE2, (byte)0x60, (byte)0x62, (byte)0x6D, (byte)0xF0, (byte)0x05, (byte)0xC5,
            (byte)0x05, (byte)0x2E, (byte)0x27, (byte)0x01, (byte)0x00, (byte)0x7F, (byte)0x59, (byte)0x7E,
            (byte)0x2E, (byte)0x04, (byte)0xA0, (byte)0x03, (byte)0xE7, (byte)0x00, (byte)0x00, (byte)0x00,
            (byte)0x00, (byte)0x49, (byte)0x45, (byte)0x4E, (byte)0x44, (byte)0xAE, (byte)0x42, (byte)0x60,
            (byte)0x82
        };

        byte[]  SUPERSCRIPT =
        {
            (byte)0x89, (byte)0x50, (byte)0x4E, (byte)0x47, (byte)0x0D, (byte)0x0A, (byte)0x1A, (byte)0x0A,
            (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x0D, (byte)0x49, (byte)0x48, (byte)0x44, (byte)0x52,
            (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x0C, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x0D,
            (byte)0x08, (byte)0x06, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x9D, (byte)0x29, (byte)0x8F,
            (byte)0x42, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0xD2, (byte)0x49, (byte)0x44, (byte)0x41,
            (byte)0x54, (byte)0x78, (byte)0xDA, (byte)0x95, (byte)0xD1, (byte)0xBD, (byte)0x0E, (byte)0x01,
            (byte)0x41, (byte)0x14, (byte)0x86, (byte)0xE1, (byte)0x89, (byte)0xBF, (byte)0x0B, (byte)0x20,
            (byte)0x0A, (byte)0x14, (byte)0x48, (byte)0x88, (byte)0x82, (byte)0x44, (byte)0xE5, (byte)0x1E,
            (byte)0x74, (byte)0x0A, (byte)0x51, (byte)0xA9, (byte)0x5D, (byte)0x80, (byte)0x4A, (byte)0xA3,
            (byte)0x74, (byte)0x23, (byte)0x42, (byte)0x22, (byte)0xD1, (byte)0xA1, (byte)0x15, (byte)0x12,
            (byte)0x14, (byte)0x2A, (byte)0xD1, (byte)0x28, (byte)0xFC, (byte)0x15, (byte)0x0A, (byte)0x6A,
            (byte)0x37, (byte)0x20, (byte)0xEB, (byte)0x3D, (byte)0xC9, (byte)0x29, (byte)0x06, (byte)0xBB,
            (byte)0xC4, (byte)0x26, (byte)0xCF, (byte)0x66, (byte)0xE6, (byte)0xCC, (byte)0x7C, (byte)0x39,
            (byte)0xB3, (byte)0xB3, (byte)0xC6, (byte)0x71, (byte)0x1C, (byte)0xE3, (byte)0x85, (byte)0x27,
            (byte)0x89, (byte)0xE1, (byte)0x4B, (byte)0xED, (byte)0x47, (byte)0x20, (byte)0x8B, (byte)0xF5,
            (byte)0x3F, (byte)0x81, (byte)0x04, (byte)0xE6, (byte)0x6F, (byte)0x35, (byte)0x13, (byte)0x40,
            (byte)0x13, (byte)0x4B, (byte)0xEC, (byte)0x30, (byte)0x42, (byte)0x4E, (byte)0x17, (byte)0x63,
            (byte)0x58, (byte)0xA0, (byte)0x83, (byte)0x29, (byte)0x5A, (byte)0x52, (byte)0xAC, (byte)0x63,
            (byte)0x83, (byte)0x34, (byte)0xFC, (byte)0xE8, (byte)0xE1, (byte)0xA8, (byte)0x63, (byte)0x09,
            (byte)0x5C, (byte)0x10, (byte)0x47, (byte)0x50, (byte)0xEB, (byte)0x26, (byte)0x2A, (byte)0xAD,
            (byte)0xAD, (byte)0x96, (byte)0x65, (byte)0xC8, (byte)0x20, (byte)0xA3, (byte)0x81, (byte)0x99,
            (byte)0xB5, (byte)0xB6, (byte)0xB7, (byte)0xCF, (byte)0x96, (byte)0x43, (byte)0x5B, (byte)0x8F,
            (byte)0x20, (byte)0x85, (byte)0x92, (byte)0xDE, (byte)0xD2, (byte)0xCA, (byte)0xDA, (byte)0x73,
            (byte)0x93, (byte)0x57, (byte)0x0A, (byte)0x63, (byte)0x3D, (byte)0x63, (byte)0xC5, (byte)0xEA,
            (byte)0x50, (byte)0x44, (byte)0x1E, (byte)0x5B, (byte)0xDD, (byte)0xEC, (byte)0xC3, (byte)0x43,
            (byte)0x06, (byte)0x7D, (byte)0x4C, (byte)0x5C, (byte)0x8E, (byte)0x54, (byte)0xF0, (byte)0xB8,
            (byte)0x39, (byte)0x73, (byte)0xC2, (byte)0x40, (byte)0x27, (byte)0x11, (byte)0x74, (byte)0x35,
            (byte)0x50, (byte)0xF3, (byte)0x0A, (byte)0x34, (byte)0x70, (byte)0xD7, (byte)0xE0, (byte)0x01,
            (byte)0x55, (byte)0x5C, (byte)0x71, (byte)0x96, (byte)0x6E, (byte)0x1F, (byte)0x01, (byte)0x4D,
            (byte)0x05, (byte)0xF5, (byte)0x5B, (byte)0x7C, (byte)0x3A, (byte)0x0F, (byte)0x21, (byte)0xEC,
            (byte)0xDA, (byte)0xE1, (byte)0xDB, (byte)0x9F, (byte)0x76, (byte)0xF3, (byte)0x04, (byte)0x13,
            (byte)0x2D, (byte)0x08, (byte)0xEB, (byte)0x52, (byte)0x4C, (byte)0xD9, (byte)0x79, (byte)0x00,
            (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x49, (byte)0x45, (byte)0x4E, (byte)0x44, (byte)0xAE,
            (byte)0x42, (byte)0x60, (byte)0x82
        };

        byte[]  SUBSCRIPT   =
        {
            (byte)0x89, (byte)0x50, (byte)0x4E, (byte)0x47, (byte)0x0D, (byte)0x0A, (byte)0x1A, (byte)0x0A,
            (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x0D, (byte)0x49, (byte)0x48, (byte)0x44, (byte)0x52,
            (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x0C, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x0D,
            (byte)0x08, (byte)0x06, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x9D, (byte)0x29, (byte)0x8F,
            (byte)0x42, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0xAC, (byte)0x49, (byte)0x44, (byte)0x41,
            (byte)0x54, (byte)0x78, (byte)0xDA, (byte)0x63, (byte)0xF8, (byte)0xFF, (byte)0xFF, (byte)0x3F,
            (byte)0x03, (byte)0x29, (byte)0x98, (byte)0x81, (byte)0x64, (byte)0x0D, (byte)0x40, (byte)0xC0,
            (byte)0x02, (byte)0xC4, (byte)0x85, (byte)0x40, (byte)0x7C, (byte)0x18, (byte)0x88, (byte)0xAF,
            (byte)0x02, (byte)0xF1, (byte)0x46, (byte)0x20, (byte)0x56, (byte)0xC7, (byte)0xA7, (byte)0x21,
            (byte)0x1A, (byte)0x88, (byte)0xCF, (byte)0x01, (byte)0xB1, (byte)0x22, (byte)0x10, (byte)0x33,
            (byte)0x03, (byte)0xF1, (byte)0x62, (byte)0x20, (byte)0xBE, (byte)0x0D, (byte)0x62, (byte)0xE3,
            (byte)0xD2, (byte)0x20, (byte)0x0A, (byte)0xC4, (byte)0xD2, (byte)0x48, (byte)0x02, (byte)0x1E,
            (byte)0x40, (byte)0x0C, (byte)0x62, (byte)0xA8, (byte)0xE0, (byte)0xF5, (byte)0x03, (byte)0xC8,
            (byte)0x19, (byte)0x40, (byte)0x5C, (byte)0x03, (byte)0xC4, (byte)0x87, (byte)0xA0, (byte)0x1A,
            (byte)0x4C, (byte)0x71, (byte)0xD9, (byte)0xA0, (byte)0x00, (byte)0xC4, (byte)0x9B, (byte)0x80,
            (byte)0x78, (byte)0x0F, (byte)0x10, (byte)0xFB, (byte)0x23, (byte)0xD9, (byte)0xA0, (byte)0x8F,
            (byte)0xA4, (byte)0xA8, (byte)0x1A, (byte)0x88, (byte)0x55, (byte)0x61, (byte)0x1A, (byte)0x96,
            (byte)0x00, (byte)0xF1, (byte)0x66, (byte)0x2C, (byte)0x4E, (byte)0xD2, (byte)0x41, (byte)0x12,
            (byte)0xFB, (byte)0x04, (byte)0xC4, (byte)0xC6, (byte)0x30, (byte)0x0D, (byte)0x77, (byte)0x80,
            (byte)0x78, (byte)0x39, (byte)0x94, (byte)0x23, (byte)0x0C, (byte)0xC4, (byte)0x0B, (byte)0xA1,
            (byte)0x1A, (byte)0x42, (byte)0x91, (byte)0x34, (byte)0x3C, (byte)0x06, (byte)0x62, (byte)0x35,
            (byte)0x98, (byte)0x86, (byte)0x54, (byte)0x20, (byte)0xFE, (byte)0x00, (byte)0xD5, (byte)0x78,
            (byte)0x0B, (byte)0x88, (byte)0x83, (byte)0x81, (byte)0xF8, (byte)0x29, (byte)0x10, (byte)0xDF,
            (byte)0x05, (byte)0xD9, (byte)0x06, (byte)0x55, (byte)0xF4, (byte)0x08, (byte)0xEE, (byte)0x24,
            (byte)0x22, (byte)0x23, (byte)0x8B, (byte)0x64, (byte)0x0D, (byte)0x2F, (byte)0x60, (byte)0x7E,
            (byte)0x22, (byte)0x56, (byte)0xC3, (byte)0x4F, (byte)0x20, (byte)0xB6, (byte)0x05, (byte)0xB1,
            (byte)0x01, (byte)0xA4, (byte)0x79, (byte)0x36, (byte)0x16, (byte)0xD8, (byte)0xEC, (byte)0x44,
            (byte)0x1D, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x49, (byte)0x45, (byte)0x4E,
            (byte)0x44, (byte)0xAE, (byte)0x42, (byte)0x60, (byte)0x82
        };

        byte[]  UNDERLINE   =
        {
            (byte)0x89, (byte)0x50, (byte)0x4E, (byte)0x47, (byte)0x0D, (byte)0x0A, (byte)0x1A, (byte)0x0A,
            (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x0D, (byte)0x49, (byte)0x48, (byte)0x44, (byte)0x52,
            (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x0C, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x0D,
            (byte)0x08, (byte)0x06, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x9D, (byte)0x29, (byte)0x8F,
            (byte)0x42, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x8D, (byte)0x49, (byte)0x44, (byte)0x41,
            (byte)0x54, (byte)0x78, (byte)0xDA, (byte)0x63, (byte)0xF8, (byte)0xFF, (byte)0xFF, (byte)0x3F,
            (byte)0x03, (byte)0x29, (byte)0x98, (byte)0x81, (byte)0x6C, (byte)0x0D, (byte)0x40, (byte)0x50,
            (byte)0x00, (byte)0xC4, (byte)0x7B, (byte)0x81, (byte)0x58, (byte)0x1A, (byte)0x49, (byte)0x2C,
            (byte)0x16, (byte)0x88, (byte)0xF7, (byte)0x00, (byte)0xB1, (byte)0x31, (byte)0x36, (byte)0x0D,
            (byte)0x1D, (byte)0x40, (byte)0x0C, (byte)0x62, (byte)0x48, (byte)0x22, (byte)0x89, (byte)0xE5,
            (byte)0x42, (byte)0xC5, (byte)0xCC, (byte)0xB0, (byte)0x69, (byte)0xA8, (byte)0x81, (byte)0x4A,
            (byte)0xF2, (byte)0x22, (byte)0x89, (byte)0xC5, (byte)0x41, (byte)0xC5, (byte)0xD4, (byte)0xB1,
            (byte)0x69, (byte)0x28, (byte)0x84, (byte)0x4A, (byte)0x32, (byte)0x21, (byte)0x89, (byte)0x05,
            (byte)0x42, (byte)0xC5, (byte)0xA4, (byte)0xB0, (byte)0x69, (byte)0xC8, (byte)0x67, (byte)0x00,
            (byte)0x73, (byte)0xFF, (byte)0x23, (byte)0x8B, (byte)0xF9, (byte)0xE3, (byte)0xD3, (byte)0x00,
            (byte)0x73, (byte)0x2F, (byte)0x1B, (byte)0x92, (byte)0x58, (byte)0x27, (byte)0x54, (byte)0x4C,
            (byte)0x0F, (byte)0x9B, (byte)0x06, (byte)0x6F, (byte)0xA8, (byte)0xA4, (byte)0x05, (byte)0x10,
            (byte)0x73, (byte)0x00, (byte)0x71, (byte)0x39, (byte)0x10, (byte)0xEF, (byte)0x86, (byte)0x8A,
            (byte)0xB9, (byte)0x62, (byte)0xD3, (byte)0xC0, (byte)0x0C, (byte)0xC4, (byte)0xDB, (byte)0x81,
            (byte)0xF8, (byte)0x33, (byte)0x10, (byte)0xDF, (byte)0x07, (byte)0xE2, (byte)0x7A, (byte)0x20,
            (byte)0xB6, (byte)0x81, (byte)0x6A, (byte)0x88, (byte)0xA1, (byte)0x2C, (byte)0xE2, (byte)0x80,
            (byte)0xA0, (byte)0x01, (byte)0x6A, (byte)0x12, (byte)0x3E, (byte)0xDC, (byte)0x40, (byte)0x96,
            (byte)0x0D, (byte)0x00, (byte)0x79, (byte)0xF0, (byte)0x3F, (byte)0x49, (byte)0x09, (byte)0x2C,
            (byte)0xC1, (byte)0xAE, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x49, (byte)0x45,
            (byte)0x4E, (byte)0x44, (byte)0xAE, (byte)0x42, (byte)0x60, (byte)0x82
        };

        byte[]  STRIKETHROUGH   =
        {
            (byte)0x89, (byte)0x50, (byte)0x4E, (byte)0x47, (byte)0x0D, (byte)0x0A, (byte)0x1A, (byte)0x0A,
            (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x0D, (byte)0x49, (byte)0x48, (byte)0x44, (byte)0x52,
            (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x0C, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x0D,
            (byte)0x08, (byte)0x06, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x9D, (byte)0x29, (byte)0x8F,
            (byte)0x42, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x87, (byte)0x49, (byte)0x44, (byte)0x41,
            (byte)0x54, (byte)0x78, (byte)0xDA, (byte)0x63, (byte)0xF8, (byte)0xFF, (byte)0xFF, (byte)0x3F,
            (byte)0x03, (byte)0x29, (byte)0x98, (byte)0x81, (byte)0x62, (byte)0x0D, (byte)0x40, (byte)0x20,
            (byte)0x00, (byte)0xC4, (byte)0x7E, (byte)0x40, (byte)0x1C, (byte)0x0D, (byte)0xC4, (byte)0x26,
            (byte)0x40, (byte)0xCC, (byte)0x88, (byte)0x53, (byte)0x03, (byte)0x10, (byte)0xA8, (byte)0x02,
            (byte)0xF1, (byte)0x5D, (byte)0x20, (byte)0xAE, (byte)0x02, (byte)0xE2, (byte)0x5A, (byte)0x20,
            (byte)0x7E, (byte)0x03, (byte)0xC4, (byte)0x05, (byte)0xF8, (byte)0x34, (byte)0xB4, (byte)0x02,
            (byte)0xF1, (byte)0x51, (byte)0x24, (byte)0xBE, (byte)0x2E, (byte)0x10, (byte)0x2B, (byte)0xE3,
            (byte)0xD3, (byte)0x50, (byte)0x0C, (byte)0xC4, (byte)0x7F, (byte)0xA1, (byte)0xA6, (byte)0x8B,
            (byte)0xE1, (byte)0xF4, (byte)0x03, (byte)0x10, (byte)0x34, (byte)0x00, (byte)0xF1, (byte)0x7F,
            (byte)0x02, (byte)0xB8, (byte)0x01, (byte)0x67, (byte)0x28, (byte)0x01, (byte)0x01, (byte)0x37,
            (byte)0x10, (byte)0xA7, (byte)0x03, (byte)0xF1, (byte)0x1F, (byte)0x20, (byte)0x9E, (byte)0x8D,
            (byte)0xCF, (byte)0x49, (byte)0xEA, (byte)0x68, (byte)0xFC, (byte)0xA3, (byte)0x40, (byte)0xBC,
            (byte)0x07, (byte)0x9F, (byte)0x86, (byte)0xDB, (byte)0x40, (byte)0xDC, (byte)0x08, (byte)0xC4,
            (byte)0xF2, (byte)0x40, (byte)0x1C, (byte)0x00, (byte)0xC4, (byte)0xBF, (byte)0x80, (byte)0x38,
            (byte)0x12, (byte)0x9F, (byte)0x06, (byte)0x37, (byte)0x20, (byte)0x2E, (byte)0x03, (byte)0xE2,
            (byte)0x45, (byte)0x40, (byte)0x3C, (byte)0x09, (byte)0x88, (byte)0x9D, (byte)0xA9, (byte)0x1F,
            (byte)0xD3, (byte)0x84, (byte)0x30, (byte)0x00, (byte)0x0A, (byte)0xF5, (byte)0x5F, (byte)0xA3,
            (byte)0x54, (byte)0xC8, (byte)0xE3, (byte)0x1B, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
            (byte)0x49, (byte)0x45, (byte)0x4E, (byte)0x44, (byte)0xAE, (byte)0x42, (byte)0x60, (byte)0x82
        };
    }

////////////////////////////////////////////////////////////////////////
//  Member classes : non-inner classes
////////////////////////////////////////////////////////////////////////


    // STYLE BUTTON PANEL CLASS


    private static class StyleButtonPanel
        extends JPanel
    {

    ////////////////////////////////////////////////////////////////////
    //  Constants
    ////////////////////////////////////////////////////////////////////

        private static final    Map<StyledText.StyleAttr, ImageIcon>    ICON_MAP;

    ////////////////////////////////////////////////////////////////////
    //  Member classes : non-inner classes
    ////////////////////////////////////////////////////////////////////


        // BUTTON CLASS


        private static class Button
            extends JButton
        {

        ////////////////////////////////////////////////////////////////
        //  Constants
        ////////////////////////////////////////////////////////////////

            private static final    int VERTICAL_MARGIN     = 2;
            private static final    int HORIZONTAL_MARGIN   = 8;

            private static final    Color   BACKGROUND_COLOUR               = new Color( 224, 232, 224 );
            private static final    Color   HIGHLIGHTED_BACKGROUND_COLOUR   = new Color( 248, 240, 176 );
            private static final    Color   BORDER_COLOUR                   = new Color( 160, 192, 160 );
            private static final    Color   DISABLED_BORDER_COLOUR          = Color.LIGHT_GRAY;
            private static final    Color   FOCUSED_BORDER_COLOUR           = Color.BLACK;

        ////////////////////////////////////////////////////////////////
        //  Constructors
        ////////////////////////////////////////////////////////////////

            private Button( ImageIcon icon )
            {
                // Call superclass constructor
                super( icon );

                // Set component attributes
                setPreferredSize( new Dimension( 2 * HORIZONTAL_MARGIN + icon.getIconWidth( ),
                                                 2 * VERTICAL_MARGIN + icon.getIconHeight( ) ) );
            }

            //----------------------------------------------------------

        ////////////////////////////////////////////////////////////////
        //  Instance methods : overriding methods
        ////////////////////////////////////////////////////////////////

            @Override
            protected void paintComponent( Graphics gr )
            {
                // Create copy of graphics context
                gr = gr.create( );

                // Get dimensions
                int width = getWidth( );
                int height = getHeight( );

                // Fill interior
                gr.setColor( isEnabled( ) ? (isSelected( ) != getModel( ).isArmed( ))
                                                                            ? HIGHLIGHTED_BACKGROUND_COLOUR
                                                                            : BACKGROUND_COLOUR
                                          : getBackground( ) );
                gr.fillRect( 0, 0, width, height );

                // Draw icon
                Icon icon = isEnabled( ) ? getIcon( ) : getDisabledIcon( );
                icon.paintIcon( this, gr, HORIZONTAL_MARGIN, VERTICAL_MARGIN );

                // Draw border
                gr.setColor( isEnabled( ) ? BORDER_COLOUR : DISABLED_BORDER_COLOUR );
                gr.drawRect( 0, 0, width - 1, height - 1 );
                if ( isFocusOwner( ) )
                {
                    ((Graphics2D)gr).setStroke( GuiUtilities.getBasicDash( ) );
                    gr.setColor( FOCUSED_BORDER_COLOUR );
                    gr.drawRect( 1, 1, width - 3, height - 3 );
                }
            }

            //----------------------------------------------------------

        }

        //==============================================================

    ////////////////////////////////////////////////////////////////////
    //  Constructors
    ////////////////////////////////////////////////////////////////////

        private StyleButtonPanel( String         commandPrefix,
                                  ActionListener listener )
        {
            // Set layout manager
            setLayout( new GridLayout( 1, 0, 8, 0 ) );

            // Buttons
            for ( StyledText.StyleAttr styleAttr : StyledText.StyleAttr.values( ) )
            {
                JButton button = new Button( ICON_MAP.get( styleAttr ) );
                button.setEnabled( false );
                button.setToolTipText( styleAttr.toString( ) );
                button.setActionCommand( commandPrefix + styleAttr.getKey( ) );
                button.addActionListener( listener );
                add( button );
            }
        }

        //--------------------------------------------------------------

    ////////////////////////////////////////////////////////////////////
    //  Static initialiser
    ////////////////////////////////////////////////////////////////////

        static
        {
            ICON_MAP = new EnumMap<>( StyledText.StyleAttr.class );
            ICON_MAP.put( StyledText.StyleAttr.BOLD,          new ImageIcon( ImageData.BOLD ) );
            ICON_MAP.put( StyledText.StyleAttr.ITALIC,        new ImageIcon( ImageData.ITALIC ) );
            ICON_MAP.put( StyledText.StyleAttr.SUPERSCRIPT,   new ImageIcon( ImageData.SUPERSCRIPT ) );
            ICON_MAP.put( StyledText.StyleAttr.SUBSCRIPT,     new ImageIcon( ImageData.SUBSCRIPT ) );
            ICON_MAP.put( StyledText.StyleAttr.UNDERLINE,     new ImageIcon( ImageData.UNDERLINE ) );
            ICON_MAP.put( StyledText.StyleAttr.STRIKETHROUGH, new ImageIcon( ImageData.STRIKETHROUGH ) );
        }

    }

    //==================================================================


    // TEXT AREA UNDO MANAGER CLASS


    private static class TextAreaUndoManager
        extends UndoManager
    {

    ////////////////////////////////////////////////////////////////////
    //  Constructors
    ////////////////////////////////////////////////////////////////////

        private TextAreaUndoManager( )
        {
        }

        //--------------------------------------------------------------

    ////////////////////////////////////////////////////////////////////
    //  Instance methods : overriding methods
    ////////////////////////////////////////////////////////////////////

        public boolean addEdit( UndoableEdit edit )
        {
            return ( (compoundEdit == null) ? super.addEdit( edit ) : compoundEdit.addEdit( edit ) );
        }

        //--------------------------------------------------------------

    ////////////////////////////////////////////////////////////////////
    //  Instance methods
    ////////////////////////////////////////////////////////////////////

        private void startCompoundEdit( )
        {
            if ( compoundEdit != null )
                throw new IllegalStateException( );
            compoundEdit = new CompoundEdit( );
        }

        //--------------------------------------------------------------

        private void endCompoundEdit( )
        {
            if ( compoundEdit == null )
                throw new IllegalStateException( );
            compoundEdit.end( );
            super.addEdit( compoundEdit );
            compoundEdit = null;
        }

        //--------------------------------------------------------------

    ////////////////////////////////////////////////////////////////////
    //  Instance variables
    ////////////////////////////////////////////////////////////////////

        private CompoundEdit    compoundEdit;

    }

    //==================================================================

////////////////////////////////////////////////////////////////////////
//  Member classes : inner classes
////////////////////////////////////////////////////////////////////////


    // COMMAND ACTION CLASS


    private static class CommandAction
        extends AbstractAction
    {

    ////////////////////////////////////////////////////////////////////
    //  Constructors
    ////////////////////////////////////////////////////////////////////

        private CommandAction( String command,
                               String text )
        {
            // Call superclass constructor
            super( text );

            // Set action properties
            putValue( Action.ACTION_COMMAND_KEY, command );
        }

        //--------------------------------------------------------------

    ////////////////////////////////////////////////////////////////////
    //  Instance methods : ActionListener interface
    ////////////////////////////////////////////////////////////////////

        public void actionPerformed( ActionEvent event )
        {
            listener.actionPerformed( event );
        }

        //--------------------------------------------------------------

    ////////////////////////////////////////////////////////////////////
    //  Instance variables
    ////////////////////////////////////////////////////////////////////

        private ActionListener  listener;

    }

    //==================================================================

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

    public TextPanel( int numRows )
    {
        this( numRows, null, false );
    }

    //------------------------------------------------------------------

    public TextPanel( int          numRows,
                      List<String> paragraphs )
    {
        this( numRows,
              ListUtilities.isNullOrEmpty( paragraphs ) ? null
                                                        : StringUtilities.join( "\n\n", paragraphs ),
              false );
        textArea.setCaretPosition( 0 );
    }

    //------------------------------------------------------------------

    public TextPanel( int     numRows,
                      String  text,
                      boolean noLfs )
    {
        // Initialise instance variables
        undoManager = new TextAreaUndoManager( );
        undoManager.setLimit( AppConfig.getInstance( ).getMaxEditListLength( ) );

        // Set action listener in commands
        for ( String commandKey : COMMANDS.keySet( ) )
            COMMANDS.get( commandKey ).listener = this;


        //----  Text area

        textArea = new FTextArea( );
        textArea.setLineWrap( true );
        textArea.setWrapStyleWord( true );
        textArea.setFocusTraversalKeys( KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS,
                                        new HashSet<KeyStroke>( FOCUS_FORWARD_KEYS ) );
        textArea.setFocusTraversalKeys( KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS,
                                        new HashSet<KeyStroke>( FOCUS_BACKWARD_KEYS ) );
        if ( noLfs )
        {
            inputMapKey = InputMapUtilities.removeFromInputMap( textArea, JComponent.WHEN_FOCUSED,
                                                                ENTER_KEY );
            textArea.addPropertyChangeListener( this );
        }
        textArea.setText( text );

        // Scroll pane: text area
        JScrollPane textAreaScrollPane = new JScrollPane( textArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                                                          JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED );

        // Set size of viewport
        FontMetrics fontMetrics = textArea.getFontMetrics( textArea.getFont( ) );
        int width = NUM_COLUMNS * GuiUtilities.getCharWidth( '0', fontMetrics );
        int height = numRows * fontMetrics.getHeight( );
        textAreaScrollPane.getViewport( ).setPreferredSize( new Dimension( width, height ) );
        GuiUtilities.setViewportBorder( textAreaScrollPane, VERTICAL_MARGIN, HORIZONTAL_MARGIN );

        // Set component attributes
        textAreaScrollPane.getVerticalScrollBar( ).setFocusable( false );
        textAreaScrollPane.getHorizontalScrollBar( ).setFocusable( false );


        //----  Style button panel

        GridBagLayout gridBag = new GridBagLayout( );
        GridBagConstraints gbc = new GridBagConstraints( );

        JPanel styleButtonOuterPanel = new JPanel( gridBag );
        GuiUtilities.setPaddedLineBorder( styleButtonOuterPanel, 1 );

        styleButtonPanel = new StyleButtonPanel( Command.APPLY_STYLE, this );

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets( 0, 0, 0, 0 );
        gridBag.setConstraints( styleButtonPanel, gbc );
        styleButtonOuterPanel.add( styleButtonPanel );


        //----  This panel

        // Set layout manager
        setLayout( gridBag );

        int gridY = 0;

        gbc.gridx = 0;
        gbc.gridy = gridY++;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets( 0, 0, 0, 0 );
        gridBag.setConstraints( textAreaScrollPane, gbc );
        add( textAreaScrollPane );

        gbc.gridx = 0;
        gbc.gridy = gridY++;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets( -1, 0, 0, 0 );
        gridBag.setConstraints( styleButtonOuterPanel, gbc );
        add( styleButtonOuterPanel );

        // Add listeners
        textArea.addCaretListener( this );
        textArea.addMouseListener( this );
        textArea.getDocument( ).addUndoableEditListener( undoManager );

        // Add commands to action map
        KeyAction.create( textArea, JComponent.WHEN_FOCUSED, this, KEY_COMMANDS );

        // Add style commands to action map
        List<KeyAction.KeyCommandPair> styleCommands = new ArrayList<>( );
        for ( StyledText.StyleAttr attr : StyledText.StyleAttr.values( ) )
        {
            int keyCode = KeyEvent.getExtendedKeyCodeForChar( attr.getKeyChar( ) );
            KeyStroke keyStroke = KeyStroke.getKeyStroke( keyCode, KeyEvent.CTRL_DOWN_MASK );
            styleCommands.add( new KeyAction.KeyCommandPair( keyStroke,
                                                             Command.APPLY_STYLE + attr.getKey( ) ) );
        }
        KeyAction.create( textArea, JComponent.WHEN_FOCUSED, this, styleCommands );
    }

    //------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : ActionListener interface
////////////////////////////////////////////////////////////////////////

    public void actionPerformed( ActionEvent event )
    {
        String command = event.getActionCommand( );

        if ( command.startsWith( Command.APPLY_STYLE ) )
            onApplyStyle( StringUtilities.removePrefix( command, Command.APPLY_STYLE ) );

        else if ( command.equals( Command.UNDO ) )
            onUndo( );

        else if ( command.equals( Command.REDO ) )
            onRedo( );

        else if ( command.equals( Command.CUT ) )
            onCut( );

        else if ( command.equals( Command.COPY ) )
            onCopy( );

        else if ( command.equals( Command.PASTE ) )
            onPaste( );

        else if ( command.equals( Command.SHOW_CONTEXT_MENU ) )
            onShowContextMenu( );
    }

    //------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : CaretListener interface
////////////////////////////////////////////////////////////////////////

    public void caretUpdate( CaretEvent event )
    {
        GuiUtilities.setAllEnabled( styleButtonPanel, isSelection( ) );
    }

    //------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : MouseListener interface
////////////////////////////////////////////////////////////////////////

    public void mouseClicked( MouseEvent event )
    {
        // do nothing
    }

    //------------------------------------------------------------------

    public void mouseEntered( MouseEvent event )
    {
        // do nothing
    }

    //------------------------------------------------------------------

    public void mouseExited( MouseEvent event )
    {
        // do nothing
    }

    //------------------------------------------------------------------

    public void mousePressed( MouseEvent event )
    {
        showContextMenu( event );
    }

    //------------------------------------------------------------------

    public void mouseReleased( MouseEvent event )
    {
        showContextMenu( event );
    }

    //------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : PropertyChangeListener interface
////////////////////////////////////////////////////////////////////////

    public void propertyChange( PropertyChangeEvent event )
    {
        if ( event.getPropertyName( ).equals( ANCESTOR_PROPERTY_NAME ) && (event.getNewValue( ) == null) )
            InputMapUtilities.restoreInputMap( inputMapKey );
    }

    //------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : overriding methods
////////////////////////////////////////////////////////////////////////

    @Override
    public boolean requestFocusInWindow( )
    {
        return textArea.requestFocusInWindow( );
    }

    //------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

    public boolean isEmpty( )
    {
        return textArea.isEmpty( );
    }

    //------------------------------------------------------------------

    public String getText( )
        throws StyledText.ParseException
    {
        String text = textArea.getText( );
        try
        {
            new StyledText( text );
        }
        catch ( StyledText.ParseException e )
        {
            textArea.setCaretPosition( e.getIndex( ) );
            textArea.requestFocusInWindow( );
            throw e;
        }
        return text;
    }

    //------------------------------------------------------------------

    public List<String> getParagraphs( String lineBreak )
        throws StyledText.ParseException
    {
        String text = textArea.getText( ) + "\n\n";
        Matcher matcher = PARAGRAPH_SEPARATOR_PATTERN.matcher( text );
        List<String> paragraphs = new ArrayList<>( );
        int index = 0;
        String lineBreakRegex = StringUtilities.substitute( CrosswordDocument.LINE_BREAK_REGEX, "!",
                                                            RegexUtilities.escape( lineBreak ) );
        while ( matcher.find( ) )
        {
            String paragraph = text.substring( index, matcher.start( ) ).replaceAll( lineBreakRegex, " " );
            if ( !paragraph.isEmpty( ) )
            {
                try
                {
                    new StyledText( paragraph );
                }
                catch ( StyledText.ParseException e )
                {
                    textArea.setCaretPosition( index + e.getIndex( ) );
                    textArea.requestFocusInWindow( );
                    throw e;
                }
                paragraphs.add( paragraph );
            }
            index = matcher.end( );
        }
        return paragraphs;
    }

    //------------------------------------------------------------------

    public void clear( )
    {
        try
        {
            Document document = textArea.getDocument( );
            document.remove( 0, document.getLength( ) );
        }
        catch ( BadLocationException e )
        {
            throw new UnexpectedRuntimeException( );
        }
    }

    //------------------------------------------------------------------

    public void addDocumentListener( DocumentListener listener )
    {
        textArea.getDocument( ).addDocumentListener( listener );
    }

    //------------------------------------------------------------------

    private boolean isSelection( )
    {
        return ( textArea.getSelectionStart( ) != textArea.getSelectionEnd( ) );
    }

    //------------------------------------------------------------------

    private void showContextMenu( MouseEvent event )
    {
        if ( (event == null) || event.isPopupTrigger( ) )
        {
            // Create context menu
            if ( contextMenu == null )
            {
                contextMenu = new JPopupMenu( );

                contextMenu.add( new FMenuItem( COMMANDS.get( Command.UNDO ) ) );
                contextMenu.add( new FMenuItem( COMMANDS.get( Command.REDO ) ) );

                contextMenu.addSeparator( );

                contextMenu.add( new FMenuItem( COMMANDS.get( Command.CUT ) ) );
                contextMenu.add( new FMenuItem( COMMANDS.get( Command.COPY ) ) );
                contextMenu.add( new FMenuItem( COMMANDS.get( Command.PASTE ) ) );
            }

            // Update commands
            COMMANDS.get( Command.UNDO ).setEnabled( undoManager.canUndo( ) );
            COMMANDS.get( Command.REDO ).setEnabled( undoManager.canRedo( ) );
            COMMANDS.get( Command.CUT ).setEnabled( isSelection( ) );
            COMMANDS.get( Command.COPY ).setEnabled( isSelection( ) );
            COMMANDS.get( Command.PASTE ).setEnabled( Util.clipboardHasText( ) );

            // Display menu
            if ( event == null )
                contextMenu.show( this, 0, 0 );
            else
                contextMenu.show( event.getComponent( ), event.getX( ), event.getY( ) );
        }
    }

    //------------------------------------------------------------------

    private void onApplyStyle( String key )
    {
        int startOffset = textArea.getSelectionStart( );
        int endOffset = textArea.getSelectionEnd( );
        if ( startOffset != endOffset )
        {
            if ( startOffset > endOffset )
            {
                int temp = startOffset;
                startOffset = endOffset;
                endOffset = temp;
            }
            try
            {
                Document document = textArea.getDocument( );
                Matcher matcher = PARAGRAPH_SEPARATOR_PATTERN.
                                        matcher( document.getText( startOffset, endOffset - startOffset ) );
                List<Integer> offsets = new ArrayList<>( );
                offsets.add( startOffset );
                while ( matcher.find( ) )
                {
                    offsets.add( matcher.start( ) );
                    offsets.add( matcher.end( ) );
                }
                offsets.add( endOffset );

                undoManager.startCompoundEdit( );
                int length = 0;
                int index = 0;
                while ( index < offsets.size( ) )
                {
                    startOffset = offsets.get( index++ );
                    endOffset = offsets.get( index++ );
                    if ( startOffset < endOffset )
                    {
                        String str = StyledText.STYLE_PREFIX + key + " ";
                        document.insertString( startOffset + length, str, null );
                        length += str.length( );

                        str = StyledText.STYLE_SUFFIX;
                        document.insertString( endOffset + length, str, null );
                        length += str.length( );
                    }
                }
                endOffset += length;
                undoManager.endCompoundEdit( );
            }
            catch ( BadLocationException e )
            {
                throw new UnexpectedRuntimeException( );
            }
            textArea.setCaretPosition( endOffset );
            textArea.requestFocusInWindow( );
        }
    }

    //------------------------------------------------------------------

    private void onUndo( )
    {
        if ( undoManager.canUndo( ) )
            undoManager.undo( );
    }

    //------------------------------------------------------------------

    private void onRedo( )
    {
        if ( undoManager.canRedo( ) )
            undoManager.redo( );
    }

    //------------------------------------------------------------------

    private void onCut( )
    {
        textArea.cut( );
    }

    //------------------------------------------------------------------

    private void onCopy( )
    {
        textArea.copy( );
    }

    //------------------------------------------------------------------

    private void onPaste( )
    {
        textArea.paste( );
    }

    //------------------------------------------------------------------

    private void onShowContextMenu( )
    {
        showContextMenu( null );
    }

    //------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Static initialiser
////////////////////////////////////////////////////////////////////////

    static
    {
        // Forward traversal keys
        FOCUS_FORWARD_KEYS = new HashSet<>( );
        FOCUS_FORWARD_KEYS.add
        (
            KeyStroke.getKeyStroke( KeyEvent.VK_TAB, 0 )
        );
        FOCUS_FORWARD_KEYS.add
        (
            KeyStroke.getKeyStroke( KeyEvent.VK_TAB, KeyEvent.CTRL_DOWN_MASK )
        );

        // Backward traversal keys
        FOCUS_BACKWARD_KEYS = new HashSet<>( );
        FOCUS_BACKWARD_KEYS.add
        (
            KeyStroke.getKeyStroke( KeyEvent.VK_TAB, KeyEvent.SHIFT_DOWN_MASK )
        );
        FOCUS_BACKWARD_KEYS.add
        (
            KeyStroke.getKeyStroke( KeyEvent.VK_TAB, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK )
        );

        // Commands
        COMMANDS = new HashMap<>( );
        COMMANDS.put( Command.UNDO,  new CommandAction( Command.UNDO,  UNDO_STR ) );
        COMMANDS.put( Command.REDO,  new CommandAction( Command.REDO,  REDO_STR ) );
        COMMANDS.put( Command.CUT,   new CommandAction( Command.CUT,   CUT_STR ) );
        COMMANDS.put( Command.COPY,  new CommandAction( Command.COPY,  COPY_STR ) );
        COMMANDS.put( Command.PASTE, new CommandAction( Command.PASTE, PASTE_STR ) );
    }

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

    private int                 inputMapKey;
    private TextAreaUndoManager undoManager;
    private FTextArea           textArea;
    private StyleButtonPanel    styleButtonPanel;
    private JPopupMenu          contextMenu;

}

//----------------------------------------------------------------------
