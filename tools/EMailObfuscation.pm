package EMailObfuscation;
#file EMailObfuscation.pm

use strict;
use warnings;
use diagnostics;

sub transpose_eMails_delayed {
    my ($emails, $alternative, $index) = @_;

    my $href = "mailto:$emails"; #For mailto href here only
    #You can use a list of e-mails, comma separated, to send to several addresses from one click
    my $code = sprintf("function seb_transpose2$index(h) {var s='%s';var r='';for(var i=0;i<s.length;i++,i++){r=r+s.substring(i+1,i+2)+s.substring(i,i+1)}h.href=r;}document.write('<a href=\"#\" onMouseOver=\"javascript:seb_transpose2$index(this)\" onFocus=\"javascript:seb_transpose2$index(this)\">%s</a>');", &transpose($href), $alternative);

    $emails =~ s{@}{ [AT] }g;
    # Break e-mail addresses for noscript tag.
    my $userCode = sprintf("%s%s%s", "<script type=\"text/javascript\">eval(unescape('", &escapeencode($code), "'));</script><noscript>$emails</noscript>");

    return $userCode;
}

sub escapeencode {
    my ($strg) = @_;

    my $ret = '';
    my @arr = unpack('C*', $strg);
    for my $char (@arr){
        $ret .= sprintf("%%%X", $char);
    }

    return $ret;
}

sub transpose {
    my ($strg) = @_;

    # function takes the string and swaps the order of each group of 2 chars
    my $len = length($strg);
    my $ret = '';
    for (my $i=0; $i<$len; $i=$i+2){
        if ($i+1 == $len){
            $ret .= substr($strg, $i, 1);
        }
        else{
            $ret .= sprintf("%s%s", substr($strg, $i+1, 1), substr($strg, $i, 1));
        }
    }

    return $ret;
}

1;

