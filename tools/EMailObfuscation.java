public class EMailObfuscation {

	public String obfuscateEmail(String email, String alt_text, String index)
    {
        String href = "mailto:" + email;
        String code = String.format("function transpose2" + index + "(h) {var s='%1$s';var r='';for(var i=0;" +
                "i<s.length;i++,i++){r=r+s.substring(i+1,i+2)+s.substring(i,i+1)}h.href=r;}document.write" +
                "('<a href=\"#\" onMouseOver=\"javascript:transpose2" + index +
                "(this)\" onFocus=\"javascript:" + "transpose2" + index +
                "(this)\" title=\"Contact us\" class=\"menu\">%2$s</a>');",
                this.transposeEmail(href), alt_text);

        email = email.replaceAll("@", " [AT] ");
        String obfuscatedEmail = String.format("<script>eval(unescape('%1$s'));</script><noscript>%2$s</noscript>",
        		this.escapeencodeEmail(code), email);
        return obfuscatedEmail;
    }

	private String transposeEmail(String href)
    {
        //function takes the string and swaps the order of each group of 2 chars
        String ret = "";
        for (int i=0; i<href.length(); i=i+2)
        {
            if ( i+1 == href.length() ){
                ret = ret + href.substring(i, i+1); //+1 because endIndex is exclusive
            }
            else {
                //a sprintf like, with n=indice nbr, x=type in %n$x
                ret = ret + String.format("%1$s%2$s", href.substring(i+1, i+1+1), href.substring(i, i+1));
            }
        }
        return ret;
    }

	private String escapeencodeEmail(String code)
    {
        String ret   = "";
        for (int j=0; j<code.getBytes().length; j++)
        {
            ret = ret + String.format("%%%1$X", new Byte(code.getBytes()[j]));
        }
        return ret;
    }

}
