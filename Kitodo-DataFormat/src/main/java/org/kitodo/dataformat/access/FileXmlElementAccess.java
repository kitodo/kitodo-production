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

package org.kitodo.dataformat.access;

import java.math.BigInteger;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;

import org.kitodo.api.MdSec;
import org.kitodo.api.dataformat.PhysicalDivision;
import org.kitodo.api.dataformat.MediaVariant;
import org.kitodo.api.dataformat.mets.KitodoUUID;
import org.kitodo.dataformat.metskitodo.AmdSecType;
import org.kitodo.dataformat.metskitodo.DivType;
import org.kitodo.dataformat.metskitodo.DivType.Fptr;
import org.kitodo.dataformat.metskitodo.FileType;
import org.kitodo.dataformat.metskitodo.MdSecType;
import org.kitodo.dataformat.metskitodo.Mets;
import org.kitodo.dataformat.metskitodo.MetsType;
import org.kitodo.dataformat.metskitodo.MetsType.FileSec.FileGrp;

public class FileXmlElementAccess {

    /**
     * The data object of this file XML element access.
     */
    private final PhysicalDivisionMetsReferrerStorage physicalDivision;

    /**
     * Public constructor for a new physical division. This constructor can be used
     * with the service loader to get a new instance of physical division.
     */
    public FileXmlElementAccess() {
        physicalDivision = new PhysicalDivisionMetsReferrerStorage();
    }

    /**
     * Constructor for developing a physical division from a METS {@code <div>}
     * element.
     *
     * @param div
     *            METS {@code <div>} element to be evaluated
     * @param mets
     *            the Mets structure is searched for corresponding uses
     * @param useXmlAttributeAccess
     *            list of media variants from which the media variant for the
     *            given use is taken
     */
    FileXmlElementAccess(DivType div, Mets mets, Map<String, MediaVariant> useXmlAttributeAccess) {
        this();
        physicalDivision.setDivId(div.getID());
        Map<MediaVariant, URI> mediaFiles = new HashMap<>();
        for (Fptr fptr : div.getFptr()) {
            Object fileId = fptr.getFILEID();
            if (fileId instanceof FileType) {
                FileType file = (FileType) fileId;
                MediaVariant mediaVariant = null;
                for (FileGrp fileGrp : mets.getFileSec().getFileGrp()) {
                    if (fileGrp.getFile().contains(file)) {
                        mediaVariant = useXmlAttributeAccess.get(fileGrp.getUSE());
                        break;
                    }
                }
                if (Objects.isNull(mediaVariant)) {
                    throw new IllegalArgumentException("Corrupt file: <mets:fptr> not referenced in <mets:fileGrp>");
                }
                FLocatXmlElementAccess fLocatXmlElementAccess = new FLocatXmlElementAccess(file);
                physicalDivision.storeFileId(fLocatXmlElementAccess);
                mediaFiles.put(mediaVariant, fLocatXmlElementAccess.getUri());
            }
        }
        physicalDivision.getMediaFiles().putAll(mediaFiles);
        BigInteger order = div.getORDER();
        if (Objects.nonNull(order)) {
            physicalDivision.setOrder(order.intValue());
        }
        physicalDivision.setOrderlabel(div.getORDERLABEL());
        physicalDivision.setType(div.getTYPE());
        for (Object mdSecType : div.getDMDID()) {
            physicalDivision.getMetadata().addAll(DivXmlElementAccess.readMetadata((MdSecType) mdSecType, MdSec.DMD_SEC));
        }
        for (Object mdSecType : div.getADMID()) {
            physicalDivision.getMetadata().addAll(DivXmlElementAccess.readMetadata((MdSecType) mdSecType,
                DivXmlElementAccess.amdSecTypeOf(mets, (MdSecType) mdSecType)));
        }
    }

    FileXmlElementAccess(PhysicalDivision physicalDivision) {
        if (physicalDivision instanceof PhysicalDivisionMetsReferrerStorage) {
            this.physicalDivision = (PhysicalDivisionMetsReferrerStorage) physicalDivision;
        } else {
            this.physicalDivision = new PhysicalDivisionMetsReferrerStorage();
            this.physicalDivision.getMediaFiles().putAll(physicalDivision.getMediaFiles());
            this.physicalDivision.getMetadata().addAll(physicalDivision.getMetadata());
            this.physicalDivision.setOrder(physicalDivision.getOrder());
            this.physicalDivision.setOrderlabel(physicalDivision.getOrderlabel());
            this.physicalDivision.setType(physicalDivision.getType());
        }
    }

    PhysicalDivision getPhysicalDivision() {
        return physicalDivision;
    }

    /**
     * Creates a new METS {@code <div>} element for this physical division.
     *
     * @param mediaFilesToIDFiles
     *            map containing the corresponding XML file element for each
     *            physical division, necessary for linking
     * @param physicalDivisionIDs
     *            map with the assigned identifier for each physical division to form
     *            the link pairs of the struct link section
     * @param mets
     *            the METS structure in which the metadata is added
     * @return a new {@code <div>} element for this physical division
     */
    DivType toDiv(Map<URI, FileType> mediaFilesToIDFiles,
            Map<PhysicalDivision, String> physicalDivisionIDs, MetsType mets) {

        DivType div = new DivType();
        String divId = physicalDivision.getDivId();
        div.setID(divId);
        physicalDivisionIDs.put(physicalDivision, divId);
        if (physicalDivision.getOrder() > 0) {
            div.setORDER(BigInteger.valueOf(physicalDivision.getOrder()));
        }
        div.setORDERLABEL(physicalDivision.getOrderlabel());
        div.setTYPE(physicalDivision.getType());
        for (Entry<MediaVariant, URI> use : physicalDivision.getMediaFiles().entrySet()) {
            Fptr fptr = new Fptr();
            fptr.setFILEID(mediaFilesToIDFiles.get(use.getValue()));
            div.getFptr().add(fptr);
        }
        Optional<MdSecType> optionalDmdSec = DivXmlElementAccess.createMdSec(physicalDivision.getMetadata(), MdSec.DMD_SEC);
        String metsReferrerId = KitodoUUID.randomUUID();
        if (optionalDmdSec.isPresent()) {
            MdSecType dmdSec = optionalDmdSec.get();
            String name = metsReferrerId + ':' + MdSec.DMD_SEC.toString();
            dmdSec.setID(KitodoUUID.nameUUIDFromBytes(name.getBytes(StandardCharsets.UTF_8)));
            mets.getDmdSec().add(dmdSec);
            div.getDMDID().add(dmdSec);
        }
        Optional<AmdSecType> optionalAmdSec = DivXmlElementAccess.createAmdSec(physicalDivision.getMetadata(), metsReferrerId,
            div);
        if (optionalAmdSec.isPresent()) {
            AmdSecType admSec = optionalAmdSec.get();
            mets.getAmdSec().add(admSec);
        }
        return div;
    }
}
