#!/bin/bash
# read in the reboot number
value=$(<rebootNum.txt)

# write back to file
echo "$(($value+1))" > rebootNum.txt

# start the tomcat8
sudo service tomcat8 start