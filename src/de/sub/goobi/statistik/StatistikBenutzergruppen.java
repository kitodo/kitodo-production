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

import java.util.Iterator;
import java.util.List;

import org.jfree.data.general.Dataset;
import org.jfree.data.general.DefaultPieDataset;

import de.sub.goobi.beans.Benutzergruppe;
import de.sub.goobi.beans.Prozess;
import de.sub.goobi.beans.Schritt;

public class StatistikBenutzergruppen {

	@SuppressWarnings("unchecked")
	public static Dataset getDiagramm(List inProzesse) {
		DefaultPieDataset dataset = new DefaultPieDataset();
		for (Prozess proz : (List<Prozess>) inProzesse) {
			Schritt step = proz.getAktuellerSchritt();
			/* wenn wirklich ein aktueller Schritt zurückgegeben wurde */
			if (step != null) {
				/* von dem Schritt alle verantwortlichen Benutzergruppen ermitteln und im Diagramm erfassen */
				for (Iterator iter2 = step.getBenutzergruppenList().iterator(); iter2.hasNext();) {
					Benutzergruppe group = (Benutzergruppe) iter2.next();
					if (dataset.getIndex(group.getTitel()) != -1) dataset
							.setValue(group.getTitel(), dataset.getValue(group.getTitel()).intValue() + 1);
					else dataset.setValue(group.getTitel(), 1);
				}

			}
		}
		return dataset;
	}

}
