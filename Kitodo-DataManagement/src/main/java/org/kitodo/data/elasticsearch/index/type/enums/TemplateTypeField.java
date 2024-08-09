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

public enum TemplateTypeField implements TypeInterface {

    ID("id"),
    TITLE("title"),
    CREATION_DATE("creationDate"),
    ACTIVE("active"),
    SORT_HELPER_STATUS("sortHelperStatus"),
    CLIENT_ID("client.id"),
    CLIENT_NAME("client.name"),
    DOCKET("docket"),
    RULESET_ID("ruleset.id"),
    RULESET_TITLE("ruleset.title"),
    WORKFLOW_TITLE("workflow.title"),
    PROJECTS("projects"),
    TASKS("tasks");

    private final String name;

    TemplateTypeField(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
