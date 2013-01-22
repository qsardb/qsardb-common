/*
 * Copyright (c) 2011 University of Tartu
 */
package org.qsardb.conversion.table;

import java.io.*;
import java.util.*;

import org.qsardb.model.*;

public class Table2Qdb {

	static
	public void convert(Qdb qdb, Table table, TableSetup setup) throws Exception {
		Collection<Mapping> mappings = new LinkedHashSet<Mapping>();

		Column idColumn = null;

		CompoundIdMapping idMapping = null;

		Iterator<Column> columns = table.columns();

		try {
			while(columns.hasNext()){
				Column column = columns.next();

				List<Mapping> columnMappings = setup.getMappings(column.getId());
				if(columnMappings != null && columnMappings.size() > 0){
					mappings.addAll(columnMappings);

					for(Mapping columnMapping : columnMappings){

						if((idColumn == null && idMapping == null) && (columnMapping instanceof CompoundIdMapping)){
							idColumn = column;

							idMapping = (CompoundIdMapping)columnMapping;
						}
					}
				}
			}
		} finally {
			close(columns);
		}

		CompoundRegistry compounds = qdb.getCompoundRegistry();

		for(Mapping mapping : mappings){
			mapping.beginMapping();
		}

		Iterator<Row> rows = table.rows();

		try {
			int index = setup.getOffset();
			if(index < 0){
				index = compounds.size();
			}

			String beginId = setup.getBeginRow();

			String endId = setup.getEndRow();

			while(rows.hasNext()){
				Row row = rows.next();

				String rowId = row.getId();

				if(beginId != null && !beginId.equals(rowId)){
					continue;
				} else

				if(beginId != null){
					beginId = null;
				} // End if

				if(setup.isIgnored(rowId)){
					continue;
				}

				Map<Column, Cell> values = row.getValues();

				Compound compound = null;

				Cell idCell = null;

				if(idColumn != null){
					idCell = values.get(idColumn);
				}

				String id = null;

				if((idColumn != null && idMapping != null && idCell != null)){
					id = idMapping.filter(idCell.getText());
				} // End if

				if(id != null){
					compound = compounds.get(id);

					if(compound == null){
						compound = new Compound(id);

						compounds.add(compound);
					}
				} else

				{
					id = String.valueOf(index + 1);

					index++;

					compound = compounds.get(id);

					if(compound == null){
						compound = new Compound(id);

						compounds.add(compound);
					}
				} // End if

				if(setup.getCopyLabels()){
					(compound.getLabels()).addAll(row.getLabels());
				}

				for(Map.Entry<Column, Cell> entry : values.entrySet()){
					Column column = entry.getKey();
					Cell cell = entry.getValue();

					List<Mapping> columnMappings = setup.getMappings(column.getId());
					if(columnMappings != null && columnMappings.size() > 0){

						for(Mapping columnMapping : columnMappings){

							if((idColumn != null && idMapping != null) && idMapping.equals(columnMapping)){
								continue;
							}

							columnMapping.mapValue(compound, cell.getText());
						}
					}
				}

				if(endId != null && endId.equals(rowId)){
					endId = null;

					break;
				}
			}
		} finally {
			close(rows);
		}

		for(Mapping mapping : mappings){
			mapping.endMapping();
		}
	}

	static
	public void convert(Qdb qdb, Collection<? extends Table> tables, TableSetup setup) throws Exception {

		for(Table table : tables){
			convert(qdb, table, setup);
		}
	}

	static
	private void close(Object object) throws IOException {

		if(object instanceof Closeable){
			Closeable closeable = (Closeable)object;

			closeable.close();
		}
	}
}