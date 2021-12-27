package ece.assign3;

import java.util.Arrays;
import java.util.Collections;

/**
 * Author: Sarbjit Sarkaria
 * Date  : 07th January 2021
 * This class implements a replay memory for any type T.
 * The capacity of the memory must be specified upon construction.
 * The memory will discard the oldest items that do not fit.
 * @param <T> Type to be managed by the ReplayMemory
 */

public class ReplayMemory<T> {

    private CircularQueue<T> memory;
    private Object[] EMPTYARRAY = {};

    // Constructor
    public ReplayMemory(int size) {
        memory = new CircularQueue<T>(size);
    }

    // Add an item to the memory
    public void add(T experience) {
        memory.add(experience);
    }

    // Retrieve a sample of n most recently added items from the memory and return it as an array
    public Object[] sample(int n) {
        if (memory.isEmpty())
            return EMPTYARRAY;
        else {
            // I don't have a way of returning T[], so instead I return Object[]
            // See the unit tests to see how to use this tyle - no additional effort necessary
            // .... see here for more : https://stackoverflow.com/questions/1115230/casting-object-array-to-integer-array-error
            int size = memory.size();
            Object[] objectArray = memory.toArray();
            Object[] sampleObjectArray = Arrays.copyOfRange(objectArray, size-n, size);
            return sampleObjectArray;
        }
    }

    // Retrieve a random sample of n items from the memory and return it as an array
    public Object[] randomSample(int n) {
        if (memory.isEmpty())
            return EMPTYARRAY;
        else {
            // I don't have a way of returning T[], so instead I return Object[]
            // See the unit tests to see how to use this tyle - no additional effort necessary
            // .... see here for more : https://stackoverflow.com/questions/1115230/casting-object-array-to-integer-array-error
            int size = memory.size();
            CircularQueue shuffledMemory = new CircularQueue<T>(size);
            shuffledMemory.addAll(memory); // This does a deep copy
            Collections.shuffle(shuffledMemory);
            Object[] objectArray = shuffledMemory.toArray();
            Object[] sampleObjectArray = Arrays.copyOfRange(objectArray, size-n, size);
            return sampleObjectArray;
        }
    }

    // Returns the current size of the replay memory. Use for test/debug purposes
    public int sizeOf() {
        if (memory.isEmpty())
            return 0;
        else
            return memory.size();
    }
}
