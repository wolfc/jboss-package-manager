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
	id bigint not null GENERATED ALWAYS AS IDENTITY	CONSTRAINT PACKAGE_PK PRIMARY KEY,
	package_manager_id bigint not null,
	name varchar(255) not null, 
	version varchar(255) not null, 
	foreign key (package_manager_id) references package_manager (id) 

);

-- add a unique constraint to package name,version
ALTER TABLE package 
ADD CONSTRAINT package_name_version UNIQUE (name, version);


-- Installation file
-- DROP TABLE installation_file;
CREATE TABLE installation_file (
	id bigint not null GENERATED ALWAYS AS IDENTITY	CONSTRAINT INSTALLATION_FILE_PK PRIMARY KEY,
	package_id bigint not null,
	fileName varchar(255) not null,
	installedPath varchar(255) not null,
	fileType varchar(255),
	foreign key (package_id) references package (id) 

);

-- Package dependencies
-- DROP TABLE package_dependency;
CREATE TABLE package_dependency (
	id bigint not null GENERATED ALWAYS AS IDENTITY	CONSTRAINT PACKAGE_DEPENDENCY_PK PRIMARY KEY,
	dependent_package_id bigint not null,
	dependee_package_id bigint not null,
	--primary key (dependent_package_id, dependee_package_id),
	foreign key (dependee_package_id) references package (id),
	foreign key (dependent_package_id) references package(id)
	
);