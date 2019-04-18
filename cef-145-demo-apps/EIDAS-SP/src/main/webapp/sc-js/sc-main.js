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
});