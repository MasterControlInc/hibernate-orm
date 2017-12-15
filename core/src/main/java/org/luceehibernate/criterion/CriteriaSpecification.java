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

import org.luceehibernate.transform.AliasToEntityMapResultTransformer;
import org.luceehibernate.transform.DistinctRootEntityResultTransformer;
import org.luceehibernate.transform.PassThroughResultTransformer;
import org.luceehibernate.transform.ResultTransformer;
import org.luceehibernate.transform.RootEntityResultTransformer;

/**
 * @author Gavin King
 */
public interface CriteriaSpecification {

	/**
	 * The alias that refers to the "root" entity of the criteria query.
	 */
	public static final String ROOT_ALIAS = "this";

	/**
	 * Each row of results is a <tt>Map</tt> from alias to entity instance
	 */
	public static final ResultTransformer ALIAS_TO_ENTITY_MAP = AliasToEntityMapResultTransformer.INSTANCE;

	/**
	 * Each row of results is an instance of the root entity
	 */
	public static final ResultTransformer ROOT_ENTITY = RootEntityResultTransformer.INSTANCE;

	/**
	 * Each row of results is a distinct instance of the root entity
	 */
	public static final ResultTransformer DISTINCT_ROOT_ENTITY = DistinctRootEntityResultTransformer.INSTANCE;

	/**
	 * This result transformer is selected implicitly by calling <tt>setProjection()</tt>
	 */
	public static final ResultTransformer PROJECTION = PassThroughResultTransformer.INSTANCE;

	/**
	 * Specifies joining to an entity based on an inner join.
	 */
	public static final int INNER_JOIN = org.luceehibernate.sql.JoinFragment.INNER_JOIN;

	/**
	 * Specifies joining to an entity based on a full join.
	 */
	public static final int FULL_JOIN = org.luceehibernate.sql.JoinFragment.FULL_JOIN;

	/**
	 * Specifies joining to an entity based on a left outer join.
	 */
	public static final int LEFT_JOIN = org.luceehibernate.sql.JoinFragment.LEFT_OUTER_JOIN;
	
}
