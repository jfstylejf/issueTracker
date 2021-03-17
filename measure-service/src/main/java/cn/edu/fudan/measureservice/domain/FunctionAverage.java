package cn.edu.fudan.measureservice.domain;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FunctionAverage {
    private double ncss;
    private double ccn;
    private double javaDocs;
}
