package org.bgee.controller;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.user.User;
import org.bgee.controller.utils.MailSender;
import org.bgee.model.ServiceFactory;
import org.bgee.model.job.Job;
import org.bgee.model.job.JobService;
import org.bgee.model.job.exception.TooManyJobsException;

/**
 * Controller handling requests related to feedback from users.
 * 
 * @author  Frederic Bastian
 * @version Bgee 15.2, Apr. 2025
 * @since   Bgee 15.2, Apr. 2025
 */
public class CommandFeedback extends CommandParent {
    private final static Logger log = LogManager.getLogger(CommandFeedback.class.getName());

    /**
     * Class responsible for sending the feedback in an asynchronous manner.
     * 
     * @author  Frederic Bastian
     * @version Bgee 15.2, Apr. 2025
     * @since   Bgee 15.2, Apr. 2025
     */
    private static class FeedbackJobRunner implements Runnable {
        /**
         * A {@code String} that is the URL where the user made a feedback from.
         */
        private final String sourceUrl;
        /**
         * An {@code Integer} that is the rating the user gave.
         */
        private final Integer rating;
        /**
         * A {@code String} that is the comment the user provided.
         */
        private final String comment;
        /**
         * A {@code String} that is the email of the user.
         */
        private final String email;

        /**
         * The {@code JobService} instance allowing to manage jobs between threads 
         * across the entire webapp. 
         */
        protected final JobService jobService;
        /**
         * The {@code User} launching the job.
         */
        protected final User user;
        
        /**
         * A {@code BgeeProperties} defining parameters, notably to send mails.
         */
        private final BgeeProperties props;
        /**
         * A {@code MailSender} to send mails on completion.
         */
        private final MailSender mailSender;


        
        public FeedbackJobRunner(String sourceUrl, Integer rating, String comment, String email,
                JobService jobService, User user,
                BgeeProperties props, MailSender mailSender) {
            log.traceEntry("{}, {}, {}, {}, {}, {}, {}, {}",
                    sourceUrl, rating, comment, email,
                    jobService, user, props, mailSender);

            this.sourceUrl = sourceUrl;
            this.rating = rating;
            this.comment = comment;
            this.email = email;
            this.jobService = jobService;
            this.user = user;
            
            this.props = props;
            this.mailSender = mailSender;
            log.traceExit();
        }

        @Override
        public void run() {
            log.traceEntry();

            Job job = null;
            try {
                //acquire the Job
                job = this.jobService.registerNewJob(this.user.getUUID().toString());
                log.debug("Sending mail to RT list...");

                //build mail subject
                String subject = "Feedback from website user";
                
                //build mail body
                StringBuilder sb = new StringBuilder();
                sb.append("Hello Bgee team, here's a new feedback from a user of the website!");
                sb.append("\n\n");
                sb.append("Feedback:");
                if (StringUtils.isNotBlank(this.sourceUrl)) {
                    sb.append("\nSource URL: ").append(this.sourceUrl);
                }
                if (this.rating != null) {
                    sb.append("\nRating: ").append(this.rating);
                }
                if (StringUtils.isNotBlank(this.comment)) {
                    sb.append("\nComment: ").append(this.comment);
                }
                if (StringUtils.isNotBlank(this.email)) {
                    sb.append("\nEmail: ").append(this.email);
                }
                sb.append("\n\n");
                sb.append("Information about user:");
                if (StringUtils.isNotBlank(this.user.getIPAddress())) {
                    sb.append("\nObfuscated IP address: ")
                      .append(this.user.getIPAddress().substring(0, this.user.getIPAddress().lastIndexOf('.')));
                }
                if (this.user.getPreferredLanguage().isPresent()) {
                    sb.append("\nPreferred language: ").append(this.user.getPreferredLanguage().get());
                }
                if (this.user.getReferrer().isPresent()) {
                    sb.append("\nReferrer: ").append(this.user.getReferrer().get());
                }
                if (this.user.getUserAgent().isPresent()) {
                    sb.append("\nUser agent: ").append(this.user.getUserAgent().get());
                }
                String msgBody = sb.toString();
                log.debug("Mail to send: {}", msgBody);

                this.mailSender.sendMessage(this.props.getFeedbackFromAddress(), 
                        this.props.getFeedbackFromPersonal(), this.props.getFeedbackSendTo(), null, subject, msgBody);

                log.debug("Mail sent.");
            } catch (Exception e) {
                log.catching(Level.DEBUG, e); //will be rethrown later, don't log as error
                log.error("Unable to send mail");
                throw log.throwing(new IllegalStateException(e));
            } finally {
                if (job != null) {
                    //we immediately release the job, another thread will determine 
                    //whether the job is completed by searching for the cached results.
                    job.release();
                }
            }
            log.traceExit();
        }
    }

    public CommandFeedback(HttpServletResponse response, RequestParameters requestParameters, 
            BgeeProperties prop, ServiceFactory serviceFactory, 
            JobService jobService, User user, MailSender mailSender) {
        super(response, requestParameters, prop, null, serviceFactory, jobService, user, 
                null, mailSender);
    }

    @Override
    public void processRequest() throws Exception {
        log.traceEntry();

        //We process only if either a comment or a rating is provided.
        if (StringUtils.isNotBlank(this.requestParameters.getFirstValue(
                        this.requestParameters.getUrlParametersInstance().getParamComment())) ||
            this.requestParameters.getFirstValue(
                       this.requestParameters.getUrlParametersInstance().getParamRating()) != null) {

            this.launchNewJob();
        }
        log.traceExit();
    }

    /**
     * Launch a new thread to send the feedback asynchronously.
     *
     * @throws TooManyJobsException     If the user already has too many running jobs.
     */
    private void launchNewJob() throws TooManyJobsException {
        log.traceEntry();

        //before formally registering a new job in another thread, we check whether 
        //the user has already too many running jobs. If we checked only in the other thread, 
        //we wouldn't display an error message right away, but only after checking the job status.
        //This is to avoid spam.
        this.jobService.checkTooManyJobs(this.user.getUUID().toString());

        Thread newThread = new Thread(new FeedbackJobRunner(
                this.requestParameters.getFirstValue(
                        this.requestParameters.getUrlParametersInstance().getParamSourceUrl()),
                this.requestParameters.getFirstValue(
                        this.requestParameters.getUrlParametersInstance().getParamRating()),
                this.requestParameters.getFirstValue(
                        this.requestParameters.getUrlParametersInstance().getParamComment()),
                this.requestParameters.getFirstValue(
                        this.requestParameters.getUrlParametersInstance().getParamEmail()),
                this.jobService, this.user, this.prop, this.mailSender));
        newThread.start();

        log.traceExit();
    }
}
