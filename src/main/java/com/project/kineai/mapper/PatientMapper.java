package com.project.kineai.mapper;

<<<<<<< HEAD
import com.project.kineai.dto.request.UpdatePatientRequest;
=======
>>>>>>> 2b65152 (Create dto And mapper)
import com.project.kineai.dto.response.PatientResponse;
import com.project.kineai.model.entity.Patient;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
<<<<<<< HEAD
import org.mapstruct.MappingTarget;
=======

>>>>>>> 2b65152 (Create dto And mapper)
import java.util.List;

@Mapper(componentModel = "spring")
public interface PatientMapper {
<<<<<<< HEAD

    // ✅ toResponse — source = Patient, target = PatientResponse
    // Ignorer les champs de Patient qui n'existent pas dans PatientResponse
=======
>>>>>>> 2b65152 (Create dto And mapper)
    @Mapping(source = "kine.id",       target = "kineId")
    @Mapping(source = "kine.fullName", target = "kineName")
    PatientResponse toResponse(Patient patient);

<<<<<<< HEAD
    // ✅ toResponseList — MapStruct génère automatiquement
    List<PatientResponse> toResponseList(List<Patient> patients);

    // ✅ updateEntity — source = UpdatePatientRequest, target = Patient
    // Ignorer les champs de Patient non présents dans UpdatePatientRequest
    @Mapping(target = "id",          ignore = true)
    @Mapping(target = "user",        ignore = true)
    @Mapping(target = "kine",        ignore = true)
    @Mapping(target = "pathology",   ignore = true)
    @Mapping(target = "level",       ignore = true)
    @Mapping(target = "streakCount", ignore = true)
    @Mapping(target = "totalXp",     ignore = true)
    void updateEntity(UpdatePatientRequest request,
                      @MappingTarget Patient patient);
}
=======
    List<PatientResponse> toResponseList(List<Patient> patients);
}
>>>>>>> 2b65152 (Create dto And mapper)
