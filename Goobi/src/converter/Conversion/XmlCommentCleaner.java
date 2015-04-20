package converter.Conversion;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.jdom.Comment;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

public class XmlCommentCleaner {


	public void cleanCommentsFromXmlFile(File inFile, File outFile) throws JDOMException, IOException {
		/* --------------------------------
		 * read inFile and remove most upper xml-comment if it exists
		 * --------------------------------*/
		Document doc = new SAXBuilder().build(inFile);
		for (Object obj : new ArrayList<Object>(doc.getContent())) {
			if (obj instanceof Comment) {
				doc.getContent().remove(obj);
			}
		}
		
		runThroughChildren(doc.getRootElement());

		doc.getRootElement().addContent(0,new Comment("Cleaned from Comments :)"));
		/* --------------------------------
		 * write clean xml to outFile
		 * --------------------------------*/
		
		XMLOutputter outputter = new XMLOutputter();
		Format format = Format.getPrettyFormat();
		outputter.setFormat(format);
		FileOutputStream fos = new FileOutputStream(outFile);
		outputter.output(doc, fos);
	}

	private void runThroughChildren(Element inElement) {
		for (Object child : inElement.getChildren()) {
			Element el = (Element) child;
			/* --------------------------------
			 * remove all comments if they exist
			 * --------------------------------*/
			for (Object obj : new ArrayList<Object>(el.getContent())) {
				if (obj instanceof Comment) {
					el.getContent().remove(obj);
				}
			}
			/* --------------------------------
			 * call myself recursive
			 * --------------------------------*/
			if (el.getChildren() != null && el.getChildren().size() > 0) {
				runThroughChildren(el);
			}
		}
	}

}
