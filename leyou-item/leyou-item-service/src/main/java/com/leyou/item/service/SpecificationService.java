package com.leyou.item.service;

import com.leyou.item.mapper.SpecGroupMapper;
import com.leyou.item.mapper.SpecParamMapper;
import com.leyou.item.pojo.SpecGroup;
import com.leyou.item.pojo.SpecParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SpecificationService {

    @Autowired
    SpecGroupMapper groupMapper;

    @Autowired
    SpecParamMapper paramMapper;

    public List<SpecGroup> queryGroupByCid(Long cid) {

        SpecGroup group = new SpecGroup();
        group.setCid( cid );
        return groupMapper.select( group );
    }


    public List<SpecParam> queryParamByGid(Long gid,Long cid,Boolean generic,Boolean searching) {

        SpecParam record = new SpecParam();
        record.setGroupId(gid);
        record.setCid(cid);
        record.setGeneric(generic);
        record.setSearching(searching);
        return this.paramMapper.select(record);
    }

    public List<SpecGroup> querySpecsByCid(Long cid) {
        // 查询规格组
        List<SpecGroup> groups = this.queryGroupByCid(cid);
        groups.forEach(g -> {
            // 查询组内参数
            g.setParams(this.queryParamByGid(g.getId(), null, null, null));
        });
        return groups;
    }
}
