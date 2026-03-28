package Models;

import java.time.LocalDateTime;
import java.util.UUID;

public class BlogComment {

    private UUID commentId;
    private UUID blogId;
    private UUID userId;
    private UUID parentCommentId;
    private String commenterName;
    private String replyToName;
    private String content;
    private String status;
    private int depth;
    private boolean ownedByCurrentUser;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public UUID getCommentId() {
        return commentId;
    }

    public void setCommentId(UUID commentId) {
        this.commentId = commentId;
    }

    public UUID getBlogId() {
        return blogId;
    }

    public void setBlogId(UUID blogId) {
        this.blogId = blogId;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public UUID getParentCommentId() {
        return parentCommentId;
    }

    public void setParentCommentId(UUID parentCommentId) {
        this.parentCommentId = parentCommentId;
    }

    public String getCommenterName() {
        return commenterName;
    }

    public void setCommenterName(String commenterName) {
        this.commenterName = commenterName;
    }

    public String getReplyToName() {
        return replyToName;
    }

    public void setReplyToName(String replyToName) {
        this.replyToName = replyToName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public String getIndentClass() {
        if (depth <= 0) {
            return "ml-0";
        }
        if (depth == 1) {
            return "ml-6";
        }
        if (depth == 2) {
            return "ml-12";
        }
        if (depth == 3) {
            return "ml-16";
        }
        return "ml-20";
    }

    public String getCardClass() {
        String baseClass = "border border-slate-100 rounded-2xl p-4 space-y-3";
        return depth > 0 ? baseClass + " bg-slate-50" : baseClass;
    }

    public boolean isOwnedByCurrentUser() {
        return ownedByCurrentUser;
    }

    public void setOwnedByCurrentUser(boolean ownedByCurrentUser) {
        this.ownedByCurrentUser = ownedByCurrentUser;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
