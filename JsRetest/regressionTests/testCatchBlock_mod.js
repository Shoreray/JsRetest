function testCatchBlock(){
	try{
		var x=1;
		throw x;
	}catch(e){
		console.log("exception");
	}
}