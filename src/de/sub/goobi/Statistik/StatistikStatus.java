package de.sub.goobi.Statistik;

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
				//               if (kurztitel.length()>60) kurztitel = kurztitel.substring(0,60) + "...";
				if (dataset.getIndex(kurztitel) != -1)
					dataset.setValue(kurztitel, dataset.getValue(kurztitel).intValue() + 1);
				else
					dataset.setValue(kurztitel, 1);
			}
		}
		return dataset;
	}

}
