/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * Copyright (c) 2011, Red Hat Inc. or third-party contributors as
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
package org.hibernate.metamodel.internal.source.annotations.global;

import java.util.Collection;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.logging.Logger;

import org.hibernate.AnnotationException;
import org.hibernate.MappingException;
import org.hibernate.annotations.FetchMode;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.metamodel.internal.source.annotations.AnnotationBindingContext;
import org.hibernate.metamodel.internal.source.annotations.util.AnnotationParserHelper;
import org.hibernate.metamodel.internal.source.annotations.util.EnumConversionHelper;
import org.hibernate.metamodel.internal.source.annotations.util.HibernateDotNames;
import org.hibernate.metamodel.internal.source.annotations.util.JandexHelper;
import org.hibernate.metamodel.spi.MetadataImplementor;
import org.hibernate.metamodel.spi.binding.SecondaryTable;
import org.hibernate.metamodel.spi.relational.Column;
import org.hibernate.metamodel.spi.relational.Index;
import org.hibernate.metamodel.spi.relational.ObjectName;
import org.hibernate.metamodel.spi.relational.Schema;
import org.hibernate.metamodel.spi.relational.Table;
import org.hibernate.metamodel.spi.relational.Value;

/**
 * Binds table related information. This binder is called after the entities are bound.
 *
 * @author Hardy Ferentschik
 */
public class TableProcessor {

	private static final CoreMessageLogger LOG = Logger.getMessageLogger(
			CoreMessageLogger.class,
			TableProcessor.class.getName()
	);

	private TableProcessor() {
	}

	/**
	 * Binds {@link org.hibernate.annotations.Tables} and {@link org.hibernate.annotations.Table} annotations to the supplied
	 * metadata.
	 *
	 * @param bindingContext the context for annotation binding
	 */
	public static void bind(AnnotationBindingContext bindingContext) {
		Collection<AnnotationInstance> annotations = bindingContext.getIndex().getAnnotations( HibernateDotNames.TABLE );
		for ( AnnotationInstance tableAnnotation : annotations ) {
			bind( bindingContext.getMetadataImplementor(), tableAnnotation );
		}

		annotations = bindingContext.getIndex().getAnnotations( HibernateDotNames.TABLES );
		for ( AnnotationInstance tables : annotations ) {
			for ( AnnotationInstance table : JandexHelper.getValue( tables, "value", AnnotationInstance[].class ) ) {
				bind( bindingContext.getMetadataImplementor(), table );
			}
		}
	}

	private static void bind(MetadataImplementor metadata, AnnotationInstance tableAnnotation) {
		String tableName = JandexHelper.getValue( tableAnnotation, "appliesTo", String.class );
		ObjectName objectName = ObjectName.parse( tableName );
		Schema schema = metadata.getDatabase().getSchema( objectName.getCatalog(), objectName.getSchema() );
		Table table = schema.locateTable( objectName.getName() );
		if ( table != null ) {
			boolean isSecondaryTable = metadata.getSecondaryTables().containsKey( table.getLogicalName() );
			bindHibernateTableAnnotation( table, tableAnnotation,isSecondaryTable, metadata );
		}
		else {
			throw new MappingException( "Can't find table[" + tableName + "] from Annotation @Table" );
		}
	}

	private static void bindHibernateTableAnnotation(
			final Table table,
			final AnnotationInstance tableAnnotation,
			final boolean isSecondaryTable,
			final MetadataImplementor metadata) {
		for ( AnnotationInstance indexAnnotation : JandexHelper.getValue(
				tableAnnotation,
				"indexes",
				AnnotationInstance[].class
		) ) {
			bindIndexAnnotation( table, tableAnnotation, indexAnnotation );
		}
		String comment = JandexHelper.getValue( tableAnnotation, "comment", String.class );
		if ( StringHelper.isNotEmpty( comment ) ) {
			table.addComment( comment.trim() );
		}
		if ( !isSecondaryTable ) {
			return;
		}
		SecondaryTable secondaryTable = metadata.getSecondaryTables().get( table.getLogicalName() );
		if ( tableAnnotation.value( "fetch" ) != null ) {
			FetchMode fetchMode = JandexHelper.getEnumValue( tableAnnotation, "fetch", FetchMode.class );
			secondaryTable.setFetchStyle( EnumConversionHelper.annotationFetchModeToFetchStyle( fetchMode ) );
		}
		if ( tableAnnotation.value( "inverse" ) != null ) {
			secondaryTable.setInverse( tableAnnotation.value( "inverse" ).asBoolean() );
		}
		if ( tableAnnotation.value( "optional" ) != null ) {
			secondaryTable.setOptional( tableAnnotation.value( "optional" ).asBoolean() );
		}

		if ( tableAnnotation.value( "sqlInsert" ) != null ) {
			secondaryTable.setCustomInsert(
					AnnotationParserHelper.createCustomSQL(
							tableAnnotation.value( "sqlInsert" )
									.asNested()
					)
			);
		}
		if ( tableAnnotation.value( "sqlUpdate" ) != null ) {
			secondaryTable.setCustomUpdate(
					AnnotationParserHelper.createCustomSQL(
							tableAnnotation.value( "sqlUpdate" )
									.asNested()
					)
			);

		}
		if ( tableAnnotation.value( "sqlDelete" ) != null ) {
			secondaryTable.setCustomDelete(
					AnnotationParserHelper.createCustomSQL(
							tableAnnotation.value( "sqlDelete" )
									.asNested()
					)
			);
		}
		// TODO: ForeignKey is not binded right now, because constrint name is not modifyable after it is set
		// another option would be create something like tableDefinition and look up it when we bind table / secondary table

//		if ( tableAnnotation.value( "foreignKey" ) != null ) {
//			AnnotationInstance foreignKeyAnnotation = tableAnnotation.value( "foreignKey" ).asNested();
//			if ( foreignKeyAnnotation.value( "name" ) != null ) {
//				secondaryTable.getForeignKeyReference().setName( foreignKeyAnnotation.value( "name" ).asString() );
//			}
//		}


	}

	private static void bindIndexAnnotation(Table table, AnnotationInstance tableAnnotation, AnnotationInstance indexAnnotation) {
		String indexName = JandexHelper.getValue( indexAnnotation, "name", String.class );
		String[] columnNames = JandexHelper.getValue( indexAnnotation, "columnNames", String[].class );
		if ( columnNames == null ) {
			LOG.noColumnsSpecifiedForIndex( indexName, table.toLoggableString() );
			return;
		}
		Index index = table.getOrCreateIndex( indexName );
		for ( String columnName : columnNames ) {
			Column column = findColumn( table, columnName );
			if ( column == null ) {
				throw new AnnotationException( "@Index references a unknown column: " + columnName );
			}
			index.addColumn( column );
		}
	}

	private static Column findColumn(Table table, String columnName) {
		Column column = null;
		for ( Value value : table.values() ) {
			if ( Column.class.isInstance( value ) && Column.class.cast( value )
					.getColumnName()
					.getText()
					.equals( columnName ) ) {
				column = (Column) value;
				break;
			}
		}
		return column;
	}
}
