package org.bgee.model;

/**
 * Class defining <code>Entity</code>s that can hold expression data. 
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 01
 */
public class EntityExpressionData extends Entity {
	/**
     * Constructor providing the <code>id</code> of this <code>EntityExpressionData</code>. 
     * This <code>id</code> cannot be <code>null</code>, or empty (""), 
     * or whitespaces only, otherwise an <code>IllegalArgumentException</code> 
     * will be thrown. The ID will also be immutable, see {@link #getId()}.
     * 
     * @param id	A <code>String</code> representing the ID of 
     * 				this <code>EntityExpressionData</code>.
     * @throws IllegalArgumentException 	if <code>id</code> is <code>null</code>,  
     * 										empty, or whitespaces only. 
     */
    public EntityExpressionData(String id) {
    	super(id);
    }
}
