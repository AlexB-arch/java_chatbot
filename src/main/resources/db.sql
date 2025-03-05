drop table if exists college;
CREATE TABLE college ( 
    id TEXT PRIMARY KEY, 
    name TEXT 
    );

drop table if exists student;
create table student(
    id int, 
    firstname text, 
    lastname text
    );

DROP TABLE IF EXISTS department;
CREATE TABLE department ( 
    id TEXT NOT NULL, 
    name TEXT NOT NULL, 
    collegeID TEXT DEFAULT NULL, 
    PRIMARY KEY (id), 
    FOREIGN KEY (collegeID) REFERENCES college (id)
    );

drop table if exists course;
Create table course (
    id text, 
    department text, 
    title text, 
    num int, 
    hrs int, 
    primary key(id), 
    foreign key (department) REFERENCES department(id)
    );

drop table if exists major; 
create table major (
    id text, 
    title text, 
    deptID text, 
    reqtext text, 
    hrs int, 
    gpa float, 
    primary key(id), 
    foreign key (deptID) references department(id)
    );

Drop table if exists teachers;
CREATE TABLE teachers (
    id INT PRIMARY KEY, 
    firstname TEXT, 
    lastname TEXT, 
    departmentID TEXT, 
    adjunct INT, FOREIGN KEY (departmentID) REFERENCES department(id)
);

drop table if exists section;
create table section (
    crn int, 
    max int, 
    room text, 
    courseID text, 
    term text, 
    startdate date, 
    enddate date, 
    days text, 
    primary key(crn), 
    foreign key (courseID) references course(id)
);

DROP TABLE IF EXISTS student_section;
CREATE TABLE student_section (
    studentID INTEGER, 
    sectionID INTEGER, 
    grade REAL, 
    PRIMARY KEY (studentID, sectionID),
    FOREIGN KEY (sectionID) REFERENCES section(crn), 
    FOREIGN KEY (studentID) REFERENCES student(id)
);

DROP TABLE IF EXISTS major_class;
create table major_class(
    majorID int, 
    classID int, 
    FOREIGN KEY(majorID) REFERENCES major(id), 
    FOREIGN KEY(classID) REFERENCES course(id)
);

DROP TABLE IF EXISTS and_prereq;
CREATE TABLE and_prereq (
    course TEXT,
    prereq TEXT,
    PRIMARY KEY (course, prereq),
    FOREIGN KEY (course) REFERENCES course(id),
    FOREIGN KEY (prereq) REFERENCES course(id)
);

DROP TABLE IF EXISTS or_prereq;
CREATE TABLE or_prereq (
    course TEXT,
    prereq TEXT,
    PRIMARY KEY (course, prereq),
    FOREIGN KEY (course) REFERENCES course(id),
    FOREIGN KEY (prereq) REFERENCES course(id)
);

DROP TABLE IF EXISTS coreq;
CREATE TABLE coreq (
    course TEXT,
    prereq TEXT,
    PRIMARY KEY (course, prereq),
    FOREIGN KEY (course) REFERENCES course(id),
    FOREIGN KEY (prereq) REFERENCES course(id)
);

drop table if exists student_major;
create table student_major(
    studentID INTEGER, 
    major TEXT, 
    PRIMARY KEY (studentID, major), 
    FOREIGN KEY (major) REFERENCES major(id), 
    FOREIGN KEY (studentID) REFERENCES student(id)
);

DROP TABLE IF EXISTS concentration;
create table concentration (
    id text primary key, 
    major text, 
    title text, 
    reqtext text, 
    foreign key(major) references major(id)
);