package org.ripreal.textclassifier2.model.modelimp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.ripreal.textclassifier2.model.VocabularyWord;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class DefVocabularyBayessWord implements VocabularyWord {
    private String id;

    private final String value;

    @Override
    public boolean equals(Object o) {
        return ((o instanceof DefVocabularyWord) && (this.value.equals(((DefVocabularyWord) o).getValue())));
    }

    @Override
    public int hashCode() {
        return this.value.hashCode();
    }
}
