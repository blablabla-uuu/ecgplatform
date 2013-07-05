drop table if exists user;

create table user (
	id bigint generated by default as identity,
	mobile varchar(20) not null unique,
	id_card varchar(20),
	username varchar(20),
	name varchar(20) not null,
	password varchar(100) not null,
	roles varchar(100),
    gender int not null,
	type varchar(10),
	birthday timestamp,
	address varchar(255),
	stature double,
	weight  double,
	city     varchar(20),
	em_contact1 varchar(20),
	em_contact1_tel varchar(20),
	em_contact2 varchar(20),
	em_contact2_tel varchar(20),
	bad_habits varchar(30),
	anamnesis varchar(30),
	created_date timestamp,
	last_updated timestamp,
	remark varchar(255),
	title varchar(20),
	is_free int,
	version int,
	primary key (id)
);

drop table if exists employee;

create table employee (
    id bigint generated by default as identity,
    type varchar(20),
    name varchar(20),
    username varchar(20) not null unique,
    password varchar(100),
    roles varchar(100),
    status varchar(10) not null,
    enabled int not null,
    dismissed int not null,
    gender int not null,
    expire timestamp,
    birthday timestamp,
    id_card varchar(20),
    mobile varchar(11),
    title varchar(20),
    company varchar(100),
    created_date timestamp,
    last_updated timestamp,
    email varchar(64),
    version int,
    primary key(id)
);

drop table if exists expert_operator;

create table expert_operator (
    expert_id bigint not null ,
    operator_id bigint not null
);

drop table if exists health_rule;

create table health_rule (
    id bigint generated by default as identity,
    name varchar(20),
    code varchar(20),
    usage varchar(10),
    can_reply number(1),
    type varchar(30),
    user_id bigint,
    unit varchar(10),
    level varchar(10),
    min float,
    max float,
    created_date timestamp,
    last_updated timestamp,
    remark varchar(255),
    version int,
    primary key(id)
);

drop table if exists health_rule_reply;

create table health_rule_reply (
    id bigint generated by default as identity,
    title varchar(100) not null,
    content varchar(1000),
    result varchar(20),
    rule_id bigint ,
    created_date timestamp,
    last_updated timestamp,
    version int,
    primary key(id)
);

