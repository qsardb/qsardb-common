/*
 * Copyright (c) 2014 University of Tartu
 */
package org.qsardb.statistics;

import java.util.Map;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math.stat.regression.SimpleRegression;
import org.qsardb.cargo.map.DoubleFormat;
import org.qsardb.model.Model;
import org.qsardb.model.Prediction;

public class RegressionStatistics implements Statistics {

	private final int size;
	private final double r2;
	private final double stdev;

	public RegressionStatistics(Model model, Prediction prediction) {
		if (!StatisticsUtil.isRegression(model)) {
			throw new IllegalArgumentException("Expected regression model: "+model.getId());
		}

		Map<String, String> predicted = StatisticsUtil.loadValues(prediction);
		size = predicted.size();

		Map<String, String> experimental = StatisticsUtil.loadValues(model.getProperty());

		SimpleRegression regression = new SimpleRegression();
		DescriptiveStatistics statistic = new DescriptiveStatistics();

		DoubleFormat fmt = new DoubleFormat();
		for (String cid: predicted.keySet()){
			try {
				if (experimental.get(cid) == null) {
					continue;
				}

				double ev = fmt.parse(experimental.get(cid));
				double pv = fmt.parse(predicted.get(cid));

				if (Double.isNaN(ev) || Double.isNaN(pv)) {
					continue;
				}

				regression.addData(ev, pv);
				statistic.addValue(ev-pv);
			} catch (NumberFormatException e){
				// skip
			}
		}

		r2 = regression.getRSquare();
		stdev = statistic.getStandardDeviation();
	}

	@Override
	public int size() {
		return size;
	}

	public double rsq() {
		return r2;
	}

	public double stdev() {
		return stdev;
	}
	
}
