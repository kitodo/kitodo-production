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
import java.util.UUID;

import org.kitodo.api.MdSec;
import org.kitodo.api.dataformat.MediaUnit;
import org.kitodo.api.dataformat.MediaVariant;
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
    private final MediaUnitMetsReferrerStorage mediaUnit;

    /**
     * Public constructor for a new media unit. This constructor can be used
     * with the service loader to get a new instance of media unit.
     */
    public FileXmlElementAccess() {
        mediaUnit = new MediaUnitMetsReferrerStorage();
    }

    /**
     * Constructor for developing a media unit from a METS {@code <div>}
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
        mediaUnit.setDivId(div.getID());
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
                mediaUnit.storeFileId(fLocatXmlElementAccess);
                mediaFiles.put(mediaVariant, fLocatXmlElementAccess.getUri());
            }
        }
        mediaUnit.getMediaFiles().putAll(mediaFiles);
        BigInteger order = div.getORDER();
        if (Objects.nonNull(order)) {
            mediaUnit.setOrder(order.intValue());
        }
        mediaUnit.setOrderlabel(div.getORDERLABEL());
        for (Object mdSecType : div.getDMDID()) {
            mediaUnit.getMetadata().addAll(DivXmlElementAccess.readMetadata((MdSecType) mdSecType, MdSec.DMD_SEC));
        }
        for (Object mdSecType : div.getADMID()) {
            mediaUnit.getMetadata().addAll(DivXmlElementAccess.readMetadata((MdSecType) mdSecType,
                DivXmlElementAccess.amdSecTypeOf(mets, (MdSecType) mdSecType)));
        }
    }

    FileXmlElementAccess(MediaUnit mediaUnit) {
        if (mediaUnit instanceof MediaUnitMetsReferrerStorage) {
            this.mediaUnit = (MediaUnitMetsReferrerStorage) mediaUnit;
        } else {
            this.mediaUnit = new MediaUnitMetsReferrerStorage();
            this.mediaUnit.getMediaFiles().putAll(mediaUnit.getMediaFiles());
            this.mediaUnit.getMetadata().addAll(mediaUnit.getMetadata());
            this.mediaUnit.setOrder(mediaUnit.getOrder());
            this.mediaUnit.setOrderlabel(mediaUnit.getOrderlabel());
        }
    }

    MediaUnit getMediaUnit() {
        return mediaUnit;
    }

    /**
     * Creates a new METS {@code <div>} element for this media unit.
     *
     * @param mediaFilesToIDFiles
     *            map containing the corresponding XML file element for each
     *            media unit, necessary for linking
     * @param mediaUnitIDs
     *            map with the assigned identifier for each media unit to form
     *            the link pairs of the struct link section
     * @param mets
     *            the METS structure in which the meta-data is added
     * @return a new {@code <div>} element for this media unit
     */
    DivType toDiv(Map<URI, FileType> mediaFilesToIDFiles,
            Map<MediaUnit, String> mediaUnitIDs, MetsType mets) {

        DivType div = new DivType();
        String divId = mediaUnit.getDivId();
        div.setID(divId);
        mediaUnitIDs.put(mediaUnit, divId);
        if (mediaUnit.getOrder() > 0) {
            div.setORDER(BigInteger.valueOf(mediaUnit.getOrder()));
        }
        div.setORDERLABEL(mediaUnit.getOrderlabel());
        for (Entry<MediaVariant, URI> use : mediaUnit.getMediaFiles().entrySet()) {
            Fptr fptr = new Fptr();
            fptr.setFILEID(mediaFilesToIDFiles.get(use.getValue()));
            div.getFptr().add(fptr);
        }
        Optional<MdSecType> optionalDmdSec = DivXmlElementAccess.createMdSec(mediaUnit.getMetadata(), MdSec.DMD_SEC);
        String metsReferrerId = UUID.randomUUID().toString();
        if (optionalDmdSec.isPresent()) {
            MdSecType dmdSec = optionalDmdSec.get();
            String name = metsReferrerId + ':' + MdSec.DMD_SEC.toString();
            dmdSec.setID(UUID.nameUUIDFromBytes(name.getBytes(StandardCharsets.UTF_8)).toString());
            mets.getDmdSec().add(dmdSec);
            div.getDMDID().add(dmdSec);
        }
        Optional<AmdSecType> optionalAmdSec = DivXmlElementAccess.createAmdSec(mediaUnit.getMetadata(), metsReferrerId,
            div);
        if (optionalAmdSec.isPresent()) {
            AmdSecType admSec = optionalAmdSec.get();
            mets.getAmdSec().add(admSec);
        }
        return div;
    }
}
