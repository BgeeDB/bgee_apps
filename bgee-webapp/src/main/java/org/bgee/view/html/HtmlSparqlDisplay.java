package org.bgee.view.html;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.BgeeProperties;
import org.bgee.controller.RequestParameters;
import org.bgee.view.SparqlDisplay;

/**
 * This class displays the page having the category "sparql", i.e. with the parameter
 * page=sparql for the HTML view.
 * 
 * @author  Julien Wollbrett
 * @author  Valentine Rech de Laval
 * @version Bgee 14, May 2019
 * @since   Bgee 14, May 2019
 **/

public class HtmlSparqlDisplay extends HtmlParentDisplay implements SparqlDisplay{

    private final static Logger log = LogManager.getLogger(HtmlSparqlDisplay.class.getName());
    
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
    public HtmlSparqlDisplay(HttpServletResponse response, RequestParameters requestParameters,
            BgeeProperties prop, HtmlFactory factory) throws IllegalArgumentException, IOException {
        super(response, requestParameters, prop, factory);
    }

    private static String SPARQL_QUERY_JSON_URL = "http://biosoda.expasy.org:8080/rdf4j-server/"
            + "repositories/bgeelight?query=PREFIX%20orth%3A%20%3Chttp%3A%2F%2Fpurl.org%2Fnet%2"
            + "Forth%23%3E%0APREFIX%20up%3A%20%3Chttp%3A%2F%2Fpurl.uniprot.org%2Fcore%2F%3E%0AP"
            + "REFIX%20genex%3A%20%3Chttp%3A%2F%2Fpurl.org%2Fgenex%23%3E%0APREFIX%20obo%3A%20%3"
            + "Chttp%3A%2F%2Fpurl.obolibrary.org%2Fobo%2F%3E%0ASELECT%20DISTINCT%20%3FanatEntit"
            + "y%20%3FanatName%20%7B%0A%09%3Fseq%20a%20orth%3AGene%20.%0A%09%3Fexpr%20genex%3Ah"
            + "asSequenceUnit%20%3Fseq%20.%0A%09%3Fseq%20rdfs%3Alabel%20%3FgeneName%20.%0A%09%3"
            + "Fexpr%20genex%3AhasExpressionCondition%20%3Fcond%20.%0A%09%3Fcond%20genex%3AhasA"
            + "natomicalEntity%20%3FanatEntity%20.%0A%09%3FanatEntity%20rdfs%3Alabel%20%3FanatN"
            + "ame%20.%0A%09%3Fcond%20obo%3ARO_0002162%20%3Chttp%3A%2F%2Fpurl.uniprot.org%2Ftax"
            + "onomy%2F9606%3E%20.%20%0A%09FILTER%20(LCASE(%3FgeneName)%20%3D%20LCASE(%27apoc1%"
            + "27)%20)%0A%7D&Accept=application%2Fjson&limit=100&offset=0&inference=false";

    private static String SPARQL_QUERY_XML_URL = "http://biosoda.expasy.org:8080/rdf4j-server/"
            + "repositories/bgeelight?query=PREFIX%20orth%3A%20%3Chttp%3A%2F%2Fpurl.org%2Fnet%2"
            + "Forth%23%3E%0APREFIX%20up%3A%20%3Chttp%3A%2F%2Fpurl.uniprot.org%2Fcore%2F%3E%0AP"
            + "REFIX%20genex%3A%20%3Chttp%3A%2F%2Fpurl.org%2Fgenex%23%3E%0APREFIX%20obo%3A%20%3"
            + "Chttp%3A%2F%2Fpurl.obolibrary.org%2Fobo%2F%3E%0ASELECT%20DISTINCT%20%3FanatEntit"
            + "y%20%3FanatName%20%7B%0A%09%3Fseq%20a%20orth%3AGene%20.%0A%09%3Fexpr%20genex%3Ah"
            + "asSequenceUnit%20%3Fseq%20.%0A%09%3Fseq%20rdfs%3Alabel%20%3FgeneName%20.%0A%09%3"
            + "Fexpr%20genex%3AhasExpressionCondition%20%3Fcond%20.%0A%09%3Fcond%20genex%3AhasA"
            + "natomicalEntity%20%3FanatEntity%20.%0A%09%3FanatEntity%20rdfs%3Alabel%20%3FanatN"
            + "ame%20.%0A%09%3Fcond%20obo%3ARO_0002162%20%3Chttp%3A%2F%2Fpurl.uniprot.org%2Ftax"
            + "onomy%2F9606%3E%20.%20%0A%09FILTER%20(LCASE(%3FgeneName)%20%3D%20LCASE(%27apoc1%"
            + "27)%20)%0A%7D&Accept=application%2Fxml&limit=100&offset=0&inference=false";

    @Override
    public void displaySparql() {
        log.entry();

        String bgeeLiteDocUrl = DEVELOP_BGEE_PIPELINE_GITHUB_URL + "/pipeline/dblite_creation";
        
        this.startDisplay("Bgee SPARQL endpoint");

        this.writeln("<div class='row'>");
        this.writeln("<div class='" + CENTERED_ELEMENT_CLASS + "'>");

        this.writeln("<h1>Bgee SPARQL endpoint</h1>");

        this.writeln("<p>Bgee has a SPARQL endpoint which is based on the " +
                "<a href='" + bgeeLiteDocUrl + "' class='external_link' target='_blank' " +
                "title='Link to Bgee lite documentation'>" + BGEE_LITE_NAME + " database</a>. " +
                BGEE_LITE_NAME + " is a lighter version of Bgee database, " +
                "that contains most useful, and explicit information.</p>");

        RequestParameters urlCollabs = this.getNewRequestParameters();
        urlCollabs.setPage(RequestParameters.PAGE_COLLABORATIONS);
        
        this.writeln("<h2>Web interface to query the Bgee SPARQL endpoint</h2>");
        this.writeln("<p>Bgee SPARQL queries can be run using the web interface "
                + "<a href='http://biosoda.expasy.org:8080/build_biosodafrontend/' " 
                + "title='Link to Bio-Query' class='external_link' target='_blank'>Bio-Query</a> " 
                + "search created for the <a href='"+urlCollabs.getRequestURL()+"' " 
                + "title='Bgee collaborations'>BioSODA project</a>. " 
                + "Bgee specific queries are present under the category <span class='bioquery-section'>" 
                + "Bgee database queries</span>. It is possible to see the SPARQL queries and edit them " 
                + "by clicking on the <span class='bioquery-button'>Show SPARQL Query Editor</span> " 
                + "button. Moreover, Bio-Query allows to do federated queries between "
                + "UniProt, OMA and Bgee SPARQL endpoints.</p>");

        this.writeln("<h2>Programmatic access to the Bgee SPARQL endpoint</h2>");
        this.writeln("<p>The Bgee SPARQL endpoint is accessible by using your prefered " +
                "programming language through the URL address below: </p>" +
                "<p class='endpoint-url'>" +
                "http://biosoda.expasy.org:8080/rdf4j-server/repositories/bgeelight</p>");

        this.writeln("<p>For example, to retrieve all anatomic entities in human where " +
                "the APOC1 gene is expressed, the query is:");
        this.writeln("<pre><code>" +
                "PREFIX orth: &lt;http://purl.org/net/orth#&gt;<br>" +
                "PREFIX up: &lt;http://purl.uniprot.org/core/&gt;<br>" +
                "PREFIX genex: &lt;http://purl.org/genex#&gt;<br>" +
                "PREFIX obo: &lt;http://purl.obolibrary.org/obo/&gt;<br>" +
                "SELECT DISTINCT ?anatEntity ?anatName {<br>" +
                "    ?seq a orth:Gene .<br>" +
                "    ?expr genex:hasSequenceUnit ?seq .<br>" +
                "    ?seq rdfs:label ?geneName .<br>" +
                "    ?expr genex:hasExpressionCondition ?cond .<br>" +
                "    ?cond genex:hasAnatomicalEntity ?anatEntity .<br>" +
                "    ?anatEntity rdfs:label ?anatName .<br>" +
                "    ?cond obo:RO_0002162 &lt;http://purl.uniprot.org/taxonomy/10116&gt; . <br>" +
                "    FILTER (LCASE(?geneName) = LCASE('APOC1'))<br>" +
                "}" +
                "</code></pre>");

        this.writeln("It's possible to download result of this query in <a href='" + SPARQL_QUERY_JSON_URL + "' "
                + "title='SPARQL example query' class='external_link' target='_blank'>JSON format</a> "
                + "or in <a href='" + SPARQL_QUERY_XML_URL + "' title='SPARQL example query' " 
                + "class='external_link' target='_blank'>XML format</a>.</p>");

        this.writeln("<h2>Virtual RDF serialisation and semantic models</h2>");
        this.writeln("<p>The Bgee SPARQL endpoint was created using an Ontology Based Data Access (OBDA) "
                + "approach allowing to create a virtual RDF serialisation without exporting data "
                + "from the original relational database. It queries data stored in the "
                + "<a href='" + bgeeLiteDocUrl + "' class='external_link' target='_blank' " 
                + "title='Link to Bgee lite documentation'>" + BGEE_LITE_NAME + "</a> relational database.");
        this.writeln("The virtual RDF serialisation of the " + BGEE_LITE_NAME
                + " is based on the <a href='https://biosoda.github.io/genex/' class='external_link' "
                + "title ='Link to GenEx specification' target='_blank'>GenEx semantic model "
                + "specification</a> and the OBDA mappings defined in <a href="
                + "'https://github.com/biosoda/bioquery/tree/master/Bgee_OBDA_mappings'"
                + "title='Link to OBDA mapping' target='_blank' class='external_link'>"
                + "OBDA mappings</a>. The mappings are defined with the "
                + "<a href='https://github.com/ontop/ontop/wiki/ontopOBDAModel' "
                + "title='Link to Ontop mapping language' target = '_blank' class='external_link'>"
                + "Ontop mapping language</a>.</p>");
        this.writeln("<p>To cross-reference other resources, this SPARQL endpoint contains annotation "
                + "property assertions defined by a first draft of the life-sciences "
                + "cross-reference (LSCR) ontology that is available to download at the "
                + "<a href='https://github.com/qfo/OrthologyOntology' target='_blank' class='external_link'"
                + "title='Link to Quest for Orthologs github'> Quest for Orthologs GitHub</a> repository "
                + "<a href='https://github.com/qfo/OrthologyOntology/blob/master/lscr.ttl' "
                + "target='_blank' class='external_link' title='link to LSCR ontology'> "
                + "here</a>.</p>");
        
        this.writeln("</div>"); // close CENTERED_ELEMENT_CLASS class
        this.writeln("</div>"); // close row

        this.endDisplay();

        log.exit();
    }

    @Override
    protected void includeCss() {
        log.entry();

        this.includeCss("sparql.css");
        
        //we need to add the Bgee CSS files at the end, to override CSS file from external libs
        super.includeCss();

        log.exit();
    }
}
