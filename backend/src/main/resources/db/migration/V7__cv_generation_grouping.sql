-- One "generate" produces the English and French versions of the same CV.
-- Grouping them lets CV Studio show a single card per target role.

ALTER TABLE cv_documents ADD COLUMN generation_id UUID;
ALTER TABLE cv_documents ADD COLUMN title VARCHAR(300);

-- Existing rows predate grouping: give each its own group so nothing merges by accident.
UPDATE cv_documents SET generation_id = id WHERE generation_id IS NULL;
UPDATE cv_documents SET title = target_role WHERE title IS NULL;

ALTER TABLE cv_documents ALTER COLUMN generation_id SET NOT NULL;

CREATE INDEX idx_cv_documents_generation ON cv_documents (generation_id);
