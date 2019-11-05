package org.bgee.view.html;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.BgeeProperties;
import org.bgee.controller.RequestParameters;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * This class encapsulates the documentation for data sets. 
 * It does not implement any interface from the {@code view} package, it is meant to be used 
 * by the class {@link HtmlDocumentationDisplay}. The only reason is that the class 
 * {@code HtmlDocumentationDisplay} was getting too large and too complicated. 
 *
 * @author  Valentine Rech de Laval
 * @see 	HtmlDocumentationDisplay
 * @version Bgee 14, May 2018
 * @since   Bgee 14, May 2018
 */
public class HtmlDocumentationDataSets extends HtmlDocumentationDownloadFile {
    
    private static final Logger log = LogManager.getLogger(HtmlDocumentationDataSets.class.getName());

    /**
     * Default constructor.
     *
     * @param response          A {@code HttpServletResponse} that will be used to display the
     *                          page to the client
     * @param requestParameters The {@code RequestParameters} that handles the parameters of the
     *                          current request.
     * @param prop              A {@code BgeeProperties} instance that contains the properties
     *                          to use.
     * @param factory           The {@code HtmlFactory} that instantiated the
     *                          {@link HtmlDocumentationDisplay} object using this object.
     * @throws IllegalArgumentException If {@code factory} is {@code null}.
     * @throws IOException              If there is an issue when trying to get or to use the
     *                                  {@code PrintWriter}
     */
    protected HtmlDocumentationDataSets(HttpServletResponse response, RequestParameters requestParameters,
                                        BgeeProperties prop, HtmlFactory factory)
            throws IllegalArgumentException, IOException {
        super(response, requestParameters, prop, factory);
    }

    /**
     * Write the documentation for data sets imported in Bgee. This method is not responsible 
     * for calling the methods {@code startDisplay} and {@code endDisplay}. Otherwise, 
     * all other elements of the page are written by this method.
     *
     * @see HtmlDocumentationDisplay#displayDataSets() 
     */
    protected void writeDocumentation() {
        log.entry();

        RequestParameters urlExprCalls = this.getNewRequestParameters();
        urlExprCalls.setPage(RequestParameters.PAGE_DOWNLOAD);
        urlExprCalls.setAction(RequestParameters.ACTION_DOWLOAD_CALL_FILES);

        RequestParameters urlSearch = this.getNewRequestParameters();
        urlSearch.setPage(RequestParameters.PAGE_GENE);

        RequestParameters urlTopAnat = this.getNewRequestParameters();
        urlTopAnat.setPage(RequestParameters.PAGE_TOP_ANAT);
        
        this.writeln("<h1 property='schema:name'>GTEx data into Bgee</h1>");

        this.writeln("<div id='bgee_introduction' property='schema:description'>"
                + "<p>In addition to the continuous growth of transcriptomics datasets, "
                + "some specific projects produce large amounts of data, generated and accessible "
                + "in a consistent manner, as, notably, "
                + "the <a href='https://www.gtexportal.org/home/' title='GTEx portal' target='_blank' rel='noopener noreferrer'>" +
                "GTEx project</a>. The GTEx project aims at building a comprehensive resource for tissue-specific "
                + "gene expression in human. Here we describe how this dataset was integrated into Bgee.</p></div>");

        this.writeln("<h2>Annotation process</h2>");
        this.writeln("<div class='doc_content'>");
        this.writeln("    <p>We applied a stringent re-annotation process to the GTEx data to retain " +
                "only healthy tissues and non-contaminated samples, using the information available " +
                "under restricted-access. For instance, we rejected all samples for 31&#37; of subjects, " +
                "deemed globally unhealthy from the pathology report (e.g., drug abuse, diabetes, BMI &#62; 35), " +
                "as well as specific samples from another 28&#37; of subjects who had local pathologies " +
                "(e.g., brain from Alzheimer patients). We also rejected samples with contamination " +
                "from other tissues.</p>" +
                "        <p>In total, only 50&#37; of samples were kept; these represent a high quality " +
                "subset of GTEx. All these samples were re-annotated manually to specific Uberon " +
                "anatomy and aging terms.</p>");
        this.writeln("</div>");

        this.writeln("<h2>GTEx data into Bgee</h2>");
        this.writeln("<div class='doc_content'>");
        this.writeln("<p>All corresponding RNA-seq were reanalyzed in the Bgee pipeline, " +
                "consistently with all other healthy RNA-seq from human and other species. " +
                "These data are being made available both through the website, " +
                "and through <a href='https://bioconductor.org/packages/release/bioc/html/BgeeDB.html' " +
                "class='external_link' target='_blank' rel='noopener noreferrer'>BgeeDB R package</a> " +
                "(with sensitive information hidden).</p>");
        this.writeln("</div>");

        this.writeln("<h3>GTEx data into our website</h3>");
        this.writeln("<div class='doc_content'>");
        this.writeln("<ul>");
        this.writeln("  <li>Annotations can be retrieved from <a href='"+ this.prop.getFTPRootDirectory() +
                "/download/processed_expr_values/rna_seq/Homo_sapiens/Homo_sapiens_RNA-Seq_experiments_libraries.tar.gz' " +
                "title='Retrieve human RNA-Seq data per experiment'>RNA-Seq human experiments/libraries info</a>. " +
                "Experiment ID of GTEx is 'SRP012682'.</li>");
        this.writeln("  <li>Processed expression values, from GTEx only, are available on our FTP " +
                "(<a href='" + this.prop.getFTPRootDirectory() + "/download/processed_expr_values/" +
                "rna_seq/Homo_sapiens/Homo_sapiens_RNA-Seq_read_counts_TPM_FPKM_SRP012682.tsv.tar.gz'>" +
                "download file</a>).</li>");
        this.writeln("  <li>Gene expression calls are included into <a href='" + urlExprCalls.getRequestURL() +
                "#id1'>human files</a></li>");
        this.writeln("  <li>Each human gene page includes GTEx data if there is any " +
                "(search a gene <a href='" + urlSearch.getRequestURL() + "'>here</a>).</li>");
        this.writeln("  <li>TopAnat analyses can be performs <a href='" + urlTopAnat.getRequestURL() +
                "'>here</a>, which leverage the power of the abundant GTEx data integrated " +
                "with many smaller datasets to provide biological insight into gene lists.</li>");
        this.writeln("</ul>");
        this.writeln("</div>");

        this.writeln("<h3>GTEx data using BgeeDB R package</h3>");
        this.writeln("<div class='doc_content'>");
        this.writeln("<p>More information and examples can be found on the <a href='https://bioconductor.org/packages/BgeeDB/' "
                + "title='Link to BgeeDB on Bioconductor' target='_blank' rel='noopener noreferrer'>BgeeDB R package page</a>.</p>");
        this.writeln("<ul>");
        this.writeln("  <li>Annotations can be retrieved from RNA-Seq human experiments/libraries " +
                "information. Experiment ID of GTEx is 'SRP012682'.");
        this.writeln("<pre><code>    source(\"https://bioconductor.org/biocLite.R\")");
        this.writeln("    biocLite(\"BgeeDB\")");
        this.writeln("    library(BgeeDB)");
        this.writeln("    bgee <- Bgee$new(species = \"Homo_sapiens\", dataType = \"rna_seq\")");
        this.writeln("    myAnnotation <- getAnnotation(bgee)");
        this.writeln("</code></pre></li>");

        this.writeln("  <li>Quantitative expression data and presence calls for GTEx can be loaded.");
        this.writeln("<pre><code>    bgee <- Bgee$new(species = \"Homo_sapiens\", dataType = \"rna_seq\")");
        this.writeln("    # This step can take a lot of time as all Bgee GTEx data have to be downloaded and uncompressed.");
        this.writeln("    dataGTEx <- getData(bgee, experimentId = \"SRP012682\")");
        this.writeln("</code></pre></li>");

        this.writeln("  <li>TopAnat analyses can be performs, which leverage the power of the " +
                "abundant GTEx data integrated with many smaller datasets to provide biological " +
                "insight into gene lists.");
        this.writeln("<pre><code>    bgee <- Bgee$new(species = \"Homo_sapiens\")");
        this.writeln("    myTopAnatData <- loadTopAnatData(bgee)");
        this.writeln("    # Retrieve all genes with data in Bgee");
        this.writeln("    allGenes <- unique(row.names(myTopAnatData$gene2anatomy))");
        this.writeln("    # List of genes related to autism and epilepsy from Jabbari 2016");
        this.writeln("    genesOfInterest <- c(\"ENSG00000183044\", \"ENSG00000085563\", \"ENSG00000006071\", \"ENSG00000153086\", ");
        this.writeln("    \"ENSG00000243989\", \"ENSG00000156110\", \"ENSG00000150594\", \"ENSG00000239900\", ");
        this.writeln("    \"ENSG00000141385\", \"ENSG00000038002\", \"ENSG00000142208\", \"ENSG00000275199\", ");
        this.writeln("    \"ENSG00000117020\", \"ENSG00000163631\", \"ENSG00000159423\", \"ENSG00000112294\", ");
        this.writeln("    \"ENSG00000164904\", \"ENSG00000033011\", \"ENSG00000182858\", \"ENSG00000101901\", ");
        this.writeln("    \"ENSG00000119523\", \"ENSG00000214160\", \"ENSG00000088035\", \"ENSG00000159063\", ");
        this.writeln("    \"ENSG00000086848\", \"ENSG00000242110\", \"ENSG00000137074\", \"ENSG00000124198\", ");
        this.writeln("    \"ENSG00000118520\", \"ENSG00000198844\", \"ENSG00000131089\", \"ENSG00000100299\", ");
        this.writeln("    \"ENSG00000113273\", \"ENSG00000004848\", \"ENSG00000104763\", \"ENSG00000108381\", ");
        this.writeln("    \"ENSG00000066279\", \"ENSG00000138363\", \"ENSG00000159363\", \"ENSG00000018625\", ");
        this.writeln("    \"ENSG00000174437\", \"ENSG00000182220\", \"ENSG00000185344\", \"ENSG00000171953\", ");
        this.writeln("    \"ENSG00000175054\", \"ENSG00000158321\", \"ENSG00000086062\", \"ENSG00000103507\", ");
        this.writeln("    \"ENSG00000074582\", \"ENSG00000176697\", \"ENSG00000157764\", \"ENSG00000106009\", ");
        this.writeln("    \"ENSG00000164061\", \"ENSG00000169814\", \"ENSG00000111678\", \"ENSG00000130921\", ");
        this.writeln("    \"ENSG00000131943\", \"ENSG00000197603\", \"ENSG00000141837\", \"ENSG00000007402\", ");
        this.writeln("    \"ENSG00000182389\", \"ENSG00000198668\", \"ENSG00000143933\", \"ENSG00000147044\", ");
        this.writeln("    \"ENSG00000036828\", \"ENSG00000110395\", \"ENSG00000015133\", \"ENSG00000108691\", ");
        this.writeln("    \"ENSG00000136861\", \"ENSG00000008086\", \"ENSG00000064309\", \"ENSG00000151849\", ");
        this.writeln("    \"ENSG00000103995\", \"ENSG00000173575\", \"ENSG00000100888\", \"ENSG00000168539\", ");
        this.writeln("    \"ENSG00000181072\", \"ENSG00000120903\", \"ENSG00000101204\", \"ENSG00000175344\", ");
        this.writeln("    \"ENSG00000274542\", \"ENSG00000160716\", \"ENSG00000114859\", \"ENSG00000073464\", ");
        this.writeln("    \"ENSG00000186510\", \"ENSG00000184908\", \"ENSG00000188603\", \"ENSG00000102805\", ");
        this.writeln("    \"ENSG00000128973\", \"ENSG00000182372\", \"ENSG00000278220\", \"ENSG00000184144\", ");
        this.writeln("    \"ENSG00000278728\", \"ENSG00000174469\", \"ENSG00000166685\", \"ENSG00000168434\", ");
        this.writeln("    \"ENSG00000213380\", \"ENSG00000142173\", \"ENSG00000173085\", \"ENSG00000088682\", ");
        this.writeln("    \"ENSG00000006695\", \"ENSG00000014919\", \"ENSG00000047457\", \"ENSG00000165078\", ");
        this.writeln("    \"ENSG00000157184\", \"ENSG00000169372\", \"ENSG00000147571\", \"ENSG00000160213\", ");
        this.writeln("    \"ENSG00000064601\", \"ENSG00000117984\", \"ENSG00000174080\", \"ENSG00000115827\", ");
        this.writeln("    \"ENSG00000077279\", \"ENSG00000100150\", \"ENSG00000181192\", \"ENSG00000091140\", ");
        this.writeln("    \"ENSG00000101152\", \"ENSG00000116675\", \"ENSG00000116641\", \"ENSG00000172269\", ");
        this.writeln("    \"ENSG00000000419\", \"ENSG00000136908\", \"ENSG00000179085\", \"ENSG00000188641\", ");
        this.writeln("    \"ENSG00000197102\", \"ENSG00000157540\", \"ENSG00000101210\", \"ENSG00000096093\", ");
        this.writeln("    \"ENSG00000111361\", \"ENSG00000119718\", \"ENSG00000070785\", \"ENSG00000115211\", ");
        this.writeln("    \"ENSG00000145191\", \"ENSG00000170370\", \"ENSG00000133216\", \"ENSG00000112425\", ");
        this.writeln("    \"ENSG00000178607\", \"ENSG00000140374\", \"ENSG00000105379\", \"ENSG00000171503\", ");
        this.writeln("    \"ENSG00000103089\", \"ENSG00000122591\", \"ENSG00000145982\", \"ENSG00000091483\", ");
        this.writeln("    \"ENSG00000112367\", \"ENSG00000196924\", \"ENSG00000162769\", \"ENSG00000119686\", ");
        this.writeln("    \"ENSG00000110195\", \"ENSG00000170345\", \"ENSG00000125740\", \"ENSG00000176165\", ");
        this.writeln("    \"ENSG00000160973\", \"ENSG00000087086\", \"ENSG00000179163\", \"ENSG00000022355\", ");
        this.writeln("    \"ENSG00000166206\", \"ENSG00000187730\", \"ENSG00000113327\", \"ENSG00000054983\", ");
        this.writeln("    \"ENSG00000141012\", \"ENSG00000130005\", \"ENSG00000171766\", \"ENSG00000105607\", ");
        this.writeln("    \"ENSG00000140905\", \"ENSG00000131095\", \"ENSG00000170266\", \"ENSG00000178445\", ");
        this.writeln("    \"ENSG00000074047\", \"ENSG00000145888\", \"ENSG00000109738\", \"ENSG00000173540\", ");
        this.writeln("    \"ENSG00000087258\", \"ENSG00000159921\", \"ENSG00000111670\", \"ENSG00000090581\", ");
        this.writeln("    \"ENSG00000135677\", \"ENSG00000108433\", \"ENSG00000171723\", \"ENSG00000233276\", ");
        this.writeln("    \"ENSG00000176884\", \"ENSG00000183454\", \"ENSG00000273079\", \"ENSG00000152822\", ");
        this.writeln("    \"ENSG00000169919\", \"ENSG00000138796\", \"ENSG00000170445\", \"ENSG00000172534\", ");
        this.writeln("    \"ENSG00000164588\", \"ENSG00000138622\", \"ENSG00000213614\", \"ENSG00000049860\", ");
        this.writeln("    \"ENSG00000165102\", \"ENSG00000153187\", \"ENSG00000158104\", \"ENSG00000174775\", ");
        this.writeln("    \"ENSG00000276536\", \"ENSG00000072506\", \"ENSG00000114378\", \"ENSG00000181873\", ");
        this.writeln("    \"ENSG00000010404\", \"ENSG00000127415\", \"ENSG00000134049\", \"ENSG00000166333\", ");
        this.writeln("    \"ENSG00000124313\", \"ENSG00000150995\", \"ENSG00000120071\", \"ENSG00000278458\", ");
        this.writeln("    \"ENSG00000275867\", \"ENSG00000111262\", \"ENSG00000169282\", \"ENSG00000069424\", ");
        this.writeln("    \"ENSG00000184408\", \"ENSG00000140015\", \"ENSG00000151704\", \"ENSG00000177807\", ");
        this.writeln("    \"ENSG00000187486\", \"ENSG00000156113\", \"ENSG00000281151\", \"ENSG00000075043\", ");
        this.writeln("    \"ENSG00000184156\", \"ENSG00000107147\", \"ENSG00000243335\", \"ENSG00000068796\", ");
        this.writeln("    \"ENSG00000276734\", \"ENSG00000168280\", \"ENSG00000185467\", \"ENSG00000118162\", ");
        this.writeln("    \"ENSG00000133703\", \"ENSG00000087299\", \"ENSG00000196569\", \"ENSG00000143815\", ");
        this.writeln("    \"ENSG00000108231\", \"ENSG00000121897\", \"ENSG00000138095\", \"ENSG00000187391\", ");
        this.writeln("    \"ENSG00000169032\", \"ENSG00000126934\", \"ENSG00000109339\", \"ENSG00000204406\", ");
        this.writeln("    \"ENSG00000090674\", \"ENSG00000147316\", \"ENSG00000169057\", \"ENSG00000081189\", ");
        this.writeln("    \"ENSG00000164073\", \"ENSG00000168282\", \"ENSG00000100427\", \"ENSG00000124615\", ");
        this.writeln("    \"ENSG00000164172\", \"ENSG00000129255\", \"ENSG00000178802\", \"ENSG00000177000\", ");
        this.writeln("    \"ENSG00000198793\", \"ENSG00000196091\", \"ENSG00000108784\", \"ENSG00000072864\", ");
        this.writeln("    \"ENSG00000275911\", \"ENSG00000125356\", \"ENSG00000131495\", \"ENSG00000023228\", ");
        this.writeln("    \"ENSG00000213619\", \"ENSG00000164258\", \"ENSG00000115286\", \"ENSG00000110717\", ");
        this.writeln("    \"ENSG00000167792\", \"ENSG00000049759\", \"ENSG00000223957\", \"ENSG00000204386\", ");
        this.writeln("    \"ENSG00000234343\", \"ENSG00000228691\", \"ENSG00000227129\", \"ENSG00000184494\", ");
        this.writeln("    \"ENSG00000227315\", \"ENSG00000234846\", \"ENSG00000196712\", \"ENSG00000151092\", ");
        this.writeln("    \"ENSG00000187566\", \"ENSG00000087303\", \"ENSG00000164190\", \"ENSG00000156574\", ");
        this.writeln("    \"ENSG00000074181\", \"ENSG00000141458\", \"ENSG00000119655\", \"ENSG00000122585\", ");
        this.writeln("    \"ENSG00000185149\", \"ENSG00000213281\", \"ENSG00000179915\", \"ENSG00000079482\", ");
        this.writeln("    \"ENSG00000116329\", \"ENSG00000112038\", \"ENSG00000187848\", \"ENSG00000135124\", ");
        this.writeln("    \"ENSG00000007168\", \"ENSG00000125779\", \"ENSG00000173599\", \"ENSG00000165194\", ");
        this.writeln("    \"ENSG00000160299\", \"ENSG00000131828\", \"ENSG00000148459\", \"ENSG00000164494\", ");
        this.writeln("    \"ENSG00000127980\", \"ENSG00000108733\", \"ENSG00000142655\", \"ENSG00000121680\", ");
        this.writeln("    \"ENSG00000164751\", \"ENSG00000215193\", \"ENSG00000034693\", \"ENSG00000139197\", ");
        this.writeln("    \"ENSG00000124587\", \"ENSG00000112357\", \"ENSG00000102144\", \"ENSG00000156531\", ");
        this.writeln("    \"ENSG00000092621\", \"ENSG00000165195\", \"ENSG00000108474\", \"ENSG00000165282\", ");
        this.writeln("    \"ENSG00000124155\", \"ENSG00000060642\", \"ENSG00000121879\", \"ENSG00000184381\", ");
        this.writeln("    \"ENSG00000182621\", \"ENSG00000123560\", \"ENSG00000140650\", \"ENSG00000039650\", ");
        this.writeln("    \"ENSG00000108439\", \"ENSG00000140521\", \"ENSG00000115138\", \"ENSG00000131238\", ");
        this.writeln("    \"ENSG00000102103\", \"ENSG00000139174\", \"ENSG00000163637\", \"ENSG00000100033\", ");
        this.writeln("    \"ENSG00000167371\", \"ENSG00000197746\", \"ENSG00000185920\", \"ENSG00000179295\", ");
        this.writeln("    \"ENSG00000172053\", \"ENSG00000151552\", \"ENSG00000155961\", \"ENSG00000132155\", ");
        this.writeln("    \"ENSG00000108557\", \"ENSG00000146282\", \"ENSG00000078328\", \"ENSG00000167281\", ");
        this.writeln("    \"ENSG00000189056\", \"ENSG00000163933\", \"ENSG00000155906\", \"ENSG00000104889\", ");
        this.writeln("    \"ENSG00000136104\", \"ENSG00000172922\", \"ENSG00000067836\", \"ENSG00000151835\", ");
        this.writeln("    \"ENSG00000101347\", \"ENSG00000138760\", \"ENSG00000144285\", \"ENSG00000105711\", ");
        this.writeln("    \"ENSG00000136531\", \"ENSG00000153253\", \"ENSG00000183873\", \"ENSG00000196876\", ");
        this.writeln("    \"ENSG00000169432\", \"ENSG00000130489\", \"ENSG00000073578\", \"ENSG00000178980\", ");
        this.writeln("    \"ENSG00000152217\", \"ENSG00000127990\", \"ENSG00000181523\", \"ENSG00000164690\", ");
        this.writeln("    \"ENSG00000108061\", \"ENSG00000138083\", \"ENSG00000064651\", \"ENSG00000124140\", ");
        this.writeln("    \"ENSG00000119899\", \"ENSG00000135917\", \"ENSG00000106688\", \"ENSG00000110436\", ");
        this.writeln("    \"ENSG00000079215\", \"ENSG00000102743\", \"ENSG00000125454\", \"ENSG00000177542\", ");
        this.writeln("    \"ENSG00000117394\", \"ENSG00000164414\", \"ENSG00000117620\", \"ENSG00000181830\", ");
        this.writeln("    \"ENSG00000076351\", \"ENSG00000144290\", \"ENSG00000142319\", \"ENSG00000276996\", ");
        this.writeln("    \"ENSG00000165970\", \"ENSG00000130821\", \"ENSG00000198689\", \"ENSG00000072501\", ");
        this.writeln("    \"ENSG00000108055\", \"ENSG00000166311\", \"ENSG00000102172\", \"ENSG00000163877\", ");
        this.writeln("    \"ENSG00000115904\", \"ENSG00000104450\", \"ENSG00000152583\", \"ENSG00000166068\", ");
        this.writeln("    \"ENSG00000197694\", \"ENSG00000102359\", \"ENSG00000126091\", \"ENSG00000115525\", ");
        this.writeln("    \"ENSG00000124356\", \"ENSG00000123473\", \"ENSG00000136854\", \"ENSG00000144455\", ");
        this.writeln("    \"ENSG00000139531\", \"ENSG00000148290\", \"ENSG00000008056\", \"ENSG00000197283\", ");
        this.writeln("    \"ENSG00000227460\", \"ENSG00000102003\", \"ENSG00000198198\", \"ENSG00000164458\", ");
        this.writeln("    \"ENSG00000136463\", \"ENSG00000143374\", \"ENSG00000054611\", \"ENSG00000162065\", ");
        this.writeln("    \"ENSG00000145979\", \"ENSG00000184058\", \"ENSG00000143178\", \"ENSG00000196628\", ");
        this.writeln("    \"ENSG00000177426\", \"ENSG00000175606\", \"ENSG00000061938\", \"ENSG00000166340\", ");
        this.writeln("    \"ENSG00000213689\", \"ENSG00000165699\", \"ENSG00000103197\", \"ENSG00000154743\", ");
        this.writeln("    \"ENSG00000274672\", \"ENSG00000275165\", \"ENSG00000274796\", \"ENSG00000274078\", ");
        this.writeln("    \"ENSG00000278712\", \"ENSG00000273896\", \"ENSG00000170892\", \"ENSG00000274129\", ");
        this.writeln("    \"ENSG00000278605\", \"ENSG00000278622\", \"ENSG00000182173\", \"ENSG00000175894\", ");
        this.writeln("    \"ENSG00000104833\", \"ENSG00000131462\", \"ENSG00000128159\", \"ENSG00000198431\", ");
        this.writeln("    \"ENSG00000114062\", \"ENSG00000104517\", \"ENSG00000173218\", \"ENSG00000137411\", ");
        this.writeln("    \"ENSG00000236178\", \"ENSG00000234032\", \"ENSG00000206476\", \"ENSG00000230985\", ");
        this.writeln("    \"ENSG00000223494\", \"ENSG00000213585\", \"ENSG00000165637\", \"ENSG00000197969\", ");
        this.writeln("    \"ENSG00000141252\", \"ENSG00000196998\", \"ENSG00000075702\", \"ENSG00000186153\", ");
        this.writeln("    \"ENSG00000169554\", \"ENSG00000043355\")");

        this.writeln("    # Build the gene vector for the analysis");
        this.writeln("    geneList <- factor(as.integer(unique(allGenes) %in% genesOfInterest))");
        this.writeln("    names(geneList) <- unique(allGenes)");
        this.writeln("    # Run the test");
        this.writeln("    myTopAnatObject <- topAnat(myTopAnatData, geneList)");
        this.writeln("    resFis <- runTest(myTopAnatObject, algorithm =\"elim\", statistic =\"fisher\")");
        this.writeln("    # Format results");
        this.writeln("    tableOver <- makeTable(myTopAnatData, myTopAnatObject, resFis, 0.1)");
        this.writeln("    tableOver");
        this.writeln("</code></pre></li>");
        this.writeln("</ul>");
        this.writeln("</div>");

        log.exit();
    }
}
