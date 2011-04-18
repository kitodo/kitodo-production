package org.goobi.production.enums;

public enum StepReturnValue {

	Finished(0, "Step finished"),
	InvalidData(1, "Invalid data"),
	NoData(2, "No data found"),
	DataAllreadyExists(3, "Data already exists"),
	WriteError(4, "Data could not be written")
	;

	private int id;
	private String value;

	private StepReturnValue(int id, String title) {
		this.setId(id);
		this.setValue(title);
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public void setValue(String title) {
		this.value = title;
	}

	public String getValue() {
		return value;
	}

	public static StepReturnValue getByValue(String title) {
		for (StepReturnValue t : StepReturnValue.values()) {
			if (t.getValue().equals(title)) {
				return t;
			}
		}
		return null;
	}

	public static StepReturnValue getById(int id) {
		for (StepReturnValue t : StepReturnValue.values()) {
			if (t.getId() == id) {
				return t;
			}
		}
		return null;
	}
}
