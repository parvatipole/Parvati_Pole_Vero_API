-- Seed data for local development. Loaded by spring.sql.init on application startup.

INSERT INTO accounts (name, owner_name) VALUES ('Personal Checking', 'Alex Morgan');
INSERT INTO accounts (name, owner_name) VALUES ('Business Account', 'Alex Morgan');

-- December 2024 — the month the monthly spend demo will be using.
INSERT INTO transactions (account_id, amount, description, category, transaction_date) VALUES (1, 87.45, 'Whole Foods Market', 'FOOD', '2024-12-01');
INSERT INTO transactions (account_id, amount, description, category, transaction_date) VALUES (1, 42.00, 'Uber ride to airport', 'TRANSPORT', '2024-12-03');
INSERT INTO transactions (account_id, amount, description, category, transaction_date) VALUES (1, 124.50, 'Electric bill', 'UTILITIES', '2024-12-05');
INSERT INTO transactions (account_id, amount, description, category, transaction_date) VALUES (1, 15.99, 'Netflix subscription', 'ENTERTAINMENT', '2024-12-08');
INSERT INTO transactions (account_id, amount, description, category, transaction_date) VALUES (1, 119.00, 'Running shoes', 'SHOPPING', '2024-12-10');
INSERT INTO transactions (account_id, amount, description, category, transaction_date) VALUES (1, 56.78, 'Trader Joes groceries', 'FOOD', '2024-12-14');
INSERT INTO transactions (account_id, amount, description, category, transaction_date) VALUES (1, 34.20, 'CVS pharmacy refill', 'HEALTHCARE', '2024-12-17');
INSERT INTO transactions (account_id, amount, description, category, transaction_date) VALUES (2, 48.00, 'Shell gas station', 'TRANSPORT', '2024-12-20');
INSERT INTO transactions (account_id, amount, description, category, transaction_date) VALUES (1, 28.50, 'Movie tickets', 'ENTERTAINMENT', '2024-12-22');
INSERT INTO transactions (account_id, amount, description, category, transaction_date) VALUES (1, 92.30, 'Restaurant dinner', 'FOOD', '2024-12-28');
INSERT INTO transactions (account_id, amount, description, category, transaction_date) VALUES (2, 67.40, 'Office supplies order', 'SHOPPING', '2024-12-30');

-- November 2024
INSERT INTO transactions (account_id, amount, description, category, transaction_date) VALUES (1, 73.00, 'Whole Foods Market', 'FOOD', '2024-11-05');
INSERT INTO transactions (account_id, amount, description, category, transaction_date) VALUES (2, 89.99, 'Internet bill', 'UTILITIES', '2024-11-12');
INSERT INTO transactions (account_id, amount, description, category, transaction_date) VALUES (1, 22.50, 'Uber across town', 'TRANSPORT', '2024-11-18');
INSERT INTO transactions (account_id, amount, description, category, transaction_date) VALUES (1, 145.00, 'Winter coat', 'SHOPPING', '2024-11-25');
INSERT INTO transactions (account_id, amount, description, category, transaction_date) VALUES (1, 9.99, 'Spotify Premium', 'ENTERTAINMENT', '2024-11-29');

-- January 2025
INSERT INTO transactions (account_id, amount, description, category, transaction_date) VALUES (1, 68.45, 'Grocery shopping', 'FOOD', '2025-01-03');
INSERT INTO transactions (account_id, amount, description, category, transaction_date) VALUES (1, 45.00, 'Water bill', 'UTILITIES', '2025-01-07');
INSERT INTO transactions (account_id, amount, description, category, transaction_date) VALUES (1, 175.00, 'Doctor copay', 'HEALTHCARE', '2025-01-14');
INSERT INTO transactions (account_id, amount, description, category, transaction_date) VALUES (2, 95.00, 'Monthly train pass', 'TRANSPORT', '2025-01-20');
INSERT INTO transactions (account_id, amount, description, category, transaction_date) VALUES (1, 3.50, 'ATM withdrawal fee', 'OTHER', '2025-01-25');
