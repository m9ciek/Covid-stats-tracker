package com.maciek.coronavirustracker.service;

import com.maciek.coronavirustracker.model.LocationStatsModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DetailedCoronaVirusStatsService {

    private CoronaVirusDataService coronaVirusDataService;

    @Autowired
    public DetailedCoronaVirusStatsService(CoronaVirusDataService coronaVirusDataService) {
        this.coronaVirusDataService = coronaVirusDataService;
    }

    public List<LocationStatsModel> showStatsForSearchedCountry(String country){
        List<LocationStatsModel> locationStatsModelList = coronaVirusDataService.getAllStats()
                .stream()
                .filter(element-> element.getCountry().toUpperCase().equals(country.toUpperCase()))
                .collect(Collectors.toList());
        return locationStatsModelList;
    }
}
