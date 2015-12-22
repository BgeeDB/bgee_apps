package org.bgee.controller;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.TestAncestor;
import org.bgee.controller.utils.MailSender;
import org.bgee.model.ServiceFactory;
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

    /**
     * Test the method {@code CommandParent#launchFileDownload(String, String)}.
     */
    @Test
    public void shouldLaunchFileDownload() throws IOException {
        String filePath = this.getClass().getResource("/controller/test.zip").getPath();
        File file = new File(filePath);
        
        class FakeCommand extends CommandParent {

            public FakeCommand(HttpServletResponse response, RequestParameters requestParameters, 
                    BgeeProperties prop, ViewFactory viewFactory, ServiceFactory serviceFactory, 
                    ServletContext context, MailSender mailSender) {
                super(response, requestParameters, prop, viewFactory, serviceFactory, context, mailSender);
            }

            @Override
            public void processRequest() throws Exception {
                //nothing here
            }
            
        }
        
        HttpServletResponse response = mock(HttpServletResponse.class);
        ServletOutputStream outputStream = mock(ServletOutputStream.class);
        when(response.getOutputStream()).thenReturn(outputStream);
        
        ServletContext context = mock(ServletContext.class);
        when(context.getMimeType(filePath)).thenReturn("application/zip");
        
        FakeCommand command = new FakeCommand(response, null, BgeeProperties.getBgeeProperties(), 
                null, null, context, null);
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
