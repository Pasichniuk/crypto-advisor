package com.crypto.advisor.service.prediction.predict;

import com.crypto.advisor.service.prediction.model.RecurrentNets;
import com.crypto.advisor.service.prediction.representation.CryptoDataSetIterator;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.io.ClassPathResource;
import org.nd4j.linalg.primitives.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class CryptoPricePrediction {

    private static final Logger LOGGER = LoggerFactory.getLogger(CryptoPricePrediction.class);

    private static final int EXAMPLE_LENGTH = 22; // time series length, assume 22 working days per month

    public static double[] predict(String symbol) throws IOException {
        String fileName = String.format("%s-prices.csv", symbol);
        String file = new ClassPathResource(fileName).getFile().getAbsolutePath();

        int epochs = 30; // training epochs
        int batchSize = 64; // mini-batch size
        double splitRatio = 0.9; // 90% for training, 10% for testing
        File locationToSave = new File("src/main/resources/crypto-price-model.zip");

        LOGGER.info("Create dataSet iterator...");
        CryptoDataSetIterator iterator = new CryptoDataSetIterator(file, symbol, batchSize, EXAMPLE_LENGTH, splitRatio);

        LOGGER.info("Load test dataset...");
        List<Pair<INDArray, INDArray>> test = iterator.getTestDataSet();

        MultiLayerNetwork net;

        boolean trainModel = false;
        if (trainModel) {
            LOGGER.info("Build lstm networks...");
            net = RecurrentNets.buildLstmNetworks(iterator.inputColumns(), iterator.totalOutcomes());

            LOGGER.info("Training...");
            for (int i = 0; i < epochs; i++) {
                while (iterator.hasNext()) net.fit(iterator.next());
                iterator.reset();
                net.rnnClearPreviousState();
            }

            LOGGER.info("Saving model...");
            ModelSerializer.writeModel(net, locationToSave, true);
        }

        LOGGER.info("Load model...");
        net = ModelSerializer.restoreMultiLayerNetwork(locationToSave);

        LOGGER.info("Testing...");
        double max = iterator.getMax();
        double min = iterator.getMin();

        return predictPriceOneAhead(net, test, max, min);
    }

    private static double[] predictPriceOneAhead(MultiLayerNetwork net, List<Pair<INDArray, INDArray>> testData, double max, double min) {
        double[] predicts = new double[testData.size()];
        double[] actuals = new double[testData.size()];

        for (int i = 0; i < testData.size(); i++) {
            predicts[i] = net.rnnTimeStep(testData.get(i).getKey()).getDouble(EXAMPLE_LENGTH - 1) * (max - min) + min;
            actuals[i] = testData.get(i).getValue().getDouble(0);
        }

        LOGGER.info("Print out Predictions and Actual Values...");
        LOGGER.info("Predict,Actual");

        for (int i = 0; i < predicts.length; i++) {
            LOGGER.info("{},{}", predicts[i], actuals[i]);
        }

        LOGGER.info("Done...");

        return predicts;
    }
}