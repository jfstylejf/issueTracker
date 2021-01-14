package cn.edu.fudan.common.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author wjzho
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Word_TF_IDF {
    private String word;
    private double termFrequency;
    private double inverseDocumentFrequency;
    private double weight;

    public void cal() {
        this.weight = termFrequency * inverseDocumentFrequency;
    }

}
