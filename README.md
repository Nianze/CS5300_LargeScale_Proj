# CS5300PROJECT
A private repo for CS5300 projects

NEW DEPLOY PROCEDURES:

1. the installation scripts assume nothing has been setup so we need to first clear everything
  * clear everything in simple db: run the command "aws sdb delete-domain --domain-name ipAddressInfo"
  * clear security group name "cs5300tomcat" from the aws web console if there exists one
  * delete project-1b.war file from your s3 bucket
2. fill in the parameters at the top of the launch.sh and script.sh files with your own credentials and s3 bucket name
3. place launch.sh, script.sh and project-1b.war all in the same directory
4. open the terminal pointing to that directory
5. run ./launch.sh

Setup Procedures:

1) create a dynamic web project in Eclipse EE <br>
2) expand the src folder and create a package name "sessionManagement" <br>
3) copy and paste all java files into the newly created package <br>
4) copy and paste the "json-simple-1.1.1.jar" file into the WEB-INF -> lib folder to resolve import errors <br>
5) copy and paste the web.xml into the WEB-INF folder <br>

Completed Items:

- AWS Beanstalk setup and configuration
- AWS instance installation script (to setup IP tables)
- Datagram transmission
- Basic RPC Client & Server (send to all IPs in table and wait for 1 reply)

TODO Items:
- ~~Modify RPC Client & Server with proper parameters according to the SSM paper~~
- ~~Periodic update of the server IP table to reflect other server up/down changes~~
- ~~IP tracking of a session (which IPs have information regarding a particular session)~~
- ~~Modify session version logic? (current logic is version++ upon every request and no storing info of old versions)~~
- Garbage Collection logic of timeout sessions
* The SvrID and reboot_num of the server executing the client request
  * Inet4Address.getLocalHost().getHostAddress() + ipAddressMapping --> serverid
  * reboot.sh:
      1. increase the reboot_num in /home/ec2-user/reboot_num.txt
      2. sudo service tomcat8 start
* Report the SvrID where the session data was found
* Local meta data of WQ servers
* Cookie domain: xxx.bigdata.systems

SSM Parameters:
- N = 3 (3 total instances/nodes)
- W = 3 (send write requests to 3 nodes (all instances))
- WQ = 2 (wait for 2 instances to reply on write requests)
- R = 2 (send read requests to 2 nodes containing the sesionID-sesionValue mapping)
- wait for 1 reply on reads

To test and run the file:

1. export code into WAR file
2. ssh and copy the WAR file into var/lib/tomcat8/webapps
  * may need to do "sudo chmod 777 /var/lib/tomcat8/webapps" in terminal firstly
3. ssh into the instance and start or restart the server using "sudo service tomcat8 start"<br>
4. to debug the code and see print statements: <br>
  * type in "sudo chmod 777 /var/log/tomcat8/catalina.out"
  * navigate to the catalina.out file and see the contents
  * test url: http://ip_address:8080/project-1b/home-page 

