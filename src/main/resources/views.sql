-- 1. Required grade for CS375
DROP VIEW IF EXISTS v_required_grade;
CREATE VIEW v_required_grade AS
SELECT id, 
       CASE 
         WHEN id = ? THEN 'C'
         ELSE 'Unknown'
       END AS RequiredGrade
FROM course
WHERE id = ?;

-- 2. What concentrations does the Computer Science major have?
DROP VIEW IF EXISTS v_major_concentrations;
CREATE VIEW v_major_concentrations AS
SELECT m.title AS Major, 
       c.title AS Concentration
FROM major m
JOIN concentration c ON m.id = c.major
WHERE m.title = ?;  -- replace '?' with the major you want to query

-- 3. How many more hours are needed to complete my major requirements?
DROP VIEW IF EXISTS v_hours_remaining_major;
CREATE VIEW v_hours_remaining_major AS
SELECT sm.studentID, 
       m.title AS Major,
       m.hrs - COALESCE(t.completed, 0) AS HoursRemaining
FROM student_major sm
JOIN major m ON sm.major = m.id
LEFT JOIN (
    SELECT ss.studentID, SUM(c.hrs) AS completed
    FROM student_section ss
    JOIN section s ON ss.sectionID = s.crn
    JOIN course c ON s.courseID = c.id
    WHERE ss.grade IS NOT NULL  -- adjust if needed to consider only passing grades
    GROUP BY ss.studentID
) t ON sm.studentID = t.studentID
WHERE sm.studentID = ?
  AND sm.major = ?;

-- 4. What classes am I in?
DROP VIEW IF EXISTS v_classes_in;
CREATE VIEW v_classes_in AS
SELECT c.id AS CourseID,
       c.title AS CourseTitle,
       c.hrs AS CreditHours,
       s.crn AS SectionID,
       s.room AS Room
FROM student_section ss
JOIN section s ON ss.sectionID = s.crn
JOIN course c ON s.courseID = c.id
WHERE ss.studentID = ?;

-- 5. How many hours am I taking?
DROP VIEW IF EXISTS v_total_hours_taking;
CREATE VIEW v_total_hours_taking AS
SELECT ss.studentID,
       SUM(c.hrs) AS TotalHoursTaking
FROM student_section ss
JOIN section s ON ss.sectionID = s.crn
JOIN course c ON s.courseID = c.id
WHERE ss.studentID = ?
GROUP BY ss.studentID;

-- 6. What department is my major in?
DROP VIEW IF EXISTS v_major_department;
CREATE VIEW v_major_department AS
SELECT sm.studentID,
       m.title AS Major,
       d.id AS DepartmentID,
       d.name AS DepartmentName
FROM student_major sm
JOIN major m ON sm.major = m.id
JOIN department d ON m.deptID = d.id
WHERE sm.studentID = ?;

-- 7. Who is my professor for CS375 class? (Requires section.teacherID to be set)
DROP VIEW IF EXISTS v_professor_cs375;
CREATE VIEW v_professor_cs375 AS
SELECT t.id AS TeacherID,
       t.firstname,
       t.lastname
FROM student_section ss
JOIN section s ON ss.sectionID = s.crn
JOIN course c ON s.courseID = c.id
JOIN teachers t ON s.teacherID = t.id
WHERE ss.studentID = ?
  AND c.id = ?;

-- 8. What majors am I in?
DROP VIEW IF EXISTS v_student_majors;
CREATE VIEW v_student_majors AS
SELECT sm.studentID,
       m.title AS Major
FROM student_major sm
JOIN major m ON sm.major = m.id
WHERE sm.studentID = ?;

-- 9. What classes do I need to graduate?
DROP VIEW IF EXISTS v_classes_needed_graduate;
CREATE VIEW v_classes_needed_graduate AS
SELECT c.id AS CourseID,
       c.title AS CourseTitle,
       c.hrs AS CreditHours
FROM course c
WHERE c.id IN ?
  AND c.id NOT IN (
    SELECT s.courseID
    FROM student_section ss
    JOIN section s ON ss.sectionID = s.crn
    WHERE ss.studentID = ?
      AND ss.grade IS NOT NULL  -- adjust if needed to only count passing grades
  );

-- 10. Which department's classes do I attend the most?
DROP VIEW IF EXISTS v_most_departments_class;
CREATE VIEW v_most_departments_class AS
SELECT d.id AS DepartmentID,
       d.name AS DepartmentName,
       COUNT(*) AS TotalClassesTaken
FROM student_section ss
JOIN section s ON ss.sectionID = s.crn
JOIN course c ON s.courseID = c.id
JOIN department d ON c.department = d.id
WHERE ss.studentID = ?
GROUP BY d.id, d.name
ORDER BY TotalClassesTaken DESC
LIMIT 1;

-- 11. What classes am I currently in? (no grade recorded)
DROP VIEW IF EXISTS v_classes_current;
CREATE VIEW v_classes_current AS
SELECT c.id AS CourseID,
       c.title AS CourseTitle,
       c.hrs AS CreditHours,
       s.crn AS SectionID,
       s.room AS Room
FROM student_section ss
JOIN section s ON ss.sectionID = s.crn
JOIN course c ON s.courseID = c.id
WHERE ss.studentID = ?
  AND ss.grade IS NULL;

-- 12. How many hours more do I need to graduate?
DROP VIEW IF EXISTS v_hours_remaining;
CREATE VIEW v_hours_remaining AS
SELECT sm.studentID,
       m.title AS Major,
       m.hrs - COALESCE(t.CompletedHours, 0) AS HoursRemaining
FROM student_major sm
JOIN major m ON sm.major = m.id
LEFT JOIN (
    SELECT ss.studentID,
           SUM(c.hrs) AS CompletedHours
    FROM student_section ss
    JOIN section s ON ss.sectionID = s.crn
    JOIN course c ON s.courseID = c.id
    WHERE ss.grade IS NOT NULL  -- adjusting this clause lets you count only completed/passing courses
    GROUP BY ss.studentID
) t ON sm.studentID = t.studentID
WHERE sm.studentID = ?;

-- 13. How many teachers are in the SITC department?
DROP VIEW IF EXISTS v_teachers_sitc_count;
CREATE VIEW v_teachers_sitc_count AS
SELECT COUNT(*) AS TeachersInSITC
FROM teachers
WHERE departmentID = ?;

-- 14. What are the first and last names of all teachers in the SITC department (concatenated)?
DROP VIEW IF EXISTS v_teachers_sitc_names;
CREATE VIEW v_teachers_sitc_names AS
SELECT lastname || ', ' || firstname AS TeacherName
FROM teachers
WHERE departmentID = ?;