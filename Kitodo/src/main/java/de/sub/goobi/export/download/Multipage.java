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

package de.sub.goobi.export.download;

import com.sun.media.jai.codec.ImageCodec;
import com.sun.media.jai.codec.ImageEncoder;
import com.sun.media.jai.codec.TIFFEncodeParam;

import de.sub.goobi.config.ConfigCore;
import de.sub.goobi.helper.Helper;

import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;

import javax.faces.context.FacesContext;
import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;
import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.data.database.beans.Process;
import org.kitodo.services.ServiceManager;

/**
 * Die Klasse Multipage dient zur Erzeugung von mehrseitigen Tiffs
 *
 * @author Steffen Hankiewicz
 * @version 1.00 - 12.04.2005
 */

public class Multipage {
    private final ServiceManager serviceManager = new ServiceManager();
    private static final Logger logger = LogManager.getLogger(Multipage.class);

    private void create(Process process) throws IOException {
        // ago through all the TIFFs
        URI path = serviceManager.getFileService().getImagesDirectory(process);
        ArrayList<URI> files = serviceManager.getFileService().getSubUris(Helper.imageNameFilter, path);

        // No TIFFs available, so get out
        if (files.size() == 0) {
            logger.debug("Directory is empty!");
            return;
        }

        // take all pictures into an array
        RenderedImage[] image = new PlanarImage[files.size()];
        for (int i = 0; i < files.size(); i++) {
            if (logger.isDebugEnabled()) {
                logger.debug(path + files.get(i).toString());
            }
            image[i] = JAI.create("fileload", path + files.get(i).toString());
        }
        logger.debug("Bilder durchlaufen");

        // generate all images as multi pages
        OutputStream out = new FileOutputStream(
                ConfigCore.getKitodoDataDirectory() + process.getId() + File.separator + "multipage.tiff");
        TIFFEncodeParam param = new TIFFEncodeParam();
        param.setCompression(4);
        ImageEncoder encoder = ImageCodec.createImageEncoder("TIFF", out, param);
        Vector<RenderedImage> vector = new Vector<>(Arrays.asList(image).subList(1, image.length));
        param.setExtraImages(vector.iterator());
        encoder.encode(image[0]);
        out.close();
        logger.debug("fertig");
    }

    /**
     * Start export.
     *
     * @param process
     *            object
     */
    public void startExport(Process process) throws IOException {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (!facesContext.getResponseComplete()) {
            HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();

            String fileName = process.getTitle() + ".tif";

            ServletContext servletContext = (ServletContext) facesContext.getExternalContext().getContext();
            String contentType = servletContext.getMimeType(fileName);
            response.setContentType(contentType);
            response.setHeader("Content-Disposition", "attachment;filename=\"" + fileName + "\"");
            ServletOutputStream out = response.getOutputStream();

            // let the txt file be written directly into the stream
            String filename = ConfigCore.getKitodoDataDirectory() + process.getId() + File.separator + "multipage.tiff";
            URI fileURI = URI.create(process.getId() + File.separator + "/multipage.tiff");

            if (!serviceManager.getFileService().fileExist(fileURI)) {
                create(process);
            }

            try (FileInputStream fis = new FileInputStream(filename)) {
                byte[] buf = new byte[4096];
                int bytesRead;
                while ((bytesRead = fis.read(buf)) != -1) {
                    out.write(buf, 0, bytesRead);
                }
            }

            // return the stream
            out.flush();
            facesContext.responseComplete();
        }
    }

}
