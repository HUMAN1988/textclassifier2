package org.ripreal.textclassifier2.classifier;

import org.ripreal.textclassifier2.actions.ClassifierEventsDispatcher;
import org.ripreal.textclassifier2.model.Characteristic;
import org.ripreal.textclassifier2.model.CharacteristicValue;
import org.ripreal.textclassifier2.model.ClassifiableText;

import java.io.File;
import java.io.OutputStream;
import java.util.List;

abstract class ClassifierUnit extends ClassifierEventsDispatcher {

    // PROPERTIES

    abstract public Characteristic getCharacteristic();

    // BUILDING

    abstract void build(List<ClassifiableText> classifiableTexts);

    // COMPOSITE METHODS

    abstract public CharacteristicValue classify(ClassifiableText classifiableText);

    abstract public void saveTrainedClassifier(File file);

    abstract public void saveTrainedClassifier(OutputStream stream);

    abstract public void shutdown();

}