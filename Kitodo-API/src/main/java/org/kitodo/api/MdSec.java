/*
 * (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
 *
 * This file is part of the Kitodo project.
 *
 * It is licensed under GNU General Public License version 3 or later.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.kitodo.api;

/**
 * An enumeration of possible metadata locations in a METS file. METS
 * distinguishes five fundamentally different types of metadata.
 */
public enum MdSec {
    /**
     * Metadata which describes the development of the digital representation
     * of the digitally represented work.
     */
    DIGIPROV_MD,

    /**
     * Metadata which describes the digital representation of the digitally
     * represented work.
     */
    DMD_SEC,

    /**
     * Metadata which describes the rights to the digital representation of the
     * digitally represented work.
     */
    RIGHTS_MD,

    /**
     * Metadata which describes the non-digital work that is digitally
     * represented here.
     */
    SOURCE_MD,

    /**
     * Metadata that is necessary for the technical processing in any way and
     * therefore needs to be stored somewhere.
     */
    TECH_MD;
}
