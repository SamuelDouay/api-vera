-- Créer la table users
CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    surname VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    role VARCHAR(255) DEFAULT 'user',
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Créer l'index sur email
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);

-- Créer une fonction pour mettre à jour automatiquement updated_at
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Supprimer le trigger s'il existe déjà
DROP TRIGGER IF EXISTS update_users_updated_at ON users;

-- Créer le trigger
CREATE TRIGGER update_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Insérer des données de test
INSERT INTO users (name, surname, email, password)
VALUES  ('John', 'Doe', 'john.doe@example.com', '$argon2id$v=19$m=65536,t=10,p=1$Q2+UgerwZFJyMGC/P6O6fQ$Wq+arapkagYKmHZOeAmEvnCO3h7F8zKQaRU1gilEbOc'),
        ('Jane', 'Smith', 'jane.smith@example.com', '$argon2id$v=19$m=65536,t=10,p=1$Q2+UgerwZFJyMGC/P6O6fQ$Wq+arapkagYKmHZOeAmEvnCO3h7F8zKQaRU1gilEbOc'),
        ('Bob', 'Martin', 'bob.martin@example.com', '$argon2id$v=19$m=65536,t=10,p=1$Q2+UgerwZFJyMGC/P6O6fQ$Wq+arapkagYKmHZOeAmEvnCO3h7F8zKQaRU1gilEbOc'),
        ('Alice', 'Johnson', 'alice.johnson@example.com', '$argon2id$v=19$m=65536,t=10,p=1$Q2+UgerwZFJyMGC/P6O6fQ$Wq+arapkagYKmHZOeAmEvnCO3h7F8zKQaRU1gilEbOc'),
        ('Charlie', 'Brown', 'charlie.brown@example.com', '$argon2id$v=19$m=65536,t=10,p=1$Q2+UgerwZFJyMGC/P6O6fQ$Wq+arapkagYKmHZOeAmEvnCO3h7F8zKQaRU1gilEbOc')
ON CONFLICT (email) DO NOTHING;

INSERT INTO users (name, surname, email, role, password)
VALUES  ('Admin', 'Akadmin', 'admin@example.com', 'admin', '$argon2id$v=19$m=65536,t=10,p=1$Q2+UgerwZFJyMGC/P6O6fQ$Wq+arapkagYKmHZOeAmEvnCO3h7F8zKQaRU1gilEbOc')
ON CONFLICT (email) DO NOTHING;