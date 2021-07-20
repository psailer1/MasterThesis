# Use Case 1 

In this file the Use Case 1 is described. The table below gives an overview of the other branches with a link to each of them. 

| Branch | Scenario | IP Address | Protocol | Required Hardware | Link |
| -------- | ---------- | ---------- | -------- | ----------------- | ------ |
| Master | Only Arrowhead Coresystems | 127.0.0.1  | HTTPS | 1 Device | [Link](https://github.com/igo3r/MIT4.0) |
| Use Case 1 | Scenario 1 | 127.0.0.1  | HTTPS | 1 Device |  [Link](https://github.com/igo3r/MIT4.0/tree/UseCase1) |
| Use Case 2 | Scenario 2 | 127.0.0.1  | HTTPS | 1 Device | [Link](https://github.com/igo3r/MIT4.0/tree/UseCase2) |
| Use Case 3 | Scenario 1 | 127.0.0.1  | HTTP | 1 Device | [Link](https://github.com/igo3r/MIT4.0/tree/UseCase3) |
| Use Case 4 | Scenario 2 | 127.0.0.1  | HTTP | 1 Device | [Link](https://github.com/igo3r/MIT4.0/tree/UseCase4) |
| Final Prototype | Scenario 2 | 10.20.30.1 - 10.20.30.6 | HTTPS | 6 Devices | [Link](https://github.com/igo3r/MIT4.0/tree/final_prototype)| 

Use Case 1 is implemented with the procedure of Scenario 1 and is considered for the local execution on one device. It used the protocol HTTPS, which means secure communication is possible between the components of the CPS. 

# Architecture

The figure shows the procedure within the CPS between the components. 

![Use Case 1](/images/usecase1.png)



* Step 1: The SC measures the temperature of the physical environment.
* Step 2: The SC  sends a request to the OS asking it to send back a suitable AC.
* Step 3: The OS forwards the request to the SR.
* Step 4: The SR searches the database for suitable actuators and sends information about the AC back to the OS.
* Step 5: The OS asks the AS if they are allowed to communicate with each other. 
* Step 6: The AS searches the existing authorization rules and reports back whether an authorization exists or not (this thesis deals with the case of an existing authorization).
* Step 7: The OS then sends the communication endpoint of the AC to the SC.
* Step 8: The SC sends a request to the AC with the request to activate itself.
* Step 9: The AC activates to cool down the physical environment. 
