# GrontLys
Exam project for App development at USN, fall 2019. Home exam, individual, one week deadline.

Project description:

Du skal lage en app som kan vise resultater fra Mattilsynets tilsyn av serveringssteder. Appen skal
basere seg på åpne data fra Mattilsynet som er tilgjengelig fra et REST-API hos Difis Datahotell på
data.norge.no.

Datagrunnlag
REST-APIet består av disse to datasettene fra Mattilsynet:
Tilsyn
Dette datasettet inneholder informasjonen tilsvarende den plakaten som henges opp hos
spisestedene etter at de har hatt tilsyn. Settet inneholder fra 2016 til 2019.
• Innsyn i hele datasettet: https://hotell.difi.no/?dataset=mattilsynet/smilefjes/tilsyn
• Endepunkt for JSON-data: https://hotell.difi.no/api/json/mattilsynet/smilefjes/tilsyn

Kravpunkter
Dette datasettet inneholder hvert enkelt kravpunkt som inngår i ett tilsyn, sammen med karakteren
kravpunktet er gitt.
• Innsyn i hele datasettet: https://hotell.difi.no/?dataset=mattilsynet/smilefjes/kravpunkter
• Endepunkt for JSON-data: https://hotell.difi.no/api/json/mattilsynet/smilefjes/kravpunkter
De to datasettene er beskrevet på denne siden:
https://data.norge.no/data/mattilsynet/smilefjestilsyn-på-serveringssteder. Her finner du forklaring
på hver tabell og kolonne, samt noen dataverdier. Kolonnen tilsynid i datasettet Tilsyn kobler til
kolonnen tilsynid i datasettet Kravpunkter.
Data skal leses on-line fra REST-APIet hos Difi. Det er mulig å søke i datasettene ved å bruke
parametere i URLen som brukes for REST-kallene. På denne siden: https://hotell.difi.no/api finner du
en beskrivelse av hvordan du kan søke og noen eksempler på URLer for søk. (Eksemplene er fra
datasett med fylker og kommuner, men syntaksen er den sammen for Mattilsynets datasett.)
Du vil også få bruk for APIet "Åpent adresse-API fra Kartverket". Se punkt d nedenfor.

Funksjonalitet
App’en skal la brukeren utføre funksjonene nedenfor. For oversikten skyld er funksjonaliteten
spesifisert i nummererte punkter, men du behøver ikke følge denne rekkefølgen/strukturen når du
løser oppgaven. Du står svært fritt til å designe GUI’et selv.
Hvis du ikke klarer / rekker å implementere all funksjonaliteten nedenfor, bør du prioritere å få det
du programmerer til å fungere korrekt. Hvis du har tid, kan du også implementere nye funksjoner
som du synes er "nyttige".
Obs! IKKE bruk tid på å lage noen logg-inn funksjon på denne appen.
Det vil ikke gi noe ekstra score.

a. Finne tilsyn for et spisested via navn og eller poststed
Bruker skal kunne søke etter spisesteder (tilsyn) basert på en kombinasjon av navn på spisestedet og
eller navn på poststed i datasettet tilsyn. Appen skal vise resultatet av søket i en liste med følgende
data som spisestedet: org.nr, navn, adresse, postnr, poststed og total karakter. Total karakter bør
vises som et grafisk symbol. Velg selv hvordan listen ellers skal sorteres og presenteres.

b. Vise detaljinformasjon om et tilsyn
Når bruker velger en rad (spisested) fra listen skal det vises et nytt skjermbilde med all relevant
informasjon fra datasettet Tilsyn. Velg selv hva du mener er relevant informasjon, og finn selv gode /
brukervennlige måter å presentere denne informasjonen på. En del av skjermen skal også vise en liste
med alle kravpunktene for det aktuelle tilsynet. Disse må hentes fra datasettet Kravpunkter basert
på kolonnen tilsynid. Du bestemmer selv hvordan disse dataene skal presenteres for bruker.

c. Tilpasse søkelisten for tilsyn
Bruker skal kunne filtrere søkelisten basert på årstall for tilsynet, f.eks. kunne velge alle, eller ett
årstall. Vurder selv om det er andre egenskaper det er naturlig å filtrere på.
Bruker skal også kunne sveipe bort uinteressante rader fra listen over tilsyn/spisesteder slik at de
fjernes fra listen. (De ligger selvsagt fremdeles i datasettet og kan dukke opp igjen i listen ved et nytt
søk). Før raden fjernes fra listen skal bruker få et valg om å bekrefte eller angre handlingen.

d. Finne tilsyn/spisesteder basert på brukers posisjon (geografisk søk)
Appen skal også ha en funksjon for å vise en liste med alle tilsyn/spisesteder som er registrert på det
postnummeret der brukeren befinner seg. Denne funksjonen skal bruke mobilenhetens GPS for å
finne brukerens posisjon. Datasettet fra Mattilsynet inneholder ikke koordinater, så posisjon må først
kobles til postnummer f.eks. med APIet "Åpent adresse-API fra Kartverket", eller med klassen
Geocoder i Android APIet.

REST-tjenesten punktsøk i dette Kartverkets adresse-API vil returnere alle adresser innen en oppgitt
radius fra et punkt med koordinater. Tjenesten vil kunne returnere flere adresser, men for enkelthet
skyld kan du bruke postnummer fra den første adressen i resultatsøket for å søke videre i datasettet
Tilsyn. De aktuelle spisestedene / tilsynene skal vises i samme liste som i pkt. a og kunne vises i detalj
som i pkt. b.

e. Innstillinger / Settings / brukervalg
App’en skal ha et valg for Innstillinger (Settings) der bruker kan legge inn noen faste opplysninger
som lagres lokalt mellom hver programkjøring, f.eks. et "favorittsted" (poststed eller postnummer) og
evt. fast årstall for filtrering av Tilsyn.
