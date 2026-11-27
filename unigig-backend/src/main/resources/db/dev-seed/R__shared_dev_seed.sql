-- Dev seed data. Loaded ONLY with the `shared-dev` Spring profile (see README).
-- Repeatable + idempotent: safe to re-run, never overwrites or duplicates existing rows.
-- All seeded users share the test-only password: Password123!

-- 'admin' is listed first so it gets id 1 on a fresh database. It is a normal CLIENT account.
INSERT INTO users (full_name, email, password_hash, role) VALUES
    ('admin',            'admin@unigig.test',       '$2a$10$FlSTsW/dj2LUEsmx3.fvF.IoS39kNncNcb54U3waObbGJCrWvUqlG', 'CLIENT'),
    ('Nour Client',      'client1@unigig.test',     '$2a$10$FlSTsW/dj2LUEsmx3.fvF.IoS39kNncNcb54U3waObbGJCrWvUqlG', 'CLIENT'),
    ('Omar Client',      'client2@unigig.test',     '$2a$10$FlSTsW/dj2LUEsmx3.fvF.IoS39kNncNcb54U3waObbGJCrWvUqlG', 'CLIENT'),
    ('Salma Freelancer', 'freelancer1@unigig.test', '$2a$10$FlSTsW/dj2LUEsmx3.fvF.IoS39kNncNcb54U3waObbGJCrWvUqlG', 'FREELANCER'),
    ('Youssef Freelancer','freelancer2@unigig.test','$2a$10$FlSTsW/dj2LUEsmx3.fvF.IoS39kNncNcb54U3waObbGJCrWvUqlG', 'FREELANCER')
ON CONFLICT (email) DO NOTHING;

INSERT INTO freelancer_profiles (user_id, title, bio, skills, portfolio_url)
SELECT u.id, v.title, v.bio, v.skills, v.portfolio_url
FROM (VALUES
    ('freelancer1@unigig.test', 'Full-stack student developer', 'CSE student who enjoys Angular and Spring Boot.',
        ARRAY['Angular', 'Spring Boot', 'PostgreSQL']::text[], 'https://example.com/salma'),
    ('freelancer2@unigig.test', 'Graphic & UI designer', 'Posters, logos and app mockups.',
        ARRAY['Figma', 'Illustrator']::text[], 'https://example.com/youssef')
) AS v(email, title, bio, skills, portfolio_url)
JOIN users u ON u.email = v.email
ON CONFLICT (user_id) DO NOTHING;

-- 14 OPEN gigs (more than one page of 10) + non-OPEN gigs that must never show in the marketplace.
INSERT INTO gigs (title, description, status, client_id, category_id, created_at, updated_at)
SELECT v.title, v.description, v.status, c.id, cat.id,
       NOW() - (v.age_hours * INTERVAL '1 hour'), NOW() - (v.age_hours * INTERVAL '1 hour')
FROM (VALUES
    ('Landing page for a student club',  'One-page responsive site for the robotics club.',          'OPEN', 'client1@unigig.test', 'Web Development',      1),
    ('Angular dashboard',                'Admin dashboard with charts and tables.',                  'OPEN', 'client1@unigig.test', 'Web Development',      2),
    ('Fix Spring Boot REST bug',         'Endpoint returns 500 on empty payload.',                   'OPEN', 'client2@unigig.test', 'Web Development',      3),
    ('Portfolio website',                'Personal portfolio with a contact form.',                  'OPEN', 'client2@unigig.test', 'Web Development',      4),
    ('Android study planner',            'Simple planner app with reminders.',                       'OPEN', 'client1@unigig.test', 'Mobile Development',   5),
    ('Flutter campus map',               'Map of faculty buildings with search.',                    'OPEN', 'client2@unigig.test', 'Mobile Development',   6),
    ('Club logo redesign',               'Modern logo and colour palette.',                          'OPEN', 'client1@unigig.test', 'Graphic Design',       7),
    ('Event poster',                     'A3 poster for the annual career fair.',                    'OPEN', 'client2@unigig.test', 'Graphic Design',       8),
    ('Translate thesis abstract',        'English to Arabic, about 300 words.',                      'OPEN', 'client1@unigig.test', 'Writing & Translation', 9),
    ('Proofread lab report',             'Proofread a 10-page networks lab report.',                 'OPEN', 'client2@unigig.test', 'Writing & Translation', 10),
    ('Edit a promo video',               'Cut a 60 second promo from raw footage.',                  'OPEN', 'client1@unigig.test', 'Video & Audio',        11),
    ('Clean survey dataset',             'Clean and summarise a CSV survey in Python.',              'OPEN', 'client2@unigig.test', 'Data & Analytics',     12),
    ('Calculus tutoring',                'Weekly sessions before the midterm.',                      'OPEN', 'client1@unigig.test', 'Tutoring',             13),
    ('Misc: help moving lab equipment',  'Two hours of help on campus.',                             'OPEN', 'client2@unigig.test', 'Other',                14),
    ('Build a REST API (in progress)',   'Already assigned, should not be listed.',                  'IN_PROGRESS', 'client1@unigig.test', 'Web Development', 15),
    ('Mobile app prototype (in progress)','Already assigned, should not be listed.',                 'IN_PROGRESS', 'client2@unigig.test', 'Mobile Development', 16),
    ('Finished poster (completed)',      'Delivered, should not be listed.',                         'COMPLETED',   'client1@unigig.test', 'Graphic Design', 17),
    ('Cancelled tutoring (canceled)',    'Canceled by client, should not be listed.',                'CANCELED',    'client2@unigig.test', 'Tutoring', 18)
) AS v(title, description, status, client_email, category, age_hours)
JOIN users c ON c.email = v.client_email
JOIN categories cat ON cat.name = v.category
WHERE NOT EXISTS (
    SELECT 1 FROM gigs g WHERE g.title = v.title AND g.client_id = c.id
);