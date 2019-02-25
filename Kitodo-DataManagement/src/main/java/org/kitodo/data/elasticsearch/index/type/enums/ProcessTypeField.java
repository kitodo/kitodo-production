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

public enum ProcessTypeField implements TypeInterface {

    ID("id"),
    TITLE("title"),
    CREATION_DATE("creationDate"),
    WIKI_FIELD("wikiField"),
    SORT_HELPER_ARTICLES("sortHelperArticles"),
    SORT_HELPER_DOCSTRUCTS("sortHelperDocstructs"),
    SORT_HELPER_IMAGES("sortHelperImages"),
    SORT_HELPER_METADATA("sortHelperMetadata"),
    SORT_HELPER_STATUS("sortHelperStatus"),
    PROCESS_BASE_URI("processBaseUri"),
    TEMPLATE_ID("template.id"),
    TEMPLATE_TITLE("template.title"),
    PROJECT_ID("project.id"),
    PROJECT_TITLE("project.title"),
    PROJECT_ACTIVE("project.active"),
    PROJECT_CLIENT_ID("project.client.id"),
    DOCKET("docket"),
    RULESET("ruleset"),
    BATCHES("batches"),
    TASKS("tasks"),
    PROPERTIES("properties"),
    TEMPLATES("templates"),
    WORKPIECES("workpieces"),
    METADATA("meta");

    private String name;

    ProcessTypeField(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
