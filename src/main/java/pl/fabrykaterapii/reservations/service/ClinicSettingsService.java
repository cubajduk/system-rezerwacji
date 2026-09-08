package pl.fabrykaterapii.reservations.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.constraints.*;
import org.springframework.stereotype.Service;
import pl.fabrykaterapii.reservations.domain.ClinicSettings;
import pl.fabrykaterapii.reservations.repository.ClinicSettingsRepository;

@Service
public class ClinicSettingsService {
 public record Settings(
  @NotBlank @Size(max=150) String clinicName, @Size(max=500) String address,
  @Pattern(regexp="[0-9]{10}|",message="NIP powinien mieć 10 cyfr") String nip,
  @Size(max=40) String phone, @Email @Size(max=150) String email,
  @Size(max=300000) String logo,
  @Pattern(regexp="week|workweek|threeDays|twoDays|day") String calendarMode,
  boolean showWeekends, boolean showSummary, boolean showLegend,
  @Min(1) @Max(36) int recurrenceMonths,
  @Min(0) @Max(22) int openingHour, @Min(1) @Max(23) int closingHour,
  @Pattern(regexp="#[a-fA-F0-9]{6}") String accentColor) {}
 private final ClinicSettingsRepository repository;
 private final ObjectMapper mapper;
 public ClinicSettingsService(ClinicSettingsRepository repository,ObjectMapper mapper){this.repository=repository;this.mapper=mapper;}
 public Settings read(){return repository.findById(1L).map(row->{try{return mapper.readValue(row.getConfiguration(),Settings.class);}catch(Exception e){throw new IllegalStateException("Nie można odczytać ustawień",e);}}).orElse(defaults());}
 public static Settings defaults(){return new Settings("Fabryka Terapii","","","","","","workweek",false,true,true,12,7,20,"#be0e7d");}
 public Settings save(Settings value){
  if(value.closingHour()<=value.openingHour())throw new IllegalArgumentException("Zamknięcie musi być później niż otwarcie");
  if(value.logo()!=null&&!value.logo().isEmpty()&&!value.logo().matches("data:image/(png|jpeg|webp);base64,[A-Za-z0-9+/=]+"))throw new IllegalArgumentException("Wybierz logo PNG, JPEG lub WebP");
  try{var row=new ClinicSettings();row.setConfiguration(mapper.writeValueAsString(value));repository.save(row);return value;}catch(com.fasterxml.jackson.core.JsonProcessingException e){throw new IllegalStateException(e);}
 }
}
