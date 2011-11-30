package de.sub.goobi.helper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.log4j.Logger;
import org.goobi.production.properties.IProperty;
import org.goobi.production.properties.ProcessProperty;
import org.goobi.production.properties.PropertyParser;

import de.sub.goobi.Beans.Prozess;
import de.sub.goobi.Beans.Prozesseigenschaft;
import de.sub.goobi.Beans.Schritt;
import de.sub.goobi.Persistence.ProzessDAO;
import de.sub.goobi.helper.exceptions.DAOException;

public class BatchHelper {

	private List<Schritt> steps;
	private ProzessDAO pdao = new ProzessDAO();
	private static final Logger logger = Logger.getLogger(BatchHelper.class);

	public BatchHelper(List<Schritt> steps) {
		this.setSteps(steps);
		loadProcessProperties(steps.get(0));
	}

	public List<Schritt> getSteps() {
		return this.steps;
	}

	public void setSteps(List<Schritt> steps) {
		this.steps = steps;
	}

	private List<ProcessProperty> processPropertyList;

	private ProcessProperty processProperty;

	public ProcessProperty getProcessProperty() {
		return this.processProperty;
	}

	public void setProcessProperty(ProcessProperty processProperty) {
		this.processProperty = processProperty;
	}

	public List<ProcessProperty> getProcessProperties() {
		return this.processPropertyList;
	}

	private void loadProcessProperties(Schritt s) {
		this.processPropertyList = PropertyParser.getPropertiesForStep(s);
		for (ProcessProperty pt : this.processPropertyList) {
			if (!this.containers.contains(pt.getContainer())) {
				this.containers.add(pt.getContainer());
			}
		}
		Collections.sort(this.containers);
	}

	public Schritt getCurrentStep() {
		return this.steps.get(0);
	}

	// TODO validierung nur bei Schritt abgeben, nicht bei normalen speichern
	public void saveProcessProperties() {
		boolean valid = true;
		for (IProperty p : this.processPropertyList) {
			if (!p.isValid()) {
				Helper.setFehlerMeldung("Property " + p.getName() + " not valid");
				valid = false;
			}
		}

		if (valid) {
			List<Prozesseigenschaft> peList = new ArrayList<Prozesseigenschaft>();
			for (ProcessProperty p : this.processPropertyList) {
				p.transfer();
				peList.add(p.getProzesseigenschaft());
			}

			for (Schritt s : this.steps) {
				Prozess process = s.getProzess();
				for (Prozesseigenschaft pe : peList) {
					if (pe.getTitel() != null) {
						boolean match = false;
						for (Prozesseigenschaft processPe : process.getEigenschaftenList()) {
							if (processPe.getTitel() != null) {
								if (pe.getTitel().equals(processPe.getTitel())) {
									processPe.setWert(pe.getWert());
									match = true;
									break;
								}
							}
						}
						if (!match) {
							Prozesseigenschaft p = new Prozesseigenschaft();
							p.setTitel(pe.getTitel());
							p.setWert(pe.getWert());
							p.setContainer(pe.getContainer());
							p.setType(pe.getType());
							process.getEigenschaften().add(p);
						}
					}
				}
				try {
					this.pdao.save(process);
				} catch (DAOException e) {
					logger.error(e);
					Helper.setFehlerMeldung("Properties for process " + process.getTitel() + " could not be saved");
				}
			}

		}

	}

	private void saveWithoutValidation() {
		List<Prozesseigenschaft> peList = new ArrayList<Prozesseigenschaft>();
		for (ProcessProperty p : this.processPropertyList) {
			p.transfer();
			peList.add(p.getProzesseigenschaft());
		}

		for (Schritt s : this.steps) {
			Prozess process = s.getProzess();
			for (Prozesseigenschaft pe : peList) {
				boolean match = false;
				for (Prozesseigenschaft processPe : process.getEigenschaftenList()) {
					if (pe.getTitel().equals(processPe.getTitel())) {
						processPe.setWert(pe.getWert());
						match = true;
						break;
					}
				}
				if (!match) {
					Prozesseigenschaft p = new Prozesseigenschaft();
					p.setTitel(pe.getTitel());
					p.setWert(pe.getWert());
					p.setContainer(pe.getContainer());
					p.setType(pe.getType());
					process.getEigenschaften().add(p);
				}

			}
			try {
				this.pdao.save(process);
			} catch (DAOException e) {
				logger.error(e);
				Helper.setFehlerMeldung("Properties for process " + process.getTitel() + " could not be saved");
			}
		}

	}

	private List<Integer> containers = new ArrayList<Integer>();

	public String duplicateContainer() {
		Integer currentContainer = this.processProperty.getContainer();
		List<ProcessProperty> plist = new ArrayList<ProcessProperty>();
		// search for all properties in container
		for (ProcessProperty pt : this.processPropertyList) {
			if (pt.getContainer() == currentContainer) {
				plist.add(pt);
			}
		}

		// find new unused container number
		boolean search = true;
		int newContainerNumber = 1;
		while (search) {
			if (!this.containers.contains(newContainerNumber)) {
				search = false;
			} else {
				newContainerNumber++;
			}
		}
		// clone properties
		for (ProcessProperty pt : plist) {
			ProcessProperty newProp = pt.getClone(newContainerNumber);
			this.processPropertyList.add(newProp);
			saveWithoutValidation();
		}

		return "";
	}

	public List<Integer> getContainers() {
		return this.containers;
	}

	public List<ProcessProperty> getSortedProperties() {
		Comparator<ProcessProperty> comp = new ProcessProperty.CompareProperties();
		Collections.sort(this.processPropertyList, comp);
		return this.processPropertyList;
	}

	// kein delete im batch möglich, da keine eindeutige Zuordnung möglich ist, wenn mehr als eine eigenschaft mit dem selben Namen vorhanden ist

	// public void deleteProperty() {
	// this.processPropertyList.remove(this.processProperty);

	// if (this.processProperty.getProzesseigenschaft().getId() != null) {
	// this.mySchritt.getProzess().removeProperty(this.processProperty.getProzesseigenschaft());
	// }
	// saveWithoutValidation();
	// loadProcessProperties(steps.get(0));
	// }
	//
	// public void duplicateProperty() {
	// ProcessProperty pt = this.processProperty.getClone(0);
	// this.processPropertyList.add(pt);
	// saveWithoutValidation();
	// }
}
