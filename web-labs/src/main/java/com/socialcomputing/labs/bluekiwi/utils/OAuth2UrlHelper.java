/**
 * 
 */
package com.socialcomputing.labs.bluekiwi.utils;

import com.socialcomputing.wps.server.planDictionnary.connectors.utils.UrlHelper;

/**
 * @author "Jonathan Dray <jonathan@social-computing.com>"
 *
 */
public class OAuth2UrlHelper extends UrlHelper {

	private final String superToken;
	
	public OAuth2UrlHelper(String url, String superToken) {
		super(url);
		this.superToken = superToken;
	}
	

	public OAuth2UrlHelper(String url, Type type, String superToken) {
		super(type, url);
		this.superToken = superToken;
	}
	
}
