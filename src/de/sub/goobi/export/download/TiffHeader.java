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
			Werkstueck myWerkstueck = (Werkstueck) inProzess.getWerkstueckeList().get(0);
			if (myWerkstueck.getEigenschaftenSize() > 0) {
				for (Werkstueckeigenschaft eig : myWerkstueck.getEigenschaftenList()) {
					// Werkstueckeigenschaft eig = (Werkstueckeigenschaft) iter.next();

					if (eig.getTitel().equals("TifHeaderDocumentname"))
						tifHeader_documentname = eig.getWert();
					if (eig.getTitel().equals("TifHeaderImagedescription"))
						tifHeader_imagedescription = eig.getWert();

					// if (eig.getTitel().equals("PPN digital"))
					// PPNdigital = eig.getWert();
					// if (eig.getTitel().equals("Band") && Band == null)
					// Band = eig.getWert();
					// if (eig.getTitel().equals("BandTiffheader"))
					// Band = eig.getWert();
					// if (eig.getTitel().equals("TSL"))
					// TSL = eig.getWert();
					// if (eig.getTitel().equals("ATS"))
					// ATS = eig.getWert();
					// if (eig.getTitel().equals("ISSN"))
					// ISSN = eig.getWert();
					// if (eig.getTitel().equals("DocType"))
					// DocType = eig.getWert();
					if (eig.getTitel().equals("Artist"))
						Artist = eig.getWert();
					// if (eig.getTitel().equals("Erscheinungsort"))
					// Ort = eig.getWert();
					// if (eig.getTitel().equals("Verlag"))
					// Verlag = eig.getWert();
					// if (eig.getTitel().equals("Autor"))
					// Autor = eig.getWert();
					// if (eig.getTitel().equals("Haupttitel"))
					// Haupttitel = eig.getWert();
					// if (eig.getTitel().equals("Erscheinungsjahr"))
					// Jahr = eig.getWert();
				}
			}

			// /* Die Bandnummer auf vier Stellen ändern */
			// if (Band != null && Band.length() < 4)
			// Band = "0000".substring(Band.length()) + Band;
		}
	}

	/**
	 * Rückgabe des kompletten Tiff-Headers
	 */
	public String getImageDescription() {
		return tifHeader_imagedescription;
		// StringBuffer strBuf = new StringBuffer();
		// strBuf.append("|<RUSDML>|");
		//
		// /* --------------------------------
		// * Tiff-Header für eine Zeitschrift
		// * --------------------------------*/
		// if (DocType.equals("periodical")) {
		// if (Haupttitel != null && Haupttitel.length() > 0)
		// strBuf.append("<HAUPTTITEL>|<" + Haupttitel + ">|");
		// if (Verlag != null && Verlag.length() > 0)
		// strBuf.append("<VERLAG>|<" + Verlag + ">|");
		// if (Jahr != null && Jahr.length() > 0)
		// strBuf.append("<ERSCHEINUNGSJAHR>|<" + Jahr + ">|");
		// if (Ort != null && Ort.length() > 0)
		// strBuf.append("<ERSCHEINUNGSORT>|<" + Ort + ">|");
		// if (TSL != null && TSL.length() > 0)
		// strBuf.append("<TSL>|<" + TSL + ">|");
		// if (PPNdigital != null && PPNdigital.length() > 0)
		// strBuf.append("<PPN>|<" + PPNdigital + ">|");
		// if (ISSN != null && ISSN.length() > 0)
		// strBuf.append("<ISSN>|<" + ISSN + ">|");
		// if (Band != null && Band.length() > 0)
		// strBuf.append("<BAND>|<" + Band + ">|");
		// }
		//
		// /* --------------------------------
		// * Tiff-Header für eine Zeitschriftenreihe
		// * --------------------------------*/
		// if (DocType.equals("periodicalrow")) {
		// if (Haupttitel != null && Haupttitel.length() > 0)
		// strBuf.append("<HAUPTTITEL>|<" + Haupttitel + ">|");
		// if (Autor != null && Autor.length() > 0)
		// strBuf.append("<AUTOR>|<" + Autor + ">|");
		// if (Verlag != null && Verlag.length() > 0)
		// strBuf.append("<VERLAG>|<" + Verlag + ">|");
		// if (Jahr != null && Jahr.length() > 0)
		// strBuf.append("<ERSCHEINUNGSJAHR>|<" + Jahr + ">|");
		// if (Ort != null && Ort.length() > 0)
		// strBuf.append("<ERSCHEINUNGSORT>|<" + Ort + ">|");
		// if (TSL != null && TSL.length() > 0)
		// strBuf.append("<TSL>|<" + TSL + ">|");
		// if (PPNdigital != null && PPNdigital.length() > 0)
		// strBuf.append("<PPN>|<" + PPNdigital + ">|");
		// if (ISSN != null && ISSN.length() > 0)
		// strBuf.append("<ISSN>|<" + ISSN + ">|");
		// if (Band != null && Band.length() > 0)
		// strBuf.append("<BAND>|<" + Band + ">|");
		// }
		//
		// /* --------------------------------
		// * Tiff-Header für eine Monographie
		// * --------------------------------*/
		// if (DocType.equals("monograph")) {
		// if (Autor != null && Autor.length() > 0)
		// strBuf.append("<AUTOR>|<" + Autor + ">|");
		// if (Haupttitel != null && Haupttitel.length() > 0)
		// strBuf.append("<HAUPTTITEL>|<" + Haupttitel + ">|");
		// if (Verlag != null && Verlag.length() > 0)
		// strBuf.append("<VERLAG>|<" + Verlag + ">|");
		// if (Jahr != null && Jahr.length() > 0)
		// strBuf.append("<ERSCHEINUNGSJAHR>|<" + Jahr + ">|");
		// if (Ort != null && Ort.length() > 0)
		// strBuf.append("<ERSCHEINUNGSORT>|<" + Ort + ">|");
		// if (ATS != null && ATS.length() > 0)
		// strBuf.append("<ATS>|<" + ATS + ">|");
		// if (PPNdigital != null && PPNdigital.length() > 0)
		// strBuf.append("<PPN>|<" + PPNdigital + ">|");
		// }
		//
		// /* --------------------------------
		// * Tiff-Header für ein Mehrb�ndiges Werk
		// * --------------------------------*/
		// if (DocType.equals("multivolume")) {
		// if (Autor != null && Autor.length() > 0)
		// strBuf.append("<AUTOR>|<" + Autor + ">|");
		// if (Haupttitel != null && Haupttitel.length() > 0)
		// strBuf.append("<HAUPTTITEL>|<" + Haupttitel + ">|");
		// if (Verlag != null && Verlag.length() > 0)
		// strBuf.append("<VERLAG>|<" + Verlag + ">|");
		// if (Jahr != null && Jahr.length() > 0)
		// strBuf.append("<ERSCHEINUNGSJAHR>|<" + Jahr + ">|");
		// if (Ort != null && Ort.length() > 0)
		// strBuf.append("<ERSCHEINUNGSORT>|<" + Ort + ">|");
		// if (ATS != null && ATS.length() > 0)
		// strBuf.append("<ATS>|<" + ATS + ">|");
		// if (PPNdigital != null && PPNdigital.length() > 0)
		// strBuf.append("<PPN>|<" + PPNdigital + ">|");
		// if (Band != null && Band.length() > 0)
		// strBuf.append("<BAND>|<" + Band + ">|");
		// }
		//
		// return strBuf.toString();
	}

	/**
	 * Rückgabe des kompletten Tiff-Headers
	 */
	private String getDocumentName() {
		return tifHeader_documentname;
		// StringBuffer strBuf = new StringBuffer();
		// if (DocType.equals("Monographie")) {
		// if (ATS != null && ATS.length() > 0)
		// strBuf.append(ATS + "_");
		// if (PPNdigital != null && PPNdigital.length() > 0)
		// strBuf.append(PPNdigital + "_");
		// } else {
		// if (TSL != null && TSL.length() > 0)
		// strBuf.append(TSL + "_");
		// if (PPNdigital != null && PPNdigital.length() > 0)
		// strBuf.append("PPN" + PPNdigital + "_");
		// if (Band != null && Band.length() > 0)
		// strBuf.append(Band);
		// }
		// return strBuf.toString();
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
//		 strBuf.append("# - has to be in tif folder"
//		 + lineBreak);
		strBuf.append("# - overwrites tiff-tags." + lineBreak);
		strBuf.append("#" + lineBreak);
		// strBuf.append("#CheckNames=no" + lineBreak);
		// strBuf.append("#CheckNames=yes" + lineBreak);
		strBuf.append("#" + lineBreak);
		strBuf.append("Artist=" + Artist + lineBreak);
		strBuf.append("Documentname=" + getDocumentName() + lineBreak);
		strBuf.append("ImageDescription=" + getImageDescription() + lineBreak);
		// strBuf.append("#ResolutionX=200" + lineBreak);
		// strBuf.append("#ResolutionY=203" + lineBreak);
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
