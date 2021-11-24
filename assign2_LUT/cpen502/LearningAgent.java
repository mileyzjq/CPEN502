package ece.cpen502;

import java.util.ArrayList;

public class LearningAgent {
    public static final double LEARNING_RATE = 0.2;
    public static final double DISCOUNT_RATE = 0.8;
    public static double EXPLORE_RATE = 0.8;
    private int prevState = -1;
    private int prevAction = -1;
    private boolean firstRound = true;
    private LookUpTable table;
    public ArrayList<String> finalStates = new ArrayList<>();

    public LearningAgent(LookUpTable table) {
        this.table = table;
    }

    public void Learn(int currState, int currAction, double reward, boolean isOnPolicy, boolean isIntermidiateRewards) {
        if(!isIntermidiateRewards) {
            finalStates.add(currState+"-"+currAction);
            return;
        }
        double newValue;
        if(firstRound) {
            firstRound = false;
        } else {
            double oldValue = table.getQValue(prevState, prevAction);
            if(isOnPolicy) {
                newValue = oldValue + LEARNING_RATE * (reward + DISCOUNT_RATE * table.getQValue(currState, currAction)
                        -oldValue);
            } else {
                newValue = oldValue + LEARNING_RATE * (reward + DISCOUNT_RATE * table.getMaxValue(currState) - oldValue);
            }
            table.setQValue(prevState, prevAction, newValue);
        }
        prevState = currState;
        prevAction = currAction;
    }

    public int getNextAction(int state) {
        double random = Math.random();
        if(random < EXPLORE_RATE) {
            return (int)(Math.random() * Action.ROBOT_NUM_ACTIONS);
        }
        return table.getBestAction(state);
    }

    public void feedReward(double value) {
        int n = finalStates.size();
        double currValue, nextValue;
        String[] strs = finalStates.get(n-1).split("-");
        int state = Integer.valueOf(strs[0]);
        int action = Integer.valueOf(strs[1]);
        table.setQValue(state, action, value);
        nextValue = value;
        for(int i=n-2; i>=0; i--) {
            strs = finalStates.get(i).split("-");
            state = Integer.valueOf(strs[0]);
            action = Integer.valueOf(strs[1]);
            currValue = table.getQValue(state, action);
            currValue += LEARNING_RATE * (DISCOUNT_RATE * nextValue - currValue);
            table.setQValue(state, action, currValue);
            nextValue = currValue;
        }
    }
}
