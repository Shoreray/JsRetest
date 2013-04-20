function testCatchExp(){
	try{
		var x=1;
		throw x;
	}catch(ex){
		Console.log(ex);
	}
}