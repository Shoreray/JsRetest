function testCatchExp(){
	try{
		var x=1;
		throw x;
	}catch(e){
		Console.log(e);
	}
}