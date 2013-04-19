var x;
var y;

try{
	x=1;
	throw x;
	y=2;
}catch(ex if x==2){
	doSomething();
	throw ex;
}catch(ex if y==2){
	doSomethigElse();
}
print(x);
