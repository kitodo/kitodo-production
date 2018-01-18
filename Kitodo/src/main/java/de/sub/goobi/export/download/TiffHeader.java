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

import java.io.IOException;

import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Property;
import org.kitodo.services.ServiceManager;

/**
 * Die Klasse TiffHeader dient zur Generierung einer Tiffheaderdatei *.conf
 *
 * @author Steffen Hankiewicz
 * @version 1.00 - 12.04.2005
 */
public class TiffHeader {
    // private String Haupttitel="";
    // private String Autor="";
    // private String DocType="";
    // private String PPNdigital="";
    // private String Band="";
    // private String TSL="";
    // private String ATS="";
    // private String ISSN="";
    // private String Jahr="";
    // private String Ort="";
    // private String Verlag="";
    private String artist = "";
    private String tifHeaderImageDescription = "";
    private String tifHeaderDocumentName = "";
    private final ServiceManager serviceManager = new ServiceManager();

    /**
     * Erzeugen des Tiff-Headers anhand des übergebenen Prozesses Einlesen der
     * Eigenschaften des Werkstücks bzw. der Scanvorlage
     */
    public TiffHeader(Process process) {
        if (serviceManager.getProcessService().getWorkpiecesSize(process) > 0) {
            for (Property workpieceProperty : process.getWorkpieces()) {
                if (workpieceProperty.getTitle().equals("TifHeaderDocumentname")) {
                    this.tifHeaderDocumentName = workpieceProperty.getValue();
                }
                if (workpieceProperty.getTitle().equals("TifHeaderImagedescription")) {
                    this.tifHeaderImageDescription = workpieceProperty.getValue();
                }
                if (workpieceProperty.getTitle().equals("Artist")) {
                    this.artist = workpieceProperty.getValue();
                }
            }
        }
    }

    /**
     * Rückgabe des kompletten Tiff-Headers.
     */
    public String getImageDescription() {
        return this.tifHeaderImageDescription;
    }

    /**
     * Rückgabe des kompletten Tiff-Headers.
     */
    private String getDocumentName() {
        return this.tifHeaderDocumentName;
    }

    /**
     * Tiff-Header-Daten als ein grosser String.
     */
    public String getTiffAlles() {
        String lineBreak = "\r\n";
        StringBuffer strBuf = new StringBuffer();
        strBuf.append("#");
        strBuf.append(lineBreak);
        strBuf.append("# Configuration file for TIFFWRITER.pl");
        strBuf.append(lineBreak);
        strBuf.append("#");
        strBuf.append(lineBreak);
        strBuf.append("# - overwrites tiff-tags.");
        strBuf.append(lineBreak);
        strBuf.append("#");
        strBuf.append(lineBreak);
        strBuf.append("#");
        strBuf.append(lineBreak);
        strBuf.append("Artist=");
        strBuf.append(this.artist);
        strBuf.append(lineBreak);
        strBuf.append("Documentname=");
        strBuf.append(getDocumentName());
        strBuf.append(lineBreak);
        strBuf.append("ImageDescription=");
        strBuf.append(getImageDescription());
        strBuf.append(lineBreak);
        return strBuf.toString();
    }

    /**
     * Start export.
     */
    public void exportStart() throws IOException {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (!facesContext.getResponseComplete()) {
            HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();
            String fileName = "tiffwriter.conf";
            ServletContext servletContext = (ServletContext) facesContext.getExternalContext().getContext();
            String contentType = servletContext.getMimeType(fileName);
            response.setContentType(contentType);
            response.setHeader("Content-Disposition", "attachment;filename=\"" + fileName + "\"");
            ServletOutputStream out = response.getOutputStream();
            /*
             * die txt-Datei direkt in den Stream schreiben lassen
             */
            out.print(getTiffAlles());

            out.flush();
            facesContext.responseComplete();
        }
    }

}
