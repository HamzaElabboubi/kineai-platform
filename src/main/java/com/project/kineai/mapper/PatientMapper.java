package com.project.kineai.mapper;

import com.project.kineai.dto.response.PatientResponse;
import com.project.kineai.model.entity.Patient;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PatientMapper {
    @Mapping(source = "kine.id",       target = "kineId")
    @Mapping(source = "kine.fullName", target = "kineName")
    PatientResponse toResponse(Patient patient);
}
