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

package org.kitodo.dataeditor.entities;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.kitodo.config.Config;
import org.kitodo.dataeditor.MetsKitodoObjectFactory;
import org.kitodo.dataformat.metskitodo.DivType;
import org.kitodo.dataformat.metskitodo.FileType;
import org.kitodo.dataformat.metskitodo.StructMapType;

public class PhysicalStructMapType extends StructMapType {

    private MetsKitodoObjectFactory objectFactory = new MetsKitodoObjectFactory();

    /**
     * Constructor to copy the data from parent class.
     *
     * @param structMapType
     *            The structMapType object.
     */
    public PhysicalStructMapType(StructMapType structMapType) {
        super.div = structMapType.getDiv();
        super.id = structMapType.getID();
        super.label = structMapType.getLABEL();
        super.type = structMapType.getTYPE();
    }

    /**
     * Reads the FileSec and inserts corresponding divType objects.
     *
     * @param fileSec
     *            The FileSec object.
     */
    public void createDivsByFileSec(FileSec fileSec) {
        DivType rootDiv = objectFactory.createRootDivTypeForPhysicalStructMap();
        rootDiv.getDiv()
            .addAll(getDivTypesByFileTypes(fileSec.getLocalFileGroup().getFile()));
        this.setDiv(rootDiv);
    }

    private List<DivType> getDivTypesByFileTypes(List<FileType> fileTypes) {
        List<DivType> divTypes = new ArrayList<>();
        int counter = 1;
        for (FileType file : fileTypes) {
            DivType div = objectFactory.createDivType();
            div.setID("PHYS_" + String.format("%04d", counter));
            div.setORDER(BigInteger.valueOf(counter));
            div.setORDERLABEL(Config.getParameter("MetsEditorDefaultPagination"));
            div.setTYPE(getPhysicalDivTypeByFileType(file));
            DivType.Fptr divTypeFptr = objectFactory.createDivTypeFptr();
            divTypeFptr.setFILEID(file);
            div.getFptr().add(divTypeFptr);

            divTypes.add(div);

            counter++;
        }
        return divTypes;
    }

    private String getPhysicalDivTypeByFileType(FileType file) {
        if (file.getMIMETYPE().contains("image")) {
            return "page";
        }
        if (file.getMIMETYPE().contains("audio")) {
            return "track";
        }
        return "other";
    }
}
