USE lab5_social;

INSERT INTO users (username, password_hash, display_name, gender, birthday, age) VALUES
('alice', '{noop}123456', 'Alice', 'F', '2004-03-01', 22),
('bob', '{noop}123456', 'Bob', 'M', '2003-09-12', 23),
('cathy', '{noop}123456', 'Cathy', 'F', '2004-11-20', 22);

INSERT INTO admins (username, password_hash, display_name) VALUES
('admin', '{noop}admin123', 'System Admin');

INSERT INTO friend_groups (user_id, name) VALUES
(1, 'Classmates'),
(1, 'Project Team'),
(2, 'Friends');

INSERT INTO friendships (owner_id, friend_id, group_id) VALUES
(1, 2, 1),
(1, 3, 2),
(2, 1, 3);

INSERT INTO posts (author_id, content) VALUES
(2, 'Today I finished the Lab5 backend API. The keyword search works well.'),
(3, 'I plan to review MySQL triggers and transactions in the library this weekend.'),
(1, 'The Vue3 page is running. Next step is the AI post draft assistant.');

INSERT INTO comments (post_id, author_id, content) VALUES
(1, 1, 'Use keyword as the demo search term.'),
(2, 1, 'Remember to explain the trigger in the report.'),
(3, 2, 'The AI key must stay on the backend.');
