#! /bin/bash
pwd
if test -f target/$1.jar
then
    echo "jar exists"
    ls -l target/
else 
    echo "jar not exists"
fi