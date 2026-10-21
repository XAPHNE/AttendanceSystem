-- Demo data so a fresh install has something to show. All people are fictional.
-- Logins: admin / admin123 (Admin), employee / employee123 (Employee).

INSERT INTO admin (username, password_hash, user_role) VALUES
    ('admin',    '$2a$12$/TSI06SDr1tpZOG3Uqpko.iGs508jyAlyUHW1IfRenmBv/Jq8TekO', 'Admin'),
    ('employee', '$2a$12$ZrsO0HCV5Q78ynVoI1eDmOqQxP75WN6zzlhbbaXGeH20j/GeQgZQW', 'Employee');

INSERT INTO course (course, description, degree) VALUES
    ('BCA',  'Bachelor of Computer Applications', 'Bachelor'),
    ('BSc',  'Bachelor of Science',               'Bachelor'),
    ('MCA',  'Master of Computer Applications',   'Master');

INSERT INTO student (studentNum, semester, course, firstName, lastName, gender, birthDate, status, image, added_on) VALUES
    (1001, '3rd Sem', 'BCA', 'Asha',   'Verma',  'Female', DATE '2005-04-12', 'Enrolled',  '', CURRENT_DATE - 30),
    (1002, '3rd Sem', 'BCA', 'Rohan',  'Das',    'Male',   DATE '2005-09-03', 'Enrolled',  '', CURRENT_DATE - 30),
    (1003, '1st Sem', 'BSc', 'Meera',  'Nair',   'Female', DATE '2006-01-21', 'Enrolled',  '', CURRENT_DATE - 10),
    (1004, '5th Sem', 'BSc', 'Kabir',  'Singh',  'Male',   DATE '2004-07-30', 'Enrolled',  '', CURRENT_DATE - 60),
    (1005, '2nd Sem', 'MCA', 'Tara',   'Bose',   'Other',  DATE '2002-11-15', 'Enrolled',  '', CURRENT_DATE - 5),
    (1006, 'Graduated', 'BCA', 'Imran', 'Khan',  'Male',   DATE '2001-02-08', 'Graduated', '', CURRENT_DATE - 400);

-- Attendance relative to the day the database is created, so the dashboard
-- shows "today" and a few days of history on first run.
INSERT INTO student_attendance (student_id, course_id, entry_time, exit_time, status)
SELECT s.id, c.id,
       DATEADD(HOUR, 9, CAST(CURRENT_DATE - d.x AS TIMESTAMP)),
       DATEADD(HOUR, 15, CAST(CURRENT_DATE - d.x AS TIMESTAMP)),
       CASE WHEN MOD(s.studentNum + d.x, 4) = 0 THEN 'Absent' ELSE 'Present' END
FROM student s
JOIN course c ON c.course = s.course
CROSS JOIN (VALUES 0, 1, 2, 3, 4) AS d(x)
WHERE s.status = 'Enrolled';
