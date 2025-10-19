package org.example.hotel;

import java.io.IOException;
import org.apache.http.HttpHost;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.example.hotel.constants.HotelIndexConstants;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest
class HotelIndexTest {

    private RestHighLevelClient client;

    /**
     * 创建索引库
     *
     * @throws IOException
     */
    @Test
    public void testCreateIndex() throws IOException {
        // 1.准备Request： PUT /hotel
        CreateIndexRequest request = new CreateIndexRequest("hotel");
        // 2.准备请求参数，MAPPING_TEMPLATE是静态常量字符串，内容是创建索引库的DSL语句
        request.source(HotelIndexConstants.MAPPING_TEMPLATE, XContentType.JSON);
        // 3.发送请求：indices()返回的是IndicesClient，而IndicesClient提供了所有索引库操作方法
        CreateIndexResponse createIndexResponse = client.indices().create(request, RequestOptions.DEFAULT);
        assert createIndexResponse != null;
        System.out.println(createIndexResponse);
    }

    /**
     * 判断索引库是否存在
     *
     * @throws IOException
     */
    @Test
    public void testExistsIndex() throws IOException {
        // 1.准备Request
        GetIndexRequest request = new GetIndexRequest("hotel");
        // 3.发送请求
        boolean isExists = client.indices().exists(request, RequestOptions.DEFAULT);

        System.out.println(isExists ? "存在" : "不存在");
    }

    /**
     * 删除索引库
     *
     * @throws IOException
     */
    @Test
    public void testDeleteIndex() throws IOException {
        // 1.准备Request
        DeleteIndexRequest request = new DeleteIndexRequest("hotel");
        // 3.发送请求
        client.indices().delete(request, RequestOptions.DEFAULT);
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
