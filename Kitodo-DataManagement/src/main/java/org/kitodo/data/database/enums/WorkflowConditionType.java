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

package org.kitodo.data.database.enums;

/**
 * Enum for workflow condition type. Types:
 *
 * <dl>
 * <dt>SCRIPT</dt>
 * <dd>path to executable script will be used for value</dd>
 * <dt>XPATH</dt>
 * <dd>expression to search in the metadata file will be used for value</dd>
 * </dl>
 */
public enum WorkflowConditionType {
    NONE,
    SCRIPT,
    XPATH
}
