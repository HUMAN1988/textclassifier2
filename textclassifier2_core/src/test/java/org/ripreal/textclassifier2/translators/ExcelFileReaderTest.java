package org.ripreal.textclassifier2.translators;

import org.junit.Test;
import org.ripreal.textclassifier2.model.ClassifiableFactory;
import org.ripreal.textclassifier2.model.ClassifiableText;
import org.ripreal.textclassifier2.model.modelimp.DefClassifiableFactory;

import java.io.File;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ExcelFileReaderTest {
    private final ExcelFileTranslator excelFileReader = new ExcelFileTranslator(new File("./test_db/test.xlsx"), 1, new DefClassifiableFactory());
    private final ClassifiableFactory characteristicFactory = new DefClassifiableFactory();

    @Test
    public void xlsxToClassifiableTexts() throws Exception {
        List<ClassifiableText> classifiableTexts = excelFileReader.toClassifiableTexts();

        assertEquals(classifiableTexts.size(), 18);

        assertEquals(classifiableTexts.get(0).getText(), "Требуется починить телефон");
        assertEquals(classifiableTexts.get(0).getCharacteristics().size(), 2);
        //assertEquals(classifiableTexts.get(0).getCharacteristics().get(characteristicFactory.newCharacteristic("Отдел")).getValue(), "Автосалон");
        //assertEquals(classifiableTexts.get(0).getCharacteristics().get(characteristicFactory.newCharacteristic("Тип")).getValue(), "Техподдержка");

        assertEquals(classifiableTexts.get(1).getText(), "У меня не проводится документ");
        assertEquals(classifiableTexts.get(1).getCharacteristics().size(), 2);
        //assertEquals(classifiableTexts.get(1).getCharacteristics().get(characteristicFactory.newCharacteristic("Отдел")).getValue(), "Бухгалтерия");
        //assertEquals(classifiableTexts.get(1).getCharacteristics().get(characteristicFactory.newCharacteristic("Тип")).getValue(), "Разработчики");

    }


    @Test
    public void nonExistentFile() {
        ExcelFileTranslator reader = new ExcelFileTranslator(new File("incorrect_path"), 1, characteristicFactory);

        reader.subscribe((action, text) -> assertTrue(!text.isEmpty()));
        reader.toClassifiableTexts();
    }

    @Test
    public void IncorrectSheetNumber() {
        ExcelFileTranslator reader = new ExcelFileTranslator(new File("./test_db/test.xlsx"), 2, characteristicFactory);
        reader.subscribe((action, text) -> assertTrue(!text.isEmpty()));
        reader.toClassifiableTexts();
    }
}