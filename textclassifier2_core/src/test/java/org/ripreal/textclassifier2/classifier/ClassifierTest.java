package org.ripreal.textclassifier2.classifier;

import org.junit.Before;
import org.junit.Test;
import org.ripreal.textclassifier2.model.*;
import org.ripreal.textclassifier2.model.modelimp.DefCharacteristicFactory;
import org.ripreal.textclassifier2.ngram.NGramStrategy;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class ClassifierTest {

    private final File trainedClassifier = new File("./test_db/TestNeuralNetworkClassifier");
    private final NGramStrategy nGramStrategy = NGramStrategy.getNGramStrategy(NGramStrategy.NGRAM_TYPES.FILTERED_UNIGRAM);
    private Classifier classifier;
    private Characteristic characteristic;
    private List<VocabularyWord> vocabulary;
    private CharacteristicFactory characteristicFactory;

    @Before
    public void init() {
        // create characteristic
        //
         characteristicFactory = new DefCharacteristicFactory();

        characteristic = characteristicFactory.newCharacteristic("Method");
        characteristic.addPossibleValue(characteristicFactory.newCharacteristicValue("get", 1, characteristic));
        characteristic.addPossibleValue(characteristicFactory.newCharacteristicValue("set", 2, characteristic));
        characteristic.addPossibleValue(characteristicFactory.newCharacteristicValue("add", 3, characteristic));

        // create vocabulary
        //

        vocabulary = new ArrayList<>();
        vocabulary.add(characteristicFactory.newVocabularyWord("their"));
        vocabulary.add(characteristicFactory.newVocabularyWord("specified"));
        vocabulary.add(characteristicFactory.newVocabularyWord("that"));
        vocabulary.add(characteristicFactory.newVocabularyWord("and"));
        vocabulary.add(characteristicFactory.newVocabularyWord("shifts"));
        vocabulary.add(characteristicFactory.newVocabularyWord("subsequent"));
        vocabulary.add(characteristicFactory.newVocabularyWord("if"));
        vocabulary.add(characteristicFactory.newVocabularyWord("element"));
        vocabulary.add(characteristicFactory.newVocabularyWord("adds"));
        vocabulary.add(characteristicFactory.newVocabularyWord("in"));
        vocabulary.add(characteristicFactory.newVocabularyWord("replaces"));
        vocabulary.add(characteristicFactory.newVocabularyWord("one"));
        vocabulary.add(characteristicFactory.newVocabularyWord("this"));
        vocabulary.add(characteristicFactory.newVocabularyWord("optional"));
        vocabulary.add(characteristicFactory.newVocabularyWord("right"));
        vocabulary.add(characteristicFactory.newVocabularyWord("list"));
        vocabulary.add(characteristicFactory.newVocabularyWord("any"));
        vocabulary.add(characteristicFactory.newVocabularyWord("the"));
        vocabulary.add(characteristicFactory.newVocabularyWord("with"));
        vocabulary.add(characteristicFactory.newVocabularyWord("indices"));
        vocabulary.add(characteristicFactory.newVocabularyWord("at"));
        vocabulary.add(characteristicFactory.newVocabularyWord("currently"));
        vocabulary.add(characteristicFactory.newVocabularyWord("elements"));
        vocabulary.add(characteristicFactory.newVocabularyWord("returns"));
        vocabulary.add(characteristicFactory.newVocabularyWord("position"));
        vocabulary.add(characteristicFactory.newVocabularyWord("to"));
        vocabulary.add(characteristicFactory.newVocabularyWord("operation"));
        vocabulary.add(characteristicFactory.newVocabularyWord("inserts"));

        boolean exist = trainedClassifier.exists();
        File file = new File("./t1");
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(file.toPath().toAbsolutePath().toString());
        classifier = new Classifier(
            Collections.singletonList(
                new NeroClassifierUnit(trainedClassifier, characteristic, vocabulary, nGramStrategy))
        );

   }

    @Test(expected = NullPointerException.class)
    public void nullCharacteristic() {
        new NeroClassifierUnit(trainedClassifier, null, vocabulary, nGramStrategy);
    }

    @Test(expected = IllegalArgumentException.class)
    public void emptyCharacteristic() {
        new NeroClassifierUnit(trainedClassifier, characteristicFactory.newCharacteristic("Test"), vocabulary, nGramStrategy);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullVocabulary() {
        new NeroClassifierUnit(trainedClassifier, characteristic, null, nGramStrategy);
    }

    @Test(expected = IllegalArgumentException.class)
    public void emptyVocabulary() {
        new NeroClassifierUnit(trainedClassifier, characteristic, new ArrayList<>(), nGramStrategy);
    }

    @Test(expected = NullPointerException.class)
    public void nullNGram() {
        new NeroClassifierUnit(trainedClassifier, characteristic, vocabulary, null);
    }

    @Test
    public void classify() throws Exception {

        ClassifiableText ctGet = characteristicFactory.newClassifiableText("Returns the element at the specified position in this list");
        List<CharacteristicValue> cvGet = classifier.classify(ctGet);

        assertEquals(cvGet.get(0).getOrderNumber(), 1);
        assertEquals(cvGet.get(0).getValue(), "get");

        //

        ClassifiableText ctSet = characteristicFactory.newClassifiableText("Replaces the element at the specified position in this list with the specified element (optional operation)");
        List<CharacteristicValue> cvSet = classifier.classify(ctSet);

        assertEquals(cvSet.get(0).getOrderNumber(), 2);
        assertEquals(cvSet.get(0).getValue(), "set");

        //

        ClassifiableText ctAdd = characteristicFactory.newClassifiableText("Inserts the specified element at the specified position in this list (optional operation). Shifts the element currently at that position (if any) and any subsequent elements to the right (adds one to their indices)");
        List<CharacteristicValue> cvAdd = classifier.classify(ctAdd);

        assertEquals(cvAdd.get(0).getOrderNumber(), 3);
        assertEquals(cvAdd.get(0).getValue(), "add");
    }

    @Test
    public void saveTrainedClassifier() throws Exception {
        classifier.saveClassifiers(new File("./test_db/TestSave"));
        assertEquals(new File("./test_db/TestSave").delete(), true);
    }

    @Test
    public void build() throws Exception {

        List<ClassifiableText> classifiableTexts = new ArrayList<>();

        Map<Characteristic, CharacteristicValue> characteristics = new HashMap<>();
        characteristics.put(characteristicFactory.newCharacteristic("Method"), characteristicFactory.newCharacteristicValue("get", 1, characteristic));
        classifiableTexts.add(characteristicFactory.newClassifiableText("shifts right any this operation", characteristics));

        characteristics = new HashMap<>();
        characteristics.put(characteristicFactory.newCharacteristic("Method"), characteristicFactory.newCharacteristicValue("add", 3, characteristic));
        classifiableTexts.add(characteristicFactory.newClassifiableText("that at returns", characteristics));

        // make sure classifier is stupid
        //

        assertNotEquals(classifier.classify(classifiableTexts.get(0)).get(0).getValue(), "get");
        assertNotEquals(classifier.classify(classifiableTexts.get(1)).get(0).getValue(), "add");

        // train
        classifier.build(classifiableTexts);

        // make sure classifier became smart
        //

        assertEquals(classifier.classify(classifiableTexts.get(0)).get(0).getValue(), "get");
        assertEquals(classifier.classify(classifiableTexts.get(1)).get(0).getValue(), "add");
        assertEquals(classifier.classify(characteristicFactory.newClassifiableText("shifts right sdawwda any this operation")).get(0).getValue(), "get");
    }

    @Test
    public void shutdown() throws Exception {
        classifier.shutdown();
    }

}