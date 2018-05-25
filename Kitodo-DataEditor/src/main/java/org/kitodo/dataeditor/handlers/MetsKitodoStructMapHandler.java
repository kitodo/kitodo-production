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

package org.kitodo.dataeditor.handlers;

import java.math.BigInteger;
import java.util.NoSuchElementException;
import java.util.Objects;

import org.kitodo.dataeditor.MetsKitodoObjectFactory;
import org.kitodo.dataformat.metskitodo.DivType;
import org.kitodo.dataformat.metskitodo.FileType;
import org.kitodo.dataformat.metskitodo.Mets;
import org.kitodo.dataformat.metskitodo.StructMapType;

public class MetsKitodoStructMapHandler {

    private static final MetsKitodoObjectFactory objectFactory = new MetsKitodoObjectFactory();

    private MetsKitodoStructMapHandler() {
    }

    /**
     * Returns the StructMap element of the given type from given mets object.
     * 
     * @param mets
     *            The mets object from which the StructMap element should be
     *            returned.
     * @param type
     *            The type of the StructMap element which should be returned, e.g.
     *            "PHYSICAL" or "LOGICAL".
     * @return The StructMapType object.
     */
    public static StructMapType getMetsStructMapByType(Mets mets, String type) {
        for (StructMapType structMap : mets.getStructMap()) {
            if (Objects.equals(type, structMap.getTYPE())) {
                return structMap;
            }
        }
        throw new NoSuchElementException("StructMap element of type " + type + " does not exist");
    }

    /**
     * Reads the FileSec of mets object and inserts corresponding physical structMap.
     * 
     * @param mets
     *            The mets object.
     */
    public static void fillPhysicalStructMapByMetsFileSec(Mets mets) {
        DivType rootDiv = objectFactory.createDivType();
        rootDiv.setID("PHYS_0000");
        rootDiv.setTYPE("physSequence");
        StructMapType physicalStructMap = getMetsStructMapByType(mets, "PHYSICAL");
        physicalStructMap.setDiv(rootDiv);

        int counter = 1;
        for (FileType file : MetsKitodoFileSecHandler.getLocalFileGroupOfMets(mets).getFile()) {
            DivType div = objectFactory.createDivType();
            div.setID("PHYS_" + String.format("%04d", counter));
            div.setORDER(BigInteger.valueOf(counter));
            div.setORDERLABEL("uncounted");
            div.setTYPE(getPhysicalDivTypeByFileType(file));
            DivType.Fptr divTypeFptr = objectFactory.createDivTypeFptr();
            divTypeFptr.setFILEID(file);
            div.getFptr().add(divTypeFptr);

            rootDiv.getDiv().add(div);

            counter++;
        }
    }

    private static String getPhysicalDivTypeByFileType(FileType file) {
        if (file.getMIMETYPE().contains("image")) {
            return "page";
        }
        if (file.getMIMETYPE().contains("audio")) {
            return "track";
        }
        return "";
    }
}
