# Rikiki
It is a card game for playing with [Kotlin language](https://kotlinlang.org).

This game has several names and variations.
[Rikiki](https://hu.wikipedia.org/wiki/Rikiki) is the Hungarian name. In France it is known as the [Ascenseur](https://fr.wikipedia.org/wiki/Ascenseur_(jeu_de_cartes)), among other names. It is often called [Oh Hell](https://en.wikipedia.org/wiki/Oh_Hell) in the USA.

[See a screenshot of the non-interactive console version](https://github.com/caillette/Rikiki/blob/master/src/main/resources/Screenshot.png)

To run the game, clone this repository and open it as a project from your favorite IDE with Kotlin support.

- Run `TournamentTest.singleTournament` to get nice console output.
- Run `Tournament` to run lots of tournaments and see each strategy's score. Takes a few minutes.
- Edit default values int `Tournament` to change number of players or their strategies.

The most sophisticated strategy is `AimZero` which bids a result of zero and plays non-winning cards whenever possible (this strategy also works well in real world and it is not antiplay). The performance of `AimZero` doesn't degrade too much against players with non-dumb strategies, its average score is about 80 points for a 30-game tournament with 6 players. A human beginner quickly reaches 150 points.
