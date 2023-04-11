package com.crypto.advisor.service.prediction.predict;

import com.crypto.advisor.service.prediction.model.RecurrentNets;
import com.crypto.advisor.entity.CryptoData;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.primitives.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class CryptoPricePrediction {

    private static final Logger LOGGER = LoggerFactory.getLogger(CryptoPricePrediction.class);

    private static final int EXAMPLE_LENGTH = 22; // time series length, assume 22 working days per month

    private CryptoPricePrediction() {}

    public static double[] predict(List<CryptoData> cryptoData) throws IOException {
        var modelFile = new File("src/main/resources/crypto-price-model.zip");

        double splitRatio = 0.9; // 90% for training, 10% for testing
        int trainingEpochs = 30;
        int miniBatchSize = 64;

        LOGGER.info("Create data set iterator...");
        var iterator = new CryptoDataSetIterator(cryptoData, miniBatchSize, EXAMPLE_LENGTH, splitRatio);

        LOGGER.info("Load test dataset...");
        var testDataSet = iterator.getTestDataSet();

        MultiLayerNetwork net;

        boolean trainModel = false; // true -> train & use the model; false -> simply use the model
        if (trainModel) {
            LOGGER.info("Build lstm networks...");
            net = RecurrentNets.buildLstmNetworks(iterator.inputColumns(), iterator.totalOutcomes());

            LOGGER.info("Training...");
            for (int i = 0; i < trainingEpochs; i++) {
                while (iterator.hasNext()) net.fit(iterator.next());
                iterator.reset();
                net.rnnClearPreviousState();
            }

            LOGGER.info("Saving model...");
            ModelSerializer.writeModel(net, modelFile, true);
        }

        LOGGER.info("Load model...");
        net = ModelSerializer.restoreMultiLayerNetwork(modelFile);

        LOGGER.info("Testing...");
        double max = iterator.getMax();
        double min = iterator.getMin();

        return predictPriceOneAhead(net, testDataSet, max, min);
    }

    // uncomment code to see predicted/actual prices
    private static double[] predictPriceOneAhead(MultiLayerNetwork net, List<Pair<INDArray, INDArray>> testData, double max, double min) {
        double[] predicts = new double[testData.size()];
        //double[] actuals = new double[testData.size()];

        for (int i = 0; i < testData.size(); i++) {
            predicts[i] = net.rnnTimeStep(testData.get(i).getKey()).getDouble(EXAMPLE_LENGTH - 1) * (max - min) + min;
            //actuals[i] = testData.get(i).getValue().getDouble(0);
        }

        /*
        LOGGER.info("Predict, Actual");
        for (int i = 0; i < predicts.length; i++) {
            LOGGER.info("{}, {}", predicts[i], actuals[i]);
        }
        LOGGER.info("Done!");
        */

        return predicts;
    }
}