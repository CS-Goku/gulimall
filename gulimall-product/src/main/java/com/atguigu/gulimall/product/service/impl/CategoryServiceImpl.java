package com.atguigu.gulimall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.product.dao.CategoryDao;
import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.CategoryBrandRelationService;
import com.atguigu.gulimall.product.service.CategoryService;
import com.atguigu.gulimall.product.vo.Catelog2Vo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

//    @Autowired
//    CategoryDao categoryDao;

//    private Map<String,Object> cache = new HashMap<>();

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RedissonClient redisson;
    @Autowired
    CategoryBrandRelationService categoryBrandRelationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listWithTree() {
        //1、查出所有分类,这个实现类已经继承类服务层的泛型dao，直接调用用baseMapper就好
        List<CategoryEntity> entities = baseMapper.selectList(null);

        //2、组装成父子的树形结构

        //2.1）、找到所有的一级分类
        List<CategoryEntity> level1Menus = entities.stream().filter(categoryEntity ->//所有分类用stream的过滤方法，根据条件找到一级分类
                categoryEntity.getParentCid() == 0//一级分类的条件
        ).map(menu -> {//接着用这个一级分类
            menu.setChildren(getChildrens(menu, entities));//菜单的实体类，添加一个private List<CategoryEntity> children;用来装23级分类，获取用到递归方法
            return menu;
        }).sorted((menu1, menu2) -> {//分类排序
            return (menu1.getSort() == null ? 0 : menu1.getSort()) - (menu2.getSort() == null ? 0 : menu2.getSort());
        }).collect(Collectors.toList());//收集


        return level1Menus;
    }
    //递归查找所有菜单的子菜单
    private List<CategoryEntity> getChildrens(CategoryEntity root, List<CategoryEntity> all) {//传递过来一级分类和所有分类

        List<CategoryEntity> children = all.stream().filter(categoryEntity -> {//找到二级分类
            return categoryEntity.getParentCid() == root.getCatId();//条件
        }).map(categoryEntity -> {//拿着二级分类
            //1、找到子菜单
            categoryEntity.setChildren(getChildrens(categoryEntity, all));//递归找三级分类
            return categoryEntity;
        }).sorted((menu1, menu2) -> {//排序
            //2、菜单的排序
            return (menu1.getSort() == null ? 0 : menu1.getSort()) - (menu2.getSort() == null ? 0 : menu2.getSort());
        }).collect(Collectors.toList());//收集好

        return children;
    }


    @Override
    public void removeMenuByIds(List<Long> asList) {
        //TODO  1、检查当前删除的菜单，是否被别的地方引用，还不确定

        //逻辑删除
        baseMapper.deleteBatchIds(asList);
    }

    //[2,25,225]
    @Override
    public Long[] findCatelogPath(Long catelogId) {
        //创建一个集合用来收集路径
        List<Long> paths = new ArrayList<>();
        //调用递归收集父分类
        paths = findParentPath(catelogId, paths); //[255,25,2]
        //反转一下
        Collections.reverse(paths);

        return paths.toArray(new Long[paths.size()]);//把集合变数组
    }

    //[255,25,2]
    //递归收集所有父分类
    private List<Long> findParentPath(Long catelogId, List<Long> paths) {
        //先把当前id放进集合
        paths.add(catelogId);
        //搜索这个id的实体
        CategoryEntity byId = this.getById(catelogId);
        //找到这个实体的父id
        if (byId.getParentCid() != 0) {//不等于0就代表有父分类
            //就继续查
            findParentPath(byId.getParentCid(), paths);//查到一个放一个
        }
        return paths;
    }

    /**
     * 级联更新所有数据
     *
     * @param category
     */
//    @CacheEvict(value = "category" ,key = "'selectLevel1'")//失效模式，更新了数据库就删除缓存，到下次请求selectLevel1方法又设置好了
    @Caching(evict =
            {@CacheEvict(value = "category" ,key = "'selectLevel1'"),
             @CacheEvict(value = "category" ,key = "'selectLevel2'")})//多操作模式
    @CacheEvict(value = "catefory",allEntries = true)//删除这个区的所有缓存
//    @CachePut//双写模式，这里没返回值不适用
    @Transactional//开启事务
    @Override
    public void updateCascade(CategoryEntity category) {
        //先进行常规的更新自己
        //updateById和update方法的区别，前者用于有实体主id根据id查，后者没主id，只能传入特定的更新条件
        this.updateById(category);
        //再更新冗余数据
        categoryBrandRelationService.updateCategory(category.getCatId(), category.getName());
    }

    @Cacheable(value = "category",key = "#root.methodName")
    @Override
    public List<CategoryEntity> selectLevel1() {
        System.out.println("查询一级分类数据");
        return baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", 0));
    }

    @Cacheable(value = "category",key = "#root.methodName",sync = true)//简化了加入redis缓存的操作
    @Override
    public Map<String, List<Catelog2Vo>> selectLevel2() {
        System.out.println("查询了23级数据");
        return selectLevel2ForDb();
    }


    //使用redisson加入分布式锁 简化
    public Map<String, List<Catelog2Vo>> selectLevel2ForDbWithRedisson() {
        RLock lock = redisson.getLock("catalogJSON-lock");
        lock.lock();

        Map<String, List<Catelog2Vo>> dataFormDb;
        try {
            dataFormDb = selectLevel2ForDbWithRedis();
        } finally {
            lock.unlock();
        }

        return dataFormDb;

    }


    //加入分布式锁--原理
//    public Map<String, List<Catelog2Vo>> selectLevel2ForRedisLock() {
//        //加分布式锁占坑
//        String uuid = UUID.randomUUID().toString();//坑1：保证创建和删除的锁是同一个
//        Boolean lock = stringRedisTemplate.opsForValue().setIfAbsent("lock", uuid, 300, TimeUnit.SECONDS);//坑2：设置过期时间，防止业务终止造成死锁，没有执行删锁
//        if (lock) {
//            System.out.println("获取到分布式锁成功。。。");
//            //true，占锁成功,执行方法，拿到数据并返回，再释放锁给其他服务竞争
//            Map<String, List<Catelog2Vo>> dataFormDb;
//            try {
//                dataFormDb=getDataFormDb();
//            }finally {//坑3：如果业务执行时间很长，也不能一直等锁过期，最终都是要删除锁
//                //坑4：保证删的是自己的锁，因为就算uuid一样，锁过期了，进行到下一步还是会删别人的锁，所以redis+Lua脚本，保证原子性删除锁
//                String script = "if redis.call(\"get\",KEYS[1]) == ARGV[1] \n" +
//                        "then\n" +
//                        "\treturn redis.call(\"del\",KEYS[1])\n" +
//                        "else\n" +
//                        "    return 0\n" +
//                        "end;";
//                Long lock1 = stringRedisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class), Arrays.asList("lock"), uuid);
//            }
//
//            return dataFormDb;
//        } else {
//            //没有拿到锁，设置自旋，反复重试直到该服务拿到锁
//            //坑5：可以设置休眠时间，没获取到等待一会，别一直自旋
//            System.out.println("获取分布式锁失败。。");
//            try {
//                Thread.sleep(3000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            return selectLevel2ForRedisLock();
//        }
//    }

    //加入redis缓存
    private Map<String, List<Catelog2Vo>> selectLevel2ForDbWithRedis() {
        //直接从redis缓存中拿
        String catalogJSON = stringRedisTemplate.opsForValue().get("catalogJSON");
        if (StringUtils.isEmpty(catalogJSON)) {
            //缓存中没有就从数据库拿
            Map<String, List<Catelog2Vo>> stringListMap1 = selectLevel2ForDb();
            //拿完放到redis中,value需要string字符串，统一把数据变为json字符串，因为json跨语言跨平台
            String s = JSON.toJSONString(stringListMap1);
            stringRedisTemplate.opsForValue().set("catalogJSON", s);

            //返回需要到数据
            return stringListMap1;
        }
        //继续第一行，因为redis有数据，是个json字符串，所以要转为我们需要的对象
        Map<String, List<Catelog2Vo>> stringListMap = JSON.parseObject(catalogJSON, new TypeReference<Map<String, List<Catelog2Vo>>>() {});

        return stringListMap;
    }

    //查询数据库二三级分类封装---业务代码
    public Map<String, List<Catelog2Vo>> selectLevel2ForDb() {

        //在缓存中查询
//        Map<String, List<Catelog2Vo>> level2 = (Map<String, List<Catelog2Vo>>) cache.get("level2");
//        if (cache.get("level2") == null){//缓存中没有。就查询一次数据库

//        cache.put("level2",parent_cid);//更新到缓存中

//        }
//        return level2;


        //1.方法优化，把嵌套查询，变成一次查询
        List<CategoryEntity> entities = baseMapper.selectList(null);


        //1、查出所有1级分类
        List<CategoryEntity> levels1 = getParent_cid(entities, 0l);
        //2、封装数据
        Map<String, List<Catelog2Vo>> parent_cid = levels1.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            //1.查询所有2级分类
            List<CategoryEntity> levels2 = getParent_cid(entities, v.getCatId());
            //2.设置Vo
            //    private String catalog1Id;
            //    private List<Object> catalog3List;
            //    private String id;
            //    private String name;
            List<Catelog2Vo> catelog2Vos = null;
            if (levels2 != null) {
                catelog2Vos = levels2.stream().map(l2 -> {
                    Catelog2Vo catelog2Vo = new Catelog2Vo(v.getCatId().toString(), null, l2.getCatId().toString(), l2.getName());

                    //1、找当前2级分类的3级分类数组
                    List<CategoryEntity> levels3 = getParent_cid(entities, l2.getCatId());
                    //2、设置三级分类Vo
                    //        private String catalog2Id;
                    //        private String id;
                    //        private String name;
                    List<Catelog2Vo.Catelog3Vo> catelog3Vos = null;
                    if (levels3 != null) {
                        catelog3Vos = levels3.stream().map(l3 -> {
                            Catelog2Vo.Catelog3Vo catelog3Vo = new Catelog2Vo.Catelog3Vo(l2.getCatId().toString(), l3.getCatId().toString(), l3.getName());
                            return catelog3Vo;
                        }).collect(Collectors.toList());

                        catelog2Vo.setCatalog3List(catelog3Vos);
                    }


                    return catelog2Vo;
                }).collect(Collectors.toList());
            }


            return catelog2Vos;
        }));

        return parent_cid;


    }

    private List<CategoryEntity> getParent_cid(List<CategoryEntity> entities, Long parent_cid) {
        List<CategoryEntity> collect = entities.stream().filter(item -> item.getParentCid() == parent_cid).collect(Collectors.toList());
//        return baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", v.getCatId()));
        return collect;
    }


}