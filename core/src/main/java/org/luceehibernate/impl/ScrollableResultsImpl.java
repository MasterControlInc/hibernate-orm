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

import org.luceehibernate.HibernateException;
import org.luceehibernate.MappingException;
import org.luceehibernate.ScrollableResults;
import org.luceehibernate.engine.QueryParameters;
import org.luceehibernate.engine.SessionImplementor;
import org.luceehibernate.exception.JDBCExceptionHelper;
import org.luceehibernate.hql.HolderInstantiator;
import org.luceehibernate.loader.Loader;
import org.luceehibernate.type.Type;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Implementation of the <tt>ScrollableResults</tt> interface
 * @author Gavin King
 */
public class ScrollableResultsImpl extends AbstractScrollableResults implements ScrollableResults {

	private Object[] currentRow;

	public ScrollableResultsImpl(
	        ResultSet rs,
	        PreparedStatement ps,
	        SessionImplementor sess,
	        Loader loader,
	        QueryParameters queryParameters,
	        Type[] types, HolderInstantiator holderInstantiator) throws MappingException {
		super( rs, ps, sess, loader, queryParameters, types, holderInstantiator );
	}

	protected Object[] getCurrentRow() {
		return currentRow;
	}

	/**
	 * @see org.luceehibernate.ScrollableResults#scroll(int)
	 */
	public boolean scroll(int i) throws HibernateException {
		try {
			boolean result = getResultSet().relative(i);
			prepareCurrentRow(result);
			return result;
		}
		catch (SQLException sqle) {
			throw JDBCExceptionHelper.convert(
					getSession().getFactory().getSQLExceptionConverter(),
					sqle,
					"could not advance using scroll()"
				);
		}
	}

	/**
	 * @see org.luceehibernate.ScrollableResults#first()
	 */
	public boolean first() throws HibernateException {
		try {
			boolean result = getResultSet().first();
			prepareCurrentRow(result);
			return result;
		}
		catch (SQLException sqle) {
			throw JDBCExceptionHelper.convert(
					getSession().getFactory().getSQLExceptionConverter(),
					sqle,
					"could not advance using first()"
				);
		}
	}

	/**
	 * @see org.luceehibernate.ScrollableResults#last()
	 */
	public boolean last() throws HibernateException {
		try {
			boolean result = getResultSet().last();
			prepareCurrentRow(result);
			return result;
		}
		catch (SQLException sqle) {
			throw JDBCExceptionHelper.convert(
					getSession().getFactory().getSQLExceptionConverter(),
					sqle,
					"could not advance using last()"
				);
		}
	}

	/**
	 * @see org.luceehibernate.ScrollableResults#next()
	 */
	public boolean next() throws HibernateException {
		try {
			boolean result = getResultSet().next();
			prepareCurrentRow(result);
			return result;
		}
		catch (SQLException sqle) {
			throw JDBCExceptionHelper.convert(
					getSession().getFactory().getSQLExceptionConverter(),
					sqle,
					"could not advance using next()"
				);
		}
	}

	/**
	 * @see org.luceehibernate.ScrollableResults#previous()
	 */
	public boolean previous() throws HibernateException {
		try {
			boolean result = getResultSet().previous();
			prepareCurrentRow(result);
			return result;
		}
		catch (SQLException sqle) {
			throw JDBCExceptionHelper.convert(
					getSession().getFactory().getSQLExceptionConverter(),
					sqle,
					"could not advance using previous()"
				);
		}
	}

	/**
	 * @see org.luceehibernate.ScrollableResults#afterLast()
	 */
	public void afterLast() throws HibernateException {
		try {
			getResultSet().afterLast();
		}
		catch (SQLException sqle) {
			throw JDBCExceptionHelper.convert(
					getSession().getFactory().getSQLExceptionConverter(),
					sqle,
					"exception calling afterLast()"
				);
		}
	}

	/**
	 * @see org.luceehibernate.ScrollableResults#beforeFirst()
	 */
	public void beforeFirst() throws HibernateException {
		try {
			getResultSet().beforeFirst();
		}
		catch (SQLException sqle) {
			throw JDBCExceptionHelper.convert(
					getSession().getFactory().getSQLExceptionConverter(),
					sqle,
					"exception calling beforeFirst()"
				);
		}
	}

	/**
	 * @see org.luceehibernate.ScrollableResults#isFirst()
	 */
	public boolean isFirst() throws HibernateException {
		try {
			return getResultSet().isFirst();
		}
		catch (SQLException sqle) {
			throw JDBCExceptionHelper.convert(
					getSession().getFactory().getSQLExceptionConverter(),
					sqle,
					"exception calling isFirst()"
				);
		}
	}

	/**
	 * @see org.luceehibernate.ScrollableResults#isLast()
	 */
	public boolean isLast() throws HibernateException {
		try {
			return getResultSet().isLast();
		}
		catch (SQLException sqle) {
			throw JDBCExceptionHelper.convert(
					getSession().getFactory().getSQLExceptionConverter(),
					sqle,
					"exception calling isLast()"
				);
		}
	}

	public int getRowNumber() throws HibernateException {
		try {
			return getResultSet().getRow()-1;
		}
		catch (SQLException sqle) {
			throw JDBCExceptionHelper.convert(
					getSession().getFactory().getSQLExceptionConverter(),
					sqle,
					"exception calling getRow()"
				);
		}
	}

	public boolean setRowNumber(int rowNumber) throws HibernateException {
		if (rowNumber>=0) rowNumber++;
		try {
			boolean result = getResultSet().absolute(rowNumber);
			prepareCurrentRow(result);
			return result;
		}
		catch (SQLException sqle) {
			throw JDBCExceptionHelper.convert(
					getSession().getFactory().getSQLExceptionConverter(),
					sqle,
					"could not advance using absolute()"
				);
		}
	}

	private void prepareCurrentRow(boolean underlyingScrollSuccessful) 
	throws HibernateException {
		
		if (!underlyingScrollSuccessful) {
			currentRow = null;
			return;
		}

		Object result = getLoader().loadSingleRow(
				getResultSet(),
				getSession(),
				getQueryParameters(),
				false
		);
		if ( result != null && result.getClass().isArray() ) {
			currentRow = (Object[]) result;
		}
		else {
			currentRow = new Object[] { result };
		}

		if ( getHolderInstantiator() != null ) {
			currentRow = new Object[] { getHolderInstantiator().instantiate(currentRow) };
		}

		afterScrollOperation();
	}

}
