CREATE TABLE analyst_coverage (
    analyst_id UUID NOT NULL,
    state varchar(2) NOT NULL,

    CONSTRAINT pk_analyst_coverage
      PRIMARY KEY (analyst_id, state),

    CONSTRAINT fk_analyst_coverage_user
      FOREIGN KEY (analyst_id)
          REFERENCES users (id)
          ON DELETE CASCADE,

    CONSTRAINT ck_analyst_coverage_state
      CHECK (state IN (
            'AC', 'AL', 'AP', 'AM', 'BA',
            'CE', 'DF', 'ES', 'GO', 'MA',
            'MT', 'MS', 'MG', 'PA', 'PB',
            'PR', 'PE', 'PI', 'RJ', 'RN',
            'RS', 'RO', 'RR', 'SC', 'SP',
            'SE', 'TO'
          ))
);

CREATE INDEX idx_analyst_coverage_state
    ON analyst_coverage (state);
