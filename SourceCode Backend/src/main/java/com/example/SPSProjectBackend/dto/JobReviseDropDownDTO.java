package com.example.SPSProjectBackend.dto;

import java.util.List;

public class JobReviseDropDownDTO {
    private List<String> projectNos;

    public void setProjectNos(List<String> projectNos) {
        this.projectNos = projectNos;
    }

    public List<String> getProjectNos() {
        return projectNos;
    }
}
