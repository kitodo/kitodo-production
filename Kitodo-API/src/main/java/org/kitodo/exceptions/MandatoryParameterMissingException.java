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

package org.kitodo.exceptions;

/**
 * This exception is thrown during the import of catalog configurations from 'kitodo_opac.xml' when
 * a mandatory XML configuration element like "interfaceType" is missing.
 */
public class MandatoryParameterMissingException extends Exception {

    /**
     * Constructor with given parameter name.
     * @param parameterName name of missing parameter
     */
    public MandatoryParameterMissingException(String parameterName) {
        super("Mandatory XML parameter '" + parameterName + "' missing!");
    }
}
