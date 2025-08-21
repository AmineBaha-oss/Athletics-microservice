USE `facility-db`;
CREATE TABLE IF NOT EXISTS facilities (
                                          id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                                          facility_id VARCHAR(36) UNIQUE NOT NULL,
    facility_name VARCHAR(100) NOT NULL,
    capacity INT NOT NULL,
    location VARCHAR(100) NOT NULL
    );