CREATE TABLE solicitations (
    id UUID NOT NULL,
    client_id UUID NOT NULL,

    service_type VARCHAR(30),
    title VARCHAR(80),
    description VARCHAR(1000),

    cep VARCHAR(8),
    street VARCHAR(255),
    city VARCHAR(150),
    neighborhood VARCHAR(150),
    state VARCHAR(2),
    address_number VARCHAR(20),
    complement VARCHAR(255),
    postal_code_validated BOOLEAN,

    priority VARCHAR(20),
    preferred_date DATE,
    estimated_value NUMERIC(12, 2),
    terms_accepted BOOLEAN,

    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    current_step INTEGER NOT NULL DEFAULT 1,

    analyst_id UUID,
    decision_type VARCHAR(20),
    decision_comment VARCHAR(1000),
    decided_at TIMESTAMP,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    submitted_at TIMESTAMP,

    CONSTRAINT pk_solicitations
       PRIMARY KEY (id),

    CONSTRAINT fk_solicitations_client
       FOREIGN KEY (client_id)
           REFERENCES users (id),

    CONSTRAINT fk_solicitations_analyst
       FOREIGN KEY (analyst_id)
           REFERENCES users (id),

    CONSTRAINT ck_solicitations_status
       CHECK (status IN (
                         'DRAFT',
                         'SUBMITTED',
                         'IN_REVIEW',
                         'APPROVED',
                         'REJECTED'
           )),

    CONSTRAINT ck_solicitations_service_type
       CHECK (
           service_type IS NULL
               OR service_type IN (
                                   'INSTALLATION',
                                   'MAINTENANCE',
                                   'INSPECTION'
               )
           ),

    CONSTRAINT ck_solicitations_priority
       CHECK (
           priority IS NULL
               OR priority IN ('LOW', 'MEDIUM', 'HIGH')
           ),

    CONSTRAINT ck_solicitations_decision_type
       CHECK (
           decision_type IS NULL
               OR decision_type IN ('APPROVE', 'REJECT')
           ),

    CONSTRAINT ck_solicitations_current_step
       CHECK (current_step BETWEEN 1 AND 3),

    CONSTRAINT ck_solicitations_estimated_value
       CHECK (
           estimated_value IS NULL
               OR estimated_value >= 0
           ),

    CONSTRAINT ck_solicitations_cep
       CHECK (
           cep IS NULL
               OR cep ~ '^[0-9]{8}$'
        ),

    CONSTRAINT ck_solicitations_state
        CHECK (
            state IS NULL
            OR state ~ '^[A-Z]{2}$'
        ),

    CONSTRAINT ck_solicitations_decision_data
        CHECK (
            (
                decision_type IS NULL
                AND decision_comment IS NULL
                AND decided_at IS NULL
            )
            OR
            (
                decision_type IS NOT NULL
                AND decision_comment IS NOT NULL
                AND decided_at IS NOT NULL
                AND analyst_id IS NOT NULL
            )
        )
);

CREATE INDEX idx_solicitations_client_status
    ON solicitations (client_id, status);

CREATE INDEX idx_solicitations_analyst_status
    ON solicitations (analyst_id, status);

CREATE INDEX idx_solicitations_created_at
    ON solicitations (created_at);
