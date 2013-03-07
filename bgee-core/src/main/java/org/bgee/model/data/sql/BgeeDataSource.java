package org.bgee.model.data.sql;

import java.util.HashMap;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.BgeeProperties;

/**
 * Abstraction layer to obtain {@link BgeeConnection}s. 
 * <p>
 * This class first tries to load a <code>DataSource</code> 
 * from an <code>InitialContext</code> (most likely provided by Tomcat 
 * when this application is used in a webapp context), to obtain <code>Connection</code>s from. 
 * If the <code>DataSource</code> cannot be obtained, it means that the application 
 * is most likely used in a standalone context, and a classic <code>DriverManager</code> 
 * approach is used to obtain <code>Connection</code>s. 
 * In the standalone context, the parameters to establish a connection are retrieved from 
 * the {@link org.bgee.model.BgeeProperties BgeeProperties}.
 * <p>
 * Following the first call <b>inside a thread</b> to a method to obtain a connection 
 * with a given <code>username</code> and a given <code>password</code>
 * (meaning, using {@link #getConnection()} or {@link #getConnection(String, String)}), 
 * this class obtains a <code>Connection</code> (either from a <code>DataSource</code> 
 * or a <code>Driver</code>, depending on the context), 
 * and return it. These <code>Connection</code>s are stored using a per-thread singleton pattern, 
 * so that any consecutive call to obtain a <code>Connection</code> 
 * with the same <code>username</code> and <code>password</code>, inside the same thread, 
 * will return the same <code>Connection</code> object, 
 * without trying to obtain a new <code>Connection</code>.
 *  
 * @author Frederic Bastian
 * @version Bgee 13, Mar 2013
 * @since Bgee 13
 */
public class BgeeDataSource 
{
	//****************************
	// CLASS ATTRIBUTES
	//****************************
	/**
	 * A <code>ThreadLocal</code> used for this class to be a "per-thread singleton" 
	 * (yes, we know this term is incorrect).
	 * Each thread will have its own instance of this class, and only one.
	 */
	private static final ThreadLocal<BgeeDataSource> bgeeDataSource =
		new ThreadLocal<BgeeDataSource>() {
		    @Override 
		    protected BgeeDataSource initialValue() {
			    return new BgeeDataSource();
		    }
	    };
	/**
	 * The real <code>DataSource</code> that this class wraps. 
	 * <code>null</code> if no <code>DataSource</code> could be obtained 
	 * from <code>InitialContext</code> (see static initializer).
	 */
	private static final DataSource realDataSource;
	/**
	 * A <code>boolean</code> that is <code>true</code> if an error occurred 
	 * while trying to register a <code>Driver</code> during the static initialization. 
	 * It is used because no <code>SQLException</code> can be thrown 
	 * from the static initializer. This <code>boolean</code> will be thus checked 
	 * when trying to obtain a <code>Connection</code> from the <code>DriverManager</code>, 
	 * and a <code>SQLException</code> thrown if this <code>boolean</code> 
	 * is <code>true</code>.
	 */
	private static final boolean driverRegistrationError;
	/**
	 * <code>Logger</code> of the class. 
	 */
	private final static Logger log = LogManager.getLogger(BgeeDataSource.class.getName());
	
	//****************************
	// INSTANCE ATTRIBUTES
	//****************************
	private Map<String, BgeeConnection> openConnections;
	

	//****************************
	// CLASS METHODS
	//****************************
	/**
	 * Static initializer, initialize {@link realDataSource}, 
	 * or try to register a <code>Driver</code>.
	 */
	static {
		log.entry();
		log.info("Initializing BgeeDataSource...");
		DataSource dataSourceTemp = null;
		boolean driverRegErrTemp = false; 
		try {
			Context ctx = new InitialContext();
			dataSourceTemp = (DataSource) ctx.lookup("java:comp/env/jdbc/bgeedatasource");
			//here it is likely that we are in a webapp context
			log.info("DataSource obtained from InitialContext using JNDI");
		} catch (NamingException e) {
			//here it is likely that we are in a standalone context, 
			//then we don't need a DataSource and will rely on a classic DriverManager
			//if the name of a driver was provide, try to load it, 
			//otherwise, only the URL to connect will be used
			if (BgeeProperties.getJdbcDriver() != null) {
				log.info("No DataSource obtained from InitialContext using JNDI, register Driver {}", 
						BgeeProperties.getJdbcDriver());
				//we register the Driver once 
				try {
					Class.forName(BgeeProperties.getJdbcDriver());
				} catch (ClassNotFoundException e1) {
					log.error("Could not load Driver", e1);
					driverRegErrTemp = true;
				}
			} else {
				log.info("Will rely on URL to identify proper Driver: {}", 
						BgeeProperties.getJdbcUrl());
			}
		}
		realDataSource = dataSourceTemp;
		driverRegistrationError = driverRegErrTemp;
		log.info("BgeeDataSource initialization done.");
		log.exit();
	}
	
	/**
	 * Return a <code>BgeeDataSource</code> object. At the first call of this method 
	 * inside a given thread, a new <code>BgeeDataSource</code> will be instantiated 
	 * and returned. Then all subsequent calls to this method inside the same thread 
	 * will return the same <code>BgeeDataSource</code> object 
	 * (use of a <code>ThreadLocal</code>). 
	 * <p>
	 * This is to ensure that each thread uses one and only one 
	 * <code>BgeeDataSource</code> instance, 
	 * independent from other threads ("per-thread singleton").
	 *  
	 * @return	A <code>BgeeDataSource</code> object, instantiated at the first call 
	 * 			of this method. Subsequent call will return the same object.
	 */
	public static BgeeDataSource getBgeeDataSource()
	{
		log.entry();
		return log.exit(bgeeDataSource.get());
	}
	
	//****************************
	// INSTANCE METHODS
	//****************************
	/**
	 * Private constructor. Instances of this class can only be obtained 
	 * through the <code>static</code> method {@link getBgeeDataSource()}. 
	 * This is to ensure that a thread will use its own and only one instance of this class.
	 */
	private BgeeDataSource()
	{
		log.entry();
		this.setOpenConnections(new HashMap<String, BgeeConnection>());
		log.exit();
	}
	
	/**
	 * Close all <code>BgeeConnection</code>s that this <code>BgeeDataSource</code> holds, 
	 * 
	 * @return
	 */
	public int closeConnections()
	{
		
	}
	
	/**
	 * @param openConnections A <code>Map<String,BgeeConnection></code> to set {@link #openConnections} 
	 */
	private void setOpenConnections(Map<String, BgeeConnection> openConnections) {
		this.openConnections = openConnections;
	}
	/**
	 * Store <code>connection</code> in the <code>Map</code> {@link #openConnections}, 
	 * associated to the key provided by a call on <code>connection</code>  
	 * to the method <code>getId()</code>.
	 * 
	 * @param connection 	A <code>BgeeConnection</code> that is opened, to be stored. 
	 */
	private void storeOpenConnection(BgeeConnection connection)
	{
		this.openConnections.put(connection.getId(), connection);
	}
}
