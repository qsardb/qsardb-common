/*
 * Copyright (c) 2012 University of Tartu
 */
package org.qsardb.evaluation;

import org.qsardb.cargo.pmml.*;
import org.qsardb.cargo.rds.*;
import org.qsardb.model.*;

public class EvaluatorFactory {

	private boolean activating = false;


	protected EvaluatorFactory(){
	}

	public Evaluator getEvaluator(Model model) throws Exception {
		Qdb qdb = model.getQdb();

		if(model.hasCargo(PMMLCargo.class)){
			PMMLCargo pmmlCargo = model.getCargo(PMMLCargo.class);

			return new PMMLEvaluator(qdb, pmmlCargo.loadPmml());
		} else

		if(model.hasCargo(RDSCargo.class)){
			RDSCargo rdsCargo = model.getCargo(RDSCargo.class);

			if(isActivating() && Context.getEngine() == null){
				Context.startEngine();
			}

			return new RDSEvaluator(qdb, rdsCargo.loadRdsObject());
		}

		throw new IllegalArgumentException();
	}

	public boolean isActivating(){
		return this.activating;
	}

	public void setActivating(boolean activating){
		this.activating = activating;
	}

	static
	public EvaluatorFactory getInstance(){
		return new EvaluatorFactory();
	}
}