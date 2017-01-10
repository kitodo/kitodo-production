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

import de.sub.goobi.beans.Benutzergruppe;
import de.sub.goobi.beans.Prozess;
import de.sub.goobi.beans.Schritt;

import java.util.Iterator;
import java.util.List;

import org.jfree.data.general.Dataset;
import org.jfree.data.general.DefaultPieDataset;

public class StatistikBenutzergruppen {
	/**
	 * @param inProzesse add description
	 * @return add description
	 */
	public static Dataset getDiagramm(List<Prozess> inProzesse) {
		DefaultPieDataset dataset = new DefaultPieDataset();
		for (Prozess proz : inProzesse) {
			Schritt step = proz.getAktuellerSchritt();
			/* wenn wirklich ein aktueller Schritt zurückgegeben wurde */
			if (step != null) {
				/* von dem Schritt alle verantwortlichen Benutzergruppen ermitteln und im Diagramm erfassen */
				for (Iterator<Benutzergruppe> iter2 = step.getBenutzergruppenList().iterator(); iter2.hasNext();) {
					Benutzergruppe group = iter2.next();
					if (dataset.getIndex(group.getTitel()) != -1) {
						dataset.setValue(group.getTitel(), dataset.getValue(group.getTitel()).intValue() + 1);
					} else {
						dataset.setValue(group.getTitel(), 1);
					}
				}

			}
		}
		return dataset;
	}

}
