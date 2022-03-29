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
 * @author  Valentine Rech de Laval
 * @version Bgee 14 Oct 2018
 * @since   Bgee 13 Sept 2014
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

//prototype extensions

//add indexOf to Array prototype, only for browsers not supporting ECMAScript 5th edition.
if (!Array.indexOf)
{
  Array.indexOf = [].indexOf ?
      function (arr, obj, from) { return arr.indexOf(obj, from); }:
      function (arr, obj, from) { // (for IE6)
        var l = arr.length,
            i = from ? parseInt( (1*from) + (from<0 ? l:0), 10) : 0;
        i = i<0 ? 0 : i;
        for (; i<l; i++) {
          if (i in arr  &&  arr[i] === obj) { return i; }
        }
        return -1;
      };
}

//global vars
//create a BgeeProperties object to be accessed by all scripts
var GLOBAL_PROPS = new bgeeProperties();
//CURRENT_REQUEST will store a requestParameters object storing
//parameters from the current URL, initialized a document ready
var CURRENT_REQUEST;

$(document).ready(function() {
    // Init object urlParameters, which provides the list of all parameters allowed in an URL.
    urlParameters.init();
    // Create a requestParameters for the current URL
    CURRENT_REQUEST = new requestParameters(window.location.search + window.location.hash);

    // Add a listener in navbar to change caret image when mouse over 'li'
    var $deployLi = $( "#nav ul#bgee_links li" );
    $deployLi.on("mouseover", function() {
        // Change img
        $("img", this).attr('src', '/img/arrow_down_light.png');
    });
    $deployLi.on( "mouseout", function() {
        // Change img
        $("img", this).attr('src', '/img/arrow_down_dark.png');
    });

    // Add external_link class automatically to each 'a[target=_blank]' element without 'img' inside.
    $( "#sib_body a[target=_blank]:not(:has(img))" ).each(function() {
        $( this ).addClass("external_link");
    });

    // Check cookie for privacy notice banner
    checkCookie();

    // Copy to clipboard
    // Grab any text in the attribute 'data-copy' and pass it to the copy function
    $('.js-tooltip').tooltip();
    $('.js-copy').on("click", function() {
        copyToClipboard($(this));
    });
});

// Getter/setter for cookies from https://www.w3schools.com/js/js_cookies.asp
var noticeVersion = 1; // Update this integer if privacy notice updated and needs to be re-validated by the user!
function checkCookie() {
    var urlPrivacyPolicy = new requestParameters();
    urlPrivacyPolicy.addValue(urlParameters.getParamPage(), urlPrivacyPolicy.PAGE_PRIVACY_POLICY());

    var cookieName = "__Host-bgee-privacy-policy";
    var isPrivacyChecked = getCookie(cookieName);

    var toDisplay = true;
    if (isPrivacyChecked === "") {
        // Unseen notice
        $('#bgee_privacy_banner_text').html("This website requires cookies, and limited " +
            "processing of your personal data in order to function. By using the site " +
            "you are agreeing to this as outlined in our " +
            "<a href='" + urlPrivacyPolicy.getRequestURL() + "'>privacy notice</a>.");
    } else if (isPrivacyChecked < noticeVersion) {
        // Updated notice
        $('#bgee_privacy_banner_text').html("We'd like to inform you that we have updated our " +
            "<a href='" + urlPrivacyPolicy.getRequestURL() + "'>privacy notice</a>.");
    } else {
        // Seen and last notice
        toDisplay = false;
    }
    displayPrivacyBanner(cookieName, toDisplay);
}

function getCookie(cookieName) {
    var name = cookieName + "=";
    var decodedCookie = decodeURIComponent(document.cookie);
    var ca = decodedCookie.split(";");
    for (var i = 0; i < ca.length; i++) {
        var c = ca[i];
        while (c.charAt(0) == " ") {
            c = c.substring(1);
        }
        if (c.indexOf(name) == 0) {
            return c.substring(name.length, c.length);
        }
    }
    return "";
}

function setCookie(cookieName, cookieVersion) {
    var d = new Date();
    d.setTime(d.getTime() + (365*24*60*60*1000)); // 1 year
    var expires = "expires="+ d.toUTCString();
    document.cookie = cookieName + "=" + cookieVersion + ";" + expires + ";path=/;Secure;SameSite=Strict";
}

function displayPrivacyBanner(cookieName, toDisplay) {
    if (toDisplay) {
        $('#bgee_privacy_banner').show();
        $('#bgee_privacy_banner_accept').on("click", function() {
            setCookie(cookieName, noticeVersion);
            $('#bgee_privacy_banner').hide();
        });
    } else {
        $('#bgee_privacy_banner').hide();
    }
}

// COPY TO CLIPBOARD
// ------------------------------------------------------------------------------
function copyToClipboard(el) {
    var copyTest = document.queryCommandSupported('copy');
    var text = el.attr('data-copy');
    var elOriginalText = el.attr('data-original-title');

    if (copyTest === true) {
        var copyTextArea = document.createElement("textarea");
        copyTextArea.value = text;
        document.body.appendChild(copyTextArea);
        copyTextArea.select();
        try {
            var successful = document.execCommand('copy');
            var msg = successful ? 'Copied!' : 'Whoops, not copied!';
            el.attr('data-original-title', msg).tooltip('show');
        } catch (err) {
            console.log('Oops, unable to copy');
        }
        document.body.removeChild(copyTextArea);
        el.attr('data-original-title', elOriginalText);
    } else {
        // Fallback if browser doesn't support .execCommand('copy')
        alert("Copy following link by selecting following link and typing Cmd+C or Command+C: " + text);
    }
}
