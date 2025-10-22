package org.example.hotel;

import java.io.IOException;
import javax.annotation.Resource;
import org.apache.http.HttpHost;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.elasticsearch.search.suggest.completion.CompletionSuggestion;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @Author: Rimecoxu@gmail.com
 * @CreateTime: 2025-10-21 23:32
 * @Description: 自动补全测试
 */
@SpringBootTest
public class HotelDocumentSuggestTest {

    @Resource
    private RestHighLevelClient client;

    /**
     * 测试自动补全
     *
     * @throws IOException
     */
    @Test
    public void testSuggest() throws IOException {
        SearchRequest request = new SearchRequest("hotel");
        request.source().suggest(new SuggestBuilder().addSuggestion(
                "mySuggest",
                SuggestBuilders.
                        completionSuggestion("suggestion")
                        .prefix("h")
                        .skipDuplicates(true)
                        .size(10)
        ));
        SearchResponse searchResponse = client.search(request, RequestOptions.DEFAULT);
        Suggest suggest = searchResponse.getSuggest();
        CompletionSuggestion mySuggest = suggest.getSuggestion("mySuggest");
        mySuggest.getOptions().forEach(option -> System.out.println(option.getText().string()));
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
