package com.crypto.advisor.service.prediction.model;

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

public class RecurrentNets {

    private static final int SEED = 12345;
    private static final int ITERATIONS = 1;
    private static final int DENSE_LAYER_SIZE = 32;
    private static final int LSTM_LAYER_1_SIZE = 256;
    private static final int LSTM_LAYER_2_SIZE = 256;
    private static final int TRUNCATED_BPTT_LENGTH = 22;

    private static final double LEARNING_RATE = 0.05;
    private static final double DROPOUT_RATIO = 0.2;

    private RecurrentNets() {}

    public static MultiLayerNetwork buildLstmNetworks(int nIn, int nOut) {
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(12345)
                .iterations(1)
                .learningRate(0.05)
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .weightInit(WeightInit.XAVIER)
                .updater(Updater.RMSPROP)
                .regularization(true)
                .l2(1e-4)
                .list()
                .layer(0, new ConvolutionLayer.Builder()
                        .nIn(nIn)
                        .nOut(25)
                        .stride(1)
                        .activation(Activation.RELU)
                        .build())
                .layer(1, new ConvolutionLayer.Builder()
                        .nIn(128)
                        .nOut(64)
                        .activation(Activation.RELU)
                        .build())
                .layer(2, new GravesLSTM.Builder()
                        .nIn(64)
                        .nOut(256)
                        .activation(Activation.TANH)
                        .gateActivationFunction(Activation.HARDSIGMOID)
                        .dropOut(DROPOUT_RATIO)
                        .build())
                .layer(3, new GravesLSTM.Builder()
                        .nIn(256)
                        .nOut(256)
                        .activation(Activation.TANH)
                        .gateActivationFunction(Activation.HARDSIGMOID)
                        .dropOut(0.2)
                        .build())
                .layer(4, new DenseLayer.Builder()
                        .nIn(256)
                        .nOut(32)
                        .activation(Activation.RELU)
                        .build())
                .layer(5, new RnnOutputLayer.Builder()
                        .nIn(32)
                        .nOut(nOut)
                        .activation(Activation.IDENTITY)
                        .lossFunction(LossFunctions.LossFunction.MSE)
                        .build())
                .backpropType(BackpropType.TruncatedBPTT)
                .tBPTTForwardLength(22)
                .tBPTTBackwardLength(22)
                .pretrain(false)
                .backprop(true)
                .build();

        MultiLayerNetwork net = new MultiLayerNetwork(conf);
        net.init();
        net.setListeners(new ScoreIterationListener(100));

        return net;
    }

    public static MultiLayerNetwork buildHybridNetwork(int nIn, int nOut) {
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
            .seed(12345)
            .iterations(1)
            .learningRate(0.05)
            .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
            .weightInit(WeightInit.XAVIER)
            .updater(Updater.RMSPROP)
            .regularization(true)
            .l2(1e-4)
            .list()
            .layer(0, new Convolution1DLayer.Builder(1)
                    .nIn(nIn)
                    .nOut(25)
                    .stride(1)
                    .activation(Activation.RELU)
                    .build())
            .layer(1, new GlobalPoolingLayer.Builder(PoolingType.AVG)
                    .build())
            .layer(2, new GravesLSTM.Builder()
                    .nIn(25)
                    .nOut(256)
                    .activation(Activation.TANH)
                    .gateActivationFunction(Activation.HARDSIGMOID)
                    .dropOut(0.2)
                    .build())
            .layer(3, new GravesLSTM.Builder()
                    .nIn(256)
                    .nOut(256)
                    .activation(Activation.TANH)
                    .gateActivationFunction(Activation.HARDSIGMOID)
                    .dropOut(0.2)
                    .build())
            .layer(4, new DenseLayer.Builder()
                    .nIn(256)
                    .nOut(32)
                    .activation(Activation.RELU)
                    .build())
            .layer(5, new RnnOutputLayer.Builder()
                    .nIn(32)
                    .nOut(nOut)
                    .activation(Activation.IDENTITY)
                    .lossFunction(LossFunctions.LossFunction.MSE)
                    .build())
            .inputPreProcessor(4, new RnnToFeedForwardPreProcessor())
            .inputPreProcessor(5, new FeedForwardToRnnPreProcessor())
            .backpropType(BackpropType.TruncatedBPTT)
            .tBPTTForwardLength(22)
            .tBPTTBackwardLength(22)
            .pretrain(false)
            .backprop(true)
            .build();

        MultiLayerNetwork net = new MultiLayerNetwork(conf);
        net.init();
        net.setListeners(new ScoreIterationListener(100));

        return net;
    }
}