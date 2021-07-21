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
