package com.andmeanalyys.peace_project.controllers;

import com.andmeanalyys.peace_project.dto.PostDTO;
import com.andmeanalyys.peace_project.records.ImageResponse;
import com.andmeanalyys.peace_project.records.Post;
import com.andmeanalyys.peace_project.services.PostService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping(value = "/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping("/create")
    public ResponseEntity<PostDTO> createPost(
            @RequestParam("caption") String caption,
            @RequestParam("isAI") String isAI,
            @RequestParam("uploadImageBase64") String uploadImageBase64
    ) {
        try {
            PostDTO savedPost = postService.handlePostCreation(caption, isAI, uploadImageBase64);
            return ResponseEntity.ok(savedPost);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (SecurityException e) {
            // SecurityException thrown if image not peaceful
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping(value = "/generateImage", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ImageResponse> getPosts(@RequestParam(required = false) String prompt) throws IOException {
        System.out.println(prompt);
        String base64String = postService.generateImage(prompt);
        ImageResponse response = new ImageResponse(base64String);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<PostDTO>> getAllPosts() {
        List<PostDTO> allPosts = postService.getAllPosts();
        System.out.println("All posts: " + allPosts.size());
        System.out.println(allPosts.getFirst().toString());
        return ResponseEntity.ok(allPosts);
    }

    @PostMapping("/{id}/like")
    public ResponseEntity<Post> likePost(@PathVariable Long id) {
        try {
            Post updatedPost = postService.incrementLikes(id);
            return ResponseEntity.ok(updatedPost);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
}