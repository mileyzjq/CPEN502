package ece.assign3;

import robocode.*;

import java.awt.*;
import java.awt.geom.Point2D;
import java.io.File;

public class NNRobot extends AdvancedRobot {
    private static final boolean RECORD_MEMORY = false;
    private static final double BASE_DISTANCE = 400.0;
    private static final double IMMEDIATE_REWARD = 5;
    private static final double TERMINAL_REWARD = 15;
    private static final double TERMINAL_FINE = -10;
    private static final double IMMEDIATE_FINE = -4;
    private static final double ENEMY_DEATH_REWARD = 6;
    private static final int NUM_INPUT_LAYERS = 5;
    private static final int NUM_HIDDEN_LAYERS = 10;
    private static final int NUM_OUTPUT_LAYERS = 1;
    private static final double NN_LEARNING_RATE = 0.2;
    private static final double NN_MOMENTUM = 0.9;
    private static final double NN_ALPHA = 0.1;
    private static final double NN_GAMMA = 0.5;
    private static final double NN_EXPLORATION_RATE = 0.0;
    private static final int PERIOD = 200;
    private static final int MEMORY_N = 10;
    private Enemy enemy;
    private double reward = 0.0;
    private double firePower = 1;
    private int isHitByBullet = 0;
    private int isHitWall = 0;
    int action;
    public static double enemyBearing;
    public static NeuralNet[] nn = new NeuralNet[Action.ROBOT_NUM_ACTIONS];
    public static ReplayMemory<Experience> memory = new ReplayMemory<>(MEMORY_N);
    private double[] NNPrevState = new double[5];
    private int NNPrevAction = 0;
    private static int numRounds = 0;
    private static int winRounds = 0;
    private static String scoreListFile = "scoreList.txt";;
    private File weightFiles[] = new File[Action.ROBOT_NUM_ACTIONS];
    private double RMS_Error = 0.0d;
    private double totalError = 0.0d;

    public void run() {
        setUpNeuralNets();
//        if(numRounds > 0) {
//            loadNN_Weights();
//        }
        loadNN_Weights();
        enemy = new Enemy("enemy");
        enemy.distance = 10000;

        setAllColors(Color.red);
        setAdjustGunForRobotTurn(true);
        setAdjustRadarForGunTurn(true);

        while(true) {
            radarMovement();
            firePower = BASE_DISTANCE / enemy.distance;
            firePower = Math.min(3, firePower);
            gunMovement();
            robotMovement();
            if (getGunHeat() == 0) {
                setFire(firePower);
            }
            execute();
        }
    }

    public void setUpNeuralNets(){
        for(int i = 0; i< Action.ROBOT_NUM_ACTIONS; i++){
            nn[i]=new NeuralNet(NUM_INPUT_LAYERS, NUM_HIDDEN_LAYERS, NUM_OUTPUT_LAYERS, NN_LEARNING_RATE, NN_MOMENTUM);
        }
    }

    public int getMaxAction(double[] currState) {
        int nextAction = 0;
        for(int i=0; i<Action.ROBOT_NUM_ACTIONS; i++) {
            if(nn[i].outputFor(currState) > nn[nextAction].outputFor(currState)) {
                nextAction = i;
            }
        }
        return nextAction;
    }

    public int getNNAction(double heading, double distance, double bearing, double reward) {
        double[] NNCurrStates = new double[5];
        NNCurrStates[0] = heading % 180;
        NNCurrStates[1] = distance % 10000;
        NNCurrStates[2] = bearing % Math.PI;
        NNCurrStates[3] = isHitByBullet;
        NNCurrStates[4] = isHitWall;
        int nextAction = getMaxAction(NNCurrStates);

        double NN_NewVal = nn[nextAction].outputFor(NNCurrStates);
        double NN_PrevVal = nn[NNPrevAction].outputFor(NNPrevState);
        double error = NN_ALPHA * (reward + NN_GAMMA * NN_NewVal - NN_PrevVal);
        RMS_Error += error*error;
        //totalError += error*error/2;

        double NN_CorrectPrevVal = NN_PrevVal + error;
        nn[NNPrevAction].train(NNPrevState, NN_CorrectPrevVal);
        if(RECORD_MEMORY) {
            Experience e = new Experience();
            e.currState = NNPrevState;
            e.action = NNPrevAction;
            e.reward = reward;
            e.nextState = NNCurrStates;
            memory.add(e);
        }
        for(int i=0; i<NUM_INPUT_LAYERS; i++) {
            NNPrevState[i] = NNCurrStates[i];
        }
        NNPrevAction = nextAction;
        if(Math.random() < NN_EXPLORATION_RATE) {
            return (int)(Math.random() * Action.ROBOT_NUM_ACTIONS);
        }
        return nextAction;
    }

    public void radarMovement() {
        setTurnRadarRightRadians(Double.POSITIVE_INFINITY);
    }

    public void robotMovement() {
        action = getNNAction(getHeading(), enemy.distance, enemy.bearing, reward);
        reward = 0.0;
        isHitWall = 0;
        isHitByBullet = 0;
        switch (action) {
            case Action.ROBOT_UP:
                setAhead(ece.cpen502.Action.ROBOT_MOVE_SHORT_DISTANCE);
                break;
            case Action.ROBOT_UP_LONG:
                setAhead(ece.cpen502.Action.ROBOT_MOVE_LONG_DISTANCE);
                break;
            case Action.ROBOT_DOWN:
                setBack(ece.cpen502.Action.ROBOT_MOVE_SHORT_DISTANCE);
                break;
            case Action.ROBOT_DOWN_LONG:
                setBack(ece.cpen502.Action.ROBOT_MOVE_LONG_DISTANCE);
                break;
            case Action.ROBOT_LEFT:
                setTurnLeft(ece.cpen502.Action.ROBOT_TURN_DEGREE);
                break;
            case Action.ROBOT_RIGHT:
                setTurnRight(ece.cpen502.Action.ROBOT_TURN_DEGREE);
                break;
//            case Action.ROBOT_FIRE:
//                //turnGunRight(getHeading() - getGunHeading() + enemyBearing);
//                setFire(firePower);
//                break;
        }
    }

    public void onScannedRobot(ScannedRobotEvent e) {
        if ((enemy.name == e.getName()) || (e.getDistance() < enemy.distance)) {
            enemy.name = e.getName();
            double bearingRadius = (getHeadingRadians() + e.getBearingRadians()) % (2 * Math.PI);
            double heading = normaliseBearing(e.getHeadingRadians() - enemy.heading);
            heading /= (getTime() - enemy.ctime);
            enemy.changeHeading = heading;
            enemy.distance = e.getDistance();
            enemy.x = Math.sin(bearingRadius) * enemy.distance + getX();
            enemy.y = Math.cos(bearingRadius) * enemy.distance + getY();
            enemy.ctime = getTime();
            enemy.speed = e.getVelocity();
            enemy.bearing = e.getBearingRadians();
            enemy.heading = e.getHeadingRadians();
            enemyBearing = e.getBearing();
        }
    }

    private void gunMovement() {
        long gaussTime, nextTime;
        double gunOffset;
        Point2D.Double p = new Point2D.Double(enemy.x, enemy.y);
        for (int i=0; i<20; i++) {
            nextTime = (int)Math.round((getEuDistance(getX(),getY(),p.x,p.y) / (20 - (3 * firePower))));
            gaussTime = getTime() + nextTime - 10;
            p = enemy.getNextPosition(gaussTime);
        }

        gunOffset = normaliseBearing(getGunHeadingRadians() -
                (Math.PI/2 - Math.atan2(p.y - getY(),p.x -  getX())));
        setTurnGunLeftRadians(gunOffset);
    }

    public double getEuDistance(double x1, double y1, double x2, double y2) {
        double x = x1 - x2;
        double y = y1 - y2;
        return Math.sqrt(x * x + y * y);
    }

    double normaliseBearing(double degree) {
        if (degree > Math.PI) {
            degree -= 2*Math.PI;
        }
        if (degree < -Math.PI) {
            degree += 2*Math.PI;
        }
        return degree;
    }

    public void onHitWall(HitWallEvent e){
        isHitWall = 1;
        reward += IMMEDIATE_FINE / 2;
        getNNAction(getHeading(), enemy.distance, enemy.bearing, reward);
        stepBack();
    }

    public void onBulletHit(BulletHitEvent e) {
        reward += IMMEDIATE_REWARD;
        getNNAction(getHeading(), enemy.distance, enemy.bearing, reward);
        //trainReplayMemory();
    }

    public void onHitByBullet(HitByBulletEvent e) {
        isHitByBullet = 1;
        reward += IMMEDIATE_FINE;
        getNNAction(getHeading(), enemy.distance, enemy.bearing, reward);
        //trainReplayMemory();
    }

    public void onBulletMissed(BulletMissedEvent e) {
        reward += IMMEDIATE_FINE / 2;
        getNNAction(getHeading(), enemy.distance, enemy.bearing, reward);
    }

    public void onRobotDeath(RobotDeathEvent e) {
        reward += ENEMY_DEATH_REWARD;
        getNNAction(getHeading(), enemy.distance, enemy.bearing, reward);
    }

    public void stepBack() {
        setBack(180);
        setTurnRight(60);
        execute();
    }

    public void onDeath(DeathEvent event) {
        numRounds++;
        Statistics.saveScore(0);
        getNNAction(getHeading(), enemy.distance, enemy.bearing, TERMINAL_FINE);
        recordScores();
        saveNN_Weights();
    }

    public void onWin(WinEvent event) {
        numRounds++;
        winRounds++;
        Statistics.saveScore(1);
        getNNAction(getHeading(), enemy.distance, enemy.bearing, TERMINAL_REWARD);
        recordScores();
        saveNN_Weights();
    }

    public void trainReplayMemory() {
        Object[] experiences = memory.randomSample(MEMORY_N/2);
        for(Object e: experiences) {
            Experience x = (Experience) e;
            int nextAction = getMaxAction(x.currState);
            double NN_NewVal = nn[nextAction].outputFor(x.nextState);
            double NN_PrevVal = nn[x.action].outputFor(x.currState);
            double error = NN_ALPHA * (x.reward + NN_GAMMA * NN_NewVal - NN_PrevVal);
            double NN_CorrectPrevVal = NN_PrevVal + error;
            nn[action].train(x.currState, NN_CorrectPrevVal);
        }
    }


    public void recordScores() {
        if(numRounds % PERIOD == 0 && numRounds != 0) {
            double winRate = ((double)winRounds) / PERIOD;
            int round = numRounds / PERIOD;
            winRounds = 0;
            try{
                File file = getDataFile(scoreListFile);
                RobocodeFileWriter fileWriter = new RobocodeFileWriter(file.getAbsolutePath(), true);
                RMS_Error = Math.sqrt(RMS_Error/numRounds);
                fileWriter.write(String.format("round: %s score: %s RMS error: %s\n", round, winRate, RMS_Error));
                fileWriter.close();
            } catch(Exception e) {
                System.out.println(e);
            }
        }
    }

    public void onBattleEnded(BattleEndedEvent event) {
        Statistics.printWinRates();
    }

    public void saveNN_Weights() {
        for(int i=0; i<Action.ROBOT_NUM_ACTIONS; i++) {
            String weightsPath = "weights" + i + ".txt";
            weightFiles[i] = getDataFile(weightsPath);
            nn[i].saveWeights(weightFiles[i]);
        }
    }

    public void loadNN_Weights() {
        for(int i=0; i<Action.ROBOT_NUM_ACTIONS; i++) {
            String weightsPath = "weights" + i + ".txt";
            weightFiles[i] = getDataFile(weightsPath);
            try {
                nn[i].loadWeights(weightFiles[i]);
            } catch(Exception e) {
                System.out.println(e);
            }
        }
    }
}

