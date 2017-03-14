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

import java.util.List;

import org.jfree.data.general.Dataset;
import org.jfree.data.general.DefaultPieDataset;

import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Task;
import org.kitodo.services.ProcessService;

public class StatistikStatus {

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static Dataset getDiagramm(List inProzesse) {
        ProcessService processService = new ProcessService();
        DefaultPieDataset dataset = new DefaultPieDataset();
        for (Process proz : (List<Process>) inProzesse) {
            Task step = processService.getCurrentTask(proz);
            /* wenn wirklich ein aktueller Schritt zurückgegeben wurde */
            if (step != null) {
                /* prüfen, ob der Schritt schon erfasst wurde, wenn ja hochzählen */
                String kurztitel = (step.getTitle().length() > 60 ? step.getTitle().substring(0, 60) + "..." : step
                    .getTitle());
                if (dataset.getIndex(kurztitel) != -1) {
                    dataset.setValue(kurztitel, dataset.getValue(kurztitel).intValue() + 1);
                } else {
                    dataset.setValue(kurztitel, 1);
                }
            }
        }
        return dataset;
    }

}
