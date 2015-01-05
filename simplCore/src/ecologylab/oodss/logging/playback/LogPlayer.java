package ecologylab.oodss.logging.playback;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import ecologylab.appframework.PropertiesAndDirectories;
import ecologylab.appframework.SingletonApplicationEnvironment;
import ecologylab.oodss.logging.Logging;
import ecologylab.oodss.logging.MixedInitiativeOp;
import ecologylab.oodss.logging.translationScope.MixedInitiativeOpClassesProvider;
import ecologylab.serialization.SIMPLTranslationException;
import ecologylab.serialization.SimplTypesScope;

/**
 * The main application for playing back log files.
 * 
 * @author Zachary O. Toups (zach@ecologylab.net)
 */
public abstract class LogPlayer<OP extends MixedInitiativeOp, LOG extends Logging<OP>> extends
		SingletonApplicationEnvironment implements ActionListener, WindowListener, PlaybackControlCommands,
		Runnable
{
	private static final int										LOG_LOADED									= 2;

	private static final int										LOG_LOADING									= 1;

	private static final int										LOG_NOT_SELECTED						= 0;

	private List<ActionListener>								actionListeners							= new LinkedList<ActionListener>();

	protected LogPlaybackControls<OP, LOG>			controlsDisplay							= null;

	protected View<OP>													logDisplay									= null;

	protected LogPlaybackControlModel<OP, LOG>	log;

	protected File															logFile											= null;

	protected JFrame														mainFrame;

	protected boolean														playing											= false;

	protected Timer															t;

	/**
	 * Modes: 0 = no file selected (need to get a file name). 1 = file selected and loading. 2 = file
	 * loaded, display log.
	 */

	protected int																mode												= LOG_NOT_SELECTED;

	private boolean															guiShown;

	protected SimplTypesScope									translationScope;

	public final static int											DEFAULT_PLAYBACK_INTERVAL		= 100;

	public final static int											TIMESTAMP_PLAYBACK_INTERVAL	= -1;

	/**
	 * The number of milliseconds between ops when the log is playing. Setting to -1 will try to use
	 * op timestamps.
	 */
	protected int																playbackInterval						= DEFAULT_PLAYBACK_INTERVAL;

	private boolean															logLoadComplete							= false;

	/**
	 * 
	 * @param appName
	 * @param args
	 * @param translationScope
	 * @param opSubclasses
	 *          An array of subclasses of MixedInitiativeOp that will be used to translate the
	 *          operations read in by the player.
	 * @throws SIMPLTranslationException
	 */
	public LogPlayer(String appName, String[] args, SimplTypesScope translationScope,
			Class[] opSubclasses) throws SIMPLTranslationException
	{
		super(appName, translationScope, (SimplTypesScope) null, args, 0);

		// create a translation scope for the opSubclasses
		if (opSubclasses == null)
			opSubclasses = MixedInitiativeOpClassesProvider.STATIC_INSTANCE.provideClasses();

		SimplTypesScope.get(Logging.MIXED_INITIATIVE_OP_TRANSLATION_SCOPE, opSubclasses);

		guiShown = false;

		if (translationScope != null)
			this.translationScope = translationScope;
		else
			this.translationScope = SimplTypesScope.get(Logging.MIXED_INITIATIVE_OP_TRANSLATION_SCOPE);

		LOG incomingLog = null;

		// see if a log file has been specified
		if (args.length > 1)
		{ // a log was specified
			debug("Getting log file from args!");
			logFile = new File(args[1]);
			mode = LOG_LOADING;

			logLoadComplete = false;
		}

		while (incomingLog == null)
		{
			// Schedule a job for the event-dispatching thread:
			// creating and showing this application's GUI.
			javax.swing.SwingUtilities.invokeLater(this);

			while (mode != LOG_LOADING || logFile == null)
			{ // wait until we get a logFile or until the program quits
				try
				{
					Thread.sleep(100);
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}
			}

			// by now, incomingLog must have something
			debug("Reading log from file: " + logFile);

			// Schedule a job for the event-dispatching thread:
			// creating and showing this application's GUI.
			javax.swing.SwingUtilities.invokeLater(this);

			incomingLog = this.readInXMLFile(logFile);

			if (incomingLog != null)
			{
				this.log = generateLogPlaybackControlModel(incomingLog);

				t = new Timer(this.playbackInterval, this);

				mode = LOG_LOADED;

				secondaryLoad();
			}
			else
			{
				System.err.println("No log found; exiting.");
				// System.exit(0);

				mode = LOG_NOT_SELECTED;

				logLoadComplete = false;
			}

			// Schedule a job for the event-dispatching thread:
			// creating and showing this application's GUI.
			javax.swing.SwingUtilities.invokeLater(this);
		}
	}

	protected abstract LogPlaybackControlModel<OP, LOG> generateLogPlaybackControlModel(
			LOG incomingLog);

	protected abstract View<OP> generateView();

	protected abstract LogPlaybackControls<OP, LOG> generateLogPlaybackControls();

	/**
	 * Translates the given file to XML. Necessary because it is not possible to cast directly to
	 * generic types.
	 * 
	 * @param logToRead
	 * @return
	 * @throws SIMPLTranslationException
	 */
	protected abstract LOG translateXMLFromFile(File logToRead) throws SIMPLTranslationException;

	protected LOG readInXMLFile(File logToRead)
	{
		if (logToRead == null)
		{
			System.err.println("NO LOG TO READ!");
			return null;
		}

		// read in the XML; this may take awhile
		try
		{
			debug("READING LOG");

			return translateXMLFromFile(logToRead);
		}
		catch (SIMPLTranslationException e)
		{
			System.err.println("READING LOG FAILED!");
			e.printStackTrace();

			return null;
		}
	}

	protected JFileChooser createFileChooser()
	{
		JFileChooser fC = new JFileChooser(PropertiesAndDirectories.logDir());
		ExtensionFilter fF = new ExtensionFilter("xml");

		fC.addChoosableFileFilter(fC.getAcceptAllFileFilter());
		fC.addChoosableFileFilter(fF);

		// find the most recent log file in the directory

		File[] logDirContents = PropertiesAndDirectories.logDir().listFiles(fF);

		if (logDirContents.length > 0)
		{
			File newestLog = logDirContents[0];

			for (int i = 1; i < logDirContents.length; i++)
			{
				if (logDirContents[i].lastModified() > newestLog.lastModified())
				{
					newestLog = logDirContents[i];
				}
			}

			fC.setSelectedFile(newestLog);
		}

		return fC;
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		if (e.getActionCommand() == null)
		{
			if (playing && !this.controlsDisplay.isMousePressed())
			{
				// advance the frame
				log.forward();

				if (this.playbackInterval == TIMESTAMP_PLAYBACK_INTERVAL)
				{ // if we're basing off the interval, we have to check some
					// stuff
					long delay = log.getNext().getSessionTime() - log.getCurrentOp().getSessionTime();

					if (delay > 0)
					{
						t.setDelay((int) delay);
					}
				}

				if (log.getMaximum() <= log.getValue())
				{
					this.fireActionEvent(new ActionEvent(this, 0, PAUSE));
				}
			}
		}

		if (PLAY.equals(e.getActionCommand()))
		{
			playing = true;
		}

		if (PAUSE.equals(e.getActionCommand()))
		{
			playing = false;
		}

		if (STOP.equals(e.getActionCommand()))
		{
			playing = false;
			log.reset();
		}

		if (STEP_BACK.equals(e.getActionCommand()))
		{
			log.back();
		}

		if (STEP_FORWARD.equals(e.getActionCommand()))
		{
			log.forward();
		}

		if (logDisplay != null)
			logDisplay.changeOp(log.getCurrentOp());

		mainFrame.repaint();
	}

	public void addActionListener(ActionListener l)
	{
		actionListeners.add(l);
	}

	protected void fireActionEvent(ActionEvent e)
	{
		for (ActionListener l : actionListeners)
		{
			l.actionPerformed(e);
		}
	}

	protected void showLogPlaybackGUI()
	{
		if (logDisplay != null)
			logDisplay.load(this, log, log.getLogPrologue());

		controlsDisplay = generateLogPlaybackControls();
		if (controlsDisplay != null)
		{
			controlsDisplay.setPreferredSize(new Dimension(800, 100));
			controlsDisplay.setMinimumSize(new Dimension(800, 100));
			controlsDisplay.setLoading(true);

			controlsDisplay.setLog(log);
			controlsDisplay.setLoading(false);

			controlsDisplay.setupImportantEvents();
		}

		// logDisplay.setPreferredSize(new Dimension(800, 600));
		// logDisplay.setMinimumSize(new Dimension(800, 600));
		// logDisplay.setMaximumSize(new Dimension(800, 600));
		if (logDisplay != null)
			logDisplay.invalidate();

		mainFrame.getContentPane().removeAll();
		if (logDisplay != null)
			mainFrame.getContentPane().add(logDisplay, BorderLayout.CENTER);
		if (controlsDisplay != null)
			mainFrame.getContentPane().add(controlsDisplay, BorderLayout.SOUTH);

		if (logDisplay != null && logDisplay.hasKeyListenerSubObject())
		{
			mainFrame.addKeyListener(logDisplay.getKeyListenerSubObject());
		}

		if (logDisplay != null && logDisplay.hasActionListenerSubObject())
		{
			t.addActionListener(logDisplay.getActionListenerSubObject());
		}

		mainFrame.invalidate();
		mainFrame.pack();
		mainFrame.setVisible(true);

		logLoadComplete = true;
	}

	private void showLoadingGUI()
	{
		// logDisplay.setPreferredSize(new Dimension(800, 600));

		if (logDisplay != null)
			logDisplay.invalidate();
	}

	/**
	 * Creates the GUI for the log playback application.
	 */
	private void createGUI()
	{
		// JFrame.setDefaultLookAndFeelDecorated(true);
		try
		{
			// Set cross-platform Java L&F (also called "Metal")
			UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
		}
		catch (UnsupportedLookAndFeelException e)
		{
			// handle exception
			e.printStackTrace();
		}
		catch (ClassNotFoundException e)
		{
			// handle exception
			e.printStackTrace();
		}
		catch (InstantiationException e)
		{
			// handle exception
			e.printStackTrace();
		}
		catch (IllegalAccessException e)
		{
			// handle exception
			e.printStackTrace();
		}

		mainFrame = new JFrame(PropertiesAndDirectories.applicationName());
		mainFrame.addWindowListener(this);

		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// mainFrame.setBackground(Color.BLACK);
		mainFrame.setPreferredSize(new Dimension(825, 875));
		mainFrame.setFocusable(true);
		mainFrame.requestFocus();

		mainFrame.getContentPane().setLayout(new BorderLayout());

		logDisplay = this.generateView();

		// mainFrame.getContentPane().add(logDisplay);

		mainFrame.validate();
		mainFrame.pack();
		mainFrame.setVisible(true);
	}

	public void selectFile()
	{
		showFileSelectGUI();

		mode = LOG_LOADING;

		javax.swing.SwingUtilities.invokeLater(this);
	}

	public void showFileSelectGUI()
	{
		JFileChooser fC = this.createFileChooser();

		int returnVal = fC.showOpenDialog(mainFrame);

		if (returnVal == JFileChooser.APPROVE_OPTION)
		{ // we now have a file...display a new GUI
			logFile = fC.getSelectedFile();
		}
		else
		{ // told to cancel; quitting
			debug("No log selected; quitting.");
			System.exit(0);
		}
	}

	protected void secondaryLoad()
	{
	}

	public void startAdjusting()
	{
	}

	/**
	 * Used to be thread safe with Swing. Either sets up the UI, or switches it, depending on whether
	 * or not a file has been selected.
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run()
	{
		if (!guiShown)
		{

			debug("Showing GUI.");
			this.createGUI();

			guiShown = true;
		}

		if (!logLoadComplete)
		{
			debug("mode is: " + mode);

			switch (mode)
			{
			case (LOG_NOT_SELECTED):
				// no file selected
				this.selectFile();
				break;
			case (LOG_LOADING):
				this.showLoadingGUI();
				break;
			case (LOG_LOADED):
				this.showLogPlaybackGUI();
				break;
			}
		}
	}

	public void startProgram()
	{
		// Schedule a job for the event-dispatching thread:
		// creating and showing this application's GUI.
		javax.swing.SwingUtilities.invokeLater(this);

		t.start();
	}

	@Override
	public void windowActivated(WindowEvent e)
	{
	}

	@Override
	public void windowClosed(WindowEvent e)
	{
		t.stop();
	}

	/**
	 * Sends the server notification that we are logging-out, then shuts down the program.
	 */
	@Override
	public void windowClosing(WindowEvent e)
	{
	}

	@Override
	public void windowDeactivated(WindowEvent e)
	{
	}

	@Override
	public void windowDeiconified(WindowEvent e)
	{
		t.start();
	}

	@Override
	public void windowIconified(WindowEvent e)
	{
		t.stop();
	}

	@Override
	public void windowOpened(WindowEvent e)
	{
	}

}
