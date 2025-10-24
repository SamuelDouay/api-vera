CREATE DATABASE IF NOT EXISTS vera;
USE vera;

CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    surname VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    is_admin BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS surveys (
    id SERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    is_quiz BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    allow_editing BOOLEAN DEFAULT TRUE,
    share_token VARCHAR(255) UNIQUE,
    created_by INTEGER NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (created_by) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS questions (
    id SERIAL PRIMARY KEY,
    survey_id INTEGER NOT NULL,
    text TEXT NOT NULL,
    type VARCHAR(50) DEFAULT 'text',
    is_required BOOLEAN DEFAULT TRUE,
    display_order INTEGER DEFAULT 0,
    config JSON, -- options, correct_answers, points, media, etc.
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (survey_id) REFERENCES surveys(id) ON DELETE CASCADE
);

CREATE TABLE survey_responses (
    id SERIAL PRIMARY KEY,
    survey_id INTEGER NOT NULL,
    respondent_id VARCHAR(255), -- session_id ou user_id
    answers JSON NOT NULL, -- Toutes les réponses structurées
    score INTEGER DEFAULT 0, -- Pour les quiz
    submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (survey_id) REFERENCES surveys(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS survey_history (
    id SERIAL PRIMARY KEY,
    survey_id INTEGER NOT NULL,
    action VARCHAR(50) NOT NULL, -- create/update/delete
    snapshot JSON NOT NULL, -- État avant modification
    user_id INTEGER NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (survey_id) REFERENCES surveys(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Index essentiels seulement
CREATE INDEX IF NOT EXISTS idx_surveys_active ON surveys(is_active);
CREATE INDEX IF NOT EXISTS idx_questions_survey ON questions(survey_id);
CREATE INDEX IF NOT EXISTS idx_responses_survey ON survey_responses(survey_id);
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);

-- Administrateurs
INSERT INTO users (name, surname, email, password, is_admin)
VALUES  ('Admin', 'Akadmin', 'admin@example.com', '$argon2id$v=19$m=65536,t=10,p=1$Q2+UgerwZFJyMGC/P6O6fQ$Wq+arapkagYKmHZOeAmEvnCO3h7F8zKQaRU1gilEbOc', true),
        ('Samy', 'Douay', 'samy@example.com', '$argon2id$v=19$m=65536,t=10,p=1$zcZdug8Ah7l1D+Ng1GOWhQ$EfOZcHOK3bgHNoJYcR01MbvgMWDKnSmcn5cEJS4o5yE', true);


-- Utilisateurs normaux
INSERT INTO users (name, surname, email, password)
VALUES  ('John', 'Doe', 'jhon@entreprise.com', '$argon2id$v=19$m=65536,t=10,p=1$Q2+UgerwZFJyMGC/P6O6fQ$Wq+arapkagYKmHZOeAmEvnCO3h7F8zKQaRU1gilEbOc'),
        ('Jane', 'Smith', 'jane.smith@startup.fr', '$argon2id$v=19$m=65536,t=10,p=1$Q2+UgerwZFJyMGC/P6O6fQ$Wq+arapkagYKmHZOeAmEvnCO3h7F8zKQaRU1gilEbOc'),
        ('Jane', 'Smith', 'jane.smith@data.com', '$argon2id$v=19$m=65536,t=10,p=1$Q2+UgerwZFJyMGC/P6O6fQ$Wq+arapkagYKmHZOeAmEvnCO3h7F8zKQaRU1gilEbOc');

-- Sondage normal (feedback produit)
INSERT INTO surveys (title, description, is_quiz, is_active, allow_editing, share_token, created_by) VALUES
('Feedback Produit Alpha', 'Donnez votre avis sur notre nouveau produit', FALSE, TRUE, TRUE, 'share_abc123', 1);

-- Quiz technique
INSERT INTO surveys (title, description, is_quiz, is_active, allow_editing, share_token, created_by) VALUES
('Quiz Connaissance Python', 'Testez vos connaissances en Python', TRUE, TRUE, TRUE, 'quiz_python456', 2);

-- Sondage interne entreprise
INSERT INTO surveys (title, description, is_quiz, is_active, allow_editing, share_token, created_by) VALUES
('Satisfaction Employés 2024', 'Votre avis sur l''environnement de travail', FALSE, TRUE, FALSE, 'internal_789', 1);

-- Questions pour le sondage produit (ID 1)
INSERT INTO questions (survey_id, text, type, is_required, display_order, config) VALUES
(1, 'Que pensez-vous de notre nouveau produit ?', 'text', TRUE, 1,
 '{"anonymization": "high", "max_length": 500}'),

(1, 'Quelles fonctionnalités appréciez-vous le plus ?', 'multiple_choice', TRUE, 2,
 '{"options": ["Interface utilisateur", "Performance", "Fiabilité", "Prix"], "anonymization": "medium"}'),

(1, 'Recommanderiez-vous ce produit à vos collègues ?', 'boolean', TRUE, 3,
 '{"anonymization": "low"}');

 -- Questions pour le quiz Python (ID 2)
INSERT INTO questions (survey_id, text, type, is_required, display_order, config) VALUES
(2, 'Quelle est la sortie de : print(3 * "7") ?', 'multiple_choice', TRUE, 1,
 '{"options": ["21", "777", "37", "Erreur"], "correct_answers": [1], "points": 5, "explanation": "En Python, multiplier une chaîne répète la chaîne."}'),

(2, 'Les listes en Python sont-elles mutables ?', 'boolean', TRUE, 2,
 '{"correct_answer": true, "points": 3, "explanation": "Les listes peuvent être modifiées après leur création."}'),

(2, 'Expliquez la différence entre "==" et "is" en Python', 'text', TRUE, 3,
 '{"points": 10, "evaluation_criteria": ["comparaison valeur", "comparaison identité", "exemples"], "max_length": 300}');

 INSERT INTO survey_responses (survey_id, respondent_id, answers, score) VALUES
(1, 'user_session_12345',
 '{
   "q1": {"answer": "Je travaille chez Google Paris et ce produit est excellent pour nos équipes.", "anonymized": "Je travaille dans une grande entreprise tech et ce produit est excellent pour nos équipes."},
   "q2": {"selected": [0, 2]},
   "q3": {"answer": true}
 }', 0);

 INSERT INTO survey_responses (survey_id, respondent_id, answers, score) VALUES
(2, 'student_67890',
 '{
   "q1": {"selected": [1], "correct": true, "points_earned": 5},
   "q2": {"answer": true, "correct": true, "points_earned": 3},
   "q3": {"answer": "== compare les valeurs, is compare les identités (adresses mémoire)", "points_earned": 8}
 }', 16);

-- Une réponse avec erreurs
INSERT INTO survey_responses (survey_id, respondent_id, answers, score) VALUES
(2, 'beginner_54321',
 '{
   "q1": {"selected": [0], "correct": false, "points_earned": 0},
   "q2": {"answer": false, "correct": false, "points_earned": 0},
   "q3": {"answer": "c''est la même chose", "points_earned": 2}
 }', 2);

 -- Historique de création
INSERT INTO survey_history (survey_id, action, snapshot, user_id) VALUES
(1, 'create',
 '{
   "title": "Feedback Produit Alpha",
   "description": "Donnez votre avis sur notre nouveau produit",
   "is_quiz": false,
   "questions": []
 }', 1);

-- Historique de modification (ajout question)
INSERT INTO survey_history (survey_id, action, snapshot, user_id) VALUES
(1, 'update',
 '{
   "title": "Feedback Produit Alpha",
   "description": "Donnez votre avis sur notre nouveau produit",
   "is_quiz": false,
   "questions": [
     {
       "id": 1,
       "text": "Que pensez-vous de notre nouveau produit ?",
       "type": "text",
       "config": {"anonymization": "high"}
     }
   ]
 }', 2);

-- Historique de publication
INSERT INTO survey_history (survey_id, action, snapshot, user_id) VALUES
(1, 'publish',
 '{
   "title": "Feedback Produit Alpha",
   "is_active": false,
   "previous_state": "draft"
 }', 1);