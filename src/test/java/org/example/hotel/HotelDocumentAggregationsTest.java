package org.example.hotel;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.http.HttpHost;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.MultiBucketsAggregation;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @Author: Rimecoxu@gmail.com
 * @CreateTime: 2025-10-21 23:32
 * @Description: 聚合测试
 */
@SpringBootTest
public class HotelDocumentAggregationsTest {

    private RestHighLevelClient client;


    /**
     * 测试聚合查询
     *
     * @throws IOException
     */
    @Test
    public void testAggregation() throws IOException {
        SearchRequest request = new SearchRequest("hotel");
        request.source()
                .size(0)
                .aggregation(AggregationBuilders.terms("brand_agg").field("brand").size(10));
        SearchResponse searchResponse = client.search(request, RequestOptions.DEFAULT);
        assert searchResponse != null;
        Aggregations aggregations = searchResponse.getAggregations();
        // IDEA返回的是Aggregations
        Terms terms = aggregations.get("brand_agg");
        List<? extends Terms.Bucket> buckets = terms.getBuckets();
        for (Terms.Bucket bucket : buckets) {
            System.out.println(bucket.getKeyAsString() + ":" + bucket.getDocCount());
        }
    }

    /**
     * 测试多重聚合查询
     *
     * @throws IOException
     */
    @Test
    public void testMultiAggregation() throws IOException {
        Map<String, List<String>> map = new HashMap<>();

        SearchRequest request = new SearchRequest("hotel");
        request.source()
                .size(0)
                .aggregation(AggregationBuilders.terms("brand_agg").field("brand").size(30));

        request.source().clearRescorers()
                .size(0)
                .aggregation(AggregationBuilders.terms("city_agg").field("city").size(30));

        request.source().clearRescorers()
                .size(0)
                .aggregation(AggregationBuilders.terms("starName_agg").field("starName").size(30));

        SearchResponse searchResponse = client.search(request, RequestOptions.DEFAULT);
        Aggregations aggregations = searchResponse.getAggregations();

        Terms terms = aggregations.get("brand_agg");
        List<? extends Terms.Bucket> buckets = terms.getBuckets();
        List<String> brandList = buckets.stream().map(MultiBucketsAggregation.Bucket::getKeyAsString).collect(Collectors.toList());
        map.put("品牌", brandList);

        terms = aggregations.get("city_agg");
        buckets = terms.getBuckets();
        List<String> cityList = buckets.stream().map(MultiBucketsAggregation.Bucket::getKeyAsString).collect(Collectors.toList());
        map.put("城市", cityList);

        terms = aggregations.get("starName_agg");
        buckets = terms.getBuckets();
        List<String> starNameList = buckets.stream().map(MultiBucketsAggregation.Bucket::getKeyAsString).collect(Collectors.toList());
        map.put("星级", starNameList);

        System.out.println(map);
    }


    @BeforeEach
    public void setUp() {
        client = new RestHighLevelClient(RestClient.builder(
                HttpHost.create("http://192.168.99.99:9200")
        ));
    }

    @AfterEach
    public void tearDown() throws IOException {
        client.close();
    }
}
