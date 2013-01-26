package ecologylab.serialization.library.rest;

import simpl.annotations.dbal.Hint;
import simpl.annotations.dbal.simpl_composite;
import simpl.annotations.dbal.simpl_hints;
import simpl.annotations.dbal.simpl_scalar;
import simpl.core.ElementState;

public class Record extends ElementState
{
	@simpl_scalar @simpl_hints(Hint.XML_LEAF)	protected	int		position;
	@simpl_scalar @simpl_hints(Hint.XML_LEAF)	protected	double	score;
	@simpl_composite protected	Header	header;
	@simpl_composite protected 	Fields	fields;
	
	public Record() {}
	
	public Record(int position, double score, Header header, Fields fields)
	{
		this.position 	= position;
		this.score		= score;
		this.header		= header;
		this.fields		= fields;
	}
	
	/**
	 * @param fields the fields to set
	 */
	public void setFields(Fields fields)
	{
		this.fields = fields;
	}
	/**
	 * @return the fields
	 */
	public Fields getFields()
	{
		return fields;
	}
}
