package com.maciek.coronavirustracker.controller;

import com.maciek.coronavirustracker.service.CoronaVirusDataService;
import com.maciek.coronavirustracker.service.DetailedCoronaVirusStatsService;
import com.maciek.coronavirustracker.model.LocationStatsModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.util.StringUtils;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Controller
public class HomeController {

    private CoronaVirusDataService coronaVirusDataService;
    private DetailedCoronaVirusStatsService detailedCoronaVirusStatsService;

    @Autowired
    public HomeController(CoronaVirusDataService coronaVirusDataService, DetailedCoronaVirusStatsService detailedCoronaVirusStatsService) {
        this.coronaVirusDataService = coronaVirusDataService;
        this.detailedCoronaVirusStatsService = detailedCoronaVirusStatsService;
    }

    @ModelAttribute
    public void populateModel(Model model){
        List<LocationStatsModel> allStats = coronaVirusDataService.getAllStats();
        allStats.sort(Comparator.comparingInt(LocationStatsModel::getLatestCases).reversed());

        int totalCases = allStats.stream().mapToInt(LocationStatsModel::getLatestCases).sum();
        int newCases = allStats.stream().mapToInt(LocationStatsModel::getDiffFromPreviousDay).sum();
        int totalDeaths = allStats.stream().mapToInt(stat -> stat.getDeaths()).sum();
        int totalRecovered = allStats.stream().mapToInt(stat -> stat.getRecovered()).sum();

        model.addAttribute("totalDeaths", totalDeaths);
        model.addAttribute("totalRecovered", totalRecovered);
        model.addAttribute("locationStats", allStats);
        model.addAttribute("totalCases", totalCases);
        model.addAttribute("newCases", newCases);
    }

    @GetMapping("/")
    public String home(Model model){
        Map<String, Integer> countryStats = coronaVirusDataService.getCountryStats();
        model.addAttribute("countryStats", countryStats);
        return "home";
    }

    @GetMapping("/detailed")
    public String detailed(Model model){
        return "detailed";
    }

    @GetMapping("/search")
    public String searchForCountry(@RequestParam String country, Model model){
        model.addAttribute("countryStat", detailedCoronaVirusStatsService.showStatsForSearchedCountry(country));
        model.addAttribute("countryName", StringUtils.capitalize(country));
        return "search-result-view";
    }
}
