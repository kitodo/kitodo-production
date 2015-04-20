package converter.ruleSetProcessing;

import org.jdom.Attribute;

public class SlubAttribute
{

	private Attribute myAttribute = null;
	private boolean flagSameID = false;
	
	@SuppressWarnings("unused")
	private SlubAttribute()
	{

	}

	public SlubAttribute(Attribute att)
	{
		myAttribute = att;
	}

	public boolean equals(Attribute att)
	{

		if (!att.getName().equals(myAttribute.getName()))
		{
			return false;
		}

		if (!att.getNamespace().equals(myAttribute.getNamespace()))
		{
			return false;
		}
		
		flagSameID = true;

		if (!att.getValue().equals(myAttribute.getValue()))
		{
			return false;
		}

		return true;
	}
	
	public boolean hasSameID(){
		return flagSameID;
	}
	
	public Attribute getAttribute(){
		return myAttribute;
	}
}
