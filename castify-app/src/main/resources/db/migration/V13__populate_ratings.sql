-- V13__populate_ratings.sql
-- Seed rating data for completed collaborations

-- =============================================================================
-- TALENT RATINGS
-- =============================================================================
-- This migration populates ratings for completed collaborations.
-- Only COMPLETED collaborations should have ratings.
--
-- Rating Scale: 1 (Poor) to 5 (Excellent)
-- Rating Categories:
--   - overall_rating: Overall satisfaction with the collaboration
--   - professionalism_rating: Professional conduct and behavior
--   - quality_rating: Quality of work delivered
--   - communication_rating: Communication effectiveness
--   - timeliness_rating: Meeting deadlines and punctuality
-- =============================================================================

-- 1. Rating for: ABC Productions + John Actor - "Urban Justice" (COMPLETED)
-- Collaboration: b0000001-0000-0000-0000-000000000001
INSERT INTO talent_rating (
    rating_id,
    talent_profile_id,
    client_user_id,
    collaboration_id,
    overall_rating,
    professionalism_rating,
    quality_rating,
    communication_rating,
    timeliness_rating,
    review_text,
    would_work_again,
    created_at,
    is_verified
) VALUES (
    '90000001-0000-0000-0000-000000000001',
    '10000001-0000-0000-0000-000000000001', -- John Actor
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', -- ABC Productions
    'b0000001-0000-0000-0000-000000000001', -- Urban Justice collaboration
    5, -- overall
    5, -- professionalism
    5, -- quality
    5, -- communication
    4, -- timeliness
    'John was absolutely phenomenal in this role. His martial arts skills combined with his dramatic range made him perfect for the lead in Urban Justice. He showed up prepared every day, took direction well, and brought creative ideas to his character. The only minor issue was a couple of instances where he arrived 15-20 minutes late to set, but he always notified us in advance. His performance exceeded our expectations and we are confident this film will be a commercial success. Highly recommend and would definitely work with him again!',
    true,
    CURRENT_TIMESTAMP - INTERVAL '25 days',
    true
);

-- 2. Rating for: ABC Productions + Sarah Voice - "Ocean Mysteries" Documentary (COMPLETED)
-- Collaboration: b0000003-0000-0000-0000-000000000003
INSERT INTO talent_rating (
    rating_id,
    talent_profile_id,
    client_user_id,
    collaboration_id,
    overall_rating,
    professionalism_rating,
    quality_rating,
    communication_rating,
    timeliness_rating,
    review_text,
    would_work_again,
    created_at,
    is_verified
) VALUES (
    '90000002-0000-0000-0000-000000000002',
    '20000002-0000-0000-0000-000000000002', -- Sarah Voice
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', -- ABC Productions
    'b0000003-0000-0000-0000-000000000003', -- Ocean Mysteries collaboration
    5, -- overall
    5, -- professionalism
    5, -- quality
    5, -- communication
    5, -- timeliness
    'Sarah delivered outstanding narration for our Ocean Mysteries documentary series. Her voice has the perfect blend of warmth and authority that brought the underwater world to life. She was incredibly professional throughout the entire recording process, always on time, and required minimal retakes. Her ability to adjust her tone and pacing based on our direction was impressive. The final product exceeded our expectations and we have received excellent feedback from test audiences. We will absolutely hire Sarah for future documentary projects.',
    true,
    CURRENT_TIMESTAMP - INTERVAL '115 days',
    true
);

-- 3. Rating for: Creative Minds Agency + Sarah Voice - Fantasy Trilogy Audiobooks (COMPLETED)
-- Collaboration: b0000005-0000-0000-0000-000000000005
INSERT INTO talent_rating (
    rating_id,
    talent_profile_id,
    client_user_id,
    collaboration_id,
    overall_rating,
    professionalism_rating,
    quality_rating,
    communication_rating,
    timeliness_rating,
    review_text,
    would_work_again,
    created_at,
    is_verified
) VALUES (
    '90000003-0000-0000-0000-000000000003',
    '20000002-0000-0000-0000-000000000002', -- Sarah Voice
    'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', -- Creative Minds Agency
    'b0000005-0000-0000-0000-000000000005', -- Fantasy Trilogy collaboration
    5, -- overall
    5, -- professionalism
    5, -- quality
    5, -- communication
    5, -- timeliness
    'Working with Sarah on this fantasy trilogy was an absolute pleasure. She created distinct, memorable voices for over 30 different characters and maintained consistency across all three books. Her dramatic interpretation brought emotional depth to key scenes that had listeners crying and laughing. She met every deadline, was responsive to feedback, and went above and beyond by providing pronunciation guides for the fantasy terms. The author was thrilled with the final audiobooks and specifically requested Sarah for their next series. Five stars across the board!',
    true,
    CURRENT_TIMESTAMP - INTERVAL '145 days',
    true
);

-- 4. Rating for: Brand Studios + Mike Model - Fitness Supplement Campaign (COMPLETED)
-- Collaboration: b0000010-0000-0000-0000-000000000010
INSERT INTO talent_rating (
    rating_id,
    talent_profile_id,
    client_user_id,
    collaboration_id,
    overall_rating,
    professionalism_rating,
    quality_rating,
    communication_rating,
    timeliness_rating,
    review_text,
    would_work_again,
    created_at,
    is_verified
) VALUES (
    '90000004-0000-0000-0000-000000000004',
    '30000003-0000-0000-0000-000000000003', -- Mike Model
    'cccccccc-cccc-cccc-cccc-cccccccccccc', -- Brand Studios
    'b0000010-0000-0000-0000-000000000010', -- Fitness Campaign collaboration
    5, -- overall
    5, -- professionalism
    5, -- quality
    4, -- communication
    5, -- timeliness
    'Mike was fantastic as our brand ambassador for the fitness supplement launch. His physique was exactly what we were looking for and he photographed beautifully. He was professional on set, took direction well, and brought great energy to the photoshoot. His social media posts performed exceptionally well with high engagement rates. The only minor feedback is that sometimes it took him a day or two to respond to emails, but this never caused any delays in the project. The campaign exceeded sales targets by 40%. We would absolutely work with Mike again for future fitness-related campaigns.',
    true,
    CURRENT_TIMESTAMP - INTERVAL '85 days',
    true
);

-- 5. Rating for: Brand Studios + John Actor - Tech Brand Voice Over (COMPLETED)
-- Collaboration: b0000011-0000-0000-0000-000000000011
INSERT INTO talent_rating (
    rating_id,
    talent_profile_id,
    client_user_id,
    collaboration_id,
    overall_rating,
    professionalism_rating,
    quality_rating,
    communication_rating,
    timeliness_rating,
    review_text,
    would_work_again,
    created_at,
    is_verified
) VALUES (
    '90000005-0000-0000-0000-000000000005',
    '10000001-0000-0000-0000-000000000001', -- John Actor
    'cccccccc-cccc-cccc-cccc-cccccccccccc', -- Brand Studios
    'b0000011-0000-0000-0000-000000000011', -- Tech Brand VO collaboration
    4, -- overall
    5, -- professionalism
    4, -- quality
    5, -- communication
    4, -- timeliness
    'John did a solid job on our tech brand voice over campaign. His voice has the professional, trustworthy quality we were looking for. He was very responsive to communication and easy to work with throughout the project. The recordings were good quality, though a couple of scripts required 2-3 takes to get the technical terminology pronunciation just right. He delivered all files on time, though one batch came in about 6 hours before the deadline which was a bit tight for our production schedule. Overall a good experience and we would consider working with John again for future VO projects.',
    true,
    CURRENT_TIMESTAMP - INTERVAL '42 days',
    true
);

-- =============================================================================
-- SUMMARY OF RATING DATA
-- =============================================================================
-- Total Ratings: 5
--
-- By Talent:
--   John Actor: 2 ratings (avg: 4.5)
--   Sarah Voice: 3 ratings (avg: 5.0)
--   Mike Model: 1 rating (avg: 5.0)
--
-- Rating Distribution:
--   5 stars: 4 ratings
--   4 stars: 1 rating
--
-- Would Work Again: 5/5 (100%)
--
-- All ratings are verified (is_verified = true)
--
-- Note: Ratings are only created for COMPLETED collaborations.
-- The talent_statistics table will be automatically updated by triggers
-- based on these ratings and collaboration data.
-- =============================================================================
