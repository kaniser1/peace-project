package com.andmeanalyys.peace_project.mapper;

import com.andmeanalyys.peace_project.dto.PostDTO;
import com.andmeanalyys.peace_project.dto.UserDTO;
import com.andmeanalyys.peace_project.records.Post;
import com.andmeanalyys.peace_project.records.User;
import javax.annotation.processing.Generated;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2024-12-17T23:59:58+0200",
    comments = "version: 1.5.5.Final, compiler: IncrementalProcessingEnvironment from gradle-language-java-8.11.1.jar, environment: Java 21.0.3 (Oracle Corporation)"
)
public class EntityMapperImpl implements EntityMapper {

    @Override
    public UserDTO userToUserDTO(User user) {
        if ( user == null ) {
            return null;
        }

        UserDTO.UserDTOBuilder userDTO = UserDTO.builder();

        userDTO.id( user.getId() );
        userDTO.username( user.getUsername() );
        userDTO.email( user.getEmail() );

        return userDTO.build();
    }

    @Override
    public PostDTO postToPostDTO(Post post) {
        if ( post == null ) {
            return null;
        }

        PostDTO.PostDTOBuilder postDTO = PostDTO.builder();

        postDTO.id( post.getId() );
        postDTO.user( userToUserDTO( post.getUser() ) );
        postDTO.caption( post.getCaption() );
        postDTO.likes( post.getLikes() );
        postDTO.comments( post.getComments() );
        postDTO.createdAt( post.getCreatedAt() );
        postDTO.updatedAt( post.getUpdatedAt() );
        postDTO.attachmentUrl( post.getAttachmentUrl() );

        return postDTO.build();
    }

    @Override
    public User userDTOToUser(UserDTO userDTO) {
        if ( userDTO == null ) {
            return null;
        }

        User.UserBuilder user = User.builder();

        user.id( userDTO.getId() );
        user.email( userDTO.getEmail() );
        user.username( userDTO.getUsername() );

        return user.build();
    }

    @Override
    public Post postDTOToPost(PostDTO postDTO) {
        if ( postDTO == null ) {
            return null;
        }

        Post.PostBuilder post = Post.builder();

        post.id( postDTO.getId() );
        post.user( userDTOToUser( postDTO.getUser() ) );
        post.caption( postDTO.getCaption() );
        post.likes( postDTO.getLikes() );
        post.comments( postDTO.getComments() );
        post.attachmentUrl( postDTO.getAttachmentUrl() );
        post.createdAt( postDTO.getCreatedAt() );
        post.updatedAt( postDTO.getUpdatedAt() );

        return post.build();
    }
}
