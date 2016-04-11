#!/bin/bash

# remove java 1.7 and install java 1.8 + tomcat 8
yum -y remove java-1.7.0-openjdk
yum -y install java-1.8.0
yum -y install tomcat8-webapps tomcat8-docs-webapp tomcat8-admin-webapps

# get the ip and ami index info of this instance
wget http://169.254.169.254/latest/meta-data/local-ipv4
wget http://169.254.169.254/latest/meta-data/ami-launch-index
ipVar=$(cat local-ipv4)
indexVar=$(cat ami-launch-index)

# setup aws sdb configuration
aws configure set aws_access_key_id AKIAJEPSLO6AH7DOYNQQ
aws configure set aws_secret_access_key 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
aws configure set default.region us-east-1
aws configure set preview.sdb true

# put the information into sdb
aws sdb put-attributes --domain-name ipAddressInfo --item-name ${indexVar} --attributes "[{\"Name\": \"internalIPAddress\", \"Value\": \"${ipVar}\", \"Replace\":true}]"

# declare number of N nodes starting
nodeNum="3"

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

# set permission
chmod o+x /var/lib/tomcat8/webapps
chmod o+x /home/ec2-user
chmod o+x /var/log/tomcat8