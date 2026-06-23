package com.liv.api.dto;

import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseInfoDetail {
	
    private Integer status = 200;
    private String title = "Sucesso";
    private String detail;
    private Map<String, Object> details;

    public ResponseInfoDetail() {
    }

    public ResponseInfoDetail(String detail) {
        this.detail = detail;
    }

    public Integer getStatus() {
        return status;
    }

    public ResponseInfoDetail setStatus(Integer status) {
        this.status = status;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public ResponseInfoDetail setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getDetail() {
        return detail;
    }

    public ResponseInfoDetail setDetail(String detail) {
        this.detail = detail;
        return this;
    }

    public Map<String, Object> getDetails() {
        return details;
    }

    public ResponseInfoDetail addDetails(String key, Object value) {
        if (details == null) {
            details = new LinkedHashMap<>();
        }
        details.put(key, value);
        return this;
    }

}
