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
import java.sql.SQLException;

import javax.faces.context.FacesContext;
import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import de.sub.goobi.beans.Prozess;
import de.sub.goobi.beans.Werkstueck;
import de.sub.goobi.beans.Werkstueckeigenschaft;


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
    private String Artist = "";

    private String tifHeader_imagedescription = "";
    private String tifHeader_documentname = "";

    /**
     * Erzeugen des Tiff-Headers anhand des übergebenen Prozesses Einlesen der Eigenschaften des Werkstücks bzw. der Scanvorlage
     */
    public TiffHeader(Prozess inProzess) {
        if (inProzess.getWerkstueckeSize() > 0) {
            Werkstueck myWerkstueck = inProzess.getWerkstueckeList().get(0);
            if (myWerkstueck.getEigenschaftenSize() > 0) {
                for (Werkstueckeigenschaft eig : myWerkstueck.getEigenschaftenList()) {
                    // Werkstueckeigenschaft eig = (Werkstueckeigenschaft) iter.next();

                    if (eig.getTitel().equals("TifHeaderDocumentname")) {
                        this.tifHeader_documentname = eig.getWert();
                    }
                    if (eig.getTitel().equals("TifHeaderImagedescription")) {
                        this.tifHeader_imagedescription = eig.getWert();
                    }

                    if (eig.getTitel().equals("Artist"))
                     {
                        this.Artist = eig.getWert();
                    }
                }
            }
        }
    }

    /**
     * Rückgabe des kompletten Tiff-Headers
     */
    public String getImageDescription() {
        return this.tifHeader_imagedescription;
    }

    /**
     * Rückgabe des kompletten Tiff-Headers
     */
    private String getDocumentName() {
        return this.tifHeader_documentname;
    }

    /**
     *  Tiff-Header-Daten als ein grosser String
     *
     * @throws NamingException
     * @throws SQLException
     * @throws NamingException
     * @throws SQLException
     */
    public String getTiffAlles() {
        String lineBreak = "\r\n";
        StringBuffer strBuf = new StringBuffer();
        strBuf.append("#" + lineBreak);
        strBuf.append("# Configuration file for TIFFWRITER.pl" + lineBreak);
        strBuf.append("#" + lineBreak);
        strBuf.append("# - overwrites tiff-tags." + lineBreak);
        strBuf.append("#" + lineBreak);
        strBuf.append("#" + lineBreak);
        strBuf.append("Artist=" + this.Artist + lineBreak);
        strBuf.append("Documentname=" + getDocumentName() + lineBreak);
        strBuf.append("ImageDescription=" + getImageDescription() + lineBreak);
        return strBuf.toString();
    }

    public void ExportStart() throws IOException {
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
             * -------------------------------- die txt-Datei direkt in den Stream schreiben lassen --------------------------------
             */
            out.print(getTiffAlles());

            out.flush();
            facesContext.responseComplete();
        }
    }

}
