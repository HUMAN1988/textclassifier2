package org.ripreal.textclassifier2.classifier;

import lombok.Getter;
import lombok.NonNull;
import org.encog.Encog;
import org.encog.engine.network.activation.ActivationSigmoid;
import org.encog.ml.data.basic.BasicMLDataSet;
import org.encog.neural.networks.BasicNetwork;
import org.encog.neural.networks.layers.BasicLayer;
import org.encog.neural.networks.training.propagation.Propagation;
import org.encog.neural.networks.training.propagation.resilient.ResilientPropagation;
import org.encog.persist.PersistError;
import org.ripreal.textclassifier2.CharacteristicUtils;
import org.ripreal.textclassifier2.actions.ClassifierAction;
import org.ripreal.textclassifier2.model.Characteristic;
import org.ripreal.textclassifier2.model.CharacteristicValue;
import org.ripreal.textclassifier2.model.ClassifiableText;
import org.ripreal.textclassifier2.model.VocabularyWord;
import org.ripreal.textclassifier2.model.modelimp.DefVocabularyWord;
import org.ripreal.textclassifier2.ngram.NGramStrategy;

import java.io.File;
import java.io.OutputStream;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.encog.persist.EncogDirectoryPersistence.loadObject;
import static org.encog.persist.EncogDirectoryPersistence.saveObject;

// todo: add other types of Classifiers (Naive Bayes classifier for example)
public class NeroClassifierUnit extends ClassifierUnit {

    @Getter
    private final Characteristic characteristic;
    @Getter
    private final List<VocabularyWord> vocabulary;
    private final int inputLayerSize;
    private final int outputLayerSize;
    private final BasicNetwork network;
    private final NGramStrategy nGramStrategy;

    // CONSTRUCTORS

    NeroClassifierUnit(File trainedNetwork, @NonNull Characteristic characteristic, @NonNull List<VocabularyWord> vocabulary, @NonNull NGramStrategy nGramStrategy) {
        if (characteristic.getName().equals("") ||
                characteristic.getPossibleValues() == null ||
                characteristic.getPossibleValues().size() == 0 ||
                vocabulary.isEmpty()) {
            throw new IllegalArgumentException();
        }

        this.characteristic = characteristic;
        this.vocabulary = vocabulary;
        this.inputLayerSize = vocabulary.size();
        this.outputLayerSize = characteristic.getPossibleValues().size();
        this.nGramStrategy = nGramStrategy;

        if (trainedNetwork == null) {
            this.network = createNeuralNetwork();
        } else {
            // load neural network from file
            try {
                this.network = (BasicNetwork) loadObject(trainedNetwork);
            } catch (PersistError e) {
                throw new IllegalArgumentException();
            }
        }
    }

    // CLIENT SECTION

    public void build(List<ClassifiableText> classifiableTexts) {


        // prepare input and ideal vectors
        // input <- ClassifiableText text vector
        // ideal <- characteristicValue vector
        //

        double[][] input = getInput(classifiableTexts);
        double[][] ideal = getIdeal(classifiableTexts);

        // train
        //

        Propagation train = new ResilientPropagation(network, new BasicMLDataSet(input, ideal));
        train.setThreadCount(16);

        // todo: throw exception if iteration count more than 1000
        do {
            train.iteration();
            dispatch("Training Classifier for '" + characteristic.getName() + "' characteristic. Errors: " + String.format("%.2f", train.getError() * 100) + "%. Wait...");
        } while (train.getError() > 0.01);

        train.finishTraining();
        dispatch("Classifier for '" + characteristic.getName() + "' characteristic trained. Wait...");
    }

    public Optional<CharacteristicValue> classify(ClassifiableText classifiableText) {
        double[] output = new double[outputLayerSize];

        // calculate output vector
        network.compute(getTextAsVectorOfWords(classifiableText), output);
        Encog.getInstance().shutdown();

        return convertVectorToCharacteristic(output);
    }

    public void saveClassifier(File file) {
        saveObject(file, network);
        dispatch("Trained Classifier for '" + characteristic.getName() + "' characteristic saved. Wait...");
    }

    public void saveClassifier(OutputStream stream) {
        saveObject(stream, network);
        dispatch("Trained Classifier for '" + characteristic.getName() + "' characteristic saved. Wait...");
    }

    public void shutdown() {
        Encog.getInstance().shutdown();
    }

    // PRIVATE SECTION

    private BasicNetwork createNeuralNetwork() {
        BasicNetwork network = new BasicNetwork();

        // input layer
        network.addLayer(new BasicLayer(null, true, inputLayerSize));

        // hidden layer
        network.addLayer(new BasicLayer(new ActivationSigmoid(), true, inputLayerSize / 6));
        network.addLayer(new BasicLayer(new ActivationSigmoid(), true, inputLayerSize / 6 / 4));

        // output layer
        network.addLayer(new BasicLayer(new ActivationSigmoid(), false, outputLayerSize));

        network.getStructure().finalizeStructure();
        network.reset();

        return network;
    }

    private Optional<CharacteristicValue> convertVectorToCharacteristic(double[] vector) {
        int idOfMaxValue = getIdOfMaxValue(vector);

        // find CharacteristicValue with found Id
        //

        for (CharacteristicValue c : characteristic.getPossibleValues()) {
            if (c.getOrderNumber() == idOfMaxValue) {
                //todo: rewrite as optional
                return Optional.of(c);
            }
        }

        return Optional.empty();
    }

    private int getIdOfMaxValue(double[] vector) {
        int indexOfMaxValue = 0;
        double maxValue = vector[0];

        for (int i = 1; i < vector.length; i++) {
            if (vector[i] > maxValue) {
                maxValue = vector[i];
                indexOfMaxValue = i;
            }
        }

        return indexOfMaxValue + 1;
    }

    private double[][] getInput(List<ClassifiableText> classifiableTexts) {
        double[][] input = new double[classifiableTexts.size()][inputLayerSize];

        // convert all classifiable texts to vectors
        //

        int i = 0;

        for (ClassifiableText classifiableText : classifiableTexts) {
            input[i++] = getTextAsVectorOfWords(classifiableText);
        }

        return input;
    }

    private double[][] getIdeal(List<ClassifiableText> classifiableTexts) {
        double[][] ideal = new double[classifiableTexts.size()][outputLayerSize];

        // convert all classifiable text characteristics to vectors
        //

        int i = 0;

        for (ClassifiableText classifiableText : classifiableTexts) {
            ideal[i++] = getCharacteristicAsVector(classifiableText);
        }

        return ideal;
    }

    // example:
    // count = 5; id = 4;
    // vector = {0, 0, 0, 1, 0}
    private double[] getCharacteristicAsVector(ClassifiableText classifiableText) {

        int orderNumber = classifiableText.getCharacteristicValue(characteristic.getName()).getOrderNumber();

        if (orderNumber < 1 || orderNumber > outputLayerSize)
            throw new IllegalArgumentException("OrderNumber property of a characteristic value " +
                    "should start with 1 and be not greater than outputLayerSize");

        double[] vector = new double[outputLayerSize];

        vector[orderNumber - 1] = 1;
        return vector;
    }

    private double[] getTextAsVectorOfWords(ClassifiableText classifiableText) {
        double[] vector = new double[inputLayerSize];

        // convert text to nGramStrategy
        Set<String> uniqueValues = nGramStrategy.getNGram(classifiableText.getText());

        // create vector
        //

        for (String word : uniqueValues) {
            VocabularyWord vw = CharacteristicUtils.findByValue(vocabulary, word, DefVocabularyWord::new);

            if (vw != null) { // word found in vocabulary
                vector[vocabulary.indexOf(vw)] = 1;
            }
        }

        return vector;
    }

    // DO-KNOW-HOW-TO-NAME-IT

    @Override
    public String toString() {
        return characteristic.getName() + "NeuralNetworkClassifier";
    }

    @Override
    public void dispatch(String text) {
        listeners.forEach(action -> action.dispatch(ClassifierAction.EventTypes.NERO_CLASSIFIER_EVENT, text));
    }
}