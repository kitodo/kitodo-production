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

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static Dataset getDiagramm( List inProzesse) {
        DefaultCategoryDataset categoryDataSet = new DefaultCategoryDataset();
        for (Process proz : (List<Process>) inProzesse) {
            for (Task step : proz.getTasks()) {
                /* wenn Anfangs- und Enddatum vorhanden sind, diese auswerten */
                if (step.getProcessingBegin() != null && step.getProcessingEnd() != null) {
                    String kurztitel = (step.getTitle().length() > 60 ? step.getTitle().substring(0, 60) + "..." : step
                        .getTitle());
                    categoryDataSet.addValue(dateDifference(step.getProcessingBegin(), step.getProcessingEnd()),
                        kurztitel, proz.getTitle());
                }
            }
        }
        return categoryDataSet;
    }

    private static int dateDifference(Date datoStart, Date datoEnd) {
        if (datoStart.before(datoEnd)) {
            long difference = datoEnd.getTime() - datoStart.getTime();
            Date datoDifference = new Date(difference);
            Calendar differenz = Calendar.getInstance();
            differenz.setTime(datoDifference);

            int summe = differenz.get(Calendar.DAY_OF_YEAR);
            return summe;
        } else {
            return 1;
        }
    }

}
