package de.sub.goobi.Beans;

import java.io.Serializable;


public class ProjectFileGroup implements Serializable {
	private static final long serialVersionUID = -5506252462891480484L;
	private Integer id;
	private String name;
	private String path;
	private String mimetype;
	private String suffix;

	private Projekt project;

	/*#####################################################
	 #####################################################
	 ##																															 
	 ##				Getter und Setter									
	 ##                                                   															    
	 #####################################################
	 ####################################################*/

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Projekt getProject() {
		return project;
	}

	public void setProject(Projekt project) {
		this.project = project;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getMimetype() {
		return mimetype;
	}

	public void setMimetype(String mimetype) {
		this.mimetype = mimetype;
	}

	public String getSuffix() {
		return suffix;
	}

	public void setSuffix(String suffix) {
		this.suffix = suffix;
	}

}
