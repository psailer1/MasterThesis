# Use Case 4

In this file the Use Case 4 is described. Use Case 4 is implemented with the procedure of Scenario 2 and is considered for the local execution on one device. It used the protocol HTTP, which means communication between the components of the CPS is not secure. 

## Table of Contents


1. [Link to other branches](#branches)
2. [Architecture](#architecture)
3. [Implementation](#implementation)
   * [Workload Balancer](#workload)
   * [Maven Project](#maven)
   * [Class Diagram](#class)
   * [How to start](#start)
4. [How to add C0, C1 and C2](#addcomponents)
   * [Create Maven Module](#mavenmodule)
   * [Create folder Structure for Components](#folderstructure)
   * [Create application.properties Files](#application)
   * [Add Constants for Components in Arrowhead Source Code](#constants)
   * [Add Functionality to C1](#functionalityc1)   
   * [Add Functionality to C2](#functionalityc2)   
   * [Add Functionality to C0](#functionalityc0)
   * [Add Systems and Services to Arrowhead Source Code](#systemsandservice)
   * [Summary](#summary1)

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

![Use Case 4](/images/usecase4.png)

Procedure of Use Case 4: 
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

![SequenceDiagramm Use Case 4](/images/sequencediagrammUC2andUC4.png)

<a name="implementation" />

## Implementation

<a name="workload" />

### Workload Balancer (C0)

As mentioned in the previous section, C0 is used to start the runs. C0 is a workload balancer to ensure that all use cases can be compared with each other in a controlled way. The workload balancer is used to define runs, which specify how many measurements should be taken per run. As MIT 4.0 is used to explore security and performance, this allows both aspects to be analysed. 

To measure Use Case 4, the workload must first be defined and executed. The workload controller connects to C2 and send a request to the orchestration system, as shown in step 1 of the first figure. The rest of the process can be found in the section [Architecture](#architecture). 

<a name="maven" />

### Maven Project

The Arrowhead Framework is implemented in Java as a Maven Project, which uses the Spring Boot Framework and the Model-View-Control (MVC) Design Pattern. This means that the three core systems are implemented as a RESTful web application with web services that can be accessed in this Use Case 4 with HTTP. For more detailed information to Arrowhead, please go to the official [Arrowhead Github Repository](https://github.com/arrowhead-f/core-java-spring). 

As mentioned in the [Master Branch](https://github.com/igo3r/MIT4.0) in all Use Cases only the three core systems are used. To add the components C1, C2 and C0 the the Arrowhead Maven Project, for each component a Java Maven Module Project have been created. To use the components within the project the pom.xml file has to be updated like shown in the figure below: 

![pom.xml File Use Case 4](/images/mavenuc4.png)

As shown in the figure three lines (32-34) are added to integrate the newly created Maven Module Projects to the Arrowhead root package (core).
In the code base following names where selected: 
* arrowhead-client = Workload-Balancer C0
* arrowhead-consumer = Air Condition System C1
* arrowhead-producer = Temperater Sensor C2

<a name="class" />

### Class Diagram 

In this section a Class Diagram is shown, which indicates how the Controller Classes of the components uses their services to implement Use Case 4:

* C1 Controller uses *airConditioningService* which is called **turn_aircondition_on**: This component is waiting to be called by C2 to cool down the phyiscal environment, if the authorization rules allow the communication. 
* C2 Controller uses *temperatureMeasurementService* which is called **get_temperature**: This is the Component with which the interactin of a run starts. In this Use Case C2 measures the temperature and checks whether the predefined limit of 25 degrees has been reached. It measures as long as the temperature is above this limit. If this happens, C2 consumes the *orchestrationProcess* to lookup for C1 to cool down the room. 
* OrchestratorController uses *orchestrationProcess*: This action is called each time a component of a CPS is looking for another component or service. Therefore the request is forwarded to the Service Registry for looking up all registered systems and their services. Only if the requested service is registered, the next step is to check the authorization rules by consuming *checkAuthorizationIntraCloudRequest* service. If communication is allowed the Orechstration System return the endpoint data, so the requesting component can consume the requested service. 
* ServiceRegistryController uses *queryRegistry*: This service is used to search in the database for existing systems and services. This service is called during the *orchestrationProcess* to find the requested system and its service(s). 
* AuthorizationController uses *checkAutroizationIntraCloudRequest*: This serives is used to look for authorization rules to determine if two components are allowed to communicate with each other. This services is called during the *orchestrationProcess* to check, whether the requesting compontent and the requested component (in this case C1 and C2) are allowed to interact with each other. 

![Class Diagram Use Case 4](/images/ClassdiagrammUC2and4andfinal.png)


<a name="start" />

### How to start 

1. Clone branch from Githab 

`` git clone --branch UseCase4 https://github.com/igo3r/MIT4.0.git ``

2. Open Development Environment like Eclipse or IntelliJ and import as *existing Maven Project*

![Import Maven Project](/images/import.PNG)

3. To create a Webserver and get access to database, start XAMPP Control Panel (we used v3.2.4). If you work on a Linux machine Apache Webserver and MariaDB can be used. 

![XAMPP Control Panel](/images/xampp.PNG)


4. Enter URL http://127.0.0.1/phpmyadmin/ in Browser

5. Create Empty Arrowhead Database
   1. Click on SQL to enter Queries 
   2. Go to script folder of the Github Project 
   3. Copy content from file *create_empty_arrowhead_db.sql*
   4. Paste the content into the SQL Query field and execute 
   5. It should look similar to the picture below

![Structure Arrowhead Database](/images/emptydatabase.PNG)

6. Start Components of the project. Attention, please follow the noted sequence, else the script for database dependencies will not work, as there are other ids for the systems and services: 
   1. ServiceRegistryMain.java
   2. AuthorizationMain.java
   3. OrchestratorMain.java
   4. ConsumerMain.java
   5. ProducerMain.java
   6. ClientMain.java --> has no influence to the database

![Successful start AuthorizationMain.java](/images/successfulstart.PNG)

  7. It can be tested by checking the SwaggerSide of the components, like shown in the Picture below for Service Registry. To go to swagger use the ip-addresses and ports from the first picture at the top of the side and put them in the URL line of the browser. If this sides are available, the systems work. Use 127.0.0.1:2245 to go to the Service Registry Swagger Webpage. As this use case use HTTP no certificates are needed. 

   ![Swagger Service Registry](/images/swaggersr.png)


8. Now it should similar to the pictures below. The Systems should be registered in Table *system_* (first picture) and the Services in Table *service_registry* (second picture). At this stage C1 and C2 are not able to communicate with each other. 

![Table system_](/images/tablesystem_UC4.png)

![Table service_registry](/images/tableserviceregistry_uc4.PNG)


![Table service_definition](/images/tableservicedefinition_UC4.PNG)

9. After all systems are started successfully go back to script folder. 
   1. Copy content from file *database_dependencies.sql*
   2. Paste the content into the SQL Query field and execute
   3. Following table will be updated, like shown in the figures below: 
      *  authorization_intra_cloud
      *  authorization_intra_cloud_interface_connection
      *  orechstrator_store

![Table authorization_intra_cloud](/images/tableauthorizationintracloud_uc4.PNG)

![Table authorization_intra_cloud_interface_connection](/images/tableauthorizationintracloudinterfaceconnection_uc4.PNG)


![Table orechstrator_store](/images/tableorchestratorstore_uc4.PNG)


10. Now it should work. To test it enter 127.0.0.1:2258 (C0) in the URL line of the browser to get to the Swagger of the **Arrowhead Client Core System**. 

![Arrowhead Client Core System](/images/client.png)
11. Make the runs using Arrowhead Client Core System API. To do this, click on the *All* tab and go to the second method called **run**. This will start the workload balancer. Important to note is the following: 
   1. innerLoops: this number specifies how many measurements should be taken. It must be an even number, as half of the numbers are below and half are above the defined limit. A maximum of 1000 measurements can be performed. 
   2. innerTimeout: this number specifies how many milliseconds there should be a pause between the measurements. If you want to pause for one second, enter 1000. 
   3. outerLoop: this number indicates how many test runs are to be made. For each test run, the specified number of InnerLoops will be measured. If you enter 10 here and 20 for InnerLoop, then 10 times 20 measurements are carried out. 
   4. outerTimeout: this number specifies how many milliseconds there should be between outer loops. If you want to pause for one second, enter 1000. 

The URL for this is build as followed: /client/run/{outerLoop}/{outerTimeout}/{innerLoop}/{innerTimeout}

Following the results of a test run is shown by taking /client/run/2/1/6/1. First the input is shown and afterwards the result. 

![Swagger Client Test run](/images/testrunclient_UC4.PNG)

![Result of Test run](/images/outputtestruns.png)


<a name="addcomponents" />

## How to add C0, C1 and C2

This section describe how to add new components to the system. This is a step by step guide on how C0, C1 and C2 were implemented to see how new components are added. By downloading only the code from the Master Branch, it is possible to add these components. ATTENTION: not all classes have been added completely, as they are included in the branch. If you want to rebuild the Branch, please go to the linked classes to copy them. 

Important: It is only supposed to illustrate how to add another component, like an additional sensor or actuator, and which classes from arrowhead-common have to be edited. 

<a name="mavenmodule" />

### Create Maven Modules 

Create a new Maven Module Project for each of the component. This section will describe how to add C1, but it is the same way for C2. As mentioned above, the name of C1 in the project structure is *arrowhead-consumer*, while C2 is *arrowhead-producer* and C0 is *arrowhead-client*. 
To add such a project click on File --> New --> Project --> Select Maven Module like shown in the picture below. 


![Add new Maven Module Project](/images/mavenmodule.PNG)

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

Fill the application.properties File with content. This means to add database connection, server address and ports.

application.properties File C0: 

```
spring.datasource.url=jdbc:mysql://127.0.0.1:3306/arrowhead?serverTimezone=Europe/Vienna  
spring.datasource.username=yourusername
spring.datasource.password=yourpassword
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.jpa.database-platform=org.hibernate.dialect.MySQL5InnoDBDialect
spring.jpa.show-sql=false  
spring.jpa.properties.hibernate.format_sql=false
spring.jpa.hibernate.ddl-auto=none

server.address=127.0.0.1
server.port=2258
core_system_name=CLIENT
sr_address=127.0.0.1
sr_port=2255

# ******************* Scenario 2 **********************
c2_address=127.0.0.1
c2_path=producer/get_temperature
c2_port=2252

# ******************* INSECURE **********************
server.ssl.enabled=false
server.ssl.key-store-type=""
server.ssl.key-store=""
server.ssl.key-store-password=""
server.ssl.key-password=""
server.ssl.trust-store=""
server.ssl.trust-store-password=""
```
In the C0 file it is important to give information about the endpoint of the system, which starts the request. In this case C2 is measuring the temperature and check if it is above a limit, if yes, C2 send the value to C1. Therefore, C0 needs to know to start with C2. 


application.properties File C1: 

```
spring.datasource.url=jdbc:mysql://127.0.0.1:3306/arrowhead?serverTimezone=Europe/Vienna  
spring.datasource.username=yourusername
spring.datasource.password=yourpassword
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.jpa.database-platform=org.hibernate.dialect.MySQL5InnoDBDialect
spring.jpa.show-sql=false  
spring.jpa.properties.hibernate.format_sql=false
spring.jpa.hibernate.ddl-auto=none

server.address=127.0.0.1
server.port=2251
core_system_name=CONSUMER
sr_address=127.0.0.1
sr_port=2255

# ******************* INSECURE **********************
server.ssl.enabled=false
server.ssl.key-store-type=""
server.ssl.key-store=""
server.ssl.key-store-password=""
server.ssl.key-password=""
server.ssl.trust-store=""
server.ssl.trust-store-password=""
```
 C1 do not need an end, it just has to know where to find the Service Registry to register.

application.properties File C2: 

```
spring.datasource.url=jdbc:mysql://127.0.0.1:3306/arrowhead?serverTimezone=Europe/Vienna  
spring.datasource.username=yourusername
spring.datasource.password=yourpassword
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.jpa.database-platform=org.hibernate.dialect.MySQL5InnoDBDialect
spring.jpa.show-sql=false  
spring.jpa.properties.hibernate.format_sql=false
spring.jpa.hibernate.ddl-auto=none

server.address=127.0.0.1
server.port=2252
core_system_name=PRODUCER
sr_address=127.0.0.1
sr_port=2255
os_address=127.0.0.1
os_port=2253
c1_address=127.0.0.1
c1_path=/on_ac
c1_port=2251

# ******************* INSECURE **********************
server.ssl.enabled=false
server.ssl.key-store-type=""
server.ssl.key-store=""
server.ssl.key-store-password=""
server.ssl.key-password=""
server.ssl.trust-store=""
server.ssl.trust-store-password=""
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

![Overview classes consumer](/images/consumerclassesuc4.PNG)

In the package *eu.arrowhead.mit.consumer* the [ApplicationListener](https://github.com/igo3r/MIT4.0/blob/UseCase4/arrowhead-consumer/src/main/java/eu/arrowhead/mit/consumer/ConsumerApplicationInitListener.java), [Controller](https://github.com/igo3r/MIT4.0/blob/UseCase4/arrowhead-consumer/src/main/java/eu/arrowhead/mit/consumer/ConsumerController.java) and [Main](https://github.com/igo3r/MIT4.0/blob/UseCase4/arrowhead-consumer/src/main/java/eu/arrowhead/mit/consumer/ConsumerMain.java) are located. 

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

ConsumerAirCondition.java - this class simulates turning on and off of the air condition:

```
@Component
public class ConsumerAirCondition {
  public ConsumerAirCondition() {}
	
	public ConsumerAirCondition(boolean airCondition) { 
	}
	
	public void setAirCondition(boolean airCondition) { 
	}
	
	public String turnAirConditionOn() { 
		setAirCondition(true); 
		String ret = "ON"; 
		return ret; 
	}
	
	public String turnAirConditionOff() {
		setAirCondition(true); 
		String ret = "OFF"; 
		return ret; 
	}
}
```

NOTE: The ConsumerApplicationInitListener.java, the AuthSwaggerConfig.java and the ConsumerConnection.java were taken over and adapted from the Arrowhead Source Code. 

<a name="functionalityc2" />

### Add Functionality to C2

To be able to fulfil a task C2 requires a functionality, as C2 should simulate a temperature sensor to measure the physical environment. Therefore following classes are required: 

![Overview classes producer](/images/producerclassesuc4.PNG)

In the package *eu.arrowhead.mit.producer* the [ApplicationListener](https://github.com/igo3r/MIT4.0/blob/UseCase4/arrowhead-producer/src/main/java/eu/arrowhead/mit/producer/ProducerApplicationInitListener.java), [Controller](https://github.com/igo3r/MIT4.0/blob/UseCase4/arrowhead-producer/src/main/java/eu/arrowhead/mit/producer/ProducerController.java) and [Main](https://github.com/igo3r/MIT4.0/blob/UseCase4/arrowhead-producer/src/main/java/eu/arrowhead/mit/producer/ProducerMain.java) are located. 

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

	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Runs: Please enter an even number between 0 and 1000 \n currentRun: Enter a number below runs."),
			@ApiResponse(code = 406, message = "Please follow the instructions. You entered not acceptable numbers."), })
	@RequestMapping(value = MITConstants.MIT_PRODUCER_GET_TEMPERATURE_URI, method = RequestMethod.GET, produces = MediaType.TEXT_PLAIN_VALUE)
	ResponseEntity<String> emperatureArraySingleEntry(@PathVariable int runs, @PathVariable int currentRun)
			throws IOException {
		String ret = "";
		int runBound = runs / 2;
		ProducerTempGenerator ptg = new ProducerTempGenerator();
		double temperatureValue = ptg.getTemperature();
		if (runs <= 1000 && currentRun <= runs && (runs % 2 == 0)) {
			temperatureValue = ptg.getTemperatureArray(currentRun - 1, runBound);
			if (temperatureValue >= MITConstants.TEMPERATURE_LIMIT) {
				ret = "\n Temperature Sensor: " + temperatureValue + " °C" + "\n" + " Air-Conditioning System: "
						+ pc.turnAirConditionOn() + "\n";
			} else {
				ret = "\n Temperature Sensor: " + temperatureValue + " °C" + "\n" + " Air-Conditioning System: OFF \n";
			}
		} else {
			ret = "Please enter an even number between 1 and 1000!";
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

The **ProducerTempGenerator.java** class simulates the measurement of the physical environment. It is defined, that half of the testruns, which will be conducted, have a value below the limit of 25 degrees, while the other half is above. This ensures, that the testruns are compareable. Therefore, two arrays (VALUES_TEMPERATURE_HIGH and VALUES_TEMPERATURE_LOW) are added to *arrowhead-core-common/src/main/java/eu/arrowhead/common/mit/MITConstants.java* (Line 74 and 76) with 500 values each. This means a maximum of 1000 inner loops can be performed. 

```
public class ProducerTempGenerator {
	double maxValue = MITConstants.MAX_MEASUREMENT_VALUE;
	double minValue = MITConstants.MIN_MEASUREMENT_VALUE;

	public double getTemperature() {
		Random r = new Random();
		double value = (r.nextInt((int) ((maxValue - minValue) * 10 + 1)) + minValue * 10) / 10.0;
		return value;
	}

	public double getTemperatureArray(int runs, int runBound) {
		double temperature = 0.0;
		if (runs < runBound) {
			temperature = MITConstants.VALUES_TEMPERATURE_LOW[runs];
		} else {
			temperature = MITConstants.VALUES_TEMPERATURE_HIGH[runs - runBound];
		}
		return temperature;
	}
}
```

NOTE: The ProducerApplicationInitListener.java and the AuthSwaggerConfig.java  were taken over and adapted from the Arrowhead Source Code. 

<a name="functionalityc0" />

### Add Functionality to C0

To be able to create the workload for the testruns, C0 requires some classes: 

![Overview classes Client](/images/clientclasses.PNG)

In the package *eu.arrowhead.mit.client* the [ApplicationListener](https://github.com/igo3r/MIT4.0/blob/UseCase4/arrowhead-client/src/main/java/eu/arrowhead/mit/client/ClientApplicationInitListener.java), [Controller](https://github.com/igo3r/MIT4.0/blob/UseCase4/arrowhead-client/src/main/java/eu/arrowhead/mit/client/ClientController.java) and [Main](https://github.com/igo3r/MIT4.0/blob/UseCase4/arrowhead-client/src/main/java/eu/arrowhead/mit/client/ClientMain.java) are located. 

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

**ClientController.java** - this class contains the logic of C0. As this class has 177 lines, just the run()-Methode is shown here. For the class [click here](https://github.com/igo3r/MIT4.0/blob/UseCase4/arrowhead-client/src/main/java/eu/arrowhead/mit/client/ClientController.java)
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
CONSUMER_CLTC_ARRAY_SINGLE_SERVICE(MITConstants.MIT_SERVICE_CLTC_ARRAY_SINGLE, MITConstants.MIT_CONSUMER_URI + MITConstants.MIT_CONSUMER_CLTC_ARRAY_SINGLE_URI),
PRODUCER_GET_ARRAY_SERVICE(MITConstants.MIT_PRODUCER_SERVICE_GET_ARRAY, MITConstants.MIT_PRODUCER_URI + MITConstants.MIT_PRODUCER_GET_ARRAY_URI_CONNECTION);
```

Go to *arrowhead-core-common/src/main/java/eu/arrowhead/common/core/CoreSystem.java* and add following lines at the beginning before SERVICE_REGISTRY:

```
CONSUMER(MITConstants.MIT_DEFAULT_CONSUMER_PORT, List.of(CoreSystemService.CONSUMER_CLTC_ARRAY_SINGLE_SERVICE)),
PRODUCER(MITConstants.MIT_DEFAULT_PRODUCER_PORT, List.of(CoreSystemService.PRODUCER_GET_ARRAY_SERVICE)),
CLIENT(MITConstants.MIT_DEFAULT_CLIENT_PORT, null),
```

Looking at these inserted lines, it can be seen that these are the previously created constants from the **MITConstants.java** class. By adding the services (provided by C1 and C2) to the class **CoreSystemService.java**, the they are added with their Uri, under which each service can be reached. In **CoreSystem.java**, the systems themselves are created with the services that are provided. It is important to mention that a line must be added to **CoreSystemService.java** for each service provided, however, this service must only be added to **CoreSystem.java** for the providing system. Since both C1 and C2 only provide one service each, such a listing can be viewed at the Authorisation System (**CoreSystemService.java** Line 14 to 18 and **CoreSystem.java** Line 21 to 23).

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
