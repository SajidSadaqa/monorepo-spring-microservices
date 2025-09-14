package com.microservices.admin.application.dto.response;

import java.util.List;

public record PageResDto<T>(List<T> content, int page, int size, long totalElements, int totalPages) {}
