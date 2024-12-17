package com.andmeanalyys.peace_project.mapper;

import com.andmeanalyys.peace_project.dto.PostDTO;
import com.andmeanalyys.peace_project.dto.UserDTO;
import com.andmeanalyys.peace_project.records.Post;
import com.andmeanalyys.peace_project.records.User;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface EntityMapper {
    EntityMapper INSTANCE = Mappers.getMapper(EntityMapper.class);

    UserDTO userToUserDTO(User user);
    PostDTO postToPostDTO(Post post);

    User userDTOToUser(UserDTO userDTO);
    Post postDTOToPost(PostDTO postDTO);
}
