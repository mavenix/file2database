file2database
=============

a very little programm to import file in database in java 

generate:
=========
mvn package assembly:single

run:
====
java -Xmx1024m -jar file2database-0.1-jar-with-dependencies.jar mydb

table:
======

CREATE TABLE `comptes` (                                                                                                           
       `id` int(11) unsigned NOT NULL AUTO_INCREMENT,                                                                                    
       `Numero` varchar(11) COLLATE utf8_unicode_ci DEFAULT NULL,                                                                       
       `DateFermeture` datetime DEFAULT NULL,                                                                                            
       PRIMARY KEY (`id`),                                                                                                              
       UNIQUE KEY `IDX_Numero` (`Numero`)                                                                                                  
    ) ENGINE = MYISAM   ;


