$(function(){
	$("#hide").click(function(){
		$("#phs").hide();
	});
	$("#show").click(function(){
		$("#phs").show();
	});
	$("#toggle").click(function(){
		$("#ptgl").toggle();
	});
	$("#fadein").dblclick(function(){
		$("#pfa").fadeIn();
		$("#pfb").fadeIn("slow");
		$("#pfc").fadeIn(3000);
	});
	$("#fadeout").dblclick(function(){
		$("#pfa").fadeOut();
		$("#pfb").fadeOut("slow");
		$("#pfc").fadeOut(3000);
	});
	$("#fadetoggle").dblclick(function(){
		$("#pft").fadeToggle("slow");
	});
	$("#fadeto").dblclick(function(){
		$("#pfto").fadeTo("slow", 0.15);
	});
	$("#fadeback").dblclick(function(){
		$("#pfto").fadeTo("slow", 1);
	});
	$("#tddiv").mouseenter(function(){
		$("#div1").animate({
			opacity:'0.8',
			height:'300px',
			width:'300px'
		},3000).animate({
			opacity:'1',
			height:'80px',
			width:'80px'
		},3000,function() {
			alert("Done!");
		});

	});
	$("#tddiv").mouseleave(function(){
		$("#div1").finish();
	}); 

	$("#akp").keypress(function(){
		$("#div2").fadeOut(2000).fadeIn(2000).fadeTo(2000, 0.15).fadeTo(2000, 1);
	});

	$("#akd").keydown(function(){
		$("#div3").slideUp(2000)
		.slideDown(2000)
		.animate({height:'200px', width:'200px'}, "slow")
		.animate({height:'80px', width:'80px'}, "slow");
	});
});