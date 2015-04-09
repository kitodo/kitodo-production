package de.sub.goobi.export.download;

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
