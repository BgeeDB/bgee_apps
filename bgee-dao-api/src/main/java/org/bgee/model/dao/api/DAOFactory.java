package org.bgee.model.dao.api;

import java.util.Map;

/**
 * Abstract factory of DAOs, following the abstract factory pattern. 
 * This abstract class list all the methods that must be implemented by concrete factories 
 * extending this class, to obtain DAOs. It also provides the <code>getDAOFactory</code> 
 * methods, which allow to obtain a concrete factory extending this class. 
 * The client calling this method will not be aware of which concrete 
 * implementation it obtained, so that the client code is not dependent of 
 * any concrete implementation. 
 * <p>
 * This class supports the standard <a href=
 * 'http://docs.oracle.com/javase/6/docs/technotes/guides/jar/jar.html#Service%20Provider'> 
 * Service Provider</a> mechanism. Concrete implementations must include the file 
 * <code>META-INF/services/org.bgee.model.dao.api.DAOFactory</code>. The file must contain 
 * the name of the implementation of <code>org.bgee.model.dao.api.DAOFactory</code>. 
 * For example, to load the <code>my.sql.Factory</code> class, 
 * the <code>META-INF/services/org.bgee.model.dao.api.DAOFactory</code> file 
 * would contain the entry:
 * <pre>my.sql.Factory</pre>
 * <p>
 * When calling {@link #getDAOFactory(Map)}, the <code>DAOFactory</code> will locate 
 * all available implementations of the <code>Service</code>, and will return the first one 
 * accepting the parameters provided by the client (see {@link #acceptParameters(Map)}). 
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 01
 */
public abstract class DAOFactory 
{
	private DAOFactory() {
		
	}
	
	public static DAOFactory getDAOFactory(Map<String, String> parameters)
	{
		
	}
	
}
