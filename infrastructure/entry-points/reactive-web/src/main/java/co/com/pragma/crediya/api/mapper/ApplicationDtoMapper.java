package co.com.pragma.crediya.api.mapper;

import co.com.pragma.crediya.api.dto.CreateApplicationDTO;
import co.com.pragma.crediya.api.dto.ResponseApplicationDTO;
import co.com.pragma.crediya.model.solicitud.Solicitud;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ApplicationDtoMapper {

    default Solicitud toModel(CreateApplicationDTO dto) {
        if (dto == null) return null;
        return new Solicitud(
                null,
                dto.monto(),
                dto.documentoIdentidad(),
                dto.email(),
                dto.plazo(),
                dto.idEstado(),
                dto.idPrestamo()
        );
    }

    ResponseApplicationDTO toResponse(Solicitud solicitud);

}
