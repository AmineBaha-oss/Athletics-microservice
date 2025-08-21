DROP TABLE IF EXISTS teams;

CREATE TABLE IF NOT EXISTS teams (
                                     id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                                     team_id VARCHAR(36) UNIQUE NOT NULL,
    team_name VARCHAR(100) NOT NULL,
    coach_name VARCHAR(100) NOT NULL,
    team_level VARCHAR(50) NOT NULL
    );
DROP TABLE IF EXISTS athletes;

CREATE TABLE IF NOT EXISTS athletes (
                                        id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                                        athlete_id VARCHAR(36) UNIQUE NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    date_of_birth DATE NOT NULL,
    athlete_category VARCHAR(50) NOT NULL,
    team_id VARCHAR(36)
    );
