-- Connect to auth-db and insert default users
\c "auth-db";

-- Insert default users with BCrypt hashed passwords
-- admin/admin123 -> $2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM5lE9cBNKt6I5O9w8Oy
-- client/client123 -> $2a$10$5H7akTE7y9mBuVMWewYKNOxaF7K5XQnhkUOKkpxTJ8.VRVKjQZ8Oy  
-- test/password -> $2a$10$e0MYzXyjpJS7Pd0RVvHqHOxHjJZPy/nBwMae0MaiAMgCO7fUznoFm

INSERT INTO users (username, password, role) VALUES 
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM5lE9cBNKt6I5O9w8Oy', 'ADMIN'),
('client', '$2a$10$5H7akTE7y9mBuVMWewYKNOxaF7K5XQnhkUOKkpxTJ8.VRVKjQZ8Oy', 'CLIENT'),
('test', '$2a$10$e0MYzXyjpJS7Pd0RVvHqHOxHjJZPy/nBwMae0MaiAMgCO7fUznoFm', 'CLIENT')
ON CONFLICT (username) DO NOTHING;