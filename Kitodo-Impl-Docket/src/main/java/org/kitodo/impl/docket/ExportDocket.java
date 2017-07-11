/*
 * (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
 *
 * This file is part of the Kitodo project.
 *
 * It is licensed under GNU General Public License version 3 or later.
 *
 * For the full copyright and license information, please read the
 * GPL3-License.txt file that was distributed with this source code.
 */

package org.kitodo.impl.docket;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;
import org.kitodo.api.docket.DocketData;

/**
 * This class provides generating a run note based on the generated xml log.
 *
 * @author Steffen Hankiewicz
 */
public class ExportDocket {

    /**
     * This method exports the production metadata as run note to a given
     * stream. the docket.xsl has to be in the config-folder.
     *
     * @param docketData
     *            the docketData to export
     * @throws IOException
     * @throws ExportFileException
     */
    public void startExport(DocketData docketData, OutputStream os, File xsltfile) throws IOException {

        ExportXmlLog exl = new ExportXmlLog();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        exl.startExport(docketData, out);

        byte[] pdfBytes = generatePdfBytes(out, xsltfile);

        os.write(pdfBytes);
    }

    public void startExport(Iterable<DocketData> docketDataList, OutputStream os, File xsltfile) throws IOException {

        ExportXmlLog exl = new ExportXmlLog();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        exl.startMultipleExport(docketDataList, out, null);

        byte[] pdfBytes = generatePdfBytes(out, xsltfile);

        os.write(pdfBytes);
    }

    public byte[] generatePdfBytes(ByteArrayOutputStream out, File xsltfile) throws IOException {
        // generate pdf file
        StreamSource source = new StreamSource(new ByteArrayInputStream(out.toByteArray()));
        StreamSource transformSource = new StreamSource(xsltfile);
        FopFactory fopFactory = FopFactory.newInstance();
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        // transform xml
        try {
            Transformer xslfoTransformer = TransformerFactory.newInstance().newTransformer(transformSource);
            Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, outStream);
            Result res = new SAXResult(fop.getDefaultHandler());
            xslfoTransformer.transform(source, res);
        } catch (FOPException e) {
            throw new IOException("FOPException occurred", e);
        } catch (TransformerException e) {
            throw new IOException("TransformerException occurred", e);
        }

        // write the content to output stream
        return outStream.toByteArray();
    }

    private static Transformer getTransformer(StreamSource streamSource) {
        // setup the xslt transformer
        net.sf.saxon.TransformerFactoryImpl impl = new net.sf.saxon.TransformerFactoryImpl();
        try {
            return impl.newTransformer(streamSource);
        } catch (TransformerConfigurationException e) {
        }
        return null;
    }

}
