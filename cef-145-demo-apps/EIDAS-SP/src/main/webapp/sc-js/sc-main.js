$(document).ready(function(){
	$('.drop-down > p').click(function(){
		$(this).parent('.drop-down').toggleClass('open');
	});


	// Not to be used in production
	$('#headertoggle').click(function(){
		$('.header').toggleClass('hide');
	});

	$('#elemtoggle').click(function(){
		$('.ns-providers').toggleClass('hide');
	});

	processAttrs("reqNpAttr");
	processAttrs("reqLpAttr");

});



function processAttrs(prefix){
	$("[id^="+prefix+"]").each(function (index) {
		var selectedItem = $(this).prop('selectedIndex');
		var td = $(this).closest("td");
		switch (selectedItem) {
			case 0:
				td.attr("class", "attr-no-req");
				break;
			case 1:
				td.attr("class", "attr-request");
				break;
			case 2:
				td.attr("class", "attr-require");
				break;
		}
	});
}
