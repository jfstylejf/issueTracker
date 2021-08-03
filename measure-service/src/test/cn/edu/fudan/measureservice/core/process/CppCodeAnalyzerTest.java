package cn.edu.fudan.measureservice.core.process;

import cn.edu.fudan.measureservice.domain.dto.FileInfo;
import cn.edu.fudan.measureservice.filter.FileFilter;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Value;

import static org.junit.Assert.*;
@Slf4j
public class CppCodeAnalyzerTest {


    String path = "/Users/keyon/Documents/bigDataPlatform/IssueTracker-Master/measure-service/src/test/" +
            "cn/edu/fudan/measureservice/core/process/cppTestFile/test1.cpp";
    @Test
    public void parseFile_method() throws Exception{

        FileInfo fileInfo = new FileInfo();
        fileInfo = CppCodeAnalyzer.parseFile(path);
        assertEquals("方法数不对",3,fileInfo.getMethodInfoList().size());
        //assertEquals("全局变量数不对",5,fileInfo.getGlobalParameterList().size());
    }
    @Test
    public void parseFile_global() throws Exception{

        FileInfo fileInfo = new FileInfo();
        fileInfo = CppCodeAnalyzer.parseFile(path);
        assertEquals("全局变量数不对",3,fileInfo.getGlobalParameterList().size());
    }

    @Test
    public void parseFile_paraSpe() throws Exception{

        FileInfo fileInfo = new FileInfo();
        fileInfo = CppCodeAnalyzer.parseFile(path);
        assertEquals("类成员属性不对","double ",fileInfo.getMemberList().get(0).getSpecifier());
    }

    @Test
    public void parseFile_paraName() throws Exception{

        FileInfo fileInfo = new FileInfo();
        fileInfo = CppCodeAnalyzer.parseFile(path);
        assertEquals("类成员名字不对","start_",fileInfo.getMemberList().get(0).getParameterName());
    }

    @Test
    public void parseFile_MethodSpe() throws Exception{

        FileInfo fileInfo = new FileInfo();
        fileInfo = CppCodeAnalyzer.parseFile(path);
        assertEquals("方法修饰符不对","void ",fileInfo.getMethodInfoList().get(0).getSpecifier());
    }

    @Test
    public void parseFile_MethodName() throws Exception{

        FileInfo fileInfo = new FileInfo();
        fileInfo = CppCodeAnalyzer.parseFile(path);
        assertEquals("方法名字不对","Start",fileInfo.getMethodInfoList().get(0).getMethodName());
    }

    @Test
    public void parseFile_MethodPara() throws Exception{

        FileInfo fileInfo = new FileInfo();
        fileInfo = CppCodeAnalyzer.parseFile(path);
        assertEquals("方法参数数量不对",2,fileInfo.getMethodInfoList().get(0).getMethodParameter().size());
    }



}