$(document).ready(function() {
    $('pre code').each(function(i, block) {
        hljs.highlightBlock(block);
    });
});


function navSelect(idx, units) {
    for (i = 0; i < units; i++) {
        var contentDiv = "#nav-containter-" + i;
        var navTab = "#navItem-" + i;
        if (i === idx) {
            $(navTab).attr('class', 'nav-link result-menu-selected active');
            $(contentDiv).show();
        } else {
            $(navTab).attr('class', 'nav-link result-menu-item');
            $(contentDiv).hide();
        }
    }
}
