#!/bin/bash

# Please fill in these parameters
AMI=ami-08111162
NUM_NODES=3
S3_BUCKET=XXXX
ACCESS_KEY_ID=XXXX
SECRET_ACCESS_KEY=XXXX

# Setup SimpleDB and create a domain so the installation script can store ip info
aws configure set aws_access_key_id ${ACCESS_KEY_ID}
aws configure set aws_secret_access_key ${SECRET_ACCESS_KEY}
aws configure set default.region us-east-1
aws configure set preview.sdb true
aws sdb create-domain --domain-name ipAddressInfo

# Upload the war file onto S3 bucket so the installation script and download from S3
aws s3 cp project-1b.war s3://{S3_BUCKET}/project-1b.war

# Setup a security group with the proper ports enabled
aws ec2 create-security-group --group-name cs5300tomcat --description "security group for cs5300 instances"
aws ec2 authorize-security-group-ingress --group-name cs5300tomcat --protocol tcp --port 22 --cidr 0.0.0.0/0
aws ec2 authorize-security-group-ingress --group-name cs5300tomcat --protocol tcp --port 80 --cidr 0.0.0.0/0
aws ec2 authorize-security-group-ingress --group-name cs5300tomcat --protocol tcp --port 8080 --cidr 0.0.0.0/0
aws ec2 authorize-security-group-ingress --group-name cs5300tomcat --protocol udp --port 5300 --cidr 0.0.0.0/0

# Launch the instances
aws ec2 run-instances --image-id ${AMI} --count ${NUM_NODES} --instance-type t2.micro --security-groups cs5300tomcat --key-name cs5300aws --user-data file://script.sh