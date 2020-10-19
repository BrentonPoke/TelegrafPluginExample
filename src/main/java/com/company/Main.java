package com.company;

import gov.noaa.WeatherServiceGenerator;
import gov.noaa.models.stations.StationService;
import gov.noaa.models.stations.observations.Observation;
import gov.noaa.models.stations.observations.ObservationFeature;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import retrofit2.Call;
import retrofit2.Response;

public class Main {

    public static void main(String[] args) {
	// write your code here
        Scanner scanner = new Scanner(System.in);
        while(scanner.hasNextLine()){
            scanner.nextLine();
            StationService service = WeatherServiceGenerator.createService(StationService.class);
    
            Call<ObservationFeature> call = service.getObservations("BDLM4");
            try {
                Response<ObservationFeature> response = call.execute();
                ObservationFeature observationResponse = response.body();
                Stream<Observation> stream = observationResponse.getFeatures().stream();
                List<Observation> list = stream
                    .filter(a->
                        a.getProperties()
                            .getDewpoint().
                            getValue() > 14.0f
                            &&
                            a.getProperties()
                                .getWindSpeed()
                                .getValue() > 8.0f
                        ).collect(Collectors.toCollection(ArrayList::new));
                
                lineProtocol(list);
                
            } catch (IOException e) {
        Logger.getAnonymousLogger().info(e.getMessage());
            }
        }
        
    }
    
    static void lineProtocol(List<Observation> metrics){
        /*Must use atomic reference because the streams api utilizes multi-threading under the hood
         and type String would just result in filling up the string pool with empty String objects*/
    
        StringBuffer stringBuffer = new StringBuffer();
        metrics.stream().forEach((s) -> {
            stringBuffer.append(s.getProperties().getStation().replace("https://api.weather.gov/stations/","stationReading,station="))
                .append(" temperature="+s.getProperties().getTemperature().getValue().toString())
                .append(",dewpoint=")
                .append(s.getProperties().getDewpoint().getValue().toString())
                .append(",windgust=")
                .append(s.getProperties().getWindGust().getValue().toString())
                .append(" ")
                .append(s.getProperties().getTimestamp().toInstant().toEpochMilli());
            System.out.println(stringBuffer.toString());
            stringBuffer.delete(0,stringBuffer.length());
        });
        
    }
}
