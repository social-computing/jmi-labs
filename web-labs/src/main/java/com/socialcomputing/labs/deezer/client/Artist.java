/**
 * 
 */
package com.socialcomputing.labs.deezer.client;

/**
 * @author "Jonathan Dray <jonathan@social-computing.com>"
 *
 */
public class Artist {
	public final String id;
	public final String name;
	public final String link;
	public final String picture;
	
	public Artist(String id, String name, String link, String picture) {
		this.id = id;
		this.name = name;
		this.link = link;
		this.picture = picture;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{\"id\": ").append(this.id).append(", ")
		  .append("\"name\": ").append(this.name).append(", ")
		  .append("\"link\": ").append(this.link).append(", ")
		  .append("\"picture\": ").append(this.picture).append("}");
		return sb.toString();
	}
}
