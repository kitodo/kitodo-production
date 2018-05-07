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

package de.sub.goobi.helper.tasks;

import java.util.Comparator;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Defines an order on the objects in the {@code aggregation} set of the
 * {@code ExportNewspaperBatchTask}.
 */
class AggregationOrder implements Comparator<Pair<Pair<LocallyDefinedDate, Integer>, ?>> {

    /**
     * Compares two objects in the {@code aggregation} set of the
     * {@code ExportNewspaperBatchTask} to define a sort order on them.
     * <ul>
     * <li>If the two entries differ by date, they will be sorted by date.
     * <li>If the two issues have the same date, they will be sorted by sorting
     * number. The comparison is {@code null}-proof, where {@code null} sorts
     * before any non-{@code null} value.
     * <li>If the two issues have the same or no sorting number at all, they
     * will be compared in an unpredictable order, but they will be treated as
     * two different individuals. That means, the comparison will never return
     * 0, because 0 would cause one of the entries to be deduplicated (that is,
     * lost) by the tree set.
     * </ul>
     */
    @Override
    public int compare(Pair<Pair<LocallyDefinedDate, Integer>, ?> o1, Pair<Pair<LocallyDefinedDate, Integer>, ?> o2) {
        Pair<LocallyDefinedDate, Integer> one = o1.getKey();
        Pair<LocallyDefinedDate, Integer> another = o2.getKey();

        // if the two issues differ by date, sort them by date
        int dateComparison = ObjectUtils.compare(one.getLeft().getDate(), another.getLeft().getDate());
        if (dateComparison != 0) {
            return dateComparison;
        }

        // if the two issues have the same date and a sorting number, sort them
        // by number
        int sortingNumberComparison = ObjectUtils.compare(one.getRight(), another.getRight());
        if (sortingNumberComparison != 0) {
            return sortingNumberComparison;
        }

        // if the two issues have the same, or no sorting number at all, sort
        // them anyhow, but treat them as two different individuals
        return Integer.compare(System.identityHashCode(one), System.identityHashCode(another));
    }
}
