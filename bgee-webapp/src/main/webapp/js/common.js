/**
 * Main javascript file, entry point for javascripts.
 * 
 * It contains the main functions that are needed by all pages.
 * For the moment, its only purpose is to call the initializer of all other objects 
 * when the page is ready.
 * 
 * Keep the content of this file as simple as possible to keep it readable :
 * - Place the js functions that are specific to a page or a functionality in a specific file,
 *   for example, download.js for ?page=download functions
 * - Use a separate file to group misc. functions if needed.
 * 
 * @author  Mathieu Seppey
 * @version Bgee 13 Aug 2014
 * @since   Bgee 13
 */

/**
 * Call the initializer of the js objects loaded for all pages.
 * 
 * Note : the ready() function can be called a second time by a page specific javascript,
 * for example download.js, for its own initialization. This not a source of problem, ready()
 * can be executed several times without too harmful effects on the perfs. However, be sure that
 * any scripts that uses ready() is loaded after this one, so this function will always be the
 * first piece of javascript code executed.
 * 
 */
$( document ).ready(function() {
    // Init object urlParameters, which provides the list of all parameters allowed in an URL.
    urlParameters.init();
    // Create a requestParameters for the current URL
    var currentRequest = new requestParameters(null,true, "&");
});