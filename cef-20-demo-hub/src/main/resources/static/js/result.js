$(document).ready(function() {
    $('pre code').each(function(i, block) {
        hljs.highlightBlock(block);
    });
});


function pageConfNav(idx, units) {
    for (i = 0; i < units; i++) {
        var contentDiv = "#pageConfNavDiv-" + i;
        var navTab = "#pageConfNavTab-" + i;
        if (i === idx) {
            $(navTab).attr('class', 'nav-link result-menu-selected active');
            $(contentDiv).show();
        } else {
            $(navTab).attr('class', 'nav-link result-menu-item');
            $(contentDiv).hide();
        }
    }
}
