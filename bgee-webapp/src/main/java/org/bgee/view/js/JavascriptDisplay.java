package org.bgee.view.js;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.BgeeProperties;
import org.bgee.controller.RequestParameters;
import org.bgee.controller.URLParameters.Parameter;
import org.bgee.controller.utils.BgeeStringUtils;
import org.bgee.view.ConcreteDisplayParent;

/**
 * This class displays the output of all javascript files that are dynamically generated
 * within Bgee webapp.
 * 
 * @author  Mathieu Seppey
 * @version Bgee 13 Aug 2014
 * @since   Bgee 13
 */
public class JavascriptDisplay extends ConcreteDisplayParent
{

    private final static Logger log = LogManager.getLogger(JavascriptDisplay.class.getName());

    /**
     *  The {@code RequestParameters} that handles the parameters of the 
     *  current request.
     */
    private final RequestParameters requestParameters;

    /**
     * Constructor 
     * 
     * @param response          A {@code HttpServletResponse} that will be used to display the 
     *                          page to the client
     * @param requestParameters The {@code RequestParameters} that handles the parameters of the 
     *                          current request.
     * @param prop              A {@code BgeeProperties} instance that contains the properties
     *                          to use.
     * 
     * @throws IOException      If there is an issue when trying to get or to use the
     *                          {@code PrintWriter} 
     */
    public JavascriptDisplay(HttpServletResponse response, RequestParameters requestParameters, 
            BgeeProperties prop) throws IOException
    {
        super(response, prop);
        this.requestParameters = requestParameters;
    }

    @Override
    public void sendHeaders(boolean ajax) {
        log.exit(ajax);
        if (this.response == null) {
            return;
        }
        if (!this.headersAlreadySent) {
            this.response.setContentType("application/javascript");
            if (ajax) {
                this.response.setDateHeader("Expires", 1);
                this.response.setHeader("Cache-Control", 
                        "no-store, no-cache, must-revalidate, proxy-revalidate");
                this.response.addHeader("Cache-Control", "post-check=0, pre-check=0");
                this.response.setHeader("Pragma", "No-cache");
            }
            this.headersAlreadySent = true;
        }
        log.exit();
    }

    /**
     * Display the javascript equivalent of {@link org.bgee.controller.BgeeProperties}
     * It is only a selection of the properties that are useful for the js and that can
     * be public without security issue, for example {@code javascriptFilesRootDirectory} but
     * not {@code requestParametersStorageDirectory}
     */
    public void displayBgeeProperties() {
        log.entry();
        this.writeln("/**");
        this.writeln(" * Provides the properties of the Bgee webapp that are needed"
                +" by the js scripts");
        this.writeln(" * @author  Mathieu Seppey");
        this.writeln(" * @version Bgee 13 Aug 2014");
        this.writeln(" * @since   Bgee 13");
        this.writeln(" **/");
        this.writeln("var bgeeProperties = {");
        this.writeln("                ");
        this.writeln("                /**");
        this.writeln("                 * @return  A {@code String} that defines the root of URLs to Bgee, for instance, ");
        this.writeln("                 *          'http://bgee.unil.ch/bgee/bgee'.");
        this.writeln("                 */");
        this.writeln("                getBgeeRootDirectory: function() {");
        this.writeln("                    return '"+this.prop.getBgeeRootDirectory()+"';");
        this.writeln("                },");
        this.writeln("                ");
        this.writeln("                /**");
        this.writeln("                 * @return  A {@code String} that defines the root directory where are located files ");
        this.writeln("                 *          available for download, to be added to the {@code bgeeRootDirectory} to ");
        this.writeln("                 *          generate URL to download files");
        this.writeln("                 */");
        this.writeln("                getDownloadRootDirectory: function() {");
        this.writeln("                    return '"+this.prop.getDownloadRootDirectory()+"';");
        this.writeln("                },");
        this.writeln("                ");
        this.writeln("                /**");
        this.writeln("                 * @return  A {@code String} that defines the root directory where are located javascript ");
        this.writeln("                 *          files, to be added to the {@code bgeeRootDirectory} to generate URL to obtain ");
        this.writeln("                 *          javascript files.");
        this.writeln("                 */");
        this.writeln("               getJavascriptFilesRootDirectory: function() {");
        this.writeln("                    return '"+this.prop.getJavascriptFilesRootDirectory()+"';");
        this.writeln("                },");
        this.writeln("                ");
        this.writeln("                /**");
        this.writeln("                 * @return  A {@code String} that defines the root directory where are located css files, ");
        this.writeln("                 *          to be added to the {@code bgeeRootDirectory} to generate URL to obtain css files.");
        this.writeln("                 */");
        this.writeln("                getCssFilesRootDirectory: function() {");
        this.writeln("                    return '"+this.prop.getCssFilesRootDirectory()+"';");
        this.writeln("                },");
        this.writeln("                ");
        this.writeln("                /**");
        this.writeln("                 * @return  A {@code String} that defines the root directory where are located images, ");
        this.writeln("                 *          to be added to the {@code bgeeRootDirectory} to generate URL to obtain images.");
        this.writeln("                 */");
        this.writeln("                getImagesRootDirectory: function() {");
        this.writeln("                    return '"+this.prop.getImagesRootDirectory()+"';");
        this.writeln("                },");
        this.writeln("                ");
        this.writeln("                /**");
        this.writeln("                 * @return  A {@code String} that defines the directory where are stored TopOBO result files, ");
        this.writeln("                 *          to be added to the {@code bgeeRootDirectory} to generate URL to obtain result files.");
        this.writeln("                */");
        this.writeln("               getTopOBOResultsUrlRootDirectory: function() {");
        this.writeln("                   return '"+this.prop.getTopOBOResultsUrlRootDirectory()+"';");
        this.writeln("                },");
        this.writeln("                ");
        this.writeln("                /**");
        this.writeln("                 * @return  An {@code Integer} that definesmax length of URLs. Typically, if the URL ");
        this.writeln("                 *          exceeds the max length, a key is generated to store and retrieve a query ");
        this.writeln("                 *          string, holding the 'storable' parameters. The 'storable' parameters are");
        this.writeln("                 *          removed from the URL, and replaced by the generated key.");
        this.writeln("                 */");
        this.writeln("               getUrlMaxLength: function() {");
        this.writeln("                   return '"+this.prop.getUrlMaxLength()+"';");
        this.writeln("                },");
        this.writeln("               ");
        this.writeln("               /**");
        this.writeln("                 * @return  A {@code boolean} that defines whether parameters should be url encoded ");
        this.writeln("                 *          by the {@code encodeUrl} method.");
        this.writeln("                 */");
        this.writeln("                isEncodeUrl: function() {");
        this.writeln("                    return '"+this.prop.isEncodeUrl()+"';");
        this.writeln("               }");
        this.writeln("       };");
        log.exit();
    }
    /**
     * Display the javascript equivalent of {@link org.bgee.controller.URLParameters}
     */
    public void displayURLParameters() {
        log.entry();
        this.writeln("/**");
        this.writeln(" * Provide an instance of each parameters that can be passed through the URL.");
        this.writeln(" * A Parameter does not contains the actual value from the URL for the parameter,");
        this.writeln(" * but provides all the configuration of the parameter and is used as key to store"
                + " the value, see requestparameters.js");
        this.writeln(" * @author  Mathieu Seppey");
        this.writeln(" * @version Bgee 13 Aug 2014");
        this.writeln(" * @since   Bgee 13");
        this.writeln(" **/");
        this.writeln("var urlParameters = {");
        this.writeln("");
        this.writeln("        /**");
        this.writeln("         * Constructor");
        this.writeln("         *                                                                                    ");
        this.writeln("         * @param name                    A {@code String} that is the name of the parameter"); 
        this.writeln("         *                                as seen in an URL");
        this.writeln("         * @param allowsMultipleValues    A {@code Boolean} that indicates whether ");
        this.writeln("         *                                the parameter accepts multiple values.");
        this.writeln("         * @param isStorable              A {@code boolean} defining whether the parameter ");
        this.writeln("         *                                is storable.");
        this.writeln("         * @param isSecure                A {@code boolean} defining whether the parameter ");
        this.writeln("         *                                is secure.");
        this.writeln("         * @param maxSize                 An {@code int} that represents the maximum number ");
        this.writeln("         *                                of characters allowed if the type of this ");
        this.writeln("         *                                {@code Parameter} is a {@code String}.");
        this.writeln("         * @param format                  A {@code String} that contains the regular expression ");
        this.writeln("         *                                that this parameter has to fit to.");
        this.writeln("         * @param type                    A {@code Class<T>} that is the data type of the value ");
        this.writeln("         *                                to be store by this parameter.");
        this.writeln("         **/");
        this.writeln("        Parameter: function(name,allowsMultipleValues,isStorable,isSecure,"
                + "maxSize,format,type) {");
        this.writeln("            this.name = name ;");
        this.writeln("            this.allowsMultipleValues = allowsMultipleValues;");
        this.writeln("            this.isStorable = isStorable ;");
        this.writeln("            this.isSecure = isSecure ;");
        this.writeln("            this.maxSize = maxSize ;");
        this.writeln("            this.format = format ;");
        this.writeln("            this.type = type ;");
        this.writeln("        },");
        this.writeln("");
        this.writeln("        /**");
        this.writeln("         * Initialization of all Parameters allowed in Bgee");
        this.writeln("         **/");           
        this.writeln("        init: function(){");
        this.writeln("            this.list = [];");
        for(Parameter<?> p : this.requestParameters.getURLParametersInstance().getList()){
            this.writeln("            this."+BgeeStringUtils.upperCase(p.getName())+" = "
                    + "new urlParameters.Parameter('"+p.getName()+"',"+p.allowsMultipleValues()+",'"
                    +p.isStorable()+"','"+p.isSecure()+"','"+p.getMaxSize()+"','"+p.getFormat()+"','"
                    +p.getType()+"');");
            this.writeln("            this.list.push(this."+BgeeStringUtils.upperCase(p.getName())+");");
        }
        this.writeln("        },");
        this.writeln("");
        for(Parameter<?> p : this.requestParameters.getURLParametersInstance().getList()){
            this.writeln("        /**");
            this.writeln("         * @return    The parameter "+p.getName());
            this.writeln("         */");
            this.writeln("        getParam"+this.toCamelCase(p.getName())+": function(){");
            this.writeln("            return this."+BgeeStringUtils.upperCase(p.getName())+";");
            this.writeln("        },");
        }
        this.writeln("        /**");
        this.writeln("         * @return A {@code List<Parameter<T>>} to list all declared"
                + " {@code Parameter<T>}");
        this.writeln("         */");
        this.writeln("        getList: function() {");
        this.writeln("            return this.list;");
        this.writeln("        }");
        this.writeln("");
        this.writeln("};");
        log.exit();
    }
    /**
     * 
     */
    private String toCamelCase(String name){
        String camelCaseName = "";
        for(String word : name.split("_")){
            camelCaseName += BgeeStringUtils.upperCase(word.substring(0,1))+word.substring(1);
        }
        return camelCaseName;
    }

}

