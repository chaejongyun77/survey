-- =============================================
-- MEMBER 테이블
-- =============================================
CREATE TABLE IF NOT EXISTS member (
    member_id  BIGINT          NOT NULL AUTO_INCREMENT,
    login_id   VARCHAR(50)     NOT NULL UNIQUE,
    password   VARCHAR(255)    NOT NULL,
    name       VARCHAR(50)     NOT NULL,
    email      VARCHAR(100),
    role       VARCHAR(20)     NOT NULL DEFAULT 'ROLE_USER',
    status     VARCHAR(10)     NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME        NOT NULL DEFAULT NOW(),
    updated_at DATETIME        NOT NULL DEFAULT NOW(),
    PRIMARY KEY (member_id)
);

-- =============================================
-- SURVEY 테이블
-- =============================================
CREATE TABLE IF NOT EXISTS survey (
    survey_id   BIGINT          NOT NULL AUTO_INCREMENT,
    title       VARCHAR(200)    NOT NULL,
    description VARCHAR(2000),
    status      VARCHAR(10)     NOT NULL DEFAULT 'DRAFT',
    created_by  VARCHAR(50)     NOT NULL,
    start_date  DATETIME,
    end_date    DATETIME,
    created_at  DATETIME        NOT NULL DEFAULT NOW(),
    updated_at  DATETIME        NOT NULL DEFAULT NOW(),
    PRIMARY KEY (survey_id)
);


