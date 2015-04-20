package converter.ruleSetProcessing;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.jdom.Comment;
import org.jdom.Content;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Parent;
import org.jdom.input.SAXBuilder;
import org.xml.sax.InputSource;

public class RulesetReader {

	protected final Logger myLogger = Logger
			.getLogger(StarterRulesetMerging.class);

	private String myFilePath = "";
	private Document myDoc = null;

	@SuppressWarnings("unused")
	private RulesetReader() {
	}

	public RulesetReader(String filePath) {
		//document is first read from file when getDocument() is called for the first time 
		this.myFilePath = filePath;
	}

	public Document getDocument() throws JDOMException, IOException {

		if (myDoc == null) {
			//read document from file if it has not been done so far
			SAXBuilder sxbuild = new SAXBuilder();
			InputSource is = new InputSource(myFilePath);
			myDoc = sxbuild.build(is);
			myLogger.info("document '" + myFilePath
					+ "' read from file without exceptions");
		}

		return myDoc;
	}

	public String toString() {
		//just here in order to trigger reading xml file without other effects
		try {
			return getDocument().toString();
		} catch (JDOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}

	private void cleanOutTree(Parent ele) {
		Comment comm = new Comment("");
		Content con = null;
		for (Iterator<Content> i = ele.getContent().iterator(); i.hasNext();) {
			con = i.next();
			if (con.getClass().equals(comm.getClass())) {
				i.remove();
			}
			if (con.getClass().equals(new Element("Test").getClass())) {
				cleanOutTree((Element) con);
			}
		}
	}

	public void cleanOutComments() {
		cleanOutTree(myDoc.getRootElement());
	}

	@SuppressWarnings("unchecked")
	public Document getDoubleElements() {
		ArrayList<Element> doubles = new ArrayList<Element>();
		Element ele = null;

		for (Iterator<Element> i = myDoc.getDescendants(); i.hasNext();) {
			try {
				ele = (Element) i.next();
				ele = (Element) ele.clone();
			} catch (Exception e) {
				// setting ele didn't work, keep looping
				continue;
			}

			if (SlubIntegrity.isProtected(ele)) {
				Comment com = new Comment(
						"INTRANDA COMMENT: ### This Element is contained more than once ### END OF INTRANDA COMMENT");
				ele.addContent(com);
				doubles.add(ele);
				ele = SlubIntegrity.getProtectedElement(ele);
				Comment com2 = new Comment(
						"INTRANDA COMMENT: ### This Element is contained more than once ### END OF INTRANDA COMMENT");
				ele.addContent(com2);
			} else {
				SlubIntegrity.addWatch((Element) ele.clone());
			}

		}

		Document doubleDoc = new Document(new Element("Doubles"));
		if (doubles.size() == 0) {
			doubleDoc
					.getRootElement()
					.addContent(
							new Comment(
									"INTRANDA COMMENT: ### This Document does not contain doubles ### END OF INTRANDA COMMENT"));
		}
		doubleDoc.getRootElement().addContent(doubles);
		return doubleDoc;
	}
}
