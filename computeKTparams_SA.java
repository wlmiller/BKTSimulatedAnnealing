import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.StreamTokenizer;
import java.util.HashMap;
import java.util.Map;

public class computeKTparams_SA {
	 /** 
	 * This class expects data sorted on Skill and then on Student in the below mentioned format
	 * num		lesson					student			skill		   cell 	right	eol
	 *	1	Z3.Three-FactorZCros2008	student102	META-DETERMINE-DXO	cell	0	eol
	 * */

	public String students_[] = new String[27600];// Number of instances
	public String skill_[] = new String[27600];
	public double right_[] = new double[27600];
	public int skillends_[] = new int[15];//Number of Skills
	public int skillnum = -1;
	public final boolean lnminus1_estimation = false;
	public final boolean bounded = true;
	public final boolean L0Tbounded = false;
	
	public Map<String,Double> top = new HashMap<String, Double>();
	public final double stepSize = 0.05;
	public final double minVal = 0.000001;
	public final Integer totalSteps = 1000000;

	class BKTParams {
		public double L0, G, S, T;

		public BKTParams(Double init) {
			if (init < 0) {
				this.L0 = Math.random()*top.get("L0");
				this.G = Math.random()*top.get("G");
				this.S = Math.random()*top.get("S");
				this.T = Math.random()*top.get("T");
			} else {
				this.L0 = init;
				this.G = init;
				this.S = init;
				this.T = init;
			}
		}
		
		public BKTParams(BKTParams copy, Boolean randStep) {
			this.L0 = copy.L0;
			this.G = copy.G;
			this.S = copy.S;
			this.T = copy.T;
			
			if (randStep) {
				Double randomchange = Math.random();
				Double thisStep = 2.*(Math.random()-0.5)*stepSize;
					
				// Randomly change one of the BKT parameters.
				if ( randomchange <= 0.25 ) {
					this.L0 = Math.max(Math.min(this.L0 + thisStep,top.get("L0")),minVal);
				} else if ( randomchange <= 0.5 ) {
					this.T = Math.max(Math.min(this.T + thisStep,top.get("T")),minVal);
				} else if ( randomchange <= 0.75 ) {
					this.G = Math.max(Math.min(this.G + thisStep,top.get("G")),minVal);
				} else {
					this.S = Math.max(Math.min(this.S + thisStep,top.get("S")),minVal);
				}
			}
		}
	}
	
	public StreamTokenizer create_tokenizer(String infile) {
		try {
			StreamTokenizer st = new StreamTokenizer(new FileReader(infile));
			st.wordChars(95, 95);
			return st;
		} catch (FileNotFoundException fnfe) {
			fnfe.printStackTrace();
		}
		return null;
	}

	public void read_in_data(StreamTokenizer st_) {
		int actnum = 0;
		try {
			int tt = 724;
			skillnum = -1;
			String prevskill = "FLURG";

			tt = st_.nextToken();
			tt = st_.nextToken();
			tt = st_.nextToken();
			tt = st_.nextToken();
			tt = st_.nextToken();
			tt = st_.nextToken();
			tt = st_.nextToken();

			while (tt != StreamTokenizer.TT_EOF) {
				tt = st_.nextToken(); // num

				if (tt == StreamTokenizer.TT_EOF) {
					prevskill = skill_[actnum - 1];
					if (skillnum > -1)
						skillends_[skillnum] = actnum - 1;
					break;
				}

				tt = st_.nextToken(); // lesson

				tt = st_.nextToken();
				students_[actnum] = st_.sval;

				tt = st_.nextToken();
				skill_[actnum] = st_.sval;

				tt = st_.nextToken(); // cell

				tt = st_.nextToken();
				right_[actnum] = st_.nval;

				tt = st_.nextToken(); // eol

				actnum++;
				if (!skill_[actnum - 1].equals(prevskill)) {
					prevskill = skill_[actnum - 1];
					if (skillnum > -1)
						skillends_[skillnum] = actnum - 2;
					skillnum++;
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public double findGOOF(int start, int end, BKTParams params) {
		double SSR = 0.0;
		String prevstudent = "FWORPLEJOHN";		// A random student id.
		double prevL = 0.0;
		double prevLgivenresult = 0.0;
		double newL = 0.0;
		
		double likelihoodcorrect = 0.0;
		
		Integer count = 0;

		for (int i = start; i <= end; i++) {

			if (!students_[i].equals(prevstudent)) {
				prevL = params.L0;
				prevstudent = students_[i];
			}

			if (lnminus1_estimation)
				likelihoodcorrect = prevL;
			else
				likelihoodcorrect = (prevL * (1.0 - params.S)) + ((1.0 - prevL) * params.G);
			if ( right_[i] != -1.0 ) {
				SSR += (right_[i] - likelihoodcorrect) * (right_[i] - likelihoodcorrect);
				count++;
			}

			if ( right_[i] == -1.0 ) {
				prevLgivenresult = prevL;
			} else {
				prevLgivenresult = right_[i]*((prevL * (1.0 - params.S)) / ((prevL * (1 - params.S)) + ((1.0 - prevL) * (params.G))));
				prevLgivenresult += (1-right_[i])*((prevL * params.S) / ((prevL * params.S) + ((1.0 - prevL) * (1.0 - params.G))));
			}

			newL = prevLgivenresult + (1.0 - prevLgivenresult) * params.T;
			prevL = newL;
		}
		if ( count == 0 ) return 0;
		return Math.sqrt(SSR/count);		// Using the RMSE instead of the SSR
	}

	public void fit_skill_model(int curskill) {
		if (L0Tbounded) {
			top.put("L0",0.85);
			top.put("T", 0.3);
		} else {
			top.put("L0",0.999999);
			top.put("T",0.999999);
		}
		
		if (bounded) {
			top.put("G", 0.3);
			top.put("S", 0.1);
		} else {
			top.put("G",0.999999);
			top.put("S",0.999999);
		}

		// oldParams is randomized.
		BKTParams oldParams = new BKTParams(-1.);
		BKTParams bestParams = new BKTParams(0.01);
		
		double oldRMSE = 1.;
		double newRMSE = 1.;
		
		double bestRMSE = 9999999.0;
		double prevBestRMSE = 9999999.0;
		
		double temp = 0.005;
		
		int startact = 0;
		if (curskill > 0)
			startact = skillends_[curskill - 1] + 1;
		int endact = skillends_[curskill];
		
		// Get the initial RMSE.
		oldRMSE = findGOOF(startact, endact, oldParams);
	
		for ( Integer i = 0; i < totalSteps; i++ ) {
			// Take a random step.
			BKTParams newParams = new BKTParams(oldParams, true);
				
			newRMSE = findGOOF(startact, endact, newParams);
			
			if ( Math.random() <= Math.exp((oldRMSE-newRMSE)/temp) ) {	// Accept (otherwise move is rejected)
				oldParams = new BKTParams(newParams, false);
				oldRMSE = newRMSE;
			}
			
			if ( newRMSE < bestRMSE ) {							// This method allows the RMSE to increase, but we're interested 
				bestParams = new BKTParams(newParams, false);	// in the global minimum, so save the minimum values as the "best."
				bestRMSE = newRMSE;
			}
				
			if ( i % 10000 == 0 && i > 0 ) {			// Every 10,000 steps, decrease the "temperature."
				if ( bestRMSE == prevBestRMSE ) break;	// If the best estimate didn't change, we're done.
				
				prevBestRMSE = bestRMSE;
				temp = temp/2.0;
			}
					
		}
		
		System.out.print(skill_[startact]);
		System.out.print("\t");
		System.out.print(bestParams.L0);
		System.out.print("\t");
		System.out.print(bestParams.G);
		System.out.print("\t");
		System.out.print(bestParams.S);
		System.out.print("\t");
		System.out.print(bestParams.T);
		System.out.print("\t");
		System.out.print(bestRMSE);
		System.out.println("\teol");
	}

	public void computelzerot(String infile_) {
		StreamTokenizer st_ = create_tokenizer(infile_);
		if (st_ != null) {
			read_in_data(st_);
	
			System.out.println("skill\tL0\tG\tS\tT\tRMSE\teol");
			for (int curskill = 0; curskill <= skillnum; curskill++) {
				fit_skill_model(curskill);
			}
		}
	}

	public static void main(String args[]) {
		if (args.length < 1) {
			System.err.println("Please specify the location of the log file.");
		} else {
			String infile_ = args[0];//Needs to be tab delimited
			computeKTparams_SA m = new computeKTparams_SA();
			m.computelzerot(infile_);
		}
	}

}