/**
 * 
 */
package ecologylab.appframework.types.prefs;

import ecologylab.serialization.annotations.simpl_inherit;
import ecologylab.serialization.annotations.simpl_scalar;

/**
 * Preference that is a String
 * 
 * @author andruid
 * 
 */
@simpl_inherit
public class PrefString extends Pref<String>
{
	/**
	 * Value of Pref
	 */
	@simpl_scalar
	String	value;

	/**
	 * 
	 */
	public PrefString()
	{
		super();
	}

	/**
	 * Instantiate Pref to value
	 * 
	 * @param value
	 */
	public PrefString(String value)
	{
		super();
		this.value = value;
	}

	public PrefString(String name, String value)
	{
		super(name);

		this.value = value;
	}

	/**
	 * Get the value of the Pref
	 * 
	 * @return The value of the Pref
	 */
	@Override
	protected String getValue()
	{
		return value;
	}

	/**
	 * Set the value of the Pref
	 * 
	 * @param The
	 *          value the Pref will be set to
	 */
	@Override
	public void setValue(String value)
	{
		this.value = value;

		prefChanged();
	}

	/**
	 * @see ecologylab.appframework.types.prefs.Pref#clone()
	 */
	@Override
	public Pref<String> clone()
	{
		return new PrefString(this.name, this.value);
	}
}
