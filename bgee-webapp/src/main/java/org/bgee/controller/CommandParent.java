package org.bgee.controller;

import java.io.Writer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.bgee.view.ViewFactory;

/**
 * Parent class of all controllers. It achieves operations that are common to all subclasses, 
 * and defines methods that subclasses must implement.
 * 
 * Notably, it defines the abstract method <code>processRequest</code>, 
 * that is the main method of all controllers, 
 * launching actions on the <code>model</code> layer, 
 * and display from the <code>view</code> layer.
 * <p>
 * This class is not instantiable, as all concrete controllers must provide at least 
 * their own implementation of <code>processRequest()</code>.
 * 
 * @author 	Frederic Bastian
 * @version Bgee 11 Mar 2012
 * @see 	#processRequest()
 * @since 	Bgee 1
 *
 */
abstract class CommandParent {
	/**
	 * Concrete factory providing classes from the <code>view</code> package. 
	 * This concrete factory implements the <code>ViewFactory</code> interface.
	 * @see #setFactory()
	 */
    protected ViewFactory viewFactory;
    protected Writer out;
    protected HttpServletResponse response;
    protected HttpSession session;
    /**
     * Stores the parameters of the current request.
     */
    protected RequestParameters requestParameters;
    
    protected String serverRoot;
    protected String homePage;
    protected String bgeeRoot;
   	
    
	public CommandParent(HttpSession session, HttpServletResponse response, 
			RequestParameters requestParameters)
    {
    	this.response = response;
    	this.session  = session;
    	this.requestParameters = requestParameters;
    	this.setFactory();
    	
    	this.serverRoot = BgeeProperties.getBgeeRootDirectory();
    	this.homePage   = BgeeProperties.getBgeeRootDirectory();
    	this.bgeeRoot   = BgeeProperties.getBgeeRootDirectory();
    }
	
	/**
	 * This method is responsible for pre-processing a request, 
	 * and returns <code>true</code> if the application can continue 
	 * and call the <code>processRequest()</code> method.
	 * 
	 * A pre-processing step is, for instance, redirecting to another page, 
	 * filtering a parameter... In the case of a redirection, the method should 
	 * return <code>false</code> so that the application is aware that 
	 * the request should not be processed further.
	 * <p>
	 * Concrete controllers can override this method to provide 
	 * their own pre-processing, specific to their domain of action. 
	 * It is recommended that they call the parent method in their own implementation, 
	 * unless their pre-processing steps overlap (for instance, performing a redirection 
	 * in the concrete controller, while this parent controller also perform a redirection). 
	 * 
	 * @return 	<code>true</code> if the request can be further processed, 
	 * 			<code>false</code> otherwise (<code>processRequest()</code> should then not been called).
	 * @throws 	Exception 	any exception not-caught during the process of the request 
	 * 						is thrown to be caught by the <code>FrontController</code>.
	 * @see 	#processRequest()
	 * @see 	FrontController#doRequest(HttpServletRequest, HttpServletResponse, boolean)
	 */
	public boolean preprocessRequestAndCheckIfContinue() {
		return false;
	}
	
	/**
	 * Main method of all controllers, responsible for analyzing the query, 
	 * and triggering the appropriate actions on the <code>model</code> layer 
	 * and the <code>view</code> layer.
	 * 
	 * Each concrete controller must provide its own implementation of this method.
	 * 
	 * @throws  Exception 	any exception not-caught during the process of the request 
	 * 						is thrown to be caught by the <code>FrontController</code>.
	 * @see 	FrontController#doRequest(HttpServletRequest, HttpServletResponse, boolean)
	 */
	public abstract void processRequest() throws Exception;
	
	/**
	 * Chooses the appropriate concrete view factory.
	 * 
	 * This concrete view factory, implements the interface <code>ViewFactory</code>, 
	 * and provides concrete classes from a specific sub-package 
	 * of the <code>view</code>: either <code>view.xml</code>, or <code>view.html</code>, 
	 * or <code>view.dsv</code>. It allows the controllers to be provided with classes 
	 * to generate outputs, without being aware which type of output they generate 
	 * (Abstract Factory Pattern).
	 * <p>
	 * the default concrete factory is the <code>HtmlFactory</code>.
	 * 
	 * @see view.ViewFactory
	 * @see view.html.HtmlFactory
	 * @see view.xml.XmlFactory
	 * @see view.dsv.DsvFactory
	 */
	protected void setFactory()
	{
		int displayType = ViewFactory.DEFAULT;
		if (this.requestParameters.isXmlDisplayType()) {
			displayType = ViewFactory.XML;
		} else if (this.requestParameters.isCsvDisplayType()) {
			displayType = ViewFactory.CSV;
		} else if (this.requestParameters.isTsvDisplayType()) {
			displayType = ViewFactory.TSV;
		}
		this.viewFactory = ViewFactory.getFactory(this.response, displayType, this.requestParameters);
	}
	
	protected void pageNotFound()
	{
		
	}
	
	protected void failedRequest()
	{
		
	}
	
	protected String getPreviousPage()
	{
		String previousPage = (String) this.session.getAttribute("previousPage");
		if (StringUtils.isNotBlank(previousPage)) {
			return previousPage;
		}
		return this.bgeeRoot;
	}
}
