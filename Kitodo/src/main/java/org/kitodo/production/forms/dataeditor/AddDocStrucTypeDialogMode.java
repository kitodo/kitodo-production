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

package org.kitodo.production.forms.dataeditor;

/**
 * Enumeration of the possible dialog sub-dialogs for adding DocStrucTypes.
 */
public enum AddDocStrucTypeDialogMode {
    /**
     * In this way several logical elements can be added at once.
     */
    ADD_MULTIPLE_LOGICAL_ELEMENTS,

    /**
     * In this way meta-data can be added.
     */
    ADD_METADATA,

    /**
     * In this way a link can be created.
     */
    ADD_LINK;
}
