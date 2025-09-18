package com.microservices.user.domain.events.fileEvent.helpers;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@Schema(name = "MultiFileUploadRequest")
public class MultiFileUploadRequest {

  @ArraySchema(schema = @Schema(type = "string", format = "binary"))
  @Schema(description = "Multiple files to upload")
  private MultipartFile[] files;

  @Schema(type = "string", defaultValue = "documents", description = "Folder to upload to")
  private String folder = "documents";
}
