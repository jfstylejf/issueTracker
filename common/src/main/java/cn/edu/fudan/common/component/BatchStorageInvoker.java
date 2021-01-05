package cn.edu.fudan.common.component;

import cn.edu.fudan.common.domain.CommonInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * @author Song Rui
 */
public class BatchStorageInvoker {
    private static final Logger log = LoggerFactory.getLogger(BatchStorageInvoker.class);
    private static final int PAGE_SIZE = 5000;

    public static <T> void batchStorage(Object object, String methodName, int pageSize, List<T> dataList, CommonInfo commonInfo) {
        if (object != null && methodName.length() > 0) {
            if (dataList != null && !dataList.isEmpty()) {
                pageSize = pageSize > 0 ? pageSize : 5000;
                int pageNum = dataList.size() / pageSize + (dataList.size() % pageSize > 0 ? 1 : 0);
                int num = 0;

                try {
                    Method method = object.getClass().getMethod(methodName, List.class, CommonInfo.class);
                    if (pageNum <= 1) {
                        method.invoke(object, dataList, commonInfo);
                        return;
                    }

                    for(int i = 1; i <= pageNum; ++i) {
                        List<T> subList = divideBatchList(i, pageSize, dataList);
                        num += (Integer)method.invoke(object, subList, commonInfo);
                    }
                } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException var10) {
                    var10.printStackTrace();
                }

                log.info("{} records has been saved by {}", num, methodName);
            }
        } else {
            throw new IllegalArgumentException("dao or invoke method is null");
        }
    }

    private static <T> List<T> divideBatchList(int pageNum, int pageSize, List<T> dataList) {
        return dataList.subList((pageNum - 1) * pageSize, Math.min(pageNum * pageSize, dataList.size()));
    }

    BatchStorageInvoker() {
    }
}

