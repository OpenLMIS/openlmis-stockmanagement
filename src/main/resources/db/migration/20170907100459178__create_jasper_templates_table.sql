CREATE TABLE jasper_templates (
  id uuid NOT NULL,
  name text NOT NULL,
  data bytea,
  type text,
  description text
);
