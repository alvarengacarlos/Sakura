package com.alvarengacarlos.sakura.imageanalyzer;

import java.math.BigDecimal;
import java.util.List;

import lombok.ToString;

public class AnalysisResponse {
    public Boolean isTaxReceipt;
    public List<Item> items;

    @ToString
    public static class Item {
        public String description;
        public Double quantity;
        public String measureUnit;
        public BigDecimal price;
        public String paymentMethod;
        public String where;
        public String when;
    }
}
