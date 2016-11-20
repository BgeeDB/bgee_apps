package org.bgee.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.stream.Collectors;

import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.TestAncestor;
import org.bgee.controller.exception.InvalidFormatException;
import org.bgee.controller.exception.InvalidRequestException;
import org.bgee.controller.user.User;
import org.bgee.controller.utils.MailSender;
import org.bgee.model.ServiceFactory;
import org.bgee.model.expressiondata.baseelements.DataQuality;
import org.bgee.model.expressiondata.baseelements.DataType;
import org.bgee.model.job.JobService;
import org.bgee.view.ViewFactory;
import org.junit.Test;

/**
 * Unit tests for {@link CommandParent}.
 * 
 * @author  Frederic Bastian
 * @version Bgee 13 Dec. 2015
 * @since   Bgee 13 Dec. 2015
 */
public class CommandParentTest extends TestAncestor {
    
    private final static Logger log = 
            LogManager.getLogger(CommandParentTest.class.getName());

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
    
    /**
     * Test method {@link CommandParent#checkAndGetDataTypes()}
     */
    @Test
    public void shouldCheckAndGetDataTypes() throws InvalidRequestException {

        HttpServletResponse response = mock(HttpServletResponse.class);
        ServletContext context = mock(ServletContext.class);
        
        RequestParameters params = new RequestParameters();
        log.info("Generated query URL: " + params.getRequestURL());
        //no data type parameter, should retrieve no data types
        FakeCommand command = new FakeCommand(response, params, BgeeProperties.getBgeeProperties(), 
                null, null, null, null, context, null);
        assertEquals("No data types should have been retrieved", null, command.checkAndGetDataTypes());
        
        //all parameters, should retrieve all data types
        params = new RequestParameters();
        params.addValues(params.getUrlParametersInstance().getParamDataType(), 
                Arrays.stream(DataType.class.getEnumConstants()).map(e -> e.name().toLowerCase())
                .collect(Collectors.toList()));
        log.info("Generated query URL: " + params.getRequestURL());
        command = new FakeCommand(response, params, BgeeProperties.getBgeeProperties(), 
                null, null, null, null, context, null);
        assertEquals("All data types should have been retrieved", EnumSet.allOf(DataType.class), 
                command.checkAndGetDataTypes());
        
        //ALL_VALUES, should retrieve all params
        params = new RequestParameters();
        params.addValue(params.getUrlParametersInstance().getParamDataType(), RequestParameters.ALL_VALUE);
        log.info("Generated query URL: " + params.getRequestURL());
        command = new FakeCommand(response, params, BgeeProperties.getBgeeProperties(), 
                null, null, null, null, context, null);
        assertEquals("All data types should have been retrieved", EnumSet.allOf(DataType.class), 
                command.checkAndGetDataTypes());
        
        //only 1 parameter
        params = new RequestParameters();
        params.addValues(params.getUrlParametersInstance().getParamDataType(), 
                Arrays.stream(DataType.class.getEnumConstants()).map(e -> e.name().toLowerCase())
                .limit(1)
                .collect(Collectors.toList()));
        log.info("Generated query URL: " + params.getRequestURL());
        command = new FakeCommand(response, params, BgeeProperties.getBgeeProperties(), 
                null, null, null, null, context, null);
        assertEquals("All data types should have been retrieved", 
                new HashSet<>(Arrays.asList(DataType.class.getEnumConstants()).subList(0, 1)), 
                command.checkAndGetDataTypes());
        
        //test incorrect param
        try {
            params = new RequestParameters();
            params.addValue(params.getUrlParametersInstance().getParamDataType(), "test");
            log.info("Generated query URL: " + params.getRequestURL());
            command = new FakeCommand(response, params, BgeeProperties.getBgeeProperties(), 
                    null, null, null, null, context, null);
            command.checkAndGetDataTypes();
            //test failed, an exception should have been thrown
            fail("Incorrect data type param should raise an exception.");
        } catch (InvalidFormatException|InvalidRequestException e) {
            //test passed
        }
    }
    /**
     * Test method {@link CommandParent#checkAndGetDataQuality()}
     */
    @Test
    public void shouldCheckAndGetDataQuality() throws InvalidRequestException {

        HttpServletResponse response = mock(HttpServletResponse.class);
        ServletContext context = mock(ServletContext.class);
        
        RequestParameters params = new RequestParameters();
        log.info("Generated query URL: " + params.getRequestURL());
        //no data quality parameter, should retrieve no data quality
        FakeCommand command = new FakeCommand(response, params, BgeeProperties.getBgeeProperties(), 
                null, null, null, null, context, null);
        assertEquals("No data quality should have been retrieved", null, command.checkAndGetDataQuality());

        //LOW quality
        params = new RequestParameters();
        params.addValue(params.getUrlParametersInstance().getParamDataQuality(), DataQuality.LOW.name().toLowerCase());
        log.info("Generated query URL: " + params.getRequestURL());
        command = new FakeCommand(response, params, BgeeProperties.getBgeeProperties(), 
                null, null, null, null, context, null);
        assertEquals("Low quality should have been retrieved", DataQuality.LOW, 
                command.checkAndGetDataQuality());
        
        //ALL quality
        params = new RequestParameters();
        params.addValue(params.getUrlParametersInstance().getParamDataQuality(), RequestParameters.ALL_VALUE);
        log.info("Generated query URL: " + params.getRequestURL());
        command = new FakeCommand(response, params, BgeeProperties.getBgeeProperties(), 
                null, null, null, null, context, null);
        assertEquals("Low quality should have been retrieved", DataQuality.LOW, 
                command.checkAndGetDataQuality());
        
        //HIGH quality
        params = new RequestParameters();
        params.addValue(params.getUrlParametersInstance().getParamDataQuality(), DataQuality.HIGH.name().toLowerCase());
        log.info("Generated query URL: " + params.getRequestURL());
        command = new FakeCommand(response, params, BgeeProperties.getBgeeProperties(), 
                null, null, null, null, context, null);
        assertEquals("High quality should have been retrieved", DataQuality.HIGH, 
                command.checkAndGetDataQuality());
        
        //test incorrect param
        try {
            params = new RequestParameters();
            params.addValue(params.getUrlParametersInstance().getParamDataQuality(), "test");
            log.info("Generated query URL: " + params.getRequestURL());
            command = new FakeCommand(response, params, BgeeProperties.getBgeeProperties(), 
                    null, null, null, null, context, null);
            command.checkAndGetDataQuality();
            //test failed, an exception should have been thrown
            fail("Incorrect data quality param should raise an exception.");
        } catch (InvalidFormatException|InvalidRequestException e) {
            //test passed
        }
    }

    /**
     * Test the method {@code CommandParent#launchFileDownload(String, String)}.
     */
    @Test
    public void shouldLaunchFileDownload() throws IOException {
        String filePath = this.getClass().getResource("/controller/test.zip").getPath();
        File file = new File(filePath);
        
        
        HttpServletResponse response = mock(HttpServletResponse.class);
        ServletOutputStream outputStream = mock(ServletOutputStream.class);
        when(response.getOutputStream()).thenReturn(outputStream);
        
        ServletContext context = mock(ServletContext.class);
        when(context.getMimeType(filePath)).thenReturn("application/zip");
        
        FakeCommand command = new FakeCommand(response, null, BgeeProperties.getBgeeProperties(), 
                null, null, null, null, context, null);
        command.launchFileDownload(filePath, "myFile");
        
        verify(response).setContentType("application/zip");
        verify(response).setContentLength((int) file.length());
        verify(response).setHeader("Content-Disposition", "attachment; filename=\"myFile\"");

        FileInputStream inStream = new FileInputStream(file);
        byte[] buffer = new byte[4096];
        int bytesRead = -1;
        while ((bytesRead = inStream.read(buffer)) != -1) {
            verify(outputStream).write(buffer, 0, bytesRead);
        }
        inStream.close();
        verify(outputStream).close();
    }
}
