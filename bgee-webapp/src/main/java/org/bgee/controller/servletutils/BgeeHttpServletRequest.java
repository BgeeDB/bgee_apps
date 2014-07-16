package org.bgee.controller.servletutils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

/**
 * This class implements the <code>HttpServletRequest</code> for mainly two purposes: 
 * first, allowing to perform tests on the Bgee application in a servlet context, 
 * without the need of actually using a servlet container (i.e., tomcat); 
 * second, to load parameters into a <code>RequestParameters</code> object, 
 * as if they were coming from a regular <code>HttpServletRequest</code>, 
 * while they actually come from a query string stored in a file 
 * (when parameters are too long to be put in a URL, because of URL length restrictions, 
 * they are stored in a file on the server, the file is associated to a key, 
 * and the key is put in the URL; parameters are then retrieved from this query string stored in a file.
 * See the <code>RequestParameters</code> class for more information).
 * This strategy avoid to duplicate, in <code>RequestParameters</code>, 
 * the methods to retrieve parameters from a <code>HttpServletRequest</code>: 
 * we do not need to write methods retrieving them from a query string.
 * <p>
 * The <code>RequestParameters</code> class only uses the methods 
 * <code>getParameter(String)</code>, and <code>getParameterValues(String)</code> 
 * from the <code>HttpServletRequest</code>. As of Bgee 11, these are the only methods implemented here.
 * They use a query string provided to this class, in order to simulate the behavior of these methods.
 * Other methods will be implemented in the future, to use this class for test purposes.
 * 
 * @author Frederic Bastian
 * @version Bgee 11 July 2012
 * @see controller.RequestParameters
 * @since Bgee 11
 */
public class BgeeHttpServletRequest implements HttpServletRequest
{
	/**
	 * A regular query string, that will be used to load <code>parameterMap</code>, 
	 * that will allow to simulate the behavior of <code>getParameterMap()</code>, 
	 * and from there, <code>getParameter(String)</code>, <code>getParameterValues(String)</code>, etc.
	 */
	private String queryString;
	/**
	 * A <code>String</code> containing the name of the character encoding 
	 * of the <code>queryString</code>.
	 * 
	 * @see #querystring
	 * @see #loadParameterMap()
	 */
	private String characterEncoding;
	/**
	 * A <code>Map</code> of the parameters contained in <code>queryString</code>, 
	 * where keys are parameters names, and values are <code>String</code> <code>vectors</code> 
	 * representing the values corresponding to a parameter name. 
	 * Values are URL decoded.
	 * @see #loadParameterMap()
	 * @see #queryString
	 */
	private Map<String, String[]> parameterMap;
	/**
	 * A <code>String</code> to simulate <code>getMethod()</code>, 
	 * that should contain the name of the HTTP method 
	 * with which this request was made, for example, GET, POST, or PUT.
	 * 
	 * @see getMethod()
	 */
	private String method;

	/**
	 * Default constructor, 
	 * used to simulate a request with no parameters set (empty query string).
	 */
	public BgeeHttpServletRequest()
	{
		this("");
	}
	
	/**
	 * A public constructor taking as a parameter a <code>String</code>, 
	 * that should be a regular query string of a URL. 
	 * It will be used to set the <code>queryString</code> attribute of this class. 
	 * This attribute is then used to simulate regular <code>HttpServletRequest</code> 
	 * functionalities, such as <code>getParameter(String)</code>, etc.
	 * Parameters in this query string are assumed to be URL encoded with an UTF-8 encoding. 
	 * 
	 * @param queryString 	a <code>String</code> representing the query string part of an URL, 
	 * 						to set the <code>queryString</code> attribute of this class, 
	 * 						with values of parameters URL encoded in UTF-8.
	 */
	public BgeeHttpServletRequest(String queryString)
	{
		this(queryString, "UTF-8");
	}
	
	/**
	 * A public constructor taking as a parameter a <code>String</code>, 
	 * that should be a regular query string of a URL. 
	 * It will be used to set the <code>queryString</code> attribute of this class. 
	 * This attribute is then used to simulate regular <code>HttpServletRequest</code> 
	 * functionalities, such as <code>getParameter(String)</code>, etc.
	 * Parameters in this query string are URL encoded with the <code>encoding</code> provided. 
	 * 
	 * @param queryString 	a <code>String</code> representing the query string part of an URL, 
	 * 						to set the <code>queryString</code> attribute of this class.
	 * @param encoding 		a <code>String</code> representing the encoding to use while parsing the <code>queryString</code>
	 */
	public BgeeHttpServletRequest(String queryString, String encoding) {
		try {
			this.setCharacterEncoding(encoding);
		} catch (UnsupportedEncodingException e) {
			//here we just do nothing, because we haven't implement 
			//a check of the character encoding yet. An exception will never be thrown then.
		}
		this.setMethod("GET");
		//if this is not a query string, but an URL, filter it
		Pattern queryStringPattern = Pattern.compile("^(.*?\\?)?(.*)");
		Matcher queryStringMatcher = queryStringPattern.matcher(queryString);
		if (queryStringMatcher.matches()) {
			this.setQueryString(queryStringMatcher.group(2));
		} else {
			assert false: "Error, could not match query string: " + queryString;
		}
	}

	/**
	 * Set the <code>queryString</code> and load the parameters <code>Map</code> 
	 * from it, to be able to use <code>getParameterMap</code>.
	 * @param 	queryString the queryString to set
	 * @see 	#queryString
	 * @see 	#loadParameterMap()
	 */
	private void setQueryString(String queryString) 
	{
		this.queryString = queryString;
		this.loadParameterMap();
	}
	
	/**
	 * Populate <code>parameterMap</code> using the <code>queryString</code>, 
	 * in order to implement <code>getParameterMap()</code>.
	 * 
	 * @see #parameterMap
	 * @see #getParameterMap()
	 * @see #queryString
	 */
	private void loadParameterMap()
	{
		this.parameterMap = new HashMap<String, String[]>();
	    if (StringUtils.isBlank(this.queryString)){
	        return;
	    }
	    
	    //parsing URL is never as simple as it looks. We then use an external library to do it.
	    //this library returns a list of NameValuePair.
	    List<NameValuePair> list = URLEncodedUtils.parse(
	    		URI.create("http://localhost/?" + this.getQueryString()), 
	    		this.getCharacterEncoding());

	    //we will convert it to a Map<String, String[]>, 
	    //in order to stay as close as possible to the HttpServletInterface 
	    //and its getParameterMap() method
	    Map<String, List<String>> mapOfLists = new HashMap<String, List<String>>();
	    for (NameValuePair pair : list) {
	        if (pair.getValue() != null) {
	            List<String> values = mapOfLists.get(pair.getName());
	            if (values == null) {
	                values = new ArrayList<String>();
	                mapOfLists.put(pair.getName(), values);
	            }
	            values.add(pair.getValue());
	        }
	    }
	    
	    //conversion to Map<String, String[]>
	    for (String key : mapOfLists.keySet()) {
	        this.getParameterMap().put(key, mapOfLists.get(key).toArray(new String[] {}));
	    }
	}
	
	/**
	 * @param 	the <code>method</code> to set
	 * @see #method
	 */
	private void setMethod(String method) {
		this.method = method;
	}
	
//*******************************************
//    IMPLEMENTED OVERRIDEN METHODS
//*******************************************

	@Override
	public String getMethod() {
		return this.method;
	}
	
	@Override
	public String getQueryString() 
	{
		return this.queryString;
	}
	
	@Override
	public String getParameter(String parameterName) 
	{
		String[] values = this.getParameterValues(parameterName);
		if (values != null && values.length > 0) {
			return values[0];
		}
		return null;
	}

	@Override
	public String[] getParameterValues(String parameterName) 
	{
		return this.getParameterMap().get(parameterName);
	}

	@Override
	//TODO: this public method should return a clone of the Map, not the actual Map
	public Map<String, String[]> getParameterMap() 
	{
		return this.parameterMap;
	}

	@Override
	//TODO: Do a check on the character encoding to be able to throw a UnsupportedEncodingException
	public void setCharacterEncoding(String encoding)
			throws UnsupportedEncodingException 
	{
		this.characterEncoding = encoding;
	}

	@Override
	public String getCharacterEncoding() 
	{
		return this.characterEncoding;
	}
//*******************************************
//  NON-IMPLEMENTED OVERRIDEN METHODS
//*******************************************
	@Override
	public Object getAttribute(String arg0) {
		return null;
	}

	@Override
	public Enumeration<String> getAttributeNames() {
		return null;
	}

	@Override
	public int getContentLength() {
		return 0;
	}

	@Override
	public String getContentType() {
		return null;
	}

	@Override
	public ServletInputStream getInputStream() throws IOException {
		return null;
	}

	@Override
	public String getLocalAddr() {
		return null;
	}

	@Override
	public String getLocalName() {
		return null;
	}

	@Override
	public int getLocalPort() {
		return 0;
	}

	@Override
	public Locale getLocale() {
		return null;
	}

	@Override
	public Enumeration<Locale> getLocales() {
		return null;
	}

	@Override
	public Enumeration<String> getParameterNames() {
		return null;
	}

	@Override
	public String getProtocol() {
		return null;
	}

	@Override
	public BufferedReader getReader() throws IOException {
		return null;
	}

	@Override
	public String getRealPath(String arg0) {
		return null;
	}

	@Override
	public String getRemoteAddr() {
		return null;
	}

	@Override
	public String getRemoteHost() {
		return null;
	}

	@Override
	public int getRemotePort() {
		return 0;
	}

	@Override
	public RequestDispatcher getRequestDispatcher(String arg0) {
		return null;
	}

	@Override
	public String getScheme() {
		return null;
	}

	@Override
	public String getServerName() {
		return null;
	}

	@Override
	public int getServerPort() {
		return 0;
	}

	@Override
	public boolean isSecure() {
		return false;
	}

	@Override
	public void removeAttribute(String arg0) {
		
	}

	@Override
	public void setAttribute(String arg0, Object arg1) {
		
	}

	@Override
	public String getAuthType() {
		return null;
	}

	@Override
	public String getContextPath() {
		return null;
	}

	@Override
	public Cookie[] getCookies() {
		return null;
	}

	@Override
	public long getDateHeader(String arg0) {
		return 0;
	}

	@Override
	public String getHeader(String arg0) {
		return null;
	}

	@Override
	public Enumeration<String> getHeaderNames() {
		return null;
	}

	@Override
	public Enumeration<String> getHeaders(String arg0) {
		return null;
	}

	@Override
	public int getIntHeader(String arg0) {
		return 0;
	}

	@Override
	public String getPathInfo() {
		return null;
	}

	@Override
	public String getPathTranslated() {
		return null;
	}

	@Override
	public String getRemoteUser() {
		return null;
	}

	@Override
	public String getRequestURI() {
		return null;
	}

	@Override
	public StringBuffer getRequestURL() {
		return null;
	}

	@Override
	public String getRequestedSessionId() {
		return null;
	}

	@Override
	public String getServletPath() {
		return null;
	}

	@Override
	public HttpSession getSession() {
		return null;
	}

	@Override
	public HttpSession getSession(boolean arg0) {
		return null;
	}

	@Override
	public Principal getUserPrincipal() {
		return null;
	}

	@Override
	public boolean isRequestedSessionIdFromCookie() {
		return false;
	}

	@Override
	public boolean isRequestedSessionIdFromURL() {
		return false;
	}

	@Override
	public boolean isRequestedSessionIdFromUrl() {
		return false;
	}

	@Override
	public boolean isRequestedSessionIdValid() {
		return false;
	}

	@Override
	public boolean isUserInRole(String arg0) {
		return false;
	}

	@Override
	public ServletContext getServletContext() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AsyncContext startAsync() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AsyncContext startAsync(ServletRequest servletRequest,
			ServletResponse servletResponse) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isAsyncStarted() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isAsyncSupported() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public AsyncContext getAsyncContext() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DispatcherType getDispatcherType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean authenticate(HttpServletResponse response)
			throws IOException, ServletException {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public void login(String username, String password) throws ServletException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void logout() throws ServletException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Collection<Part> getParts() throws IOException,
			IllegalStateException, ServletException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Part getPart(String name) throws IOException, IllegalStateException,
			ServletException {
		// TODO Auto-generated method stub
		return null;
	}

}
