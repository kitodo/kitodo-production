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

import de.sub.goobi.beans.Prozess;
import de.sub.goobi.beans.Schritt;

import java.util.List;

import org.jfree.data.general.Dataset;
import org.jfree.data.general.DefaultPieDataset;

public class StatistikStatus {
	/**
	 * @param inProzesse add description
	 * @return add description
	 */
	@SuppressWarnings({"unchecked", "rawtypes" })
	public static Dataset getDiagramm(List inProzesse) {
		DefaultPieDataset dataset = new DefaultPieDataset();
		for (Prozess proz : (List<Prozess>) inProzesse) {
			Schritt step = proz.getAktuellerSchritt();
			/* wenn wirklich ein aktueller Schritt zurückgegeben wurde */
			if (step != null) {
				/* prüfen, ob der Schritt schon erfasst wurde, wenn ja hochzählen */
				String kurztitel = (step.getTitel().length() > 60 ? step.getTitel().substring(0, 60) + "..." : step
						.getTitel());
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
