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

package org.kitodo.api.dataformat.mets;

/**
 * An enumeration of possible meta-data locations in a METS file. METS
 * distinguishes five fundamentally different types of meta-data.
 */
public enum MdSec {
    /**
     * Meta-data which describes the development of the digital representation
     * of the digitally represented work.
     */
    DIGIPROV_MD,

    /**
     * Meta-data which describes the digital representation of the digitally
     * represented work.
     */
    DMD_SEC,

    /**
     * Meta-data which describes the rights to the digital representation of the
     * digitally represented work.
     */
    RIGHTS_MD,

    /**
     * Meta-data which describes the non-digital work that is digitally
     * represented here.
     */
    SOURCE_MD,

    /**
     * Meta-data that is necessary for the technical processing in any way and
     * therefore needs to be stored somewhere.
     */
    TECH_MD;
}
