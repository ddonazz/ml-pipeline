#!/bin/bash   

while true; do 
	NOW=`date +%s`
	STR_IN=""
	for i in {1..1000}; do
		STR_IN=${STR_IN}$"0.0,0.0,1.0,20.0,0.0,0.0,0.0,1.0,0.0,1.0,0.0,1.0,0.0,2.0,5.0,0.0,0.0,0.0,2.0,6.0,7.0\n
						1.0,1.0,1.0,30.0,1.0,0.0,1.0,0.0,1.0,1.0,0.0,1.0,0.0,5.0,30.0,30.0,1.0,0.0,9.0,5.0,1.0\n"
	done
	echo -e $STR_IN
	echo -e $STR_IN > rt/diabetes/input/random.$NOW.input
done

