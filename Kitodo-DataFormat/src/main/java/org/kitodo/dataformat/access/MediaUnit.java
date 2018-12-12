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
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.kitodo.api.dataformat.mets.FLocatXmlElementAccessInterface;
import org.kitodo.api.dataformat.mets.FileXmlElementAccessInterface;
import org.kitodo.api.dataformat.mets.UseXmlAttributeAccessInterface;
import org.kitodo.dataformat.metskitodo.DivType;
import org.kitodo.dataformat.metskitodo.DivType.Fptr;
import org.kitodo.dataformat.metskitodo.FileType;
import org.kitodo.dataformat.metskitodo.Mets;

public class MediaUnit implements FileXmlElementAccessInterface {
    /**
     * Each media unit can be available in different variants, for each of which
     * a media file is available. This is in this map.
     */
    private Map<MediaVariant, MediaFile> mediaFiles = new HashMap<>();

    /**
     * Sequence number of the media unit. The playback order of the media units
     * when referenced from a structure is determined by this attribute (not by
     * the order of the references).
     */
    private int order;

    /**
     * A human readable label for the order of this media unit. This need not be
     * directly related to the order number. Examples of order labels could be
     * “I, II, III, IV, V,  - , 1, 2, 3”, meanwhile the order would be “1, 2, 3,
     * 4, 5, 6, 7, 8, 9”.
     */
    private String orderlabel;

    /**
     * Public constructor for a new media unit. This constructor can be used
     * with the service loader to get a new instance of media unit.
     */
    public MediaUnit() {
    }

    /**
     * Constructor for developing a media unit from a METS {@code <div>}
     * element.
     * 
     * @param div
     *            METS {@code <div>} element to be evaluated
     * @param mets
     *            the Mets structure is searched for corresponding uses
     * @param mediaVariants
     *            list of media variants from which the media variant for the
     *            given use is taken
     */
    MediaUnit(DivType div, Mets mets, Map<String, MediaVariant> mediaVariants) {
        mediaFiles = div.getFptr().parallelStream().map(Fptr::getFILEID).filter(FileType.class::isInstance)
                .map(FileType.class::cast)
                .collect(Collectors.toMap(
                    file -> mediaVariants.get(mets.getFileSec().getFileGrp().parallelStream()
                            .filter(fileGrp -> fileGrp.getFile().contains(file)).findFirst()
                            .orElseThrow(() -> new IllegalArgumentException(
                                    "Corrupt file: <mets:fptr> not referenced in <mets:fileGrp>"))
                            .getUSE()),
                    file -> new MediaFile(file.getFLocat().get(0))));
        order = div.getORDER().intValue();
        orderlabel = div.getORDERLABEL();
    }

    /**
     * Returns the list of available media variants with the corresponding media
     * files.
     * 
     * @return available media variants with corresponding media files
     */
    @Override
    public Set<Entry<MediaVariant, MediaFile>> getAllUsesWithFLocats() {
        return mediaFiles.entrySet();
    }

    /**
     * Returns the media file for a particular media variant.
     * 
     * @param mediaVariant
     *            media variant for which the media file is to be returned
     * @return media file for the media variant, can be {@code null} if there is
     *         no file for the requested variant
     */
    @Override
    public FLocatXmlElementAccessInterface getFLocatForUse(UseXmlAttributeAccessInterface mediaVariant) {
        return mediaFiles.get(mediaVariant);
    }

    /**
     * Returns the order number for this media unit.
     * 
     * @return the order number
     */
    @Override
    public int getOrder() {
        return order;
    }

    /**
     * Returns the order label for this media unit.
     * 
     * @return the order label
     */
    @Override
    public String getOrderlabel() {
        return orderlabel;
    }

    /**
     * Adds a media file for a specific usage variant.
     * 
     * @param mediaVariant
     *            variant of the added file
     * @param mediaFile
     *            media file to add
     */
    @Override
    public void putFLocatForUse(UseXmlAttributeAccessInterface mediaVariant,
            FLocatXmlElementAccessInterface mediaFile) {
        mediaFiles.put((MediaVariant) mediaVariant, (MediaFile) mediaFile);

    }

    /**
     * Removes the media file for a specific purpose.
     * 
     * @param mediaVariant
     *            use for which the media file should be removed
     */
    @Override
    public void removeFLocatForUse(UseXmlAttributeAccessInterface mediaVariant) {
        mediaFiles.remove(mediaVariant);

    }

    /**
     * Sets the order number for this media unit.
     * 
     * @param order
     *            order number to set
     */
    @Override
    public void setOrder(int order) {
        this.order = order;
    }

    /**
     * Sets the order label for this media unit.
     * 
     * @param order
     *            order label to set
     */
    @Override
    public void setOrderlabel(String orderlabel) {
        this.orderlabel = orderlabel;
    }

    /**
     * Creates a new METS {@code <div>} element for this media unit.
     * 
     * @param identifierProvider
     *            an object that generates a new, not yet assigned identifier
     *            each time it is called
     * @param mediaFilesToIDFiles
     *            map containing the corresponding XML file element for each
     *            media unit, necessary for linking
     * @param mediaUnitIDs
     *            map with the assigned identifier for each media unit to form
     *            the link pairs of the struct link section
     * @return a new {@code <div>} element for this media unit
     */
    DivType toDiv(Map<MediaFile, FileType> mediaFilesToIDFiles,
            Map<MediaUnit, String> mediaUnitIDs) {

        DivType div = new DivType();
        String divId = UUID.randomUUID().toString();
        div.setID(divId);
        mediaUnitIDs.put(this, divId);
        div.setORDER(BigInteger.valueOf(order));
        div.setORDERLABEL(orderlabel);
        for (Entry<MediaVariant, MediaFile> use : mediaFiles.entrySet()) {
            Fptr fptr = new Fptr();
            fptr.setFILEID(mediaFilesToIDFiles.get(use.getValue()));
            div.getFptr().add(fptr);
        }
        return div;
    }
}
