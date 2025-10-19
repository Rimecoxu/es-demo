package org.example.hotel.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.hotel.mapper.HotelMapper;
import org.example.hotel.pojo.Hotel;
import org.example.hotel.service.IHotelService;
import org.springframework.stereotype.Service;

@Service
public class HotelService extends ServiceImpl<HotelMapper, Hotel> implements IHotelService {
}
