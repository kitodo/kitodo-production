/*
 * (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
 *
 * This file is part of the Kitodo project.
 *
 * It is licensed under GNU General Public License version 3 or later.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
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
