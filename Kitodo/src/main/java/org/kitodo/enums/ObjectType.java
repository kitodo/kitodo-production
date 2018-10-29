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

package org.kitodo.enums;

import org.kitodo.helper.Helper;

public enum ObjectType {
    AUTHORITY("authority", "authorities"),
    CLIENT("client", "clients"),
    BATCH("batch", "batches"),
    DOCKET("docket", "dockets"),
    PROCESS("process", "processes"),
    PROJECT("project", "projects"),
    PROPERTY("property", "properties"),
    RULESET("ruleset", "rulesets"),
    TASK("task", "tasks"),
    TEMPLATE("template", "template"),
    USER("user", "users"),
    ROLE("role", "roles"),
    WORKFLOW("workflow", "workflows"),
    FILTER("filter", "filters"),
    NONE("", "");

    private String messageKeySingular;

    private String messageKeyPlural;

    /**
     * Constructor setting the message key of the object type, used to retrieve it's
     * translation from the messages resource bundle.
     *
     * @param messageKeySingular
     *            used for translating the object types name
     */
    ObjectType(String messageKeySingular, String messageKeyPlural) {
        this.messageKeySingular = messageKeySingular;
        this.messageKeyPlural = messageKeyPlural;
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
}
