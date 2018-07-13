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

public enum ProjectTypeField {

    ID("id"),
    TITLE("title"),
    START_DATE("startDate"),
    END_DATE("endDate"),
    NUMBER_OF_PAGES("numberOfPages"),
    NUMBER_OF_VOLUMES("numberOfVolumes"),
    FILE_FORMAT_DMS_EXPORT("fileFormatDmsExport"),
    FILE_FORMAT_INTERNAL("fileFormatInternal"),
    METS_RIGTS_OWNER("metsRightsOwner"),
    ACTIVE("active"),
    PROCESSES("processes"),
    TEMPLATES("templates"),
    USERS("users"),
    CLIENT_ID("client.id"),
    CLIENT_NAME("client.name"),
    PROJECT_FILE_GROUPS("projectFileGroups"),
    PFG_FILE_GROUP("fileGroup"),
    PFG_URL_STRUCTURE("urlStructure"),
    PFG_MIME_TYPE("mimeType"),
    PFG_PATH("path");

    private String name;

    ProjectTypeField(String name) {
        this.name = name;
    }

    /**
     * Get name.
     *
     * @return value of name
     */
    public String getName() {
        return name;
    }
}
