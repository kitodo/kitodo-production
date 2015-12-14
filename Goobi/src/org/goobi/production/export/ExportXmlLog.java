package org.goobi.production.export;

/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 *     		- http://www.goobi.org
 *     		- https://github.com/goobi/goobi-production
 * 		    - http://gdz.sub.uni-goettingen.de
 * 			- http://www.intranda.com
 * 			- http://digiverso.com 
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
 * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
 * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.apache.log4j.Logger;
import org.goobi.production.IProcessDataExport;
import org.jaxen.JaxenException;
import org.jaxen.jdom.JDOMXPath;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.jdom.transform.XSLTransformException;
import org.jdom.transform.XSLTransformer;

import de.sub.goobi.beans.Batch;
import de.sub.goobi.beans.Prozess;
import de.sub.goobi.beans.Prozesseigenschaft;
import de.sub.goobi.beans.Schritt;
import de.sub.goobi.beans.Vorlage;
import de.sub.goobi.beans.Vorlageeigenschaft;
import de.sub.goobi.beans.Werkstueck;
import de.sub.goobi.beans.Werkstueckeigenschaft;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.ExportFileException;
import de.sub.goobi.helper.exceptions.SwapException;

/**
 * This class provides xml logfile generation. After the generation the file will be written to user home directory
 * 
 * @author Robert Sehr
 * @author Steffen Hankiewicz
 * 
 */
public class ExportXmlLog implements IProcessDataExport {
	private static final Logger logger = Logger.getLogger(ExportXmlLog.class);
	
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

	public void startExport(Prozess p, File dest) throws FileNotFoundException, IOException {
		startExport(p, new FileOutputStream(dest), null);
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
	@Override
	public void startExport(Prozess process, OutputStream os, String xslt) throws IOException {
		try {
			Document doc = createDocument(process, true);

			XMLOutputter outp = new XMLOutputter();
			outp.setFormat(Format.getPrettyFormat());

			outp.output(doc, os);
			os.close();

		} catch (Exception e) {
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
	public Document createDocument(Prozess process, boolean addNamespace) {

		Element processElm = new Element("process");
		Document doc = new Document(processElm);

		processElm.setAttribute("processID", String.valueOf(process.getId()));

		Namespace xmlns = Namespace.getNamespace("http://www.goobi.org/logfile");
		processElm.setNamespace(xmlns);
		// namespace declaration
		if (addNamespace) {

			Namespace xsi = Namespace.getNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
			processElm.addNamespaceDeclaration(xsi);
			Attribute attSchema = new Attribute("schemaLocation", "http://www.goobi.org/logfile" + " XML-logfile.xsd", xsi);
			processElm.setAttribute(attSchema);
		}
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

		StringBuilder batches = new StringBuilder();
		for (Batch batch : process.getBatchesInitialized()) {
			if (batch.getType() != null) {
				batches.append(batch.getTypeTranslated());
				batches.append(": ");
			}
			if (batches.length() != 0) {
				batches.append(", ");
			}
			batches.append(batch.getLabel());
		}
		if (batches.length() != 0) {
			Element batch = new Element("batch", xmlns);
			batch.setText(batches.toString());
			processElements.add(batch);
		}
	

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
					secondProperty.setAttribute("propertyIdentifier", prop.getTitel() + "Encoded");
					if (prop.getWert() != null) {
						secondProperty.setAttribute("value", "vorl:" + replacer(prop.getWert()));
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

		// METS information
		Element metsElement = new Element("metsInformation", xmlns);
		ArrayList<Element> metadataElements = new ArrayList<Element>();

		try {
			String filename = process.getMetadataFilePath();
			Document metsDoc = new SAXBuilder().build(filename);
			Document anchorDoc = null;
			String anchorfilename = process.getMetadataFilePath().replace("meta.xml", "meta_anchor.xml");
			File anchorFile = new File(anchorfilename);
			if (anchorFile.exists() && anchorFile.canRead()) {
				anchorDoc = new SAXBuilder().build(anchorfilename);
			}
			HashMap<String, Namespace> namespaces = new HashMap<String, Namespace>();

			HashMap<String, String> names = getNamespacesFromConfig();
			for (String key : names.keySet()) {
				namespaces.put(key, Namespace.getNamespace(key, names.get(key)));
			}

			HashMap<String, String> fields = getMetsFieldsFromConfig(false);
			for (String key : fields.keySet()) {
				List<Element> metsValues = getMetsValues(fields.get(key), metsDoc, namespaces);
				for (Element element : metsValues) {
					Element ele = new Element("property", xmlns);
					ele.setAttribute("name", key);
					ele.addContent(element.getTextTrim());
					metadataElements.add(ele);
				}
			}

			if (anchorDoc != null) {
				fields = getMetsFieldsFromConfig(true);
				for (String key : fields.keySet()) {
					List<Element> metsValues = getMetsValues(fields.get(key), anchorDoc, namespaces);
					for (Element element : metsValues) {
						Element ele = new Element("property", xmlns);
						ele.setAttribute("name", key);
						ele.addContent(element.getTextTrim());
						metadataElements.add(ele);
					}
				}
			}

			if (metadataElements != null) {
				metsElement.addContent(metadataElements);
				processElements.add(metsElement);
			}

		} catch (SwapException e) {
			logger.error(e);
		} catch (DAOException e) {
			logger.error(e);
		} catch (IOException e) {
			logger.error(e);
		} catch (InterruptedException e) {
			logger.error(e);
		} catch (JDOMException e) {
			logger.error(e);
		} catch (JaxenException e) {
			logger.error(e);
		}

		processElm.setContent(processElements);
		return doc;

	}

	@SuppressWarnings("unchecked")
	public List<Element> getMetsValues(String expr, Object element, HashMap<String, Namespace> namespaces) throws JaxenException {
			JDOMXPath xpath = new JDOMXPath(expr.trim().replace("\n", ""));
			// Add all namespaces
			for (String key : namespaces.keySet()) {
				Namespace value = namespaces.get(key);
				xpath.addNamespace(key, value.getURI());
			}
			return xpath.selectNodes(element);
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
		Document docTrans;
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
		Document doc = createDocument(p, true);
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

	/**
	 * This method exports the production metadata for al list of processes as a single file to a given stream.
	 * 
	 * @param processList
	 * @param outputStream
	 * @param xslt
	 */

	public void startExport(Iterable<Prozess> processList, OutputStream outputStream, String xslt) {
		Document answer = new Document();
		Element root = new Element("processes");
		answer.setRootElement(root);
		Namespace xmlns = Namespace.getNamespace("http://www.goobi.org/logfile");

		Namespace xsi = Namespace.getNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
		root.addNamespaceDeclaration(xsi);
		root.setNamespace(xmlns);
		Attribute attSchema = new Attribute("schemaLocation", "http://www.goobi.org/logfile" + " XML-logfile.xsd", xsi);
		root.setAttribute(attSchema);
		for (Prozess p : processList) {
			Document doc = createDocument(p, false);
			Element processRoot = doc.getRootElement();
			processRoot.detach();
			root.addContent(processRoot);
		}

		XMLOutputter outp = new XMLOutputter();
		outp.setFormat(Format.getPrettyFormat());

		try {
		
			outp.output(answer, outputStream);
		} catch (IOException e) {

		} finally {
			if (outputStream != null) {
				try {
					outputStream.close();
				} catch (IOException e) {
					outputStream = null;
				}
			}
		}

	}

	private HashMap<String, String> getMetsFieldsFromConfig(boolean useAnchor) {
		String xmlpath = "mets.property";
		if (useAnchor) {
			xmlpath = "anchor.property";
		}

		HashMap<String, String> fields = new HashMap<String, String>();
		try {
			File file = new File(new Helper().getGoobiConfigDirectory() + "goobi_exportXml.xml");
			if (file.exists() && file.canRead()) {
				XMLConfiguration config = new XMLConfiguration(file);
				config.setListDelimiter('&');
				config.setReloadingStrategy(new FileChangedReloadingStrategy());

				int count = config.getMaxIndex(xmlpath);
				for (int i = 0; i <= count; i++) {
					String name = config.getString(xmlpath + "(" + i + ")[@name]");
					String value = config.getString(xmlpath + "(" + i + ")[@value]");
					fields.put(name, value);
				}
			}
		} catch (Exception e) {
			fields = new HashMap<String, String>();
		}
		return fields;
	}

	private HashMap<String, String> getNamespacesFromConfig() {
		HashMap<String, String> nss = new HashMap<String, String>();
		try {
			File file = new File(new Helper().getGoobiConfigDirectory() + "goobi_exportXml.xml");
			if (file.exists() && file.canRead()) {
				XMLConfiguration config = new XMLConfiguration(file);
				config.setListDelimiter('&');
				config.setReloadingStrategy(new FileChangedReloadingStrategy());

				int count = config.getMaxIndex("namespace");
				for (int i = 0; i <= count; i++) {
					String name = config.getString("namespace(" + i + ")[@name]");
					String value = config.getString("namespace(" + i + ")[@value]");
					nss.put(name, value);
				}
			}
		} catch (Exception e) {
			nss = new HashMap<String, String>();
		}
		return nss;

	}

}
