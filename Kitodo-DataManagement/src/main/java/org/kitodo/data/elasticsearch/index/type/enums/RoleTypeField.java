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

public enum RoleTypeField implements TypeInterface {

    ID("id"),
    TITLE("title"),
    CLIENT_ID("client.id"),
    CLIENT_NAME("client.name"),
    USERS("users");

    private String name;

    RoleTypeField(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
