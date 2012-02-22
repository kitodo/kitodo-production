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

//TODO: This should be transformed into a genral API for image metadata, without the need ofusing the Perl implementation (https://jira.bibforge.org/jira/browse/DPD-84)  

/**
 * Die Klasse TiffHeader dient zur Generierung einer Tiffheaderdatei *.conf
 * 
 * @author Steffen Hankiewicz
 * @version 1.00 - 12.04.2005
 */
public class TiffHeader {
	private String Artist = "";

	private String tifHeader_imagedescription = "";
	private String tifHeader_documentname = "";

	/**
	 * Erzeugen des Tiff-Headers anhand des übergebenen Prozesses Einlesen der Eigenschaften des Werkstücks bzw. der Scanvorlage
	 */
	public TiffHeader(Prozess inProzess) {
		if (inProzess.getWerkstueckeSize() > 0) {
			Werkstueck myWerkstueck = (Werkstueck) inProzess.getWerkstueckeList().get(0);
			if (myWerkstueck.getEigenschaftenSize() > 0) {
				for (Werkstueckeigenschaft eig : myWerkstueck.getEigenschaftenList()) {

					if (eig.getTitel().equals("TifHeaderDocumentname"))
						tifHeader_documentname = eig.getWert();
					if (eig.getTitel().equals("TifHeaderImagedescription"))
						tifHeader_imagedescription = eig.getWert();

					if (eig.getTitel().equals("Artist"))
						Artist = eig.getWert();
				}
			}
		}
	}

	/**
	 * Rückgabe des kompletten Tiff-Headers
	 */
	public String getImageDescription() {
		return tifHeader_imagedescription;
	}

	/**
	 * Rückgabe des kompletten Tiff-Headers
	 */
	private String getDocumentName() {
		return tifHeader_documentname;
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
		strBuf.append("Artist=" + Artist + lineBreak);
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
