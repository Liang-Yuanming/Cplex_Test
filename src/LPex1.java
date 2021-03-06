import ilog.concert.*;
import ilog.cplex.*;

public class LPex1 {
	static void usage(){
		System.out.println("usage: LPex1 <option>");
		System.out.println("options:   -r build model row by row");
		System.out.println("options:   -c build model column by column");
		System.out.println("options:   -n build model nonzero by nonzero");
	}
	public static void main(String[] args){
		char a='r';
//		if(args.length!=1 || args[0].charAt(0) != '-'){
//			usage();
//			return;
//		}
		
		try{
			IloCplex cplex=new IloCplex();
			IloNumVar[][] var=new IloNumVar[1][];
			IloRange[][] rng=new IloRange[1][];

			switch(a){
			case 'r':
				populateByRow(cplex,var,rng);
				break;
			case 'c':
				populateByColumn(cplex,var,rng);
				break;
			case 'n':
				populateByNonzero(cplex, var, rng);
				break;
			default: 
				usage();
				return;
			}
			cplex.exportModel("lpex1.lp");
			if(cplex.solve()){
				double[] x=cplex.getValues(var[0]);
				double[] dj=cplex.getReducedCosts(var[0]);
				double[] pi= cplex.getDuals(rng[0]);
				double[] slack=cplex.getSlacks(rng[0]);
				
				System.out.println("Solution status = " + cplex.getStatus());
				System.out.println("Solution value = " + cplex.getObjValue());
				
				int ncols=cplex.getNcols();
				for(int j=0;j<ncols;++j){
					System.out.println("Column: " + j + " Value = "+ x[j] + " Reduced cost = " +dj[j]);
				}
				int nrows=cplex.getNrows();
				for(int i=0;i<nrows;++i){
					System.out.println("Row: " + i+ " Slack = " + slack[i] + " Pi = " + pi[i]);
				}
			}
			
			cplex.end();
		}catch(IloException e){
			e.printStackTrace();
		}
	}
	static void populateByRow(IloMPModeler model,IloNumVar[][] var,IloRange[][] rng)throws IloException{
		double[] lb={0.0,0.0,0.0};
		double[] ub={40,Double.MAX_VALUE,Double.MAX_VALUE};
		IloNumVar[] x=model.numVarArray(3, lb, ub);
		var[0]=x;
		
		double[] objvals={1.0,2.0,3.0};
		model.addMaximize(model.scalProd(x, objvals));
		
		rng[0]=new IloRange[2];
		rng[0][0]=model.addLe(model.sum(model.prod(-1.0, x[0]),model.prod(1.0, x[1]),model.prod(1.0,x[2])), 20.0);
		rng[0][1]=model.addLe(model.sum(model.prod(1.0, x[0]),model.prod(-3.0, x[1]),model.prod(1.0,x[2])), 30.0);
		
	}
	static void populateByColumn(IloMPModeler model,IloNumVar[][] var,IloRange[][] rng)throws IloException{
		IloObjective obj=model.addMaximize();
		rng[0]=new IloRange[2];
		rng[0][0]=model.addRange(-Double.MAX_VALUE, 20.0);
		rng[0][1]=model.addRange(-Double.MAX_VALUE, 3.0);
		
		IloRange r0=rng[0][0];
		IloRange r1=rng[0][1];
		
		var[0]=new IloNumVar[3];
		var[0][0]=model.numVar(model.column(obj,1.0).and(model.column(r0,-1.0).and(model.column(r1,1.0))),0.0, 40.0);
		var[0][1]=model.numVar(model.column(obj,2.0).and(model.column(r0,1.0).and(model.column(r1,-3.0))),0.0, Double.MAX_VALUE);
		var[0][2]=model.numVar(model.column(obj,3.0).and(model.column(r0,1.0).and(model.column(r1,1.0))),0.0, Double.MAX_VALUE);		
	}
	static void populateByNonzero(IloMPModeler model,IloNumVar[][] var,IloRange[][] rng)throws IloException{
		double[] lb={0.0,0.0,0.0};
		double[] ub={40.0,Double.MAX_VALUE,Double.MAX_VALUE};
		
		IloNumVar[] x=model.numVarArray(3, lb, ub);
		var[0]=x;
		
		double[] objvals={1.0,2.0,3.0};
		model.add(model.maximize(model.scalProd(x, objvals)));
		
		rng[0]=new IloRange[2];
		rng[0][0]=model.addRange(-Double.MAX_VALUE,20.0);
		rng[0][1]=model.addRange(-Double.MAX_VALUE,30.0);
		
		rng[0][0].setExpr(model.sum(model.prod(-1.0, x[0]),model.prod(1.0,x[1]),model.prod(1.0, x[2])));
		rng[0][1].setExpr(model.sum(model.prod(1.0, x[0]),model.prod(-3.0,x[1]),model.prod(1.0, x[2])));
		
	}
}
