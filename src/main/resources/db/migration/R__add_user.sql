MERGE INTO user_t
    (email, password_hash, name, tos_accepted)
    KEY (email)
    VALUES ('august@crud.business', '456def', 'August Lilleaas', true);