#!/bin/bash

string=$1
for dir in $(ls res|grep values*); do
	file="res/$dir/strings.xml"
	if [ -f $file ]; then
		echo "Found translation: $file"
		sed -i "/$string/d" $file
	fi

done
