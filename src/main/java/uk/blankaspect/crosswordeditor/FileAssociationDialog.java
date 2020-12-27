/*====================================================================*\

FileAssociationDialog.java

File association dialog box class.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.crosswordeditor;

//----------------------------------------------------------------------


// IMPORTS


import java.awt.Component;
import java.awt.Dialog;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Window;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.io.File;

import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import uk.blankaspect.common.exception.AppException;
import uk.blankaspect.common.exception.FileException;

import uk.blankaspect.common.filesystem.PathnameUtils;

import uk.blankaspect.common.misc.FilenameSuffixFilter;

import uk.blankaspect.common.platform.windows.FileAssociations;

import uk.blankaspect.common.property.PropertyString;

import uk.blankaspect.common.swing.action.KeyAction;

import uk.blankaspect.common.swing.button.FButton;

import uk.blankaspect.common.swing.checkbox.FCheckBox;

import uk.blankaspect.common.swing.combobox.FComboBox;

import uk.blankaspect.common.swing.container.PathnamePanel;

import uk.blankaspect.common.swing.label.FLabel;

import uk.blankaspect.common.swing.misc.GuiUtils;

//----------------------------------------------------------------------


// FILE ASSOCIATION DIALOG BOX CLASS


class FileAssociationDialog
	extends JDialog
	implements ActionListener
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	private static final	Insets	BUTTON_MARGINS	= new Insets(2, 4, 2, 4);

	private static final	String	KEY	= FileAssociationDialog.class.getCanonicalName();

	private static final	String	JAVA_HOME_KEY	= "java.home";
	private static final	String	USER_DIR_KEY	= "user.dir";

	private static final	String	JAVA_LAUNCHER_PATHNAME	= "bin\\javaw.exe";

	private static final	String	TITLE_STR				= "Windows file associations";
	private static final	String	ACTION_STR				= "Action";
	private static final	String	JAVA_LAUNCHER_STR		= "Java launcher";
	private static final	String	JAR_STR					= "JAR";
	private static final	String	ICON_STR				= "Icon";
	private static final	String	FILES_MUST_EXIST_STR	= "Files must exist";
	private static final	String	SCRIPT_LIFE_CYCLE_STR	= "Script life cycle";
	private static final	String	DEFAULT_STR				= "Default";
	private static final	String	JAVA_LAUNCHER_FILE_STR	= "Java launcher file";
	private static final	String	JAR_FILE_STR			= "JAR file";
	private static final	String	ICON_FILE_STR			= "Icon file";
	private static final	String	SELECT_STR				= "Select";
	private static final	String	SELECT_FILE_STR			= "Select file";

	// Commands
	private interface Command
	{
		String	SELECT_ACTION						= "selectAction";
		String	CHOOSE_JAVA_LAUNCHER_FILE			= "chooseJavaLauncherFile";
		String	CHOOSE_JAR_FILE						= "chooseJarFile";
		String	CHOOSE_ICON_FILE					= "chooseIconFile";
		String	SET_DEFAULT_JAVA_LAUNCHER_PATHNAME	= "setDefaultJavaLauncher";
		String	SET_DEFAULT_JAR_PATHNAME			= "setDefaultJar";
		String	ACCEPT								= "accept";
		String	CLOSE								= "close";
	}

////////////////////////////////////////////////////////////////////////
//  Enumerated types
////////////////////////////////////////////////////////////////////////


	// ACTION


	public enum Action
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		ADD
		(
			"Add"
		),

		REMOVE
		(
			"Remove"
		);

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private Action(String text)
		{
			this.text = text;
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

		NO_JAVA_LAUNCHER_FILE
		("No Java launcher file was specified."),

		NO_JAR_FILE
		("No JAR file was specified."),

		NO_ICON_FILE
		("No icon file was specified."),

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


	// RESULT CLASS


	public static class Result
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		private static final	char	ENV_VAR_PREFIX	= '%';
		private static final	char	ENV_VAR_SUFFIX	= '%';

		private static final	char	UNIX_FILE_SEPARATOR		= '/';
		private static final	char	WINDOWS_FILE_SEPARATOR	= '\\';

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private Result(String                           javaLauncherPathname,
					   String                           jarPathname,
					   String                           iconPathname,
					   boolean                          removeEntries,
					   FileAssociations.ScriptLifeCycle scriptLifeCycle)
		{
			this.javaLauncherPathname = processPathname(javaLauncherPathname);
			this.jarPathname = processPathname(jarPathname);
			this.iconPathname = processPathname(iconPathname);
			this.removeEntries = removeEntries;
			this.scriptLifeCycle = scriptLifeCycle;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Class methods
	////////////////////////////////////////////////////////////////////

		private static String processPathname(String pathname)
		{
			StringBuilder buffer = new StringBuilder(256);
			for (PropertyString.Span span : PropertyString.getSpans(pathname))
			{
				if (span.getValue() != null)
				{
					if (span.getKind() == PropertyString.Span.Kind.ENVIRONMENT)
					{
						buffer.append(ENV_VAR_PREFIX);
						buffer.append(span.getKey());
						buffer.append(ENV_VAR_SUFFIX);
					}
					else
						buffer.append(span.getValue().replace(UNIX_FILE_SEPARATOR, WINDOWS_FILE_SEPARATOR));
				}
			}
			return buffer.toString();
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		String								javaLauncherPathname;
		String								jarPathname;
		String								iconPathname;
		boolean								removeEntries;
		FileAssociations.ScriptLifeCycle	scriptLifeCycle;

	}

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	private FileAssociationDialog(Window owner)
	{

		// Call superclass constructor
		super(owner, TITLE_STR, Dialog.ModalityType.APPLICATION_MODAL);

		// Set icons
		setIconImages(owner.getIconImages());

		// Initialise instance variables
		javaLauncherFileChooser = new JFileChooser(System.getProperty(JAVA_HOME_KEY));
		javaLauncherFileChooser.setDialogTitle(JAVA_LAUNCHER_FILE_STR);
		javaLauncherFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		javaLauncherFileChooser.setApproveButtonMnemonic(KeyEvent.VK_S);
		javaLauncherFileChooser.setApproveButtonToolTipText(SELECT_FILE_STR);
		javaLauncherFileChooser.setFileFilter(new FilenameSuffixFilter(AppConstants.EXE_FILES_STR,
																	   AppConstants.EXE_FILENAME_EXTENSION));

		jarFileChooser = new JFileChooser(System.getProperty(USER_DIR_KEY));
		jarFileChooser.setDialogTitle(JAR_FILE_STR);
		jarFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		jarFileChooser.setApproveButtonMnemonic(KeyEvent.VK_S);
		jarFileChooser.setApproveButtonToolTipText(SELECT_FILE_STR);
		jarFileChooser.setFileFilter(new FilenameSuffixFilter(AppConstants.JAR_FILES_STR,
															  AppConstants.JAR_FILENAME_EXTENSION));

		iconFileChooser = new JFileChooser(System.getProperty(USER_DIR_KEY));
		iconFileChooser.setDialogTitle(ICON_FILE_STR);
		iconFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		iconFileChooser.setApproveButtonMnemonic(KeyEvent.VK_S);
		iconFileChooser.setApproveButtonToolTipText(SELECT_FILE_STR);
		iconFileChooser.setFileFilter(new FilenameSuffixFilter(AppConstants.ICON_FILES_STR,
															   AppConstants.ICON_FILENAME_EXTENSION));

		actionComponents = new ArrayList<>();


		//----  Control panel

		GridBagLayout gridBag = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();

		JPanel controlPanel = new JPanel(gridBag);
		GuiUtils.setPaddedLineBorder(controlPanel);

		int gridY = 0;

		// Label: action
		JLabel actionLabel = new FLabel(ACTION_STR);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(actionLabel, gbc);
		controlPanel.add(actionLabel);

		// Combo box: action
		actionComboBox = new FComboBox<>(Action.values());
		actionComboBox.setSelectedValue(action);
		actionComboBox.setActionCommand(Command.SELECT_ACTION);
		actionComboBox.addActionListener(this);

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(actionComboBox, gbc);
		controlPanel.add(actionComboBox);

		// Label: Java launcher
		JLabel javaLauncherLabel = new FLabel(JAVA_LAUNCHER_STR);
		actionComponents.add(javaLauncherLabel);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(javaLauncherLabel, gbc);
		controlPanel.add(javaLauncherLabel);

		// Panel: Java launcher
		JPanel javaLauncherPanel = new JPanel(gridBag);
		actionComponents.add(javaLauncherPanel);

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(javaLauncherPanel, gbc);
		controlPanel.add(javaLauncherPanel);

		// Panel: Java launcher pathname
		javaLauncherPathnameField = new FPathnameField(javaLauncherPathname);
		FPathnameField.addObserver(KEY, javaLauncherPathnameField);
		JPanel javaLauncherPathnamePanel = new PathnamePanel(javaLauncherPathnameField,
															 Command.CHOOSE_JAVA_LAUNCHER_FILE, this);

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(0, 0, 0, 0);
		gridBag.setConstraints(javaLauncherPathnamePanel, gbc);
		javaLauncherPanel.add(javaLauncherPathnamePanel);

		// Button: Java launcher default
		javaLauncherDefaultButton = new FButton(DEFAULT_STR);
		javaLauncherDefaultButton.setMargin(BUTTON_MARGINS);
		javaLauncherDefaultButton.setActionCommand(Command.SET_DEFAULT_JAVA_LAUNCHER_PATHNAME);
		javaLauncherDefaultButton.addActionListener(this);

		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(0, 6, 0, 0);
		gridBag.setConstraints(javaLauncherDefaultButton, gbc);
		javaLauncherPanel.add(javaLauncherDefaultButton);

		// Label: JAR
		JLabel jarLabel = new FLabel(JAR_STR);
		actionComponents.add(jarLabel);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(jarLabel, gbc);
		controlPanel.add(jarLabel);

		// Panel: JAR
		JPanel jarPanel = new JPanel(gridBag);
		actionComponents.add(jarPanel);

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(jarPanel, gbc);
		controlPanel.add(jarPanel);

		// Panel: JAR pathname
		jarPathnameField = new FPathnameField(jarPathname);
		FPathnameField.addObserver(KEY, jarPathnameField);
		JPanel jarPathnamePanel = new PathnamePanel(jarPathnameField, Command.CHOOSE_JAR_FILE, this);

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(0, 0, 0, 0);
		gridBag.setConstraints(jarPathnamePanel, gbc);
		jarPanel.add(jarPathnamePanel);

		// Button: JAR default
		jarDefaultButton = new FButton(DEFAULT_STR);
		jarDefaultButton.setMargin(BUTTON_MARGINS);
		jarDefaultButton.setActionCommand(Command.SET_DEFAULT_JAR_PATHNAME);
		jarDefaultButton.addActionListener(this);

		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(0, 6, 0, 0);
		gridBag.setConstraints(jarDefaultButton, gbc);
		jarPanel.add(jarDefaultButton);

		// Label: icon
		JLabel iconLabel = new FLabel(ICON_STR);
		actionComponents.add(iconLabel);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(iconLabel, gbc);
		controlPanel.add(iconLabel);

		// Panel: icon pathname
		iconPathnameField = new FPathnameField(iconPathname);
		FPathnameField.addObserver(KEY, iconPathnameField);
		JPanel iconPathnamePanel = new PathnamePanel(iconPathnameField, Command.CHOOSE_ICON_FILE, this);
		actionComponents.add(iconPathnamePanel);

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(iconPathnamePanel, gbc);
		controlPanel.add(iconPathnamePanel);

		// Check box: files must exist
		filesMustExistCheckBox = new FCheckBox(FILES_MUST_EXIST_STR);
		actionComponents.add(filesMustExistCheckBox);
		filesMustExistCheckBox.setSelected(filesMustExist);

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(filesMustExistCheckBox, gbc);
		controlPanel.add(filesMustExistCheckBox);

		// Label: script life cycle
		JLabel scriptLifeCycleLabel = new FLabel(SCRIPT_LIFE_CYCLE_STR);

		gbc.gridx = 0;
		gbc.gridy = gridY;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(scriptLifeCycleLabel, gbc);
		controlPanel.add(scriptLifeCycleLabel);

		// Combo box: script life cycle
		scriptLifeCycleComboBox = new FComboBox<>(FileAssociations.ScriptLifeCycle.values());
		scriptLifeCycleComboBox.setSelectedValue(scriptLifeCycle);

		gbc.gridx = 1;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = AppConstants.COMPONENT_INSETS;
		gridBag.setConstraints(scriptLifeCycleComboBox, gbc);
		controlPanel.add(scriptLifeCycleComboBox);

		// Update components
		updateComponents();


		//----  Button panel

		JPanel buttonPanel = new JPanel(new GridLayout(1, 0, 8, 0));
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

		JPanel mainPanel = new JPanel(gridBag);
		mainPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

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
		mainPanel.add(controlPanel);

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

		// Show dialog
		setVisible(true);

	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	public static Result showDialog(Component parent)
	{
		return new FileAssociationDialog(GuiUtils.getWindow(parent)).getResult();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : ActionListener interface
////////////////////////////////////////////////////////////////////////

	public void actionPerformed(ActionEvent event)
	{
		String command = event.getActionCommand();

		if (command.equals(Command.SELECT_ACTION))
			onSelectAction();

		else if (command.equals(Command.CHOOSE_JAVA_LAUNCHER_FILE))
			onChooseJavaLauncherFile();

		else if (command.equals(Command.CHOOSE_JAR_FILE))
			onChooseJarFile();

		else if (command.equals(Command.CHOOSE_ICON_FILE))
			onChooseIconFile();

		else if (command.equals(Command.SET_DEFAULT_JAVA_LAUNCHER_PATHNAME))
			onSetDefaultJavaLauncherPathname();

		else if (command.equals(Command.SET_DEFAULT_JAR_PATHNAME))
			onSetDefaultJarPathname();

		else if (command.equals(Command.ACCEPT))
			onAccept();

		else if (command.equals(Command.CLOSE))
			onClose();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	private Result getResult()
	{
		return (accepted ? new Result(javaLauncherPathname, jarPathname, iconPathname, action == Action.REMOVE,
									  scriptLifeCycle)
						 : null);
	}

	//------------------------------------------------------------------

	private void updateComponents()
	{
		boolean enabled = (actionComboBox.getSelectedValue() == Action.ADD);
		for (Component component : actionComponents)
			GuiUtils.setAllEnabled(component, enabled);
		if (enabled)
		{
			javaLauncherDefaultButton.setEnabled(defaultJavaLauncherPath != null);
			jarDefaultButton.setEnabled(defaultJarPath != null);
		}
	}

	//------------------------------------------------------------------

	private void validateUserInput()
		throws AppException
	{
		if (actionComboBox.getSelectedValue() == Action.ADD)
		{
			boolean mustExist = filesMustExistCheckBox.isSelected();

			// Java launcher pathname
			try
			{
				if (javaLauncherPathnameField.isEmpty())
					throw new AppException(ErrorId.NO_JAVA_LAUNCHER_FILE);
				if (mustExist)
				{
					File file = javaLauncherPathnameField.getFile();
					try
					{
						if (mustExist && !file.isFile())
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
				GuiUtils.setFocus(javaLauncherPathnameField);
				throw e;
			}

			// JAR pathname
			try
			{
				if (jarPathnameField.isEmpty())
					throw new AppException(ErrorId.NO_JAR_FILE);
				if (mustExist)
				{
					File file = jarPathnameField.getFile();
					try
					{
						if (!file.isFile())
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
				GuiUtils.setFocus(jarPathnameField);
				throw e;
			}

			// Icon pathname
			try
			{
				if (iconPathnameField.isEmpty())
					throw new AppException(ErrorId.NO_ICON_FILE);
				if (mustExist)
				{
					File file = iconPathnameField.getFile();
					try
					{
						if (!file.isFile())
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
				GuiUtils.setFocus(iconPathnameField);
				throw e;
			}
		}
	}

	//------------------------------------------------------------------

	private void onSelectAction()
	{
		updateComponents();
	}

	//------------------------------------------------------------------

	private void onChooseJavaLauncherFile()
	{
		if (!javaLauncherPathnameField.isEmpty())
			javaLauncherFileChooser.setSelectedFile(javaLauncherPathnameField.getCanonicalFile());
		javaLauncherFileChooser.rescanCurrentDirectory();
		if (javaLauncherFileChooser.showDialog(this, SELECT_STR) == JFileChooser.APPROVE_OPTION)
			javaLauncherPathnameField.setFile(javaLauncherFileChooser.getSelectedFile());
	}

	//------------------------------------------------------------------

	private void onChooseJarFile()
	{
		if (!jarPathnameField.isEmpty())
			jarFileChooser.setSelectedFile(jarPathnameField.getCanonicalFile());
		jarFileChooser.rescanCurrentDirectory();
		if (jarFileChooser.showDialog(this, SELECT_STR) == JFileChooser.APPROVE_OPTION)
			jarPathnameField.setFile(jarFileChooser.getSelectedFile());
	}

	//------------------------------------------------------------------

	private void onChooseIconFile()
	{
		if (!iconPathnameField.isEmpty())
			iconFileChooser.setSelectedFile(iconPathnameField.getCanonicalFile());
		iconFileChooser.rescanCurrentDirectory();
		if (iconFileChooser.showDialog(this, SELECT_STR) == JFileChooser.APPROVE_OPTION)
			iconPathnameField.setFile(iconFileChooser.getSelectedFile());
	}

	//------------------------------------------------------------------

	private void onSetDefaultJavaLauncherPathname()
	{
		javaLauncherPathnameField.setText(defaultJavaLauncherPath.toString());
	}

	//------------------------------------------------------------------

	private void onSetDefaultJarPathname()
	{
		jarPathnameField.setText(defaultJarPath.toString());
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
			JOptionPane.showMessageDialog(this, e, TITLE_STR, JOptionPane.ERROR_MESSAGE);
		}
	}

	//------------------------------------------------------------------

	private void onClose()
	{
		FPathnameField.removeObservers(KEY);

		location = getLocation();
		action = actionComboBox.getSelectedValue();
		javaLauncherPathname = javaLauncherPathnameField.getText();
		jarPathname = jarPathnameField.getText();
		iconPathname = iconPathnameField.getText();
		filesMustExist = filesMustExistCheckBox.isSelected();
		scriptLifeCycle = scriptLifeCycleComboBox.getSelectedValue();
		setVisible(false);
		dispose();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class variables
////////////////////////////////////////////////////////////////////////

	private static	Point								location;
	private static	Action								action					= Action.ADD;
	private static	String								javaLauncherPathname;
	private static	String								jarPathname;
	private static	String								iconPathname;
	private static	boolean								filesMustExist			= true;
	private static	FileAssociations.ScriptLifeCycle	scriptLifeCycle			=
													FileAssociations.ScriptLifeCycle.WRITE_EXECUTE_DELETE;
	private static	Path								defaultJavaLauncherPath;
	private static	Path								defaultJarPath;

////////////////////////////////////////////////////////////////////////
//  Static initialiser
////////////////////////////////////////////////////////////////////////

	static
	{
		// Java launcher path
		String pathname = System.getProperty(JAVA_HOME_KEY);
		if (pathname != null)
		{
			try
			{
				Path path = Paths.get(pathname, JAVA_LAUNCHER_PATHNAME);
				if (Files.exists(path))
					defaultJavaLauncherPath = path.toAbsolutePath();
			}
			catch (Exception e)
			{
				// ignore
			}
		}

		// JAR path
		try
		{
			Path path = Paths.get(FileAssociationDialog.class.getProtectionDomain().getCodeSource().getLocation().toURI());
			if (Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS)
					&& PathnameUtils.suffixMatches(path, AppConstants.JAR_FILENAME_EXTENSION))
				defaultJarPath = path.toAbsolutePath();
		}
		catch (Exception e)
		{
			// ignore
		}
	}

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private	boolean										accepted;
	private	FComboBox<Action>							actionComboBox;
	private	FPathnameField								javaLauncherPathnameField;
	private	JButton										javaLauncherDefaultButton;
	private	FPathnameField								jarPathnameField;
	private	JButton										jarDefaultButton;
	private	FPathnameField								iconPathnameField;
	private	JCheckBox									filesMustExistCheckBox;
	private	FComboBox<FileAssociations.ScriptLifeCycle>	scriptLifeCycleComboBox;
	private	JFileChooser								javaLauncherFileChooser;
	private	JFileChooser								jarFileChooser;
	private	JFileChooser								iconFileChooser;
	private	List<Component>								actionComponents;

}

//----------------------------------------------------------------------
