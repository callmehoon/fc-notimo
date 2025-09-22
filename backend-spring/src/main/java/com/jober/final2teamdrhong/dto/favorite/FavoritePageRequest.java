package com.jober.final2teamdrhong.dto.favorite;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FavoritePageRequest {

    private int page = 0;
    private int size = 10;
}