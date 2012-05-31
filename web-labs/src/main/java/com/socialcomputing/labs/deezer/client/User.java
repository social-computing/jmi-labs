/**
 * 
 */
package com.socialcomputing.labs.deezer.client;

/**
 * @author "Jonathan Dray <jonathan@social-computing.com>"
 *
 */
public class User {

	public final String id;
	public final String name;
	public final String link;
	public final String picture;
	public final String country;
	
	public User(String id, String title, String link, String picture, String country) {
		this.id = id;
		this.name = title;
		this.link = link;
		this.picture = picture;
		this.country = country;
	}
	
	public User(String id, String title, String link, String picture) {
		this.id = id;
		this.name = title;
		this.link = link;
		this.picture = picture;
		this.country = null;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{")
		  .append("\"id\": \"").append(id).append("\"")
		  .append("\"name\": \"").append(name).append("\"")
		  .append("\"link\": \"").append(link).append("\"")
		  .append("\"picture\": \"").append(picture).append("\"");
		  
		if(country != null ) {
			sb.append("\"country\": \"").append(country).append("\"");
		}
		  
		sb.append("}");
		return sb.toString();
	}
}
