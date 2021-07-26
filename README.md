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
   1. [Master Branch](#master1)
   2. [UseCase1 Branch](#usecase1)
   3. [UseCase2 Branch](#usecase2)
   4. [UseCase3 Branch](#usecase3)
   5. [UseCase4 Branch](#usecase4)
   6. [final_prototype Branch](#finalprototype)
   7. [Summary Branches](#summarybranches)
6. [How to start](#start)

<a name="project" />

## Presentation Project
 Github repository is used to document the developed prototype for the project **MIT 4.0 - Messung der IT-Sicherheit in Industrie 4.0**, which is led by Forschung Burgenland and funded under the EFRE programme "Investionen in Wachstum und Beschäftigung Österreich 2014-2020". 

As the name of the project indicates, Industry 4.0 is a forward-looking field, with security being a particularly important factor. Industry 4.0 is about networking software and IT systems with production facilities in order to increase efficiency in production with the resulting cyber-physical systems (CPS). This networking results in new security risks which must be kept under control to guarantee the success of the company. To prevent security risks, compromises must be made, such as reducing computing power or increasing time by strengthening encryption mechanisms. In other words, it is a trade-off between the necessary reduction of the threat risk through security mechanisms, and the effort required to implement these mechanisms at the different levels.  Therefore, the aim of this project is the development of a tool-set to develop methods for the evaluation of performance and security for Industry 4.0 applications, which can be termed as a strong foundation-oriented focus. 

<a name="scenarios" />

## Scenarios

This section describes two scenarios which have been implemented in the different [branches](#branches) of this Github repository. 

In both scenarios, a CPS is formed, which means that several components are interconnected to measure the physical world, perform calculations and change the physical world based on this, if necessary. Therefore, the simple scenario of a temperature control of a room is used. In the scenarios, the CPS consists of two components (C1, C2) that are involved in the closed-loop temperature control.  C1 acts as an actuator, which uses an air-conditioning system, and C2 as a sensor, which measures the temperature. The goal of this interaction is to control the temperature of a physical space by periodically measuring the current temperature and activating an air-conditioning system when the temperature reaches a predefined limit, in this case 25°C. 

In scenario 1, the actuator (C1) needs the temperature to decide whether to cool the room or not. To do this, it sends a request to the sensor (C2), which measures the temperature and sends the temperature value back. This means that this run is carried out periodically without knowing beforehand whether cooling must take place or not. The interaction in this scenario is started by C1, as shown in the figure below. 

![Scenario 1: Actuator to Sensor Solution](/images/scenario1.PNG)

Another approach would be to measure the temperature periodically and perform the temperature limit calculation on the sensor component (C2). On the one hand, this means that the temperature is only sent to the actuator (C1) when cooling is necessary, but on the other hand, it is important to realise that the sensor component must be able to perform this calculation. The interaction in this scenario is started by C2, as shown in the figure below. 

![Scenario 2: Sensor to Actuator Solution](/images/scenario2.PNG)

<a name="technologies" />

## Technologies 

In this section the used Hardware and Software is described to get an overview about their functionality. 

<a name="raspi" />


### Raspberry Pi

Used Version Image: TBD 

For the components of the CPS Raspberry Pi 3 Model B+ were used. With this model it is possible to use the PiLogger One for power consumption measurements. To minimise the influence of unnecessary services, in order not to influence the measurements too much, the Lite image of Raspbian was used. Alternatively, the desktop version could be used if a GUI is preferred. For both scenarios, however, the GUI loose Lite Image is sufficient, since the components only have to be started via it. 


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
| XAMPP Control Panel | | X | tbd | tbd |

It should be noted that we have built and tested the various use cases with these versions, so there may be problems when using older or newer versions. 


<a name="branches" />

## Description branches 

This section gives an overview of the different branches. For more detailed information of a branch click on the link to go to the selected specific branch. 
All branches except final are executed locally on a device with the IP address 127.0.0.1. The branch final_prototype has a separate device for each component, hence different IP addresses for the components are entered in the figures. 

As already mentioned, the communication protocol HTTP as well as HTTPS can be used. For the distinction, different ports would be chosen for each variant. Each port has four digits, with HTTP 225x and HTTPS 224x as the pattern. As an example, the service registry port in HTTP **2254** and HTTPS **2244** is taken. All used ports are listed in the table below. 

| Component | HTTPS | HTTP | 
| --------- | ---- | ----- |
| C1 | 2241 | 2251 |
| C2 | 2242 | 2252 |
| Orchestration System | 2243 | 2253 |
| Authorization System | 2244 | 2254 |
| Service Registry System | 2245 | 2255 |
| Database | 3306 | 3306 |
| PiLogger One Webmonitor | 8080 | 8080 |
| Pinpoint APM | tbd | tbd |

Each branch description provides a figure, including C1, C2 and Arrowhead with the core systems. The figures show the procedure of the CPS and illustrate the individual ip-addresses and the associated ports for each component, like described in the table above. 

<a name="master1" />

### Master

The master branch includes the three Arrowhead core systems Service Registry System, Authorization System and Orchestration System in Version 4.1.3. As the Arrowhead Framework includes more Systems like mentioned above this brunch provides a **Clean Code Version**. The Master branch is intended for local execution of the code via HTTPS. This branch is the basis for the other brunches by extending it with C1 and C2. 

![Arrowhead Core Systems](/images/coresystems.png)

The connection between the core systems is defined in the application.properties files. In this file the own ip address is defined, followed by the endpoints of the service registry and the reference to the required certificate. Furthermore the connection information to the database are stored in this file. 

Service Registry System - application.properties file: 

```
############################################
###       APPLICATION PARAMETERS         ###
############################################

spring.datasource.url=jdbc:mysql://127.0.0.1:3306/arrowhead?serverTimezone=Europe/Budapest
spring.datasource.username=yourusername
spring.datasource.password=yourpassword
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

spring.jpa.database-platform=org.hibernate.dialect.MySQL5InnoDBDialect
# use true only for debugging
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.hibernate.ddl-auto=none

# Service Registry web-server parameters
server.address=127.0.0.1
server.port=2245
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
############################################
###       APPLICATION PARAMETERS         ###
############################################

spring.datasource.url=jdbc:mysql://127.0.0.1:3306/arrowhead?serverTimezone=Europe/Budapest
spring.datasource.username=yourusername
spring.datasource.password=yourpassword
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

spring.jpa.database-platform=org.hibernate.dialect.MySQL5InnoDBDialect
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.hibernate.ddl-auto=none

# Authorization web-server parameters
server.address=127.0.0.1
server.port=2244
sr_port=2245
sr_address=127.0.0.1
core_system_name=AUTHORIZATION

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
spring.datasource.url=jdbc:mysql://127.0.0.1:3306/arrowhead?serverTimezone=Europe/Budapest
spring.datasource.username=yourusername
spring.datasource.password=yourpassword
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

spring.jpa.database-platform=org.hibernate.dialect.MySQL5InnoDBDialect
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.hibernate.ddl-auto=none

# Orchestrator web-server parameters
server.address=127.0.0.1
server.port=2243
core_system_name=ORCHESTRATOR
sr_address=127.0.0.1
sr_port=2245

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

If these files are compared with each other, it can be seen that the Service Registry only contains its own parameters, while Authorization System and Orchestration System contain those of the Service Registry in addition to their own. This is needed to allow Authorization System and Orchestration System to register as a system at the SR to provide their services. 

Furhter it should be mentioned that `server.ssl.enabled=true` defines that HTTPS is used. If this parameter is set to `false` HTTP will be used, which means no certificates are required. 


<a name="usecase1" />

### Use Case 1

Use Case 1 is the implementation of [Scenario 1](#scenarios) using the communication protocol HTTPS. The Arrowhead Framework is used to ensure authorized communication between C1 and C2. The branch is intended for local execution of the code on one device.


![Use Case 1](/images/usecase1_corrected.png)

[For detailed information click here to go to UseCase1 Branch](https://github.com/igo3r/MIT4.0/tree/UseCase1)


<a name="usecase2" />

### Use Case 2

Use Case 2 is the implementation of [Scenario 2](#scenarios) using the communication protocol HTTPS. The Arrowhead Framework is used to ensure authorized communication between C1 and C2. The branch is intended for local execution of the code on one device.

![Use Case 2](/images/usecase2.png)

[For detailed information click here to go to UseCase2 Branch](https://github.com/igo3r/MIT4.0/tree/UseCase2)


<a name="usecase3" />


### Use Case 3

Use Case 3 is the implementation of [Scenario 1](#scenarios) using the communication protocol HTTP. The Arrowhead Framework is used to ensure authorized communication between C1 and C2. The branch is intended for local execution of the code on one device.

![Use Case 3](/images/usecase3_corrected.png)

[For detailed information click here to go to UseCase3 Branch](https://github.com/igo3r/MIT4.0/tree/UseCase3)



<a name="usecase4" />

### Use Case 4

Use Case 4 is the implementation of [Scenario 2](#scenarios) using the communication protocol HTTP. The Arrowhead Framework is used to ensure authorized communication between C1 and C2. The branch is intended for local execution of the code on one device.

![Use Case 4](/images/usecase4.png)

[For detailed information click here to go to UseCase4 Branch](https://github.com/igo3r/MIT4.0/tree/UseCase4)


<a name="finalprototype" />

### Final Prototype

This branch provides the implementation of the final prototype of this project, which uses [Scenario 2](#scenarios) and the communication protocol HTTPS. The Arrowhead Framework is used to ensure authorized communication between C1 and C2. The branch is intended for the execution of the code on six devices. As shown in the figure below each component of the CPS has its own ip address in the range from 10.20.30.1-10.20.30.6, whereby the last provides the database. 

![Final Prototype](/images/finalprototype.png)

[For detailed information click here to go to final_prototype Branch](https://github.com/igo3r/MIT4.0/tree/final_prototype)


<a name="summarybranches" />

### Summary 


| Branch | Scenario | IP Address | Protocol | Required Hardware |
| -------- | ---------- | ---------- | -------- | ----------------- |
| Master | Only Arrowhead Coresystems | 127.0.0.1  | HTTPS | 1 Device |
| Use Case 1 | Scenario 1 | 127.0.0.1  | HTTPS | 1 Device |
| Use Case 2 | Scenario 2 | 127.0.0.1  | HTTPS | 1 Device |
| Use Case 3 | Scenario 1 | 127.0.0.1  | HTTP | 1 Device |
| Use Case 4 | Scenario 2 | 127.0.0.1  | HTTP | 1 Device |
| Final Prototype | Scenario 2 | 10.20.30.1 - 10.20.30.6 | HTTPS | 6 Devices |

<a name="start" />

### How to start

1. Clone branch from Githab 

`` git clone https://github.com/igo3r/MIT4.0.git ``

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
   1. ServiceRegistryMain.java <br>
      Location: arrowhead-serviceregistry/src/main/java/eu/arrowhead/core/serviceregistry/ServiceRegistryMain.java
   3. AuthorizationMain.java <br>
      Location: arrowhead-authorization/src/main/java/eu/arrowhead/core/authorization/AuthorizationMain.java
   5. OrchestratorMain.java <br>
      Location: arrowhead-orchestrator/src/main/java/eu/arrowhead/core/orchestrator/OrchestratorMain.java

![Successful start AuthorizationMain.java](/images/successfulstartHTTPS.PNG)

  7. It can be tested by checking the SwaggerSide of the components, like shown in the picture below for Service Registry. To go to swagger use the ip-addresses and ports in the table below put them in the URL line of the browser. If this sides are available, the systems work. 

| System | IP-Address | Port |
| ------ | ---------- | ---- |
| Service Registry System | 127.0.0.1 | 2245 |
| Authorization System | 127.0.0.1 | 2244 |
| Orchestration System | 127.0.0.1 | 2243 |

Attention: in this Branch HTTPS is used. Therefore you have to add https:// in front of the IP-Address, like https://127.0.0.1:2245, else you will get following errormessage: 

![Error message if https:// is missing](/images/errormessageHTTPS.PNG)

  By entering the correct URL you will get an "Certificate" Error, because for using HTTPS certificates are required. For each system the certificates are located in the src/main/ressource/certificate folder. The picture below shows the location of the Service Registry Certificate. 
   
   ![Service Registry Certificate Location](/images/locationcertificatesr.png)
   
   This certificate has to be imported in the browser you use to check Swagger. For Firefox Browser you have to go to Settings --> Privacy & Security --> Scroll down to section Security --> Click on View Certificates and it should look like the picture below. 
   
   ![Import Certificate](/images/importcertificate.PNG)
   
   Then click on Import and browse to the location where the cloned Github project is located. Then go to the cerficate folder in src/main/ressources and import the certificate. Afterwards you can enter the URL again and the browser will ask which certificate to use, like shown on the picture below. 
   
   ![Select Certificate](/images/importcertificate2.PNG)
   
   Click ok and now the Swagger Webpage should appear. 
   
   ![Swagger Service Registry](/images/serviceregistryswaggerhttps.PNG)

   8. Now it should similar to the pictures below. The Systems should be registered in Table *system_* (first picture) and the Services in Table *service_registry* (second picture).

![Table system_](/images/tablesystemHTTPSmaster.PNG)

![Table service_registry](/images/tableserviceregistryHTTPSmaster.PNG)

   The other tables remain empty in this branch, because they are required to set orchestration and authorization information for communication of components. 

9. Now it should work, but as there is no component implemented in this Branch, nothing will happen. This code will be extended in Branch Use Case 1 - 4 and Final Prototype with Components. 
