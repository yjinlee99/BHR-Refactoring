INSERT INTO sequence_table (sequence_name, next_val)
VALUES ('entity_sequence', 1);

INSERT INTO badge_master (
    badge_name,
    badge_detail,
    badge_image,
    status
)
VALUES (
           'X세대',
           '배지 API 정상 응답 확인을 위한 테스트 배지',
           NULL,
           'Disabled'
       );