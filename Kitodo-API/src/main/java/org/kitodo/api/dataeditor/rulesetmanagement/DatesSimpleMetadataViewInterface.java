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

package org.kitodo.api.dataeditor.rulesetmanagement;

import java.time.MonthDay;

/**
 * Provides an interface for the metadata key view service for a view on the
 * metadata key that is used to store on which date the division dates.
 */
public interface DatesSimpleMetadataViewInterface extends SimpleMetadataViewInterface {
    /**
     * Returns the scheme after which the date value is formatted. Apart from
     * the dates built into Java and interpreted by the runtime, there is still
     * the special string “{@code yyyy/yyyy}”, which stands for a double year,
     * eg. an operation year that starts on a day other than January 1. This
     * works in conjunction with {@link #getYearBegin()}.
     * 
     * @return the scheme after which the date value is formatted
     */
    String getScheme();

    /**
     * Returns the annual start of operation year.
     * 
     * @return the annual start of the operation year
     */
    MonthDay getYearBegin();
}
