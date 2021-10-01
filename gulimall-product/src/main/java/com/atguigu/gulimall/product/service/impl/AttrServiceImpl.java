package com.atguigu.gulimall.product.service.impl;

import com.atguigu.gulimall.product.dao.AttrAttrgroupRelationDao;
import com.atguigu.gulimall.product.dao.AttrGroupDao;
import com.atguigu.gulimall.product.dao.CategoryDao;
import com.atguigu.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.atguigu.gulimall.product.entity.AttrGroupEntity;
import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.AttrAttrgroupRelationService;
import com.atguigu.gulimall.product.service.CategoryService;
import com.atguigu.gulimall.product.vo.AttrRespVo;
import com.atguigu.gulimall.product.vo.AttrVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.product.dao.AttrDao;
import com.atguigu.gulimall.product.entity.AttrEntity;
import com.atguigu.gulimall.product.service.AttrService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;


@Service("attrService")
public class AttrServiceImpl extends ServiceImpl<AttrDao, AttrEntity> implements AttrService {

    @Autowired
//    private AttrAttrgroupRelationService attrAttrgroupRelationService;
    private AttrAttrgroupRelationDao attrAttrgroupRelationDao;

    @Autowired
    private CategoryDao categoryDao;

    @Autowired
    private AttrGroupDao attrGroupDao;

    @Autowired
    private CategoryService categoryService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                new QueryWrapper<AttrEntity>()
        );

        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void saveAttr(AttrVo attr) {
        //需求1：要保存本身属性表的数据
        AttrEntity attrEntity = new AttrEntity();
        //把数据拷贝过去
        BeanUtils.copyProperties(attr, attrEntity);
        this.save(attrEntity);

        //需求2：保存属性、属性分组关联表数据,就要用到Vo实体
        AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = new AttrAttrgroupRelationEntity();
        //把数据拿出来再放到实体里，最后保存
        attrAttrgroupRelationEntity.setAttrGroupId(attr.getAttrGroupId());
        attrAttrgroupRelationEntity.setAttrId(attrEntity.getAttrId());
//        attrAttrgroupRelationService.save(attrAttrgroupRelationEntity);
        attrAttrgroupRelationDao.insert(attrAttrgroupRelationEntity);

    }


    @Override
    public PageUtils queryBaseAttrPage(Map<String, Object> params, Long catelogId) {

        //1.查询全部
        QueryWrapper<AttrEntity> wrapper = new QueryWrapper<>();


        if (catelogId != 0) {
            wrapper.eq("catelog_id", catelogId);
        }


        //模糊查询
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            wrapper.and((obj) -> {
                obj.eq("attr_id", key).or().like("attr_name", key);
            });
        }


        //最后封装到page返回
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                wrapper);

        PageUtils pageUtils = new PageUtils(page);
        //得到它获取到到记录
        List<AttrEntity> records = page.getRecords();
        //用流式编程，映射其中每一个元素
        List<AttrRespVo> respVos = records.stream().map((attrEntity) -> {
            //把原实体的信息拷贝过去
            AttrRespVo attrRespVo = new AttrRespVo();
            BeanUtils.copyProperties(attrEntity, attrRespVo);
            //然后就剩下两条新字段需要查询

            //查询所属属性分组名
            AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = attrAttrgroupRelationDao.selectOne(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrEntity.getAttrId()));
            if (attrAttrgroupRelationEntity != null) {
                AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrAttrgroupRelationEntity.getAttrGroupId());
                //搞到groupName
                attrRespVo.setGroupName(attrGroupEntity.getAttrGroupName());
            }

            //查询所属分类名
            //根据所属分类id，查询分类表实体
            CategoryEntity categoryEntity = categoryDao.selectById(attrEntity.getCatelogId());
            if (categoryEntity != null) {
                //搞到cateLogName
                attrRespVo.setCatelogName(categoryEntity.getName());
            }

            return attrRespVo;
        }).collect(Collectors.toList());

        pageUtils.setList(respVos);
        return pageUtils;
    }

    @Override
    public AttrRespVo getByIdInfo(Long attrId) {

        AttrEntity attrEntity = this.getById(attrId);
        AttrRespVo attrRespVo = new AttrRespVo();
        BeanUtils.copyProperties(attrEntity, attrRespVo);

        //设置所属属性分组名字，用属性id查关系表的分组id，查分组表的分组名字，然后设置
        AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = attrAttrgroupRelationDao.selectOne(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrId));
        if (attrAttrgroupRelationEntity != null) {
            attrRespVo.setAttrGroupId(attrAttrgroupRelationEntity.getAttrGroupId());
            AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrAttrgroupRelationEntity.getAttrGroupId());
            if (attrGroupEntity != null) {
                attrRespVo.setGroupName(attrGroupEntity.getAttrGroupName());
            }
        }

        //设置所属分类完整路径,原实体获取分类id，然乎注入分类服务，使用之前写过的查完整路径的方法
        Long catelogId = attrEntity.getCatelogId();
        Long[] catelogPath = categoryService.findCatelogPath(catelogId);
        attrRespVo.setCatelogPath(catelogPath);

        CategoryEntity categoryEntity = categoryDao.selectById(catelogId);
        if (categoryEntity != null) {
            attrRespVo.setCatelogName(categoryEntity.getName());
        }

        return attrRespVo;
    }

    @Transactional
    @Override
    public void updateAttr(AttrVo attr) {
        //先把基本数据改了
        AttrEntity attrEntity = new AttrEntity();
        BeanUtils.copyProperties(attr, attrEntity);
        this.updateById(attrEntity);


        //修改vo加进去的数据
        AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = new AttrAttrgroupRelationEntity();

        //要更新哪个值，先是设置好实体数据，后面再改到数据库
        attrAttrgroupRelationEntity.setAttrGroupId(attr.getAttrGroupId());
        attrAttrgroupRelationEntity.setAttrId(attr.getAttrId());

        //判断关系表中，这个属性id有没有分组id，有的话就改，没有的话就插入
        Integer count = attrAttrgroupRelationDao.selectCount(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attr.getAttrId()));
        if (count > 0) {
            attrAttrgroupRelationDao.update(attrAttrgroupRelationEntity,
                    new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attr.getAttrId()));
        }else {
            attrAttrgroupRelationDao.insert(attrAttrgroupRelationEntity);
        }


    }

}