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
package org.luceehibernate.id;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import org.luceehibernate.MappingException;
import org.luceehibernate.HibernateException;
import org.luceehibernate.id.insert.InsertGeneratedIdentifierDelegate;
import org.luceehibernate.id.insert.IdentifierGeneratingInsert;
import org.luceehibernate.id.insert.AbstractSelectingDelegate;
import org.luceehibernate.dialect.Dialect;
import org.luceehibernate.engine.SessionImplementor;
import org.luceehibernate.engine.ValueInclusion;
import org.luceehibernate.type.Type;

/**
 * A generator that selects the just inserted row to determine the identifier
 * value assigned by the database. The correct row is located using a unique
 * key.
 * <p/>
 * One mapping parameter is required: key (unless a natural-id is defined in the mapping).
 *
 * @author Gavin King
 */
public class SelectGenerator extends AbstractPostInsertGenerator implements Configurable {
	
	private String uniqueKeyPropertyName;

	public void configure(Type type, Properties params, Dialect d) throws MappingException {
		uniqueKeyPropertyName = params.getProperty( "key" );
	}

	public InsertGeneratedIdentifierDelegate getInsertGeneratedIdentifierDelegate(
			PostInsertIdentityPersister persister,
	        Dialect dialect,
	        boolean isGetGeneratedKeysEnabled) throws HibernateException {
		return new SelectGeneratorDelegate( persister, dialect, uniqueKeyPropertyName );
	}

	private static String determineNameOfPropertyToUse(PostInsertIdentityPersister persister, String supplied) {
		if ( supplied != null ) {
			return supplied;
		}
		int[] naturalIdPropertyIndices = persister.getNaturalIdentifierProperties();
		if ( naturalIdPropertyIndices == null ){
			throw new IdentifierGenerationException(
					"no natural-id property defined; need to specify [key] in " +
					"generator parameters"
			);
		}
		if ( naturalIdPropertyIndices.length > 1 ) {
			throw new IdentifierGenerationException(
					"select generator does not currently support composite " +
					"natural-id properties; need to specify [key] in generator parameters"
			);
		}
		ValueInclusion inclusion = persister.getPropertyInsertGenerationInclusions() [ naturalIdPropertyIndices[0] ];
		if ( inclusion != ValueInclusion.NONE ) {
			throw new IdentifierGenerationException(
					"natural-id also defined as insert-generated; need to specify [key] " +
					"in generator parameters"
			);
		}
		return persister.getPropertyNames() [ naturalIdPropertyIndices[0] ];
	}


	/**
	 * The delegate for the select generation strategy.
	 */
	public static class SelectGeneratorDelegate
			extends AbstractSelectingDelegate
			implements InsertGeneratedIdentifierDelegate {
		private final PostInsertIdentityPersister persister;
		private final Dialect dialect;

		private final String uniqueKeyPropertyName;
		private final Type uniqueKeyType;
		private final Type idType;

		private final String idSelectString;

		private SelectGeneratorDelegate(
				PostInsertIdentityPersister persister,
		        Dialect dialect,
		        String suppliedUniqueKeyPropertyName) {
			super( persister );
			this.persister = persister;
			this.dialect = dialect;
			this.uniqueKeyPropertyName = determineNameOfPropertyToUse( persister, suppliedUniqueKeyPropertyName );

			idSelectString = persister.getSelectByUniqueKeyString( uniqueKeyPropertyName );
			uniqueKeyType = persister.getPropertyType( uniqueKeyPropertyName );
			idType = persister.getIdentifierType();
		}

		public IdentifierGeneratingInsert prepareIdentifierGeneratingInsert() {
			return new IdentifierGeneratingInsert( dialect );
		}


		// AbstractSelectingDelegate impl ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

		protected String getSelectSQL() {
			return idSelectString;
		}

		protected void bindParameters(
				SessionImplementor session,
		        PreparedStatement ps,
		        Object entity) throws SQLException {
			Object uniqueKeyValue = persister.getPropertyValue( entity, uniqueKeyPropertyName, session.getEntityMode() );
			uniqueKeyType.nullSafeSet( ps, uniqueKeyValue, 1, session );
		}

		protected Serializable getResult(
				SessionImplementor session,
		        ResultSet rs,
		        Object entity) throws SQLException {
			if ( !rs.next() ) {
				throw new IdentifierGenerationException(
						"the inserted row could not be located by the unique key: " +
						uniqueKeyPropertyName
				);
			}
			return ( Serializable ) idType.nullSafeGet(
					rs,
					persister.getRootTableKeyColumnNames(),
					session,
					entity
			);
		}
	}
}
