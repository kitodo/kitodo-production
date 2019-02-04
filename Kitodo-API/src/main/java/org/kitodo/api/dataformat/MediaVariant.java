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

package org.kitodo.api.dataformat;

/**
 * A variant of the {@code MediaUnit}s for a particular use. By use is meant a
 * technical form of use. Therefore, the variant has as technical property the
 * Internet MIME type. Examples of variants include thumbnails, full resolution
 * images, file ready to print, OCR results, etc. For Production, a variant is
 * always linked to a MIME type, it is not possible to have mixed MIME types in
 * the same usage variant.
 */
public class MediaVariant {

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
     * Returns the MIME type of the media variant.
     * 
     * @return the MIME type
     */
    public String getMimeType() {
        return mimeType;
    }

    /**
     * Sets the MIME type of the media variant.
     * 
     * @param mimeType
     *            MIME type to set
     */
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    /**
     * Returns the use of the media variant.
     * 
     * @return the use
     */
    public String getUse() {
        return use;
    }

    /**
     * Sets the use of the media variant.
     * 
     * @param use
     *            use type to set
     */
    public void setUse(String use) {
        this.use = use;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
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
