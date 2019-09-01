package com.leyou.item.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.pojo.PageResult;
import com.leyou.item.bo.SpuBo;
import com.leyou.item.mapper.*;
import com.leyou.item.pojo.Sku;
import com.leyou.item.pojo.Spu;
import com.leyou.item.pojo.SpuDetail;
import com.leyou.item.pojo.Stock;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GoodsService {

    @Autowired
    SpuMapper spuMapper;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private BrandMapper brandMapper;

    @Autowired
    private SpuDetailMapper spuDetailMapper;

    @Autowired
    private SkuMapper skuMapper;

    @Autowired
    private StockMapper stockMapper;

    @Autowired
    private AmqpTemplate amqpTemplate;

    private Logger logger = LoggerFactory.getLogger( GoodsService.class );

    public void sendMassage(Long id,String type){
        // 发送消息
        try {
            this.amqpTemplate.convertAndSend( "item."+type,id );
        } catch (AmqpException e) {

            logger.error("{}商品消息发送异常，商品id：{}", type, id, e);
        }
    }

    private void saveSkuAndStock(SpuBo spuBo) {
        spuBo.getSkus().forEach( sku -> {
            // 新增sku
            sku.setSpuId( spuBo.getId() );
            sku.setCreateTime( new Date() );
            sku.setLastUpdateTime( sku.getCreateTime() );
            this.skuMapper.insertSelective( sku );

            // 新增库存
            Stock stock = new Stock();
            stock.setSkuId( sku.getId() );
            stock.setStock( sku.getStock() );
            this.stockMapper.insertSelective( stock );

        } );
    }

    public PageResult<SpuBo> querySpuBoByPage(String key,Boolean saleable,Integer page,Integer rows) {

        Example example = new Example( Spu.class );
        Example.Criteria criteria = example.createCriteria();

        if (StringUtils.isNotBlank( key )){
            criteria.andLike( "title","%"+key+"%" );
        }
        if (saleable!=null){
            criteria.andEqualTo( "saleable",saleable );
        }

        PageHelper.startPage( page,rows );

        List<Spu> spus = this.spuMapper.selectByExample( example );
        PageInfo<Spu> pageInfo = new PageInfo<>( spus );

        List<SpuBo> spuBos = new ArrayList<>();
        spus.forEach(spu->{
            SpuBo spuBo = new SpuBo();
            // copy共同属性的值到新的对象
            BeanUtils.copyProperties(spu, spuBo);
            // 查询分类名称
            List<String> names = this.categoryService.queryNamesByIds( Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));
            spuBo.setCname(StringUtils.join(names, "/"));

            // 查询品牌的名称
            spuBo.setBname(this.brandMapper.selectByPrimaryKey(spu.getBrandId()).getName());
            spuBos.add(spuBo);
        });

        return new PageResult<>(pageInfo.getTotal(), spuBos);

    }

    @Transactional
    public void saveGoods(SpuBo spuBo) {

        // 新增spu
        // 设置默认字段
        spuBo.setId(null);
        spuBo.setSaleable(true);
        spuBo.setValid(true);
        spuBo.setCreateTime(new Date());
        spuBo.setLastUpdateTime(spuBo.getCreateTime());
        this.spuMapper.insertSelective(spuBo);

        // 新增spuDetail
        SpuDetail spuDetail = spuBo.getSpuDetail();
        spuDetail.setSpuId(spuBo.getId());
        this.spuDetailMapper.insertSelective(spuDetail);

        saveSkuAndStock(spuBo);

        this.sendMassage( spuBo.getId(),"insert" );
    }


    public SpuDetail querySpuDetailBySpuId(Long spuid) {

        return this.spuDetailMapper.selectByPrimaryKey( spuid );
    }

    public List<Sku> querySkusBySpuId(Long spuId) {

        Sku sku = new Sku();
        sku.setSpuId(spuId);
        List<Sku> skus = this.skuMapper.select(sku);
        skus.forEach(s -> {
            Stock stock = this.stockMapper.selectByPrimaryKey(s.getId());
            s.setStock(stock.getStock());
        });
        return skus;

    }

    @Transactional
    public void update(SpuBo spu) {
        // 查询以前sku
        List<Sku> skus = this.querySkusBySpuId(spu.getId());
        // 如果以前存在，则删除
        if(!CollectionUtils.isEmpty(skus)) {
            List<Long> ids = skus.stream().map(s -> s.getId()).collect( Collectors.toList());
            // 删除以前库存
            Example example = new Example(Stock.class);
            example.createCriteria().andIn("skuId", ids);
            this.stockMapper.deleteByExample(example);

            // 删除以前的sku
            Sku record = new Sku();
            record.setSpuId(spu.getId());
            this.skuMapper.delete(record);

        }
        // 新增sku和库存
        saveSkuAndStock(spu);

        // 更新spu
        spu.setLastUpdateTime(new Date());
        spu.setCreateTime(null);
        spu.setValid(null);
        spu.setSaleable(null);
        this.spuMapper.updateByPrimaryKeySelective(spu);

        // 更新spu详情
        this.spuDetailMapper.updateByPrimaryKeySelective(spu.getSpuDetail());

        this.sendMassage( spu.getId(),"update" );
    }

    public Spu querySpuById(Long id) {
        return this.spuMapper.selectByPrimaryKey(id);
    }

    public Sku querySkuById(Long id) {
        return this.skuMapper.selectByPrimaryKey(id);
    }
}
