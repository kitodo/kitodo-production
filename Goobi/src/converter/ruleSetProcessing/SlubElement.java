package converter.ruleSetProcessing;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.jdom.Attribute;
import org.jdom.Comment;
import org.jdom.Element;

public class SlubElement
{

	protected final Logger myLogger = Logger
			.getLogger(StarterRulesetMerging.class);

	private Element myElement = null;
	private Element myDiffElement = null;
	private SlubIntegrity myIntegrityWatch = null;

	private String myDiffFeedback = "";

	@SuppressWarnings("unused")
	private SlubElement()
	{
	}

	public SlubElement(Element ele)
	{
		myElement = ele;
	}

	/*
	 * overwritten method equals checks on a deep level, if element represented
	 * in myElement and it's tree is the same as the Element in parameter ele
	 * 
	 * @parameter eleChecked
	 */
	@SuppressWarnings("unchecked")
	public boolean equals(Element eleChecked)
	{
		// myLogger.debug("check Element " + eleChecked.getName());

		boolean flagFound = false;
		boolean flagChildElementsDiffer = false;
		boolean flagAttributesDiffer = false;

		// this adds this element on the integrity watch for the time of
		// examination
		// and protects it's descendents from being ripped out, instanciate and
		// delete
		// in here

		myIntegrityWatch = SlubIntegrity.addWatch(myElement);

		// if Element is on integrity watch this sequres that only
		// an element of the same name is checked against it
		if (!(myIntegrityWatch == null))
		{
			if (!SlubIntegrity.isProtected(eleChecked))
			{
				unprotect();
				return false;
			}
		}

		// if name is different form its own it is already false
		if (!eleChecked.getName().equals(myElement.getName()))
		{
			unprotect();
			return false;
		}

		// if value is different from it's own value it is already false
		if (!eleChecked.getTextNormalize().equals(myElement.getTextNormalize()))
		{
			unprotect();
			return false;
		}

		// if it went so far we have to analyze the content(here each attribute)
		// and compare
		String diffAttribs = "";
		SlubAttribute satt = null;

		List<Attribute> attributesOfEleChecked = new ArrayList<Attribute>(
				eleChecked.getAttributes());
		for (Attribute attributeChecked : attributesOfEleChecked)
		{
			// Attribute attributeChecked = (Attribute)
			// objectOfAttributeChecked;
			for (Object objectOfAttributeComparedTo : myElement.getAttributes())
			{
				// using the Slub version of attribute
				satt = new SlubAttribute(
						(Attribute) objectOfAttributeComparedTo);
				if (satt.equals(attributeChecked))
				{
					flagFound = true;
					break;
				}
				if (satt.hasSameID())
				{
					//TODO see if break does what you want it to do (loop)
					break;
				}
			}

			// if it comes out of the attribute iteration once with a flagFound
			// = false then there is at least one differing attribute
			if (flagFound == false)
			{
				flagAttributesDiffer = true;
				if (satt.hasSameID())
				{
					diffAttribs = diffAttribs + attributeChecked.getName()
							+ " original value="
							+ satt.getAttribute().getValue() + " /";
					// reset satt for the next iteration
					satt = null;
				} else
				{
					diffAttribs = diffAttribs + attributeChecked.getName()
							+ " /";
					// reset satt for the next iteration
					satt = null;
				}
			}
			// reset flag, it might be true
			flagFound = false;
		}

		// if different Attributes apply add log to the diffFeedback
		if (flagAttributesDiffer)
		{
			myDiffFeedback = myDiffFeedback + "-> differing attribute(s): "
					+ diffAttribs;
		}

		if (!(myElement.getChildren().size() == eleChecked.getChildren().size()))
		{
			myDiffFeedback = myDiffFeedback
					+ "different number of elements: origin - "
					+ myElement.getChildren().size() + " target - "
					+ eleChecked.getChildren().size();
		}

		String diffElements = "";

		// now same procedure with the elements, basically an recursive use of
		// this SlubElement class
		// copy the list, so that a removal doesn't effect the iteration
		List<Element> childrenElementsOfElementChecked = new ArrayList<Element>(
				eleChecked.getChildren());

		for (Element childOfElementChecked : childrenElementsOfElementChecked)
		{

			for (Object objectOfChildComparedTo : myElement.getChildren())
			{
				// using the Slub version of element
				SlubElement sel = new SlubElement(
						(Element) objectOfChildComparedTo);
				if (sel.equals(childOfElementChecked))
				{
					flagFound = true;

					// remove child elements if parent Element (which is this
					// one)
					// is not on the integrityList

					if (!SlubIntegrity.isProtected(eleChecked))
					{
						eleChecked.getChildren().remove(childOfElementChecked);
						myLogger.debug("Removed Element:"
								+ eleChecked.getName());
					}
					break;
				}

			}

			if (flagFound == false)
			{
				flagChildElementsDiffer = true;
				diffElements = diffElements + childOfElementChecked.getName()
						+ " /";
			}
			// reset flag, it might be true
			flagFound = false;
		}

		if (flagChildElementsDiffer)
		{
			myDiffFeedback = myDiffFeedback + "-> differing Element(s): "
					+ diffElements;
		}

		if (flagChildElementsDiffer || flagAttributesDiffer)
		{
			// myLogger.debug("element is different");

			myDiffElement = eleChecked;

			// adding feedabck as comment
			Comment comm = new Comment("INTRANDA COMMENT:" + myDiffFeedback
					+ " END OF INTRANDA COMMENT");

			myDiffElement.addContent(comm);

			unprotect();
			return false;
		}

		unprotect();
		return true;
	}

	public String qualifiedDiffFeedback()
	{
		return myDiffFeedback;
	}

	public Element getJDOMElement()
	{
		return myDiffElement;
	}

	public boolean hasDiffs()
	{
		if (myDiffElement != null)
		{
			return true;
		}
		return false;
	}

	/*
	 * taking this element off the watch list
	 */
	private void unprotect()
	{
		SlubIntegrity.remove(myIntegrityWatch);
	}

}