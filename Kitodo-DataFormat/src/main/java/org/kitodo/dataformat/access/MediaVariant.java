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

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.kitodo.api.dataformat.mets.UseXmlAttributeAccessInterface;
import org.kitodo.dataformat.metskitodo.MetsType.FileSec.FileGrp;

/**
 * A variant of the {@code MediaUnit}s for a particular use. By use is meant a
 * technical form of use. Therefore, the variant has as technical property the
 * Internet MIME type. Examples of variants include thumbnails, full resolution
 * images, file ready to print, OCR results, etc. For Production, a variant is
 * always linked to a MIME type, it is not possible to have mixed MIME types in
 * the same usage variant.
 */
public class MediaVariant implements UseXmlAttributeAccessInterface {
    /**
     * Specifying the Internet MIME type for this type of use.
     */
    private String mimeType;

    /**
     * Identifier of use. In terms of METS, this is a string, but if the
     * generated METS file is to be used with the DFG Viewer this string must be
     * a prescribed value, depending on its intended use.
     * 
     * @see "https://www.zvdd.de/fileadmin/AGSDD-Redaktion/METS_Anwendungsprofil_2.0.pdf#page=12"
     */
    private String use;

    /**
     * Public constructor for a media variant. This constructor can be used with
     * the service loader to create a new variant.
     */
    public MediaVariant() {
    }

    /**
     * Constructor that creates a variant from a METS {@code <fileGrp>}.
     * 
     * @param fileGrp
     *            METS {@code <fileGrp>} from which a media variant is to be
     *            created
     * @throws IllegalArgumentException
     *             if the MIME type is mixed within the METS {@code <fileGrp>}
     */
    MediaVariant(FileGrp fileGrp) {
        this.use = fileGrp.getUSE();
        Set<String> mimeTypes = fileGrp.getFile().parallelStream().map(fileType -> fileType.getMIMETYPE())
                .filter(Objects::nonNull).collect(Collectors.toSet());
        switch (mimeTypes.size()) {
            case 0:
                throw new IllegalArgumentException("Corrupt file: <mets:fileGrp USE=\"" + this.use
                        + "\"> does not have any <mets:file> with a MIMETYPE.");
            case 1:
                this.mimeType = mimeTypes.iterator().next();
                break;
            default:
                throw new IllegalArgumentException("Corrupt file: <mets:fileGrp USE=\"" + this.use
                        + "\"> contains differing MIMETYPE values: " + String.join(", ", mimeTypes));
        }
    }

    /**
     * Returns the MIME type of the media variant.
     * 
     * @return the MIME type
     */
    @Override
    public String getMimeType() {
        return mimeType;
    }

    /**
     * Returns the use of the media variant.
     * 
     * @return the use
     */
    @Override
    public String getUse() {
        return use;
    }

    /**
     * Sets the MIME type of the media variant.
     * 
     * @param mimeType
     *            MIME type to set
     */
    @Override
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    /**
     * Sets the use of the media variant.
     * 
     * @param use
     *            use type to set
     */
    @Override
    public void setUse(String use) {
        this.use = use;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((mimeType == null) ? 0 : mimeType.hashCode());
        result = prime * result + ((use == null) ? 0 : use.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        MediaVariant other = (MediaVariant) obj;
        if (mimeType == null) {
            if (other.mimeType != null) {
                return false;
            }
        } else if (!mimeType.equals(other.mimeType)) {
            return false;
        }
        if (use == null) {
            if (other.use != null) {
                return false;
            }
        } else if (!use.equals(other.use)) {
            return false;
        }
        return true;
    }
}
