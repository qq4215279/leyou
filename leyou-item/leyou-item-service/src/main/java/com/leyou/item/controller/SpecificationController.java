package com.leyou.item.controller;

import com.leyou.item.pojo.SpecGroup;
import com.leyou.item.pojo.SpecParam;
import com.leyou.item.service.SpecificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Configuration
@RequestMapping("spec")
public class SpecificationController {

    @Autowired
    SpecificationService specificationService;



    @GetMapping("groups/{cid}")
    public ResponseEntity<List<SpecGroup>> queryGroupByCid(@PathVariable("cid")Long cid){

        List<SpecGroup> groups = this.specificationService.queryGroupByCid(cid);
        if (CollectionUtils.isEmpty( groups )){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok( groups );
    }

    @GetMapping("params")
    public ResponseEntity<List<SpecParam>> queryParamsByGid(
            @RequestParam(value = "gid",required = false)Long gid,
            @RequestParam(value = "cid",required = false)Long cid,
            @RequestParam(value = "generic",required = false)Boolean generic,
            @RequestParam(value = "searching",required = false)Boolean searching
    ){

        List<SpecParam> params = this.specificationService.queryParamByGid(gid,cid,generic,searching);
        if (CollectionUtils.isEmpty( params )){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok( params );

    }

    @GetMapping("{cid}")
    public ResponseEntity<List<SpecGroup>> querySpecsByCid(@PathVariable("cid") Long cid){
        List<SpecGroup> list = this.specificationService.querySpecsByCid(cid);
        if(list == null || list.size() == 0){
            return new ResponseEntity<>( HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(list);
    }

}
