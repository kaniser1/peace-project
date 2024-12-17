document.addEventListener('DOMContentLoaded', () => {
    const postsContainer = document.getElementById('posts-container');

    // Fetch posts from the backend
    async function fetchPosts() {
        try {
            const response = await fetch('https://peace-project.fly.dev/api/posts', {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json'
                },
                credentials: 'include'
            });

            if (response.ok) {
                const posts = await response.json();
                renderPosts(posts);
            } else {
                console.error('Failed to fetch posts.');
                postsContainer.innerHTML = '<p>Error loading posts.</p>';
            }
        } catch (error) {
            console.error('Error fetching posts:', error);
            postsContainer.innerHTML = '<p>Error loading posts.</p>';
        }
    }

    function renderPosts(posts) {
        postsContainer.innerHTML = ''; // Clear existing posts
        posts.forEach(post => {
            const postElement = document.createElement('div');
            postElement.classList.add('post');

            // Content Section
            const postContent = document.createElement('div');
            postContent.classList.add('post-content');

            // Header
            const postHeader = document.createElement('div');
            postHeader.classList.add('post-header');

            const userName = document.createElement('span');
            userName.classList.add('post-user');
            userName.textContent = post.user.username;

            const userHandle = document.createElement('span');
            userHandle.classList.add('post-handle');
            userHandle.textContent = `@${post.user.username} · ${formatTimestamp(post.createdAt)}`;

            postHeader.appendChild(userName);
            postHeader.appendChild(userHandle);

            // Text Content
            const postText = document.createElement('p');
            postText.classList.add('post-text');
            postText.textContent = post.caption;

            // Attachment Image
            if (post.attachmentUrl) {
                const attachmentImg = document.createElement('img');
                attachmentImg.src = post.attachmentUrl;
                attachmentImg.alt = 'Attachment';
                attachmentImg.classList.add('attachment-image');
                postContent.appendChild(attachmentImg);
            }

            // Likes Section
            const likesSection = document.createElement('div');
            likesSection.classList.add('likes-section');

            // Like Button with Icon
            const likeIconContainer = document.createElement('div');
            likeIconContainer.classList.add('like-icon');
            likeIconContainer.id = `like-container-${post.id}`;
            likeIconContainer.dataset.postId = post.id;

// SVG Like Icon
            const likeIcon = document.createElementNS('http://www.w3.org/2000/svg', 'svg');
            likeIcon.setAttribute('viewBox', '0 0 24 24');
            likeIcon.innerHTML = `
    <path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"/>
`;

            const likesCount = document.createElement('span');
            likesCount.textContent = post.likes;
            likesCount.id = `likes-count-${post.id}`;

            likeIconContainer.appendChild(likeIcon);
            likeIconContainer.appendChild(likesCount);

            // Add event listener to like icon
            likeIconContainer.addEventListener('click', handleLike);

            likesSection.appendChild(likeIconContainer);
            likesSection.appendChild(likesCount);

            // Assemble Content
            postContent.appendChild(postHeader);
            postContent.appendChild(postText);
            postContent.appendChild(likesSection);

            postElement.appendChild(postContent);

            // Append Post to Container
            postsContainer.appendChild(postElement);
        });
    }

    async function handleLike(event) {
        // Ensure we target the container with the postId
        const postId = Number(event.currentTarget.dataset.postId);
        const likeButton = document.getElementById(`like-container-${postId}`)
        likeButton.style.pointerEvents = "none";

        if (!postId) {
            console.error('Post ID is undefined.');
            return;
        }

        console.log(`https://peace-project.fly.dev/api/posts/${postId}/like`);
        try {
            const response = await fetch(`https://peace-project.fly.dev/api/posts/${postId}/like`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                credentials: 'include'
            });

            if (response.ok) {
                const updatedPost = await response.json();
                const likesCount = document.getElementById(`likes-count-${postId}`);
                likesCount.textContent = updatedPost.likes;

            } else {
                console.error('Failed to like the post.');
                alert('Failed to like the post. Please try again.');
            }
        } catch (error) {
            console.error('Error liking the post:', error);
            alert('An error occurred. Please try again.');
        }
    }

    // Format timestamps
    function formatTimestamp(timestamp) {
        const date = new Date(timestamp);
        const hours = date.getHours();
        const minutes = date.getMinutes();
        const ampm = hours >= 12 ? 'PM' : 'AM';
        const formattedHours = hours % 12 || 12;
        const formattedMinutes = minutes < 10 ? `0${minutes}` : minutes;
        return `${formattedHours}:${formattedMinutes} ${ampm}`;
    }

    // Fetch posts on load
    fetchPosts();
});
