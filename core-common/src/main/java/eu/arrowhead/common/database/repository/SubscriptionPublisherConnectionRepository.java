package eu.arrowhead.common.database.repository;

import java.util.List;
import java.util.Set;

import eu.arrowhead.common.database.entity.Subscription;
import eu.arrowhead.common.database.entity.SubscriptionPublisherConnection;
import eu.arrowhead.common.database.entity.System;

public interface SubscriptionPublisherConnectionRepository
		extends RefreshableRepository<SubscriptionPublisherConnection, Long> {

	public List<SubscriptionPublisherConnection> findAllBySystemAndAuthorized(final System providerSystem, final boolean authorized);
	public Set<SubscriptionPublisherConnection> findBySubscriptionEntry(final Subscription subscriptionEntry);

}
