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
}
