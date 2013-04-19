var x=1;
var z=0;
outer:
while(x<100){
	var y=1;
	while(y<100){
		if(x == 50 && y==50)
			continue outer;
		z+=x*y;
	}
}
x=1;
z=0;

outer2:
do{
	var y=1;
	while(y<100){
		if(x == 50 && y==50)
			continue outer2;
		z+=x*y;
	}
}while(x<100);

print(z);
