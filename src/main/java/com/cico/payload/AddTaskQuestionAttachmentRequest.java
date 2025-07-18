package com.cico.payload;

import javax.validation.constraints.NotNull;

import org.springframework.web.multipart.MultipartFile;

import com.cico.model.AttachmentStatus;
import com.cico.util.AppConstants;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddTaskQuestionAttachmentRequest {

    @NotNull(message = AppConstants.TASK_ID_REQUIRED)
    private Long taskId;

    @NotNull(message = AppConstants.QUESTION_ID_REQUIRED)
    private Long questionId;

    private MultipartFile attachment;

    @NotNull(message = AppConstants.ATTACHMENT_STATUS_REQUIRED)
    private AttachmentStatus status;
}

