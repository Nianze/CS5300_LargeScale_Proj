# CS5300PROJECT-1b

#### Overall structure

1. Cookie formats

   cookieValue = sessionID_sessionVersion,localMetaData_dummyParam

   * sessionID is defined by randomly generated UUID.

   * sessionVersion is the version of the session.

   * localMetaData is in the format of 

     * SvrID-1_SvrID-2_ …_SvrID-WQ

     which keeps track of the WQ servers where the session message is stored in.

     Note that sessionID_sessionVersion and localMetaData_dummyParam is connected by a comma sign ","

   * dummyParam is the dummy parameter in case for future function extensions.

2. RPC messages

   * RPC client message:

     callID_operationCode_sessionIDWithVersion_dummyParam

     * callID is a unique ID for the call.

     * operationCode is either 0 or 1. 

       * For read request, operationCode = 0; 
       * For write request, operationCode = 1.

     * sessionIDWithVersion is in the format of 

       * sessionID_sessionVersion

       which is exactly the first half of cookieValue mentioned above.

     * dummyParam is dummy with no usability currently.

   * RPC server message:

     * For read request:

       callID_sessionID_sessionVersion_sessionMessage_sessionExpiredTS_dummyParam

     * For write request:

       callID_sessionID_sessionVersion_sessionMessage_sessionExpiredTS_locMetaData_dummyParam

3. Funcionalities in each source file

   * Globals.java
   * HomePage.java
   * RPCClient.java
   * RPCServer.java
   * RPCServerThread.java
   * SessionValues.java



#### NEW DEPLOY PROCEDURES:

1. the installation scripts assume nothing has been setup so we need to first clear everything
   * clear everything in simple db: run the command "aws sdb delete-domain --domain-name ipAddressInfo"
   * clear security group name "cs5300tomcat" from the aws web console if there exists one
   * delete project-1b.war file from your s3 bucket
2. fill in the parameters at the top of the launch.sh and script.sh files with your own credentials and s3 bucket name
3. fill in the cookie domain in Globals.cookieDomain. (in our case it is ".xxyyy.bigdata.systems")
4. place launch.sh, script.sh and project-1b.war all in the same directory
5. open the terminal pointing to that directory
6. run ./launch.sh

##### Setup Procedures:

1) create a dynamic web project in Eclipse EE 
2) expand the src folder and create a package name "sessionManagement" 
3) copy and paste all java files into the newly created package 
4) copy and paste the "json-simple-1.1.1.jar" file into the WEB-INF -> lib folder to resolve import errors 
5) copy and paste the web.xml into the WEB-INF folder 

Completed Items:

- AWS Beanstalk setup and configuration
- AWS instance installation script (to setup IP tables)
- Datagram transmission
- Basic RPC Client & Server (send to all IPs in table and wait for 1 reply)

~~TODO Items:~~
- ~~Modify RPC Client & Server with proper parameters according to the SSM paper~~
- ~~Periodic update of the server IP table to reflect other server up/down changes~~
- ~~IP tracking of a session (which IPs have information regarding a particular session)~~
- ~~Modify session version logic? (current logic is version++ upon every request and no storing info of old versions)~~
- ~~Garbage Collection logic of timeout sessions~~
* ~~The SvrID and reboot_num of the server executing the client request~~
  * ~~Inet4Address.getLocalHost().getHostAddress() + ipAddressMapping --> serverid~~
  * ~~reboot.sh:~~
      1. ~~increase the reboot_num in /home/ec2-user/reboot_num.txt~~
          1. ~~sudo service tomcat8 start~~
* ~~Report the SvrID where the session data was found~~
* ~~Local meta data of WQ servers~~
* ~~Cookie domain: xxx.bigdata.systems~~

##### SSM Parameters:

- N = 3 (3 total instances/nodes)
- W = 3 (send write requests to 3 nodes (all instances))
- WQ = 2 (wait for 2 instances to reply on write requests)
- R = 2 (send read requests to 2 nodes containing the sesionID-sesionValue mapping)
- wait for 1 reply on reads

##### To test and run the file:

1. export code into WAR file
2. ssh and copy the WAR file into var/lib/tomcat8/webapps
* may need to do "sudo chmod 777 /var/lib/tomcat8/webapps" in terminal firstly
1. ssh into the instance and start or restart the server using "sudo service tomcat8 start"
2. to debug the code and see print statements: 
* type in "sudo chmod 777 /var/log/tomcat8/catalina.out"
* navigate to the catalina.out file and see the contents
* test url: http://ip_address:8080/project-1b/home-page 

