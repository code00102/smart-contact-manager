

const toggleSideBar = () => {
	
	if($(".sidebar").is(":visible")){
		
		// banda garne
		$(".sidebar").css("display", "none");
		$(".content").css("margin-left", "0%");
	}
	else{
			
			//dekhaune
			$(".sidebar").css("display", "block");
			$(".content").css("margin-left", "20%");

		
	}
	
};