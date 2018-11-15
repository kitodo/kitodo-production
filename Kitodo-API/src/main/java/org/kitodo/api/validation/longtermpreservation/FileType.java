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

package org.kitodo.api.validation.longtermpreservation;

/**
 * Determines the file types supported by the module.
 */
public enum FileType {
    /**
     * GIF 87a format. Image data only uncompressed.
     *
     * @see "https://www.nationalarchives.gov.uk/pronom/fmt/3"
     * @see "https://www.nationalarchives.gov.uk/pronom/fmt/4"
     */
    GIF,

    /**
     * JPEG format. Versions 1.00, 1.01, and 1.02.
     *
     * @see "https://www.nationalarchives.gov.uk/pronom/fmt/42"
     * @see "https://www.nationalarchives.gov.uk/pronom/fmt/43"
     * @see "https://www.nationalarchives.gov.uk/pronom/fmt/44"
     */
    JPEG,

    /**
     * JPEG 2000 part 1 format.
     *
     * @see "https://www.nationalarchives.gov.uk/pronom/x-fmt/392"
     */
    JPEG_2000,

    /**
     * PDF/A-1 (ISO 19005-1:2005) and PDF/A-2 (ISO 19005-2:2011) format.
     *
     * @see "https://www.nationalarchives.gov.uk/pronom/fmt/95"
     * @see "https://www.nationalarchives.gov.uk/pronom/fmt/354"
     */
    PDF,

    /**
     * PNG format. Versions 1.0, 1.1, and 1.2.
     *
     * @see "https://www.nationalarchives.gov.uk/pronom/fmt/11"
     * @see "https://www.nationalarchives.gov.uk/pronom/fmt/12"
     * @see "https://www.nationalarchives.gov.uk/pronom/fmt/13"
     */
    PNG,

    /**
     * TIFF Rev. 6.0 Part 1 (Baseline TIFF) format. Image data only uncompressed
     * (TIFF tag 259 (data type SHORT) = value 1 ("no compression")).
     *
     * @see "https://www.nationalarchives.gov.uk/pronom/fmt/10"
     */
    TIFF
}
