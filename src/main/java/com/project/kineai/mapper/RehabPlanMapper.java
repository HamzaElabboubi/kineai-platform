package com.project.kineai.mapper;

import com.project.kineai.dto.response.RehabPlanResponse;
import com.project.kineai.model.entity.RehabPlan;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface RehabPlanMapper {

    @Mapping(source = "patient.id", target = "patientId")
    RehabPlanResponse toResponse(RehabPlan plan);
}