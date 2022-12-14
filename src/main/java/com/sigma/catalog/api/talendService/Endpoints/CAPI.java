package com.sigma.catalog.api.talendService.Endpoints;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sigma.catalog.api.restservices.CatalogRowService;

@RestController
@RequestMapping("/capi/Entity")
public class CAPI {

    @PostMapping("/stage")
    public ResponseEntity<Object> stage(@RequestBody Map<String, Object> request) {
        CatalogRowService.refreshToken();
        String response = CatalogRowService.StageEntity((String) request.get("guid"));
        return new ResponseEntity<Object>(response, HttpStatus.OK);

    }

    @PostMapping("/live")
    public ResponseEntity<Object> live(@RequestBody Map<String, Object> request) {
        CatalogRowService.refreshToken();
        String response = CatalogRowService.LaunchEntity((String) request.get("guid"));
        return new ResponseEntity<Object>(response, HttpStatus.OK);

    }

    @PostMapping("/aprove")
    public ResponseEntity<Object> aprove(@RequestBody Map<String, Object> request) {
        CatalogRowService.refreshToken();
        String response = CatalogRowService.approveEntity((String) request.get("guid"));
        return new ResponseEntity<Object>(response, HttpStatus.OK);

    }

}
