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
 * @param <D>
 *            the type of declaration
 */
abstract class AbstractKeyView<D extends Labeled> implements MetadataViewInterface {
    /**
     * The underlying declaration. This can be a key or a division declaration.
     */
    protected final D declaration;

    /**
     * The rule.
     */
    protected Rule rule;

    /**
     * The wish list of the user regarding the human languages best understood
     * by it.
     *
     * <p>
     * <small>That takes in both male and female users.</small>
     */
    protected final List<LanguageRange> priorityList;

    /**
     * Creates an abstract key view.
     *
     * @param declaration
     *            the declaration, either a division or a key declaration
     * @param priorityList
     *            wish language of the user
     */
    protected AbstractKeyView(D declaration, Rule rule, List<LanguageRange> priorityList) {
        this.declaration = declaration;
        this.rule = rule;
        this.priorityList = priorityList;
    }

    /**
     * Returns the identifier of the declaration.
     *
     * @return the identifier
     */
    @Override
    public String getId() {
        return declaration.getId();
    }

    /**
     * Gives the best matching label for declaration.
     *
     * @return the label
     */
    @Override
    public String getLabel() {
        return declaration.getLabel(priorityList);
    }

    /**
     * Returns the maximum number of occurrences for this type of metadata.
     *
     * @return the maximum number
     */
    @Override
    public int getMaxOccurs() {
        return rule.getMaxOccurs();
    }

    /**
     * Returns the minimum number of occurrences for this type of metadata.
     *
     * @return the minimum number
     */
    @Override
    public int getMinOccurs() {
        return rule.getMinOccurs();
    }

    /**
     * Indicates whether the declaration is undefined.
     *
     * @return whether the declaration is undefined
     */
    @Override
    public boolean isUndefined() {
        return declaration.isUndefined();
    }
}
