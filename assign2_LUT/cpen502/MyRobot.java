package ece.cpen502;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import robocode.*;

public class MyRobot extends AdvancedRobot {
    private static final boolean ON_POLICY = true;
    private static final boolean INTERMEDIATE_REWARD = true;
    private static final boolean BASELINE_ROBOT= false;
    private static final double BASE_DISTANCE = 400.0;
    private Enemy enemy;
    private static LookUpTable table;
    private LearningAgent agent;
    private double reward;
    private double firePower = 1;
    private int isHitByBullet = 0;
    private int isHitWall = 0;
    private ArrayList<Integer> scores = new ArrayList<>();

    public void run() {
        //state = new State();
        table = new LookUpTable();
        agent = new LearningAgent(table);
        enemy = new Enemy("enemy");
        enemy.distance = 10000;

        setAllColors(Color.red);
        setAdjustGunForRobotTurn(true);
        setAdjustRadarForGunTurn(true);
        turnRadarRightRadians(2 * Math.PI);

        while(true) {
            if(!BASELINE_ROBOT) {
                firePower = BASE_DISTANCE / enemy.distance;
                firePower = Math.min(3, firePower);
            }
            radarMovement();
            gunMovement();
            robotMovement();
            execute();
        }
    }

    public void radarMovement() {
        setTurnRadarRightRadians(Double.POSITIVE_INFINITY);
    }

    public void robotMovement() {
        int action;
        if(BASELINE_ROBOT) {
            action = (int)(Math.random() * Action.ROBOT_NUM_ACTIONS);
        } else {
            int state = getState();
            action= agent.getNextAction(state);
            agent.Learn(state, action, reward, ON_POLICY, INTERMEDIATE_REWARD);
            reward = 0.0;
            isHitByBullet = 0;
            isHitWall = 0;
        }

        switch (action) {
            case Action.ROBOT_UP:
                setAhead(Action.ROBOT_MOVE_SHORT_DISTANCE);
                break;
            case Action.ROBOT_UP_LONG:
                setAhead(Action.ROBOT_MOVE_LONG_DISTANCE);
                break;
            case Action.ROBOT_DOWN:
                setBack(Action.ROBOT_MOVE_SHORT_DISTANCE);
                break;
            case Action.ROBOT_DOWN_LONG:
                setBack(Action.ROBOT_MOVE_LONG_DISTANCE);
                break;
            case Action.ROBOT_LEFT:
                setTurnLeft(Action.ROBOT_TURN_DEGREE);
                break;
            case Action.ROBOT_RIGHT:
                setTurnRight(Action.ROBOT_TURN_DEGREE);
                break;
            case Action.ROBOT_FIRE:
                setFire(firePower);
                break;
        }
    }

    private int getState() {
        int heading = State.getHeading(getHeading());
        int bearing = State.getBearing(enemy.bearing);
        int distance = State.getDistance(enemy.distance);
        int energy = State.getEnergyLevel(getEnergy());
        return State.states[distance][bearing][heading][isHitByBullet][isHitWall][energy];
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

    public double getEuDistance(double x1, double y1, double x2, double y2)
    {
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
        if(INTERMEDIATE_REWARD) {
            reward -= 5;
        }
    }

    public void onBulletHit(BulletHitEvent e) {
        if(INTERMEDIATE_REWARD) {
            reward += 10;
        }
    }

    public void onHitByBullet(HitByBulletEvent e) {
        isHitByBullet = 1;
        if(INTERMEDIATE_REWARD) {
            reward -= 10;
        }
    }

    public void onBulletMissed(BulletMissedEvent e) {
        if(INTERMEDIATE_REWARD) {
            reward -= 3;
        }
    }

    public void onDeath(DeathEvent event) {
        scores.add(0);
        Statistics.saveScore(0);
        if(BASELINE_ROBOT) {
            return;
        }
        if(INTERMEDIATE_REWARD) {
            reward -= 60;
        } else {
            agent.feedReward(0);
        }
    }

    public void onWin(WinEvent event) {
        //System.out.println("win! ");
        //scores.saveScore(1);
        Statistics.saveScore(1);
        if(BASELINE_ROBOT) {
            return;
        }
        if(INTERMEDIATE_REWARD) {
            reward += 60;
        } else {
            agent.feedReward(1);
        }
    }

    public void onBattleEnded(BattleEndedEvent event) {
        Statistics.printWinRates();
    }
}
