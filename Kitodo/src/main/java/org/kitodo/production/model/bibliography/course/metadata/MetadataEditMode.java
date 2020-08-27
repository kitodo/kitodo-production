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

package org.kitodo.production.model.bibliography.course.metadata;

/**
 * Enumerates the possible edit modes for the metadata.
 */
public enum MetadataEditMode {
    /**
     * The counter continues with the next value.
     */
    CONTINUE,

    /**
     * The counter is (re-)defined (i.e., (re-)starts) here.
     */
    DEFINE,

    /**
     * The counter is deleted here, i.e. stops at the last issue before this one.
     */
    DELETE,

    /**
     * The counter is defined on a later issue, or has been deleted on a previous one.
     */
    HIDDEN
}
