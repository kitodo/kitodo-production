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

package org.kitodo.docket;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.FopFactoryBuilder;
import org.apache.fop.apps.MimeConstants;
import org.kitodo.api.docket.DocketData;

/**
 * This class provides generating a run note based on the generated xml log.
 *
 * @author Steffen Hankiewicz
 */
public class ExportDocket {

    File xsltFile;

    /**
     * Makes the class polymorphic.
     * 
     * @param xsltFile
     *            XSLT file
     */
    ExportDocket(File xsltFile) {
        this.xsltFile = xsltFile;
    }

    /**
     * This method exports the production metadata as run note to a given
     * stream. the docket.xsl has to be in the config-folder.
     *
     * @param docketData
     *            the docketData to export
     * @throws IOException
     *             Throws IOException, when pdfGeneration fails
     */
    void startExport(DocketData docketData, OutputStream outputStream) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        new ExportXmlLog(docketData).accept(out);

        byte[] pdfBytes = generatePdfBytes(out);

        outputStream.write(pdfBytes);
        outputStream.flush();
        outputStream.close();
    }

    /**
     * This method exports the production metadata as run note to a given
     * stream. the docket.xsl has to be in the config-folder.
     *
     * @param docketDataList
     *            the docketData to export for several processes
     * @throws IOException
     *             Throws IOException, when pdfGeneration fails.
     */
    void startExport(Iterable<DocketData> docketDataList, OutputStream os) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        new ExportXmlLog(docketDataList).accept(out);

        byte[] pdfBytes = generatePdfBytes(out);

        os.write(pdfBytes);
    }

    private byte[] generatePdfBytes(ByteArrayOutputStream out) throws IOException {
        // generate pdf file
        StreamSource source = new StreamSource(new ByteArrayInputStream(out.toByteArray()));
        StreamSource transformSource = new StreamSource(xsltFile);
        FopFactoryBuilder builder = new FopFactoryBuilder(new File(".").toURI());
        builder.setStrictFOValidation(false);
        FopFactory fopFactory = builder.build();
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        // transform xml
        try {
            Transformer xslTransformer = TransformerFactory.newInstance().newTransformer(transformSource);
            Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, outStream);
            Result res = new SAXResult(fop.getDefaultHandler());
            xslTransformer.transform(source, res);
        } catch (FOPException e) {
            throw new IOException("FOPException occurred", e);
        } catch (TransformerException e) {
            throw new IOException("TransformerException occurred", e);
        }

        // write the content to output stream
        return outStream.toByteArray();
    }

}
