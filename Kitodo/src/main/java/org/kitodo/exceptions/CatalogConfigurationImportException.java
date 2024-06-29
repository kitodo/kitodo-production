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
 * This is the base exception class for errors that can occur during the import of catalog configurations from the file
 * 'kitodo_opac.xml'.
 */
public class CatalogConfigurationImportException extends Exception {

    /**
     * Constructor with given exception message.
     * @param exceptionMessage as String
     */
    public CatalogConfigurationImportException(String exceptionMessage) {
        super(exceptionMessage);
    }

}
