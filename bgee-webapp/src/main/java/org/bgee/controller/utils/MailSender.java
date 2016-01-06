package org.bgee.controller.utils;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.SendFailedException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.BgeeProperties;

/**
 * A utility class to send mails. This class notably uses locks internally to ensure 
 * that it cannot be used for email flooding (in case our webapp is compromised...).
 * 
 * @author Frederic Bastian
 * @version Bgee 13 Dec. 2015
 * @since Bgee 13 Dec. 2015
 */
public class MailSender {
    private final static Logger log = LogManager.getLogger(MailSender.class.getName());
    
    //***************************
    //  STATIC FINAL VARIABLES
    //***************************
    /**
     * A {@code String} that is the name of the parameter in {@code BgeeProperties} mail URI 
     * providing the username to connect to the mail server.
     */
    public static final String USERNAME_PARAM_NAME = "username";
    /**
     * A {@code String} that is the name of the parameter in {@code BgeeProperties} mail URI 
     * providing the password to connect to the mail server.
     */
    public static final String PASSWORD_PARAM_NAME = "password";

    /**
     * A {@code long} that is the default waiting time in milliseconds between 2 mails are sent.
     */
    public static final long DEFAULT_WAIT_TIME_IN_MS = 10000;
    /**
     * A fair {@code Lock} used to ensure that only one mail is sent at a time.
     */
    private static final Lock LOCK  = new ReentrantLock(true);
    
    //**************************
    // STATIC VARS AND METHODS
    //**************************
    /**
     * A {@code long} that is the time in milliseconds when the last mail was sent. 
     * Note that this variable will be read and modified solely inside {@code lock}/{@code unlock} blocks 
     * using {@link #LOCK}, so it doesn't need to be further synchronized.
     */
    private static long lastSendTime = 0;
    /**
     * A {@code long} that is the waiting time in milliseconds between 2 mails are sent. 
     * As this variable can be accessed outside of any lock mechanism, it needs to be {@code volatile} 
     * (we don't care about atomicity). Default value of {@link #DEFAULT_WAIT_TIME_IN_MS}.
     */
    private static volatile long waitTimeInMs = DEFAULT_WAIT_TIME_IN_MS;
    
    /**
     * @return  A {@code long} that is the waiting time in milliseconds between 2 mails are sent.
     */
    public static long getWaitTimeInMs() {
        return waitTimeInMs;
    }
    /**
     * @param waitTimeInMs  A {@code long} that will define the waiting time in milliseconds 
     *                      between 2 mails are sent.
     */
    public static void setWaitTimeInMs(long waitTimeInMs) {
        MailSender.waitTimeInMs = waitTimeInMs;
    }
    
    //***************************
    // INSTANCE VARS AND METHODS
    //***************************
    /**
     * A {@code BgeeProperties} providing the parameters to connect to the mail server.
     */
    private final BgeeProperties props;
    /**
     * A {@code Consumer} of {@code Message} responsible for sending it. Useful for unit testing.
     */
    private final Consumer<Message> transportSender;
    
    /**
     * A {@code String} that is the username to connect to the server, retrieved from 
     * the {@code BgeeProperties} mail URI.
     * @see #USERNAME_PARAM_NAME
     */
    private final String username;
    /**
     * A {@code String} that is the password to connect to the server, retrieved from 
     * the {@code BgeeProperties} mail URI.
     * @see #PASSWORD_PARAM_NAME
     */
    private final String password;
    /**
     * The {@code Properties} actually used to connect to the mail server, extracted from 
     * the {@code BgeeProperties} mail URI. See {@link BgeeProperties#getMailUri()} for more details.
     */
    private final Properties mailProps;
    
    /**
     * Default constructor using all default parameters.
     * @throws IllegalArgumentException If the default {@code BgeeProperties} does not allow 
     *                                  to send mails.
     */
    public MailSender() throws IllegalArgumentException {
        this(BgeeProperties.getBgeeProperties());
    }
    /**
     * Instantiate a {@code MailSender} using the default {@code Transport#send(Message)} 
     * method to send mails.
     * <p>
     * Please note that the value returned by {@code getMailWaitTime} on {@code props} 
     * will have no effect, it is needed to call {@link #setWaitTimeInMs(int)}.
     * 
     * @param props The {@code BgeeProperties} which to retrieve parameters to connect to the mail server from.
     * @throws IllegalArgumentException If {@code props} is {@code null} or does not allow to send mails.
     */
    public MailSender(BgeeProperties props) throws IllegalArgumentException {
        this(props, msg -> {
            try {
                Transport.send(msg);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
    /**
     * Please note that the value returned by {@code getMailWaitTime} on {@code props} 
     * will have no effect, it is needed to call {@link #setWaitTimeInMs(int)}.
     * 
     * @param props             The {@code BgeeProperties} which to retrieve parameters to connect 
     *                          to the mail server from.
     * @param transportSender   A {@code Consumer} of {@code Message} responsible for sending it. 
     *                          Useful for unit testing.
     * @throws IllegalArgumentException If {@code props} or {@code transportSender} are {@code null}, 
     *                                  or {@code props} does not allow to send mails.
     */
    public MailSender(BgeeProperties props, Consumer<Message> transportSender) 
            throws IllegalArgumentException {
        if (props == null || transportSender == null) {
            throw log.throwing(new IllegalArgumentException("No argument can be null."));
        }
        this.props = props;
        this.transportSender = transportSender;
        
        //retrieve parameters, perform sanity checks
        String stringUri = this.props.getMailUri();
        if (StringUtils.isBlank(stringUri)) {
            throw log.throwing(new IllegalArgumentException("No parameter provided allowing to send mail "
                    + "in BgeeProperties."));
        }
        URI uri = null;
        try {
            uri = new URI(stringUri);
        } catch (URISyntaxException e) {
            throw log.throwing(new IllegalArgumentException("The BgeeProperties mail URI is malformed : " 
                     + props.getMailUri(), e));
        }
        
        Properties mailProps = new Properties();
        
        //add properties related to protocol, server, port
        mailProps.put("mail." + uri.getScheme() + ".host", uri.getHost());
        if (uri.getPort() >= 0) {
            //javax.mail property values are always provided as Strings.
            mailProps.put("mail." + uri.getScheme() + ".port", String.valueOf(uri.getPort()));
        }
        
        //Retrieve properties provided in query string, including special props username/password
        String username = null;
        String password = null;
        for (NameValuePair pair: URLEncodedUtils.parse(uri, "UTF-8")) {
            if (USERNAME_PARAM_NAME.equals(pair.getName())) {
                username = pair.getValue();
            } else if (PASSWORD_PARAM_NAME.equals(pair.getName())) {
                password = pair.getValue();
            } else {
                mailProps.put(pair.getName(), pair.getValue());
            }
        }
        this.username = username;
        this.password = password;
        
        this.mailProps = mailProps;
        
        log.debug("Prefer ipv4: {}", System.getProperty("java.net.preferIPv4Stack"));
    }
    
    /**
     * Send a mail to a unique recipient. 
     * 
     * @param fromAddress   A {@code String} that is the email of the sender.
     * @param fromPersonal  A {@code String} that is the display name of the sender.
     * @param toAddress     A {@code String} that is the email of the receiver.
     * @param toPersonal    A {@code String} that is the display name of the receiver.
     * @param subject       A {@code String} that is the subject of the mail.
     * @param msgBody       A {@code String} that is the message body of the mail.
     * @throws InterruptedException         If this {@code Thread} was interrupted while waiting 
     *                                      for the permission to send a mail (anti-flood system).
     * @throws UnsupportedEncodingException If the encoding of the provided arguments is not supported.
     * @throws MessagingException           If an error occurred while trying to send a mail.
     * @throws SendFailedException          If a mail was not delivered to the recipient.
     * @see #sendMessage(String, String, Map, String, String)
     */
    public void sendMessage(String fromAddress, String fromPersonal, 
            String toAddress, String toPersonal, String subject, String msgBody) 
                    throws UnsupportedEncodingException, MessagingException, InterruptedException {
        log.entry(fromAddress, fromPersonal, toAddress, toPersonal, subject, msgBody);
        Map<String, String> to = new HashMap<>();
        to.put(toAddress, toPersonal);
        this.sendMessage(fromAddress, fromPersonal, to, subject, msgBody);
        log.exit();
    }
    /**
     * Send a mail to several recipients. 
     * 
     * @param fromAddress   A {@code String} that is the email of the sender.
     * @param fromPersonal  A {@code String} that is the display name of the sender.
     * @param recipients    A {@code Map} where keys are {@code String}s that are the emails 
     *                      of the receivers, the associated values being their display name.
     * @param subject       A {@code String} that is the subject of the mail.
     * @param msgBody       A {@code String} that is the message body of the mail.
     * @throws InterruptedException         If this {@code Thread} was interrupted while waiting 
     *                                      for the permission to send a mail (anti-flood system).
     * @throws UnsupportedEncodingException If the encoding of the provided arguments is not supported.
     * @throws MessagingException           If an error occurred while trying to send a mail.
     * @throws SendFailedException          If a mail was not delivered to one or several of the recipients.
     * @see #sendMessage(String, String, String, String, String, String)
     */
    public void sendMessage(String fromAddress, String fromPersonal, 
            Map<String, String> recipients, String subject, String msgBody) 
                    throws UnsupportedEncodingException, MessagingException, InterruptedException {
        log.entry(fromAddress, fromPersonal, recipients, subject, msgBody);
        
        Session session = null;
        if (this.username != null && this.password != null) {
            log.trace("Using Session with Authenticator.");
            session = Session.getInstance(this.mailProps, 
                new Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });
        } else {
            log.trace("Using default Session.");
            session = Session.getInstance(this.mailProps);
        }
        
        Message msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(fromAddress, fromPersonal));
        
        for (Entry<String, String> recipient: recipients.entrySet()) {
            if (StringUtils.isBlank(recipient.getValue())) {
                msg.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient.getKey()));
            } else {
                msg.addRecipient(Message.RecipientType.TO,
                        new InternetAddress(recipient.getKey(), recipient.getValue()));
                
            }
        }
        msg.setSubject(subject);
        msg.setText(msgBody);
        
        boolean mailSent = false;
        //as we have used a functional interface to send message for easier unit testing, 
        //we had to send a RuntimeException with potential checked Exception as cause.
        //We try to get the checked exception back.
        try {
            this.acquireLock();
            //we consider that we have sent a mail even in case of failure.
            mailSent = true;
            this.transportSender.accept(msg);
        } catch (RuntimeException e) {
            if (e.getCause() != null && e.getCause() instanceof MessagingException) {
                throw log.throwing((MessagingException) e.getCause());
            } else if (e.getCause() != null && e.getCause() instanceof SendFailedException) {
                throw log.throwing((SendFailedException) e.getCause());
            }
            throw log.throwing(e);
        } finally {
            this.releaseLock(mailSent);
        }
        
        log.exit();
    }
    
    /**
     * Lock mechanism to ensure that this class is not used for enail flooding. 
     * Will ensure that only one mail is sent at a time, and that there is a waiting time 
     * corresponding to {@link #getWaitTimeInMs()} between two mails are sent.
     * 
     * @throws InterruptedException     In case the current {@code Thread} is interrupted while 
     *                                  waiting for the lock.
     * @see #releaseLock(boolean)
     */
    private void acquireLock() throws InterruptedException {
        log.entry();
        
        LOCK.lock();
        //now that we have acquire the lock, we make sure we don't send another mail too fast
        long toWaitInMs = getWaitTimeInMs() - (System.currentTimeMillis() - MailSender.lastSendTime);
        long waitUntilInMs = System.currentTimeMillis() + toWaitInMs;
        //good old synchronized/wait, because if we were to use a Condition, the Lock would be released 
        //during the wait, but we want to hold the Lock while we wait for the permission 
        //to send a mail again.
        synchronized (LOCK) {
            while (toWaitInMs > 0) {
                LOCK.wait(toWaitInMs);
                // in case of spurious wake up
                toWaitInMs = waitUntilInMs - System.currentTimeMillis();
            }
        }
        
        log.exit();
    }
    /**
     * Release lock, and save the current time where a mail was sent if {@code mailSent} is {@code true}.
     * 
     * @param mailSent  A {@code boolean} defining whether a mail was actually tried to be sent 
     *                  by the current {@code Thread}, before calling this method.
     * @see #acquireLock()
     */
    private void releaseLock(boolean mailSent) {
        log.entry();
        
        if (mailSent) {
            MailSender.lastSendTime = System.currentTimeMillis();
        }
        LOCK.unlock();
        
        log.exit();
    }
}
