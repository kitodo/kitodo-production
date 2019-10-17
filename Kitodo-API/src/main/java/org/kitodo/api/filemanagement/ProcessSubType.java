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

package org.kitodo.api.filemanagement;

/**
 * The sub types of processes, which are represented in separate folders or
 * files on the filemanagement.
 */
public enum ProcessSubType {
    IMAGE,
    IMAGE_SOURCE,
    META_XML,
    TEMPLATE,
    IMPORT,
    OCR,
    OCR_PDF,
    OCR_TXT,
    OCR_WORD,
    OCR_ALTO
}
