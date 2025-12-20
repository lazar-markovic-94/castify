-- V12__populate_collaborations.sql
-- Seed collaboration data between clients and talents

-- =============================================================================
-- COLLABORATIONS
-- =============================================================================
-- This migration populates the collaboration table with realistic project data
-- showcasing various collaboration statuses and scenarios.
--
-- Clients:
--   ABC Productions: aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa
--   Creative Minds Agency: bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb
--   Brand Studios Inc: cccccccc-cccc-cccc-cccc-cccccccccccc
--
-- Talents:
--   John Actor: 10000001-0000-0000-0000-000000000001
--   Sarah Voice: 20000002-0000-0000-0000-000000000002
--   Mike Model: 30000003-0000-0000-0000-000000000003
--   Emma Singer: 40000004-0000-0000-0000-000000000004
--   David Host: 50000005-0000-0000-0000-000000000005
-- =============================================================================

-- 1. ABC Productions + John Actor - Action Film (COMPLETED)
INSERT INTO collaboration (
    collaboration_id,
    client_user_id,
    talent_profile_id,
    title,
    description,
    budget,
    start_date,
    end_date,
    status,
    proposed_at,
    responded_at,
    completed_at,
    notes,
    created_at,
    updated_at
) VALUES (
    'b0000001-0000-0000-0000-000000000001',
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
    '10000001-0000-0000-0000-000000000001',
    'Lead Role - Urban Justice',
    'Looking for a lead actor for our upcoming action thriller "Urban Justice". The role requires someone with martial arts experience and strong dramatic skills. Shooting schedule is 8 weeks in Los Angeles.',
    75000.00,
    CURRENT_TIMESTAMP - INTERVAL '90 days',
    CURRENT_TIMESTAMP - INTERVAL '30 days',
    'COMPLETED',
    CURRENT_TIMESTAMP - INTERVAL '120 days',
    CURRENT_TIMESTAMP - INTERVAL '115 days',
    CURRENT_TIMESTAMP - INTERVAL '30 days',
    'Excellent performance! John brought incredible energy and professionalism to the set. Would definitely work together again.',
    CURRENT_TIMESTAMP - INTERVAL '120 days',
    CURRENT_TIMESTAMP - INTERVAL '30 days'
);

-- 2. ABC Productions + David Host - Reality TV Show (IN_PROGRESS)
INSERT INTO collaboration (
    collaboration_id,
    client_user_id,
    talent_profile_id,
    title,
    description,
    budget,
    start_date,
    end_date,
    status,
    proposed_at,
    responded_at,
    completed_at,
    notes,
    created_at,
    updated_at
) VALUES (
    'b0000002-0000-0000-0000-000000000002',
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
    '50000005-0000-0000-0000-000000000005',
    'Host - "The Talent Search"',
    'We need an experienced TV host for our new reality competition show "The Talent Search". 12 episodes, filmed over 3 months. Remote work possible with travel to filming locations.',
    120000.00,
    CURRENT_TIMESTAMP - INTERVAL '30 days',
    CURRENT_TIMESTAMP + INTERVAL '60 days',
    'IN_PROGRESS',
    CURRENT_TIMESTAMP - INTERVAL '45 days',
    CURRENT_TIMESTAMP - INTERVAL '42 days',
    NULL,
    'Production is going smoothly. David has great chemistry with contestants and crew.',
    CURRENT_TIMESTAMP - INTERVAL '45 days',
    CURRENT_TIMESTAMP - INTERVAL '5 days'
);

-- 3. ABC Productions + Sarah Voice - Documentary Narration (COMPLETED)
INSERT INTO collaboration (
    collaboration_id,
    client_user_id,
    talent_profile_id,
    title,
    description,
    budget,
    start_date,
    end_date,
    status,
    proposed_at,
    responded_at,
    completed_at,
    notes,
    created_at,
    updated_at
) VALUES (
    'b0000003-0000-0000-0000-000000000003',
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
    '20000002-0000-0000-0000-000000000002',
    'Documentary Narration - "Ocean Mysteries"',
    'Voice over narration for 6-part nature documentary series about marine life. Warm, authoritative tone required. Recording sessions in New York studio.',
    45000.00,
    CURRENT_TIMESTAMP - INTERVAL '150 days',
    CURRENT_TIMESTAMP - INTERVAL '120 days',
    'COMPLETED',
    CURRENT_TIMESTAMP - INTERVAL '165 days',
    CURRENT_TIMESTAMP - INTERVAL '160 days',
    CURRENT_TIMESTAMP - INTERVAL '120 days',
    'Sarah delivered exceptional narration. Her voice perfectly captured the wonder and beauty of the ocean.',
    CURRENT_TIMESTAMP - INTERVAL '165 days',
    CURRENT_TIMESTAMP - INTERVAL '120 days'
);

-- 4. ABC Productions + Emma Singer - Film Soundtrack (DECLINED)
INSERT INTO collaboration (
    collaboration_id,
    client_user_id,
    talent_profile_id,
    title,
    description,
    budget,
    start_date,
    end_date,
    status,
    proposed_at,
    responded_at,
    completed_at,
    notes,
    created_at,
    updated_at
) VALUES (
    'b0000004-0000-0000-0000-000000000004',
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
    '40000004-0000-0000-0000-000000000004',
    'Original Song - Romantic Comedy Soundtrack',
    'Looking for an original song for our upcoming romantic comedy. Country-pop style to match the Nashville setting. Budget includes composition and recording rights.',
    25000.00,
    CURRENT_TIMESTAMP + INTERVAL '30 days',
    CURRENT_TIMESTAMP + INTERVAL '60 days',
    'DECLINED',
    CURRENT_TIMESTAMP - INTERVAL '10 days',
    CURRENT_TIMESTAMP - INTERVAL '8 days',
    NULL,
    'Emma declined due to scheduling conflict with her album release tour.',
    CURRENT_TIMESTAMP - INTERVAL '10 days',
    CURRENT_TIMESTAMP - INTERVAL '8 days'
);

-- 5. Creative Minds Agency + Sarah Voice - Audiobook Series (COMPLETED)
INSERT INTO collaboration (
    collaboration_id,
    client_user_id,
    talent_profile_id,
    title,
    description,
    budget,
    start_date,
    end_date,
    status,
    proposed_at,
    responded_at,
    completed_at,
    notes,
    created_at,
    updated_at
) VALUES (
    'b0000005-0000-0000-0000-000000000005',
    'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
    '20000002-0000-0000-0000-000000000002',
    'Audiobook Narration - Fantasy Trilogy',
    'We need a narrator for a popular fantasy book trilogy. Character voices required for diverse cast. Estimated 40 hours of finished audio across 3 books.',
    32000.00,
    CURRENT_TIMESTAMP - INTERVAL '200 days',
    CURRENT_TIMESTAMP - INTERVAL '150 days',
    'COMPLETED',
    CURRENT_TIMESTAMP - INTERVAL '220 days',
    CURRENT_TIMESTAMP - INTERVAL '215 days',
    CURRENT_TIMESTAMP - INTERVAL '150 days',
    'Outstanding work! Sarah brought each character to life. The publisher and author were thrilled.',
    CURRENT_TIMESTAMP - INTERVAL '220 days',
    CURRENT_TIMESTAMP - INTERVAL '150 days'
);

-- 6. Creative Minds Agency + Mike Model - Commercial Photoshoot (ACCEPTED)
INSERT INTO collaboration (
    collaboration_id,
    client_user_id,
    talent_profile_id,
    title,
    description,
    budget,
    start_date,
    end_date,
    status,
    proposed_at,
    responded_at,
    completed_at,
    notes,
    created_at,
    updated_at
) VALUES (
    'b0000006-0000-0000-0000-000000000006',
    'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
    '30000003-0000-0000-0000-000000000003',
    'Fitness Apparel Campaign - Spring Collection',
    'Looking for athletic male model for our client''s spring fitness apparel line. 2-day shoot in Miami. Images will be used for print ads and e-commerce.',
    8500.00,
    CURRENT_TIMESTAMP + INTERVAL '15 days',
    CURRENT_TIMESTAMP + INTERVAL '17 days',
    'ACCEPTED',
    CURRENT_TIMESTAMP - INTERVAL '5 days',
    CURRENT_TIMESTAMP - INTERVAL '3 days',
    NULL,
    'Mike accepted! Finalizing shoot location and wardrobe details.',
    CURRENT_TIMESTAMP - INTERVAL '5 days',
    CURRENT_TIMESTAMP - INTERVAL '3 days'
);

-- 7. Creative Minds Agency + David Host - Podcast Series (IN_PROGRESS)
INSERT INTO collaboration (
    collaboration_id,
    client_user_id,
    talent_profile_id,
    title,
    description,
    budget,
    start_date,
    end_date,
    status,
    proposed_at,
    responded_at,
    completed_at,
    notes,
    created_at,
    updated_at
) VALUES (
    'b0000007-0000-0000-0000-000000000007',
    'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
    '50000005-0000-0000-0000-000000000005',
    'Host - "Creative Conversations" Podcast',
    'Weekly podcast interviewing creative professionals (actors, artists, musicians). 24 episodes, remote recording. David will host and conduct interviews.',
    18000.00,
    CURRENT_TIMESTAMP - INTERVAL '20 days',
    CURRENT_TIMESTAMP + INTERVAL '160 days',
    'IN_PROGRESS',
    CURRENT_TIMESTAMP - INTERVAL '35 days',
    CURRENT_TIMESTAMP - INTERVAL '32 days',
    NULL,
    'Recording 2 episodes per week. David is doing a fantastic job with guest interviews.',
    CURRENT_TIMESTAMP - INTERVAL '35 days',
    CURRENT_TIMESTAMP - INTERVAL '2 days'
);

-- 8. Creative Minds Agency + John Actor - Commercial VO (PROPOSED)
INSERT INTO collaboration (
    collaboration_id,
    client_user_id,
    talent_profile_id,
    title,
    description,
    budget,
    start_date,
    end_date,
    status,
    proposed_at,
    responded_at,
    completed_at,
    notes,
    created_at,
    updated_at
) VALUES (
    'b0000008-0000-0000-0000-000000000008',
    'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
    '10000001-0000-0000-0000-000000000001',
    'Voice Over - Automotive Commercial',
    'Need strong, authoritative male voice for luxury car commercial. 30-second spot, national broadcast. Quick turnaround - script ready.',
    5500.00,
    CURRENT_TIMESTAMP + INTERVAL '7 days',
    CURRENT_TIMESTAMP + INTERVAL '7 days',
    'PROPOSED',
    CURRENT_TIMESTAMP - INTERVAL '2 days',
    NULL,
    NULL,
    'Awaiting John''s response. Client wants to move quickly on this.',
    CURRENT_TIMESTAMP - INTERVAL '2 days',
    CURRENT_TIMESTAMP - INTERVAL '2 days'
);

-- 9. Brand Studios + Emma Singer - Music Licensing (IN_PROGRESS)
INSERT INTO collaboration (
    collaboration_id,
    client_user_id,
    talent_profile_id,
    title,
    description,
    budget,
    start_date,
    end_date,
    status,
    proposed_at,
    responded_at,
    completed_at,
    notes,
    created_at,
    updated_at
) VALUES (
    'b0000009-0000-0000-0000-000000000009',
    'cccccccc-cccc-cccc-cccc-cccccccccccc',
    '40000004-0000-0000-0000-000000000004',
    'Music Licensing - Summer Beverage Campaign',
    'Want to license "Summer Nights" for major beverage brand campaign. 6-month usage across digital platforms (social media, streaming ads, website). National campaign.',
    65000.00,
    CURRENT_TIMESTAMP - INTERVAL '10 days',
    CURRENT_TIMESTAMP + INTERVAL '180 days',
    'IN_PROGRESS',
    CURRENT_TIMESTAMP - INTERVAL '25 days',
    CURRENT_TIMESTAMP - INTERVAL '22 days',
    NULL,
    'Finalizing licensing agreement and usage rights. Campaign launches next month.',
    CURRENT_TIMESTAMP - INTERVAL '25 days',
    CURRENT_TIMESTAMP - INTERVAL '3 days'
);

-- 10. Brand Studios + Mike Model - Fitness Campaign (COMPLETED)
INSERT INTO collaboration (
    collaboration_id,
    client_user_id,
    talent_profile_id,
    title,
    description,
    budget,
    start_date,
    end_date,
    status,
    proposed_at,
    responded_at,
    completed_at,
    notes,
    created_at,
    updated_at
) VALUES (
    'b0000010-0000-0000-0000-000000000010',
    'cccccccc-cccc-cccc-cccc-cccccccccccc',
    '30000003-0000-0000-0000-000000000003',
    'Brand Ambassador - Fitness Supplements',
    'Looking for fitness model to be brand ambassador for new supplement line. Includes photoshoot, social media posts, and appearance at product launch event.',
    35000.00,
    CURRENT_TIMESTAMP - INTERVAL '180 days',
    CURRENT_TIMESTAMP - INTERVAL '90 days',
    'COMPLETED',
    CURRENT_TIMESTAMP - INTERVAL '210 days',
    CURRENT_TIMESTAMP - INTERVAL '205 days',
    CURRENT_TIMESTAMP - INTERVAL '90 days',
    'Mike was fantastic! Great work ethic and strong social media engagement. Product sales exceeded expectations.',
    CURRENT_TIMESTAMP - INTERVAL '210 days',
    CURRENT_TIMESTAMP - INTERVAL '90 days'
);

-- 11. Brand Studios + John Actor - Commercial VO (COMPLETED)
INSERT INTO collaboration (
    collaboration_id,
    client_user_id,
    talent_profile_id,
    title,
    description,
    budget,
    start_date,
    end_date,
    status,
    proposed_at,
    responded_at,
    completed_at,
    notes,
    created_at,
    updated_at
) VALUES (
    'b0000011-0000-0000-0000-000000000011',
    'cccccccc-cccc-cccc-cccc-cccccccccccc',
    '10000001-0000-0000-0000-000000000001',
    'Voice Over - Tech Brand Campaign',
    'Series of voice overs for tech company product launches. 5 different scripts for various products. Professional, trustworthy tone required.',
    12000.00,
    CURRENT_TIMESTAMP - INTERVAL '60 days',
    CURRENT_TIMESTAMP - INTERVAL '45 days',
    'COMPLETED',
    CURRENT_TIMESTAMP - INTERVAL '75 days',
    CURRENT_TIMESTAMP - INTERVAL '72 days',
    CURRENT_TIMESTAMP - INTERVAL '45 days',
    'Perfect delivery on all 5 scripts. Client loved John''s voice and wants to work with him again.',
    CURRENT_TIMESTAMP - INTERVAL '75 days',
    CURRENT_TIMESTAMP - INTERVAL '45 days'
);

-- 12. Brand Studios + David Host - Live Event Hosting (ACCEPTED)
INSERT INTO collaboration (
    collaboration_id,
    client_user_id,
    talent_profile_id,
    title,
    description,
    budget,
    start_date,
    end_date,
    status,
    proposed_at,
    responded_at,
    completed_at,
    notes,
    created_at,
    updated_at
) VALUES (
    'b0000012-0000-0000-0000-000000000012',
    'cccccccc-cccc-cccc-cccc-cccccccccccc',
    '50000005-0000-0000-0000-000000000005',
    'Event Host - Annual Tech Conference',
    'Need experienced host for 2-day technology conference in San Francisco. 1,000+ attendees. Responsibilities include keynote introductions, panel moderation, and award ceremony hosting.',
    15000.00,
    CURRENT_TIMESTAMP + INTERVAL '45 days',
    CURRENT_TIMESTAMP + INTERVAL '47 days',
    'ACCEPTED',
    CURRENT_TIMESTAMP - INTERVAL '12 days',
    CURRENT_TIMESTAMP - INTERVAL '9 days',
    NULL,
    'David confirmed! Sending event schedule and speaker bios for preparation.',
    CURRENT_TIMESTAMP - INTERVAL '12 days',
    CURRENT_TIMESTAMP - INTERVAL '9 days'
);

-- 13. Brand Studios + Sarah Voice - Commercial Campaign (CANCELED)
INSERT INTO collaboration (
    collaboration_id,
    client_user_id,
    talent_profile_id,
    title,
    description,
    budget,
    start_date,
    end_date,
    status,
    proposed_at,
    responded_at,
    completed_at,
    notes,
    created_at,
    updated_at
) VALUES (
    'b0000013-0000-0000-0000-000000000013',
    'cccccccc-cccc-cccc-cccc-cccccccccccc',
    '20000002-0000-0000-0000-000000000002',
    'Voice Over - Retail Chain Campaign',
    'Voice over work for national retail chain. Series of radio and streaming audio ads. Friendly, approachable female voice needed.',
    8000.00,
    CURRENT_TIMESTAMP - INTERVAL '50 days',
    CURRENT_TIMESTAMP - INTERVAL '40 days',
    'CANCELED',
    CURRENT_TIMESTAMP - INTERVAL '65 days',
    CURRENT_TIMESTAMP - INTERVAL '62 days',
    NULL,
    'Client decided to go in a different creative direction. Canceled before recording sessions began.',
    CURRENT_TIMESTAMP - INTERVAL '65 days',
    CURRENT_TIMESTAMP - INTERVAL '55 days'
);

-- =============================================================================
-- SUMMARY OF COLLABORATION DATA
-- =============================================================================
-- Total Collaborations: 13
--
-- By Status:
--   COMPLETED: 5 (can receive ratings)
--   IN_PROGRESS: 4 (active projects)
--   ACCEPTED: 3 (scheduled to start)
--   PROPOSED: 1 (awaiting talent response)
--   DECLINED: 1 (talent unavailable)
--   CANCELED: 1 (client canceled)
--
-- By Client:
--   ABC Productions: 4 collaborations
--   Creative Minds Agency: 5 collaborations
--   Brand Studios Inc: 4 collaborations
--
-- By Talent:
--   John Actor: 3 collaborations
--   Sarah Voice: 4 collaborations
--   Mike Model: 2 collaborations
--   Emma Singer: 2 collaborations
--   David Host: 3 collaborations
--
-- Budget Range: $5,500 - $120,000
-- Total Contract Value: $508,000
-- =============================================================================
