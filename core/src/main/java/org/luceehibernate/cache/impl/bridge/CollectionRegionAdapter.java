/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * Copyright (c) 2008, Red Hat Middleware LLC or third-party contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors.  All third-party contributions are
 * distributed under license by Red Hat Middleware LLC.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 *
 */
package org.luceehibernate.cache.impl.bridge;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.luceehibernate.cache.CollectionRegion;
import org.luceehibernate.cache.Cache;
import org.luceehibernate.cache.CacheDataDescription;
import org.luceehibernate.cache.OptimisticCache;
import org.luceehibernate.cache.CacheException;
import org.luceehibernate.cache.CacheConcurrencyStrategy;
import org.luceehibernate.cache.TransactionalCache;
import org.luceehibernate.cache.ReadWriteCache;
import org.luceehibernate.cache.NonstrictReadWriteCache;
import org.luceehibernate.cache.ReadOnlyCache;
import org.luceehibernate.cache.access.CollectionRegionAccessStrategy;
import org.luceehibernate.cache.access.AccessType;
import org.luceehibernate.cfg.Settings;

/**
 * Adapter specifically bridging {@link CollectionRegion} to {@link Cache}.
 *
 * @author Steve Ebersole
 */
public class CollectionRegionAdapter extends BaseTransactionalDataRegionAdapter implements CollectionRegion {
	private static final Logger log = LoggerFactory.getLogger( CollectionRegionAdapter.class );

	public CollectionRegionAdapter(Cache underlyingCache, Settings settings, CacheDataDescription metadata) {
		super( underlyingCache, settings, metadata );
		if ( underlyingCache instanceof OptimisticCache ) {
			( ( OptimisticCache ) underlyingCache ).setSource( new OptimisticCacheSourceAdapter( metadata ) );
		}
	}

	public CollectionRegionAccessStrategy buildAccessStrategy(AccessType accessType) throws CacheException {
		CacheConcurrencyStrategy ccs;
		if ( AccessType.READ_ONLY.equals( accessType ) ) {
			if ( metadata.isMutable() ) {
				log.warn( "read-only cache configured for mutable collection [" + getName() + "]" );
			}
			ccs = new ReadOnlyCache();
		}
		else if ( AccessType.READ_WRITE.equals( accessType ) ) {
			ccs = new ReadWriteCache();
		}
		else if ( AccessType.NONSTRICT_READ_WRITE.equals( accessType ) ) {
			ccs = new NonstrictReadWriteCache();
		}
		else if ( AccessType.TRANSACTIONAL.equals( accessType ) ) {
			ccs = new TransactionalCache();
		}
		else {
			throw new IllegalArgumentException( "unrecognized access strategy type [" + accessType + "]" );
		}
		ccs.setCache( underlyingCache );
		return new CollectionAccessStrategyAdapter( this, ccs, settings );
	}
}
