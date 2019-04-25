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

	processAttrsRd("reqNpAttr");
	processAttrsRd("reqLpAttr");

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

function processAttrsRd(prefix){
	$("input[name^='"+prefix+"']:checked").each(function (index) {
		var selectedItem = $(this).val();
		var td = $(this).closest("td").find("div").eq(0).find("div").eq(0);
		switch (selectedItem) {
			case "n":
				td.attr("class", "col-lg-8 col-sm-12 attr-no-req");
				break;
			case "o":
				td.attr("class", "col-lg-8 col-sm-12 attr-request");
				break;
			case "r":
				td.attr("class", "col-lg-8 col-sm-12 attr-require");
				break;
		}
	});
}

function selectAllAttr(prefix, opt) {
	$("input[name^='"+prefix+"']").each(function (index) {
		var optionVal = $(this).val();
		if (optionVal === opt){
			$(this).prop("checked", true);
		} else {
			$(this).prop("checked", false);
		}
	});
	processAttrsRd(prefix);
}


