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
package org.luceehibernate.loader.entity;

import org.luceehibernate.MappingException;
import org.luceehibernate.engine.CascadingAction;
import org.luceehibernate.engine.SessionFactoryImplementor;
import org.luceehibernate.engine.LoadQueryInfluencers;
import org.luceehibernate.loader.JoinWalker;
import org.luceehibernate.persister.entity.OuterJoinLoadable;

public class CascadeEntityLoader extends AbstractEntityLoader {
	
	public CascadeEntityLoader(
			OuterJoinLoadable persister,
			CascadingAction action,
			SessionFactoryImplementor factory) throws MappingException {
		super(
				persister, 
				persister.getIdentifierType(), 
				factory,
				LoadQueryInfluencers.NONE
		);

		JoinWalker walker = new CascadeEntityJoinWalker(
				persister, 
				action,
				factory
		);
		initFromWalker( walker );

		postInstantiate();
		
		log.debug( "Static select for action " + action + " on entity " + entityName + ": " + getSQLString() );

	}

}
