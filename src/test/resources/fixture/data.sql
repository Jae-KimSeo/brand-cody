-- 테스트용 브랜드 데이터 추가
INSERT INTO brands (id, name, version) VALUES (1, 'TestA', 0);
INSERT INTO brands (id, name, version) VALUES (2, 'TestB', 0);
INSERT INTO brands (id, name, version) VALUES (3, 'TestC', 0);
INSERT INTO brands (id, name, version) VALUES (4, 'TestD', 0);
INSERT INTO brands (id, name, version) VALUES (5, 'TestE', 0);

-- 브랜드 TestA 상품 데이터 추가
INSERT INTO products (id, brand_id, category, price, version) VALUES (1, 1, 'TOP', 10000, 0);
INSERT INTO products (id, brand_id, category, price, version) VALUES (2, 1, 'OUTER', 5000, 0);
INSERT INTO products (id, brand_id, category, price, version) VALUES (3, 1, 'PANTS', 4000, 0);
INSERT INTO products (id, brand_id, category, price, version) VALUES (4, 1, 'SNEAKERS', 8000, 0);
INSERT INTO products (id, brand_id, category, price, version) VALUES (5, 1, 'BAG', 2000, 0);
INSERT INTO products (id, brand_id, category, price, version) VALUES (6, 1, 'HAT', 1500, 0);
INSERT INTO products (id, brand_id, category, price, version) VALUES (7, 1, 'SOCKS', 1500, 0);
INSERT INTO products (id, brand_id, category, price, version) VALUES (8, 1, 'ACCESSORY', 2000, 0);

-- 브랜드 TestB 상품 데이터 추가
INSERT INTO products (id, brand_id, category, price, version) VALUES (9, 2, 'TOP', 9000, 0);
INSERT INTO products (id, brand_id, category, price, version) VALUES (10, 2, 'OUTER', 5500, 0);
INSERT INTO products (id, brand_id, category, price, version) VALUES (11, 2, 'PANTS', 3500, 0);
INSERT INTO products (id, brand_id, category, price, version) VALUES (12, 2, 'SNEAKERS', 9000, 0);
INSERT INTO products (id, brand_id, category, price, version) VALUES (13, 2, 'BAG', 2200, 0);
INSERT INTO products (id, brand_id, category, price, version) VALUES (14, 2, 'HAT', 1800, 0);
INSERT INTO products (id, brand_id, category, price, version) VALUES (15, 2, 'SOCKS', 1800, 0);
INSERT INTO products (id, brand_id, category, price, version) VALUES (16, 2, 'ACCESSORY', 2100, 0);

-- 브랜드 TestC 상품 데이터 추가
INSERT INTO products (id, brand_id, category, price, version) VALUES (17, 3, 'TOP', 9500, 0);
INSERT INTO products (id, brand_id, category, price, version) VALUES (18, 3, 'OUTER', 6000, 0);
INSERT INTO products (id, brand_id, category, price, version) VALUES (19, 3, 'PANTS', 3000, 0);
INSERT INTO products (id, brand_id, category, price, version) VALUES (20, 3, 'SNEAKERS', 8500, 0);
INSERT INTO products (id, brand_id, category, price, version) VALUES (21, 3, 'BAG', 2500, 0);
INSERT INTO products (id, brand_id, category, price, version) VALUES (22, 3, 'HAT', 1600, 0);
INSERT INTO products (id, brand_id, category, price, version) VALUES (23, 3, 'SOCKS', 2000, 0);
INSERT INTO products (id, brand_id, category, price, version) VALUES (24, 3, 'ACCESSORY', 1900, 0);

-- 브랜드 TestD 상품 데이터 추가
INSERT INTO products (id, brand_id, category, price, version) VALUES (25, 4, 'TOP', 11000, 0);
INSERT INTO products (id, brand_id, category, price, version) VALUES (26, 4, 'OUTER', 4800, 0);
INSERT INTO products (id, brand_id, category, price, version) VALUES (27, 4, 'PANTS', 3200, 0);
INSERT INTO products (id, brand_id, category, price, version) VALUES (28, 4, 'SNEAKERS', 9500, 0);
INSERT INTO products (id, brand_id, category, price, version) VALUES (29, 4, 'BAG', 1800, 0);
INSERT INTO products (id, brand_id, category, price, version) VALUES (30, 4, 'HAT', 1400, 0);
INSERT INTO products (id, brand_id, category, price, version) VALUES (31, 4, 'SOCKS', 1600, 0);
INSERT INTO products (id, brand_id, category, price, version) VALUES (32, 4, 'ACCESSORY', 1700, 0);

-- 브랜드 TestE 상품 데이터 추가
INSERT INTO products (id, brand_id, category, price, version) VALUES (33, 5, 'TOP', 10500, 0);
INSERT INTO products (id, brand_id, category, price, version) VALUES (34, 5, 'OUTER', 5200, 0);
INSERT INTO products (id, brand_id, category, price, version) VALUES (35, 5, 'PANTS', 3800, 0);
INSERT INTO products (id, brand_id, category, price, version) VALUES (36, 5, 'SNEAKERS', 8800, 0);
INSERT INTO products (id, brand_id, category, price, version) VALUES (37, 5, 'BAG', 2100, 0);
INSERT INTO products (id, brand_id, category, price, version) VALUES (38, 5, 'HAT', 1700, 0);
INSERT INTO products (id, brand_id, category, price, version) VALUES (39, 5, 'SOCKS', 1900, 0);
INSERT INTO products (id, brand_id, category, price, version) VALUES (40, 5, 'ACCESSORY', 2000, 0);