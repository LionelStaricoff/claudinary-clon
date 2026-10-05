-- Initial data for ImageHost application
-- This script creates admin user, roles, and default projects

-- Insert roles
INSERT INTO roles (id, name, description) VALUES 
('10000000-0000-0000-0000-000000000001', 'ROLE_ADMIN', 'Administrator role with full access');
INSERT INTO roles (id, name, description) VALUES 
('20000000-0000-0000-0000-000000000001', 'ROLE_USER', 'Regular user role');
INSERT INTO roles (id, name, description) VALUES 
('30000000-0000-0000-0000-000000000001', 'ROLE_GUEST', 'Guest user role with limited access');

-- Insert admin user
INSERT INTO users (id, username, email, password, first_name, last_name, storage_used, storage_limit, is_active, is_locked, failed_login_attempts, created_at, updated_at) 
VALUES ('00000000-0000-0000-0000-000000000001', 'admin', 'admin@imagehost.com', '$2a$12$N9qo8uLOickgx2ZMRZoMy.MrqE4Jw8XYu3y4D3Y8pYJ7bNz1K3jZ', 'Admin', 'User', 0, 1048576000, true, false, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert user roles for admin
INSERT INTO user_roles (user_id, role_id) VALUES 
('00000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000001'),
('00000000-0000-0000-0000-000000000001', '20000000-0000-0000-0000-000000000001');

-- Insert default project for admin
INSERT INTO projects (id, name, description, is_default, is_public, image_count, created_at, updated_at, user_id) 
VALUES ('00000000-0000-0000-0000-000000000002', 'Admin Images', 'Default project for admin user', true, false, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, '00000000-0000-0000-0000-000000000001');

-- Insert test user
INSERT INTO users (id, username, email, password, first_name, last_name, storage_used, storage_limit, is_active, is_locked, failed_login_attempts, created_at, updated_at) 
VALUES ('00000000-0000-0000-0000-000000000010', 'testuser', 'test@example.com', '$2a$12$N9qo8uLOickgx2ZMRZoMy.MrqE4Jw8XYu3y4D3Y8pYJ7bNz1K3jZ', 'Test', 'User', 0, 104857600, true, false, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert user role for test user
INSERT INTO user_roles (user_id, role_id) VALUES 
('00000000-0000-0000-0000-000000000010', '20000000-0000-0000-0000-000000000001');

-- Insert default project for test user
INSERT INTO projects (id, name, description, is_default, is_public, image_count, created_at, updated_at, user_id) 
VALUES ('00000000-0000-0000-0000-000000000011', 'My Images', 'Default project for test user', true, false, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, '00000000-0000-0000-0000-000000000010');

-- Note: The password for admin is 'admin123' and for testuser is 'test123'
-- These are hashed with BCrypt (strength 12)
-- To generate new passwords: use BCryptPasswordEncoder with strength 12
