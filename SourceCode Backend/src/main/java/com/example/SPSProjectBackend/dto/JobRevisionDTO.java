package com.example.SPSProjectBackend.dto;
import com.example.SPSProjectBackend.model.Pcestdmt;
import com.example.SPSProjectBackend.model.Pcesthmt;

import java.util.List;

public class JobRevisionDTO {
    private Pcesthmt header;
    private List<Pcestdmt> details;

    public Pcesthmt getHeader() {
        return header;
    }

    public void setHeader(Pcesthmt header) {
        this.header = header;
    }

    public List<Pcestdmt> getDetails() {
        return details;
    }

    public void setDetails(List<Pcestdmt> details) {
        this.details = details;
    }
}