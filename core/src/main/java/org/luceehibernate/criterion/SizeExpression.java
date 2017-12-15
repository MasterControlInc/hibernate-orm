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
package org.luceehibernate.criterion;

import org.luceehibernate.Criteria;
import org.luceehibernate.EntityMode;
import org.luceehibernate.Hibernate;
import org.luceehibernate.HibernateException;
import org.luceehibernate.engine.TypedValue;
import org.luceehibernate.persister.collection.QueryableCollection;
import org.luceehibernate.persister.entity.Loadable;
import org.luceehibernate.sql.ConditionFragment;

/**
 * @author Gavin King
 */
public class SizeExpression implements Criterion {
	
	private final String propertyName;
	private final int size;
	private final String op;
	
	protected SizeExpression(String propertyName, int size, String op) {
		this.propertyName = propertyName;
		this.size = size;
		this.op = op;
	}

	public String toString() {
		return propertyName + ".size" + op + size;
	}

	public String toSqlString(Criteria criteria, CriteriaQuery criteriaQuery)
	throws HibernateException {
		String role = criteriaQuery.getEntityName(criteria, propertyName) + 
				'.' +  
				criteriaQuery.getPropertyName(propertyName);
		QueryableCollection cp = (QueryableCollection) criteriaQuery.getFactory()
				.getCollectionPersister(role);
		//String[] fk = StringHelper.qualify( "collection_", cp.getKeyColumnNames() );
		String[] fk = cp.getKeyColumnNames();
		String[] pk = ( (Loadable) cp.getOwnerEntityPersister() ).getIdentifierColumnNames(); //TODO: handle property-ref
		return "? " + 
				op + 
				" (select count(*) from " +
				cp.getTableName() +
				//" collection_ where " +
				" where " +
				new ConditionFragment()
						.setTableAlias( criteriaQuery.getSQLAlias(criteria, propertyName) )
						.setCondition(pk, fk)
						.toFragmentString() +
				")";
	}

	public TypedValue[] getTypedValues(Criteria criteria, CriteriaQuery criteriaQuery) 
	throws HibernateException {
		return new TypedValue[] { 
			new TypedValue( Hibernate.INTEGER, new Integer(size), EntityMode.POJO ) 
		};
	}

}
