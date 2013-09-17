package org.bgee.model.anatdev.core;

import org.bgee.model.anatdev.DevElement;

/**
 * An Uberon developmental stage. 
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 01
 */
public class DevStage extends AnatDevEntity implements DevElement {
	/**
     * Constructor providing the <code>id</code> of this <code>DevStage</code>. 
     * This <code>id</code> cannot be <code>null</code>, or empty (""), 
     * or whitespaces only, otherwise an <code>IllegalArgumentException</code> 
     * will be thrown. The ID will also be immutable, see {@link #getId()}.
     * <p>
     * Default constructor protected, instances should be obtained using a factory.
     * 
     * @param id	A <code>String</code> representing the ID of 
     * 				this <code>DevStage</code>.
     * @throws IllegalArgumentException 	if <code>id</code> is <code>null</code>,  
     * 										empty, or whitespaces only. 
     */
    protected DevStage(String id) {
    	super(id);
    }
}
