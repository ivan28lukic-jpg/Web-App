-- =========================
-- USERS
-- =========================
INSERT INTO users (
  username, email, first_name, last_name, password_hash, role, blocked,
  registered_at, preferred_currency_code, birth_date
) VALUES
('nikola','nikola@example.com','Nikola','Vučković','{noop}pass','USER',FALSE,
 CURRENT_TIMESTAMP,'RSD', DATE '2002-05-21'),
('ivan','ivan@example.com','Ivan','Lukić','{noop}pass','USER',FALSE,
 CURRENT_TIMESTAMP,'RSD', DATE '2001-11-03'),
('admin','admin@example.com','Admin','Root','{noop}admin','ADMIN',FALSE,
 CURRENT_TIMESTAMP,'EUR', DATE '1990-01-01');

-- =========================
-- CURRENCIES
-- value_vs_eur = koliko JEDINICA te valute je 1 EUR
-- =========================
INSERT INTO currencies (code, name, value_vs_eur, updated_at) VALUES
('EUR','Euro',1.000000,CURRENT_TIMESTAMP),
('RSD','Serbian Dinar',117.300000,CURRENT_TIMESTAMP),
('USD','US Dollar',1.080000,CURRENT_TIMESTAMP),
('GBP','British Pound',0.860000,CURRENT_TIMESTAMP);

-- =========================
-- WALLETS  (obavezno polje savings!)
-- =========================
INSERT INTO wallets (name, balance, owner_id, currency_id, archived, created_at, savings) VALUES
('Nikola Main',     1200.00,
  (SELECT id FROM users WHERE username='nikola'),
  (SELECT id FROM currencies WHERE code='RSD'),
  FALSE, CURRENT_TIMESTAMP, FALSE),

('Nikola Savings',   450.00,
  (SELECT id FROM users WHERE username='nikola'),
  (SELECT id FROM currencies WHERE code='EUR'),
  FALSE, CURRENT_TIMESTAMP, TRUE),

('Nikola USD',       150.00,
  (SELECT id FROM users WHERE username='nikola'),
  (SELECT id FROM currencies WHERE code='USD'),
  FALSE, CURRENT_TIMESTAMP, FALSE),

('Ivan Main',        800.00,
  (SELECT id FROM users WHERE username='ivan'),
  (SELECT id FROM currencies WHERE code='RSD'),
  FALSE, CURRENT_TIMESTAMP, FALSE),

('Ivan Savings',     300.00,
  (SELECT id FROM users WHERE username='ivan'),
  (SELECT id FROM currencies WHERE code='EUR'),
  FALSE, CURRENT_TIMESTAMP, TRUE);

-- =========================
-- CATEGORIES (korisničke)
-- =========================
INSERT INTO categories (owner_id, name, type, archived, created_at, color) VALUES
((SELECT id FROM users WHERE username='nikola'), 'Salary',       'INCOME',  FALSE, CURRENT_TIMESTAMP, '#2E7D32'),
((SELECT id FROM users WHERE username='nikola'), 'Groceries',    'EXPENSE', FALSE, CURRENT_TIMESTAMP, '#F57C00'),
((SELECT id FROM users WHERE username='nikola'), 'Rent',         'EXPENSE', FALSE, CURRENT_TIMESTAMP, '#E74C3C'),
((SELECT id FROM users WHERE username='nikola'), 'Bonus',        'INCOME',  FALSE, CURRENT_TIMESTAMP, '#27AE60'),
((SELECT id FROM users WHERE username='nikola'), 'Transfer In',  'INCOME',  FALSE, CURRENT_TIMESTAMP, '#1976D2'),
((SELECT id FROM users WHERE username='nikola'), 'Transfer Out', 'EXPENSE', FALSE, CURRENT_TIMESTAMP, '#D32F2F'),

((SELECT id FROM users WHERE username='ivan'),   'Salary',       'INCOME',  FALSE, CURRENT_TIMESTAMP, '#2E7D32'),
((SELECT id FROM users WHERE username='ivan'),   'Groceries',    'EXPENSE', FALSE, CURRENT_TIMESTAMP, '#F57C00'),
((SELECT id FROM users WHERE username='ivan'),   'Rent',         'EXPENSE', FALSE, CURRENT_TIMESTAMP, '#E74C3C'),
((SELECT id FROM users WHERE username='ivan'),   'Bonus',        'INCOME',  FALSE, CURRENT_TIMESTAMP, '#27AE60'),
((SELECT id FROM users WHERE username='ivan'),   'Transfer In',  'INCOME',  FALSE, CURRENT_TIMESTAMP, '#1976D2'),
((SELECT id FROM users WHERE username='ivan'),   'Transfer Out', 'EXPENSE', FALSE, CURRENT_TIMESTAMP, '#D32F2F');

-- =========================
-- GLOBAL CATEGORIES (owner_id = NULL)
-- =========================
INSERT INTO categories (name, type, color, archived, created_at, owner_id) VALUES
('Salary',     'INCOME',  '#2ECC71', FALSE, CURRENT_TIMESTAMP, NULL),
('Bonus',      'INCOME',  '#27AE60', FALSE, CURRENT_TIMESTAMP, NULL),
('Groceries',  'EXPENSE', '#E67E22', FALSE, CURRENT_TIMESTAMP, NULL),
('Rent',       'EXPENSE', '#E74C3C', FALSE, CURRENT_TIMESTAMP, NULL);

-- =========================
-- TRANSACTIONS (non-transfer)
-- =========================
INSERT INTO transactions (amount, description, occurred_at, transfer_id, category_id, wallet_id) VALUES
-- Nikola: plata i kupovina
(150000.00,'September salary', DATEADD('DAY', -10, CURRENT_TIMESTAMP), NULL,
 (SELECT id FROM categories WHERE owner_id=(SELECT id FROM users WHERE username='nikola') AND name='Salary'    AND type='INCOME'),
 (SELECT id FROM wallets    WHERE name='Nikola Main' AND owner_id=(SELECT id FROM users WHERE username='nikola'))),
(120.50,'Groceries - Maxi', DATEADD('DAY',  -7, CURRENT_TIMESTAMP), NULL,
 (SELECT id FROM categories WHERE owner_id=(SELECT id FROM users WHERE username='nikola') AND name='Groceries' AND type='EXPENSE'),
 (SELECT id FROM wallets    WHERE name='Nikola Main' AND owner_id=(SELECT id FROM users WHERE username='nikola'))),

-- Ivan: plata i kupovina
(120000.00,'September salary', DATEADD('DAY', -9, CURRENT_TIMESTAMP), NULL,
 (SELECT id FROM categories WHERE owner_id=(SELECT id FROM users WHERE username='ivan') AND name='Salary'    AND type='INCOME'),
 (SELECT id FROM wallets    WHERE name='Ivan Main'   AND owner_id=(SELECT id FROM users WHERE username='ivan'))),
(80.00,'Groceries - Lidl',  DATEADD('DAY',  -3, CURRENT_TIMESTAMP), NULL,
 (SELECT id FROM categories WHERE owner_id=(SELECT id FROM users WHERE username='ivan') AND name='Groceries' AND type='EXPENSE'),
 (SELECT id FROM wallets    WHERE name='Ivan Main'   AND owner_id=(SELECT id FROM users WHERE username='ivan')));

-- =========================
-- TRANSACTIONS (transfer pairs) – same-currency primeri
-- =========================
-- Nikola: Main -> Savings
INSERT INTO transactions (amount, description, occurred_at, transfer_id, category_id, wallet_id) VALUES
(20000.00,'Move to savings', DATEADD('DAY', -5, CURRENT_TIMESTAMP), 'tr-nik-1',
 (SELECT id FROM categories WHERE owner_id=(SELECT id FROM users WHERE username='nikola') AND name='Transfer Out' AND type='EXPENSE'),
 (SELECT id FROM wallets    WHERE name='Nikola Main' AND owner_id=(SELECT id FROM users WHERE username='nikola')));
INSERT INTO transactions (amount, description, occurred_at, transfer_id, category_id, wallet_id) VALUES
(20000.00,'Move to savings', DATEADD('DAY', -5, CURRENT_TIMESTAMP), 'tr-nik-1',
 (SELECT id FROM categories WHERE owner_id=(SELECT id FROM users WHERE username='nikola') AND name='Transfer In'  AND type='INCOME'),
 (SELECT id FROM wallets    WHERE name='Nikola Savings' AND owner_id=(SELECT id FROM users WHERE username='nikola')));

-- Ivan: Main -> Savings
INSERT INTO transactions (amount, description, occurred_at, transfer_id, category_id, wallet_id) VALUES
(15000.00,'Monthly savings', DATEADD('DAY', -2, CURRENT_TIMESTAMP), 'tr-iva-1',
 (SELECT id FROM categories WHERE owner_id=(SELECT id FROM users WHERE username='ivan') AND name='Transfer Out' AND type='EXPENSE'),
 (SELECT id FROM wallets    WHERE name='Ivan Main' AND owner_id=(SELECT id FROM users WHERE username='ivan')));
INSERT INTO transactions (amount, description, occurred_at, transfer_id, category_id, wallet_id) VALUES
(15000.00,'Monthly savings', DATEADD('DAY', -2, CURRENT_TIMESTAMP), 'tr-iva-1',
 (SELECT id FROM categories WHERE owner_id=(SELECT id FROM users WHERE username='ivan') AND name='Transfer In'  AND type='INCOME'),
 (SELECT id FROM wallets    WHERE name='Ivan Savings' AND owner_id=(SELECT id FROM users WHERE username='ivan')));

-- =========================
-- TRANSACTIONS (FX transfer pairs) – različite valute
-- =========================
-- Nikola: RSD -> EUR, 11,730.00 RSD => 100.00 EUR (pri 1 EUR = 117.30 RSD)
INSERT INTO transactions (amount, description, occurred_at, transfer_id, category_id, wallet_id) VALUES
(11730.00,'FX RSD->EUR (example)', DATEADD('DAY', -1, CURRENT_TIMESTAMP), 'fx-nik-1',
 (SELECT id FROM categories WHERE owner_id=(SELECT id FROM users WHERE username='nikola') AND name='Transfer Out' AND type='EXPENSE'),
 (SELECT id FROM wallets    WHERE name='Nikola Main' AND owner_id=(SELECT id FROM users WHERE username='nikola')));
INSERT INTO transactions (amount, description, occurred_at, transfer_id, category_id, wallet_id) VALUES
(100.00,'FX RSD->EUR (example)', DATEADD('DAY', -1, CURRENT_TIMESTAMP), 'fx-nik-1',
 (SELECT id FROM categories WHERE owner_id=(SELECT id FROM users WHERE username='nikola') AND name='Transfer In'  AND type='INCOME'),
 (SELECT id FROM wallets    WHERE name='Nikola Savings' AND owner_id=(SELECT id FROM users WHERE username='nikola')));

-- Nikola: EUR -> USD, 50.00 EUR => 54.00 USD (pri 1 EUR = 1.08 USD)
INSERT INTO transactions (amount, description, occurred_at, transfer_id, category_id, wallet_id) VALUES
(50.00,'FX EUR->USD (example)', DATEADD('HOUR', -6, CURRENT_TIMESTAMP), 'fx-nik-2',
 (SELECT id FROM categories WHERE owner_id=(SELECT id FROM users WHERE username='nikola') AND name='Transfer Out' AND type='EXPENSE'),
 (SELECT id FROM wallets    WHERE name='Nikola Savings' AND owner_id=(SELECT id FROM users WHERE username='nikola')));
INSERT INTO transactions (amount, description, occurred_at, transfer_id, category_id, wallet_id) VALUES
(54.00,'FX EUR->USD (example)', DATEADD('HOUR', -6, CURRENT_TIMESTAMP), 'fx-nik-2',
 (SELECT id FROM categories WHERE owner_id=(SELECT id FROM users WHERE username='nikola') AND name='Transfer In'  AND type='INCOME'),
 (SELECT id FROM wallets    WHERE name='Nikola USD' AND owner_id=(SELECT id FROM users WHERE username='nikola')));

-- Ivan: RSD -> EUR, 5,865.00 RSD => 50.00 EUR
INSERT INTO transactions (amount, description, occurred_at, transfer_id, category_id, wallet_id) VALUES
(5865.00,'FX RSD->EUR (example)', DATEADD('HOUR', -3, CURRENT_TIMESTAMP), 'fx-iva-1',
 (SELECT id FROM categories WHERE owner_id=(SELECT id FROM users WHERE username='ivan') AND name='Transfer Out' AND type='EXPENSE'),
 (SELECT id FROM wallets    WHERE name='Ivan Main' AND owner_id=(SELECT id FROM users WHERE username='ivan')));
INSERT INTO transactions (amount, description, occurred_at, transfer_id, category_id, wallet_id) VALUES
(50.00,'FX RSD->EUR (example)', DATEADD('HOUR', -3, CURRENT_TIMESTAMP), 'fx-iva-1',
 (SELECT id FROM categories WHERE owner_id=(SELECT id FROM users WHERE username='ivan') AND name='Transfer In'  AND type='INCOME'),
 (SELECT id FROM wallets    WHERE name='Ivan Savings' AND owner_id=(SELECT id FROM users WHERE username='ivan')));

-- =========================
-- SAVING GOALS
-- =========================
INSERT INTO saving_goals (owner_id, wallet_id, name, target_amount, current_amount, due_date, archived, created_at, updated_at) VALUES
((SELECT id FROM users WHERE username='nikola'),
 (SELECT id FROM wallets WHERE name='Nikola Savings' AND owner_id=(SELECT id FROM users WHERE username='nikola')),
 'Emergency Fund', 1000.00, 200.00, DATE '2025-12-31', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
((SELECT id FROM users WHERE username='ivan'),
 (SELECT id FROM wallets WHERE name='Ivan Savings' AND owner_id=(SELECT id FROM users WHERE username='ivan')),
 'New Phone', 600.00, 120.00, DATE '2026-03-01', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- =========================
-- ADMIN NOTES
-- =========================
INSERT INTO admin_notes (user_id, admin_id, note, created_at) VALUES
((SELECT id FROM users WHERE username='nikola'),
 (SELECT id FROM users WHERE username='admin'),
 'Check KYC documents', CURRENT_TIMESTAMP);

-- =========================
-- RECURRING_TEMPLATES (seed)
-- =========================
INSERT INTO recurring_templates
(owner_id, wallet_id, category_id, name, amount, description_template, frequency, repeat_every, start_date, end_date, next_run_date, active, created_at, updated_at) VALUES
(
  (SELECT id FROM users WHERE username='nikola'),
  (SELECT id FROM wallets WHERE name='Nikola Main' AND owner_id=(SELECT id FROM users WHERE username='nikola')),
  (SELECT id FROM categories WHERE owner_id=(SELECT id FROM users WHERE username='nikola') AND name='Salary' AND type='INCOME'),
  'Monthly Salary',
  150000.00,
  'Salary payout',
  'MONTHLY',
  1,
  DATE '2025-01-01',
  NULL,
  DATE '2025-10-01',
  TRUE,
  CURRENT_TIMESTAMP,
  CURRENT_TIMESTAMP
),
(
  (SELECT id FROM users WHERE username='nikola'),
  (SELECT id FROM wallets WHERE name='Nikola Main' AND owner_id=(SELECT id FROM users WHERE username='nikola')),
  (SELECT id FROM categories WHERE owner_id IS NULL AND name='Rent' AND type='EXPENSE'),
  'Monthly Rent',
  45000.00,
  'Apartment rent',
  'MONTHLY',
  1,
  DATE '2025-01-05',
  NULL,
  DATE '2025-10-05',
  TRUE,
  CURRENT_TIMESTAMP,
  CURRENT_TIMESTAMP
),
(
  (SELECT id FROM users WHERE username='nikola'),
  (SELECT id FROM wallets WHERE name='Nikola Main' AND owner_id=(SELECT id FROM users WHERE username='nikola')),
  (SELECT id FROM categories WHERE owner_id=(SELECT id FROM users WHERE username='nikola') AND name='Groceries' AND type='EXPENSE'),
  'Weekly Groceries',
  5000.00,
  'Groceries budget',
  'WEEKLY',
  1,
  DATE '2025-01-06',
  NULL,
  DATE '2025-01-06',
  FALSE,
  CURRENT_TIMESTAMP,
  CURRENT_TIMESTAMP
);

-- =========================
-- RECURRING_INSTANCES (inicijalno prazno)
-- =========================
-- Ostavi prazno; run-due i run-now će ih popunjavati idempotentno.