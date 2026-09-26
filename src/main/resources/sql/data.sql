-- ==========================================================
-- Mukplay OX Quiz Game - Sample Initial Questions (DML)
-- ==========================================================

USE mukplay;

INSERT INTO questions (content, answer, difficulty, status, submitter_id, created_at, updated_at) VALUES
('토마토는 과일이 아니라 채소다.', 'O', 'EASY', 'APPROVED', 1, NOW(), NOW()),
('남극과 북극 중 남극의 평균 기온이 더 낮다.', 'O', 'NORMAL', 'APPROVED', 1, NOW(), NOW()),
('낙타의 혹은 물로 채워져 있다.', 'X', 'EASY', 'APPROVED', 1, NOW(), NOW()),
('달팽이도 이빨이 있다.', 'O', 'HARD', 'APPROVED', 1, NOW(), NOW()),
('빛의 속도는 소리의 속도보다 느리다.', 'X', 'EASY', 'APPROVED', 1, NOW(), NOW()),
('비행기의 블랙박스는 주황색이다.', 'O', 'NORMAL', 'APPROVED', 1, NOW(), NOW()),
('딸기는 장미과에 속하는 식물이다.', 'O', 'HARD', 'APPROVED', 1, NOW(), NOW()),
('인간의 뼈는 성인이 아기보다 개수가 더 많다.', 'X', 'NORMAL', 'APPROVED', 1, NOW(), NOW()),
('북극곰의 피부색은 검은색이다.', 'O', 'HARD', 'APPROVED', 1, NOW(), NOW()),
('치타는 사자보다 달리기 속도가 빠르다.', 'O', 'EASY', 'APPROVED', 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE updated_at = NOW();
