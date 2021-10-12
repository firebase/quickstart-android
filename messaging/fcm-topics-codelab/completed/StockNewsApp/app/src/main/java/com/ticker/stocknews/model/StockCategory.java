package com.ticker.stocknews.model;

/**
 * Data structure representing Stock Category
 */
public class StockCategory {

  private final String categoryName;
  private final String topicName;
  private boolean isSubscribed;

  public StockCategory(String categoryName, String topicName, boolean isSubscribed) {
    this.categoryName = categoryName;
    this.topicName = topicName;
    this.isSubscribed = isSubscribed;
  }

  public String getCategoryName() {
    return categoryName;
  }

  public String getTopicName() {
    return topicName;
  }

  public boolean isSubscribed() {
    return isSubscribed;
  }

  public void setSubscribed(boolean subscribed) {
    isSubscribed = subscribed;
  }
}
