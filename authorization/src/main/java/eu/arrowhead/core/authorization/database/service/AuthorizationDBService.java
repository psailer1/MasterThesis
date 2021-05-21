package eu.arrowhead.core.authorization.database.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.database.entity.AuthorizationIntraCloud;
import eu.arrowhead.common.database.entity.AuthorizationIntraCloudInterfaceConnection;
import eu.arrowhead.common.database.entity.ServiceDefinition;
import eu.arrowhead.common.database.entity.ServiceInterface;
import eu.arrowhead.common.database.entity.System;
import eu.arrowhead.common.database.repository.AuthorizationIntraCloudInterfaceConnectionRepository;
import eu.arrowhead.common.database.repository.AuthorizationIntraCloudRepository;
import eu.arrowhead.common.database.repository.ServiceDefinitionRepository;
import eu.arrowhead.common.database.repository.ServiceInterfaceRepository;
import eu.arrowhead.common.database.repository.SystemRepository;
import eu.arrowhead.common.dto.internal.AuthorizationIntraCloudCheckResponseDTO;
import eu.arrowhead.common.dto.internal.AuthorizationIntraCloudListResponseDTO;
import eu.arrowhead.common.dto.internal.AuthorizationIntraCloudResponseDTO;
import eu.arrowhead.common.dto.internal.AuthorizationSubscriptionCheckResponseDTO;
import eu.arrowhead.common.dto.internal.DTOConverter;
import eu.arrowhead.common.dto.internal.DTOUtilities;
import eu.arrowhead.common.dto.internal.IdIdListDTO;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;
import eu.arrowhead.common.dto.shared.SystemResponseDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.core.authorization.service.AuthorizationDriver;
import eu.arrowhead.core.authorization.service.PublishAuthUpdateTask;

@Service
public class AuthorizationDBService {
	
	//=================================================================================================
	// members
	
	@Autowired
	private AuthorizationIntraCloudRepository authorizationIntraCloudRepository;

	@Autowired
	private SystemRepository systemRepository;
	
	@Autowired
	private ServiceDefinitionRepository serviceDefinitionRepository;
	
	@Autowired
	private ServiceInterfaceRepository serviceInterfaceRepository;
	
	@Autowired
	private AuthorizationIntraCloudInterfaceConnectionRepository authorizationIntraCloudInterfaceConnectionRepository;

	@Autowired
	private AuthorizationDriver authorizationDriver;
	
	@Value(CoreCommonConstants.$AUTHORIZATION_IS_EVENTHANDLER_PRESENT_WD)
	private boolean eventhandlerIsPresent;
	
	private final Logger logger = LogManager.getLogger(AuthorizationDBService.class);

	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public Page<AuthorizationIntraCloud> getAuthorizationIntraCloudEntries(final int page, final int size, final Direction direction, final String sortField) {
		logger.debug("getAuthorizationIntraCloudEntries started...");
		
		final int validatedPage = page < 0 ? 0 : page;
		final int validatedSize = size <= 0 ? Integer.MAX_VALUE : size; 		
		final Direction validatedDirection = direction == null ? Direction.ASC : direction;
		final String validatedSortField = Utilities.isEmpty(sortField) ? CoreCommonConstants.COMMON_FIELD_NAME_ID : sortField.trim();
		
		if (!AuthorizationIntraCloud.SORTABLE_FIELDS_BY.contains(validatedSortField)) {
			throw new InvalidParameterException("Sortable field with reference '" + validatedSortField + "' is not available");
		}
		
		try {
			return authorizationIntraCloudRepository.findAll(PageRequest.of(validatedPage, validatedSize, validatedDirection, validatedSortField));
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	public AuthorizationIntraCloudListResponseDTO getAuthorizationIntraCloudEntriesResponse(final int page, final int size, final Direction direction, final String sortField) {
		logger.debug("getAuthorizationIntraCloudEntriesResponse started...");
		
		final Page<AuthorizationIntraCloud> authorizationIntraCloudEntries = getAuthorizationIntraCloudEntries(page, size, direction, sortField);
		
		return DTOConverter.convertAuthorizationIntraCloudListToAuthorizationIntraCloudListResponseDTO(authorizationIntraCloudEntries);
	}
	
	//-------------------------------------------------------------------------------------------------
	public AuthorizationIntraCloud getAuthorizationIntraCloudEntryById(final long id) {
		logger.debug("getAuthorizationIntraCloudEntryById started...");
		
		try {
			final Optional<AuthorizationIntraCloud> find = authorizationIntraCloudRepository.findById(id);
			if (find.isPresent()) {
				return find.get();
			} else {
				throw new InvalidParameterException("AuthorizationIntraCloud with id of '" + id + "' not exists");
			}
		} catch (final InvalidParameterException ex) {
			throw ex;
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	public AuthorizationIntraCloudResponseDTO getAuthorizationIntraCloudEntryByIdResponse(final long id) {
		logger.debug("getAuthorizationIntraCloudEntryByIdResponse started...");		
		
		final AuthorizationIntraCloud authorizationIntraCloudEntry = getAuthorizationIntraCloudEntryById(id);
		
		return DTOConverter.convertAuthorizationIntraCloudToAuthorizationIntraCloudResponseDTO(authorizationIntraCloudEntry);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Transactional(rollbackFor = ArrowheadException.class)
	public void removeAuthorizationIntraCloudEntryById(final long id) {
		logger.debug("removeAuthorizationIntraCloudEntryById started...");
		
		try {
			if (!authorizationIntraCloudRepository.existsById(id)) {
				throw new InvalidParameterException("AuthorizationIntraCloud with id of '" + id + "' not exists");
			}
			
			if ( eventhandlerIsPresent)  {
				final Optional<AuthorizationIntraCloud> authOptional = authorizationIntraCloudRepository.findById( id );
				if ( authOptional.isPresent() ) {
					
					final PublishAuthUpdateTask publishAuthUpdateTask = new PublishAuthUpdateTask(authorizationDriver, authOptional.get().getConsumerSystem().getId());
					final Thread publishingThread = new Thread(publishAuthUpdateTask);
					publishingThread.start();

				}
			}
			
			authorizationIntraCloudRepository.deleteById(id);
			authorizationIntraCloudRepository.flush();
		} catch (final InvalidParameterException ex) {
			throw ex;
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Transactional(rollbackFor = ArrowheadException.class)
	public List<AuthorizationIntraCloud> createBulkAuthorizationIntraCloud(final long consumerId, final Set<Long> providerIds, final Set<Long> serviceDefinitionIds, final Set<Long> interfaceIds) {
		logger.debug("createBulkAuthorizationIntraCloud started...");
		
		if (consumerId < 1) {
			throw new InvalidParameterException("Consumer id can't be null and must be greater than 0.");
		}
		
		if (providerIds == null || providerIds.isEmpty()) {
			throw new InvalidParameterException("providerIds list is empty");
		}
		
		if (serviceDefinitionIds == null || serviceDefinitionIds.isEmpty()) {
			throw new InvalidParameterException("serviceDefinitionIds list is empty");
		}
		
		if (interfaceIds == null || interfaceIds.isEmpty()) {
			throw new InvalidParameterException("interfaceIds list is empty");
		}
		
		if (providerIds.size() > 1 && serviceDefinitionIds.size() > 1) {
			throw new InvalidParameterException("providerIds list or serviceDefinitionIds list should contain only one element, but both contain more");
		}
		
		if (serviceDefinitionIds.size() > 1 && interfaceIds.size() > 1) {
			throw new InvalidParameterException("serviceDefinitionIds list or interfaceIds list should contain only one element, but both contain more");
		}
		
		for (final Long id : providerIds) {
			if (id == null || id < 1) {
				throw new InvalidParameterException("Provider id can't be null and must be greater than 0.");
			}
		}
		
		for (final Long id : serviceDefinitionIds) {
			if (id == null || id < 1) {
				throw new InvalidParameterException("ServiceDefinition id can't be null and must be greater than 0.");
			}
		}
		
		for (final Long id : interfaceIds) {
			if (id == null || id < 1) {
				throw new InvalidParameterException("ServiceInterface id can't be null and must be greater than 0.");
			}
		}
		
		try {
			final Optional<System> consumerOpt = systemRepository.findById(consumerId);
			System consumer;
			if (consumerOpt.isPresent()) {
				consumer = consumerOpt.get();
			} else {
				throw new InvalidParameterException("Consumer system with id of " + consumerId + " not exists");
			}
			
			if (providerIds.size() <= serviceDefinitionIds.size() && serviceDefinitionIds.size() >= interfaceIds.size()) {
				// Case: One provider with more or one service and with one interface
				final Long providerId = providerIds.iterator().next();
				final Long interfaceId = interfaceIds.iterator().next();
				return createBulkAuthorizationIntraCloudWithOneProviderAndMoreServiceDefinitionAndOneInterface(consumer, providerId, serviceDefinitionIds, interfaceId);
			} else {
				// Case: One service with more or one provider and with more or one interface
				final Long serviceId = serviceDefinitionIds.iterator().next();
				return createBulkAuthorizationIntraCloudWithOneServiceDefinitionAndMoreProviderAndMoreInterface(consumer, providerIds, serviceId, interfaceIds);
			}
		} catch (final InvalidParameterException ex) {
			throw ex;
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}
	

	//-------------------------------------------------------------------------------------------------
	@Transactional(rollbackFor = ArrowheadException.class)
	public AuthorizationIntraCloudListResponseDTO createBulkAuthorizationIntraCloudResponse(final long consumerId, final Set<Long> providerIds, final Set<Long> serviceDefinitionIds,
																							final Set<Long> interfaceIds) {
		logger.debug("createBulkAuthorizationIntraCloudResponse started...");
		
		final List<AuthorizationIntraCloud> entries = createBulkAuthorizationIntraCloud(consumerId, providerIds, serviceDefinitionIds, interfaceIds);
		final Page<AuthorizationIntraCloud> entryPage = new PageImpl<>(entries);
		
		return DTOConverter.convertAuthorizationIntraCloudListToAuthorizationIntraCloudListResponseDTO(entryPage);
	}
	
	//-------------------------------------------------------------------------------------------------
	public AuthorizationIntraCloudCheckResponseDTO checkAuthorizationIntraCloudRequest(final String consumerName, final String consumerAddress, final int consumerPort, final long serviceDefinitionId,
																					   final Set<IdIdListDTO> providerIdsWithInterfaceIds) {
		logger.debug("checkAuthorizationIntraCloudRequest started...");
				
		try {
			final boolean isServiceDefinitionIdInvalid = serviceDefinitionId < 1 || !serviceDefinitionRepository.existsById(serviceDefinitionId);
			final boolean isProviderListEmpty = providerIdsWithInterfaceIds == null || providerIdsWithInterfaceIds.isEmpty();
			if (isServiceDefinitionIdInvalid || isProviderListEmpty) {
				String exceptionMsg = "Following parameters are invalid:";
				exceptionMsg = isServiceDefinitionIdInvalid ? exceptionMsg + " serviceDefinition id," : exceptionMsg;
				exceptionMsg = isProviderListEmpty ? exceptionMsg + " empty providerId list," : exceptionMsg;
				exceptionMsg = exceptionMsg.substring(0, exceptionMsg.length() - 1);
				
				throw new InvalidParameterException(exceptionMsg);
			}
			
			final System consumer = checkAndGetConsumer(consumerName, consumerAddress, consumerPort);
			final long consumerId = consumer.getId();
			
			final List<IdIdListDTO> authorizedProvidersWithInterfaces = new ArrayList<>(providerIdsWithInterfaceIds.size());
			for (final IdIdListDTO providerWithInterfaces : providerIdsWithInterfaceIds) {
				final Long providerId = providerWithInterfaces.getId();
				if (providerId == null || providerId < 1 || !systemRepository.existsById(providerId)) {
					logger.debug("Invalid provider id: {}", providerId);
				} else {
					final Optional<AuthorizationIntraCloud> authIntraOpt = authorizationIntraCloudRepository.findByConsumerIdAndProviderIdAndServiceDefinitionId(consumerId, providerId,
																																							 	 serviceDefinitionId);
					if (authIntraOpt.isPresent()) {
						final Set<AuthorizationIntraCloudInterfaceConnection> interfaceConnections = authIntraOpt.get().getInterfaceConnections();
						final List<Long> authorizedInterfaces = new ArrayList<>();
						for (final Long interfaceId : providerWithInterfaces.getIdList()) {
							for (final AuthorizationIntraCloudInterfaceConnection connection : interfaceConnections) {
								if (connection.getServiceInterface().getId() == interfaceId) {
									authorizedInterfaces.add(interfaceId);
								}
							}
						}
						
						if (!authorizedInterfaces.isEmpty()) {
							authorizedProvidersWithInterfaces.add(new IdIdListDTO(providerId, authorizedInterfaces));
						}
					}
				}
			}
			
			if (authorizedProvidersWithInterfaces.isEmpty()) {
				logger.debug("Have no any authorization intra cloud rule");
			}			
			
			return new AuthorizationIntraCloudCheckResponseDTO(DTOConverter.convertSystemToSystemResponseDTO(consumer), serviceDefinitionId, authorizedProvidersWithInterfaces);
		} catch (final InvalidParameterException ex) {
			throw ex;
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}
	
	
	
	//-------------------------------------------------------------------------------------------------
	public AuthorizationSubscriptionCheckResponseDTO checkAuthorizationSubscriptionRequest(final String consumerName,
			final String consumerAddress, final Integer consumerPort, final Set<SystemRequestDTO> publishers) {
		logger.debug("checkAuthorizationSubscriptionRequest started...");
		
		try {
			
			final System consumer = checkAndGetConsumer(consumerName, consumerAddress, consumerPort);		
			
			final Set<SystemResponseDTO> authorizedPublishers = getAuthorizedPublishers(consumer, publishers);
			
			return new AuthorizationSubscriptionCheckResponseDTO(DTOConverter.convertSystemToSystemResponseDTO(consumer), authorizedPublishers );
		} catch (final InvalidParameterException ex) {
			throw ex;
		} catch (final Exception ex) {
			logger.debug(ex.getMessage(), ex);
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		}
	}
	
	//=================================================================================================
	// assistant methods	

	//-------------------------------------------------------------------------------------------------
	private void checkConstraintsOfAuthorizationIntraCloudTable(final System consumer, final System provider, final ServiceDefinition serviceDefinition) {
		logger.debug("checkConstraintsOfAuthorizationIntraCloudTable started...");
		
		final Optional<AuthorizationIntraCloud> optional = authorizationIntraCloudRepository.findByConsumerSystemAndProviderSystemAndServiceDefinition(consumer, provider, serviceDefinition);
		if (optional.isPresent()) {
			throw new InvalidParameterException("AuthorizationIntraCloud entry with consumer id " +  consumer.getId() + ", provider id " + provider.getId() + " and serviceDefinitionId " +
												serviceDefinition.getId() + " already exists");
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	private List<AuthorizationIntraCloud> createBulkAuthorizationIntraCloudWithOneProviderAndMoreServiceDefinitionAndOneInterface(final System consumer, final Long providerId,
																												   				  final Set<Long> serviceDefinitionIds, final Long interfaceId) {
		logger.debug("createBulkAuthorizationIntraCloudWithOneProviderAndMoreServiceDefinitionAndOneInterface started...");
		
		final Optional<ServiceInterface> interfaceOpt = serviceInterfaceRepository.findById(interfaceId);
		if (interfaceOpt.isEmpty()) {
			throw new InvalidParameterException("ServiceInterface with id of " + interfaceId + " not exists");
		}
		final ServiceInterface serviceInterface = interfaceOpt.get();
		
		final Optional<System> providerOpt = systemRepository.findById(providerId);		
		if (providerOpt.isPresent()) {
			final System provider = providerOpt.get();

			final List<AuthorizationIntraCloud> toBeSaved = new ArrayList<>(serviceDefinitionIds.size());
			for (final Long serviceId : serviceDefinitionIds) {
				final Optional<ServiceDefinition> serviceOpt = serviceDefinitionRepository.findById(serviceId);
				if (serviceOpt.isPresent()) {
					final ServiceDefinition service = serviceOpt.get();
					try {
						checkConstraintsOfAuthorizationIntraCloudTable(consumer, provider, service);
						toBeSaved.add(new AuthorizationIntraCloud(consumer, provider, service));
					} catch (final InvalidParameterException ex) {
						//not throwing towards as in this bulk operation case should be only a warning
						logger.debug(ex.getMessage());
					}
				} else {
					throw new InvalidParameterException("ServiceDefinition with id of " + serviceId + " not exists");
				}
			}
			
			final List<AuthorizationIntraCloud> authIntraEntries = authorizationIntraCloudRepository.saveAll(toBeSaved);
			for (final AuthorizationIntraCloud authIntraEntry : authIntraEntries) {
				final AuthorizationIntraCloudInterfaceConnection connection = authorizationIntraCloudInterfaceConnectionRepository.save(new AuthorizationIntraCloudInterfaceConnection(authIntraEntry,
																																													   serviceInterface));
				authIntraEntry.getInterfaceConnections().add(connection);
			}
			authorizationIntraCloudInterfaceConnectionRepository.flush();

			final List<AuthorizationIntraCloud> savedAuthIntraEntries = authorizationIntraCloudRepository.saveAll(authIntraEntries);
			authorizationIntraCloudRepository.flush();
			
			if ( eventhandlerIsPresent)  {
				
				final PublishAuthUpdateTask publishAuthUpdateTask = new PublishAuthUpdateTask(authorizationDriver, consumer.getId());
				final Thread publishingThread = new Thread(publishAuthUpdateTask);
				publishingThread.start();			

			}
			
			return savedAuthIntraEntries;
		} else {
			throw new InvalidParameterException("Provider system with id of " + providerId + " not exists");
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	private List<AuthorizationIntraCloud> createBulkAuthorizationIntraCloudWithOneServiceDefinitionAndMoreProviderAndMoreInterface(final System consumer, final Set<Long> providerIds,
																																   final Long serviceId, final Set<Long> interfaceIds) {
		logger.debug("createBulkAuthorizationIntraCloudWithOneServiceDefinitionAndMoreProviderAndMoreInterface started...");

		final ServiceDefinition service = getServiceDefinitionById(serviceId);
		final List<ServiceInterface> interfaces = getServiceInterfacesById(interfaceIds);

		final List<AuthorizationIntraCloud> toBeSaved = new ArrayList<>(providerIds.size());
		for (final Long providerId : providerIds) {
			final Optional<System> providerOpt = systemRepository.findById(providerId);
			if (providerOpt.isPresent()) {
				final System provider = providerOpt.get();
				try {
					checkConstraintsOfAuthorizationIntraCloudTable(consumer, provider, service);
					toBeSaved.add(new AuthorizationIntraCloud(consumer, provider, service));
				} catch (final InvalidParameterException ex) {
					// not throwing towards as in this bulk operation case should be only a warning
					logger.debug(ex.getMessage());
				}
			} else {
				throw new InvalidParameterException("Provider system with id of " + providerId + " not exists");
			}
		}
		
		final List<AuthorizationIntraCloud> authIntraEntries = authorizationIntraCloudRepository.saveAll(toBeSaved);
		for (final AuthorizationIntraCloud authIntraEntry : authIntraEntries) {
			for (final ServiceInterface serviceInterface : interfaces) {
				final AuthorizationIntraCloudInterfaceConnection connection = authorizationIntraCloudInterfaceConnectionRepository.save(new AuthorizationIntraCloudInterfaceConnection(authIntraEntry,
																																													   serviceInterface));
				authIntraEntry.getInterfaceConnections().add(connection);
			}
		}
		authorizationIntraCloudInterfaceConnectionRepository.flush();
		
		final List<AuthorizationIntraCloud> savedAuthIntraEntries = authorizationIntraCloudRepository.saveAll(authIntraEntries);
		authorizationIntraCloudRepository.flush();
		
		if ( eventhandlerIsPresent)  {
			
			final PublishAuthUpdateTask publishAuthUpdateTask = new PublishAuthUpdateTask(authorizationDriver, consumer.getId());
			final Thread publishingThread = new Thread(publishAuthUpdateTask);
			publishingThread.start();
			
		}
		
		return savedAuthIntraEntries;
	}
	
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("squid:S3655")
	private ServiceDefinition getServiceDefinitionById(final long serviceId) {
		final Optional<ServiceDefinition> serviceOpt = serviceDefinitionRepository.findById(serviceId);
		if (serviceOpt.isEmpty()) {
			throw new InvalidParameterException("ServiceDefinition with id of " + serviceId + " not exists");
		}
		
		return serviceOpt.get();
	}
	
	//-------------------------------------------------------------------------------------------------
	private List<ServiceInterface> getServiceInterfacesById(final Set<Long> interfaceIds) {
		final List<ServiceInterface> interfaces = new ArrayList<>(interfaceIds.size());
		for (final Long id : interfaceIds) {
			final Optional<ServiceInterface> interfaceOpt = serviceInterfaceRepository.findById(id);
			if (interfaceOpt.isPresent()) {
				interfaces.add(interfaceOpt.get());
			} else {
				logger.debug("ServiceInterface with id of '{}' not exists", id);
			}				
		}
		
		if (interfaces.isEmpty()) {
			throw new InvalidParameterException("interfaceId list doesn't contain any existing ServiceInterface");
		}
		
		return interfaces;
	}
	
	//-------------------------------------------------------------------------------------------------
	private System checkAndGetConsumer(final String name, final String address, final int port) {
		logger.debug("checkSystemRequest started...");
		
		if (Utilities.isEmpty(name)) {
			throw new InvalidParameterException("Consumer name is null or blank");
		}
		
		if (Utilities.isEmpty(address)) {
			throw new InvalidParameterException("Consumer address is null or blank");
		}
		
		if (port < CommonConstants.SYSTEM_PORT_RANGE_MIN || port > CommonConstants.SYSTEM_PORT_RANGE_MAX) {
			throw new InvalidParameterException("Consumer port must be between " + CommonConstants.SYSTEM_PORT_RANGE_MIN + " and " + CommonConstants.SYSTEM_PORT_RANGE_MAX + ".");
		}
		
		final String validatedName = name.toLowerCase().trim();
		final String validatedAddress = address.toLowerCase().trim();
		
		final Optional<System> optConsumer = systemRepository.findBySystemNameAndAddressAndPort(validatedName, validatedAddress, port);
		if (optConsumer.isPresent()) {
			return optConsumer.get();
		} else {
			throw new InvalidParameterException("Consumer with name: " + name + ", address: " + address + " and port: " + port + " is not exist in the database.");
		}
	}

	//-------------------------------------------------------------------------------------------------
	private Set<SystemResponseDTO> getAuthorizedPublishers(final System consumer, final Set<SystemRequestDTO> publishers) {
		logger.debug("getAuthorizedProviders started...");
		
		final List<AuthorizationIntraCloud> authorizationIntraCloudList = authorizationIntraCloudRepository.findAllByConsumerSystem(consumer);
		
		final Set<SystemResponseDTO> authorizedPublishers = new HashSet<>(authorizationIntraCloudList.size());
		
		if ( authorizationIntraCloudList.isEmpty() ) {
			
			return authorizedPublishers; 
		}		
				
		for (final AuthorizationIntraCloud authorizationIntraCloud : authorizationIntraCloudList) {
			final SystemResponseDTO authorizedPublisher = DTOConverter.convertSystemToSystemResponseDTO(authorizationIntraCloud.getProviderSystem());
			
			if (publishers != null && !publishers.isEmpty()) {
				
				for (final SystemRequestDTO systemRequestDTO : publishers) {
					
					if (DTOUtilities.equalsSystemInResponseAndRequest(authorizedPublisher, systemRequestDTO)) {
						
						if (!authorizedPublishers.contains(authorizedPublisher)) {
							
							authorizedPublishers.add(authorizedPublisher);
							break;
						}
					}
				}
			}else {

				if (!authorizedPublishers.contains(authorizedPublisher)) {
					
					authorizedPublishers.add(authorizedPublisher);
				}
				
			}
		}
		
		return authorizedPublishers;
	}
}