package org.example.hotel.service;


import com.baomidou.mybatisplus.extension.service.IService;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.example.hotel.pojo.Hotel;
import org.example.hotel.pojo.PageResult;
import org.example.hotel.pojo.RequestParams;

public interface IHotelService extends IService<Hotel> {

    /**
     * 搜索
     *
     * @param params 查询参数
     * @return 搜索结果
     */
    PageResult search(RequestParams params);

    /**
     * 聚合查询
     *
     * @param params 查询参数
     * @return 聚合结果
     */
    Map<String, List<String>> filters(RequestParams params) throws IOException;


}
