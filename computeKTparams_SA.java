import java.io.*;

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
	public boolean lnminus1_estimation = false;
	public boolean bounded = true;
	public boolean L0Tbounded = false;

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

				// System.out.println(slip_[actnum]);

				actnum++;
				if (!skill_[actnum - 1].equals(prevskill)) {
					prevskill = skill_[actnum - 1];
					if (skillnum > -1)
						skillends_[skillnum] = actnum - 2;
					skillnum++;
				}

			}
		} catch (Exception e) {
			System.out.println(actnum);
			e.printStackTrace();
		}

	}

	public double findGOOF(int start, int end, double Lzero, double trans,
			double G, double S) {
		double SSR = 0.0;
		String prevstudent = "FWORPLEJOHN";
		double prevL = 0.0;
		double likelihoodcorrect = 0.0;
		double prevLgivenresult = 0.0;
		double newL = 0.0;

		for (int i = start; i <= end; i++) {
			// System.out.println(SSR);
			if (!students_[i].equals(prevstudent)) {
				prevL = Lzero;
				prevstudent = students_[i];
			}

			if (lnminus1_estimation)
				likelihoodcorrect = prevL;
			else
				likelihoodcorrect = (prevL * (1.0 - S)) + ((1.0 - prevL) * G);
			SSR += (right_[i] - likelihoodcorrect) * (right_[i] - likelihoodcorrect);

			if (right_[i] == 1.0)
				prevLgivenresult = ((prevL * (1.0 - S)) / ((prevL * (1 - S)) + ((1.0 - prevL) * (G))));
			else
				prevLgivenresult = ((prevL * (S)) / ((prevL * (S)) + ((1.0 - prevL) * (1.0 - G))));

			newL = prevLgivenresult + (1.0 - prevLgivenresult) * trans;
			prevL = newL;
		}
		return SSR;
	}

	public void fit_skill_model(int curskill) {
		double SSR = 0.0;
		double BestSSR = 9999999.0;
		double bestLzero = 0.01;
		double besttrans = 0.01;
		double bestG = 0.01;
		double bestS = 0.01;
		double topG = 0.99;
		double topS = 0.99;
		double topL0 = 0.99;
		double topT = 0.99;
		if (L0Tbounded) {
			topL0 = 0.85;
			topT = 0.3;
		}
		if (bounded) {
			topG = 0.3;
			topS = 0.1;
		}

		int startact = 0;
		if (curskill > 0)
			startact = skillends_[curskill - 1] + 1;
		int endact = skillends_[curskill];

		// System.out.print(students_[startact]);
		// System.out.print(" ");
		// System.out.println(students_[endact]);

		for (double Lzero = 0.01; Lzero <= topL0; Lzero = Lzero + 0.01)
			for (double trans = 0.01; trans <= topT; trans = trans + 0.01) {
				for (double G = 0.01; G <= topG; G = G + 0.01) {
					for (double S = 0.01; S <= topS; S = S + 0.01) {
						SSR = findGOOF(startact, endact, Lzero, trans, G, S);
						/**
						 * System.out.print(Lzero); System.out.print("\t");
						 * System.out.println(trans);
						 */
						if (SSR < BestSSR) {
							BestSSR = SSR;
							bestLzero = Lzero;
							besttrans = trans;
							bestG = G;
							bestS = S;
						}
					}
				}
			}

		// for a bit mroe precision
		double startLzero = bestLzero;
		double starttrans = besttrans;
		double startG = bestG;
		double startS = bestS;
		for (double Lzero = startLzero - 0.009; ((Lzero <= startLzero + 0.009) && (Lzero <= topL0)); Lzero = Lzero + 0.001)
			for (double G = startG - 0.009; ((G <= startG + 0.009) && (G <= topG)); G = G + 0.001) {
				for (double S = startS - 0.009; ((S <= startS + 0.009) && (S <= topS)); S = S + 0.001) {
					for (double trans = starttrans - 0.009; ((trans <= starttrans + 0.009) && (trans < topT)); trans = trans + 0.001) {
						SSR = findGOOF(startact, endact, Lzero, trans, G, S);
						if (SSR < BestSSR) {
							BestSSR = SSR;
							bestLzero = Lzero;
							besttrans = trans;
							bestG = G;
							bestS = S;
						}
					}
				}
			}

		System.out.print(skill_[startact]);
		System.out.print("\t");
		System.out.print(bestLzero);
		System.out.print("\t");
		System.out.print(bestG);
		System.out.print("\t");
		System.out.print(bestS);
		System.out.print("\t");
		System.out.print(besttrans);
		System.out.println("\teol");
	}

	public void computelzerot(String infile_) {
		StreamTokenizer st_ = create_tokenizer(infile_);

		read_in_data(st_);

		System.out.println("skill\tL0\tG\tS\tT\teol");
		for (int curskill = 0; curskill <= skillnum; curskill++) {
			fit_skill_model(curskill);
		}
	}

	public static void main(String args[]) {
		String infile_ = "./TestData.txt";//Needs to be tab delimited
		computeKTparams_SA m = new computeKTparams_SA();
		m.computelzerot(infile_);
	}

}