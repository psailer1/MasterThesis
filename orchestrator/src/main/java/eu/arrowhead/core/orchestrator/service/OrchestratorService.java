package eu.arrowhead.core.orchestrator.service;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.database.entity.OrchestratorStore;
import eu.arrowhead.common.database.entity.ServiceDefinition;
import eu.arrowhead.common.dto.internal.DTOConverter;
import eu.arrowhead.common.dto.internal.DTOUtilities;
import eu.arrowhead.common.dto.shared.CloudRequestDTO;
import eu.arrowhead.common.dto.shared.OrchestrationFlags;
import eu.arrowhead.common.dto.shared.OrchestrationFlags.Flag;
import eu.arrowhead.common.dto.shared.OrchestrationFormRequestDTO;
import eu.arrowhead.common.dto.shared.OrchestrationResponseDTO;
import eu.arrowhead.common.dto.shared.OrchestrationResultDTO;
import eu.arrowhead.common.dto.shared.OrchestratorWarnings;
import eu.arrowhead.common.dto.shared.PreferredProviderDataDTO;
import eu.arrowhead.common.dto.shared.ServiceInterfaceResponseDTO;
import eu.arrowhead.common.dto.shared.ServiceQueryFormDTO;
import eu.arrowhead.common.dto.shared.ServiceQueryResultDTO;
import eu.arrowhead.common.dto.shared.ServiceRegistryResponseDTO;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;
import eu.arrowhead.common.dto.shared.SystemResponseDTO;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.core.orchestrator.database.service.OrchestratorStoreDBService;
import eu.arrowhead.core.orchestrator.matchmaking.IntraCloudProviderMatchmakingAlgorithm;
import eu.arrowhead.core.orchestrator.matchmaking.IntraCloudProviderMatchmakingParameters;

@Service
public class OrchestratorService {
	
	//=================================================================================================
	// members
	
	private static final String NULL_PARAMETER_ERROR_MESSAGE = " is null.";
	private static final String NULL_OR_BLANK_PARAMETER_ERROR_MESSAGE = " is null or blank.";
	private static final String LESS_THAN_ONE_ERROR_MESSAGE= " must be greater than zero.";
	private static final String MORE_THAN_ONE_ERROR_MESSAGE= " must not have more than one element.";
	
	private static final int EXPIRING_TIME_IN_MINUTES = 2;
	
	private static final Logger logger = LogManager.getLogger(OrchestratorService.class);
	
	@Autowired
	private OrchestratorDriver orchestratorDriver;
	
	@Autowired
	private OrchestratorStoreDBService orchestratorStoreDBService;
	
	@Resource(name = CoreCommonConstants.INTRA_CLOUD_PROVIDER_MATCHMAKER)
	private IntraCloudProviderMatchmakingAlgorithm intraCloudProviderMatchmaker;
	
	@Value(CoreCommonConstants.$ORCHESTRATOR_IS_GATEKEEPER_PRESENT_WD)
	private boolean gateKeeperIsPresent;
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	/**
	 * This method represents the orchestration process where the requester System is NOT in the local Cloud. This means that the Gatekeeper made sure
	 * that this request from the remote Orchestrator can be satisfied in this Cloud. (Gatekeeper polled the Service Registry and Authorization
	 * Systems.)
	 */
	@SuppressWarnings("squid:S1612")
	public OrchestrationResponseDTO externalServiceRequest(final OrchestrationFormRequestDTO request) {
		logger.debug("externalServiceRequest started ...");
		checkServiceRequestForm(request, false);
		
		// Querying the Service Registry to get the list of Provider Systems
		final OrchestrationFlags flags = request.getOrchestrationFlags();
		final ServiceQueryResultDTO queryResult = orchestratorDriver.queryServiceRegistry(request.getRequestedService(), flags.get(Flag.METADATA_SEARCH), flags.get(Flag.PING_PROVIDERS));
		
		List<ServiceRegistryResponseDTO> queryData = queryResult.getServiceQueryData();
	    // If necessary, removing the non-preferred providers from the SR response. (If necessary, matchmaking is done after this at the request sender Cloud.)
		if (flags.get(Flag.ONLY_PREFERRED)) {  
			// This request contains only local preferred systems, since this request came from another cloud, but the unboxing is necessary
			final List<PreferredProviderDataDTO> localProviders = request.getPreferredProviders().stream().filter(p -> p.isLocal()).collect(Collectors.toList());
			queryData = removeNonPreferred(queryData, localProviders);
		}

		logger.debug("externalServiceRequest finished with {} service providers.", queryData.size());
		
		return compileOrchestrationResponse(queryData, request);
	}


	//-------------------------------------------------------------------------------------------------	
	public OrchestrationResponseDTO topPriorityEntriesOrchestrationProcess(final OrchestrationFormRequestDTO orchestrationFormRequestDTO, final Long systemId) {
		logger.debug("topPriorityEntriesOrchestrationProcess started ...");		
		
		if (orchestrationFormRequestDTO == null) {
			throw new InvalidParameterException("request" + NULL_PARAMETER_ERROR_MESSAGE);
		}
		
		orchestrationFormRequestDTO.validateCrossParameterConstraints();
			
		if (systemId != null && systemId < 1) {
			throw new InvalidParameterException("systemId " + LESS_THAN_ONE_ERROR_MESSAGE);
		}
		
		final long validSystemId = (systemId != null ? systemId.longValue() : validateSystemRequestDTO(orchestrationFormRequestDTO.getRequesterSystem()));
		final SystemRequestDTO consumerSystemRequestDTO = orchestrationFormRequestDTO.getRequesterSystem();
		
		final List<OrchestratorStore> entryList = orchestratorStoreDBService.getAllTopPriorityOrchestratorStoreEntriesByConsumerSystemId(validSystemId);
		if (entryList == null || entryList.isEmpty()) {
			return new OrchestrationResponseDTO(); // empty response
		}
		
		final List<ServiceRegistryResponseDTO> crossCheckedEntryList = crossCheckTopPriorityEntries(entryList, orchestrationFormRequestDTO, consumerSystemRequestDTO);
		if (crossCheckedEntryList == null || crossCheckedEntryList.isEmpty()) {
			return new OrchestrationResponseDTO(); // empty response
		}
		
		return compileOrchestrationResponse(crossCheckedEntryList, orchestrationFormRequestDTO);
	}
	
	//-------------------------------------------------------------------------------------------------	
	public OrchestrationResponseDTO orchestrationFromStore(final OrchestrationFormRequestDTO orchestrationFormRequestDTO) {
		logger.debug("orchestrationFromStore started ...");		
		
		if (orchestrationFormRequestDTO == null) {
			throw new InvalidParameterException("request" + NULL_PARAMETER_ERROR_MESSAGE);
		}
		
		if (orchestrationFormRequestDTO.getRequestedService() == null) {
			return topPriorityEntriesOrchestrationProcess(orchestrationFormRequestDTO, null);
		}
	
		orchestrationFormRequestDTO.validateCrossParameterConstraints();

		final List<OrchestratorStore> entryList = getOrchestrationStoreEntries(orchestrationFormRequestDTO.getRequesterSystem(), orchestrationFormRequestDTO.getRequestedService());	        
		if (entryList == null || entryList.isEmpty()) {
			return new OrchestrationResponseDTO(); // empty response
		}
		
		final List<ServiceRegistryResponseDTO> authorizedLocalServiceRegistryEntries = getAuthorizedServiceRegistryEntries(entryList, orchestrationFormRequestDTO);
        
		return getHighestPriorityCurrentlyWorkingStoreEntryFromEntryList(orchestrationFormRequestDTO, entryList, authorizedLocalServiceRegistryEntries);
	}

	//-------------------------------------------------------------------------------------------------	
	/**
	 * Represents the regular orchestration process where the requester system is in the local Cloud. In this process the
     * <i>Orchestrator Store</i> is ignored, and the Orchestrator first tries to find a provider for the requested service in the local Cloud.
	 */
	public OrchestrationResponseDTO dynamicOrchestration(final OrchestrationFormRequestDTO request) {
		logger.debug("dynamicOrchestration started ...");

		// necessary, because we want to use a flag value when we call the check method
		if (request == null) {
			throw new InvalidParameterException("request" + NULL_PARAMETER_ERROR_MESSAGE);
		}
		
		final OrchestrationFlags flags = request.getOrchestrationFlags();
		// Querying the Service Registry to get the list of Provider Systems
		final ServiceQueryResultDTO queryResult = orchestratorDriver.queryServiceRegistry(request.getRequestedService(), flags.get(Flag.METADATA_SEARCH), flags.get(Flag.PING_PROVIDERS));
		List<ServiceRegistryResponseDTO> queryData = queryResult.getServiceQueryData();
		if (queryData.isEmpty()) {
			
				return new OrchestrationResponseDTO(); // empty response
			
		}
		
	    // Cross-checking the SR response with the Authorization
		queryData = orchestratorDriver.queryAuthorization(request.getRequesterSystem(), queryData);
		if (queryData.isEmpty()) {
			
				return new OrchestrationResponseDTO(); // empty response
			
		}
		
		final List<PreferredProviderDataDTO> localProviders = request.getPreferredProviders().stream().filter(p -> p.isLocal()).collect(Collectors.toList());

		// If necessary, removing the non-preferred providers from the SR response. 
		if (flags.get(Flag.ONLY_PREFERRED)) {
			queryData = removeNonPreferred(queryData, localProviders);
			if (queryData.isEmpty()) {
					return new OrchestrationResponseDTO(); // empty response
			}
		}

		// If matchmaking is requested, we pick out 1 ServiceRegistryEntry entity from the list.
		if (flags.get(Flag.MATCHMAKING)) {
			final IntraCloudProviderMatchmakingParameters params = new IntraCloudProviderMatchmakingParameters(localProviders);
			// set additional parameters here if you use a different matchmaking algorithm
			final ServiceRegistryResponseDTO selected = intraCloudProviderMatchmaker.doMatchmaking(queryData, params);
			queryData.clear();
			queryData.add(selected);
		}

		// all the filtering is done
		logger.debug("dynamicOrchestration finished with {} service providers.", queryData.size());
		
		return compileOrchestrationResponse(queryData, request);
	}
	
	//-------------------------------------------------------------------------------------------------
	public OrchestrationResponseDTO storeOchestrationProcessResponse(final long systemId) {
		logger.debug("storeOchestrationProcessResponse started ...");
		
		final SystemResponseDTO validConsumerSystemResponseDTO  =  validateSystemId(systemId);
		final SystemRequestDTO systemRequestDTO = DTOConverter.convertSystemResponseDTOToSystemRequestDTO(validConsumerSystemResponseDTO);
	    final OrchestrationFormRequestDTO orchestrationFormRequestDTO = new OrchestrationFormRequestDTO.Builder(systemRequestDTO).build();

	    return topPriorityEntriesOrchestrationProcess(orchestrationFormRequestDTO, systemId);
	}
	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	private void checkServiceRequestForm(final OrchestrationFormRequestDTO request, final boolean cloudCheckInProviders) {
		logger.debug("checkExternalServiceRequestForm started ...");
		
		if (request == null) {
			throw new InvalidParameterException("request" + NULL_PARAMETER_ERROR_MESSAGE);
		}
		
		request.validateCrossParameterConstraints();
		
		// Requested service
		checkRequestedServiceForm(request.getRequestedService());
		
		// Preferred Providers
		checkPreferredProviders(request.getPreferredProviders(), cloudCheckInProviders);
	}

	//-------------------------------------------------------------------------------------------------
	private void checkRequestedServiceForm(final ServiceQueryFormDTO form) {
		logger.debug("checkRequestedServiceForm started ...");
		
		if (form == null) {
			throw new InvalidParameterException("form" + NULL_PARAMETER_ERROR_MESSAGE);
		}
		
		if (Utilities.isEmpty(form.getServiceDefinitionRequirement())) {
			throw new InvalidParameterException("Service definition requirement" + NULL_OR_BLANK_PARAMETER_ERROR_MESSAGE);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	private void checkPreferredProviders(final List<PreferredProviderDataDTO> providers, final boolean checkCloudInfo) {
		if (providers != null) {
			for (final PreferredProviderDataDTO provider : providers) {
				checkSystemRequestDTO(provider.getProviderSystem(), false);
				if (checkCloudInfo && provider.getProviderCloud() != null) {
					checkCloudRequestDTO(provider.getProviderCloud());
				}
			}
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	private void checkSystemRequestDTO(final SystemRequestDTO system, final boolean portRangeCheck) {
		logger.debug("checkSystemRequestDTO started...");
		
		if (system == null) {
			throw new InvalidParameterException("System" + NULL_PARAMETER_ERROR_MESSAGE);
		}
		
		if (Utilities.isEmpty(system.getSystemName())) {
			throw new InvalidParameterException("System name" + NULL_OR_BLANK_PARAMETER_ERROR_MESSAGE);
		}
		
		if (Utilities.isEmpty(system.getAddress())) {
			throw new InvalidParameterException("System address" + NULL_OR_BLANK_PARAMETER_ERROR_MESSAGE);
		}
		
		if (system.getPort() == null) {
			throw new InvalidParameterException("System port" + NULL_PARAMETER_ERROR_MESSAGE);
		}
		
		final int validatedPort = system.getPort().intValue();
		if (portRangeCheck && (validatedPort < CommonConstants.SYSTEM_PORT_RANGE_MIN || validatedPort > CommonConstants.SYSTEM_PORT_RANGE_MAX)) {
			throw new InvalidParameterException("System port must be between " + CommonConstants.SYSTEM_PORT_RANGE_MIN + " and " + CommonConstants.SYSTEM_PORT_RANGE_MAX + ".");
		}
	}

	//-------------------------------------------------------------------------------------------------
	private void checkCloudRequestDTO(final CloudRequestDTO cloud) {
		logger.debug("checkCloudRequestDTO started...");
		
		if (cloud == null) {
			throw new InvalidParameterException("Cloud" + NULL_PARAMETER_ERROR_MESSAGE);
		}
		
		if (Utilities.isEmpty(cloud.getOperator())) {
			throw new InvalidParameterException("Cloud operator" + NULL_OR_BLANK_PARAMETER_ERROR_MESSAGE);
		}
		
		if (Utilities.isEmpty(cloud.getName())) {
			throw new InvalidParameterException("Cloud name" + NULL_OR_BLANK_PARAMETER_ERROR_MESSAGE);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	private List<ServiceRegistryResponseDTO> removeNonPreferred(final List<ServiceRegistryResponseDTO> srList, final List<PreferredProviderDataDTO> preferredProviders) {
		logger.debug("removeNonPreferred started...");
		
		final List<ServiceRegistryResponseDTO> result = new ArrayList<>();
		for (final ServiceRegistryResponseDTO srResult : srList) {
			for (final PreferredProviderDataDTO preferredProvider : preferredProviders) {
				if (DTOUtilities.equalsSystemInResponseAndRequest(srResult.getProvider(), preferredProvider.getProviderSystem())) {
					result.add(srResult);
				}
			}
		}
		
		logger.debug("removeNonPreferred returns with {} entries.", result.size());
		
		return result;
	}
	
	
	//-------------------------------------------------------------------------------------------------
	private OrchestrationResponseDTO compileOrchestrationResponse(final List<ServiceRegistryResponseDTO> srList, final OrchestrationFormRequestDTO request) {
		logger.debug("compileOrchestrationResponse started...");
		
		List<OrchestrationResultDTO> orList = new ArrayList<>(srList.size());
		for (final ServiceRegistryResponseDTO entry : srList) {
			final OrchestrationResultDTO result = new OrchestrationResultDTO(entry.getProvider(), entry.getServiceDefinition(), entry.getServiceUri(), entry.getSecure(), entry.getMetadata(), 
																			 entry.getInterfaces(), entry.getVersion());
			
			if (request.getOrchestrationFlags().get(Flag.OVERRIDE_STORE)) {
				final List<OrchestratorWarnings> warnings = calculateOrchestratorWarnings(entry);
				result.setWarnings(warnings);
			}
			orList.add(result);
		}
		
	    // Generate the authorization tokens if it is requested based on the service security (modifies the orList)
	    orList = orchestratorDriver.generateAuthTokens(request, orList);
		
	    logger.debug("compileOrchestrationResponse creates {} orchestration forms", orList.size());

		return new OrchestrationResponseDTO(orList);
	}

	//-------------------------------------------------------------------------------------------------
	private List<OrchestratorWarnings> calculateOrchestratorWarnings(final ServiceRegistryResponseDTO entry) {
		logger.debug("calculateOrchestratorWarnings started...");
		final ZonedDateTime now = ZonedDateTime.now();
		
		final List<OrchestratorWarnings> result = new ArrayList<>(2);
		if (Utilities.isEmpty(entry.getEndOfValidity())) {
			result.add(OrchestratorWarnings.TTL_UNKNOWN);
		} else {
			final ZonedDateTime endOfValidity = Utilities.parseUTCStringToLocalZonedDateTime(entry.getEndOfValidity());
			if (endOfValidity.isBefore(now)) {
				result.add(OrchestratorWarnings.TTL_EXPIRED);
			} else if (endOfValidity.plusMinutes(EXPIRING_TIME_IN_MINUTES).isBefore(now)) {
		        // EXPIRING_TIME_IN_MINUTES minutes is an arbitrarily chosen value for the Time To Live measure, which got its value when the SR was queried.
				// The provider presumably will stop offering this service in somewhat less than EXPIRING_TIME_IN_MINUTES minutes.
				result.add(OrchestratorWarnings.TTL_EXPIRING);
			}
		}
		
		return result;
	}
	
	//-------------------------------------------------------------------------------------------------
	private SystemResponseDTO validateSystemId(final long systemId) {
		logger.debug("validateSystemId started...");
		
		if (systemId < 1) {
			throw new InvalidParameterException("SystemId " + LESS_THAN_ONE_ERROR_MESSAGE);
		}
		
		return orchestratorDriver.queryServiceRegistryBySystemId(systemId);
	}
	
	//-------------------------------------------------------------------------------------------------
	private long validateSystemRequestDTO(final SystemRequestDTO consumerSystemRequestDTO) {
		logger.debug("validateSystemId started...");
		
		if (consumerSystemRequestDTO == null) {
			throw new InvalidParameterException("SystemRequestDTO " + NULL_PARAMETER_ERROR_MESSAGE);
		}
		
		final SystemResponseDTO systemResponseDTO = orchestratorDriver.queryServiceRegistryBySystemRequestDTO(consumerSystemRequestDTO);
		if (systemResponseDTO == null) {
			throw new InvalidParameterException("SystemResponseDTO " + NULL_PARAMETER_ERROR_MESSAGE);
		}
		
		return systemResponseDTO.getId();
	}
	
	//-------------------------------------------------------------------------------------------------
	private List<OrchestratorStore> getOrchestrationStoreEntries(final SystemRequestDTO requesterSystem, final ServiceQueryFormDTO requestedService) {
		logger.debug("getOrchestrationStoreEntries started...");
		
		if (requesterSystem == null) {
			throw new InvalidParameterException("ConsumerSystem " + NULL_PARAMETER_ERROR_MESSAGE);
		}
		
		if (Utilities.isEmpty(requestedService.getServiceDefinitionRequirement())) {
			throw new InvalidParameterException("ServiceDefinitionRequirement " + NULL_OR_BLANK_PARAMETER_ERROR_MESSAGE);
		}
		
		final String serviceDefinitionName = requestedService.getServiceDefinitionRequirement().trim().toLowerCase();
		
		if (requestedService.getInterfaceRequirements() == null || requestedService.getInterfaceRequirements().isEmpty() || Utilities.isEmpty(requestedService.getInterfaceRequirements().get(0))) {
			throw new InvalidParameterException("InterfaceRequirement " + NULL_OR_BLANK_PARAMETER_ERROR_MESSAGE);
		}
		
		if (requestedService.getInterfaceRequirements().size() != 1) {
			throw new InvalidParameterException("InterfaceRequirement " + MORE_THAN_ONE_ERROR_MESSAGE);
		}
		
		final String serviceInterfaceName =  requestedService.getInterfaceRequirements().get(0).trim();
		
		final long consumerSystemId = validateSystemRequestDTO(requesterSystem);
		
		return orchestratorStoreDBService.getOrchestratorStoresByConsumerIdAndServiceDefinitionAndServiceInterface(consumerSystemId, serviceDefinitionName, serviceInterfaceName);
	}
	
	//-------------------------------------------------------------------------------------------------
	private List<ServiceRegistryResponseDTO> crossCheckTopPriorityEntries(final List<OrchestratorStore> entryList, final OrchestrationFormRequestDTO orchestrationFormRequestDTO,
																		  final SystemRequestDTO consumerSystem) {
		logger.debug("crossCheckTopPriorityEntries started...");
	    
		logger.debug("currently only local entries are allowed to be returned by topPriorityOrchestration");
	    final List<OrchestratorStore> onlyLocalEntryList = filterEntryListByForeign(entryList);
	    
	    if (onlyLocalEntryList.isEmpty()) {
	    	return List.of();
		}
	    
	    final Map<Long,String> serviceDefinitionsIdsMap = mapServiceDefinitionsToServiceDefinitionIds(onlyLocalEntryList); 
	    final Map<Long,List<String>> serviceDefinitionIdInterfaceMap = mapInterfacesToServiceDefinitions(onlyLocalEntryList);
	    final Map<Long,Set<String>> providerIdInterfaceMap = mapInterfacesToProviders(onlyLocalEntryList);
	    
	    final OrchestrationFlags flags = orchestrationFormRequestDTO.getOrchestrationFlags();	   
	    final List<ServiceRegistryResponseDTO> filteredServiceQueryResultDTOList = new ArrayList<>();
	    for (final Entry<Long, String> entry : serviceDefinitionsIdsMap.entrySet()) {
	    	final ServiceQueryFormDTO serviceQueryFormDTO = new ServiceQueryFormDTO();
	    	serviceQueryFormDTO.setServiceDefinitionRequirement(entry.getValue());
		    final List<String> interfaceRequirements = serviceDefinitionIdInterfaceMap.get(entry.getKey());
		    serviceQueryFormDTO.setInterfaceRequirements(interfaceRequirements);
		    
		    final ServiceQueryResultDTO queryResult = orchestratorDriver.queryServiceRegistry(serviceQueryFormDTO, flags.get(Flag.METADATA_SEARCH), flags.get(Flag.PING_PROVIDERS));
		    final List<ServiceRegistryResponseDTO> filteredQueryResultByInterfaces = filterQueryResultByInterfaces(providerIdInterfaceMap, queryResult);	
		    final List<ServiceRegistryResponseDTO> filteredQueryResultByAuthorization = orchestratorDriver.queryAuthorization(consumerSystem, filteredQueryResultByInterfaces);	
		    
		    filteredServiceQueryResultDTOList.addAll(filteredQueryResultByAuthorization);
	    }
	    
		return filteredServiceQueryResultDTOList;
	}
	
	//-------------------------------------------------------------------------------------------------
	private List<OrchestratorStore> filterEntryListByForeign(final List<OrchestratorStore> entryList) {
		logger.debug(" filterEntryListByForeign started...");
		
		if (entryList == null || entryList.isEmpty()) {
			return List.of();
		}
		
		return entryList.stream().filter(e -> !e.isForeign()).collect(Collectors.toList()); 
	}

	//-------------------------------------------------------------------------------------------------
	private List<ServiceRegistryResponseDTO> getAuthorizedServiceRegistryEntries(final List<OrchestratorStore> entryList, final OrchestrationFormRequestDTO orchestrationFormRequestDTO) {
		logger.debug("crossCheckStoreEntries started...");
		
	    final List<OrchestratorStore> onlyLocalEntryList = filterEntryListByForeign(entryList);    
	    if (onlyLocalEntryList.isEmpty()) {
	    	return List.of();
		}
		
    	final ServiceQueryFormDTO serviceQueryFormDTO = orchestrationFormRequestDTO.getRequestedService();
    	serviceQueryFormDTO.setInterfaceRequirements(List.of(orchestrationFormRequestDTO.getRequestedService().getInterfaceRequirements().get(0)));
    	final OrchestrationFlags flags = orchestrationFormRequestDTO.getOrchestrationFlags();
		final ServiceQueryResultDTO serviceQueryResultDTO = orchestratorDriver.queryServiceRegistry(serviceQueryFormDTO,  flags.get(Flag.METADATA_SEARCH), flags.get(Flag.PING_PROVIDERS)); 
		
		return orchestratorDriver.queryAuthorization(orchestrationFormRequestDTO.getRequesterSystem(), serviceQueryResultDTO.getServiceQueryData());
	}
	
	//-------------------------------------------------------------------------------------------------
	private Map<Long,List<String>> mapInterfacesToServiceDefinitions(final List<OrchestratorStore> entryList) {
		logger.debug("mapInterfacesToServiceDefinitions started...");
		
		final Map<Long,List<String>> serviceDefinitionsInterfacesMap = new HashMap<>();
		for (final OrchestratorStore orchestratorStore : entryList) {
			final Long serviceDefinitionId = orchestratorStore.getServiceDefinition().getId();
			final String interfaceName = orchestratorStore.getServiceInterface().getInterfaceName();
			
			serviceDefinitionsInterfacesMap.putIfAbsent(serviceDefinitionId, new ArrayList<>());
			if (!serviceDefinitionsInterfacesMap.get(serviceDefinitionId).contains(interfaceName)) {
				serviceDefinitionsInterfacesMap.get(serviceDefinitionId).add(interfaceName);
			}
		}
		
		return serviceDefinitionsInterfacesMap;
	}
	
	//-------------------------------------------------------------------------------------------------
	private Map<Long,Set<String>> mapInterfacesToProviders(final List<OrchestratorStore> entryList) {
		logger.debug("mapInterfacesToProviders started...");
		
		final Map<Long,Set<String>> providersInterfacesMap = new HashMap<>();
		
		for (final OrchestratorStore orchestratorStore : entryList) {
			Assert.isTrue(!orchestratorStore.isForeign(), "Provider is foreign");
			
			final Long providerSystemId = orchestratorStore.getProviderSystemId();
			providersInterfacesMap.putIfAbsent(providerSystemId, new HashSet<>());
			providersInterfacesMap.get(providerSystemId).add(orchestratorStore.getServiceInterface().getInterfaceName());
		}
		
		return providersInterfacesMap;
	}

	//-------------------------------------------------------------------------------------------------
	private Map<Long,String> mapServiceDefinitionsToServiceDefinitionIds(final List<OrchestratorStore> entryList) {
		logger.debug("mapServiceDefinitionsToServiceDefinitionIds started...");
		
		final Map<Long,String> serviceDefinitionsIdToStringMap = new HashMap<>();
		for (final OrchestratorStore orchestratorStore : entryList) {
			final ServiceDefinition serviceDefinition = orchestratorStore.getServiceDefinition();
			if (!serviceDefinitionsIdToStringMap.containsKey(serviceDefinition.getId())) {
				serviceDefinitionsIdToStringMap.put(serviceDefinition.getId(), serviceDefinition.getServiceDefinition());
			}
		}
		
		return serviceDefinitionsIdToStringMap;
	}

	//-------------------------------------------------------------------------------------------------
	private List<ServiceRegistryResponseDTO> filterQueryResultByInterfaces(final Map<Long, Set<String>> providerIdInterfaceIdsMap, final ServiceQueryResultDTO queryResult) {
		logger.debug("filterQueryResultByInterfaces started...");
		
		final List<ServiceRegistryResponseDTO> filteredResults = new ArrayList<>();
		
		final List<ServiceRegistryResponseDTO> result = queryResult.getServiceQueryData();
		for (final ServiceRegistryResponseDTO serviceRegistryResponseDTO : result) {
			final Long providerIdFromResult = serviceRegistryResponseDTO.getProvider().getId();
			final List<ServiceInterfaceResponseDTO> interfaceListFromResult = serviceRegistryResponseDTO.getInterfaces();
			final List<ServiceInterfaceResponseDTO> filteredInterfaceList = new ArrayList<>();
			
			if (providerIdInterfaceIdsMap.containsKey(providerIdFromResult)) {
				final Set<String> interfaceSetFromRequest = providerIdInterfaceIdsMap.get(providerIdFromResult);
				for (final ServiceInterfaceResponseDTO interfaceResponseDTO : interfaceListFromResult) {
					if (interfaceSetFromRequest.contains(interfaceResponseDTO.getInterfaceName())) {
						filteredInterfaceList.add(interfaceResponseDTO);
					}
				} 
			}
			
			if (!filteredInterfaceList.isEmpty()) {
				serviceRegistryResponseDTO.setInterfaces(filteredInterfaceList);
				filteredResults.add(serviceRegistryResponseDTO);
			}
		}
		
		return filteredResults;
	}
	

	//-------------------------------------------------------------------------------------------------
	private OrchestrationResponseDTO getHighestPriorityCurrentlyWorkingStoreEntryFromEntryList(final OrchestrationFormRequestDTO request, final List<OrchestratorStore> entryList,
																							   final List<ServiceRegistryResponseDTO> authorizedLocalServiceRegistryEntries) {
		logger.debug("getHighestPriorityCurrentlyWorkingStoreEntryFromEntryList started...");
		
        for (final OrchestratorStore orchestratorStore : entryList) {
        	if (!orchestratorStore.isForeign()) {
				final OrchestrationResponseDTO orchestrationResponseDTO = crossCheckLocalStoreEntry(orchestratorStore, request, authorizedLocalServiceRegistryEntries);
				if (orchestrationResponseDTO != null && !orchestrationResponseDTO.getResponse().isEmpty()) {
            		return orchestrationResponseDTO;
				}
        	
			}				
		}
        
        return new OrchestrationResponseDTO(); // empty response
	}

	//-------------------------------------------------------------------------------------------------
	private OrchestrationResponseDTO crossCheckLocalStoreEntry(final OrchestratorStore orchestratorStore, final OrchestrationFormRequestDTO request, 
															   final List<ServiceRegistryResponseDTO> authorizedLocalServiceRegistryEntries) {
		logger.debug("crossCheckLocalStoreEntry started ...");
		
		final Long providerSystemId = orchestratorStore.getProviderSystemId();
		for (final ServiceRegistryResponseDTO serviceRegistryResponseDTO : authorizedLocalServiceRegistryEntries) {
			if (serviceRegistryResponseDTO.getProvider().getId() == providerSystemId) {
				return compileOrchestrationResponse(List.of(serviceRegistryResponseDTO), request);							
			}
		}
		
		return new OrchestrationResponseDTO(); // empty response
	}




}