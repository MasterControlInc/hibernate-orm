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
package org.luceehibernate.type;

import java.io.Serializable;
import java.util.Comparator;
import java.util.TreeSet;

import org.dom4j.Element;
import org.luceehibernate.EntityMode;
import org.luceehibernate.collection.PersistentCollection;
import org.luceehibernate.collection.PersistentElementHolder;
import org.luceehibernate.collection.PersistentSortedSet;
import org.luceehibernate.engine.SessionImplementor;
import org.luceehibernate.persister.collection.CollectionPersister;

public class SortedSetType extends SetType {

	private final Comparator comparator;

	public SortedSetType(String role, String propertyRef, Comparator comparator, boolean isEmbeddedInXML) {
		super(role, propertyRef, isEmbeddedInXML);
		this.comparator = comparator;
	}

	public PersistentCollection instantiate(SessionImplementor session, CollectionPersister persister, Serializable key) {
		if ( session.getEntityMode()==EntityMode.DOM4J ) {
			return new PersistentElementHolder(session, persister, key);
		}
		else {
			PersistentSortedSet set = new PersistentSortedSet(session);
			set.setComparator(comparator);
			return set;
		}
	}

	public Class getReturnedClass() {
		return java.util.SortedSet.class;
	}

	public Object instantiate(int anticipatedSize) {
		return new TreeSet(comparator);
	}
	
	public PersistentCollection wrap(SessionImplementor session, Object collection) {
		if ( session.getEntityMode()==EntityMode.DOM4J ) {
			return new PersistentElementHolder( session, (Element) collection );
		}
		else {
			return new PersistentSortedSet( session, (java.util.SortedSet) collection );
		}
	}
}






