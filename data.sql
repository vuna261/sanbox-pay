create table pay_code
(
    id             int auto_increment
        primary key,
    transaction_id varchar(255) null,
    pay_code       varchar(255) null,
    used           tinyint(1)   null,
    created_date   timestamp    null
)
    collate = utf8mb4_unicode_ci;

create table price
(
    id           int auto_increment
        primary key,
    name         varchar(255) null,
    account_type varchar(255) null,
    amount       int          null,
    created_date timestamp    null,
    created_by   varchar(255) null,
    updated_date timestamp    null,
    updated_by   varchar(255) null,
    price        int          null,
    description  varchar(255) null
)
    collate = utf8mb4_unicode_ci;

create table promo
(
    id             int auto_increment
        primary key,
    name           varchar(255) null,
    description    mediumtext   null,
    promote_code   varchar(255) null,
    available_date timestamp    null,
    expired_date   timestamp    null,
    total_code     int          null,
    promote_type   varchar(255) null,
    amount         int          null,
    active         tinyint(1)   null
)
    collate = utf8mb4_unicode_ci;

create table promo_history
(
    campaign_id    int          not null,
    transaction_id varchar(255) not null,
    apply_date     timestamp    null,
    apply          tinyint(1)   null,
    primary key (campaign_id, transaction_id)
)
    collate = utf8mb4_unicode_ci;

create table recharge
(
    transaction_id varchar(255)             not null,
    user_id        int                      null,
    payment_method varchar(255)             null,
    description    varchar(255) default '0' null,
    email          varchar(255)             null,
    bill_status    varchar(255)             null,
    amount         int                      null,
    price_id       int                      null,
    updated_date   timestamp                null,
    created_date   timestamp                null,
    constraint recharge_transaction_id_uindex
        unique (transaction_id)
)
    collate = utf8mb4_unicode_ci;

alter table recharge
    add primary key (transaction_id);

create table transaction_history
(
    user_id            int                                   not null,
    wallet_id          int                                   not null,
    amount             decimal(10, 2)                        null,
    method             varchar(255)                          null,
    transaction_date   timestamp default current_timestamp() not null,
    transaction_status varchar(255)                          null,
    detail             varchar(255)                          null,
    email              varchar(255)                          null,
    primary key (user_id, wallet_id, transaction_date)
)
    collate = utf8mb4_unicode_ci;

create table wallet
(
    id             int auto_increment
        primary key,
    user_id        int            not null,
    amount         decimal(10, 2) null,
    wallet_type    varchar(255)   null,
    available_date timestamp      null
)
    collate = utf8mb4_unicode_ci;

