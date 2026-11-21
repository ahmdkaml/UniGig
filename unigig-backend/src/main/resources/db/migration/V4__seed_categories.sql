INSERT INTO categories (name) VALUES
    ('Web Development'),
    ('Mobile Development'),
    ('Graphic Design'),
    ('Writing & Translation'),
    ('Video & Audio'),
    ('Data & Analytics'),
    ('Tutoring'),
    ('Other')
ON CONFLICT (name) DO NOTHING;