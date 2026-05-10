package com.project.lumix.repository;

import com.project.lumix.dto.request.CreateMomoRequest;
import com.project.lumix.dto.request.QueryMomoRequest;
import com.project.lumix.dto.response.CreateMomoResponse;
import com.project.lumix.dto.response.QueryMomoResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "momo", url = "${momo.end-point}")
public interface MomoApi {
    @PostMapping("/create")
    CreateMomoResponse createMomoQr(@RequestBody CreateMomoRequest request);

    @PostMapping("/query")
    QueryMomoResponse queryPayment(@RequestBody QueryMomoRequest request);
}