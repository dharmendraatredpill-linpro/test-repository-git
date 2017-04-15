1. Exportera data från DM och lägg på filtjänst som syns från linux
2. Exportera från databasen till DM_specifik_metadata2.xslx
3. I DM_specifik_metadata2.xslx ersätt alla "-" och alla "NULL" med en tomsträng i xlsx filen och exportera sedan som CSV
4. Konvertera log och csv filerna till UTF-8
recode -vf txt..UTF-8 Export.log 
recode -vf txt..UTF-8 Security.log 
recode -vf txt..UTF-8 DM_specifik_metadata2.csv 
5. Kör migreringskriptet
input path: där exporten ligger (linux path)
output path: där alfresco ska importera från (linux path)
strip this prefix from export path: den katalog på windows man exporterade till innan man fyttade till filtjänst typiskt: "D:\\DMExport\\EXPORT\\"
type mapping file: hela linux sökvägen till mappnings filen doc_type_mapping.csv (inkl filnamn)
<sökväg till...>/migrate.py <input path> <output path> <strip this prefix from export path> <type mapping file>
OBS migrerings scriptet kopierar inte in alla dokument i alfresco strukturen utan skapar endast symlänkar för att slippa skapa en kopia till av alla data.
6. Importera i alfresco
