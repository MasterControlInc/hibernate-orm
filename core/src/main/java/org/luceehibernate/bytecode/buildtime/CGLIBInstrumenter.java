/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * Copyright (c) 2009, Red Hat Middleware LLC or third-party contributors as
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
 */
package org.luceehibernate.bytecode.buildtime;

import java.util.Set;
import java.io.ByteArrayInputStream;

import org.luceehibernate.bytecode.util.ClassDescriptor;
import org.luceehibernate.bytecode.util.BasicClassFilter;
import org.luceehibernate.bytecode.ClassTransformer;
import org.luceehibernate.bytecode.cglib.BytecodeProviderImpl;
import org.objectweb.asm.ClassReader;
import net.sf.cglib.core.ClassNameReader;
import net.sf.cglib.transform.impl.InterceptFieldEnabled;

/**
 * Strategy for performing build-time instrumentation of persistent classes in order to enable
 * field-level interception using CGLIB.
 *
 * @author Steve Ebersole
 * @author Gavin King
 */
public class CGLIBInstrumenter extends AbstractInstrumenter {
	private static final BasicClassFilter CLASS_FILTER = new BasicClassFilter();

	private final BytecodeProviderImpl provider = new BytecodeProviderImpl();

	public CGLIBInstrumenter(Logger logger, Options options) {
		super( logger, options );
	}

	protected ClassDescriptor getClassDescriptor(byte[] byecode) throws Exception {
		return new CustomClassDescriptor( byecode );
	}

	protected ClassTransformer getClassTransformer(ClassDescriptor descriptor, Set classNames) {
		if ( descriptor.isInstrumented() ) {
			logger.debug( "class [" + descriptor.getName() + "] already instrumented" );
			return null;
		}
		else {
			return provider.getTransformer( CLASS_FILTER, new CustomFieldFilter( descriptor, classNames ) );
		}
	}

	private static class CustomClassDescriptor implements ClassDescriptor {
		private final byte[] bytecode;
		private final String name;
		private final boolean isInstrumented;

		public CustomClassDescriptor(byte[] bytecode) throws Exception {
			this.bytecode = bytecode;
			ClassReader reader = new ClassReader( new ByteArrayInputStream( bytecode ) );
			String[] names = ClassNameReader.getClassInfo( reader );
			this.name = names[0];
			boolean instrumented = false;
			for ( int i = 1; i < names.length; i++ ) {
				if ( InterceptFieldEnabled.class.getName().equals( names[i] ) ) {
					instrumented = true;
					break;
				}
			}
			this.isInstrumented = instrumented;
		}

		public String getName() {
			return name;
		}

		public boolean isInstrumented() {
			return isInstrumented;
		}

		public byte[] getBytes() {
			return bytecode;
		}
	}

}
