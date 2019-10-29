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

import org.kitodo.production.model.bibliography.course.Granularity;

/**
 * A metadata notation in XML during reading.
 */
public class RecoveredMetadata {
    private final LocalDate date;
    private final String issue;
    private String metadataType;
    private String value;
    private Granularity stepSize;

    public RecoveredMetadata(LocalDate date, String issue) {
        this.date = date;
        this.issue = issue;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getIssue() {
        return issue;
    }

    public String getMetadataType() {
        return metadataType;
    }

    public void setMetadataType(String metadataType) {
        this.metadataType = metadataType;
    }

    public Granularity getStepSize() {
        return stepSize;
    }

    public void setStepSize(Granularity stepSize) {
        this.stepSize = stepSize;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
