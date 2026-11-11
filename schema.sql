-- tables for TaskTracker
-- column names match fromResultSet() in User.java and task.java

CREATE TABLE users (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    username      VARCHAR(50)  NOT NULL UNIQUE,
    email         VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    date_created  DATE         NOT NULL
);

CREATE TABLE tasks (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    name          VARCHAR(100) NOT NULL,
    description   TEXT,
    created_by    VARCHAR(50)  NOT NULL,
    assigned_to   VARCHAR(50),
    date_created  DATE         NOT NULL,
    date_finished DATE,
    due_date      DATE,
    priority      VARCHAR(10)  NOT NULL,  -- LOW, MEDIUM, HIGH
    status        VARCHAR(15)  NOT NULL,  -- NOT_STARTED, WIP, COMPLETE
    FOREIGN KEY (created_by)  REFERENCES users(username),
    FOREIGN KEY (assigned_to) REFERENCES users(username)
);
