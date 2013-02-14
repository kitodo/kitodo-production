/*
 * This file is part of the Goobi Application - a Workflow tool for the support of
 * mass digitization.
 *
 * Visit the websites for more information.
 *     - http://gdz.sub.uni-goettingen.de
 *     - http://www.goobi.org
 *     - http://launchpad.net/goobi-production
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details. You
 * should have received a copy of the GNU General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 59 Temple Place,
 * Suite 330, Boston, MA 02111-1307 USA
 */

package de.sub.goobi.statistik;

import java.util.List;

import org.jfree.data.general.Dataset;
import org.jfree.data.general.DefaultPieDataset;

import de.sub.goobi.beans.Prozess;
import de.sub.goobi.beans.Schritt;

public class StatistikStatus {

	@SuppressWarnings("unchecked")
	public static Dataset getDiagramm(List inProzesse) {
		DefaultPieDataset dataset = new DefaultPieDataset();
		for (Prozess proz : (List<Prozess>) inProzesse) {
			Schritt step = proz.getAktuellerSchritt();
			/* wenn wirklich ein aktueller Schritt zurückgegeben wurde */
			if (step != null) {
				/* prüfen, ob der Schritt schon erfasst wurde, wenn ja hochzählen */
				String kurztitel = (step.getTitel().length() > 60 ? step.getTitel().substring(0, 60) + "..." : step
					.getTitel());
				if (dataset.getIndex(kurztitel) != -1)
					dataset.setValue(kurztitel, dataset.getValue(kurztitel).intValue() + 1);
				else
					dataset.setValue(kurztitel, 1);
			}
		}
		return dataset;
	}

}
