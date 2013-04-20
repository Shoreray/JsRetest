function testCatchBlock(){
	try{
		var x=1;
		throw x;
	}catch(e){
		Console.log("exception");
	}
}