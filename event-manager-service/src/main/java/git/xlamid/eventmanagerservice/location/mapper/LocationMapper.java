package git.xlamid.eventmanagerservice.location.mapper;

import git.xlamid.eventmanagerservice.location.dto.CreateLocationDto;
import git.xlamid.eventmanagerservice.location.dto.GetLocationDto;
import git.xlamid.eventmanagerservice.location.dto.UpdateLocationDto;
import git.xlamid.eventmanagerservice.location.entity.LocationEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface LocationMapper {

    LocationEntity dtoToEntity(CreateLocationDto dto);

    GetLocationDto entityToGetDto(LocationEntity save);

    void updateEntityByDto(@MappingTarget LocationEntity locationEntity,
                           UpdateLocationDto locationDto);
}