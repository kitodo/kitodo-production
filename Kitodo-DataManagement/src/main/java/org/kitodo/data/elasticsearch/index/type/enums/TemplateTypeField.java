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
