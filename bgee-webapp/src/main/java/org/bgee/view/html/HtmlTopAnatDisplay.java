package org.bgee.view.html;

import java.io.IOException;
import java.util.LinkedHashMap;

import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.BgeeProperties;
import org.bgee.controller.RequestParameters;
import org.bgee.model.TaskManager;
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

        
        this.writeln("<div id='appLoading' class='loader'>" + 
        		"<!-- BEGIN: Actual animated container. -->" + 
        		"<div class='anim-cover'>" + 
        			"<div class='messaging'>" +
        				"<h1>" + 
        					"<li class='fa fa-circle-o-notch fa-spin'></li> TopAnat is Loading" +
        				"</h1>" + 
        			"</div>" + 
        		"</div>" +
        	"</div>");
    
        this.writeln("<!--[if lt IE 7]>" +
        "<p class='browsehappy'>You are using an <strong>outdated</strong> browser. Please <a href='http://browsehappy.com/'>upgrade your" +
            "browser</a> to improve your experience.</p>" +
        "<![endif]-->");

        //FB: I really hate this ribbon :p
        //this.writeln("<div class='corner-ribbon top-left sticky red shadow'>Beta</div>");
        this.writeln("<h1>TopAnat - Gene Expression Enrichment <span class='alert'>(BETA VERSION)</span></h1>");
        
        this.writeln("<div style='margin-left: 20px; margin-right: 20px' ng-view=''>" +

        "</div>");

        //End AngularJS module container
        this.writeln("</div>");


        
        this.endDisplay();

        log.exit();
    }
    
    @Override
    public void sendResultResponse(LinkedHashMap<String, Object> data, String msg) {
        throw log.throwing(new UnsupportedOperationException("Not available for HTML display"));
    }

    @Override
    public void sendTrackingJobResponse(LinkedHashMap<String, Object> data, String msg) {
        throw log.throwing(new UnsupportedOperationException("Not available for HTML display"));
    }

    @Override
    public void sendJobErrorResponse(TaskManager taskManager) {
        throw log.throwing(new UnsupportedOperationException("Not available for HTML display"));
    }

    @Override
    public void sendGeneListReponse(LinkedHashMap<String, Object> data, String msg) {
        throw log.throwing(new UnsupportedOperationException("Not available for HTML display"));
    }

    @Override
    public void sendTopAnatParameters(String hash) {
        throw log.throwing(new UnsupportedOperationException("Not available for HTML display"));
    }

    @Override
    protected void includeJs() {
        log.entry();
        super.includeJs();
        //external libs used only by TopAnat
        this.includeJs("lib/angular.min.js");
        this.includeJs("lib/angular_modules/angular-animate.min.js");
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
        this.includeJs("lib/angular_modules/angular-location-update.min.js");
        this.includeJs("lib/Blob.js");
        this.includeJs("lib/FileSaver.min.js");
        this.includeJs("lib/angular_modules/angular-file-saver.bundle.min.js");
        
        //TopAnat JS files
        this.includeJs("topanat/topanat.js");
        this.includeJs("topanat/services/logger.module.js");
        this.includeJs("topanat/services/logger.js");
        this.includeJs("topanat/controllers/main.js");
        this.includeJs("topanat/controllers/result.js");
        this.includeJs("topanat/services/bgeedataservice.js");
        this.includeJs("topanat/services/bgeejobservice.js");
        this.includeJs("topanat/services/helpservice.js");
        this.includeJs("topanat/services/datatypefactory.js");
        this.includeJs("topanat/services/config.js");
        this.includeJs("topanat/services/lang.js");
        this.includeJs("topanat/services/constants.js");
        this.includeJs("topanat/directives/loading.js");
        log.exit();
    }
    @Override
    protected void includeCss() {
        log.entry();
        //CSS files of AngularJS modules only used by TopAnat
        this.includeCss("lib/angular_modules/ui_grid/ui-grid.min.css");
        //CSS files of jQuery plugins only used by TopAnat
        this.includeCss("lib/jquery_plugins/bootstrap.min.css");
        this.includeCss("lib/jquery_plugins/toastr.min.css");
        //font-awesome
        this.includeCss("lib/font_awesome/css/font-awesome.min.css");
        //CSS files specific to TopAnat
        this.includeCss("topanat.css");
        //we need to add the Bgee CSS files at the end, to override CSS file from bootstrap
        super.includeCss();
        log.exit();
    }

}
