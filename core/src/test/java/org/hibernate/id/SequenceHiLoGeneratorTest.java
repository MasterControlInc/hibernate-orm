/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * Copyright (c) 2010, Red Hat Inc. or third-party contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors.  All third-party contributions are
 * distributed under license by Red Hat Inc.
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
package org.luceehibernate.id;

import java.util.Properties;

import junit.framework.TestCase;

import org.luceehibernate.Hibernate;
import org.luceehibernate.TestingDatabaseInfo;
import org.luceehibernate.cfg.Configuration;
import org.luceehibernate.cfg.Environment;
import org.luceehibernate.cfg.NamingStrategy;
import org.luceehibernate.cfg.ObjectNameNormalizer;
import org.luceehibernate.dialect.Dialect;
import org.luceehibernate.dialect.HSQLDialect;
import org.luceehibernate.engine.SessionFactoryImplementor;
import org.luceehibernate.impl.SessionImpl;
import org.luceehibernate.mapping.SimpleAuxiliaryDatabaseObject;

/**
 * I went back to 3.3 source and grabbed the code/logic as it existed back then and crafted this
 * unit test so that we can make sure the value keep being generated in the expected manner
 *
 * @author Steve Ebersole
 */
public class SequenceHiLoGeneratorTest extends TestCase {
	private static final String TEST_SEQUENCE = "test_sequence";

	private Configuration cfg;
	private SessionFactoryImplementor sessionFactory;
	private SequenceHiLoGenerator generator;

	protected void setUp() throws Exception {
		super.setUp();

		Properties properties = new Properties();
		properties.setProperty( SequenceGenerator.SEQUENCE, TEST_SEQUENCE );
		properties.setProperty( SequenceHiLoGenerator.MAX_LO, "3" );
		properties.setProperty( SequenceGenerator.PARAMETERS, "start with 1" );  // hsqldb sequences start with 0 by default :?
		properties.put(
				PersistentIdentifierGenerator.IDENTIFIER_NORMALIZER,
				new ObjectNameNormalizer() {
					protected boolean isUseQuotedIdentifiersGlobally() {
						return false;
					}

					protected NamingStrategy getNamingStrategy() {
						return cfg.getNamingStrategy();
					}
				}
		);

		Dialect dialect = new HSQLDialect();

		generator = new SequenceHiLoGenerator();
		//noinspection deprecation
		generator.configure( Hibernate.LONG, properties, dialect );

		cfg = TestingDatabaseInfo.buildBaseConfiguration()
				.setProperty( Environment.HBM2DDL_AUTO, "create-drop" );
		cfg.addAuxiliaryDatabaseObject(
				new SimpleAuxiliaryDatabaseObject(
						generator.sqlCreateStrings( dialect )[0],
						generator.sqlDropStrings( dialect )[0]
				)
		);

		sessionFactory = (SessionFactoryImplementor) cfg.buildSessionFactory();
	}

	protected void tearDown() throws Exception {
		if ( sessionFactory != null ) {
			sessionFactory.close();
		}

		super.tearDown();
	}

	public void testHiLoAlgorithm() {
		SessionImpl session = (SessionImpl) sessionFactory.openSession();
		session.beginTransaction();

		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// initially sequence should be uninitialized
//		assertEquals( 1L, generator.getHiloOptimizer().getLastSourceValue().makeValue().longValue() );
// we have to assume here since in this branch we are testing with hsqldb which does not allow access to the
// current sequence value and the optimizer does not yet know the value.  On trunk (3.6), against H2, we physically
// check the sequence value in the database

		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// historically the hilo generators skipped the initial block of values;
		// 		so the first generated id value is maxlo + 1, here be 4
		Long generatedValue = (Long) generator.generate( session, null );
		assertEquals( 4L, generatedValue.longValue() );
		// which should also perform the first read on the sequence which should set it to its "start with" value (1)
		assertEquals( 1L, generator.getHiloOptimizer().getLastSourceValue().makeValue().longValue() );

		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		generatedValue = (Long) generator.generate( session, null );
		assertEquals( 5L, generatedValue.longValue() );
		assertEquals( 1L, generator.getHiloOptimizer().getLastSourceValue().makeValue().longValue() );

		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		generatedValue = (Long) generator.generate( session, null );
		assertEquals( 6L, generatedValue.longValue() );
		assertEquals( 1L, generator.getHiloOptimizer().getLastSourceValue().makeValue().longValue() );

		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		generatedValue = (Long) generator.generate( session, null );
		assertEquals( 7L, generatedValue.longValue() );
		// unlike the newer strategies, the db value will not get update here.  It gets updated on the next invocation
		// 	after a clock over
		assertEquals( 1L, generator.getHiloOptimizer().getLastSourceValue().makeValue().longValue() );

		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		generatedValue = (Long) generator.generate( session, null );
		assertEquals( 8L, generatedValue.longValue() );
		// this should force an increment in the sequence value
		assertEquals( 2L, generator.getHiloOptimizer().getLastSourceValue().makeValue().longValue() );

		session.getTransaction().commit();
		session.close();
	}
}
