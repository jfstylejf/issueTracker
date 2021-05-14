package cn.edu.fudan.issueservice.domain.vo;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.Data;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * @author Beethoven
 */
@Data
public class PagedGridResult<T> {
    /**
     * page:页
     * total:总页数
     * records:总记录数
     * rows:每行显示内容
     */
    private int page;

    private int total;

    private long records;

    private List<T> rows;

    public static void handlePageHelper(int page, int ps, String order, Boolean isAsc) {
        if (StringUtils.isEmpty(order)) {
            PageHelper.startPage(page, ps);
        } else {
            String orderBy = order;
            if (isAsc != null && isAsc) {
                orderBy = order + ' ' + "asc";
            }
            if (isAsc != null && !isAsc) {
                orderBy = order + ' ' + "desc";
            }
            PageHelper.startPage(page, ps, orderBy);
        }
    }
}
