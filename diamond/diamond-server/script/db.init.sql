create database diamond default charset utf8;

grant select,insert,update,delete on diamond.* to diamond@'%' identified by 'diamond';
use diamond ;

create table config_info (
id bigint(64) unsigned NOT NULL auto_increment,
data_id varchar(255) NOT NULL default ' ',
group_id varchar(128) NOT NULL default ' ',
content longtext NOT NULL,
md5 varchar(32) NOT NULL default ' ' ,
gmt_create datetime NOT NULL default '2010-05-05 00:00:00',
gmt_modified datetime NOT NULL default '2010-05-05 00:00:00',
PRIMARY KEY  (id),
UNIQUE KEY uk_config_datagroup (data_id,group_id)
);