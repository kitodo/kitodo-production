package org.goobi.production.flow.helper;

import de.sub.goobi.Beans.Schritt;
import de.sub.goobi.helper.enums.StepStatus;

public class BatchDisplayItem implements Comparable<BatchDisplayItem>{

	private String stepTitle = "";
	private Integer stepOrder = null;
	private StepStatus stepStatus = StepStatus.DONE;

	public BatchDisplayItem(Schritt s) {
		this.stepTitle = s.getTitel();
		this.stepOrder = s.getReihenfolge();
		this.stepStatus = s.getBearbeitungsstatusEnum();
	}

	public String getStepTitle() {
		return this.stepTitle;
	}

	public void setStepTitle(String stepTitle) {
		this.stepTitle = stepTitle;
	}

	public Integer getStepOrder() {
		return this.stepOrder;
	}

	public void setStepOrder(Integer stepOrder) {
		this.stepOrder = stepOrder;
	}

	public StepStatus getStepStatus() {
		return this.stepStatus;
	}

	public void setStepStatus(StepStatus stepStatus) {
		this.stepStatus = stepStatus;
	}

	@Override
	public int compareTo(BatchDisplayItem o) {
	
		return this.getStepOrder().compareTo(o.getStepOrder());
	}
}
