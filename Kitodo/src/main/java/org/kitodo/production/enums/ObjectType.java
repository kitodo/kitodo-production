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

package org.kitodo.production.enums;

import java.util.ArrayList;
import java.util.List;

import org.kitodo.production.helper.Helper;

public enum ObjectType {
    AUTHORITY("authority", "authorities", false),
    CLIENT("client", "clients", false),
    BATCH("batch", "batches", true),
    DATAEDITORSETTING("dataEditorSetting", "dataEditorSettings", false),
    DOCKET("docket", "dockets", true),
    FOLDER("folder", "folders", false),
    LDAP_GROUP("ldapGroup", "ldapGroups", false),
    LDAP_SERVER("ldapServer", "ldapServers", false),
    PROCESS("process", "processes", true),
    PROJECT("project", "projects", true),
    PROPERTY("property", "properties", false),
    RULESET("ruleset", "rulesets", true),
    TASK("task", "tasks", true),
    TEMPLATE("template", "template", true),
    USER("user", "users", false),
    ROLE("role", "roles", false),
    WORKFLOW("workflow", "workflows", true),
    FILTER("filter", "filters", true),
    IMPORT_CONFIGURATION("importConfig.configuration", "importConfig.configurations", false),
    MAPPING_FILE("mappingFile.file", "mappingFile.files", false),
    COMMENT("comment", "comments", false),
    NONE("", "", false);

    private final String messageKeySingular;

    private final String messageKeyPlural;

    private final boolean indexable;

    /**
     * Constructor setting the message key of the object type, used to retrieve it's
     * translation from the messages resource bundle.
     *
     * @param messageKeySingular
     *            used for translating the object types name
     */
    ObjectType(String messageKeySingular, String messageKeyPlural, boolean indexable) {
        this.messageKeySingular = messageKeySingular;
        this.messageKeyPlural = messageKeyPlural;
        this.indexable = indexable;
    }

    /**
     * Retrieve and return the translation of the object type for singular object.
     *
     * @return singular translation of this object type
     */
    public String getTranslationSingular() {
        return Helper.getTranslation(messageKeySingular);
    }

    /**
     * Retrieve and return the translation of the object type for plural object.
     *
     * @return plural translation of this object type
     */
    public String getTranslationPlural() {
        return Helper.getTranslation(messageKeyPlural);
    }

    /**
     * Get indexable.
     *
     * @return value of indexable
     */
    public boolean isIndexable() {
        return indexable;
    }

    /**
     * Get list of indexable object types.
     *
     * @return list of indexable object types.
     */
    public static List<ObjectType> getIndexableObjectTypes() {
        List<ObjectType> objectTypes = new ArrayList<>();
        for (ObjectType objectType : ObjectType.values()) {
            if (objectType.isIndexable()) {
                objectTypes.add(objectType);
            }
        }
        return objectTypes;
    }
}
