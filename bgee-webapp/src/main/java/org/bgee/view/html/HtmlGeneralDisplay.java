package org.bgee.view.html;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.bgee.controller.BgeeProperties;
import org.bgee.controller.RequestParameters;
import org.bgee.view.GeneralDisplay;

public class HtmlGeneralDisplay extends HtmlParentDisplay implements GeneralDisplay
{
	public HtmlGeneralDisplay(HttpServletResponse response, RequestParameters requestParameters, BgeeProperties prop) throws IOException
	{
		super(response, requestParameters, prop);
	}

	public void serviceUnavailable()
	{
		this.sendServiceUnavailableHeaders();

		this.startDisplay("unavailable", 
				"Service unavailable for maintenance");

		this.writeln("<p class='alert'>Due to technical problems, Bgee is currently unavailable. " +
				"We are working to restore Bgee as soon as possible. " +
				"We apologize for any inconvenience.</p>");

		this.endDisplay();
	}

	public void displayHomePage() 
	{
		this.startDisplay("home", 
				"welcome on Bgee: a dataBase for Gene Expression Evolution");

		this.endDisplay();
	}

	@Override
	public void displayAbout() 
	{
		this.startDisplay("home", 
				"Information about Bgee: a dataBase for Gene Expression Evolution");

		this.writeln("<h1>What is Bgee?</h1>");

		this.endDisplay();
	}

	@Override
	public void displayRequestParametersNotFound(String key)
	{
		this.sendBadRequestHeaders();
		this.startDisplay("", "Request parameters not found");
		this.writeln("<p class='alert'>Woops, something wrong happened</p>");
		this.writeln("<p>You tried to use in your query some parameters supposed to be stored on our server, " +
				"but we could not find them. Either the key you used was wrong, " +
				"or we were not able to save these parameters. " +
				"Your query should be rebuilt by setting all the parameters from scratch. " +
				"We apologize for any inconvenience.</p>");
		this.endDisplay();
	}

	@Override
	public void displayPageNotFound(String message)
	{
		this.sendPageNotFoundHeaders();
		this.startDisplay("", "404 not found");
		this.writeln("<p class='alert'>Woops, something wrong happened</p>");
		this.writeln("<p>404 not found. We could not understand your query, see details below:</p> " +
				"<p>" + htmlEntities(message) + "</p>");
		this.endDisplay();
	}

	@Override
	public void displayUnexpectedError()
	{
		this.sendInternalErrorHeaders();
		this.startDisplay("", "500 internal server error");
		this.writeln("<p class='alert'>Woops, something wrong happened</p>");
		this.writeln("<p>500 internal server error. " +
				"An error occurred on our side. This error was logged and will be investigated. " +
				"We apologize for any inconvenience.</p>");
		this.endDisplay();
	}

	@Override
	public void displayMultipleParametersNotAllowed(String message) {
		this.sendBadRequestHeaders();
		this.startDisplay("", "Multiple values not allowed");
		this.writeln("<p class='alert'>Woops, something wrong happened</p>");
		this.writeln("<p>"+ message
				+ "</p>"
				+ "Please check the URL and retry.</p>");
		this.endDisplay();	
	}

	@Override
	public void displayRequestParametersNotStorable(String message) {
		this.sendBadRequestHeaders();
		this.startDisplay("", "A parameter is not storable or the key is missing");
		this.writeln("<p class='alert'>Woops, something wrong happened</p>");
		this.writeln("<p>"+ message
				+ "</p>");
		this.endDisplay();
	}
}
