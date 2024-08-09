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

package org.kitodo.data.elasticsearch.index.type.enums;

public enum UserTypeField implements TypeInterface {

    ID("id"),
    NAME("name"),
    SURNAME("surname"),
    LOGIN("login"),
    LDAP_LOGIN("ldapLogin"),
    ACTIVE("active"),
    LOCATION("location"),
    METADATA_LANGUAGE("metadataLanguage"),
    ROLES("roles"),
    FILTERS("filters"),
    PROJECTS("projects"),
    CLIENTS("clients"),
    PROCESSING_TASKS("processingTasks");

    private String name;

    UserTypeField(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
