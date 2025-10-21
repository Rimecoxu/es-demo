package org.example.hotel.pojo;

import java.io.Serializable;
import lombok.Data;

@Data
public class RequestParams implements Serializable {
    private static final long serialVersionUID = -7037007657990364612L;
    private String key;
    private Integer page;
    private Integer size;
    private String sortBy;
    private String brand;
    private String city;
    private String starName;
    private Integer minPrice;
    private Integer maxPrice;
    private String location;
}
