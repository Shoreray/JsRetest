function testFinally(){
	try{
		var x=1;
		throw x;
	}catch(e){
		console.log(e);
	}finally{
		console.log("finally");
		console.log("after finally");
	}
	
}