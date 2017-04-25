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

package de.sub.goobi.helper;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.elasticsearch.exceptions.CustomResponseException;
import org.kitodo.services.ServiceManager;

import ugh.dl.DigitalDocument;
import ugh.dl.DocStruct;
import ugh.dl.Fileformat;
import ugh.dl.Metadata;
import ugh.dl.Person;
import ugh.exceptions.PreferencesException;

public class XmlArtikelZaehlen {
    private final ServiceManager serviceManager = new ServiceManager();
    private static final Logger logger = Logger.getLogger(XmlArtikelZaehlen.class);

    public enum CountType {
        METADATA, DOCSTRUCT;
    }

    /**
     * Anzahl der Strukturelemente ermitteln.
     *
     * @param myProcess
     *            process object
     */
    public int getNumberOfUghElements(Process myProcess, CountType inType) throws IOException, CustomResponseException {
        int rueckgabe = 0;

        /*
         * Dokument einlesen
         */
        Fileformat gdzfile;
        try {
            gdzfile = serviceManager.getProcessService().readMetadataFile(myProcess);
        } catch (Exception e) {
            Helper.setFehlerMeldung("xml error", e.getMessage());
            return -1;
        }

        /*
         * DocStruct rukursiv durchlaufen
         */
        DigitalDocument mydocument = null;
        try {
            mydocument = gdzfile.getDigitalDocument();
            DocStruct logicalTopstruct = mydocument.getLogicalDocStruct();
            rueckgabe += getNumberOfUghElements(logicalTopstruct, inType);
        } catch (PreferencesException e1) {
            Helper.setFehlerMeldung("[" + myProcess.getId() + "] Can not get DigitalDocument: ", e1.getMessage());
            logger.error(e1);
            rueckgabe = 0;
        }

        /*
         * die ermittelte Zahl im Prozess speichern
         */
        myProcess.setSortHelperArticles(Integer.valueOf(rueckgabe));
        try {
            serviceManager.getProcessService().save(myProcess);
        } catch (DAOException e) {
            logger.error(e);
        }
        return rueckgabe;
    }

    /**
     * Anzahl der Strukturelemente oder der Metadaten ermitteln, die ein Band
     * hat, rekursiv durchlaufen.
     *
     * @param inStruct
     *            DocStruct object
     * @param inType
     *            CountType object
     */
    public int getNumberOfUghElements(DocStruct inStruct, CountType inType) {
        int rueckgabe = 0;
        if (inStruct != null) {
            /*
             * increment number of docstructs, or add number of metadata
             * elements
             */
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

            /*
             * call children recursive
             */
            if (inStruct.getAllChildren() != null) {
                for (DocStruct struct : inStruct.getAllChildren()) {
                    rueckgabe += getNumberOfUghElements(struct, inType);
                }
            }
        }
        return rueckgabe;
    }

}
