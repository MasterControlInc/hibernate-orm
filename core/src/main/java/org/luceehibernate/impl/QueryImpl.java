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
package org.luceehibernate.impl;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.luceehibernate.FlushMode;
import org.luceehibernate.HibernateException;
import org.luceehibernate.LockMode;
import org.luceehibernate.Query;
import org.luceehibernate.ScrollMode;
import org.luceehibernate.ScrollableResults;
import org.luceehibernate.LockOptions;
import org.luceehibernate.engine.QueryParameters;
import org.luceehibernate.engine.SessionImplementor;
import org.luceehibernate.engine.query.ParameterMetadata;

/**
 * default implementation of the <tt>Query</tt> interface,
 * for "ordinary" HQL queries (not collection filters)
 * @see CollectionFilterImpl
 * @author Gavin King
 */
public class QueryImpl extends AbstractQueryImpl {

	private LockOptions lockOptions = new LockOptions();

	public QueryImpl(
			String queryString,
	        FlushMode flushMode,
	        SessionImplementor session,
	        ParameterMetadata parameterMetadata) {
		super( queryString, flushMode, session, parameterMetadata );
	}

	public QueryImpl(String queryString, SessionImplementor session, ParameterMetadata parameterMetadata) {
		this( queryString, null, session, parameterMetadata );
	}

	public Iterator iterate() throws HibernateException {
		verifyParameters();
		Map namedParams = getNamedParams();
		before();
		try {
			return getSession().iterate(
					expandParameterLists(namedParams),
			        getQueryParameters(namedParams)
				);
		}
		finally {
			after();
		}
	}

	public ScrollableResults scroll() throws HibernateException {
		return scroll( ScrollMode.SCROLL_INSENSITIVE );
	}

	public ScrollableResults scroll(ScrollMode scrollMode) throws HibernateException {
		verifyParameters();
		Map namedParams = getNamedParams();
		before();
		QueryParameters qp = getQueryParameters(namedParams);
		qp.setScrollMode(scrollMode);
		try {
			return getSession().scroll( expandParameterLists(namedParams), qp );
		}
		finally {
			after();
		}
	}

	public List list() throws HibernateException {
		verifyParameters();
		Map namedParams = getNamedParams();
		before();
		try {
			return getSession().list(
					expandParameterLists(namedParams),
			        getQueryParameters(namedParams)
				);
		}
		finally {
			after();
		}
	}

	public int executeUpdate() throws HibernateException {
		verifyParameters();
		Map namedParams = getNamedParams();
		before();
		try {
            return getSession().executeUpdate(
                    expandParameterLists( namedParams ),
                    getQueryParameters( namedParams )
	            );
		}
		finally {
			after();
		}
	}

	public Query setLockMode(String alias, LockMode lockMode) {
		lockOptions.setAliasSpecificLockMode( alias, lockMode );
		return this;
	}
	
	public Query setLockOptions(LockOptions lockOption) {
		this.lockOptions.setLockMode(lockOption.getLockMode());
		this.lockOptions.setScope(lockOption.getScope());
		this.lockOptions.setTimeOut(lockOptions.getTimeOut());
		return this;
	}

	public LockOptions getLockOptions() {
		return lockOptions;
	}

}






