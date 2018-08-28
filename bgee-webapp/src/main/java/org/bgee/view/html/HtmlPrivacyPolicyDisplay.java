package org.bgee.view.html;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.BgeeProperties;
import org.bgee.controller.RequestParameters;
import org.bgee.view.PrivacyPolicyDisplay;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


/**
 * This class displays the page having the category "about", i.e. with the parameter
 * page=about for the HTML view.
 *
 * @author  Valentine Rech de Laval
 * @version Bgee 14, Aug. 2018
 * @since   Bgee 13, Mar. 2015
 */
public class HtmlPrivacyPolicyDisplay extends HtmlParentDisplay implements PrivacyPolicyDisplay {

    private final static Logger log = LogManager.getLogger(HtmlPrivacyPolicyDisplay.class.getName());

    /**
     * Default constructor.
     *
     * @param response          A {@code HttpServletResponse} that will be used to display the 
     *                          page to the client.
     * @param requestParameters A {@code RequestParameters} handling the parameters of the 
     *                          current request, to determine the requested displayType, 
     *                          and for display purposes.
     * @param prop              A {@code BgeeProperties} instance that contains the properties
     *                          to use.
     * @param factory           The {@code HtmlFactory} that instantiated this object.
     * @throws IOException      If there is an issue when trying to get or to use the
     *                          {@code PrintWriter}.
     */
    public HtmlPrivacyPolicyDisplay(HttpServletResponse response, RequestParameters requestParameters,
                                    BgeeProperties prop, HtmlFactory factory) throws IOException {
        super(response, requestParameters, prop, factory);
    }

    @Override
    public void displayPrivacyPolicyPage() {
        log.entry();

        this.startDisplay("Bgee privacy notice");

        this.writeln("<h1>Bgee privacy notice</h1>");

        this.writeln("<div class='row'>");

        this.writeln("<div class='" + CENTERED_ELEMENT_CLASS + "'>");
        this.writeln("<p>This privacy notice explains what personal data is collected by the specific " +
                "service you are requesting, for what purposes, how it is processed, " +
                "nd how we keep it secure.</p>");

        this.writeln("<h2>Who controls your personal data and how to contact us?</h2>");
        this.writeln("<dl>");
        this.writeln("  <dt>The Bgee data controller's contact</dt>" +
                "       <dd>Marc Robinson-Rechavi, Bgee Principal Investigator and" +
                "            Professor at University of Lausanne (" + getObfuscateEmail() + ")</dd>" +
                "       <dd>Department of Ecology and Evolution, University of Lausanne, 1015 Lausanne, Switzerland</dd>");
        this.writeln("</dl>");

        this.writeln("<dl>");
        this.writeln("  <dt>The University of Lausanne Data Protection Officer's contact</dt>" +
                "       <dd>Gwena&euml;lle Gilli&eacute;ron, Responsable Service Juridique (" +
                                getUnilEmail() + ")</dd>" +
                "       <dd>Service Juridique, University of Lausanne, 1015 Lausanne, Switzerland</dd>");
        this.writeln("</dl>");

        this.writeln("<dl>");
        this.writeln("  <dt>The SIB Data Protection Officer's contact</dt>" +
                "       <dd>Marc Filliettaz (" + getSibDpoEmail() + ")</dd>" +
                "       <dd>Tel: +41 21 692 40 50</dd>" +
                "       <dd>SIB Swiss Institute of Bioinformatics, Quartier Sorge - " +
                "                Batiment Genopode, 1015 Lausanne, Switzerland</dd>");
        this.writeln("</dl>");

        this.writeln("<h2>Which is the lawful basis for processing personal data?</h2>");
        this.writeln("<p>Processing your personal data is necessary for our legitimate interests in " +
                "providing services to you, to help improve our resources and for the purposes of " +
                "day-to-day running of the Bgee resource and underlying infrastructure.</p>");
        
        this.writeln("<h2>What personal data is collected from users of the service?" +
                " How do we use this personal data?</h2>");
        this.writeln("<p>");
        this.writeln("   The personal data collected from the services listed below is as follows:");
        this.writeln("   <h4>Website and API</h4>");
        this.writeln("   <ul>");
        this.writeln("       <li>IP address</li>");
        this.writeln("       <li>Email address</li>");
        this.writeln("       <li>Date and time of a visit to the service</li>");
        this.writeln("       <li>Operating system</li>");
        this.writeln("       <li>Browser</li>");
        this.writeln("       <li>Amount of data transmitted</li>");
        this.writeln("   </ul>");
        this.writeln("   <h4>BgeeDB R package</h4>");
        this.writeln("   <ul>");
        this.writeln("       <li>IP address</li>");
        this.writeln("       <li>Date and time of a visit to the service</li>");
        this.writeln("       <li>Operating system</li>");
        this.writeln("       <li>Browser</li>");
        this.writeln("       <li>Amount of data transmitted</li>");
        this.writeln("   </ul>");
        this.writeln("   <h4>FTP</h4>");
        this.writeln("   <ul>");
        this.writeln("       <li>IP address</li>");
        this.writeln("       <li>Date and time of a visit to the service</li>");
        this.writeln("       <li>Operating system</li>");
        this.writeln("       <li>Browser</li>");
        this.writeln("       <li>Amount of data transmitted</li>");
        this.writeln("   </ul>");
        this.writeln("   <h4>Help desk</h4>");
        this.writeln("   <ul>");
        this.writeln("       <li>Name</li>");
        this.writeln("       <li>Email address</li>");
        this.writeln("   </ul>");
        this.writeln("</p>");
        this.writeln("<p>The data controller will use your personal data for the following purposes:");
        this.writeln("  <ul>");
        this.writeln("      <li>To provide the user access to the service</li>");
        this.writeln("      <li>To answer questions from users</li>");
        this.writeln("      <li>To better understand the needs of the users and guide future " +
                "                   improvements of the service</li>");
        this.writeln("      <li>To create anonymous usage statistics</li>");
        this.writeln("      <li>To conduct and monitor data protection activities</li>");
        this.writeln("      <li>To conduct and monitor security activities</li>");
        this.writeln("  </ul>");
        this.writeln("</p>");
        
        this.writeln("<h2>Who will have access to your personal data?</h2>");
        this.writeln("<p>Personal data will only be disclosed to authorized staff of Bgee." +
                "Your personal data are stored internally and not exposed to third parties or countries." +
                "IP addresses are removed from log files or anonymized before being stored in case they are needed.</p>");
        
        this.writeln("<h2>Will your personal data be transferred to third parties/countries " +
                "(i.e. countries not part of EU/EAA) and/or international organisations?</h2>");
        this.writeln("<p>There are no personal data transfers to international organisations outside of Bgee.</p>");
        this.writeln("<p>Bgee uses Google Analytics as a third-party analytics service to collect " +
                "information about website performance and how users navigate through and use our site " +
                "to help us design better interfaces. We do not use Google Analytics to track you individually " +
                "or collect personal data.<em>Personal data send to Google Analytics are anonymized upstream.</em></p>");
        
        this.writeln("<h2>How long do we keep your personal data?</h2>");
        this.writeln("<p>Any personal data directly obtained from you will be retained as long as " +
                "the service is live, even if you stop using the service. We will keep the personal " +
                "data for the minimum amount of time possible to ensure legal compliance and to " +
                "facilitate internal and external audits if they arise.</p>");
        this.writeln("<p>Most data are kept maximum 24 months (26 months for Google Analytics statistics).</p>");
        this.writeln("<p>Only <strong>Help desk</strong> data are kept longer.</p>");
        
        this.writeln("<h2>Cookies</h2>");
        this.writeln("<p>The user is informed that cookies may be stored on the hard drive of " +
                "his/her/its computer to record information about his/her/its visits to this portal.</p>");
        this.writeln("<p>Cookies are used to identify the user and to facilitate and customize " +
                "this portal's use by memorizing certain parameters. Cookies are used, in particular, " +
                "to obtain information on the visits to the pages of this portal and the date and " +
                "time of consultation.</p>");
        this.writeln("<p>The user can at any time prevent the recording and storage of cookies on " +
                "her/his/its computer by disabling the option in her/his/its browser. However, " +
                "some features of this portal may require the mandatory use of cookies " +
                "(customization, warnings, etc.). Otherwise the use of this portal may be impaired " +
                "or even impossible.</p>");
        
        this.writeln("<h2>The joint Data Controllers provide these rights regarding your personal data</h2>");
        this.writeln("<p>You have the right to:");
        this.writeln("  <ol>");
        this.writeln("	    <li>Not be subject to decisions based solely on an automated processing " +
                "               of data (i.e. without human intervention) without you having " +
                "               your views taken into consideration.</li>");
        this.writeln("	    <li>Request at reasonable intervals and without excessive delay or expense, " +
                "               information about the personal data processed about you. " +
                "               Under your request we will inform you in writing about, for example, " +
                "               the origin of the personal data or the preservation period.</li>");
        this.writeln("	    <li>Request information to understand data processing activities " +
                "               when the results of these activities are applied to you.</li>");
        this.writeln("	    <li>Object at any time to the processing of your personal data unless " +
                "               we can demonstrate that we have legitimate reasons to process your personal data.</li>");
        this.writeln("	    <li>Request free of charge and without excessive delay rectification or " +
                "               erasure of your personal data if we have not been processing it " +
                "               respecting the data protection policies of the respective controllers.</li>");
        this.writeln("  </ol>");
        this.writeln("</p>");
        this.writeln("<p>Requests and objections can be sent to our " + getObfuscateBgeeEmail() + ".</p>");
        this.writeln("<p>It must be clarified that rights 4 and 5 are only available whenever " +
                "       the processing of your personal data is not necessary to:");
        this.writeln("  <ul>");
        this.writeln("	    <li>Comply with a legal obligation.</li>");
        this.writeln("	    <li>Perform a task carried out in the public interest.</li>");
        this.writeln("	    <li>Exercise authority as a data controller.</li>");
        this.writeln("	    <li>Archive for purposes in the public interest, " +
                "               or for historical research purposes, or for statistical purposes.</li>");
        this.writeln("	    <li>Establish, exercise or defend legal claims.</li>");
        this.writeln("  </ul>");
        this.writeln("</p>");
        this.writeln("<p>Additional terms: privacy policy</p>");
        this.writeln("</div>"); // close row

        this.endDisplay();

        log.exit();
    }

    private String getUnilEmail() {
        log.entry();
        return log.exit("<script type='text/javascript'>eval(unescape("
                + "'%66%75%6E%63%74%69%6F%6E%20%73%65%62%5F%74%72%61%6E%73%70%6F%73%65%32%31%30%38" +
                "%31%32%30%28%68%29%20%7B%76%61%72%20%73%3D%27%61%6D%6C%69%6F%74%67%3A%65%77%61%6E" +
                "%6C%65%65%6C%67%2E%6C%69%69%6C%72%65%6E%6F%75%40%69%6E%2E%6C%68%63%27%3B%76%61%72" +
                "%20%72%3D%27%27%3B%66%6F%72%28%76%61%72%20%69%3D%30%3B%69%3C%73%2E%6C%65%6E%67%74" +
                "%68%3B%69%2B%2B%2C%69%2B%2B%29%7B%72%3D%72%2B%73%2E%73%75%62%73%74%72%69%6E%67%28" +
                "%69%2B%31%2C%69%2B%32%29%2B%73%2E%73%75%62%73%74%72%69%6E%67%28%69%2C%69%2B%31%29" +
                "%7D%68%2E%68%72%65%66%3D%72%3B%7D%64%6F%63%75%6D%65%6E%74%2E%77%72%69%74%65%28%27" +
                "%3C%61%20%68%72%65%66%3D%22%23%22%20%6F%6E%4D%6F%75%73%65%4F%76%65%72%3D%22%6A%61" +
                "%76%61%73%63%72%69%70%74%3A%73%65%62%5F%74%72%61%6E%73%70%6F%73%65%32%31%30%38%31" +
                "%32%30%28%74%68%69%73%29%22%20%6F%6E%46%6F%63%75%73%3D%22%6A%61%76%61%73%63%72%69" +
                "%70%74%3A%73%65%62%5F%74%72%61%6E%73%70%6F%73%65%32%31%30%38%31%32%30%28%74%68%69" +
                "%73%29%22%3E%65%2D%6D%61%69%6C%3C%2F%61%3E%27%29%3B'));</script>");
    }

    private String getSibDpoEmail() {
        log.entry();
        return log.exit("<script type='text/javascript'>eval(unescape("
                + "'%66%75%6E%63%74%69%6F%6E%20%73%65%62%5F%74%72%61%6E%73%70%6F%73%65%32%31%30%38" +
                "%35%39%28%68%29%20%7B%76%61%72%20%73%3D%27%61%6D%6C%69%6F%74%64%3A%6F%70%73%40%62" +
                "%69%73%2E%69%77%73%73%27%3B%76%61%72%20%72%3D%27%27%3B%66%6F%72%28%76%61%72%20%69" +
                "%3D%30%3B%69%3C%73%2E%6C%65%6E%67%74%68%3B%69%2B%2B%2C%69%2B%2B%29%7B%72%3D%72%2B" +
                "%73%2E%73%75%62%73%74%72%69%6E%67%28%69%2B%31%2C%69%2B%32%29%2B%73%2E%73%75%62%73" +
                "%74%72%69%6E%67%28%69%2C%69%2B%31%29%7D%68%2E%68%72%65%66%3D%72%3B%7D%64%6F%63%75" +
                "%6D%65%6E%74%2E%77%72%69%74%65%28%27%3C%61%20%68%72%65%66%3D%22%23%22%20%6F%6E%4D" +
                "%6F%75%73%65%4F%76%65%72%3D%22%6A%61%76%61%73%63%72%69%70%74%3A%73%65%62%5F%74%72" +
                "%61%6E%73%70%6F%73%65%32%31%30%38%35%39%28%74%68%69%73%29%22%20%6F%6E%46%6F%63%75" +
                "%73%3D%22%6A%61%76%61%73%63%72%69%70%74%3A%73%65%62%5F%74%72%61%6E%73%70%6F%73%65" +
                "%32%31%30%38%35%39%28%74%68%69%73%29%22%3E%65%2D%6D%61%69%6C%3C%2F%61%3E%27%29%3B" +
                "'));</script>");
    }

    @Override
    protected void includeCss() {
        log.entry();
        super.includeCss();
        //If you ever add new files, you need to edit bgee-webapp/pom.xml 
        //to correctly merge/minify them.
        this.includeCss("documentation.css");
        log.exit();
    }
}
