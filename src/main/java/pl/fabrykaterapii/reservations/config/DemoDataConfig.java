package pl.fabrykaterapii.reservations.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import pl.fabrykaterapii.reservations.domain.Client;
import pl.fabrykaterapii.reservations.domain.Room;
import pl.fabrykaterapii.reservations.domain.Specialist;
import pl.fabrykaterapii.reservations.domain.TherapyService;
import pl.fabrykaterapii.reservations.domain.WorkSchedule;
import pl.fabrykaterapii.reservations.domain.WorkScheduleRecurrence;
import pl.fabrykaterapii.reservations.repository.ClientRepository;
import pl.fabrykaterapii.reservations.repository.RoomRepository;
import pl.fabrykaterapii.reservations.repository.SpecialistRepository;
import pl.fabrykaterapii.reservations.repository.TherapyServiceRepository;
import pl.fabrykaterapii.reservations.repository.WorkScheduleRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Dane demonstracyjne uruchamiane wyłącznie poza profilem produkcyjnym.
 * Klienci są całkowicie fikcyjni i służą wyłącznie do lokalnego testowania UI.
 */
@Configuration
@Profile("!prod")
public class DemoDataConfig {
    private static final List<String> COLORS = List.of("#0b786f", "#6d91d7", "#a88bc9", "#d88a94", "#d98a50", "#438a7c", "#8a5b9e", "#3674a6", "#b35d6f", "#548d59", "#b77837", "#4f6c92", "#956c45", "#7d758e", "#a65e45", "#397c83", "#aa6f94", "#557458", "#8065a1", "#a36b36", "#527aaf", "#8d6b5a", "#4c9278", "#a44d67", "#697c46");

    @Bean
    CommandLineRunner loadDemoData(RoomRepository rooms, SpecialistRepository specialists,
                                   ClientRepository clients, TherapyServiceRepository services, WorkScheduleRepository schedules) {
        return args -> {
            if (rooms.count() == 0) {
                rooms.saveAll(List.of(
                        new Room("Gabinet 1", "G1", "Biurko, krzesła, materiały logopedyczne"),
                        new Room("Gabinet 2", "G2", "Biurko, lustro logopedyczne, materiały terapeutyczne"),
                        new Room("Gabinet 3", "G3", "Wyposażenie do terapii ręki"),
                        new Room("Gabinet 4", "G4", "Kołyska, stół terapeutyczny"),
                        new Room("Gabinet 5", "G5", "Wyposażenie do terapii psychologicznej"),
                        new Room("Gabinet 6", "G6", "Sprzęt do terapii słuchowej"),
                        new Room("Gabinet 7", "G7", "Wyposażenie do integracji sensorycznej"),
                        new Room("Gabinet 8", "G8", "Sala do zajęć grupowych i TUS")
                ));
            }
            if (specialists.count() == 0) {
                List<SpecialistSeed> seeds = List.of(
                        new SpecialistSeed("Adrianna", "Kunach", "Logopeda, specjalista metody krakowskiej"),
                        new SpecialistSeed("Agata", "Andruczyk", "Fizjoterapeuta dzieci i niemowląt"),
                        new SpecialistSeed("Agata", "Zakonek", "Neurologopeda, psycholog kliniczny, terapeuta SI"),
                        new SpecialistSeed("Aleksandra", "Furmańska-Hajduk", "Neurologopeda, pedagog"),
                        new SpecialistSeed("Aleksandra", "Mejster", "Neurologopeda, wczesna interwencja logopedyczna"),
                        new SpecialistSeed("Aleksandra", "Olędzka", "Neurologopeda, terapeuta oddechu"),
                        new SpecialistSeed("Anita", "Misior", "Neurologopeda, terapeuta integracji sensorycznej"),
                        new SpecialistSeed("Barbara", "Wróblewska", "Pedagog specjalny i logopeda"),
                        new SpecialistSeed("Daria", "Świdurska", "Psycholog kliniczny i seksuolog"),
                        new SpecialistSeed("Ewa", "Karabin", "Logopeda i pedagog"),
                        new SpecialistSeed("Jakub", "Liwuś", "Logopeda, pedagog, terapeuta integracji sensorycznej"),
                        new SpecialistSeed("Joanna", "Krzemińska", "Pedagog specjalny, komunikacja alternatywna"),
                        new SpecialistSeed("Justyna", "Michańska", "Logopeda i pedagog"),
                        new SpecialistSeed("Justyna", "Ostrowska", "Pedagog specjalny, wczesna edukacja"),
                        new SpecialistSeed("Katarzyna", "Boguniewicz", "Psycholog, psychoterapeuta, terapeuta ręki"),
                        new SpecialistSeed("Lucyna", "Zuba", "Neurologopeda, nauczyciel edukacji wczesnoszkolnej"),
                        new SpecialistSeed("Magdalena", "Gębska", "Fizjoterapeuta stomatologiczny"),
                        new SpecialistSeed("Magdalena", "Jędrzejewska", "Fizjoterapeuta, terapeuta SI"),
                        new SpecialistSeed("Magdalena", "Kalinowska-Giemra", "Pedagog specjalny, komunikacja alternatywna"),
                        new SpecialistSeed("Marta", "Kostrubiec", "Logopeda i oligofrenopedagog"),
                        new SpecialistSeed("Natalia", "Martyniak-Kamińska", "Psycholog, trener TUS"),
                        new SpecialistSeed("Natalia", "Zielińska", "Neurologopeda, pedagog specjalny"),
                        new SpecialistSeed("Oliwia", "Dembek", "Psycholog i psychoonkolog"),
                        new SpecialistSeed("Paula", "Bieczyńska-Winnicka", "Psycholog i psychoterapeuta systemowy"),
                        new SpecialistSeed("Sylwia", "Kozłowska", "Psycholog")
                );
                for (int i = 0; i < seeds.size(); i++) {
                    SpecialistSeed s = seeds.get(i);
                    specialists.save(new Specialist(s.firstName(), s.lastName(), s.specialization(), COLORS.get(i % COLORS.size())));
                }
            }
            if (clients.count() == 0) {
                clients.saveAll(List.of(
                        new Client("Antoni", "Kowalski", "500101101", "antoni.kowalski@example.test", LocalDate.of(2018, 3, 12), true, true, "Dane demonstracyjne"),
                        new Client("Zofia", "Nowak", "500101102", "zofia.nowak@example.test", LocalDate.of(2016, 9, 21), true, true, "Dane demonstracyjne"),
                        new Client("Maja", "Wiśniewska", "500101103", "maja.wisniewska@example.test", LocalDate.of(2019, 1, 5), true, false, "Dane demonstracyjne"),
                        new Client("Jan", "Wójcik", "500101104", "jan.wojcik@example.test", LocalDate.of(2014, 6, 17), true, true, "Dane demonstracyjne"),
                        new Client("Oliwia", "Kamińska", "500101105", "oliwia.kaminska@example.test", LocalDate.of(2017, 11, 29), true, true, "Dane demonstracyjne")
                ));
            }
            if (schedules.count() == 0) {
                var weekdays = java.util.Set.of(java.time.DayOfWeek.MONDAY, java.time.DayOfWeek.TUESDAY, java.time.DayOfWeek.WEDNESDAY, java.time.DayOfWeek.THURSDAY);
                for (Specialist specialist : specialists.findAll()) schedules.save(new WorkSchedule(specialist, LocalDate.of(2020, 1, 1), null, java.time.LocalTime.of(9, 0), java.time.LocalTime.of(15, 0), WorkScheduleRecurrence.WEEKLY, weekdays));
            }
        };
    }

    private record SpecialistSeed(String firstName, String lastName, String specialization) { }
}
