package git.xlamid.eventmanagerservice.user.mapper;

import git.xlamid.eventmanagerservice.user.dto.GetUserDto;
import git.xlamid.eventmanagerservice.user.entity.UserEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    GetUserDto entityToGetDto(UserEntity entity);
}