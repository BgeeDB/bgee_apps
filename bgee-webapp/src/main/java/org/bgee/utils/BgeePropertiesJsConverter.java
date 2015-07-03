package org.bgee.utils;

import java.io.FileWriter;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.BgeeProperties;

/**
 * Class that generates a javascript equivalent of the provided {@link BgeeProperties} class.
 * The aim should be to use an instance similar to the one injected to the {@link FrontController}
 * of bgee-webapp. Note that it is only a subset of the properties that are useful to the
 * javascript. Be careful never to make available a property that should not be publicly available
 * for security reasons.
 * It writes the result in the file provided to the constructor.
 *
 * @author Mathieu Seppey
 *
 * @version Bgee 13, Aug 2014
 * @since Bgee 13
 */
public class BgeePropertiesJsConverter extends JSConverter {

    private final static Logger log = LogManager.getLogger(BgeePropertiesJsConverter
            .class.getName());

    /**
     * The {@code} BgeeProperties instance to use as source
     */
    private final BgeeProperties prop;

    /**
     * Constructor
     * @param writer   A {@code FileWriter} to produce the output file
     * @param prop     The {@code} BgeeProperties instance to use as source 
     */
    public BgeePropertiesJsConverter(FileWriter writer, BgeeProperties prop){
        super(writer);
        this.prop = prop;
    }

    @Override
    public void writeJSCode() throws IOException {
        log.entry();
        this.writeln("/**");
        this.writeln(" * Provides the webapp properties");
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
//        this.writeln("                /**");
//        this.writeln("                 * @return  A {@code String} that defines the root directory where are located javascript ");
//        this.writeln("                 *          files, to be added to the {@code bgeeRootDirectory} to generate URL to obtain ");
//        this.writeln("                 *          javascript files.");
//        this.writeln("                 */");
//        this.writeln("               getJavascriptFilesRootDirectory: function() {");
//        this.writeln("                    return '"+this.prop.getJavascriptFilesRootDirectory()+"';");
//        this.writeln("                },");
//        this.writeln("                ");
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
//        this.writeln("                /**");
//        this.writeln("                 * @return  A {@code String} that defines the directory where are stored TopOBO result files, ");
//        this.writeln("                 *          to be added to the {@code bgeeRootDirectory} to generate URL to obtain result files.");
//        this.writeln("                */");
//        this.writeln("               getTopOBOResultsUrlRootDirectory: function() {");
//        this.writeln("                   return '"+this.prop.getTopOBOResultsUrlRootDirectory()+"';");
//        this.writeln("                },");
//        this.writeln("                ");
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
        this.writeln("       };");
        log.exit();
    }

}
