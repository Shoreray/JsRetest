var x=1;
broken:{
	var y=2;
	var z=x+y;
	if(c != undefined)
		break broken;
	var c=x;
	print(c);
}
while(c<100){
	print("hello");
}