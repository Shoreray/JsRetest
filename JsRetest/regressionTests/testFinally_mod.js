function testFinally(){
	try{
		var x=1;
		throw x;
	}catch(e){
		Console.log(e);
	}finally{
		Console.log("finally");
		
	}
	Console.log("after finally");
}