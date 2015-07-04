/**
 * Provides the properties used by the Bgee javascript code, 
 * and convenient methods to use them. 
 * 
 * @author  Frederic Bastian
 * @author  Mathieu Seppey
 * @version Bgee 13 Jul 2015
 * @since   Bgee 13
 **/
var bgeeProperties = {
           
    //*************************************
	// BGEE PROPERTIES USED BY JAVASCRIPT
	//*************************************
    /**
     * @return  A {@code String} that defines the host to use for generating 
     * 			URLs to Bgee, for instance, 'bgee.org:8080'. By default, 
     * 			it corresponds to the host that generated the page 
     * 			({@code location.host}), but it could be changed 
     * 			to perform cross-domain queries to a different Bgee host.
     * 			You must not include a trailing '/' in this property.
     * 			For definitions of URL parts, see http://bl.ocks.org/abernier/3070589 
     */
    getBgeeHost: function() {
        return location.host;
    },
    /**
     * @return  A {@code String} that defines the path to query the Bgee web-app, 
     * 			to be added to the host used (see {@link #getBgeeHost()}), 
     * 			e.g., '/bgee_v13/'. By default, 
     * 			it corresponds to the path used when generating the page 
     * 			({@code location.pathname}), but it could be 
     * 			changed to perform cross-domain queries to a different Bgee host.
     * 			This property must start and end with '/'.
     * 			For definitions of URL parts, see http://bl.ocks.org/abernier/3070589.
     */
    getWebAppPath: function() {
        return location.pathname;
    },
    /**
     * @return  A {@code String} that defines the path to retrieve Bgee images, 
     * 			to be added to the host used (see {@link #getBgeeHost()}), 
     * 			e.g., '/bgee/images/'.
     * 			This property must start and end with '/'.
     * 			For definitions of URL parts, see http://bl.ocks.org/abernier/3070589.
     */
    getImgPath: function() {
        return '/img/';
    }, 
    
    
    //**********************************
    // METHODS PROVIDED FOR CONVENIENCE
    //**********************************
    /**
     * @return  A {@code String} that defines the beginning of the URL to used to query 
     * 			the Bgee web-app, before the search part, e.g. 'http://bgee.org/bgee_v13/'.
     * 			This method is provided for convenience, it simply concatenates 
     * 			{@code 'http://' + getBgeeHost() + getWebAppPath()}.
     * 			For definitions of URL parts, see http://bl.ocks.org/abernier/3070589.
     */
    getWebAppURLStart: function() {
        return 'http://' + bgeeProperties.getBgeeHost() + bgeeProperties.getWebAppPath();
    }, 
    /**
     * @return  A {@code String} that defines the beginning of the URL to used to retrieve 
     * 			Bgee images, be.g. 'http://bgee.org/bgee/img/'.
     * 			This method is provided for convenience, it simply concatenates 
     * 			{@code 'http://' + getBgeeHost() + getImgPath()}.
     * 			For definitions of URL parts, see http://bl.ocks.org/abernier/3070589.
     */
    getImgURLStart: function() {
        return 'http://' + bgeeProperties.getBgeeHost() + bgeeProperties.getImgPath();
    }
};