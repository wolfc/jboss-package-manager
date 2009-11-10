-- Package Manager table
-- DROP TABLE package_manager;
CREATE TABLE package_manager (
 	id bigint not null GENERATED ALWAYS AS IDENTITY	CONSTRAINT PACKAGE_MANAGER_PK PRIMARY KEY,
 	jbossHome varchar(255) not null
);
-- add a unique constraint to server home
ALTER TABLE package_manager 
ADD CONSTRAINT jboss_server_home_uniqueness UNIQUE (jbossHome);

-- Package table
-- DROP TABLE package;
CREATE TABLE package (
	package_manager_id bigint not null,
	name varchar(255) not null CONSTRAINT PACKAGE_PK PRIMARY KEY, 
	version varchar(255) not null, 
	foreign key (package_manager_id) references package_manager (id) 

);

-- Installation file
-- DROP TABLE installation_file;
CREATE TABLE installation_file (
	id bigint not null GENERATED ALWAYS AS IDENTITY	CONSTRAINT INSTALLATION_FILE_PK PRIMARY KEY,
	package_name varchar(255) not null,
	fileName varchar(255) not null,
	installedPath varchar(255) not null,
	fileType varchar(255),
	foreign key (package_name) references package (name) 

);

-- Script file
-- DROP TABLE installation_file;
CREATE TABLE script (
	id bigint not null GENERATED ALWAYS AS IDENTITY	CONSTRAINT SCRIPT_PK PRIMARY KEY,
	package_name varchar(255) not null,
	name varchar(255) not null,
	path varchar(255) not null,
	scriptType varchar(255) not null,
	foreign key (package_name) references package (name) 

);

-- Package dependencies
-- DROP TABLE package_dependency;
CREATE TABLE package_dependency (
	id bigint not null GENERATED ALWAYS AS IDENTITY	CONSTRAINT PACKAGE_DEPENDENCY_PK PRIMARY KEY,
	dependent_package varchar(255) not null,
	dependee_package varchar(255) not null,
	foreign key (dependee_package) references package (name),
	foreign key (dependent_package) references package(name)
	
);