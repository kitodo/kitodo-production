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
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.ugh.DigitalDocumentInterface;
import org.kitodo.api.ugh.DocStructInterface;
import org.kitodo.api.ugh.FileformatInterface;
import org.kitodo.api.ugh.MetadataInterface;
import org.kitodo.api.ugh.PersonInterface;
import org.kitodo.api.ugh.exceptions.PreferencesException;
import org.kitodo.api.ugh.exceptions.ReadException;
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
        int result = 0;

        // read document
        FileformatInterface gdzfile;
        try {
            gdzfile = serviceManager.getProcessService().readMetadataFile(myProcess);
        } catch (PreferencesException | IOException | ReadException | RuntimeException e) {
            Helper.setErrorMessage("xml error", logger, e);
            return -1;
        }

        // DocStruct rukursiv durchlaufen
        try {
            DigitalDocumentInterface document = gdzfile.getDigitalDocument();
            DocStructInterface logicalTopstruct = document.getLogicalDocStruct();
            result += getNumberOfUghElements(logicalTopstruct, inType);
        } catch (PreferencesException e) {
            Helper.setErrorMessage("[" + myProcess.getId() + "] "
                    + Helper.getTranslation("cannotGetDigitalDocument") + ": ", logger, e);
            result = 0;
        }

        // save the determined number in the process
        myProcess.setSortHelperArticles(result);
        try {
            serviceManager.getProcessService().save(myProcess);
        } catch (DataException e) {
            logger.error(e.getMessage(), e);
        }
        return result;
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
        int result = 0;
        if (inStruct != null) {
            /*
             * increment number of docstructs, or add number of metadata
             * elements
             */
            if (inType == CountType.DOCSTRUCT) {
                result++;
            } else {
                result += countNonEmptyPersons(inStruct);
                result += countNonEmptyMetadata(inStruct);
            }

            // call children recursive
            if (inStruct.getAllChildren() != null) {
                for (DocStructInterface struct : inStruct.getAllChildren()) {
                    result += getNumberOfUghElements(struct, inType);
                }
            }
        }
        return result;
    }

    private int countNonEmptyPersons(DocStructInterface inStruct) {
        int result = 0;
        List<PersonInterface> persons = inStruct.getAllPersons();
        if (persons != null) {
            for (PersonInterface person : persons) {
                String lastName = person.getLastName();
                if (lastName != null && lastName.trim().length() > 0) {
                    result++;
                }
            }
        }
        return result;
    }

    private int countNonEmptyMetadata(DocStructInterface inStruct) {
        int result = 0;
        List<MetadataInterface> allMetadata = inStruct.getAllMetadata();
        if (allMetadata != null) {
            for (MetadataInterface md : allMetadata) {
                String value = md.getValue();
                if (value != null && value.trim().length() > 0) {
                    result++;
                }
            }
        }
        return result;
    }

}
