package org.goobi.production.properties;

import java.util.List;

public interface IProperty {

	public abstract String getName();

	public abstract void setName(String name);

	public abstract int getContainer();

	public abstract void setContainer(int container);

	public abstract String getValidation();

	public abstract void setValidation(String validation);

	public abstract Type getType();

	public abstract void setType(Type type);

	public abstract String getValue();

	public abstract void setValue(String value);

	public abstract List<String> getPossibleValues();

	public abstract void setPossibleValues(List<String> possibleValues);

	public abstract List<String> getProjects();

	public abstract void setProjects(List<String> projects);

	public abstract List<ShowStepCondition> getShowStepConditions();

	public abstract void setShowStepConditions(List<ShowStepCondition> showStepConditions);

	public abstract AccessCondition getShowProcessGroupAccessCondition();

	public abstract void setShowProcessGroupAccessCondition(AccessCondition showProcessGroupAccessCondition);

	public abstract boolean isValid();

//	public abstract void save(Schritt step);

//	public abstract Prozesseigenschaft getProzesseigenschaft();

//	public abstract void setProzesseigenschaft(Prozesseigenschaft prozesseigenschaft);

	public abstract IProperty getClone();

	public abstract void transfer();

}