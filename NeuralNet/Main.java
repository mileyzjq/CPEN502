package NeuralNet;
import java.io.File;

public class Main {
    public static void main(String[] args) {
        NeuralNet nn = new NeuralNet(2,4,1,0.2,0.0);
        //nn.trainWholeProcess(new double[][]{{0,0},{0,1},{1,0},{1,1}}, new double[][]{{0},{1},{1},{0}});
        //nn.trainWholeProcess(new double[][]{{-1, -1}, {-1, 1}, {1, -1}, {1, 1}}, new double[][]{{-1}, {1}, {1}, {-1}});
        int count = 0;
        for(int i=0; i<100; i++) {
            count += nn.trainWholeProcess(new double[][]{{-1, -1}, {-1, 1}, {1, -1}, {1, 1}}, new double[][]{{-1}, {1}, {1}, {-1}});
            //count += nn.trainWholeProcess(new double[][]{{0,0},{0,1},{1,0},{1,1}}, new double[][]{{0},{1},{1},{0}});
        }
        System.out.println(count/100);
        nn.save(new File("./errors.txt"));
    }
}
