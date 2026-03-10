INSERT INTO users (username, email, password) VALUES
('nico', 'nico@mail.com', '$2a$12$fXtMnxBlh1e87G9qvbtp/.8xe67k6ZuVj5KVkAVBir5rCkbrlb4tm'),
('admin', 'admin@mail.com', '$2a$12$fXtMnxBlh1e87G9qvbtp/.8xe67k6ZuVj5KVkAVBir5rCkbrlb4tm');

INSERT INTO user_roles (user_id, role) VALUES
(1, 'ROLE_USER'),
(2, 'ROLE_USER'),
(2, 'ROLE_ADMIN');

INSERT INTO projects (name, description, user_id) VALUES
('Proyecto personal', 'Mis tareas del dia a dia', 1),
('Trabajo', 'Tareas del trabajo', 1);

INSERT INTO tasks (title, description, status, priority, due_date, project_id) VALUES
('Estudiar Spring Security', 'Repasar filtros y JWT', 'IN_PROGRESS', 'HIGH', '2026-03-15', 1),
('Comprar cafe', NULL, 'PENDING', 'LOW', NULL, 1),
('Terminar informe', 'Informe mensual de ventas', 'PENDING', 'HIGH', '2026-03-10', 2);