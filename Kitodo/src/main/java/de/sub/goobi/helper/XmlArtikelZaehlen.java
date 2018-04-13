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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.ugh.DigitalDocumentInterface;
import org.kitodo.api.ugh.DocStructInterface;
import org.kitodo.api.ugh.FileformatInterface;
import org.kitodo.api.ugh.MetadataInterface;
import org.kitodo.api.ugh.PersonInterface;
import org.kitodo.api.ugh.exceptions.PreferencesException;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.services.ServiceManager;

public class XmlArtikelZaehlen {
    private final ServiceManager serviceManager = new ServiceManager();
    private static final Logger logger = LogManager.getLogger(XmlArtikelZaehlen.class);

    public enum CountType {
        METADATA,
        DOCSTRUCT
    }

    /**
     * Anzahl der Strukturelemente ermitteln.
     *
     * @param myProcess
     *            process object
     */
    public int getNumberOfUghElements(Process myProcess, CountType inType) {
        int rueckgabe = 0;

        /*
         * Dokument einlesen
         */
        FileformatInterface gdzfile;
        try {
            gdzfile = serviceManager.getProcessService().readMetadataFile(myProcess);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            Helper.setFehlerMeldung("xml error", e.getMessage());
            return -1;
        }

        // DocStruct rukursiv durchlaufen
        DigitalDocumentInterface document;
        try {
            document = gdzfile.getDigitalDocument();
            DocStructInterface logicalTopstruct = document.getLogicalDocStruct();
            rueckgabe += getNumberOfUghElements(logicalTopstruct, inType);
        } catch (PreferencesException e) {
            Helper.setFehlerMeldung("[" + myProcess.getId() + "] Can not get DigitalDocument: ", e.getMessage());
            logger.error(e.getMessage(), e);
            rueckgabe = 0;
        }

        /*
         * die ermittelte Zahl im Prozess speichern
         */
        myProcess.setSortHelperArticles(rueckgabe);
        try {
            serviceManager.getProcessService().save(myProcess);
        } catch (DataException e) {
            logger.error(e.getMessage(), e);
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
    public int getNumberOfUghElements(DocStructInterface inStruct, CountType inType) {
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
                    for (PersonInterface p : inStruct.getAllPersons()) {
                        if (p.getLastName() != null && p.getLastName().trim().length() > 0) {
                            rueckgabe++;
                        }
                    }
                }
                /* count non-empty metadata */
                if (inStruct.getAllMetadata() != null) {
                    for (MetadataInterface md : inStruct.getAllMetadata()) {
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
                for (DocStructInterface struct : inStruct.getAllChildren()) {
                    rueckgabe += getNumberOfUghElements(struct, inType);
                }
            }
        }
        return rueckgabe;
    }

}
