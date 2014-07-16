package de.sub.goobi.statistik;

/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 *     		- http://www.goobi.org
 *     		- http://launchpad.net/goobi-production
 * 		    - http://gdz.sub.uni-goettingen.de
 * 			- http://www.intranda.com
 * 			- http://digiverso.com 
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
 * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
 * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */
import java.util.Iterator;
import java.util.List;

import org.jfree.data.general.Dataset;
import org.jfree.data.general.DefaultPieDataset;

import de.sub.goobi.beans.Benutzergruppe;
import de.sub.goobi.beans.Prozess;
import de.sub.goobi.beans.Schritt;

public class StatistikBenutzergruppen {

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
						dataset
								.setValue(group.getTitel(), dataset.getValue(group.getTitel()).intValue() + 1);
					} else {
						dataset.setValue(group.getTitel(), 1);
					}
				}

			}
		}
		return dataset;
	}

}
