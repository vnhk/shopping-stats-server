package com.bervan.shstat.dtomappers;

import com.bervan.shstat.DataHolder;

public interface DTOMapper<T, DTO> {
    void map(DataHolder<T> t, DataHolder<DTO> dto);

    static String getOfferUrl(String shop, String offerUrl) {
        return switch (shop) {
            case "Media Expert" -> "https://mediaexpert.pl" + offerUrl;
            case "Morele" -> "https://morele.net" + offerUrl;
            case "RTV Euro AGD" -> "https://www.euro.com.pl" + offerUrl;
            default -> offerUrl;
        };
    }
}
