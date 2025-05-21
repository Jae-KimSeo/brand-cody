-- 테스트용 브랜드 데이터 추가
INSERT INTO brands (id, name) VALUES (1, 'TestA');
INSERT INTO brands (id, name) VALUES (2, 'TestB');
INSERT INTO brands (id, name) VALUES (3, 'TestC');
INSERT INTO brands (id, name) VALUES (4, 'TestD');
INSERT INTO brands (id, name) VALUES (5, 'TestE');

-- 브랜드 TestA 상품 데이터 추가
INSERT INTO products (id, brand_id, category, price) VALUES (1, 1, 'TOP', 10000);
INSERT INTO products (id, brand_id, category, price) VALUES (2, 1, 'OUTER', 5000);
INSERT INTO products (id, brand_id, category, price) VALUES (3, 1, 'PANTS', 4000);
INSERT INTO products (id, brand_id, category, price) VALUES (4, 1, 'SNEAKERS', 8000);
INSERT INTO products (id, brand_id, category, price) VALUES (5, 1, 'BAG', 2000);
INSERT INTO products (id, brand_id, category, price) VALUES (6, 1, 'HAT', 1500);
INSERT INTO products (id, brand_id, category, price) VALUES (7, 1, 'SOCKS', 1500);
INSERT INTO products (id, brand_id, category, price) VALUES (8, 1, 'ACCESSORY', 2000);

-- 브랜드 TestB 상품 데이터 추가
INSERT INTO products (id, brand_id, category, price) VALUES (9, 2, 'TOP', 9000);
INSERT INTO products (id, brand_id, category, price) VALUES (10, 2, 'OUTER', 5500);
INSERT INTO products (id, brand_id, category, price) VALUES (11, 2, 'PANTS', 3500);
INSERT INTO products (id, brand_id, category, price) VALUES (12, 2, 'SNEAKERS', 9000);
INSERT INTO products (id, brand_id, category, price) VALUES (13, 2, 'BAG', 2200);
INSERT INTO products (id, brand_id, category, price) VALUES (14, 2, 'HAT', 1800);
INSERT INTO products (id, brand_id, category, price) VALUES (15, 2, 'SOCKS', 1800);
INSERT INTO products (id, brand_id, category, price) VALUES (16, 2, 'ACCESSORY', 2100);

-- 브랜드 TestC 상품 데이터 추가
INSERT INTO products (id, brand_id, category, price) VALUES (17, 3, 'TOP', 9500);
INSERT INTO products (id, brand_id, category, price) VALUES (18, 3, 'OUTER', 6000);
INSERT INTO products (id, brand_id, category, price) VALUES (19, 3, 'PANTS', 3000);
INSERT INTO products (id, brand_id, category, price) VALUES (20, 3, 'SNEAKERS', 8500);
INSERT INTO products (id, brand_id, category, price) VALUES (21, 3, 'BAG', 2500);
INSERT INTO products (id, brand_id, category, price) VALUES (22, 3, 'HAT', 1600);
INSERT INTO products (id, brand_id, category, price) VALUES (23, 3, 'SOCKS', 2000);
INSERT INTO products (id, brand_id, category, price) VALUES (24, 3, 'ACCESSORY', 1900);

-- 브랜드 TestD 상품 데이터 추가
INSERT INTO products (id, brand_id, category, price) VALUES (25, 4, 'TOP', 11000);
INSERT INTO products (id, brand_id, category, price) VALUES (26, 4, 'OUTER', 4800);
INSERT INTO products (id, brand_id, category, price) VALUES (27, 4, 'PANTS', 3200);
INSERT INTO products (id, brand_id, category, price) VALUES (28, 4, 'SNEAKERS', 9500);
INSERT INTO products (id, brand_id, category, price) VALUES (29, 4, 'BAG', 1800);
INSERT INTO products (id, brand_id, category, price) VALUES (30, 4, 'HAT', 1400);
INSERT INTO products (id, brand_id, category, price) VALUES (31, 4, 'SOCKS', 1600);
INSERT INTO products (id, brand_id, category, price) VALUES (32, 4, 'ACCESSORY', 1700);

-- 브랜드 TestE 상품 데이터 추가
INSERT INTO products (id, brand_id, category, price) VALUES (33, 5, 'TOP', 10500);
INSERT INTO products (id, brand_id, category, price) VALUES (34, 5, 'OUTER', 5200);
INSERT INTO products (id, brand_id, category, price) VALUES (35, 5, 'PANTS', 3800);
INSERT INTO products (id, brand_id, category, price) VALUES (36, 5, 'SNEAKERS', 8800);
INSERT INTO products (id, brand_id, category, price) VALUES (37, 5, 'BAG', 2100);
INSERT INTO products (id, brand_id, category, price) VALUES (38, 5, 'HAT', 1700);
INSERT INTO products (id, brand_id, category, price) VALUES (39, 5, 'SOCKS', 1900);
INSERT INTO products (id, brand_id, category, price) VALUES (40, 5, 'ACCESSORY', 2000);