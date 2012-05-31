/**
 * 
 */
package com.socialcomputing.labs.deezer.client;


/**
 * @author "Jonathan Dray <jonathan@social-computing.com>"
 *
 */
public class Album {
	public final String id;
	public final String title;
	public final String link;
	public final String cover;
	
	public Album(String id, String title, String link, String cover) {
		this.id = id;
		this.title = title;
		this.link = link;
		this.cover = cover;
	}
}
