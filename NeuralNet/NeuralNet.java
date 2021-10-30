package NeuralNet;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
public class NeuralNet implements NeuralNetInterface{
    private static final double ERROR_THRESHOLD = 0.05;
    private Layer inputLayer;
    private Layer hiddenLayer;
    private Layer outputLayer;
    private double learningRate;
    private double momentum;
    private int numInput;
    private int numHidden;
    private int numOutput;
    private double lowerBound = -0.5;
    private double upperBound = 0.5;
    private ArrayList<String> arr = new ArrayList<>();
    public NeuralNet(int numInput, int numHidden, int numOutput, double
            learningRate, double momentum) {
        this.numInput = numInput;
        this.numHidden = numHidden;
        this.numOutput = numOutput;
        this.learningRate = learningRate;
        this.momentum = momentum;
        setUpNeuralNet();
    }
    public void setUpNeuralNet() {
        inputLayer = new Layer(numInput, Layer.INPUT_LAYER);
        hiddenLayer = new Layer(numHidden, Layer.HIDDEN_LAYER);
        outputLayer = new Layer(numOutput, Layer.OUTPUT_LAYER);
        inputLayer.connectLayer(null, hiddenLayer);
        hiddenLayer.connectLayer(inputLayer, outputLayer);
        outputLayer.connectLayer(hiddenLayer, null);
    }

    public int trainWholeProcess(double[][] inputs, double[][] outputs) {
        initializeWeights();
        double[] totalErrors = new double[numOutput];
        int epoch = 0;
        totalErrors[0] = ERROR_THRESHOLD;
        while(totalErrors[0]>=ERROR_THRESHOLD && epoch<10000) {
            for(int i=0; i<numOutput; i++) {
                totalErrors[i] = 0;
            }
            for (int i = 0; i < inputs.length; i++) {
                inputLayer.setInputs(inputs[i]);
                outputLayer.setOutputs(outputs[i]);
                feedForward();
                for(int j=0; j<numOutput; j++) {
                    double singleError = outputs[i][j] -
                            outputLayer.getValues()[j];
                    totalErrors[j] += Math.pow(singleError, 2) / 2;
                }
                backwardPropagation();
            }
            epoch++;
            System.out.println("Epoch: " + epoch + ", total error: " +
                    totalErrors[0]);
            arr.add("Epoch: " + epoch + ", total error: " + totalErrors[0]);
        }
        return epoch;
    }
    @Override
    public double outputFor(double[] X) {
        initializeWeights();
        inputLayer.setInputs(X);
        feedForward();
        return outputLayer.getValues()[0];
    }
    @Override
    public double train(double[] X, double argValue) {
        return argValue - outputFor(X);
    }
    @Override
    public void save(File argFile) {
        PrintStream file = null;
        try{
            file = new PrintStream(new FileOutputStream(argFile) );
            for(int i=0; i<arr.size(); i++) {
                file.println(arr.get(i));
            }
            file.flush();
            file.close();
        }
        catch(IOException e){
            System.out.println("ERROR");
        }
    }
    @Override
    public void load(String argFileName) throws IOException {
    }
    @Override
    public void initializeWeights() {
        for(Layer currLayer=inputLayer; currLayer != outputLayer; currLayer =
                currLayer.getNext()) {
            currLayer.initializeWeights(lowerBound, upperBound);
        }
    }
    @Override
    public void zeroWeights() {
        for(Layer currLayer=inputLayer; currLayer != outputLayer; currLayer =
                currLayer.getNext()) {
            currLayer.zeroWeights();
        }
    }
    public void feedForward() {
        for(Layer currLayer=inputLayer; currLayer != outputLayer; currLayer =
                currLayer.getNext()) {
            currLayer.forwardPropagation();
        }
    }
    public void backwardPropagation() {
        for(Layer currLayer=outputLayer; currLayer != inputLayer; currLayer =
                currLayer.getPrev()) {
            currLayer.backwardPropagation(learningRate, momentum);
        }
    }
}