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
    
    private static String SPARQL_QUERY_EXAMPLE = "http://biosoda.expasy.org:8080/rdf4j-server/"
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
            + "27)%20)%0A%7D&format=JSON&limit=100&offset=0&inference=false";
    
    @Override
    public void displaySparql() {
        
        
        log.entry();

        this.startDisplay("Bgee SPARQL endpoint");

        this.writeln("<div class='row'>");
        this.writeln("<div class='" + CENTERED_ELEMENT_CLASS + "'>");

        this.writeln("<h1>Bgee SPARQL endpoint</h1>");

        this.writeln("<p>The Bgee SPARQL endpoint was created using an Ontology Based Data "
                + "Access (OBDA) approach allowing to create a virtual RDF serialisation without exporting "
                + "data from the original relational database. It queries data stored in the "
                + "<a href = 'https://github.com/BgeeDB/bgee_pipeline/tree/develop/pipeline/dblite_creation' "
                + "class='external_link' title ='link to Bgee lite documentation' target = '_blank'>"
                + "Bgee lite</a> relational database.</p>");
        
        this.writeln("<p>It is reachable by using your preferable programming language or "
                + "SPARQL interface through the URL address below : </p>");
        this.writeln("<p><center>http://biosoda.expasy.org:8080/rdf4j-server/repositories/bgeelight</center></p>");
        
        this.writeln("<p><a href = '" + SPARQL_QUERY_EXAMPLE + "'"
                + "title = 'SPARQL example query' class='external_link' "
                + "target = '_blank'>Example URL</a>retrieving all anatomic entities in human where "
                + "the apoc1 gene is expressed.</p>");
        
        this.writeln("<h1>Web Interface to query the Bgee SPARQL endpoint</h1>");
        
        this.writeln("<p>It is also possible to run Bgee SPARQL queries using the "
                + "<a href = 'http://biosoda.expasy.org:8080/build_biosodafrontend/' title = 'link to Bio-Query'"
                + " class='external_link' target = '_blank'>Bio-Query Federated template</a> search created for "
                + "the BioSODA project. Bgee specific queries are present under the category <code>Bgee "
                + "database queries</code>. It is possible to show the actual SPARQL queries by clicking on the "
                + "<code>Show SPARQL Query Editor</code> button. Bio-Query allows to run federated queries on "
                + "UniProt, OMA and Bgee SPARQL endpoints.</p>");
        this.writeln("<h1>Virtual RDF serialisation and semantic models</h1>");
        this.writeln("The virtual RDF serialisation of the Bgee lite "
                + "database is based on the <a href = 'https://biosoda.github.io/genex/' "
                + "class='external_link' title ='link to GenEx specification' target = '_blank'> GenEx "
                + "semantic model specification</a> and the OBDA mappings defined in <a href = "
                + "'https://github.com/biosoda/bioquery/tree/master/Bgee_OBDA_mappings'"
                + "title ='link to OBDA mapping' target = '_blank'"
                + "class='external_link'> OBDA mappings</a>. The mappings are defined with the "
                + "<a href = 'https://github.com/ontop/ontop/wiki/ontopOBDAModel' "
                + "title ='link to Ontop mapping language' target = '_blank' "
                + "class='external_link'> Ontop mapping language</a>.</p>");
        this.writeln("<p>To cross-reference other resources, this SPARQL endpoint contains annotation "
                + "property assertions defined by a first draft of the life-sciences "
                + "cross-reference (LSCR) ontology that is available to download at the "
                + "<a href = 'https://github.com/qfo/OrthologyOntology' target = '_blank' "
                + "title ='link to Quest for Orthologs github' class='external_link'> Quest for "
                + "Orthologs github</a> repository "
                + "<a href = 'https://github.com/qfo/OrthologyOntology/blob/master/lscr.ttl' "
                + "target = '_blank' title ='link to LSCR ontology' class='external_link'> "
                + "here</a>.</p>");


        this.writeln("</div>"); // close CENTERED_ELEMENT_CLASS class
        this.writeln("</div>"); // close row

        this.endDisplay();

        log.exit();
    }

}
