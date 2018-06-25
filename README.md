# Transaction Parser

This is a parser for rates and transaction files.  It uses spring-batch for importing the data files to mysql.  
It uses Commons CSV for generating the results file. 

## Build

Run `mvn clean package` to build the project. The build artifacts will be stored in the `target/` directory.

## Setup

This project uses mysql as data store.  Run `resources/createSchema.sql` to create the `test` database.

`application.yml` contains the db configuration and the following folders: 

- `load.directory` specifies the load folder

- `archive.directory` specifies the archive folder

- `results.directory` specifies the results folder

## Run

Run the following in the command prompt:

`java -jar target/txnparser-0.0.1-SNAPSHOT.jar`

This will monitor the load folder for a new rates or transaction files.  Once loaded, the file will be moved to the 
archive folder, then performs amount conversion, and stores the calculations in the results folder.

The results file will be of the following format: `result_{date}.csv`