package ecologylab.services.logging.playback;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import ecologylab.services.logging.Logging;
import ecologylab.services.logging.MixedInitiativeOp;

/**
 * GUI elements for the log playback application.
 * 
 * @author Zachary O. Toups (toupsz@cs.tamu.edu)
 * 
 * @param <E>
 * @param <T>
 */
public class LogPlaybackControls<E extends MixedInitiativeOp, T extends Logging<E>> extends JPanel implements
		ActionListener, PlaybackControlCommands, ChangeListener
{
	private static final long						serialVersionUID	= 1L;

	protected LogPlaybackControlModel<E, T>	log					= null;

	private JProgressBar								loadSpinner			= new JProgressBar();

	protected JSlider									jogShuttle			= new JSlider();

	private ImageIcon									playIcon				= new ImageIcon(
																							"toolbarButtonGraphics/media/Play24.gif", "Play");

	private ImageIcon									pauseIcon			= new ImageIcon(
																							"toolbarButtonGraphics/media/Pause24.gif", "Pause");

	private JButton									playPauseButton	= new JButton(playIcon);

	private JButton									stopButton			= new JButton(new ImageIcon(
																							"toolbarButtonGraphics/media/Stop24.gif", "Stop"));

	private JButton									stepBackButton		= new JButton(new ImageIcon(
																							"toolbarButtonGraphics/media/StepBack24.gif",
																							"Step back"));

	private JButton									stepForwardButton	= new JButton(new ImageIcon(
																							"toolbarButtonGraphics/media/StepForward24.gif",
																							"Step forward"));

	protected boolean 								mousePressed 		= false;
	
	LogPlayer<E, T>									player;
	
	public LogPlaybackControls(LogPlayer<E, T> p)
	{
		super();

		player = p;
		
		this.setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));

		jogShuttle.setOrientation(JSlider.HORIZONTAL);
		loadSpinner.setOrientation(JProgressBar.VERTICAL);

		playPauseButton.setActionCommand(PLAY);
		stopButton.setActionCommand(STOP);
		stepBackButton.setActionCommand(STEP_BACK);
		stepForwardButton.setActionCommand(STEP_FORWARD);

		playPauseButton.addActionListener(player);
		playPauseButton.addActionListener(this);

		stopButton.addActionListener(player);
		stopButton.addActionListener(this);

		stepBackButton.addActionListener(player);
		stepBackButton.addActionListener(this);

		stepForwardButton.addActionListener(player);
		stepForwardButton.addActionListener(this);
		
		player.addActionListener(this);

		this.add(stepBackButton);

		JPanel playStopPanel = new JPanel();
		playStopPanel.setLayout(new BoxLayout(playStopPanel, BoxLayout.PAGE_AXIS));

		playStopPanel.add(playPauseButton);
		playStopPanel.add(stopButton);

		this.add(playStopPanel);

		this.add(stepForwardButton);

		jogShuttle.setPreferredSize(new Dimension(800, 50));
		jogShuttle.setPaintLabels(true);

		jogShuttle.addMouseListener(new MouseListener()
		{
			public void mousePressed(MouseEvent me)
			{
				jogShuttle
						.setValue((int) (jogShuttle.getMinimum() + (jogShuttle.getExtent() * (double) ((double) me.getX() / (double) jogShuttle
								.getWidth()))));
				player.startAdjusting();
				mousePressed = true;
			}

			public void mouseClicked(MouseEvent me)
			{
				jogShuttle
						.setValue((int) (jogShuttle.getMinimum() + (jogShuttle.getExtent() * (double) ((double) me.getX() / (double) jogShuttle
								.getWidth()))));
			}

			public void mouseReleased(MouseEvent me)
			{
				jogShuttle
						.setValue((int) (jogShuttle.getMinimum() + (jogShuttle.getExtent() * (double) ((double) me.getX() / (double) jogShuttle
								.getWidth()))));
				mousePressed = false;
			}

			public void mouseEntered(MouseEvent me)
			{
			}

			public void mouseExited(MouseEvent me)
			{
			}
		});

		jogShuttle.addMouseMotionListener(new MouseMotionListener()
		{

			public void mouseDragged(MouseEvent me)
			{
				jogShuttle
						.setValue((int) (jogShuttle.getMinimum() + (jogShuttle.getExtent() * (double) ((double) me.getX() / (double) jogShuttle
								.getWidth()))));
			}

			public void mouseMoved(MouseEvent me)
			{
			}
		});

		this.add(jogShuttle);
	}

	public boolean isMousePressed()
	{
		return mousePressed;
	}
	/**
	 * @param loading
	 *           The loading to set.
	 */
	public void setLoading(boolean loading)
	{
		loadSpinner.setIndeterminate(loading);
	}

	/**
	 * @param log
	 *           The log to set.
	 */
	public void setLog(LogPlaybackControlModel<E, T> log)
	{
		this.log = log;

		jogShuttle.setModel(log);
	}

	public void actionPerformed(ActionEvent arg0)
	{
		if (PLAY.equals(arg0.getActionCommand()))
		{
			playPauseButton.setActionCommand(PAUSE);
			playPauseButton.setIcon(pauseIcon);
		}

		if (PAUSE.equals(arg0.getActionCommand()) ||
			 STEP_BACK.equals(arg0.getActionCommand()) ||
			 STEP_FORWARD.equals(arg0.getActionCommand()) ||
			 STOP.equals(arg0.getActionCommand()))
		{
			playPauseButton.setActionCommand(PLAY);
			playPauseButton.setIcon(playIcon);
		}

	}

	public void stateChanged(ChangeEvent arg0)
	{
	}

	/**
	 * Hook method for subclasses that need to set up important events on the log op timeline.
	 * 
	 */
	public void setupImportantEvents()
	{
	}
}
