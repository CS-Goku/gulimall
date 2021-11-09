package com.atguigu.gulimall.search;

import com.alibaba.fastjson.JSON;
import com.atguigu.gulimall.search.config.GulimallElasticSearchConfig;
import lombok.Data;
import lombok.ToString;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.AvgAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

@RunWith(SpringRunner.class)//指定Spring驱动来跑单元测试
@SpringBootTest
public class GulimallSearchApplicationTests {

    @Autowired
    private RestHighLevelClient client;

    /**
     *
     * 1.
     Creates the SearchRequest. Without arguments this runs against all indices.
     创建SearchRequest。如果没有参数，将对所有索引运行
     Most search parameters are added to the SearchSourceBuilder. It offers setters for everything that goes into the search request body.
     大多数搜索参数都添加到SearchSourceBuilder中。它为搜索请求体中的所有内容提供了setter方法。

     Add a match_all query to the SearchSourceBuilder.
     向SearchSourceBuilder添加一个match_all查询

     Add the SearchSourceBuilder to the SearchRequest.
     将SearchSourceBuilder添加到SearchRequest。
     */
    @Test
    public void find() throws IOException {

        //1.创建检索请求
        SearchRequest searchRequest = new SearchRequest("bank");
        //2.创建检索源建筑者
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //3.构造检索条件
//        sourceBuilder.query();
//        sourceBuilder.from();
//        sourceBuilder.size();
//        sourceBuilder.aggregation();
        //3.1
        searchSourceBuilder.query(QueryBuilders.matchQuery("address","mill"));

        //3.2
        //AggregationBuilders工具类构建AggregationBuilder
        // 构建第一个聚合条件:按照年龄的值分布
        TermsAggregationBuilder agg1 = AggregationBuilders.terms("agg1").field("age").size(10);// 聚合名称
// 参数为AggregationBuilder
        searchSourceBuilder.aggregation(agg1);
        // 构建第二个聚合条件:平均薪资
        AvgAggregationBuilder agg2 = AggregationBuilders.avg("agg2").field("balance");
        searchSourceBuilder.aggregation(agg2);


        //4.将检索源放入检索请求
        searchRequest.source(searchSourceBuilder);

        //5.执行检索
        SearchResponse response = client.search(searchRequest, GulimallElasticSearchConfig.COMMON_OPTIONS);
        //6.结果分析
        SearchHits hits = response.getHits();
        SearchHit[] hits1 = hits.getHits();
        for (SearchHit hit : hits1) {
            hit.getId();
            hit.getIndex();
            String sourceAsString = hit.getSourceAsString();
            Account account = JSON.parseObject(sourceAsString, Account.class);
            System.out.println(account);
        }

    }

    @Test
    public void indexData() throws IOException {

        IndexRequest indexRequest = new IndexRequest("users");
        indexRequest.id("1");

        User user = new User();
        user.setUserName("小王");
        user.setGender("男");
        user.setAge(18);

        String jsonString = JSON.toJSONString(user);
        indexRequest.source(jsonString, XContentType.JSON);//要保存的内容

        //执行操作
        IndexResponse index = client.index(indexRequest, GulimallElasticSearchConfig.COMMON_OPTIONS);
        System.out.println(index);
    }

    @Test
    public void contextLoads() {
        System.out.println(client);
    }

    @Data
    @ToString
    class Account{

    }
    @Data
    class User{
        private String userName;
        private String gender;
        private Integer age;

    }

}
