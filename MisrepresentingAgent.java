import java.util.ArrayList;

public class MisrepresentingAgent extends SelfishAgent{
	private int fakeUtility = 0;

	public MisrepresentingAgent(ArrayList<Pair<Character, Integer>> preference) {
		super(preference);
		fakeUtility = 0;
	}
	
	public int getFakeUtility(){
		return this.fakeUtility;
	}
	
	public void setFakeUtility(int utility){
		fakeUtility = utility;
	}
	
	public int calculateFakeUtility(ArrayList<Character> issue, Agent opponentAgent){
		int result = 0;
		
		for(int i = 0; i < issue.size(); i++){
			for(int j = 0; j < opponentAgent.getPreference().size(); j++){
				if(issue.get(i) == opponentAgent.getPreference().get(j).getLeft()){
					result += opponentAgent.getPreference().get(j).getRight();
				}
			}
		}
		
		return result;
	}
	
	public Pair<Character, Character> compareIssue(char issue1, char issue2, Agent opponentAgent){
		return opponentAgent.compareIssue(issue1, issue2);
	}
	
	public char compareIssue(int x, Agent opponentAgent){
		return opponentAgent.compareIssue(x);
	}
}
