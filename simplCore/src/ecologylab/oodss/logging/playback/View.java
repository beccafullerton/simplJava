/*
 * Created on Apr 13, 2007
 */
package ecologylab.oodss.logging.playback;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionListener;
import java.awt.event.KeyListener;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import ecologylab.oodss.logging.MixedInitiativeOp;
import ecologylab.oodss.logging.Prologue;

/**
 * Abstract class for displaying logged operations. Subclasses provide specific visualization of log
 * ops.
 * 
 * @author Zachary O. Toups (toupsz@cs.tamu.edu)
 * 
 * @param <T>
 */
public abstract class View<T extends MixedInitiativeOp> extends JPanel
{
	protected T					currentOp;

	protected Prologue	prologue;

	protected boolean		loaded	= false;

	public View()
	{
		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
	}

	/**
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
	@Override
	protected void paintComponent(Graphics arg0)
	{
		if (loaded)
			this.render(arg0);
		else
			this.renderLoading(arg0);
	}

	protected void load(LogPlayer player, LogPlaybackControlModel log, Prologue prologue)
	{
		if (!loaded)
		{
			this.prologue = prologue;

			this.currentOp = (T) log.getCurrentOp();

			this.loaded = true;
		}
	}

	protected abstract void render(Graphics g);

	protected void renderLoading(Graphics g)
	{
		g.fillRect(0, 0, this.getWidth(), this.getHeight());

		g.setColor(Color.BLACK);

		g.drawString("No log file loaded.", 10, 10);
	}

	public void setLoaded(boolean loaded)
	{
		this.loaded = loaded;
	}

	public void changeOp(T newOp)
	{
		this.currentOp = newOp;
		this.repaint();
	}

	/**
	 * Indicates whether or not this View has a KeyListener object.
	 * 
	 * @return
	 */
	public boolean hasKeyListenerSubObject()
	{
		return false;
	}

	/**
	 * If this View contains a KeyListener object (for example, to enable some sort of keyboard
	 * interaction with the log frame); this method returns it.
	 * 
	 * If hasKeyListener() returns true, then this method must return a KeyListener object; otherwise,
	 * it should return null.
	 * 
	 * @return
	 */
	public KeyListener getKeyListenerSubObject()
	{
		return null;
	}

	/**
	 * Indicates whether or not this View has an ActionListener object.
	 * 
	 * @return
	 */
	public boolean hasActionListenerSubObject()
	{
		return false;
	}

	/**
	 * If this View contains a ActionListener object (for example, to enable some sort of interaction
	 * with the log frame); this method returns it.
	 * 
	 * If hasActionListener() returns true, then this method must return a KeyListener object;
	 * otherwise, it should return null.
	 * 
	 * @return
	 */
	public ActionListener getActionListenerSubObject()
	{
		return null;
	}

}
