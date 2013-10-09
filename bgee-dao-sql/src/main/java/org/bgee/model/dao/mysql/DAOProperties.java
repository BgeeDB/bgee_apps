package org.bgee.model.dao.mysql;

import java.net.URLDecoder;
import java.util.Iterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class loads the properties for <code>psp4jdbc</code>.
 * They can be either set using the setters of this class, or using 
 * a <code>String</code> representation of an <code>URL</code>, or as key/value pairs 
 * in a <code>java.util.Properties</code> object.
 * <p>
 * The properties are :
 * <ul>
 * <li><code>pspDriverClassName</code>: the name of the real underlying driver to use 
 * (e.g., <code>com.mysql.jdbc.Driver</code>). It will be used to load the real driver 
 * (using <code>Class.forName(...)</code>). If not provided, you should either use 
 * a <code>DataSource</code>, or load the driver yourself before using psp4jdbc.
 * <li><code>pspPoolMaxSize</code>:
 * An <code>int</code> containing the maximum size 
 * allowed for a single pool. Default is <code>50</code>
 * <li><code>pspGlobalMaxSize</code>:<p> 
 * An <code>int</code> containing the global size 
 * allowed for a all pools
 * in the application. Default is <code>500</code>. Impact all pools 
 * of all connections.
 * <li><code>pspEvictionMethod</code>: the eviction method to use when a pool 
 * has reached its maximum capacity. Possible choices are LRU and LFU. 
 * Default is LRU. 
 * <li><code>pspEvictionFactor</code>: A float defining the proportion of elements 
 * in a pool to be removed when the eviction method is triggered. 
 * Default is 0.1.
 * </ul>
 * <p>
 * This class has been copied from <code>net.sf.log4jdbc.DriverSpy</code> 
 * developed by Arthur Blake.
 * 
 * @author Frederic Bastian
 * @author Mathieu Seppey
 * @version 1, August 2013
 * @since 1
 */
public class DAOProperties 
{
	/**
     * Logger for this class
     */
    private final static Logger log = LogManager.getLogger(DAOProperties.class.getName());
	/**
     * An <code>int</code> defining the maximum size 
     * allowed for a single pool. Default value is {@link DEFAULTPOOLMAXSIZE}.
     * 
     */
    private int poolMaxSize; 
    /**
     * Default {@link #poolMaxSize}.
     */
    public final static int DEFAULTPOOLMAXSIZE = 50;
    /**
	 * A <code>String</code> that is the key to use to retrieve the maximum 
	 * size of a pool.
	 * @see #poolMaxSize
	 */
    public final static String POOLMAXSIZEKEY = "pspPoolMaxSize";
    
    /**
     * Constructor providing a <code>String</code> representing an <code>URL</code> 
     * and a <code>DAOProperties</code> object, which to retrieve configuration from. 
     * <code>URL</code> has precedence over <code>DAOProperties</code>.
     * 
     * @param url 		A <code>String</code> representing an <code>URL</code> 
     * 					to retrieve configuration from.
     * @param props		A <code>DAOProperties</code> object to retrieve configuration from. 
     * 					can be <code>null</code>.
     * @throws IllegalArgumentException 	If <code>url</code> is <code>null</code> 
     * 										or not accepted.
     * @see #DAOProperties()
     */
    public DAOProperties(String url, java.util.Properties props) 
    		throws IllegalArgumentException {
    	this();
    	if (!this.loadProperties(url, props)) {
    		throw new IllegalArgumentException(
    				"This application does not accept this connection URL.");
    	}
    }
    
    /**
     * Constructor providing no way to initialize properties, they should be 
     * initialized using the setters of this class. 
     * 
     * @see #DAOProperties(String, DAOProperties)
     */
    public DAOProperties() {
    	this.setDriverClassName("");
    	this.setEvictionMethod(DEFAULTEVICTIONMETHOD);
    	this.setEvictionFactor(DEFAULTEVICTIONFACTOR);
    	this.setPoolMaxSize(DEFAULTPOOLMAXSIZE);
    }
    
    /**
     * Set attributes of this object using a <code>String</code> representation 
     * of a connection <code>URI</code>, and a <code>java.util.Properties</code> object. 
     * Parameters set via the <code>DAOProperties</code> object have priority over 
     * parameters set via <code>uri</code>.
     * <p>
     * Return <code>false</code> if <code>uri</code> is <code>null</code>, invalid, 
     * or not accepted by this application.
     * 
     * @param uri 		a <code>String</code> representation of a JDBC connection URI.
     * @param props		a <code>java.util.Properties</code> object to provide the parameters. 
     * 					can be <code>null</code>.
     * @return			<code>true</code> if the parameters are accepted by 
     * 					this <code>Driver</code>, <code>false</code> otherwise.
     */
    public boolean loadProperties(String uri, java.util.Properties props) 
    {
    	log.entry(uri, props);
   
   		if (uri == null || !uri.toLowerCase().startsWith(URLPREFIX)) {
   			return log.exit(false);
   		}
    			
   		
		try {
   			log.trace("Retrieving parameters from URI");
			/*
	   		 * {Sadly, the URI class is not able to find a query string properly 
	   		 * if the prefix includes a column (for instance, 
	   		 * jdbc:mysql://localhost:3306/sakila?profileSQL=true is not parsed correctly).
	   		 * We keep this code for the record...}
			URI theUri = new URI(uri);
			log.trace("Built URI is: {}", theUri);
			String query = theUri.getRawQuery();
			 */
			String query = null;
			String[] uriSplit = uri.split("\\?");
			if (uriSplit.length > 1) {
				query = uriSplit[1];
			}
			log.trace("Query string in provided URI is: {}", query);
			if (query != null && query.length() > 0) {
				for (String param: query.split("&")) {
					log.trace("Found key-value pair: {}", param);
					String pair[] = param.split("=");
					String key = URLDecoder.decode(pair[0], "UTF-8");
					if (pair.length > 1) {
						String value = URLDecoder.decode(pair[1], "UTF-8");
						log.trace("Using as key {} and value {}", key, value);
						this.setAttribute(key, value);
					} else {
						log.trace("Could not extract key and value");
					}
				}
			}
		} catch (Exception e) {
			log.catching(e);
			return log.exit(false);
		}
   
   		// DAOProperties passed in should override ones in URL
   		if (props != null) {
   			log.trace("Retrieving parameters from java.util.Properties");
   			Iterator<Object> propsIter = props.keySet().iterator();
   			while (propsIter.hasNext()) {
   				String key = propsIter.next().toString();
   				String property = props.getProperty(key);
   				this.setAttribute(key, property);
   			}
   		}
   
   		return log.exit(true);
   	}
    
    /**
     * Set the attribute of this object corresponding to <code>key</code>, 
     * using the value <code>value</code>. 
     * <code>key</code> should correspond to one of the final static <code>String</code>s 
     * of this class. 
     * 
     * @param key 		A <code>String</code> that is a key mappable to an attribute 
     * 					of this object. 
     * @param value		The <code>String</code> representation of the value to give 
     * 					to the attribute. 
     */
    public void setAttribute(String key, String value) {
    	log.entry(key, value);
    	if (key == null || value == null) {
    		log.exit(); return;
    	}
    	String keyTrimmed = key.trim();
    	String valueTrimmed = value.trim();
    	if (keyTrimmed.length() == 0 || valueTrimmed.length() == 0){
    		log.exit(); return;
    	}
        
        if (DRIVERCLASSNAMEKEY.equalsIgnoreCase(keyTrimmed)) {
        	this.setDriverClassName(valueTrimmed);
        } else if (POOLMAXSIZEKEY.equalsIgnoreCase(keyTrimmed)) {
        	this.setPoolMaxSize(Integer.parseInt(valueTrimmed));
        } else if (GLOBALMAXSIZEKEY.equalsIgnoreCase(keyTrimmed)) {
        	DAOProperties.setGlobalMaxSize(Integer.parseInt(valueTrimmed));
        } else if (EVICTIONMETHODKEY.equalsIgnoreCase(keyTrimmed)) {
        	if ("lfu".equalsIgnoreCase(valueTrimmed)) {
        		this.setEvictionMethod(EvictionMethod.LFU);
        	} else if ("lru".equalsIgnoreCase(valueTrimmed)) {
        		this.setEvictionMethod(EvictionMethod.LRU);
        	} else {
        		this.setEvictionMethod(DEFAULTEVICTIONMETHOD);
        	}
        } else if (EVICTIONFACTORKEY.equalsIgnoreCase(keyTrimmed)) {
        	this.setEvictionFactor(Float.parseFloat(valueTrimmed));
        }
        log.exit();
    }

	/**
	 * @return 	A <code>String</code> representing the underling real 
	 * 			JDBC <code>Driver</code> to use. 
	 */
	public String getDriverClassName() {
		return this.driverClassName;
	}

	/**
	 * @param underlyingDriver 	A <code>String</code> to set the underling real 
	 * 							JDBC <code>Driver</code> to use.
	 */
	public void setDriverClassName(String underlyingDriver) {
		if (underlyingDriver != null) {
			underlyingDriver = underlyingDriver.trim();
		}
		this.driverClassName = underlyingDriver;
	}

	/**
	 * @return 	An <code>int</code> defining the maximum size 
     * 			allowed for a single pool. Default value is 50.
	 */
	public int getPoolMaxSize() {
		return poolMaxSize;
	}

	/**
	 * @param poolMaxSize 	A positive <code>int</code> to set the maximum size 
     * 						allowed for a single pool. Default value is 50.
     * @throws IllegalArgumentException	If <code>poolMaxSize</code> is less than 
     * 									or equal to 0.
	 */
	public void setPoolMaxSize(int poolMaxSize) throws IllegalArgumentException {
		if (poolMaxSize <= 0) {
			throw new IllegalArgumentException(
					"Pool max size can not be less than or equal to 0");
		}
		this.poolMaxSize = poolMaxSize;
	}

	/**
	 * Global size allowed for all pools in the application. Impact all pools 
	 * of all connections. 
	 * 
	 * @return 	An <code>int</code> defining the global size allowed for all pools 
     * 			in the application. Default value is 500.
	 */
	public static int getGlobalMaxSize() {
		return DAOProperties.globalMaxSize;
	}
	/**
	 * Global size allowed for all pools in the application. Impact all pools 
	 * of all connections.
	 * 
	 * @param globalMaxSize		A positive <code>int</code> defining the global size allowed 
	 * 							for all pools in the application. Default value is 500.
     * @throws IllegalArgumentException	If <code>globalMaxSize</code> is less than 
     * 									or equal to 0.
	 */
	public static void setGlobalMaxSize(int globalMaxSize) throws IllegalArgumentException {
		if (globalMaxSize <= 0) {
			throw new IllegalArgumentException(
					"Global max size can not be less than or equal to 0");	
		}
		DAOProperties.globalMaxSize = globalMaxSize;
	}

	/**
	 * @return 	An <code>EvictionMethod</code> defining how to drop an element 
     * 			from a pool when it reaches its maximum size, or when the global max 
     * 			is reached. Possible choices are LRU and LFU. Default is LRU. 
	 */
	public EvictionMethod getEvictionMethod() {
		return evictionMethod;
	}

	/**
	 * @param evictionMethod 	An <code>EvictionMethod</code> to set how to drop 
	 * 							an element from a pool when it reaches its maximum size, 
	 * 							or when the global max is reached. Possible choices are 
	 * 							LRU and LFU. Default is LRU. 
	 */
	public void setEvictionMethod(EvictionMethod evictionMethod) {
		this.evictionMethod = evictionMethod;
	}
	
	/**
	 * @return	A <code>float</code> defining the proportion of elements 
     * 			in a pool to be removed when the eviction method is triggered. 
     * 			Default is 0.1.
	 */
	public float getEvictionFactor() {
		return evictionFactor;
	}

	/**
	 * @param evictionFactor A <code>float</code> defining the proportion of elements 
     * 						in a pool to be removed when the eviction method is triggered. 
     * 						Default is 0.1.
     * @throws IllegalArgumentException	If <code>evictionFactor</code> is less than 
     * 									or equal to 0, or greater than 1.
	 */
	public void setEvictionFactor(float evictionFactor) throws IllegalArgumentException {
		if (evictionFactor <= 0 || evictionFactor > 1) {
			throw new IllegalArgumentException(
					"The eviction factor cannot be less than or equal to 0, or greater than 1");
		}
		this.evictionFactor = evictionFactor;
	}

	@Override
	public String toString() {
		return "psp4jdbc properties: " + 
	        DRIVERCLASSNAMEKEY + "=" + this.getDriverClassName() + " - " + 
			GLOBALMAXSIZEKEY + "=" + getGlobalMaxSize() + " - " + 
	        POOLMAXSIZEKEY + "=" + this.getPoolMaxSize() + " - " + 
			EVICTIONMETHODKEY + "=" + this.getEvictionMethod();
	}
}
