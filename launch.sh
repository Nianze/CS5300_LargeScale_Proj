#!/bin/bash

#AMI=ami-08111162
#S3_BUCKET=edu-cornell-cs-cs5300s16-ah935

aws configure set aws_access_key_id AKIAJEPSLO6AH7DOYNQQ
aws configure set aws_secret_access_key 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
aws configure set default.region us-east-1
aws configure set preview.sdb true
aws sdb create-domain --domain-name ipAddressInfo
#aws s3 cp hello.war s3://${S3_BUCKET}/hello.war
aws ec2 run-instances --image-id ami-08111162 --count 3 --instance-type t2.micro --security-groups AutoScaling-Security-Group-2 --key-name cs5300aws --user-data file://script.sh