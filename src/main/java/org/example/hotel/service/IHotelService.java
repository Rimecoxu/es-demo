package org.example.hotel.service;


import com.baomidou.mybatisplus.extension.service.IService;
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

}
