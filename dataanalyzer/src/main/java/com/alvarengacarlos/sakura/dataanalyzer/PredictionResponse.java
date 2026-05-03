package com.alvarengacarlos.sakura.dataanalyzer;

import java.util.List;

public class PredictionResponse {

    public List<PredictionItem> predictions;

    public static class PredictionItem {
        public String item;
        public Integer quantity;
        public String measureUnit;
        public String nextPurchaseDate;
    }
}
