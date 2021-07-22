# Use Case 1 

In this file the Use Case 1 is described. Use Case 1 is implemented with the procedure of Scenario 1 and is considered for the local execution on one device. It used the protocol HTTPS, which means secure communication is possible between the components of the CPS. 

## Table of Contents


1. [Link to other branches](#branches)
1. [Architecture](#architecture)
2. [Implementation](#implementation)
  * [Workload Balancer](#workload)
  * [Maven Project](#maven)
  * [Class Diagram](#class)
  * [How to start](#start)

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

![Use Case 1](/images/usecase1_corrected.png)


Procedure of Use Case 1: 
* Step 1: C1 required the temperature to make a decision if cooling the room is necessary. Therefore a reqest to get the temperature from C2 is sent to Orchestration System.
* Step 2: The Orchestration System forwards the request to the SR.
* Step 3: The Service Requistry System searches the database for suitable actuators and sends information about C2 back to the Orchestration System.
* Step 4: The Orchestration System asks the Authorization System if they are allowed to communicate with each other. 
* Step 5: The Authorization System searches the existing authorization rules and reports back whether an authorization exists or not.
* Step 6: The Orchestration System then sends the communication endpoint of C2 to C1.
* Step 7: C1 sends a request to the C2 to request the current temperature.
* Step 8: C2 measures the phyiscal environment. 
* Step 9: C2 returns the measured temperature to C1. 
* Step 10: C1 cools down the phyiscal environment, if the receives temperature is above a predefined limit. 

The sequence diagram below shows a more detailed procedure, by showing which services are called by which component. Furthermore it includes the database, which stores the data, and C0, with which it is possible to start the run in CPS. Since C0 and the database have no influence on the scenario itself, they are not shown in the previous figure. 

![SequenceDiagramm Use Case 1](/images/sequencediagrammUC1andUC3.png)

<a name="implementation" />

## Implementation

<a name="workload" />

### Workload Balancer (C0)

As mentioned in the previous section, C0 is used to start the runs. C0 is a workload balancer to ensure that all use cases can be compared with each other in a controlled way. The workload balancer is used to define runs, which specify how many measurements should be taken per run. As MIT 4.0 is used to explore security and performance, this allows both aspects to be analysed. 

To measure Use Case 1, the workload must first be defined and executed. The workload controller connects to C1 and send a request to the orchestration system, as shown in step 1 of the first figure. The rest of the process can be found in the section [Architecture](#architecture). 

<a name="maven" />

### Maven Project

The Arrowhead Framework is implemented in Java as a Maven Project, which uses the Spring Boot Framework and the Model-View-Control (MVC) Design Pattern. This means that the three core systems are implemented as a RESTful web application with web services that can be accessed in this Use Case 1 with HTTPS. For more detailed information to Arrowhead, please go to the official [Arrowhead Github Repository](https://github.com/arrowhead-f/core-java-spring). 

As mentioned in the [Master Branch](https://github.com/igo3r/MIT4.0) in all Use Cases only the three core systems are used. To add the components C1, C2 and C0 the the Arrowhead Maven Project, for each component a Java Maven Module Project have been created. To use the components within the project the pom.xml file has to be updated like shown in the figure below: 

![pom.xml File Use Case 1](/images/mavenuc1t.png)

As shown in the figure three lines (32-34) are added to integrate the newly created Maven Module Projects to the Arrowhead root package (core).
In the code base following names where selected: 
* arrowhead-client = Workload-Balancer C0
* arrowhead-consumer = Air Condition System C1
* arrowhead-producer = Temperater Sensor C2

<a name="class" />

### Class Diagram 

In this section a Class Diagram is shown, which indicates how the Controller Classes of the components uses their services to implement Use Case 1:

* C1 Controller uses *airConditioningService* which is called **cltc_array_single**: This is the Component with which the interaction of a run starts. It first consumes the *orchestrationProcess* service to loopup C2. After it receives the information required it consumes the *temperatureMeasurementService* of C2. After C2 measured the temperature and sent it back to C1, C1 decides if cooling down is necessary or not. If it is necessary C1 set the Air Condition System from false to true, which simulates the activation by using a boolean variable. 
* C2 Controller uses *temperatureMeasurementService* which is called **get_array**: C1 provides the temperatureMeasurementService, which can by used to get the current temperature of an physical environment. In this use case there is a predefined array, from which the temperature value is taken. After C2 has the temperature value it send it back to the requesting component. 
* OrchestratorController uses *orchestrationProcess*: This action is called each time a component of a CPS is looking for another component or service. Therefore the request is forwarded to the Service Registry for looking up all registered systems and their services. Only if the requested service is registered, the next step is to check the authorization rules by consuming *checkAuthorizationIntraCloudRequest* service. If communication is allowed the Orechstration System return the endpoint data, so the requesting component can consume the requested service. 
* ServiceRegistryController uses *queryRegistry*: This service is used to search in the database for existing systems and services. This service is called during the *orchestrationProcess* to find the requested system and its service(s). 
* AuthorizationController uses *checkAutroizationIntraCloudRequest*: This serives is used to look for authorization rules to determine if two components are allowed to communicate with each other. This services is called during the *orchestrationProcess* to check, whether the requesting compontent and the requested component (in this case C1 and C2) are allowed to interact with each other. 

![Class Diagram Use Case 1](/images/ClassdiagrammUC1and3.png)


<a name="start" />

### How to start 

1. Clone branch from Githab 

`` git clone --branch UseCase1 https://github.com/igo3r/MIT4.0.git ``

2. Open Development Environment like Eclipse or IntelliJ and import as *existing Maven Project*

![Import Maven Project](/images/import.PNG)

3. To create a Webserver and get access to database, start XAMPP Control Panel (we used v3.2.4). If you work on a Linux machine Apache Webserver and MariaDB can be used. 

![XAMPP Control Panel](/images/xampp.PNG)


4. Enter URL [http://127.0.0.1/phpmyadmin/](http://127.0.0.1/phpmyadmin/) in Browser

5. Create Empty Arrowhead Database
   1. Click on SQL to enter Queries 
   2. Go to script folder of the Github Project 
   3. Copy content from file *create_empty_arrowhead_db.sql*
   4. Paste the content into the SQL Query field and execute 
   5. It should look similar to the picture below

![Structure Arrowhead Database](/images/emptydatabase.PNG)

6. Start Components of the project. Attention, please follow the noted sequence: 
   1. ServiceRegistryMain.java
   2. AuthorizationMain.java
   3. OrchestratorMain.java
   4. ConsumerMain.java
   5. ProducerMain.java
   6. ClientMain.java --> has no influence to the database

![Successful start AuthorizationMain.java](/images/successfulstart.PNG)

  7. It can be tested by checking the SwaggerSide of the components, like shown in the Picture below for Service Registry. To go to swagger use the ip-addresses and ports from the first picture at the top of the side and put them in the URL line of the browser. If this sides are available, the systems work. 

![Swagger Service Registry](/images/swaggersr.png)


7. Now it should similar to the pictures below. The Systems should be registered in Table *system_* (first picture) and the Services in Table *service_registry* (second picture). At this stage C1 and C2 are not able to communicate with each other. 

![Table system_](/images/system.PNG)

![Table service_registry](/images/serviceregistry.PNG)

7. After all are started successfully go back to script folder. 
   1. Copy content from file *database_dependencies.sql*
   2. Paste the content into the SQL Query field and execute

8. Now it should work. To test it enter 127.0.0.1:2258 (C0) in the URL line of the browser to get to the Swagger of the **Arrowhead Client Core System**. 

![Arrowhead Client Core System](/images/client.png)

9. Make the runs using Arrowhead Client Core System API. To do this, click on the *All* tab and go to the second method called **run**. This will start the workload balancer. Important to note is the following: 
   1. innerLoops: this number specifies how many measurements should be taken. It must be an even number, as half of the numbers are below and half are above the defined limit. A maximum of 1000 measurements can be performed. 
   2. innerTimeout: this number specifies how many milliseconds there should be a pause between the measurements. If you want to pause for one second, enter 1000. 
   3. outerLoop: this number indicates how many test runs are to be made. For each test run, the specified number of InnerLoops will be measured. If you enter 10 here and 20 for InnerLoop, then 10 times 20 measurements are carried out. 
   4. outerTimeout: this number specifies how many milliseconds there should be between outer loops. If you want to pause for one second, enter 1000. 

DA KOMMT NOCH EIN BILD HER 
