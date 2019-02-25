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
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import org.kitodo.api.dataformat.MediaUnit;
import org.kitodo.api.dataformat.MediaVariant;
import org.kitodo.dataformat.metskitodo.DivType;
import org.kitodo.dataformat.metskitodo.DivType.Fptr;
import org.kitodo.dataformat.metskitodo.FileType;
import org.kitodo.dataformat.metskitodo.Mets;
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
        if (Objects.nonNull(div.getORDER())) {
            mediaUnit.setOrder(div.getORDER().intValue());
        }
        if (Objects.nonNull(div.getORDERLABEL())) {
            mediaUnit.setOrderlabel(div.getORDERLABEL());
        }
    }

    FileXmlElementAccess(MediaUnit mediaUnit) {
        if (mediaUnit instanceof MediaUnitMetsReferrerStorage) {
            this.mediaUnit = (MediaUnitMetsReferrerStorage) mediaUnit;
        } else {
            this.mediaUnit = new MediaUnitMetsReferrerStorage();
            this.mediaUnit.getMediaFiles().putAll(mediaUnit.getMediaFiles());
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
     * @return a new {@code <div>} element for this media unit
     */
    DivType toDiv(Map<URI, FileType> mediaFilesToIDFiles,
            Map<MediaUnit, String> mediaUnitIDs) {

        DivType div = new DivType();
        String divId = mediaUnit.getDivId();
        div.setID(divId);
        mediaUnitIDs.put(mediaUnit, divId);
        div.setORDER(BigInteger.valueOf(mediaUnit.getOrder()));
        div.setORDERLABEL(mediaUnit.getOrderlabel());
        for (Entry<MediaVariant, URI> use : mediaUnit.getMediaFiles().entrySet()) {
            Fptr fptr = new Fptr();
            fptr.setFILEID(mediaFilesToIDFiles.get(use.getValue()));
            div.getFptr().add(fptr);
        }
        return div;
    }
}
