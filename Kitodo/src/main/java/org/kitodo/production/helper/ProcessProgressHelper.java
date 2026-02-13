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

package org.kitodo.production.helper;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.kitodo.data.database.enums.TaskStatus;

public class ProcessProgressHelper {

    /**
     * Returns whether any tasks exist based on the given status counts.
     *
     * @param counts
     *            map containing task status counts
     * @return true if at least one task exists, otherwise false
     */
    public boolean hasAnyTasks(Map<TaskStatus, Integer> counts) {
        if (Objects.isNull(counts) || counts.isEmpty()) {
            return false;
        }
        return counts.values().stream().mapToInt(Integer::intValue).sum() > 0;
    }

    /**
     * Calculates the percentage of tasks for the given status.
     *
     * @param counts
     *            map containing task status counts
     * @param status
     *            task status to calculate percentage for
     * @return percentage value between 0 and 100
     */
    public double progress(Map<TaskStatus, Integer> counts, TaskStatus status) {
        if (Objects.isNull(counts) || counts.isEmpty()) {
            return 0.0;
        }

        int total = counts.values().stream().mapToInt(Integer::intValue).sum();
        if (total == 0) {
            counts.put(TaskStatus.LOCKED, 1);
            total = 1;
        }

        return 100.0 * counts.getOrDefault(status, 0) / total;
    }

    /**
     * Returns percentage of completed tasks.
     *
     * @param counts
     *            map containing task status counts
     * @return percentage of DONE tasks
     */
    public double progressClosed(Map<TaskStatus, Integer> counts) {
        return progress(counts, TaskStatus.DONE);
    }

    /**
     * Returns percentage of tasks currently in processing.
     *
     * @param counts
     *            map containing task status counts
     * @return percentage of INWORK tasks
     */
    public double progressInProcessing(Map<TaskStatus, Integer> counts) {
        return progress(counts, TaskStatus.INWORK);
    }

    /**
     * Returns percentage of open tasks.
     *
     * @param counts
     *            map containing task status counts
     * @return percentage of OPEN tasks
     */
    public double progressOpen(Map<TaskStatus, Integer> counts) {
        return progress(counts, TaskStatus.OPEN);
    }

    /**
     * Builds a formatted tooltip string containing titles of open and in-work tasks.
     *
     * @param titles
     *            map containing task titles grouped by status
     * @return formatted tooltip string
     */
    public String buildTaskTitleTooltip(Map<TaskStatus, List<String>> titles) {
        if (Objects.isNull(titles) || titles.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();

        appendTitles(sb, TaskStatus.OPEN, titles);
        appendTitles(sb, TaskStatus.INWORK, titles);

        return sb.toString();
    }

    private void appendTitles(StringBuilder sb,
                              TaskStatus status,
                              Map<TaskStatus, List<String>> titles) {

        List<String> list = titles.get(status);
        if (Objects.isNull(list) || list.isEmpty()) {
            return;
        }

        if (!sb.isEmpty()) {
            sb.append("\n");
        }

        sb.append(Helper.getTranslation(status.getTitle())).append(":");

        for (String title : list) {
            sb.append("\n - ").append(Helper.getTranslation(title));
        }
    }

    /**
     * Returns a safe empty EnumMap for task status counts.
     *
     * @return empty EnumMap
     */
    public Map<TaskStatus, Integer> emptyCounts() {
        return new EnumMap<>(TaskStatus.class);
    }
}
