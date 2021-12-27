package ece.assign3;

public class State {
    public static final double TOTAL_ANGLE = 360.0;
    public static final double CIRCLE = Math.PI * 2;
    public static int NumStates;
    public static final int NUM_DISTANCE = 10;
    public static final int NUM_BREARING = 4;
    public static final int NUM_HEADING = 4;
    public static final int NUM_HIT_BY_BULLETS = 2;
    public static final int NUM_HIT_WALL = 2;
    public static final int NUM_ENERGY = 5;
    public static int states[][][][][];

    static {
        states = new int[NUM_DISTANCE][NUM_BREARING][NUM_HEADING][NUM_HIT_BY_BULLETS][NUM_HIT_WALL];
        int cnt = 0;
        for(int a=0; a<NUM_DISTANCE; a++) {
            for(int b=0; b<NUM_BREARING; b++) {
                for(int c=0; c<NUM_HEADING; c++) {
                    for(int d=0; d<NUM_HIT_BY_BULLETS; d++) {
                        for(int e=0; e<NUM_HIT_BY_BULLETS; e++) {
                            states[a][b][c][d][e] = cnt++;
                        }
                    }
                }
            }
        }
        NumStates = cnt;
    }

    public static int getDistance(double distance) {
        int res = (int)(distance / 100.0);
        return Math.min(NUM_DISTANCE-1, res);
    }

    public static int getHeading(double heading) {
        double angle = TOTAL_ANGLE / NUM_HEADING;
        double newHeading = heading+angle/2;
        while (newHeading > TOTAL_ANGLE) {
            newHeading-=TOTAL_ANGLE;
        }
        return (int)(newHeading/angle);
    }

    public static int getBearing(double bearing) {
        double angle=CIRCLE / NUM_BREARING;
        double newBearing = bearing;
        if(bearing < 0) {
            newBearing += CIRCLE;
        }
        newBearing += angle / 2;
        if(newBearing > CIRCLE) {
            newBearing = newBearing - CIRCLE;
        }
        return (int) (newBearing / angle);
    }

    public static int getEnergyLevel(double energy) {
        double levels = 100 / NUM_ENERGY;
        return Math.min((int)(energy/levels), NUM_ENERGY-1);
    }
}
