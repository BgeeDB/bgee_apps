package org.bgee.model.data.sql;

/**
 * Abstraction layer to obtain {@link BgeeConnection}s. 
 * <p>
 * Following the first call <b>inside a thread</b> to a method to obtain a connection 
 * with a given <code>username</code> and a given <code>password</code>
 * (meaning, using {@link #getConnection()} or {@link #getConnection(String, String)}), 
 * this class obtains a <code>Connection</code> from a <code>DataSource</code> 
 * and return it. These <code>Connection</code>s are stored using a per-thread singleton pattern, 
 * so that any consecutive call to obtain a <code>Connection</code> 
 * with the same <code>username</code> and <code>password</code>, inside the same thread, 
 * will return the same <code>Connection</code> object, 
 * without reusing the <code>DataSource</code>.
 *  
 * @author Frederic Bastian
 * @version Bgee 13, Mar 2013
 * @since Bgee 13
 */
public class BgeeDataSource 
{

}
