package cn.edu.fudan.scanservice.dao;

import cn.edu.fudan.scanservice.domain.dbo.Tool;
import cn.edu.fudan.scanservice.mapper.ToolMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ToolDao {
    private ToolMapper toolMapper;

    public List<Tool> getAllTools(){
        return toolMapper.getAllTools ();
    }


    public Integer modifyToolEnabled(int id, int enabled){
        return toolMapper.modifyToolEnabled (id, enabled);
    }

    @Autowired
    public void setToolMapper(ToolMapper toolMapper) {
        this.toolMapper = toolMapper;
    }
}
