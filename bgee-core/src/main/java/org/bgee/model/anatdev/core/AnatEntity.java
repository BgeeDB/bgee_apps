package org.bgee.model.anatdev.core;

import org.bgee.model.anatdev.AnatElement;

/**
 * An Uberon anatomical entity. 
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 01
 */
public class AnatEntity extends AnatDevEntity implements AnatElement {
	/**
     * Constructor providing the {@code id} of this {@code AnatEntity}. 
     * This {@code id} cannot be {@code null}, or empty (""), 
     * or whitespaces only, otherwise an {@code IllegalArgumentException} 
     * will be thrown. The ID will also be immutable, see {@link #getId()}.
     * <p>
     * Default constructor protected, instances should be obtained using a factory.
     * 
     * @param id	A {@code String} representing the ID of 
     * 				this {@code AnatEntity}.
     * @throws IllegalArgumentException 	if {@code id} is {@code null},  
     * 										empty, or whitespaces only. 
     */
    protected AnatEntity(String id) {
    	super(id);
    }
}
