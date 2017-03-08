package org.bgee.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.exception.InvalidRequestException;
import org.bgee.controller.user.User;
import org.bgee.controller.utils.MailSender;
import org.bgee.model.BgeeEnum;
import org.bgee.model.ServiceFactory;
import org.bgee.model.expressiondata.baseelements.DataType;
import org.bgee.model.expressiondata.baseelements.SummaryQuality;
import org.bgee.model.job.JobService;
import org.bgee.view.ViewFactory;

/**
 * Parent class of all controllers. It achieves operations that are common to all subclasses, 
 * and defines methods that subclasses must implement.
 * 
 * Notably, it defines the abstract method {@code processRequest}, 
 * that is the main method of all controllers, 
 * launching actions on the {@code model} layer, 
 * and display from the {@code view} layer.
 * <p>
 * This class is not instantiable, as all concrete controllers must provide at least 
 * their own implementation of {@code processRequest()}.
 * 
 * @author 	Frederic Bastian
 * @author  Mathieu Seppey
 * @author  Valentine Rech de Laval
 * @version Bgee 14, Mar. 2017
 * @see 	#processRequest()
 * @since 	Bgee 1
 *
 */
abstract class CommandParent {
    
    private final static Logger log = LogManager.getLogger(CommandParent.class.getName());

    /**
     * Concrete factory providing classes from the {@code view} package. 
     * This concrete factory implements the {@code ViewFactory} interface.
     */
    protected final ViewFactory viewFactory;
    protected final ServiceFactory serviceFactory;
    /**
     * The {@code JobService} instance allowing to manage jobs between threads 
     * across the entire webapp. 
     */
    protected final JobService jobService;
    /**
     * The {@code User} who is making the query to the webapp. 
     */
    protected final User user;
    /**
     * The {@code MailSender} used to send emails.
     */
    protected final MailSender mailSender;
    
    protected final HttpServletResponse response;
    /**
     * The {@code ServletContext} of the servlet using this object.
     */
    protected final ServletContext context;
    /**
     * Stores the parameters of the current request.
     */
    protected final RequestParameters requestParameters;

    protected final String serverRoot;
    protected final String homePage;
    protected final String bgeeRoot;

    /**
     * A {@code BgeeProperties} instance that contains the properties
     * to use.
     */
    protected BgeeProperties prop;

    /**
     * Constructor
     * 
     * @param response          A {@code HttpServletResponse} that will be used to display the 
     *                          page to the client
     * @param requestParameters The {@code RequestParameters} that handles the parameters of the 
     *                          current request.
     * @param prop              A {@code BgeeProperties} instance that contains the properties
     *                          to use.
     * @param viewFactory       A {@code ViewFactory} that provides the display type to be used.
     * @param serviceFactory    A {@code ServiceFactory} that provides the services (might be null)
     */
    public CommandParent(HttpServletResponse response, RequestParameters requestParameters,
                         BgeeProperties prop, ViewFactory viewFactory, ServiceFactory serviceFactory) {
        this(response, requestParameters, prop, viewFactory, serviceFactory, null, null, null, null);
    }

    /**
     * Constructor. This constructor doesn't provide a {@code ServiceFactory}
     *
     * @param response          A {@code HttpServletResponse} that will be used to display the
     *                          page to the client
     * @param requestParameters The {@code RequestParameters} that handles the parameters of the
     *                          current request.
     * @param prop              A {@code BgeeProperties} instance that contains the properties
     *                          to use.
     * @param viewFactory       A {@code ViewFactory} that provides the display type to be used.
     */
    public CommandParent(HttpServletResponse response, RequestParameters requestParameters,
                         BgeeProperties prop, ViewFactory viewFactory) {
        this(response, requestParameters, prop, viewFactory, null);
    }

    /**
     * Constructor
     * 
     * @param response          A {@code HttpServletResponse} that will be used to display the 
     *                          page to the client
     * @param requestParameters The {@code RequestParameters} that handles the parameters of the 
     *                          current request.
     * @param prop              A {@code BgeeProperties} instance that contains the properties
     *                          to use.
     * @param viewFactory       A {@code ViewFactory} that provides the display type to be used.
     * @param serviceFactory    A {@code ServiceFactory} that provides the services (might be null)
     * @param jobService        A {@code JobService} instance allowing to manage jobs between threads 
     *                          across the entire webapp.
     * @param user              The {@code User} who is making the query to the webapp (might be null).
     * @param context           The {@code ServletContext} of the servlet using this object. 
     *                          Notably used when forcing file download.
     * @param mailSender        A {@code MailSender} instance used to send mails to users.
     */
    public CommandParent(HttpServletResponse response, RequestParameters requestParameters,
                         BgeeProperties prop, ViewFactory viewFactory, ServiceFactory serviceFactory, 
                         JobService jobService, User user, ServletContext context, MailSender mailSender) {
        log.entry(response, requestParameters, prop, viewFactory, serviceFactory, jobService, 
                user, context, mailSender);
        this.response = response;
        this.context = context;
        this.requestParameters = requestParameters;
        this.prop = prop;
        this.viewFactory = viewFactory;
        this.serviceFactory = serviceFactory;
        this.jobService = jobService;
        this.user = user;
        this.mailSender = mailSender;
        this.serverRoot = prop.getBgeeRootDirectory();
        this.homePage   = prop.getBgeeRootDirectory();
        this.bgeeRoot   = prop.getBgeeRootDirectory();
        log.exit();
    }

    /**
     * This method is responsible for pre-processing a request, 
     * and returns {@code true} if the application can continue 
     * and call the {@code processRequest()} method.
     * 
     * A pre-processing step is, for instance, redirecting to another page, 
     * filtering a parameter... In the case of a redirection, the method should 
     * return {@code false} so that the application is aware that 
     * the request should not be processed further.
     * <p>
     * Concrete controllers can override this method to provide 
     * their own pre-processing, specific to their domain of action. 
     * It is recommended that they call the parent method in their own implementation, 
     * unless their pre-processing steps overlap (for instance, performing a redirection 
     * in the concrete controller, while this parent controller also perform a redirection). 
     * 
     * @return 	{@code true} if the request can be further processed, 
     * 			{@code false} otherwise ({@code processRequest()} should then not been called).
     * @throws 	Exception 	any exception not-caught during the process of the request 
     * 						is thrown to be caught by the {@code FrontController}.
     * @see 	#processRequest()
     * @see 	FrontController#doRequest(HttpServletRequest, HttpServletResponse, boolean)
     */
    public boolean preprocessRequestAndCheckIfContinue() {
        log.entry();
        return log.exit(false);
    }

    /**
     * Main method of all controllers, responsible for analyzing the query, 
     * and triggering the appropriate actions on the {@code model} layer 
     * and the {@code view} layer.
     * 
     * Each concrete controller must provide its own implementation of this method.
     * 
     * @throws  Exception 	any exception not-caught during the process of the request 
     * 						is thrown to be caught by the {@code FrontController}.
     * @see 	FrontController#doRequest(HttpServletRequest, HttpServletResponse, boolean)
     */
    public abstract void processRequest() throws Exception;
    
    /**
     * Trigger the download of a file, from the client point of view. This is useful 
     * when the files to send are stored on the webapp server, but not within the webapp directory. 
     * <p>
     * The response output stream will be closed following a call to this method.
     * <p>
     * Code from http://www.codejava.net/java-ee/servlet/java-servlet-download-file-example. 
     * 
     * @param filePath          A {@code String} that is the path to a file to send to the client.
     * @param downloadFileName  A {@code String} that is the name of the file, as displayed to the client.
     * @throws IOException      If the file is not found, or {@code downloadFileName} could not be encoded 
     *                          in UTF-8, or the response output stream could not be obtained. 
     */
    protected void launchFileDownload(String filePath, String downloadFileName) throws IOException {
        log.entry(filePath, downloadFileName);
        
        File downloadFile = new File(filePath);
        FileInputStream inStream = new FileInputStream(downloadFile);

        // gets MIME type of the file
        String mimeType = context.getMimeType(filePath);
        if (mimeType == null) {        
            // set to binary type if MIME mapping not found
            mimeType = "application/octet-stream";
        }
        log.debug("MIME type of download file: {}", mimeType);
        
        // modifies response
        response.setContentType(mimeType);
        response.setContentLength((int) downloadFile.length());
        response.setHeader("Content-Disposition", "attachment; filename=\"" 
            + URLEncoder.encode(downloadFileName, "UTF-8") + '"');
        
        // obtains response's output stream
        OutputStream outStream = response.getOutputStream();
         
        byte[] buffer = new byte[4096];
        int bytesRead = -1;
         
        while ((bytesRead = inStream.read(buffer)) != -1) {
            outStream.write(buffer, 0, bytesRead);
        }
         
        inStream.close();
        outStream.close();
        
        log.exit();
    }
    
    /**
     * Check and retrieve the data types requested in the {@code RequestParameters} object 
     * provided at instantiation. 
     * 
     * @return  A {@code Set} of {@code DataType}s retrieved from the request parameters.
     * @throws InvalidRequestException  If the data type request parameter is incorrectly used.
     */
    protected Set<DataType> checkAndGetDataTypes() throws InvalidRequestException {
        log.entry();
        
        List<String> rqDatatypes  = this.requestParameters.getDataType();
        if (rqDatatypes != null && 
                rqDatatypes.size() == 1 && rqDatatypes.get(0).equals(RequestParameters.ALL_VALUE)) {
            return log.exit(EnumSet.allOf(DataType.class));
        }
        if (rqDatatypes != null && !BgeeEnum.areAllInEnum(DataType.class, rqDatatypes)) {
            throw log.throwing(new InvalidRequestException("Incorrect data types provided: "
                    + rqDatatypes));
        }
        return log.exit(DataType.convertToDataTypeSet(rqDatatypes));
    }
    /**
     * Check and retrieve the summary quality requested in the {@code RequestParameters} object 
     * provided at instantiation. 
     * 
     * @return  A {@code DataQuality} retrieved from the request parameters.
     * @throws InvalidRequestException  If the summary quality request parameter is incorrectly used.
     */
    protected SummaryQuality checkAndGetDataQuality() throws InvalidRequestException {
        log.entry();
        
        String rqDataQual = this.requestParameters.getDataQuality();
        SummaryQuality dataQuality = null; 
        if (rqDataQual != null) {
            if (rqDataQual.equalsIgnoreCase(SummaryQuality.GOLD.name())) {
                dataQuality = SummaryQuality.GOLD;
            } else if (rqDataQual.equalsIgnoreCase(SummaryQuality.SILVER.name()) || 
                    rqDataQual.equalsIgnoreCase(RequestParameters.ALL_VALUE)) {
                dataQuality = SummaryQuality.SILVER;
            } else {
                throw log.throwing(new InvalidRequestException("Incorrect data quality provided: "
                        + rqDataQual));
            }
        }
        return log.exit(dataQuality);
    }

}
