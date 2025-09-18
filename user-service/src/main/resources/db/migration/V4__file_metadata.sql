CREATE TABLE file_metadata (
    id BIGSERIAL PRIMARY KEY,
    file_name VARCHAR(500) NOT NULL UNIQUE,
    original_name VARCHAR(255) NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    file_size BIGINT NOT NULL,
    folder VARCHAR(100) NOT NULL,
    bucket VARCHAR(100) NOT NULL,
    uploaded_by VARCHAR(100) NOT NULL,
    file_path VARCHAR(600) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE
);

-- Create indexes separately in PostgreSQL
CREATE INDEX idx_file_name ON file_metadata (file_name);
CREATE INDEX idx_uploaded_by ON file_metadata (uploaded_by);
CREATE INDEX idx_folder ON file_metadata (folder);
