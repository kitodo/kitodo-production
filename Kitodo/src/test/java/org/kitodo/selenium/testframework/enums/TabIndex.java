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

package org.kitodo.selenium.testframework.enums;

@SuppressWarnings("unused")
public enum TabIndex {
    // users page
    USERS(0),
    ROLES(1),
    CLIENTS(2),
    LDAP_GROUPS(3),

    // user edit page
    USER_DETAILS(0),
    USER_METADATA_EDITOR_SETTINGS(1),
    USER_ROLES(2),
    USER_PROJECT_LIST(3),
    USER_CLIENT_LIST(4),

    // role edit page
    ROLE_DETAILS(0),

    // projects page
    PROJECTS(0),
    TEMPLATES(1),
    WORKFLOWS(2),
    DOCKETS(3),
    RULESETS(4),
    IMPORT_CONFIGURATIONS(5),
    MAPPING_FILES(6),

    // template edit page
    TEMPLATE_DETAILS(0),
    TEMPLATE_TASKS(1),

    // processes page
    PROCESSES(0),
    BATCHES(1),

    // template edit page
    PROCESS_DETAILS(0),
    PROCESS_TASKS(1),
    PROCESS_TEMPLATES(2),
    PROCESS_WORKPIECES(3),
    PROCESS_PROPERTIES(1),

    // system page
    TASKMANAGER(0),
    INDEXING(2),
    MIGRATION(3),

    // user configuration
    SETTINGS(0),
    CHANGE_PASSWORD(1),

    // import configuration edit page
    IMPORT_CONFIGURATION_DETAILS(0),
    IMPORT_CONFIGURATION_MAPPING_FILES(1);


    private final int index;

    TabIndex(final int newIndex) {
        index = newIndex;
    }

    public int getIndex() {
        return index;
    }
}
