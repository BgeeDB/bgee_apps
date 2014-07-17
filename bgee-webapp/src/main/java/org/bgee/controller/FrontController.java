package org.bgee.controller;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.exception.MultipleValuesNotAllowedException;
import org.bgee.controller.exception.PageNotFoundException;
import org.bgee.controller.exception.RequestParametersNotFoundException;
import org.bgee.controller.exception.RequestParametersNotStorableException;
import org.bgee.view.GeneralDisplay;
import org.bgee.view.ViewFactory;

public class FrontController extends HttpServlet {
	private final static Logger log = LogManager.getLogger(FrontController.class.getName());

	/**
	 * The serialVersionUID is needed, as this class extends the HttpServlet, 
	 * which is serializable.
	 */
	private static final long serialVersionUID = 2022651427006588913L;

	/**
	 * Class constructor.
	 */
	public FrontController() {

	}

	public void doRequest(HttpServletRequest request, HttpServletResponse response, 
	        boolean postData) { 
	    log.entry(request, response);
	    
		URLParameters urlParameters = new URLParameters();
		RequestParameters requestParameters = null;
		GeneralDisplay generalDisplay = null;
		ViewFactory factory = null;

		try {
			requestParameters = new RequestParameters(urlParameters);
		} catch (RequestParametersNotFoundException
				| RequestParametersNotStorableException
				| MultipleValuesNotAllowedException e) {
			log.error(e.getMessage());
			// Do nothing as there is no display loaded to display anything yet
		}
		
		factory = ViewFactory.getFactory(response, requestParameters);
		
		//then let's start the real job!
		try {
						
			//in order to display error message in catch clauses. 
			//we do it in the try clause, because getting a view can throw an IOException.
			//so here we get the default view from the default factory before any exception 
		    //can be thrown.
			generalDisplay = factory.getGeneralDisplay();
			request.setCharacterEncoding("UTF-8");
			requestParameters = new RequestParameters(request, urlParameters);
			
			log.info("Analyzed URL: " + requestParameters.getRequestURL());

			HttpSession session = request.getSession();
			session.setMaxInactiveInterval(86400);

			//in order to display error message in catch clauses. 
			//we redo it here to get the correct display type and correct user, if no exception was thrown yet
			factory = ViewFactory.getFactory(response, requestParameters);

			CommandParent controller = null;
			
			if (requestParameters.isADownloadPageCategory()) {
				controller = new CommandDownload(session, response, requestParameters);
			}
		
			if (controller == null) {
				controller = new CommandHome(session, response, requestParameters);
			}
			
			controller.processRequest();

			session.setAttribute("previousPage", requestParameters.getRequestURL());
			
		} catch(RequestParametersNotFoundException e) {
			generalDisplay.displayRequestParametersNotFound(requestParameters.getFirstValue(
					urlParameters.getParamData()));
			log.error("RequestParametersNotFoundException", e);
		} catch(PageNotFoundException e) {
			generalDisplay.displayPageNotFound(e.getMessage());
			log.error("PageNotFoundException", e);
		} catch(RequestParametersNotStorableException e) {
			generalDisplay.displayRequestParametersNotStorable(e.getMessage());
			log.error("RequestParametersNotStorableException", e);
		} catch(MultipleValuesNotAllowedException e) {
			generalDisplay.displayMultipleParametersNotAllowed(e.getMessage());
			log.error("MultipleValuesNotAllowedException", e);
		} catch(Exception e) {
			if (generalDisplay != null) {
				generalDisplay.displayUnexpectedError();
			}
			log.error("Other Exception", e);
		} finally {
			//Database.destructAll();
		}
	}

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) {
		doRequest(request, response, false);
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) {
		doRequest(request, response, true);
	}

}
