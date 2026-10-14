INSERT INTO users (
    id,
    name,
    email,
    password_hash,
    role,
    active,
    created_at
)
VALUES (
    'a8b6d3e1-7c42-4f91-9a25-6e8d0b3c1f72',
    'Administrador',
    'admin@requestsystem.local',
--         Senha gerada utilizando o BCrypt: adm12345
    '$2a$10$BvkdjBCe7rODcunXTtSad.FjMThcWUW96uAcNsU/n3iq8yrjCfkVm',
    'ADMIN',
    TRUE,
    CURRENT_TIMESTAMP
);
