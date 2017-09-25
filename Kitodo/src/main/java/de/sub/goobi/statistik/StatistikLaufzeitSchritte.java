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

package de.sub.goobi.statistik;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.Dataset;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Task;

public class StatistikLaufzeitSchritte {

    /**
     * Get diagram.
     * 
     * @param processes
     *            List
     * @return diagram as Dataset
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    public static Dataset getDiagramm(List processes) {
        DefaultCategoryDataset categoryDataSet = new DefaultCategoryDataset();
        for (Process process : (List<Process>) processes) {
            for (Task task : process.getTasks()) {
                /* wenn Anfangs- und Enddatum vorhanden sind, diese auswerten */
                if (task.getProcessingBegin() != null && task.getProcessingEnd() != null) {
                    String shortTitle = (task.getTitle().length() > 60 ? task.getTitle().substring(0, 60) + "..."
                            : task.getTitle());
                    categoryDataSet.addValue(dateDifference(task.getProcessingBegin(), task.getProcessingEnd()),
                            shortTitle, process.getTitle());
                }
            }
        }
        return categoryDataSet;
    }

    private static int dateDifference(Date dateStart, Date dateEnd) {
        if (dateStart.before(dateEnd)) {
            long difference = dateEnd.getTime() - dateStart.getTime();
            Date dateDifference = new Date(difference);
            Calendar differenz = Calendar.getInstance();
            differenz.setTime(dateDifference);
            return differenz.get(Calendar.DAY_OF_YEAR);
        } else {
            return 1;
        }
    }

}
