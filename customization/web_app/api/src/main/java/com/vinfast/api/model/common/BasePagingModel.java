package com.vinfast.api.model.common;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

@Getter
@Setter
public class BasePagingModel<T> extends BaseModel {
    public int currentPage = 0;
    public int itemsPerPage = 0;
    public long totalItems = 0;
    public ArrayList<T> dataList = new ArrayList<>();

    private int offset = 0;
    private String sortColumn = "";
    private String sortColumnDir = "";

    public boolean isDesc() {
        return (sortColumnDir.compareToIgnoreCase("DESC") == 0);
    }
    public int getOffset() {
        if(itemsPerPage == 0)
            itemsPerPage = 15;
        return (this.currentPage == 1 ? 0 : ((this.itemsPerPage * this.currentPage) - this.itemsPerPage));
    }
}
