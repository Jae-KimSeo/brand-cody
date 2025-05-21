DROP TABLE IF EXISTS products;
DROP TABLE IF EXISTS brands;

CREATE TABLE brands (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    brand_id BIGINT NOT NULL,
    category VARCHAR(30) NOT NULL,
    price INT NOT NULL,
    FOREIGN KEY (brand_id) REFERENCES brands(id),
    CONSTRAINT uk_brand_category UNIQUE (brand_id, category)
);