
DROP DATABASE IF EXISTS Reservas;

CREATE DATABASE Reservas CHARACTER SET utf8mb4;

use Reservas;

create table usuario (
       id  int  not null,
       nombre varchar(50) not null,
       username varchar(30) not null,
       password varchar(30) not null,
       rol varchar(15) not null,
       telefono varchar(20),
       Primary Key (id),
       Unique (username)
     ) ENGINE=InnoDB;

create table categoria (
       id  int  not null AUTO_INCREMENT,
       nombre varchar(50),
       descripcion varchar(100) not null,
       Primary Key (id)
     ) ENGINE=InnoDB;

insert into usuario (id, nombre, username, password, rol, telefono) values(1,'Administrador','admin','admin','ADMIN',null);

insert into usuario (id, nombre, username, password, rol, telefono) values(1234,'1234','1234','1234','FUNCIONARIO','1234');
insert into usuario (id, nombre, username, password, rol, telefono) values(4321,'4321','4321','4321','FUNCIONARIO','4321');
insert into usuario (id, nombre, username, password, rol, telefono) values(117870312,'pope','pope','117870312','FUNCIONARIO','222222');
insert into usuario (id, nombre, username, password, rol, telefono) values(2303,'Kevin Rivera','vinke23','2303','FUNCIONARIO','70574037');

insert into categoria (id, nombre, descripcion) values(1,'','Elementos Electronicos');
insert into categoria (id, nombre, descripcion) values(2,'','Salon Comun');
insert into categoria (id, nombre, descripcion) values(3,'','Elementos Varios');
