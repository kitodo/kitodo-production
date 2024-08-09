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

package org.kitodo.api.persistentidentifier.urn;

import org.kitodo.api.persistentidentifier.PersistentIdentifierGeneratorInterface;

public interface UnifiedResourceNameGeneratorInterface extends PersistentIdentifierGeneratorInterface {

    /**
     * Generates a URN for the given namespace and id.
     *
     * @param namespace
     *            the URN-namespace (usually unique within an organisation).
     * @param libraryIdentifier
     *            the identifier of the library
     * @param subNamespace
     *            sub namespace
     * @param identifier
     *            the identifier of the specific object to which the URN points.
     * @return a valid URN (including check digit).
     */
    String generate(String namespace, String libraryIdentifier, String subNamespace, String identifier);
}
