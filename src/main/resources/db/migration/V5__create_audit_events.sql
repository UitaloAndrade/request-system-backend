CREATE TABLE audit_logs (
    id UUID NOT NULL,
    actor_id UUID,
    actor VARCHAR(254),
    action VARCHAR(100) NOT NULL,
    resource_type VARCHAR(100) NOT NULL,
    resource_id VARCHAR(100),
    outcome VARCHAR(20) NOT NULL,
    details VARCHAR(1000),
    occurred_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT pk_audit_logs PRIMARY KEY (id),

    CONSTRAINT ck_audit_logs_outcome
      CHECK (outcome IN ('SUCCESS', 'FAILURE'))
);

CREATE INDEX idx_audit_logs_actor
    ON audit_logs (actor_id);

CREATE INDEX idx_audit_logs_resource
    ON audit_logs (resource_type, resource_id);

CREATE INDEX idx_audit_logs_occurred_at
    ON audit_logs (occurred_at);