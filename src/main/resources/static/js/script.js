document.addEventListener('DOMContentLoaded', () => {
    const generateBtn = document.getElementById('generateAI');
    const postForm = document.getElementById('postForm');
    const captionField = document.getElementById('caption');
    const attachmentsInput = document.getElementById('attachments');
    const spinnerContainer = document.getElementById('loading-spinner');
    const uploadImageBase64Input = document.getElementById('uploadImageBase64');
    const isAIInput = document.getElementById('isAI');
    const divider = document.getElementsByClassName("divider")[0]
    const attachmentsDiv = document.getElementById("attachment-input")

    const aiImageEndpoint = 'http://localhost:8080/api/posts/generateImage';
    const submitImageEndpoint = 'http://localhost:8080/api/posts/create';

    // Handle file input changes for toggling AI generation button
    attachmentsInput.addEventListener('change', () => {
        const files = attachmentsInput.files;
        if (files && files.length > 0) {
            // User selected a file, disable AI generation and set isAI to false
            generateBtn.disabled = true;
            generateBtn.style.display = 'none';
            divider.style.display = 'none';
            removeImagePreview();
            isAIInput.value = "false";

            // Convert the selected file to base64
            const file = files[0];
            const reader = new FileReader();
            reader.onload = (e) => {
                const dataUrl = e.target.result; // e.g., "data:image/png;base64,..."
                const base64String = dataUrl.split(',')[1]; // Extract base64 part

                // Set the hidden input to this base64 string
                uploadImageBase64Input.value = base64String;

                // Remove the file from the input to prevent sending binary data
                attachmentsInput.value = "";

                // Show the image preview
                attachImageToForm(dataUrl);
            };
            reader.readAsDataURL(file);
        } else {
            // No file selected, enable AI generation and clear isAI flag
            generateBtn.disabled = false;
            generateBtn.style.display = 'inline-block';
            divider.style.display = 'flex';
            uploadImageBase64Input.value = "";
            isAIInput.value = "false";
            removeImagePreview();
        }
    });

    // Intercept the form submission
    postForm.addEventListener('submit', async (event) => {
        event.preventDefault(); // Prevent default form submit

        showSpinner();
        try {
            const formData = new FormData(postForm);

            const response = await fetch(submitImageEndpoint, {
                method: 'POST',
                body: formData
            });

            hideSpinner();

            if (!response.ok) {
                // Handle error codes from backend
                if (response.status === 400) {
                    alert("Bad Request: Please check your input and try again.");
                } else if (response.status === 403) {
                    alert("Your image is not considered peaceful. Please try another image.");
                } else if (response.status === 500) {
                    alert("A server error occurred. Please try again later.");
                } else {
                    const errorText = await response.text();
                    alert(`Error: ${response.status} - ${errorText}`);
                }
                return;
            }

            // If successful, process the response
            const savedPost = await response.json();
            alert("Post created successfully!");
            postForm.reset();
            removeImagePreview();
            uploadImageBase64Input.value = "";
            isAIInput.value = "false";
            generateBtn.disabled = false;

        } catch (error) {
            hideSpinner();
            console.error(error);
            alert("Failed to submit the post. Check console for details.");
        }
    });

    // Handle AI image generation
    generateBtn.addEventListener('click', async () => {
        const prompt = captionField.value.trim();
        if (!prompt) {
            alert("Please enter a caption before generating an AI image.");
            return;
        }

        // Ensure no files are uploaded and set isAI to true
        attachmentsInput.value = "";
        uploadImageBase64Input.value = "";
        isAIInput.value = "true";
        attachmentsDiv.style.display = 'none';
        divider.style.display = 'none';
        generateBtn.disabled = true;

        showSpinner();
        try {
            const url = new URL(aiImageEndpoint);
            url.searchParams.append('prompt', prompt);

            const response = await fetch(url.toString(), {
                method: 'GET'
            });

            if (!response.ok) {
                const errorText = await response.text();
                throw new Error(`Error: ${response.status} - ${errorText}`);
            }

            const data = await response.json();
            const imageUrl = data.imageUrl; // Base64 string returned by backend

            if (imageUrl) {
                // Create a data URL from the base64 string
                const dataUrl = "data:image/png;base64," + imageUrl;
                uploadImageBase64Input.value = imageUrl; // Set the base64 string
                attachImageToForm(dataUrl);
            } else {
                alert("No image returned from the AI service.");
                isAIInput.value = "false";
            }
        } catch (error) {
            console.error(error);
            alert("Failed to generate image. Check console for details.");
            isAIInput.value = "false";
            generateBtn.disabled = false;
            attachmentsDiv.style.display = 'block';
            divider.style.display = 'flex';
        } finally {
            hideSpinner();
        }
    });

    // Function to attach image preview to the form
    function attachImageToForm(dataUrl) {
        let preview = document.getElementById('imagePreview');
        if (!preview) {
            preview = document.createElement('div');
            preview.id = 'imagePreview';
            preview.style.textAlign = 'center';
            preview.style.marginBottom = '15px';
            postForm.insertBefore(preview, postForm.querySelector('button[type="submit"]'));
        }

        preview.innerHTML = `<img src="${dataUrl}" alt="Image Preview" 
            style="max-width:100%; border:1px solid #ddd; border-radius:5px; margin-top:10px;" />`;
    }

    // Function to remove image preview from the form
    function removeImagePreview() {
        const preview = document.getElementById('imagePreview');
        if (preview) {
            preview.remove();
        }
    }

    // Functions to show and hide the loading spinner
    function showSpinner() {
        spinnerContainer.style.display = 'flex';
    }

    function hideSpinner() {
        spinnerContainer.style.display = 'none';
    }
});
