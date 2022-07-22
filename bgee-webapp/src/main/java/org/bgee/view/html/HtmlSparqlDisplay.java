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

    private final String sparql_query_json_url;
    private final String sparql_query_xml_url;
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
        this.sparql_query_json_url = this.prop.getSparqlCurrentUrl()
            + "?default-graph-uri=&"
            + "query=PREFIX+orth%3A+%3Chttp%3A%2F%2Fpurl.org%2Fnet%2Forth%23%3E%0D%0APREFIX+up"
            + "%3A+%3Chttp%3A%2F%2Fpurl.uniprot.org%2Fcore%2F%3E%0D%0APREFIX+genex%3A+%3Chttp%"
            + "3A%2F%2Fpurl.org%2Fgenex%23%3E%0D%0APREFIX+obo%3A+%3Chttp%3A%2F%2Fpurl.obolibra"
            + "ry.org%2Fobo%2F%3E%0D%0ASELECT+DISTINCT+%3FanatEntity+%3FanatName+%7B%0D%0A++++"
            + "%3Fseq+a+orth%3AGene+.%0D%0A++++%3Fseq+rdfs%3Alabel+%3FgeneName+.%0D%0A++++%3Fs"
            + "eq+genex%3AisExpressedIn+%3Fcond+.%0D%0A++++%3Fcond+genex%3AhasAnatomicalEntity"
            + "+%3FanatEntity+.%0D%0A++++%3FanatEntity+rdfs%3Alabel+%3FanatName+.%0D%0A++++%3F"
            + "cond+obo%3ARO_0002162+%3Chttp%3A%2F%2Fpurl.uniprot.org%2Ftaxonomy%2F10116%3E+.+"
            + "%0D%0A++++FILTER+%28LCASE%28%3FgeneName%29+%3D+LCASE%28%27APOC1%27%29%29%0D%0A%"
            + "7D&should-sponge=&format=application%2Fsparql-results%2Bjson&timeout=0&debug=on"
            + "&run=+Run+Query+";
        this.sparql_query_xml_url = this.prop.getSparqlCurrentUrl()
            + "?default-graph-uri=&q"
            + "uery=PREFIX+orth%3A+%3Chttp%3A%2F%2Fpurl.org%2Fnet%2Forth%23%3E%0D%0APREFIX+up%"
            + "3A+%3Chttp%3A%2F%2Fpurl.uniprot.org%2Fcore%2F%3E%0D%0APREFIX+genex%3A+%3Chttp%3"
            + "A%2F%2Fpurl.org%2Fgenex%23%3E%0D%0APREFIX+obo%3A+%3Chttp%3A%2F%2Fpurl.obolibrar"
            + "y.org%2Fobo%2F%3E%0D%0ASELECT+DISTINCT+%3FanatEntity+%3FanatName+%7B%0D%0A++++%"
            + "3Fseq+a+orth%3AGene+.%0D%0A++++%3Fseq+rdfs%3Alabel+%3FgeneName+.%0D%0A++++%3Fse"
            + "q+genex%3AisExpressedIn+%3Fcond+.%0D%0A++++%3Fcond+genex%3AhasAnatomicalEntity+"
            + "%3FanatEntity+.%0D%0A++++%3FanatEntity+rdfs%3Alabel+%3FanatName+.%0D%0A++++%3Fc"
            + "ond+obo%3ARO_0002162+%3Chttp%3A%2F%2Fpurl.uniprot.org%2Ftaxonomy%2F10116%3E+.+%"
            + "0D%0A++++FILTER+%28LCASE%28%3FgeneName%29+%3D+LCASE%28%27APOC1%27%29%29%0D%0A%7"
            + "D&should-sponge=&format=application%2Fsparql-results%2Bxml&timeout=0&debug=on&r"
            + "un=+Run+Query+";
    }

    @Override
    public void displaySparql() {
        log.traceEntry();

        String easyBgeeDocUrl = MASTER_BGEE_PIPELINE_GITHUB_URL + "/pipeline/easybgee_creation";
        
        this.startDisplay("Bgee SPARQL endpoint", "WebPage");

        this.writeln("<div class='row'>");
        this.writeln("<div class='" + CENTERED_ELEMENT_CLASS + "'>");

        this.writeln("<h1 property='schema:name'>Bgee SPARQL endpoint</h1>");

        this.writeln("<p property='schema:description'>Bgee has a SPARQL endpoint which is based on the "
                + EASY_BGEE_NAME + " database (<a href='" + easyBgeeDocUrl
                + "' class='external_link' target='_blank' rel='noopener' title='Link to "
                + EASY_BGEE_NAME + " documentation'>see documentation on Bgee pipeline github</a>). "
                + EASY_BGEE_NAME + " is a view of the Bgee database that contains most useful, "
                + "and explicit information.</p>");

        RequestParameters urlCollabs = this.getNewRequestParameters();
        urlCollabs.setPage(RequestParameters.PAGE_COLLABORATIONS);
        
        this.writeln("<h2>Web interface to query the Bgee SPARQL endpoint</h2>");
        this.writeln("<p>Bgee SPARQL queries can be run using the web interface "
                + "<a href='http://biosoda.expasy.org' "
                + "title='Link to Bio-Query' class='external_link' target='_blank' rel='noopener'>Bio-Query</a> "
                + "search created for the <a href='" + urlCollabs.getRequestURL()
                + "' title='Bgee collaborations'>BioSODA project</a>. "
                + "Bgee specific queries are present under the category <span class='bioquery-section'>" 
                + "Bgee database queries</span>. It is possible to see the SPARQL queries and edit them " 
                + "by clicking on the <span class='bioquery-button'>Show SPARQL Query Editor</span> " 
                + "button. Moreover, Bio-Query allows for writing federated queries among "
                + "UniProt, OMA and Bgee SPARQL endpoints.</p>");

        this.writeln("<h2>Programmatic access to the latest version of the Bgee SPARQL endpoint</h2>");
        if(this.prop.isArchive()) {
            this.writeln("<div class='alert alert-warning'> This is an archived version of Bgee. If you want "
                    + "to access the Bgee SPARQL endpoint specifically for this archived version, "
                    + "please go to section <a href='#" + RequestParameters.HASH_SPARQL_STABLE + "' "
                    + "title='jump to stable SPARQL endpoint documentation'>"
                    + "Stable programmatic access to this version of the Bgee SPARQL endpoint</a>.</div>");
        }
        this.writeln("<p>The latest version of the Bgee SPARQL endpoint is accessible by using your prefered " +
                "programming language through the URL address below: </p>" +
                "<p class='endpoint-url'>" +
                "<a href='" + this.prop.getSparqlCurrentUrl()
                + "' title='Link to Bgee SPARQL endpoint' class='external_link' target='_blank' rel='noopener'>"
                + this.prop.getSparqlCurrentUrl() + "</a></p>");

        this.writeln("<p>For example, to retrieve all anatomic entities in Rattus norvegicus where " +
                "the APOC1 gene is expressed, the query is:</p>");
        this.writeln("<pre><code>" +
                "PREFIX orth: &lt;http://purl.org/net/orth#&gt;<br>" +
                "PREFIX up: &lt;http://purl.uniprot.org/core/&gt;<br>" +
                "PREFIX genex: &lt;http://purl.org/genex#&gt;<br>" +
                "PREFIX obo: &lt;http://purl.obolibrary.org/obo/&gt;<br>" +
                "SELECT DISTINCT ?anatEntity ?anatName {<br>" +
                "    ?seq a orth:Gene .<br>" +
                "    ?seq rdfs:label ?geneName .<br>" +
                "    ?seq genex:isExpressedIn ?cond .<br>" +
                "    ?cond genex:hasAnatomicalEntity ?anatEntity .<br>" +
                "    ?anatEntity rdfs:label ?anatName .<br>" +
                "    ?cond obo:RO_0002162 &lt;http://purl.uniprot.org/taxonomy/10116&gt; . <br>" +
                "    FILTER (LCASE(?geneName) = LCASE('APOC1'))<br>" +
                "}" +
                "</code></pre>");

        this.writeln("<p>It is possible to download the result of this query in <a href='" + sparql_query_json_url + "' "
                + "title='SPARQL example query' class='external_link' target='_blank' rel='noopener'>JSON format</a> "
                + "or in <a href='" + sparql_query_xml_url + "' title='SPARQL example query' "
                + "class='external_link' target='_blank' rel='noopener'>XML format</a>.</p>");

        this.writeln("<p>(Of note, as opposed to the example below to access an archived version, "
                + "when accessing the endpoint for the latest version, it is important "
                + "<strong>NOT</strong> to specify the name of a graph to target; otherwise, "
                + "results will be incorrect)</p>");

        this.writeln("<h2 id='" + RequestParameters.HASH_SPARQL_STABLE +"'>Stable programmatic access to this version of the Bgee SPARQL endpoint</h2>");

        this.writeln("<p>This version of the Bgee SPARQL endpoint is accessible in a stable manner "
                + "by using your prefered programming language through the stable URL address below: </p>"
                + "<p class='endpoint-url'><a href='" + this.prop.getSparqlStableUrl()
                + "' title='Link to Bgee SPARQL endpoint' class='external_link' target='_blank' rel='noopener'>"
                + this.prop.getSparqlStableUrl() + "</a></p>");

        this.writeln("<p>In the SELECT section of your query, it is essential to specify the URL of the graph you want "
                + "to query (" + this.prop.getSparqlStableGraph() + "), otherwise you won't be using the data "
                + "for this version. For example, to retrieve "
                + "all anatomic entities in Rattus norvegicus where the APOC1 gene is expressed, the query is:</p>");
        this.writeln("<pre><code>" +
                "PREFIX orth: &lt;http://purl.org/net/orth#&gt;<br>" +
                "PREFIX up: &lt;http://purl.uniprot.org/core/&gt;<br>" +
                "PREFIX genex: &lt;http://purl.org/genex#&gt;<br>" +
                "PREFIX obo: &lt;http://purl.obolibrary.org/obo/&gt;<br>" +
                "SELECT DISTINCT ?anatEntity ?anatName  FROM &lt;" + this.prop.getSparqlStableGraph() + "&gt; {<br>" +
                "    ?seq a orth:Gene .<br>" +
                "    ?seq rdfs:label ?geneName .<br>" +
                "    ?seq genex:isExpressedIn ?cond .<br>" +
                "    ?cond genex:hasAnatomicalEntity ?anatEntity .<br>" +
                "    ?anatEntity rdfs:label ?anatName .<br>" +
                "    ?cond obo:RO_0002162 &lt;http://purl.uniprot.org/taxonomy/10116&gt; . <br>" +
                "    FILTER (LCASE(?geneName) = LCASE('APOC1'))<br>" +
                "}" +
                "</code></pre>");

        this.writeln("<p>Again, <strong>it is essential to specify the name of the graph of the version "
                + "to target</strong> (in the example above, " + this.prop.getSparqlStableGraph() + "); "
                + "otherwise, " + "results will be incorrect.</p>");

        this.writeln("<h2>RDF serialisation and semantic models</h2>");
        this.writeln("<p>The Bgee RDF data were created using an Ontology Based Data Access (OBDA) "
                + "approach so-called Ontop. The RDF serialisation of the '" + EASY_BGEE_NAME
                + "' database is based on the <a href='https://biosoda.github.io/genex/' "
                + "class='external_link' title ='Link to GenEx specification' target='_blank' "
                + "rel='noopener'> GenEx semantic model specification</a> and the OBDA mappings "
                + "defined in <a href='https://github.com/biosoda/bioquery/tree/master/Bgee_OBDA_mappings'"
                + " title='Link to OBDA mapping' target='_blank' rel='noopener' "
                + "class='external_link'>OBDA mappings</a>. The mappings are defined using the "
                + "<a href='https://github.com/ontop/ontop/wiki/ontopOBDAModel' "
                + "title='Link to Ontop mapping language' target='_blank' rel='noopener' "
                + "class='external_link'>Ontop mapping language</a>. We also inferred all implicit "
                + "information based on <a href='https://www.w3.org/TR/owl2-profiles/#OWL_2_QL' "
                + "title='Link to OWL2 QL profile' target='_blank' rel='noopener' "
                + "class='external_link'>OWL 2 Web Ontology Language Profile QL</a>  reasoning "
                + "over GenEx.</p>");
        this.writeln("<p>To cross-reference other resources, this SPARQL endpoint contains annotation "
                + "property assertions defined by a first draft of the life-sciences "
                + "cross-reference (LSCR) ontology that is available to download at the "
                + "<a href='https://github.com/qfo/OrthologyOntology' target='_blank' rel='noopener' class='external_link'"
                + " title='Link to Quest for Orthologs github'> Quest for Orthologs GitHub</a> repository "
                + "<a href='https://github.com/qfo/OrthologyOntology/blob/master/lscr.ttl' "
                + "target='_blank' rel='noopener' class='external_link' title='link to LSCR ontology'> "
                + "here</a>.</p>");
        
        this.writeln("</div>"); // close CENTERED_ELEMENT_CLASS class
        this.writeln("</div>"); // close row

        this.endDisplay();

        log.traceExit();
    }

    @Override
    protected void includeCss() {
        log.traceEntry();

        this.includeCss("sparql.css");
        
        //we need to add the Bgee CSS files at the end, to override CSS file from external libs
        super.includeCss();

        log.traceExit();
    }
}

