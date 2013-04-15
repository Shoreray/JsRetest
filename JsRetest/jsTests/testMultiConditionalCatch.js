var x;
var y;

try{
	x=1;
	y=2;
}catch(ex if x==2){
	doSomething();
}catch(ex if y==2){
	doSomethigElse();
}
print(x);
