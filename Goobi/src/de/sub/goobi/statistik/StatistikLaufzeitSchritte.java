package de.sub.goobi.statistik;

/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 *     		- http://www.goobi.org
 *     		- https://github.com/goobi/goobi-production
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
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.Dataset;

import de.sub.goobi.beans.Prozess;
import de.sub.goobi.beans.Schritt;

public class StatistikLaufzeitSchritte {

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static Dataset getDiagramm( List inProzesse) {
		DefaultCategoryDataset categoryDataSet = new DefaultCategoryDataset();
		for (Prozess proz : (List<Prozess>) inProzesse) {
			for (Schritt step : proz.getSchritteList()) {
				/* wenn Anfangs- und Enddatum vorhanden sind, diese auswerten */
				if (step.getBearbeitungsbeginn() != null && step.getBearbeitungsende() != null) {
					String kurztitel = (step.getTitel().length() > 60 ? step.getTitel().substring(0, 60) + "..." : step
						.getTitel());
					categoryDataSet.addValue(dateDifference(step.getBearbeitungsbeginn(), step.getBearbeitungsende()),
						kurztitel, proz.getTitel());
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
