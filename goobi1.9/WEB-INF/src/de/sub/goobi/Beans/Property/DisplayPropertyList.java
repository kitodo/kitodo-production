package de.sub.goobi.Beans.Property;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.log4j.Logger;
import org.goobi.production.api.property.xmlbasedprovider.Status;
import org.goobi.production.api.property.xmlbasedprovider.impl.PropertyTemplate;
import org.goobi.production.api.property.xmlbasedprovider.impl.XMLBasedPropertyTemplateProvider;
import org.jdom.JDOMException;

import de.sub.goobi.Persistence.SimpleDAO;
import de.sub.goobi.config.ConfigMain;
import de.sub.goobi.helper.enums.PropertyType;
import de.sub.goobi.helper.exceptions.DAOException;

public class DisplayPropertyList {
	private static final Logger logger = Logger.getLogger(DisplayPropertyList.class);
	private IGoobiEntity owningEntity;
	List<PropertyTemplate> myPropTemplateCollection;
	private PropertyTemplate currentPropertyTemplate;
	SimpleDAO dao = new SimpleDAO();
	private List<Integer> containers = new ArrayList<Integer>();

	/**
	 * constructor
	 * 
	 * @param inEntity
	 */

	public DisplayPropertyList(IGoobiEntity inEntity) {
		owningEntity = inEntity;
	}

	/**
	 * 
	 * @return list of {@link PropertyTemplate}s
	 */
	public List<PropertyTemplate> getPropertyTemplatesAsList() {
		if (owningEntity.getId() != null) {
			if (myPropTemplateCollection == null || myPropTemplateCollection.size() == 0) {
				myPropTemplateCollection = buildTemplatesCollections();
			}
		}
		return myPropTemplateCollection;
	}

	/**
	 * 
	 * @return size of list of {@link PropertyTemplate}s
	 */
	public int getPropertySize() {
		return getPropertyTemplatesAsList().size();
	}

	/**
	 * method matches properties with property templates, and passes the property into the property template, generates new properties if they don't
	 * exist to match existing templates, generates new templates, where template don't exist yet
	 */
	private List<PropertyTemplate> buildTemplatesCollections() {
		// getting the templates from XML
		List<PropertyTemplate> defaultTemplates = getDefaultProperties(owningEntity);
		// getting the properties from the Entity
		List<IGoobiProperty> realProps =  owningEntity.getProperties();
		List<PropertyTemplate> returnList = new ArrayList<PropertyTemplate>();

		// iterating through the templates list and properties and finding matches
		for (PropertyTemplate pt : defaultTemplates) {

			List<IGoobiProperty> properties = new ArrayList<IGoobiProperty>();
			properties.addAll(realProps);
			boolean match = false;
			for (IGoobiProperty prop : properties) {
				PropertyTemplate x = clonePropertyTemplate(pt);
				try {
					match = x.setProperty(prop);
					if (match) {
						pt.setProperty(null);
						if (pt.getIsUsed()) {
							returnList.add(x);
						} else {
							pt.setIsUsed(true);
							returnList.add(x);
						}
						realProps.remove(prop);

					}
					// exception is thrown, if it is a no match
				} catch (Exception e) {
					// do nothing
				}
			}
		}

		for (PropertyTemplate pt : defaultTemplates) {
			if (!pt.getIsUsed()) {
				PropertyTemplate x = clonePropertyTemplate(pt);
				returnList.add(x);
			}
		}

		// now iterating through the remaining properties,
		// which have no predefined templates
		if (realProps.size() > 0) {
			List<IGoobiProperty> myProps = new ArrayList<IGoobiProperty>();
			myProps.addAll(realProps);
			for (IGoobiProperty prop : myProps) {
				// create new PropertyTemplate using the constructor with property
				// parameter
				PropertyTemplate pt = new PropertyTemplate(prop);
				returnList.add(pt);
				realProps.remove(prop);
			}
		}
		if (realProps.size() > 0) {
			logger.error("an Error occurred building property list");
		}

		for (PropertyTemplate pt : returnList) {
			if (!containers.contains(pt.getContainer())) {
				containers.add(pt.getContainer());
			}
		}
		Collections.sort(containers);
		return returnList;
	}

	private PropertyTemplate clonePropertyTemplate(PropertyTemplate x) {
		PropertyTemplate pt = new PropertyTemplate(x.getOwningEntity(), x.getName());
		pt.setAuswahl(x.getAuswahl());
		pt.setCreationDate(x.getCreationDate());
		pt.setDate(x.getDate());
		pt.setRequired(x.isRequired());
		pt.setProperty(x.getProperty());
		pt.setSelectedValue(x.getSelectedValue());
		pt.setSelectedValuesList(x.getSelectedValuesList());
		pt.setTitel(x.getTitel());
		pt.setType(x.getType());
		pt.setValue(x.getValue());
		pt.setValuesList(x.getValuesList());
		pt.setWert(x.getWert());
		pt.setContainer(x.getContainer());
		pt.setOwningEntity(x.getOwningEntity());
		return pt;
	}

	/**
	 * creates list of default properties for given {@link IGoobiEntity}
	 * 
	 * @param inEntity
	 * @return
	 */

	public List<PropertyTemplate> getDefaultProperties(IGoobiEntity inEntity) {
		List<PropertyTemplate> defProps = new ArrayList<PropertyTemplate>();
		try {
			XMLBasedPropertyTemplateProvider.getInstance(inEntity).setFilepath(ConfigMain.getParameter("KonfigurationVerzeichnis"));

			Status status = inEntity.getStatus();

			defProps = XMLBasedPropertyTemplateProvider.getInstance(inEntity).getTemplates(status, inEntity);

		} catch (IOException e) {
			logger.error("templateProviderOfflineFileNotFound", e);
		} catch (JDOMException e) {
			logger.error(e);
		}
		return defProps;
	}

	/**
	 * used by GUI to display a specific property
	 * 
	 * @return
	 */
	public PropertyTemplate getCurrentProperty() {
		return currentPropertyTemplate;
	}

	/**
	 * used by GUI to display a specific property
	 * 
	 * @return
	 */
	public void setCurrentProperty(PropertyTemplate inCurrentProperty) {
		this.currentPropertyTemplate = inCurrentProperty;
	}

	/**
	 * removing {@link IGoobiProperty} from set in entity
	 * 
	 * @return
	 */
	public String deleteProperty() {
		// removing property from Set in entity
		currentPropertyTemplate.getOwningEntity().removeProperty(currentPropertyTemplate.getProperty());
		currentPropertyTemplate.getProperty().setOwningEntity(null);
		currentPropertyTemplate = null;
		owningEntity.refreshProperties();

		try {
			dao.save(owningEntity);
		} catch (DAOException e) {
			logger.error("dao exception: " + e);
		}
		return "";
	}

	/**
	 * creates a new {@link IGoobiProperty}
	 * 
	 * @return
	 */

	public String createNewProperty() {
		currentPropertyTemplate = new PropertyTemplate(owningEntity, "");
		currentPropertyTemplate.setType(PropertyType.String);
		currentPropertyTemplate.setRequired(false);
		return "";
	}

	/**
	 * creates a copy of given property
	 * 
	 * @return
	 */

	public String duplicateProperty() {
		if (currentPropertyTemplate != null) {
			PropertyTemplate pt = currentPropertyTemplate.copy(0);
			try {
				pt.getOwningEntity().addProperty(pt.getProperty());
				dao.save(pt.getProperty());
			} catch (DAOException e) {
				logger.error("dao-exception occured", e);
			}
			owningEntity = currentPropertyTemplate.getOwningEntity();
			myPropTemplateCollection = buildTemplatesCollections();
			owningEntity.refreshProperties();
		}
		return "";
	}

	/**
	 * creates a copy of a container of properties
	 * 
	 * @return
	 */

	public String duplicateContainer() {
		Integer currentContainer = currentPropertyTemplate.getContainer();
		List<PropertyTemplate> plist = new ArrayList<PropertyTemplate>();
		// search for all properties in container
		for (PropertyTemplate pt : getPropertyTemplatesAsList()) {
			if (pt.getContainer() == currentContainer) {
				plist.add(pt);
			}
		}
		// find new unused container number
		boolean search = true;
		int newContainerNumber = 1;
		while (search) {
			if (!containers.contains(newContainerNumber)) {
				search = false;
			} else {
				newContainerNumber++;
			}
		}
		// clone properties
		for (PropertyTemplate pt : plist) {
			PropertyTemplate newProp = pt.copy(newContainerNumber);
			try {
				newProp.getOwningEntity().addProperty(newProp.getProperty());
				dao.save(newProp.getProperty());
			} catch (DAOException e) {
				logger.error("dao-exception occured", e);
			}
		}
		owningEntity = currentPropertyTemplate.getOwningEntity();
		myPropTemplateCollection = buildTemplatesCollections();
		owningEntity.refreshProperties();
		return "";
	}

	/**
	 * @param cointainers
	 *            the cointainers to set
	 */
	public void setContainers(List<Integer> containers) {
		this.containers = containers;
	}

	/**
	 * @return the cointainers
	 */
	public List<Integer> getContainers() {
		getPropertyTemplatesAsList();
		return containers;
	}

	// public List<PropertyTemplate> getPropertiesForContainer() {
	// List<PropertyTemplate> pl = getPropertyTemplatesAsList();
	// List<PropertyTemplate> answer = new ArrayList<PropertyTemplate>();
	// for (PropertyTemplate pt : pl) {
	// if (pt.getContainer() == currentContainer) {
	// answer.add(pt);
	// }
	// }
	// return answer;
	// }

	public List<PropertyTemplate> getSortedProperties() {
		List<PropertyTemplate> answer = getPropertyTemplatesAsList();
		Comparator<PropertyTemplate> comp = new CompareProps();
		Collections.sort(answer, comp);
		return answer;
	}

	private static class CompareProps implements Comparator<PropertyTemplate> {

		public int compare(PropertyTemplate o1, PropertyTemplate o2) {

			return new Integer(o1.getContainer()).compareTo(new Integer(o2.getContainer()));
		}

	}

}
