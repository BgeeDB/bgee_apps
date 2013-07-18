package org.bgee.model.anatdev;

/**
 * An Uberon developmental stage. 
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 01
 */
public class Stage extends AnatDevEntity {
	/**
     * Constructor providing the <code>id</code> of this <code>Stage</code>. 
     * This <code>id</code> cannot be <code>null</code>, or empty (""), 
     * or whitespaces only, otherwise an <code>IllegalArgumentException</code> 
     * will be thrown. The ID will also be immutable, see {@link #getId()}.
     * 
     * @param id	A <code>String</code> representing the ID of 
     * 				this <code>Stage</code>.
     * @throws IllegalArgumentException 	if <code>id</code> is <code>null</code>,  
     * 										empty, or whitespaces only. 
     */
    public Stage(String id) {
    	super(id);
    }
}
