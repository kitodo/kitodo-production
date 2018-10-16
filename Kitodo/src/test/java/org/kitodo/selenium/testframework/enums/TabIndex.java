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
    USER_GROUPS(1),
    CLIENTS(2),
    LDAP_GROUPS(3),

    // user edit page
    USER_DETAILS(0),
    USER_USER_GROUPS(1),
    USER_PROJECT_LIST(2),
    USER_CLIENT_LIST(3),

    // projects page
    PROJECTS(0),
    TEMPLATES(1),
    WORKFLOWS(2),
    DOCKETS(3),
    RULESETS(4),

    // template edit page
    TEMPLATE_DETAILS(0),
    TEMPLATE_TASKS(1),

    // processes page
    PROCESSES(0),
    BATCHES(1),

    // system page
    INDEXING(0),
    TASKMANAGER(1),

    // user configuration
    SETTINGS(0),
    CHANGE_PASSWORD(1);

    private final int index;

    TabIndex(final int newIndex) {
        index = newIndex;
    }

    public int getIndex() {
        return index;
    }
}
