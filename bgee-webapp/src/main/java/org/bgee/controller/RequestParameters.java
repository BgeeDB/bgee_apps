package org.bgee.controller;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.exception.RequestParametersNotFoundException;
import org.bgee.controller.exception.RequestParametersNotStorableException;
import org.bgee.controller.servletutils.BgeeHttpServletRequest;
import org.bgee.utils.BgeeStringUtils;

/**
 * @author Mathieu Seppey
 * @author Frederic Bastian
 * 
 * TODO Add missing functionalities + Log
 * For the moment, load the params from the URL and/or key
 * Store the storable params.
 * Provide the correct new URL by using the key.
 */
public class RequestParameters {

	private final static Logger LOGGER = LogManager.getLogger(RequestParameters.class.getName());

	/**
	 * An {@code EnumMap<URLParameter, ArrayList<ArrayList<String>>> } to store all parameters
	 * having the data type {@code String}
	 * @see loadParametersFromRequest
	 * @see getValue
	 */
	private EnumMap<URLParameter, ArrayList<ArrayList<String>>> stringParameters = 
			new EnumMap<URLParameter,ArrayList<ArrayList<String>>>(URLParameter.class);

	/**
	 * An {@code EnumMap<URLParameter, ArrayList<ArrayList<Integer>>> } to store all parameters
	 * having the data type {@code Integer}
	 * @see loadParametersFromRequest
	 * @see getValue
	 */
	private EnumMap<URLParameter, ArrayList<ArrayList<Integer>>> integerParameters = 
			new EnumMap<URLParameter,ArrayList<ArrayList<Integer>>>(URLParameter.class);

	/**
	 * An {@code EnumMap<URLParameter, ArrayList<ArrayList<Boolean>>> } to store all parameters
	 * having the data type {@code Boolean}
	 * @see loadParametersFromRequest
	 * @see getValue
	 */
	private EnumMap<URLParameter, ArrayList<ArrayList<Boolean>>> booleanParameters = 
			new EnumMap<URLParameter,ArrayList<ArrayList<Boolean>>>(URLParameter.class);

	/**
	 * This <code>String</code> is a key to retrieve from a file a query string 
	 * holding storable parameters, 
	 * to retrieve parameters that were too large to be passed through URL.
	 * <p>
	 * This key can be either retrieved from the current request, or generated 
	 * when this class is asked to generate an URL, and that URL would be too long, 
	 * if the parameters were not stored and replaced by a key.
	 * @see #generateKey()
	 * @see #loadStorableParametersFromKey()
	 * @see #store()
	 */
	private String generatedKey;

	/**
	 * A <code>boolean</code> defining whether parameters should be url encoded 
	 * by the <code>encodeUrl</code> method.
	 * If <code>false</code>, then the <code>encodeUrl</code> method returns 
	 * Strings with no modifications, otherwise, they are url encoded if needed 
	 * (it does not necessarily mean they will. For instance, if there are no 
	 * special chars to encode in the submitted String).
	 * <p>
	 * Default value is <code>true</code>.
	 * 
	 * @see #urlEncode(String)
	 */
	private boolean encodeUrl;

	/**
	 * <code>String</code> defining the directory where query strings holding storable parameters  
	 * from previous large queries are stored. 
	 * Category: server parameters.
	 * @see #loadStorableParametersFromKey()
	 * @see #store()
	 */
	private String requestParametersStorageDirectory;

	/**
	 * <code>ConcurrentMap</code> used to manage concurrent access to 
	 * the read/write locks that are used to manage concurrent reading and writing 
	 * of the files storing query strings holding storable parameters. 
	 * The generated key of the <code>RequestParameters</code> object to be loaded or stored 
	 * is associated to the lock in this <code>Map</code>.
	 * 
	 * @see 	#generatedKey
	 * @see 	#store()
	 * @see 	#loadStorableParametersFromKey()
	 */
	private static ConcurrentMap<String, ReentrantReadWriteLock> readWriteLocks= 
			new ConcurrentHashMap<String, ReentrantReadWriteLock>();

	/**
	 * Default constructor. 
	 */
	public RequestParameters(){
		// call the constructor with an empty request
		this(new BgeeHttpServletRequest());
	}

	/**
	 * Constructor building a <code>RequestParameters</code> object from a 
	 * <code>HttpServletRequest</code> object.
	 * <p>
	 * It means that the parameters are recovered from the query string or posted data.
	 * 
	 * @param 	request 			The HttpServletRequest object corresponding to the current 
	 * 								request to the server.
	 * @throws 	RequestParametersNotFoundException		if a <code>generatedKey</code> is set in the URL, 
	 * 								meaning that a stored query string should be retrieved using this key, 
	 * 								to populate the storable parameters of this <code>RequestParameters</code> object, 
	 * 								but these parameters could not be found using this key. 
	 * 								See <code>loadStorableParameters(HttpServletRequest)</code> and 
	 * 								<code>generatedKey</code> for more details.
	 * @see 	#loadParameters(HttpServletRequest)
	 * @see 	#generatedKey
	 */
	public RequestParameters(HttpServletRequest request)
	{
		// TODO set parameters properly
		this.requestParametersStorageDirectory = "/tmp/";
		this.encodeUrl = true;

		try {
			this.loadParameters(request);
			if(request.getParameterMap().isEmpty() == false){
				this.generateKey(this.generateParametersQuery("&",",",true,false,false));
				this.store();
			}
		} catch (RequestParametersNotFoundException | RequestParametersNotStorableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Load all the parameters related to the request, based on 
	 * the <code>HttpServletRequest</code> object. 
	 * It uses the parameters present in the request or load them from a file.
	 * If the current request includes a key to retrieve a stored query string, 
	 * the corresponding query string is retrieved from a file named as the key, 
	 * If no key is provided, the storable parameters are simply retrieved from the current request.
	 * 
	 * @param 	request 	the <code>HttpServletRequest</code> object 
	 * 						representing the current request to the server.
	 * 
	 * @see 	#loadParametersFromRequest
	 * @see		#loadStorableParametersFromKey
	 */
	private void loadParameters(HttpServletRequest request) throws RequestParametersNotFoundException
	{
		//get the key, if set in the current URL
		this.setGeneratedKey(BgeeStringUtils.secureString(request.getParameter(RequestParameters.getGeneratedKeyParameterName())));

		if (StringUtils.isBlank(this.getGeneratedKey())) {
			//no key set, get the parameters from the URL
			this.loadParametersFromRequest(request,true);
		} else {
			//a key is set, get the storable parameters from a file

			//we need to store the key, 
			//because setting storable parameters reset the generatedKey
			String key = this.getGeneratedKey();
			try {
				this.loadStorableParametersFromKey();
			} catch (IOException e) {
				throw new RequestParametersNotFoundException(e);
			}
			//we need to set again the key, 
			//because setting storable parameters reset the key
			this.setGeneratedKey(key);

			// load the non storable params
			this.loadParametersFromRequest(request,false);
		}

	}

	/**
	 * Load the parameters from the <code>HttpServletRequest</code> object 
	 * 
	 * @param 	request							the <code>HttpServletRequest</code> object 
	 * 											representing the current request to the server.
	 * 
	 * @param	loadStorable					a {@Code boolean} that indicates whether the storable 
	 * 											parameters have to be loaded from the request. 
	 * 											For example, if the storable parameters were
	 * 											loaded from the key, this method will be called to load
	 * 											the non-storable parameter only.
	 * @see #loadStorableParametersFromKey
	 * @see #loadParameters
	 */
	private void loadParametersFromRequest(HttpServletRequest request, boolean loadStorable) {

		for (URLParameter p : URLParameter.values()){

			if(loadStorable || p.isStorable() == false){

				String[] values = request.getParameterValues(p.getName());

				if(values != null){

					switch(p.getType().getCanonicalName()){

					case("java.lang.String") :

						ArrayList<ArrayList<String>> level1ListString = 
						new ArrayList<ArrayList<String>>();

					for(String s1:values){

						ArrayList<String> level2ListString = 
								new ArrayList<String>();

						for(String s2:s1.split(",")){
							level2ListString.add(
									BgeeStringUtils.secureString(s2,p.getMaxSize(),p.getFormat()));
						}

						level1ListString.add(level2ListString);

					}

					stringParameters.put(p,level1ListString);

					break;

					case("java.lang.Integer") :

						ArrayList<ArrayList<Integer>> level1HashSetInteger = 
						new ArrayList<ArrayList<Integer>>();

					for(String s1:values){

						ArrayList<Integer> level2HashSetInteger = 
								new ArrayList<Integer>();

						for(String s2:s1.split(",")){
							level2HashSetInteger.add(
									Integer.valueOf(s2));
						}

						level1HashSetInteger.add(level2HashSetInteger);

					}

					integerParameters.put(p,level1HashSetInteger);

					break;

					case("java.lang.Boolean") :

						ArrayList<ArrayList<Boolean>> level1HashSetBoolean = 
						new ArrayList<ArrayList<Boolean>>();

					for(String s1:values){

						ArrayList<Boolean> level2HashSetBoolean = 
								new ArrayList<Boolean>();

						for(String s2:s1.split(",")){
							level2HashSetBoolean.add(
									Boolean.valueOf(s2));
						}

						level1HashSetBoolean.add(level2HashSetBoolean);

					}

					booleanParameters.put(p,level1HashSetBoolean);

					break;

					default:
						// TODO throw exception if this portion of code is reached
						break;
					}
					
				}
				
			}
			
		}
		
	}

	/**
	 * Load the storable parameters from the file corresponding to the provided key.
	 * <p>
	 * If a key is provided, but no stored query string is found corresponding to this key, 
	 * a RequestParametersNotFoundException is thrown.
	 *
	 * @throw 	RequestParametersNotFoundException 	if a <code>generatedKey</code> is set, 
	 * 			but no stored query string is retrieved using this key.
	 * 
	 * @see #loadParameters
	 * @see #loadParametersFromRequest
	 */
	private void loadStorableParametersFromKey() throws IOException 
	{
		final String key = this.getGeneratedKey();

		ReentrantReadWriteLock lock = this.getReadWriteLock(key);
		try {
			lock.readLock().lock();

			while (readWriteLocks.get(key) == null ||  
					!readWriteLocks.get(key).equals(lock)) {

				lock = this.getReadWriteLock(key);
				lock.readLock().lock();
			}

			BufferedReader br = new BufferedReader(new FileReader(
					this.getRequestParametersStorageDirectory() + key));

			String retrievedQueryString;
			//just one line in the file, a query string including storable parameters, 
			//that will be used to recover storable parameters
			if ((retrievedQueryString = br.readLine()) != null) {

				//				retrievedQueryString = this.getCompleteStorableParametersQueryString() + "&" + retrievedQueryString;

				//here we create a fake HttpServletRequest using the query string we retrieved.
				//this way we do not duplicate code to load parameters into this RequestParameters object.
								
				BgeeHttpServletRequest request = new BgeeHttpServletRequest(retrievedQueryString);
				this.loadParametersFromRequest(request,true);
			}
			br.close();

		} finally {
			lock.readLock().unlock();
			this.removeLockIfPossible(key);
		}

	}

	/**
	 * Store the part of the query string holding storable parameters into a file: 
	 * get the part of the query string containing "storable" parameters 
	 * (by calling <code>getCompleteStorableParametersQueryString()</code>), 
	 * generate a key based on that string, and store the string in a file named as the key.
	 * <p>
	 * This allows to store parameters too lengthy to be put in URL, to replace these parameters 
	 * by the <code>generatedKey</code>, and to store these parameters to retrieve them at later pages 
	 * using that key.
	 * 
	 * @throws RequestParametersNotStorableException 	if an error occur while trying to use the key 
	 * 													or to write the query string in a file
	 * @see #getCompleteStorableParametersQueryString()
	 * @see #generatedKey
	 */
	private void store() throws RequestParametersNotStorableException
	{

		if (StringUtils.isBlank(this.getGeneratedKey())) {
			throw new RequestParametersNotStorableException("No key generated before storing a "
					+ "RequestParameters object");
		}

		//first check whether these parameters have already been serialized
		File storageFile = new File(this.getRequestParametersStorageDirectory() + this.getGeneratedKey());
		if (storageFile.exists()) {
			//file already exists, no need to continue
			return;
		}

		ReentrantReadWriteLock lock = this.getReadWriteLock(this.getGeneratedKey());
		BufferedWriter bufferedWriter = null;
		try {


			lock.writeLock().lock();

			while (readWriteLocks.get(this.getGeneratedKey()) == null ||  
					!readWriteLocks.get(this.getGeneratedKey()).equals(lock)) {

				lock = this.getReadWriteLock(this.getGeneratedKey());
				lock.writeLock().lock();
			}

			bufferedWriter = new BufferedWriter(
					new FileWriter(this.getRequestParametersStorageDirectory() + this.getGeneratedKey()));

			boolean encodeUrlValue = this.encodeUrl;
			this.encodeUrl = false;
			bufferedWriter.write(generateParametersQuery("&",",",true,false,false));
			this.encodeUrl = encodeUrlValue;

			
			bufferedWriter.close();
		} catch (IOException e) {
			//delete the file if something went wrong
			storageFile = new File(this.getRequestParametersStorageDirectory() + this.getGeneratedKey());
			if (storageFile.exists()) {
				storageFile.delete();
			}
			throw new RequestParametersNotStorableException(e.getMessage(), e);
		} finally {
			lock.writeLock().unlock();
			this.removeLockIfPossible(this.getGeneratedKey());
		}
	}

	private void removeLockIfPossible(String key)
	{
		//check if there is already a lock stored for this key
		ReentrantReadWriteLock lock = readWriteLocks.get(key);

		//there is a lock to remove
		if (lock != null) {
			//there is no thread with write lock, or read lock, or waiting to acquire a lock
			if (!lock.isWriteLocked() && lock.getReadLockCount() == 0 && !lock.hasQueuedThreads()) {
				readWriteLocks.remove(key);
			}
		}
	}

	/**
	 * Obtain a <code>ReentrantReadWriteLock</code>, for the param <code>key</code>.
	 * 
	 * This method tries to obtain <code>ReentrantReadWriteLock</code> corresponding to the key, 
	 * from the <code>ConcurrentHashMap</code> <code>readWriteLocks</code>. 
	 * If the lock is not already stored, 
	 * create a new one, and put it in <code>readWriteLocks</code>, to be used by other threads.
	 * 
	 * @param key 				a <code>String</code> corresponding to the key to retrieve the lock from 
	 * 							<code>readWriteLocks</code>.
	 * 							This key is generated by the method <code>generateKey</code>
	 * @return 					a <code>ReentrantReadWriteLock</code> corresponding to the key.
	 * @see 					#generateKey()
	 * @see 					#readWriteLocks
	 */
	private ReentrantReadWriteLock getReadWriteLock(String key)
	{
		//check if there is already a lock stored for this key
		ReentrantReadWriteLock readWritelock = readWriteLocks.get(key);

		//no lock already stored
		if (readWritelock == null) {
			ReentrantReadWriteLock newReadWriteLock = new ReentrantReadWriteLock(true);
			//try to put the new lock in the ConcurrentHashMap
			readWritelock = readWriteLocks.putIfAbsent(key, newReadWriteLock);
			//if readWritelock is null, the newLock has been successfully put in the map, and we use it.
			//otherwise, it means that another thread has inserted a new lock for this key in the mean time.
			//readWritelock then corresponds to this value, that we should use.
			if (readWritelock == null) {
				readWritelock = newReadWriteLock;
			}
		}

		return readWritelock;
	}

	/**
	 * @return 	the requestParametersStorageDirectory
	 * @see 	#requestParametersStorageDirectory
	 */
	private String getRequestParametersStorageDirectory() {
		return this.requestParametersStorageDirectory;
	}

	private String generateParametersQuery(String parametersSeparator,String valueSeparator,
			boolean includeStorable, boolean includeNonStorable, boolean includeKey){

		String urlFragment = "";


		for (URLParameter p : URLParameter.values()){

			if((includeStorable && p.isStorable()) || (includeNonStorable && p.isStorable() == false)){

				switch(p.getType().getCanonicalName()){

				case("java.lang.String") :

					ArrayList<ArrayList<String>> level1ListString = this.getValue(p);

				if(level1ListString != null){

					for(ArrayList<String> level2ListString:level1ListString){
						urlFragment += p.getName()+ "=";
						for(String value :level2ListString){
							if(StringUtils.isNotBlank(value)){
								urlFragment += this.urlEncode(value + valueSeparator);
							}
						}
						urlFragment = urlFragment.substring(0, urlFragment.length()-1);
						urlFragment += parametersSeparator ;
					}
				}

				break;

				case("java.lang.Integer") :

					ArrayList<ArrayList<Integer>> level1HashSetInteger = this.getValue(p);

				if(level1HashSetInteger != null){

					for(ArrayList<Integer> level2HashSetInteger:level1HashSetInteger){
						urlFragment += p.getName()+ "=";
						for(Integer value :level2HashSetInteger){
							urlFragment +=  
									this.urlEncode(value.toString() + valueSeparator);
						}
						urlFragment = urlFragment.substring(0, urlFragment.length()-1);
						urlFragment += parametersSeparator ;
					}
				}

				break;

				case("java.lang.Boolean") :

					ArrayList<ArrayList<Boolean>> level1HashSetBoolean = this.getValue(p);

				if(level1HashSetBoolean != null){

					for(ArrayList<Boolean> level2HashSetBoolean:level1HashSetBoolean){
						urlFragment += p.getName()+ "=";
						for(Boolean value :level2HashSetBoolean){
							urlFragment +=  
									this.urlEncode(value.toString() + valueSeparator);
						}
						urlFragment = urlFragment.substring(0, urlFragment.length()-1);
						urlFragment += parametersSeparator ;
					}
				}

				break;
				default:
					// Throw exception if this portion of code is reached
				}

			}
		}

		if(includeKey){
			urlFragment += RequestParameters.getGeneratedKeyParameterName() + "=" + this.getGeneratedKey();
		}

		return urlFragment ;
	}

	/**
	 * Generate a key to set the <code>generatedKey</code> attribute, 
	 * based on the param <code>urlFragment</code>, 
	 * in order to store this <code>RequestParameters</code> object.
	 * 
	 * This key is a hash of an URL fragment generated from the storable attributes of this object, 
	 * without any length restriction (all the storable attributes are then represented). 
	 * It will be used as an index to store and retrieve this <code>RequestParameters</code> object.
	 * <p>
	 * The key is reset as soon as a storable parameter is modified. A new call to this method will then 
	 * trigger the computation of a new key.
	 * 
	 * @param 	urlFragment 	The fragment of URL based on the storable parameters
	 * @throws NoSuchAlgorithmException 
	 * @see 	#generatedKey
	 * @see 	#getRequestParametersFromKey()
	 * @see 	#store()
	 */
	private void generateKey(String urlFragment) 
	{

		LOGGER.info("Trying to generate a key based on urlFragment: {}", 
				urlFragment);

		if (StringUtils.isNotBlank(urlFragment)) {
			this.setGeneratedKey(DigestUtils.sha1Hex(urlFragment.toLowerCase(Locale.ENGLISH)));
		}

		LOGGER.info("Key generated: {}", this.getGeneratedKey());
	}

	public String generateUrl(){
		if(this.getGeneratedKey() != null){
			return generateParametersQuery("&",",",false,true,true);
		}
		else{
			return generateParametersQuery("&",",",true,true,false);
		}
	}

	/**
	 * Encode String to be used in URLs. 
	 * <p>
	 * This method is different from the <code>encodeURL</code> method 
	 * of <code>HttpServletResponse</code>, as it does not incude a logic 
	 * for session tracking. It just converts special chars to be used in URL.
	 * <p>
	 * The encoding can be desactivated by setting the <code>encodeUrl</code> attribute to <code>false</code>.
	 * 
	 * @param string 	the <code>String</code> to be encoded.
	 * @return 			a <code>String</code> encoded, if needed (meaning, if including special chars), 
	 * 					and if the <code>encodeUrl</code> attribute is <code>true</code>
	 * 
	 * @see #encodeUrl
	 */
	private String urlEncode(String url){
		if(this.encodeUrl){
			return BgeeStringUtils.urlEncode(url);
		}
		return url;
	}

	/**
	 * Performs security checks before assigning the <code>String</code>.
	 * 
	 * @param 	generatedKey the generatedKey to set
	 * @see 	#generatedKey
	 * @see 	#secureString(String)
	 */
	private void setGeneratedKey(String generatedKey) {
		this.generatedKey = BgeeStringUtils.secureString(generatedKey);
	}

	/**
	 * @return 	the generatedKey
	 * @see 	#generatedKey
	 */
	private String getGeneratedKey() {
		return this.generatedKey;
	}

	/**
	 * Get the name of the parameter used in the query string of URLs, 
	 * corresponding to the <code>generatedKey</code> attribute. 
	 * (for instance, ?data=blablabla)
	 * 
	 * @return 	a <code>String</code> corresponding to the name of the parameter 
	 * 			for the <code>generatedKey</code> attribute.
	 * @see 	#generatedKey
	 */
	private static String getGeneratedKeyParameterName()
	{
		return "data";
	}

	@SuppressWarnings("unchecked") // TODO comment on this issue
	public <T> T getValue(URLParameter p){

		switch(p.getType().getCanonicalName()){

		case("java.lang.String") :

			return (T) stringParameters.get(p);

		case("java.lang.Integer") :

			return (T) integerParameters.get(p);

		case("java.lang.Boolean") :

			return (T) booleanParameters.get(p);

		// TODO, throw exception

		}

		return null;

	}
}


