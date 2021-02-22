package org.bgee.view.html;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.BgeeProperties;
import org.bgee.controller.RequestParameters;
import org.bgee.view.PublicationDisplay;

/**
 * This class displays the page having the category "publications", i.e. with the parameter
 * page=publications for the HTML view.
 * 
 * @author  Wollbrett J
 * @version Bgee 14, July 2020
 * @since   Bgee 14, July 2020
 **/

public class HtmlPublicationsDisplay extends HtmlParentDisplay implements PublicationDisplay{
    
    private final static Logger log = LogManager.getLogger(HtmlPublicationsDisplay.class.getName());
    
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

    public HtmlPublicationsDisplay(HttpServletResponse response, RequestParameters requestParameters,
            BgeeProperties prop, HtmlFactory factory) throws IllegalArgumentException, IOException {
        super(response, requestParameters, prop, factory);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void displayPublications() {
        log.traceEntry();
        
        this.startDisplay("Publications", "Publications");
        
        this.writeln("<h1 property='schema:name'>Publications</h1>");
        
        this.writeln("<div class='row'>");

        this.writeln("<div class='" + CENTERED_ELEMENT_CLASS + "'>");

        this.writeln("<h2 property='schema:description'>How to cite Bgee?</h2>");
        
        this.writeln("<p>If you find Bgee useful please consider citing:</p>");
        
        this.writeln("<ul>");
        
        this.writeln("<li typeof='schema:ScholarlyArticle'>" + this.getAuthors(Arrays.asList("Bastian FB", 
                        "Roux J", "Niknejad A", "Comte A", 
                        "Fonseca Costa SS", "Mendes de Farias T", "Moretti S", "Parmentier G", 
                        "Rech de Laval V", "Rosikiewicz M", "Wollbrett J", "Echchiki A", 
                        "Escoriza A", "Gharib W", "Gonzales-Porta M", "Jarosz Y", "Laurenczy B", 
                        "Moret P", "Person E", "Roelli P", "Sanjeev K", "Seppey M", 
                        "Robinson-Rechavi M"))
                + "<br><a href='https://doi.org/10.1093/nar/gkaa793' "
                    + " target='_blank' rel='noopener' property='schema:url'> " 
                    + this.getTitle("The Bgee suite: integrated curated expression atlas and comparative "
                            + "transcriptomics in animals") 
                + "</a>"
                + "<br><em>in</em> " + this.getPeriodical("Nucleic Acids Research, Volume 49, "
                        + "Issue D1, 8 January 2021, Pages D831–D847")
                + "<a href='ftp://ftp.bgee.org/general/citation_bgee_suite.ris' property='schema:sameAs'>RIS</a>"
                + "</li>");
        
        this.writeln("</ul>");

        
        this.writeln("<br><p>or choose the publication that best covers the Bgee aspects or components you used in your "
                + "work from the list of publications below. </p>");
        
        this.writeln("<h2>How to cite specific components of Bgee?</h2>");

        this.writeln("<ul>");
        
        this.writeln("<li typeof='schema:ScholarlyArticle'>For the BgeeDB R package: <br>" +  
                this.getAuthors(Arrays.asList("Komljenovic A", "Roux J", "Wollbrett J", "Robinson-Rechavi M", 
                        "Bastian F"))
                + "<br><a href='https://f1000research.com/articles/5-2748/v2' "
                    + " target='_blank' rel='noopener'  property='schema:url'>" 
                    + this.getTitle("BgeeDB, an R package for retrieval of curated expression datasets and "
                            + "for gene list enrichment tests") 
                + "</a>"
                + "<br><em>in</em> " + this.getPeriodical("F1000Research") + " 2018, 5:2748. "
                + "<a href='ftp://ftp.bgee.org/general/citation05.ris' property='schema:sameAs'>RIS</a>"
                + "</li>");
        
        this.writeln("<li typeof='schema:ScholarlyArticle'>For UBERON: <br>" + this.getAuthors(Arrays.asList("Haendel MA", 
                    "Balhoff JP", "Bastian FB",
                    "Blackburn DC", "Blake JA", "Bradford Y", "Comte A", "Dahdul WM", "Dececchi TA",
                    "Druzinsky RE", "Hayamizu TF", "Ibrahim N", "Lewis SE", "Mabee PM", "Niknejad A",
                    "Robinson-Rechavi M", "Sereno PC", "Mungall CJ"))
                + "<br><a href='https://www.ncbi.nlm.nih.gov/pmc/articles/PMC4089931/' "
                    + " target='_blank' rel='noopener' property='schema:url'>"
                    + this.getTitle("Unification of multi-species vertebrate anatomy ontologies for comparative "
                            + "biology in Uberon")
                + "</a>"
                + "<br><em>in</em> " + this.getPeriodical("J Biomed Semantics") + " (2014): 5:21. "
                + "<a href='ftp://ftp.bgee.org/general/citation04.ris'>RIS</a>"
                + "</li>");
        
        this.writeln("</ul>");

        this.writeln("<h2>Other publications</h2>");
        
        this.writeln("<ul>");

        this.writeln("<li typeof='schema:ScholarlyArticle'>" + this.getAuthors(Arrays.asList("Dingerdissen HM", 
                "Bastian FB", "Vijay-Shanker K", "Robinson-Rechavi M", "Bell A", "Gogate N", 
                "Gupta S", "Holmes E", "Kahsay R", "Keeney J", "Kincaid H", "King CH", 
                "Liu D", "Crichton DJ", "Mazumder R"))
                + "<br><a href='https://doi.org/10.1200/CCI.19.00117' "
                    + " target='_blank' rel='noopener' property='schema:url'>" 
                    + this.getTitle("OncoMX: A Knowledgebase for Exploring Cancer Biomarkers in the "
                            + "Context of Related Cancer and Healthy Data")
                + "</a>"
                + "<br><em>in</em> " + this.getPeriodical("JCO Clinical Cancer Informatics") + " 4. 210-220. "
                + "</li>");
        
        this.writeln("<li typeof='schema:ScholarlyArticle'>" + this.getAuthors(Arrays.asList("Chen Q", 
                "Britto R", "Erill I", "Jeffery CJ", "Liberzon A", "Magrane M", 
                "Onami J", "Robinson-Rechavi M", "Sponarova J", "Zobel J", "Karin Verspoor"))
                + "<br><a href='https://doi.org/10.1016/j.gpb.2018.11.006'"
                    + " target='_blank' rel='noopener' property='schema:url'>"
                    + this.getTitle("Quality Matters: Biocuration Experts on the Impact of Duplication and "
                            + "Other Data Quality")
                + "</a>"     
                + "<br><em>in</em> " + this.getPeriodical("Genomics, Proteomics & Bioinformatics")+ " 2020 "
                + "</li>");
        
        this.writeln("<li typeof='schema:ScholarlyArticle'>" + this.getAuthors(Arrays.asList("Robinson-Rechavi M", 
                "Rech de Laval V", "Bastian FB", "Wollbrett J", "Bgee Team"))
                + "<br><a href='https://hal.archives-ouvertes.fr/hal-02535720'"
                    + " target='_blank' rel='noopener' property='schema:url'>" 
                    + this.getTitle("The Expression Comparison Tool in Bgee")
                + "</a>"
                + "<br><em>in</em> " + this.getPeriodical("Scornavacca, Celine; Delsuc, Frédéric; Galtier, Nicolas. "
                        + "Phylogenetics in the Genomic Era")
                + " No commercial publisher | Authors open access book, pp.4.3:1--4.3:4, 2020. "
                + "</li>");
        
        this.writeln("<li typeof='schema:ScholarlyArticle'>" + this.getAuthors(Arrays.asList("Sima AC", 
                "Mendes de Farias T", "Zbinden E", "Anisimova M", "Gil M", "Stockinger H", 
                "Stockinger K", "Robinson-Rechavi M", "Dessimoz C"))
                + "<br><a href='https://doi.org/10.1093/database/baz106' "
                    + " target='_blank' rel='noopener' property='schema:url'>"
                    + this.getTitle("Enabling semantic queries across federated bioinformatics databases")
                + "</a>"
                + "<br><em>in</em> " + this.getPeriodical("Database") + " Volume 2019, 2019, baz106. "
                + "</li>");
        
        this.writeln("<li typeof='schema:ScholarlyArticle'>" + this.getAuthors(Arrays.asList("Mendes de Farias T", 
                "Stockinger H", "Dessimoz C"))
                + "<br><a href='https://doi.org/10.1007/978-3-030-33246-4_38' "
                    + "target='_blank' rel='noopener' property='schema:url'>"
                    + this.getTitle("VoIDext: Vocabulary and Patterns for Enhancing Interoperable Datasets "
                        + "with Virtual Links")
                +"</a>"
                + "<br><em>in</em> " + "On the Move to Meaningful Internet Systems: OTM 2019 Conferences. "
                        + "Lecture Notes in Computer Science. vol 11877. "
                + "</li>");

        this.writeln("<li typeof='schema:ScholarlyArticle'>" + 
                this.getAuthors(Arrays.asList("SIB Swiss Institute of Bioinformatics Members"))
                + "<br><a href='https://doi.org/10.1093/nar/gkv1310'"
                        + " target='_blank' rel='noopener' property='schema:url'>"
                        + this.getTitle("The SIB Swiss Institute of Bioinformatics’ resources: "
                                + "focus on curated databases")
                +"</a>"
                + "<br><em>in</em> " + this.getPeriodical("Nucleic Acids Research")
                        + "Volume 44, Issue D1, 4 January 2016, Pages D27–D37. "
                + "</li>");

        this.writeln("<li typeof='schema:ScholarlyArticle'>" + this.getAuthors(Arrays.asList("Bastian FB", 
                "Chibucos MC", "Gaudet P", "Giglio M", "Holliday GL", "Huang H", 
                "Lewis SE", "Niknejad A", "Orchard S", "Poux S", "Skunca N", 
                "Robinson-Rechavi M"))
                + "<br><a href='https://doi.org/10.1093/database/bav043'"
                        + " target='_blank' rel='noopener' property='schema:url'>"
                        + this.getTitle("The Confidence Information Ontology: a step towards a standard for "
                                + "asserting confidence in annotations")
                + "</a>"
                + "<br><em>in</em> " + this.getPeriodical("Database") + "Volume 2015, 2015. "
                + "</li>");
        
        this.writeln("<li typeof='schema:ScholarlyArticle'>" + this.getAuthors(Arrays.asList("Rosikiewicz M", 
                "Robinson-Rechavi M"))
                + "<br><a href='https://doi.org/10.1093/bioinformatics/btu027'"
                    + " target='_blank' rel='noopener' property='schema:url'>"
                    + this.getTitle("IQRray, a new method for Affymetrix microarray quality control, and "
                            + "the homologous organ conservation score, a new benchmark method for "
                            + "quality control metrics")
                + "</a>"
                + "<br><em>in</em> " + this.getPeriodical("Bioinformatics")
                + " Volume 30, Issue 10, 15 May 2014, Pages 1392–1399. "
                + "</li>");
                
        this.writeln("<li typeof='schema:ScholarlyArticle'>" + this.getAuthors(Arrays.asList("Niknejad A", 
                "Comte A", "Parmentier G", "Roux J", "Bastian FB", "Robinson-Rechavi M"))
                + "<br><a href='https://doi.org/10.1093/bioinformatics/bts048'"
                    + " target='_blank' rel='noopener' property='schema:url'>"
                    + this.getTitle("vHOG, a multispecies vertebrate ontology of homologous organs groups")
                + "</a>"
                + "<br><em>in</em> " + this.getPeriodical("Bioinformatics")
                + "Volume 28, Issue 7, 1 April 2012, Pages 1017–1020. "
                + "</li>");
        
        this.writeln("<li typeof='schema:ScholarlyArticle'>" + this.getAuthors(Arrays.asList("Roux J", 
                "Robinson-Rechavi M"))
                + "<br><a href='https://doi.org/10.1016/j.tig.2009.12.012'"
                    + " target='_blank' rel='noopener' property='schema:url'>"
                    + this.getTitle("An ontology to clarify homology-related concepts")
                + "</a>"
                + "<br><em>in</em> " + this.getPeriodical("Trends Genet") + " 2010;26(3):99-102. "
                + "</li>");

        
        this.writeln("<li typeof='schema:ScholarlyArticle'>" + this.getAuthors(Arrays.asList("Bastian F", 
                "Parmentier G", "Robinson-Rechavi M"))
                + "<br><a href='https://www.nature.com/articles/npre.2009.3546.1' "
                    + " target='_blank' rel='noopener' property='schema:url'>"
                    + this.getTitle("Generating Homology Relationships by Alignment of Anatomical "
                            + "Ontologies")
                + "</a>"
                + "<br><em>in</em> " + this.getPeriodical("Nat Prec (2009)")
                + "</li>");
        
                
        this.writeln("<li typeof='schema:ScholarlyArticle'>" + this.getAuthors(Arrays.asList("Bastian FB", 
                "Parmentier G", "Roux J", "Moretti S", "Laudet V", "Robinson-Rechavi M"))
                + "<br><a href='https://link.springer.com/chapter/10.1007/978-3-540-69828-9_12' "
                    + " target='_blank' rel='noopener' property='schema:url'>"
                    + this.getTitle("Bgee: Integrating and Comparing Heterogeneous Transcriptome "
                            + "Data Among Species")
                + "</a>"
                + "<br><em>in</em> " + this.getPeriodical("DILS: Data Integration in Life Sciences")
                + " Lecture Notes in Computer Science. " + "5109:124-131."
                + "<a href='ftp://ftp.bgee.org/general/citation01.ris' property='schema:sameAs'>RIS</a></li>");

        this.writeln("</ul>");
        
        this.writeln("</div>"); // close specific class
        this.writeln("</div>"); // close row
        
        this.endDisplay();

        log.traceExit();
    }
    
    private String getTitle(String title) {
        log.traceEntry("{}", title);
        return log.traceExit("<span property='schema:headline'>" + title + "</span>.");
    }

    private String getAuthors(List<String> names) {
        log.traceEntry("{}", names);
        return log.traceExit(names.stream().map(this::getAuthor).collect(Collectors.joining(", ", "", ".")));
    }

    private String getAuthor(String name) {
        log.traceEntry("{}", name);
        return log.traceExit(
                "<span property='schema:author' typeof='schema:Person'>" +
                "    <span property='schema:name'>" + name + "</span>" +
                "</span>");
    }

    private String getPeriodical(String journalName) {
        log.traceEntry("{}", journalName);
        return log.traceExit(
                "<span property='schema:isPartOf' typeof='schema:Periodical'>" +
                "    <span property='schema:name'>" + journalName + "</span>" +
                "</span>.");
    }
    

}
