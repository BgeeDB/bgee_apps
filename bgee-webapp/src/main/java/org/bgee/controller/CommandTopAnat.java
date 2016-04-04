package org.bgee.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.mail.MessagingException;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.exception.InvalidRequestException;
import org.bgee.controller.exception.PageNotFoundException;
import org.bgee.controller.utils.MailSender;
import org.bgee.model.BgeeEnum;
import org.bgee.model.ServiceFactory;
import org.bgee.model.TaskManager;
import org.bgee.model.anatdev.DevStage;
import org.bgee.model.dao.api.DAOManager;
import org.bgee.model.expressiondata.baseelements.CallType;
import org.bgee.model.expressiondata.baseelements.DataQuality;
import org.bgee.model.expressiondata.baseelements.DataType;
import org.bgee.model.expressiondata.baseelements.DecorrelationType;
import org.bgee.model.gene.Gene;
import org.bgee.model.species.Species;
import org.bgee.model.topanat.TopAnatController;
import org.bgee.model.topanat.TopAnatParams;
import org.bgee.model.topanat.TopAnatResults;
import org.bgee.model.topanat.exception.MissingParameterException;
import org.bgee.view.TopAnatDisplay;
import org.bgee.view.ViewFactory;

/**
 * Controller handling requests related to topAnat.
 * 
 * @author  Frederic Bastian
 * @author  Valentine Rech de Laval
 * @version Bgee 13 Dec. 2015
 * @since   Bgee 13
 */
public class CommandTopAnat extends CommandParent {
    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = LogManager.getLogger(CommandTopAnat.class.getName());
    
    /**
     * Class responsible for launching a TopAnat job, and for sending a mail to the user 
     * on completion.
     * 
     * @author  Frederic Bastian
     */
    private static class TopAnatJobRunner implements Runnable {

        /**
         * A {@code List} of {@code TopAnatParams} defining the analyses to launch.
         */
        private final List<TopAnatParams> topAnatParams;
        /**
         * A {@code String} that is the URL to link to the results.
         */
        private final String resultUrl;
        /**
         * A {@code String} that is the description of the job.
         */
        private final String jobTitle;
        /**
         * An {@code int} that is the ID of the job, to register to {@code TaskManager}.
         */
        private final long jobId;
        /**
         * A {@code String} that is the creation date of the job, as formatted on the client environment.
         */
        private final String jobCreationDate;
        /**
         * A {@code String} that is the email address of the user, to be notified on job completion.
         */
        private final String sendToAddress;
        
        /**
         * A {@code BgeeProperties} defining parameters, notably to send mails.
         */
        private final BgeeProperties props;
        /**
         * A {@code Supplier} of {@code ServiceFactory}s.
         */
        private final Supplier<ServiceFactory> serviceFactoryProvider;
        /**
         * A {@code MailSender} to send mails on completion.
         */
        private final MailSender mailSender;
        
        public TopAnatJobRunner(List<TopAnatParams> topAnatParams, String resultUrl, 
                String jobTitle, long jobId, String jobCreationDate, String sendToAddress, 
                BgeeProperties props, MailSender mailSender, Supplier<ServiceFactory> serviceFactoryProvider) {
            log.entry(topAnatParams, resultUrl, jobTitle, jobId, jobCreationDate, sendToAddress, 
                    props, mailSender, serviceFactoryProvider);
            this.topAnatParams = topAnatParams;
            
            this.resultUrl = resultUrl;
            this.jobTitle = jobTitle;
            this.jobId = jobId;
            this.jobCreationDate = jobCreationDate;
            this.sendToAddress = sendToAddress;
            
            this.props = props;
            this.mailSender = mailSender;
            this.serviceFactoryProvider = serviceFactoryProvider;
            log.exit();
        }
        
        @Override
        public void run() {
            log.entry();
            
            long startTimeInMs = System.currentTimeMillis();
            Exception exceptionThrown = null;
            int resultCount = 0;
            int analysesWithResultCount = 0;
            boolean sendMail = this.mailSender != null && StringUtils.isNotBlank(this.sendToAddress);
            try {
                log.trace("Executor thread ID: {}", Thread.currentThread().getId());
                //Now, the problem is, we cannot register the TaskManager from the launching thread. 
                //And maybe the jobId will be "stolen" by another thread before this one use it. 
                //But to avoid this we would need a lock mechanism, 
                //and the probability of having 2 same jobIds generated by two different threads 
                //at the same time is low. An error will be thrown anyway if it happens.
                //TODO: we really need to get rid of all these methods returning "per-thread" singletons, 
                //now that we always inject our dependencies. This way, we could acquire 
                //the TaskManager from the launching Thread already.
                //TODO: in task manager, we should have kind of an "atomic" method, like 
                //getTaskManagerIfNotAssigned(long).
                
                //acquire the TaskManager
                if (TaskManager.getTaskManager(this.jobId) != null) {
                    throw log.throwing(new IllegalStateException("Job ID stolen!"));
                }
                //again, there is no atomicity here, but that's good enough
                TaskManager.registerTaskManager(this.jobId);
                TaskManager manager = TaskManager.getTaskManager();
                
                log.debug("Launching TopAnat job");
                TopAnatController controller2 = new TopAnatController(this.topAnatParams, 
                        this.props, 
                        //For properly loading the ServiceFactory, 
                        //we need to acquire a different DAOManager than the one used 
                        //by the launching thread, because it will be closed when the thread terminates. 
                        //The supplier used should take of calling DAOManager.getDAOManager(), 
                        //e.g., new ServiceFactory(DAOManager.getDAOManager()).
                        //FIXME: but we should have a way to "clone" the serviceFactory, 
                        //we have no guarantee that the DAOManager will have the same configuration here, 
                        //and we don't use the ServiceFactory injected to CommandTopAnat...
                        this.serviceFactoryProvider.get(), 
                        manager);
                
                //we need to consume the Stream to actually launch the analysis. 
                //Also, if it is requested to send an email to the user, we count number of results, 
                //otherwise it is not needed.
                Set<TopAnatResults> results = controller2.proceedToTopAnatAnalyses()
                        .collect(Collectors.toSet());
                if (sendMail) {
                    for (TopAnatResults result: results) {
                        int rowCount = result.getRows().size();
                        resultCount += rowCount;
                        if (rowCount > 0) {
                            analysesWithResultCount++;
                        }
                    }
                    log.trace("{} results in {} analyses", resultCount, analysesWithResultCount);
                }
            } catch (Exception e) {
                log.catching(e);
                exceptionThrown = e;
                //The exception will be thrown after sending the mail. We cannot simply 
                //send the mail in the finally clause, otherwise we won't have access to this exception.
            } finally {
                try {
                    DAOManager daoManager = DAOManager.getDAOManager();
                    if (daoManager != null) {
                        daoManager.close();
                    }
                } finally {
                    TaskManager manager = TaskManager.getTaskManager();
                    if (manager != null) {
                        manager.release();
                    }
                }
            }
            log.debug("TopAnat job completed.");
            
            if (sendMail) {
                try {
                    log.debug("Sending mail on job completion...");
                    this.sendMail(startTimeInMs, this.jobCreationDate, 
                            resultCount, analysesWithResultCount, exceptionThrown);
                    log.debug("Mail sent.");
                } catch (UnsupportedEncodingException | MessagingException | InterruptedException e) {
                    log.catching(e);
                    log.error("Unable to send mail");
                    //we throw this exception only if there was not already an error during the analyses
                    if (exceptionThrown == null) {
                        throw log.throwing(new IllegalStateException(e));
                    }
                }
            }
            
            //if there was an error during the analyses, we throw it after sending the mail
            if (exceptionThrown != null) {
                throw log.throwing(new IllegalStateException(exceptionThrown));
            }
            
            log.exit();
        }
        
        /**
         * Send mail to user on job completion.
         * 
         * @param startTimeInMs                 An {@code long} that is the time in ms 
         *                                      when the job was started.
         * @param jobCreationDate               A {@code String} that is the creation date of the job, 
         *                                      as formatted on the client environment. Can be {@code null} 
         *                                      or empty.
         * @param resultCount                   An {@code int} that is the number of significant results 
         *                                      from the job.
         * @param analysesWithResultCount       An {@code int} that is the number of analyses with results
         *                                      from this job.
         * @param exceptionThrown               An {@code Exception} potentially thrown during 
         *                                      the analyses. {@code null} if no error occurred. 
         * @throws UnsupportedEncodingException If the mail parameters used have incorrect encoding.
         * @throws MessagingException           If an error occurred while sending the mail.
         * @throws SendFailedException          If the mail was not delivered to the recipient.
         * @throws InterruptedException         If this Thread was interrupted while waiting 
         *                                      for the permission to send a mail (anti-flood system).
         */
        private void sendMail(long startTimeInMs, String jobCreationDate, 
                int resultCount, int analysesWithResultCount, Exception exceptionThrown) 
                        throws UnsupportedEncodingException, MessagingException, InterruptedException {
            log.entry(startTimeInMs, jobCreationDate, resultCount, analysesWithResultCount, exceptionThrown);
            assert this.mailSender != null && StringUtils.isNotBlank(this.sendToAddress): "Cannot send mail";
            
            //build mail subject
            StringBuilder sb = new StringBuilder();
            sb.append("TopAnat analysis ");
            if (exceptionThrown == null) {
                sb.append("successful: ");
            } else {
                sb.append("failed! - ");
            }
            if (StringUtils.isNotBlank(this.jobTitle)) {
                sb.append(this.jobTitle + " - ");
            } else {
                sb.append("job ");
            }
            
            if (StringUtils.isNotBlank(jobCreationDate)) {
                log.trace("Creation date: {}", jobCreationDate);
                sb.append("started on ").append(jobCreationDate);
            } else {
                long duration = System.currentTimeMillis() - startTimeInMs;
                log.trace("Duration: {}", duration);
                String timeFormat = "HHhmm";
                if (duration < 3600000) {
                    timeFormat = "mm";
                }
                String formattedDuration = DurationFormatUtils.formatDuration(duration, timeFormat);
                log.trace("Formatted duration: {}", formattedDuration);
                if (duration < 60000) {
                    sb.append("started a minute ago");
                } else {
                    sb.append("started ").append(formattedDuration).append(" mn ago");
                }
            }
            String subject = sb.toString();
            
            //build mail body
            sb = new StringBuilder();
            sb.append("Hello, \n\nYour analysis ");
            if (StringUtils.isNotBlank(this.jobTitle)) {
                sb.append("with description \"").append(this.jobTitle).append("\" ");
            }
            if (exceptionThrown == null) {
                sb.append("was ");
                //don't say successfully when there is no result :p
                if (resultCount > 0) {
                    sb.append("successfully ");
                }
                sb.append("completed. \n");
                
                sb.append("Found ").append(resultCount).append(" significant result");
                if (resultCount > 1) {
                    sb.append("s");
                }
                sb.append(", from ").append(analysesWithResultCount);
                if (analysesWithResultCount > 1) {
                    sb.append(" analyses");
                } else {
                    sb.append(" analysis");
                }
                sb.append(" with results, over ").append(this.topAnatParams.size());
                if (this.topAnatParams.size() > 1) {
                    sb.append(" analyses");
                } else {
                    sb.append(" analysis");
                }
                sb.append(" launched. \n\n");
                
                sb.append("You can visualize your analysis by using the following link: ")
                  .append(this.resultUrl).append("\n\n");
                sb.append("We hope that you appreciate TopAnat, and we thank you for using it. ")
                  .append("Do not hesitate to contact us, should you have any questions. \n");
                sb.append("Best regards, \n\n");
                sb.append("The Bgee team.");
            } else {
                sb.append("was interrupted by an unexpected error (details follow). ")
                  .append("We apologize for any inconvenience. You can try to submit ")
                  .append("your analysis again by using the following link: ")
                  .append(this.resultUrl).append(". ")
                  .append("If the error persists, please do not hesitate to contact us ")
                  .append("by replying to this message. \n\n");
                sb.append("Best regards, \n");
                sb.append("The Bgee team. \n\n");
                
                sb.append("Details about the error: \n");
                StringWriter sw = new StringWriter();
                exceptionThrown.printStackTrace(new PrintWriter(sw));
                sb.append(sw.toString());
            }
            String msgBody = sb.toString();
            
            this.mailSender.sendMessage(this.props.getTopAnatFromAddress(), 
                    this.props.getTopAnatFromPersonal(), this.sendToAddress, null, subject, msgBody);
            
            log.exit();
        }
    }
    
    /**
     * An {@code int} that is the level to be used to filter retrieved dev. stages. 
     */
    private final static int DEV_STAGE_LEVEL = 2;
    
    /**
     * A {@code String} that is the label of the count of genes whose the species is undetermined. 
     */
    private final static String UNDETERMINED_SPECIES_LABEL = "UNDETERMINED";

    /**
     * A {@code String} that is the label of the job response. 
     */
    private final static String JOB_RESPONSE_LABEL = "jobResponse";
    
    
    /**
     * An {@code enum} defining the job status. 
     * 
     * @author  Valentine Rech de Laval
     * @version Bgee 13 Nov. 2015
     * @since   Bgee 13 Nov. 2015
     */
    private static enum JobStatus {
        UNDEFINED, RUNNING;
    }
    
    
    /**
     * A {@code List} of {@code String}s that are messages to be displayed.
     */
    private static List<String> messages;

    /**
     * Constructor providing necessary dependencies. 
     * 
     * @param response          A {@code HttpServletResponse} that will be used 
     *                          to display the page to the client
     * @param requestParameters The {@code RequestParameters} that handles 
     *                          the parameters of the current request.
     * @param prop              A {@code BgeeProperties} instance that contains 
     *                          the properties to use.
     * @param viewFactory       A {@code ViewFactory} providing the views of the appropriate 
     *                          display type.
     * @param serviceFactory    A {@code ServiceFactory} that provides bgee services.
     * @param context           The {@code ServletContext} of the servlet using this object. 
     *                          Notably used when forcing file download.
     * @param mailSender        A {@code MailSender} instance used to send mails to users.
     */
    public CommandTopAnat(HttpServletResponse response, RequestParameters requestParameters, 
            BgeeProperties prop, ViewFactory viewFactory, ServiceFactory serviceFactory, 
            ServletContext context, MailSender mailSender) {
        super(response, requestParameters, prop, viewFactory, serviceFactory, context, mailSender);
        messages = new ArrayList<>();
    }

    @Override
    public void processRequest() throws IOException, PageNotFoundException, InvalidRequestException,
            MissingParameterException {
        log.entry();
        
        //we initialize the display only if there is no file to download requested, 
        //otherwise we could not obtain the response outputstream to directly print 
        //the file to send to it.
        TopAnatDisplay display = null;
        if (!this.requestParameters.isATopAnatDownloadFile()) {
            display = this.viewFactory.getTopAnatDisplay();
        }
        
        // Gene list validation 
        if (this.requestParameters.isATopAnatGeneUpload()) {
            
            // Get gene responses
            LinkedHashMap<String, Object> data = this.getGeneResponses();

            // Send response
            display.sendGeneListReponse(data, messages.stream().collect(Collectors.joining("\n")));

        // New job submission
        } else if (this.requestParameters.isATopAnatSubmitJob()) {
            
            //first, we need to know whether the results already exist, to be able to immediately 
            //send a "job's done" response. 
            JobStatus status = JobStatus.RUNNING;
            long jobId = 0;
            final TopAnatController controller = this.loadTopAnatController();
            if (controller.areAnalysesDone()) {
                log.debug("Results already exist.");
                status = JobStatus.UNDEFINED;
                
            } else {
                log.debug("Results don't exist, launching job");
                
                //generate result URL, for sending it to the user
                RequestParameters resultParams = new RequestParameters();
                resultParams.setPage(RequestParameters.PAGE_TOP_ANAT);
                resultParams.setURLHash("/result/" + this.requestParameters.getDataKey());
                final String resultUrl = resultParams.getRequestURL();
                
                //OK, we need to launch the analyses. 
                //launch analysis in a different thread, otherwise the response will not be sent.
                //And we need to provide it with a fresh DAOManager, because this Thread will close it.
                //And we assign an ID to the TaskManager from this thread, otherwise we won't be able 
                //to know the ID to return it.
                //First, generate job ID. We use the current thread ID as "stem" (we can't simply 
                //use this threadId, because maybe another thread has been assigned with it already), 
                //we multiply using current time stamp, as job can be canceled based on their IDs
                jobId = Thread.currentThread().getId() * (System.currentTimeMillis() % 100);
                int i = 0;
                while (TaskManager.getTaskManager(jobId) != null && 
                        i <= 10000) {//10000 max tries
                    jobId = jobId * 3;
                    i++;
                }
                final long finalJobId = jobId;
                log.trace("Job ID: {}" + finalJobId);
                
                //Now, the problem is, we cannot register the TaskManager from this thread, 
                //because of the stupid design "per-thread singleton". 
                //And maybe the jobId will be "stolen" by another thread before the one we are going 
                //to launch use it. But to avoid this we would need a lock mechanism, 
                //and the probability of having 2 same jobIds generated by two different threads 
                //at the same time is low. An error will be thrown anyway if it happens.
                //TODO: we really need to get rid of all these methods returning "per-thread" singletons, 
                //now that we always inject our dependencies. This way, we could acquire 
                //the TaskManager from this Thread already.
                //TODO: in task manager, we should have kind of an "atomic" method, like 
                //getTaskManagerIfNotAssigned(long). 
                
                Thread newThread = new Thread(new TopAnatJobRunner(controller.getTopAnatParams(), 
                        resultUrl, this.requestParameters.getFirstValue(
                                this.requestParameters.getUrlParametersInstance().getParamJobTitle()), 
                        finalJobId, this.requestParameters.getFirstValue(
                                this.requestParameters.getUrlParametersInstance().getParamJobCreationDate()), 
                        this.requestParameters.getFirstValue(
                                this.requestParameters.getUrlParametersInstance().getParamEmail()), 
                        this.prop, this.mailSender, 
                        //Also, for properly loading the ServiceFactory, 
                        //we need to acquire a different DAOManager than the one used 
                        //by the launching thread, because it will be closed when the thread terminates. 
                        //The supplier used should take of calling DAOManager.getDAOManager(), 
                        //e.g., new ServiceFactory(DAOManager.getDAOManager()).
                        //FIXME: but we should have a way to "clone" the serviceFactory, 
                        //we have no guarantee that the DAOManager will have the same configuration here, 
                        //and we don't use the ServiceFactory injected to CommandTopAnat...
                        () -> new ServiceFactory(DAOManager.getDAOManager())));
                
                newThread.start();
            }
            
            // Job ID if available, add hash
            String message = "Job is " + status.name();
            if (jobId == 0) {
                message = "The results already exist.";
            }
            LinkedHashMap<String, Object> data = new LinkedHashMap<>();
            data.put(JOB_RESPONSE_LABEL, new JobResponse(
                    jobId, status.name(), this.requestParameters.getDataKey()));
            display.sendTrackingJobResponse(data, message);
            

        // Job tracking
        } else if (this.requestParameters.isATopAnatTrackingJob()) {
            //TODO: use the more detailed information provided by the TaskManager. 
            //Maybe we can even actually provide the TaskManager in JSON?
            // Get params
            Integer jobID = this.requestParameters.getJobId(); 
            String keyParam = this.requestParameters.getDataKey(); 
            
            if (jobID == null || jobID < 1) {
                throw log.throwing(new InvalidRequestException("A job ID must be provided"));
            }
            
            // Retrieve task manager associated to the provided ID
            TaskManager taskManager = TaskManager.getTaskManager(jobID);
            JobStatus jobStatus = JobStatus.UNDEFINED;
            if (taskManager != null && !taskManager.isTerminated()) {
                jobStatus = JobStatus.RUNNING;
            } 
            LinkedHashMap<String, Object> data = new LinkedHashMap<>();

            data.put(JOB_RESPONSE_LABEL, new JobResponse(jobID, jobStatus.name(), keyParam));

            if (this.requestParameters.getGeneInfo() != null &&
                    this.requestParameters.getGeneInfo()) {
                data.putAll(this.getGeneResponses());
            }
            display.sendTrackingJobResponse(data, "Job is " + jobStatus.name());

        // Get results
        } else if (this.requestParameters.isATopAnatGetResult()) {
            
            TopAnatController controller = this.loadTopAnatController();
            if (!controller.areAnalysesDone()) {
                throw log.throwing(new InvalidRequestException(
                        "No results available for the provided parameters."));
            }
            Stream<TopAnatResults> topAnatResults = controller.proceedToTopAnatAnalyses();

            LinkedHashMap<String, Object> data = new LinkedHashMap<>();
            if (this.requestParameters.getGeneInfo() != null &&
                    this.requestParameters.getGeneInfo()) {
                data.putAll(this.getGeneResponses());
            }

            //FIXME: I don't know why, but I don't manage to make the Stream to be printed.
            //Collecting the results while waiting for a fix.
            data.put("topAnatResults", topAnatResults.collect(Collectors.toSet()));
            display.sendResultResponse(data, "");

            
         // Download result zip file
        } else if (this.requestParameters.isATopAnatDownloadFile()) {
            
            this.launchExportDownload();
            
        // Home page, empty
        } else if (this.requestParameters.getAction() == null) {
            display.displayTopAnatHomePage();
            
        } else {
            throw log.throwing(new PageNotFoundException("Incorrect " 
                + this.requestParameters.getUrlParametersInstance().getParamAction() 
                + " parameter value."));
        }

        log.exit();
    }
    
    /**
     * Launch the downloading of a TopAnat result files from the server to the client.
     * 
     * @throws InvalidRequestException
     * @throws MissingParameterException
     * @throws UnsupportedEncodingException
     * @throws IOException
     */
    private void launchExportDownload() throws InvalidRequestException, MissingParameterException, 
        UnsupportedEncodingException, IOException {
        log.entry();
        
        final TopAnatController controller = this.loadTopAnatController();
        if (!controller.areAnalysesDone()) {
            throw log.throwing(new InvalidRequestException(
                    "No results available for the provided parameters."));
        }
        List<TopAnatResults> topAnatResults = controller.proceedToTopAnatAnalyses()
                .collect(Collectors.toList());
        Function<TopAnatResults, Path> generateFilePath = result -> 
            Paths.get(controller.getBgeeProperties().getTopAnatResultsWritingDirectory(), 
                result.getResultDirectory(), result.getZipFileName());
        Function<TopAnatResults, String> generateFileName = result -> 
            result.getTopAnatParams().toString("_", "_", false)
            .replace(' ', '_').replace(':', '_') 
            + "_" + result.getTopAnatParams().getKey() + ".zip";
            
        String globalFileName = "";
        String jobTitle = this.requestParameters.getFirstValue(
                this.requestParameters.getUrlParametersInstance().getParamJobTitle());
        if (StringUtils.isNotBlank(jobTitle)) {
            globalFileName += jobTitle.replace(' ', '_') + "_";
        }
        globalFileName += this.requestParameters.getDataKey() + ".zip";
        
        //If several results are requested, then we will generate a zip of the result zips, 
        //that will be directly printed to the response outputstream. 
        //Otherwise, we simply send the already existing zip file of one analysis result
        String requestedAnalysisId = this.requestParameters.getFirstValue(
                this.requestParameters.getUrlParametersInstance().getParamAnalysisId());
        
        if (StringUtils.isNotBlank(requestedAnalysisId) || topAnatResults.size() == 1) {
            for (TopAnatResults result: topAnatResults) {
                if (topAnatResults.size() != 1 && 
                        !result.getTopAnatParams().getKey().equals(requestedAnalysisId)) {
                    continue;
                }
                String filePath = generateFilePath.apply(result).toString();
                String fileName = URLEncoder.encode(generateFileName.apply(result), "UTF-8");
                //if there is only one result, and the user gave a title to its analysis, 
                //then this is what we use. 
                if (topAnatResults.size() == 1 && StringUtils.isNotBlank(jobTitle)) {
                    fileName = globalFileName;
                }
                this.launchFileDownload(filePath, fileName);
                //OK, we stop here, we have sent the requested file
                log.exit(); return;
            }
        }
        
        //If several analysis results are requested:  
        //update response for sending the file to the client. We will write directly 
        //to the response outputstream. 
        response.setContentType("application/zip");
        //we don't send the length to not have to first generate the file
        //response.setContentLength((int) downloadFile.length());
        response.setHeader("Content-Disposition", "attachment; filename=\"" 
            + URLEncoder.encode(globalFileName, "UTF-8") + '"');

        // create byte buffer
        byte[] buffer = new byte[1024];
        ZipOutputStream zos = new ZipOutputStream(this.response.getOutputStream());

        for (TopAnatResults result: topAnatResults) {
            File srcFile = generateFilePath.apply(result).toFile();
            FileInputStream fis = new FileInputStream(srcFile);
            // begin writing a new ZIP entry, positions the stream to the start of the entry data
            zos.putNextEntry(new ZipEntry(URLEncoder.encode(generateFileName.apply(result), "UTF-8")));
            int length;
            while ((length = fis.read(buffer)) > 0) {
                zos.write(buffer, 0, length);
            }
            zos.closeEntry();
            // close the InputStream
            fis.close();
        }
        // close the ZipOutputStream
        zos.close();
        
        log.exit();
    }
    
    /**
     * @return
     * @throws InvalidRequestException
     */
    private LinkedHashMap<String, Object> getGeneResponses() throws InvalidRequestException {
        log.entry();

        //retrieve possible parameters for this query
        final List<String> fgList = Collections.unmodifiableList(Optional.ofNullable(
                this.requestParameters.getForegroundList()).orElse(new ArrayList<>()));
        final List<String> bgList = Collections.unmodifiableList(Optional.ofNullable(
                this.requestParameters.getBackgroundList()).orElse(new ArrayList<>()));
        
        //sanity checks
        if (fgList.isEmpty() && bgList.isEmpty()) {
            throw log.throwing(new InvalidRequestException("A gene ID list must be provided"));
        }
        
        //OK, start processing the query. First, retrieve the gene list.
        LinkedHashMap<String, Object> data = new LinkedHashMap<>();
        if (!fgList.isEmpty()) {
            data.put(
                    this.requestParameters.getUrlParametersInstance().getParamForegroundList().getName(),
                    this.getGeneResponse(fgList, 
                            this.requestParameters.getUrlParametersInstance().getParamForegroundList().getName()));
        } 
        if (!bgList.isEmpty()) {
            data.put(
                    this.requestParameters.getUrlParametersInstance().getParamBackgroundList().getName(),
                    this.getGeneResponse(bgList,
                            this.requestParameters.getUrlParametersInstance().getParamBackgroundList().getName()));
        }
        return log.exit(data);
    }

    /**
     * Build a {@code GeneListResponse}.
     * 
     * @param geneList  A {@code List} of {@code String}s that are IDs of genes.
     * @param paramName A {@code String} that is the name of the parameter.
     * @return          The {@code GeneListResponse} built from 
     *                  {@code geneList} and {@code paramName}.
     */
    private GeneListResponse getGeneResponse(List<String> geneList, String paramName) {
        log.entry(geneList, paramName);
        
        if (geneList.isEmpty()) {
            throw log.throwing(new AssertionError("Code supposed to be unreachable."));
        }

        TreeSet<String> geneSet = new TreeSet<>(geneList);
        // Load valid submitted gene IDs
        final Set<Gene> validGenes = new HashSet<>(this.getGenes(null, geneSet));
        // Identify undetermined gene IDs
        final Set<String> undeterminedGeneIds = new HashSet<>(geneSet);
        undeterminedGeneIds.removeAll(validGenes.stream()
                .map(Gene::getId)
                .collect(Collectors.toSet()));
        
        // Map species ID to valid gene ID count
        final Map<String, Long> speciesIdToGeneCount = validGenes.stream()
                    .collect(Collectors.groupingBy(Gene::getSpeciesId, Collectors.counting()));
        // Retrieve detected species, and create a new Map Species -> Long
        final Map<Species, Long> speciesToGeneCount = speciesIdToGeneCount.isEmpty()? new HashMap<>(): 
                this.serviceFactory.getSpeciesService()
                .loadSpeciesByIds(speciesIdToGeneCount.keySet())
                .stream()
                .collect(Collectors.toMap(spe -> spe, spe -> speciesIdToGeneCount.get(spe.getId())));
        // Determine selected species ID. 
        String selectedSpeciesId = speciesIdToGeneCount.entrySet().stream()
                //sort based on gene count (and in case of equality, based on species ID)
                .max((e1, e2) -> {
                    if (e1.getValue().equals(e2.getValue())) {
                        return e1.getKey().compareTo(e2.getKey()); 
                    } 
                    return e1.getValue().compareTo(e2.getValue());
                })
                .map(e -> e.getKey())
                .orElse(null);
        // Load valid stages for selected species
        Set<DevStage> validStages = null;
        if (selectedSpeciesId != null) {
            validStages = this.getGroupingDevStages(selectedSpeciesId, DEV_STAGE_LEVEL);
        }

        // Identify gene IDs not in the selected species
        TreeSet<String> notSelectedSpeciesGenes = new TreeSet<>(
                validGenes.stream()
                    .filter(g -> !g.getSpeciesId().equals(selectedSpeciesId))
                    .map(Gene::getId)
                    .collect(Collectors.toSet()));
        
        // Determine message
        messages.add(this.getGeneUploadResponseMessage(geneSet, speciesToGeneCount, 
                undeterminedGeneIds, paramName));
        
        //sanity checks
        if (speciesToGeneCount.isEmpty() && undeterminedGeneIds.isEmpty()) {
            throw log.throwing(new IllegalArgumentException(
                    "Some gene information to display must be provided."));
        }

        //Transform speciesToGeneCount into a Map species ID -> gene count, and add
        //the invalid gene count, associated to a specific key, and make it a LinkedHashMap,
        //for sorted and predictable responses
        LinkedHashMap<String, Long> responseSpeciesIdToGeneCount = Optional.of(speciesToGeneCount)
                .map(map -> {
                    //create a map species ID -> gene count
                    Map<String, Long> newMap = map.entrySet().stream()
                            .collect(Collectors.toMap(e -> e.getKey().getId(), e -> e.getValue()));
                    //add an entry for undetermined genes
                    if (!undeterminedGeneIds.isEmpty()) {
                        newMap.put(UNDETERMINED_SPECIES_LABEL, Long.valueOf(undeterminedGeneIds.size()));
                    }
                    return newMap;
                })
                .get().entrySet().stream()
                //sort in descending order of gene count (and in case of equality,
                //by ascending order of key, for predictable message generation)
                .sorted((e1, e2) -> {
                    if (e1.getValue().equals(e2.getValue())) {
                        return e1.getKey().compareTo(e2.getKey());
                    }
                    return e2.getValue().compareTo(e1.getValue());
                }).collect(Collectors.toMap(Entry::getKey, Entry::getValue,
                    (v1, v2) -> {throw log.throwing(new IllegalStateException("no key collision possible"));},
                    LinkedHashMap::new));

        return log.exit(new GeneListResponse(
                responseSpeciesIdToGeneCount,
                //provide a TreeMap species ID -> species
                speciesToGeneCount.keySet().stream().collect(Collectors.toMap(
                        spe -> spe.getId(), spe -> spe,
                        (v1, v2) -> {throw log.throwing(new IllegalStateException("No key collision possible"));},
                        TreeMap::new)),
                selectedSpeciesId,
                //provide a List of DevStages sorted by their natural ordering
                //(= by left bound = by temporal ordering)
                Optional.ofNullable(validStages)
                    .map(stages -> stages.stream()
                    .sorted(Comparator.naturalOrder())
                    .collect(Collectors.toList()))
                    .orElse(new ArrayList<>()),
                //SortedSet of gene IDs with known species but not the selected species
                notSelectedSpeciesGenes,
                //SortedSet of undetermined gene IDs
                Optional.ofNullable(undeterminedGeneIds)
                    .map(TreeSet<String>::new)
                    .orElse(new TreeSet<>())));
    }

    /**
     * Build message according to submitted gene IDs, the gene count by species,
     * and the undetermined gene IDs.
     * 
     * @param submittedGeneIds      A {@code Set} of {@code String}s that are submitted gene IDs.
     * @param speciesToGeneCount    A {@code Map} where keys are {@code Species} objects, 
     *                              the associated values being a {@code Long} that are gene ID 
     *                              count found in the species.
     * @param undeterminedGeneIds   A {@code Set} of {@code String}s that are submitted gene IDs
     *                              from undetermined species.
     * @return                      A {@code String} that is the message to display.
     */
    private String getGeneUploadResponseMessage(Set<String> submittedGeneIds, 
            Map<Species, Long> speciesToGeneCount, Set<String> undeterminedGeneIds,
            String paramName) {
        log.entry(submittedGeneIds, speciesToGeneCount, undeterminedGeneIds);
        
        StringBuilder msg = new StringBuilder();
        msg.append(submittedGeneIds.size());
        msg.append(" genes entered");
        if (!speciesToGeneCount.isEmpty()) {
            msg.append(speciesToGeneCount.entrySet().stream()
                //sort in descending order of gene count (and in case of equality, 
                //by ascending order of key, for predictable message generation)
                .sorted((e1, e2) -> {
                    if (e1.getValue().equals(e2.getValue())) {
                        return e1.getKey().getId().compareTo(e2.getKey().getId()); 
                    } 
                    return e2.getValue().compareTo(e1.getValue());
                })
                .map(e -> ", " + e.getValue() + " in " + e.getKey().getName())
                .collect(Collectors.joining()));
        }
        if (!undeterminedGeneIds.isEmpty()) {
            msg.append(", ");
            msg.append(undeterminedGeneIds.size());
            msg.append(" not found");
        }
        msg.append(" in Bgee for ");
        msg.append(paramName);
        
        return log.exit(msg.toString());
    }

    /**
     * Get the {@code List} of {@code Gene}s containing valid genes from a given list.
     * 
     * @param speciesIds    A {@code Set} of {@code String}s that are IDs of species 
     *                      for which to return the {@code Gene}s.
     * @param geneIds       A {@code Set} of {@code String}s that are IDs of genes 
     *                      for which to return the {@code Gene}s.
     * @return              A {@List} of {@code Gene}s that are valid genes.
     * @throws IllegalStateException    If the {@code GeneService} obtained from the 
     *                                  {@code ServiceFactory} did not allow
     *                                  to obtain any {@code Gene}.
     */
    private List<Gene> getGenes(Set<String> speciesIds, Set<String> geneIds) 
            throws IllegalStateException {
        log.entry(speciesIds, geneIds);
        List<Gene> genes = serviceFactory.getGeneService().
                loadGenesByIdsAndSpeciesIds(geneIds, speciesIds);
        return log.exit(genes);
    }
    
    /**
     * Get the {@code Set} of {@code DevStage}s for the given {@code speciesId}.
     * 
     * @param speciesId     A {@code String}s that are ID of species 
     *                      for which to return the {@code DevStage}s.
     * @param level         An {@code Integer} that is the level of dev. stages 
     *                      allowing to filter the dev. stages.
     * @return              A {@List} of {@code DevStage}s that are dev. stages in the 
     *                      provided species at the provided level.
     * @throws IllegalStateException    If the {@code DevStageService} obtained from the 
     *                                  {@code ServiceFactory} did not allow
     *                                  to obtain any {@code DevStage}.
     */
    private Set<DevStage> getGroupingDevStages(String speciesId, Integer level) 
            throws IllegalStateException {
        log.entry(speciesId, level);
        Set<DevStage> devStages = serviceFactory.getDevStageService().
                loadGroupingDevStages(Arrays.asList(speciesId), level);

        if (devStages.isEmpty()) {
            throw log.throwing(new IllegalStateException("A DevStageService did not allow "
                    + "to obtain any DevStage."));
        }
        return log.exit(new HashSet<>(devStages));
    }

    /**
     * @param display
     * @throws InvalidRequestException
     * @throws MissingParameterException
     */
    private TopAnatController loadTopAnatController() 
            throws InvalidRequestException, MissingParameterException {
        log.entry();

        // Get submitted params
        // Fg gene list cannot be null
        final List<String> subFgIds = Collections.unmodifiableList(Optional.ofNullable(
                this.requestParameters.getForegroundList()).orElse(new ArrayList<>()));
        if (subFgIds.isEmpty()) {
            throw log.throwing(new InvalidRequestException(
                    "A foreground gene ID list must be provided"));
        }
    
        // Bg gene list can be null if the default species background should be used
        final List<String> subBgIds = Collections.unmodifiableList(Optional.ofNullable(
                this.requestParameters.getBackgroundList()).orElse(new ArrayList<>())); 
        boolean hasBgList = !subBgIds.isEmpty();
    
        // Expr type cannot be null
        final List<String> subCallTypes = Collections.unmodifiableList(Optional.ofNullable(
                this.requestParameters.getExprType()).orElse(new ArrayList<>()));
        if (subCallTypes.isEmpty()) {
            throw log.throwing(new InvalidRequestException("A expression type must be provided"));
        }
        Set<String> callTypes = subCallTypes.stream().map(s -> s.toUpperCase(Locale.ROOT))
                .collect(Collectors.toSet());
        // TODO remove when "ALL" call type is removed from web app
        if (callTypes.contains("ALL")) {
            callTypes.remove("ALL");
            callTypes.add(CallType.Expression.EXPRESSED.name());
            callTypes.add(CallType.DiffExpression.DIFF_EXPRESSED.name());
        }
        
        // Data quality can be null if there is no filter to be applied
        DataQuality dataQuality = this.checkAndGetDataQuality();
        // Data types can be null if there is no filter to be applied
        Set<DataType> dataTypes = this.checkAndGetDataTypes();
    
        // Dev. stages can be null if all selected species stages should be used
        final List<String> subDevStages = Collections.unmodifiableList(Optional.ofNullable(
                this.requestParameters.getDevStage()).orElse(new ArrayList<>()));
        Set<String> devStageIds = null; 
        if (!subDevStages.isEmpty()) {
            devStageIds = new HashSet<>(subDevStages);
        }

        // Decorrelation type can be null if all selected species stages should be used
        final String subDecorrType = this.requestParameters.getDecorrelationType();
        if (StringUtils.isBlank(subDecorrType)) {
            throw log.throwing(new InvalidRequestException("A decorrelation type must be provided"));
        }
        
        final Integer subNodeSize = this.requestParameters.getNodeSize(); 
        if (subNodeSize != null && subNodeSize <= 0) {
            throw log.throwing(new InvalidRequestException("A node size must be positive"));
        }
        
        final Integer subNbNodes = this.requestParameters.getNbNode(); 
        if (subNbNodes != null && subNbNodes <= 0) {
            throw log.throwing(new InvalidRequestException("A number of nodes must be positive"));
        }
        
        final Double subFdrThr = this.requestParameters.getFdrThreshold(); 
        if (subFdrThr != null && subFdrThr < 0) {
            throw log.throwing(new InvalidRequestException("A FDR threshold must be positive"));
        }
        
        final Double subPValueThr = this.requestParameters.getPValueThreshold();
        if (subPValueThr != null && subPValueThr < 0) {
            throw log.throwing(new InvalidRequestException("A p-value threshold must be positive"));
        }
        
        Set<String> cleanFgIds = new HashSet<>(subFgIds);
        Set<String> cleanBgIds = null;
        String speciesId = null;
        // If a bg list is provided, we do a gene validation on it and clean both lists
        if (hasBgList) {
            GeneListResponse bgGeneResponse = this.getGeneResponse(subBgIds, null);
            speciesId = bgGeneResponse.getSelectedSpecies();
            devStageIds = this.cleanDevStages(bgGeneResponse, devStageIds);
            cleanBgIds = new HashSet<>(subBgIds);
            // Remove in fg gene IDs that are not in bg list
            cleanFgIds.retainAll(subBgIds);
            if (cleanFgIds.isEmpty()) {
                throw log.throwing(new InvalidRequestException("No gene IDs of foreground "
                        + "are in background gene ID list"));
            }
            cleanFgIds = this.cleanGeneIds(bgGeneResponse, cleanFgIds);
            cleanBgIds = this.cleanGeneIds(bgGeneResponse, cleanBgIds);
            if (subNodeSize > cleanBgIds.size()) {
                throw log.throwing(new InvalidRequestException("It is impossible to obtain results "
                        + "if the node size parameter is greater than the number of *valid* genes "
                        + "in the background."));
            }
        }
        
        // Get gene response for clean fg gene IDs
        GeneListResponse fgGeneResponse = this.getGeneResponse(new ArrayList<>(cleanFgIds), null);
        
        // If a bg list is NOT provided, we clean fg list and get data according to fgGeneResponse
        if (!hasBgList) {
            cleanFgIds = this.cleanGeneIds(fgGeneResponse, cleanFgIds);
            devStageIds = this.cleanDevStages(fgGeneResponse, devStageIds);
            speciesId = fgGeneResponse.getSelectedSpecies();
        }

        assert cleanFgIds != null && !cleanFgIds.isEmpty();
        assert devStageIds != null && !devStageIds.isEmpty();
        assert StringUtils.isNotBlank(speciesId);
        assert callTypes == null || callTypes.isEmpty();

        // One TopAnat analyze has one call type and one dev. stage
        List<TopAnatParams> allTopAnatParams = new ArrayList<TopAnatParams>();
        for (String callType: callTypes) {
            if (callType.isEmpty()) {
                continue;
            }
            for (String devStageId: devStageIds) {
                log.debug("Iteration: callType={} - devStageId={}", callType, devStageId);
                if (StringUtils.isBlank(devStageId)) {
                    continue;
                }
                CallType callTypeEnum = null;
                
                if (BgeeEnum.isInEnum(CallType.Expression.class, callType)) {
                    callTypeEnum = CallType.Expression.convertToExpression(callType);
                } else if (BgeeEnum.isInEnum(CallType.DiffExpression.class, callType)) {
                    callTypeEnum = CallType.DiffExpression.convertToDiffExpression(callType);
                } else {
                    throw log.throwing(new InvalidRequestException("Unkown call type: " + callType));
                }

                TopAnatParams.Builder builder = new TopAnatParams.Builder(
                        cleanFgIds, cleanBgIds, speciesId, callTypeEnum);
                
                builder.dataQuality(dataQuality);
                builder.dataTypes(dataTypes);
                
                if(devStageId.equals("ALL")) {
                    builder.devStageIds(null);
                } else {
                    builder.devStageIds(Arrays.asList(devStageId));
                }
                if (BgeeEnum.isInEnum(DecorrelationType.class, subDecorrType)) {
                    builder.decorrelationType(DecorrelationType.convertToDecorrelationType(subDecorrType));
                } else {
                    throw log.throwing(new InvalidRequestException("Unkown decorrelation type: " + 
                            subDecorrType));
                }
                builder.nodeSize(subNodeSize);
                builder.numberOfSignificantNode(subNbNodes);
                builder.fdrThreshold(subFdrThr);
                builder.pvalueThreshold(subPValueThr);
                allTopAnatParams.add(builder.build());
            }
        }
        
        TopAnatController controller = new TopAnatController(allTopAnatParams, this.prop, 
                this.serviceFactory);
        return log.exit(controller);
    }

    /**
     * @param geneResponse
     * @param geneIds
     * @return
     */
    private Set<String> cleanGeneIds(GeneListResponse geneResponse, Set<String> geneIds) {
        log.entry(geneResponse, geneIds);
        
        Set<String> cleanGeneIds = new HashSet<>(geneIds);
        
        // Remove gene IDs that are not in bg selected species.
        cleanGeneIds.removeAll(geneResponse.getNotInSelectedSpeciesGeneIds());
        // Remove gene IDs that are in bg undetermined gene IDs
        cleanGeneIds.removeAll(geneResponse.getUndeterminedGeneIds());
        
        if (cleanGeneIds.isEmpty()) {
            throw log.throwing(new IllegalArgumentException("No gene IDs of foreground "
                    + "are in selected species from background gene ID list"));
        }
        
        return log.exit(cleanGeneIds);
    }

    /**
     * @param geneResponse
     * @param devStageIds
     * @return
     */
    private Set<String> cleanDevStages(GeneListResponse geneResponse, Set<String> devStageIds) {
        log.entry(geneResponse, devStageIds);

        Set<String> allDevStageIds = geneResponse.getStages().stream()
                .map(DevStage::getId)
                .collect(Collectors.toSet()); 
        if (devStageIds == null) {
            // We need stages to be able to build all TopAnatParams
            return log.exit(allDevStageIds);
        }
        Set<String> cleanDevStageIds = new HashSet<>(devStageIds);
        if (!allDevStageIds.containsAll(cleanDevStageIds)) {
            throw log.throwing(new IllegalArgumentException("Provided developmental stages " +
                    "are not from selected species"));
        }
        
        return log.exit(cleanDevStageIds);
    }

    /**
     * A convenient class to be passed to {@link JsonHelper} for gene list upload responses. 
     */
    public static class GeneListResponse {
        /**
         * See {@link #getGeneCount()}.
         */
        private final LinkedHashMap<String, Long> geneCount;
        /**
         * See {@link #getDetectedSpecies()}.
         */
        private final TreeMap<String, Species> detectedSpecies;
        /**
         * See {@link #getSelectedSpecies()}.
         */
        private final String selectedSpecies;
        /**
         * See {@link #getStages()}.
         */
        private final List<DevStage> stages;
        /**
         * See {@link #getNotInSelectedSpeciesGeneIds()}.
         */
        private final TreeSet<String> notInSelectedSpeciesGeneIds;
        /**
         * See {@link #getUndeterminedGeneIds()}.
         */
        private final TreeSet<String> undeterminedGeneIds;
        
        /**
         * Constructor of {@code GeneListResponse}. All {@code Collection}s or {@code Map}s
         * have a predictable iteration order, for predictable and consistent responses.
         * 
         * @param geneCount             A {@code LinkedHashMap} where keys are {@code String}s
         *                              corresponding to species IDs, the associated value being
         *                              a {@code Long} that is the gene count on the species.
         * @param detectedSpecies       A {@code List} of {@code Species} detected in the gene list uploaded.
         * @param selectedSpecies       A {@code String} representing the ID of the selected species.
         * @param stages                A {@code Collection} of {@code DevStage}s that are
         *                              valid dev. stages for {@code selectedSpecies}. 
         *                              They will be ordered by their natural ordering.
         * @param notInSelectedSpeciesGeneIds      A {@code TreeSet} of {@code String}s that are 
         *                              submitted gene IDs that are not in the selected species.
         * @param undeterminedGeneIds   A {@code TreeSet} of {@code String}s that are gene IDs
         *                              with undetermined species.
         */
        public GeneListResponse(LinkedHashMap<String, Long> geneCount,
                TreeMap<String, Species> detectedSpecies,
                String selectedSpecies, Collection<DevStage> stages, TreeSet<String> notInSelectedSpeciesGeneIds,
                TreeSet<String> undeterminedGeneIds) {
            log.entry(geneCount, detectedSpecies, selectedSpecies, stages,
                    notInSelectedSpeciesGeneIds, undeterminedGeneIds);
            this.geneCount= geneCount;
            this.detectedSpecies = detectedSpecies;
            this.selectedSpecies = selectedSpecies;
            this.stages = stages.stream().sorted().collect(Collectors.toList());
            this.notInSelectedSpeciesGeneIds = notInSelectedSpeciesGeneIds;
            this.undeterminedGeneIds = undeterminedGeneIds;
            log.exit();
        }
        
        /**
         * @return  The {@code Map} where keys are {@code String}s corresponding species IDs,
         *          the associated value being a {@code Long} that is the gene count on the species.
         */
        public LinkedHashMap<String, Long> getGeneCount() {
            return this.geneCount;
        }
        /**
         * @return  The {@code TreeMap} where keys are {@code String}s corresponding 
         *          to IDs of detected species, the associated value being the corresponding 
         *          {@code Species} object.
         */
        public TreeMap<String, Species> getDetectedSpecies() {
            return this.detectedSpecies;
        }
        /**
         * @return  The {@code String} representing the ID of the selected species.
         */
        public String getSelectedSpecies() {
            return this.selectedSpecies;
        }
        /**
         * @return The {@code Set} of {@code DevStage}s that are 
         *          valid dev. stages for {@code selectedSpecies}.
         */
        public List<DevStage> getStages() {
            return this.stages;
        }
        /**
         * @return  The {@code TreeSet} of {@code String}s that are 
         *          submitted gene IDs that are not in the selected species.
         */
        public TreeSet<String> getNotInSelectedSpeciesGeneIds() {
            return this.notInSelectedSpeciesGeneIds;
        }
        /**
         * @return  The {@code Set} of {@code String}s that are gene IDs with undetermined species.
         */
        public TreeSet<String> getUndeterminedGeneIds() {
            return this.undeterminedGeneIds;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((detectedSpecies == null) ? 0 : detectedSpecies.hashCode());
            result = prime * result + ((geneCount == null) ? 0 : geneCount.hashCode());
            result = prime * result + ((selectedSpecies == null) ? 0 : selectedSpecies.hashCode());
            result = prime * result + ((stages == null) ? 0 : stages.hashCode());
            result = prime * result + ((notInSelectedSpeciesGeneIds == null) ? 0 : 
                notInSelectedSpeciesGeneIds.hashCode());
            result = prime * result + ((undeterminedGeneIds == null) ? 0 : undeterminedGeneIds.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            GeneListResponse other = (GeneListResponse) obj;
            if (detectedSpecies == null) {
                if (other.detectedSpecies != null)
                    return false;
            } else if (!detectedSpecies.equals(other.detectedSpecies))
                return false;
            if (geneCount == null) {
                if (other.geneCount != null)
                    return false;
            } else if (!geneCount.equals(other.geneCount))
                return false;
            if (selectedSpecies == null) {
                if (other.selectedSpecies != null)
                    return false;
            } else if (!selectedSpecies.equals(other.selectedSpecies))
                return false;
            if (stages == null) {
                if (other.stages != null)
                    return false;
            } else if (!stages.equals(other.stages))
                return false;
            if (notInSelectedSpeciesGeneIds == null) {
                if (other.notInSelectedSpeciesGeneIds != null)
                    return false;
            } else if (!notInSelectedSpeciesGeneIds.equals(other.notInSelectedSpeciesGeneIds))
                return false;
            if (undeterminedGeneIds == null) {
                if (other.undeterminedGeneIds != null)
                    return false;
            } else if (!undeterminedGeneIds.equals(other.undeterminedGeneIds))
                return false;
            return true;
        }

        @Override
        public String toString() {
            return "Gene count: " + getGeneCount() + " - Detected species: " + getDetectedSpecies()
                    + " - Selected species: " + getSelectedSpecies() + " - Stages: " + getStages()
                    + " - Gene IDs not in selected: " + getNotInSelectedSpeciesGeneIds()
                    + " - Undetermined gene IDs: " + getUndeterminedGeneIds();
        }
    }

    /**
     * A convenient class to be passed to {@link JsonHelper} for job responses. 
     */
    public static class JobResponse {
        /**
         * See {@link #getJobId()}.
         */
        private final long jobId;
        /**
         * See {@link #getJobStatus()}.
         */
        private final String jobStatus;
        /**
         * See {@link #getData()}.
         */
        private final String data;
        
        /**
         * Constructor of {@code JobResponse}.
         * 
         * @param jobId     A {@code long} representing the ID of the job (task).
         * @param jobStatus A {@code String} representing the status of the job.
         * @param data      A {@code String} representing the key of the parameters.
         */
        public JobResponse(long jobId, String jobStatus, String data) {
            log.entry(jobId, jobStatus, data);
            this.jobId = jobId;
            this.jobStatus = jobStatus;
            this.data = data;
            log.exit();
        }
        
        /**
         * @return  The {@code long} representing the ID of the job (task).
         */
        public long getJobId() {
            return this.jobId;
        }
        /**
         * @return  The {@code String} representing the status of the job.
         */
        public String getJobStatus() {
            return this.jobStatus;
        }
        /**
         * @return  The {@code String} representing the key of the parameters.
         */
        public String getData() {
            return this.data;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((data == null) ? 0 : data.hashCode());
            result = prime * result + (int) (jobId ^ (jobId >>> 32));
            result = prime * result + ((jobStatus == null) ? 0 : jobStatus.hashCode());
            return result;
        }
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            JobResponse other = (JobResponse) obj;
            if (data == null) {
                if (other.data != null) {
                    return false;
                }
            } else if (!data.equals(other.data)) {
                return false;
            }
            if (jobId != other.jobId) {
                return false;
            }
            if (jobStatus == null) {
                if (other.jobStatus != null) {
                    return false;
                }
            } else if (!jobStatus.equals(other.jobStatus)) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return "Job ID: " + getJobId() + " - Job status: "
                    + getJobStatus() + " - Data: " + getData();
        }
    }
}