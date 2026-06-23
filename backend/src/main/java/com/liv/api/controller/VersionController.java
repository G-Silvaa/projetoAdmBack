package com.liv.api.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.liv.api.dto.ResponseInfoDetail;

@RestController
@RequestMapping
public class VersionController {
	
	@Autowired
	private BuildProperties buildProperties;
	
	@GetMapping(produces = "application/json")
	public ResponseEntity<ResponseInfoDetail> get() {
		ResponseInfoDetail infoDetail = new ResponseInfoDetail()
                .addDetails("Versão", buildProperties.getVersion())
                .addDetails("Date Formato", new Date())
                .addDetails("LocalDate Formato", LocalDate.now())
                .addDetails("LocalDateTime Formato", LocalDateTime.now())
                .addDetails("LocalTime Formato", LocalTime.now());
        return ResponseEntity.ok(infoDetail);
	}

}
