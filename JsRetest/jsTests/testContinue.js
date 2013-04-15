var x=1;
while(x<100){
	x++;
	if(x==23){
		continue;
	}
	x++;
}
do{
	x++;
	if(x%3==1){
		continue;
	}
	x++;
}while(x<1000);

for(x=1;x<100;x++){
	
	if(x==23){
		continue;
	}
	x++;
}

for(var i in [1,2,3,4,5,6,7,8]){
	if(i==5){
		continue;
	}
	x+=i;
}

print(x);
