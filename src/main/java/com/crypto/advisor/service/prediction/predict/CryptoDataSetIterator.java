package com.crypto.advisor.service.prediction.predict;

import com.crypto.advisor.entity.CryptoData;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.DataSetPreProcessor;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.primitives.Pair;

import java.util.*;

public class CryptoDataSetIterator implements DataSetIterator {

    private static final String NOT_IMPLEMENTED = "Not Implemented";

    private static final int PREDICT_LENGTH = 1;
    private static final int VECTOR_SIZE = 1;

    private final int miniBatchSize;
    private final int exampleLength;

    private final double min;
    private final double max;

    private final LinkedList<Integer> exampleStartOffsets = new LinkedList<>();
    private final List<Pair<INDArray, INDArray>> testDataSet;
    private final transient List<CryptoData> trainDataSet;

    public CryptoDataSetIterator(List<CryptoData> cryptoData, int miniBatchSize, int exampleLength, double splitRatio) {
        this.min = calculateMin(cryptoData);
        this.max = calculateMax(cryptoData);
        this.miniBatchSize = miniBatchSize;
        this.exampleLength = exampleLength;
        int split = (int) Math.round(cryptoData.size() * splitRatio);
        trainDataSet = cryptoData.subList(0, split);
        testDataSet = generateTestDataSet(cryptoData.subList(split, cryptoData.size()));
        initializeOffsets();
    }

    private double calculateMin(List<CryptoData> cryptoData) {
        return cryptoData.stream()
                .min(Comparator.comparing(CryptoData::getPrice))
                .orElseThrow(NoSuchElementException::new)
                .getPrice();
    }

    private double calculateMax(List<CryptoData> cryptoData) {
        return cryptoData.stream()
                .max(Comparator.comparing(CryptoData::getPrice))
                .orElseThrow(NoSuchElementException::new)
                .getPrice();
    }

    private List<Pair<INDArray, INDArray>> generateTestDataSet(List<CryptoData> cryptoData) {
        List<Pair<INDArray, INDArray>> testDs = new ArrayList<>();
        int window = exampleLength + PREDICT_LENGTH;

        for (int i = 0; i < cryptoData.size() - window; i++) {
            var input = Nd4j.create(new int[] {exampleLength, VECTOR_SIZE}, 'f');

            for (int j = i; j < i + exampleLength; j++) {
                var crypto = cryptoData.get(j);
                input.putScalar(new int[] {j - i, 0}, (crypto.getPrice() - min) / (max - min));
            }

            var crypto = cryptoData.get(i + exampleLength);
            var label = Nd4j.create(new int[]{1}, 'f');

            label.putScalar(new int[]{0}, crypto.getPrice());
            testDs.add(new Pair<>(input, label));
        }

        return testDs;
    }

    private void initializeOffsets() {
        exampleStartOffsets.clear();
        int window = exampleLength + PREDICT_LENGTH;
        for (int i = 0; i < trainDataSet.size() - window; i++) {
            exampleStartOffsets.add(i);
        }
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    public List<Pair<INDArray, INDArray>> getTestDataSet() {
        return testDataSet;
    }

    @Override
    public DataSet next(int num) {
        if (exampleStartOffsets.isEmpty()) {
            throw new NoSuchElementException();
        }

        int actualMiniBatchSize = Math.min(num, exampleStartOffsets.size());
        var input = Nd4j.create(new int[]{actualMiniBatchSize, VECTOR_SIZE, exampleLength}, 'f');
        var label = Nd4j.create(new int[]{actualMiniBatchSize, PREDICT_LENGTH, exampleLength}, 'f');

        for (int index = 0; index < actualMiniBatchSize; index++) {
            int startIdx = exampleStartOffsets.removeFirst();
            int endIdx = startIdx + exampleLength;

            CryptoData curData = trainDataSet.get(startIdx);
            CryptoData nextData;

            for (int i = startIdx; i < endIdx; i++) {
                int c = i - startIdx;

                input.putScalar(new int[]{index, 0, c}, (curData.getPrice() - min) / (max - min));
                nextData = trainDataSet.get(i + 1);

                label.putScalar(new int[]{index, 0, c}, (nextData.getPrice() - min) / (max - min));
                curData = nextData;
            }

            if (exampleStartOffsets.isEmpty()) {
                break;
            }
        }

        return new DataSet(input, label);
    }

    @Override
    public int totalExamples() {
        return trainDataSet.size() - exampleLength - PREDICT_LENGTH;
    }

    @Override
    public int inputColumns() {
        return VECTOR_SIZE;
    }

    @Override
    public int totalOutcomes() {
        return PREDICT_LENGTH;
    }

    @Override
    public boolean resetSupported() {
        return false;
    }

    @Override
    public boolean asyncSupported() {
        return false;
    }

    @Override
    public void reset() {
        initializeOffsets();
    }

    @Override
    public int batch() {
        return miniBatchSize;
    }

    @Override
    public int cursor() {
        return totalExamples() - exampleStartOffsets.size();
    }

    @Override
    public int numExamples() {
        return totalExamples();
    }

    @Override
    public void setPreProcessor(DataSetPreProcessor dataSetPreProcessor) {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED);
    }

    @Override
    public DataSetPreProcessor getPreProcessor() {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED);
    }

    @Override
    public List<String> getLabels() {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED);
    }

    @Override
    public boolean hasNext() {
        return !exampleStartOffsets.isEmpty();
    }

    @Override
    public DataSet next() {
        return next(miniBatchSize);
    }
}