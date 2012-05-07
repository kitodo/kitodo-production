package de.sub.goobi.helper;

public class Batch {

	private String batchId;
	private String batchLabel;
	
	
	public Batch(Integer id, String label) {
		this.batchId = String.valueOf(id);
		this.batchLabel = label;
	}
	
	
	public String getBatchId() {
		return this.batchId;
	}
	public void setBatchId(String batchId) {
		this.batchId = batchId;
	}
	public String getBatchLabel() {
		return this.batchLabel;
	}
	public void setBatchLabel(String batchLabel) {
		this.batchLabel = batchLabel;
	}
	
	
	
	
	
	
	
	
	
}
