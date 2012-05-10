package de.unigoettingen.sub.commons.util.xml;

import java.io.StringWriter;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Node;

public class XMLDumper {

	public static String NodeToString(Node node) {
		StringWriter writer = new StringWriter();
		try {
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "true");
			transformer.transform(new DOMSource(node), new StreamResult(writer));
		} catch (TransformerException t) {
			throw new IllegalStateException(t);
		}
		return writer.toString();
	}
	
}
