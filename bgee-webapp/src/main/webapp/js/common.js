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
$(document).ready(function() {
    // Init object urlParameters, which provides the list of all parameters allowed in an URL.
    urlParameters.init();
    // Create a requestParameters for the current URL
    var currentRequest = new requestParameters(null,true, "&");
    
    // Add a listener in navbar to change caret image when mouse over 'li'
    var $deployLi =  $( "#nav li .deploy" );
    $deployLi.mouseover( function() {
    	// Change img
    	$("img", this).attr('src', '/img/arrow_down_light.png');
    });
    $deployLi.mouseout( function() {
    	// Change img
    	$("img", this).attr('src', '/img/arrow_down_dark.png');
    });
    
//    var contactLi =  document.getElementById('contact');
//    $contactLi.innerHTML += getBgeeContact();

});

function getBgeeContact() {
	return eval(unescape('%66%75%6E%63%74%69%6F%6E%20%74%72%61%6E%73%70%6F%73%65%32%30%28%68%29%20%7B%76%61%72%20%73%3D%27%61%6D%6C%69%6F%74%42%3A%65%67%40%65%73%69%2D%62%69%73%2E%62%68%63%27%3B%76%61%72%20%72%3D%27%27%3B%66%6F%72%28%76%61%72%20%69%3D%30%3B%69%3C%73%2E%6C%65%6E%67%74%68%3B%69%2B%2B%2C%69%2B%2B%29%7B%72%3D%72%2B%73%2E%73%75%62%73%74%72%69%6E%67%28%69%2B%31%2C%69%2B%32%29%2B%73%2E%73%75%62%73%74%72%69%6E%67%28%69%2C%69%2B%31%29%7D%68%2E%68%72%65%66%3D%72%3B%7D%64%6F%63%75%6D%65%6E%74%2E%77%72%69%74%65%28%27%3C%61%20%68%72%65%66%3D%22%23%22%20%6F%6E%4D%6F%75%73%65%4F%76%65%72%3D%22%6A%61%76%61%73%63%72%69%70%74%3A%74%72%61%6E%73%70%6F%73%65%32%30%28%74%68%69%73%29%22%20%6F%6E%46%6F%63%75%73%3D%22%6A%61%76%61%73%63%72%69%70%74%3A%74%72%61%6E%73%70%6F%73%65%32%30%28%74%68%69%73%29%22%20%74%69%74%6C%65%3D%22%43%6F%6E%74%61%63%74%20%75%73%22%20%63%6C%61%73%73%3D%22%6D%65%6E%75%22%3E%43%6F%6E%74%61%63%74%3C%2F%61%3E%27%29%3B'));
}
