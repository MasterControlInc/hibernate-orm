/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * Copyright (c) 2010, Red Hat Inc. or third-party contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors.  All third-party contributions are
 * distributed under license by Red Hat Inc.
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
 */
package org.luceehibernate.cache.impl.bridge;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.luceehibernate.cache.RegionFactory;
import org.luceehibernate.cache.CacheProvider;
import org.luceehibernate.cache.CacheException;
import org.luceehibernate.cache.EntityRegion;
import org.luceehibernate.cache.CollectionRegion;
import org.luceehibernate.cache.QueryResultsRegion;
import org.luceehibernate.cache.NoCacheProvider;
import org.luceehibernate.cache.TimestampsRegion;
import org.luceehibernate.cache.CacheDataDescription;
import org.luceehibernate.cache.access.AccessType;
import org.luceehibernate.util.PropertiesHelper;
import org.luceehibernate.util.ReflectHelper;
import org.luceehibernate.cfg.Environment;
import org.luceehibernate.cfg.Settings;

/**
 * Acts as a bridge between the {@link RegionFactory} contract and the older
 * {@link CacheProvider} contract.
 *
 * @author Steve Ebersole
 */
public class RegionFactoryCacheProviderBridge implements RegionFactory {
	public static final String DEF_PROVIDER = NoCacheProvider.class.getName();
	private static final Logger log = LoggerFactory.getLogger( RegionFactoryCacheProviderBridge.class );

	private CacheProvider cacheProvider;
	private Settings settings;

	public RegionFactoryCacheProviderBridge(Properties properties) {
		String providerClassName = PropertiesHelper.getString( Environment.CACHE_PROVIDER, properties, DEF_PROVIDER );
		log.info( "Cache provider: " + providerClassName );
		try {
			cacheProvider = ( CacheProvider ) ReflectHelper.classForName( providerClassName ).newInstance();
		}
		catch ( Exception cnfe ) {
			throw new CacheException( "could not instantiate CacheProvider [" + providerClassName + "]", cnfe );
		}
	}

	public void start(Settings settings, Properties properties) throws CacheException {
		this.settings = settings;
		cacheProvider.start( properties );
	}

	public void stop() {
		cacheProvider.stop();
		cacheProvider = null;
	}

	public boolean isMinimalPutsEnabledByDefault() {
		return cacheProvider.isMinimalPutsEnabledByDefault();
	}

	/**
	 * {@inheritDoc}
	 */
	public AccessType getDefaultAccessType() {
		// we really have no idea
		return null;
	}

	public long nextTimestamp() {
		return cacheProvider.nextTimestamp();
	}

	public CacheProvider getCacheProvider() {
		return cacheProvider;
	}

	public EntityRegion buildEntityRegion(
			String regionName,
			Properties properties,
			CacheDataDescription metadata) throws CacheException {
		return new EntityRegionAdapter( cacheProvider.buildCache( regionName, properties ), settings, metadata );
	}

	public CollectionRegion buildCollectionRegion(
			String regionName,
			Properties properties,
			CacheDataDescription metadata) throws CacheException {
		return new CollectionRegionAdapter( cacheProvider.buildCache( regionName, properties ), settings, metadata );
	}

	public QueryResultsRegion buildQueryResultsRegion(String regionName, Properties properties) throws CacheException {
		return new QueryResultsRegionAdapter( cacheProvider.buildCache( regionName, properties ), settings );
	}

	public TimestampsRegion buildTimestampsRegion(String regionName, Properties properties) throws CacheException {
		return new TimestampsRegionAdapter( cacheProvider.buildCache( regionName, properties ), settings );
	}


}
