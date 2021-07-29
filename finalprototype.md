# Final Prototype MIT 4.0

In this file the Final Prototype of the MIT 4.0 project is described. The Final Prototype is implemented with the procedure of Scenario 2 and is considered for the execution on seven devices, each providing one system. It uses the protocol HTTPS, which means secure communication is possible between the components of the CPS. 

## Table of Contents


1. [Link to other branches](#branches)
2. [Architecture](#architecture)
3. [Implementation](#implementation)
   * [Workload Balancer](#workload)
   * [Maven Project](#maven)
   * [Class Diagram](#class)
   * [Component Diagram](#component)
   * [Summary Table](#table)
4. [How to build the prototype](#prototype)
   * [How to setup the hardware](#hardware)
   * [How to start with Github Code](#start)
   * [Application.properties Files](#applicationfile)
6. [How to add C0, C1 and C2](#addcomponents)
   * [Create Maven Module](#mavenmodule)
   * [Create folder Structure for Components](#folderstructure)
   * [Create application.properties Files](#application)
   * [Add Constants for Components in Arrowhead Source Code](#constants)
   * [Add Functionality to C1](#functionalityc1)   
   * [Add Functionality to C2](#functionalityc2)   
   * [Add Functionality to C0](#functionalityc0)
   * [Add Systems and Services to Arrowhead Source Code](#systemsandservice)
   * [Summary](#summary1)
7. [Pictures final prototype](#pictures)

<a name="branches" />

## Link to other branches

The table below gives an overview of the other branches with a link to each of them. 

| Branch | Scenario | IP Address | Protocol | Required Hardware | Link |
| -------- | ---------- | ---------- | -------- | ----------------- | ------ |
| Master | Only Arrowhead Coresystems | 127.0.0.1  | HTTPS | 1 Device | [Link](https://github.com/igo3r/MIT4.0) |
| Use Case 1 | Scenario 1 | 127.0.0.1  | HTTPS | 1 Device |  [Link](https://github.com/igo3r/MIT4.0/tree/UseCase1) |
| Use Case 2 | Scenario 2 | 127.0.0.1  | HTTPS | 1 Device | [Link](https://github.com/igo3r/MIT4.0/tree/UseCase2) |
| Use Case 3 | Scenario 1 | 127.0.0.1  | HTTP | 1 Device | [Link](https://github.com/igo3r/MIT4.0/tree/UseCase3) |
| Use Case 4 | Scenario 2 | 127.0.0.1  | HTTP | 1 Device | [Link](https://github.com/igo3r/MIT4.0/tree/UseCase4) |
| Final Prototype | Scenario 2 | 10.20.30.1 - 10.20.30.6 | HTTPS | 6 Devices | [Link](https://github.com/igo3r/MIT4.0/tree/final_prototype)| 




<a name="architecture" />

## Architecture

The figure shows the procedure within the CPS between the components. 

![Use Case 2](/images/finalprototype1.png)

Like shown in the picture the final prototype consists of six devices, in this case Raspberry Pi 3 Model B+. Following table shows the ip addresses and ports of the devices: 

| System | IP Address | Port |
| ------ | ---------- | ---- |
| C1 (Consumer) | 10.20.30.1 | 1231 |
| C2 (Producer) | 10.20.30.2 | 1232 |
| Orchestration System | 10.20.30.3 | 1233 |
| Authorization System | 10.20.30.4 | 1234 |
| Service Registry System | 10.20.30.5 | 1235 |
| C0 (Client) | 10.20.30.23 | 1239 |
| Database | 10.20.30.6 | 3306 |

Procedure of Use Case 2: 
* Step 1: C2 measures the temperature of the physical environment at a given time interval. 
* Step 2: C2 checks if the temperature is above the predefined limit. If it is above, this implementation continues with Step 3, else it goes back to Step 1.
* Step 3: C2 sends a request to Orechstration System to find a suitable AC. 
* Step 4: The Orchestration System forwards the request to the SR.
* Step 5: The Service Requistry System searches the database for suitable actuators and sends information about C1 back to the Orchestration System.
* Step 6: The Orchestration System asks the Authorization System if they are allowed to communicate with each other. 
* Step 7: The Authorization System searches the existing authorization rules and reports back whether an authorization exists or not.
* Step 8: The Orchestration System then sends the communication endpoint of C1 to C2.
* Step 9: C2 sends the current temperature to C1.
* Step 10: C1 cools down the phyiscal environment.

The sequence diagram below shows a more detailed procedure, by showing which services are called by which component. Furthermore it includes the database, which stores the data, and C0, with which it is possible to start the run in CPS. Since C0 and the database have no influence on the scenario itself, they are not shown in the previous figure. 

![SequenceDiagramm Use Case 2](/images/sequencediagrammUC2andUC4.png)

<a name="implementation" />

## Implementation

<a name="workload" />

### Workload Balancer (C0)

As mentioned in the previous section, C0 is used to start the runs. C0 is a workload balancer to ensure that all use cases can be compared with each other in a controlled way. The workload balancer is used to define runs, which specify how many measurements should be taken per run. As MIT 4.0 is used to explore security and performance, this allows both aspects to be analysed. 

To measure Use Case 2, the workload must first be defined and executed. The workload controller connects to C1 and send a request to the orchestration system, as shown in step 1 of the first figure. The rest of the process can be found in the section [Architecture](#architecture). 

<a name="maven" />

### Maven Project

The Arrowhead Framework is implemented in Java as a Maven Project, which uses the Spring Boot Framework and the Model-View-Control (MVC) Design Pattern. This means that the three core systems are implemented as a RESTful web application with web services that can be accessed in this Use Case 2 with HTTPS. For more detailed information to Arrowhead, please go to the official [Arrowhead Github Repository](https://github.com/arrowhead-f/core-java-spring). 

As mentioned in the [Master Branch](https://github.com/igo3r/MIT4.0) in all Use Cases only the three core systems are used. To add the components C1, C2 and C0 the the Arrowhead Maven Project, for each component a Java Maven Module Project have been created. To use the components within the project the pom.xml file has to be updated like shown in the figure below: 

![pom.xml File Final Prototype](/images/mavenfinalprototype.png)

As shown in the figure three lines (32-34) are added to integrate the newly created Maven Module Projects to the Arrowhead root package (core).
In the code base following names where selected: 
* arrowhead-client = Workload-Balancer C0
* arrowhead-consumer = Air Condition System C1
* arrowhead-producer = Temperater Sensor C2

<a name="class" />

### Class Diagram 

In this section a Class Diagram is shown, which indicates how the Controller Classes of the components uses their services to implement the Final Prototype:

* C1 Controller uses the services **turn_aircondition_on** and **turn_aircondition_off**: This component is waiting to be called by C2 to cool down the phyiscal environment, if the authorization rules allow the communication. 
* C2 Controller uses *temperatureMeasurementService* which is called **get_temperature**: This is the Component with which the interactin of a run starts. In the final prototype C2 measures the temperature and checks whether the predefined limit of 25 degrees has been reached. It measures as long as the temperature is above this limit. If this happens, C2 consumes the *orchestrationProcess* to lookup for C1 to cool down the room. 
* OrchestratorController uses *orchestrationProcess*: This action is called each time a component of a CPS is looking for another component or service. Therefore the request is forwarded to the Service Registry for looking up all registered systems and their services. Only if the requested service is registered, the next step is to check the authorization rules by consuming *checkAuthorizationIntraCloudRequest* service. If communication is allowed the Orechstration System return the endpoint data, so the requesting component can consume the requested service. 
* ServiceRegistryController uses *queryRegistry*: This service is used to search in the database for existing systems and services. This service is called during the *orchestrationProcess* to find the requested system and its service(s). 
* AuthorizationController uses *checkAutroizationIntraCloudRequest*: This serives is used to look for authorization rules to determine if two components are allowed to communicate with each other. This services is called during the *orchestrationProcess* to check, whether the requesting compontent and the requested component (in this case C1 and C2) are allowed to interact with each other. 

![Class Diagram Final Prototype](/images/Classdiagrammfinal.png)



<a name="component" />

### Component Diagram

The following component diagram shows how the components within the prototype communicate and are connected with each other. 


![Component Diagram Final Prototype](/images/component.PNG)

<a name="table" />

### Summary Table

<style type="text/css">
.tg  {border-collapse:collapse;border-spacing:0;}
.tg td{border-color:black;border-style:solid;border-width:1px;font-family:Arial, sans-serif;font-size:14px;
  overflow:hidden;padding:10px 5px;word-break:normal;}
.tg th{border-color:black;border-style:solid;border-width:1px;font-family:Arial, sans-serif;font-size:14px;
  font-weight:normal;overflow:hidden;padding:10px 5px;word-break:normal;}
.tg .tg-zlqz{background-color:#c0c0c0;border-color:inherit;font-weight:bold;text-align:center;vertical-align:top}
.tg .tg-c3ow{border-color:inherit;text-align:center;vertical-align:top}
.tg .tg-6e8n{background-color:#c0c0c0;border-color:inherit;font-weight:bold;text-align:left;vertical-align:top}
.tg .tg-7btt{border-color:inherit;font-weight:bold;text-align:center;vertical-align:top}
.tg .tg-0pky{border-color:inherit;text-align:left;vertical-align:top}
</style>
<table class="tg">
<thead>
  <tr>
    <th class="tg-zlqz">Bulding Blocks</th>
    <th class="tg-6e8n">Sub Building Blocks</th>
    <th class="tg-6e8n">Components</th>
    <th class="tg-6e8n">Responsibilites</th>
  </tr>
</thead>
<tbody>
  <tr>
    <td class="tg-7btt">Client<br></td>
    <td class="tg-c3ow">-</td>
    <td class="tg-c3ow">Web Browser</td>
    <td class="tg-0pky">• a client uses a web browser to connect to the workload controller<br>• the user configures via web browser workloads<br>• the user runs different workloads and measures their results</td>
  </tr>
  <tr>
    <td class="tg-7btt">Workload <br>Balancer</td>
    <td class="tg-c3ow">-</td>
    <td class="tg-c3ow">C0</td>
    <td class="tg-0pky">• provides a webservice for configuring different workloads<br>• workloads can be executed and their performance measured</td>
  </tr>
  <tr>
    <td class="tg-7btt" rowspan="6">Arrowhead <br>Framework</td>
    <td class="tg-c3ow" rowspan="2">Application <br>Systems<br>Scenario 1<br></td>
    <td class="tg-c3ow">C1</td>
    <td class="tg-0pky">• provides a webservice that controls an Air-Conditioning System to cool down a physical room<br>• this component initiates the duration in Scenario 1</td>
  </tr>
  <tr>
    <td class="tg-c3ow">C2</td>
    <td class="tg-0pky">• provides a webservice that controls a Temperature Sensor to measure the current temperature of a physical room<br>• this component initiates the duration in Scenario 2</td>
  </tr>
  <tr>
    <td class="tg-c3ow" rowspan="3">Mandatory Core <br>Systems</td>
    <td class="tg-c3ow">Service Registry <br>System</td>
    <td class="tg-0pky">• provides a webservice for looking up registered services within the Arrowhead local cloud</td>
  </tr>
  <tr>
    <td class="tg-c3ow">Orchestrator <br>System</td>
    <td class="tg-0pky">• provides a webservice for finding webservices (by forwarding the request to the Service Registry System) and performing an authorisation check (by consuming the webservice provided by the Authorisation System)<br>• this component handles all requests by a component within an Arrowhead local cloud</td>
  </tr>
  <tr>
    <td class="tg-c3ow">Authorization <br>System </td>
    <td class="tg-0pky">• provides a webservice for checking whether two components are authorised to interact with each other</td>
  </tr>
  <tr>
    <td class="tg-c3ow">Persistence Layer</td>
    <td class="tg-c3ow">Database</td>
    <td class="tg-0pky">• contains all metadata about components, services, orchestration rules and authorisation rules</td>
  </tr>
  <tr>
    <td class="tg-7btt" rowspan="4">Performance <br>Measurement <br>Tools</td>
    <td class="tg-c3ow" rowspan="2">Pinpoint APM </td>
    <td class="tg-c3ow">Pinpoint <br>Collector</td>
    <td class="tg-0pky">• provides a webservice that can be used by a Pinpoint Agent to send performance measurement data<br>• this component collects all performance measurement data and stores it in a database</td>
  </tr>
  <tr>
    <td class="tg-c3ow">Database</td>
    <td class="tg-0pky">• contains all performance measurement data sent by each Pinpoint Agent<br>• based on a column-oriented database for handling Big Data</td>
  </tr>
  <tr>
    <td class="tg-c3ow" rowspan="2">PiLogger One</td>
    <td class="tg-c3ow">PiLogger <br>WebMonitor</td>
    <td class="tg-0pky">• provides a webapplicaition for displaying power consumption measurement results<br>• can be used to configure the PiLogger One measurement board</td>
  </tr>
  <tr>
    <td class="tg-c3ow">CSV Files</td>
    <td class="tg-0pky">• contains all power consumption measurement results measured by a PiLogger One board</td>
  </tr>
  <tr>
    <td class="tg-7btt">Admin</td>
    <td class="tg-c3ow">-</td>
    <td class="tg-c3ow">-</td>
    <td class="tg-0pky">• a user with root-privileges to configure all components, run all scripts and have full control of the entire MIt 4.0 Toolset</td>
  </tr>
  <tr>
    <td class="tg-7btt" rowspan="2">Scripts</td>
    <td class="tg-c3ow">-</td>
    <td class="tg-c3ow">Global Config Files</td>
    <td class="tg-0pky">• provided by the Arrowhead Framework to create all necessary tables for the Arrowhead database</td>
  </tr>
  <tr>
    <td class="tg-c3ow">-</td>
    <td class="tg-c3ow"></td>
    <td class="tg-0pky">• used to define (hardcoded) properties for each Application System<br>• these properties contain configurations and connection strings</td>
  </tr>
</tbody>
</table>


<a name="prototype" />

## How to build the prototype

As mentioned before the prototype contains of seven devices. To illustrate this, the first prototype **Leni 1.0** developed is used, as shown in the figure below. It should be noted, there is no C0 drawn in, this is the reason why online six Raspberry Pi are shown. The C0 is running on a Laptop, this is the reason why it is excluded on this figure.  


![Leni 1.0](/images/leni10.PNG)

In the first prototype **Leni 1.0**, the air condition was also simulated and the temperature values were read from the defined arrays, as in Branches Use Case 1 - 4. This branch deals with the final prototype, which actually measures the environment using a temperature sensor and then changes it using a ventilator. The approach is explained in the following. 

<a name="hardware" />

### How to setup the hardware 

1. The hardware must be purchased, please follow the links below. 

| Technology | Version | Link |
| ---------- | ------ | ---- |
| Raspberry Pi | Model 3 B + | [Purchase](https://www.raspberrypi.org/products/raspberry-pi-3-model-b-plus/) |
| PiLogger One |  1.0 | [Purchase](https://www.pilogger.eu/) |
| Router |  e.g. Ubiquiti Networks ES-8-150W EdgeSwitch | [Purchase](https://www.amazon.de/Ubiquiti-Networks-ES-8-150W-EdgeSwitch-8-150W-Schwarz/dp/B01JP7EQI0)

We used six Rapsberry Pis for the three Arrowhead Core Systems, C1 and C2. For this five Raspberry Pis PiLogger are required for the power consumption measurements. Further C1 uses the PiLogger to integrate a temperature sensor. 
The sixth Rapsberry Pi is the database, whereby this can alternatively be run on the laptop with C0 or virtualised, for example as a VM. If the laptop is used with C0, the IP address must be changed in the application.properties files. Since the power consumption was not important for us, the database does not have to be equipped with a PiLogger, but of course this can be done. 
The Raspberry Pis are connected via LAN, therefore a router is required. It is possible to run it via WLAN, but then the IP addresses must be adapted.

2. Load an image on the Raspberry Pis. 
Initially, the image Rasbian Buster 10 Lite OS (without GUI) was loaded from ([Link](https://www.raspberrypi.org/software/operating-systems/)) and afterwards flashed onto the supplied SD card using a software like BalenaEtcha ([Link](https://www.balena.io/etcher/)).

![Balena Etcher](/images/balenaetcher.PNG)


3. Configure the Rapsberry Pis
   1. Enable SSH and I2C (for PiLogger One)
   2. Activate WLAN to download PiLogger Software and some required Software like Java
   3. Follow the installation instructions of the PiLogger One and make all the necessary settings according it. Installation Instruction can be found [here](https://www.pilogger.de/index.php/en/download-en/send/3-documentation/2-manual-pilogger-one) in English. 
   5. For the usage of PiLogger Webmonitor please follow the [Installation Guide](https://www.pilogger.de/index.php/de/download-de/send/3-documentation/14-anleitung-pilogger-webmonitor). Attention: How to configure the PiLogger is available in English, the *Anleitung PiLogger WebMonitor* is available only in German.  
   6. Configure the network on the Raspberry Pis so that the eth0 interfaces have the correct ip addresses (10.20.30.1 - 10.20.30.6). Instructions can be found [here](https://www.mathworks.com/help/supportpkg/raspberrypi/ug/getting-the-raspberry_pi-ip-address.html). 
   7. Install java 
   	sudo apt update
	sudo apt install default-jdk -y
  8. Install Pinpoint. Instructions can be found [here](https://github.com/pinpoint-apm/pinpoint/blob/master/doc/installation.md). It is recommended to name the PinPoint agents with the system names. 


<a name="start" />

### How to start with Github Code

1. Clone branch from Github 

`` git clone --branch final_prototype https://github.com/igo3r/MIT4.0.git ``

2. Open Development Environment like Eclipse or IntelliJ and import as *existing Maven Project*

![Import Maven Project](/images/import.PNG)

3. To create a Webserver and get access to database, start XAMPP Control Panel (we used v3.2.4). If you work on a Linux machine Apache Webserver and MariaDB can be used. 

![XAMPP Control Panel](/images/xampp.PNG)


4. Enter URL http://10.20.30.6/phpmyadmin/ in Browser

5. Create Empty Arrowhead Database
   1. Click on SQL to enter Queries 
   2. Go to script folder of the Github Project 
   3. Copy content from file *create_empty_arrowhead_db.sql*
   4. Paste the content into the SQL Query field and execute 
   5. It should look similar to the picture below

![Structure Arrowhead Database](/images/emptydatabase.PNG)

6. To start Arrowhead the Source Code needs to be transferred to the Raspberry Pis . Therefore, Maven is used to create jar-File. 
   Go to the folder of the source code (use CMD in Windows or Shell in Linux or the Development Environment) and execute following maven statement: 
   ``` mvn install -DskipTests   ```	
    
   If the command was executed successfully, it should look similar to the following picture:
   
![Successful Maven Build](/images/maven.PNG)

This command causes a target folder to be created in all modules in which, among other things, a jar file has been generated. 

![Target Folder in Service Registry Module](/images/srtarget.PNG)

. This jar file now needs to be transferred to the Raspberry Pi. To do this, a remote connection can be set up via the development environment or programs such as WINSCP (download) can be used. For WinSCP a connection must be established, and afterwards the files can be moved with drag and drop from local environment to Rapsberry Pi. ATTENTION: Move the correct jar-file to the correct Rapsberry, e.g. Consumer jar-file needs to be transferred to 10.20.30.1 (see pictures below). 


![Establish Connection WinSCP to Consumer (10.20.30.1)](/images/winscp1.PNG)

![Move File from local environment to Rapsberry Pi](/images/winscp2.PNG)


7. Start Components of the project on each Raspberry Pi by using the command

	```java  -javaagent:LOCATION_PINPOINT_AGENT.jar -Dpinpoint.agentId=AGENT_ID -Dpinpoint.applicationName=AGENT_NAME -jar LOCATION_ARROWHEAD_SYSTEM ```
	
   Attention, please follow the noted sequence, else the script for database dependencies will not work, as there are other ids for the systems and services: 
   1. ServiceRegistryMain.java
   2. AuthorizationMain.java
   3. OrchestratorMain.java
   4. ConsumerMain.java
   5. ProducerMain.java
   6. ClientMain.java --> has no influence to the database

![Successful start AuthorizationMain.java](/images/successfulstartHTTPS.PNG)

  8. It can be tested by checking the SwaggerSide of the components, like shown in the Picture below for Service Registry. To go to swagger use the ip-addresses and ports from the first picture at the top of the side and put them in the URL line of the browser. If this sides are available, the systems work. 

Attention: in this Branch HTTPS is used. Therefore you have to add https:// in front of the IP-Address, like https://10.20.30.5:1235, else you will get following errormessage: 

![Error message if https:// is missing](/images/errormessageHTTPS_final.PNG)

  By entering the correct URL you will get an "Certificate" Error, because for using HTTPS certificates are required. For each system the certificates are located in the src/main/ressource/certificate folder. The picture below shows the location of the Service Registry Certificate. 
   
   ![Service Registry Certificate Location](/images/locationcertificatesr.png)
   
   This certificate has to be imported in the browser you use to check Swagger. For Firefox Browser you have to go to Settings --> Privacy & Security --> Scroll down to section Security --> Click on View Certificates and it should look like the picture below. 
   
   ![Import Certificate](/images/importcertificate.PNG)
   
   Then click on Import and browse to the location where the cloned Github project is located. Then go to the cerficate folder in src/main/ressources and import the certificate. Afterwards you can enter the URL again and the browser will ask which certificate to use, like shown on the picture below. 
   
   ![Select Certificate](/images/importcertificate2_final.PNG)
   
   Click ok and now the Swagger Webpage should appear. 
   
   ![Swagger Service Registry](/images/serviceregistryswaggerhttps_final.PNG)


9. Now it should similar to the pictures below. The Systems should be registered in Table *system_* (first picture) and the Services in Table *service_registry* (second picture). At this stage C1 and C2 are not able to communicate with each other. 

![Table system_](/images/tablesystem_.PNG)

![Table service_registry](/images/tableservice_registry.PNG)


![Table service_definition](/images/tableservice_definition.PNG)

10. After all systems are started successfully go back to script folder. 
   1. Copy content from file *database_dependencies.sql*
   2. Paste the content into the SQL Query field and execute
   3. Following table will be updated, like shown in the figures below: 
      *  authorization_intra_cloud
      *  authorization_intra_cloud_interface_connection
      *  orechstrator_store

![Table authorization_intra_cloud](/images/tableauthorization_intra_cloud.PNG)

![Table authorization_intra_cloud_interface_connection](/images/tableauthorization_intra_cloud_interface_connection.PNG)


![Table orechstrator_store](/images/tableorchestrator_store.PNG)


11. Now it should work. To test it enter https://10.20.30.23:1239 (C0) in the URL line of the browser to get to the Swagger of the **Arrowhead Client Core System**. 

![Arrowhead Client Core System](/images/clientfinal.PNG)

12. Make the runs using Arrowhead Client Core System API. To do this, click on the *All* tab and go to the second method called **run**. This will start the workload balancer. Important to note is the following: 
   1. innerLoops: this number specifies how many measurements should be taken. It must be an even number, as half of the numbers are below and half are above the defined limit. A maximum of 1000 measurements can be performed. 
   2. innerTimeout: this number specifies how many milliseconds there should be a pause between the measurements. If you want to pause for one second, enter 1000. 
   3. outerLoop: this number indicates how many test runs are to be made. For each test run, the specified number of InnerLoops will be measured. If you enter 10 here and 20 for InnerLoop, then 10 times 20 measurements are carried out. 
   4. outerTimeout: this number specifies how many milliseconds there should be between outer loops. If you want to pause for one second, enter 1000. 

The URL for this is build as followed: /client/run/{outerLoop}/{outerTimeout}/{innerLoop}/{innerTimeout}

Following the results of a test run is shown by taking /client/run/2/1/6/1. First the input is shown and afterwards the result. 

![Swagger Client Test run](/images/testrunclient_UC2.PNG)

![Result of Test run](/images/outputtestruns.png)


<a name="applicationfile" />

### Application.properties Files

In this files the difference to the other branches is obvious, as all ip adresses are different. 

Service Registry System - application.properties file: 

```
spring.datasource.url=jdbc:mysql://10.20.30.6:3306/arrowhead?serverTimezone=Europe/Vienna  
spring.datasource.username=mitadmin
spring.datasource.password=mit
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.jpa.database-platform=org.hibernate.dialect.MySQL5InnoDBDialect
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.hibernate.ddl-auto=none

# Service Registry web-server parameters
server.address=10.20.30.5
server.port=1235
core_system_name=SERVICE_REGISTRY

############################################
###           SECURE MODE                ###
############################################
server.ssl.enabled=true
server.ssl.key-store-type=PKCS12
server.ssl.key-store=classpath:certificates/service_registry.p12
server.ssl.key-store-password=123456
server.ssl.key-alias=service_registry
server.ssl.key-password=123456
server.ssl.client-auth=need
server.ssl.trust-store-type=PKCS12
server.ssl.trust-store=classpath:certificates/truststore.p12
server.ssl.trust-store-password=123456
```

Authorisation System - application.properties file: 

```
spring.datasource.url=jdbc:mysql://10.20.30.6:3306/arrowhead?serverTimezone=Europe/Vienna  
spring.datasource.username=yourusername
spring.datasource.password=yourpassword
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.jpa.database-platform=org.hibernate.dialect.MySQL5InnoDBDialect
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.hibernate.ddl-auto=none

# Authorization web-server parameters

server.address=10.20.30.4
server.port=1234
core_system_name=AUTHORIZATION
sr_address=10.20.30.5
sr_port=1235

############################################
###           SECURE MODE                ###
############################################
server.ssl.enabled=true
server.ssl.key-store-type=PKCS12
server.ssl.key-store=classpath:certificates/authorization.p12
server.ssl.key-store-password=123456
server.ssl.key-alias=authorization
server.ssl.key-password=123456
server.ssl.client-auth=need
server.ssl.trust-store-type=PKCS12
server.ssl.trust-store=classpath:certificates/truststore.p12
server.ssl.trust-store-password=123456
```

Orchestration System - application.properties file: 

```
############################################
###       APPLICATION PARAMETERS         ###
############################################
spring.datasource.url=jdbc:mysql://10.20.30.6:3306/arrowhead?serverTimezone=Europe/Vienna  
spring.datasource.username=yourusername
spring.datasource.password=yourpassword
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

spring.jpa.database-platform=org.hibernate.dialect.MySQL5InnoDBDialect
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.hibernate.ddl-auto=none

# Orchestrator web-server parameters
server.address=10.20.30.3
server.port=1233
core_system_name=ORCHESTRATOR
sr_address=10.20.30.5
sr_port=1235

############################################
###           SECURE MODE                ###
############################################
server.ssl.enabled=true
server.ssl.key-store-type=PKCS12
server.ssl.key-store=classpath:certificates/orchestrator.p12
server.ssl.key-store-password=123456
server.ssl.key-alias=orchestrator
server.ssl.key-password=123456
server.ssl.client-auth=need
server.ssl.trust-store-type=PKCS12
server.ssl.trust-store=classpath:certificates/truststore.p12
server.ssl.trust-store-password=123456
```

It is important to mention that the certificates must as well be adapted to the IP addresses. These were created specifically for the selected IP addresses. If other IP addresses are used, new certificates must be created. 


<a name="addcomponents" />

## How to add C0, C1 and C2

This section describe how to add new components to the system. This is a step by step guide on how C0, C1 and C2 were implemented to see how new components are added. By downloading only the code from the Master Branch, it is possible to add these components. ATTENTION: not all classes have been added completely, as they are included in the branch. If you want to rebuild the Branch, please go to the linked classes to copy them. 

The difference to the other branches is the usage of two services instead of one on C1. 

Important: It is only supposed to illustrate how to add another component, like an additional sensor or actuator, and which classes from arrowhead-common have to be edited. 

<a name="mavenmodule" />

### Create Maven Modules 

Create a new Maven Module Project for each of the component. This section will describe how to add C1, but it is the same way for C2. As mentioned above, the name of C1 in the project structure is *arrowhead-consumer*, while C2 is *arrowhead-producer* and C0 is *arrowhead-client*. 
To add such a project click on File --> New --> Project --> Select Maven Module like shown in the picture below. 


![Add new Maven Module Project](/images/mavenmuodule.PNG)

In the next step the name of the module and the parent project must be selected. The name is *arrowhead-consumer*. 

![Name the Maven Module Project](/images/module-name.PNG)

The parent project is the downloaded **core** project of Arrowhead from the Github. In the picture below *arrowhead-consumer* and *arrowhead-producer* are already integrated, but the marked core project need to be selected as parent. 


![Select Parent Project](/images/parent1.PNG)

After everything was set up correctly in this window it should look like this: 


![Overview New Maven Model](/images/parent2.PNG)

If the window looks like this, click next to select the Archetype. As Arrowhead use Spring Boot, the Archetype with Artefact ID *spring-boot-blank-archetype* should be selected. 

![Select Archetype](/images/archetype.PNG)

After selecting the correct Archetype click next, to come to the next window. In this window nothing has to be changed and it should look like this: 

![Finishing creation of New Maven Module](/images/groupid.PNG)

Click finish to integrate the Maven Module to the project. 

<a name="folderstructure" />

### Create folder structure

Create the following folder structure for C0 (arrowhead-client):


![Folder Structure C0](/images/structureclient.PNG)

Create the following folder structure for C1 (arrowhead-consumer): 

![Folder Structure C1](/images/structureconsumer.PNG)

Create the following folder structure for C2 (arrowhead-producer): 

![Folder Structure C2](/images/structureproducer.PNG)

<a name="application" />

### Application.properties Files

Fill the application.properties File with content. This means to add database connection, server address, ports and information like certificates, for the HTTPS connection. 

application.properties File C0: 

```
spring.datasource.url=jdbc:mysql://10.20.30.6:3306/arrowhead?serverTimezone=Europe/Vienna  
spring.datasource.username=yourusername
spring.datasource.password=yourpassword
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.jpa.database-platform=org.hibernate.dialect.MySQL5InnoDBDialect
spring.jpa.show-sql=false  
spring.jpa.properties.hibernate.format_sql=false
spring.jpa.hibernate.ddl-auto=none

server.address=10.20.30.23
server.port=1239
core_system_name=CLIENT
sr_address=10.20.30.5
sr_port=1235

# ******************* Scenario 2 **********************
c2_address=10.20.30.2
c2_path=producer/get_temperature
c2_port=1232

# ******************* SECURE **********************
server.ssl.enabled=true
server.ssl.key-store-type=PKCS12
server.ssl.key-store=classpath:certificates/client.p12
server.ssl.key-store-password=123456
server.ssl.key-alias=client
server.ssl.key-password=123456
server.ssl.client-auth=need
server.ssl.trust-store-type=PKCS12
server.ssl.trust-store=classpath:certificates/truststore.p12
server.ssl.trust-store-password=123456
```
In the C0 file it is important to give information about the endpoint of the system, which starts the request. In this case C2 is measuring the temperature and check if it is above a limit, if yes, C2 send the value to C1. Therefore, C0 needs to know to start with C2. 


application.properties File C1: 

```
spring.datasource.url=jdbc:mysql://10.20.30.6:3306/arrowhead?serverTimezone=Europe/Vienna  
spring.datasource.username=yourusername
spring.datasource.password=yourpassword
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.jpa.database-platform=org.hibernate.dialect.MySQL5InnoDBDialect
spring.jpa.show-sql=false  
spring.jpa.properties.hibernate.format_sql=false
spring.jpa.hibernate.ddl-auto=none

server.address=10.20.30.1
server.port=1231
core_system_name=CONSUMER
sr_address=10.20.30.5
sr_port=1235

# ******************* SECURE **********************
server.ssl.enabled=true
server.ssl.key-store-type=PKCS12
server.ssl.key-store=classpath:certificates/consumer.p12
server.ssl.key-store-password=123456
server.ssl.key-alias=consumer
server.ssl.key-password=123456
server.ssl.client-auth=need
server.ssl.trust-store-type=PKCS12
server.ssl.trust-store=classpath:certificates/truststore.p12
server.ssl.trust-store-password=123456
```
 C1 do not need an end, it just has to know where to find the Service Registry to register.

application.properties File C2: 

```
spring.datasource.url=jdbc:mysql://10.20.30.6:3306/arrowhead?serverTimezone=Europe/Vienna  
spring.datasource.username=yourusername
spring.datasource.password=yourpassword
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.jpa.database-platform=org.hibernate.dialect.MySQL5InnoDBDialect
spring.jpa.show-sql=false  
spring.jpa.properties.hibernate.format_sql=false
spring.jpa.hibernate.ddl-auto=none

server.address=10.20.30.2
server.port=1232
core_system_name=PRODUCER
c1_address=10.20.30.1
c1_path=/on_ac
c1_port=1231
sr_address=10.20.30.5
sr_port=1235
os_address=10.20.30.3
os_port=1233

############################################
###           SECURE MODE                ###
############################################
server.ssl.enabled=true
server.ssl.key-store-type=PKCS12
server.ssl.key-store=classpath:certificates/producer.p12
server.ssl.key-store-password=123456
server.ssl.key-alias=producer
server.ssl.key-password=123456
server.ssl.client-auth=need
server.ssl.trust-store-type=PKCS12
server.ssl.trust-store=classpath:certificates/truststore.p12
server.ssl.trust-store-password=123456
```
C2 measures the temperature and send this value to C1, if necessary. Therefore the endpoint of C1 (ip address, por and service-url) has to be defined in the application.properties file of C2. Further C2 has to know the endpoint of Service Registry System and of Orechstrator System. 
 

<a name="constants" />

### Add Constants for Components to Arrowhead Source Code

To be able to connect to Arrowhead, the information about the systems has to be added to the code. The properties for Service Registry, Authrization System and Orchestrator System can be found in *arrowhead-core-common/src/main/java/eu/arrowhead/common/CommonConstants.java*.

![Overview Arrowhead CommonConstants.java ](/images/corecommon.PNG)

To avoid changing the code of Arrowhead directly, the package *eu.arrwohead.common.mit* was added to the arrowhead-core-common module, which contains the class **MITConstants.java**. This file contains the properties of C1 and C2 and has the following structure: 
 ```
public interface MITConstants {
	/* --- Common constants --- */
	public static final String SECURE_PROTOCOL = "https://";
	public static final String INSECURE_PROTOCOL = "http://";
	public static final String PORT_COLON = ":";
	public static final String GET_REQUEST_METHOD = "GET";
	public static final String ACCEPT_REQUEST_PROPERTY_KEYWORD = "Accept";
	public static final String ACCEPT_REQUEST_PROPERTY_VALUE = "text/plain";
	public static final String PROPERTY_FILE_NAME = "application.properties";
	public static final DateFormat SDF = new SimpleDateFormat("[dd.MM.yyyy, HH:mm:ss.SSS]: ");
	public static final int MIT_MAX_TASKS = 1000;

	/* --- Consumer constants --- */
	public static final String PROPERTY_C1_ADDRESS = "c1_address";
	public static final String PROPERTY_C1_PATH = "c1_path";
	public static final String PROPERTY_C1_PORT = "c1_port";
	public static final String MIT_SYSTEM_CONSUMER = "Consumer";
	public static final String MIT_CONSUMER_URI = "/consumer";
	public static final String MIT_CONSUMER_SERVICE_TURN_ON = "turn_aircondition_on"; 
	public static final String MIT_CONSUMER_SERVICE_TURN_ON_URI = "/turn_aircondition_on"; 
	public static final String MIT_CONSUMER_SERVICE_TURN_OFF = "turn_aircondition_off"; 
	public static final String MIT_CONSUMER_SERVICE_TURN_OFF_URI = "/turn_aircondition_off"; 	
	public static final String MIT_CONSUMER_SYSTEM_NAME = "consumer";
	
	/* --- Producer Constants --- */
	public static final String PROPERTY_C2_ADDRESS = "c2_address";
	public static final String PROPERTY_C2_PATH = "c2_path";
	public static final String PROPERTY_C2_PORT = "c2_port";
	public static final String MIT_SYSTEM_PRODUCER = "Producer";
	public static final String MIT_PRODUCER_URI = "/producer";
	public static final String MIT_PRODUCER_SERVICE_GET_TEMPERATURE = "get_temperature";
	public static final String MIT_PRODUCER_GET_TEMPERATURE_URI = "/get_temperature/{runs}/{currentRun}";
	public static final String MIT_PRODUCER_SYSTEM_NAME = "producer";
	
	/* --- Producer(C2) upper- and lower-boundaries for temperature measurement --- */
	public static final double MAX_MEASUREMENT_VALUE = 30.0;
	public static final double MIN_MEASUREMENT_VALUE = 20.0;
	public static final double TEMPERATURE_LIMIT = 25.0;
	
	/* ---  Core Systems --- */
	public static final String ORCHESTRATOR_ADDRESS = "os_address";
	public static final String ORCHESTRATOR_PORT = "os_port";
	
	public static final String SERVER_ADDRESS = "server.address";
	public static final String SERVER_PORT = "server.port";
	public static final String SECURITY_MODE= "server.ssl.enabled"; 
	
	public static final String MIT_AIR_CONDITIONING_SYSTEM_OFF = "OFF";
	public static final String MIT_AIR_CONDITIONING_SYSTEM_ON = "ON";
	
	public static final String MIT_SYSTEM_CLIENT = "Client";
	public static final String MIT_CLIENT_URI = "/client";
	public static final String MIT_CLIENT_SERVICE_RUN = "run";
	public static final String MIT_CLIENT_RUN_URI = "/run";
	public static final String MIT_CLIENT_RUN_URI_WITH_PARAMS = "/run/{outerLoop}/{outerTimeout}/{innerLoop}/{innerTimeout}";
	
	public static final int MIT_DEFAULT_CONSUMER_PORT = 2241;
	public static final int MIT_DEFAULT_PRODUCER_PORT = 2242;
	public static final int MIT_DEFAULT_CLIENT_PORT = 2249;
	public static final String PARAMETER_ID = "id";

```

<a name="functionalityc1" />

### Add Functionality to C1 

To be able to fulfil a task C1 requires a functionality, as C1 should simulate an air condition system. Therefore following classes are required: 

![Overview classes consumer](/images/consumerclassesUC2.PNG)

In the package *eu.arrowhead.mit.consumer* the [ApplicationListener](https://github.com/igo3r/MIT4.0/blob/UseCase2/arrowhead-consumer/src/main/java/eu/arrowhead/mit/consumer/ConsumerApplicationInitListener.java), [Controller](https://github.com/igo3r/MIT4.0/blob/UseCase2/arrowhead-consumer/src/main/java/eu/arrowhead/mit/consumer/ConsumerController.java) and [Main](https://github.com/igo3r/MIT4.0/blob/UseCase2/arrowhead-consumer/src/main/java/eu/arrowhead/mit/consumer/ConsumerMain.java) are located. 

**ConsumerMain.java** - required to start this Maven Module:

```
@SpringBootApplication(exclude = SecurityAutoConfiguration.class)
@ComponentScan(CommonConstants.BASE_PACKAGE)
@EntityScan(CoreCommonConstants.DATABASE_ENTITY_PACKAGE)
@EnableJpaRepositories(basePackages = CoreCommonConstants.DATABASE_REPOSITORY_PACKAGE, repositoryBaseClass = RefreshableRepositoryImpl.class)
@EnableSwagger2
public class ConsumerMain {
	public static void main(final String[] args) {
		SpringApplication.run(ConsumerMain.class, args);
	}
}
```

**ConsumerApplicationInitListener.java** - spring boot application startup listener or init Method called when spring application will start. It will be called only once in spring boot application cycle: 
```
@Component
public class ConsumerApplicationInitListener extends ApplicationInitListener{
	@Autowired
	private CommonDBService commonDBService; 
	@Override
	protected void customInit(final ContextRefreshedEvent event) {
		logger.debug("customInit started...");
		if (!isOwnCloudRegistered()) {
			registerOwnCloud(event.getApplicationContext());
		}
	}
	
	private boolean isOwnCloudRegistered() {
		logger.debug("isOwnCloudRegistered started...");
		try {
			commonDBService.getOwnCloud(sslProperties.isSslEnabled());
			return true;
		} catch (final DataNotFoundException ex) {
			return false;
		}
	}
	
	private void registerOwnCloud(final ApplicationContext appContext) {
		logger.debug("registerOwnCloud started...");
			
		if (!standaloneMode) {
			String name = CoreDefaults.DEFAULT_OWN_CLOUD_NAME;
			String operator = CoreDefaults.DEFAULT_OWN_CLOUD_OPERATOR;
				
			if (sslProperties.isSslEnabled()) {
				@SuppressWarnings("unchecked")
				final Map<String,Object> context = appContext.getBean(CommonConstants.ARROWHEAD_CONTEXT, Map.class);
				final String serverCN = (String) context.get(CommonConstants.SERVER_COMMON_NAME);
				final String[] serverFields = serverCN.split("\\.");
				name = serverFields[1];
				operator = serverFields[2];
			}
				
			commonDBService.insertOwnCloud(operator, name, sslProperties.isSslEnabled(), null);
			logger.info("{}.{} own cloud is registered in {} mode.", name, operator, getModeString());
		}
	}
}
```

**ConsumerController.java** - this class contains the services of C2: 
```
@Api(tags = { CoreCommonConstants.SWAGGER_TAG_ALL })
@CrossOrigin(maxAge = Defaults.CORS_MAX_AGE, allowCredentials = Defaults.CORS_ALLOW_CREDENTIALS, 
			 allowedHeaders = { HttpHeaders.ORIGIN, HttpHeaders.CONTENT_TYPE, HttpHeaders.ACCEPT, HttpHeaders.AUTHORIZATION }
)
@RestController
@RequestMapping(MITConstants.MIT_CONSUMER_URI)
public class ConsumerController {
	
	@Autowired
	private ConsumerAirCondition cac;
	
	@GetMapping(path = CommonConstants.ECHO_URI)
	public String echoService() {
		return "Got it!";
	}
	
	@RequestMapping(value = MITConstants.MIT_CONSUMER_SERVICE_TURN_ON_URI, method = RequestMethod.GET, produces = MediaType.TEXT_PLAIN_VALUE)
	ResponseEntity<String> turnAirConditionOn() {
		return new ResponseEntity<String>(cac.turnAirConditionOn(), HttpStatus.OK);
	}
	
	@RequestMapping(value = MITConstants.MIT_CONSUMER_SERVICE_TURN_OFF_URI, method = RequestMethod.GET, produces = MediaType.TEXT_PLAIN_VALUE)
	ResponseEntity<String> turnAirConditionOff() {
		return new ResponseEntity<String>(cac.turnAirConditionOff(), HttpStatus.OK);
	}
}
```


To use Swagger the class **AuthSwaggerConfig.java** in the package *eu.arrowhead.mit.swagger* is required. Therefore the *MITConstants.MIT_SYSTEM_CONSUMER* must be used. 

```
@EnableSwagger2
@Configuration
public class AuthSwaggerConfig extends DefaultSwaggerConfig {
	public AuthSwaggerConfig() {
		super(MITConstants.MIT_SYSTEM_CONSUMER);
	}

	@Bean
	public Docket customizeSwagger() {
		return configureSwaggerForCoreSystem(this.getClass().getPackageName());
	}
}

```

ConsumerAirCondition.java - this class turns on and off of the air condition:

```

@Component
public class ConsumerAirCondition {
	// this class will be extended with the code for the air conditioner
	boolean status = false; 
	
	public ConsumerAirCondition() {}
	
	public ConsumerAirCondition(boolean airCondition) { 
	}
	
	public boolean isStatus() {
		return status;
	}

	public void setStatus(boolean status) {
		this.status = status;
	}

	public String turnAirConditionOn() { 
		String ret = "ON"; 
		try {
			if(isStatus() == false) {
				// necessary to turn on the Light Bulb
				Runtime.getRuntime().exec("/home/pi/PowerPlug/sem-6000.exp AC --on");
			}
			setStatus(true); 
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		return ret; 
	}
	
	public String turnAirConditionOff() {
		String ret = "OFF"; 
		try {
			if(isStatus() == true) {
			// necessary to turn off the Light Bulb
			Runtime.getRuntime().exec("/home/pi/PowerPlug/sem-6000.exp AC --off");
			}
			setStatus(false); 
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		return ret; 
	}
}
```

NOTE: The ConsumerApplicationInitListener.java, the AuthSwaggerConfig.java and the ConsumerConnection.java were taken over and adapted from the Arrowhead Source Code. 

<a name="functionalityc2" />

### Add Functionality to C2

To be able to fulfil a task C2 requires a functionality, as C2 should simulate a temperature sensor to measure the physical environment. Therefore following classes are required: 

![Overview classes producer](/images/producerclassesUC2.PNG)

In the package *eu.arrowhead.mit.producer* the [ApplicationListener](https://github.com/igo3r/MIT4.0/blob/UseCase2/arrowhead-producer/src/main/java/eu/arrowhead/mit/producer/ProducerApplicationInitListener.java), [Controller](https://github.com/igo3r/MIT4.0/blob/UseCase2/arrowhead-producer/src/main/java/eu/arrowhead/mit/producer/ProducerController.java) and [Main](https://github.com/igo3r/MIT4.0/blob/UseCase2/arrowhead-producer/src/main/java/eu/arrowhead/mit/producer/ProducerMain.java) are located. 

**ProducerMain.java** - required to start this Maven Module:

```
@SpringBootApplication(exclude = SecurityAutoConfiguration.class)
@ComponentScan(CommonConstants.BASE_PACKAGE)
@EntityScan(CoreCommonConstants.DATABASE_ENTITY_PACKAGE)
@EnableJpaRepositories(basePackages = CoreCommonConstants.DATABASE_REPOSITORY_PACKAGE, repositoryBaseClass = RefreshableRepositoryImpl.class)
@EnableSwagger2
public class ProducerMain {
	public static void main(final String[] args) {
		SpringApplication.run(ProducerMain.class, args);
	}

}
```

**ProducerApplicationInitListener.java** - spring boot application startup listener or init Method called when spring application will start. It will be called only once in spring boot application cycle: 
```
@Component
public class ProducerApplicationInitListener extends ApplicationInitListener{

	@Autowired
	private CommonDBService commonDBService; 
	
	@Override
	protected void customInit(final ContextRefreshedEvent event) {
		logger.debug("customInit started...");
		if (!isOwnCloudRegistered()) {
			registerOwnCloud(event.getApplicationContext());
		}
	}
	
	private boolean isOwnCloudRegistered() {
		logger.debug("isOwnCloudRegistered started...");
		try {
			commonDBService.getOwnCloud(sslProperties.isSslEnabled());
			return true;
		} catch (final DataNotFoundException ex) {
			return false;
		}
	}
	
	private void registerOwnCloud(final ApplicationContext appContext) {
		logger.debug("registerOwnCloud started...");
			
		if (!standaloneMode) {
			String name = CoreDefaults.DEFAULT_OWN_CLOUD_NAME;
			String operator = CoreDefaults.DEFAULT_OWN_CLOUD_OPERATOR;
				
			if (sslProperties.isSslEnabled()) {
				@SuppressWarnings("unchecked")
				final Map<String,Object> context = appContext.getBean(CommonConstants.ARROWHEAD_CONTEXT, Map.class);
				final String serverCN = (String) context.get(CommonConstants.SERVER_COMMON_NAME);
				final String[] serverFields = serverCN.split("\\.");
				name = serverFields[1];
				operator = serverFields[2];
			}
				
			commonDBService.insertOwnCloud(operator, name, sslProperties.isSslEnabled(), null);
			logger.info("{}.{} own cloud is registered in {} mode.", name, operator, getModeString());
		}
	}
}
```

**ProducerController.java** - this class contains the logic of C1: 
```
@Api(tags = { CoreCommonConstants.SWAGGER_TAG_ALL })
@CrossOrigin(maxAge = Defaults.CORS_MAX_AGE, allowCredentials = Defaults.CORS_ALLOW_CREDENTIALS, allowedHeaders = {
		HttpHeaders.ORIGIN, HttpHeaders.CONTENT_TYPE, HttpHeaders.ACCEPT, HttpHeaders.AUTHORIZATION })
@RestController
@RequestMapping(MITConstants.MIT_PRODUCER_URI)
public class ProducerController {
	@Autowired
	private ProducerConnection pc;

	@GetMapping(path = CommonConstants.ECHO_URI)
	public String echoService() {
		return "Got it!";
	}

	@RequestMapping(value = MITConstants.MIT_PRODUCER_GET_TEMPERATURE_URI, method = RequestMethod.GET, produces = MediaType.TEXT_PLAIN_VALUE)
	ResponseEntity<String> TemperatureAndAirCondition()
			throws IOException {
		String ret = "";
		ProducerTempSensor pts = new ProducerTempSensor(); 
		double temperatureValue = pts.getTempFromSensor(); 
		
			if (temperatureValue >= MITConstants.TEMPERATURE_LIMIT) {
				ret = "\n Temperature Sensor: " + temperatureValue + " °C" + "\n" + " Air-Conditioning System: "
						+ pc.turnAirConditionOn() + "\n";
			} else if(temperatureValue < MITConstants.TEMPERATURE_LIMIT){
				ret = "\n Temperature Sensor: " + temperatureValue + " °C" + "\n" + " Air-Conditioning System: "
						+ pc.turnAirConditionOff()+ "\n";
			}

		return new ResponseEntity<String>(ret, HttpStatus.OK);
	}
}
```


To use Swagger the class **AuthSwaggerConfig.java** in the package *eu.arrowhead.mit.swagger* is required. Therefore the *MITConstants.MIT_SYSTEM_PRODUCER* must be used. 

```
@EnableSwagger2
@Configuration
public class AuthSwaggerConfig extends DefaultSwaggerConfig {
	public AuthSwaggerConfig() {
		super(MITConstants.MIT_SYSTEM_PRODUCER);
	}

	@Bean
	public Docket customizeSwagger() {
		return configureSwaggerForCoreSystem(this.getClass().getPackageName());
	}
}

```

The package *eu.arrowhead.mit.utils* contains the class **ProducerConnection.java** and **ProducerTempGenerator.java**. The **ProducerConnection.java** is necessary to be able to connect in further steps with C1. This class is responsible for the connection to the orchestrator (Line 46 - 58) and the consumer (Line 68 - 100). Further the service which should be used, is called in this class (Line 103 - 125)

The **ProducerTempGenerator.java** class reads the temperature value from an csv File, in which the PiLogger One writes the current temperature value. 


NOTE: The ProducerApplicationInitListener.java and the AuthSwaggerConfig.java  were taken over and adapted from the Arrowhead Source Code. 

<a name="functionalityc0" />

### Add Functionality to C0

To be able to create the workload for the testruns, C0 requires some classes: 

![Overview classes Client](/images/clientclasses.PNG)

In the package *eu.arrowhead.mit.client* the [ApplicationListener](https://github.com/igo3r/MIT4.0/blob/UseCase2/arrowhead-client/src/main/java/eu/arrowhead/mit/client/ClientApplicationInitListener.java), [Controller](https://github.com/igo3r/MIT4.0/blob/UseCase2/arrowhead-client/src/main/java/eu/arrowhead/mit/client/ClientController.java) and [Main](https://github.com/igo3r/MIT4.0/blob/UseCase2/arrowhead-client/src/main/java/eu/arrowhead/mit/client/ClientMain.java) are located. 

**ClientMain.java** - required to start this Maven Module:

```
@SpringBootApplication(exclude = SecurityAutoConfiguration.class)
@ComponentScan(CommonConstants.BASE_PACKAGE)
@EntityScan(CoreCommonConstants.DATABASE_ENTITY_PACKAGE)
@EnableJpaRepositories(basePackages = CoreCommonConstants.DATABASE_REPOSITORY_PACKAGE, repositoryBaseClass = RefreshableRepositoryImpl.class)
@EnableSwagger2
public class ClientMain {

	public static void main(final String[] args) {
		SpringApplication.run(ClientMain.class, args);
	}
}
```

**ClientApplicationInitListener.java** - spring boot application startup listener or init Method called when spring application will start. It will be called only once in spring boot application cycle: 
```
@Component
public class ClientApplicationInitListener extends ApplicationInitListener{

	@Autowired
	private CommonDBService commonDBService; 

	@Override
	protected void customInit(final ContextRefreshedEvent event) {
		logger.debug("customInit started...");
		if (!isOwnCloudRegistered()) {
			registerOwnCloud(event.getApplicationContext());
		}
	}

	private boolean isOwnCloudRegistered() {
		logger.debug("isOwnCloudRegistered started...");
		try {
			commonDBService.getOwnCloud(sslProperties.isSslEnabled());
			return true;
		} catch (final DataNotFoundException ex) {
			return false;
		}
	}
	
	private void registerOwnCloud(final ApplicationContext appContext) {
		logger.debug("registerOwnCloud started...");
			
		if (!standaloneMode) {
			String name = CoreDefaults.DEFAULT_OWN_CLOUD_NAME;
			String operator = CoreDefaults.DEFAULT_OWN_CLOUD_OPERATOR;
				
			if (sslProperties.isSslEnabled()) {
				@SuppressWarnings("unchecked")
				final Map<String,Object> context = appContext.getBean(CommonConstants.ARROWHEAD_CONTEXT, Map.class);
				final String serverCN = (String) context.get(CommonConstants.SERVER_COMMON_NAME);
				final String[] serverFields = serverCN.split("\\.");
				name = serverFields[1];
				operator = serverFields[2];
			}
				
			commonDBService.insertOwnCloud(operator, name, sslProperties.isSslEnabled(), null);
			logger.info("{}.{} own cloud is registered in {} mode.", name, operator, getModeString());
		}
	}
}
```

**ClientController.java** - this class contains the logic of C0. As this class has 177 lines, just the run()-Methode is shown here. For the class [click here](https://github.com/igo3r/MIT4.0/blob/UseCase2/arrowhead-client/src/main/java/eu/arrowhead/mit/client/ClientController.java)
```
@Override
	public void run() {
		RunParams rp;
		ResponseEntity<String> result = null;
		String c1_path; 
		while (running.get()) {
			try {
				Properties prop = cp.getProp();
				c1_path = prop.getProperty(MITConstants.PROPERTY_C1_PATH);
				do {
					rp = runQueue.poll(5, TimeUnit.SECONDS);
				} while (running.get() && Objects.isNull(rp));

				if (!running.get()) {
					break;
				}

				logger.info("Starting new run with parameters:");
				logger.info("OuterLoop/OuterTimeout: {}{}", rp.outerLoop, rp.outerTimeout);
				logger.info("InnerLoop/InnerTimeout: {}{}", rp.innerLoop, rp.innerTimeout);
				for (int i = 1; (i - 1) < rp.outerLoop; i++) {
					logger.info("");
					logger.info("[----------------------- Start - Outer: {} -----------------------]", i);

					for (int j = 1; (j - 1) < rp.innerLoop; j++) {
						try {
							logger.info("");
							logger.info("[Start - Inner: {}]", j);

							if (c1_path != null) {
								result = cc1.run(rp.innerLoop, j);
							} else {
								logger.info("There is no path in the application.properties file.");
							}

							if (result.getStatusCode().is2xxSuccessful()) {
								logger.info("[Run - Outer: {}, Inner: {}]: {}", i, j, result.getBody());
							}
						} catch (IOException e) {
							logger.error(e.getMessage(), e);
						}

						logger.info("[Stop - Inner: {}]", j);
						logger.debug("[Sleep - Inner: {}]", j);
						mysleep(rp.innerTimeout);
					}
					logger.info("");
					logger.info("[----------------------- Stop - Outer: {} ------------------------]", i);
					logger.info("");
					logger.debug("[Sleep - Outer: {}]", i);
					mysleep(rp.outerTimeout);

				}
			} catch (final Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
	}
```


To use Swagger the class **AuthSwaggerConfig.java** in the package *eu.arrowhead.mit.swagger* is required. Therefore the *MITConstants.MIT_SYSTEM_PRODUCER* must be used. 
```
@EnableSwagger2
@Configuration
public class AuthSwaggerConfig extends DefaultSwaggerConfig {
	public AuthSwaggerConfig() {
		super(MITConstants.MIT_SYSTEM_CLIENT);
	}
	
	@Bean
	public Docket customizeSwagger() {
		return configureSwaggerForCoreSystem(this.getClass().getPackageName());
	}
}
```

The package *eu.arrowhead.mit.utils* contains the classes **ClientConnectionUC2.java** and **ClientProperties.java**. The first one is required to connect to C1 to start the testruns. For each inner loop a connection is established with this class. The second one is required to load the correct properties. 

ClientConnectionUC1.java:
```
@Component
public class ClientConnectionUC2 {
	@Autowired
	private HttpService httpService;

	public ResponseEntity<String> run(int runs, int currentRun) throws IOException {
		ResponseEntity<String> retResult = null;
		ClientProperties cp = new ClientProperties();
		Properties prop = cp.getProp();

		String securityMode = prop.getProperty(MITConstants.SECURITY_MODE);
		String scheme = "";

		if (securityMode.equals("false")) {
			scheme = CommonConstants.HTTP;
		} else {
			scheme = CommonConstants.HTTPS;
		}
		System.out.println("Scheme in ClientConnection " + scheme);
		UriComponents providerUri = Utilities.createURI(scheme, prop.getProperty(MITConstants.PROPERTY_C2_ADDRESS),
				Integer.valueOf(prop.getProperty(MITConstants.PROPERTY_C2_PORT)),
				prop.getProperty(MITConstants.PROPERTY_C2_PATH) +"/"+ runs + "/" + currentRun);
		System.out.println("Providerui in Clientconnection " + providerUri);
		retResult = httpService.sendRequest(providerUri, HttpMethod.GET, String.class);
		return retResult;
	}
}


```

ClientProperties.java: 

```
public class ClientProperties {
	public Properties getProp() throws IOException {
		Properties prop = new Properties();		
		String propFileName = MITConstants.PROPERTY_FILE_NAME;
		InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);
		
		if(inputStream != null) {
			prop.load(inputStream);
			return prop;
		} else {
			throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
		}
	}
}
```

NOTE: The ClientApplicationInitListener.java and the AuthSwaggerConfig.java  were taken over and adapted from the Arrowhead Source Code. 


<a name="systemsandservice" />

### Add Systems and Services to Arrowhead

Previous the properties of C1 and C2 were added to the MITConstants.java Class. In this step these systems and their services are included to Arrowhead Source Code as follow: 

Go to *arrowhead-core-common/src/main/java/eu/arrowhead/common/core/CoreSystemService.java* and add following lines after **ORCHESTRATION_SERVICE*** (Line 21):

```
CONSUMER_TURN_ON_SERVICE(MITConstants.MIT_CONSUMER_SERVICE_TURN_ON, MITConstants.MIT_CONSUMER_URI + MITConstants.MIT_CONSUMER_SERVICE_TURN_ON_URI),
CONSUMER_TURN_OFF_SERVICE(MITConstants.MIT_CONSUMER_SERVICE_TURN_OFF, MITConstants.MIT_CONSUMER_URI + MITConstants.MIT_CONSUMER_SERVICE_TURN_OFF_URI),
PRODUCER_GET_TEMPERATURE_SERVICE(MITConstants.MIT_PRODUCER_SERVICE_GET_TEMPERATURE, MITConstants.MIT_PRODUCER_URI + MITConstants.MIT_PRODUCER_GET_TEMPERATURE_URI);
```
In contrast to the other branches, the consumer in the final prototype has two services, one to switch on the air conditioning and one to switch it off. Therefore one line needs to be entered for each service. 

Go to *arrowhead-core-common/src/main/java/eu/arrowhead/common/core/CoreSystem.java* and add following lines at the beginning before SERVICE_REGISTRY:

```
CONSUMER(MITConstants.MIT_DEFAULT_CONSUMER_PORT, List.of(CoreSystemService.CONSUMER_TURN_ON_SERVICE, CoreSystemService.CONSUMER_TURN_OFF_SERVICE)),
PRODUCER(MITConstants.MIT_DEFAULT_PRODUCER_PORT, List.of(CoreSystemService.PRODUCER_GET_TEMPERATURE_SERVICE)),
CLIENT(MITConstants.MIT_DEFAULT_CLIENT_PORT, null),
```

Looking at these inserted lines, it can be seen that these are the previously created constants from the **MITConstants.java** class. By adding the services (provided by C1 and C2) to the class **CoreSystemService.java**, the they are added with their Uri, under which each service can be reached. In **CoreSystem.java**, the systems themselves are created with the services that are provided.  

ATTENTION: C0 is the Workload Balancer, which is used to start testruns. This is the reason why it do not provide a service. 

<a name="summary1" />

### Summary

1. Create Maven Modules for C0, C1 and C2
2. Create folder Structure for C0, C1 and C2
3. Create the application.properties files 
4. Add Constants for C0, C1 and C2 to arrowhead-core-common 
5. Add Functionality to C1
6. Add Functionality to C2
7. Add Functionality to C0
8. Add Systems and Services to Arrowhead
9. Go to [how to start](#start)




<a name="pictures" />

## Pictures final prototype

The picture below shows the final prototype with all components introduced above. 

![Final Prototype Big Picture](/images/leni.jpg)

![Final Prototype Cycle](/images/leni1.jpg)


