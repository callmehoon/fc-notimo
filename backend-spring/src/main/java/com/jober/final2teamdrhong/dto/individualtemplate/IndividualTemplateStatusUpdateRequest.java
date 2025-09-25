package com.jober.final2teamdrhong.dto.individualtemplate;

import com.jober.final2teamdrhong.entity.IndividualTemplate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IndividualTemplateStatusUpdateRequest {
    private IndividualTemplate.Status status;
}
