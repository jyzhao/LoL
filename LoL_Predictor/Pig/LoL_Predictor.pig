--1 declare a variable for dataset path
%declare DATASETPATH '/Users/zhaojiyuan/Dropbox/NEU/INFO7374/LoL_Predictor';

--2 load ratings file
champion = LOAD '$DATASETPATH/result-sample.txt' using PigStorage (',')
AS (champion_name:chararray, kills:int, deaths:int, assists:int, result:chararray);

champion = FOREACH champion GENERATE champion_name, kills, deaths, assists, (result == 'v' ? 1 : 0) AS victory, (result == 'd' ? 1 : 0) AS defeated, result;

--4 group the ratings by movie_id
groupdChampion = GROUP champion BY champion_name;

final_data = FOREACH groupdChampion GENERATE group AS champion_name, ROUND(AVG(champion.kills)*100.00)/100.00 AS avgKills, ROUND(AVG(champion.deaths)*100.00)/100.00 AS avgDeaths, ROUND(AVG(champion.assists)*100.00)/100.00 AS avgAssits, SUM(champion.victory) AS sumVictory, SUM(champion.defeated) AS sumDefeated, ROUND(SUM(champion.victory)*10000.00/COUNT(champion.result))/100.00 AS rateVictory, ROUND(SUM(champion.defeated)*10000.00/COUNT(champion.result))/100.00 AS rateDefeated;

final_data = ORDER final_data BY rateVictory DESC;

--STORE final_data INTO 'mr_result';
STORE final_data INTO 'mr_result' using PigStorage(',');