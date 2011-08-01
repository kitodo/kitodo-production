package de.sub.goobi.Statistik;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.Dataset;

import de.sub.goobi.Beans.Prozess;
import de.sub.goobi.Beans.Schritt;

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
