package org.bgee.controller;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import java.awt.List;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.stream.Collectors;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.TestAncestor;
import org.bgee.controller.exception.InvalidRequestException;
import org.bgee.controller.user.User;
import org.bgee.controller.utils.MailSender;
import org.bgee.model.ServiceFactory;
import org.bgee.model.expressiondata.baseelements.DataType;
import org.bgee.model.job.JobService;
import org.bgee.view.ViewFactory;
import org.junit.Test;

public class CommandAnatEntityTest extends TestAncestor {

	/**
	 * Unit tests for {@link CommandAnatEntity}.
	 * 
	 * @author  Julien Wollbrett
	 * @version Bgee 13 Jan. 2017
	 * @since   Bgee 13 Jan. 2017
	 */
	
	private final static Logger log = 
            LogManager.getLogger(CommandAnatEntityTest.class.getName());

    @Override
    protected Logger getLogger() {
        return log;
    }
    
    private static class FakeCommand extends CommandParent {

        public FakeCommand(HttpServletResponse response, RequestParameters requestParameters, 
                BgeeProperties prop, ViewFactory viewFactory, ServiceFactory serviceFactory, 
                JobService jobService, User user, ServletContext context, MailSender mailSender) {
            super(response, requestParameters, prop, viewFactory, serviceFactory, jobService, user, 
                    context, mailSender);
        }

        @Override
        public void processRequest() throws Exception {
            //nothing here
        }
        
    }
    
    @Test
    public void shouldCheckAndGetDataTypes() throws InvalidRequestException {

        HttpServletResponse response = mock(HttpServletResponse.class);
        ServletContext context = mock(ServletContext.class);
        
        RequestParameters params = new RequestParameters();
//        log.info("Generated query URL: " + params.getRequestURL());
//        //no data type parameter, should retrieve no data types
//        FakeCommand command = new FakeCommand(response, params, BgeeProperties.getBgeeProperties(), 
//                null, null, null, null, context, null);
//        assertEquals("No data types should have been retrieved", null, command.checkAndGetDataTypes());
        
      //all parameters, should retrieve all data types
        params = new RequestParameters();
        log.debug(params.getUrlParametersInstance().getParamAnatEntityId());
        params.addValues(params.getUrlParametersInstance().getParamAnatEntityId(), 
               Collections.singletonList("TEST"));
        log.info("Generated query URL: " + params.getRequestURL());
        FakeCommand command = new FakeCommand(response, params, BgeeProperties.getBgeeProperties(), 
                null, null, null, null, context, null);
        assertEquals("All data types should have been retrieved", EnumSet.allOf(DataType.class), 
                command.checkAndGetDataTypes());
    }
    
    
    @Test
    public void shouldProcessRequest(){
    	
    }
}
