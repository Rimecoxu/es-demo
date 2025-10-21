package org.example.hotel.pojo;

import java.io.Serializable;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult implements Serializable {
    private static final long serialVersionUID = -109120515192611362L;
    private Long total;
    private List<HotelDoc> hotels;
}
