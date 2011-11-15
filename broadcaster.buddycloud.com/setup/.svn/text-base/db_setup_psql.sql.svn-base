CREATE TABLE broadcaster.roster (
	jid          character varying PRIMARY KEY,
	entrytype    character varying DEFAULT 'normal',
	CHECK ( entrytype IN ( 'normal', 'temporary', 'blocked')),
	datecreated  timestamp NOT NULL DEFAULT now(),
	lastseen  timestamp NOT NULL DEFAULT now(),
);

ALTER table broadcaster.roster ADD column lastseen timestamp;
UPDATE broadcaster.roster SET lastseen = '1970-01-01 00:00:00';
ALTER table broadcaster.roster alter column lastseen SET NOT NULL;
ALTER table broadcaster.roster alter column lastseen SET DEFAULT now();

CREATE SEQUENCE broadcaster.leafnode_seq;
CREATE TABLE broadcaster.leafnode (
    leafnode_id  integer PRIMARY KEY DEFAULT nextval('broadcaster.leafnode_seq'),
	nodename     character varying NOT NULL UNIQUE,
	title        character varying NOT NULL,
	description  character varying,
	access_model character varying NOT NULL,
	publish_model character varying NOT NULL,
	max_items    integer NOT NULL,
	payload_type character varying NOT NULL,
	default_affiliation character varying DEFAULT 'member',
	datecreated  timestamp NOT NULL default now(),
	avatar_hash  character varying
);

CREATE SEQUENCE broadcaster.subscription_seq;
CREATE TABLE broadcaster.subscription (
    subscription_id integer PRIMARY KEY DEFAULT nextval('broadcaster.subscription_seq'),
	nodename     character varying NOT NULL,
	jid          character varying NOT NULL,
	subscription character varying NOT NULL,
	affiliation  character varying NOT NULL,
	datecreated  timestamp NOT NULL default now()
);
CREATE INDEX subscription_nodename_idx ON broadcaster.subscription (nodename);
CREATE INDEX subscription_jid_idx ON broadcaster.subscription (jid);
CREATE INDEX subscription_subscription_idx ON broadcaster.subscription (subscription);
CREATE INDEX subscription_affiliation_idx ON broadcaster.subscription (affiliation);

CREATE SEQUENCE broadcaster.item_db_seq;
CREATE TABLE broadcaster.item (
    item_dbid    integer PRIMARY KEY DEFAULT nextval('broadcaster.item_db_seq'),
	nodename     character varying NOT NULL,
	id           bigint NOT NULL,
	payload      character varying NOT NULL,
	datecreated  timestamp NOT NULL default now()
);
CREATE INDEX item_nodename_idx ON broadcaster.item (nodename);
CREATE INDEX item_id_idx ON broadcaster.item (id);
CREATE INDEX item_datecreated_idx ON broadcaster.item (datecreated);

CREATE SEQUENCE broadcaster.offline_storage_db_seq;
CREATE TABLE broadcaster.offline_storage (
	id          integer PRIMARY KEY DEFAULT nextval('broadcaster.offline_storage_db_seq'),
	jid         character varying NOT NULL,
	payload     character varying NOT NULL,
	datecreated timestamp NOT NULL default now()
);
CREATE INDEX offline_storage_jid_idx ON broadcaster.offline_storage (jid);

CREATE SEQUENCE broadcaster.pubsubhubbubsubscription_seq;
CREATE TABLE broadcaster.pubsubhubbubsubscription (
    subscription_id integer PRIMARY KEY DEFAULT nextval('broadcaster.pubsubhubbubsubscription_seq'),
	nodename     character varying NOT NULL,
	callback     character varying NOT NULL,
	topic        character varying NOT NULL,
	verify_token character varying NOT NULL,
	lease_seconds character varying NOT NULL,
	secret       character varying NOT NULL,
	goes_old      bigint NOT NULL,
	datecreated  timestamp NOT NULL default now()
);
CREATE INDEX pubsubhubbubsubscription_nodename_idx ON broadcaster.pubsubhubbubsubscription (nodename);
CREATE INDEX pubsubhubbubsubscription_callback_idx ON broadcaster.pubsubhubbubsubscription (callback);
CREATE INDEX pubsubhubbubsubscription_goes_old_idx ON broadcaster.pubsubhubbubsubscription (goes_old);
