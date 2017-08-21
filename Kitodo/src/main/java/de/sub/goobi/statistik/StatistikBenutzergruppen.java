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
import org.kitodo.data.database.beans.UserGroup;
import org.kitodo.services.ServiceManager;

public class StatistikBenutzergruppen {
    private static final ServiceManager serviceManager = new ServiceManager();

    /**
     * Get diagram.
     *
     * @param inProzesse
     *            list of Process objects
     * @return Dataset object
     */
    public static Dataset getDiagramm(List<Process> inProzesse) {
        DefaultPieDataset dataset = new DefaultPieDataset();
        for (Process proz : inProzesse) {
            Task step = serviceManager.getProcessService().getCurrentTask(proz);
            /* wenn wirklich ein aktueller Schritt zurückgegeben wurde */
            if (step != null) {
                /*
                 * von dem Schritt alle verantwortlichen Benutzergruppen
                 * ermitteln und im Diagramm erfassen
                 */
                for (UserGroup userGroup : step.getUserGroups()) {
                    if (dataset.getIndex(userGroup.getTitle()) != -1) {
                        dataset.setValue(userGroup.getTitle(), dataset.getValue(userGroup.getTitle()).intValue() + 1);
                    } else {
                        dataset.setValue(userGroup.getTitle(), 1);
                    }
                }

            }
        }
        return dataset;
    }

}
