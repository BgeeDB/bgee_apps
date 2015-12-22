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
import java.util.function.Consumer;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.TestAncestor;
import org.bgee.controller.BgeeProperties;
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

    @Override
    protected Logger getLogger() {
        return log;
    }

    /**
     * Test {@link MailSender#sendMessage(String, String, Map, String, String)}
     */
    @Test
    public void shouldSendMail() throws MessagingException, IOException {
        Properties setProps = new Properties();
        setProps.setProperty(BgeeProperties.MAIL_URI_KEY, 
                "smtp://smtp.unil.ch:500/?username=user&password=pass&mail.smtp.auth=true"
                + "&mail.smtp.ssl.enable=true&mail.smtp.starttls.enable=true");
        BgeeProperties props = BgeeProperties.getBgeeProperties(setProps);
        try {
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
        } finally {
            props.release();
        }
    }
    
    /**
     * Test {@link MailSender#sendMessage(String, String, Map, String, String)} 
     * with no authentication and a single recipient.
     */
    @Test
    public void shouldSendSimpleMail() throws MessagingException, IOException {
        Properties setProps = new Properties();
        setProps.setProperty(BgeeProperties.MAIL_URI_KEY, 
                "smtp://smtp.unil.ch:500/?mail.smtp.auth=true"
                + "&mail.smtp.ssl.enable=true&mail.smtp.starttls.enable=true");
        BgeeProperties props = BgeeProperties.getBgeeProperties(setProps);
        try {
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
        } finally {
            props.release();
        }
    }
    
    /**
     * Test {@link MailSender#sendMessage(String, String, Map, String, String)} 
     * when Transport throws a MessagingException. This test is needed because we actually use 
     * a functional interface to send messages, with actual exception wrapped into RuntimeException.
     */
    @Test
    public void shouldThrowMessagingException() throws UnsupportedEncodingException {
        Properties setProps = new Properties();
        setProps.setProperty(BgeeProperties.MAIL_URI_KEY, 
                "smtp://smtp.unil.ch:500/?mail.smtp.auth=true"
                + "&mail.smtp.ssl.enable=true&mail.smtp.starttls.enable=true");
        BgeeProperties props = BgeeProperties.getBgeeProperties(setProps);
        try {
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
        } finally {
            props.release();
        }
    }
}
