package org.bgee.model.dao.api;

/**
 * A {@code TransferObject} used to communicate between the DAO layer and 
 * the business/model layer. 
 * <p>
 * {@code TransferObject}s should be immutable. For this reason, all their setters 
 * are {@code package-private}, as they are always in the same package as 
 * the {@code DAO} allowing to produce them. This is simpler as compared to 
 * using a {@code Builder} pattern. It is the responsibility of the {@code DAO} 
 * producing a {@code TransferObject} to use its setters, so that it ends up 
 * in a proper state before being returned to the client.
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 01
 */
public abstract class TransferObject 
{
    /**
     * Default constructor.
     */
    public TransferObject() {
        
    }
}
