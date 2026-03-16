package DAO;

import Models.Blog;
import Models.BlogComment;
import Utils.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BlogDAO {

    public int countBlogsForRole(UUID userId, String roleName, String statusFilter, String keyword) throws SQLException {
        String normalizedRole = normalizeRole(roleName);

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(*) FROM Blog b WHERE 1 = 1 ");

        List<String> params = new ArrayList<>();

        appendVisibilityCondition(sql, params, normalizedRole, userId);

        if ("manager".equals(normalizedRole) && statusFilter != null && !statusFilter.trim().isEmpty() && !"all".equalsIgnoreCase(statusFilter.trim())) {
            sql.append(" AND LOWER(b.status) = ? ");
            params.add(statusFilter.trim().toLowerCase());
        }

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (LOWER(b.title) LIKE ? OR LOWER(ISNULL(b.summary, '')) LIKE ? OR LOWER(b.content) LIKE ?) ");
            String kw = "%" + keyword.trim().toLowerCase() + "%";
            params.add(kw);
            params.add(kw);
            params.add(kw);
        }

        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql.toString())) {
            bindStringParams(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }

        return 0;
    }

    public List<Blog> getBlogsForRole(UUID userId, String roleName, int page, int pageSize, String statusFilter, String keyword) throws SQLException {
        String normalizedRole = normalizeRole(roleName);

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT b.blog_id, b.title, b.summary, b.content, b.image_url, b.status, ");
        sql.append("b.created_by, b.approved_by, b.created_at, b.updated_at, b.published_at, ");
        sql.append("u.full_name AS created_by_name, au.full_name AS approved_by_name, ");
        sql.append("(SELECT COUNT(*) FROM Blog_Like bl WHERE bl.blog_id = b.blog_id) AS like_count, ");
        sql.append("(SELECT COUNT(*) FROM Blog_Comment bc WHERE bc.blog_id = b.blog_id AND LOWER(bc.status) = 'active') AS comment_count, ");
        sql.append("CASE WHEN EXISTS (SELECT 1 FROM Blog_Like bl2 WHERE bl2.blog_id = b.blog_id AND bl2.user_id = ?) THEN 1 ELSE 0 END AS liked_by_current_user ");
        sql.append("FROM Blog b ");
        sql.append("JOIN Users u ON b.created_by = u.user_id ");
        sql.append("LEFT JOIN Users au ON b.approved_by = au.user_id ");
        sql.append("WHERE 1 = 1 ");

        List<String> params = new ArrayList<>();
        params.add(userId != null ? userId.toString() : "");

        appendVisibilityCondition(sql, params, normalizedRole, userId);

        if ("manager".equals(normalizedRole) && statusFilter != null && !statusFilter.trim().isEmpty() && !"all".equalsIgnoreCase(statusFilter.trim())) {
            sql.append(" AND LOWER(b.status) = ? ");
            params.add(statusFilter.trim().toLowerCase());
        }

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (LOWER(b.title) LIKE ? OR LOWER(ISNULL(b.summary, '')) LIKE ? OR LOWER(b.content) LIKE ?) ");
            String kw = "%" + keyword.trim().toLowerCase() + "%";
            params.add(kw);
            params.add(kw);
            params.add(kw);
        }

        if ("manager".equals(normalizedRole)) {
            sql.append(" ORDER BY b.created_at DESC ");
        } else {
            sql.append(" ORDER BY CASE WHEN b.published_at IS NULL THEN b.created_at ELSE b.published_at END DESC ");
        }

        sql.append(" OFFSET ? ROWS FETCH NEXT ? ROWS ONLY ");

        int safePage = page <= 0 ? 1 : page;
        int safePageSize = pageSize <= 0 ? 10 : pageSize;
        int offset = (safePage - 1) * safePageSize;

        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql.toString())) {
            int idx = bindStringParams(ps, params);
            ps.setInt(idx++, offset);
            ps.setInt(idx, safePageSize);

            List<Blog> blogs = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    blogs.add(mapBlog(rs));
                }
            }
            return blogs;
        }
    }

    public Blog getBlogById(UUID blogId) throws SQLException {
        String sql = "SELECT b.blog_id, b.title, b.summary, b.content, b.image_url, b.status, "
                + "b.created_by, b.approved_by, b.created_at, b.updated_at, b.published_at, "
                + "u.full_name AS created_by_name, au.full_name AS approved_by_name, "
                + "(SELECT COUNT(*) FROM Blog_Like bl WHERE bl.blog_id = b.blog_id) AS like_count, "
                + "(SELECT COUNT(*) FROM Blog_Comment bc WHERE bc.blog_id = b.blog_id AND LOWER(bc.status) = 'active') AS comment_count, "
                + "0 AS liked_by_current_user "
                + "FROM Blog b "
                + "JOIN Users u ON b.created_by = u.user_id "
                + "LEFT JOIN Users au ON b.approved_by = au.user_id "
                + "WHERE b.blog_id = ?";

        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, blogId.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapBlog(rs);
                }
            }
        }

        return null;
    }

    public Blog getBlogDetailForRole(UUID blogId, UUID currentUserId, String roleName) throws SQLException {
        String normalizedRole = normalizeRole(roleName);

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT b.blog_id, b.title, b.summary, b.content, b.image_url, b.status, ");
        sql.append("b.created_by, b.approved_by, b.created_at, b.updated_at, b.published_at, ");
        sql.append("u.full_name AS created_by_name, au.full_name AS approved_by_name, ");
        sql.append("(SELECT COUNT(*) FROM Blog_Like bl WHERE bl.blog_id = b.blog_id) AS like_count, ");
        sql.append("(SELECT COUNT(*) FROM Blog_Comment bc WHERE bc.blog_id = b.blog_id AND LOWER(bc.status) = 'active') AS comment_count, ");
        sql.append("CASE WHEN EXISTS (SELECT 1 FROM Blog_Like bl2 WHERE bl2.blog_id = b.blog_id AND bl2.user_id = ?) THEN 1 ELSE 0 END AS liked_by_current_user ");
        sql.append("FROM Blog b ");
        sql.append("JOIN Users u ON b.created_by = u.user_id ");
        sql.append("LEFT JOIN Users au ON b.approved_by = au.user_id ");
        sql.append("WHERE b.blog_id = ? ");

        List<String> params = new ArrayList<>();
        params.add(currentUserId != null ? currentUserId.toString() : "");
        params.add(blogId.toString());

        appendVisibilityCondition(sql, params, normalizedRole, currentUserId);

        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql.toString())) {
            bindStringParams(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapBlog(rs);
                }
            }
        }

        return null;
    }

    public List<BlogComment> getCommentsByBlogId(UUID blogId, UUID currentUserId) throws SQLException {
        String sql = "SELECT bc.comment_id, bc.blog_id, bc.user_id, bc.parent_comment_id, bc.content, bc.status, bc.created_at, bc.updated_at, "
            + "u.full_name, pu.full_name AS reply_to_name "
                + "FROM Blog_Comment bc "
                + "JOIN Users u ON bc.user_id = u.user_id "
            + "LEFT JOIN Blog_Comment parent_bc ON bc.parent_comment_id = parent_bc.comment_id "
            + "LEFT JOIN Users pu ON parent_bc.user_id = pu.user_id "
                + "WHERE bc.blog_id = ? AND LOWER(bc.status) = 'active' "
                + "ORDER BY bc.created_at ASC";

        List<BlogComment> flatComments = new ArrayList<>();
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, blogId.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    BlogComment comment = new BlogComment();
                    comment.setCommentId(toUuid(rs.getString("comment_id")));
                    comment.setBlogId(toUuid(rs.getString("blog_id")));
                    comment.setUserId(toUuid(rs.getString("user_id")));
                    comment.setParentCommentId(toUuid(rs.getString("parent_comment_id")));
                    comment.setContent(rs.getString("content"));
                    comment.setStatus(rs.getString("status"));
                    comment.setCommenterName(rs.getString("full_name"));
                    comment.setReplyToName(rs.getString("reply_to_name"));
                    comment.setOwnedByCurrentUser(currentUserId != null && currentUserId.equals(comment.getUserId()));

                    Timestamp createdAt = rs.getTimestamp("created_at");
                    if (createdAt != null) {
                        comment.setCreatedAt(createdAt.toLocalDateTime());
                    }
                    Timestamp updatedAt = rs.getTimestamp("updated_at");
                    if (updatedAt != null) {
                        comment.setUpdatedAt(updatedAt.toLocalDateTime());
                    }

                    flatComments.add(comment);
                }
            }
        }

        Map<UUID, List<BlogComment>> childrenByParent = new HashMap<>();
        List<BlogComment> roots = new ArrayList<>();

        for (BlogComment c : flatComments) {
            UUID parentId = c.getParentCommentId();
            if (parentId == null) {
                roots.add(c);
            } else {
                childrenByParent.computeIfAbsent(parentId, k -> new ArrayList<>()).add(c);
            }
        }

        List<BlogComment> threaded = new ArrayList<>();
        for (BlogComment root : roots) {
            appendThread(root, childrenByParent, threaded, 0);
        }

        // Handle orphan replies if parent was deleted or filtered.
        for (BlogComment c : flatComments) {
            if (!threaded.contains(c)) {
                appendThread(c, childrenByParent, threaded, 0);
            }
        }

        return threaded;
    }

    public boolean isCommentOwner(UUID commentId, UUID userId) throws SQLException {
        String sql = "SELECT 1 FROM Blog_Comment WHERE comment_id = ? AND user_id = ? AND LOWER(status) = 'active'";
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, commentId.toString());
            ps.setString(2, userId.toString());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private void appendThread(
            BlogComment current,
            Map<UUID, List<BlogComment>> childrenByParent,
            List<BlogComment> output,
            int depth
    ) {
        current.setDepth(depth);
        output.add(current);

        List<BlogComment> children = childrenByParent.get(current.getCommentId());
        if (children == null || children.isEmpty()) {
            return;
        }

        for (BlogComment child : children) {
            appendThread(child, childrenByParent, output, depth + 1);
        }
    }

    public UUID createBlog(Blog blog) throws SQLException {
        String sql = "INSERT INTO Blog(blog_id, title, summary, content, image_url, status, created_by, approved_by, created_at, updated_at, published_at) "
                + "VALUES(?, ?, ?, ?, ?, ?, ?, ?, SYSDATETIME(), SYSDATETIME(), ?)";

        UUID blogId = blog.getBlogId() != null ? blog.getBlogId() : UUID.randomUUID();

        try (Connection con = DBConnection.getConnection()) {
            String generated = newGuid(con);
            if (blog.getBlogId() == null && generated != null) {
                blogId = UUID.fromString(generated);
            }

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, blogId.toString());
                ps.setString(2, blog.getTitle());
                ps.setString(3, blog.getSummary());
                ps.setString(4, blog.getContent());
                ps.setString(5, blog.getImageUrl());
                ps.setString(6, blog.getStatus());
                ps.setString(7, blog.getCreatedBy().toString());
                ps.setString(8, blog.getApprovedBy() == null ? null : blog.getApprovedBy().toString());
                if (blog.getPublishedAt() == null) {
                    ps.setTimestamp(9, null);
                } else {
                    ps.setTimestamp(9, Timestamp.valueOf(blog.getPublishedAt()));
                }
                ps.executeUpdate();
            }
        }

        return blogId;
    }

    public boolean updateBlog(Blog blog) throws SQLException {
        String sql = "UPDATE Blog SET title = ?, summary = ?, content = ?, image_url = ?, status = ?, approved_by = ?, "
                + "published_at = ?, updated_at = SYSDATETIME() WHERE blog_id = ?";

        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, blog.getTitle());
            ps.setString(2, blog.getSummary());
            ps.setString(3, blog.getContent());
            ps.setString(4, blog.getImageUrl());
            ps.setString(5, blog.getStatus());
            ps.setString(6, blog.getApprovedBy() == null ? null : blog.getApprovedBy().toString());
            if (blog.getPublishedAt() == null) {
                ps.setTimestamp(7, null);
            } else {
                ps.setTimestamp(7, Timestamp.valueOf(blog.getPublishedAt()));
            }
            ps.setString(8, blog.getBlogId().toString());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean approveBlog(UUID blogId, UUID managerId) throws SQLException {
        String sql = "UPDATE Blog SET status = 'approved', approved_by = ?, published_at = COALESCE(published_at, SYSDATETIME()), updated_at = SYSDATETIME() WHERE blog_id = ?";
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, managerId.toString());
            ps.setString(2, blogId.toString());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean rejectBlog(UUID blogId, UUID managerId) throws SQLException {
        String sql = "UPDATE Blog SET status = 'rejected', approved_by = ?, published_at = NULL, updated_at = SYSDATETIME() WHERE blog_id = ?";
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, managerId.toString());
            ps.setString(2, blogId.toString());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean deleteBlog(UUID blogId) throws SQLException {
        String sql = "DELETE FROM Blog WHERE blog_id = ?";
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, blogId.toString());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean isBlogOwner(UUID blogId, UUID userId) throws SQLException {
        String sql = "SELECT 1 FROM Blog WHERE blog_id = ? AND created_by = ?";
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, blogId.toString());
            ps.setString(2, userId.toString());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean addComment(UUID blogId, UUID userId, UUID parentCommentId, String content) throws SQLException {
        String sql = "INSERT INTO Blog_Comment(comment_id, blog_id, user_id, parent_comment_id, content, status, created_at, updated_at) VALUES(?, ?, ?, ?, ?, 'active', SYSDATETIME(), SYSDATETIME())";

        try (Connection con = DBConnection.getConnection()) {
            String commentId = newGuid(con);
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, commentId);
                ps.setString(2, blogId.toString());
                ps.setString(3, userId.toString());
                ps.setString(4, parentCommentId == null ? null : parentCommentId.toString());
                ps.setString(5, content);
                return ps.executeUpdate() > 0;
            }
        }
    }

    public boolean commentBelongsToBlog(UUID commentId, UUID blogId) throws SQLException {
        String sql = "SELECT 1 FROM Blog_Comment WHERE comment_id = ? AND blog_id = ? AND LOWER(status) = 'active'";
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, commentId.toString());
            ps.setString(2, blogId.toString());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean deleteComment(UUID commentId) throws SQLException {
        String sql = "WITH CommentTree AS ("
                + " SELECT comment_id FROM Blog_Comment WHERE comment_id = ? "
                + " UNION ALL "
                + " SELECT c.comment_id "
                + " FROM Blog_Comment c "
                + " JOIN CommentTree ct ON c.parent_comment_id = ct.comment_id"
                + ") "
                + "DELETE FROM Blog_Comment WHERE comment_id IN (SELECT comment_id FROM CommentTree)";
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, commentId.toString());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean toggleLike(UUID blogId, UUID userId) throws SQLException {
        String checkSql = "SELECT 1 FROM Blog_Like WHERE blog_id = ? AND user_id = ?";

        try (Connection con = DBConnection.getConnection()) {
            boolean exists;
            try (PreparedStatement ps = con.prepareStatement(checkSql)) {
                ps.setString(1, blogId.toString());
                ps.setString(2, userId.toString());
                try (ResultSet rs = ps.executeQuery()) {
                    exists = rs.next();
                }
            }

            if (exists) {
                try (PreparedStatement ps = con.prepareStatement("DELETE FROM Blog_Like WHERE blog_id = ? AND user_id = ?")) {
                    ps.setString(1, blogId.toString());
                    ps.setString(2, userId.toString());
                    ps.executeUpdate();
                }
                return false;
            }

            try (PreparedStatement ps = con.prepareStatement("INSERT INTO Blog_Like(blog_id, user_id, created_at) VALUES(?, ?, SYSDATETIME())")) {
                ps.setString(1, blogId.toString());
                ps.setString(2, userId.toString());
                ps.executeUpdate();
            }
            return true;
        }
    }

    private String normalizeRole(String roleName) {
        return roleName == null ? "" : roleName.trim().toLowerCase();
    }

    private void appendVisibilityCondition(StringBuilder sql, List<String> params, String normalizedRole, UUID userId) {
        if ("manager".equals(normalizedRole)) {
            return;
        }

        if ("staff".equals(normalizedRole) && userId != null) {
            sql.append(" AND (LOWER(b.status) = 'approved' OR b.created_by = ?) ");
            params.add(userId.toString());
            return;
        }

        sql.append(" AND LOWER(b.status) = 'approved' ");
    }

    private int bindStringParams(PreparedStatement ps, List<String> params) throws SQLException {
        int idx = 1;
        for (String p : params) {
            ps.setString(idx++, p);
        }
        return idx;
    }

    private Blog mapBlog(ResultSet rs) throws SQLException {
        Blog blog = new Blog();
        blog.setBlogId(toUuid(rs.getString("blog_id")));
        blog.setTitle(rs.getString("title"));
        blog.setSummary(rs.getString("summary"));
        blog.setContent(rs.getString("content"));
        blog.setImageUrl(rs.getString("image_url"));
        blog.setStatus(rs.getString("status"));
        blog.setCreatedBy(toUuid(rs.getString("created_by")));
        blog.setApprovedBy(toUuid(rs.getString("approved_by")));
        blog.setCreatedByName(rs.getString("created_by_name"));
        blog.setApprovedByName(rs.getString("approved_by_name"));
        blog.setLikeCount(rs.getInt("like_count"));
        blog.setCommentCount(rs.getInt("comment_count"));
        blog.setLikedByCurrentUser(rs.getInt("liked_by_current_user") == 1);

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            blog.setCreatedAt(createdAt.toLocalDateTime());
        }

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            blog.setUpdatedAt(updatedAt.toLocalDateTime());
        }

        Timestamp publishedAt = rs.getTimestamp("published_at");
        if (publishedAt != null) {
            blog.setPublishedAt(publishedAt.toLocalDateTime());
        }

        return blog;
    }

    private UUID toUuid(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private String newGuid(Connection con) throws SQLException {
        String sql = "SELECT CONVERT(VARCHAR(36), NEWID())";
        try (PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getString(1);
            }
            throw new SQLException("Cannot generate GUID from SQL Server.");
        }
    }
}
