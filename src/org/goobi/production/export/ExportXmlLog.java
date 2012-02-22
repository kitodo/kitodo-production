/*
 * This file is part of the Goobi Application - a Workflow tool for the support of
 * mass digitization.
 *
 * Visit the websites for more information.
 *     - http://gdz.sub.uni-goettingen.de
 *     - http://www.goobi.org
 *     - http://launchpad.net/goobi-production
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details. You
 * should have received a copy of the GNU General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 59 Temple Place,
 * Suite 330, Boston, MA 02111-1307 USA
 */

package org.goobi.production.export;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

import org.apache.commons.configuration.ConfigurationException;
import org.goobi.production.IProcessDataExport;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.jdom.transform.XSLTransformException;
import org.jdom.transform.XSLTransformer;

import de.sub.goobi.beans.Prozess;
import de.sub.goobi.beans.Prozesseigenschaft;
import de.sub.goobi.beans.Schritt;
import de.sub.goobi.beans.Schritteigenschaft;
import de.sub.goobi.beans.Vorlage;
import de.sub.goobi.beans.Vorlageeigenschaft;
import de.sub.goobi.beans.Werkstueck;
import de.sub.goobi.beans.Werkstueckeigenschaft;
import de.sub.goobi.helper.exceptions.ExportFileException;

/**
 * This class provides xml logfile generation. After the the generation the file will be written to user home directory
 * 
 * @author Robert Sehr
 * 
 */
public class ExportXmlLog implements IProcessDataExport {

	/**
	 * This method exports the production metadata as xml to a given directory
	 * 
	 * @param p
	 *            the process to export
	 * @param destination
	 *            the destination to write the file
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws ExportFileException
	 */

	public void startExport(Prozess p, String destination) throws FileNotFoundException, IOException {
		startExport(p, new FileOutputStream(destination), null);
	}

	/**
	 * This method exports the production metadata as xml to a given stream.
	 * 
	 * @param process
	 *            the process to export
	 * @param os
	 *            the OutputStream to write the contents to
	 * @throws IOException
	 * @throws ExportFileException
	 */
	public void startExport(Prozess process, OutputStream os, String xslt) throws IOException {
		try {
			Document doc = createDocument(process);

			XMLOutputter outp = new XMLOutputter();
			outp.setFormat(Format.getPrettyFormat());

			outp.output(doc, os);
			os.close();

		} catch (ConfigurationException e) {
			throw new IOException(e);
		}
	}

	/**
	 * This method creates a new xml document with process metadata
	 * 
	 * @param process
	 *            the process to export
	 * @return a new xml document
	 * @throws ConfigurationException
	 */
	public Document createDocument(Prozess process) throws ConfigurationException {

		Element processElm = new Element("process");
		Document doc = new Document(processElm);

		processElm.setAttribute("processID", String.valueOf(process.getId()));

		// namespace declaration

		Namespace xmlns = Namespace.getNamespace("http://www.goobi.org/logfile");

		Namespace xsi = Namespace.getNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
		processElm.addNamespaceDeclaration(xsi);
		processElm.setNamespace(xmlns);
		Attribute attSchema = new Attribute("schemaLocation", "http://www.goobi.org/logfile" + " XML-logfile.xsd", xsi);
		processElm.setAttribute(attSchema);

		// process information

		ArrayList<Element> processElements = new ArrayList<Element>();
		Element processTitle = new Element("title", xmlns);
		processTitle.setText(process.getTitel());
		processElements.add(processTitle);

		Element project = new Element("project", xmlns);
		project.setText(process.getProjekt().getTitel());
		processElements.add(project);

		Element date = new Element("time", xmlns);
		date.setAttribute("type", "creation date");
		date.setText(String.valueOf(process.getErstellungsdatum()));
		processElements.add(date);

		Element ruleset = new Element("ruleset", xmlns);
		ruleset.setText(process.getRegelsatz().getDatei());
		processElements.add(ruleset);

		Element comment = new Element("comment", xmlns);
		comment.setText(process.getWikifield());
		processElements.add(comment);

		ArrayList<Element> processProperties = new ArrayList<Element>();
		for (Prozesseigenschaft prop : process.getEigenschaftenList()) {
			Element property = new Element("property", xmlns);
			property.setAttribute("propertyIdentifier", prop.getTitel());
			if (prop.getWert() != null) {
				property.setAttribute("value", replacer(prop.getWert()));
			} else {
				property.setAttribute("value", "");
			}

			Element label = new Element("label", xmlns);

			label.setText(prop.getTitel());
			property.addContent(label);

			processProperties.add(property);
		}
		if (processProperties.size() != 0) {
			Element properties = new Element("properties", xmlns);
			properties.addContent(processProperties);
			processElements.add(properties);
		}

		// step information
		Element steps = new Element("steps", xmlns);
		ArrayList<Element> stepElements = new ArrayList<Element>();
		for (Schritt s : process.getSchritteList()) {
			Element stepElement = new Element("step", xmlns);
			stepElement.setAttribute("stepID", String.valueOf(s.getId()));

			Element steptitle = new Element("title", xmlns);
			steptitle.setText(s.getTitel());
			stepElement.addContent(steptitle);

			Element state = new Element("processingstatus", xmlns);
			state.setText(s.getBearbeitungsstatusAsString());
			stepElement.addContent(state);

			Element begin = new Element("time", xmlns);
			begin.setAttribute("type", "start time");
			begin.setText(String.valueOf(s.getBearbeitungsbeginn()));
			stepElement.addContent(begin);

			Element end = new Element("time", xmlns);
			end.setAttribute("type", "end time");
			end.setText(String.valueOf(s.getBearbeitungsendeAsFormattedString()));
			stepElement.addContent(end);

			if (s.getBearbeitungsbenutzer() != null && s.getBearbeitungsbenutzer().getNachVorname() != null) {
				Element user = new Element("user", xmlns);
				user.setText(s.getBearbeitungsbenutzer().getNachVorname());
				stepElement.addContent(user);
			}
			Element editType = new Element("edittype", xmlns);
			editType.setText(s.getEditTypeEnum().getTitle());
			stepElement.addContent(editType);

			ArrayList<Element> stepProperties = new ArrayList<Element>();
			for (Schritteigenschaft prop : s.getEigenschaftenList()) {
				Element property = new Element("property", xmlns);
				property.setAttribute("propertyIdentifier", prop.getTitel());
				if (prop.getWert() != null) {
					property.setAttribute("value", replacer(prop.getWert()));
				} else {
					property.setAttribute("value", "");
				}

				Element label = new Element("label", xmlns);
				label.setText(prop.getTitel());
				property.addContent(label);

				stepProperties.add(property);
			}
			if (stepProperties.size() != 0) {
				Element properties = new Element("properties", xmlns);
				properties.addContent(stepProperties);
				stepElement.addContent(properties);
			}

			stepElements.add(stepElement);
		}
		if (stepElements != null) {
			steps.addContent(stepElements);
			processElements.add(steps);
		}

		// template information
		Element templates = new Element("originals", xmlns);
		ArrayList<Element> templateElements = new ArrayList<Element>();
		for (Vorlage v : process.getVorlagenList()) {
			Element template = new Element("original", xmlns);
			template.setAttribute("originalID", String.valueOf(v.getId()));

			ArrayList<Element> templateProperties = new ArrayList<Element>();
			for (Vorlageeigenschaft prop : v.getEigenschaftenList()) {
				Element property = new Element("property", xmlns);
				property.setAttribute("propertyIdentifier", prop.getTitel());
				if (prop.getWert() != null) {
					property.setAttribute("value", replacer(prop.getWert()));
				} else {
					property.setAttribute("value", "");
				}

				Element label = new Element("label", xmlns);

				label.setText(prop.getTitel());
				property.addContent(label);

				templateProperties.add(property);
				if (prop.getTitel().equals("Signatur")) {
					Element secondProperty = new Element("property", xmlns);
					secondProperty.setAttribute("propertyIdentifier", prop.getTitel()+"Encoded");
					if (prop.getWert() != null) {
						secondProperty.setAttribute("value", "vorl:"+replacer(prop.getWert()));
						Element secondLabel = new Element("label", xmlns);
						secondLabel.setText(prop.getTitel());
						secondProperty.addContent(secondLabel);
						templateProperties.add(secondProperty);
					}
				}
			}
			if (templateProperties.size() != 0) {
				Element properties = new Element("properties", xmlns);
				properties.addContent(templateProperties);
				template.addContent(properties);
			}
			templateElements.add(template);
		}
		if (templateElements != null) {
			templates.addContent(templateElements);
			processElements.add(templates);
		}

		// digital document information
		Element digdoc = new Element("digitalDocuments", xmlns);
		ArrayList<Element> docElements = new ArrayList<Element>();
		for (Werkstueck w : process.getWerkstueckeList()) {
			Element dd = new Element("digitalDocument", xmlns);
			dd.setAttribute("digitalDocumentID", String.valueOf(w.getId()));

			ArrayList<Element> docProperties = new ArrayList<Element>();
			for (Werkstueckeigenschaft prop : w.getEigenschaftenList()) {
				Element property = new Element("property", xmlns);
				property.setAttribute("propertyIdentifier", prop.getTitel());
				if (prop.getWert() != null) {
					property.setAttribute("value", replacer(prop.getWert()));
				} else {
					property.setAttribute("value", "");
				}

				Element label = new Element("label", xmlns);

				label.setText(prop.getTitel());
				property.addContent(label);

				docProperties.add(property);
			}
			if (docProperties.size() != 0) {
				Element properties = new Element("properties", xmlns);
				properties.addContent(docProperties);
				dd.addContent(properties);
			}
			docElements.add(dd);
		}
		if (docElements != null) {
			digdoc.addContent(docElements);
			processElements.add(digdoc);
		}

		processElm.setContent(processElements);
		return doc;

	}

	/**
	 * This method transforms the xml log using a xslt file and opens a new window with the output file
	 * 
	 * @param out
	 *            ServletOutputStream
	 * @param doc
	 *            the xml document to transform
	 * @param filename
	 *            the filename of the xslt
	 * @throws XSLTransformException
	 * @throws IOException
	 */

	public void XmlTransformation(OutputStream out, Document doc, String filename) throws XSLTransformException, IOException {
		Document docTrans = new Document();
		if (filename != null && filename.equals("")) {
			XSLTransformer transformer;
			transformer = new XSLTransformer(filename);
			docTrans = transformer.transform(doc);
		} else {
			docTrans = doc;
		}
		Format format = Format.getPrettyFormat();
		format.setEncoding("utf-8");
		XMLOutputter xmlOut = new XMLOutputter(format);

		xmlOut.output(docTrans, out);

	}

	public void startTransformation(OutputStream out, Prozess p, String filename) throws ConfigurationException, XSLTransformException, IOException {
		startTransformation(p, out, filename);
	}

	public void startTransformation(Prozess p, OutputStream out, String filename) throws ConfigurationException, XSLTransformException, IOException {
		Document doc = createDocument(p);
		XmlTransformation(out, doc, filename);
	}

	private String replacer(String in) {
		in = in.replace("°", "?");
		in = in.replace("^", "?");
		in = in.replace("|", "?");
		in = in.replace(">", "?");
		in = in.replace("<", "?");
		return in;
	}
}
