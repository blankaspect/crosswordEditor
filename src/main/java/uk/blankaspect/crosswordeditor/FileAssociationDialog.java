/*====================================================================*\

FileAssociationDialog.java

Class: file-association dialog.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.crosswordeditor;

//----------------------------------------------------------------------


// IMPORTS


import java.awt.Component;
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
import uk.blankaspect.common.filesystem.PathUtils;

import uk.blankaspect.common.platform.windows.FileAssociations;

import uk.blankaspect.common.property.PropertyString;

import uk.blankaspect.ui.swing.action.KeyAction;

import uk.blankaspect.ui.swing.button.FButton;

import uk.blankaspect.ui.swing.checkbox.FCheckBox;

import uk.blankaspect.ui.swing.combobox.FComboBox;

import uk.blankaspect.ui.swing.container.PathnamePanel;

import uk.blankaspect.ui.swing.filechooser.FileChooserUtils;

import uk.blankaspect.ui.swing.label.FLabel;

import uk.blankaspect.ui.swing.misc.GuiUtils;

import uk.blankaspect.ui.swing.workaround.LinuxWorkarounds;

//----------------------------------------------------------------------


// CLASS: FILE-ASSOCIATION DIALOG


class FileAssociationDialog
	extends JDialog
	implements ActionListener
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	private static final	Insets	BUTTON_MARGINS	= new Insets(2, 4, 2, 4);

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

	// Keys of system properties
	private interface SystemPropertyKey
	{
		String	JAVA_HOME_DIR	= "java.home";
		String	WORKING_DIR		= "user.dir";
	}

////////////////////////////////////////////////////////////////////////
//  Class variables
////////////////////////////////////////////////////////////////////////

	private static	Point								location;
	private static	Action								action			= Action.ADD;
	private static	String								javaLauncherPathname;
	private static	String								jarPathname;
	private static	String								iconPathname;
	private static	boolean								filesMustExist	= true;
	private static	FileAssociations.ScriptLifeCycle	scriptLifeCycle	=
			FileAssociations.ScriptLifeCycle.WRITE_EXECUTE_DELETE;
	private static	Path								defaultJavaLauncherLocation;
	private static	Path								defaultJarLocation;

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

////////////////////////////////////////////////////////////////////////
//  Static initialiser
////////////////////////////////////////////////////////////////////////

	static
	{
		// Location of Java launcher
		String pathname = System.getProperty(SystemPropertyKey.JAVA_HOME_DIR);
		if (pathname != null)
		{
			try
			{
				Path location = Path.of(pathname, JAVA_LAUNCHER_PATHNAME);
				if (Files.exists(location))
					defaultJavaLauncherLocation = PathUtils.abs(location);
			}
			catch (Exception e)
			{
				// ignore
			}
		}

		// Location of JAR
		try
		{
			Path location =
					Path.of(FileAssociationDialog.class.getProtectionDomain().getCodeSource().getLocation().toURI());
			if (Files.isRegularFile(location, LinkOption.NOFOLLOW_LINKS)
					&& PathnameUtils.suffixMatches(location, AppConstants.JAR_FILENAME_EXTENSION))
				defaultJarLocation = PathUtils.abs(location);
		}
		catch (Exception e)
		{
			// ignore
		}
	}

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	private FileAssociationDialog(
		Window	owner)
	{
		// Call superclass constructor
		super(owner, TITLE_STR, ModalityType.APPLICATION_MODAL);

		// Set icons
		setIconImages(owner.getIconImages());

		// Initialise instance variables
		javaLauncherFileChooser = new JFileChooser(System.getProperty(SystemPropertyKey.JAVA_HOME_DIR));
		javaLauncherFileChooser.setDialogTitle(JAVA_LAUNCHER_FILE_STR);
		javaLauncherFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		javaLauncherFileChooser.setApproveButtonMnemonic(KeyEvent.VK_S);
		javaLauncherFileChooser.setApproveButtonToolTipText(SELECT_FILE_STR);
		FileChooserUtils.setFilter(javaLauncherFileChooser, AppConstants.EXE_FILE_FILTER);

		jarFileChooser = new JFileChooser(System.getProperty(SystemPropertyKey.WORKING_DIR));
		jarFileChooser.setDialogTitle(JAR_FILE_STR);
		jarFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		jarFileChooser.setApproveButtonMnemonic(KeyEvent.VK_S);
		jarFileChooser.setApproveButtonToolTipText(SELECT_FILE_STR);
		FileChooserUtils.setFilter(jarFileChooser, AppConstants.JAR_FILE_FILTER);

		iconFileChooser = new JFileChooser(System.getProperty(SystemPropertyKey.WORKING_DIR));
		iconFileChooser.setDialogTitle(ICON_FILE_STR);
		iconFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		iconFileChooser.setApproveButtonMnemonic(KeyEvent.VK_S);
		iconFileChooser.setApproveButtonToolTipText(SELECT_FILE_STR);
		FileChooserUtils.setFilter(iconFileChooser, AppConstants.ICON_FILE_FILTER);

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

		// Handle window events
		addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowOpened(
				WindowEvent	event)
			{
				// WORKAROUND for a bug that has been observed on Linux/GNOME whereby a window is displaced downwards
				// when its location is set.  The error in the y coordinate is the height of the title bar of the
				// window.  The workaround is to set the location of the window again with an adjustment for the error.
				LinuxWorkarounds.fixWindowYCoord(event.getWindow(), location);
			}

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

		// Set location of dialog
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

	public static Result showDialog(
		Component	parent)
	{
		return new FileAssociationDialog(GuiUtils.getWindow(parent)).getResult();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : ActionListener interface
////////////////////////////////////////////////////////////////////////

	@Override
	public void actionPerformed(
		ActionEvent	event)
	{
		switch (event.getActionCommand())
		{
			case Command.SELECT_ACTION                      -> onSelectAction();
			case Command.CHOOSE_JAVA_LAUNCHER_FILE          -> onChooseJavaLauncherFile();
			case Command.CHOOSE_JAR_FILE                    -> onChooseJarFile();
			case Command.CHOOSE_ICON_FILE                   -> onChooseIconFile();
			case Command.SET_DEFAULT_JAVA_LAUNCHER_PATHNAME -> onSetDefaultJavaLauncherPathname();
			case Command.SET_DEFAULT_JAR_PATHNAME           -> onSetDefaultJarPathname();
			case Command.ACCEPT                             -> onAccept();
			case Command.CLOSE                              -> onClose();
		}
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	private Result getResult()
	{
		return accepted
				? new Result(javaLauncherPathname, jarPathname, iconPathname, action == Action.REMOVE, scriptLifeCycle)
				: null;
	}

	//------------------------------------------------------------------

	private void updateComponents()
	{
		boolean enabled = (actionComboBox.getSelectedValue() == Action.ADD);
		for (Component component : actionComponents)
			GuiUtils.setAllEnabled(component, enabled);
		if (enabled)
		{
			javaLauncherDefaultButton.setEnabled(defaultJavaLauncherLocation != null);
			jarDefaultButton.setEnabled(defaultJarLocation != null);
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
		javaLauncherPathnameField.setText(defaultJavaLauncherLocation.toString());
	}

	//------------------------------------------------------------------

	private void onSetDefaultJarPathname()
	{
		jarPathnameField.setText(defaultJarLocation.toString());
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
//  Enumerated types
////////////////////////////////////////////////////////////////////////


	// ENUMERATION: ACTION


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
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	String	text;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private Action(
			String	text)
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

	}


	//==================================================================


	// ENUMERATION: ERROR IDENTIFIERS


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


	// CLASS: RESULT


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
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		String								javaLauncherPathname;
		String								jarPathname;
		String								iconPathname;
		boolean								removeEntries;
		FileAssociations.ScriptLifeCycle	scriptLifeCycle;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private Result(
			String								javaLauncherPathname,
			String								jarPathname,
			String								iconPathname,
			boolean								removeEntries,
			FileAssociations.ScriptLifeCycle	scriptLifeCycle)
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

		private static String processPathname(
			String	pathname)
		{
			StringBuilder buffer = new StringBuilder(256);
			for (PropertyString.Span span : PropertyString.getSpans(pathname))
			{
				if (span.text() != null)
				{
					if (span.kind() == PropertyString.SpanKind.ENVIRONMENT)
					{
						buffer.append(ENV_VAR_PREFIX);
						buffer.append(span.key());
						buffer.append(ENV_VAR_SUFFIX);
					}
					else
						buffer.append(span.text().replace(UNIX_FILE_SEPARATOR, WINDOWS_FILE_SEPARATOR));
				}
			}
			return buffer.toString();
		}

		//--------------------------------------------------------------

	}

	//==================================================================

}

//----------------------------------------------------------------------
