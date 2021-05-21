package eu.arrowhead.common.database.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.EntityGraph.EntityGraphType;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import eu.arrowhead.common.database.entity.Relay;
import eu.arrowhead.common.dto.internal.RelayType;

@Repository
public interface RelayRepository extends RefreshableRepository<Relay,Long> {
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public boolean existsByAddressAndPort(final String address, final int port);
	public Optional<Relay> findByAddressAndPort(final String address, final int port);
	public List<Relay> findAllByExclusiveAndTypeIn(final boolean exclusive, final List<RelayType> type);
	
	//-------------------------------------------------------------------------------------------------
	@EntityGraph(value = "relayWithCloudGatekeeperRelayEntries" , type = EntityGraphType.FETCH)
	@Query("SELECT r FROM Relay r WHERE id = ?1")
	public Optional<Relay> getByIdWithCloudGatekeepers(final long id);
}