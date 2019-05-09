package org.bgee.view.html;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.BgeeProperties;
import org.bgee.controller.RequestParameters;
import org.bgee.model.anatdev.AnatEntity;
import org.bgee.model.anatdev.multispemapping.AnatEntitySimilarity;
import org.bgee.model.anatdev.multispemapping.AnatEntitySimilarityAnalysis;
import org.bgee.model.anatdev.multispemapping.AnatEntitySimilarityTaxonSummary;
import org.bgee.model.ontology.Ontology;
import org.bgee.model.species.Species;
import org.bgee.model.species.Taxon;
import org.bgee.view.AnatomicalSimilarityDisplay;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This class is the HTML implementation of the {@code AnatomicalSimilarityDisplay}.
 *
 * @author  Valentine Rech de Laval
 * @version Bgee 14, May 2019
 * @since   Bgee 14, May 2019
 */
public class HtmlAnatomicalSimilarityDisplay extends HtmlParentDisplay 
        implements AnatomicalSimilarityDisplay {

    /**
     * {@code Logger} of the class.
     */
    private final static Logger log = LogManager.getLogger(HtmlAnatomicalSimilarityDisplay.class.getName());

    /**
     * @param response             A {@code HttpServletResponse} that will be used to display 
     *                             the page to the client.
     * @param requestParameters    The {@code RequestParameters} that handles the parameters of
     *                             the current request.
     * @param prop                 A {@code BgeeProperties} instance that contains the properties
     *                             to use.
     * @param factory              The {@code HtmlFactory} that instantiated this object.
     * @throws IOException         If there is an issue when trying to get or to use the {@code PrintWriter}.
     */
    public HtmlAnatomicalSimilarityDisplay(HttpServletResponse response, RequestParameters requestParameters,
                                           BgeeProperties prop, HtmlFactory factory) 
            throws IllegalArgumentException, IOException {
        super(response, requestParameters, prop, factory);
    }

    @Override
    public void displayAnatSimilarityHomePage(Set<Species> allSpecies) {
        log.entry(allSpecies);

        this.displayAnatSimilarityPage(allSpecies, null, null, null, null);

        log.exit();
    }

    @Override
    public void displayAnatSimilarityResult(Set<Species> allSpecies, List<Integer> userSpeciesList,
                                            Ontology<Taxon, Integer> taxonOntology,
                                            List<String> userAnatEntityList, AnatEntitySimilarityAnalysis result) {
        log.entry(allSpecies, userSpeciesList, taxonOntology, userAnatEntityList, result);

        this.displayAnatSimilarityPage(allSpecies, userSpeciesList, taxonOntology, userAnatEntityList, result);
        
        log.exit();
    }

    private void displayAnatSimilarityPage(Set<Species> allSpecies, List<Integer> userSpeciesList,
                                           Ontology<Taxon, Integer> taxonOntology,
                                           List<String> userAnatEntityList, AnatEntitySimilarityAnalysis result) {
        log.entry(allSpecies, userSpeciesList, taxonOntology, userAnatEntityList, result);

        this.startDisplay("Anatomical similarity tool");

        this.writeln("<h1>Anatomical similarity tool</h1>");

        this.writeln("<div id='bgee_introduction'>");

        this.writeln("<p>Retrieve anatomical similarities from a list of species and a list of Uberon IDs.</p>");

        this.writeln("</div>");

        this.writeln(getForm(allSpecies, userSpeciesList, userAnatEntityList));

        if (result != null) {
            this.writeln(getResult(result, taxonOntology));
        }

        this.endDisplay();
        
        log.exit();
    }
    
    private String getForm(Set<Species> allSpecies, List<Integer> userSpeciesIds,
                           List<String> userAnatEntityIds) {
        log.entry(allSpecies, userSpeciesIds, userAnatEntityIds);

        String speciesDisplay = allSpecies.stream()
                .sorted(Comparator.comparing(Species::getPreferredDisplayOrder,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .map(sp -> "<div class='checkbox col-sm-6'>" +
                        "       <label>" +
                        "           <input type='checkbox' name='" +
                                            this.getRequestParameters().getUrlParametersInstance()
                                                .getParamSpeciesList().getName() + "'" +
                        "                  value='" + sp.getId() + "' " +
                                           (userSpeciesIds!= null && userSpeciesIds.contains(sp.getId())
                                                ? "checked" : "") +
                        "           />  " + htmlEntities(sp.getScientificName()) +
                        "       </label>" +
                        "   </div>")
                .collect(Collectors.joining());
        
        StringBuilder sb = new StringBuilder();

        RequestParameters action = this.getNewRequestParameters();
        action.setPage(RequestParameters.PAGE_ANAT_SIM);
        sb.append("<div class='row'>");
        sb.append("    <div id='bgee_anat_sim' class='row well well-sm col-xs-offset-1 col-xs-10 " +
                "                                  col-md-offset-1 col-md-10 col-lg-offset-2 col-lg-8'>");
        sb.append("        <form id='bgee_anat_sim_form' method='post' class='form-inline' action='")
                                    .append(action.getRequestURL()).append("' >");

        // Hidden parameter defining it's a POST form
        sb.append("            <input type='hidden' name='").append(this.getRequestParameters()
                .getUrlParametersInstance().getParamPostFormSubmit().getName()).append("' value='1' />");
        
        // Anat. entity list
        String aeText = userAnatEntityIds == null? "" : htmlEntities(String.join("\n", userAnatEntityIds));
        sb.append("            <div class='form-group col-sm-5 row'>");
        sb.append("                <label for='bgee_ae_list' class='col-xs-12 group-title'>" +
                "                       Anatomical entities</label>");
        sb.append("                <textarea id='bgee_ae_list' class='form-control col-xs-12' name='")
                .append(this.getRequestParameters().getUrlParametersInstance()
                        .getParamAnatEntityList().getName()).append("'" +
                "                            form='bgee_anat_sim_form' autofocus " +
                "                            rows='13' cols='35' " +
                "                            placeholder='Enter a list of Uberon IDs'>")
                                        .append(aeText)
                          .append("</textarea>");
        sb.append("            </div>");

        // Species list
        String allSpeciesCheckedTag = userSpeciesIds != null && allSpecies.stream()
                .allMatch(s -> userSpeciesIds.contains(s.getId()))? "checked" : "";
        sb.append("            <div id='bgee_species_list' class='form-group col-sm-7 row'>");
        sb.append("                <span class='col-xs-12 group-title'>Species to define least common ancestor</span>");
        sb.append("                <div class='checkbox col-sm-6'>" +
                "                      <label>" +
                "                          <input type='checkbox' class='select-all' " +allSpeciesCheckedTag + 
                "                               />  Select all</label>" +
                "                  </div>");
        sb.append(                 speciesDisplay);
        sb.append("            </div>");
        
        // Submit
        sb.append("            <input id='bgee_anatsim_submit' class='col-sm-2' type='submit' value='Find'>");

        // Message
        sb.append("            <span id='bgee_anatsim_msg' class='col-sm-10'></span>");

        sb.append("        </form>");
        sb.append("    </div>");
        sb.append("</div>");

        return log.exit(sb.toString());
    }

    private String getResult(AnatEntitySimilarityAnalysis result, Ontology<Taxon, Integer> taxonOntology) {
        log.entry(result, taxonOntology);
        
        StringBuilder sb = new StringBuilder();
        sb.append("<h2>Results</h2>");

        if (!result.getAnatEntitySimilarities().isEmpty()) {
            sb.append("<p>Least common ancestor of provided species: ")
                    .append(htmlEntities(result.getLeastCommonAncestor().getScientificName()))
                    .append("</p>");

            sb.append("<div class='table-container'>");
            sb.append("    <table class='anat-sim-result stripe compact'>");
            sb.append("        <thead>");
            sb.append("            <tr>");
            sb.append("                <th>Anatomical entities</th>");
            sb.append("                <th>Ancestral taxon</th>");
            sb.append("                <th>Presence among selected species</th>");
            sb.append("            </tr>");
            sb.append("        </thead>");
            sb.append("        <tbody>");
            sb.append(result.getAnatEntitySimilarities().stream()
                    .map(sim -> getSimilarityRow(sim, result, taxonOntology))
                    .collect(Collectors.joining()));
            sb.append("        </tbody>");
            sb.append("    </table>");
            sb.append("</div>");
        }

        if (!result.getRequestedAnatEntityIdsNotFound().isEmpty()) {
            sb.append("<p><span class=''>Anatomical entities IDs unknown: ");
            sb.append(result.getRequestedAnatEntityIdsNotFound().stream()
                    .sorted()
                    .map(id -> "'" + htmlEntities(id) + "'")
                    .collect(Collectors.joining(" - ")));
            sb.append("</p>");
        }

        if (!result.getAnatEntitiesWithNoSimilarities().isEmpty()) {
            sb.append("<p>Anatomical entities without anatomical similarity: ");
            sb.append(result.getAnatEntitiesWithNoSimilarities().stream()
                    .sorted(Comparator.comparing(AnatEntity::getName))
                    .map(ae -> getAnatEntityUrl(ae, ae.getName() + " (" + ae.getId() + ")"))
                    .collect(Collectors.joining(" - ")));
            sb.append("</p>");
        }

        return log.exit(sb.toString());
    }

    private String getSimilarityRow(AnatEntitySimilarity sim, AnatEntitySimilarityAnalysis result,
                                    Ontology<Taxon, Integer> taxonOntology) {
        log.entry(sim, result, taxonOntology);

        StringBuilder row = new StringBuilder();
        
        row.append("<tr>");
        row.append("    <td>");
        row.append(sim.getSourceAnatEntities().stream()
                .sorted(Comparator.comparing(AnatEntity::getName))
                .map(ae -> getAnatEntityUrl(ae, ae.getName() + " (" + ae.getId() + ")"))
                .collect(Collectors.joining(" - ")));
        row.append("    </td>");

        row.append("    <td>");
        Set<Taxon> ancestorsAmongElements = taxonOntology.getAncestorsAmongElements(
                sim.getAnnotTaxonSummaries().stream()
                        .map(AnatEntitySimilarityTaxonSummary::getTaxon)
                        .collect(Collectors.toList()), null);
        if (ancestorsAmongElements.size() > 1) {
            log.warn("All taxa should be ancestor or descendant of other taxa.");
        }
        row.append(ancestorsAmongElements.stream()
                .sorted(Comparator.comparing(Taxon::getScientificName))
                .map(t -> htmlEntities(t.getScientificName() + " (" + t.getId() +")"))
                .collect(Collectors.joining("; ")));
        row.append("    </td>");
        
        row.append("    <td>");
        row.append(sim.getSourceAnatEntitiesSortedById().stream()
                .map(ae -> result.getAnatEntitiesExistInSpecies().get(ae))
                .flatMap(Set::stream)
                .distinct()
                .filter(sp -> result.getRequestedSpecies().contains(sp))
                .map(species -> htmlEntities(species.getScientificName()))
                .sorted()
                .collect(Collectors.joining(" - ")));
        row.append("    </td>");

        row.append("</tr>");
        return log.exit(row.toString());
    }

    @Override
    protected void includeCss() {
        log.entry();

        //If you ever add new files, you need to edit bgee-webapp/pom.xml 
        //to correctly merge/minify them.
        if (!this.prop.isMinify()) {
            this.includeCss("lib/jquery_plugins/jquery.dataTables.min.css");
            this.includeCss("lib/jquery_plugins/responsive.dataTables.min.css");
        } else {
            this.includeCss("lib/jquery_plugins/vendor_anat_sim.css");
        }
        this.includeCss("anat_sim.css");

        //we need to add the Bgee CSS files at the end, to override CSS file from external libs
        super.includeCss();

        log.exit();
    }

    @Override
    protected void includeJs() {
        log.entry();

        super.includeJs();
        //If you ever add new files, you need to edit bgee-webapp/pom.xml 
        //to correctly merge/minify them.
        if (!this.prop.isMinify()) {
            this.includeJs("lib/jquery_plugins/jquery.dataTables.min.js");
            this.includeJs("lib/jquery_plugins/dataTables.responsive.min.js");
            this.includeJs("anat_sim.js");
        } else {
            this.includeJs("lib/jquery_plugins/vendor_anat_sim.js");
            this.includeJs("script_anat_sim.js");
        }
        log.exit();
    }
}
