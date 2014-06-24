package de.intranda.goobi.plugins.sru;

/**
 * Copyright by intranda GmbH 2013. All rights reserved.
 * 
 * Visit the websites for more information. 
 * 			- http://www.intranda.com
 * 			- http://digiverso.com 
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 59
 * Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
 * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
 * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */

import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

import ugh.dl.DigitalDocument;
import ugh.dl.DocStruct;
import ugh.dl.DocStructType;
import ugh.dl.Fileformat;
import ugh.dl.Prefs;
import ugh.exceptions.PreferencesException;
import ugh.exceptions.ReadException;
import ugh.exceptions.TypeNotAllowedForParentException;
import ugh.fileformats.mets.XStream;
import ugh.fileformats.opac.PicaPlus;

import com.googlecode.fascinator.redbox.sru.SRUClient;

public class SRUHelper {
    private static final Namespace SRW = Namespace.getNamespace("srw", "http://www.loc.gov/zing/srw/");

    private static final Namespace PICA = Namespace.getNamespace("pica", "info:srw/schema/5/picaXML-v1.0");

    // private static final Namespace DC = Namespace.getNamespace("dc", "http://purl.org/dc/elements/1.1/");
    // private static final Namespace DIAG = Namespace.getNamespace("diag", "http://www.loc.gov/zing/srw/diagnostic/");
    // private static final Namespace XCQL = Namespace.getNamespace("xcql", "http://www.loc.gov/zing/cql/xcql/");

    public static String search(String ppn, String address) {
        SRUClient client;
        try {
        		client = new SRUClient("http://"+address, "picaxml", null, null);
            //           client = new SRUClient("http://sru.gbv.de/opac-de-27", "picaxml", null, null);

            return client.getSearchResponse("pica.ppn=" + ppn);
        } catch (MalformedURLException e) {
        }
        return "";
    }
    
    
    public static Node parseResult(String resultString) throws IOException, JDOMException, ParserConfigurationException {

        // removed validation against external dtd
        SAXBuilder builder = new SAXBuilder(false);
        builder.setValidation(false);
        builder.setFeature("http://xml.org/sax/features/validation", false);
        builder.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
        builder.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

        Document doc = builder.build(new StringReader(resultString));
        // srw:searchRetrieveResponse
        Element root = doc.getRootElement();
        // <srw:records>
        Element srw_records = root.getChild("records", SRW);
        // <srw:record>
        Element srw_record = srw_records.getChild("record", SRW);
        // <srw:recordData>
        if (srw_record != null) {
            Element recordData = srw_record.getChild("recordData", SRW);
            Element record = recordData.getChild("record", PICA);

            // generate an answer document
            DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
            org.w3c.dom.Document answer = docBuilder.newDocument();
            org.w3c.dom.Element collection = answer.createElement("collection");
            answer.appendChild(collection);

            org.w3c.dom.Element picaRecord = answer.createElement("record");
            collection.appendChild(picaRecord);

            @SuppressWarnings("unchecked")
            List<Element> data = record.getChildren();
            for (Element datafield : data) {
                if (datafield.getAttributeValue("tag") != null) {
                    org.w3c.dom.Element field = answer.createElement("field");
                    picaRecord.appendChild(field);
                    if (datafield.getAttributeValue("occurrence") != null) {
                        field.setAttribute("occurrence", datafield.getAttributeValue("occurrence"));
                    }
                    field.setAttribute("tag", datafield.getAttributeValue("tag"));
                    @SuppressWarnings("unchecked")
                    List<Element> subfields = datafield.getChildren();
                    for (Element sub : subfields) {
                        org.w3c.dom.Element subfield = answer.createElement("subfield");
                        field.appendChild(subfield);
                        subfield.setAttribute("code", sub.getAttributeValue("code"));
                        Text text = answer.createTextNode(sub.getText());
                        subfield.appendChild(text);
                    }
                }
            }
            return answer.getDocumentElement();
        }
        return null;
    }

    public static Fileformat parsePicaFormat(Node pica, Prefs prefs) throws ReadException, PreferencesException, TypeNotAllowedForParentException {

        PicaPlus pp = new PicaPlus(prefs);
        pp.read(pica);
        DigitalDocument dd = pp.getDigitalDocument();
        Fileformat ff = new XStream(prefs);
        ff.setDigitalDocument(dd);
        /* BoundBook hinzufügen */
        DocStructType dst = prefs.getDocStrctTypeByName("BoundBook");
        DocStruct dsBoundBook = dd.createDocStruct(dst);
        dd.setPhysicalDocStruct(dsBoundBook);
        return ff;

    }
}
