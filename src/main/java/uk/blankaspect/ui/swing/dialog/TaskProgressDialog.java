/*====================================================================*\

TaskProgressDialog.java

Class: task-progress dialog.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.ui.swing.dialog;

//----------------------------------------------------------------------


// IMPORTS


import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
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

import java.net.URL;

import java.util.Objects;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import uk.blankaspect.common.exception.AppException;
import uk.blankaspect.common.exception.ExceptionUtils;

import uk.blankaspect.common.misc.IProgressListener;
import uk.blankaspect.common.misc.Task;

import uk.blankaspect.common.number.NumberUtils;

import uk.blankaspect.common.ui.progress.IProgressView;

import uk.blankaspect.ui.swing.action.KeyAction;

import uk.blankaspect.ui.swing.button.FButton;

import uk.blankaspect.ui.swing.font.FontKey;
import uk.blankaspect.ui.swing.font.FontUtils;

import uk.blankaspect.ui.swing.label.FLabel;

import uk.blankaspect.ui.swing.misc.GuiConstants;
import uk.blankaspect.ui.swing.misc.GuiUtils;

import uk.blankaspect.ui.swing.text.TextRendering;
import uk.blankaspect.ui.swing.text.TextUtils;

import uk.blankaspect.ui.swing.workaround.LinuxWorkarounds;

//----------------------------------------------------------------------


// CLASS: TASK-PROGRESS DIALOG


public class TaskProgressDialog
	extends JDialog
	implements IProgressListener, IProgressView
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	private static final	int		PROGRESS_UPDATE_INTERVAL	= 500;

	private static final	int		INFO_FIELD_WIDTH	= 480;

	private static final	int		PROGRESS_BAR_WIDTH		= INFO_FIELD_WIDTH;
	private static final	int		PROGRESS_BAR_HEIGHT		= 15;
	private static final	int		PROGRESS_BAR_MAX_VALUE	= 10000;

	private static final	String	TIME_ELAPSED_STR	= "Time elapsed";
	private static final	String	TIME_REMAINING_STR	= "Estimated time remaining";

	// Commands
	private interface Command
	{
		String	CLOSE	= "close";
	}

////////////////////////////////////////////////////////////////////////
//  Class variables
////////////////////////////////////////////////////////////////////////

	private static	Point	location;

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private volatile	boolean	stopped;

	private	DeferredOutput		deferredOutput;
	private	Object				deferredOutputLock;
	private	int					timeProgressIndex;
	private	long				startTime;
	private	long				updateTime;
	private	InfoField			infoField;
	private	JProgressBar[]		progressBars;
	private	TimeField			timeElapsedField;
	private	TimeField			timeRemainingField;
	private	JButton				cancelButton;

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	protected TaskProgressDialog(
		Window	owner,
		String	title,
		Task	task,
		int		delay,
		int		numProgressBars,
		int		timeProgressIndex,
		boolean	canCancel)
		throws AppException
	{
		// Call superclass constructor
		super(owner, title, ModalityType.APPLICATION_MODAL);

		// Set icons
		if (owner != null)
			setIconImages(owner.getIconImages());

		// Initialise instance variables
		deferredOutputLock = new Object();
		this.timeProgressIndex = timeProgressIndex;

		// Create handler for 'close' command
		ActionListener closeCommandHandler = new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent event)
			{
				if (event.getActionCommand().equals(Command.CLOSE))
					onClose();
			}
		};


		//----  Info field

		infoField = new InfoField();


		//----  Progress bars

		progressBars = new JProgressBar[numProgressBars];
		for (int i = 0; i < progressBars.length; i++)
		{
			progressBars[i] = new JProgressBar(0, PROGRESS_BAR_MAX_VALUE);
			progressBars[i].setPreferredSize(new Dimension(PROGRESS_BAR_WIDTH, PROGRESS_BAR_HEIGHT));
		}


		//----  Time pane

		GridBagLayout gridBag = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();

		int gridY = 0;

		boolean hasTimeFields = timeProgressIndex >= 0;
		JPanel timePane = null;
		if (hasTimeFields)
		{
			timePane = new JPanel(gridBag);

			// Label: time elapsed
			JLabel timeElapsedLabel = new FLabel(TIME_ELAPSED_STR);

			gbc.gridx = 0;
			gbc.gridy = gridY;
			gbc.gridwidth = 1;
			gbc.gridheight = 1;
			gbc.weightx = 0.0;
			gbc.weighty = 0.0;
			gbc.anchor = GridBagConstraints.LINE_END;
			gbc.fill = GridBagConstraints.NONE;
			gbc.insets = new Insets(0, 0, 0, 0);
			gridBag.setConstraints(timeElapsedLabel, gbc);
			timePane.add(timeElapsedLabel);

			// Field: time elapsed
			timeElapsedField = new TimeField();

			gbc.gridx = 1;
			gbc.gridy = gridY++;
			gbc.gridwidth = 1;
			gbc.gridheight = 1;
			gbc.weightx = 0.0;
			gbc.weighty = 0.0;
			gbc.anchor = GridBagConstraints.LINE_START;
			gbc.fill = GridBagConstraints.NONE;
			gbc.insets = new Insets(0, 4, 0, 0);
			gridBag.setConstraints(timeElapsedField, gbc);
			timePane.add(timeElapsedField);

			// Label: time remaining
			JLabel timeRemainingLabel = new FLabel(TIME_REMAINING_STR);

			gbc.gridx = 0;
			gbc.gridy = gridY;
			gbc.gridwidth = 1;
			gbc.gridheight = 1;
			gbc.weightx = 0.0;
			gbc.weighty = 0.0;
			gbc.anchor = GridBagConstraints.LINE_END;
			gbc.fill = GridBagConstraints.NONE;
			gbc.insets = new Insets(1, 0, 0, 0);
			gridBag.setConstraints(timeRemainingLabel, gbc);
			timePane.add(timeRemainingLabel);

			// Field: time remaining
			timeRemainingField = new TimeField();

			gbc.gridx = 1;
			gbc.gridy = gridY++;
			gbc.gridwidth = 1;
			gbc.gridheight = 1;
			gbc.weightx = 0.0;
			gbc.weighty = 0.0;
			gbc.anchor = GridBagConstraints.LINE_START;
			gbc.fill = GridBagConstraints.NONE;
			gbc.insets = new Insets(1, 4, 0, 0);
			gridBag.setConstraints(timeRemainingField, gbc);
			timePane.add(timeRemainingField);
		}


		//----  Button pane

		JPanel buttonPane = null;
		if (canCancel)
		{
			buttonPane = new JPanel(new GridLayout(1, 0, 0, 0));

			// Button: cancel
			cancelButton = new FButton(GuiConstants.CANCEL_STR);
			cancelButton.setActionCommand(Command.CLOSE);
			cancelButton.addActionListener(closeCommandHandler);
			buttonPane.add(cancelButton);
		}


		//----  Bottom pane

		JComponent bottomPane = null;
		if (hasTimeFields && canCancel)
		{
			bottomPane = new JPanel(gridBag);

			int gridX = 0;

			gbc.gridx = gridX++;
			gbc.gridy = 0;
			gbc.gridwidth = 1;
			gbc.gridheight = 1;
			gbc.weightx = 0.5;
			gbc.weighty = 0.0;
			gbc.anchor = GridBagConstraints.WEST;
			gbc.fill = GridBagConstraints.NONE;
			gbc.insets = new Insets(0, 0, 0, 0);
			gridBag.setConstraints(timePane, gbc);
			bottomPane.add(timePane);

			gbc.gridx = gridX++;
			gbc.gridy = 0;
			gbc.gridwidth = 1;
			gbc.gridheight = 1;
			gbc.weightx = 0.5;
			gbc.weighty = 0.0;
			gbc.anchor = GridBagConstraints.EAST;
			gbc.fill = GridBagConstraints.NONE;
			gbc.insets = new Insets(0, 24, 0, 0);
			gridBag.setConstraints(buttonPane, gbc);
			bottomPane.add(buttonPane);
		}
		else if (hasTimeFields && !canCancel)
			bottomPane = timePane;
		else if (!hasTimeFields && canCancel)
			bottomPane = buttonPane;

		if (bottomPane == null)
			bottomPane = GuiUtils.spacer();
		else
			bottomPane.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));


		//----  Main pane

		JPanel mainPane = new JPanel(gridBag);
		mainPane.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));

		gridY = 0;

		gbc.gridx = 0;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.NORTH;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(8, 2, 6, 2);
		gridBag.setConstraints(infoField, gbc);
		mainPane.add(infoField);

		for (int i = 0; i < progressBars.length; i++)
		{
			gbc.gridx = 0;
			gbc.gridy = gridY++;
			gbc.gridwidth = 1;
			gbc.gridheight = 1;
			gbc.weightx = 0.0;
			gbc.weighty = 0.0;
			gbc.anchor = GridBagConstraints.NORTH;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.insets = new Insets(6, 0, 0, 0);
			gridBag.setConstraints(progressBars[i], gbc);
			mainPane.add(progressBars[i]);
		}

		gbc.gridx = 0;
		gbc.gridy = gridY++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.NORTH;
		gbc.fill = hasTimeFields ? GridBagConstraints.HORIZONTAL : GridBagConstraints.NONE;
		gbc.insets = new Insets(3, 0, 0, 0);
		gridBag.setConstraints(bottomPane, gbc);
		mainPane.add(bottomPane);

		// Add commands to action map
		KeyAction.create(mainPane, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT,
						 KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), Command.CLOSE, closeCommandHandler);


		//----  Window

		// Set content pane
		setContentPane(mainPane);

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

				synchronized (deferredOutputLock)
				{
					if (deferredOutput != null)
						deferredOutput.write();
				}
			}

			@Override
			public void windowClosing(
				WindowEvent	event)
			{
				if (isVisible())
					location = getLocation();
				if (stopped)
					dispose();
				else
					Task.setCancelled(true);
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
		getRootPane().setDefaultButton(cancelButton);

		// Start task
		Task.setProgressView(this);
		Task.setException(null, true);
		Task.setCancelled(false);
		task.start();

		// Delay before making dialog visible
		long endTime = System.currentTimeMillis() + delay;
		while (!stopped)
		{
			if (System.currentTimeMillis() >= endTime)
			{
				setVisible(!stopped);
				break;
			}
			try
			{
				Thread.sleep(100);
			}
			catch (InterruptedException e)
			{
				// ignore
			}
		}

		// Throw any exception from task thread
		Task.throwIfException();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : IProgressView interface
////////////////////////////////////////////////////////////////////////

	@Override
	public void setInfo(
		String	text)
	{
		setInfo(text, (File)null);
	}

	//------------------------------------------------------------------

	@Override
	public void setInfo(
		String	text,
		File	file)
	{
		if (isVisible())
		{
			SwingUtilities.invokeLater(() ->
					infoField.setText((file == null)
											? text
											: pathnameText(text, getPathname(file), File.separatorChar)));
		}
		else
		{
			synchronized (deferredOutputLock)
			{
				if (deferredOutput == null)
					deferredOutput = new DeferredOutput();
				deferredOutput.init(text, file, null);
			}
		}
	}

	//------------------------------------------------------------------

	@Override
	public int getNumProgressIndicators()
	{
		return progressBars.length;
	}

	//------------------------------------------------------------------

	@Override
	public void setProgress(
		int		index,
		double	value)
	{
		if (isVisible())
			SwingUtilities.invokeLater(() -> setProgress2(index, value));
		else
		{
			synchronized (deferredOutputLock)
			{
				if (deferredOutput == null)
					deferredOutput = new DeferredOutput();
				deferredOutput.progresses[index] = value;
			}
		}
	}

	//------------------------------------------------------------------

	@Override
	public void waitForIdle()
	{
		EventQueue eventQueue = getToolkit().getSystemEventQueue();
		while (eventQueue.peekEvent() != null)
		{
			// do nothing
		}
	}

	//------------------------------------------------------------------

	@Override
	public void close()
	{
		stopped = true;
		SwingUtilities.invokeLater(() ->
		{
			if (isVisible())
				dispatchEvent(new WindowEvent(TaskProgressDialog.this, WindowEvent.WINDOW_CLOSING));
		});
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : IProgressListener interface
////////////////////////////////////////////////////////////////////////

	@Override
	public void setProgress(
		double	fractionDone)
	{
		setProgress(0, fractionDone);
	}

	//------------------------------------------------------------------

	@Override
	public boolean isTaskCancelled()
	{
		return Task.isCancelled();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	public void setInfo(
		String	text,
		URL		url)
	{
		if (isVisible())
		{
			SwingUtilities.invokeLater(() ->
					infoField.setText((url == null) ? text : pathnameText(text, url.toString(), '/')));
		}
		else
		{
			synchronized (deferredOutputLock)
			{
				if (deferredOutput == null)
					deferredOutput = new DeferredOutput();
				deferredOutput.init(text, null, url);
			}
		}
	}

	//------------------------------------------------------------------

	protected int getNumProgressBars()
	{
		return progressBars.length;
	}

	//------------------------------------------------------------------

	protected String getPathname(
		File	file)
	{
		String pathname = null;
		try
		{
			pathname = file.getCanonicalPath();
		}
		catch (Exception e)
		{
			ExceptionUtils.printTopOfStack(e);
			pathname = file.getAbsolutePath();
		}
		return pathname;
	}

	//------------------------------------------------------------------

	private String pathnameText(
		String	text,
		String	pathname,
		char	separatorChar)
	{
		text = (text == null) ? "" : text + " ";
		FontMetrics fontMetrics = infoField.getFontMetrics(infoField.getFont());
		int maxWidth = infoField.getWidth() - fontMetrics.stringWidth(text);
		return text + TextUtils.getLimitedWidthPathname(pathname, fontMetrics, maxWidth, separatorChar);
	}

	//------------------------------------------------------------------

	private void setProgress2(
		int		index,
		double	value)
	{
		if (value < 0.0)
		{
			progressBars[index].setIndeterminate(true);

			if (timeElapsedField != null)
			{
				timeElapsedField.setText(null);
				timeRemainingField.setText(null);
			}
		}
		else
		{
			if (progressBars[index].isIndeterminate())
				progressBars[index].setIndeterminate(false);
			progressBars[index].setValue((int)Math.round(value * (double)PROGRESS_BAR_MAX_VALUE));

			if (index == timeProgressIndex)
			{
				if (value == 0.0)
				{
					startTime = System.currentTimeMillis();
					timeElapsedField.setTime(0);
					timeRemainingField.setText(null);
					updateTime = startTime + PROGRESS_UPDATE_INTERVAL;
				}
				else
				{
					long currentTime = System.currentTimeMillis();
					if (currentTime >= updateTime)
					{
						long timeElapsed = currentTime - startTime;
						timeElapsedField.setTime((int)timeElapsed);
						timeRemainingField.setTime((int)Math.round((1.0 / value - 1.0) * (double)timeElapsed) + 500);
						updateTime = currentTime + PROGRESS_UPDATE_INTERVAL;
					}
				}
			}
		}
	}

	//------------------------------------------------------------------

	private void onClose()
	{
		cancelButton.setEnabled(false);
		dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Member classes : non-inner classes
////////////////////////////////////////////////////////////////////////


	// CLASS: INFORMATION FIELD


	private static class InfoField
		extends JComponent
	{

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	String	text;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private InfoField()
		{
			FontUtils.setAppFont(FontKey.MAIN, this);
			setPreferredSize(new Dimension(INFO_FIELD_WIDTH, getFontMetrics(getFont()).getHeight()));
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
			// Create copy of graphics context
			Graphics2D gr2d = GuiUtils.copyGraphicsContext(gr);

			// Draw background
			gr2d.setColor(getBackground());
			gr2d.fillRect(0, 0, getWidth(), getHeight());

			// Draw text
			if (text != null)
			{
				// Set rendering hints for text antialiasing and fractional metrics
				TextRendering.setHints(gr2d);

				// Draw text
				gr2d.setColor(Color.BLACK);
				gr2d.drawString(text, 0, gr2d.getFontMetrics().getAscent());
			}
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		public void setText(
			String	text)
		{
			if (!Objects.equals(text, this.text))
			{
				this.text = text;
				repaint();
			}
		}

		//--------------------------------------------------------------

	}

	//==================================================================


	// CLASS: TIME FIELD


	private static class TimeField
		extends JComponent
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		private static final	int		MIN_TIME	= 0;
		private static final	int		MAX_TIME	= 100 * 60 * 60 * 1000 - 1;

		private static final	String	SEPARATOR	= ":";

		private static final	String	PROTOTYPE_TEXT	= "00" + SEPARATOR + "00" + SEPARATOR + "00";

		private static final	String	OUT_OF_RANGE_STR	= "--";

		private static final	Color	TEXT_COLOUR	= new Color(0, 0, 144);

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	String	text;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private TimeField()
		{
			FontUtils.setAppFont(FontKey.MAIN, this);
			FontMetrics fontMetrics = getFontMetrics(getFont());
			setPreferredSize(new Dimension(fontMetrics.stringWidth(PROTOTYPE_TEXT),
										   fontMetrics.getAscent() + fontMetrics.getDescent()));
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
			// Create copy of graphics context
			Graphics2D gr2d = GuiUtils.copyGraphicsContext(gr);

			// Draw background
			gr2d.setColor(getBackground());
			gr2d.fillRect(0, 0, getWidth(), getHeight());

			// Draw text
			if (text != null)
			{
				// Set rendering hints for text antialiasing and fractional metrics
				TextRendering.setHints(gr2d);

				// Draw text
				FontMetrics fontMetrics = gr2d.getFontMetrics();
				gr2d.setColor(TEXT_COLOUR);
				gr2d.drawString(text, getWidth() - fontMetrics.stringWidth(text), fontMetrics.getAscent());
			}
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		public void setTime(
			int	milliseconds)
		{
			String str = OUT_OF_RANGE_STR;
			if ((milliseconds >= MIN_TIME) && (milliseconds <= MAX_TIME))
			{
				int seconds = milliseconds / 1000;
				int minutes = seconds / 60;
				int hours = minutes / 60;
				str = ((hours == 0)
							? Integer.toString(minutes)
							: Integer.toString(hours) + SEPARATOR + NumberUtils.uIntToDecString(minutes % 60, 2, '0'))
									+ SEPARATOR + NumberUtils.uIntToDecString(seconds % 60, 2, '0');
			}
			setText(str);
		}

		//--------------------------------------------------------------

		public void setText(
			String	text)
		{
			if (!Objects.equals(text, this.text))
			{
				this.text = text;
				repaint();
			}
		}

		//--------------------------------------------------------------

	}

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Member classes : inner classes
////////////////////////////////////////////////////////////////////////


	// CLASS: DEFERRED OUTPUT


	private class DeferredOutput
	{

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	String		text;
		private	File		file;
		private	URL			url;
		private	double[]	progresses;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private DeferredOutput()
		{
			progresses = new double[progressBars.length];
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		private void init(
			String	text,
			File	file,
			URL		url)
		{
			this.text = text;
			this.file = file;
			this.url = url;
		}

		//--------------------------------------------------------------

		private void write()
		{
			infoField.setText((url == null)
									? (file == null)
											? text
											: pathnameText(text, getPathname(file), File.separatorChar)
									: pathnameText(text, url.toString(), '/'));

			for (int i = 0; i < progresses.length; i++)
			{
				if (progresses[i] > 0.0)
					setProgress2(i, 0.0);
				setProgress2(i, progresses[i]);
			}
		}

		//--------------------------------------------------------------

	}

	//==================================================================

}

//----------------------------------------------------------------------
