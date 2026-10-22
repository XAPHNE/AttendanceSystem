# Database

The app keeps its data in an embedded [H2](https://h2database.com) database, one file in the
per-user data folder that `AppPaths` picks for each OS. Nothing has
to be installed: on every start [Flyway](https://flywaydb.org) runs any migration in
`src/main/resources/db/migration` that hasn't run yet.

| Migration | What it does |
|---|---|
| `V1__schema.sql` | Creates the four tables below |
| `V2__demo_data.sql` | Adds two logins, three courses and a few fictional students with attendance |

To change the schema, add a new `V3__what_it_does.sql`. Never edit a migration that has already
been released: Flyway checks each one's checksum and refuses to start if one has changed.

## Tables

```mermaid
erDiagram
    admin {
        BIGINT id PK
        VARCHAR username UK
        VARCHAR password_hash "bcrypt"
        VARCHAR user_role "Admin or Employee"
    }
    course {
        INTEGER id PK
        VARCHAR course UK "e.g. BCA"
        VARCHAR description
        VARCHAR degree
    }
    student {
        BIGINT id PK
        INTEGER studentNum UK
        VARCHAR semester
        VARCHAR course "course name"
        VARCHAR firstName
        VARCHAR lastName
        ENUM gender "Male, Female, Other"
        DATE birthDate
        VARCHAR status
        VARCHAR image "photo file name or empty"
        DATE added_on
    }
    student_attendance {
        BIGINT id PK
        BIGINT student_id FK
        INTEGER course_id FK
        TIMESTAMP entry_time
        TIMESTAMP exit_time
        VARCHAR status "Present or Absent"
    }

    student ||--o{ student_attendance : "has"
    course ||--o{ student_attendance : "for"
    course ||..o{ student : "by name"
```

- **admin**: the people who can log in. Only a bcrypt hash of the password is stored. An
  *Admin* can manage users; an *Employee* can't.
- **course**: the courses a student can be enrolled in.
- **student**: one row per student. `image` holds only a file name. The photo itself is copied
  into `images/` in the data folder, so the database works on any machine.
- **student_attendance**: one row per entry/exit. Deleting a student deletes their attendance
  (`ON DELETE CASCADE`). A course that has attendance rows can't be deleted.

## Things worth knowing

- `student.course` stores the course **name**, not a foreign key. This is kept from the original
  schema. So the database alone doesn't stop a course from being deleted while students use it;
  `CourseService` checks for that first and throws `StillInUseException`. Making it a real foreign
  key would be a good next migration.
- Column names mix `camelCase` and `snake_case` because V1 kept the names of the original
  database, so the existing SQL didn't have to change.
- Demo logins: `admin` / `admin123` (Admin) and `employee` / `employee123` (Employee).
