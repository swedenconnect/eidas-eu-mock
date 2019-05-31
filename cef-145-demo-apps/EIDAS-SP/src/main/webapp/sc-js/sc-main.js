var selectCountryCookie="selectedCountry";
var selectSectorCookie="selectedSector";
var selectLoaCookie="selectedLoa";


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

	var selectedCountry = $.cookie(selectCountryCookie);
	if (selectedCountry != undefined){
		$('#selectedCountry').val(selectedCountry);
	}
	var selectedSector = $.cookie(selectSectorCookie);
	if (selectedSector != undefined){
		$('#spType').val(selectedSector);
	}
	var selectedLoa = $.cookie(selectLoaCookie);
	if (selectedLoa != undefined){
		$('#reqLoa').val(selectedLoa);
	}
	$('.selectpicker').selectpicker('refresh');
});

function processAttrs(prefix){
	$("input[name^='"+prefix+"']:checked").each(function (index) {
		var selectedItem = $(this).val().substr(0,1);
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
		var optionVal = $(this).val().substr(0,1);
		if (optionVal === opt){
			$(this).prop("checked", true);
		} else {
			$(this).prop("checked", false);
		}
	});
	processAttrs(prefix);
}

function saveSelectedCountry(){
	var selectedCountry =  $("#selectedCountry").val();
	$.cookie(selectCountryCookie, selectedCountry, {expires : 200} );
}

function saveSelectedSector(){
	var selectedSector =  $("#spType").val();
	$.cookie(selectSectorCookie, selectedSector, {expires : 200} );
}

function saveSelectedLoa(){
	var selectedLoa =  $("#reqLoa").val();
	$.cookie(selectLoaCookie, selectedLoa, {expires : 200} );
}



