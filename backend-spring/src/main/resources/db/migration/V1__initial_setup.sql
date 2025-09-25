-- @dialect MySQL

-- DB 스키마 변경 스크립트

-- 1. 외래 키(FK) 제약 조건 검사 비활성화
SET foreign_key_checks = 0;


-- 2. 더 이상 사용하지 않는 테이블 삭제
DROP TABLE IF EXISTS chat_session, chat_message;


-- 3. template_modified_history 테이블의 불필요한 컬럼 삭제
-- ALTER TABLE template_modified_history DROP KEY `UKnpsb3ag3h7ou3oaidvf86i886`;
ALTER TABLE template_modified_history DROP COLUMN IF EXISTS chat_message_id;


-- 4. template_modified_history 테이블에 새로운 컬럼 추가
ALTER TABLE template_modified_history
    ADD COLUMN IF NOT EXISTS chat_user TEXT,
    ADD COLUMN IF NOT EXISTS chat_ai TEXT;


-- 5. 외래 키(FK) 제약 조건 검사 다시 활성화
SET foreign_key_checks = 1;