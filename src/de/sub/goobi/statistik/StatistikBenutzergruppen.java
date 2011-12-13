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
