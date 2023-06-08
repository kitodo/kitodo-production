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
    COMMENTS("comments"),
    COMMENTS_MESSAGE("comments.message"),
    HAS_CHILDREN("hasChildren"),
    PARENT_ID("parent.id"),
    TASKS("tasks"),
    PROPERTIES("properties"),
    TEMPLATES("templates"),
    WORKPIECES("workpieces"),
    METADATA("meta"),
    NUMBER_OF_METADATA("numberOfMetadata"),
    NUMBER_OF_IMAGES("numberOfImages"),
    NUMBER_OF_STRUCTURES("numberOfStructures"),
    BASE_TYPE("baseType"),
    IN_CHOICE_LIST_SHOWN("inChoiceListShown"),
    LAST_EDITING_USER("lastEditingUser"),
    PROCESSING_BEGIN_LAST_TASK("processingBeginLastTask"),
    PROCESSING_END_LAST_TASK("processingEndLastTask"),
    CORRECTION_COMMENT_STATUS("correctionCommentStatus"),
    PROGRESS_CLOSED("progressClosed"),
    PROGRESS_IN_PROCESSING("progressInProcessing"),
    PROGRESS_OPEN("progressOpen"),
    PROGRESS_LOCKED("progressLocked"),
    PROGRESS_COMBINED("progressCombined");

    private String name;

    ProcessTypeField(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
