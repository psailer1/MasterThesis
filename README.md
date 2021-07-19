# Description of Prototype for MIT 4.0

## Table of Contents

1. [Presentation Project](#project)
2. [Scenarios](#scenarios)
3. [Technologies](#technologies)
    1. [Raspberry Pi](#raspi)
    2. [PiLogger One](#pilogger)
    3. [Arrowhead Framework](#arrowhead)
        * [Service Registry](#sr)
        * [Authorisation System](#as)
        * [Orchestrator System](#os)
    5. [Pinpoint APM](#pinpoint)
    6. [Summary with links](#links)
5. [Description different branches](#branches)
   1. [Master Branch](#master)
   2. [UseCase1 Branch](#usecase1)
   3. [UseCase2 Branch](#usecase2)
   4. [UseCase3 Branch](#usecase3)
   5. [UseCase4 Branch](#usecase4)
   6. [final_prototype Branch](#finalprototype)
   7. [Summary Branches](#summarybranches)

<a name="project" />

## Presentation Project

Was das Projekt überhaupt zeigen soll --> Gibts da ein Deliverable was ich heranziehen kann? Ich weiß zwar was der Prototyp zeigen soll, aber bezüglich MIT 4.0 bin ich mir nicht sicher ob ich es 100%  richtig beschreiben kann. 

<a name="scenarios" />

## Scenarios

Scenario 1: Consumer to Producer --> Erklärung wie das Funktioniert

Eher Sequence Diagram oder diese Bilder? 

![Scenario 1: Actuator to Sensor Solution](/images/scenario1.PNG)

Scenario 2: Producer to Consumer --> Erklärung Unterschied Scenario 1

Eher Sequence Diagram oder oder diese Bilder? 

![Scenario 2: Sensor to Actuator Solution](/images/scenario2.PNG)

<a name="technologies" />

## Technologies 

Erklären welche Technologien wir verwendet haben 

<a name="raspi" />


### Raspberry Pi

Used Version Image: TBD 

Raspi mit welchen Image


<a name="pilogger" />

### PiLogger One

Used Version Software: 0v10 Beta 

To measure the power, the PiLogger One is used as Power Measurement Device, shown in the figure below. It is an expansion board which can be plugged onto the Rapsberry Pi Model 3B+. 

![PiLogger One as extension for Rapsberry Pi ](/images/pilogger.PNG )

The PiLogger One is connected to the Raspberry Pi via Inter-Integrated Circuit I2C-Bus. To be able to measure current and voltage with the PiLogger One, it has to be integrated into the circuit. For this a USB charging cable was cut to integrate it into the 8 pin terminal view. Thereby the first two terminals are declared as input, while the third and fourth terminals serve as output for the loop-through. The other terminal slots provide the possibility to connect a pulse counter and a temperature sensor to measure various values like wind speed or temperature. 

An important point, why the PiLogger was chosen, is the availability of its own time base. This enables precise time intervals between measurements without being influenced by the actual activities of the Raspberry Pi. The PiLogger can be operated by two software, one for an OS with a GUI and the so called WebMonitor for an OS without GUI. Since in the prototype with Rasbian Strecht Lite an OS without GUI was chosen, the Webmonitor interface is used for the power measurements. This can be accessed via the respective IP address of the Rapsberry Pis with port 8080. As shown in Figure \ref{webmonitor}, various values like voltage in Volt (V), current in Amperes (A), power in W, resistance in Ohm (Ω) and several others are displayed. 


![Webmonitor PiLogger One](/images/pilogger3.png)


Furthermore, these values are logged and saved in a log file, where a differentiation is made between the latest measured value, minimum, maximum and average. In addition, diagrams with the logged values can be created. In summary, PiLogger WebMonitor can be used for logging and processing measurement data. By means of the setting functions in the WebMonitor the control over the whole PiLogger One extension can be obtained. 


<a name="arrowhead" />

### Arrowhead Framework

Used Version: Arrowhead Framework 4.1.3

The Arrowhead Framework is an IoT framework that helps manage interactions within a CPS. It facilitates the creation of local automation clouds consisting of different devices, different application-specific systems and services to perform automation tasks. Furthermore, the Arrowhead framework offers the possibility to create a boundary between the local cloud and the open internet or external activities. This results in the ability to pair local (on-premise and private) real-time performance and security with simple and cost-effective engineering. Furthermore, it can be used for multi-cloud interactions, which means it also offers scalability. Since Arrowhead is an open-source project written in Java, it can be extended with any functionality needed to support multiple use cases. It should be mentioned that Arrowhead includes various systems, which are listed under the [official Github](https://github.com/arrowhead-f/core-java-spring) of the Arrowhead Consortia. For Arrowhead to work, at least the three core systems Service Registry System, Authorisation System and Orchestration System must be used. Therefore, a database is used to store information about the local cloud. 

<a name="sr" />

#### Service Registry

In the first step, the service registry is responsible for ensuring application systems can register and provide their services. This is necessary in order for other application systems in the network to be able to use these services. Furthermore, the service registry can remove these entries from the database or update them if necessary. Finally, it is responsible for allowing application systems to use its look-up functionality to find and use publicly offered services of other application systems. The service registry works together with the orchestration system. 


<a name="as" />

#### Authorisation System

The authorisation system is responsible for managing the intra-cloud access rules. This means it describes which application system may use which services of another application system. It further manages the inter-cloud access rules, which define which other local clouds are allowed to consume which services from this cloud. This is relevant when using multi-cloud. 


<a name="os" />

#### Orchestration System

The Orchestration System is responsible for the binding between the application systems at runtime. This means that the Orchestration System takes over the entire orchestration process between the previously registered systems. This is done by respecting rules telling the application system which other application system it should connect to. In order for these orchestration rules to work, some information is needed, such as the reachability information of the respective application system (e.g. IP address and port), details of the service instance (e.g. URL) and authorisation-related information (e.g. certificates). In this way, the connection between two application systems can be established so that the services can be used with each other. 


<a name="pinpoint" />

### Pinpoint APM 

Pinpoint is an open source APM tool used to measure the performance of large distributed systems using an agent-based approach. Thereby, the overall structure of a system is analysed and it is shown how several components of a system interact with each other (e.g. the core systems in Arrowhead). Each component is equipped with an agent, which monitors the runtime and provides information on the code level about which transaction has been executed. It is highlighted that the use of Pinpoint increases resource consumption by only 3 %. Since it is as well written in Java, it is compatible with the Arrowhead framework. Furthermore, it can instrument the Arrowhead core systems to track transactions without changing a line of code. Like PiLogger One, Pinpoint offers different metrics to measure the performance of the tasks being performed. 

![Webmonitor PiLogger One](/images/pinpoint.PNG)
 
 <a name="links" />

### Summary

| Technology | Hardware | Software | Version | Link |
| ---------- | -------- | -------- | ------- | ---- |
| Raspberry Pi | X  |  | Model 3 B + | [Purchase](https://www.raspberrypi.org/products/raspberry-pi-3-model-b-plus/) |
| Raspberry Pi OS Lite |  | X | tbd | [Download](https://www.raspberrypi.org/software/operating-systems/#raspberry-pi-os-32-bit) |
| PiLogger One | X |  | 1.0 | [Purchase](https://www.pilogger.eu/) |
| PiLogger One Webmonitor |  | X | 0v10 Beta | [Download Software](https://www.pilogger.de/index.php/de/download-de/category/2-software), [Download Installation Description](https://www.pilogger.de/index.php/de/download-de/category/3-documentation)|
| Arrowhead Framework |  | X | 4.1.3 | [Download](https://github.com/arrowhead-f/core-java-spring) |
| Java Version |  | X | tbd | [Download]() |
| PinPoint APM  |  | X | tbd | [Download]() |


<a name="branches" />

## Description branches 

<a name="master" />

### Master

[Click here to go to Master Branch](https://github.com/igo3r/MIT4.0/tree/main)

<a name="usecase1" />

### Use Case 1


[Click here to go to UseCase1 Branch](https://github.com/igo3r/MIT4.0/tree/UseCase1)


<a name="usecase2" />

### Use Case 2

[Click here to go to UseCase2 Branch](https://github.com/igo3r/MIT4.0/tree/UseCase2)


<a name="usecase3" />

### Use Case 3

[Click here to go to UseCase3 Branch](https://github.com/igo3r/MIT4.0/tree/UseCase3)



<a name="usecase4" />

### Use Case 4

[Click here to go to UseCase4 Branch](https://github.com/igo3r/MIT4.0/tree/UseCase4)


<a name="finalprototype" />

### Final Prototype

[Click here to go to final_prototype Branch](https://github.com/igo3r/MIT4.0/tree/final_prototype)


<a name="summarybranches" />

### Summary 

Tabelle machen mit unterschieden 

| Branch | Scenario | IP-Address | Protocol | Required Hardware |
| -------- | ---------- | ---------- | -------- | ----------------- |
| Master | Only Arrowhead Coresystems | 127.0.0.1  | HTTPS | 1 Device |
| Use Case 1 | Scenario 1 | 127.0.0.1  | HTTPS | 1 Device |
| Use Case 2 | Scenario 2 | 127.0.0.1  | HTTPS | 1 Device |
| Use Case 3 | Scenario 1 | 127.0.0.1  | HTTP | 1 Device |
| Use Case 4 | Scenario 2 | 127.0.0.1  | HTTP | 1 Device |
| Final Prototype | Scenario 2 | 10.20.30.1 - 10.20.30.6 | HTTPS | 6 Devices |
