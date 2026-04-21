CREATE TABLE regions (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL
);

CREATE TABLE oblasts (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    region_id INT REFERENCES regions(id) ON DELETE CASCADE
);

CREATE TABLE settlements (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    population INT,
    oblast_id INT REFERENCES oblasts(id) ON DELETE CASCADE
);