package cn.edu.fudan.issueservice.domain.vo;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.Data;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
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

    public PagedGridResult<T> setterPagedGrid(List<T> list, Integer page) {
        PageInfo<?> pageList = new PageInfo<>(list);
        PagedGridResult<T> grid = new PagedGridResult<>();
        grid.setPage(page);
        grid.setRows(list);
        grid.setTotal(pageList.getPages());
        grid.setRecords(pageList.getTotal());
        return grid;
    }

    public static PagedGridResult<?> getPagedGridResult(int page, int records, int size, List<Object> list) {
        PagedGridResult<Object> result = new PagedGridResult<>();
        result.page = page;
        result.records = records;
        result.total = size;
        result.rows = new ArrayList<>();
        list.forEach(r -> result.rows.addAll((Collection<?>) r));
        return result;
    }
}
