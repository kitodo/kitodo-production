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

package de.sub.goobi.helper;
//TODO: Check if this can be moved into UGH
import org.apache.log4j.Logger;

import ugh.dl.DigitalDocument;
import ugh.dl.DocStruct;
import ugh.dl.Fileformat;
import ugh.dl.Metadata;
import ugh.dl.Person;
import ugh.exceptions.PreferencesException;
import de.sub.goobi.beans.Prozess;
import de.sub.goobi.persistence.ProzessDAO;
import de.sub.goobi.helper.exceptions.DAOException;

public class XmlArtikelZaehlen {
	private static final Logger logger = Logger.getLogger(XmlArtikelZaehlen.class);
	public enum CountType {
		METADATA, DOCSTRUCT;
	}

	

	/**
	 * Anzahl der Strukturelemente ermitteln
	 * @param myProzess
	 */
	public int getNumberOfUghElements(Prozess myProzess, CountType inType) {
		int rueckgabe = 0;

		/* --------------------------------
		 * Dokument einlesen 
		 * --------------------------------*/
		Fileformat gdzfile;
		try {
			gdzfile = myProzess.readMetadataFile();
		} catch (Exception e) {
			Helper.setFehlerMeldung("xml error", e.getMessage());
			return -1;
		}

		/* --------------------------------
		 * DocStruct rukursiv durchlaufen
		 * --------------------------------*/
		DigitalDocument mydocument = null;
		try {
			mydocument = gdzfile.getDigitalDocument();
			DocStruct logicalTopstruct = mydocument.getLogicalDocStruct();
			rueckgabe += getNumberOfUghElements(logicalTopstruct, inType);
		} catch (PreferencesException e1) {
			Helper.setFehlerMeldung("[" + myProzess.getId() + "] Can not get DigitalDocument: ", e1.getMessage());
			logger.error(e1);
			rueckgabe = 0;
		}

		/* --------------------------------
		 * die ermittelte Zahl im Prozess speichern
		 * --------------------------------*/
		myProzess.setSortHelperArticles(Integer.valueOf(rueckgabe));
		try {
			new ProzessDAO().save(myProzess);
		} catch (DAOException e) {
			logger.error(e);
		}
		return rueckgabe;
	}

	

	/**
	 * Anzahl der Strukturelemente oder der Metadaten ermitteln, die ein Band hat, rekursiv durchlaufen
	 * @param myProzess
	 */
	public int getNumberOfUghElements(DocStruct inStruct, CountType inType) {
		int rueckgabe = 0;
		if (inStruct != null) {
			/* --------------------------------
			 * increment number of docstructs, or add number of metadata elements
			 * --------------------------------*/
			if (inType == CountType.DOCSTRUCT) {
				rueckgabe++;
			} else {
				/* count non-empty persons */
				if (inStruct.getAllPersons() != null) {
					for (Person p : inStruct.getAllPersons()) {
						if (p.getLastname() != null && p.getLastname().trim().length() > 0) {
							rueckgabe++;
						}
					}
				}
				/* count non-empty metadata */
				if (inStruct.getAllMetadata() != null) {
					for (Metadata md : inStruct.getAllMetadata()) {
						if (md.getValue() != null && md.getValue().trim().length() > 0) {
							rueckgabe++;
						}
					}
				}
			}

			/* --------------------------------
			 * call children recursive
			 * --------------------------------*/
			if (inStruct.getAllChildren() != null) {
				for (DocStruct struct : inStruct.getAllChildren()) {
					rueckgabe += getNumberOfUghElements(struct, inType);
				}
			}
		}
		return rueckgabe;
	}

}