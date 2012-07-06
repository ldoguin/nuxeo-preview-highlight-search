/*

highlight v3

Highlights arbitrary terms.

<http://johannburkard.de/blog/programming/javascript/highlight-javascript-text-higlighting-jquery-plugin.html>

MIT license.

Johann Burkard
<http://johannburkard.de>
<mailto:jb@eaio.com>

Laurent Doguin
<http://dev.blogs.nuxeo.com>
<mailto:ldoguin@nuxeo.com>
*/
var anchors;
var idx;
jQuery.fn.highlight = function(pat) {
 function innerHighlight(node, pat) {
  var skip = 0;
  if (node.nodeType == 3) {
   var pos = node.data.toUpperCase().indexOf(pat);
   if (pos >= 0) {
    var spannode = document.createElement('span');
    anchors.push(spannode);
    spannode.className = 'highlight';
    var middlebit = node.splitText(pos);
    var endbit = middlebit.splitText(pat.length);
    var middleclone = middlebit.cloneNode(true);
    spannode.appendChild(middleclone);
    middlebit.parentNode.replaceChild(spannode, middlebit);
    skip = 1;
   }
  }
  else if (node.nodeType == 1 && node.childNodes && !/(script|style)/i.test(node.tagName)) {
   for (var i = 0; i < node.childNodes.length; ++i) {
    i += innerHighlight(node.childNodes[i], pat);
   }
  }
  return skip;
 }
 return this.each(function() {
  innerHighlight(this, pat.toUpperCase());
 });
};

jQuery.fn.removeHighlight = function() {
 anchors = new Array();
 idx = -1;
 return this.find("span.highlight").each(function() {
  this.parentNode.firstChild.nodeName;
  with (this.parentNode) {
   replaceChild(this.firstChild, this);
   normalize();
  }
 }).end();
};

jQuery.fn.backwardScroll = function(){
     if (anchors.length == 0) return;
     if (idx < 0) {
       idx = anchors.length - 1;
     } else {
       idx = idx - 1;
       if (idx < 0) {
         idx = anchors.length - 1;
       }
     }
     $().scrollToAnchor(idx);
}
jQuery.fn.forwardScroll = function(){
     if (anchors.length == 0) return;
     if (idx < 0) {
         idx = -1;
     } 
         idx = idx + 1;
         if (idx == anchors.length) {
           idx = 0;
         
     }
     $().scrollToAnchor(idx);
}

jQuery.fn.scrollToAnchor = function(idx){
     selectedKeyword = $('.highlight-selected');
     if (selectedKeyword) selectedKeyword.removeClass('highlight-selected');
     $(anchors[idx]).addClass('highlight-selected');
     $('html,body').animate({scrollTop: $(anchors[idx]).offset().top},'fast');
}

function show() {
    $('#search-box').show();
    $('#showBoxButton').hide();
};

function hide() {
    $('#search-box').hide();
    $('#showBoxButton').show();
};

function search() {
    keyword = $('#query').val();
    if (keyword) {
        $('body').removeHighlight().highlight(keyword);
    }
};

function createFloatingBox(contextPath, searchKeyword) {
    var $floatingBox = $("<div>").attr("id", "floating-box").attr("style","position: fixed;");
    var $showBoxButton = $("<div>").attr("id", "showBoxButton").attr("style","display: none;").appendTo($floatingBox);
    var $showBoxButtonInput = $("<input>").attr("src", contextPath + "/icons/search.png").attr("type", "image").click(show).appendTo($showBoxButton);
    var $searchBox = $("<div>").attr("id", "search-box").attr("style","display: block;").appendTo($floatingBox);
    var $queryInput = $("<input>").attr("value", searchKeyword).attr("id", "query").attr("type", "text").appendTo($searchBox);
    var $showBoxButtonInput = $("<input>").attr("value", "Search").attr("type", "button").click(search).appendTo($searchBox);
    var $showBoxButtonInput = $("<input>").attr("src", contextPath + "/icons/action_page_previous.gif").attr("type", "image").click( $().backwardScroll).appendTo($searchBox);
    var $showBoxButtonInput = $("<input>").attr("src", contextPath + "/icons/action_page_next.gif").attr("type", "image").click($().forwardScroll).appendTo($searchBox);
    var $showBoxButtonInput = $("<input>").attr("src", contextPath + "/icons/action_delete_mini.png").attr("type", "image").click(hide).appendTo($searchBox);
    $floatingBox.appendTo($("body"));
};

function initSearchBox(contextPath)  {
    var searchKeyword;
    keywordSpan = parent.document.getElementById('searchKeyWords');
    if (keywordSpan) searchKeyword = keywordSpan.getAttribute('key');
    createFloatingBox(contextPath, searchKeyword);
    keyword = $('#query').attr('value');
    if (keyword) {
      $('body').removeHighlight().highlight(keyword);
      $('#search-box').show();
      $('#showBoxButton').hide();
    } else {
      $('#search-box').hide();
      $('#showBoxButton').show();
    }
};