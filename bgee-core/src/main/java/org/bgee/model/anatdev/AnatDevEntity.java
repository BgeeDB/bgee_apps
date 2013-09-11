package org.bgee.model.anatdev;

import org.bgee.model.expressiondata.rawdata.AnyRawDataHolder;

/**
 * Parent class of entities used in anatomical or developmental ontologies.
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public abstract class AnatDevEntity extends AnyRawDataHolder {
	/**
     * Constructor providing the <code>id</code> of this <code>AnatDevEntity</code>. 
     * This <code>id</code> cannot be <code>null</code>, or empty (""), 
     * or whitespaces only, otherwise an <code>IllegalArgumentException</code> 
     * will be thrown. The ID will also be immutable, see {@link #getId()}.
     * <p>
     * Default constructor protected, instances should be obtained using a factory.
     * 
     * @param id	A <code>String</code> representing the ID of 
     * 				this <code>AnatDevEntity</code>.
     * @throws IllegalArgumentException 	if <code>id</code> is <code>null</code>,  
     * 										empty, or whitespaces only. 
     */
    protected AnatDevEntity(String id) {
    	super(id);
    }
}
