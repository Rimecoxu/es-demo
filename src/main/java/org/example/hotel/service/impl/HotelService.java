package org.example.hotel.service.impl;


import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.MultiBucketsAggregation;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.elasticsearch.search.suggest.completion.CompletionSuggestion;
import org.example.hotel.mapper.HotelMapper;
import org.example.hotel.pojo.Hotel;
import org.example.hotel.pojo.HotelDoc;
import org.example.hotel.pojo.PageResult;
import org.example.hotel.pojo.RequestParams;
import org.example.hotel.service.IHotelService;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class HotelService extends ServiceImpl<HotelMapper, Hotel> implements IHotelService {

    @Resource
    private RestHighLevelClient restHighLevelClient;

    @Override
    public PageResult search(RequestParams params) {
        log.warn("查询参数：{}", params);
        try {
            // 1.准备Request
            SearchRequest request = new SearchRequest("hotel");
            // 2.准备请求参数
            buildBasicQuery(params, request);
            // 3.设置分页
            int page = params.getPage();
            int size = params.getSize();
            // ES 最大查询数量限制
            if ((page - 1) * size > 10000) {
                return new PageResult(0L, null);
            }
            request.source().from((page - 1) * size).size(size);
            // 4.设置地理排序
            String location = params.getLocation();
            if (StringUtils.isNotBlank(location)) {
                request.source().sort(SortBuilders
                        .geoDistanceSort("location", new GeoPoint(location))
                        .order(SortOrder.ASC)
                        .unit(DistanceUnit.KILOMETERS)
                );
            }
            // 4.发送请求
            SearchResponse response = restHighLevelClient.search(request, RequestOptions.DEFAULT);
            // 5.解析响应
            return handleResponse(response);
        } catch (IOException e) {
            throw new RuntimeException("搜索数据失败", e);
        }
    }

    private void buildBasicQuery(RequestParams params, SearchRequest request) {
        // 1.准备Boolean查询
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

        // 1.1.关键字搜索，match查询，放到must中
        String key = params.getKey();
        if (StringUtils.isNotBlank(key)) {
            // 不为空，根据关键字查询
            boolQuery.must(QueryBuilders.matchQuery("all", key));
        } else {
            // 为空，查询所有
            boolQuery.must(QueryBuilders.matchAllQuery());
        }

        // 1.2.品牌
        String brand = params.getBrand();
        if (StringUtils.isNotBlank(brand)) {
            boolQuery.filter(QueryBuilders.termQuery("brand", brand));
        }
        // 1.3.城市
        String city = params.getCity();
        if (StringUtils.isNotBlank(city)) {
            boolQuery.filter(QueryBuilders.termQuery("city", city));
        }
        // 1.4.星级
        String starName = params.getStarName();
        if (StringUtils.isNotBlank(starName)) {
            boolQuery.filter(QueryBuilders.termQuery("starName", starName));
        }
        // 1.5.价格范围
        Integer minPrice = params.getMinPrice();
        Integer maxPrice = params.getMaxPrice();
        if (minPrice != null && maxPrice != null) {
            maxPrice = maxPrice == 0 ? Integer.MAX_VALUE : maxPrice;
            boolQuery.filter(QueryBuilders.rangeQuery("price").gte(minPrice).lte(maxPrice));
        }

        // 2.算分函数查询
        FunctionScoreQueryBuilder functionScoreQuery = QueryBuilders.functionScoreQuery(
                boolQuery, // 原始查询，boolQuery
                new FunctionScoreQueryBuilder.FilterFunctionBuilder[]{ // function数组
                        new FunctionScoreQueryBuilder.FilterFunctionBuilder(
                                QueryBuilders.termQuery("isAD", true), // 过滤条件
                                ScoreFunctionBuilders.weightFactorFunction(10) // 算分函数
                        )
                }
        );

        // 3.设置查询条件
        request.source().query(functionScoreQuery);
    }


    private PageResult handleResponse(SearchResponse response) {
        SearchHits searchHits = response.getHits();
        // 4.1.总条数
        long total = searchHits.getTotalHits().value;
        // 4.2.获取文档数组
        SearchHit[] hits = searchHits.getHits();
        // 4.3.遍历
        List<HotelDoc> hotels = Arrays.stream(hits).map(hit -> {
            // 4.4.获取source
            String json = hit.getSourceAsString();
            HotelDoc hotelDoc = JSON.parseObject(json, HotelDoc.class);
            Object[] sortValues = hit.getSortValues();
            if (sortValues.length > 0) {
                // 获取距离
                hotelDoc.setDistance(sortValues[0]);
            }
            return hotelDoc;
        }).collect(Collectors.toList());
        return new PageResult(total, hotels);
    }

    @Override
    public Map<String, List<String>> filters(RequestParams params) throws IOException {
        Map<String, List<String>> map = new HashMap<>();

        SearchRequest request = new SearchRequest("hotel");

        // 基于查询结果进行聚合
        buildBasicQuery(params, request);

        buildAggregations(request);

        SearchResponse searchResponse = restHighLevelClient.search(request, RequestOptions.DEFAULT);
        Aggregations aggregations = searchResponse.getAggregations();

        Terms terms = aggregations.get("brand_agg");
        List<? extends Terms.Bucket> buckets = terms.getBuckets();
        List<String> brandList = buckets.stream().map(MultiBucketsAggregation.Bucket::getKeyAsString).collect(Collectors.toList());
        map.put("brand", brandList);

        terms = aggregations.get("starName_agg");
        buckets = terms.getBuckets();
        List<String> starNameList = buckets.stream().map(MultiBucketsAggregation.Bucket::getKeyAsString).collect(Collectors.toList());
        map.put("starName", starNameList);

        terms = aggregations.get("city_agg");
        buckets = terms.getBuckets();
        List<String> cityList = buckets.stream().map(MultiBucketsAggregation.Bucket::getKeyAsString).collect(Collectors.toList());
        map.put("city", cityList);

        return map;
    }

    @Override
    public List<String> suggestion(String prefix) throws IOException {
        if (StringUtils.isBlank(prefix)) {
            return Collections.emptyList();
        }
        SearchRequest request = new SearchRequest("hotel");
        request.source().suggest(new SuggestBuilder().addSuggestion(
                "mySuggest",
                SuggestBuilders.
                        completionSuggestion("suggestion")
                        .prefix(prefix)
                        .skipDuplicates(true)
                        .size(10)
        ));
        SearchResponse searchResponse = restHighLevelClient.search(request, RequestOptions.DEFAULT);
        Suggest suggest = searchResponse.getSuggest();
        CompletionSuggestion mySuggest = suggest.getSuggestion("mySuggest");
        return mySuggest.getOptions().stream().map(option -> option.getText().toString()).collect(Collectors.toList());
    }


    public void buildAggregations(SearchRequest request) {
        request.source().aggregation(AggregationBuilders.terms("brand_agg").field("brand").size(30));
        request.source().aggregation(AggregationBuilders.terms("city_agg").field("city").size(30));
        request.source().aggregation(AggregationBuilders.terms("starName_agg").field("starName").size(30));
    }
}
