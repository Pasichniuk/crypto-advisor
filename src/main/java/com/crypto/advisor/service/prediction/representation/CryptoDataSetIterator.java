package com.crypto.advisor.service.prediction.representation;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.DataSetPreProcessor;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.primitives.Pair;

import java.util.*;

public class CryptoDataSetIterator implements DataSetIterator {

    private static final int PREDICT_LENGTH = 1;
    private static final int VECTOR_SIZE = 1;

    private final int miniBatchSize;
    private final int exampleLength;

    private double min;
    private double max;

    private final LinkedList<Integer> exampleStartOffsets = new LinkedList<>();
    private final List<Pair<INDArray, INDArray>> test;
    private final transient List<CryptoData> train;

    public CryptoDataSetIterator(List<CryptoData> cryptoData, int miniBatchSize, int exampleLength, double splitRatio) {
        this.min = calculateMin(cryptoData);
        this.max = calculateMax(cryptoData);
        this.miniBatchSize = miniBatchSize;
        this.exampleLength = exampleLength;
        int split = (int) Math.round(cryptoData.size() * splitRatio);
        train = cryptoData.subList(0, split);
        test = generateTestDataSet(cryptoData.subList(split, cryptoData.size()));
        initializeOffsets();
    }

    private double calculateMin(List<CryptoData> cryptoData) {
        return cryptoData.stream()
                .min(Comparator.comparing(CryptoData::getClose))
                .orElseThrow(NoSuchElementException::new)
                .getClose();
    }

    private double calculateMax(List<CryptoData> cryptoData) {
        return cryptoData.stream()
                .max(Comparator.comparing(CryptoData::getClose))
                .orElseThrow(NoSuchElementException::new)
                .getClose();
    }

    private void initializeOffsets() {
        exampleStartOffsets.clear();
        int window = exampleLength + PREDICT_LENGTH;
        for (int i = 0; i < train.size() - window; i++) {
            exampleStartOffsets.add(i);
        }
    }

    public List<Pair<INDArray, INDArray>> getTestDataSet() {
        return test;
    }

    public double getMax() {
        return max;
    }

    public double getMin() {
        return min;
    }

    @Override
    public DataSet next(int num) {
        if (exampleStartOffsets.size() == 0) throw new NoSuchElementException();
        int actualMiniBatchSize = Math.min(num, exampleStartOffsets.size());
        INDArray input = Nd4j.create(new int[]{actualMiniBatchSize, VECTOR_SIZE, exampleLength}, 'f');
        INDArray label = Nd4j.create(new int[]{actualMiniBatchSize, PREDICT_LENGTH, exampleLength}, 'f');
        for (int index = 0; index < actualMiniBatchSize; index++) {
            int startIdx = exampleStartOffsets.removeFirst();
            int endIdx = startIdx + exampleLength;
            CryptoData curData = train.get(startIdx);
            CryptoData nextData;
            for (int i = startIdx; i < endIdx; i++) {
                int c = i - startIdx;
                input.putScalar(new int[]{index, 0, c}, (curData.getClose() - min) / (max - min));
                nextData = train.get(i + 1);
                label.putScalar(new int[]{index, 0, c}, (nextData.getClose() - min) / (max - min));
                curData = nextData;
            }
            if (exampleStartOffsets.size() == 0) break;
        }
        return new DataSet(input, label);
    }

    @Override
    public int totalExamples() {
        return train.size() - exampleLength - PREDICT_LENGTH;
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
        throw new UnsupportedOperationException("Not Implemented");
    }

    @Override
    public DataSetPreProcessor getPreProcessor() {
        throw new UnsupportedOperationException("Not Implemented");
    }

    @Override
    public List<String> getLabels() {
        throw new UnsupportedOperationException("Not Implemented");
    }

    @Override
    public boolean hasNext() {
        return exampleStartOffsets.size() > 0;
    }

    @Override
    public DataSet next() {
        return next(miniBatchSize);
    }

    private List<Pair<INDArray, INDArray>> generateTestDataSet(List<CryptoData> stockDataList) {
        int window = exampleLength + PREDICT_LENGTH;
        List<Pair<INDArray, INDArray>> test = new ArrayList<>();
        for (int i = 0; i < stockDataList.size() - window; i++) {
            INDArray input = Nd4j.create(new int[]{exampleLength, VECTOR_SIZE}, 'f');
            for (int j = i; j < i + exampleLength; j++) {
                CryptoData stock = stockDataList.get(j);
                input.putScalar(new int[]{j - i, 0}, (stock.getClose() - min) / (max - min));
            }
            CryptoData stock = stockDataList.get(i + exampleLength);
            INDArray label = Nd4j.create(new int[]{1}, 'f');
            label.putScalar(new int[]{0}, stock.getClose());
            test.add(new Pair<>(input, label));
        }
        return test;
    }
}