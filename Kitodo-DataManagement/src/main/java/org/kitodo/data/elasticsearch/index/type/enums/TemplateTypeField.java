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
    OUTPUT_NAME("outputName"),
    CREATION_DATE("creationDate"),
    WIKI_FIELD("wikiField"),
    SORT_HELPER_STATUS("sortHelperStatus"),
    PROJECT_ID("project.id"),
    PROJECT_TITLE("project.title"),
    PROJECT_ACTIVE("project.active"),
    DOCKET("docket"),
    RULESET("ruleset"),
    TASKS("tasks");

    private String name;

    TemplateTypeField(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
