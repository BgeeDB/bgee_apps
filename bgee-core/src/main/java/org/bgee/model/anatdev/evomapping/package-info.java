/**
 * This package provides the classes allowing to map between them anatomical and 
 * developmental stage entities, according to various evolutionary relations. 
 * So it means that the classes in this package allow to map the core 
 * {@link org.bgee.model.anatdev.AnatDevEntity AnatDevEntity}s from 
 * the package {@link org.bgee.model.anatdev.core}. As of Bgee 13, they can only 
 * be mapped using transitive evolutionary relations, described by the class 
 * {@link AnatDevMapping.TransRelationType}.
 * <p>
 * The base class to store mappings is {@link AntDevMapping}. It is then 
 * extended to represent mappings of {@link org.bgee.model.anatdev.core.AnatEntity 
 * AnatEntity}s (see {@link AnatMapping}), or of 
 * {@link org.bgee.model.anatdev.core.DevStage DevStage}s (see {@link DevMapping}).
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
package org.bgee.model.anatdev.evomapping;