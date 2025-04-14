# Inteligentny System Zarządzania Skrzyżowaniem



## Przebieg algorytmu sterującego ruchem na skrzyżowaniu:
1. Wywoływana jest metoda intersection.step(), która rozpoczyna przetwarzenie kroku symulacji
2. Aktualizowane są dane na temat pojazdów które czekają na skrzyżowaniu - wykonywana przez TrafficLightControler.updateWaitingTimes()
3. Wywoływana jest metoda TrafficLightControler.nextStep(), która podejmuje decyzję o wyborze optymalnej fazy świateł:
    - zostają uaktualnione współczynniki wag za pomocą których wybierana jest faza w zależności od natężenia ruchu, czasu oczekiwania pojazdów, czasu oczekiwania pieszych
    - obliczane jest względne natężnie ruchu na poszczególnych kierunkach - procentowy udział liczby aut w każdym kierunku względem wszystkich i na jego podstawie sprawdzane jest czy występuje nierównowaga w natężeniu - do sprawdzenia tego wykorzystywane są *metody statystyczne* - obliczana jest średnia i odchylenie standardowe
        - jeśli odchylenie standardowe przekracza 20% to występuje nierównowaga w natężeniu i zostaną premiowane kierunki zatłoczone,
        - w przypadku długiego czasu oczekiwania premiowane są kierunki z długim czasem oczekiwania,
        - na końcu rozważani są piesi którzy mają własny współcznynnik premiowania faza w których mogą przemieszczać się - brany jest czas oczekiwania, który przy wydłużaniu zostaje zwiększony.
    - po optymalizacji współczynników rozpoczyna się proces decyzyjny którą fazę wybrać za pomocą metody selectBestPhase() - ocenia ona każdą fazę w kontekście obecnej sytuacji na skrzyżowaniu - evaluatePhase() - wyliczając *score* jako sumę iloczynu wcześniej obliczanych współczynników i liczby pojazdów, ich czasów oczekiwania i identyczny iloczyn dla pieszych przemnożony przez współczynnik równoważący
    - na podstawie obliczonych *score'ów* wybierana jest najkorzystniejsza faza
    - algorytm implementuje także mechanizm uczenia się z przeszłości, śledzący efektywność każdej z faz.


### Szczegółowe wyjaśnienie mechanizmów algorytmu:


#### Dynamiczne dostosowanie współczynników wag (adjustWeightFactors)
Algorytm analizuje bieżącą sytuację i dostosowuje wagi dla trzech głównych parametrów:
- α (alpha) - waga dla natężenia ruchu pojazdów
- β (beta) - waga dla czasu oczekiwania pojazdów
- γ (gamma) - waga dla ważności pieszych

W przypadku nierównomiernego rozkładu ruchu (sprawdzanego za pomocą odchylenia standardowego > 0.2):
- α zwiększana jest do 1.5, aby dać pierszeństwo obsłudze zatłoczonych kierunków
- β zmniejszana jest do 0.8, aby zrównoważyć wpływ czasu oczekiwania

Natomiast gdy czasy oczekiwania przekraczają 70% maksymalnego dozwolonego czasu:
- α zmniejszana jest do 0.8
- β zwiększana jest do 1.5, aby na pierwszy plan wysunąć kierunki z długim czasem oczekiwania

Dla pieszych, gdy ich średni czas oczekiwania przekracza średni czas oczekiwania pojazdów:
- γ jest ustawiana na 2.0, dając im wyższy priorytet
- w przeciwnym razie γ ma standardową wartość 1.5



#### Mechanizm obliczania punktacji dla faz (evaluatePhase)
Funkcja evaluatePhase oblicza punktację każdej fazy na podstawie:
1. Liczby pojazdów oczekujących w kierunkach obsługiwanych przez fazę
2. Średniego czasu oczekiwania tych pojazdów
3. Efektywności przepustowej - czyli ile pojazdów faktycznie może przejechać w danej fazie
4. Liczby pieszych i ich czasu oczekiwania

Dodatkowo algorytm implementuje:
- Premiowanie kierunków, gdzie czas oczekiwania zbliża się do maksymalnego (mnożnik 1.5)
- Premiowanie przejść dla pieszych z długim czasem oczekiwania (mnożnik 1.3)
- Współczynnik zapobiegający "głodzeniu" rzadziej wybieranych faz (starvationFactor)



#### Uczenie się na podstawie historii (evaluateLastPhaseEffectiveness)
Po każdej zmianie fazy, algorytm ocenia efektywność zakończonej fazy jako stosunek pojazdów obsłużonych do tych oczekujących. Ta informacja jest wykorzystywana do aktualizacji współczynnika efektywności fazy:

```  
newEffectiveness = oldEffectiveness + LEARNING_RATE * (currentEffectiveness - oldEffectiveness)  
```  

gdzie LEARNING_RATE wynosi 0.1, co zapewnia stopniowe dostosowanie oceny faz w czasie.

#### Zapobieganie utknięciu w tej samej fazie
Algorytm monitoruje liczbę kroków bez zmiany fazy i wymusza zmianę po przekroczeniu progu 10 kroków, ale tylko jeśli:
1. Aktualna faza nie obsługuje żadnego ruchu
2. Istnieje lepsza alternatywa dla obecnej sytuacji

Dzięki temu system jest elastyczny i szybko adaptuje się do zmiennych warunków ruchu, optymalizując przepływ pojazdów i pieszych przez skrzyżowanie.


### Podsumowanie wybranego algorytmu
W projekcie zastosowano adaptacyjny algorytm sterowania ruchem oparty na dynamicznej ocenie sytuacji na skrzyżowaniu, który:

1. Balansuje różne aspekty ruchu poprzez system wag (α, β, γ) dynamicznie dostosowywanych do aktualnej sytuacji
2. Wykrywa nierównowagę w ruchu używając metod statystycznych (odchylenie standardowe)
3. Zapobiega zbyt długim czasom oczekiwania przez premiowanie kierunków z długo czekającymi pojazdami
4. Uczy się na podstawie wcześniejszych doświadczeń z pomocą prostego mechanizmu uczenia maszynowego
5. Zwiększa przepustowość dzięki warunkowym zielonym strzałkom działającym równolegle z głównym algorytmem

Algorytm jest efektywny w różnych warunkach ruchu i adaptuje się zarówno do równomiernego natężenia, jak i sytuacji z nierównowagą w przepływie pojazdów.



## Interfejs JSON i obsługa komend

System implementuje wymagany interfejs JSON do sterowania symulacją. Obsługuje następujące komendy:

#### Komenda `addVehicle`
Dodaje pojazd na wskazanej drodze początkowej z określonym celem dojazdu:
```json  
{  
  "type": "addVehicle",   
  "vehicleId": "vehicle1",   
  "startRoad": "north",   
  "endRoad": "south"
}  
```  
#### Komenda `addPedestrian`
Dodaje pieszego na wskazanym przejściu dla pieszych:
```json  
{  
  "type": "addPedestrian",   
  "vehicleId": "pedestrian1",   
  "crossingDirection": "south"
}  
```  

#### Komenda step
Wykonuje krok symulacji, podczas którego:

1. Aktualizowane są czasy oczekiwania wszystkich pojazdów
2. Algorytm decyzyjny wybiera optymalną fazę świateł (jeśli potrzebna jest zmiana)
3. Pojazdy na drogach z zielonym światłem przejeżdżają przez skrzyżowanie
4. Dodatkowo sprawdzane są możliwości przejazdu na warunkowych zielonych strzałkach
```json  
{  
  "type": "step"
}  
```  


### Format wyjściowy:
System generuje wymagany format wyjściowy zawierający informacje o pojazdach opuszczających skrzyżowanie, wzbogacony o informację o pieszych którzy opuścili skrzyżowanie w każdym kroku:

```json  
{
  "stepStatuses" : [ {
    "leftVehicles" : [ "vehicle2", "vehicle1" ],
    "leftPedestrians" : [ "pedestrian1" ]
  }, {
    "leftVehicles" : [ "vehicle3" ],
    "leftPedestrians" : [ ]
  }, {
    "leftVehicles" : [ "vehicle4" ],
    "leftPedestrians" : [ ]
  }]
} 
```  




## Dodatkowo zrealizowane rozszerzenia projektu:

### Możliwość konfiguracji liczby pasów i ich kierunków
System oferuje zaawansowany mechanizm konfiguracji infrastruktury skrzyżowania poprzez plik JSON (np. `config.json`):
- Definiowanie dowolnej liczby pasów dla każdego wlotu skrzyżowania
- Konfiguracja dozwolonych kierunków ruchu dla każdego pasa (prosto, w lewo, w prawo)
- Możliwość zdefiniowania pasów wyłącznie dla skrętów (np. pas tylko do skrętu w lewo)

Algorytm automatycznie adaptuje się do różnych konfiguracji skrzyżowań i generuje optymalne fazy świateł dostosowane do zadanej infrastruktury. Pozwala to na:
- Symulację różnych typów rzeczywistych skrzyżowań (4-wlotowe, z różnymi układami pasów)
- Testowanie wydajności algorytmu w różnych scenariuszach infrastrukturalnych
- Optymalizację układu pasów dla zwiększenia przepustowości skrzyżowania

Mechanizm walidacji zapewnia, że tylko poprawne konfiguracje zostaną załadowane, a system zgłosi błędy w przypadku wykrycia nieprawidłowości w pliku konfiguracyjnym.

### Przykładowa konfiguracja skrzyżowania

Poniżej znajduje się fragment przykładowego pliku konfiguracyjnego `config.json`, który definiuje układ skrzyżowania:

```json  
{
  "north": [
    { "allowedDestinations": ["south"] },
    { "allowedDestinations": ["east"] },
    { "allowedDestinations": ["west"] }
  ],
  "south": [
    { "allowedDestinations": ["north"] },
    { "allowedDestinations": ["east"] },
    { "allowedDestinations": ["west"] }
  ],
  "west": [
    { "allowedDestinations": ["north"] },
    { "allowedDestinations": ["east"] },
    { "allowedDestinations": ["south"] }
  ],
  "east": [
    { "allowedDestinations": ["south"] },
    { "allowedDestinations": ["north"] },
    { "allowedDestinations": ["west"] }
  ]
} 
```  
gdzie *allowedDestinations* reprezentuje pas z poszczególnego kierunku i posiada listę kierunków docelowych.


### Bezpieczna obsługa pieszych
System implementuje kompleksowe rozwiązanie dla ruchu pieszych, które zapewnia bezpieczeństwo:
- Piesi mogą przechodzić tylko podczas faz, w których żaden pojazd nie przecina ich trasy
- Algorytm uwzględnia czas oczekiwania pieszych jako istotny czynnik przy wyborze optymalnej fazy
- Implementacja specjalnego mechanizmu wykrywania długiego czasu oczekiwania pieszych:
    - Gdy średni czas oczekiwania pieszych przekracza średni czas oczekiwania pojazdów, współczynnik γ zwiększany jest do 2.0
    - Po przekroczeniu krytycznego czasu oczekiwania (80% maksymalnego), piesi otrzymują dodatkowy mnożnik 1.3 dla swoich punktacji


### Warunkowe zielone strzałki
Implementacja inteligentnego systemu warunkowych zielonych strzałek, działającego równolegle do głównego algorytmu wyboru faz:
- System automatycznie analizuje możliwe kolizje dla każdego kierunku skrętu
- Pojazdy mogą otrzymać warunkowe pozwolenie na przejazd (zieloną strzałkę), nawet jeśli główna sygnalizacja pokazuje czerwone światło, pod warunkiem braku kolizji z innymi uczestnikami ruchu
- Implementacja uwzględnia zasadę pierwszeństwa dla pojazdów jadących na zielonym świetle i pieszych na przejściach

Korzyści systemu warunkowych zielonych strzałek:
- Zwiększenie przepustowości skrzyżowania bez kompromisów w kwestii bezpieczeństwa
- Redukcja niepotrzebnych postojów pojazdów, szczególnie w przypadku skrętów w prawo i godzin o mniejszym natężeniu ruchu
- Dynamiczna adaptacja do aktualnych warunków ruchu, niezależnie od wybranej głównej fazy świateł


## Pokrycie testami:
Projekt jest objęty kompleksowymi testami jednostkowymi i integracyjnymi, które zapewniają wysoką jakość i niezawodność kodu:

- Ogólne pokrycie testami: 80% (według raportu SonarQube)
- Testy jednostkowe sprawdzają poprawność działania poszczególnych komponentów:
    - Algorytmy wyboru optymalnej fazy świateł
    - Mechanizmy obsługi pojazdów i pieszych
    - Funkcje oceny efektywności faz i adaptacji parametrów
    - Walidacja poprawności konfiguracji
- Testy integracyjne weryfikują współpracę pomiędzy komponentami systemu:
    - Pełny przepływ przetwarzania komend JSON
    - Kompleksowe scenariusze z wieloma pojazdami i pieszymi
    - Zachowanie systemu przy zmiennym natężeniu ruchu

Testy zostały zaimplementowane przy użyciu JUnit 5, a raportowanie pokrycia testami zapewnia narzędzie JaCoCo. Raporty z testów są generowane automatycznie przy budowaniu projektu i dostępne w katalogu target/site/jacoco (po uruchomieniu komendy.



## Uruchomienie symulacji
Po skompilowaniu projektu za pomocą polecenia:
```shell  
mvn clean package
```  
  
uruchamiamy symulację poleceniem:  
```shell  
java -jar ./main/target/main-1.0-jar-with-dependencies.jar input.json output.json config.json
```  
  
gdzie:  
- input.json - plik wejściowy z komendami (wymagany żeby istniał)  
- output.json - plik, do którego zostanie zapisany wynik symulacji (opcjonalny) - w przypadku braku podania (tworzony jest plik *output.json*)  
- config.json - plik wejściowy z configuracją skrzyżowania (opcjonalny) - w przypadku braku podania zaczytywany jest załączony plik *config.json*


## Wykorzystane biblioteki:
- Maven (wymagana aby uruchomić projekt)
- Lombok
- JUnit 5 
- Mockito
- jackson
- Jacoco (oraz SonarQube do sprawdzenia pokrycia testami)