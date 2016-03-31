Content-Type: multipart/mixed; boundary="===============5189065377222898407=="
MIME-Version: 1.0

--===============5189065377222898407==
Content-Type: text/cloud-config; charset="us-ascii"
MIME-Version: 1.0
Content-Transfer-Encoding: 7bit
Content-Disposition: attachment; filename="cloud-config.txt"

#cloud-config
repo_upgrade: none
repo_releasever: 2015.09
cloud_final_modules:
 - [scripts-user, always]

--===============5189065377222898407==
Content-Type: text/x-shellscript; charset="us-ascii"
MIME-Version: 1.0
Content-Transfer-Encoding: 7bit
Content-Disposition: attachment; filename="user-data.txt"

#!/bin/bash
wget http://169.254.169.254/latest/meta-data/local-ipv4
wget http://169.254.169.254/latest/meta-data/ami-launch-index
ipVar=$(cat local-ipv4)
indexVar=$(cat ami-launch-index)
aws configure set aws_access_key_id AKIAJEPSLO6AH7DOYNQQ
aws configure set aws_secret_access_key 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
aws configure set default.region us-east-1
aws configure set preview.sdb true
aws sdb put-attributes --domain-name ipAddressInfo --item-name ${indexVar} --attributes "[{\"Name\": \"internalIPAddress\", \"Value\": \"${ipVar}\", \"Replace\":true}]"
aws sdb select --consistent-read --select-expression "select * from ipAddressInfo" > /home/ec2-user/ipAddrInfo.txt
chmod o+x /home/ec2-user

exec > >(tee -a /var/log/eb-cfn-init.log|logger -t [eb-cfn-init] -s 2>/dev/console) 2>&1
echo [`date -u +"%Y-%m-%dT%H:%M:%SZ"`] Started EB User Data
set -x

function sleep_delay 
{
  if (( $SLEEP_TIME < $SLEEP_TIME_MAX )); then 
    echo Sleeping $SLEEP_TIME
    sleep $SLEEP_TIME  
    SLEEP_TIME=$(($SLEEP_TIME * 2)) 
  else 
    echo Sleeping $SLEEP_TIME_MAX  
    sleep $SLEEP_TIME_MAX  
  fi
}

# Executing bootstrap script
SLEEP_TIME=10
SLEEP_TIME_MAX=3600
while true; do 
  curl https://s3.amazonaws.com/elasticbeanstalk-env-resources-us-east-1/stalks/eb_tomcat_4.0.1.57.1/lib/UserDataScript.sh > /tmp/ebbootstrap.sh 
  RESULT=$?
  if [[ "$RESULT" -ne 0 ]]; then 
    sleep_delay 
  else
    /bin/bash /tmp/ebbootstrap.sh     'https://s3.amazonaws.com/elasticbeanstalk-env-resources-us-east-1/stalks/eb_tomcat_4.0.1.57.1/lib/aws-elasticbeanstalk-tools-1.18-1.noarch.rpm'    'https://s3.amazonaws.com/elasticbeanstalk-env-resources-us-east-1/stalks/eb_tomcat_4.0.1.57.1/lib/awseb-ruby-2.2.2-x86_64-20150919_0250.tar.gz'    'https://s3.amazonaws.com/elasticbeanstalk-env-resources-us-east-1/stalks/eb_tomcat_4.0.1.57.1/lib/beanstalk-core-2.1.gem https://s3.amazonaws.com/elasticbeanstalk-env-resources-us-east-1/stalks/eb_tomcat_4.0.1.57.1/lib/beanstalk-core-healthd-1.1.gem https://s3.amazonaws.com/elasticbeanstalk-env-resources-us-east-1/stalks/eb_tomcat_4.0.1.57.1/lib/executor-1.1.gem'    'https://cloudformation-waitcondition-us-east-1.s3.amazonaws.com/arn%3Aaws%3Acloudformation%3Aus-east-1%3A488694385683%3Astack/awseb-e-p6qrq9imsp-stack/9a3816a0-f53f-11e5-a31d-50d5caf92cd2/AWSEBInstanceLaunchWaitHandle?AWSAccessKeyId=AKIAIIT3CWAIMJYUTISA&Expires=1459295327&Signature=B3n1o6fPzgJv%2FTEr%2FyTnZLcmQo8%3D'    'arn:aws:cloudformation:us-east-1:488694385683:stack/awseb-e-p6qrq9imsp-stack/9a3816a0-f53f-11e5-a31d-50d5caf92cd2'    'us-east-1'    '7ecb46ab92b8'    'b8e9e5c1-170e-4cb9-bf56-986bf92cf348'    ''    'httpd'    ''    && 
    exit 0  
  fi 
done
--===============5189065377222898407==-- 