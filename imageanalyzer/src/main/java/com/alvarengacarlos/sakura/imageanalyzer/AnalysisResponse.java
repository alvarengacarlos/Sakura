package com.alvarengacarlos.sakura.imageanalyzer;

import java.math.BigDecimal;

public class AnalysisResponse {
    public Boolean isTaxReceipt;
    public String description;
    public Integer quantity;
    public BigDecimal price;
    public String paymentMethod;
    public String where;
    public String when;
}
