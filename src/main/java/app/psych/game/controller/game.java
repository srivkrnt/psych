package app.psych.game.controller;

import app.psych.game.Utils;
import app.psych.game.model.*;
import app.psych.game.repository.*;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/game")
public class game {
    @Autowired
    PlayerRepository playerRepository;
    @Autowired
    QuestionRepository questionRepository;
    @Autowired
    GameRepository gameRepository;
    @Autowired
    RoundRepository roundRepository;

    @GetMapping("/create/{pid}/{gm}/{nr}")
    public String createGame(@PathVariable(value = "pid") Long playerId,
                             @PathVariable(value = "gm") int gameMode,
                             @PathVariable(value = "nr") int numRounds) {
        Optional<Player> optionalPlayer = playerRepository.findById(playerId);
        Player player = optionalPlayer.get();
        GameMode mode = GameMode.IS_THIS_A_FACT;
        Game game = new Game();
        game.setNumRounds(numRounds);
        game.setLeader(player);
        game.setGameMode(mode);
        List<Player> players = game.getPlayers();
        game.setPlayers(players);
        gameRepository.save(game);
        return "" + game.getId() + "-" + Utils.getSecretCodeFromId(game.getId());
    }

    @GetMapping("/join/{pid}/{gc}")
    public String joinGame(@PathVariable(value = "pid") Long playerId,
                             @PathVariable(value = "gc") String gameCode) {
        Optional<Game> optionalGame = gameRepository.findById(Utils.getGameIdFromSecretCode(gameCode));
        Game game = optionalGame.get();
        if(!game.getGameStatus().equals(GameStatus.JOINING)) {
            // throw some error
        }
        Optional<Player> optionalPlayer = playerRepository.findById(playerId);
        Player player = optionalPlayer.get();

        game.getPlayers().add(player);
        gameRepository.save(game);

        return "successfully joined";
    }
    // startGame - pid, gid/gc
    // pid is actually the leader of the current game
    // game has not already been started
    // the game has more than 1 players

    @GetMapping ("/start/{pid}/{gc}")
    public String startGame(@PathVariable(value = "pid") Long playerId,
                            @PathVariable(value = "gc") String gameCode){
        Optional<Game> optionalGame = gameRepository.findById(Utils.getGameIdFromSecretCode(gameCode));
        Game game = optionalGame.get();

        if(game.getGameStatus().equals(GameStatus.IN_PROGRESS)){
            // throw some Exception
            return "Game is already in progress";
        }

        Player player = game.getLeader();
        if(player.getId()!= playerId){
            // throw some Exception
            return "You are not allowed to start the game";
        }

        List<Player> players = game.getPlayers();
        if(players.size() == 1){
            // Raise some Exception
            return "The game needs more than 1 players to get started";
        }
        game.setGameStatus(GameStatus.IN_PROGRESS);
        gameRepository.save(game);
        return "Game started";
    }

    // endGame - pid gid
    // make sure that you're the leader of the game
    @GetMapping("/end/{pid}/{gid}")
    public String endGame(@PathVariable(value = "pid") Long playerId,
                          @PathVariable(value = "gid") Long gameId){
        Optional<Game> optionalGame = gameRepository.findById(gameId);
        Game game = optionalGame.get();
        Player player = game.getLeader();

        if(player.getId() != playerId){
            return "You are not authorised to end this game";
        }
        if(game.getGameStatus().equals(GameStatus.OVER)){
            // Throw some Exception;
            return "Game has already ended";
        }

        game.setGameStatus(GameStatus.OVER);
        gameRepository.save(game);
        return "Game has successfully ended";
    }

    // getGameState - gid
    // JSON - current round - game stats of each player
    // - current round state - submitting-answer, selecting-answers-round-over

    // submitAnswer - pid, gid, answer

    // leaveGame - pid, gid
    // update player's stats
    @GetMapping("/leave/{pid}/{gid}")
    public String leaveGame(@PathVariable(value = "pid") Long playerId,
                            @PathVariable(value = "gid") Long gameId){
        Optional<Game> optionalGame = gameRepository.findById(gameId);
        Game game = optionalGame.get();

        List<Player> players = game.getPlayers();
        Optional<Player> optionalPlayer = playerRepository.findById(playerId);
        Player player = optionalPlayer.get();
        if(!players.contains(player)){
            return "You are not part of this game";
        }

        Map<Player, Stats> playerStats = game.getPlayerStats();
        Stats curPlayerStat = playerStats.get(player);
        Stats prevPlayerStat = player.getStats();

        Stats stat = new Stats();
        stat.setCorrectAnswers(10);
        stat.setGotPsychedCount(10);
        stat.setPsychedOthersCount(10);

        player.setStats(stat);
        playerRepository.save(player);
        game.getPlayers().remove(player);
        gameRepository.save(game);

        return "Game left - Stats Updated";
    }

    // selectAnswer - pid, gid, answer-id
    // check if the answer is right or not,
    // update the and the game stats
    // to detect if the game has ended, and to end the game.
    // when the game ends, update every players stats

    // getReady - pid, gid
}


// pragy@interviewbit.com

