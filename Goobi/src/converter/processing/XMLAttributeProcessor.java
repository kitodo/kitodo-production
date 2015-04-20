package converter.processing;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.xml.sax.InputSource;

public class XMLAttributeProcessor {
	private static final Logger logger = Logger.getLogger(XMLAttributeProcessor.class);

	private Document myDoc;
	private String myDocPath;

	/**
	 * 
	 * @param noClone
	 * @param e
	 * @param args
	 * @param ns
	 * @return
	 * @throws JDOMException
	 * @throws IOException
	 */
	public List<Element> getElementList(boolean noClone, Element e, List<String> args, Namespace ns) throws JDOMException, IOException {
		/*
		 * args 1-> filename of XML-source (optional, required if myDoc==null)
		 * 2-> Elementtype1 
		 * 3-> Attribute1 
		 * 4-> equals Condition1 
		 * 5-> Elementtype2 
		 * 6-> Attribute2 
		 * 7-> equals Condition2
		 *  ..... 
		 *  ..... 
		 *  n
		 */

		List<Element> elements = new ArrayList<Element>();
		String attValue;
		// if myDoc==null the first argument has to be the filelocation
		if (myDoc == null) {
			// load XML Stream from file, dump filename from List of args
			myDocPath = args.get(0);
			args.remove(0);

			try {
				myDoc = this.getDocument(myDocPath);
			} catch (IOException e1) {
				logger.error(new String("File " + myDocPath + " does probably not exist."), e1);
				return null;
			}
		}

		// if e==null the root Element has to be set to e
		if (e == null) {
			e = myDoc.getRootElement();
			logger.debug(e.getContent());
		}

		for (Object oe : e.getChildren()) {
			Element eIt = (Element) oe;

			String arg1 = args.get(1);
			String arg2 = args.get(2);

			//for cases with or without namespace
			if (ns == null)
				attValue = eIt.getAttributeValue(arg1);
			else
				attValue = eIt.getAttributeValue(arg1, ns);

			if (attValue != null) {

				if (attValue.equals(arg2)) {
					logger.info("element found");
					if (args.size() > 3) {
						// load argumentlist, drop first 3 arguments
						// for recursive call of getElementList
						List<String> tmpArgs = new ArrayList<String>();
						tmpArgs.addAll(args);
						tmpArgs.remove(0);
						tmpArgs.remove(0);
						tmpArgs.remove(0);
						// recursive call
						List<Element> temp = this.getElementList(noClone, eIt, tmpArgs, ns);
						// result of recursive call added to ArrayList elements
						elements.addAll(temp);
					} else {
						// if dealing with last 3 arguments there are
						// no lower level elements in xml tree and current
						// element is target element so needs to be added to
						// ListArray
						// clone - no clone for direct manipulation
						if (noClone)
							elements.add(eIt);
						else
							elements.add((Element) eIt.clone());
					}
				} else {
					logger.debug("not found");
				}
			} else {
				logger.debug("attribute " + arg1 + " not found");
				logger.debug("not found");
			}
		}

		return elements;
	}

	private Document getDocument(String path) throws JDOMException, IOException {

		SAXBuilder sxbuild = new SAXBuilder();
		InputSource is = new InputSource(path);
		Document doc = sxbuild.build(is);
		return doc;

	}

	public void saveTo(String fileName) {
		if (fileName == null)
			fileName = myDocPath + ".bak";

	}

	public void saveChanges() {

	}

}
