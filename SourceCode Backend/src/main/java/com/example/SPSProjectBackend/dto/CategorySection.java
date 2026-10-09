package com.example.SPSProjectBackend.dto;

import java.util.Date;
import java.util.List;

public class CategorySection {
    private String categoryName;
    private Date fromDate;
    private CategoryType categoryType;
    private List<Object> tariffData;

    public CategorySection() {
    }

    public CategorySection(String categoryName, Date fromDate, CategoryType categoryType, List<Object> tariffData) {
        this.categoryName = categoryName;
        this.fromDate = fromDate;
        this.categoryType = categoryType;
        this.tariffData = tariffData;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public Date getFromDate() {
        return fromDate;
    }

    public void setFromDate(Date fromDate) {
        this.fromDate = fromDate;
    }

    public CategoryType getCategoryType() {
        return categoryType;
    }

    public void setCategoryType(CategoryType categoryType) {
        this.categoryType = categoryType;
    }

    public List<Object> getTariffData() {
        return tariffData;
    }

    public void setTariffData(List<Object> tariffData) {
        this.tariffData = tariffData;
    }
}
