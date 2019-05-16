package org.bgee.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.BgeeProperties;
import org.bgee.controller.URLParameters;

/**
 * Abstract class that has to be extended by any class that generate javascript files based
 * on java classes. The method {@link #writeJSCode()} has to be overridden to produce the 
 * javascript code.
 * 
 * @author Mathieu Seppey
 *
 * @version Bgee 13, Aug 2014
 * @since Bgee 13
 */
public abstract class JSConverter {

    private final static Logger log = LogManager.getLogger(
            JSConverter.class.getName());

    /**
     * A {@code FileWriter} to produce the output file
     */
    private FileWriter writer;

    /**
     * Constructor that has to be invoked by the extending class
     * 
     * @param writer    A {@code FileWriter} to produce the output file
     */
    public JSConverter(FileWriter writer){
        this.writer = writer;
    }

    /**
     * Main method to trigger the generation of a javascript file. Parameters that
     * must be provided in order in {@code args} are:
     * <ol>
     * <li> The name of the class to generate, it can be bgeeproperties or urlparameters
     * <li> The full path to the file that will be generated 
     * ( ! it will be overwritten it if already present  )
     * </ol>
     * 
     * @param args An {@code Array} of {@code String}s containing the requested parameters.
     * @throws IOException              If any problem happen when writing the file on the disk
     * @throws IllegalArgumentException If the provided class to generate is not supported 
     */
    public static void main(String[] args) throws IOException,
    IllegalArgumentException {
        log.entry((Object[]) args);
        int expectedArgLength = 2;
        if (args.length != expectedArgLength) {
            throw log.throwing(new IllegalArgumentException("Incorrect number of " +
                    "arguments provided, expected " + expectedArgLength + " arguments, " +
                    args.length + " provided."));
        }
        generateFile(args[0],new File(args[1]));
        log.exit();
    }

    /**
     * Main method to trigger the generation of a javascript file. 
     * 
     * @param className     The name of the class to generate, it can be bgeeproperties or 
     *                      urlparameters
     * @param targetFile    The full path to the file that will be generated 
     *                      ( ! it will be overwritten it if already present )
     * 
     * @throws IOException              If any problem happen when writing the file on the disk, or
     *                                  the file already exists and could not be deleted.
     * @throws IllegalArgumentException If the provided class to generate is not supported 
     */
    protected static void generateFile(String className, File targetFile) throws IOException, 
    IllegalArgumentException {
        log.entry(className, targetFile);
        if (targetFile.exists()) {
            if (targetFile.delete()) {
                throw log.throwing(new IOException("The previous version of the file could not be deleted"));
            }
        }
        if (!targetFile.createNewFile()) {
            throw log.throwing(new IOException("The file already exists"));
        }
        if(className.equals("bgeeproperties")){
            new BgeePropertiesJsConverter(new FileWriter(targetFile),
                    BgeeProperties.getBgeeProperties()).writeFile();
        }
        else if(className.equals("urlparameters")){
            new URLParametersJsConverter(new FileWriter(targetFile),
                    new URLParameters()).writeFile();
        }
        else{
            throw log.throwing(new IllegalArgumentException("Incorrect class name provided :"
                    + className + ". It should be either bgeeproperties or urlparameters"));
        }
        log.exit();
    }

    /**
     * @throws IOException  If the output {@code File} provided to the {@code FileWriter} 
     *                      is not available for any reason.
     */
    protected void writeFile() throws IOException{
        log.entry();
        this.writeJSCode();
        this.end();
        log.exit();
    }

    /**
     * Method that has to be overridden to define what will be written in the javascript
     * file.
     * 
     * @throws IOException  If the output {@code File} provided to the {@code FileWriter} 
     *                      is not available for any reason.
     */
    protected abstract void writeJSCode() throws IOException;

    /**
     * Write one line with a line separator at the end
     * 
     * @param str           The {@code String} to fill the line to be written
     * @throws IOException  If the output {@code File} provided to the {@code FileWriter} 
     *                      is not available for any reason.
     */
    protected void writeln(String str) throws IOException{
        log.entry(str);
        this.writer.write(str+System.lineSeparator());
        log.exit();
    }

    /**
     * Close the {@code File} properly, to call when the file generation is done
     * 
     * @throws IOException  If the output {@code File} provided to the {@code FileWriter} 
     *                      is not available for any reason.
     */
    protected void end() throws IOException{
        log.entry();
        this.writer.close();
        log.exit();
    }
}
