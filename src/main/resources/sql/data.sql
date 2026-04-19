-- 초기 데이터는 DataInitializer.java 에서 코드로 처리합니다.

-- =============================================
-- SVY_QST_TB 더미 데이터 (SVY_ID = 2)
-- 문항 유형: SINGLE_CHOICE, MULTIPLE_CHOICE, SUBJECTIVE, SCALE, RANKING
-- 조건분기: QST_ID=1(SINGLE_CHOICE) → PRNTS_ITM_ID=2(옵션2 선택 시) → CHLRN_QST_ID=6(분기 문항)
-- =============================================

INSERT INTO svy_qst_tb (SVY_ID, QST_TYPE, QST_NM, ESNTL_RSPN_YN, PRNTS_ITM_ID, CHLRN_QST_ID, SORT_ODR, FRST_CRTN_ID)
VALUES
-- 1번: 단일선택 (조건분기 트리거 — 옵션2 선택 시 6번 문항 활성화)
(2, 'SINGLE_CHOICE',   '전반적인 서비스 만족도는 어떠셨나요?',                    1, 0, 6, 1, 1),
-- 2번: 복수선택
(2, 'MULTIPLE_CHOICE', '개선이 필요한 분야를 모두 선택해 주세요. (복수 선택 가능)', 0, 0, 0, 2, 1),
-- 3번: 주관식
(2, 'SUBJECTIVE',      '서비스 이용 중 불편했던 점이나 개선 사항을 적어주세요.',    0, 0, 0, 3, 1),
-- 4번: 척도
(2, 'SCALE',           '이용하신 서비스의 가격 대비 만족도는 어떠셨나요?',          1, 0, 0, 4, 1),
-- 5번: 순위선택
(2, 'RANKING',         '중요하다고 생각하는 서비스 요소를 순위대로 정렬해 주세요.', 0, 0, 0, 5, 1),
-- 6번: 조건분기 문항 (1번 SINGLE_CHOICE에서 옵션2 선택 시 활성화)
(2, 'SINGLE_CHOICE',   '왜 만족하신다고 느끼셨나요?',                             1, 2, 0, 6, 1);


-- =============================================
-- SVY_QST_ITM_TB 더미 데이터
-- 문항 유형별 옵션 (TEXT는 옵션 없음, 나머지 5개씩)
-- QST_ID 는 위 INSERT 순서 기준 auto_increment 값에 맞게 조정 필요
-- 현재 테이블이 비어있다고 가정하고 QST_ID = 1~6 으로 작성
-- =============================================

-- QST_ID=1: 단일선택 옵션 5개
INSERT INTO svy_qst_itm_tb (QST_ID, ITM_NM, SORT_ODR, FRST_CRTN_ID)
VALUES
(1, '매우 만족해요',    1, 1),
(1, '만족해요',        2, 1),
(1, '보통이에요',      3, 1),
(1, '불만족해요',      4, 1),
(1, '매우 불만족해요', 5, 1);

-- QST_ID=2: 복수선택 옵션 5개
INSERT INTO svy_qst_itm_tb (QST_ID, ITM_NM, SORT_ODR, FRST_CRTN_ID)
VALUES
(2, '가격 / 비용',   1, 1),
(2, '서비스 품질',   2, 1),
(2, '직원 응대',     3, 1),
(2, '배송/처리 속도', 4, 1),
(2, '앱/웹 편의성',  5, 1);

-- QST_ID=3: 주관식 — 옵션 없음 (SKIP)

-- QST_ID=4: 척도 옵션 5개 (1~5점)
INSERT INTO svy_qst_itm_tb (QST_ID, ITM_NM, SORT_ODR, FRST_CRTN_ID)
VALUES
(4, '매우 불만족', 1, 1),
(4, '불만족',     2, 1),
(4, '보통',       3, 1),
(4, '만족',       4, 1),
(4, '매우 만족',  5, 1);

-- QST_ID=5: 순위선택 옵션 5개
INSERT INTO svy_qst_itm_tb (QST_ID, ITM_NM, SORT_ODR, FRST_CRTN_ID)
VALUES
(5, '가격 / 비용',   1, 1),
(5, '서비스 품질',   2, 1),
(5, '직원 응대',     3, 1),
(5, '처리 속도',     4, 1),
(5, '앱/웹 편의성',  5, 1);

-- QST_ID=6: 조건분기 단일선택 옵션 5개
INSERT INTO svy_qst_itm_tb (QST_ID, ITM_NM, SORT_ODR, FRST_CRTN_ID)
VALUES
(6, '인사와 말투가 좋았어요',      1, 1),
(6, '요청을 빠르게 처리해줬어요',  2, 1),
(6, '문제 해결에 적극적이었어요',  3, 1),
(6, '가격이 합리적이었어요',       4, 1),
(6, '기타',                       5, 1);
