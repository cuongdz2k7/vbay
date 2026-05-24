CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    phone_number VARCHAR(20),
    position VARCHAR(20) NOT NULL DEFAULT 'USER',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    available_balance DECIMAL(15,2) NOT NULL DEFAULT 0,
    hold_balance DECIMAL(15,2) NOT NULL DEFAULT 0,
    version BIGINT NOT NULL DEFAULT 0,
    time_init TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS products (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    seller_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    category_id VARCHAR(100) NOT NULL,
    product_condition VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT fk_products_seller
        FOREIGN KEY (seller_id) REFERENCES users(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS product_images (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_id BIGINT NOT NULL,
    image_url VARCHAR(255) NOT NULL,
    is_thumbnail BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT fk_product_images_product
        FOREIGN KEY (product_id) REFERENCES products(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
);


CREATE TABLE IF NOT EXISTS auctions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_id BIGINT NOT NULL,
    seller_id BIGINT NOT NULL,
    title TEXT NOT NULL,
    description TEXT,
    minimum_bid_step DECIMAL(15,2) NOT NULL,
    starting_price DECIMAL(15,2) NOT NULL,
    current_price DECIMAL(15,2) NOT NULL,
    reserve_price DECIMAL(15,2),
    buy_now_price DECIMAL(15,2),
    final_price DECIMAL(15,2),
    starting_time TIMESTAMP NOT NULL,
    ending_time TIMESTAMP NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED',
    winner_user_id BIGINT NULL,
    anti_snipe_extension_count INT NOT NULL DEFAULT 0,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_auctions_product
        FOREIGN KEY (product_id) REFERENCES products(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,
    CONSTRAINT fk_auctions_seller
        FOREIGN KEY (seller_id) REFERENCES users(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,
    CONSTRAINT fk_auctions_winner
        FOREIGN KEY (winner_user_id) REFERENCES users(id)
        ON DELETE SET NULL
        ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS bids (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    auction_id BIGINT NOT NULL,
    bidder_id BIGINT NOT NULL,
    bid_amount DECIMAL(15,2) NOT NULL,
    bid_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    bid_source VARCHAR(20) NOT NULL DEFAULT 'USER_BID',
    status VARCHAR(20) NOT NULL,

    CONSTRAINT fk_bids_auction
        FOREIGN KEY (auction_id) REFERENCES auctions(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT fk_bids_bidder
        FOREIGN KEY (bidder_id) REFERENCES users(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS autobids (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    auction_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    max_bid_amount DECIMAL(15,2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_autobids_auction
        FOREIGN KEY (auction_id) REFERENCES auctions(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT fk_autobids_user
        FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT uq_autobids_auction_user UNIQUE (auction_id, user_id)
);

CREATE TABLE IF NOT EXISTS payments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,

    auction_id BIGINT NOT NULL,
    buyer_id BIGINT NOT NULL,
    seller_id BIGINT NOT NULL,
    winning_bid_id BIGINT NULL,

    amount DECIMAL(15,2) NOT NULL,
    type VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL,

    held_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    released_at TIMESTAMP NULL,
    refunded_at TIMESTAMP NULL,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_payments_auction
        FOREIGN KEY (auction_id) REFERENCES auctions(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT fk_payments_buyer
        FOREIGN KEY (buyer_id) REFERENCES users(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT fk_payments_seller
        FOREIGN KEY (seller_id) REFERENCES users(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT fk_payments_winning_bid
        FOREIGN KEY (winning_bid_id) REFERENCES bids(id)
        ON DELETE SET NULL
        ON UPDATE CASCADE,

    CONSTRAINT uq_payments_auction UNIQUE (auction_id)
);
