USE FifaFieldDB;
GO

IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'Blog')
BEGIN
    CREATE TABLE Blog (
        blog_id VARCHAR(36) PRIMARY KEY,
        title NVARCHAR(255) NOT NULL,
        summary NVARCHAR(500) NULL,
        content NVARCHAR(MAX) NOT NULL,
        image_url NVARCHAR(500) NULL,
        status NVARCHAR(20) NOT NULL DEFAULT N'draft',
        created_by VARCHAR(36) NOT NULL,
        approved_by VARCHAR(36) NULL,
        created_at DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
        updated_at DATETIME2 NULL,
        published_at DATETIME2 NULL,
        CONSTRAINT FK_Blog_Creator FOREIGN KEY (created_by) REFERENCES Users(user_id),
        CONSTRAINT FK_Blog_Approver FOREIGN KEY (approved_by) REFERENCES Users(user_id)
    );
    PRINT 'Created table Blog';
END
ELSE
BEGIN
    PRINT 'Table Blog already exists';
END
GO

IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'Blog_Like')
BEGIN
    CREATE TABLE Blog_Like (
        blog_id VARCHAR(36) NOT NULL,
        user_id VARCHAR(36) NOT NULL,
        created_at DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
        CONSTRAINT PK_Blog_Like PRIMARY KEY (blog_id, user_id),
        CONSTRAINT FK_BlogLike_Blog FOREIGN KEY (blog_id) REFERENCES Blog(blog_id) ON DELETE CASCADE,
        CONSTRAINT FK_BlogLike_User FOREIGN KEY (user_id) REFERENCES Users(user_id)
    );
    PRINT 'Created table Blog_Like';
END
ELSE
BEGIN
    PRINT 'Table Blog_Like already exists';
END
GO

IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'Blog_Comment')
BEGIN
    CREATE TABLE Blog_Comment (
        comment_id VARCHAR(36) PRIMARY KEY,
        blog_id VARCHAR(36) NOT NULL,
        user_id VARCHAR(36) NOT NULL,
        parent_comment_id VARCHAR(36) NULL,
        content NVARCHAR(1000) NOT NULL,
        status NVARCHAR(20) NOT NULL DEFAULT N'active',
        created_at DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
        updated_at DATETIME2 NULL,
        CONSTRAINT FK_BlogComment_Blog FOREIGN KEY (blog_id) REFERENCES Blog(blog_id) ON DELETE CASCADE,
        CONSTRAINT FK_BlogComment_User FOREIGN KEY (user_id) REFERENCES Users(user_id),
        CONSTRAINT FK_BlogComment_Parent FOREIGN KEY (parent_comment_id) REFERENCES Blog_Comment(comment_id) ON DELETE CASCADE
    );
    PRINT 'Created table Blog_Comment';
END
ELSE
BEGIN
    PRINT 'Table Blog_Comment already exists';
END
GO

IF EXISTS (SELECT * FROM sys.tables WHERE name = 'Blog_Comment')
   AND NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('Blog_Comment') AND name = 'parent_comment_id')
BEGIN
    ALTER TABLE Blog_Comment ADD parent_comment_id VARCHAR(36) NULL;
    ALTER TABLE Blog_Comment
        ADD CONSTRAINT FK_BlogComment_Parent FOREIGN KEY (parent_comment_id) REFERENCES Blog_Comment(comment_id) ON DELETE CASCADE;
    PRINT 'Added parent_comment_id to Blog_Comment';
END
GO

IF EXISTS (SELECT * FROM sys.tables WHERE name = 'Blog_Comment')
   AND EXISTS (SELECT * FROM sys.foreign_keys WHERE name = 'FK_BlogComment_Parent')
BEGIN
    ALTER TABLE Blog_Comment DROP CONSTRAINT FK_BlogComment_Parent;
    ALTER TABLE Blog_Comment
        ADD CONSTRAINT FK_BlogComment_Parent FOREIGN KEY (parent_comment_id) REFERENCES Blog_Comment(comment_id) ON DELETE CASCADE;
    PRINT 'Updated FK_BlogComment_Parent to ON DELETE CASCADE';
END
GO

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IDX_Blog_Status_PublishedAt' AND object_id = OBJECT_ID('Blog'))
BEGIN
    CREATE INDEX IDX_Blog_Status_PublishedAt ON Blog(status, published_at DESC);
END
GO

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IDX_Blog_CreatedBy' AND object_id = OBJECT_ID('Blog'))
BEGIN
    CREATE INDEX IDX_Blog_CreatedBy ON Blog(created_by);
END
GO

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IDX_BlogComment_Blog' AND object_id = OBJECT_ID('Blog_Comment'))
BEGIN
    CREATE INDEX IDX_BlogComment_Blog ON Blog_Comment(blog_id, created_at DESC);
END
GO

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IDX_BlogComment_Parent' AND object_id = OBJECT_ID('Blog_Comment'))
BEGIN
    CREATE INDEX IDX_BlogComment_Parent ON Blog_Comment(parent_comment_id);
END
GO
