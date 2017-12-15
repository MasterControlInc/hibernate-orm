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
import org.luceehibernate.HibernateException;
import org.luceehibernate.engine.TypedValue;
import org.luceehibernate.util.StringHelper;

/**
 * Constrains a property to be non-null
 * @author Gavin King
 */
public class NotNullExpression implements Criterion {

	private final String propertyName;

	private static final TypedValue[] NO_VALUES = new TypedValue[0];

	protected NotNullExpression(String propertyName) {
		this.propertyName = propertyName;
	}

	public String toSqlString(Criteria criteria, CriteriaQuery criteriaQuery)
	throws HibernateException {
		String[] columns = criteriaQuery.getColumnsUsingProjection(criteria, propertyName);
		String result = StringHelper.join(
			" or ",
			StringHelper.suffix( columns, " is not null" )
		);
		if (columns.length>1) result = '(' + result + ')';
		return result;

		//TODO: get SQL rendering out of this package!
	}

	public TypedValue[] getTypedValues(Criteria criteria, CriteriaQuery criteriaQuery)
	throws HibernateException {
		return NO_VALUES;
	}

	public String toString() {
		return propertyName + " is not null";
	}

}
