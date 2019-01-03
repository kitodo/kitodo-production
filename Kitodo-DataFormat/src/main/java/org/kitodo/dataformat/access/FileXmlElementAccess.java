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
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.kitodo.api.dataformat.MediaUnit;
import org.kitodo.api.dataformat.MediaVariant;
import org.kitodo.api.dataformat.mets.FLocatXmlElementAccessInterface;
import org.kitodo.api.dataformat.mets.FileXmlElementAccessInterface;
import org.kitodo.api.dataformat.mets.UseXmlAttributeAccessInterface;
import org.kitodo.dataformat.metskitodo.DivType;
import org.kitodo.dataformat.metskitodo.DivType.Fptr;
import org.kitodo.dataformat.metskitodo.FileType;
import org.kitodo.dataformat.metskitodo.Mets;

public class FileXmlElementAccess implements FileXmlElementAccessInterface {

    /**
     * The data object of this file XML element access.
     */
    private final MediaUnit mediaUnit;

    /**
     * Public constructor for a new media unit. This constructor can be used
     * with the service loader to get a new instance of media unit.
     */
    public FileXmlElementAccess() {
        mediaUnit = new MediaUnit();
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
        Map<MediaVariant, URI> mediaFiles = div.getFptr().parallelStream().map(Fptr::getFILEID)
                .filter(FileType.class::isInstance)
                .map(FileType.class::cast)
                .collect(Collectors.toMap(
                    file -> useXmlAttributeAccess.get(mets.getFileSec().getFileGrp().parallelStream()
                            .filter(fileGrp -> fileGrp.getFile().contains(file)).findFirst()
                            .orElseThrow(() -> new IllegalArgumentException(
                                    "Corrupt file: <mets:fptr> not referenced in <mets:fileGrp>"))
                            .getUSE()),
                    file -> new FLocatXmlElementAccess(file.getFLocat().get(0)).getUri()));
        mediaUnit.getMediaFiles().putAll(mediaFiles);
        mediaUnit.setOrder(div.getORDER().intValue());
        mediaUnit.setOrderlabel(div.getORDERLABEL());
    }

    public FileXmlElementAccess(MediaUnit mediaUnit) {
        this.mediaUnit = mediaUnit;
    }

    @Override
    public Set<Entry<UseXmlAttributeAccessInterface, FLocatXmlElementAccessInterface>> getAllUsesWithFLocats() {
        throw new UnsupportedOperationException("discontinued interface method pending removal");
    }

    @Override
    public FLocatXmlElementAccessInterface getFLocatForUse(UseXmlAttributeAccessInterface use) {
        throw new UnsupportedOperationException("discontinued interface method pending removal");
    }

    MediaUnit getMediaUnit() {
        return mediaUnit;
    }

    @Override
    public int getOrder() {
        throw new UnsupportedOperationException("discontinued interface method pending removal");
    }

    @Override
    public String getOrderlabel() {
        throw new UnsupportedOperationException("discontinued interface method pending removal");
    }

    @Override
    public void putFLocatForUse(UseXmlAttributeAccessInterface use, FLocatXmlElementAccessInterface fLocat) {
        throw new UnsupportedOperationException("discontinued interface method pending removal");
    }

    @Override
    public void removeFLocatForUse(UseXmlAttributeAccessInterface use) {
        throw new UnsupportedOperationException("discontinued interface method pending removal");
    }

    @Override
    public void setOrder(int order) {
        throw new UnsupportedOperationException("discontinued interface method pending removal");
    }

    @Override
    public void setOrderlabel(String orderlabel) {
        throw new UnsupportedOperationException("discontinued interface method pending removal");
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
        String divId = UUID.randomUUID().toString();
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
