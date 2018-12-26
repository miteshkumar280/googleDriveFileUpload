package com.example.driveAPI.drivefile;

import java.io.Serializable;

public class FileResponse implements Serializable{
	
	private String name;
	private String id;
	private String thumbnailLink;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getThumbnailLink() {
		return thumbnailLink;
	}
	public void setThumbnailLink(String thumbnailLink) {
		this.thumbnailLink = thumbnailLink;
	}
	
	

}
