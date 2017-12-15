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
package org.luceehibernate.tuple.component;

import java.util.Map;

import org.luceehibernate.mapping.Component;
import org.luceehibernate.mapping.Property;
import org.luceehibernate.property.Getter;
import org.luceehibernate.property.PropertyAccessor;
import org.luceehibernate.property.PropertyAccessorFactory;
import org.luceehibernate.property.Setter;
import org.luceehibernate.tuple.Instantiator;
import org.luceehibernate.tuple.DynamicMapInstantiator;

/**
 * A {@link ComponentTuplizer} specific to the dynamic-map entity mode.
 *
 * @author Gavin King
 * @author Steve Ebersole
 */
public class DynamicMapComponentTuplizer extends AbstractComponentTuplizer {

	public Class getMappedClass() {
		return Map.class;
	}

	protected Instantiator buildInstantiator(Component component) {
		return new DynamicMapInstantiator();
	}

	public DynamicMapComponentTuplizer(Component component) {
		super(component);
	}

	private PropertyAccessor buildPropertyAccessor(Property property) {
		return PropertyAccessorFactory.getDynamicMapPropertyAccessor();
	}

	protected Getter buildGetter(Component component, Property prop) {
		return buildPropertyAccessor(prop).getGetter( null, prop.getName() );
	}

	protected Setter buildSetter(Component component, Property prop) {
		return buildPropertyAccessor(prop).getSetter( null, prop.getName() );
	}

}
