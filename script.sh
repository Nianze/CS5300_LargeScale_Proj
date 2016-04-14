#!/bin/bash

# Please fill in these parameters
# note that the nodeNum refers to the number of nodes launched and needs to be declare as a string with double quotes
nodeNum="3"
S3_BUCKET=XXXX
ACCESS_KEY_ID=XXXX
SECRET_ACCESS_KEY=XXXX

# remove java 1.7 and install java 1.8 + tomcat 8
yum -y remove java-1.7.0-openjdk
yum -y install java-1.8.0
yum -y install tomcat8-webapps tomcat8-docs-webapp tomcat8-admin-webapps

cd /home/ec2-user

# create rebootNum.txt to keep track of the reboot number
echo 0 > rebootNum.txt

# get the ip and ami index info of this instance
wget http://169.254.169.254/latest/meta-data/local-ipv4
wget http://169.254.169.254/latest/meta-data/ami-launch-index
ipVar=$(cat local-ipv4)
indexVar=$(cat ami-launch-index)

# setup aws sdb configuration
aws configure set aws_access_key_id ${ACCESS_KEY_ID}
aws configure set aws_secret_access_key ${SECRET_ACCESS_KEY}
aws configure set default.region us-east-1
aws configure set preview.sdb true

# put the information into sdb
aws sdb put-attributes --domain-name ipAddressInfo --item-name ${indexVar} --attributes "[{\"Name\": \"internalIPAddress\", \"Value\": \"${ipVar}\", \"Replace\":true}]"

# need to wait here for all nodes
while true
do
    sleep 1
    output=$(aws sdb select --consistent-read --select-expression "select count(*) from ipAddressInfo" | grep -Po '[0-9]')
    if [ $output = $nodeNum ]
    then
        break
    fi
done
# retrieve ip and ami index info of all instances
aws sdb select --consistent-read --select-expression "select * from ipAddressInfo" > /home/ec2-user/ipAddrInfo.txt
# download the war file from s3 into the tomcat webapps folder
aws s3 cp s3://${S3_BUCKET}/project-1b.war /var/lib/tomcat8/webapps/project-1b.war
# download the reboot.sh from s3 into the ec2-user folder
aws s3 cp s3://${S3_BUCKET}/reboot.sh /home/ec2-user/reboot.sh
# set permission so the program can access the files in this folder
chmod o+x /home/ec2-user
# start tomcat8 server
service tomcat8 start
