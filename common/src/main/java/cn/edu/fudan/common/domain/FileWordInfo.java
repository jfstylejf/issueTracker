package cn.edu.fudan.common.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.File;
import java.util.Map;

/**
 * @author wjzho
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileWordInfo {
    private File file;
    private Map<String,Integer> fileWordList;
    private int totalWordNum;
}
