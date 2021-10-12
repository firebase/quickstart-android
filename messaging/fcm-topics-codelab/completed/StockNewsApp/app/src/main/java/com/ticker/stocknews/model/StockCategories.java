package com.ticker.stocknews.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds the static list of Stock Categories.
 */
public class StockCategories {

  public static final List<StockCategory> STOCK_CATEGORIES = new ArrayList<StockCategory>() {{
    add(new StockCategory("Technology", "Technology", false));
    add(new StockCategory("Automotive", "Automotive", false));
    add(new StockCategory("Financial", "Financial", false));
    add(new StockCategory("Utilities", "Utilities", false));
    add(new StockCategory("Energy", "Energy", false));
    add(new StockCategory("Health Care", "HealthCare", false));
    add(new StockCategory("Telecom", "Telecom", false));
    add(new StockCategory("Materials", "Materials", false));
    add(new StockCategory("Industrials", "Industrials", false));
    add(new StockCategory("Real Estate", "RealEstate", false));
    add(new StockCategory("Consumer Staples", "ConsumerStaples", false));
  }};

}
