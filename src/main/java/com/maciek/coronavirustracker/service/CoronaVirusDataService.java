package com.maciek.coronavirustracker.service;

import com.maciek.coronavirustracker.model.LocationStatsModel;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CoronaVirusDataService {

    private static final String VIRUS_DATA_URL = "https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_covid19_confirmed_global.csv";
    private static final String VIRUS_DATA_URL_RECOVERED = "https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_19-covid-Recovered.csv";
    private static final String VIRUS_DATA_URL_DEATHS = "https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_covid19_deaths_global.csv";
    private List<LocationStatsModel> allStats = new ArrayList<>();
    private Map<String, Integer> countryStats = new HashMap<>();

    @PostConstruct
    @Scheduled(fixedRate = 3600000) //run method every hour
    public void fetchVirusData() throws IOException, InterruptedException {
        List<LocationStatsModel> newStats = new ArrayList<>();
        HttpResponse<String> confirmedCasesResponse = getHttpResponse(VIRUS_DATA_URL);

        //Apache Commons CSV converter
        StringReader csvBodyReader = new StringReader(confirmedCasesResponse.body());
        Iterable<CSVRecord> records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(csvBodyReader);
        int tempId = 0;
        for (CSVRecord record : records) {
                LocationStatsModel locationStat = new LocationStatsModel();
                locationStat.setId(tempId++);
                locationStat.setState(record.get("Province/State"));
                locationStat.setCountry(record.get("Country/Region"));
                int latestCases;
                int prevDayCases;
                try {
                    latestCases = Integer.parseInt(record.get(record.size() - 1));
                    prevDayCases = Integer.parseInt(record.get(record.size() - 2));
                }catch (NumberFormatException e){
                    latestCases = 0;
                    prevDayCases = 0;
                }
                locationStat.setLatestCases(latestCases);
                locationStat.setDiffFromPreviousDay(latestCases - prevDayCases);
                newStats.add(locationStat);
        }
        this.allStats = newStats;
        this.countryStats = showCasesForEachCountry(newStats);
        fetchDeaths();
//        fetchRecovered();
    }

    public HttpResponse<String> getHttpResponse(String URL) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(URL))
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private Map<String, Integer> showCasesForEachCountry(List<LocationStatsModel> locationStatsModels) {
        Map<String, Integer> tempMap = locationStatsModels.stream()
                .collect(Collectors.groupingBy(LocationStatsModel::getCountry, Collectors.summingInt(LocationStatsModel::getLatestCases)));
        return tempMap.entrySet().stream()
                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue())) //highest to lowest
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (v1, v2)-> v1, LinkedHashMap::new));
    }

    public void fetchDeaths() throws IOException, InterruptedException {
        HttpResponse<String> deathsResponse = getHttpResponse(VIRUS_DATA_URL_DEATHS);

        StringReader csvBodyReader = new StringReader(deathsResponse.body());
        Iterable<CSVRecord> records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(csvBodyReader);
        int recordId = 0;
        for (CSVRecord record : records) {
            int deathStats;
            try{
                deathStats = Integer.parseInt(record.get(record.size() - 1));
            }catch (NumberFormatException e){
                deathStats = 0;
            }
            this.allStats.get(recordId++).setDeaths(deathStats);
        }
    }

    public void fetchRecovered() throws IOException, InterruptedException {
        HttpResponse<String> recoveredResponse = getHttpResponse(VIRUS_DATA_URL_RECOVERED);

        StringReader csvBodyReader = new StringReader(recoveredResponse.body());
        Iterable<CSVRecord> records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(csvBodyReader);
        int recordId = 0;
        for (CSVRecord record : records) {
            int recoveredStats;
            try{
                recoveredStats = Integer.parseInt(record.get(record.size() - 1));
            }catch (NumberFormatException e){
                recoveredStats = 0;
            }
            this.allStats.get(recordId++).setRecovered(recoveredStats);
        }
    }

    public List<LocationStatsModel> getAllStats() {
        return allStats;
    }

    public Map<String, Integer> getCountryStats() {
        return countryStats;
    }
}
