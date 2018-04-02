package org.ripreal.textclassifier2.classifier;

import lombok.*;
import org.ripreal.textclassifier2.CharacteristicUtils;
import org.ripreal.textclassifier2.actions.ClassifierAction;
import org.ripreal.textclassifier2.model.Characteristic;
import org.ripreal.textclassifier2.model.ClassifiableFactory;
import org.ripreal.textclassifier2.model.VocabularyWord;
import org.ripreal.textclassifier2.ngram.NGramStrategy;
import org.ripreal.textclassifier2.translators.ClassifiableTranslator;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

// BUILDER + COMPOSITE
@RequiredArgsConstructor
public final class ClassifierBuilder {

    @NonNull
    private final ClassifiableTranslator reader;
    @NonNull
    private final ClassifiableFactory characteristicFactory;
    @NonNull
    private final List<ClassifierUnitProxy> classifierUnits = new ArrayList<>();
    @NonNull
    private final List<ClassifierAction> listeners = new ArrayList<>();
    private final int AMOUNT_OF_TEXTS_FOR_CHECKING = 5;

    // CONSTRUCTORS

    public static ClassifierBuilder fromReader(@NonNull ClassifiableTranslator reader) {
        return new ClassifierBuilder(reader, reader.getCharacteristicFactory());
    }

    // CLIENT SECTION

    public ClassifierBuilder addNeroClassifierUnit(@NonNull String characteristicName, @NonNull NGramStrategy nGramStrategy) {
        addNeroClassifierUnit(null, characteristicName, null, nGramStrategy);
        return this;
    }

    public ClassifierBuilder addNeroClassifierUnit(File trainedClassifier, @NonNull String characteristicName, List<VocabularyWord> vocabulary, @NonNull NGramStrategy nGramStrategy) {
        classifierUnits.add(
                new ClassifierUnitProxy(
                        NeroClassifierUnit::new,
                        trainedClassifier,
                        nGramStrategy,
                        vocabulary,
                        characteristicFactory.newCharacteristic(characteristicName)
                ));
        return this;
    }

    public ClassifierBuilder addBayessClassifierUnit(@NonNull String characteristicName, @NonNull NGramStrategy nGramStrategy) {
        addBayessClassifierUnit(null, characteristicName, null, nGramStrategy);
        return this;
    }

    public ClassifierBuilder addBayessClassifierUnit(File trainedClassifier, @NonNull String characteristicName, List<VocabularyWord> vocabulary, @NonNull NGramStrategy nGramStrategy) {
        classifierUnits.add(
                new ClassifierUnitProxy(
                        BayessClassifierUnit::new,
                        trainedClassifier,
                        nGramStrategy,
                        vocabulary,
                        characteristicFactory.newCharacteristic(characteristicName)
                ));
        return this;
    }

    public ClassifierBuilder subscribe(@NonNull ClassifierAction action) {
        listeners.add(action);
        reader.subscribe(action);
        return this;
    }

    public Classifier build() {

        if (!initialized()) {
            dispatch("Error. No classifier units were specified!");
            return null;
        }

        List<ClassifierUnit> units = buildClassifiers();

        shutDownClassifiers(units);

        return new Classifier(units);
    }

    // INNER SECTION

    private List<ClassifierUnit> buildClassifiers() {

        Set<Characteristic> characteristics = reader.toCharacteristics();

        List<ClassifierUnit> units = new ArrayList<>();
        for (ClassifierUnitProxy proxy : classifierUnits) {

            proxy.setVocabulary(reader.toVocabulary(proxy.getNGramStrategy()));

            proxy.setCharacteristic(
                    CharacteristicUtils.findByValue(
                            characteristics,
                            proxy.getCharacteristic().getName(),
                            characteristicFactory::newCharacteristic)
            );

            ClassifierUnit unit = proxy.get();
            listeners.forEach(unit::subscribe);

            unit.build(reader.toClassifiableTexts());

            units.add(unit);
        }

        reader.reset();

        return units;
    }

    private boolean initialized() {
        return !(reader == null || classifierUnits.size() == 0);
    }

    private void shutDownClassifiers(List<ClassifierUnit> units) {
        for (ClassifierUnit classifier : units) {
            classifier.shutdown();
        }
    }

    private void dispatch(@NonNull String text) {
        listeners.forEach(action -> action.dispatch(ClassifierAction.EventTypes.CLASSIFIER_EVENT, text));
    }

    @RequiredArgsConstructor
    @AllArgsConstructor
    class ClassifierUnitProxy {
        private final ClassifierUnitSupplier supplier;
        @Getter
        private final File trainedClassifier;
        @Getter
        private final NGramStrategy nGramStrategy;
        @Getter
        @Setter
        private List<VocabularyWord> vocabulary;
        @Getter
        @Setter
        private Characteristic characteristic;

        public ClassifierUnit get() {
            return supplier.get(trainedClassifier, characteristic, vocabulary, nGramStrategy);
        }

        ;
    }

    @FunctionalInterface
    interface ClassifierUnitSupplier {
        ClassifierUnit get(File trainedClassifier, Characteristic characteristic, List<VocabularyWord> vocabulary, NGramStrategy nGramStrategy);
    }
}

