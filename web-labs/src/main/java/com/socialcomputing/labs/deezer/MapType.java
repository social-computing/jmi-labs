/**
 * 
 */
package com.socialcomputing.labs.deezer;

/**
 * @author "Jonathan Dray <jonathan@social-computing.com>"
 *
 */
public enum MapType {

	ALBUM("album"),
	RELARTIST("relArtist"),
	ARTIST("artist");
	
	public final String type;
	
	private MapType(String type) {
		this.type = type;
	}
	
    @Override
    public String toString() {
         return this.type;
    }
	
	public static MapType fromValue(String value) {
		for(MapType mapType : MapType.values()){
			if(mapType.type.equalsIgnoreCase(value)) {
				return mapType;
			}
		}
		throw new InvalidEnumerationElement(MapType.class, value);
	}
}
