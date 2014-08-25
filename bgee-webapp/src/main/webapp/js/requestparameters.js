/**
 * requestParameters handles the parameter that are passed through the URL. It checks the validity
 * of the Parameter (see urlparameters.js), add/remove/reset parameters and generates URL
 * TODO : in progress, and comment still in java style
 * @author Mathieu Seppey
 * @author Frederic Bastian
 * @version Bgee 13, Jul 2014
 * @since Bgee 13
 */
var requestParameters = {

        /**
         * initialization. Load the parameters from the request
         */
        init: function() {
            this.values = new Array();
            this.loadParametersFromRequest();
        },
        /**
         * Load the parameters that are present in the URL
         */
        loadParametersFromRequest: function(){
            // Browse all available parameters
            var parametersAssocArr = this.getQueryStringVars(window.location.search.substr(1), 
                    true);
            for (i in urlParameters.getList()){    
                var parameter = urlParameters.getList()[i];
                // Fetch the string values from the URL
                valuesFromUrl = parametersAssocArr[parameter.name]
                // If the param is set, initialize an List to receive the values 
                // and browse them
                if(valuesFromUrl != null){
                    if(!parameter.allowsMultipleValues && valuesFromUrl instanceof Array){
                        // TODO catch or not catch ?
                        // TODO other check, like format/secure string
                        // Or not ? because it is already check on the server side for this step
                        throw(parameter.name + " does not accept mutliple values");
                    }
                    this.values[parameter.name] = valuesFromUrl;
                }
            }
        },
        /**
         * Add a value to the given {@code URLParameters.Parameter<T>}
         *  
         * @param parameter The {@code URLParameters.Parameter<T>} to add the value to
         * 
         * @param value     A {@code T}, the value to set
         */    
        addValue: function(parameter, value){

            // Secure the value
            if(value != null){
                value = bgeeStringUtils.secureString(value, parameter.maxSize,
                        parameter.format);
            }
            // fetch the existing values for the given parameter and try to add the value
            parameterValues = this.values[parameter.name];
            // Throw an exception if the param does not allow 
            // multiple values and has already one
            if (!parameter.allowsMultipleValues && parameterValues != null){
                // TODO exception
                console.log("multiple values exception");
            }
            if(parameterValues == undefined){
                parameterValues = new Array();
            }
            if(!(parameterValues instanceof Array)){
                parameterValues = new Array(parameterValues);
            }
            parameterValues.push(value);
            this.values[parameter.name] = parameterValues;
        },
        /**
         * Generate the URL from the current state of the parameters
         * 
         * TODO URL encode
         * 
         * @param parametersSeparator   A {@code String} that is used as parameters separator in the URL
         *                              
         * @return  A {@code String} that is the generated query
         */
        getRequestURL: function(parametersSeparator){
            urlFragment = "";
            // Browse all available parameters
            for (i in urlParameters.getList()){           
                // Fetch the values of this param and generate a query with all
                // its values
                parameter = urlParameters.getList()[i];
                parameterValues = this.values[parameter.name];
                if(!(parameterValues instanceof Array) && parameterValues != undefined){
                    parameterValues = new Array(parameterValues);
                }
                if(parameterValues != undefined){
                    for(j in parameterValues){
                        urlFragment += parameter.name+ "=";
                        urlFragment += parameterValues[j] + parametersSeparator;
                    }
                }
            }
            // Remove the extra separator at the end 
            if(urlFragment){
                urlFragment = urlFragment.substring(0, urlFragment.length - 1);
            }
            return urlFragment;
        },
        /**
         * Get parameters from a query string and return them as an associative array.
         * 
         * @param   queryString         a <code>String</code> representing a query string 
         *                              (without the initial '?') 
         *                              to be parsed to extract parameters
         *                              and to return them as an associative array
         * @param   parametersEncoded   a <code>boolean</code> <code>true</code> if the 
         *                              parameters are URL encoded, 
         *                              and need to be decoded
         * @return  an associative array where the keys are parameter names, and values are 
         *          parameter values, 
         *          extracted from the argument <code>queryString</code>
         * source: http://stackoverflow.com/a/3855394
         */
        getQueryStringVars : function(queryString, parametersEncoded)
        {
            a = queryString.split('&');
            if (a === "") return {};
            var b = {};
            for (var i = 0; i < a.length; ++i)
            {
                var p=a[i].split('=');
                if (p.length !== 2) continue;
                var valueTemp = p[1];
                if (parametersEncoded === true) {
                    valueTemp = decodeURIComponent(p[1].replace(/\+/g, " "));
                }
                var value = valueTemp;
                if (value === "") continue;
                if (b[p[0]] !== undefined) {
                    if (typeof b[p[0]] === "string") {
                        var previousValue = b[p[0]];
                        b[p[0]] = new Array();
                        b[p[0]].push(previousValue);
                    }
                    b[p[0]].push(value);
                } else {
                    b[p[0]] = value;
                }
            }
            return b;
        }
};
$( document ).ready( function(){ 
    // TODO add a main js controller somewhere that will do that
    urlParameters.init() ;
    requestParameters.init();
    requestParameters.addValue(urlParameters.getParamEmail(), "test1@mail.com");
    console.log(requestParameters.values);
    console.log(requestParameters.getRequestURL("&"));
} );