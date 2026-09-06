# Fabryka Terapii - REST API

Pierwszy etap systemu rezerwacji wizyt: aplikacja Java 21 / Spring Boot 3 z REST API i bazą H2 do lokalnego uruchomienia.

## Zakres obecnej wersji

- role i ochrona endpointów (właściciel, manager, recepcja, specjalista),
- klienci z wymaganym potwierdzeniem zgody na przetwarzanie danych,
- specjaliści, gabinety i usługi,
- blokady dostępności specjalistów lub gabinetów,
- tworzenie wizyt oraz kalendarz w zakresie dat,
- kontrola konfliktów specjalista + gabinet + czas,
- transakcyjne blokowanie specjalisty i gabinetu podczas zapisu, które chroni przed równoczesnym podwójnym zapisem,
- podstawowa maszyna stanów wizyty.

## Uruchomienie

Wymagane: Java 21 oraz Maven 3.9+.

```powershell
mvn spring-boot:run
```

API działa pod `http://localhost:8080`. Do testów HTTP Basic użyj:

- właściciel: `owner@fabrykaterapii.pl` / `change-me`
- recepcja: `recepcja@fabrykaterapii.pl` / `change-me`

Przed wdrożeniem należy przenieść te konta do trwałej bazy użytkowników i zmienić hasła/secrets.

Po starcie sprawdź publiczny endpoint diagnostyczny:

```powershell
Invoke-RestMethod http://localhost:8080/api/hello
```

Powinien zwrócić komunikat `Hello World - Fabryka Terapii API działa`.

## Przykładowy scenariusz

Najpierw utwórz specjalistę, gabinet oraz klienta, a następnie wizytę.

```powershell
$auth = [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes('owner@fabrykaterapii.pl:change-me'))
$headers = @{ Authorization = "Basic $auth"; 'Content-Type' = 'application/json' }

Invoke-RestMethod -Method Post -Uri http://localhost:8080/api/specialists -Headers $headers -Body '{"firstName":"Anna","lastName":"Nowak","specialization":"Fizjoterapia","color":"#2563eb"}'
Invoke-RestMethod -Method Post -Uri http://localhost:8080/api/rooms -Headers $headers -Body '{"name":"Gabinet 1","equipment":"Kołyska"}'
Invoke-RestMethod -Method Post -Uri http://localhost:8080/api/clients -Headers $headers -Body '{"firstName":"Jan","lastName":"Kowalski","phone":"500600700","email":"jan@example.com","personalDataConsent":true,"communicationConsent":true}'
```

Identyfikatory z odpowiedzi przekaż w żądaniu `POST /api/appointments`:

```json
{
  "clientId": "...",
  "specialistId": "...",
  "roomId": "...",
  "startsAt": "2026-09-08T10:00:00",
  "endsAt": "2026-09-08T10:55:00",
  "price": 180.00,
  "paid": false
}
```

Kalendarz: `GET /api/appointments?from=2026-09-08&to=2026-09-14`.

## Kolejne etapy

Grafiki pracy, edycja i odwoływanie wizyt, audyt, seria wizyt, lista oczekujących, powiadomienia, panel online i trwała baza PostgreSQL są kolejnymi elementami wynikającymi ze specyfikacji.

## Uruchomienie na serwerze (Docker)

```bash
cp .env.example .env
# ustaw własne hasła w .env
docker compose up -d --build
curl http://127.0.0.1:8080/api/hello
```

Aplikacja jest celowo dostępna tylko lokalnie na VPS (`127.0.0.1:8080`). W produkcji należy wystawić ją przez Caddy lub Nginx z HTTPS.
