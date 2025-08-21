INSERT INTO teams (team_id, team_name, coach_name, team_level)
VALUES
    ('11111111-1111-1111-1111-111111111111', 'Montreal Eagles', 'John Smith', 'COLLEGE'),
    ('22222222-2222-2222-2222-222222222222', 'Toronto Hawks', 'Jane Doe', 'PROFESSIONAL'),
    ('33333333-3333-3333-3333-333333333333', 'Quebec Falcons', 'Albert Martin', 'NATIONAL'),
    ('44444444-4444-4444-4444-444444444444', 'Vancouver Wolves', 'Nadia Li', 'HIGH_SCHOOL'),
    ('55555555-5555-5555-5555-555555555555', 'Ottawa Bears', 'Mark Spencer', 'COLLEGE'),
    ('66666666-6666-6666-6666-666666666666', 'Calgary Cougars', 'Linda Evans', 'COLLEGE'),
    ('77777777-7777-7777-7777-777777777777', 'Edmonton Eagles', 'Robert Brown', 'PROFESSIONAL'),
    ('88888888-8888-8888-8888-888888888888', 'Winnipeg Wolves', 'Susan Clark', 'NATIONAL'),
    ('99999999-9999-9999-9999-999999999999', 'Halifax Hurricanes', 'George King', 'HIGH_SCHOOL'),
    ('abcdefab-cdef-cdef-cdef-abcdefabcdef', 'Saskatoon Stallions', 'Karen White', 'COLLEGE');
INSERT INTO athletes (athlete_id, first_name, last_name, date_of_birth, athlete_category, team_id)
VALUES
    ('ath11111-1111-1111-1111-111111111111', 'Michael', 'Jordan', '1995-01-15', 'SENIOR', '11111111-1111-1111-1111-111111111111'),
    ('ath22222-2222-2222-2222-222222222222', 'Emily', 'Johnson', '1998-07-22', 'JUNIOR', '11111111-1111-1111-1111-111111111111'),
    ('ath33333-3333-3333-3333-333333333333', 'James', 'Williams', '1990-03-10', 'MASTER', '22222222-2222-2222-2222-222222222222'),
    ('ath44444-4444-4444-4444-444444444444', 'Sarah', 'Brown', '2000-11-05', 'SENIOR', '33333333-3333-3333-3333-333333333333'),
    ('ath55555-5555-5555-5555-555555555555', 'David', 'Smith', '1992-08-10', 'SENIOR', '44444444-4444-4444-4444-444444444444'),
    ('ath66666-6666-6666-6666-666666666666', 'Alex', 'Turner', '1997-04-12', 'JUNIOR', '66666666-6666-6666-6666-666666666666'),
    ('ath77777-7777-7777-7777-777777777777', 'Olivia', 'Benson', '1999-09-30', 'SENIOR', '77777777-7777-7777-7777-777777777777'),
    ('ath88888-8888-8888-8888-888888888888', 'Liam', 'Nolan', '2001-02-20', 'JUNIOR', '88888888-8888-8888-8888-888888888888'),
    ('ath99999-9999-9999-9999-999999999999', 'Emma', 'Stone', '1996-12-05', 'MASTER', '99999999-9999-9999-9999-999999999999'),
    ('ath101010-1010-1010-1010-10101010101', 'Noah', 'Davis', '1994-06-18', 'SENIOR', 'abcdefab-cdef-cdef-cdef-abcdefabcdef');