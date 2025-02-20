package com.crypto.advisor.service.prediction.model;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.*;
import org.deeplearning4j.nn.conf.layers.*;
import org.deeplearning4j.nn.conf.preprocessor.FeedForwardToRnnPreProcessor;
import org.deeplearning4j.nn.conf.preprocessor.RnnToFeedForwardPreProcessor;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.lossfunctions.LossFunctions;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class NeuralNetworkBuilder {

    private static final int SEED = 12345;
    private static final int ITERATIONS = 1;
    private static final double LEARNING_RATE = 0.01;
    private static final double DROPOUT_RATIO = 0.5;
    private static final int CNN_LAYER_SIZE = 25;
    private static final int LSTM_LAYER_SIZE = 512;
    private static final int DENSE_LAYER_SIZE = 32;
    private static final int TRUNCATED_BPTT_LENGTH = 22;

    public static MultiLayerNetwork buildHybridNetwork(int nIn, int nOut) {
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
            .seed(SEED)
            .iterations(ITERATIONS)
            .learningRate(LEARNING_RATE)
            .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
            .weightInit(WeightInit.XAVIER)
            .updater(Updater.RMSPROP)
            .regularization(true)
            .l2(1e-4)
            .list()
            .layer(0, new Convolution1DLayer.Builder(1)
                    .nIn(nIn)
                    .nOut(CNN_LAYER_SIZE)
                    .stride(1)
                    .activation(Activation.RELU)
                    .build())
            .layer(1, new GlobalPoolingLayer.Builder(PoolingType.AVG)
                    .build())
            .layer(2, new GravesLSTM.Builder()
                    .nIn(CNN_LAYER_SIZE)
                    .nOut(LSTM_LAYER_SIZE)
                    .activation(Activation.RELU)
                    .gateActivationFunction(Activation.HARDSIGMOID)
                    .dropOut(DROPOUT_RATIO)
                    .build())
            .layer(3, new GravesLSTM.Builder()
                    .nIn(LSTM_LAYER_SIZE)
                    .nOut(LSTM_LAYER_SIZE)
                    .activation(Activation.RELU)
                    .gateActivationFunction(Activation.HARDSIGMOID)
                    .dropOut(DROPOUT_RATIO)
                    .build())
            .layer(4, new DenseLayer.Builder()
                    .nIn(LSTM_LAYER_SIZE)
                    .nOut(DENSE_LAYER_SIZE)
                    .activation(Activation.RELU)
                    .build())
            .layer(5, new RnnOutputLayer.Builder()
                    .nIn(DENSE_LAYER_SIZE)
                    .nOut(nOut)
                    .activation(Activation.IDENTITY)
                    .lossFunction(LossFunctions.LossFunction.MSE)
                    .build())
            .inputPreProcessor(4, new RnnToFeedForwardPreProcessor())
            .inputPreProcessor(5, new FeedForwardToRnnPreProcessor())
            .backpropType(BackpropType.TruncatedBPTT)
            .tBPTTForwardLength(TRUNCATED_BPTT_LENGTH)
            .tBPTTBackwardLength(TRUNCATED_BPTT_LENGTH)
            .pretrain(false)
            .backprop(true)
            .build();

        MultiLayerNetwork net = new MultiLayerNetwork(conf);
        net.init();
        net.setListeners(new ScoreIterationListener(100));

        return net;
    }
}