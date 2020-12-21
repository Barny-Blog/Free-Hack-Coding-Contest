# Free-Hack Coding Contest 2017
Dies ist das offizielle Repository für den Free-Hack Coding-Contest (Frühjahr 2017).

### Die Aufgabe:
---
Schreibe einen MauMau-Bot, welcher gegen andere Bots, im K.O.-System, antritt. Die zu verwendene Sprache ist hierbei egal, solange sie mit einfachen Netzwerkverbindungen (Sockets) klar kommt. 
###### Was ist gefordert?
- Der Bot muss sich mit der Serveranwendung verbinden und authentifizieren können.
- Der Bot wartet bis er am Zug ist und reagiert auf die aktuelle Spielsituation mit einem korrekten Zug.
- Der Bot stellt selbstständig fest, ob er eine Karte legen kann und zieht eine Karte, wenn dies nicht der Fall ist.
- Der Bot setzt eine Runde aus, wenn dies der Spielverlauf erfordert (beispielsweise wurde eine 8 gelegt)
- Der Bot zieht selbstständig zwei Karten, wenn dies der Spielverlauf erfordert (beispielsweise wurde eine 7 gelegt)

### Die Spielregeln:
---
###### Die Grundregeln:
> Mau-Mau ist ein Auslegespiel. Gewonnen hat, wer zuerst alle seine Karten abspielen konnte. [...]Zu Beginn erhält jeder Spieler die gleiche Anzahl Karten [...], die er verdeckt – als Kartenfächer – auf seine Hand nimmt. Die restlichen Karten werden verdeckt als Stapel (Talon) abgelegt. Die oberste Karte des Talons wird offen daneben gelegt.
Reihum legt nun jeder Spieler eine seiner Karten offen auf die nebenliegende Karte – wenn dies möglich ist. Möglich ist dies, wenn die abzulegende Karte in Kartenwert oder Kartenfarbe mit der obersten offen liegenden Karte übereinstimmt. Auf die Pik 10 darf also entweder eine andere Pik-Karte oder eine andere 10 gelegt werden. Kann oder will ein Spieler keine Karte ablegen, so muss er eine Karte vom Talon ziehen. Je nach Regel darf er anschließend diese Karte, wenn sie den angegebenen Bedingungen genügt, ablegen, oder muss warten, bis er erneut an der Reihe ist. Ist der Talon irgendwann aufgebraucht, so werden die abgelegten Karten, außer der obersten sichtbaren, erneut als Talon ausgelegt. Oft werden sie vorher noch gemischt.“ [Quelle Wikipedia](https://de.wikipedia.org/wiki/Mau-Mau_(Kartenspiel)#Regeln)

Die Grundregeln entsprechen dem oben zu sehenden Auszug aus Wikipedia, mit der Ausnahme, dass man nicht weiterlegen darf, wenn man eine Karte ziehen musste. 
###### Beispiel:
Spieler 1 kann keine Karte legen und zieht eine Karte vom Stapel (oben „Talon“). Spieler 1 darf die gezogene Karte erst im nächsten Zug legen, auch wenn diese auf die oberste Karte passen würde.
Ich denke, dass diese aber geläufig sein sollten. 
###### Die Zusatzregeln:
Da sich diese Regeln regional unterscheiden, sind unsere Zusatzregeln hier einmal zusammengefasst:
- Legt ein Spieler eine 7 (egal welche Farbe), dann muss der darauffolgende Spieler 2 Karten ziehen. Der nächste Spieler MUSS 2 Karten ziehen. Er kann das „Ziehen“ NICHT durch erneutes Legen einer 7 „weitergeben“. Hat ein Spieler aufgrund einer 7 zwei Karten ziehen müssen, so beendet dies nicht seinen Zug. Er zieht zwei Karten und führt seinen normalen Zug aus.
- Legt ein Spieler eine 8 (egal welche Farbe), dann muss der darauffolgende Spieler aussetzen ziehen. Auch hier gilt: Er MUSS aussetzen, und kann nicht noch eine 8 legen, um das Aussetzen weiterzugeben.
- Der Bube ist der „Joker“ durch den es möglich wird, dass sich der Spieler, welcher den Buben gelegt hat, eine Farbe wünschen darf. Auch wenn es die letzte Karte des Spielers war, muss er sich eine Karte wünschen für den restlichen Spielverlauf.
- Außerdem gilt die Regel, dass auf einen Bube nicht erneut ein Bube gelegt werden darf. Die kommt daher, dass der Bube in Mau Mau als Joker behandelt wird und keine Farbe besitzt. Ihn darf man überall drauf legen (außer auf einen anderen Buben). Da sich vorher eine Farbe gewünscht worden ist, muss diese Farbe bedient werden.

Ich möchte an dieser Stelle noch einmal unterstreichen, dass die Regel „Weitergeben“ NICHT existiert und mit Strafkarten belohnt wird, wenn sich ein Spieler nicht daran hält.
### Der Ablauf
---
###### Die Serveranwendung starten:
Die Anwendung muss einmal auf einem beliebigen System wie nachfolgend ausgeführt werden:
```
java -jar FH_CodingContest.jar -FreeHackContest -port=1234
```
Dies würde den Server für den Contest starten und er wartet jetzt auf zwei Spieler. Der Parameter "-port" kann natürlich nach belieben angepasst werden, jedoch sollte der zu verwendende Port auch frei sein.

Des Weiteren habt ihr die Möglichkeit den Server im normalen Netzwerkmodus zu starten oder gegen einen Bot offline zu spielen. Für ein Netzwerkspiel startet ihr den Server so:
```
java -jar FH_CodingContest.jar -isNetworkGame -players=2 -port=1234
```
Und für ein Spiel gegen einen Bot so:
```
java -jar FH_CodingContest.jar -isBotgame -player=Spieler -player=Bot
```
Hierbei ist der erste angegebene Spieler immer der Mensch und der Rest die Bots. Ein Offlinespiel kann mit __max. 3 Spielern__ gespielt werden. 
Die genannten Modi sind für den Contest __nicht__ relevant.

###### Spielstart:
Ist der Server erfolgreich gestartet worden, so können sich nun Bots mit dem Server verbinden. Nach einer erfolgreichen Verbindung antwortet der Server mit nachfolgender Meldung:
```json
{
    "status" : "okay",
    "sendName" : True
}
```
Der Bot muss wie nachfolgend antworten, um seinen Namen mitzuteilen:
```json
{   
    "status" : "okay",
    "name" : "Bot1"
}
```
Damit der Server diese Nachricht verarbeiten kann, muss die Nachricht mit einem __Linebreak__ abgeschlossen werden. Dies gilt für __jede__ Nachricht die an den Server geschickt wird. Beispiel:
```
sendMessage("Das ist ein Beispiel\n");
```
Außerdem erwartet der Server das JSON-Statement __einzeilig__! Der Grund, warum in dieser Doku alles Mehrzeilig ausgegeben wird, ist die bessere Übersicht.

Der Server antwortet darauf (nicht das Beispiel) mit der Bestätigung des Namens oder eine Fehlermeldung. Die Fehlermeldung unterbricht aber keineswegs den Programmablauf. Sollte etwas hier schon schief gehen, so vergibt der Server eigenständig Namen (Bot [Zahlen von0-2]). Ist der Name aber korrekt empfangen worden, so antwortet der Server mit nachfolgender Meldung und bestätigt das Setzen des Namens:

```json
{
    "status" : "okay",
    "name" : "Bot1"
    "setName" : True
}
```
Ist ein Fehler aufgetreten, dann antwortet er so und vergibt einen Namen:
```json
{
    "status" : "error",
    "name" : "Bot 0"
    "setName" : False
}
```
Diese Prozedur muss jeder Bot einmal durchlaufen. Danach wird nur darauf gewartet, dass alle Spieler sich verbunden haben. Der Beginn des Spiels wird mit nachfolgendem Broadcast allen Bots gemeldet:
```json
{
 "status" : "okay",
 "gameStart" : True
}
```
Der Bot, welcher sich als erstes verbunden hat beginnt das Spiel.
###### Der Spielverlauf:
Die erste Runde beginnt und es wird an alle Bots eine Mitteilung gesendet, welcher Bot dran ist:
```json
{
    "status" : "okay",
    "turnOf" : "Bot1"
    "skipped" : False,
    "topCard" : 2
}
```
Außerdem wird allen Spielern mitgeteilt, welche Karte derzeit offen aufgedeckt liegt.
###### Die Karten:
Die Karten sind von 0 bis 31 durchnummeriert und die Kommunikation zwischen Server und Bot wird auschließlich über diese Struktur erfolgen. Möchte also ein Bot die Karte "Pik 7" legen, so sendet er die Kennzahl "0". Möchte ein Bot die Karte "Kreuz Dame" legen, so sendet er die Kennzahl "23". Die Zuordnungen zwischen Kennzahlen und Karten lauten wie nachfolgend:
- 0 = Pik 7
- 1 = Karo 7
- 2 = Herz 7
- 3 = Kreuz 7
- 4 = Pik 8
- 5 = Karo 8
- 6 = Herz 8
- 7 = Kreuz 8
- 8 = Pik 9
- 9 = Karo 9
- 10 = Herz 9
- 11 = Kreuz 9
- 12 = Pik 10
- 13 = Karo 10
- 14 = Herz 10
- 15 = Kreuz 10
- 16 = Pik Bube
- 17 = Karo Bube
- 18 = Herz Bube
- 19 = Kreuz Bube
- 20 = Pik Dame
- 21 = Karo Dame
- 22 = Herz Dame
- 23 = Kreuz Dame
- 24 = Pik König
- 25 = Karo König
- 26 = Herz König
- 27 = Kreuz König
- 28 = Pik Ass
- 29 = Karo Ass
- 30 = Herz Ass
- 31 = Kreuz Ass

Der Spieler der dran ist, erhält eine weitere Meldung mit zusätzlichen spezifischen Informationen zu beispielsweise seinem Blatt:
```json
{
    "status" : "okay",
    "topCard" : 19,
    "drawTwoCards" : false,
    "skipped" : false,
    "wishedColor" : -1
    "cardsLeft" : 7,
    "hand" : [
               4,
               9,
               15,
               7,
               30,
               18,
               1
             ]
}
```
Dieser Meldung sind nachfolgende Informationen zu entnehmen:
1. __Status__: Der Server meldet, dass alles in Ordnung ist
2. __topCard__: Die oberste Karte (auf die gelegt werden soll) ist die 19 (Kreuz Bube)
3. __drawTwoCards__: Der Bot musste keine zwei Karten ziehen
4. __skipped__: Der Bot musste nicht aussetzen
5. __wishedColor__: Welche Farbe wurde sich vom Spieler zuvor gewünscht? (Infos zu  den Farben wird weiter unten erklärt.)
6. __CardsLeft__: Wie viele Karte hat der Bot noch auf der Hand?
7. __Hand__: Einmalig wird dem Bot seine Hand mitgeteilt. Dies erfolgt ausschließlich beim ersten Zug und muss sich gemerkt werden!
Wenn der Bot im Verlauf des Spiels zusätzliche Karten erhält (Bsp.: Er muss zwei Karten ziehen wegen einer 7 oder muss eine Strafkarte ziehen oder setzt aus) dann wird ihm das mit der Information „drawedCards“ mitgeteilt. Muss er eine Karte ziehen, dann erhält er die Karte als normalen Ausdruck. Wenn es mehr als eine Karte ist, dann erhält er diese Karten wie mit dem JSON-Element "hand" als Array.

Auf obenstehende Meldung erwartet der Server eine Antwort mit nachfolgendem Aufbau:
```json
{
 "status" : "turn",
 "selectedCard" : 4,
 "wishedColor" : -1,
 "skipped" : false
}
```
Wird der "status" auf den Wert "__skip__" gesetzt, so setzt der Bot eine Runde aus und erhält eine Karte. Legt der Bot einen Buben, so muss der Wert "__wishedColor__" zusätzlich angepasst werden, um mitzuteilen, welche Farben sich gewünscht wird. Wird der Wert nicht angepasst, so wird sich automatisch die Farbe "Pik" gewünscht. Die Werte für die jeweiligen Farben lauten:

- 0 = Pik
- 1 = Karo
- 2 = Herz
- 3 = Kreuz

Dies wird dem nächsten Spieler mit dem Wert „__wishedColor__“ mitgeteilt. Dieser muss dann die gewünschte Farbe bedienen. Tut er dies nicht, dann erhält der Bot eine Strafkarte.

__Anmerkung__: Liegt als erste Karte die aufgedeckt wird ein Bube, so ist die Jokerfunktion vertan. Das bedeutet, dass auf diese Farbe entweder die Farbe des Buben oder ein Bube selber gelegt werden darf. Es wird sich weder eine Farbe automatisch gewünscht und vorgegeben, noch darf der nächste Spieler sich eine Farbe wünschen. Das heißt, es __muss__ die Farbe oder das Bild bedient werden.

Der Wert in "__skipped__" sagt nur, ob der Bot zuvor ausgesetzt hat. Wird eine falsche Karte gelegt, oder eine Karte gelegt, welche nicht auf der Hand des Bots ist, so wird dies mit einer Strafkarte belohnt und der Bot erhält nachfolgende Meldung vom Bot:
```json
{
    "status" : "error"
    "drawedCards" : 24
}
```
Die 24 würde in diesem Fall bedeuten, dass der Bot aufgrund des falschen Zuges die Karte „Pik König“ als Strafkarte erhalten hat.
War der Zug jedoch gültig, so ist der nächste Bot an der Reihe und es beginnt wieder von vorne.
### Der Gewinner:
---
Der Gewinner wird ausgegeben, nachdem die letzte Karte gespielt worden ist. Der Gewinner wird mit nachfolgender Meldung des Servers verkündet:
```json
{
    "status" : "okay",
    "won" : True,
    "nameOfWinner" : "Bot1"
}
```
Der Server trennt nach dieser Meldung die Verbindungen und das Spiel ist vorbei.
### Verhalten des Servers bei Fehlern oder Ähnliches
---
Da der normale Spielablauf nicht gesichert ist, weil immer Fehler auftreten können gibt es hier einige Sonderregelungen, welche einen normalen Spielfluss ermöglichen:
- Wenn ein Bot die Verbindung zum Server verliert oder nicht antwortet, so wird die letzte Nachricht des Servers 3 Mal wiederholt. Antwortet der Bot dann immer noch nicht, so wird der Bot im weiteren Spielverlauf ignoriert und scheidet aus. Scheiden alle Bots, bis auf einer aus, so ist der übrig gebliebene Bot der Gewinner.
- Wie oben schon erwähnt, erhält ein Bot eine Strafkarte, sofern dieser keinen gültigen Zug macht. Die Karte die er ziehen musste wird dem Bot mitgeteilt. Dasselbe geschieht, wenn er zwei Karten aufgrund einer 7 ziehen musste.
- Wenn kein Bot in der Lage ist eine gültige Karte zu legen, dann müssen alle Bots nacheinander Strafkarten ziehen. Wenn es keine Karten mehr zu ziehen gibt, weil alle Karten auf der jeweiligen Hand des Bots sind und es keine Karten mehr zum untermischen gibt aus dem „Pott“, dann wird das Spiel abgebrochen und der Gewinner ist derjenige Spieler, welcher die meisten gültigen Züge gemacht hat.

In diesem Sinne wünsche ich allen viel Spaß!

Bei Fragen oder Problemen stehe ich oder der Rest des FH-Teams jederzeit unter
- per PM
- im Thread
zur Verfügung.