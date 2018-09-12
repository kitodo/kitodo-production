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
    WIKI_FIELD("wikiField"),
    SORT_HELPER_STATUS("sortHelperStatus"),
    WORKFLOW_TITLE("workflow.title"),
    WORKFLOW_FILE_NAME("workflow.fileName"),
    DOCKET("docket"),
    RULESET("ruleset"),
    PROJECTS("projects");

    private String name;

    TemplateTypeField(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
