package org.bgee.controller.utils;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Exchanger;
import java.util.function.Consumer;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.TestAncestor;
import org.bgee.controller.BgeeProperties;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
/**
 * Unit tests for {@link MailSender}.
 * 
 * @author  Frederic Bastian
 * @version Bgee 13 Dec. 2015
 * @since   Bgee 13 Dec. 2015
 */
public class MailSenderTest extends TestAncestor {

    private final static Logger log = 
            LogManager.getLogger(MailSenderTest.class.getName());

    private static final long WAIT_TIME = 500;
    
    @Override
    protected Logger getLogger() {
        return log;
    }
    
    @BeforeClass
    public static void init() {
        //set a reasonable waiting time between sending mails, not too long 
        //but not too short to be able to test the anti-flood system.
        MailSender.setWaitTimeInMs(WAIT_TIME);
    }
    @AfterClass
    public static void shutDown() {
        MailSender.setWaitTimeInMs(MailSender.DEFAULT_WAIT_TIME_IN_MS);
    }

    /**
     * Test {@link MailSender#sendMessage(String, String, Map, String, String)}
     */
    @Test
    public void shouldSendMail() throws MessagingException, IOException, InterruptedException {
        Properties setProps = new Properties();
        setProps.setProperty(BgeeProperties.MAIL_URI_KEY, 
                "smtp://smtp.unil.ch:500/?username=user&password=pass&mail.smtp.auth=true"
                + "&mail.smtp.ssl.enable=true&mail.smtp.starttls.enable=true");
        BgeeProperties props = new BgeeProperties(setProps);
        List<Message> capturedMsgs = new ArrayList<>();
        Consumer<Message> transportSender = msg -> capturedMsgs.add(msg);
        
        MailSender sender = new MailSender(props, transportSender);
        Map<String, String> recipients = new HashMap<>();
        recipients.put("you@test.com", "You Yourself");
        recipients.put("you2@test.com", null);
        sender.sendMessage("me@test.com", "Me Myself", recipients, "About test", 
                "I hope you appreciate this test!");
        
        assertEquals("Incorrect number of Message sent", 1, capturedMsgs.size());
        Message msg = capturedMsgs.get(0);
        Session session = msg.getSession();
        
        Properties mailProps = session.getProperties();
        Properties expectedMailProps = new Properties();
        expectedMailProps.put("mail.smtp.host", "smtp.unil.ch");
        expectedMailProps.put("mail.smtp.port", "500");
        expectedMailProps.put("mail.smtp.auth", "true");
        expectedMailProps.put("mail.smtp.ssl.enable", "true");
        expectedMailProps.put("mail.smtp.starttls.enable", "true");
        assertEquals("Incorrect Properties provided to Session", expectedMailProps, mailProps);
        
        assertEquals("Incorrect authentication", "user", 
                session.requestPasswordAuthentication(null, -1, null, null, null).getUserName());
        assertEquals("Incorrect authentication", "pass", 
                session.requestPasswordAuthentication(null, -1, null, null, null).getPassword());
        
        assertEquals("Incorrect from Address", new InternetAddress("me@test.com", "Me Myself"), 
                msg.getFrom()[0]);
        assertEquals("Incorrect recipients Address", new HashSet<>(Arrays.asList(
                new InternetAddress[]{new InternetAddress("you@test.com", "You Yourself"), 
                        new InternetAddress("you2@test.com")})), 
                new HashSet<>(Arrays.asList(msg.getRecipients(Message.RecipientType.TO))));
        
        assertEquals("Incorrect subject", "About test", msg.getSubject());
        assertEquals("Incorrect text", "I hope you appreciate this test!", msg.getContent());
    }
    
    /**
     * Test {@link MailSender#sendMessage(String, String, Map, String, String)} 
     * with no authentication and a single recipient.
     */
    @Test
    public void shouldSendSimpleMail() throws MessagingException, IOException, InterruptedException {
        Properties setProps = new Properties();
        setProps.setProperty(BgeeProperties.MAIL_URI_KEY, 
                "smtp://smtp.unil.ch:500/?mail.smtp.auth=true"
                + "&mail.smtp.ssl.enable=true&mail.smtp.starttls.enable=true");
        BgeeProperties props = new BgeeProperties(setProps);
        List<Message> capturedMsgs = new ArrayList<>();
        Consumer<Message> transportSender = msg -> capturedMsgs.add(msg);
        
        MailSender sender = new MailSender(props, transportSender);
        
        sender.sendMessage("me@test.com", "Me Myself", "onlyyou@test.com", "Only You", 
                "About test", "I hope you appreciate this test!");
        
        assertEquals("Incorrect number of Message sent", 1, capturedMsgs.size());
        Message msg = capturedMsgs.get(0);
        Session session = msg.getSession();
        
        Properties mailProps = session.getProperties();
        Properties expectedMailProps = new Properties();
        expectedMailProps.put("mail.smtp.host", "smtp.unil.ch");
        expectedMailProps.put("mail.smtp.port", "500");
        expectedMailProps.put("mail.smtp.auth", "true");
        expectedMailProps.put("mail.smtp.ssl.enable", "true");
        expectedMailProps.put("mail.smtp.starttls.enable", "true");
        assertEquals("Incorrect Properties provided to Session", expectedMailProps, mailProps);
        
        assertEquals("Incorrect authentication", null, 
                session.requestPasswordAuthentication(null, -1, null, null, null));
        
        assertEquals("Incorrect from Address", new InternetAddress("me@test.com", "Me Myself"), 
                msg.getFrom()[0]);
        assertEquals("Incorrect recipients Address", new HashSet<>(Arrays.asList(
                new InternetAddress[]{new InternetAddress("onlyyou@test.com", "Only You")})), 
                new HashSet<>(Arrays.asList(msg.getRecipients(Message.RecipientType.TO))));
        
        assertEquals("Incorrect subject", "About test", msg.getSubject());
        assertEquals("Incorrect text", "I hope you appreciate this test!", msg.getContent());
    }
    
    /**
     * Test {@link MailSender#sendMessage(String, String, Map, String, String)} 
     * when Transport throws a MessagingException. This test is needed because we actually use 
     * a functional interface to send messages, with actual exception wrapped into RuntimeException.
     */
    @Test
    public void shouldThrowMessagingException() throws UnsupportedEncodingException, InterruptedException {
        Properties setProps = new Properties();
        setProps.setProperty(BgeeProperties.MAIL_URI_KEY, 
                "smtp://smtp.unil.ch:500/?mail.smtp.auth=true"
                + "&mail.smtp.ssl.enable=true&mail.smtp.starttls.enable=true");
        BgeeProperties props = new BgeeProperties(setProps);
        Consumer<Message> transportSender = msg -> {throw new RuntimeException(new MessagingException());};
        MailSender sender = new MailSender(props, transportSender);
        
        try {
            sender.sendMessage("me@test.com", "Me Myself", "onlyyou@test.com", "Only You", 
                    "About test", "I hope you appreciate this test!");
            //test failed
            fail("A MessagingException should have been thrown");
        } catch (MessagingException e) {
            //test passed
        }
    }
    
    /**
     * Test the anti-flood mechanism when using {@code MailSender.sendMessage} methods.
     */
    @Test
    public void testAntiFlood() throws InterruptedException, UnsupportedEncodingException, MessagingException {
        Properties setProps = new Properties();
        setProps.setProperty(BgeeProperties.MAIL_URI_KEY, "smtp://localhost35/");
        BgeeProperties props = new BgeeProperties(setProps);
        //nothing to be done by the sender
        Consumer<Message> transportSender = msg -> {return;};
        final MailSender sender = new MailSender(props, transportSender);
        
        /**
         * An anonymous class to acquire {@code MySQLDAOManager}s 
         * from a different thread than this one, 
         * and to be run alternatively to the main thread.
         */
        class ThreadTest extends Thread {
            public volatile boolean exceptionThrown = false;
            public volatile long stopTime = -1;
            /**
             * An {@code Exchanger} that will be used to run threads alternatively. 
             */
            public final Exchanger<Integer> exchanger = new Exchanger<Integer>();
            
            @Override
            public void run() {
                try {
                    sender.sendMessage("me@test.com", "Me Myself", "onlyyou@test.com", "Only You", 
                            "About test", "I hope you appreciate this test!");
                    stopTime = System.currentTimeMillis();
                    
                    //main thread's turn
                    this.exchanger.exchange(null);
                    
                } catch (Exception e) {
                    exceptionThrown = true;
                }
            }
        }

        ThreadTest test = new ThreadTest();
        long startTime = System.currentTimeMillis();
        sender.sendMessage("me@test.com", "Me Myself", "onlyyou@test.com", "Only You", 
                "About test", "I hope you appreciate this test!");
        
        //launch a second thread also acquiring BgeeConnections
        test.start();
        //wait for this thread's turn
        test.exchanger.exchange(null);
        //check that no exception was thrown in the second thread 
        if (test.exceptionThrown) {
            throw new IllegalStateException("An Exception occurred in the second thread.");
        }
        
        //This test is not bullet-proof, but with a high enough wait time, 
        //we should confidently assume that the mechanism is working.
        assertTrue("Incorrect wait time between mails", test.stopTime != -1 && 
                (test.stopTime - startTime) >= WAIT_TIME);
    }
}
