package cn.edu.fudan.issueservice.util;

import cn.edu.fudan.issueservice.domain.dto.XmlError;
import org.jdom2.JDOMException;
import org.junit.Assert;
import org.junit.Test;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.List;

/**
 * @author beethoven
 * @date 2021-07-01 16:36:13
 */
public class XmlUtilTest {

    @Test
    public void getErrorsTest() throws IOException, JDOMException, SAXException {
        List<XmlError> error2 = XmlUtil.getError(System.getProperty("user.dir") + "/src/test/resources/testFile/err-test2.xml");
        Assert.assertEquals(error2.size(), 108);
    }
}
