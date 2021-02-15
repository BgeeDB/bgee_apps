package org.bgee.view.html;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.BgeeProperties;
import org.bgee.controller.RequestParameters;
import org.bgee.model.source.Source;
import org.bgee.model.source.SourceCategory;
import org.bgee.view.SourceDisplay;

/**
 * This class is the HTML implementation of the {@code SourceDisplay}.
 * 
 * @author  Valentine Rech de Laval
 * @version Bgee 14, May 2018
 * @since   Bgee 13, Mar. 2016
 */
public class HtmlSourceDisplay extends HtmlParentDisplay implements SourceDisplay {

    private final static Logger log = LogManager.getLogger(HtmlSourceDisplay.class.getName());

    /**
     * @param response          A {@code HttpServletResponse} that will be used to display 
     *                          the page to the client.
     * @param requestParameters The {@code RequestParameters} that handles the parameters of
     *                          the current request.
     * @param prop              A {@code BgeeProperties} instance that contains 
     *                          the properties to use.
     * @param factory           The {@code HtmlFactory} that instantiated this object.
     * @throws IOException      If there is an issue when trying to get or to use the 
     *                          {@code PrintWriter}.
     */
    public HtmlSourceDisplay(HttpServletResponse response, RequestParameters requestParameters,
            BgeeProperties prop, HtmlFactory factory) throws IllegalArgumentException, IOException {
        super(response, requestParameters, prop, factory);
    }
    
    @Override
    public void displaySources(Collection<Source> sources) {
        log.entry(sources);

        this.startDisplay("Data sources", "WebPage");
        
        this.writeln("<h1 property='schema:name'>Data sources</h1>");

        this.writeln("<div class='row'>");
        this.writeln("<div class='" + CENTERED_ELEMENT_CLASS + "'>");
        this.writeln("<p property='schema:description'>This page provides information about the data sources used in Bgee "
                + this.getWebAppVersion() + ".</p>");
        this.writeln(this.getContent(sources));
        this.writeln("</div>"); // end class

        this.writeln("</div>"); // close row 

        this.endDisplay();

        log.traceExit();
    }

    /** 
     * Generates the HTML code to display sources by category. 
     * 
     * @param sources   A {@code List} of {@code Source}s that are sources to be displayed.
     * @return          The {@code String} that is the HTML code to display sources by category.
     */
    private String getContent(Collection<Source> sources) {
        log.entry(sources);
        Map<SourceCategory, List<Source>> mapSources = sources.stream()
                .collect(Collectors.groupingBy(Source::getCategory, TreeMap::new, Collectors.toList()));

        StringBuilder sb = new StringBuilder();
        for (Entry<SourceCategory, List<Source>> entry : mapSources.entrySet()) {
            sb.append("<h2>");
            sb.append(entry.getKey().getStringRepresentation()); // Category name
            sb.append("</h2>");

            sb.append("<div class='source-list col-sm-12'>");
            
            List<Source> orderedSources = entry.getValue().stream()
                .sorted((s1, s2) -> s1.getName().compareToIgnoreCase(s2.getName()))
                .collect(Collectors.toList());
            
            boolean isFirst = true;
            for (Source source : orderedSources) {
                boolean hasReleaseDate = source.getReleaseDate() != null;
                String border = "border-top";
                if (isFirst) {
                    border = "";
                    isFirst = false;
                }
                sb.append("<div class='source row ").append(border).append("'>");
                sb.append("    <div class='source-name col-sm-3 col-md-2'>");
                sb.append("        <a href='").append(source.getBaseUrl()).append("' target='_blank' rel='noopener'>");
                sb.append(             source.getName());
                sb.append("        </a>");
                sb.append("    </div>"); // close source-name
                sb.append("    <div class='source-details col-sm-9 col-md-10'>");
                sb.append("        <div class='details col-xs-12'>");
                if (hasReleaseDate) {
                    sb.append("        <div class='source-details-header col-sm-4 col-md-3 col-lg-2'>Description</div>");
                }
                sb.append("            <div class='source-details-content col-sm-8 col-md-9'>");
                sb.append(                 source.getDescription());
                sb.append("            </div>"); // close source-details-content
                sb.append("        </div>"); // close details
                if (hasReleaseDate) {
                    sb.append("    <div class='details col-xs-12'>");
                    sb.append("        <div class='source-details-header col-sm-4 col-md-3 col-lg-2'>Last import</div>");
                    sb.append("        <div class='source-details-content col-sm-8 col-md-9'>");
                    sb.append(source.getReleaseDate() == null ? "-" : source.getReleaseDate());
                    if (StringUtils.isNotBlank(source.getReleaseVersion())) {
                        sb.append(" (release ").append(source.getReleaseVersion()).append(")");
                    }
                    sb.append("        </div>"); // close source-details-content
                    sb.append("    </div>"); // close details
                }
                sb.append("    </div>"); // close source-details
                sb.append("</div>");  // close source
            }
            sb.append("</div>"); // close source-list
        }
        return log.traceExit(sb.toString());
    }
        
    @Override
    protected void includeCss() {
        log.entry();
        super.includeCss();
        //If you ever add new files, you need to edit bgee-webapp/pom.xml
        //to correctly merge/minify them.
        this.includeCss("source.css");
        log.traceExit();
    }
}
