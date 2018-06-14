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

package org.kitodo.dataeditor;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.transform.TransformerException;

import org.kitodo.dataeditor.entities.FileSec;
import org.kitodo.dataeditor.entities.LogicalStructMapType;
import org.kitodo.dataeditor.entities.PhysicalStructMapType;
import org.kitodo.dataeditor.entities.StructLink;
import org.kitodo.dataeditor.handlers.MetsKitodoMdSecHandler;
import org.kitodo.dataeditor.handlers.MetsKitodoStructMapHandler;
import org.kitodo.dataformat.metskitodo.DivType;
import org.kitodo.dataformat.metskitodo.KitodoType;
import org.kitodo.dataformat.metskitodo.MdSecType;
import org.kitodo.dataformat.metskitodo.Mets;
import org.kitodo.dataformat.metskitodo.MetsType;
import org.kitodo.dataformat.metskitodo.StructMapType;

/**
 * This is a wrapper class for holding and manipulating the content of a
 * serialized mets-kitodo format xml file.
 */
public class MetsKitodoWrapper {

    private Mets mets;
    private MetsKitodoObjectFactory objectFactory = new MetsKitodoObjectFactory();
    private LogicalStructMapType logicalStructMapType;
    private PhysicalStructMapType physicalStructMapType;
    private FileSec fileSec;
    private StructLink structLink;

    /**
     * Gets the mets object.
     *
     * @return The mets object.
     */
    public Mets getMets() {
        return mets;
    }

    /**
     * Constructor which creates a Mets object with corresponding object factory and
     * also inserts the basic mets elements (FileSec with local file group,
     * StructLink, MetsHdr, physical and logical StructMap).
     *
     * @param documentType
     *            The type of the document which will be used for setting the
     *            logical root div type.
     */
    public MetsKitodoWrapper(String documentType) throws DatatypeConfigurationException, IOException {
        this.mets = objectFactory.createMets();
        createBasicMetsElements(this.mets);
        createLogicalRootDiv(this.mets, documentType);
    }

    private void createBasicMetsElements(Mets mets) throws DatatypeConfigurationException, IOException {
        if (Objects.isNull(mets.getFileSec())) {
            mets.setFileSec(objectFactory.createMetsTypeFileSec());
            MetsType.FileSec.FileGrp fileGroup = objectFactory.createMetsTypeFileSecFileGrpLocal();
            mets.getFileSec().getFileGrp().add(fileGroup);
        }
        if (Objects.isNull(mets.getStructLink())) {
            mets.setStructLink(objectFactory.createMetsTypeStructLink());
        }
        if (Objects.isNull(mets.getMetsHdr())) {
            mets.setMetsHdr(objectFactory.createKitodoMetsHeader());
        }
        if (mets.getStructMap().isEmpty()) {
            StructMapType logicalStructMapType = objectFactory.createLogicalStructMapType();
            mets.getStructMap().add(logicalStructMapType);

            StructMapType physicalStructMapType = objectFactory.createPhysicalStructMapType();
            mets.getStructMap().add(physicalStructMapType);
        }
    }

    private void createLogicalRootDiv(Mets mets, String type) {
        MdSecType dmdSecOfLogicalRootDiv = objectFactory.createDmdSecByKitodoMetadata(objectFactory.createKitodoType(),
            "DMDLOG_ROOT");
        mets.getDmdSec().add(dmdSecOfLogicalRootDiv);
        getLogicalStructMap().setDiv(objectFactory.createRootDivTypeForLogicalStructMap(type, dmdSecOfLogicalRootDiv));
    }

    /**
     * Constructor which creates Mets object by unmarshalling given xml file of
     * mets-kitodo format.
     *
     * @param xmlFile
     *            The xml file in mets-kitodo format as URI.
     * @param xsltFile
     *            The URI to the xsl file for transformation of old format goobi
     *            metadata files.
     */
    public MetsKitodoWrapper(URI xmlFile, URI xsltFile)
            throws JAXBException, TransformerException, IOException, DatatypeConfigurationException {
        this.mets = MetsKitodoReader.readAndValidateUriToMets(xmlFile, xsltFile);
        createBasicMetsElements(this.mets);
    }

    /**
     * Gets all dmdSec elements.
     *
     * @return All dmdSec elements as list of MdSecType objects.
     */
    public List<MdSecType> getDmdSecs() {
        return this.mets.getDmdSec();
    }

    /**
     * Inserts MediaFile objects into fileSec of the wrapped mets document and
     * creates corresponding physical structMap.
     *
     * @param files
     *            The list of MediaFile objects.
     */
    public void insertMediaFiles(List<MediaFile> files) {
        getFileSec().insertMediaFiles(files);
        // TODO implement logic to check if pagination is set to automatic or not
        getPhysicalStructMap().createDivsByFileSec(getFileSec());
    }

    /**
     * Returns the physical StructMap of the wrapped mets document.
     *
     * @return The StructMapType object.
     */
    public PhysicalStructMapType getPhysicalStructMap() {
        if (Objects.isNull(this.physicalStructMapType)) {
            this.physicalStructMapType = new PhysicalStructMapType(
                    MetsKitodoStructMapHandler.getMetsStructMapByType(mets, "PHYSICAL"));
        }
        return this.physicalStructMapType;
    }

    /**
     * Returns the logical StructMap of the wrapped mets document.
     *
     * @return The LogicalStructMapType object.
     */
    public LogicalStructMapType getLogicalStructMap() {
        if (Objects.isNull(this.logicalStructMapType)) {
            this.logicalStructMapType = new LogicalStructMapType(
                    MetsKitodoStructMapHandler.getMetsStructMapByType(mets, "LOGICAL"));
        }
        return this.logicalStructMapType;
    }

    /**
     * Returns the FileSec of the wrapped mets document.
     *
     * @return The FileSec object.
     */
    public FileSec getFileSec() {
        if (Objects.isNull(this.fileSec)) {
            this.fileSec = new FileSec(getMets().getFileSec());
        }
        return this.fileSec;
    }

    /**
     * Returns the structLink of the wrapped mets document.
     * 
     * @return The StructLink Object.
     */
    public StructLink getSructLink() {
        if (Objects.isNull(this.structLink)) {
            this.structLink = new StructLink(getMets().getStructLink());
        }
        return this.structLink;
    }

    /**
     * Returns the first KitodoType object and its metadata of an DmdSec element
     * which is referenced by a given logical divType object.
     *
     * @param div
     *            The DivType object which is referencing the DmdSec by DMDID.
     * @return The KitodoType object.
     */
    public KitodoType getFirstKitodoTypeOfLogicalDiv(DivType div) {
        List<Object> objects = div.getDMDID();
        if (!objects.isEmpty()) {
            MdSecType mdSecType = (MdSecType) div.getDMDID().get(0);
            return MetsKitodoMdSecHandler.getKitodoTypeOfDmdSecElement(mdSecType);
        }
        throw new NoSuchElementException("Div element with id: " + div.getID() + " does not have metadata!");
    }

    /**
     * Returns a list of divs from physical structMap which are linked by a given
     * div from logical structMap.
     * 
     * @param logicalDiv
     *            The logical div which links to physical divs.
     * @return A list of physical divs.
     */
    public List<DivType> getPhysicalDivsByLogicalDiv(DivType logicalDiv) {
        return getPhysicalStructMap().getDivsByIds(getSructLink().getPhysicalDivIdsByLogicalDiv(logicalDiv));
    }
}
