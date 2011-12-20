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

package org.goobi.samples;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;

public class PdfGeneratorSample {

	public static void main(String[] args) throws FOPException, IOException, TransformerException {
		String rootpath = "/Path/To/Your/Workspace/";
		File xmlfile = new File(rootpath + "source.xml");
		File xsltfile = new File(rootpath + "docket.xsl");
		File fofile = new File(rootpath + "target.xml");

		// create xslt for tests
		convertXML2FO(xmlfile, xsltfile, fofile);
		
		// generate pdf file
		StreamSource source = new StreamSource(xmlfile);
		StreamSource transformSource = new StreamSource(xsltfile);
		FopFactory fopFactory = FopFactory.newInstance();
		FOUserAgent foUserAgent = fopFactory.newFOUserAgent();
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		// transform xml 
		Transformer xslfoTransformer = getTransformer(transformSource);
		Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, foUserAgent, outStream);
		Result res = new SAXResult(fop.getDefaultHandler());
		xslfoTransformer.transform(source, res);

		// if you want to get the PDF bytes, use the following code
		//return outStream.toByteArray();

		// if you want to save PDF file use the following code
		File pdffile = new File(rootpath +"target.pdf");
		OutputStream out = new java.io.FileOutputStream(pdffile);
		out = new java.io.BufferedOutputStream(out);
		FileOutputStream str = new FileOutputStream(pdffile);
		str.write(outStream.toByteArray());
		str.close();
		out.close();

		// to write the content to out put stream
		//					byte[] pdfBytes = outStream.toByteArray();
		//					respon	se.setContentLength(pdfBytes.length);
		//					response.setContentType("application/pdf");
		//					response.addHeader("Content-Disposition", "attachment;filename=pdffile.pdf");
		//					response.getOutputStream().write(pdfBytes);
		//					response.getOutputStream().flush();

	}

	private static Transformer getTransformer(StreamSource streamSource) {
		// setup the xslt transformer
		net.sf.saxon.TransformerFactoryImpl impl = new net.sf.saxon.TransformerFactoryImpl();
		try {
			return impl.newTransformer(streamSource);
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
		}
		return null;
	}

	private static void convertXML2FO(File xml, File xslt, File fo) throws IOException, TransformerException {
		//Setup output
		OutputStream out = new java.io.FileOutputStream(fo);
		try {
			//Setup XSLT
			TransformerFactory factory = TransformerFactory.newInstance();
			Transformer transformer = factory.newTransformer(new StreamSource(xslt));
			//Setup input for XSLT transformation
			Source src = new StreamSource(xml);
			//Resulting SAX events (the generated FO) must be piped through to FOP
			Result res = new StreamResult(out);
			//Start XSLT transformation and FOP processing
			transformer.transform(src, res);
		} finally {
			out.close();
		}
	}
	
}
