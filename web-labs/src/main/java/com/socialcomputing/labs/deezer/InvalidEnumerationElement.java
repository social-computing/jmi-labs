/**
 * 
 */
package com.socialcomputing.labs.deezer;

/**
 * @author "Jonathan Dray <jonathan@social-computing.com>"
 *
 */
public class InvalidEnumerationElement extends RuntimeException {

    /**
     * Auto-generated description stub for serialVersionUID
     */
    private static final long serialVersionUID = 6483340065774786386L;

    /**
     * @param message
     * @param cause
     */
    public InvalidEnumerationElement(Class <?> clazz, String elementName) {
        super("Element with name " + elementName + " does not exist for enumeration " + clazz.toString());
    }
}