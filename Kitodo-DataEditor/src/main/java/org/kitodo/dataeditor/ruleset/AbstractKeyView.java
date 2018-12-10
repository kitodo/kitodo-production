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

package org.kitodo.dataeditor.ruleset;

import java.util.List;
import java.util.Locale.LanguageRange;

import org.kitodo.api.dataeditor.rulesetmanagement.MetadataViewInterface;

/**
 * The abstract type view summarizes methods needed for both the views of keys
 * and views of complex types.
 * 
 * @param <U>
 *            the universal type
 */
abstract class AbstractKeyView<U extends Labeled> implements MetadataViewInterface {
    /**
     * The underlying universal. This can be a universal key or a universal
     * division, so this is just a matter of the universal and it depends on the
     * calling class what it is ultimately about.
     */
    protected final U universal;

    /**
     * The wish list of the user regarding the human languages best understood
     * by it.
     *
     * <p>
     * <small>That takes in both male and female users.</small>
     */
    protected final List<LanguageRange> priorityList;

    /**
     * Creates an abstracted key view.
     *
     * @param universal
     *            the universal, either a universal division or a universal key,
     *            as appropriate
     * @param priorityList
     *            wish language of the user
     */
    protected AbstractKeyView(U universal, List<LanguageRange> priorityList) {
        this.universal = universal;
        this.priorityList = priorityList;
    }

    /**
     * Returns the identifier of the respective universal.
     *
     * @return the identifier
     */
    @Override
    public String getId() {
        return universal.getId();
    }

    /**
     * Gives the best matching label for the universal.
     *
     * @return the label
     */
    @Override
    public String getLabel() {
        return universal.getLabel(priorityList);
    }

    /**
     * Indicates whether the universal is undefined.
     *
     * @return whether the universal is undefined
     */
    @Override
    public boolean isUndefined() {
        return universal.isUndefined();
    }
}
