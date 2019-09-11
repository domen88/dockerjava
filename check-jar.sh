#! /bin/bash

if test -f target/$1.jar
then
    echo "jar exists"
else 
    echo "jar not exists"
fi