package ecologylab.fundamental.simplescalar;
import simpl.annotations.dbal.simpl_scalar;
public class SimpleString {
	@simpl_scalar
	private String simplestring;

	public String getSimpleString(){ 
		 return this.simplestring;
	}

	public void setSimpleString(String value){
		this.simplestring = value;
	}
}
