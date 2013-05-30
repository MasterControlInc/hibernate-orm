package org.hibernate.test.annotations.uniqueconstraint;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.junit.Test;

import org.hibernate.AnnotationException;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.metamodel.MetadataSources;
import org.hibernate.service.spi.ServiceRegistryImplementor;
import org.hibernate.testing.FailureExpectedWithNewMetamodel;
import org.hibernate.testing.TestForIssue;
import org.hibernate.testing.junit4.BaseUnitTestCase;

/**
 * @author Nikolay Shestakov
 *
 */
public class UniqueConstraintValidationTest extends BaseUnitTestCase {

	@Test(expected = AnnotationException.class)
	@TestForIssue(jiraKey = "HHH-4084")
	@FailureExpectedWithNewMetamodel
	public void testUniqueConstraintWithEmptyColumnName() {
		buildSessionFactory(EmptyColumnNameEntity.class);
	}

	@Test
	public void testUniqueConstraintWithEmptyColumnNameList() {
		buildSessionFactory(EmptyColumnNameListEntity.class);
	}

	@Test(expected = AnnotationException.class)
	@FailureExpectedWithNewMetamodel
	public void testUniqueConstraintWithNotExistsColumnName() {
		buildSessionFactory(NotExistsColumnEntity.class);
	}

	private void buildSessionFactory(Class<?> entity) {
		if ( isMetadataUsed ) {
			StandardServiceRegistry registry = new StandardServiceRegistryBuilder().build();
			MetadataSources metadataSources = new MetadataSources( registry );
			metadataSources.addAnnotatedClass( entity );
			metadataSources.buildMetadata();
			StandardServiceRegistryBuilder.destroy( registry );
		}
		else {
			Configuration cfg = new Configuration();
			cfg.addAnnotatedClass( entity );
			cfg.buildMappings();
			ServiceRegistryImplementor serviceRegistry = (ServiceRegistryImplementor) new StandardServiceRegistryBuilder()
					.applySettings( cfg.getProperties() ).build();
			cfg.buildSessionFactory( serviceRegistry ).close();
			serviceRegistry.destroy();
		}
	}

	@Entity
	@Table(name = "tbl_emptycolumnnameentity", uniqueConstraints = @UniqueConstraint(columnNames = ""))
	public static class EmptyColumnNameEntity implements Serializable {
		@Id
		protected Long id;
	}

	@Entity
	@Table(name = "tbl_emptycolumnnamelistentity", uniqueConstraints = @UniqueConstraint(columnNames = {}))
	public static class EmptyColumnNameListEntity implements Serializable {
		@Id
		protected Long id;
	}

	@Entity
	@Table(name = "tbl_notexistscolumnentity", uniqueConstraints = @UniqueConstraint(columnNames = "notExists"))
	public static class NotExistsColumnEntity implements Serializable {
		@Id
		protected Long id;
	}
}
