package org.ripreal.textclassifier2.classifier;

import lombok.Getter;
import lombok.NonNull;
import org.encog.Encog;
import org.encog.ml.data.basic.BasicMLDataSet;
import org.encog.neural.networks.training.propagation.Propagation;
import org.encog.neural.networks.training.propagation.resilient.ResilientPropagation;
import org.ripreal.textclassifier2.model.Characteristic;
import org.ripreal.textclassifier2.model.CharacteristicValue;
import org.ripreal.textclassifier2.model.ClassifiableText;
import org.ripreal.textclassifier2.model.VocabularyWord;
import org.ripreal.textclassifier2.ngram.NGramStrategy;

import java.io.File;
import java.io.OutputStream;
import java.util.List;
import java.util.Optional;

import static org.encog.persist.EncogDirectoryPersistence.saveObject;

public class BayessClassifierUnit extends ClassifierUnit{
    @Getter
    private final Characteristic characteristic;
    private final int outputLayerSize;
    @Getter
    private final List<VocabularyWord> vocabulary;
    private final int inputLayerSize;
    private final NGramStrategy nGramStrategy;
    // CONSTRUCTOR

    BayessClassifierUnit(File trainedNetwork, @NonNull Characteristic characteristic, @NonNull List<VocabularyWord> vocabulary, @NonNull NGramStrategy nGramStrategy){
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
    }

    public void build(List<ClassifiableText> classifiableTexts) {

    }

    public Optional<CharacteristicValue> classify(ClassifiableText classifiableText) {
        double[] output = new double[outputLayerSize];

        return convertVectorToCharacteristic(output);
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

    public void saveClassifier(File file) {

    }

    public void saveClassifier(OutputStream stream) {

    }

    public void shutdown() {

    }

}
