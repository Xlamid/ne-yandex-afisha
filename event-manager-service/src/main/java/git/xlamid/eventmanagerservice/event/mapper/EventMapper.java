package git.xlamid.eventmanagerservice.event.mapper;

import git.xlamid.eventmanagerservice.event.dto.CreateEventDto;
import git.xlamid.eventmanagerservice.event.dto.GetEventDto;
import git.xlamid.eventmanagerservice.event.dto.UpdateEventDto;
import git.xlamid.eventmanagerservice.event.entity.EventEntity;
import org.mapstruct.*;

import static org.mapstruct.NullValuePropertyMappingStrategy.IGNORE;

@Mapper(componentModel = "spring")
public interface EventMapper {

    EventEntity dtoToEntity(CreateEventDto dto);

    @Mapping(source = "location.id", target = "locationId")
    @Mapping(source = "user.id", target = "ownerId")
    GetEventDto entityToGetDto(EventEntity save);

    @BeanMapping(nullValuePropertyMappingStrategy = IGNORE)
    void updateEntityByDto(@MappingTarget EventEntity entity,
                           UpdateEventDto dto);
}