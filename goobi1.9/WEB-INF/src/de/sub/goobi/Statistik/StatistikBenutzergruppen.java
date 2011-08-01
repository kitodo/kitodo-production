package de.sub.goobi.Statistik;

import java.util.Iterator;
import java.util.List;

import org.jfree.data.general.Dataset;
import org.jfree.data.general.DefaultPieDataset;

import de.sub.goobi.Beans.Benutzergruppe;
import de.sub.goobi.Beans.Prozess;
import de.sub.goobi.Beans.Schritt;

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
