import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;

public class GA {	
	final public static int GENOM_LENGTH = 100; //遺伝子の長さ
	final public static int GENOM_NUM = 100; //遺伝子の数
	final public static int OFFSPRING_NUM = 50; //子孫の数
	final public static double INDIVUDUAL_MUTATION_PROBABILITY = 0.1; //個体突然変異確率
	final public static double GENOM_MUTATION_PROBABILITY = 0.1; //遺伝子突然変異確率
	final public static int MAX_GENERATION = 40; //繰り返す世代数
	
	Random rand = new Random();

	//1個のランダムな遺伝子を生成
	public Genom createGenom(){
		ArrayList<Boolean> genom = new ArrayList<>();

		for(int i = 0; i < GENOM_LENGTH; i++){
			genom.add(rand.nextBoolean());
		}
		
		return (new Genom(genom, 0.0));
	}
	
	//評価関数
	// TODO ラムダ式を渡せるように
	public double evaluation(Genom g){
		int sum = 0;
		double result = 0.0;
		
		for(int i = 0; i < GENOM_LENGTH; i++){
			if(g.getGenom().get(i)) sum++;
		}
		result = sum / (double)GENOM_LENGTH;
		//System.out.println("sum :" + sum + ", " + "result : " + result);
		
		return result;
	}

	public double negotiation(Genom g){
		double result = 0.0;	//評価値
		Pair<Character, Integer> p = new Pair<>();
		ArrayList<Pair<Character, Integer>> selfishAgentPref = new ArrayList<>();
		ArrayList<Pair<Character, Integer>> misrepresentingAgentPref = new ArrayList<>();
		MisrepresentationGame mg = new MisrepresentationGame();
		
		// エージェントの選好を設定
		for(int i = 0; i < MisrepresentationGame.ISSUE.size(); i++){
			p.setBoth(MisrepresentationGame.ISSUE.get(i), i+1);
			selfishAgentPref.add(p);
//			System.out.println("agent1: " + selfishAgentPref.get(i).getLeft() + ", " + selfishAgentPref.get(i).getRight());
//			System.out.println("agent1: " + p.getLeft() + ", " + p.getRight());
			p.setBoth(MisrepresentationGame.ISSUE.get(i), MisrepresentationGame.ISSUE.size()-i);
			misrepresentingAgentPref.add(p);
//			System.out.println("agent2: " + misrepresentingAgentPref.get(i).getLeft() + ", " + misrepresentingAgentPref.get(i).getRight());
//			System.out.println("agent2: " + p.getLeft() + ", " + p.getRight());
//			System.out.println(misrepresentingAgentPref);
		}
//		System.out.println(selfishAgentPref.size());
//		for(int i = 0; i < selfishAgentPref.size(); i++){
//			System.out.println(MisrepresentationGame.ISSUE.get(i));
//			System.out.println("agent1: " + selfishAgentPref.get(i).getLeft() + ", " + selfishAgentPref.get(i).getRight());
//			System.out.println("agent2: " + misrepresentingAgentPref.get(i).getLeft() + ", " + misrepresentingAgentPref.get(i).getRight());
//		}
		
		// エージェント生成
		SelfishAgent sAgent = new SelfishAgent(selfishAgentPref);
		MisrepresentingAgent mAgent = new MisrepresentingAgent(misrepresentingAgentPref);

//		System.out.println(sAgent.revealPreference());
//		System.out.println(mAgent.revealPreference());
		
		mg.preferenceElicitation(sAgent, mAgent, g);
		mg.deal(sAgent, mAgent, g);

		result = (factorial(MisrepresentationGame.ISSUE.size()) - Math.abs(mAgent.getFakeUtility() - mAgent.getUtility())) / factorial(MisrepresentationGame.ISSUE.size());

		return result;
	}

	public int factorial(int x){
		if(x > 0){
			return x + factorial(x-1);
		}else{
			return 0;
		}
	}
	
	//選択関数(エリート主義)
	/*
	public ArrayList<Genom> selection(ArrayList<Genom> genomGroup){
		ArrayList<Genom> elite = new ArrayList<>();
		
		Collections.sort(genomGroup, new Comparator<Genom>() {
			public int compare(Genom genom1, Genom genom2){
				return Double.compare(genom1.getEvaluation(), genom2.getEvaluation());
			}
		});
		Collections.reverse(genomGroup);
		
		for(int i = 0; i < selectGenom; i++){
			elite.add(genomGroup.get(i));
		}
		
		return elite;
	}
	*/
	
	public ArrayList<Genom> stochasticUniversalSampling(ArrayList<Genom> parents){
		ArrayList<Genom> matingPool = new ArrayList<>();		

		double sum = 0.0;
		double[] cpdist = new double[parents.size()];
		/* cumulative probability distributionの計算 */
		for(int j = 0; j < parents.size(); j++){
			//System.out.println("parents: " + Main.encodeGenom(parents.get(j).getGenom()) + "  eval: " + parents.get(j).getEvaluation());
			sum += parents.get(j).getEvaluation();
			//System.out.println(sum);
			cpdist[j] = sum;
		}
		for(int j = 0; j < parents.size(); j++){
			cpdist[j] /= sum;
		}
		//System.out.println(parents.size());
		//System.out.println(cpdist[99]);
		
		/* SUS */
		double r = rand.nextDouble();
		r /= OFFSPRING_NUM;
	
		int currentMember = 0;
		int i = 0;
		while(currentMember < OFFSPRING_NUM){
			while(r <= cpdist[i] && currentMember < OFFSPRING_NUM){
				matingPool.add(parents.get(i));
				r = r+(1/(double)OFFSPRING_NUM);
				currentMember++;
			}
			i++;
		}
		
		Collections.sort(matingPool, new Comparator<Genom>() {
			public int compare(Genom genom1, Genom genom2){
				return Double.compare(genom1.getEvaluation(), genom2.getEvaluation());
			}
		});
		Collections.reverse(matingPool);
	
		//for(int j = 0; j < matingPool.size(); j++){
			//System.out.println("childs: " + Main.encodeGenom(matingPool.get(j).getGenom()) + "  eval: " + matingPool.get(j).getEvaluation());
		//}
		return matingPool;
	}
	
	//交叉関数
	public ArrayList<Genom> crossover(Genom father, Genom mother){
		ArrayList<Genom> genomList = new ArrayList<>();
		ArrayList<Boolean> olderBrother = new ArrayList<>();
		ArrayList<Boolean> yongerBrother = new ArrayList<>();
		// 交叉する2点を決定
		int crossFirst = rand.nextInt(GENOM_LENGTH);
		int crossSecond = rand.nextInt(GENOM_LENGTH-crossFirst) + crossFirst;
		// 親の遺伝子を取り出す
		ArrayList<Boolean> fatherGenom = father.getGenom();
		ArrayList<Boolean> motherGenom = mother.getGenom();
		// 交叉
		for(int i = 0; i < GENOM_LENGTH; i++){
			if(i < crossFirst || i >= crossSecond){
				olderBrother.add(i, fatherGenom.get(i));
				yongerBrother.add(i, motherGenom.get(i));
			}else if(i < crossSecond){
				olderBrother.add(i, motherGenom.get(i));
				yongerBrother.add(i, fatherGenom.get(i));							
			}
		}
		//リストに格納
		genomList.add(new Genom(olderBrother, 0));
		genomList.add(new Genom(yongerBrother, 0));
		
		return genomList;
	}
	
	//世代交代処理
	public ArrayList<Genom> nextGenerationGeneCreate(ArrayList<Genom> genomGroup, ArrayList<Genom> progeny){
		ArrayList<Genom> nextGenerationGenom = new ArrayList<>();
		
		//現行世代個体集団を評価が低い順番にソート
		Collections.sort(genomGroup, new Comparator<Genom>() {
			public int compare(Genom genom1, Genom genom2){
				return Double.compare(genom1.getEvaluation(), genom2.getEvaluation());
			}
		});
		Collections.reverse(genomGroup);
		for(int i = 0; i < genomGroup.size()-progeny.size(); i++){
			nextGenerationGenom.add(genomGroup.get(i));
		}
		//エリート集団と子孫集団を次世代集団に追加
		nextGenerationGenom.addAll(progeny);
		
		return nextGenerationGenom;
	}
	
	//突然変異
	public ArrayList<Genom> mutation(ArrayList<Genom> genomGroup){
		ArrayList<Genom> mutated = new ArrayList<>(); //突然変異後の遺伝子集団
		
		for(int i = 0; i < genomGroup.size(); i++){
			if(INDIVUDUAL_MUTATION_PROBABILITY > rand.nextDouble()){
				ArrayList<Boolean> tmpGeno = new ArrayList<>(); //変異させる遺伝子を入れる
				for(int j = 0; j < GENOM_LENGTH; j++){
					if(GENOM_MUTATION_PROBABILITY > rand.nextDouble()){
						tmpGeno.add(rand.nextBoolean());
					}else{
						tmpGeno.add(genomGroup.get(i).getGenom().get(j));
					}
				}				
				mutated.add(new Genom(tmpGeno, 0.0));
			}else{
				mutated.add(genomGroup.get(i));
			}
		}
		
		return mutated;
	}
}
