package org.example.hotel.web;

import javax.annotation.Resource;
import org.example.hotel.pojo.PageResult;
import org.example.hotel.pojo.RequestParams;
import org.example.hotel.service.IHotelService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("hotel")
public class HotelController {

    @Resource
    private IHotelService hotelService;

    @PostMapping("/list")
    public PageResult search(@RequestBody RequestParams params) {
        return hotelService.search(params);
    }
}
