package ece.cpen502;

public class LookUpTable {
    private double table[][];

    public LookUpTable() {
        this.table = new double[State.NumStates][Action.ROBOT_NUM_ACTIONS];
        initializeLUT();
    }

    public void initializeLUT() {
        for(int i=0; i<State.NumStates; i++) {
            for(int j=0; j<Action.ROBOT_NUM_ACTIONS; j++) {
                table[i][j] = 0;
            }
        }
    }

    public double getQValue(int state, int action) {
        return table[state][action];
    }

    public void setQValue(int state, int action, double value) {
        this.table[state][action] = value;
    }

    public double getMaxValue(int state) {
        double maxValue = -10;
        for(int i=0; i<Action.ROBOT_NUM_ACTIONS; i++) {
            maxValue = Math.max(table[state][i], maxValue);
        }
        return maxValue;
    }

    public int getBestAction(int state) {
        double maxValue = -10;
        int action = 0;
        for(int i=0; i<Action.ROBOT_NUM_ACTIONS; i++) {
            if(table[state][i] > maxValue) {
                maxValue = table[state][i];
                action = i;
            }
        }
        return action;
    }
}
