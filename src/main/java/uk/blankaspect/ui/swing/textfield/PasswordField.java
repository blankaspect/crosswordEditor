/*====================================================================*\

PasswordField.java

Password field class.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.ui.swing.textfield;

//----------------------------------------------------------------------


// IMPORTS


import javax.swing.JPasswordField;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

//----------------------------------------------------------------------


// PASSWORD FIELD CLASS


public class PasswordField
	extends JPasswordField
{

////////////////////////////////////////////////////////////////////////
//  Member classes : inner classes
////////////////////////////////////////////////////////////////////////


	// PASSWORD DOCUMENT CLASS


	private class PasswordDocument
		extends PlainDocument
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		private static final	String	OFFSET_BEYOND_END_STR	= "Offset is beyond end of text";

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private PasswordDocument()
		{
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		public void insertString(int          offset,
								 String       str,
								 AttributeSet attrSet)
			throws BadLocationException
		{
			// Test for offset beyond end of text
			int length = getLength();
			if (offset > length)
				throw new BadLocationException(OFFSET_BEYOND_END_STR, offset);

			// Translate string
			String insertStr = translateInsertString(str, offset);

			// Create an array of characters to be inserted from the valid characters in the string
			char[] buffer = new char[insertStr.length()];
			int insertLength = 0;
			for (int i = 0; i < insertStr.length(); i++)
			{
				char ch = insertStr.charAt(i);
				if (acceptCharacter(ch, offset + insertLength))
					buffer[insertLength++] = ch;
			}
			boolean isError = (insertLength < insertStr.length());

			// If inserted characters would make the text too long, remove excess from end of text
			if (length + insertLength > maxLength)
			{
				isError = true;
				if (insertLength > maxLength - offset)
					insertLength = maxLength - offset;
				if ((insertLength > 0) && (offset < length))
				{
					int excessLength = length + insertLength - maxLength;
					if (excessLength > 0)
						remove(length - excessLength, excessLength);
				}
			}

			// Beep if not all input string will be inserted
			if (isError)
				getToolkit().beep();

			// Insert valid characters and set caret position to end of insertion
			if (insertLength > 0)
			{
				super.insertString(offset, new String(buffer, 0, insertLength), attrSet);
				setCaretPosition(offset + insertLength);
			}
		}

		//--------------------------------------------------------------

	}

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	public PasswordField(int maxLength)
	{
		super(maxLength);
		this.maxLength = maxLength;
	}

	//------------------------------------------------------------------

	public PasswordField(int    maxLength,
						 String text)
	{
		this(maxLength);
		setText(text);
	}

	//------------------------------------------------------------------

	public PasswordField(int maxLength,
						 int columns)
	{
		super(columns);
		this.maxLength = (maxLength == 0) ? Integer.MAX_VALUE : maxLength;
	}

	//------------------------------------------------------------------

	public PasswordField(int    maxLength,
						 int    columns,
						 String text)
	{
		this(maxLength, columns);
		setText(text);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : overriding methods
////////////////////////////////////////////////////////////////////////

	@Override
	protected Document createDefaultModel()
	{
		return new PasswordDocument();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	public boolean isEmpty()
	{
		Document document = getDocument();
		return ((document == null) ? true : (document.getLength() == 0));
	}

	//------------------------------------------------------------------

	protected String translateInsertString(String str,
										   int    offset)
	{
		return str;
	}

	//------------------------------------------------------------------

	protected boolean acceptCharacter(char ch,
									  int  index)
	{
		return !(Character.isISOControl(ch) || Character.isSurrogate(ch));
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private	int	maxLength;

}

//----------------------------------------------------------------------
