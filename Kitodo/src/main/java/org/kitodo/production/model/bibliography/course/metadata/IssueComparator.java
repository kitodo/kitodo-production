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

package org.kitodo.production.model.bibliography.course.metadata;

import java.time.LocalDate;
import java.util.Comparator;

import org.apache.commons.lang3.tuple.Pair;
import org.kitodo.production.model.bibliography.course.Block;
import org.kitodo.production.model.bibliography.course.Issue;

/**
 * Defines a sort order on the issues.
 */
public class IssueComparator implements Comparator<Pair<LocalDate, Issue>> {

    /**
     * Block on which the order of issues is defined.
     */
    private Block block;

    /**
     * Creates an issue comparator.
     *
     * @param block
     *            block on which the order of issues is defined.
     */
    public IssueComparator(Block block) {
        this.block = block;
    }

    /**
     * Defines a sort order on the issues.
     *
     * @param comparee
     *            first issue
     * @param compared
     *            second issue
     * @return &lt;0, if the first issue comes before the second one; 0, if the
     *         two issues are equal; &gt;0 if the second issue comes before the
     *         first.
     */
    @Override
    public int compare(Pair<LocalDate, Issue> comparee, Pair<LocalDate, Issue> compared) {
        if (compared == null) {
            return Integer.MIN_VALUE;
        }
        int localDateCompares = comparee.getLeft().compareTo(compared.getLeft());
        if (localDateCompares != 0) {
            return localDateCompares;
        }
        if (comparee.getRight().equals(compared.getRight())) {
            return 0;
        }
        return block.getIssueIndex(comparee.getRight()) - block.getIssueIndex(compared.getRight());
    }
}
