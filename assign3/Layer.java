package ece.assign3;

import java.util.Arrays;

public class Layer {
    private boolean IS_BINARY = false;
    private static final String RANDOM = "random";
    private static final String ZERO = "zero";
    public static final String INPUT_LAYER = "input layer";
    public static final String HIDDEN_LAYER = "hidden layer";
    public static final String OUTPUT_LAYER = "output layer";
    private int size;
    private String type;
    private Layer prev;
    private Layer next;
    private double[] values;
    private double[] deltas;
    private double[][] currWeights;
    // only for output layer
    private double[] expectedOutputs;
    private double[][] deltaWeights;
    public Layer(int size, String type){
        this.size = size;
        this.type = type;
        this.deltas = new double[size];
        if(type.equals(OUTPUT_LAYER)) {
            this.values = new double[size];
        } else {
            this.values = new double[size+1];
            values[size] = 1.0; // bias neuron
        }
    }
    // set input data
    public void setInputs(double[] inputs) {
        if(!type.equals(INPUT_LAYER) || inputs.length != size) {
            return;
        }
        for(int i=0; i<size; i++) {
            values[i] = inputs[i];
        }
    }
    // set output data
    public void setOutputs(double[] outputs) {
        if(!type.equals(OUTPUT_LAYER) || outputs.length != size) {
            return;
        }
        expectedOutputs = new double[size];
        for(int i=0; i<size; i++) {
            expectedOutputs[i] = outputs[i];
        }
    }
    // connect current layer with prev layer and next layer
    public void connectLayer(Layer prevLayer, Layer nextLayer) {
        prev = prevLayer;
        next = nextLayer;
        // outputLayer
        if(type.equals(OUTPUT_LAYER)) {
            currWeights = null;
            deltaWeights = null;
            expectedOutputs = new double[size];
        } else {
            currWeights = new double[size+1][next.size];
            deltaWeights = new double[size+1][next.size];
            expectedOutputs = null;
        }
    }
    public double getRandom(double lowerBound, double upperBound) {
        return lowerBound + (upperBound-lowerBound)*Math.random();
    }
    public void setWeights(String setValue, double lowerBound, double
            upperBound) {
        if(type.equals(OUTPUT_LAYER)) {
            return;
        }
        for (int j = 0; j < next.size; j++) {
            for (int i = 0; i < size + 1; i++) {
                if(setValue == RANDOM) {
                    currWeights[i][j] = getRandom(lowerBound, upperBound);
                } else if(setValue == ZERO) {
                    currWeights[i][j] = 0.0;
                }
                deltaWeights[i][j] = 0.0;
            }
        }
    }
    public void initializeWeights(double lowerBound, double upperBound) {
        setWeights(RANDOM, lowerBound, upperBound);
    }
    public void zeroWeights() {
        setWeights(ZERO, 0, 0);
    }
    public double sigmoid(double x) {
        return 2/(1+Math.exp(-x))-1;
    }
    public double customSigmoid(double x) {
        if (IS_BINARY) {
            return 1/(1+Math.exp(-x));
        }
        return sigmoid(x);
    }
    public void forwardPropagation() {
        if(type.equals(OUTPUT_LAYER)) return;
        for (int j=0; j<next.size; j++) {
            next.values[j] = 0.0;
            // include current layer bias neuron to predict next layer values
            for(int i=0; i<size+1; i++) {
                next.values[j] += currWeights[i][j]*values[i];
            }
            next.values[j] = customSigmoid(next.values[j]);
        }
    }
    public double getDerivative(double value) {
        if(IS_BINARY) {
            return value*(1-value);
        }
        return (value+1)*(1-value)*0.5;
    }

    public void backwardPropagation(double learningRate, double momentum) {
        if(type.equals(INPUT_LAYER)) {
            return;
        }
        if(type.equals(OUTPUT_LAYER)) {
            for(int i=0; i<size; i++) {
                double error = expectedOutputs[i]-values[i];
                deltas[i] = getDerivative(values[i])*error;
            }
        } else if(type.equals(HIDDEN_LAYER)) {
            for(int i=0; i<size; i++) {
                deltas[i] = 0;
                for(int j=0; j<next.size; j++) {
                    deltas[i] += currWeights[i][j]*next.deltas[j];
                }
                deltas[i] = getDerivative(values[i])*deltas[i];
            }
        }
        for(int j=0; j<size; j++) {
            for(int i=0; i <= prev.size; i++) {
                prev.deltaWeights[i][j] = momentum*prev.deltaWeights[i][j] + learningRate*deltas[j]*prev.values[i];
                prev.currWeights[i][j] += prev.deltaWeights[i][j];
            }
        }
    }

    public Layer getNext() {
        return next;
    }
    public Layer getPrev() {
        return prev;
    }
    public double[] getValues() {
        return values;
    }
    public String saveCurrWeightsToString() {
        String str = "";
        for (int j = 0; j < next.size; j++) {
            for (int i = 0; i < size + 1; i++) {
                str += currWeights[i][j] + "-";
            }
        }
        return str;
    }

    public void updateCurrWeights(String str) {
        String[] weights = Arrays.stream(str.split("-")).filter(e -> e.trim().length() > 0).toArray(String[]::new);
        int n = weights.length;
        int id = 0;
        if(n == next.size*(size+1)) {
            System.out.println("validation: success ..." );
        } else {
            System.out.println("validation: fails ...");
        }
        for (int j = 0; j < next.size; j++) {
            for (int i = 0; i < size + 1; i++) {
                currWeights[i][j] = Double.valueOf(weights[id]);
                System.out.print(currWeights[i][j] + "-");
                id++;
            }
        }
        System.out.println();
    }
}
