package com.example.recycling_app.service;

import com.example.recycling_app.domain.Comment;
import com.example.recycling_app.domain.ContentBlock;
import com.example.recycling_app.domain.Post;
import com.example.recycling_app.exception.NotFoundException;
import com.example.recycling_app.exception.UnauthorizedException;
import com.example.recycling_app.repository.CommentRepository;
import com.example.recycling_app.repository.LikeRepository;
import com.example.recycling_app.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class CommunityService {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private CommentRepository commentRepository;

    // 미디어 URL 유효성 검증
    private void validateMediaContents(List<ContentBlock> contents) {
        if (contents == null) return;

        contents.forEach(contentBlock -> {
            if ("image".equals(contentBlock.getType()) || "video".equals(contentBlock.getType())) {
                if (contentBlock.getMediaUrl() == null || contentBlock.getMediaUrl().isEmpty()) {
                    throw new IllegalArgumentException("미디어 URL이 올바르지 않습니다.");
                }
            }
        });
    }

    // 게시글 작성
    public void writePost(Post post) throws Exception {
        post.setCreatedAt(new Date());
        post.setUpdatedAt(new Date());
        postRepository.save(post);
    }

    // 게시글 수정
    public void updatePost(String postId, Post updatedPost, String verifiedUid) throws Exception {
        Post existingPost = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("게시글을 찾을 수 없습니다."));

        // 권한 검증: 게시글 작성자와 요청한 사용자의 UID가 일치하는지 확인
        if (!existingPost.getUid().equals(verifiedUid)) {
            throw new UnauthorizedException("게시글을 수정할 권한이 없습니다.");
        }

        updatedPost.setPostId(existingPost.getPostId());
        updatedPost.setUpdatedAt(new Date());
        postRepository.save(updatedPost);
    }

    // 게시글 삭제 (논리적 삭제)
    public void deletePost(String postId, String verifiedUid) throws Exception {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("게시글을 찾을 수 없습니다."));

        // 권한 검증: 게시글 작성자와 요청한 사용자의 UID가 일치하는지 확인
        if (!post.getUid().equals(verifiedUid)) {
            throw new UnauthorizedException("게시글을 삭제할 권한이 없습니다.");
        }

        post.setDeleted(true);
        post.setDeletedAt(new Date());
        postRepository.save(post);
    }

    // 전체 게시글 조회
    public List<Post> getAllPosts(String uid) throws Exception {
        List<Post> posts = postRepository.findAll();
        // 각 게시글에 대해 현재 사용자의 좋아요 여부 설정
        setLikedStatusForPosts(posts, uid);
        return posts;
    }

    // 카테고리별 게시글 조회
    public List<Post> getPosts(String category, String uid) throws Exception {
        List<Post> posts = postRepository.findByCategory(category);
        setLikedStatusForPosts(posts, uid);
        return posts;
    }

    // 특정 게시글 상세 조회
    public Post getPost(String postId, String uid) throws Exception {
        Optional<Post> postOptional = postRepository.findById(postId);
        Post post = postOptional.orElseThrow(() -> new NotFoundException("게시글을 찾을 수 없습니다."));


        // 현재 사용자의 좋아요 여부 설정
        if (uid != null) {
            boolean isLiked = likeRepository.findById(postId, uid).isPresent();
            post.setLikedByCurrentUser(isLiked);
        }
        return post;
    }

    // 게시글 좋아요/취소 토글
    public int toggleLikes(String postId, String verifiedUid) throws Exception {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("게시글을 찾을 수 없습니다."));

        Optional<com.example.recycling_app.domain.Like> existingLike = likeRepository.findById(postId, verifiedUid);

        if (existingLike.isPresent()) {
            likeRepository.deleteById(postId, verifiedUid);
            post.setLikesCount(post.getLikesCount() - 1);
        } else {
            likeRepository.save(postId, verifiedUid);
            post.setLikesCount(post.getLikesCount() + 1);
        }

        postRepository.save(post);
        return post.getLikesCount();
    }

    // 댓글 작성
    public void writeComment(Comment comment) throws Exception {
        comment.setCreatedAt(new Date());
        comment.setUpdatedAt(new Date());
        commentRepository.save(comment);
/*
        // 게시글의 댓글 수 업데이트
        Post post = postRepository.findById(comment.getPostId()).orElse(null);
        if (post != null) {
            post.setCommentsCount(post.getCommentsCount() + 1);
            postRepository.save(post);
        }
        */
        postRepository.incrementCommentCount(comment.getPostId());

    }

    // 댓글 수정
    public void updateComment(String commentId, String content, String verifiedUid) throws Exception {
        Comment existingComment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("댓글을 찾을 수 없습니다."));

        // 권한 검증: 댓글 작성자와 요청한 사용자의 UID가 일치하는지 확인
        if (!existingComment.getUid().equals(verifiedUid)) {
            throw new UnauthorizedException("댓글을 수정할 권한이 없습니다.");
        }

        existingComment.setContent(content);
        existingComment.setUpdatedAt(new Date());
        commentRepository.save(existingComment);
    }

    // 댓글 삭제 (논리적 삭제)
    public void deleteComment(String commentId, String verifiedUid) throws Exception {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("댓글을 찾을 수 없습니다."));

        // 권한 검증: 댓글 작성자와 요청한 사용자의 UID가 일치하는지 확인
        if (!comment.getUid().equals(verifiedUid)) {
            throw new UnauthorizedException("댓글을 삭제할 권한이 없습니다.");
        }

        comment.setDeleted(true);
        comment.setDeletedAt(new Date());
        commentRepository.save(comment);

        // 게시글의 댓글 수 업데이트
        Post post = postRepository.findById(comment.getPostId()).orElse(null);
        if (post != null) {
            post.setCommentsCount(post.getCommentsCount() - 1);
            postRepository.save(post);
        }
    }

    // 특정 게시글의 모든 댓글 조회 (대댓글 포함)
    public List<Comment> getComments(String postId) throws Exception {
        return commentRepository.findByUidAndDeletedFalse(postId);
    }

    // 내가 작성한 게시글 조회
    public List<Post> getMyPosts(String uid) throws Exception {
        List<Post> posts = postRepository.findByUidAndDeletedFalse(uid);
        setLikedStatusForPosts(posts, uid);
        return posts;
    }

    // 내가 댓글 단 게시글 조회
    public List<Post> getPostsCommentedByUser(String uid) throws Exception {
        List<String> postIds = commentRepository.findDistinctPostIdsByUidAndDeletedFalse(uid);
        List<Post> posts = postRepository.findAllByIds(postIds);
        setLikedStatusForPosts(posts, uid);
        return posts;
    }

    // 내가 좋아요한 게시글 조회
    public List<Post> getLikedPostsByUser(String uid) throws Exception {
        List<Post> posts = likeRepository.findPostsLikedByUser(uid);
        setLikedStatusForPosts(posts, uid);
        return posts;
    }

    // 상대방 게시글 조회
    public List<Post> getUserPosts(String uid) throws Exception {
        return postRepository.findByUidAndDeletedFalse(uid);
    }

    // 상대방 댓글 조회
    public List<Comment> getUserComments(String uid) throws Exception {
        return commentRepository.findByUidAndDeletedFalse(uid);
    }

    // 헬퍼 메서드: 게시글 목록에 좋아요 상태 설정
    private void setLikedStatusForPosts(List<Post> posts, String uid) throws Exception {
        if (uid == null) return;
        List<String> likedPostIds = likeRepository.findLikedPostIdsByUid(uid);
        for (Post post : posts) {
            if (likedPostIds.contains(post.getPostId())) {
                post.setLikedByCurrentUser(true);
            }
        }
    }
}
