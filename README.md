# CS5300PROJECT
A private repo for CS5300 projects

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
- Modify RPC Client & Server with proper parameters according to the SSM paper
- Periodic update of the server IP table to reflect other server up/down changes
- IP tracking of a session (which IPs have information regarding a particular session)
- Modify session version logic? (current logic is version++ upon every request and no storing info of old versions)
- Garbage Collection logic of timeout sessions
