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
     * Constructor providing the {@code id} of this {@code DevStage}. 
     * This {@code id} cannot be {@code null}, or empty (""), 
     * or whitespaces only, otherwise an {@code IllegalArgumentException} 
     * will be thrown. The ID will also be immutable, see {@link #getId()}.
     * <p>
     * Default constructor protected, instances should be obtained using a factory.
     * 
     * @param id	A {@code String} representing the ID of 
     * 				this {@code DevStage}.
     * @throws IllegalArgumentException 	if {@code id} is {@code null},  
     * 										empty, or whitespaces only. 
     */
    protected DevStage(String id) {
    	super(id);
    }
}
