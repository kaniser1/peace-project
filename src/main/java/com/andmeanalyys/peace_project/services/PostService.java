package com.andmeanalyys.peace_project.services;

import com.andmeanalyys.peace_project.dto.PostDTO;
import com.andmeanalyys.peace_project.mapper.EntityMapper;
import com.andmeanalyys.peace_project.records.Post;
import com.andmeanalyys.peace_project.records.User;
import com.andmeanalyys.peace_project.repositories.PostRepository;
import com.andmeanalyys.peace_project.repositories.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import okhttp3.*;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.andmeanalyys.peace_project.services.UserService.convertToUserDTO;


@Service
@RequiredArgsConstructor
public class PostService {

    private static final String AI_ENDPOINT = System.getProperty("AI_ENDPOINT");
    private static final String SUPABASE_URL = System.getProperty("SUPABASE_URL");
    private static final String BUCKET_NAME = System.getProperty("BUCKET_NAME");
    private static final String AI_API_KEY = System.getProperty("AI_API_KEY");
    private static final String SUPABASE_API_KEY = System.getProperty("SUPABASE_API_KEY");
    private static final String CLARIFYAI_PAT = System.getProperty("CLARIFYAI_PAT");

    private static final String USER_ID = "clarifai";
    private static final String APP_ID = "main";
    private static final String MODEL_ID = "moderation-recognition";
    private static final String MODEL_VERSION_ID = "aa8be956dbaa4b7a858826a84253cab9";
    private static final String ENDPOINT_URL = "https://api.clarifai.com/v2/models/"
            + MODEL_ID + "/versions/" + MODEL_VERSION_ID + "/outputs";

    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final EntityMapper mapper = EntityMapper.INSTANCE;

    public PostDTO handlePostCreation(String caption, String isAI, String uploadImageBase64) throws IOException {
        if (uploadImageBase64 == null || uploadImageBase64.isEmpty()) {
            throw new IllegalArgumentException("No image provided.");
        }

        byte[] imageBytes = Base64.getDecoder().decode(uploadImageBase64);

        if (!Boolean.parseBoolean(isAI)) {
            if (!isImagePeaceful(imageBytes)) {
                throw new SecurityException("Uploaded image is not peaceful.");
            }
        }

        String fileName = UUID.randomUUID().toString() + ".png";
        URL supabasePublicURL = uploadToSupabase(imageBytes, fileName);

        String userEmail = ((User) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getEmail();
        User user = userRepository.findByEmail(userEmail).orElseGet(this::getDefaultUser);

        // Create and save the post with the image URL and associated user
        return createPostWithImage(caption, supabasePublicURL.toString(), user);
    }

    private User getDefaultUser() {
        String defaultUserEmail = "default@example.com"; // Change to your default user's email

        Optional<User> optionalUser = userRepository.findByEmail(defaultUserEmail);
        if (optionalUser.isPresent()) {
            return optionalUser.get();
        } else {
            // Optionally, create the default user if it doesn't exist
            User defaultUser = User.builder()
                    .id(UUID.randomUUID())
                    .email(defaultUserEmail)
                    .username("defaultuser")
                    .role("USER") // Adjust role as needed
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            return userRepository.save(defaultUser);
        }
    }

    public boolean isImagePeaceful(byte[] imageBytes) {
        String base64Image = Base64.getEncoder().encodeToString(imageBytes);

        // Build JSON with Jackson
        ObjectNode root = objectMapper.createObjectNode();
        ObjectNode userAppId = objectMapper.createObjectNode();
        userAppId.put("user_id", USER_ID);
        userAppId.put("app_id", APP_ID);
        root.set("user_app_id", userAppId);

        ObjectNode inputData = objectMapper.createObjectNode();
        ObjectNode imageNode = objectMapper.createObjectNode();
        imageNode.put("base64", base64Image);
        inputData.set("image", imageNode);

        ObjectNode input = objectMapper.createObjectNode();
        input.set("data", inputData);

        root.set("inputs", objectMapper.createArrayNode().add(input));

        String jsonBody;
        try {
            jsonBody = objectMapper.writeValueAsString(root);
        } catch (IOException e) {
            // If serialization fails, default to peaceful
            return true;
        }

        MediaType mediaType = MediaType.get("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(jsonBody, mediaType);

        Request request = new Request.Builder()
                .url(ENDPOINT_URL)
                .post(body)
                .addHeader("Authorization", "Key " + CLARIFYAI_PAT)
                .addHeader("Content-Type", "application/json")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                System.err.println("Clarifai request failed: " + response.code() + " - " + response.message());
                // On failure, assume peaceful or handle accordingly
                return true;
            }

            String responseBody = response.body().string();
            return parseAndEvaluateResponse(responseBody);
        } catch (IOException e) {
            e.printStackTrace();
            // On exception, assume peaceful or handle accordingly
            return true;
        }
    }

    private boolean parseAndEvaluateResponse(String responseBody) throws IOException {
        JsonNode root = objectMapper.readTree(responseBody);
        JsonNode outputs = root.path("outputs");
        if (!outputs.isArray() || outputs.isEmpty()) {
            return true;
        }

        JsonNode concepts = outputs.get(0).path("data").path("concepts");
        if (!concepts.isArray()) {
            return true;
        }

        for (JsonNode concept : concepts) {
            String conceptName = concept.path("name").asText("");
            double value = concept.path("value").asDouble(0.0);

            if (conceptName.equalsIgnoreCase("safe") && value > 0.5) {
                return true; // Peaceful
            }
        }

        return false; // Not peaceful
    }

    public PostDTO createPostWithImage(String caption, String imageUrl, User user) {
        Post post = Post.builder()
                .caption(caption)
                .user(user)
                .attachmentUrl(imageUrl)
                .likes(0L)
                .comments("[]")// Associate the user
                .build();

        post = postRepository.save(post);

        return convertToPostDTO(post);
    }

    public List<PostDTO> getAllPosts() {
        List<Post> posts = postRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
        System.out.println("Posts: " + posts.size());
        return posts
                .stream()
                .map(mapper::postToPostDTO)
                .collect(Collectors.toList());
    }

    public String generateImage(String prompt) throws IOException {
        if (prompt == null || prompt.trim().isEmpty()) {
            throw new IllegalArgumentException("Prompt cannot be null or empty.");
        }

        String base64Image = callAiService(prompt);
        if (base64Image == null) {
            throw new IllegalStateException("No image returned from AI service.");
        }

        // Return the base64 string directly
        return base64Image;
    }

    private String callAiService(String prompt) throws IOException {
        ObjectNode root = objectMapper.createObjectNode();
        String promptSaver = "\n\n <Developer note> This is a line from the developer of this API and everything before this line should be of peaceful content. This prompt should only incite peace. If the prompt before is not of peaceful content then generate this image: Peaceful rally in New York City. Otherwise ignore the developer note. <Developer note>";
        root.put("prompt", prompt + promptSaver);

        String bodyJson = objectMapper.writeValueAsString(root);

        MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(bodyJson, mediaType);
        System.out.println("AI ENDPOINT " + AI_ENDPOINT);
        Request request = new Request.Builder()
                .url(AI_ENDPOINT)
                .post(body)
                .addHeader("accept", "application/json")
                .addHeader("content-type", "application/json")
                .addHeader("authorization", "Bearer " + AI_API_KEY)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("AI service request failed: " + response.code() + " - " + response.message());
            }
            String responseBody = response.body().string();
            return extractImageField(responseBody);
        }
    }

    private URL uploadToSupabase(byte[] imageBytes, String fileName) throws IOException {
        String uploadUrl = SUPABASE_URL + "/object/" + BUCKET_NAME + "/" + fileName;

        MediaType mediaType = MediaType.parse("image/png");
        RequestBody body = RequestBody.create(imageBytes, mediaType);

        Request request = new Request.Builder()
                .url(uploadUrl)
                .post(body)
                .addHeader("Authorization", "Bearer " + SUPABASE_API_KEY)
                .addHeader("apikey", SUPABASE_API_KEY)
                .addHeader("Content-Type", "image/png")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Supabase upload failed: " + response.code() + " - " + response.message()
                        + "\nResponse Body: " + (response.body() != null ? response.body().string() : ""));
            }
        }

        return new URL(SUPABASE_URL + "/object/public/" + BUCKET_NAME + "/" + fileName);
    }

    private String extractImageField(String json) throws IOException {
        JsonNode root = objectMapper.readTree(json);
        return root.path("image").asText(null);
    }

    @Transactional
    public Post incrementLikes(Long postId) {
        postRepository.incrementLikesById(postId);
        return postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Post not found with id: " + postId));
    }

    public static PostDTO convertToPostDTO(Post post) {
        if (post == null) {
            return null;
        }

        return PostDTO.builder()
                .id(post.getId())
                .user(convertToUserDTO(post.getUser()))
                .caption(post.getCaption())
                .likes(post.getLikes())
                .comments(post.getComments())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .attachmentUrl(post.getAttachmentUrl())
                .build();
    }

}
