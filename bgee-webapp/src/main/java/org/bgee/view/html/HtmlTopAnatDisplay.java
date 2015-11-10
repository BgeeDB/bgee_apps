package org.bgee.view.html;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.BgeeProperties;
import org.bgee.controller.RequestParameters;
import org.bgee.model.TaskManager;
import org.bgee.model.anatdev.DevStage;
import org.bgee.model.topanat.TopAnatResults;
import org.bgee.view.TopAnatDisplay;

/**
 * This class generates the HTML views relative to topAnat.
 * 
 * @author  Frederic Bastian
 * @author  Valentine Rech de Laval
 * @version Bgee 13, Nov 2015
 * @since   Bgee 13
 */
public class HtmlTopAnatDisplay extends HtmlParentDisplay implements TopAnatDisplay {
    
    private final static Logger log = LogManager.getLogger(HtmlTopAnatDisplay.class.getName());

    /**
     * Constructor providing the necessary dependencies. 
     * 
     * @param response          A {@code HttpServletResponse} that will be used to display the 
     *                          page to the client
     * @param requestParameters The {@code RequestParameters} handling the parameters of the 
     *                          current request.
     * @param prop              A {@code BgeeProperties} instance that contains the properties
     *                          to use.
     * @param factory           The {@code HtmlFactory} that instantiated this object.
     * @throws IOException      If there is an issue when trying to get or to use the
     *                          {@code PrintWriter} 
     */
    public HtmlTopAnatDisplay(HttpServletResponse response,
            RequestParameters requestParameters, BgeeProperties prop,
            HtmlFactory factory) throws IllegalArgumentException, IOException {
        super(response, requestParameters, prop, factory);
    }

    @Override
    public void displayTopAnatHomePage() {
        log.entry();
        
        this.startDisplay("Bgee TopAnat page");
        
        //AngularJS module container
        this.writeln("<div ng-app='app'>");

        this.writeln("<!--[if lt IE 7]>" +
        "<p class='browsehappy'>You are using an <strong>outdated</strong> browser. Please <a href='http://browsehappy.com/'>upgrade your" +
            "browser</a> to improve your experience.</p>" +
        "<![endif]-->");

        this.writeln("<div class='container'>" +
            "<nav class='navbar navbar-default navbar-fixed-top'>" +
                "<div class='row'>" +
                    "<div class='col-sm-4'>" +
                        "<a class='navbar-brand' href='http://bgee.org/' style = 'padding: 15px 0px 8px 20px'>" +
                            "<img src='images/bgee13_logo.png' width='86' height='32' alt='Bgee - Gene Expression Evolution'>" +
                        "</a>" +
                        "<a class='navbar-brand' href='http://www.isb-sib.ch/' style = 'padding: 15px 0px 8px 20px'>" +
                            "<img src='images/sib_logo_small.gif' width='42' height='32' alt='SIB Swiss Institute of Bioinformatics'>" +
                        "</a>" +
                    "</div>" +
                    "<div class='col-sm-8'>" +

                        "<h2>TopAnat</h2>" +

                        "<div class='row'>" +
                            "<div class='col-sm-8'>" +
                                "<span style='font-size: small'>Test for Enrichment and Depletion of Gene Expression in Anatomical Structures</span>" +
                            "</div>" +

                            "<!-- LINKS -->" +
                            "<div class='col-sm-4'>" +
                                "<a href='' style='margin-right: 6px;' class='pull-right'>" +
                                    "<span style='margin-right: 6px;vertical-align: middle;' class='glyphicon glyphicon-education' aria-hidden='true'></span>Documentation</a>" +
                                "<a href='' style='margin-right: 6px;' class='pull-right'>" +
                                    "<span style='margin-right: 6px;vertical-align: middle;' class='glyphicon glyphicon-th-list' aria-hidden='true'></span>Recent jobs</a>" +
                            "</div>" +
                        "</div>" +
                    "</div>" +
                "</div>" +
            "</nav>" +
        "</div>");

        this.writeln("<div style='padding: 90px 0 0 0;margin-left: 20px; margin-right: 20px' ng-view=''>" +

        "</div>");

        //End AngularJS module container
        this.writeln("</div>");


        
        this.endDisplay();

        log.exit();
    }
    
    @Override
    public void sendResultResponse(TopAnatResults results) {
        throw log.throwing(new UnsupportedOperationException("Not available for HTML display"));
    }

    @Override
    public void sendJobStatusResponse(TaskManager taskManager) {
        throw log.throwing(new UnsupportedOperationException("Not available for HTML display"));
    }

    @Override
    public void sendJobErrorResponse(TaskManager taskManager) {
        throw log.throwing(new UnsupportedOperationException("Not available for HTML display"));
    }

    @Override
    public void sendGeneListReponse(Map<String, Long> speciesIdToGeneCount, String selectedSpeciesId,
            Set<DevStage> validStages, Set<String> undeterminedGeneIds, int statusCode, String msg) {
        throw log.throwing(new UnsupportedOperationException("Not available for HTML display"));
    }

    @Override
    public void sendNewJobResponse(int jobTrackingId) {
        throw log.throwing(new UnsupportedOperationException("Not available for HTML display"));
    }

    private String getForm(boolean isActive) {
        // TODO Auto-generated method stub
        return null;
    }

    private String getJobInfo() {
        // TODO Auto-generated method stub
        return null;
    }

    private String getResults() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected void includeJs() {
        log.entry();
        super.includeJs();
        //external libs used only by TopAnat
        this.includeJs("lib/angular.min.js");
        this.includeJs("lib/angular_modules/angular-animate.min.js");
        this.includeJs("lib/angular_modules/angular-cookies.min.js");
        this.includeJs("lib/angular_modules/angular-messages.min.js");
        this.includeJs("lib/angular_modules/angular-resource.min.js");
        this.includeJs("lib/angular_modules/angular-route.min.js");
        this.includeJs("lib/angular_modules/angular-sanitize.min.js");
        this.includeJs("lib/angular_modules/angular-touch.min.js");
        this.includeJs("lib/angular_modules/ui_modules/ui-grid.min.js");
        this.includeJs("lib/angular_modules/ui_modules/ui-bootstrap-tpls.min.js");
        this.includeJs("lib/jquery_plugins/bootstrap.min.js");
        this.includeJs("lib/angular_modules/angular-file-upload.min.js");
        this.includeJs("lib/jquery_plugins/toastr.min.js");
        
        //TopAnat JS files
        this.includeJs("topanat/topanat.js");
        this.includeJs("topanat/services/logger.module.js");
        this.includeJs("topanat/services/logger.js");
        this.includeJs("topanat/controllers/main.js");
        this.includeJs("topanat/services/bgeedataservice.js");
        this.includeJs("topanat/services/bgeejobservice.js");
        this.includeJs("topanat/services/helpservice.js");
        this.includeJs("topanat/services/datatypefactory.js");
        this.includeJs("topanat/services/config.js");
        this.includeJs("topanat/services/constants.js");
        log.exit();
    }
    @Override
    protected void includeCss() {
        log.entry();
        //CSS files of AngularJS modules only used by TopAnat
        this.includeCss("lib/angular_modules/ui-grid.min.css");
        //CSS files of jQuery plugins only used by TopAnat
        this.includeCss("lib/jquery_plugins/bootstrap.min.css");
        this.includeCss("lib/jquery_plugins/toastr.min.css");
        //CSS files specific to TopAnat
        this.includeCss("topanat.css");
        //we need to add the Bgee CSS files at the end, to override CSS file from bootstrap
        super.includeCss();
        log.exit();
    }

}
