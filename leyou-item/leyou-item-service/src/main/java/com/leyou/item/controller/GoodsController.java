package com.leyou.item.controller;

import com.leyou.common.pojo.PageResult;
import com.leyou.item.bo.SpuBo;
import com.leyou.item.pojo.Sku;
import com.leyou.item.pojo.Spu;
import com.leyou.item.pojo.SpuDetail;
import com.leyou.item.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;

@Controller
public class GoodsController {

    @Autowired
    GoodsService goodsService;

    @GetMapping("spu/page")
    public ResponseEntity<PageResult<SpuBo>> querySpuBoByPage(
        @RequestParam(value = "key",required = false)String key,
        @RequestParam(value = "saleable",required = false)Boolean saleable,
        @RequestParam(value = "page",defaultValue = "1")Integer page,
        @RequestParam(value = "rows",defaultValue = "5")Integer rows
    ){

        PageResult<SpuBo> pageResult = this.goodsService.querySpuBoByPage(key,saleable,page,rows);
        if (CollectionUtils.isEmpty( pageResult.getItems() )){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok( pageResult );
    }

    @PostMapping("goods")
    public ResponseEntity<Void> saveGoods(@RequestBody SpuBo spuBo){
        this.goodsService.saveGoods(spuBo);
        return ResponseEntity.status( HttpStatus.CREATED ).build();
    }

    @GetMapping("spu/detail/{spuid}")
    public ResponseEntity<SpuDetail> querySpuDetailBySpuId(@PathVariable("spuid")Long spuid){

        SpuDetail spuDetail = this.goodsService.querySpuDetailBySpuId(spuid);
        if (spuDetail == null){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok( spuDetail );
    }

    @GetMapping("sku/list")
    public ResponseEntity<List<Sku>> querySkusBySpuId(@RequestParam("id")Long spuId){

        List<Sku> list = this.goodsService.querySkusBySpuId(spuId);
        if (CollectionUtils.isEmpty( list )){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok( list );
    }

    @PutMapping("goods")
    public ResponseEntity<Void> updateGoods(@RequestBody SpuBo spuBo){
        this.goodsService.update(spuBo);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("spu/{id}")
    public ResponseEntity<Spu> querySpuById(@PathVariable("id") Long id){
        Spu spu = this.goodsService.querySpuById(id);
        if(spu == null){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(spu);
    }

    @GetMapping("sku/{id}")
    public ResponseEntity<Sku> querySkuById(@PathVariable("id")Long id){
        Sku sku = this.goodsService.querySkuById(id);
        if (sku == null){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(sku);
    }


}
