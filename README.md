# ruian2pgsql

Converts RÚIAN data to PostgreSQL database.

## What it does exactly?

ruian2pgsql is a command line application. It reads data from RÚIAN XML files
and stores them in PostgreSQL database. It does not keep the history records, it
just keeps the latest state of all the objects. It is possible to import just
one file as well as many files. If one file is imported several times, or in
case content of some files overlaps, ruian2pgsql imports all the objects only
once (they are tracked by their unique id assigned in RÚIAN).

## Prerequisities

* JRE or JDK 7+
* PostgreSQL server with installed PostGIS
* Apache Maven 3+ (just in case you want to compile the application yourself)

## Compilation

You are not required to compile the application yourself, you can download
latest binary from https://github.com/fordfrog/ruian2pgsql/downloads. Anyway,
compilation of ruian2pgsql is easy. Once you install Apache Maven, you just need
to run `mvn package` in the root directory of the sources, where pom.xml file is
located.

## Running

Here is the usage information that ruian2pgsql outputs if run without
parameters:

    Usage: java -jar ruian2pgsql-*-jar-with-dependencies.jar <options>

    Where:
    --convert-to-ewkt
            if enabled, GML strings from the source XML files are first converted to
            EWKT and then stored in database, otherwise original GML strings are
            stored in database (use this option based on the level of GML
            implementation in Postgis version you use)
    --convert-to-linear-ewkt
            if enabled, GML strings from the source XML files are first converted to
            EWKT, curves are approximated by linear geometries and then stored in
            database, otherwise original GML strings are stored in database (choose
            this option based on Postgis level of GML implementation)
    --create-tables
            creates database tables for the data (if tables with the same name
            already exists, it is first dropped)
    --db-connection-url <value>
            is JDBC connection URL, for example:
            jdbc:postgresql://localhost/ruian?user=ruian&password=p4ssw0rd
    --dry-run
            processes all specified files but no data are written to database
    --debug
            if used, debug information are output
    --input-dir <value>
            is directory that contains downloaded .xml.gz files
    --log-file <value>
            is optional specification of log file (if not specified, log will be
            output to console)
    --ignore-invalid-gml
            if GML definition is not valid, application ignores the definition and
            saves the object without the definition (if this parameter is not
            specified, application throws exception and exits while trying to save
            invalid GML definition, the drawback of this parameter is that it makes
            the import little bit slower because each GML definition is checked
            twice - once during the check and the other time during saving in
            database)
    --reset-transaction-ids
            resets RÚIAN transaction ids so that following data import will update
            all data (data are updated only if new transaction id is greater than
            transaction id that is stored in database - this feature is useful in
            case you want to regenerate your data because of some issue with
            previous import, without deleting all data and starting with fresh
            database)

## To do

List of planned features is at [wiki](https://github.com/fordfrog/ruian2pgsql/wiki).

## License

ruian2pgsql is distributed under MIT license.

## Changelog

## Version 1.3.0

* Changed index creation DDL statements to make it compatible with PostgreSQL
  versions lower than 9.0.
* Added command line switch --dry-run for running the application without making
  any modification in database.
* Added command line switch --convert-to-ewkt which converts GML strings to EWKT
  before the geometries are stored in database. This is because the level of GML
  support in Postgis is not adequate to the GML geometries contained in RÚIAN
  files.
* Added command line switch --debug which outputs extra information.

## Version 1.2.0

* Added ruian_stats view that displays statistics of RÚIAN data and its
  geometries.
* When --ignore-invalid-gml is specified, if invalid GML is encountered, it is
  printed to the log.

## Version 1.1.0

* Added workaround for Postgis [bug](http://trac.osgeo.org/postgis/ticket/1928)
  (it does not parse MultiPoint with pointMembers and instead saves empty
  multipoint). The workaround is applied automatically if the target database is
  affected. Original GML is rewritten so that instead of pointMembers element
  pointMember element is used.
* Added command line switch --reset-transaction-ids.

## Version 1.0.0

* first release