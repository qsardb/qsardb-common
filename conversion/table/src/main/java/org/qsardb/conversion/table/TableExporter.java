/*
 * Copyright (c) 2013 University of Tartu
 */
package org.qsardb.conversion.table;

import java.io.Closeable;
import java.io.IOException;
import java.util.*;
import org.qsardb.cargo.map.ValuesCargo;
import org.qsardb.evaluation.Evaluator;
import org.qsardb.evaluation.EvaluatorFactory;
import org.qsardb.model.*;

public abstract class TableExporter extends Table implements Closeable {

	private final Set<Column> columns = new LinkedHashSet<Column>();
	private final Set<Row> rows = new LinkedHashSet<Row>();

	public abstract void write() throws Exception;

	@Override
	public abstract void close() throws IOException;

	public void prepareDataSet(Qdb qdb) {
		addCompounds(qdb.getCompoundRegistry());
		loadParameters(qdb.getPropertyRegistry());
		loadParameters(qdb.getDescriptorRegistry());
	}

	public void prepareModel(Model model) {
		PredictionRegistry predRegistry = model.getQdb().getPredictionRegistry();
		Collection<Prediction> predictions = predRegistry.getByModel(model);
		prepareModelPredictions(model, predictions);
	}

	public void preparePrediction(Prediction prediction) {
		Model model = prediction.getModel();
		prepareModelPredictions(model, Collections.singleton(prediction));
	}

	private void prepareModelPredictions(Model model, Collection<Prediction> predictions) {
		Qdb qdb = model.getQdb();
		for (Prediction p: predictions) {
			Set<String> ids = loadValues(p).keySet();
			addCompounds(qdb.getCompoundRegistry().getAll(ids));
		}

		loadParameters(Arrays.asList(model.getProperty()));
		loadParameters(predictions);

		try {
			Evaluator eval = EvaluatorFactory.getInstance().getEvaluator(model);
			eval.init();
			try {
				loadParameters(eval.getDescriptors());
			} finally {
				eval.destroy();
			}
		} catch (Exception e) {
			throw new RuntimeException("Loading descriptors failed for: "+model.getId(), e);
		}
	}

	@Override
	public Iterator<Column> columns() throws Exception {
		return columns.iterator();
	}

	@Override
	public Iterator<Row> rows() throws Exception {
		return rows.iterator();
	}

	private void addCompounds(Collection<Compound> compounds) {
		HashMap<Compound, String> smiles = loadSmiles(compounds);
		boolean haveSmilesColumn = !smiles.isEmpty();

		for (Compound c: compounds) {
			Map<Column, Cell> values = new LinkedHashMap<Column, Cell>();
			addAttribute(c.getId(), ID_COLUMN, values);
			addAttribute(c.getName(), NAME_COLUMN, values);
			addAttribute(c.getCas(), CAS_COLUMN, values);
			addAttribute(c.getInChI(), INCHI_COLUMN, values);

			if (haveSmilesColumn) {
				addAttribute(smiles.get(c), SMILES_COLUMN, values);
			}

			rows.add(new Row(c.getId(), values));
		}
	}

	private void addAttribute(String value, Column column, Map<Column, Cell> values) {
		if (!columns.contains(column)) {
			columns.add(column);
		}
		values.put(column, makeCell(value));
	}

	private Cell makeCell(String value) {
		return new Cell(value != null ? value : "");
	}

	private void loadParameters(Collection<? extends Parameter> params) {
		for (Parameter<?, ?> p: params) {
			Column column = new Column(p.getId());
			columns.add(column);
			Map<String, String> values = loadValues(p);
			for (Row row: rows) {
				String v = values.get(row.getId());
				row.getValues().put(column, makeCell(v));
			}
		}
	}

	private Map<String, String> loadValues(Parameter<?,?> p) {
		try {
			return p.getCargo(ValuesCargo.class).loadStringMap();
		} catch (IOException e) {
			return new HashMap<String, String>();
		}
	}

	private HashMap<Compound, String> loadSmiles(Collection<Compound> compounds) {
		HashMap<Compound, String> smiles = new HashMap<Compound, String>();
		for (Compound c: compounds) {
			for (String cargoId : c.getCargos()) {
				if (cargoId.endsWith("smiles")) {
					Cargo<Compound> cargo = c.getCargo(cargoId);
					try {
						smiles.put(c, cargo.loadString());
					} catch (IOException ex) {
						throw new RuntimeException("Can't load "+cargoId+" for "+c.getId(), ex);
					}
				}
			}
		}

		return smiles;
	}

	private static final Column ID_COLUMN = new Column("ID");
	private static final Column NAME_COLUMN = new Column("Name");
	private static final Column CAS_COLUMN = new Column("CAS");
	private static final Column INCHI_COLUMN = new Column("InChi");
	private static final Column SMILES_COLUMN = new Column("SMILES");
}