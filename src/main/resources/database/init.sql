-- Types ENUM
DO $$ BEGIN
    CREATE TYPE anonymization AS ENUM ('none', 'low', 'medium', 'high');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

DO $$ BEGIN
    CREATE TYPE action AS ENUM ('publish', 'update', 'create', 'delete', 'unpublish');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

-- Tables
CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    surname VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password TEXT NOT NULL,
    is_admin BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS survey (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    anonymization anonymization NOT NULL DEFAULT 'none',
    description TEXT,
    link VARCHAR(255),
    id_user INTEGER NOT NULL REFERENCES users(id),
    is_quiz BOOLEAN NOT NULL DEFAULT false,
    is_active BOOLEAN NOT NULL DEFAULT true,
    allow_editing BOOLEAN NOT NULL DEFAULT true,
    is_public BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    share_token VARCHAR(255) UNIQUE
);

CREATE TABLE IF NOT EXISTS question (
    id SERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    id_survey INTEGER NOT NULL REFERENCES survey(id) ON DELETE CASCADE,
    is_mandatory BOOLEAN NOT NULL DEFAULT false,
    correct_answer JSON,
    display_order INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS answer (
    id SERIAL PRIMARY KEY,
    id_question INTEGER NOT NULL REFERENCES question(id) ON DELETE CASCADE,
    is_anonymous BOOLEAN NOT NULL,
    original_answer JSON,
    anonymous_answer JSON,
    respondent_id VARCHAR(255) NOT NULL,
    is_correct BOOLEAN,
    submitted_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS history_question (
    id SERIAL PRIMARY KEY,
    id_question INTEGER NOT NULL REFERENCES question(id) ON DELETE CASCADE,
    id_survey INTEGER NOT NULL REFERENCES survey(id) ON DELETE CASCADE,
    action action NOT NULL,
    snapshot JSON NOT NULL,
    id_user INTEGER NOT NULL REFERENCES users(id),
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS history (
    id SERIAL PRIMARY KEY,
    id_survey INTEGER NOT NULL REFERENCES survey(id) ON DELETE CASCADE,
    action action NOT NULL,
    snapshot JSON NOT NULL,
    id_user INTEGER NOT NULL REFERENCES users(id),
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Table pour stocker les tokens blacklistés
CREATE TABLE IF NOT EXISTS blacklisted_tokens (
    token VARCHAR(512) PRIMARY KEY,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    user_id INTEGER NULL REFERENCES users(id),
    reason VARCHAR(100) DEFAULT 'logout'
);

CREATE INDEX IF NOT EXISTS idx_blacklisted_tokens_expires_at ON blacklisted_tokens(expires_at);
CREATE INDEX IF NOT EXISTS idx_blacklisted_tokens_user_id ON blacklisted_tokens(user_id);

-- Fonction générique pour updated_at
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Fonction pour historiser les modifications de survey
CREATE OR REPLACE FUNCTION log_survey_changes()
RETURNS TRIGGER AS $$
BEGIN
    IF (TG_OP = 'UPDATE') THEN
        INSERT INTO history (id_survey, action, snapshot, id_user)
        VALUES (
            NEW.id,
            'update',
            json_build_object(
                'previous_name', OLD.name,
                'new_name', NEW.name,
                'previous_description', OLD.description,
                'new_description', NEW.description,
                'previous_anonymization', OLD.anonymization,
                'new_anonymization', NEW.anonymization,
                'previous_is_quiz', OLD.is_quiz,
                'new_is_quiz', NEW.is_quiz,
                'previous_is_public', OLD.is_public,
                'new_is_public', NEW.is_public,
                'previous_allow_editing', OLD.allow_editing,
                'new_allow_editing', NEW.allow_editing
            ),
            NEW.id_user
        );
    ELSIF (TG_OP = 'INSERT') THEN
        INSERT INTO history (id_survey, action, snapshot, id_user)
        VALUES (
            NEW.id,
            'create',
            json_build_object(
                'name', NEW.name,
                'description', NEW.description,
                'anonymization', NEW.anonymization,
                'is_quiz', NEW.is_quiz,
                'is_public', NEW.is_public,
                'allow_editing', NEW.allow_editing
            ),
            NEW.id_user
        );
    END IF;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Fonction pour historiser les modifications de questions
CREATE OR REPLACE FUNCTION log_question_changes()
RETURNS TRIGGER AS $$
BEGIN
    IF (TG_OP = 'UPDATE') THEN
        INSERT INTO history_question (id_question, id_survey, action, snapshot, id_user)
        VALUES (
            NEW.id,
            NEW.id_survey,
            'update',
            json_build_object(
                'previous_title', OLD.title,
                'new_title', NEW.title,
                'previous_description', OLD.description,
                'new_description', NEW.description,
                'previous_is_mandatory', OLD.is_mandatory,
                'new_is_mandatory', NEW.is_mandatory,
                'previous_display_order', OLD.display_order,
                'new_display_order', NEW.display_order,
                'previous_correct_answer', OLD.correct_answer,
                'new_correct_answer', NEW.correct_answer
            ),
            (SELECT id_user FROM survey WHERE id = NEW.id_survey)
        );
    ELSIF (TG_OP = 'INSERT') THEN
        INSERT INTO history_question (id_question, id_survey, action, snapshot, id_user)
        VALUES (
            NEW.id,
            NEW.id_survey,
            'create',
            json_build_object(
                'title', NEW.title,
                'description', NEW.description,
                'is_mandatory', NEW.is_mandatory,
                'display_order', NEW.display_order,
                'correct_answer', NEW.correct_answer
            ),
            (SELECT id_user FROM survey WHERE id = NEW.id_survey)
        );
    ELSIF (TG_OP = 'DELETE') THEN
        INSERT INTO history_question (id_question, id_survey, action, snapshot, id_user)
        VALUES (
            OLD.id,
            OLD.id_survey,
            'delete',
            json_build_object(
                'title', OLD.title,
                'description', OLD.description,
                'is_mandatory', OLD.is_mandatory,
                'display_order', OLD.display_order,
                'correct_answer', OLD.correct_answer
            ),
            (SELECT id_user FROM survey WHERE id = OLD.id_survey)
        );
    END IF;

    IF (TG_OP = 'DELETE') THEN
        RETURN OLD;
    ELSE
        RETURN NEW;
    END IF;
END;
$$ language 'plpgsql';

-- Fonction pour historiser la publication/dépublication
CREATE OR REPLACE FUNCTION log_survey_status_changes()
RETURNS TRIGGER AS $$
BEGIN
    IF (OLD.is_active IS DISTINCT FROM NEW.is_active) THEN
        INSERT INTO history (id_survey, action, snapshot, id_user)
        VALUES (
            NEW.id,
            CASE WHEN NEW.is_active THEN 'publish' ELSE 'unpublish' END,
            json_build_object(
                'previous_status', OLD.is_active,
                'new_status', NEW.is_active
            ),
            NEW.id_user
        );
    END IF;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Fonction pour nettoyer les tokens expirés
CREATE OR REPLACE FUNCTION fn_cleanup_expired_tokens()
RETURNS INTEGER AS $$
DECLARE
    deleted_count INTEGER;
BEGIN
    DELETE FROM blacklisted_tokens
    WHERE expires_at < NOW();

    GET DIAGNOSTICS deleted_count = ROW_COUNT;
    RAISE NOTICE 'Cleaned up % expired tokens', deleted_count;

    RETURN deleted_count;
END;
$$ LANGUAGE plpgsql;

-- Création des triggers
DROP TRIGGER IF EXISTS update_survey_updated_at ON survey;
CREATE TRIGGER update_survey_updated_at
    BEFORE UPDATE ON survey
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

DROP TRIGGER IF EXISTS update_user_updated_at ON users;
CREATE TRIGGER update_user_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

DROP TRIGGER IF EXISTS update_question_updated_at ON question;
CREATE TRIGGER update_question_updated_at
    BEFORE UPDATE ON question
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

DROP TRIGGER IF EXISTS question_history_trigger ON question;
CREATE TRIGGER question_history_trigger
    AFTER INSERT OR UPDATE OR DELETE ON question
    FOR EACH ROW
    EXECUTE FUNCTION log_question_changes();

DROP TRIGGER IF EXISTS survey_history_trigger ON survey;
CREATE TRIGGER survey_history_trigger
    AFTER INSERT OR UPDATE ON survey
    FOR EACH ROW
    EXECUTE FUNCTION log_survey_changes();

DROP TRIGGER IF EXISTS survey_status_history_trigger ON survey;
CREATE TRIGGER survey_status_history_trigger
    AFTER UPDATE OF is_active ON survey
    FOR EACH ROW
    EXECUTE FUNCTION log_survey_status_changes();

-- Sample Data
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM users LIMIT 1) THEN
        -- Insertion des utilisateurs
        INSERT INTO users (name, surname, email, password, is_admin) VALUES
        ('Admin', 'Akadmin', 'admin@example.com', '$argon2id$v=19$m=65536,t=10,p=1$Q2+UgerwZFJyMGC/P6O6fQ$Wq+arapkagYKmHZOeAmEvnCO3h7F8zKQaRU1gilEbOc', true),
        ('Bob', 'Martin', 'bob.martin@email.com', '$argon2id$v=19$m=65536,t=10,p=1$Q2+UgerwZFJyMGC/P6O6fQ$Wq+arapkagYKmHZOeAmEvnCO3h7F8zKQaRU1gilEbOc', false),
        ('Claire', 'Leroy', 'claire.leroy@email.com', '$argon2id$v=19$m=65536,t=10,p=1$Q2+UgerwZFJyMGC/P6O6fQ$Wq+arapkagYKmHZOeAmEvnCO3h7F8zKQaRU1gilEbOc', false),
        ('Samy', 'Douay', 'samy@example.com', '$argon2id$v=19$m=65536,t=10,p=1$zcZdug8Ah7l1D+Ng1GOWhQ$EfOZcHOK3bgHNoJYcR01MbvgMWDKnSmcn5cEJS4o5yE', true);

        -- Insertion des sondages
        INSERT INTO survey (name, anonymization, description, id_user, is_quiz, is_active, allow_editing, is_public, share_token) VALUES
        ('Satisfaction Client Restaurants', 'medium', 'Évaluez votre expérience dans nos restaurants', 1, false, true, true, true, 'token_restaurant_123'),
        ('Quiz Culture Générale', 'none', 'Testez vos connaissances en culture générale', 2, true, true, false, true, 'token_quiz_456'),
        ('Enquête Bien-être au Travail', 'high', 'Évaluation anonyme du bien-être en entreprise', 1, false, true, true, false, 'token_bienetre_789'),
        ('Sondage Préférences Alimentaires', 'low', 'Vos habitudes et préférences alimentaires', 3, false, false, true, true, 'token_alimentation_101');

        -- Insertion des questions pour le sondage de satisfaction client
        INSERT INTO question (title, description, id_survey, is_mandatory, correct_answer, display_order) VALUES
        ('Qualité du service', 'Comment évaluez-vous l''accueil et le service ?', 1, true, null, 1),
        ('Propreté des lieux', 'Notez la propreté générale du restaurant', 1, true, null, 2),
        ('Recommanderiez-vous ?', 'Recommanderiez-vous notre restaurant à vos proches ?', 1, false, null, 3);

        -- Insertion des questions pour le quiz culture générale
        INSERT INTO question (title, description, id_survey, is_mandatory, correct_answer, display_order) VALUES
        ('Capitale de la France', 'Quelle est la capitale de la France ?', 2, true, '"Paris"', 1),
        ('Année 1789', 'Que s''est-il passé en 1789 en France ?', 2, true, '"Révolution française"', 2),
        ('Planètes du système solaire', 'Combien y a-t-il de planètes dans le système solaire ?', 2, true, '8', 3);

        -- Insertion des questions pour l''enquête bien-être
        INSERT INTO question (title, description, id_survey, is_mandatory, correct_answer, display_order) VALUES
        ('Équilibre vie pro/perso', 'Comment évaluez-vous votre équilibre vie professionnelle/vie personnelle ?', 3, true, null, 1),
        ('Ambiance de travail', 'Comment décririez-vous l''ambiance générale au travail ?', 3, false, null, 2),
        ('Soutien managérial', 'Recevez-vous un soutien suffisant de la part de votre manager ?', 3, true, null, 3);

        -- Insertion des réponses pour le sondage satisfaction client
        INSERT INTO answer (id_question, is_anonymous, original_answer, anonymous_answer, respondent_id, is_correct) VALUES
        (1, false, '"Très satisfait"', '"Satisfait"', 'resp_001', null),
        (1, false, '"Satisfait"', '"Satisfait"', 'resp_002', null),
        (2, false, '"Excellente"', '"Bonne"', 'resp_001', null),
        (2, false, '"Bonne"', '"Bonne"', 'resp_002', null),
        (3, false, '"Oui"', '"Oui"', 'resp_001', null);

        -- Insertion des réponses pour le quiz culture générale
        INSERT INTO answer (id_question, is_anonymous, original_answer, anonymous_answer, respondent_id, is_correct) VALUES
        (4, false, '"Paris"', '"Paris"', 'quiz_user_001', true),
        (4, false, '"Lyon"', '"Lyon"', 'quiz_user_002', false),
        (5, false, '"Révolution française"', '"Révolution française"', 'quiz_user_001', true),
        (5, false, '"Première Guerre mondiale"', '"Première Guerre mondiale"', 'quiz_user_002', false),
        (6, false, '8', '8', 'quiz_user_001', true),
        (6, false, '9', '9', 'quiz_user_002', false);

        -- Insertion des réponses pour l'enquête bien-être (anonymisées)
        INSERT INTO answer (id_question, is_anonymous, original_answer, anonymous_answer, respondent_id, is_correct) VALUES
        (7, true, '"Très bon"', '"Bon"', 'anon_001', null),
        (7, true, '"Moyen"', '"Moyen"', 'anon_002', null),
        (8, true, '"Agréable"', '"Positive"', 'anon_001', null),
        (9, true, '"Oui tout à fait"', '"Oui"', 'anon_002', null);

        -- Insertion d'historique pour les sondages
        INSERT INTO history (id_survey, action, snapshot, id_user) VALUES
        (1, 'create', '{"name": "Satisfaction Client Restaurants", "description": "Évaluez votre expérience dans nos restaurants", "anonymization": "medium", "is_quiz": false, "is_public": true, "allow_editing": true}', 1),
        (1, 'publish', '{"previous_status": false, "new_status": true}', 1),
        (2, 'create', '{"name": "Quiz Culture Générale", "description": "Testez vos connaissances en culture générale", "anonymization": "none", "is_quiz": true, "is_public": true, "allow_editing": false}', 2);

        -- Insertion d'historique pour les questions
        INSERT INTO history_question (id_question, id_survey, action, snapshot, id_user) VALUES
        (1, 1, 'create', '{"title": "Qualité du service", "description": "Comment évaluez-vous l''accueil et le service ?", "is_mandatory": true, "display_order": 1, "correct_answer": null}', 1),
        (4, 2, 'create', '{"title": "Capitale de la France", "description": "Quelle est la capitale de la France ?", "is_mandatory": true, "display_order": 1, "correct_answer": "Paris"}', 2),
        (4, 2, 'update', '{"previous_title": "Capitale française", "new_title": "Capitale de la France", "previous_description": "Nommez la capitale", "new_description": "Quelle est la capitale de la France ?", "previous_is_mandatory": false, "new_is_mandatory": true, "previous_display_order": 1, "new_display_order": 1, "previous_correct_answer": "Paris", "new_correct_answer": "Paris"}', 2);

        RAISE NOTICE 'Données d''exemple insérées avec succès';
    ELSE
        RAISE NOTICE 'La base contient déjà des données, insertion ignorée';
    END IF;
END $$;