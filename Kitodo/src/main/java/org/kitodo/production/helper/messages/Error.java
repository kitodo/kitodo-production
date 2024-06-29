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

package org.kitodo.production.helper.messages;

import java.util.Enumeration;
import java.util.Objects;

public class Error extends CustomResourceBundle {

    @Override
    public Enumeration<String> getKeys() {
        return getBaseResources("messages.errors").getKeys();
    }

    @Override
    protected Object handleGetObject(String key) {
        // If there is an external value use that
        Object externalValue = getValueFromExternalResourceBundle(key, "errors");
        if (Objects.nonNull(externalValue)) {
            return externalValue;
        }
        // otherwise use the one defined in the property files
        return getBaseResources("messages.errors").getObject(key);
    }
}
