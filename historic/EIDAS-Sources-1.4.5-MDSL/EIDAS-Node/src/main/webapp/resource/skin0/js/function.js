/* Switchery = Checkbox Switch Toogle */
var elems = Array.prototype.slice.call(document.querySelectorAll('.js-switch'));
elems.forEach(function(html) {
  var switchery = new Switchery(html, { size: 'small', color: '#43C2CB', jackColor: '#fff', secondaryColor: '#b3b3b3' });
});


jQuery(document).ready(function() {

  /*Plus-minus-text*/
  $('.text-plus').click(function() {
    $('.col-right-content').stop().animate({fontSize: '+=0.1em'},300);
  });
  $('.text-minus').click(function() {
    $('.col-right-content').stop().animate({fontSize: '-=0.1em'},300);
  });

  /*Contrast*/
  $(".contrast").click(function(){
    $("body").toggleClass("contrast");
  });

});