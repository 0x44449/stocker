package com.hanzi.stocker.api.admin;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/news-mappings")
public class AdminNewsMappingController {

    private final AdminNewsMappingService service;

    public AdminNewsMappingController(AdminNewsMappingService service) {
        this.service = service;
    }

    @GetMapping
    public AdminNewsMappingService.NewsMappingListResponse list(
            @RequestParam(defaultValue = "all") String filter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "") String search
    ) {
        return service.getList(filter, page, size, search);
    }
}
