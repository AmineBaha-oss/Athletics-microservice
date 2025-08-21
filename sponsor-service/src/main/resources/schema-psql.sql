
CREATE TABLE IF NOT EXISTS sponsors (
                                        id SERIAL PRIMARY KEY,
                                        sponsor_id VARCHAR(36) UNIQUE NOT NULL,
    sponsor_name VARCHAR(100) NOT NULL,
    sponsor_level VARCHAR(50) NOT NULL,
    sponsor_amount DECIMAL(19,2) NOT NULL
    );
