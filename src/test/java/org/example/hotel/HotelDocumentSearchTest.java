package org.example.hotel;


import com.alibaba.fastjson.JSON;
import java.io.IOException;
import java.util.Map;
import javax.annotation.Resource;
import org.apache.http.HttpHost;
import org.apache.lucene.search.TotalHits;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.example.hotel.pojo.HotelDoc;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.CollectionUtils;

/**
 * 测试文档条件搜索
 */
@SpringBootTest
class HotelDocumentSearchTest {

    @Resource
    private RestHighLevelClient client;


    /**
     * 测试查询所有
     *
     * @throws IOException
     */
    @Test
    public void testMatchAll() throws IOException {
        // 1、创建Request
        SearchRequest request = new SearchRequest("hotel");

        // 2、创建DSL
        request.source().query(QueryBuilders.matchAllQuery());

        // 3、执行
        SearchResponse searchResponse = client.search(request, RequestOptions.DEFAULT);

        // 4、处理响应
        handleResponse(searchResponse);
    }

    /**
     * 测试条件分词查询
     *
     * @throws IOException
     */
    @Test
    public void testMatch() throws IOException {
        SearchRequest request = new SearchRequest("hotel");
        request.source().query(QueryBuilders.matchQuery("all", "如家"));
        SearchResponse searchResponse = client.search(request, RequestOptions.DEFAULT);
        handleResponse(searchResponse);
    }

    /**
     * 测试多条件分词查询
     *
     * @throws IOException
     */
    @Test
    public void testMultiMatch() throws IOException {
        SearchRequest request = new SearchRequest("hotel");
        request.source().query(QueryBuilders.multiMatchQuery("如家", "name", "brand"));
        SearchResponse searchResponse = client.search(request, RequestOptions.DEFAULT);
        handleResponse(searchResponse);
    }

    /**
     * 测试范围查询
     *
     * @throws IOException
     */
    @Test
    public void testRange() throws IOException {
        SearchRequest request = new SearchRequest("hotel");
        request.source().query(QueryBuilders.rangeQuery("price").lte(150));
        SearchResponse searchResponse = client.search(request, RequestOptions.DEFAULT);
        handleResponse(searchResponse);
    }


    /**
     * 测试精确查询
     *
     * @throws IOException
     */
    @Test
    public void testTerm() throws IOException {
        SearchRequest request = new SearchRequest("hotel");
        request.source().query(QueryBuilders.termQuery("city", "上海"));
        SearchResponse searchResponse = client.search(request, RequestOptions.DEFAULT);
        handleResponse(searchResponse);
    }

    /**
     * 测试布尔查询
     *
     * @throws IOException
     */
    @Test
    public void testBool() throws IOException {
        SearchRequest request = new SearchRequest("hotel");
        request.source().query(QueryBuilders.boolQuery()
                .must(QueryBuilders.termQuery("brand", "如家"))
                .filter(QueryBuilders.rangeQuery("price").lte(150)));
        SearchResponse searchResponse = client.search(request, RequestOptions.DEFAULT);
        handleResponse(searchResponse);
    }

    /**
     * 测试排序
     *
     * @throws IOException
     */
    @Test
    public void testSort() throws IOException {
        SearchRequest request = new SearchRequest("hotel");
        request.source().query(QueryBuilders.rangeQuery("price").lte(150));
        request.source().sort("price", SortOrder.DESC);
        request.source().sort("score", SortOrder.ASC);
        SearchResponse searchResponse = client.search(request, RequestOptions.DEFAULT);
        handleResponse(searchResponse);
    }

    /**
     * 测试分页查询
     *
     * @throws IOException
     */
    @Test
    public void testPage() throws IOException {
        SearchRequest request = new SearchRequest("hotel");
        request.source().query(QueryBuilders.termQuery("city", "上海"));
        request.source().from(0);
        request.source().size(2);
        SearchResponse searchResponse = client.search(request, RequestOptions.DEFAULT);
        handleResponse(searchResponse);
    }

    /**
     * 测试高亮查询
     *
     * @throws IOException
     */
    @Test
    public void testHighlight() throws IOException {
        SearchRequest request = new SearchRequest("hotel");
        request.source().query(QueryBuilders.matchQuery("all", "如家"));
        request.source().highlighter(new HighlightBuilder().field("name").requireFieldMatch(false));
        SearchResponse searchResponse = client.search(request, RequestOptions.DEFAULT);
        handleResponse(searchResponse);
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


    private void handleResponse(SearchResponse searchResponse) {
        assert searchResponse != null;
        SearchHits responseHits = searchResponse.getHits();
        TotalHits totalHits = responseHits.getTotalHits();
        SearchHit[] hits = responseHits.getHits();
        System.out.println(totalHits.value);
        System.out.println(totalHits.relation.name());
        for (SearchHit hit : hits) {
            // 获取文档数据
            HotelDoc hotelDoc = JSON.parseObject(hit.getSourceAsString(), HotelDoc.class);
            // 获取高亮数据
            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
            if (!CollectionUtils.isEmpty(highlightFields)) {
                HighlightField highlightField = highlightFields.get("name");
                if (highlightField == null) {
                    // value不为null
                    continue;
                }
                String keyName = highlightField.fragments()[0].toString();
                hotelDoc.setName(keyName);
            }
            System.out.println(hotelDoc);
        }
    }


}
