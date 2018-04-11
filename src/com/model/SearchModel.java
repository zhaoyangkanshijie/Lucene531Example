package com.model;

import java.util.Date;

public class SearchModel {

	private int id;
	private String name;
	private String model;
	private String detail;
	private int classid;
	private int hits;
	private Date updatetime;
	private String other;
	private boolean visible;
	private String classname;
	private int displayorder;
	
	public int getDisplayorder() {
		return displayorder;
	}
	
	public void setDisplayorder(int displayorder) {
		this.displayorder = displayorder;
	}

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	public SearchModel() {
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public String getDetail() {
		return detail;
	}

	public void setDetail(String detail) {
		this.detail = detail;
	}

	public int getClassid() {
		return classid;
	}

	public void setClassid(int classid) {
		this.classid = classid;
	}

	public int getHits() {
		return hits;
	}

	public void setHits(int hits) {
		this.hits = hits;
	}

	public Date getUpdatetime() {
		return updatetime;
	}

	public void setUpdatetime(Date updatetime) {
		this.updatetime = updatetime;
	}

	public String getOther() {
		return other;
	}

	public void setOther(String other) {
		this.other = other;
	}
	
	public String getClassName() {
		return classname;
	}

	public void setClassName(String classname) {
		this.classname = classname;
	}

}
