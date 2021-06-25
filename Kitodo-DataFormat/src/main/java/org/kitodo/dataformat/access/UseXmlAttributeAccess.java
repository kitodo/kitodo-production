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

import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.kitodo.api.dataformat.MediaVariant;
import org.kitodo.dataformat.metskitodo.MetsType.FileSec.FileGrp;

/**
 * A variant of the {@code PhysicalDivision}s for a particular use. By use is meant a
 * technical form of use. Therefore, the variant has as technical property the
 * Internet MIME type. Examples of variants include thumbnails, full resolution
 * images, file ready to print, OCR results, etc. For Production, a variant is
 * always linked to a MIME type, it is not possible to have mixed MIME types in
 * the same usage variant.
 */
public class UseXmlAttributeAccess {

    /**
     * The data object of this file XML element access.
     */
    private final MediaVariant mediaVariant;

    /**
     * Public constructor for a media variant. This constructor can be used with
     * the service loader to create a new variant.
     */
    public UseXmlAttributeAccess() {
        mediaVariant = new MediaVariant();
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
    UseXmlAttributeAccess(FileGrp fileGrp) {
        this();
        mediaVariant.setUse(fileGrp.getUSE());
        Set<String> mimeTypes = fileGrp.getFile().parallelStream().map(fileType -> fileType.getMIMETYPE())
                .filter(Objects::nonNull).collect(Collectors.toSet());
        for (Iterator<String> mimeType = mimeTypes.iterator(); mimeTypes.size() > 1 && mimeType.hasNext();) {
            if (StringUtils.isEmpty(mimeType.next())) {
                mimeType.remove();
            }
        }
        switch (mimeTypes.size()) {
            case 0:
                throw new IllegalArgumentException("Corrupt file: <mets:fileGrp USE=\"" + mediaVariant.getUse()
                        + "\"> does not have any <mets:file> with a MIMETYPE.");
            case 1:
                mediaVariant.setMimeType(mimeTypes.iterator().next());
                break;
            default:
                throw new IllegalArgumentException("Corrupt file: <mets:fileGrp USE=\"" + mediaVariant.getUse()
                        + "\"> contains differing MIMETYPE values: " + String.join(", ", mimeTypes));
        }
    }

    public UseXmlAttributeAccess(MediaVariant mediaVariant) {
        this.mediaVariant = mediaVariant;
    }

    MediaVariant getMediaVariant() {
        return mediaVariant;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int hashCode = 1;
        hashCode = prime * hashCode + ((mediaVariant == null) ? 0 : mediaVariant.hashCode());
        return hashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj instanceof UseXmlAttributeAccess) {
            UseXmlAttributeAccess other = (UseXmlAttributeAccess) obj;
            if (Objects.isNull(mediaVariant)) {
                return Objects.isNull(other.mediaVariant);
            } else {
                return mediaVariant.equals(other.mediaVariant);
            }
        }
        return false;
    }
}
