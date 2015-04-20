package converter.ruleSetProcessing;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.jdom.Element;

/* this class is in charge of checking if a certain element is already existing in the xml tree, 
 * which is checked against (here slub.xml). The general concept is that an element, which needs to
 * be treated as an entity will have a unique id, which is expressed by a sub element/tag within
 * the element (a child element) which occurs once and may be called "name", "id", "MODSName" 
 */

public class SlubIntegrity {

	protected final Logger myLogger = Logger
			.getLogger(SlubIntegrity.class);

	// these are instances on watch
	static private List<SlubIntegrity> myObjectsOnWatch = new ArrayList<SlubIntegrity>();
	// these are classes on watch
	static private List<String[]> myElementNamesOnWatch = new ArrayList<String[]>();

	private String myElementName = null;
	private String myIdTagName = null;
	private String myID = null;
	
	private Element myElement = null;
	
	static Element lastElement = null;
	
	private SlubIntegrity() {
	}

	/*
	 * 
	 */

	private SlubIntegrity(String addElementName, String idTagName, String id)
			throws Exception {
		myElementName = addElementName;
		myIdTagName = idTagName;
		if (id == null|| id.length() == 0) {
			myLogger.debug("no id to watch for:" + myElementName + "/"
					+ myIdTagName + "/null");
			throw new Exception("no id to watch for");
		}
		myID = id;
		//System.out.println("lade " + id);
		myLogger.debug("created new watchObject:" + myElementName + "/"
				+ myIdTagName + "/" + myID);
		myObjectsOnWatch.add(this);
	}

	// =========================

	/*
	 * by passing on an Element of ruleset A (against which ruleset B gets compared) this method checks 
	 * if that Element class should be watched. If so it adds it's instance to the watch list
	 */

	static public SlubIntegrity addWatch(Element ele) {
		SlubIntegrity newWatchedObject = null;
		for (String check[] : myElementNamesOnWatch) {
			if (check[0].equals(ele.getName())) {
				try {
					newWatchedObject = new SlubIntegrity(check[0], check[1],
							ele.getChildTextNormalize(check[1]));
					newWatchedObject.myElement = ele;
					return newWatchedObject;
				} catch (Exception e) {
				}
			}
		}
		return null;
	}

	/*createWatch is used to register an element type, which forms a unit with all it's descendents.
	 *in order to check it's uniqueness also a descendents element type (idTagName ) needs to be registered , 
	 *which contains the uniqueness of the instance 
	 */
	static public void createWatch(String elementName, String idTagName) {
		String[] newWatch = { elementName, idTagName };
		myElementNamesOnWatch.add(newWatch);
	}

	/* the objectsOnWatch list contains the unique elements of ruleset A against which we compare ruleset B  
	 * by passing on an element of ruleset B this method checks if that element is contained in in
	 * the observed watch list by checking it's name and if it is a match if it contains a childelement
	 * providing the same id  
	 */
	
	static public boolean isProtected(Element ele) {
		//put list in a temp container in order to avoid exception being thrown
		//on removal of objects from the original list
		String eleName = ele.getName();
		List<SlubIntegrity> sil = myObjectsOnWatch;
		for (SlubIntegrity si : sil) {
			if (si.getName().equals(eleName)) {
				for (Object eleChildO : ele.getChildren()) {
					Element eleChild = (Element) eleChildO;
					if (eleChild.getName().equals(si.getIdTagName())
							&& eleChild.getTextNormalize().equals(si.getID())) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	static private boolean replaceProtected(Element ele) {
		//put list in a temp container in order to avoid exception being thrown
		//on removal of objects from the original list
		String eleName = ele.getName();
		List<SlubIntegrity> sil = myObjectsOnWatch;
		for (SlubIntegrity si : sil) {
			if (si.getName().equals(eleName)) {
				for (Object eleChildO : ele.getChildren()) {
					Element eleChild = (Element) eleChildO;
					if (eleChild.getName().equals(si.getIdTagName())
							&& eleChild.getTextNormalize().equals(si.getID())) {
						lastElement = si.myElement;
						si.myElement = ele;
						return true;
					}
				}
			}
		}
		return false;
	}
	

	
	//this removes the instance of a watch
	static public void remove(SlubIntegrity si) {
		//System.out.println("entferne " + si.myID);
		myObjectsOnWatch.remove(si);
	}

	static public void close() {
		myObjectsOnWatch = null;
	}

	// ==========================

	protected String getName() {
		return myElementName;
	}

	protected String getIdTagName() {
		return myIdTagName;
	}

	protected String getID() {
		return myID;
	}

	public static Element getProtectedElement(Element ele) {
		replaceProtected(ele);
		Element returnMe = lastElement;
		lastElement = null;
		return returnMe;
	}
	
	public static void resetObjectsOnWatch(){
		myObjectsOnWatch.clear();
	}
}
