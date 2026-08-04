-- ---------------------------------------------------------------------------------------
-- WealthTrack - make the seeded users able to log in.
--
-- WHY THIS EXISTS
-- The original seed (Backend/MAIN/src/main/resources/db/wealthtrack-schema-and-seed.sql)
-- stores placeholder strings such as '$2a$10$demo.manager.hash' in password_hash. Those are
-- not valid BCrypt hashes, so once real authentication is switched on none of the six
-- seeded users can authenticate. This script replaces them with genuine BCrypt hashes.
--
-- There is no Flyway/Liquibase in this project yet, so run it by hand after the schema seed:
--     mysql -u root -p mydb < Backend/UserMicroService/usermicroservice/src/main/resources/db/V2__set_login_passwords.sql
--
-- DEVELOPMENT CREDENTIALS - change these before this system is exposed to anyone.
--     priya.shah@wealthtrack.test      / Manager@123    (MANAGER,  user_id 1)
--     aarav.mehta@wealthtrack.test     / Investor@123   (INVESTOR, user_id 2)
--     diya.iyer@wealthtrack.test       / Investor@123   (INVESTOR, user_id 3)
--     kabir.singh@wealthtrack.test     / Investor@123   (INVESTOR, user_id 4)
--     meera.nair@wealthtrack.test      / Investor@123   (INVESTOR, user_id 5)
--     rohan.verma@wealthtrack.test     / Investor@123   (INVESTOR, user_id 6)
--
-- Each hash below was produced with BCryptPasswordEncoder (strength 10) and verified to
-- match its plaintext. They differ per user because BCrypt salts each hash independently.
-- ---------------------------------------------------------------------------------------

UPDATE user SET password_hash = '$2a$10$iIyEtfvaCCXoHIgKPY0mQuBgNNR2QQBsv6M7u6ameCcNSg1Gjvydi'
WHERE email = 'priya.shah@wealthtrack.test';

UPDATE user SET password_hash = '$2a$10$3ALDu2CMnj9z7Jaa48eLF.xBD0WjbtYPgdA0XOefdPUxQ4Tk3uVge'
WHERE email = 'aarav.mehta@wealthtrack.test';

UPDATE user SET password_hash = '$2a$10$un3Duu4CWvjR9U6kY/WMx.REIJOnUfwW9MUV1QXbtPuTB2q9ngcM6'
WHERE email = 'diya.iyer@wealthtrack.test';

UPDATE user SET password_hash = '$2a$10$PUtEsPgKmqkUhNWgevuyGOd.gwB7PFkmuoAHv5pcb3R.lgx8znRde'
WHERE email = 'kabir.singh@wealthtrack.test';

UPDATE user SET password_hash = '$2a$10$boceaqAzp9IgtO3ffD2c9O1/L.EPgz9K3Luy6BDBUnV.QtLjEnsAS'
WHERE email = 'meera.nair@wealthtrack.test';

UPDATE user SET password_hash = '$2a$10$KMuon1SkmFm5HOm359bf/eGDKwr5QdNJZo1m7b1f8JbIhQqW51uHi'
WHERE email = 'rohan.verma@wealthtrack.test';

-- Any user whose password_hash is still a placeholder cannot log in. Surfacing them here is
-- better than letting the login endpoint fail with a generic "invalid credentials".
SELECT user_id, email, 'password_hash is not a BCrypt hash - this user cannot log in' AS warning
FROM user
WHERE password_hash NOT LIKE '$2a$%'
   OR CHAR_LENGTH(password_hash) <> 60;
