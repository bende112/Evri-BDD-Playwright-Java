# Evri-BDD-Playwright-Java
to run a scenario
mvn test -Dcucumber.filter.name="Search by city returns EH postcodes for Edinburgh"
#run environment#
mvn test -Denv=PROD   
#run browser#
 mvn test -Dbrowser=firefox 
 #run to download browsers#
 mvn -q -Dplaywright.cli.ignore=true test
