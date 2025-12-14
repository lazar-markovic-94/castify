-- Create extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS citext;

-- app_user table
CREATE TABLE app_user (
    user_id UUID PRIMARY KEY,
    email CITEXT UNIQUE NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('TALENT', 'CLIENT', 'ADMIN')),
    password_hash VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_app_user_email ON app_user(email);
CREATE INDEX idx_app_user_role ON app_user(role);

-- talent_profile table
CREATE TABLE talent_profile (
    talent_profile_id UUID PRIMARY KEY,
    user_id UUID UNIQUE NOT NULL REFERENCES app_user(user_id) ON DELETE CASCADE,
    full_name VARCHAR(255) NOT NULL,
    bio TEXT,
    location_mode VARCHAR(20) NOT NULL CHECK (location_mode IN ('REMOTE', 'SPECIFIC', 'HYBRID')),
    latitude NUMERIC(10, 7),
    longitude NUMERIC(10, 7),
    city VARCHAR(100),
    country VARCHAR(100),
    verification_status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (verification_status IN ('PENDING', 'VERIFIED', 'REJECTED')),
    profile_image_url TEXT,
    can_do_voiceover BOOLEAN NOT NULL DEFAULT FALSE,
    can_do_acting BOOLEAN NOT NULL DEFAULT FALSE,
    can_do_modeling BOOLEAN NOT NULL DEFAULT FALSE,
    can_do_singing BOOLEAN NOT NULL DEFAULT FALSE,
    can_do_dancing BOOLEAN NOT NULL DEFAULT FALSE,
    can_do_hosting BOOLEAN NOT NULL DEFAULT FALSE,
    other_capabilities TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_talent_profile_user_id ON talent_profile(user_id);
CREATE INDEX idx_talent_profile_verification ON talent_profile(verification_status);
CREATE INDEX idx_talent_profile_location ON talent_profile(latitude, longitude) WHERE latitude IS NOT NULL AND longitude IS NOT NULL;

-- talent_language table
CREATE TABLE talent_language (
    id BIGSERIAL PRIMARY KEY,
    talent_profile_id UUID NOT NULL REFERENCES talent_profile(talent_profile_id) ON DELETE CASCADE,
    language_code VARCHAR(10) NOT NULL
);

CREATE INDEX idx_talent_language_profile ON talent_language(talent_profile_id);

-- talent_tag table
CREATE TABLE talent_tag (
    id BIGSERIAL PRIMARY KEY,
    talent_profile_id UUID NOT NULL REFERENCES talent_profile(talent_profile_id) ON DELETE CASCADE,
    tag_name VARCHAR(50) NOT NULL
);

CREATE INDEX idx_talent_tag_profile ON talent_tag(talent_profile_id);

-- talent_social_link table
CREATE TABLE talent_social_link (
    social_link_id UUID PRIMARY KEY,
    talent_profile_id UUID NOT NULL REFERENCES talent_profile(talent_profile_id) ON DELETE CASCADE,
    platform VARCHAR(50) NOT NULL,
    url TEXT NOT NULL,
    follower_count BIGINT
);

CREATE INDEX idx_talent_social_link_profile ON talent_social_link(talent_profile_id);

-- client_org table
CREATE TABLE client_org (
    client_org_id UUID PRIMARY KEY,
    user_id UUID UNIQUE NOT NULL REFERENCES app_user(user_id) ON DELETE CASCADE,
    org_name VARCHAR(255) NOT NULL,
    org_type VARCHAR(50) NOT NULL CHECK (org_type IN ('PRODUCTION_COMPANY', 'AGENCY', 'BRAND', 'INDIVIDUAL')),
    description TEXT,
    website TEXT,
    contact_email VARCHAR(255),
    contact_phone VARCHAR(20),
    address TEXT,
    logo_url TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_client_org_user_id ON client_org(user_id);

-- media_asset table
CREATE TABLE media_asset (
    media_asset_id UUID PRIMARY KEY,
    uploaded_by_user_id UUID NOT NULL REFERENCES app_user(user_id) ON DELETE CASCADE,
    media_type VARCHAR(20) NOT NULL CHECK (media_type IN ('IMAGE', 'VIDEO', 'AUDIO')),
    content_type VARCHAR(20) NOT NULL CHECK (content_type IN ('HEADSHOT', 'PORTFOLIO_ITEM', 'DEMO_REEL', 'VOICE_SAMPLE', 'OTHER')),
    file_url TEXT NOT NULL,
    thumbnail_url TEXT,
    title VARCHAR(255),
    description TEXT,
    duration_seconds INT,
    file_size_bytes BIGINT NOT NULL,
    uploaded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_media_asset_uploader ON media_asset(uploaded_by_user_id);

-- talent_portfolio_item table
CREATE TABLE talent_portfolio_item (
    portfolio_item_id UUID PRIMARY KEY,
    talent_profile_id UUID NOT NULL REFERENCES talent_profile(talent_profile_id) ON DELETE CASCADE,
    media_asset_id UUID NOT NULL REFERENCES media_asset(media_asset_id) ON DELETE CASCADE,
    display_order INT NOT NULL DEFAULT 0,
    added_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_portfolio_item_profile ON talent_portfolio_item(talent_profile_id);
CREATE INDEX idx_portfolio_item_media ON talent_portfolio_item(media_asset_id);

-- conversation table
CREATE TABLE conversation (
    conversation_id UUID PRIMARY KEY,
    talent_user_id UUID NOT NULL REFERENCES app_user(user_id) ON DELETE CASCADE,
    client_user_id UUID NOT NULL REFERENCES app_user(user_id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_message_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (talent_user_id, client_user_id)
);

CREATE INDEX idx_conversation_talent ON conversation(talent_user_id);
CREATE INDEX idx_conversation_client ON conversation(client_user_id);

-- message table
CREATE TABLE message (
    message_id UUID PRIMARY KEY,
    conversation_id UUID NOT NULL REFERENCES conversation(conversation_id) ON DELETE CASCADE,
    sender_user_id UUID NOT NULL REFERENCES app_user(user_id) ON DELETE CASCADE,
    content TEXT NOT NULL,
    sent_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    read_at TIMESTAMP
);

CREATE INDEX idx_message_conversation ON message(conversation_id, sent_at);
CREATE INDEX idx_message_sender ON message(sender_user_id);
