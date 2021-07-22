# Use Case 1 

In this file the Use Case 1 is described. Use Case 1 is implemented with the procedure of Scenario 1 and is considered for the local execution on one device. It used the protocol HTTPS, which means secure communication is possible between the components of the CPS. 

## Table of Contents


1. [Link to other branches](#branches)
1. [Architecture](#architecture)
2. [Implementation](#implementation)

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

### Workload Balancer (C0)

As mentioned in the previous section, C0 is used to start the runs. C0 is a workload balancer to ensure that all use cases can be compared with each other in a controlled way. The workload balancer is used to define runs, which specify how many measurements should be taken per run. As MIT 4.0 is used to explore security and performance, this allows both aspects to be analysed. 

To measure Use Case 1, the workload must first be defined and executed. The workload controller connects to C1 and send a request to the orchestration system, as shown in step 1 of the first figure. The rest of the process can be found in the section [Architecture](#architecture). 

### Maven Project

The Arrowhead Framework is implemented in Java as a Maven Project, which uses the Spring Boot Framework and the Model-View-Control (MVC) Design Pattern. This means that the three core systems are implemented as a RESTful web application with web services that can be accessed in this Use Case 1 with HTTPS. For more detailed information to Arrowhead, please go to the official [Arrowhead Github Repository](https://github.com/arrowhead-f/core-java-spring). 

As mentioned in the [Master Branch](https://github.com/igo3r/MIT4.0) in all Use Cases only the three core systems are used. To add the components C1, C2 and C0 the the Arrowhead Maven Project, for each component a Java Maven Module Project have been created. To use the components within the project the pom.xml file has to be updated like shown in the figure below: 

![pom.xml File Use Case 1](/images/mavenuc1t.png)

As shown in the figure three lines (32-34) are added to integrate the newly created Maven Module Projects to the Arrowhead root package (core).
In the code base following names where selected: 
* arrowhead-client = Workload-Balancer C0
* arrowhead-consumer = Air Condition System C1
* arrowhead-producer = Temperater Sensor C2

### Class Diagram 

In this section a Class Diagram is shown, which indicates how the Controller Classes of the components uses their services to implement Use Case 1:

* C1 Controller uses *airConditioningService* which is called **cltc_array_single**: This is the Component with which the interaction of a run starts. It first consumes the *orchestrationProcess* service to loopup C2. After it receives the information required it consumes the *temperatureMeasurementService* of C2. After C2 measured the temperature and sent it back to C1, C1 decides if cooling down is necessary or not. If it is necessary C1 set the Air Condition System from false to true, which simulates the activation by using a boolean variable. 
* C2 Controller uses *temperatureMeasurementService* which is called **get_array**: C1 provides the temperatureMeasurementService, which can by used to get the current temperature of an physical environment. In this use case there is a predefined array, from which the temperature value is taken. After C2 has the temperature value it send it back to the requesting component. 
* OrchestratorController uses *orchestrationProcess*: This action is called each time a component of a CPS is looking for another component or service. Therefore the request is forwarded to the Service Registry for looking up all registered systems and their services. Only if the requested service is registered, the next step is to check the authorization rules by consuming *checkAuthorizationIntraCloudRequest* service. If communication is allowed the Orechstration System return the endpoint data, so the requesting component can consume the requested service. 
* ServiceRegistryController uses *queryRegistry*: This service is used to search in the database for existing systems and services. This service is called during the *orchestrationProcess* to find the requested system and its service(s). 
* AuthorizationController uses *checkAutroizationIntraCloudRequest*: This serives is used to look for authorization rules to determine if two components are allowed to communicate with each other. This services is called during the *orchestrationProcess* to check, whether the requesting compontent and the requested component (in this case C1 and C2) are allowed to interact with each other. 

