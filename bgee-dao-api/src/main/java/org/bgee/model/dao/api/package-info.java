/**
 * This package contains the interfaces that DAOs must implement, 
 * and the <code>TransfertObject</code>s used to encapsulate the information 
 * present in the data sources, returned by the DAOs. 
 * This package is thus not dedicated to a specific data source, but is used 
 * to build the packages using a data source (for instance, 
 * the {@link org.bgee.model.dao.sql.mysql} package). 
 * <p>
 * The structure of the sub-packages of this package follows the same structure 
 * than the sub-packages of {@link org.bgee.model}. Each "entity" present 
 * in the <code>org.bgee.model</code> package have a corresponding <code>TransfertObject</code> 
 * and a corresponding DAO. 
 * For instance, for the {@link org.bgee.model.gene.GeneFactory GeneFactory} 
 * to provide {@link org.bgee.model.gene.Gene Gene}s, it calls a DAO implementing 
 * the {@link org.bgee.model.dao.api.gene.GeneDAO GeneDAO} interface, 
 * that returns {@link org.bgee.model.dao.api.gene.GeneTO GeneTO}s.
 * 
 * @author Frederic Bastian
 * @version Bgee 13, Mar 2013
 * @since Bgee 01
 */
package org.bgee.model.dao.api;