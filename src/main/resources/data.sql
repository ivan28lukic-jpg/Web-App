-- H2: privremeno ugasi FK da bi čišćenje prošlo
SET REFERENTIAL_INTEGRITY FALSE;

-- obriši podatke (redosled po zavisnostima)
DELETE FROM transactions;
DELETE FROM recurring_instances;
DELETE FROM recurring_templates;
DELETE FROM saving_goals;
DELETE FROM admin_notes;
DELETE FROM categories;
DELETE FROM wallets;
DELETE FROM currencies;
DELETE FROM users;

SET REFERENTIAL_INTEGRITY TRUE;

---------------------------------------
-- USERS (za dev: {noop}pass)
---------------------------------------
INSERT INTO users (avatar_path, birth_date, blocked, email, first_name, last_name, password_hash, preferred_currency_code, registered_at, role, username)
VALUES
  (NULL, DATE '1990-01-10', FALSE, 'admin@example.com',  'System', 'Admin', '{noop}pass', 'RSD', CURRENT_TIMESTAMP, 'ADMIN', 'admin'),
  (NULL, DATE '1992-03-15', FALSE, 'nikola@example.com', 'Nikola', 'Nikolic','{noop}pass', 'RSD', CURRENT_TIMESTAMP, 'USER',  'nikola'),
  (NULL, DATE '1991-06-20', FALSE, 'ivan@example.com',   'Ivan',   'Ivic',  '{noop}pass', 'RSD', CURRENT_TIMESTAMP, 'USER',  'ivan'),
  (NULL, DATE '1993-11-05', FALSE, 'ana@example.com',    'Ana',    'Anic',  '{noop}pass', 'RSD', CURRENT_TIMESTAMP, 'USER',  'ana');

---------------------------------------
-- CURRENCIES
---------------------------------------
INSERT INTO currencies (code, name, updated_at, value_vs_eur) VALUES
 ('RSD','Serbian Dinar', CURRENT_TIMESTAMP, 117.300000),
 ('EUR','Euro',          CURRENT_TIMESTAMP,   1.000000),
 ('USD','US Dollar',     CURRENT_TIMESTAMP,   1.080000),
 ('GBP','British Pound', CURRENT_TIMESTAMP,   0.860000);

---------------------------------------
-- WALLETS (glavni + devizni)
---------------------------------------
-- Nikola: RSD + EUR
INSERT INTO wallets (archived, balance, created_at, name, savings, currency_id, owner_id)
VALUES
 (FALSE, 106200.00, CURRENT_TIMESTAMP, 'Nikola Main', FALSE,
   (SELECT id FROM currencies WHERE code='RSD'),
   (SELECT id FROM users WHERE username='nikola')),
 (FALSE,   1200.00, CURRENT_TIMESTAMP, 'Nikola Euro', FALSE,
   (SELECT id FROM currencies WHERE code='EUR'),
   (SELECT id FROM users WHERE username='nikola'));

-- Ivan: RSD + USD
INSERT INTO wallets (archived, balance, created_at, name, savings, currency_id, owner_id)
VALUES
 (FALSE,  80250.00, CURRENT_TIMESTAMP, 'Ivan Main', FALSE,
   (SELECT id FROM currencies WHERE code='RSD'),
   (SELECT id FROM users WHERE username='ivan')),
 (FALSE,    950.00, CURRENT_TIMESTAMP, 'Ivan USD', FALSE,
   (SELECT id FROM currencies WHERE code='USD'),
   (SELECT id FROM users WHERE username='ivan'));

-- Ana: RSD + GBP
INSERT INTO wallets (archived, balance, created_at, name, savings, currency_id, owner_id)
VALUES
 (FALSE,  52100.00, CURRENT_TIMESTAMP, 'Ana Main', FALSE,
   (SELECT id FROM currencies WHERE code='RSD'),
   (SELECT id FROM users WHERE username='ana')),
 (FALSE,    420.00, CURRENT_TIMESTAMP, 'Ana GBP', FALSE,
   (SELECT id FROM currencies WHERE code='GBP'),
   (SELECT id FROM users WHERE username='ana'));

-- === Savings wallets per user (RSD / EUR / USD) ===
-- Nikola -> RSD
INSERT INTO wallets (archived, balance, created_at, name, savings, currency_id, owner_id)
SELECT FALSE, 0, CURRENT_TIMESTAMP, 'Nikola Savings', TRUE,
       (SELECT id FROM currencies WHERE code='RSD'), u.id
FROM users u
WHERE u.username = 'nikola'
  AND NOT EXISTS (
    SELECT 1 FROM wallets w
    WHERE w.owner_id = u.id
      AND w.savings = TRUE
      AND w.currency_id = (SELECT id FROM currencies WHERE code='RSD')
  );

-- Ana -> EUR
INSERT INTO wallets (archived, balance, created_at, name, savings, currency_id, owner_id)
SELECT FALSE, 0, CURRENT_TIMESTAMP, 'Ana Savings', TRUE,
       (SELECT id FROM currencies WHERE code='EUR'), u.id
FROM users u
WHERE u.username = 'ana'
  AND NOT EXISTS (
    SELECT 1 FROM wallets w
    WHERE w.owner_id = u.id
      AND w.savings = TRUE
      AND w.currency_id = (SELECT id FROM currencies WHERE code='EUR')
  );

-- Ivan -> USD
INSERT INTO wallets (archived, balance, created_at, name, savings, currency_id, owner_id)
SELECT FALSE, 0, CURRENT_TIMESTAMP, 'Ivan Savings', TRUE,
       (SELECT id FROM currencies WHERE code='USD'), u.id
FROM users u
WHERE u.username = 'ivan'
  AND NOT EXISTS (
    SELECT 1 FROM wallets w
    WHERE w.owner_id = u.id
      AND w.savings = TRUE
      AND w.currency_id = (SELECT id FROM currencies WHERE code='USD')
  );

---------------------------------------
-- CATEGORIES (predefined = global)
---------------------------------------
INSERT INTO categories (archived, color, created_at, name, predefined, type, owner_id) VALUES
 (FALSE, '#2ECC71', CURRENT_TIMESTAMP, 'Salary',        TRUE, 'INCOME',  NULL),
 (FALSE, '#27AE60', CURRENT_TIMESTAMP, 'Bonus',         TRUE, 'INCOME',  NULL),
 (FALSE, '#E67E22', CURRENT_TIMESTAMP, 'Groceries',     TRUE, 'EXPENSE', NULL),
 (FALSE, '#E74C3C', CURRENT_TIMESTAMP, 'Rent',          TRUE, 'EXPENSE', NULL),
 (FALSE, '#8E44AD', CURRENT_TIMESTAMP, 'Entertainment', TRUE, 'EXPENSE', NULL),
 (FALSE, '#3498DB', CURRENT_TIMESTAMP, 'Transportation',TRUE, 'EXPENSE', NULL);

-- user kopije da ima šta da se edituje/obriše
INSERT INTO categories (archived, color, created_at, name, predefined, type, owner_id) VALUES
 (FALSE,'#2E7D32', CURRENT_TIMESTAMP, 'Salary',       FALSE, 'INCOME',  (SELECT id FROM users WHERE username='nikola')),
 (FALSE,'#F57C00', CURRENT_TIMESTAMP, 'Groceries',    FALSE, 'EXPENSE', (SELECT id FROM users WHERE username='nikola')),
 (FALSE,'#2E7D32', CURRENT_TIMESTAMP, 'Salary',       FALSE, 'INCOME',  (SELECT id FROM users WHERE username='ivan')),
 (FALSE,'#E74C3C', CURRENT_TIMESTAMP, 'Rent',         FALSE, 'EXPENSE', (SELECT id FROM users WHERE username='ivan')),
 (FALSE,'#27AE60', CURRENT_TIMESTAMP, 'Bonus',        FALSE, 'INCOME',  (SELECT id FROM users WHERE username='ana')),
 (FALSE,'#8E44AD', CURRENT_TIMESTAMP, 'Entertainment',FALSE, 'EXPENSE', (SELECT id FROM users WHERE username='ana'));

---------------------------------------
-- TRANSACTIONS
---------------------------------------
-- Nikola (4)
INSERT INTO transactions (amount, description, occurred_at, transfer_id, category_id, recurring_instance_id, recurring_template_id, wallet_id) VALUES
 (95000.00, 'Monthly salary', DATEADD('DAY', -20, CURRENT_TIMESTAMP), NULL,
   (SELECT id FROM categories WHERE owner_id=(SELECT id FROM users WHERE username='nikola') AND name='Salary' AND type='INCOME'),
   NULL, NULL,
   (SELECT id FROM wallets WHERE name='Nikola Main' AND owner_id=(SELECT id FROM users WHERE username='nikola'))),
 (6200.00, 'Groceries bill', DATEADD('DAY', -18, CURRENT_TIMESTAMP), NULL,
   (SELECT id FROM categories WHERE owner_id=(SELECT id FROM users WHERE username='nikola') AND name='Groceries' AND type='EXPENSE'),
   NULL, NULL,
   (SELECT id FROM wallets WHERE name='Nikola Main' AND owner_id=(SELECT id FROM users WHERE username='nikola'))),
 (2200.00, 'Cinema', DATEADD('DAY', -10, CURRENT_TIMESTAMP), NULL,
   (SELECT id FROM categories WHERE owner_id IS NULL AND name='Entertainment'),
   NULL, NULL,
   (SELECT id FROM wallets WHERE name='Nikola Main' AND owner_id=(SELECT id FROM users WHERE username='nikola'))),
 (120.00, 'Taxi', DATEADD('DAY', -5, CURRENT_TIMESTAMP), NULL,
   (SELECT id FROM categories WHERE owner_id IS NULL AND name='Transportation'),
   NULL, NULL,
   (SELECT id FROM wallets WHERE name='Nikola Euro' AND owner_id=(SELECT id FROM users WHERE username='nikola')));

-- Ivan (4)
INSERT INTO transactions (amount, description, occurred_at, transfer_id, category_id, recurring_instance_id, recurring_template_id, wallet_id) VALUES
 (82000.00, 'Monthly salary', DATEADD('DAY', -22, CURRENT_TIMESTAMP), NULL,
   (SELECT id FROM categories WHERE owner_id=(SELECT id FROM users WHERE username='ivan') AND name='Salary' AND type='INCOME'),
   NULL, NULL,
   (SELECT id FROM wallets WHERE name='Ivan Main' AND owner_id=(SELECT id FROM users WHERE username='ivan'))),
 (25000.00, 'Rent', DATEADD('DAY', -21, CURRENT_TIMESTAMP), NULL,
   (SELECT id FROM categories WHERE owner_id=(SELECT id FROM users WHERE username='ivan') AND name='Rent' AND type='EXPENSE'),
   NULL, NULL,
   (SELECT id FROM wallets WHERE name='Ivan Main' AND owner_id=(SELECT id FROM users WHERE username='ivan'))),
 (5400.00, 'Market', DATEADD('DAY', -12, CURRENT_TIMESTAMP), NULL,
   (SELECT id FROM categories WHERE owner_id IS NULL AND name='Groceries'),
   NULL, NULL,
   (SELECT id FROM wallets WHERE name='Ivan Main' AND owner_id=(SELECT id FROM users WHERE username='ivan'))),
 (65.00, 'Bus pass', DATEADD('DAY', -2, CURRENT_TIMESTAMP), NULL,
   (SELECT id FROM categories WHERE owner_id IS NULL AND name='Transportation'),
   NULL, NULL,
   (SELECT id FROM wallets WHERE name='Ivan USD' AND owner_id=(SELECT id FROM users WHERE username='ivan')));

-- Ana (4)
INSERT INTO transactions (amount, description, occurred_at, transfer_id, category_id, recurring_instance_id, recurring_template_id, wallet_id) VALUES
 (15000.00, 'Quarterly bonus', DATEADD('DAY', -25, CURRENT_TIMESTAMP), NULL,
   (SELECT id FROM categories WHERE owner_id=(SELECT id FROM users WHERE username='ana') AND name='Bonus' AND type='INCOME'),
   NULL, NULL,
   (SELECT id FROM wallets WHERE name='Ana Main' AND owner_id=(SELECT id FROM users WHERE username='ana'))),
 (4100.00, 'Groceries', DATEADD('DAY', -15, CURRENT_TIMESTAMP), NULL,
   (SELECT id FROM categories WHERE owner_id IS NULL AND name='Groceries'),
   NULL, NULL,
   (SELECT id FROM wallets WHERE name='Ana Main' AND owner_id=(SELECT id FROM users WHERE username='ana'))),
 (1800.00, 'Concert', DATEADD('DAY', -8, CURRENT_TIMESTAMP), NULL,
   (SELECT id FROM categories WHERE owner_id IS NULL AND name='Entertainment'),
   NULL, NULL,
   (SELECT id FROM wallets WHERE name='Ana Main' AND owner_id=(SELECT id FROM users WHERE username='ana'))),
 (30.00, 'Tube', DATEADD('DAY', -3, CURRENT_TIMESTAMP), NULL,
   (SELECT id FROM categories WHERE owner_id IS NULL AND name='Transportation'),
   NULL, NULL,
   (SELECT id FROM wallets WHERE name='Ana GBP' AND owner_id=(SELECT id FROM users WHERE username='ana')));

---------------------------------------
-- RECURRING_TEMPLATES
---------------------------------------
INSERT INTO recurring_templates (active, amount, created_at, description_template, end_date, frequency, repeat_every, name, next_run_date, start_date, updated_at, category_id, owner_id, wallet_id)
VALUES
 (TRUE, 95000.00, CURRENT_TIMESTAMP, 'Salary monthly', NULL, 'MONTHLY', 1, 'Nikola Salary', DATEADD('DAY', 10, CURRENT_DATE), DATEADD('MONTH', -2, CURRENT_DATE), CURRENT_TIMESTAMP,
   (SELECT id FROM categories WHERE owner_id=(SELECT id FROM users WHERE username='nikola') AND name='Salary' AND type='INCOME'),
   (SELECT id FROM users WHERE username='nikola'),
   (SELECT id FROM wallets WHERE name='Nikola Main' AND owner_id=(SELECT id FROM users WHERE username='nikola'))),
 (TRUE, 25000.00, CURRENT_TIMESTAMP, 'Rent monthly',   NULL, 'MONTHLY', 1, 'Ivan Rent',     DATEADD('DAY', 5, CURRENT_DATE),  DATEADD('MONTH', -2, CURRENT_DATE), CURRENT_TIMESTAMP,
   (SELECT id FROM categories WHERE owner_id=(SELECT id FROM users WHERE username='ivan') AND name='Rent' AND type='EXPENSE'),
   (SELECT id FROM users WHERE username='ivan'),
   (SELECT id FROM wallets WHERE name='Ivan Main' AND owner_id=(SELECT id FROM users WHERE username='ivan'))),
 (TRUE, 6000.00,  CURRENT_TIMESTAMP, 'Groceries weekly',NULL, 'WEEKLY',  1, 'Ana Groceries', DATEADD('DAY', 7, CURRENT_DATE),  DATEADD('MONTH', -1, CURRENT_DATE), CURRENT_TIMESTAMP,
   (SELECT id FROM categories WHERE owner_id IS NULL AND name='Groceries'),
   (SELECT id FROM users WHERE username='ana'),
   (SELECT id FROM wallets WHERE name='Ana Main' AND owner_id=(SELECT id FROM users WHERE username='ana')));

---------------------------------------
-- RECURRING_INSTANCES
---------------------------------------
INSERT INTO recurring_instances (executed_at, period_key, template_id)
VALUES
 (DATEADD('DAY', -20, CURRENT_TIMESTAMP), '2025-09', (SELECT id FROM recurring_templates WHERE name='Nikola Salary')),
 (DATEADD('DAY', -25, CURRENT_TIMESTAMP), '2025-09', (SELECT id FROM recurring_templates WHERE name='Ivan Rent')),
 (DATEADD('DAY',  -7, CURRENT_TIMESTAMP), '2025-W41',(SELECT id FROM recurring_templates WHERE name='Ana Groceries'));

---------------------------------------
-- SAVING_GOALS
---------------------------------------
INSERT INTO saving_goals (archived, created_at, current_amount, due_date, name, target_amount, updated_at, owner_id, wallet_id)
VALUES
 (FALSE, CURRENT_TIMESTAMP, 30000.00, DATEADD('MONTH', 6, CURRENT_DATE), 'New Laptop', 120000.00, CURRENT_TIMESTAMP,
   (SELECT id FROM users WHERE username='nikola'),
   (SELECT id FROM wallets WHERE name='Nikola Main' AND owner_id=(SELECT id FROM users WHERE username='nikola'))),
 (FALSE, CURRENT_TIMESTAMP, 15000.00, DATEADD('MONTH', 4, CURRENT_DATE), 'Vacation',    90000.00,  CURRENT_TIMESTAMP,
   (SELECT id FROM users WHERE username='nikola'),
   (SELECT id FROM wallets WHERE name='Nikola Euro' AND owner_id=(SELECT id FROM users WHERE username='nikola'))),
 (FALSE, CURRENT_TIMESTAMP, 20000.00, NULL,                                 'Emergency',   70000.00,  CURRENT_TIMESTAMP,
   (SELECT id FROM users WHERE username='nikola'),
   (SELECT id FROM wallets WHERE name='Nikola Main' AND owner_id=(SELECT id FROM users WHERE username='nikola'))),

 (FALSE, CURRENT_TIMESTAMP, 10000.00, DATEADD('MONTH', 2, CURRENT_DATE), 'Car Service', 40000.00, CURRENT_TIMESTAMP,
   (SELECT id FROM users WHERE username='ivan'),
   (SELECT id FROM wallets WHERE name='Ivan Main' AND owner_id=(SELECT id FROM users WHERE username='ivan'))),
 (FALSE, CURRENT_TIMESTAMP, 12000.00, DATEADD('MONTH', 5, CURRENT_DATE), 'Bike',        60000.00, CURRENT_TIMESTAMP,
   (SELECT id FROM users WHERE username='ivan'),
   (SELECT id FROM wallets WHERE name='Ivan USD' AND owner_id=(SELECT id FROM users WHERE username='ivan'))),
 (FALSE, CURRENT_TIMESTAMP,  8000.00, NULL,                               'Emergency',   50000.00, CURRENT_TIMESTAMP,
   (SELECT id FROM users WHERE username='ivan'),
   (SELECT id FROM wallets WHERE name='Ivan Main' AND owner_id=(SELECT id FROM users WHERE username='ivan'))),

 (FALSE, CURRENT_TIMESTAMP, 25000.00, DATEADD('MONTH', 7, CURRENT_DATE), 'Camera',      80000.00, CURRENT_TIMESTAMP,
   (SELECT id FROM users WHERE username='ana'),
   (SELECT id FROM wallets WHERE name='Ana Main' AND owner_id=(SELECT id FROM users WHERE username='ana'))),
 (FALSE, CURRENT_TIMESTAMP,  5000.00, DATEADD('MONTH', 3, CURRENT_DATE), 'Course',      30000.00, CURRENT_TIMESTAMP,
   (SELECT id FROM users WHERE username='ana'),
   (SELECT id FROM wallets WHERE name='Ana GBP' AND owner_id=(SELECT id FROM users WHERE username='ana'))),
 (FALSE, CURRENT_TIMESTAMP, 10000.00, NULL,                               'Emergency',   60000.00, CURRENT_TIMESTAMP,
   (SELECT id FROM users WHERE username='ana'),
   (SELECT id FROM wallets WHERE name='Ana Main' AND owner_id=(SELECT id FROM users WHERE username='ana')));

---------------------------------------
-- ADMIN_NOTES
---------------------------------------
INSERT INTO admin_notes (created_at, note, admin_id, user_id)
VALUES
 (CURRENT_TIMESTAMP, 'Initial review ok.',        (SELECT id FROM users WHERE username='admin'), (SELECT id FROM users WHERE username='nikola')),
 (CURRENT_TIMESTAMP, 'Reminder to verify phone.', (SELECT id FROM users WHERE username='admin'), (SELECT id FROM users WHERE username='ivan')),
 (CURRENT_TIMESTAMP, 'All good.',                  (SELECT id FROM users WHERE username='admin'), (SELECT id FROM users WHERE username='ana'));